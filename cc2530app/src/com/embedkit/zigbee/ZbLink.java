package com.embedkit.zigbee;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class ZbLink implements Runnable
{
	private static final String TAG = "ZbWorker";
	
	
    private final   Object mLockInit = new Object();
    private final   Object mLockConn = new Object();
    //private Looper  mLooper;
    
    private static final int CONNECT_DISCONNECT = 0;  //
    private static final int CONNECT_CONNECTING = 1;   //
    private static final int CONNECT_CONNECTED  = 2;   //
    private static final int CONNECT_CONNECTCLOSEING = 3;

    
    private static final int REQUEST_CONNECT    = 1;
    private static final int REQUEST_DISCONNECT = 2;
    private static final int REQUEST_SENDDATA	= 3;
    
    private static final int NOTIFY_CONNECT_ON	= 0x81;
    private static final int NOTIFY_CONNECT_OFF = 0x82;
    private static final int NOTIFY_CONNECT_DATA= 0x83;
    private static final int NOTIFY_CONNECT_FAIL= 0x84;
    
    
    private int     mConnectStatus = CONNECT_DISCONNECT;
    
    private Socket			mSocket;
    private InputStream 	mReader;
    private OutputStream 	mWriter;
    
    private Receiver		mReceiver;
    
    private ConnectListener mConnectListener;
    
    private WorkerHandler mWorkerHandler;
 
    /**
    * Creates a worker thread with the given name. The thread
    * then runs a {@link android.os.Looper}.
    * @param name A name for the new thread
     * @throws InterruptedException 
     * @throws InterruptedException 
    */
    ZbLink(ConnectListener li) 
    {
    	mConnectListener = li;
        Thread t = new Thread(null, this, "LinkHandlerThread");
        //t.setPriority(Thread.MIN_PRIORITY);
        t.start();
   
        synchronized(mLockInit){
        	while (mWorkerHandler == null) {
        		try {
        			mLockInit.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
        } 
    }
    
    
    public void run() 
    {
        Looper.prepare();
        synchronized(mLockInit){
        	mWorkerHandler = new WorkerHandler(Looper.myLooper());
        	mLockInit.notifyAll();
        }
        Looper.loop();
    }
  
    private int linkOpen(String host, int port)//建立连接
    {
    	try {
			//mSocket = new Socket(host, port);
    		mSocket = new Socket();
    	
    		mSocket.connect(new InetSocketAddress(host, port)/*, 30*1000*/);
    		
		} catch (Exception e) {
			//e.printStackTrace();
			Log.e(TAG, "connect host : "+host+":"+port);
				
			return -1;
		}
		try {
			mReader = mSocket.getInputStream();
			mWriter = mSocket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -2;
		}
		return 0;
    }
    private void linkClose()//关闭连接
    {
    	try {
    		mSocket.shutdownInput();
    		mSocket.shutdownOutput();
			mSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mReader = null;
		mWriter = null;
    }
    
  //  public void setConnectListener(ConnectListener li)
  //  {
  //  	mConnectListener = li;
  //  }
    /******    main thread request call   ******/
    /******    main thread request call   ******/
    /******    main thread request call   ******/

    public int requestConnect(String host, int port)//应答链接
    {
        Message msg;
        int ret;
        synchronized(mLockConn){
	        if (mConnectStatus == CONNECT_DISCONNECT) {
	            msg = Message.obtain();
	            msg.what = REQUEST_CONNECT;
	            msg.arg1 = port;
	            msg.obj = host;
	           
	            mConnectStatus = CONNECT_CONNECTING;
	            mWorkerHandler.sendMessage(msg);
	         	
	            ret = 0;
	        } else ret = 1;
        }
        return ret;
    }
    public int requestDisConnect()//无应答连接
    {
    	Message msg;
    	int ret;
    	
    	synchronized(mLockConn){
	        if (mConnectStatus == CONNECT_CONNECTED) {
	     
	            msg = Message.obtain();
	            msg.what = REQUEST_DISCONNECT;
	            
	            mConnectStatus = CONNECT_CONNECTCLOSEING;
	            mWorkerHandler.sendMessage(msg);
	         
	            ret = 0;
	        } else ret = 1; 
    	}
    	return ret;
    }
    
    public int requestSendData(byte[] b)//发送数据
    {
    	if (mConnectStatus != CONNECT_CONNECTED) {
    		return 1;
    	}
    	Message msg;
    	msg = Message.obtain();
    	msg.what = REQUEST_SENDDATA;
    	msg.obj = b;
    	
    	mWorkerHandler.sendMessage(msg);
    	return 0;
    }
    
    /******    do main thread call   ******/
    /******    do main thread call   ******/
    /******    do main thread call   ******/
    
    private void doRequestConnect(Message msg)
    {
    	String host = (String)msg.obj;
    	int port = msg.arg1;
    	int ret;
    	
    

    	ret = linkOpen(host, port);
    	msg = Message.obtain();
    	if (ret < 0) {
    		mConnectStatus = CONNECT_DISCONNECT;
    		/* notify main thread */
    		msg.what = NOTIFY_CONNECT_FAIL;	
    	} else {
    		mConnectStatus = CONNECT_CONNECTED;
    		mReceiver = new Receiver();
    		msg.what = NOTIFY_CONNECT_ON;
    	}
		/* notify main thread */
    	mWorkerHandler.sendMessage(msg);
    }
    private void doRequestDisconnect()
    {
    	Message msg;
 
    	mConnectStatus = CONNECT_DISCONNECT;
    	linkClose();
    	
		/* notify main thread */
		msg = Message.obtain();
		msg.what = NOTIFY_CONNECT_OFF;
		mWorkerHandler.sendMessage(msg);   	
    }
    private void doRequestSendData(Message msg)
    {
    	byte dat[] = (byte[])msg.obj;
    
    	if (mConnectStatus == CONNECT_CONNECTED) {
    		try {
				mWriter.write(dat);
				mWriter.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				/* notify link close */
				mConnectStatus = CONNECT_DISCONNECT;
				linkClose();
				/* notify main thread */
				msg = Message.obtain();
				msg.what = NOTIFY_CONNECT_OFF;
				mWorkerHandler.sendMessage(msg); 
			}
    	}
    }
    interface ConnectListener {
    	//void onConnectOn();
    	//void onConnectFail();
    	void onConnectStatu(boolean st);
    	void onConnectData(byte[] b);
    }
 
    
    /*  request send thread */
    private class WorkerHandler  extends Handler
    {
    	private static final String TAG = "LinkWorkHandler";
        public WorkerHandler(Looper looper){ 
            super(looper); 
        } 
        public void handleMessage (Message msg)
        {
     
            switch(msg.what)
            {
            case REQUEST_CONNECT:
            	doRequestConnect(msg);
                break;
            case REQUEST_DISCONNECT:
                doRequestDisconnect();
                break;
            case REQUEST_SENDDATA:
            	doRequestSendData(msg);
            	break;
                    
            /* response call back */
    		case NOTIFY_CONNECT_ON:
    			//Log.d(TAG, "thread("+Thread.currentThread().getName()+"):connect on");
    			mConnectListener.onConnectStatu(true);
    			break;
    		case NOTIFY_CONNECT_FAIL:
    			//Log.d(TAG, "thread("+Thread.currentThread().getName()+"):connect fail");
    			mConnectListener.onConnectStatu(false);
    			break;
    		case NOTIFY_CONNECT_OFF:
    			//Log.d(TAG, "thread("+Thread.currentThread().getName()+"):connect off");
    			mConnectListener.onConnectStatu(false);
    			break;
    		case NOTIFY_CONNECT_DATA:
    			byte dat[] = (byte[])msg.obj;
    			//Log.d(TAG, "thread("+Thread.currentThread().getName()+"):connect recv:"+dat);
    			mConnectListener.onConnectData(dat);
    			break;
    			
            	
            default:
            	Log.e(TAG, "unknow request or notify.");
            	break;
            }
        }
    }
    
    /* data recv thread */
    private class Receiver implements Runnable 
    {
    	static final String TAG = "Receiver";
    	byte[] dat = new byte[256];
    	public Receiver() 
    	{
            Thread t = new Thread(null, this, "LinkReceiver");
            //t.setPriority(Thread.MIN_PRIORITY);
            t.start();
    	}
        public void run()
        {
       
        	for (;mConnectStatus == CONNECT_CONNECTED;) {
	        	int rlen = 0;
	        	
	        	try {
	        		
					rlen = mReader.read(dat);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "thread("+Thread.currentThread().getName()+") terminal by read error.");
					//requestDisConnect();
					mConnectStatus = CONNECT_DISCONNECT;
					mConnectListener.onConnectStatu(false);
					
					break;
				}
				if (rlen < 0){
					//requestDisConnect();
					mConnectStatus = CONNECT_DISCONNECT;
					mConnectListener.onConnectStatu(false);
					Log.d(TAG, "thread("+Thread.currentThread().getName()+") terminal by read -1.");
					break;
				} else if (rlen > 0){
					Message msg;
					byte[] d= new byte[rlen];
					for (int i=0; i<rlen; i++)
						d[i] = dat[i];
					//msg = Message.obtain();
					//msg.what = NOTIFY_CONNECT_DATA;
					//msg.obj = d;
					//mWorkerHandler.sendMessage(msg);
					mConnectListener.onConnectData(d);
				}
        	}
        	mReceiver = null;
        	Log.d(TAG, "LinkReceiver terminal by disconnect...");
        }
        
    }
}
