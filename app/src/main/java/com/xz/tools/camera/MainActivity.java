package com.xz.tools.camera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.xz.tools.xcamera.ui.CameraActivity;
import com.xz.tools.xcamera.view.FocusingView;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		startActivity(new Intent(this, CameraActivity.class));
		finish();


		//FocusingView focusingView = findViewById(R.id.touch_view);
		//focusingView.setOnClickListener(new View.OnClickListener() {
		//	@Override
		//	public void onClick(View view) {
		//		focusingView.play();
		//	}
		//});
	}

}
