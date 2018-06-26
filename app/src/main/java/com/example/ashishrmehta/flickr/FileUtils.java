package com.example.ashishrmehta.flickr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class FileUtils {
	private static final String TAG = FileUtils.class.getSimpleName();

	public static boolean prepareDir(String filePath) {
		if (!filePath.endsWith(File.separator)) {
			return false;
		}
		File file = new File(filePath);
		if (file.exists() || file.mkdirs()) {
			Log.d(TAG, "prepareDir_create folder:" + filePath + ",result:true");
			return true;
		}
		else {
			Log.d(TAG, "prepareDir_create folder:" + filePath + ",result:false");
			return false;
		}
	}

	public static boolean deleteFile(String path) {
		boolean delete = false;
		try {
			File file = new File(path);
			if (file.exists()) {
				delete = file.delete();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return delete;
	}

	public static void saveByteToFile(File f, byte[] buff) {
		FileOutputStream fOut = null;
		try {
			if (buff != null && buff.length != 0) {
				if (f.exists()) {
					f.delete();
				}
				f.getParentFile().mkdirs();
				f.createNewFile();
				fOut = new FileOutputStream(f);
				fOut.write(buff);
				fOut.flush();
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (fOut != null) {
					fOut.close();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static HostnameVerifier hostnameVerifier = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	public static Bitmap downloadBitmap(File file, String url, BitmapFactory.Options options) {
		URLConnection urlConnection =  getURLConnection(url);
		if (urlConnection != null) {
			try {
				InputStream in = urlConnection.getInputStream();
				byte[] bytes = readBytes(in);

				if (bytes != null) {
					FileUtils.saveByteToFile(file, bytes);
					if (options != null) {
						return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
					}

					return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
				}
			} catch (Exception e) {
			} finally {
				disconnectURLConnection(urlConnection);
			}
		}
		return null;
	}

	static URLConnection getURLConnection(String URL) {
		Log.d("FileUtils", "getURLConnection:: " + URL);
		try {
			URL url = new URL(URL);
			URLConnection urlConnection = url.openConnection();;
			urlConnection.setConnectTimeout(30000);
			urlConnection.setReadTimeout(30000);
			if (urlConnection instanceof HttpsURLConnection) {
				((HttpsURLConnection) urlConnection).setHostnameVerifier(hostnameVerifier);
			}
			return urlConnection;
		} catch (Exception e){
		}
		return null;
	}

	static void disconnectURLConnection(URLConnection urlConnection){
		if (urlConnection instanceof HttpsURLConnection) {
			((HttpsURLConnection) urlConnection).disconnect();
		} else if (urlConnection instanceof HttpURLConnection) {
			((HttpURLConnection) urlConnection).disconnect();
		}
	}

	public static byte[] readBytes(InputStream inputStream) throws IOException {
		// this dynamically extends to take the bytes you read
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

		// this is storage overwritten on each iteration with bytes
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];

		// we need to know how may bytes were read to write them to the byteBuffer
		int len = 0;
		while ((len = inputStream.read(buffer)) != -1) {
			byteBuffer.write(buffer, 0, len);
		}

		// and then we can return your byte array.
		return byteBuffer.toByteArray();
	}
}
	

