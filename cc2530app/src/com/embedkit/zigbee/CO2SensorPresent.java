package com.embedkit.zigbee;

import com.embedkit.zigbee.InfraredSensorPresent.MyHandler;

import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.ImageView;

public class CO2SensorPresent extends NodePresent {
	
	View mInfoView;
	View mConfigView;
	
	ImageView mCo2SensorImageView;
	 
	Button mBtnDisable;  
	Button mBtnEnable;
	
	CheckBox mAlarmCheckBox;
	EditText mAlarmNumberEditText;
	
	boolean mSensorEnable = true;
	boolean mSensorAlarm = false;
	String mAlarmNumber;
	
	CO2SensorPresent(Node n) {
		super(R.layout.co2_sensor, n);
		// TODO Auto-generated constructor stub
		mInfoView = super.mView.findViewById(R.id.co2sensorInfoView);
		mConfigView = super.mView.findViewById(R.id.co2sensorConfigView);
		
		mCo2SensorImageView = (ImageView)mConfigView.findViewById(R.id.co2sensorImageView);
		
		mBtnEnable = (Button)mConfigView.findViewById(R.id.co2sensorBtnEnable);
		mBtnEnable.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (!mSensorEnable) {
					CO2SensorPresent.super.sendRequest(0x0002, 
							new byte[]{0x05,0x01, 0x01});
				}
			}
		});
		
		mBtnDisable = (Button)mConfigView.findViewById(R.id.co2sensorBtnDisable);
		mBtnDisable.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (mSensorEnable) {
					CO2SensorPresent.super.sendRequest(0x0002, 
							new byte[]{0x05,0x01, 0x00});
				}
			}
		});
		
		mAlarmNumberEditText = (EditText)mConfigView.findViewById(R.id.co2sensorAlarmNumberEditText);
		mAlarmCheckBox = (CheckBox)mConfigView.findViewById(R.id.co2sensorAlarmCheckBox);
		
		
		
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
				    	 return mConfigView; 
				     }
		}));
		
		tabHost.setCurrentTab(1);
		mMyHandler = new MyHandler();
	}

	boolean mShowBlue = true; 
	boolean mExit = false;
	class MyHandler extends Handler
    {
    	public void handleMessage(Message msg) {
    		if (mSensorEnable && mShowBlue) {
    			if (mSensorAlarm) {
    				mCo2SensorImageView.setImageBitmap(Resource.imageCo2SensorAlarm);
    			} else {
    				mCo2SensorImageView.setImageBitmap(Resource.imageCo2SensorBlue);
    			}
    		} else {
    			mCo2SensorImageView.setImageBitmap(Resource.imageCo2SensorEnable);
    		}
    		if (!mExit) {
    			Message msg2 = Message.obtain();
    			mMyHandler.sendMessageDelayed(msg2, 300);
    		}
    		mShowBlue = !mShowBlue;
    	}
    }
	MyHandler mMyHandler;
	
	
	@Override
	void procAppMsgData(int addr, int cmd, byte[] dat) {
		// TODO Auto-generated method stub
		int i = -1;
		if (addr != super.mNode.mNetAddr) return;
		
		if (cmd == 0x0003) i = 0;
		if (cmd == 0x8001 && dat[0] == 0) i = 1;
		if (cmd == 0x8002 && dat[0] == 0) {
			if (mSensorEnable) {
				//mCo2SensorImageView.setImageBitmap(Resource.imageCo2SensorBlue);
				mSensorEnable = false;
			} else {
				//mCo2SensorImageView.setImageBitmap(Resource.imageCo2SensorEnable);
				mSensorEnable = true;
			}
			return;
		}
		
		while (i>=0 && i < dat.length) {
			int pid = Tool.builduInt(dat[i], dat[i+1]);
			if (pid == 0x0501) {
				if (dat[i+2] == 0) {
					/* 传感结点被禁止*/
					mSensorEnable = false;
				} else {
					mSensorEnable = true;
				}
				i += 3;
				
			} else
			if (pid == 0x0502) {
				if (dat[i+2] != 0) {
					/* 报警*/
					mSensorAlarm = true;

					if (mAlarmCheckBox.isChecked()) {
						/* 发送告警通知和短信*/
						String msg = this.mNode.mNetAddr + ":检测到可燃气体浓度超标";
						Tool.notify("气体告警", msg);
						Tool.playAlarm(3);
						mAlarmNumber = (mAlarmNumberEditText.getText().toString());
						if (mAlarmNumber != null && mAlarmNumber.length()>0) {
							Tool.sendShortMessage(mAlarmNumber, msg);
						}
					}
				} else {
					mSensorAlarm = false;
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
		mExit = true;
	}

	@Override
	void setup() {
		// TODO Auto-generated method stub
		super.sendRequest(0x0001, new byte[]{0x05,0x01, 0x05,0x02,}); 
		Message msg = Message.obtain();
		mMyHandler.sendMessageDelayed(msg, 300);
	}
}
