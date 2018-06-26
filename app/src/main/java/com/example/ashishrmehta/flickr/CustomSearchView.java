package com.example.ashishrmehta.flickr;

import android.app.SearchableInfo;
import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CustomSearchView extends LinearLayout {

    private OnQueryTextListener mOnQueryChangeListener;
    private OnCloseListener mOnCloseListener;
    private OnFocusChangeListener mOnQueryTextFocusChangeListener;
    private OnClickListener mOnSearchClickListener;

    private boolean mIconifiedByDefault;
    private boolean mIconified;
    private View mSearchButton;
    //	private View mSearchPlate;
    private ImageView mCloseButton;
    private View mSearchEditFrame;
    private EditText mQueryTextView;
    private ImageView mSearchHintIcon;
    private CharSequence mQueryHint;
    private boolean mQueryRefinement;
    private boolean mClearingFocus;
    private int mMaxWidth;
    private CharSequence mOldQueryText;
    //	private CharSequence mUserQuery;
    private boolean mExpandedInActionView;
    private int mCollapsedImeOptions;

    private SearchableInfo mSearchable;

    public void setTextColor(int resColorId) {
        mQueryTextView.setTextColor(resColorId);
    }

    public void setHintTextColor(int resColorId) {
        mQueryTextView.setHintTextColor(resColorId);
    }

    private Runnable mShowImeRunnable = new Runnable() {
        public void run() {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.showSoftInput(mQueryTextView, 0);
            }
        }
    };

    private Runnable mUpdateDrawableStateRunnable = new Runnable() {
        public void run() {
            updateFocusedState();
        }
    };

    public interface OnQueryTextListener {

        boolean onQueryTextSubmit(String query);

        boolean onQueryTextChange(String newText);
    }

    public interface OnCloseListener {

        boolean onClose();
    }

    public interface OnSuggestionListener {

        boolean onSuggestionSelect(int position);

        boolean onSuggestionClick(int position);
    }

    public CustomSearchView(Context context) {
        this(context, null);
    }

    public CustomSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_custom_search_view, this, true);

        mSearchButton = findViewById(R.id.search_button);
        mQueryTextView = (EditText) findViewById(R.id.search_src_text);

        mSearchEditFrame = findViewById(R.id.search_edit_frame);
//		mSearchPlate = findViewById(R.id.search_plate);
        mCloseButton = (ImageView) findViewById(R.id.search_close_btn);
        mSearchHintIcon = (ImageView) findViewById(R.id.search_mag_icon);

        mSearchButton.setOnClickListener(mOnClickListener);
        mCloseButton.setOnClickListener(mOnClickListener);
        mQueryTextView.setOnClickListener(mOnClickListener);

        mQueryTextView.addTextChangedListener(mTextWatcher);
        // Inform any listener of focus changes
        mQueryTextView.setOnFocusChangeListener(new OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                if (mOnQueryTextFocusChangeListener != null) {
                    mOnQueryTextFocusChangeListener.onFocusChange(CustomSearchView.this, hasFocus);
                }
            }
        });

        // setMaxWidth(maxWidth);
        setQueryHint("input something");
        // setImeOptions(imeOptions);
        // setInputType(inputType);

        boolean focusable = true;
        setFocusable(focusable);

        updateViewsVisibility(mIconifiedByDefault);
        updateQueryHint();
    }

    public void removeOnClickListener() {
        mSearchButton.setOnClickListener(null);
        mCloseButton.setOnClickListener(null);
        mQueryTextView.setOnClickListener(null);
    }

    public void setSearchOnClickListener(OnClickListener listener) {
        mSearchButton.setOnClickListener(listener);
        mCloseButton.setOnClickListener(listener);
        mQueryTextView.setOnClickListener(listener);
    }

    public void onActionViewCollapsed() {
        clearFocus();
        updateViewsVisibility(true);
        mQueryTextView.setImeOptions(mCollapsedImeOptions);
        mExpandedInActionView = false;
    }

    public void onActionViewExpanded() {
        if (mExpandedInActionView)
            return;

        mExpandedInActionView = true;
        mCollapsedImeOptions = mQueryTextView.getImeOptions();
        mQueryTextView.setImeOptions(mCollapsedImeOptions);
        mQueryTextView.setText("");
        setIconified(false);
    }

    /**
     * Sets the SearchableInfo for this SearchView. Properties in the SearchableInfo are used to display labels, hints, suggestions, create intents for launching search results screens and controlling other affordances such as a voice button.
     *
     * @param searchable
     *            a SearchableInfo can be retrieved from the SearchManager, for a specific activity or a global search provider.
     */
    public void setSearchableInfo(SearchableInfo searchable) {
        mSearchable = searchable;
        if (mSearchable != null) {
            updateQueryHint();
        }
        updateViewsVisibility(isIconified());
    }

    /**
     * Sets the IME options on the query text field.
     *
     * @see TextView#setImeOptions(int)
     * @param imeOptions
     *            the options to set on the query text field
     *
     * @attr ref android.R.styleable#SearchView_imeOptions
     */
    public void setImeOptions(int imeOptions) {
        mQueryTextView.setImeOptions(imeOptions);
    }

    /**
     * Returns the IME options set on the query text field.
     *
     * @return the ime options
     * @see TextView#setImeOptions(int)
     *
     * @attr ref android.R.styleable#SearchView_imeOptions
     */
    public int getImeOptions() {
        return mQueryTextView.getImeOptions();
    }

    /**
     * Sets the input type on the query text field.
     *
     * @see TextView#setInputType(int)
     * @param inputType
     *            the input type to set on the query text field
     *
     * @attr ref android.R.styleable#SearchView_inputType
     */
    public void setInputType(int inputType) {
        mQueryTextView.setInputType(inputType);
    }

    /**
     * Returns the input type set on the query text field.
     *
     * @return the input type
     *
     * @attr ref android.R.styleable#SearchView_inputType
     */
    public int getInputType() {
        return mQueryTextView.getInputType();
    }

    /** @hide */
    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        // Don't accept focus if in the middle of clearing focus
        if (mClearingFocus)
            return false;
        // Check if SearchView is focusable.
        if (!isFocusable())
            return false;
        // If it is not iconified, then give the focus to the text field
        if (!isIconified()) {
            boolean result = mQueryTextView.requestFocus(direction, previouslyFocusedRect);
            if (result) {
                updateViewsVisibility(false);
            }
            return result;
        }
        else {
            return super.requestFocus(direction, previouslyFocusedRect);
        }
    }

    /** @hide */
    @Override
    public void clearFocus() {
        mClearingFocus = true;
        setImeVisibility(false);
        super.clearFocus();
        mQueryTextView.clearFocus();
        mClearingFocus = false;
    }

    /**
     * Sets a listener for user actions within the SearchView.
     *
     * @param listener
     *            the listener object that receives callbacks when the user performs actions in the SearchView such as clicking on buttons or typing a query.
     */
    public void setOnQueryTextListener(OnQueryTextListener listener) {
        mOnQueryChangeListener = listener;
    }

    /**
     * Sets a listener to inform when the user closes the SearchView.
     *
     * @param listener
     *            the listener to call when the user closes the SearchView.
     */
    public void setOnCloseListener(OnCloseListener listener) {
        mOnCloseListener = listener;
    }

    /**
     * Sets a listener to inform when the focus of the query text field changes.
     *
     * @param listener
     *            the listener to inform of focus changes.
     */
    public void setOnQueryTextFocusChangeListener(OnFocusChangeListener listener) {
        mOnQueryTextFocusChangeListener = listener;
    }

    /**
     * Sets a listener to inform when the search button is pressed. This is only relevant when the text field is not visible by default. Calling {@link #setIconified setIconified(false)} can also cause this listener to be informed.
     *
     * @param listener
     *            the listener to inform when the search button is clicked or the text field is programmatically de-iconified.
     */
    public void setOnSearchClickListener(OnClickListener listener) {
        mOnSearchClickListener = listener;
    }

    /**
     * Returns the query string currently in the text field.
     *
     * @return the query string
     */
    public CharSequence getQuery() {
        return mQueryTextView.getText();
    }

    /**
     * Sets a query string in the text field and optionally submits the query as well.
     *
     * @param query
     *            the query string. This replaces any query text already present in the text field.
     * @param submit
     *            whether to submit the query right now or only update the contents of text field.
     */
    public void setQuery(CharSequence query, boolean submit) {
        mQueryTextView.setText(query);
        if (query != null) {
            mQueryTextView.setSelection(mQueryTextView.length());
//			mUserQuery = query;
        }
    }

    /**
     * Sets the hint text to display in the query text field. This overrides any hint specified in the SearchableInfo.
     *
     * @param hint
     *            the hint text to display
     *
     * @attr ref android.R.styleable#SearchView_queryHint
     */
    public void setQueryHint(CharSequence hint) {
        mQueryHint = hint;
        updateQueryHint();
    }

    /**
     * Gets the hint text to display in the query text field.
     *
     * @return the query hint text, if specified, null otherwise.
     *
     * @attr ref android.R.styleable#SearchView_queryHint
     */
    public CharSequence getQueryHint() {
        if (mQueryHint != null) {
            return mQueryHint;
        }
        else if (mSearchable != null) {
            CharSequence hint = null;
            int hintId = mSearchable.getHintId();
            if (hintId != 0) {
                hint = getContext().getString(hintId);
            }
            return hint;
        }
        return null;
    }

    /**
     * Sets the default or resting state of the search field. If true, a single search icon is shown by default and expands to show the text field and other buttons when pressed. Also, if the default state is iconified, then it collapses to that state when the close button is pressed. Changes to this property will take effect immediately.
     *
     * <p>
     * The default value is true.
     * </p>
     *
     * @param iconified
     *            whether the search field should be iconified by default
     *
     * @attr ref android.R.styleable#SearchView_iconifiedByDefault
     */
    public void setIconifiedByDefault(boolean iconified) {
        if (mIconifiedByDefault == iconified)
            return;
        mIconifiedByDefault = iconified;
        updateViewsVisibility(iconified);
        updateQueryHint();
    }

    /**
     * Returns the default iconified state of the search field.
     *
     * @return
     *
     * @attr ref android.R.styleable#SearchView_iconifiedByDefault
     */
    public boolean isIconfiedByDefault() {
        return mIconifiedByDefault;
    }

    /**
     * Iconifies or expands the SearchView. Any query text is cleared when iconified. This is a temporary state and does not override the default iconified state set by {@link #setIconifiedByDefault(boolean)}. If the default state is iconified, then a false here will only be valid until the user closes the field. And if the default state is expanded, then a true here will only clear the text field and not close it.
     *
     * @param iconify
     *            a true value will collapse the SearchView to an icon, while a false will expand it.
     */
    public void setIconified(boolean iconify) {
        if (iconify) {
            onCloseClicked();
        }
        else {
            onSearchClicked();
        }
    }

    /**
     * Returns the current iconified state of the SearchView.
     *
     * @return true if the SearchView is currently iconified, false if the search field is fully visible.
     */
    public boolean isIconified() {
        return mIconified;
    }

    /**
     * Returns whether query refinement is enabled for all items or only specific ones.
     *
     * @return true if enabled for all items, false otherwise.
     */
    public boolean isQueryRefinementEnabled() {
        return mQueryRefinement;
    }

    /**
     * Makes the view at most this many pixels wide
     *
     * @attr ref android.R.styleable#SearchView_maxWidth
     */
    public void setMaxWidth(int maxpixels) {
        mMaxWidth = maxpixels;

        requestLayout();
    }

    /**
     * Gets the specified maximum width in pixels, if set. Returns zero if no maximum width was specified.
     *
     * @return the maximum width of the view
     *
     * @attr ref android.R.styleable#SearchView_maxWidth
     */
    public int getMaxWidth() {
        return mMaxWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Let the standard measurements take effect in iconified state.
        if (isIconified()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        switch (widthMode) {
            case MeasureSpec.AT_MOST:
                // If there is an upper limit, don't exceed maximum width (explicit or implicit)
                if (mMaxWidth > 0) {
                    width = Math.min(mMaxWidth, width);
                }
                else {
                    width = Math.min(getPreferredWidth(), width);
                }
                break;
            case MeasureSpec.EXACTLY:
                // If an exact width is specified, still don't exceed any specified maximum width
                if (mMaxWidth > 0) {
                    width = Math.min(mMaxWidth, width);
                }
                break;
            case MeasureSpec.UNSPECIFIED:
                // Use maximum width, if specified, else preferred width
                width = mMaxWidth > 0 ? mMaxWidth : getPreferredWidth();
                break;
        }
        widthMode = MeasureSpec.EXACTLY;
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, widthMode), heightMeasureSpec);
    }

    private int getPreferredWidth() {
        return 300;
    }

    private void updateViewsVisibility(final boolean collapsed) {
        mIconified = collapsed;
        // Visibility of views that are visible when collapsed
        final int visCollapsed = collapsed ? VISIBLE : GONE;
        // Is there text in the query
//		final boolean hasText = !TextUtils.isEmpty(mQueryTextView.getText());

        mSearchButton.setVisibility(visCollapsed);
        mSearchEditFrame.setVisibility(collapsed ? GONE : VISIBLE);
        mSearchHintIcon.setVisibility(mIconifiedByDefault ? GONE : VISIBLE);
        updateCloseButton();
    }

    private void updateCloseButton() {
        final boolean hasText = !TextUtils.isEmpty(mQueryTextView.getText());
        // Should we show the close button? It is not shown if there's no focus,
        // field is not iconified by default and there is no text in it.
//		final boolean showClose = hasText || (mIconifiedByDefault && !mExpandedInActionView);
        final boolean showClose = hasText;
        mCloseButton.setVisibility(showClose ? VISIBLE : GONE);
        mCloseButton.getDrawable().setState(hasText ? ENABLED_STATE_SET : EMPTY_STATE_SET);
    }

    private void postUpdateFocusedState() {
        post(mUpdateDrawableStateRunnable);
    }

    private void updateFocusedState() {
        // boolean focused = mQueryTextView.hasFocus();
        // mSearchPlate.getBackground().setState(focused ? FOCUSED_STATE_SET : EMPTY_STATE_SET);
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(mUpdateDrawableStateRunnable);
        super.onDetachedFromWindow();
    }

    private void setImeVisibility(final boolean visible) {
        if (visible) {
            post(mShowImeRunnable);
        }
        else {
            removeCallbacks(mShowImeRunnable);
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }

    /**
     * Called by the SuggestionsAdapter
     *
     * @hide
     */
	/* package */void onQueryRefine(CharSequence queryText) {
        setQuery(queryText);
    }

    private final OnClickListener mOnClickListener = new OnClickListener() {

        public void onClick(View v) {
            if (v == mSearchButton) {
                onSearchClicked();
            }
            else if (v == mCloseButton) {
                onCloseClicked();
            }
        }
    };

    /**
     * Handles the key down event for dealing with action keys.
     *
     * @param keyCode
     *            This is the keycode of the typed key, and is the same value as found in the KeyEvent parameter.
     * @param event
     *            The complete event record for the typed key
     *
     * @return true if the event was handled here, or false if not.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mSearchable == null) {
            return false;
        }

        return super.onKeyDown(keyCode, event);
    }

    private CharSequence getDecoratedHint(CharSequence hintText) {
        // If the field is always expanded, then don't add the search icon to the hint
        if (!mIconifiedByDefault)
            return hintText;

//		SpannableStringBuilder ssb = new SpannableStringBuilder("   "); // for the icon
//		ssb.append(hintText);
//		Drawable searchIcon = getContext().getResources().getDrawable(R.drawable.search_view_search_icon);
//		int textSize = (int) (mQueryTextView.getTextSize() * 1.25);
//		searchIcon.setBounds(0, 0, textSize, textSize);
//		ssb.setSpan(new ImageSpan(searchIcon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//		return ssb;
        return hintText;
    }

    private void updateQueryHint() {
        if (mQueryHint != null) {
            mQueryTextView.setHint(getDecoratedHint(mQueryHint));
        }
        else if (mSearchable != null) {
            CharSequence hint = null;
            int hintId = mSearchable.getHintId();
            if (hintId != 0) {
                hint = getContext().getString(hintId);
            }
            if (hint != null) {
                mQueryTextView.setHint(getDecoratedHint(hint));
            }
        }
        else {
            mQueryTextView.setHint(getDecoratedHint(""));
        }
    }

    private void onTextChanged(CharSequence newText) {
//		CharSequence text = mQueryTextView.getText();
//		mUserQuery = text;
        updateCloseButton();
        if (mOnQueryChangeListener != null && !TextUtils.equals(newText, mOldQueryText)) {
            mOnQueryChangeListener.onQueryTextChange(newText.toString());
        }
        mOldQueryText = newText.toString();
//		if(TextUtils.isEmpty(mOldQueryText)) {
//			mCloseButton.setVisibility(View.GONE);
//		} else {
//			mCloseButton.setVisibility(View.VISIBLE);
//		}
    }

    private void onCloseClicked() {
        CharSequence text = mQueryTextView.getText();
        if (TextUtils.isEmpty(text)) {
            if (mIconifiedByDefault) {
                // If the app doesn't override the close behavior
                if (mOnCloseListener == null || !mOnCloseListener.onClose()) {
                    // hide the keyboard and remove focus
                    clearFocus();
                    // collapse the search field
                    updateViewsVisibility(true);
                }
            }
        }
        else {
            mQueryTextView.setText("");
            mQueryTextView.requestFocus();
            setImeVisibility(true);
        }

    }

    private void onSearchClicked() {
        updateViewsVisibility(false);
        mQueryTextView.requestFocus();
        setImeVisibility(true);
        if (mOnSearchClickListener != null) {
            mOnSearchClickListener.onClick(this);
        }
    }

    void onTextFocusChanged() {
        updateViewsVisibility(isIconified());
        // Delayed update to make sure that the focus has settled down and window focus changes
        // don't affect it. A synchronous update was not working.
        postUpdateFocusedState();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        postUpdateFocusedState();
    }

    /**
     * Sets the text in the query box, without updating the suggestions.
     */
    private void setQuery(CharSequence query) {
        // mQueryTextView.setText(query, true);
        mQueryTextView.setText(query);
        // Move the cursor to the end
        mQueryTextView.setSelection(TextUtils.isEmpty(query) ? 0 : query.length());
    }

    /**
     * Callback to watch the text field for empty/non-empty
     */
    private TextWatcher mTextWatcher = new TextWatcher() {

        public void beforeTextChanged(CharSequence s, int start, int before, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int after) {
            CustomSearchView.this.onTextChanged(s);
        }

        public void afterTextChanged(Editable s) {
        }
    };

    public EditText getQueryTextView() {
        return mQueryTextView;
    }

    public void makeSearchUI(){
        ImageView magnifyIcon = (ImageView) findViewById(R.id.search_magnify_icon);
        magnifyIcon.setVisibility(View.GONE);
        ((ImageView) mSearchButton).setImageDrawable(getResources().getDrawable(R.drawable.icon_back_selector));
        int padding = DipPixUtil.dip2px(getContext(), 12);
        mSearchButton.setPadding(padding, padding, padding, padding);
        mSearchButton.setVisibility(View.VISIBLE);
        ((LayoutParams) mSearchButton.getLayoutParams()).height = DipPixUtil.dip2px(getContext(), 44);
        ((LayoutParams) mSearchButton.getLayoutParams()).width = DipPixUtil.dip2px(getContext(), 44);
        setQueryHint(getResources().getString(R.string.general_search));
        mSearchHintIcon.setVisibility(View.GONE);
        int margin = DipPixUtil.dip2px(getContext(), 5);
        ((LayoutParams) mSearchEditFrame.getLayoutParams()).setMargins(0, margin, margin, margin);
        invalidate();
    }

    public ImageView getSearchBackButton() {
        return (ImageView) mSearchButton;
    }

    public void makeSearchUIForFlickr(){
        ImageView magnifyIcon = (ImageView) findViewById(R.id.search_magnify_icon);
        magnifyIcon.setVisibility(View.GONE);
        ((ImageView) mSearchHintIcon).setImageDrawable(getResources().getDrawable(R.drawable.icon_back_selector));
        int padding = DipPixUtil.dip2px(getContext(), 12);
        mSearchButton.setPadding(padding, padding, padding, padding);
        mSearchButton.setVisibility(View.GONE);
        setQueryHint(getResources().getString(R.string.general_search));
        ((LayoutParams) mSearchHintIcon.getLayoutParams()).height = DipPixUtil.dip2px(getContext(), 25);
        ((LayoutParams) mSearchHintIcon.getLayoutParams()).width = DipPixUtil.dip2px(getContext(), 25);
        mSearchHintIcon.setVisibility(View.VISIBLE);
        int margin = DipPixUtil.dip2px(getContext(), 5);
        ((LayoutParams) mSearchEditFrame.getLayoutParams()).setMargins(margin, margin, margin, margin);
        mCloseButton.setPressed(false);
        invalidate();
    }

    public ImageView getSearchHintButton() {
        return (ImageView) mSearchHintIcon;
    }
}
