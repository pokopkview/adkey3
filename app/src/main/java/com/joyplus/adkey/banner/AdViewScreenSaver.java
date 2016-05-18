package com.joyplus.adkey.banner;import java.io.File;import java.io.InputStream;import java.lang.Thread.UncaughtExceptionHandler;import java.net.MalformedURLException;import java.net.URL;import java.util.Timer;import android.Manifest;import android.content.BroadcastReceiver;import android.content.Context;import android.content.Intent;import android.content.IntentFilter;import android.content.pm.PackageManager;import android.location.Location;import android.location.LocationManager;import android.os.Handler;import android.telephony.TelephonyManager;import android.util.AttributeSet;import android.widget.FrameLayout;import com.joyplus.adkey.AdListener;import com.joyplus.adkey.AdRequest;import com.joyplus.adkey.BannerAd;import com.joyplus.adkey.Const;import com.joyplus.adkey.RequestBannerAd;import com.joyplus.adkey.Util;import com.joyplus.adkey.Monitorer.AdMonitorManager;import com.joyplus.adkey.download.ImpressionThread;import com.joyplus.adkey.download.PicDownloader;import com.joyplus.adkey.widget.Log;import com.joyplus.adkey.widget.SerializeManager;public class AdViewScreenSaver extends FrameLayout{		public static final int LIVE = 0;	public static final int TEST = 1;		private boolean includeLocation = false;	private String publisherId;	private boolean animation;		private com.joyplus.adkey.mraid.MoPubView mMoPubview;	private BannerAdViewScreenSaver mBannerView;		private Timer reloadTimer;	private boolean isInternalBrowser = false;		private BannerAd response;	private AdRequest request;		private String requestURL = null;		private LocationManager locationManager;	private int isAccessFineLocation;	private int isAccessCoarseLocation;	private int telephonyPermission;		private BroadcastReceiver mScreenStateReceiver;	private Context mContext = null;	protected boolean mIsInForeground;		private AdListener adListener;		private Thread loadContentThread;		private InputStream xml;		private SerializeManager serializeManager = null;	private final Handler updateHandler = new Handler();		private final Runnable showContent = new Runnable()	{		@Override		public void run()		{			AdViewScreenSaver.this.showContent();		}	};		private Handler mHandler = new Handler(){		public void handleMessage(android.os.Message msg) {		switch (msg.what) {		case Const.MESSAGE_DELAY_REQUEST://			initialize(mContext);			loadContent();			break;		default:			break;		}		}	};		private String mUserAgent;		public void setWidth(int width)	{		;	}		public void setHeight(int width)	{		;	}		public AdViewScreenSaver(final Context context, final AttributeSet attributes)	{		super(context, attributes);		mContext = context;		if (attributes != null)		{			int count = attributes.getAttributeCount();			for (int i = 0; i < count; i++)			{				String name = attributes.getAttributeName(i);				if (name.equals("publisherId"))				{					this.publisherId = attributes.getAttributeValue(i);				} else if (name.equals("request_url"))				{					this.requestURL = attributes.getAttributeValue(i);				} else if (name.equals("animation"))				{					this.animation = attributes.getAttributeBooleanValue(i,							false);				} else if (name.equals("includeLocation"))				{					this.includeLocation = attributes.getAttributeBooleanValue(							i, false);				}			}		}		initialize(context);	}		/*	 * @author yyc	 */	public AdViewScreenSaver(final Context context, final String publisherId,			final boolean animation)	{		this(context, Const.REQUESTURL, publisherId, true, animation, null);	}		public AdViewScreenSaver(final Context context, final String requestURL,			final String publisherId)	{		this(context, requestURL, publisherId, false, false);	}		public AdViewScreenSaver(final Context context, final String requestURL,			final InputStream xml, final String publisherId,			final boolean includeLocation, final boolean animation)	{		this(context, xml, requestURL, publisherId, includeLocation, animation);	}		public AdViewScreenSaver(final Context context, final InputStream xml,			final String requestURL, final String publisherId,			final boolean includeLocation, final boolean animation)	{		super(context);		this.xml = xml;		this.requestURL = requestURL;		mContext = context;		this.publisherId = publisherId;		this.includeLocation = includeLocation;		this.animation = animation;		this.initialize(context);	}		public AdViewScreenSaver(final Context context, final String requestURL,			final String publisherId, final boolean includeLocation,			final boolean animation)	{		this(context, requestURL, publisherId, includeLocation, animation, null);	}		public AdViewScreenSaver(final Context context, final String requestURL,			final String publisherId, final boolean includeLocation,			final boolean animation, final AdListener listener)	{		super(context);		this.requestURL = requestURL;		mContext = context;		this.publisherId = publisherId;		Util.PublisherId = publisherId;		this.includeLocation = includeLocation;		this.animation = animation;		this.adListener = listener;		serializeManager = new SerializeManager();		this.initialize(context);	}		@Override	protected void onAttachedToWindow()	{				super.onAttachedToWindow();		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);		filter.addAction(Intent.ACTION_USER_PRESENT);		mContext.registerReceiver(mScreenStateReceiver, filter);	}		@Override	protected void onDetachedFromWindow()	{				super.onDetachedFromWindow();		unregisterScreenStateBroadcastReceiver();	}		private Location getLocation()	{				if (this.locationManager != null)		{			if (this.isAccessFineLocation == PackageManager.PERMISSION_GRANTED)			{				final boolean isGpsEnabled = this.locationManager						.isProviderEnabled(LocationManager.GPS_PROVIDER);				if (isGpsEnabled)					return this.locationManager							.getLastKnownLocation(LocationManager.GPS_PROVIDER);			}			if (this.isAccessCoarseLocation == PackageManager.PERMISSION_GRANTED)			{				final boolean isNetworkEnabled = this.locationManager						.isProviderEnabled(LocationManager.NETWORK_PROVIDER);								if (isNetworkEnabled)					return this.locationManager							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);			}		}		return null;	}		public int getRefreshRate()	{		if (this.response != null)			return this.response.getRefresh();		return -1;	}		private AdRequest getRequest()	{		if (this.request == null)		{			this.request = new AdRequest();			if (this.telephonyPermission == PackageManager.PERMISSION_GRANTED)			{				final TelephonyManager tm = (TelephonyManager) this						.getContext().getSystemService(								Context.TELEPHONY_SERVICE);				this.request.setDeviceId(tm.getDeviceId());			} else			{				this.request.setDeviceId(Util.getDeviceId(this.mContext));			}			this.request.setPublisherId(this.publisherId);			this.request.setUserAgent(mUserAgent);			this.request.setUserAgent2(Util.buildUserAgent());		}		Location location = null;		if (this.includeLocation)			location = this.getLocation();		if (location != null)		{			this.request.setLatitude(location.getLatitude());			this.request.setLongitude(location.getLongitude());		} else		{			this.request.setLatitude(0.0);			this.request.setLongitude(0.0);		}				this.request.setType(AdRequest.BANNER);		this.request.setRequestURL(requestURL);		return this.request;	}		private void initialize(final Context context)	{		/*		 * init Util.VideoFileDir		 */		Util.GetPackage(mContext);				mUserAgent = Util.getDefaultUserAgentString(getContext());		registerScreenStateBroadcastReceiver();		this.locationManager = null;		this.telephonyPermission = context				.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE);		this.isAccessFineLocation = context				.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);		this.isAccessCoarseLocation = context				.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);		if (this.isAccessFineLocation == PackageManager.PERMISSION_GRANTED				|| this.isAccessCoarseLocation == PackageManager.PERMISSION_GRANTED)			this.locationManager = (LocationManager) this.getContext()					.getSystemService(Context.LOCATION_SERVICE);	}		public boolean isInternalBrowser()	{		return this.isInternalBrowser;	}		private void registerScreenStateBroadcastReceiver()	{		mScreenStateReceiver = new BroadcastReceiver()		{			@Override			public void onReceive(Context context, Intent intent)			{				if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))				{					if (mIsInForeground)					{						pause();					}				} else if (intent.getAction()						.equals(Intent.ACTION_USER_PRESENT))				{					if (mIsInForeground)					{						resume();					}				}			}		};		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);		filter.addAction(Intent.ACTION_USER_PRESENT);		mContext.registerReceiver(mScreenStateReceiver, filter);	}		private void loadContent()	{				if (this.loadContentThread == null)		{			this.loadContentThread = new Thread(new Runnable()			{				@Override				public void run()				{					final RequestBannerAd requestAd;					if (xml == null)						requestAd = new RequestBannerAd();					else						requestAd = new RequestBannerAd(xml);										try					{												AdViewScreenSaver.this.response = requestAd								.sendRequest(AdViewScreenSaver.this.getRequest());						new ImpressionThread(mContext,response.getmImpressionUrl(),Util.PublisherId,Util.AD_TYPE.SCREEN_SAVER).start();												Util.ScreenSaverAdNum++;						String path = Const.DOWNLOAD_PATH + Util.VideoFileDir+"ad"+Util.ScreenSaverAdNum%3;						serializeManager.writeSerializableData(path, AdViewScreenSaver.this.response);												int startInd = AdViewScreenSaver.this.response.getText().indexOf(								"mAdserveAdImage")+22;						int endInd = AdViewScreenSaver.this.response.getText().indexOf(								"/>", startInd)-12;						String thisImageText = AdViewScreenSaver.this.response.getText()								.substring(startInd, endInd);						startDownload(thisImageText);						if (AdViewScreenSaver.this.response != null)						{							AdViewScreenSaver.this.updateHandler									.post(AdViewScreenSaver.this.showContent);						}					} catch (final Throwable e)					{						Util.ScreenSaverAdNum++;						String path = Const.DOWNLOAD_PATH + Util.VideoFileDir								+ "ad" + Util.ScreenSaverAdNum%3;						//read bannerAd,if BannerAdRequest is empty notifyLoadAdFailed						AdViewScreenSaver.this.response = (BannerAd) serializeManager								.readSerializableData(path);						if(AdViewScreenSaver.this.response!=null)						{							int startInd = AdViewScreenSaver.this.response									.getText().indexOf("mAdserveAdImage") + 22;							int endInd = AdViewScreenSaver.this.response.getText()									.indexOf("/>", startInd) - 12;							String thisImageText = AdViewScreenSaver.this.response									.getText().substring(startInd, endInd);							startDownload(thisImageText);							if (AdViewScreenSaver.this.response != null)							{								AdViewScreenSaver.this.updateHandler										.post(AdViewScreenSaver.this.showContent);							}						}						else{							AdViewScreenSaver.this.notifyLoadAdFailed(e);						}					}					AdViewScreenSaver.this.loadContentThread = null;				}							});			this.loadContentThread					.setUncaughtExceptionHandler(new UncaughtExceptionHandler()					{												@Override						public void uncaughtException(final Thread thread,								final Throwable ex)						{							AdViewScreenSaver.this.loadContentThread = null;						}					});			this.loadContentThread.start();		} else		{					}			}		public void startDownload(String path){		URL url = null;		try		{			url = new URL(path);		} catch (MalformedURLException e)		{			// TODO Auto-generated catch block			e.printStackTrace();		}		if (url != null)		{			Util.ExternalName = "." + Util.getExtensionName(url.getPath());		} else		{			Util.ExternalName = ".mp4";		}				PicDownloader downloader = new PicDownloader(path, mContext);		if (path.startsWith("http:") || path.startsWith("https:"))		{			if(Util.isNetworkAvailable(mContext))			{				downloader.download();				Log.i(Const.TAG, "download starting");			}		}	}		public void loadNextAd()	{		this.loadContent();	}		private void notifyNoAd()	{		mHandler.removeMessages(Const.MESSAGE_DELAY_REQUEST);		this.updateHandler.post(new Runnable()		{						@Override			public void run()			{				if (adListener != null)					adListener.noAdFound();			}		});	}		private void notifyLoadAdFailed(final Throwable e)	{		mHandler.removeMessages(Const.MESSAGE_DELAY_REQUEST);		this.updateHandler.post(new Runnable()		{						@Override			public void run()			{				if (AdViewScreenSaver.this.adListener != null)				{					adListener.noAdFound();				}			}		});	}		@Override	protected void onWindowVisibilityChanged(int visibility)	{		super.onWindowVisibilityChanged(visibility);				if (visibility == VISIBLE)		{			mIsInForeground = true;			resume();		} else		{			mIsInForeground = false;			pause();		}	}		public void pause()	{		if (this.reloadTimer != null)			try			{				this.reloadTimer.cancel();				this.reloadTimer = null;			} catch (final Exception e)			{			}	}		private void showContent()	{		//change by Jas		if(response.getmTrackingUrl().size()>0){			AdMonitorManager.getInstance(mContext).AddTRACKINGURL(response.getmTrackingUrl());		}	    //end change by Jas		if (mMoPubview != null)		{			mMoPubview.destroy();			this.removeView(mMoPubview);		}		if (mBannerView != null)		{			this.removeView(mBannerView);		}		if (response.getType() == Const.TEXT				|| response.getType() == Const.IMAGE)		{			mBannerView = new BannerAdViewScreenSaver(mContext, response, animation,					adListener);			this.addView(mBannerView);		}		if (response.getType() == Const.MRAID)		{			mMoPubview = new com.joyplus.adkey.mraid.MoPubView(mContext);			final float scale = mContext.getResources().getDisplayMetrics().density;			this.addView(mMoPubview, new FrameLayout.LayoutParams(					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,					(int) (50 * scale + 0.5f)));			com.joyplus.adkey.mraid.AdView m = new com.joyplus.adkey.mraid.AdView(					mContext, mMoPubview, response);			mMoPubview.setAdListener(adListener);			m.setAdUnitId("");			m.loadAd();		}		if (response.getType() == Const.NO_AD)		{			notifyNoAd();		}		this.startReloadTimer();	}		public void release()	{		mHandler.removeMessages(Const.MESSAGE_DELAY_REQUEST);		unregisterScreenStateBroadcastReceiver();		if (mMoPubview != null)			mMoPubview.destroy();	}		private void unregisterScreenStateBroadcastReceiver()	{		try		{			mContext.unregisterReceiver(mScreenStateReceiver);		} catch (Exception IllegalArgumentException)		{		}	}		public void resume()	{		if (this.reloadTimer != null)		{			this.reloadTimer.cancel();			this.reloadTimer = null;		}		this.reloadTimer = new Timer();				if (this.response != null && this.response.getRefresh() > 0)			this.startReloadTimer();		else if (this.response == null				|| (mMoPubview == null && mBannerView == null))		{//			this.showContent();			mHandler.sendEmptyMessageDelayed(Const.MESSAGE_DELAY_REQUEST, 10000);			if(Util.isNetworkAvailable(mContext))			{				String path = Const.DOWNLOAD_PATH + Util.VideoFileDir;				File file = new File(path);				if (!file.exists())					file.mkdirs();				String tempPath[] = file.list();				for(String temp : tempPath)				{					File tempFile = new File(path + temp);					tempFile.delete();				}			}		}	}		public void setAdListener(final AdListener bannerListener)	{		this.adListener = bannerListener;		if (mMoPubview != null)		{			mMoPubview.setAdListener(bannerListener);		}		if (mBannerView != null)		{			mBannerView.setAdListener(bannerListener);		}	}		public void setInternalBrowser(final boolean isInternalBrowser)	{		this.isInternalBrowser = isInternalBrowser;	}		private void startReloadTimer()	{		if (this.reloadTimer == null || response.getRefresh() <= 0)			return;		final int refreshTime = this.response.getRefresh() * 1000;		final ReloadTaskScreenSaver reloadTask = new ReloadTaskScreenSaver(AdViewScreenSaver.this);		this.reloadTimer.schedule(reloadTask, refreshTime);	}	}