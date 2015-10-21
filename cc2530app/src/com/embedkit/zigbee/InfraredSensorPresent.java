package com.embedkit.zigbee;

import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TabHost.OnTabChangeListener;

public class InfraredSensorPresent extends NodePresent {
	
	View mInfoView;
	View mConfigView;
	
	 
	ImageView mInfraredImageView;
	
	Button mBtnEnable; 
	Button mBtnDisable;
	
	EditText mAlarmNumberEditText;
	CheckBox mAlarmCheckBox;
	
	boolean mSensorEnable = true;
	boolean mSensorAlarm = false;
	
	//boolean mNeedStopAlarm = false;
	
	String mAlarmNumber;
	
	InfraredSensorPresent(Node n) {
		super(R.layout.infrared_sensor, n);
		// TODO Auto-generated constructor stub
		mInfoView = super.mView.findViewById(R.id.infraredsensorInfoView);
		mConfigView = super.mView.findViewById(R.id.infraredsensorConfigView);
		
		mInfraredImageView = (ImageView)mConfigView.findViewById(R.id.infraredsensorImageView);
		mBtnEnable = (Button)mConfigView.findViewById(R.id.infraredsensorBtnEnable);
		mBtnDisable = (Button)mConfigView.findViewById(R.id.infraredsensorBtnDisable);
		mAlarmNumberEditText = (EditText)mConfigView.findViewById(R.id.infraredsensorAlarmNumberEditText);
		mAlarmCheckBox = (CheckBox)mConfigView.findViewById(R.id.infraredsensorAlarmCheckBox);
		
		mBtnEnable.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!mSensorEnable) {
					InfraredSensorPresent.super.sendRequest(0x0002, new byte[]{0x04,0x01,0x01});
				}
			}	
		});
		mBtnDisable.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mSensorEnable) {
					InfraredSensorPresent.super.sendRequest(0x0002, new byte[]{0x04,0x01,0x00});
				}
			}	
		});
		/*
		mAlarmNumberEditText.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (!mAlarmNumberEditText.isFocused()) {
					mAlarmNumber = (mAlarmNumberEditText.getText().toString());
				}
			}
		});	
		*/
		
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
		
		mMyHandler = new MyHandler();
	}
	
	boolean mShowBlue = true; 
	boolean mExit = false;
	class MyHandler extends Handler
    {
    	public void handleMessage(Message msg) {
    		if (mSensorEnable && mShowBlue) {
    			if (mSensorAlarm) {
    				mInfraredImageView.setImageBitmap(Resource.imageInfraredAlarm);
    			} else {
    				mInfraredImageView.setImageBitmap(Resource.imageInfraredBlue);
    			}
    		} else {
    			mInfraredImageView.setImageBitmap(Resource.imageInfraredEnable);
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
				//mInfraredImageView.setImageBitmap(Resource.imageInfraredDisable);
				mSensorEnable = false;	
			} else {
				//mInfraredImageView.setImageBitmap(Resource.imageInfraredEnable);
				mSensorEnable = true;
			}
			return;
		}
		while (i>=0 && i<dat.length) {
			int pid = Tool.builduInt(dat[i], dat[i+1]);
			switch (pid) {
			case 0x0401:
				if (dat[i+2] == 0) {	//结点被禁止
				//	mInfraredImageView.setImageBitmap(Resource.imageInfraredDisable);
					mSensorEnable = false;
				} else {
				//	mInfraredImageView.setImageBitmap(Resource.imageInfraredEnable);
					mSensorEnable = true;
				}
				i += 3;
				break;
			case 0x0402:
				if (dat[i+2] != 0) {
					/* 报警*/
					mSensorAlarm = true;
					if (mAlarmCheckBox.isChecked()) {
						/* 发送告警通知和短信*/
						String msg = this.mNode.mNetAddr + ":检测到入侵";
						Tool.notify("入侵告警", msg);
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
				
				break;
			default:
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
		super.sendRequest(0x0001, new byte[]{0x04,0x01, 0x04,0x02});
		Message msg = Message.obtain();
		mMyHandler.sendMessageDelayed(msg, 300);
	}

}
