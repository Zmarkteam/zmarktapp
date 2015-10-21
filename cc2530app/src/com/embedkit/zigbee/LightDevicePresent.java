package com.embedkit.zigbee;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

public class LightDevicePresent extends NodePresent
	implements OnClickListener{
	
	static final String TAG = "LightDevicePresent";
	
	View mInfoView;
	View mCtrolView;
	TextView mLogView;

	ImageView mLightImageView;
	Button mBtnOff;
	Button mBtnOn;
	Button mBtnReverse;
	
	LightDevicePresent(Node n) {
		super(R.layout.light, n);
		// TODO Auto-generated constructor stub
		
		mInfoView = super.mView.findViewById(R.id.linghtInfoView);
		mCtrolView = super.mView.findViewById(R.id.lightControlView);
		mLogView = (TextView) super.mView.findViewById(R.id.linghtLogView);
		
		mLightImageView = (ImageView)mCtrolView.findViewById(R.id.lightImageView);
		
		mBtnOff = ((Button)mCtrolView.findViewById(R.id.lightBtnOff));
		mBtnOff.setOnClickListener(this);
		
		mBtnOn = ((Button)mCtrolView.findViewById(R.id.lightBtnOn));
		mBtnOn.setOnClickListener(this);
		
		mBtnReverse = ((Button)mCtrolView.findViewById(R.id.lightBtnReverse));
		mBtnReverse.setOnClickListener(this);
		
		
		final TabHost tabHost = ((TabHost)super.mView.findViewById(android.R.id.tabhost));
		tabHost.setup();
		
		tabHost.addTab(tabHost.newTabSpec("0")
		.setIndicator("", new BitmapDrawable(Resource.imageNodeInfo))
		.setContent(new TabHost.TabContentFactory(){
		     public View createTabContent(String tag) {
		    	 return mInfoView; 
		     }
		}));
		
		tabHost.addTab(tabHost.newTabSpec("1")
				.setIndicator("", new BitmapDrawable(Resource.imageNodeInfo))
				.setContent(new TabHost.TabContentFactory(){
				     public View createTabContent(String tag) {
				    	 return mCtrolView; 
				     }
				}));
		/*
		final TabWidget tabWidget = (TabWidget) tabHost.findViewById(android.R.id.tabs);
		View v = tabWidget.getChildAt(0);
		v.setBackgroundDrawable(new BitmapDrawable(Resource.imageNodeInfo));
		v = tabWidget.getChildAt(1);
		v.setBackgroundDrawable(new BitmapDrawable(Resource.imageNodeCurve));
		
		tabHost.setOnTabChangedListener(new OnTabChangeListener(){
			public void onTabChanged(String tabId) {
				// TODO Auto-generated method stub
			    for (int i = 0; i < tabWidget.getChildCount(); i++) {  
                    View v = tabWidget.getChildAt(i);  
                    if (tabHost.getCurrentTab() == i) {
                    	v.getBackground().setColorFilter(Tool.selfilter);
                    } else {  
                    	v.getBackground().setColorFilter(Tool.unselfilter);
                    }  
                    v.setBackgroundDrawable(v.getBackground()); 
                } 	
			}
		});
		*/	
		tabHost.setCurrentTab(1);
	}
	
	public void onClick(View v) {
		// TODO Auto-generated method stub
		byte[] dat = new byte[3];
		dat[0] = 0x02;
		dat[1] = 0x02;
		if (v == mBtnOn) {
			dat[2] = 1; 
		} else if (v == mBtnOff) {
			dat[2] = 0;
		} else /*if (v == mBtnReverse)*/{
			dat[2] = 2;
		}
		super.sendRequest(0x0002, dat);
	}
	
	@Override
	void procAppMsgData(int addr, int cmd, byte[] dat) {
		// TODO Auto-generated method stub
		int pid;
		int i = -1;
		Log.d(TAG, Tool.byte2string(dat));
		if (cmd == 0x8001 && dat[0] == 0) {
			i = 1;
		}
		if (cmd == 0x0003) {
			i = 0;
		}
		if (i < 0) return;
		while ( i<dat.length ) {
			pid= Tool.builduInt(dat[i], dat[i+1]);
			if (pid == 0x0201) { //亮度值
				
				i += 3;
			} else  
			if (pid == 0x0202) { //状态值
				if (dat[i+2] == 0) {
					mLightImageView.setImageBitmap(Resource.imageLightOff);
				} else {
					mLightImageView.setImageBitmap(Resource.imageLightOn);
				}
				i += 3;
			} else {
				return;
			}
		}
	}

	@Override
	void procData(int req, byte[] dat) {
		// TODO Auto-generated method stub

	}

	@Override
	void setdown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void setup() {
		// TODO Auto-generated method stub
		/* 读取灯光状态*/
		super.sendRequest(0x0001, new byte[]{0x02,0x01, 0x02,0x02});
	}
}
