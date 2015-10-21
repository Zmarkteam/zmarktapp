package com.embedkit.zigbee;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

import com.embedkit.zigbee.CO2SensorPresent.MyHandler;

import android.R.integer;
import android.R.string;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView;
import android.database.Cursor;
import android.text.InputType;
public class Fpm10aPresent extends NodePresent {
	static final String TAG = "FPM10A";
	
	DatabaseHelper mCardDB = new DatabaseHelper();
	
	View mInfoView;
	View mCtrlView;
	View mMagView;
	
	ImageView mRfidImageView;

	Button mBtnDisable;  
	Button mBtnEnable;
	
	
	boolean mSensorEnable = true;
	
	
	
	ListView mListView;
	SimpleAdapter mAdapter;
	
	TabHost tabHost;
	EditText mIDEditText;
	EditText mPhoneEditText;
	
	Button   mAddBtn;
	Button   mDelBtn;
	int weizhi;
	
	
	
	Fpm10aPresent(Node n) {
		super(R.layout.fpm10a, n);
		// TODO Auto-generated constructor stub
		
		mInfoView = super.mView.findViewById(R.id.fpm10aInfoView);
		mCtrlView = super.mView.findViewById(R.id.fpm10aCtrlView);
		mMagView  = super.mView.findViewById(R.id.fpm10aMagView);
		
		mRfidImageView = (ImageView)mCtrlView.findViewById(R.id.rfidImageView);
		
		mListView = (ListView)mMagView.findViewById(R.id.listView);
		
		mIDEditText = (EditText)mMagView.findViewById(R.id.cardId);
		mPhoneEditText = (EditText)mMagView.findViewById(R.id.phone);
		//mPhoneEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
		mAddBtn = (Button)mMagView.findViewById(R.id.fpm10aBtnadduser);
		//mDelBtn = (Button)mMagView.findViewById(R.id.fpm10aBtndelectuser);
		
		mBtnEnable = (Button)mCtrlView.findViewById(R.id.fpm10aBtnEnable);
		mBtnEnable.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				/* 响应启用指纹识别 按钮*/
				Log.d(TAG, "enable fpm10a ....");
				
					Fpm10aPresent.super.sendRequest(0x0002, 
							new byte[]{0x09,0x01, 0x01});//发送启动指纹识别指令
				
			}
		});
		
		mBtnDisable = (Button)mCtrlView.findViewById(R.id.fpm10aBtnDisable);
		mBtnDisable.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				/* 响应禁止指纹识别按钮 */
				Log.d(TAG, "disable fpm10a ....");
				Fpm10aPresent.super.sendRequest(0x0002, 
							new byte[]{0x09,0x01, 0x00});//发送启动禁止指纹识别指令
				
			}
		});
			
		tabHost = ((TabHost)super.mView.findViewById(android.R.id.tabhost));
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
		
		tabHost.addTab(tabHost.newTabSpec("2")
				.setIndicator("", new BitmapDrawable(Resource.imageNodeConfig))
				.setContent(new TabHost.TabContentFactory(){
					public View createTabContent(String tag) {
						// TODO Auto-generated method stub
						return mMagView;
					}
				}));
		
		tabHost.setCurrentTab(1);	
		mMyHandler = new MyHandler();
		
		
	
	
		mCardDB.init();
		mAdapter = new SimpleAdapter(ZigBeeTool.getInstance(), mCardDB.mList,
				R.layout.card_list_item, new String[]{"ID","PHONE"}, new int[]{R.id.ID, R.id.PHONE});
		
		mListView.setAdapter(mAdapter);
		mListView.setOnItemLongClickListener(new OnItemLongClickListener(){
			public boolean onItemLongClick(AdapterView parent, View view, int position,
					   long id) {
				int x = new Integer(mCardDB.mList.get(position).get("ID"));
				Fpm10aPresent.super.sendRequest(0x0002, 
				new byte[]{0x09,0x04,0x00,0x00,(byte)x});
				weizhi = position;
				mAdapter.notifyDataSetChanged();
				return true;
			}
		});
		
		
		mAddBtn.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				/* 响应注册指纹按钮 */
				Log.d(TAG, "add user fpm....");
				
				if(mIDEditText.getText().toString().equals(""))
				{
					Toast.makeText(ZigBeeTool.getInstance(), "请先输入用户信息！", 
						Toast.LENGTH_SHORT).show();
					     return ;
				}
				int x = new Integer(mIDEditText.getText().toString());
				int z = x>>8;
				String id = mIDEditText.getText().toString();
				id=mCardDB.find(id);
				if(id != ""){Toast.makeText(ZigBeeTool.getInstance(), "输入的ID已经存在，请重新输入！", 
						Toast.LENGTH_SHORT).show();}
				else if(x>120)
				{
					Toast.makeText(ZigBeeTool.getInstance(), "输入的ID超过存储范围！", 
							Toast.LENGTH_SHORT).show();
				}
				else{
					Fpm10aPresent.super.sendRequest(0x0002, 
						new byte[]{0x09,0x03,0x00,(byte)z,(byte)x});
				/*if (mPhoneEditText.getText().length() == 0
						|| mIDEditText.getText().length() == 0) {
					Toast.makeText(ZigBeeTool.getInstance(), "请将手指放在指纹录入模块上！", 
							Toast.LENGTH_SHORT).show();
					return;
				}*/
				
				HashMap<String, String> m = new HashMap<String, String>();
				m.put("ID", mIDEditText.getText().toString());
				m.put("PHONE", mPhoneEditText.getText().toString());
				mCardDB.add(mIDEditText.getText().toString(),
						mPhoneEditText.getText().toString());
				mAdapter.notifyDataSetChanged();
				
				Toast.makeText(ZigBeeTool.getInstance(), "添加用户成功，请将手指放在指纹录入模块上！", 
						Toast.LENGTH_LONG).show();
				mPhoneEditText.setText("");
				mIDEditText.setText("");
				} 
			}
		});
		/*mDelBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				响应删除指纹id按钮
				Log.d(TAG, "delete user fpm....");

			}
			
		});*/
		
		mP1.setOnCompletionListener(onCompletion);
		mP2.setOnCompletionListener(onCompletion);
	}
 
	
	
	@Override
	void setup() {
		// TODO Auto-generated method stub
		super.sendRequest(0x0001, new byte[]{0x09,0x01}); 
		Message msg = Message.obtain();
		mMyHandler.sendMessageDelayed(msg, 300);
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
	//	Log.d(TAG,"command:"+ cmd + " dat:" + Tool.byte2string(dat));
		
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
		while (i>=0 && i < dat.length)
		{
			int pid = Tool.builduInt(dat[i], dat[i+1]);
			if (pid == 0x0901) {
				if (dat[i+2] == 0) mSensorEnable = false;
				else mSensorEnable = true;
				i+=3;
			} else if (pid == 0x0902) {
				if(dat[i+2] ==1)
				{   
					 int z = new Integer(dat[i+4]);
					 int x = new Integer(dat[i+3]);
                         x=(x<<8)|z;
					//x=mCardDB.find(x);
                    mP1.start();     
					Toast.makeText(ZigBeeTool.getInstance(),"用户  "+mCardDB.find(""+x+"")+"  合法用户！", /*"指纹识别成功，合法用户！",*/ 
						Toast.LENGTH_SHORT).show();}
				
				else{
				mP2.start(); 	
				Toast.makeText(ZigBeeTool.getInstance(), "指纹识别失败，非法用户！", 
							Toast.LENGTH_SHORT).show();	
				
				}
				//card[0] = dat[i+2];
				//card[1] = dat[i+3];
				//将主动上报的信息弹出对话框告诉用户添加质问是否成功
				//String id = Tool.byte2string(card);
				/*String id = String.format("%02X%02X%02X%02X", 
						card[0], card[1], card[2], card[3]);
				Log.d(TAG, "Card:"+id);
				procCardId(id);*/
				i+=6;
			} else if(pid ==0x0903){
				if(dat[i+2] ==1)
				{Toast.makeText(ZigBeeTool.getInstance(), "指纹录入成功！", 
						Toast.LENGTH_SHORT).show();}
				else{
				Toast.makeText(ZigBeeTool.getInstance(), "指纹录入失败，请调整您的手指重新录入", 
							Toast.LENGTH_SHORT).show();	
				}
				//byte[] data = new byte[2];
				//data[0] = dat[i+2];
				//data[1] = dat[i+3];
			    //将主动上报的信息弹出对话框告诉用户删除质问是否成功
				i+=6;
			}
		      else if(pid ==0x0904){
		    	if(dat[i+2] ==1)
				{   
		    		mCardDB.remove(mCardDB.mList.get(weizhi).get("ID"));
					mCardDB.mList.remove(weizhi);
					mAdapter.notifyDataSetChanged();
		    		Toast.makeText(ZigBeeTool.getInstance(), "指纹删除成功！", 
							Toast.LENGTH_SHORT).show();}
				else{
				Toast.makeText(ZigBeeTool.getInstance(), "指纹删除失败，请重试！", 
							Toast.LENGTH_SHORT).show();	
					} 
					//byte[] data = new byte[2];
					//data[0] = dat[i+2];
					//data[1] = dat[i+3];
				    //将主动上报的信息弹出对话框告诉用户删除质问是否成功
					i+=6;
				}
		      else return;
	  }
		
	}

	static MediaPlayer mP1 = MediaPlayer.create(ZigBeeTool.getInstance()
			.getBaseContext(), R.raw.allow);
	static MediaPlayer mP2 = MediaPlayer.create(ZigBeeTool.getInstance()
			.getBaseContext(), R.raw.jingbao);
	
	static MediaPlayer.OnCompletionListener onCompletion = new MediaPlayer.OnCompletionListener(){
		public void onCompletion(MediaPlayer arg0) {
			try {
				arg0.prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	
	boolean mShowBlue = true; 
	boolean mExit = false;
	class MyHandler extends Handler
	{
		public void handleMessage(Message msg) {
			if (mSensorEnable && mShowBlue) {
				mRfidImageView.setImageBitmap(Resource.imageCo2SensorBlue);
			} else {
				mRfidImageView.setImageBitmap(Resource.imageCo2SensorEnable);
			}
			mShowBlue = !mShowBlue;
			
			if (!mExit) {
				Message msg2 = Message.obtain();
				mMyHandler.sendMessageDelayed(msg2, 300);
			}
			
		}
	}
	MyHandler mMyHandler;
	
	/***********************************************************************
	 * 
	 *                数据库操作
	 * 
	 ***********************************************************************/
	
	private static class DatabaseHelper extends SQLiteOpenHelper {  
        private static int version = 1;  
        private static final String mDBName = "Fpm10aDB";
  
        ArrayList<HashMap<String,String>> mList = 
        	new ArrayList<HashMap<String,String>>();
        
        public DatabaseHelper() {  
            super(ZigBeeTool.getInstance(), mDBName, null, version);  
        }  
        
        //在数据库第一次生成的时候会调用这个方法，一般我们在这个方法里生成数据表  
        @Override  
        public void onCreate(SQLiteDatabase db) {  
            // 创建数据库的SQL  
            String sql = "CREATE TABLE tcard(id TEXT , phone TEXT )";  
            Log.i(TAG, "tcard:createDB sql:"+sql);  
            db.execSQL(sql);  
            //db.close();  
        }  
        //当数据库需要升级时，Android系统会自动调用这个方法，一般我们在这里写删除表并创建新表的操作  
        @Override  
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
        }
        
        public void add(String id, String phone) {
        	SQLiteDatabase db = this.getWritableDatabase();
        	String sql = String.format(
        			"INSERT INTO tcard(id, phone) VALUES('%s','%s')", id, phone);
        	try {
        		db.execSQL(sql);
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        	HashMap<String,String> m = new HashMap<String,String>();
        	m.put("ID", id);
        	m.put("PHONE", phone);
        	mList.add(m);
        	db.close();
        }
        public void remove(String id) {
        	String[] pa = new String[1];
        	pa[0] = id;
           	SQLiteDatabase db = this.getWritableDatabase();
        	db.delete("tcard", "id=?", pa);
        	db.close(); 	
        }
       
        
        public void init()
        {
        	SQLiteDatabase db = this.getReadableDatabase();
        	Cursor cr = db.query("tcard", new String[]{"id", "phone"}, 
        			null, null, null, null, null);
        	cr.moveToFirst();
        	int cnt = cr.getCount();
        	for (int i=0; i<cnt; i++){
        		HashMap<String,String> m = new HashMap<String,String>();
        		String id = cr.getString(0);
        		String phone = cr.getString(1);
        		m.put("ID", id);
        		m.put("PHONE", phone);
        		this.mList.add(m);
        		cr.moveToNext();
        	}
        	cr.close();
        	db.close();
        	return ;
        }
        
        public String find(String id)
        {
        	String phone = "";
        	SQLiteDatabase db = this.getReadableDatabase();
        	String[] pa = new String[1];
        	pa[0] = id;
        	Cursor cr = db.query("tcard", new String[]{"phone"}, 
        			"id=?", pa, null, null, null);
        	cr.moveToFirst();
        	int cnt = cr.getCount();
        	if (cnt != 0) {
        		phone = cr.getString(0);
        	}
        	cr.close();
        	db.close();
        	return phone;
        }
    }
	
}
