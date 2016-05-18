package com.joyplus.adkey.AdkeyUtil;

import android.graphics.Bitmap;

import com.joyplus.adkey.Ad;

import java.util.List;

/**
 * Created by UPC on 2016/5/18.
 */
public class BinnerModelAd implements Ad {
    private List<BinnerImage> ImageINFO;
    private List<String> mThridINFO;
    private int mType;

    public int getmType() {
        return mType;
    }

    public void setmType(int mType) {
        this.mType = mType;
    }

    public List<BinnerImage> getImageINFO() {
        return ImageINFO;
    }

    public void setImageINFO(List<BinnerImage> imageINFO) {
        ImageINFO = imageINFO;
    }

    public List<String> getmThridINFO() {
        return mThridINFO;
    }

    public void setmThridINFO(List<String> mThridINFO) {
        this.mThridINFO = mThridINFO;
    }

    @Override
    public int getType() {
        return mType;
    }

    @Override
    public void setType(int adType) {
        this.mType = adType;
    }

    public class BinnerImage{
        private Bitmap mBitmap;
        private String mCurrentTime;
        private String mImageID;

        public BinnerImage(Bitmap bitmap,String time,String id){
            this.mBitmap = bitmap;
            this.mCurrentTime = time;
            this.mImageID = id;
        }

        public Bitmap getmBitmap() {
            return mBitmap;
        }

        public void setmBitmap(Bitmap mBitmap) {
            this.mBitmap = mBitmap;
        }

        public String getmCurrentTime() {
            return mCurrentTime;
        }

        public void setmCurrentTime(String mCurrentTime) {
            this.mCurrentTime = mCurrentTime;
        }

        public String getmImageID() {
            return mImageID;
        }

        public void setmImageID(String mImageID) {
            this.mImageID = mImageID;
        }
    }

}
