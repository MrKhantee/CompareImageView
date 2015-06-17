package com.example.androidcompareimageview;

import com.example.androidcompareimageview.View.CompareImageView;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;

public class MainActivity extends Activity {

	CompareImageView img1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		img1 = (CompareImageView) findViewById(R.id.compareImageView1);
		img1.setBeforeImage(BitmapFactory.decodeResource(this.getResources(), R.drawable.photoshop_face_before));
		img1.setAfterImage(BitmapFactory.decodeResource(this.getResources(), R.drawable.photoshop_face_after));
	}



}
