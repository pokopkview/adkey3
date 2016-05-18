package com.joyplus.adkey.request;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

import com.joyplus.adkey.Ad;
import com.joyplus.adkey.AdkeyUtil.AdkeyData;
import com.joyplus.adkey.AdkeyUtil.BinnerModelAd;
import com.joyplus.adkey.BannerAd;
import com.joyplus.adkey.Monitorer.AdMonitorManager;
import com.joyplus.adkey.Monitorer.MD5Util;
import com.joyplus.adkey.Monitorer.TRACKINGURL;
import com.joyplus.adkey.Monitorer.TRACKINGURL.TYPE;
import com.joyplus.adkey.Util;
import com.joyplus.adkey.report.IReportCallBack;
import com.joyplus.adkey.video.RichMediaAd;
import com.joyplus.adkey.widget.Log;
import com.miaozhen.mzmonitor.MZMonitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Report {

    private IReportCallBack mCallback;
    private Context mContext;
    private int infocount = 0;
    private String result = "";
    private int[] count = {0};

    public Report(Context context) {
        mContext = context;
        //Countly.sharedInstance().init(mContext, AdKeyConfig.getInstance().getAdMasterConfig());
    }

    public void report(Ad ad) {
        if (ad == null) return;
        if (ad instanceof BannerAd) {
            reportBanner((BannerAd) ad);
        } else if (ad instanceof RichMediaAd) {
            reportMediaAd((RichMediaAd) ad);
        }
    }

    private void reportMediaAd(RichMediaAd ad) {
        // TODO Auto-generated method stub
        if (ad == null) return;
        List<TRACKINGURL> urls = ad.getmTrackingUrl();
        if (urls == null) urls = new ArrayList<TRACKINGURL>();
        TRACKINGURL joyplus = GetTRACKINGURL(ad.getmImpressionUrl(), TYPE.JOYPLUS);
        if (joyplus != null) urls.add(joyplus);
        if (urls != null && urls.size() > 0) {
            AdMonitorManager.getInstance(mContext).AddTRACKINGURL(urls);
        }
    }

    private void reportBanner(BannerAd ad) {
        // TODO Auto-generated method stub
        if (ad == null) return;
        List<TRACKINGURL> urls = ad.getmTrackingUrl();
        if (urls == null) urls = new ArrayList<TRACKINGURL>();
        TRACKINGURL joyplus = GetTRACKINGURL(ad.getmImpressionUrl(), TYPE.JOYPLUS);
        if (joyplus != null) urls.add(joyplus);
        if (urls != null && urls.size() > 0) {
            AdMonitorManager.getInstance(mContext).AddTRACKINGURL(urls);
        }
    }

    private TRACKINGURL GetTRACKINGURL(String url, TYPE type) {
        if (url == null || "".equals(url)) return null;
        TRACKINGURL URL = new TRACKINGURL();
        URL.URL = url;
        URL.Type = type;
        return URL;
    }

    public void reportClick(Ad ad) {
        //这里需要解耦，为了防止以后的上报类型越来越多
        if (ad == null) return;
        if (ad instanceof BannerAd) {
            reportBannerClick((BannerAd) ad);
        } else if (ad instanceof RichMediaAd) {
            reportMediaAdClick((RichMediaAd) ad);
        } else if (ad instanceof BinnerModelAd){

        }
    }


//    public void reportClick(Ad ad,String cals) {
//        if (ad == null) return;
//        if (ad instanceof BannerAd) {
//            reportBannerClick((BannerAd) ad);
//        } else if (ad instanceof RichMediaAd) {
//            reportMediaAdClick((RichMediaAd) ad);
//        } else if (ad instanceof BinnerModelAd){
//
//        }
//    }


    private void reportMediaAdClick(RichMediaAd ad) {
        // TODO Auto-generated method stub
        if (ad == null || ad.GetClick() == null || ad.GetClick().mClickURL == null || TextUtils.isEmpty(ad.GetClick().mClickURL.trim()))
            return;
        ReportClick(ad.GetClick().mClickURL.trim());
    }

    private void reportBannerClick(BannerAd ad) {
        // TODO Auto-generated method stub
        if (ad == null || ad.GetClick() == null || ad.GetClick().mClickURL == null || TextUtils.isEmpty(ad.GetClick().mClickURL.trim()))
            return;
        ReportClick(ad.GetClick().mClickURL.trim());
    }

    private void ReportClick(String ad) {
        if (ad == null || TextUtils.isEmpty(ad.trim())) return;
        TRACKINGURL joyplus = GetTRACKINGURL(ad, TYPE.JOYPLUS);
        if (joyplus != null) {
            AdMonitorManager.getInstance(mContext).AddTRACKINGURL(joyplus);
        }
    }

    public void reportBinner(String reportInfo, List<String> Info, IReportCallBack callBack) {
        if (reportInfo == null) {
            Log.e("adkey", "reportInfo null");
            return;
        } else {
            this.mCallback = callBack;
            int flag = 0;
            if (reportInfo.contains("&")) {
                String[] reportitem = reportInfo.split("&");
                infocount = reportitem.length;
                for (int i = 0; i < reportitem.length; i++) {
                    if (!reportitem[i].split(",")[0].equals("0")) {
                        report(reportitem[i]);
                        String time = reportitem[i].split(",")[1];
                        if(null != Info.get(flag)) {
                            for (int j = 0; j < (Info.get(flag).split(",")).length; j++) {
                                String link = ((Info.get(flag).split(","))[j]).replace("nt=__TIME__", "nt=" + time);
                                reportOTH(link);
                            }
                        }
                        flag += 1;
                    } else {
                        report(reportitem[i]);
                    }
                }

//				for (int i = 0; i < Info.size(); i++) {
//					String time = reportInfo.split("&")[i + 1].split(",")[1];
//
//					report(reportInfo.split("&")[i]);
//					for (int j = 0; j < Info.get(i).length; j++) {
//						String link = Info.get(i)[j].replace("nt=__TIME__", "nt=" + time);
//						String links = link.replace("m6a=__MAC__", "m6a=" + AdkeyData.getmMac());
//						if (AdkeyData.isDeBug()) {
//							Log.i("AdKeyRequest", links);
//						}
//						reportOTH(links);
//					}
//				}
            } else {
                report(reportInfo);
                if (Info != null) {
                    String time = reportInfo.split(",")[1];
                    for (int i = 0; i < (Info.get(0).split(",")).length; i++) {
                        String link = ((Info.get(0).split(","))[i]).replace("nt=__TIME__", "nt=" + time);
                        reportOTH(link);
                    }
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void report(final String info) {
        //TODO

        //System.out.println(info + "=====report");
        final String impretion = AdkeyData.getImpretion();
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    if (AdkeyData.isDeBug()) {
                        Log.i("AdKeyRequest", impretion + "==report");
                    }
//					URL url = new URL(impretion);
//					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//					connection.setRequestMethod("POST");
//					connection.setDoOutput(true);
//					connection.setDoInput(true);
//					connection.setRequestProperty("Content-type", "application/x-java-serialized-object");
//					connection.connect();
//					StringBuffer mParams = new StringBuffer();
//					mParams.append("reportinfo=" + info);
//					byte[] bypes = params.toString().getBytes();
//					connection.getOutputStream().write(bypes);
//					connection.getOutputStream().flush();
//					int requestCode = connection.getResponseCode();
//					if (AdkeyData.isDeBug()) {
//						Log.e("AdKeyRequest",requestCode + "--report");
//					}
//					if (requestCode == HttpURLConnection.HTTP_OK) {
//						return "0";
//					} else {
//						return info.split(",")[0];
//					}
                    if (impretion == null || impretion.equals("")) {
                        Log.e("AdkeySDK", "impressionurl was null");
                        return "0";
                    }
                    String joyplus = impretion.replace("i=", "i=" + MD5Util.GetMD5Code(Util.GetMacAddress(mContext).toUpperCase()));//将Mac添加到
                    URL url = new URL(joyplus + "&sn=" + AdkeyData.getSN());//添加SN到上报链接当中
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    // 设置通用的请求属性
                    httpURLConnection.setRequestProperty("accept", "*/*");
                    httpURLConnection.setRequestProperty("connection", "Keep-Alive");
                    httpURLConnection.setRequestMethod("POST");
//					httpURLConnection.setRequestProperty("Content-Length", String
//							.valueOf(info.length()));
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    // 获取URLConnection对象对应的输出流
                    PrintWriter printWriter = new PrintWriter(httpURLConnection.getOutputStream());
                    // 发送请求参数
                    printWriter.write(params.toString());
                    // flush输出流的缓冲
                    printWriter.flush();
                    printWriter.close();
                    // 根据ResponseCode判断连接是否成功
                    int responseCode = httpURLConnection.getResponseCode();
                    if (responseCode != 200) {
                        return "0";
                    } else {
                        return "1";
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    return "0";
                    //e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String aVoid) {
                if (aVoid != null) {
                    count[0] = count[0] + 1;
                    result = result + aVoid + ",";
                    if (count[0] == infocount) {
                        mCallback.ReportState(result.substring(0, result.length() - 1));
                    }
                }
            }
        }.execute();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void reportOTH(final String info) {
        String links = info.replace("m6a=__MAC__", "m6a=" + MD5Util.GetMD5Code(AdkeyData.getSN()));//SN的MD5加密值
        String flink = links.replace("ns=__IP__", "ns=" + Util.getLocalIpAddress());//替换IP
        if (AdkeyData.isDeBug()) {
            Log.i("Adkey", info + "--reportOTH--info");
        }
        if (info.contains("miaozhen")) {
            MZMonitor.retryCachedRequests(mContext);
            MZMonitor.adTrack(mContext, flink);
        } else if (info.contains("admaster")) {
            //Countly.sharedInstance().onExpose(flink);
        }
//        //TODO 上报处理
//        //final String impretion = AdkeyData.getImpretion();
//        new AsyncTask<Void, Void, String>() {
//            @Override
//            protected String doInBackground(Void... params) {
//                try {
//                    URL url = new URL(info);
//                    if (AdkeyData.isDeBug()) {
//                        Log.i("AdKeyRequest", info + "==reportOTH_link");
//                    }
//                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                    connection.setRequestMethod("GET");
//                    connection.connect();
////                    int requestCode = connection.getResponseCode();
////					if (requestCode == HttpURLConnection.HTTP_OK) {
////						return "0";
////					} else {
////						return "0";
////					}
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(String aVoid) {
//                if (aVoid != null) {
//                    if (AdkeyData.isDeBug()) {
//                        Log.i("AdKeyRequest", aVoid + "--reportOTH");
//                    }
//                }
//            }
//        }.execute();
    }

//    private void ThridPartyReport(Context context, String url) {
//
//        if (url.contains("miaozhen")) {
//            String ipadress = Util.getLocalIpAddress();
//            url = url.replace("ns=__IP__", "ns=" + ipadress);
//            MZMonitor.retryCachedRequests(context);
//            MZMonitor.adTrack(context, url);
//        } else if (url.contains("")) {
//
//        }
//    }
}
