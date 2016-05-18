package com.joyplus.adkey.AdkeyUtil;

/**
 * Created by UPC on 2016/5/10.
 */
public class AdkeyData {
    private static String impretion = "";
    private static String mMac = "";
    private static boolean DeBug = false;
    private static String Hurl = "";
    private static String SN = "";

    public static String getSDKVersion() {
        return SDKVersion;
    }

    public static void setSDKVersion(String SDKVersion) {
        AdkeyData.SDKVersion = SDKVersion;
    }

    private static String SDKVersion = "1.0.0";//2016_05_10

    public static boolean isDeBug() {
        return DeBug;
    }

    public static void setDeBug(boolean deBug) {
        DeBug = deBug;
    }

    public static String getmMac() {
        return mMac;
    }

    public static void setmMac(String mMac) {
        AdkeyData.mMac = mMac;
    }

    public static void setImpretion(String url) {
        impretion = url;
    }

    public static String getImpretion() {
        return impretion;
    }

    public static String getHurl() {
        return Hurl;
    }

    public static void setHurl(String hurl) {
        Hurl = hurl;
    }

    public static String getSN() {
        return SN;
    }

    public static void setSN(String SN) {
        AdkeyData.SN = SN;
    }
}
