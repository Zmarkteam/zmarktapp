package com.embedkit.zigbee;

import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TabHost;

public class DistanceSensorPresent extends NodePresent implements OnClickListener {
	static final String TAG = "DistanceSensorPresent";
	
	View mInfoView;
	View mCtrlView;
	View mConfigView;

	Button mBtnEnable;
	Button mBtnDisable;
	
	EditText mDistanceEditText;
	EditText mLimitEditText;
	EditText mEditTextNumber;
	
	CheckBox mCheckBox;
	
	DistanceSensorPresent(Node n) {
		super(R.layout.distance_sensor, n);
		// TODO Auto-generated constructor stub
		
		mInfoView = super.mView.findViewById(R.id.DistanceSensorInfoView);
		mCtrlView = super.mView.findViewById(R.id.DistanceSensorCtrlView);
		
		mDistanceEditText = (EditText)super.mView.findViewById(R.id.DistanceSensorEditText);
		
		mLimitEditText = (EditText)super.mView.findViewById(R.id.DistenceLimitEditText);
		mEditTextNumber = (EditText)super.mView.findViewById(R.id.DistenceSensorAlarmNumberEditText);
		
		mBtnEnable = (Button)super.mView.findViewById(R.id.BtnEnable);
		mBtnDisable = (Button)super.mView.findViewById(R.id.BtnDisable);
		
		mBtnEnable.setEnabled(false);
		mBtnDisable.setEnabled(true);
		
		mCheckBox = (CheckBox) super.mView.findViewById(R.id.DisSensorAlarmCheckBox);
		
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
				    	 return mCtrlView; 
				     }
				}));
		tabHost.setCurrentTab(1);
		
		mBtnEnable.setOnClickListener(this);
		mBtnDisable.setOnClickListener(this);
		
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == mBtnEnable) {
			super.sendRequest(0x0002, new byte[]{0x06,0x01, 0x03});
			mBtnEnable.setEnabled(false);
			mBtnDisable.setEnabled(true);
		} 
		if (v == mBtnDisable) {
			super.sendRequest(0x0002, new byte[]{0x06,0x01, 0x00});
			mBtnEnable.setEnabled(true);
			mBtnDisable.setEnabled(false);
		}
	}	
	
	@Override
	void setup() {
		// TODO Auto-generated method stub
		super.sendRequest(0x0002, new byte[]{0x06,0x01, 0x03});
	}

	@Override
	void setdown() {
		// TODO Auto-generated method stub
		super.sendRequest(0x0002, new byte[]{0x06,0x01, 0x00});
	}

	@Override
	void procData(int req, byte[] dat) {
		// TODO Auto-generated method stub

	}

	@Override
	void procAppMsgData(int addr, int cmd, byte[] dat) {
		// TODO Auto-generated method stub
		int param;
		int value;
		
		int limit;
		
		if (addr != super.mNode.mNetAddr) return;
		if (cmd != 0x0003) return;
		if (dat.length < 3) return;
		
		
		limit = Integer.parseInt(mLimitEditText.getText().toString());
		
		param = Tool.builduInt(dat[0], dat[1]); //dat[0]<<8 | dat[1]; 
		value = Tool.builduInt(dat[2]); //dat[2];
		
		Log.d(TAG, "current distance :" + value);
		if (param == 0x0602) {
			if (value == 0xFF) 
				mDistanceEditText.setText("");
			else {
				mDistanceEditText.setText(String.format("%d", value));
				
				if (value < limit && mCheckBox.isChecked()) {
					String title = "距离警告";
					String msg = "当前距离"+value+"低于告警值"+limit;
					Tool.notify(title, msg);
					Tool.playAlarm(3);
					
					
					String mNumber = mEditTextNumber.getText().toString();
					if (mNumber != null && mNumber.length()>0) {
						Tool.sendShortMessage(mNumber, title+":"+msg);
					}
				}
			}
		} 
	}
}
