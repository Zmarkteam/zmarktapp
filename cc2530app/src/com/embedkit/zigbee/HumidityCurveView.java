package com.embedkit.zigbee;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
public class HumidityCurveView extends View {


	static String TAG = "HumidityCurveView";
	
	public HumidityCurveView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		
	}
	public HumidityCurveView(Context c, AttributeSet attr) {
		super(c, attr);

	}
	public HumidityCurveView(Context c, AttributeSet attr, int style) {
		super(c, attr, style);
		

	}
	
	static final int XP = 30;
	static final int YP = 10;
	
	static final int MAX_DATA_CNT = 20;
	
	
	int mXLength, mXStart;
	int mYLength, mYStart;
	
	byte mBaseValue = 0;
	
	byte[] mData = new byte[MAX_DATA_CNT];
	int mDataCnt = 0;
	
	void addData(byte v) {
		if (v <= 0) v = 1;
		if (v > 100) v = 100;
		
		Log.d(TAG, "temp data:"+v);
		
		if (mDataCnt == 0) mBaseValue = v;
		
		for (int i=1; i<MAX_DATA_CNT; i++) {
			mData[i-1] = mData[i];
		}
		mData[MAX_DATA_CNT-1] = v;
		if (mDataCnt<MAX_DATA_CNT) mDataCnt++;
		
		this.invalidate();
	}
	
	void drawCurve(Canvas c)
	{
		int p = mYLength/5;
		int up = p /10;
		int sx, sy, ex, ey;
		
		if (mDataCnt <= 1 ) return;
		Paint paint = new Paint();
		paint.setColor(Color.RED);
		sx = mXStart; sy = mYStart+mYLength/YP*3;
		float pa[] = new float[mDataCnt*2];
		for (int i=0; i<mDataCnt; i++) {
			pa[2*i] = sx + i*mXLength/10;
			pa[2*i+1] = sy - mData[i];
		}
		for(int i=mDataCnt-1; i>0; i--) {
			c.drawLine(pa[2*i], pa[2*i+1], pa[2*(i-1)], pa[2*(i-1)+1], paint);
		}
		
	}
	
	void drawCross(Canvas canvas)
	{
		int w = getWidth();
		int h = getHeight();

		Paint paint = new Paint(); 
		/*
		paint.setColor(Color.RED);
		paint.setStyle(Style.STROKE);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(5);
		canvas.drawRect(new RectF(10, 10, 300, 100), paint);
		
		paint.setStyle(Style.FILL);
		paint.setColor(Color.YELLOW);
		canvas.drawRect(10, 150, 300, 200, paint);
		
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setColor(Color.BLUE);
		canvas.drawRect(new Rect(10, 250, 300, 300), paint);
		
		canvas.drawRoundRect(new RectF(10, 350, 300, 450), 10, 10, paint);
		*/
		
		
		int spaceWidth = w * 5 /100;
		int textWidth = w * 23 /100;
		int curveWidth = w - spaceWidth*2 - textWidth;
		int sh = h * 8 / 100;
		int ph = h - sh * 2;
				/*	-
				 	sh
					-    */
		/* |<- spaceWidth ->|<- textWidth ->| --- cv --- |<- cw ->|<-- cs -->|<- spaceWidth ->| */
				/*   -
				 * 	ph
				 *   -
				 */
		/* |<- spaceWidth ->|<- textWidth ->| --- cv --- |<- cw ->|<-- cs -->|<- spaceWidth ->| */
				/*	-
	 				sh
					-    */
		int cw = curveWidth * 3 / 100;
		int cs = cw + cw/2;
		int cv = curveWidth - cw - cs;
		
		mXStart = spaceWidth + textWidth + cv;
		mYStart = sh + ph / 2;
		
		int dx = cv / (MAX_DATA_CNT-1);
		int dy = ph / 20;
		
		mBaseValue = 50;
		
		/* 文子显示矩形框*/
		paint.setStrokeWidth(2);
		paint.setTextAlign(Align.CENTER);
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.RED);
		int textw = textWidth - textWidth*3/20;
		canvas.drawRoundRect(new RectF(spaceWidth, sh, 
				spaceWidth+textw, sh+ph), 3, 3, paint);
		
		paint.setColor(Color.YELLOW);
		paint.setTextSize(25);
		paint.setStrokeWidth(1);
		paint.setStyle(Style.FILL_AND_STROKE);
		canvas.drawText("当前湿度", spaceWidth+textw/2, sh+ph/2-20, paint);
		if (mDataCnt > 0) { 
			canvas.drawText(String.format("%d%%", mData[MAX_DATA_CNT-1]), spaceWidth+textw/2, sh+ph/2+20, paint);
		}
		
		/* 曲线矩形框*/
		paint.setStrokeWidth(2);
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.RED);
		
		canvas.drawRoundRect(new RectF(spaceWidth+textWidth, sh, 
				spaceWidth+textWidth+cv+cw/2, sh+ph), 3, 3, paint);
		
		paint.setStrokeWidth(1); 
		for (int i=1; i<=10; i++) { //上半部刻度
			int lw = (cw*8/10);
			if (i%2 != 0) lw /= 2;
			canvas.drawLine(mXStart+cw, mYStart-i*dy, 
					mXStart+cw+lw, mYStart-i*dy, paint); 
		}
		for (int i=1; i<=10; i++) {
			int lw = (cw*8/10);
			if (i%2 != 0) lw /= 2;
			canvas.drawLine(mXStart+cw, mYStart+i*dy, 
					mXStart+cw+lw, mYStart+i*dy, paint); 
		}
		
		paint.setStrokeWidth(2);	
		canvas.drawLine(mXStart+cw, mYStart, mXStart+cw+cw, mYStart, paint); //中心刻度线
		//if (mDataCnt > 0) { //刻度数字
			paint.setStrokeWidth(1);
			paint.setStyle(Style.FILL_AND_STROKE);
			paint.setTextSize(10);
			canvas.drawText(String.format("%d", 50), mXStart+cw*2+2, mYStart+8, paint);
		//}
		
		
		paint.setStrokeWidth(2);
		paint.setColor(Color.BLUE);
		canvas.drawLine(spaceWidth+textWidth, mYStart, spaceWidth+textWidth+cv, mYStart, paint);
		
	
		
		int x1=mXStart, y1=mYStart - (mData[MAX_DATA_CNT-1] - mBaseValue)*dy / 5;
		int x2, y2;
		
		paint.setStrokeWidth(3);
		paint.setColor(Color.GREEN);
		for (int i=1; i<mDataCnt; i++) {
			x2 = mXStart - i * dx;
			y2 = mYStart - (mData[MAX_DATA_CNT-i-1] - mBaseValue)*dy / 5;
			canvas.drawLine(x1,y1, x2, y2, paint);
			x1 = x2; y1 = y2;
		}
		
		LinearGradient mLinearGradient = new LinearGradient(0, 0, 20, 500,
                new int[]{Color.RED, Color.BLUE,} , null,
                Shader.TileMode.REPEAT);
		paint.setShader(mLinearGradient);
		paint.setStyle(Style.FILL);
		RectF post = new RectF(spaceWidth+textWidth+cv, sh, spaceWidth+textWidth+cv+cw, sh+ph);
		canvas.drawRoundRect(post, 5, 5, paint);
		paint.clearShadowLayer();
		
		
		
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		 // 首先定义一个paint
	
	    Paint paint = new Paint();  
	      
	    // 绘制矩形区域-实心矩形  
	    // 设置颜色  
	    paint.setColor(Color.TRANSPARENT);
	    canvas.drawRect(new Rect(0, 0, getWidth(), getHeight()), paint);
	    
	    drawCross(canvas);
		
	   // drawCurve(canvas);
	}
	
}
