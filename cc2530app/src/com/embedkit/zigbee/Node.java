package com.embedkit.zigbee;

import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

/*
 * �����ڵ�����ݱ�ʾ 
 */
public class Node {
	private static final String TAG = "Zbnode";
	 
	/*�������*/
	final static int ZB_NODE_TYPE_COORDINATOR 	= 0;
	final static int ZB_NODE_TYPE_ROUTER		= 1;
	final static int ZB_NODE_TYPE_ENDDEVICE		= 2;
	
	/* �豸����*/
	static final int DEV_NONE			= 0;	
	static final int DEV_TEMPSENSOR		= 1;
	static final int DEV_LIGHT			= 2;
	static final int DEV_SWITCH			= 3;
	static final int DEV_INFRAREDSENSOR = 4;
	static final int DEV_CO2SENSOR		= 5;
	static final int DEV_DISTANCESENSOR = 6;
	static final int DEV_HUMIDITYSENSOR = 7;
	
	static final int DEV_RFID			= 8;
	static final int DEV_FPM10A         = 9;
	static final int DEV_HIGHRFID       = 10;
	List<Node> _childNode;	//���ӽ������		
	
	int		mHardVer;
	int		mSoftVer;
	
	byte[]	mIEEEAddr;
	int 	mNodeType;			//�ڵ�����
	int		mNetAddr;			//�����ַ
	
	int 	mDevType;			//�豸����
	
	//EndPoint[] mEndPoints;
	
	/* ��ʾ�������*/
	//Bitmap	mBmp; 				//����Ӧ����ʾͼ��
	int mLeft, mTop, mWidth, mHeight;
	int		mDeepth;		//��������������

	
	Node(int netaddr) 
	{
		mNetAddr = netaddr;
		mIEEEAddr = new byte[8];
		_childNode = new LinkedList<Node>();
	}
	
	Node(int netaddr, int nodetype) 
	{
		mNetAddr = netaddr;
		mNodeType = nodetype;
		mIEEEAddr = new byte[8];
		_childNode = new LinkedList<Node>();
	}
	
	static String getNodeTypeString(Node n)
	{
		switch(n.mNodeType) {
		case Node.ZB_NODE_TYPE_COORDINATOR:
			return "Э����";
		case Node.ZB_NODE_TYPE_ROUTER:
			return "·����";
		default:
			return "�ն˽��";
		}
	}
	
	static String getDeviceTypeString(Node n) 
	{
		switch(n.mDevType) {
		case Node.DEV_TEMPSENSOR:
			return "�¶ȴ�����";
		case Node.DEV_LIGHT:
			return "�ƹ��豸";
		case Node.DEV_SWITCH:
			return "�����豸";
		case Node.DEV_INFRAREDSENSOR:
			return "���崫����";
		case Node.DEV_CO2SENSOR:
			return "���崫����";
		case Node.DEV_DISTANCESENSOR:
			return "���봫����";
		case Node.DEV_HUMIDITYSENSOR:
			return "ʪ�ȴ�����";
		case Node.DEV_RFID:
			return "RFID�豸";
		case Node.DEV_FPM10A:
			return "ָ��ʶ���豸 ";
		case Node.DEV_HIGHRFID:
			return "����ƵRFID�豸";
		default:
			return "δ֪�豸";
		}
	}
	static String getHardVer(Node n)
	{
		return String.format("%02X.%02X", (n.mHardVer>>8)&0xff, n.mHardVer&0xff);
	}
	static String getSoftVer(Node n)
	{
		return String.format("%02X.%02X", (n.mSoftVer>>8)&0xff, n.mSoftVer&0xff);
	}
	
	void setIconRect(int left, int top, int width, int height)
	{
		mLeft = left;
		mTop = top;
		mWidth = width;
		mHeight = height;
		Log.d(TAG, "("+left+","+top+","+width+","+height+")");
	}
	void DrawIcon(Canvas cv)
	{
		Bitmap mb; 
		String desc;
		if (mNodeType == Node.ZB_NODE_TYPE_COORDINATOR) {
			mb = Resource.imageCoordinator;
			desc = Node.getNodeTypeString(this);
		} else {
			switch(mDevType) { 
			case Node.DEV_TEMPSENSOR:
				mb = Resource.imageDeviceTempSensor;
				break;
			case Node.DEV_LIGHT:
				mb = Resource.imageDeviceLight;
				break;
			case Node.DEV_SWITCH:
				mb = Resource.imageDeviceSwitch;
				break;
			case Node.DEV_CO2SENSOR: 
				mb = Resource.imageDevCo2Sensor;
				break;
			case Node.DEV_INFRAREDSENSOR:
				mb = Resource.imageDevInfrared;
				break;
			case Node.DEV_DISTANCESENSOR:
				mb = Resource.imageDevDistance;
				break; 
			case Node.DEV_HUMIDITYSENSOR:
				mb = Resource.imageDevHumidity;
				break;
			case Node.DEV_RFID:
				mb = Resource.imageDevRfid;
				break;
			case Node.DEV_FPM10A:
				mb = Resource.imageDevFpm10a;
				break;
			case Node.DEV_HIGHRFID:
				mb = Resource.imageDevHighfrid;
			default:
				mb = Resource.imageDevUnknow;
				break;
			}
			desc = Node.getDeviceTypeString(this);
		}
		
		cv.drawBitmap(mb, mLeft+mWidth/2-mb.getWidth()/2, mTop, null);
		 
		Paint p=new Paint();
		p.setColor(Color.RED);
		p.setTextAlign(Paint.Align.CENTER);   
		cv.drawText(String.format("��ַ:%d", mNetAddr), mLeft+mWidth/2, mTop+mb.getHeight()+10, p);
		cv.drawText(desc, mLeft+mWidth/2, mTop+mb.getHeight()+25, p);
	}
}
