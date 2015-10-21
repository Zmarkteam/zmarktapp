package com.embedkit.zigbee;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class TempSensorPresent extends NodePresent {
	static final String TAG = "TempSensorPresent";
	
	ZigBeeTool mZBTool;
	
	View mInfoView;
	TempCurveView mTempCurveView ;
	View mTempConfigView;
	
	EditText mEditTextHighter;
	EditText mEditTextLower;
	EditText mEditTextNumber;
	
	CheckBox mCheckBox;
	
	int mAlarmHeighter;
	int mAlarmLower;
	//boolean mEnableAlarm = false;
	boolean mAlarmTriage = false;
	String mNumber;
	
	TempSensorPresent(Node n) {
		super(R.layout.temperature_sensor, n);
		// TODO Auto-generated constructor stub
		   
		mInfoView =  super.mView.findViewById(R.id.tempInfoView);
		mTempCurveView = (TempCurveView) super.mView.findViewById(R.id.tempCurveView);
		mTempConfigView = super.mView.findViewById(R.id.tempConfigView);
		
		mEditTextHighter = (EditText)mTempConfigView.findViewById(R.id.temp_et_heighter);
		mEditTextLower = (EditText)mTempConfigView.findViewById(R.id.temp_et_lower);
		mEditTextNumber = (EditText)mTempConfigView.findViewById(R.id.temp_et_number);
		mCheckBox = (CheckBox)mTempConfigView.findViewById(R.id.temp_checkbox_enable_alarm);
		
		mAlarmLower = Integer.parseInt(mEditTextLower.getText().toString());
		mAlarmHeighter = Integer.parseInt(mEditTextHighter.getText().toString());
		
		mEditTextHighter.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (!mEditTextHighter.isFocused()) {
					mAlarmHeighter = Integer.parseInt(mEditTextHighter.getText().toString());
				}
			}
		});
		mEditTextLower.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (!mEditTextLower.isFocused()) {
					mAlarmLower = Integer.parseInt(mEditTextLower.getText().toString());
				}
			}
		});

		
		final TabHost tabHost = (TabHost)super.mView.findViewById(android.R.id.tabhost);
		tabHost.setup();
		

		TabHost.TabSpec tb = tabHost.newTabSpec("0");
		tb.setIndicator("", new BitmapDrawable(Resource.imageNodeInfo));
		tb.setContent(new TabHost.TabContentFactory()
	    {
		     public View createTabContent(String tag) {
		     
		    	 return mInfoView; 
		     }
		});
		tabHost.addTab( tb);
		
		TabSpec tb2 = tabHost.newTabSpec("1");
		tb2.setIndicator("", new BitmapDrawable(Resource.imageNodeCurve));
		tb2.setContent(new TabHost.TabContentFactory()
	    {
		     public View createTabContent(String tag) {
		     
		    	 return mTempCurveView;
		     }
		});
		tabHost.addTab( tb2 );

		tabHost.addTab(tabHost.newTabSpec("2")
				.setIndicator("", new BitmapDrawable(Resource.imageNodeConfig))
				.setContent(new TabHost.TabContentFactory(){
					public View createTabContent(String tag) {
						// TODO Auto-generated method stub
						return mTempConfigView;
					}
				}));

		tabHost.setCurrentTab(1);
	
	}
	
	@Override 
	void setup()
	{
		// enable report temp value
		super.sendRequest(0x0002, new byte[]{0x01,0x02, 0x05});
	}
	@Override
	void setdown() {
		// TODO Auto-generated method stub
		super.sendRequest(0x0002, new byte[]{0x01,0x02, 0x00});
	}
	
	
	@Override 
	void procData(int req, byte[] dat) {
		// TODO Auto-generated method stub
		float v;
		String s = new String(dat);
		if (s.contains("Temp")) {
			int i= s.lastIndexOf(':') + 1;
			s = s.substring(i,i+3);
			//mTempCurveView.addData(new Float(s));
		}
		//mDataView.setText();
	}

	@Override
	void procAppMsgData(int addr, int cmd, byte[] dat) {
		// TODO Auto-generated method stub
		int param;
		int value;
		
		if (addr != super.mNode.mNetAddr) return;
		if (cmd != 0x0003) return;
		if (dat.length < 3) return;
		
		Log.d(TAG, "current temp : "+ dat[2]);
		
		param = Tool.builduInt(dat[0], dat[1]); //dat[0]<<8 | dat[1]; 
		value = Tool.builduInt(dat[2]); //dat[2];
		
		mTempCurveView.addData((byte)value);
		
		boolean alarm = false;
		if (mCheckBox.isChecked() && value > this.mAlarmHeighter ) {
			if (!mAlarmTriage) {
				Log.d(TAG, "alarm height temp...");
				String title = "高温警告";
				String msg = "当前温度"+value+"高于告警值"+mAlarmHeighter;
				Tool.notify(title, msg);
				Tool.playAlarm(3);
				
				mNumber = mEditTextNumber.getText().toString();
				if (mNumber != null && mNumber.length()>0) {
					Tool.sendShortMessage(mNumber, title+":"+msg);
				}
			}
			mAlarmTriage = true;
		} else 
		if (mCheckBox.isChecked() && value < this.mAlarmLower) {
			if (!mAlarmTriage) {
				Log.d(TAG, "alarm lower temp...");
				String title = "低温警告";
				String msg = "当前温度"+value+"低于告警值"+mAlarmLower;
				Tool.notify(title, msg);
				Tool.playAlarm(3);
				mNumber = mEditTextNumber.getText().toString();
				if (mNumber != null && mNumber.length()>0) {
					Tool.sendShortMessage(mNumber, title+":"+msg);
				}
			}
			mAlarmTriage = true;
		} else {
			mAlarmTriage = false;
		}
	}



}
