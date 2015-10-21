package com.embedkit.zigbee;

import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TabHost.OnTabChangeListener;

public class SwitchDevicePresent extends NodePresent 
	implements OnClickListener {

	View mInfoView;
	View mConfigView;
	Button mBtnEnable;
	Button mBtnDisable;
	Button mBtnSetDevAddr;
	
	EditText  mDistAddrEditText;
	ImageView mSwitchImage;
	
	int mSwitchEnable = 0;
	int mDistAddr = 0;
	int mNewDistAddr; 
	
	SwitchDevicePresent(Node n) {
		super(R.layout.switch_device, n);
		// TODO Auto-generated constructor stub
		mInfoView = super.mView.findViewById(R.id.switchInfoView);
		mConfigView = super.mView.findViewById(R.id.switchConfigView);
		
		mSwitchImage = (ImageView)mConfigView.findViewById(R.id.switchImageView);
		
		mBtnEnable = (Button)mConfigView.findViewById(R.id.switchBtnEnable);
		mBtnEnable.setOnClickListener(this);
		
		mBtnDisable = (Button)mConfigView.findViewById(R.id.switchBtnDisable);
		mBtnDisable.setOnClickListener(this);
		
		mBtnSetDevAddr = (Button)mConfigView.findViewById(R.id.switchBtnSetCtrlDevAddr);
		mBtnSetDevAddr.setOnClickListener(this);
		
		
		mDistAddrEditText = (EditText)mConfigView.findViewById(R.id.switchCtrlDevAddrEditText);
		
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
	}
	
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == this.mBtnEnable) {
			if (mSwitchEnable == 0) {
				super.sendRequest(0x0002, new byte[]{0x03,0x21, 0x01});
				mSwitchEnable = 0x01;
			}
		} else if (v == this.mBtnDisable) {
			if (mSwitchEnable != 0) {
				super.sendRequest(0x0002, new byte[]{0x03,0x21, 0x00});
				mSwitchEnable = 0x00;
			}
		} else if (v == mBtnSetDevAddr) {
			mNewDistAddr = Integer.parseInt(mDistAddrEditText.getText().toString());
			//if (mNewDistAddr != mDistAddr) {
				super.sendRequest(0x0002, new byte[]{0x03,0x11, 
						(byte) (mNewDistAddr>>8), (byte) mNewDistAddr});
				mDistAddr = mNewDistAddr;
			//}
		}
	}


	@Override
	void procAppMsgData(int addr, int cmd, byte[] dat) {
		// TODO Auto-generated method stub
		int pid;
		if (cmd == 0x8001 && dat[0] == 0) {
			for (int i=1; i<dat.length; /*i+=2*/) {
				pid = Tool.builduInt(dat[i], dat[i+1]);
				i += 2;
				if (pid == 0x0321) {
					mSwitchEnable = dat[i];
					if (mSwitchEnable == 0) {
						mSwitchImage.setImageBitmap(Resource.imageSwitchDisable);
					} else {
						mSwitchImage.setImageBitmap(Resource.imageSwitchEnable);
					}
					i += 1;
				} else if (pid == 0x0311) {
					mDistAddr = Tool.builduInt(dat[i], dat[i+1]);
					mDistAddrEditText.setText(String.format("%d", mDistAddr));

					i += 2;
				} else {
					return;
				}
			}
		} 
		if (cmd == 0x8002 && dat[0] == 0) {
			if (mSwitchEnable == 0) {
				mSwitchImage.setImageBitmap(Resource.imageSwitchDisable);
			} else {
				mSwitchImage.setImageBitmap(Resource.imageSwitchEnable);
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
		super.sendRequest(0x0001, new byte[]{0x03,0x11, 0x03,0x21,}); //»ñÈ¡
	}
}
