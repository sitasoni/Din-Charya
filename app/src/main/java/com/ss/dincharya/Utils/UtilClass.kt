package com.ss.dincharya.Utils

import android.app.Dialog
import android.content.Context
import android.view.ViewGroup

class UtilClass() {

    companion object {
        fun setCustomParams(ctx : Context,dialog: Dialog,widthMargin : Int = 10) {
            // ViewGroup.LayoutParams.WRAP_CONTENT // default wrap

            val displayMetrics =  ctx.resources.displayMetrics
            var screenWidth = displayMetrics.widthPixels
            var screenHeight = displayMetrics.heightPixels

            val maxWidthPixel = (widthMargin * displayMetrics.density).toInt();
//            val maxHeightPixel = (heightMargin * displayMetrics.density).toInt();

            screenWidth -= maxWidthPixel;
//            screenHeight -= maxHeightPixel;

            dialog.window?.setLayout(screenWidth,ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}