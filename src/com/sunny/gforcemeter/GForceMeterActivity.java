package com.sunny.gforcemeter;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class GForceMeterActivity extends Activity {
	
	private SensorManager sensorManager;
	
	private TextView accelerationTextView;
	private TextView maxAccelerationTextView;
	
	private float currentAcceleration = 0f;
	private float maxAcceleration = 0f;
	
	/**
	 * 标准常量，标示重力引起的加速度
	 */
	private final double calibration = SensorManager.STANDARD_GRAVITY;
	
	/**
	 * 传感器事件监听器
	 */
	private final SensorEventListener sensorEventListener = new SensorEventListener() {
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			double x = event.values[0];
			double y = event.values[1];
			double z = event.values[2];
			
			double a = Math.round(Math.sqrt(Math.pow(x, 2) + 
					Math.pow(y, 2) + 
					Math.pow(z, 2)));
			currentAcceleration = Math.abs((float)(a-calibration));
			
			if (currentAcceleration > maxAcceleration) {
				maxAcceleration = currentAcceleration;
			}
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gforce_meter);
		
		accelerationTextView = (TextView) findViewById(R.id.acceleration);
		maxAccelerationTextView = (TextView) findViewById(R.id.maxAcceleration);
		
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		Sensor accelerometer = 
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(sensorEventListener, 
				accelerometer, 
				SensorManager.SENSOR_DELAY_FASTEST);
		
		// 定时更新UI（注意不要把更新UI直接放在Sensor的Listener中，因为加速器一秒内可能更新几百次，造成UI线程卡死）
		Timer updateTimer = new Timer("gForceUpdate");
		updateTimer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				updateGUI();
			}
		}, 0, 100);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// 注册加速传感器的监听器
		Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(sensorEventListener, 
				accelerometer, 
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	@Override
	protected void onPause() {
		// 注销监听器
		sensorManager.unregisterListener(sensorEventListener);
		
		super.onPause();
	}
	
	private void updateGUI() {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				String currentG = currentAcceleration/SensorManager.STANDARD_GRAVITY 
						+ "Gs";
				accelerationTextView.setText(currentG);
				accelerationTextView.invalidate();
				
				String maxG = maxAcceleration/SensorManager.STANDARD_GRAVITY + "Gs";
				maxAccelerationTextView.setText(maxG);
				maxAccelerationTextView.invalidate();
			}
		});
	}

}
