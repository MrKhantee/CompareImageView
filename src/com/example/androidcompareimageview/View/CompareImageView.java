/**
 * Written by Pontus Holmberg
 * 
 * This is not a perfect widget, but more of a proof of concept thing.
 * 
 * @author Pontus Holmberg aka EndLessMind
 */
package com.example.androidcompareimageview.View;


import com.example.androidcompareimageview.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

@SuppressLint("DrawAllocation")
public class CompareImageView extends View {
	private static final String TAG = "CompareImageView";
	String txtBefore = " ";
	String txtAfter = " ";
	
	Bitmap imgBefore;
	Bitmap imgAfter;
	Bitmap imgTrans;
	
	RectF thumb;
	RectF scrollBar;
	Rect mainRect;
	RectF fadeRect;
	
	int Height = 0;
	int Width = 0;
	int thumbSize = 20; //in dp;
	int FadebarHeight = 17; //in dp
	
	float fadePercent = 50f;
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
		loadDefaultText(context);
		// TODO Auto-generated constructor stub
	}
	
	public CompareImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initColors();
		loadDefaultText(context);
		loadAttributes(context.obtainStyledAttributes(attrs, R.styleable.CompareImageView, 0, 0));
		// TODO Auto-generated constructor stub
	}

	public CompareImageView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initColors();
		loadDefaultText(context);
		loadAttributes(context.obtainStyledAttributes(attrs, R.styleable.CompareImageView, defStyleAttr, 0));
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
	
	private void loadDefaultText(Context context) {
		//We load this in case the developer has not defined this in the xml-layout
		txtBefore = context.getResources().getString(R.string.textBefore);
		txtAfter = context.getResources().getString(R.string.textAfter);
	}
	
	private void loadAttributes(TypedArray attrs) {
		txtBefore = attrs.hasValue(R.styleable.CompareImageView_beforeText) ? attrs.getString(R.styleable.CompareImageView_beforeText) : txtBefore;
		txtAfter = attrs.hasValue(R.styleable.CompareImageView_afterText) ? attrs.getString(R.styleable.CompareImageView_afterText): txtAfter;
	}
	
	public void setBeforeText(String text) {
		txtBefore = text;
		invalidate();
	}
	
	public String getBeforeText() {
		return txtBefore;
	}
	
	public void setAfterText(String text) {
		txtAfter = text;
		invalidate();
	}
	
	public String getAfterText() {
		return txtAfter;
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
	
	/**
	 * Sets the fadebar height
	 * 
	 * @param value Height in dp
	 */
	public void setFadebarHeight(int value) {
		FadebarHeight = value;
		invalidate();
	}
	
	public int getFadebarHeight() {
		return FadebarHeight;
	}
	
    private float Dip(int value) {
    	Resources r = getResources();
    	float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, r.getDisplayMetrics());
    	return  px;

    }
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		/*
		 *The size has changed. We'll create new bitmap-layers to match the new size 
		 */

		Height = h;
		Width = w;
		thumb = new RectF(0,Height - Dip(FadebarHeight),Dip(thumbSize * 2),Height);
		scrollBar = new RectF(0,Height - Dip(FadebarHeight),Width,Height);
		fadePercent = 50f;
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
        	canvas.drawText(txtAfter, 40, Height - (Dip(FadebarHeight) * 2), textPaint);
        	
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
        	tempCan.drawText(txtBefore, (Width - textPaint.measureText(txtBefore)) - 40, Height - (Dip(FadebarHeight) * 2), textPaint);
        	tempCan.drawRect(fadeRect, transparentPaint);
        	
        	canvas.drawBitmap(imgTrans, 0, 0, dummyPaint);
        }
        float thumbsize = Dip(thumbSize);
        thumb.left = getPixelFromProcent() - thumbsize;
        thumb.right = getPixelFromProcent() + thumbsize;
        thumb.top = Height - Dip(FadebarHeight);
        scrollBar.top = Height - Dip(FadebarHeight);
        canvas.drawRect(scrollBar, scrollBarPaint);
        canvas.drawRect(thumb, thumbPaint);
        
	}
	
	
	//Will scale the view to fit the image when BOTH width and height is set to wrap_content
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		 try {
			 
			 int widthMode = MeasureSpec.getMode(widthMeasureSpec);
			 int heightMode = MeasureSpec.getMode(heightMeasureSpec);
			 if (widthMode != MeasureSpec.AT_MOST && heightMode != MeasureSpec.AT_MOST) {
				 super.onMeasure(widthMeasureSpec, heightMeasureSpec);
				 return;
			 }
			 	
		        if (imgBefore == null) {
		            setMeasuredDimension(0, 0);
		        } else {
		            float imageSideRatio = (float)imgBefore.getWidth() / (float)imgBefore.getHeight();
		            float viewSideRatio = (float)MeasureSpec.getSize(widthMeasureSpec) / (float)MeasureSpec.getSize(heightMeasureSpec);
		            if (imageSideRatio >= viewSideRatio) {
		                // Image is wider than the display (ratio)
		                int width = MeasureSpec.getSize(widthMeasureSpec);
		                int height = (int)(width / imageSideRatio);
		                setMeasuredDimension(width, height);
		            } else {
		                // Image is taller than the display (ratio)
		                int height = MeasureSpec.getSize(heightMeasureSpec);
		                int width = (int)(height * imageSideRatio);
		                setMeasuredDimension(width, height);
		            }
		        }
		    } catch (Exception e) {
		        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		    }
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
				if (event.getY() > (Height - Dip(FadebarHeight)) && event.getY() <= Height) {
					float thumbsize = Dip(thumbSize);
					//The user is touching the scrollbar area. Now we need to check if the user is touching the thumb or not.
					if (event.getX() > (getPixelFromProcent() - thumbsize) && event.getX() < (getPixelFromProcent() + thumbsize)) {
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
