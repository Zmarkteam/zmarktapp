package com.embedkit.zigbee;

import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.Log;

public class Top {
	private static final String TAG = "Top";
	
	static final int NodeSize = 84;
	
	static final int PandX	= 30;
	static final int PandY  = 80;
	
	static Bitmap bm;// = Bitmap.createBitmap(SCREEN_WIDTH,SCREEN_HEIGHT,Bitmap.Config.ARGB_4444);
	static Canvas cv;// = new Canvas(bm);
	
	static Node tree;
	
	/*
	static Node GetRoot() {
		
		return new Node(1);
	}
	static List <Node> GetChild(Node root) {
		int i;	
		List<Node> li = new LinkedList<Node>();
		Node nd;
		
		for (i=0; i<3; i++) {
			int ty = root.mNodeType*10+i;
			nd = new Node(ty);
			li.add(nd);
		}
		return li;
	}
	*/
	
	static void DistroyTree(Node root) {
		if (root == null) return;
		if (root._childNode.size() == 0){
			root = null;
			return;
		}
		for (int i=0; i<root._childNode.size(); i++){
			DistroyTree(root._childNode.get(i));
		}
		
	}
	
	private static void topDeepthInit(Node root) {
		if (root._childNode.size() == 0) root.mDeepth = 1;
		else {
			int max = 0;
			for (int i=0; i<root._childNode.size(); i++) {
				topDeepthInit(root._childNode.get(i));
				if (root._childNode.get(i).mDeepth > max) 
					max = root._childNode.get(i).mDeepth;
			}
			root.mDeepth = max + 1;
		}
	}
	
	private static int  topWidthInit(Node root) {
		if (root._childNode.size() == 0) return 1;
		else {
			int maxw = 0, w;
			for (int i=0; i<root._childNode.size(); i++) {
				w = topWidthInit(root._childNode.get(i));
				if (w > maxw) maxw = w;
			}
			return maxw * root._childNode.size();
		}
	}
	
	
	private static void _drawTop(Node root, int left, int top, int width, int height)
	{
	   	Node nd = root;
    	int i, cnt = nd._childNode.size();   	
    	int dp = root.mDeepth;	
    	
    	//PathEffect ef = new DashPathEffect(new float[]{2,2}, 1);
		Paint p=new Paint();
		p.setStyle(Paint.Style.FILL_AND_STROKE);
		p.setColor(Color.BLUE);
		p.setStrokeWidth(1);
		p.setAntiAlias(true);
		//p.setPathEffect(ef);
		
    	nd.setIconRect(left, top, width, height);
    	nd.DrawIcon(cv);
    
    	for (i=0; i<cnt; i++) {
    		_drawTop(nd._childNode.get(i), left+i*(width/cnt), /*top+h*/top+height, width/cnt, /*height-h*/height);
    		
    		//cv.drawLine(left+width/2, top+(Top.NodeSize)+30, 
    		//		left+i*(width/cnt)+width/cnt/2, top+height, p);
    		int x1 = left+width/2, y1 = top+(Top.NodeSize) + 30;
    		
    		if (nd.mNodeType != Node.ZB_NODE_TYPE_COORDINATOR) y1 -= 25;
    		
    		int x2 = left+i*(width/cnt)+width/cnt/2, y2 = top+height-8;
    		
    		Path p1 = new Path(), p2 = new Path();
    		p1.moveTo(x1, y1);
    		p1.lineTo(x1-5, y1+5);
    		p1.lineTo(x1+5, y1+5);
    		p1.close();
    		
    		p2.moveTo(x1, y1+5);
    		p2.lineTo(x2-8, y2);
    		p2.lineTo(x2+8, y2);
    		p2.close();
    		cv.drawPath(p1, p);
    		cv.drawPath(p2, p);
    	}		
	}

    static void DrawTop(Node root)
    {
    	int w, h;
    	int szx = (PandX + NodeSize);
    	int szy = (PandY + NodeSize);
    	
    	topDeepthInit(root);
    	w = topWidthInit(root); 
    	h = root.mDeepth;
    	Log.d(TAG, "top width :" + w + " height:"+h);
    	
    	int align = 40;
    	bm = Bitmap.createBitmap(align + w*szx, align + h*szy, Bitmap.Config.ARGB_4444);
    	cv = new Canvas(bm);
    	_drawTop(root, align/2 + 0, align/2+0, w*szx, /*h**/szy);
    	
    	tree = root;
    }
    /*
    static void DrawRelationship(Node root)
    {
    	PathEffect ef = new DashPathEffect(new float[]{2,2}, 1);
		Paint p=new Paint();
		p.setStyle(Paint.Style.STROKE);
		p.setColor(Color.BLACK);
		p.setPathEffect(ef);
		for (int i=0; i<root._childNode.size(); i++) {
			
			cv.drawLine(root.mLeft+root.mWidth/2, root.mTop+(Top.NodeSize), 
					root._childNode.get(i).mLeft+root._childNode.get(i).mWidth/2, root._childNode.get(i).mTop, p);
		}
		
		for (int i=0; i<root._childNode.size(); i++) {
			DrawRelationship(root._childNode.get(i));
		}
     }
    */
    static Node findClick(Node root, float x, float y)
    {
    	int l, t, r, b;
    	Node nd = root;
    	l = root.mLeft + root.mWidth/2 - (Top.NodeSize + PandX)/2;
    	r = l + (Top.NodeSize);
    	t = root.mTop;
    	b = t + (Top.NodeSize);
    	if (x>l && x <r && y>t && y<b) {
    		return nd;
    	} 
    	for (int i=0; i<root._childNode.size(); i++) {
    		nd = findClick(root._childNode.get(i), x, y);
    		if (nd != null)
    			return nd;
    	}
    	return null;
    }
    
}
