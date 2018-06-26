package com.example.ashishrmehta.flickr;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ashishrmehta.json.FlickrClass;
import com.example.ashishrmehta.json.Photo;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
public class FlickrImageSearchActivity extends ActionBarActivity {
	private final static int GRID_MARGIN = 10;
	private final static int GRID_HORIZONTALSPACING = 10;
	private final static int GRID_VERTICALSPACING = 10;
	private static final String FLICK_URL =
			"https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=3e7cc266ae2b0e0d78e279ce8e361736&\n" +
					"format=json&nojsoncallback=1&safe_search=1&text=%s&page=%d";
	private static final String FILE_PROVIDER_AUTHORITY = "Flickr.files";
	private GroupMemberGridView gvImageList;
	protected CustomSearchView mSearchView;
	private FlickrImageSearchAdapter mAdapter;
	private List<Photo> mFlickrSearchResults;
	private SearchAsyncTask mSearchTask;

	private String mGroupName = "";
	private Uri mImageOutUri;
	private int offset = 1;
	private int mNumOfResults = 100;
	private TextView txtLoadMore;
	private TextView txtEmptyView;
	private View mFooter;
	private ProgressBar mProgressBar;
	private RelativeLayout mSearchLayout;
	private boolean mShowLoadMore = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		DirectoryBuilder.createDir();
		super.onCreate(savedInstanceState);
		setContentView(getLayoutId());
		findViewById();
		initData(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	protected int getLayoutId() {

		return R.layout.activity_flickr_image_search;
	}

	protected void findViewById()
		{

			DisplayMetrics metrics = getResources().getDisplayMetrics();
			int width = metrics.widthPixels;
			int margin = DipPixUtil.dip2px(this, GRID_MARGIN);
			int horizontalSpacing = DipPixUtil.dip2px(this, GRID_HORIZONTALSPACING);
			int verticalSpacing = DipPixUtil.dip2px(this, GRID_VERTICALSPACING);
			int gridWidth = (width - margin * 2 - horizontalSpacing * 2) / 3;

			gvImageList = (GroupMemberGridView) findViewById(R.id.image_list_gridview);
			txtEmptyView = (TextView) findViewById(R.id.txtEmptyView);

			mFooter = getLayoutInflater().inflate(R.layout.layout_load_more, null);
			txtLoadMore = (TextView) mFooter.findViewById(R.id.txtLoadMore);
			mProgressBar = (ProgressBar) mFooter.findViewById(R.id.progressBar);

			txtEmptyView.setGravity(Gravity.CENTER);

			gvImageList.setEmptyView(txtEmptyView);
			gvImageList.setHorizontalSpacing(horizontalSpacing);
			gvImageList.setVerticalSpacing(verticalSpacing);
			gvImageList.setPadding(margin, margin, margin, margin);
			gvImageList.addFooterView(mFooter, null, false);

			mAdapter = new FlickrImageSearchAdapter(this, gridWidth);

			txtLoadMore.setText("Load More");
			txtLoadMore.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.INVISIBLE);

			gvImageList.setAdapter(mAdapter);

			showSearchForFlickr(true);
			prepareForFlickrSearch();
			mSearchView.getQueryTextView().setTextColor(getResources().getColor(R.color.search_view_text_color));
		}

	private void prepareForFlickrSearch() {
		mSearchView.makeSearchUIForFlickr();
	}

	private void showSearchForFlickr(boolean isSearchableTitle) {
		if(mSearchView == null){
			initSearchBar();
		}

		mSearchView.makeSearchUIForFlickr();

		if (isSearchableTitle) {
			mSearchLayout.setVisibility(View.VISIBLE);

			InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null) {
				imm.showSoftInput(mSearchView.findFocus(), 0);
			}

		}
		else {
			mSearchLayout.setVisibility(View.GONE);
			mSearchView.getQueryTextView().setText("");
		}

		mSearchView.getSearchBackButton().setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

			}
		});

		mSearchView.clearFocus();
	}

	private void initSearchBar(){
		mSearchLayout = ((RelativeLayout) findViewById(R.id.search_layout));
		mSearchView = (CustomSearchView) findViewById(R.id.search_view);
	}

	protected void initData(Bundle savedInstanceState) {

		mFlickrSearchResults = new ArrayList<Photo>();

		gvImageList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (isConnected() && mAdapter.getCount() > position) {
					Photo ph = mAdapter.getItem(position);
					String url = "http://farm" + ph.getFarm() + ".static.flickr.com/" +
							ph.getServer() + "/" + ph.getId() + "_" + ph.getSecret() + ".jpg";
					new DownloadAsyncTask(url).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
				else {
					ToastUtils.showShortToast(FlickrImageSearchActivity.this, "Internet Not Available");
				}
			}
		});

		mSearchView.getSearchHintButton().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		txtLoadMore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean flag = isConnected();
				if (!flag) {
					ToastUtils.showShortToast(FlickrImageSearchActivity.this, "Internet Not Available");
					return;

				}
				mSearchTask = new SearchAsyncTask(mSearchView.getQueryTextView().getText().toString());
				mSearchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

			}
		});

		mSearchView.getQueryTextView().setOnEditorActionListener(new EditText.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					View view = getCurrentFocus();
					if (view != null) {
						InputMethodUtil.hideInputMethod(view);
						view.clearFocus();
					}

					if (isConnected()) {
						txtLoadMore.setVisibility(View.GONE);
						mSearchTask = new SearchAsyncTask(mSearchView.getQueryTextView().getText().toString());
						mSearchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						mFlickrSearchResults.clear();
						offset = 1;
					}
					else {
						ToastUtils.showShortToast(FlickrImageSearchActivity.this, "Internet Not Available");
					}
					return true;
				}
				return false;
			}
		});

		mSearchView.getQueryTextView().setText(mGroupName);
		mSearchView.getQueryTextView().setSelection(mSearchView.getQueryTextView().getText().length());
		if (mGroupName != null && mGroupName.length() > 0 && isConnected()) {
			mSearchTask = new SearchAsyncTask(mGroupName);
			mSearchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	public class DownloadAsyncTask extends AsyncTask<Void, Void, Boolean> {
		private String mUrl;
		private ProgressDialog dialog;
		private String mImagePath;

		public DownloadAsyncTask(String url) {
			mUrl = url;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(FlickrImageSearchActivity.this);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setCancelable(false);
			dialog.show();
			dialog.setContentView(R.layout.layout_progress_bar);
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			try {
				File file = new File(DirectoryBuilder.DIR_IMAGE + ImageCache.hashKeyForDisk(mUrl)+".jpg");
				if (!file.exists()) {
					mImagePath = file.getPath();
					Bitmap bmp = FileUtils.downloadBitmap(file, mUrl, null);
					BitmapUtils.saveBitmapToSDCard(file, bmp);
				}
				else {
					mImagePath = file.getPath();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			dialog.dismiss();
			if (!result) {
				ToastUtils.showShortToast(FlickrImageSearchActivity.this, "Loading Failed");
				return;
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	private boolean isConnected() {
		return isNetworkAvailable(this);
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager != null) {
			NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
			if (activeNetworkInfo != null && activeNetworkInfo.isConnected())
				return true;
		}
		return false;
	}


	public class SearchAsyncTask extends AsyncTask<Void, Void, Boolean> {
		private final static String USER_AGENT = "Mozilla/5.0";
		private String mSearchStr;
		private ProgressDialog dialog;

		public SearchAsyncTask(String searchStr) {
			mSearchStr = searchStr;
		}

		private String sendGet(String url) throws Exception {

			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			return response.toString();

		}

		@SuppressLint("NewApi")
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(FlickrImageSearchActivity.this);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setCancelable(false);
			dialog.show();
			dialog.setContentView(R.layout.layout_progress_bar);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				String res1 = null;

				String searchStr = URLEncoder.encode(mSearchStr, "UTF-8");
				byte[] utf8Bytes = searchStr.getBytes("UTF-8");
				searchStr = new String(utf8Bytes, "UTF8");

				String flickr_url;
				flickr_url = String.format(FLICK_URL, searchStr, offset);


				res1 = sendGet(flickr_url);

				Gson gson = new Gson();
				FlickrClass flc = gson.fromJson(res1, FlickrClass.class);

				mFlickrSearchResults = flc.getPhotos().getPhoto().subList(0,99);
				if (mFlickrSearchResults.size() == mNumOfResults - 1) {
					offset = offset + 1;
					mShowLoadMore = true;
				}
				else {
					mShowLoadMore = false;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}

			return true;
		}

		@SuppressLint({ "NewApi", "ShowToast" })
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (dialog != null)
				dialog.dismiss();

			if (!result) {
				ToastUtils.showShortToast(FlickrImageSearchActivity.this, "Loading Failed");
				return;
			}
			mAdapter.setData(mFlickrSearchResults);
			mAdapter.notifyDataSetChanged();

			if (mShowLoadMore) {
				mFooter.setVisibility(View.VISIBLE);
				txtLoadMore.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.INVISIBLE);
			}
			else {
				mFooter.setVisibility(View.GONE);
				txtLoadMore.setVisibility(View.GONE);
				mProgressBar.setVisibility(View.INVISIBLE);
			}

			if (mFlickrSearchResults.size() == 0) {
				txtEmptyView.setText(String.format("No Search Results for ", mSearchStr));
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mSearchTask != null && !mSearchTask.isCancelled()) {
			mSearchTask.cancel(true);
		}
	}
}
