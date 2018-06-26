package com.example.ashishrmehta.flickr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DirectoryBuilder {

	public static final String ROOT_NAME = "Flickr";

	public static final String SDCARD_ROOT = SdcardUtils.getSDCardPathWithFileSeparators();

	public static final String RCS_ROOT = SDCARD_ROOT + ROOT_NAME + File.separator;
	public static final String DIR_IMAGE = RCS_ROOT + "image" + File.separator;

	public static void createDir() {
		FileUtils.prepareDir(DIR_IMAGE);
		deleteOldRootNoMedia();
		createAllNoMedia();
	}
	
	private static void createAllNoMedia(){
		createNomedia(DIR_IMAGE);
	}
	
	private static void deleteOldRootNoMedia() {
		FileUtils.deleteFile(RCS_ROOT + "/.nomedia");
	}

	public static void createNomedia(String filePath) {
		File file = new File(filePath + "/.nomedia");
		saveByteToFile(file, new byte[]{});
	}

	private static void saveByteToFile(File f, byte[] buff) {
		FileOutputStream fOut = null;
		try {
			if (f.exists()) {
				f.delete();
			}
			f.createNewFile();
			fOut = new FileOutputStream(f);
			fOut.write(buff);
			fOut.flush();
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
}