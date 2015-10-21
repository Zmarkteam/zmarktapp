package com.embedkit.zigbee;

import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class HumiditySensorPresent extends NodePresent {

	static final String TAG = "HumiditySensorPresent";
	
	ZigBeeTool mZBTool;
	
	View mInfoView;
	HumidityCurveView mHumidityCurveView ;
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
	
	
	HumiditySensorPresent(Node n) {
		
		// TODO Auto-generated constructor stub
		super(R.layout.humidity_sensor, n);
		// TODO Auto-generated constructor stub
	
		mInfoView =  super.mView.findViewById(R.id.humidityInfoView);
		mHumidityCurveView = (HumidityCurveView) super.mView.findViewById(R.id.humidityCurveView);
		mTempConfigView = super.mView.findViewById(R.id.humidityConfigView);
		
		mEditTextHighter = (EditText)mTempConfigView.findViewById(R.id.humidity_et_heighter);
		mEditTextLower = (EditText)mTempConfigView.findViewById(R.id.humidity_et_lower);
		mEditTextNumber = (EditText)mTempConfigView.findViewById(R.id.humidity_et_number);
		mCheckBox = (CheckBox)mTempConfigView.findViewById(R.id.humidity_checkbox_enable_alarm);
		
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
		     
		    	 return mHumidityCurveView;
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
		super.sendRequest(0x0002, new byte[]{0x07,0x02, 0x03});
	}
	@Override
	void setdown() {
		// TODO Auto-generated method stub
		super.sendRequest(0x0002, new byte[]{0x07,0x02, 0x00});
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
		
		mHumidityCurveView.addData((byte)value);
		
		boolean alarm = false;
		if (mCheckBox.isChecked() && value > this.mAlarmHeighter ) {
			if (!mAlarmTriage) {
				Log.d(TAG, "alarm height temp...");
				String title = "湿度警告";
				String msg = "当前湿度"+value+"高于告警值"+mAlarmHeighter;
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
				String title = "湿度警告";
				String msg = "当前湿度"+value+"低于告警值"+mAlarmLower;
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
