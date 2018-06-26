package com.example.ashishrmehta.flickr;

/**
 * Created by ashish mehta on 27-06-2018.
 */

import android.os.Environment;

import java.io.File;

public class SdcardUtils
{

    public static String getSDCardPathWithFileSeparators()
    {
        return Environment.getExternalStorageDirectory().toString() + File.separator;
    }

}
