package com.example.ashishrmehta.flickr;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;


/**
 * pixel util
 *
 */
public class DipPixUtil {

    /**
     * dip to px
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * px to dip
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int getDpi(Context context) {
        return context.getResources().getDisplayMetrics().densityDpi;
    }

    public static float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static int getWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static class DisplayRect {
        private int width;

        private int height;

        public DisplayRect(int width, int height){
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }
        public void setWidth(int width) {
            this.width = width;
        }
        public int getHeight() {
            return height;
        }
        public void setHeight(int height) {
            this.height = height;
        }

    }

    @SuppressWarnings("deprecation")
    public static DisplayRect getWindowDiaplay(Context context) {
        Display display = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayRect rect = new DisplayRect(display.getWidth(), display.getHeight());
        return rect;
    }


}
