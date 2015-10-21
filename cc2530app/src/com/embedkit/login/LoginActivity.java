package com.embedkit.login;

import com.embedkit.zigbee.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;

public class LoginActivity extends Activity implements OnTouchListener{
		static final String TAG = "LoginActivity";
		
    	static final   float[] BT_SELECTED = new float[] {1,0,0,0,50,0,1,0,0,50,0,0,1,0,50,0,0,0,1,0};
		static final   float[] BT_NOT_SELECTED = new float[] {1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0};
		static final   ColorMatrixColorFilter selfilter = new ColorMatrixColorFilter(BT_SELECTED);
		static final   ColorMatrixColorFilter unselfilter = new ColorMatrixColorFilter(BT_NOT_SELECTED);
 
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        
	      //  this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	        this.setContentView(R.layout.login);
	        
	        ImageButton btnlogin = (ImageButton) this.findViewById(R.id.btnlogin);
	        ImageButton btnexit = (ImageButton)this.findViewById(R.id.btnexit);
	        btnlogin.setOnTouchListener(this);
	        btnexit.setOnTouchListener(this);
	        
	        btnlogin.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(); 
					intent.setClass(LoginActivity.this, com.embedkit.zigbee.ZigBeeTool.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  //注意本行的FLAG设置
					startActivity(intent);
					LoginActivity.this.finish();
				}
	        });
	        btnexit.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					// TODO Auto-generated method stub
					LoginActivity.this.finish();
				}
	        });
	  }

	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		int ac = event.getAction();
		if (ac == MotionEvent.ACTION_DOWN) {
			v.getBackground().setColorFilter(selfilter);
			v.setBackgroundDrawable(v.getBackground());
		} else if (ac == MotionEvent.ACTION_UP) {
			v.getBackground().setColorFilter(unselfilter);
			v.setBackgroundDrawable(v.getBackground());
		}
		return false;
		
	}     
}
