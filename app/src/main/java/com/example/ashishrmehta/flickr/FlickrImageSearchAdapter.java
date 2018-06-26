package com.example.ashishrmehta.flickr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ashishrmehta.json.Photo;
import com.example.ashishrmehta.threadpool.RunnableNew;
import com.example.ashishrmehta.threadpool.ThreadPoolWrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FlickrImageSearchAdapter extends BaseAdapter {

	private Context context;
	int[] mFlag = new int[99];
	ImageView mImgView;
	int start = 0;
	int count = 0;
	private Button mButtn;
	private TextView mTexView;

	private static List<Photo> mflickrImageList = new ArrayList<Photo>();
	private HashMap<String, ViewHolder> mImageViewMap = new HashMap<String, ViewHolder>();
	private static HashMap<String, Bitmap> mImageViewBmp = new HashMap<String, Bitmap>();
	private HashMap<Integer, View> mView = new HashMap<Integer, View>();
	private LayoutInflater mInflator;
	private int mWidth;
	private int lastPos = 0;
	private int randPos = -1;

	public void resetAdapter() {
		for(int i = 0; i< 99; i++) {
			mFlag[i] = 0;
		}
		start = 0;
		count = 0;
		lastPos = 0;
		randPos = -1;
		mflickrImageList.clear();
		mImageViewBmp.clear();
		mImageViewMap.clear();
		mView.clear();
		mButtn.setVisibility(View.INVISIBLE);
		mTexView.setVisibility(View.VISIBLE);
	}

	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			ViewHolder viewHolder = null;
			Bitmap bmp = (Bitmap) msg.obj;
			Bundle data = msg.getData();
			String id = (String) data.get("idVal");
			viewHolder = mImageViewMap.get(id);
			if(mImageViewBmp.get(id) == null) {
				mImageViewBmp.put(id, bmp);
			}
			viewHolder.img.setImageBitmap(bmp);
			viewHolder.bool = 1;
		}
	};

	public Handler getHandler() {
		return mHandler;
	}

	public void setImageView(ImageView mImageView) {
		mImgView = mImageView;
	}

	public void setButtonView(Button mButton) {
		mButtn =  mButton;
	}

	public void setTextView(TextView mTextView) {
		mTexView = mTextView;
	}

	public static Bitmap getBitMap(String pic_id) {
		return mImageViewBmp.get(pic_id);
	}

	public static String getImageDetails(String pic_id) {
		for(Photo pic : mflickrImageList) {
			if(pic.getId().equals(pic_id)) {
				return pic.getOwner() + "\n" + pic.getTitle();
			}
		}
		return null;
	}

	public FlickrImageSearchAdapter(Context ctx, int width) {
		context = ctx;
		mInflator = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mWidth = width;
	}

	public void setData(List<Photo> flickrImageList) {
		mflickrImageList = flickrImageList;
	}

	@Override
	public int getCount() {
		return mflickrImageList.size();
	}

	@Override
	public Photo getItem(int position) {
		return mflickrImageList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		final Photo pic = getItem(position);
		convertView = mView.get(position);
		if (convertView == null) {
			convertView = mInflator.inflate(R.layout.grid_item_flickr_image_list, parent, false);
			holder = new ViewHolder();
			holder.position = position;
			convertView.setLayoutParams(new GridView.LayoutParams(mWidth, mWidth));
			holder.img = (ImageView) convertView.findViewById(R.id.img);
			convertView.setTag(holder);
			mView.put(position, convertView);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		try{
			if(mImageViewMap.get(pic.getId()) == null) {
				mImageViewMap.put(pic.getId(), holder);
				String url = "http://farm" + pic.getFarm() + ".static.flickr.com/" +
						pic.getServer() + "/" + pic.getId() + "_" + pic.getSecret() + ".jpg";
				RunnableNew run = new RunnableNew(url, pic.getId(), this);
				ThreadPoolWrap.getThreadPool().execute(run);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(mFlag[position] == 1) {
			holder.img.setImageBitmap(mImageViewBmp.get(pic.getId()));
			holder.img.setVisibility(View.VISIBLE);
		}

		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent in = new Intent(context, MainViewActivity.class);
				in.putExtra("PIC_ID", pic.getId());
				context.startActivity(in);
			}
		});

		return convertView;
	}

	public class ViewHolder {
		public ImageView img;
		public int bool = 0;
		public int position;
	}
}
