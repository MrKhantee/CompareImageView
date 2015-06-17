/**
 * Written by Pontus Holmberg
 * 
 * This is not a perfect widget, but more of a proof of concept thing.
 * 
 * @author Pontus Holmberg aka EndLessMind
 */
package com.example.androidcompareimageview.View;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

@SuppressLint("DrawAllocation")
public class CompareImageView extends View {
	private static final String TAG = "CompareImageView";
	Bitmap imgBefore;
	Bitmap imgAfter;
	Bitmap imgTrans;
	
	
	RectF thumb;
	RectF scrollBar;
	Rect mainRect;
	RectF fadeRect;
	
	
	int Height = 0;
	int Width = 0;
	int ScrollbarHeight = 70;
	
	float fadePercent = 100f;
	float textSize = 72f;
	
	boolean isTouchingThumb = false;
	
	Paint scrollBarPaint;
	Paint thumbPaint;
	Paint transparentPaint;
	Paint dummyPaint = new Paint();
	Paint textPaint;
	
	Canvas tempCan;
	
	
	public CompareImageView(Context context) {
		super(context);
		initColors();
		// TODO Auto-generated constructor stub
	}
	
	public CompareImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initColors();
		// TODO Auto-generated constructor stub
	}

	public CompareImageView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initColors();
		// TODO Auto-generated constructor stub
	}
	
	private void initColors() {
		scrollBarPaint = new Paint();
		scrollBarPaint.setColor(Color.argb(100, 255, 255, 255));
		
		thumbPaint = new Paint();
		thumbPaint.setColor(Color.WHITE);
		
	    transparentPaint = new Paint();
	    transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
	    transparentPaint.setAlpha(100);
	    transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
	    
	    textPaint = new Paint();
	    textPaint.setColor(Color.WHITE);
	    textPaint.setTextSize(72f);
	}
	
	public void setBeforeImage(Bitmap img) {
		imgBefore = img; //Going to need some processing before this stage. Just placeholder for now
		invalidate();
	}
	
	public void setAfterImage(Bitmap img) {
		imgAfter = img; //Going to need some processing before this stage. Just placeholder for now
		invalidate();
	}
	
	public void setTextSize(float value) {
		textSize = value;
		invalidate();
	}
	
	public float getTextSize() {
		return textSize;
	}
	
	public void setFadePercentage(float value) {
		fadePercent = value;
		invalidate();
	}
	
	public float getFadePercentage() {
		return fadePercent;
	}
	
	public void setFadebarHeight(int value) {
		ScrollbarHeight = value;
		invalidate();
	}
	
	public int getFadebarHeight() {
		return ScrollbarHeight;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		/*
		 *The size has changed. We'll create new bitmap-layers to match the new size 
		 */

		Height = h;
		Width = w;
		thumb = new RectF(0,Height - ScrollbarHeight,140,Height);
		scrollBar = new RectF(0,Height - ScrollbarHeight,Width,Height);
		fadePercent = 100f;
		mainRect = new Rect(0,0,Width,Height);
		fadeRect = new RectF(0,0, getPixelFromProcent(),Height);
		
		
		imgTrans = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
		tempCan = new Canvas(imgTrans);
	}
	
	
	@Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        ClearTempCan();
        if (imgBefore != null && imgAfter != null) {
        	canvas.drawBitmap(imgAfter, null, mainRect, null);
        	canvas.drawText("After", 40, Height - (ScrollbarHeight * 2), textPaint);
        	
        	/*
        	* We need to draw the top-most image in a different bitmap.
        	* This is because this is the bitmap we want to be able to change the visibility of.
        	* 
        	* Because we're using DuffXfer mode, we can't have them in the same bitmap. That would cause
        	* the transparency to apply to both the images. That's not what we want.
        	* 
        	* So we draw the top-most image in a different bitmap, apply the transparency and then draw that bitmap
        	* to the main canvas.
        	* 
        	*/
        	tempCan.drawBitmap(imgBefore, null, mainRect, null);
        	tempCan.drawText("Before", (Width - textPaint.measureText("Before")) - 40, Height - (ScrollbarHeight * 2), textPaint);
        	tempCan.drawRect(fadeRect, transparentPaint);
        	
        	canvas.drawBitmap(imgTrans, 0, 0, dummyPaint);
        }
        
        thumb.left = getPixelFromProcent() - 70;
        thumb.right = getPixelFromProcent() + 70;
        thumb.top = Height - ScrollbarHeight;
        scrollBar.top = Height - ScrollbarHeight;
        canvas.drawRect(scrollBar, scrollBarPaint);
        canvas.drawRect(thumb, thumbPaint);
        
	}
	
	private void ClearTempCan() {
		if (tempCan != null) {
			tempCan.drawColor(Color.BLACK);
		}
		
	}
	
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
		//Read the movment of the scroll
		int action = event.getAction();
		if (event.getPointerCount() == 1) {
			
			if (action == MotionEvent.ACTION_MOVE) {
					if (isTouchingThumb) {
						//The thumb is beeing touched. Let's move it.
						setProcentFromPixel(event.getX());
						invalidate();
					}
			} else if (action == MotionEvent.ACTION_DOWN) {
				if (event.getY() > (Height - ScrollbarHeight) && event.getY() <= Height) {
					//The user is touching the scrollbar area. Now we need to check if the user is touching the thumb or not.
					if (event.getX() > (getPixelFromProcent() - 70) && event.getX() < (getPixelFromProcent() + 70)) {
						//The user is touching the thumb, make if follow the movment
						isTouchingThumb = true;
					}
				}
			} else if (action == MotionEvent.ACTION_UP) {
				isTouchingThumb = false;
			}
			
		}
		return true;
	}

	
	/**
	 * Get the center of the thumb in pixels
	 * 
	 * @return
	 */
	private float getPixelFromProcent() {
		return (float) Width - ((float) Width * (fadePercent / 100f));
	}
	
	/**
	 * Converts the touch-movment to procent. (How much of the before image to be shown)
	 * 
	 * @param pixel
	 */
	private void setProcentFromPixel(float pixel) {
		fadePercent = ((Width - pixel) / (float) Width) * 100f;
		fadeRect.right = getPixelFromProcent();
	}
}