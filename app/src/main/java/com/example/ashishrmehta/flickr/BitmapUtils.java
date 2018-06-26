package com.example.ashishrmehta.flickr;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtils {

	public static void saveBitmapToSDCard(File f, Bitmap bitmap) throws IOException {
		saveBitmapToSDCard(f, bitmap, Bitmap.CompressFormat.JPEG);
	}

	public static void saveBitmapToSDCard(File f, Bitmap mBitmap, Bitmap.CompressFormat imageType) throws IOException {

		if(f==null||mBitmap==null)
			return;

		if (f.exists()) {
			f.delete();
		}
		else {
			f.createNewFile();
		}
		FileOutputStream fOut = null;
		fOut = new FileOutputStream(f);
		boolean bISCompressed = false;
		bISCompressed = mBitmap.compress(imageType, 100, fOut);
		if(bISCompressed == false)
			throw new IOException();

		fOut.flush();
		fOut.close();
	}
}