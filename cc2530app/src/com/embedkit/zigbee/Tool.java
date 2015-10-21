package com.embedkit.zigbee;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.ColorMatrixColorFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;

public class Tool {

	static final   float[] BT_SELECTED = new float[] {1,0,0,0,50,0,1,0,0,50,0,0,1,0,50,0,0,0,1,0};
	static final   float[] BT_NOT_SELECTED = new float[] {1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0};
	static final   ColorMatrixColorFilter selfilter = new ColorMatrixColorFilter(BT_SELECTED);
	static final   ColorMatrixColorFilter unselfilter = new ColorMatrixColorFilter(BT_NOT_SELECTED);

	
	static int builduInt(byte b)
	{
		return (b&0xff);
	}
	static int builduInt(byte b1, byte b2)
	{
		return (((b1&0xff)<<8) | (b2&0xff));
	}

	
	static String byte2string(byte[] b)
	{
		String s = "";
		if (b == null) {
			return "<null>";
		}
		for (int i=0; i<b.length; i++) {
			s += String.format("%02X ", b[i]);
		}
		return s;
	}

	
	
	/***************************************************************/
	/*class TimerHandler extends Handler
    {
    	public void handleMessage(Message msg) {
    		
    	}
    }
	interface TimerListener {
		
	}
	static TimerHandler mTimerHandler = new TimerHandler();
	static void startTimer(TimerListener li, int ms)
	{
		Message msg = Message.obtain();
		msg.obj = li;
		mTimerHandler.sendMessageDelayed(msg, 300);
	}*/
	
	
	
	
	/***************************************************************
	 *             ֪ͨ����
	 */
	
	 
	static NotificationManager mNotificationManager;
	static SmsManager mSmsManager;
	static MediaPlayer mMdeiaPlayer = MediaPlayer.create(ZigBeeTool.getInstance()
			.getBaseContext(), R.raw.alarm_1);
	static MediaPlayer.OnCompletionListener onCompletion = new MediaPlayer.OnCompletionListener(){
		public void onCompletion(MediaPlayer arg0) {
			// TODO Auto-generated method stub
			Log.d("player", "onCompletion...");
			mAlarmCnt--;
			try {
				mMdeiaPlayer.prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (mAlarmCnt > 0) {
				mMdeiaPlayer.start();
			}
		}
	};
	static int mAlarmCnt = 0;
	static void playAlarm(int cnt)
	{
		mMdeiaPlayer.setOnCompletionListener(onCompletion);
		mAlarmCnt = cnt;
		if (cnt == 0) {
			mMdeiaPlayer.setLooping(true);
		} else {
			mAlarmCnt = cnt;
		}
		mMdeiaPlayer.start();
	}
	static void stopAlarm()
	{
		mAlarmCnt = 0;
		mMdeiaPlayer.stop();
		try {
			mMdeiaPlayer.prepare();
			mMdeiaPlayer.seekTo(0);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static int sNotifyId = 1001;
	static void notify(String title, String msg)
	{
		
		if (mNotificationManager == null) {
		    mNotificationManager = (NotificationManager)ZigBeeTool.getInstance()
		       .getSystemService(ZigBeeTool.NOTIFICATION_SERVICE);       
		}
		
		Notification notification = new Notification(R.drawable.icon, title, System.currentTimeMillis());
		PendingIntent intent = PendingIntent.getActivity(ZigBeeTool.getInstance(), 0, new Intent(ZigBeeTool.getInstance(), ZigBeeTool.class), 0);
		notification.setLatestEventInfo(ZigBeeTool.getInstance(), title, msg, intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		//notification.defaults |= Notification.DEFAULT_SOUND ; //ʹ��Ĭ������
		   //��������   
		
        // noticed.sound = Uri.parse("file:///sdcard/notification/ringer.mp3");   
        // noticed.sound = Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI, "6");   
		//mNotificationManager.cancelAll();
		mNotificationManager.notify(sNotifyId++, notification);
		
		//m.notifyWithText(1001, "�錄����SHARETOP�Ǥ���", NotificationManager, null);
	}
	static void sendShortMessage(String mobile, String content)
	{

		if (mSmsManager == null) {
			mSmsManager = SmsManager.getDefault();
		}
		if(content.length() > 70){ 
			//ʹ�ö��Ź��������ж������ݵķֶΣ����طֳɵĶ� 
			ArrayList<String> contents = mSmsManager.divideMessage(content); 
			for(String msg : contents){ 
				//ʹ�ö��Ź��������Ͷ������� 
				//����һΪ���Ž����� 
				//������Ϊ�������� 
				//����������Ϊnull 
				mSmsManager.sendTextMessage(mobile, null, msg, null, null); 
			} 
			//����һ�ι����� 
		}else{ 
			mSmsManager.sendTextMessage(mobile, null, content, null, null); 
		}
	}
	
	
	interface IterateIpAddressListener {
		void iterate(String name, String ip);
	}
	static  void IterateLocalIpAddress(IterateIpAddressListener a)   
    {   
        try   
        {   
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)   
            {   
               NetworkInterface intf = en.nextElement();   
               for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)   
               {   
                   InetAddress inetAddress = enumIpAddr.nextElement();   
                   //if (!inetAddress.isLoopbackAddress())   
                   {   
                       Log.d(intf.getName(), inetAddress.getHostAddress().toString());
                       a.iterate(intf.getName(), inetAddress.getHostAddress().toString());
                   }   
               }   
           }   
        }   
        catch (SocketException ex)   
        {   
            Log.e("WifiPreference IpAddress", ex.toString());   
        }   
     
    }  
	
}
