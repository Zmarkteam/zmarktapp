package com.embedkit.zigbee;

import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class UnknowDevicePresent extends NodePresent {

	UnknowDevicePresent(Node n) {
		super(R.layout.unknowdevice, n);
		// TODO Auto-generated constructor stub	
	}

	@Override
	void setup() {
		// TODO Auto-generated method stub

	}

	@Override
	void setdown() {
		// TODO Auto-generated method stub

	}

	@Override
	void procData(int req, byte[] dat) {
		// TODO Auto-generated method stub

	}

	@Override
	void procAppMsgData(int addr, int cmd, byte[] dat) {
		// TODO Auto-generated method stub

	}

}
