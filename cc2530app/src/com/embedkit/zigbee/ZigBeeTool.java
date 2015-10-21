package com.embedkit.zigbee;

import java.io.*;
import java.util.ArrayList;

import com.embedkit.zigbee.ZbProx.ZbProxCallBack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class ZigBeeTool extends Activity implements MainView.OnNodeClickListener
		/* implements OnTouchListener*/{   
    /** Called when the activity is first created. */
	static final String TAG = "ZigBee"; 
	static final String PREFS_NAME = "ZigBeeConfig";
	static final String SAVE_IPADDR = "zbGetWayIP";
	
	
	int mConnectStatus = 0;
	int mSearchingZbNet = 0;
	
	ViewFlipper mViewFilpper;
	
	View mViewTop;
	
	//View mViewNode;
	NodePresent mNodePresent;
	Object mLock = new Object();
	
	MainView mMainView;
	
	ProgressDialog mDlg;
	//Node mFocusNode;
	
	private static ZigBeeTool mInstance;
	
	SharedPreferences mSaveVar;
	
	String mIpAddr = "";
	
	String mZigBeeGetWay;
	
	ZbThread mZbThread;
	
	boolean mNeedAutoConnect = false;
	
	static ZigBeeTool getInstance(){
		return mInstance;
	}
	static int sScreenHeight;
	static int sScreenWidth;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	DisplayMetrics dm = new DisplayMetrics();
  	  	getWindowManager().getDefaultDisplay().getMetrics(dm);
  	  	Log.i(TAG, "screen size : "+dm.widthPixels +" X "+dm.heightPixels);
  	  	sScreenHeight = dm.heightPixels;
  	  	sScreenWidth = dm.widthPixels;
  	   
        mInstance = this;
             
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);//先给Activity注册界面进度条功能
        
        
        setContentView(R.layout.main);
        mViewFilpper = (ViewFlipper) findViewById(R.id.viewFlipper1);
        mViewTop = findViewById(R.id.top);
       
         
        mMainView = (MainView)mViewTop.findViewById(R.id.MainView);
        mMainView.setOnNodeClickListener(this);
        

        mDlg = new ProgressDialog(this);
        mDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mDlg.setMessage("正在获取结点信息");
		//mDlg.setCancelable(true);
		   
		// 设置切入动画
        mViewFilpper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.slide_in_left));
        // 设置切出动画
        mViewFilpper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.slide_out_right));
		
        UiHandler hd = new UiHandler();
        mZbThread = new ZbThread(hd);
            
        mSaveVar = getSharedPreferences(PREFS_NAME, 0);
        mZigBeeGetWay = mSaveVar.getString(SAVE_IPADDR, "127.0.0.1");
        
        connect2server();
        
        Tool.IterateLocalIpAddress(new Tool.IterateIpAddressListener() {
			public void iterate(String name, String ip) {
				// TODO Auto-generated method stub
				if (mIpAddr.length() != 0) {
					mIpAddr += "\n";
				}
				mIpAddr +=  name + ":" + ip;
			}
        });
    } 
    private void connect2server() 
    {
    	mConnectStatus = 1;
        mZbThread.requestConnect(mZigBeeGetWay, 8320); //10.0.2.2 liren408990992.gicp.net
        setTitle("正在连接到ZigBee网关 -- " + this.mZigBeeGetWay);
        setProgressBarIndeterminateVisibility(true);
    }
    
    class UiHandler extends Handler
    {
    	
    	public void handleMessage(Message msg)
    	{  
    		switch(msg.what) {
    		case ZbThread.MSG_CONNECT_STATUS:
    			onConnectChange(msg.arg1);
    			break;
    		case ZbThread.MSG_NEW_NETWORK:
    			onMsgNetwork(msg.arg1);
    			break;
    			
    		case ZbThread.MSG_CONNECT_DATA:
    			byte[] dat = (byte[])msg.obj;
    			if (msg.arg1 == 0x6980) { /* app msg */
    				onResponseMSG_GET_APP_MSG(dat);
    			}
    			break;
    			
    		case ZbThread.MSG_GET_APP_MSG:
    			byte[] dat2 = (byte[])msg.obj;
    			
    			onResponseMSG_GET_APP_MSG(dat2);
    			
    			break;
    		} 
    	}
    }
  
    private void onConnectChange(int st)
    {
    	Log.d(TAG, "onConnectChange status : "+st);
		if (st == 0) {
			/* connect off*/
			mConnectStatus = 0;
			setTitle("连接ZigBee网关失败 -- " + this.mZigBeeGetWay);
			setProgressBarIndeterminateVisibility(false);
			if (mNeedAutoConnect) {
				connect2server();
			}
		} else {
			/* connect on */
			mConnectStatus = 2;
			setTitle("正在搜索ZigBee网络...");
			setProgressBarIndeterminateVisibility(true);
			mSearchingZbNet = 1;
			mZbThread.requestSerachNetWrok();
		}	
    }
    private void onMsgNetwork(int st)
    {
    	mSearchingZbNet = 0;
    	if (st < 0) {
    		setTitle("没有找到协调器");
    		setProgressBarIndeterminateVisibility(false);
    		Toast.makeText(ZigBeeTool.this, "搜索网络失败", Toast.LENGTH_LONG).show();
    		return;
    	}
    	if (st == 0) {
    		/* finish */
    		setTitle("物联网综合演示实验");
    		setProgressBarIndeterminateVisibility(false);
    		return;
    	}
		LayoutParams p = mMainView.getLayoutParams();
		int w = sScreenWidth, h = sScreenHeight;
		if (Top.bm.getHeight() > h)
			h = Top.bm.getHeight();
		if (Top.bm.getWidth() > w)
			w = Top.bm.getWidth();
		
		p.height = h;
		p.width = w;
		mMainView.setLayoutParams(p);
		
		mMainView.invalidate();
    }
  
    
    /* *******************************************************************
     * 
     *                      处理结点数据
     */
    private void onResponseMSG_GET_APP_MSG(byte[]dat)
    {
    
    	if (dat == null) return;
    	Log.d(TAG, "APP MSG :"+Tool.byte2string(dat));
    	if (dat == null || dat.length<=4) {
    		Log.d(TAG, "APP MSG timeout or package error.");
    		return;
    	}
		//int addr = 0xffff & ((dat[0]<<8) | (0xff&dat[1]));
    	int addr = Tool.builduInt(dat[0], dat[1]);
		//int cmd = 0xffff & ((dat[2]<<8) | (0xff&dat[3]));
    	int cmd = Tool.builduInt(dat[2], dat[3]);
		
		byte[] data = new byte[dat.length-4];
		for (int i=0; i<data.length; i++) data[i] = dat[4+i];
		
		synchronized(mLock) {
			if (mNodePresent != null ) {
				mNodePresent.procAppMsgData(addr, cmd, data);
			}	
		}
    }
    
    
    @Override
    public void onStop()
    {
    	Log.d(TAG, "onStop...");
    	 mZbThread.requestDisConnect();
    	
    	super.onStop();
    }
    
    @Override
    public void onRestart()
    {
    	Log.d(TAG, "onRestart...");
    	connect2server();
   
    	super.onRestart();
    }
    public void onResume()
    {
    	Log.d(TAG, "onResume....");
    	
    	super.onResume();
    }
    public void onPause()
    {
    	Log.d(TAG, "onPause....");
    		
    	
    	super.onPause();
    }
    
    
    
    /**************************************************************
     * 
     * 
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK){
       
        	synchronized(mLock) {
        		if (mNodePresent != null) {
        			mViewFilpper.showPrevious();
            		mViewFilpper.removeView(mNodePresent.mView);
            		this.setTitle("ZigBee");
            		mNodePresent.setdown();
            		mNodePresent = null;

        			return true;
        		}
        	}
        	mZbThread.requestDisConnect();
        	this.finish();
        }
        return super.onKeyDown(keyCode, event);	
    }
    

	public void onNodeClick(Node n) {
		// TODO Auto-generated method stub
		Log.d(TAG, "do node click... "+ n);
		synchronized(mLock) {
			if (mNodePresent != null) {
				return;
			}
			mNodePresent = NodePresent.FactoryCreateInstance(n);
			mNodePresent.setup();
			mViewFilpper.addView(mNodePresent.mView);
			mViewFilpper.showNext();
			this.setTitle(Node.getDeviceTypeString(mNodePresent.mNode));
		}
	}

	
	
	/*****************************************************************
	 * 	
	 * 				菜单处理
	 */
	static final int _COMMAND_SET_ID = Menu.FIRST+1;
	static final int _COMMAND_SEARCH_ID = Menu.FIRST+2;
	static final int _COMMAND_ABORT_ID = Menu.FIRST+3;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(Menu.NONE, _COMMAND_SET_ID, 1, "设置");
		menu.add(Menu.NONE, _COMMAND_SEARCH_ID, 2,"搜索网络");
		menu.add(Menu.NONE, _COMMAND_ABORT_ID, 3, "关于");
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()) {
		case _COMMAND_SET_ID:
		{
			LayoutInflater factory=LayoutInflater.from(this);  
            //得到自定义对话框  
            final View settingView=factory.inflate(R.layout.settingdlg, null);
            final EditText etIp = (EditText)settingView.findViewById(R.id.etipaddr);
            etIp.setText(this.mZigBeeGetWay);
            Builder builder =new AlertDialog.Builder(this);
			builder.setTitle("ZigBee网关");//设置标题
			builder.setView(settingView);
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					mZigBeeGetWay = etIp.getText().toString();
					if (mConnectStatus != 0) {
						mNeedAutoConnect = true;
						mZbThread.requestDisConnect();
					} else {
						connect2server();
					}
					SharedPreferences.Editor editor = mSaveVar.edit();
					editor.putString(SAVE_IPADDR, mZigBeeGetWay);
					editor.commit(); 
				}//设置监听事件		
			});
			builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			});
			builder.create().show();
			
			break;
		}
		case _COMMAND_SEARCH_ID:
			if (mConnectStatus == 0) {
				connect2server();
			} else if (mConnectStatus == 2) {
				if (mSearchingZbNet == 0) {
					setTitle("正在搜索ZigBee网络...");
					setProgressBarIndeterminateVisibility(true);
					mSearchingZbNet = 1;
					mZbThread.requestSerachNetWrok();
				} else {
					Toast.makeText(ZigBeeTool.this, "正在搜索网络，请稍等...", 
							Toast.LENGTH_LONG).show();
				}
			}
			break;
		case _COMMAND_ABORT_ID:
		{
			LayoutInflater factory=LayoutInflater.from(this);  
            //得到自定义对话框  
            final View DialogView=factory.inflate(R.layout.abort, null);
            TextView txIpAddr = (TextView)DialogView.findViewById(R.id.abortIpAddr);
            txIpAddr.setText(mIpAddr);
			Builder builder =new AlertDialog.Builder(this);
			builder.setTitle("ZigBee网关");//设置标题
			builder.setView(DialogView);
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {	
				}
			});
			
			builder.create().show();
			break;
		}
		}
		return false;
	}
	
	
	
}
  