package com.embedkit.zigbee;
import com.embedkit.zigbee.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


public abstract class NodePresent {
	View mView;
	Node mNode;
	
	static NodePresent FactoryCreateInstance(Node n)
	{
		if (n.mNodeType == Node.ZB_NODE_TYPE_COORDINATOR) {
			return new CoordinatorPresent(n);
		} else if (n.mDevType == Node.DEV_TEMPSENSOR) {
			return new TempSensorPresent(n);
		} else if (n.mDevType == Node.DEV_LIGHT) {
			return new LightDevicePresent( n);
		} else if (n.mDevType == Node.DEV_SWITCH) {
			return new SwitchDevicePresent(n);
		} else if (n.mDevType == Node.DEV_INFRAREDSENSOR) {
			return new InfraredSensorPresent(n);
		} else if (n.mDevType == Node.DEV_CO2SENSOR) {
			return new CO2SensorPresent(n);
		} else if (n.mDevType == Node.DEV_DISTANCESENSOR) {
			return new DistanceSensorPresent(n);
		} else if (n.mDevType == Node.DEV_HUMIDITYSENSOR) {
			return new HumiditySensorPresent(n);
		} else if (n.mDevType == Node.DEV_RFID) {
			return new RfidPresent(n);
		}
		  else if(n.mDevType == Node.DEV_FPM10A){
		    return new Fpm10aPresent(n);
		} else {
			return new CoordinatorPresent(n);
		}
	}
	NodePresent(int id, Node n)
	{
		LayoutInflater inflater = LayoutInflater.from(ZigBeeTool.getInstance().getBaseContext());
		mView = inflater.inflate(id, null);
		mNode = n;
		
		_commNodeInfoSet(mView, mNode);
	} 
	private void _commNodeInfoSet(View v, Node n)
	  {
	        
		    ((EditText)(v.findViewById(R.id.EditText_IEEEAddr))).
		    setText(String.format("%02X:%02X:%02X:%02X:%02X:%02X:%02X:%02X", 
			n.mIEEEAddr[0],n.mIEEEAddr[1],
			n.mIEEEAddr[2],n.mIEEEAddr[3],n.mIEEEAddr[4],
			n.mIEEEAddr[5],n.mIEEEAddr[6],n.mIEEEAddr[7]));
	
		   	((EditText)(v.findViewById(R.id.EditText_NetAddr))).
		    setText(String.format("%d", n.mNetAddr));
		    
		    ((EditText)(v.findViewById(R.id.EditText_NodeType))).
		    setText(Node.getNodeTypeString(n));	
		    
		    ((EditText)(v.findViewById(R.id.EditText_DeviceType))).
		    setText(Node.getDeviceTypeString(n));
		    
		    ((EditText)(v.findViewById(R.id.EditText_HardVer))).
		    setText(Node.getHardVer(n));
		    
		    ((EditText)(v.findViewById(R.id.EditText_SoftVer))).
		    setText(Node.getSoftVer(n));
	  }
	
	   void sendRequest(int cmd, byte[]dat)
		{
			byte[] data = new byte[dat.length+4];
			data[0] = (byte) (mNode.mNetAddr>>8);
			data[1] = (byte) mNode.mNetAddr;
			data[2] = (byte) (cmd>>8);
			data[3] = (byte) cmd;
			for (int i=0; i<dat.length; i++)
				data[4+i] = dat[i];
			
			ZigBeeTool.getInstance().mZbThread.requestAppMessage(2, data);
		}
	   
	  abstract void setup();
	  abstract void setdown();
	  
	  abstract void procData(int req, byte[]dat);
	  abstract void procAppMsgData(int addr, int cmd, byte[] dat);
}
