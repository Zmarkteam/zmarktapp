package com.embedkit.zigbee;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.telephony.SmsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

public class Resource {

	final static Bitmap imageCoordinator = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.coordinator);

	final static Bitmap imageDeviceTempSensor = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.devtempsensor);
	final static Bitmap imageDeviceLight	  = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.devlight);
	final static Bitmap imageDeviceSwitch = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.devswitch);
	final static Bitmap imageDevCo2Sensor = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.devco2sensor);
	final static Bitmap imageDevInfrared = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.devinfrared);
	final static Bitmap imageDevDistance = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.devdistancesensor);
	final static Bitmap imageDevHumidity = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.devhumidity);
	final static Bitmap imageDevRfid = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.devrfid);
	final static Bitmap imageDevUnknow = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.devunknow);
	
	
	final static Bitmap imageLightOn = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.lighton);
	final static Bitmap imageLightOff = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.lightoff);
	
	 
	final static Bitmap imageSwitchEnable = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.switchenable);
	final static Bitmap imageSwitchDisable = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.switchdisable);
	 
	final static Bitmap imageInfraredEnable = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.infraredenable);
	//final static Bitmap imageInfraredDisable = BitmapFactory.decodeResource(ZigBeeTool.
	//		getInstance().getResources(), R.drawable.infrareddisable);
	final static Bitmap imageInfraredAlarm = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.infraredalarm);
	final static Bitmap imageInfraredBlue  = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.infraredalarmblue);
	
	final static Bitmap imageCo2SensorEnable = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.co2sensorenable);
	final static Bitmap imageCo2SensorBlue = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.co2sensorblue);	
	final static Bitmap imageCo2SensorAlarm = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.co2sensoralarm);
	
	
	final static Bitmap imageNodeConfig = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.nodeconfig);
	final static Bitmap imageNodeInfo = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.nodeinfo);
	final static Bitmap imageNodeCurve = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.nodecurve);
	final static Bitmap imageNodeValue = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.nodevalue);

	final static Bitmap imageDevFpm10a = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.devfpm10a);

	final static Bitmap imageDevHighfrid = BitmapFactory.decodeResource(ZigBeeTool.
			getInstance().getResources(), R.drawable.devfpm10a);;
  
	
	/*final static Bitmap imageLight[] = {
		BitmapFactory.decodeResource(ZigBeeTool.
				getInstance().getResources(), R.drawable.lightoff),
		BitmapFactory.decodeResource(ZigBeeTool.
				getInstance().getResources(), R.drawable.lighton)
	};*/
	
	static Bitmap scale(Bitmap bm, int newWidth, int newHeight)
	{
		int width = bm.getWidth();
		int height = bm.getHeight();
		
		if (newWidth == width && newHeight == height) return bm;
		
		float scaleWidth = ((float) newWidth) / width;
	    float scaleHeight = ((float) newHeight) / height;
	   
	    // 取得想要缩放的matrix参数
	    Matrix matrix = new Matrix();
	    matrix.postScale(scaleWidth, scaleHeight);
	    // 得到新的图片
	    Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
	      true);
	    return newbm;
	}
	

}
