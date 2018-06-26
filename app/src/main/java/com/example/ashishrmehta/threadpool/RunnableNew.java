package com.example.ashishrmehta.threadpool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;

import com.example.ashishrmehta.flickr.FlickrImageSearchAdapter;

import java.net.URL;
import java.util.concurrent.Callable;

import static com.example.ashishrmehta.flickr.FlickrImageSearchAdapter.*;

public class RunnableNew implements Runnable {

    String _url;
    String _id;
    FlickrImageSearchAdapter _fISA;

    public RunnableNew(String url, String id, FlickrImageSearchAdapter fISA) {
        {        	
            _url = url;
            _id = id;
            _fISA = fISA;
        }
    }

    @Override
    public void run() {
        Bitmap bmp = null;
        try {
            URL url = new URL(_url);
            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            Message msg = new Message();
            msg.obj = bmp;
            Bundle data = new Bundle();
            data.putString("idVal", _id);
            msg.setData(data);
            _fISA.getHandler().sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }
}

