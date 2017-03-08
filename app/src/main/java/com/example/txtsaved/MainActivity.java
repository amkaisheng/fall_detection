package com.example.txtsaved;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;

import android.os.SystemClock;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private EditText et1;
	private TextView t1,show;
	private Button btn_start,btn_end,btn_ensure,mode1,mode2;
	String TAG="Main";






	private SeekBar sb1=null;
	private SeekBar sb2=null;
	private SeekBar sb3=null;



	ActivityReceiver activityReceiver;
	public static final String CTL_ACTION = "org.crazyit.action.CTL_ACTION";
	public static final String UPDATE_ACTION = "org.crazyit.action.UPDATE_ACTION";
	Intent intentservice;
	// 定义音乐的播放状态 ，0X11 代表停止 ，0x12代表播放,0x13代表暂停
	int status = 0x11;
	Intent intent = new Intent(CTL_ACTION);



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		activityReceiver = new ActivityReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATE_ACTION);
		registerReceiver(activityReceiver, filter);
		intentservice = new Intent(this, MyService.class);
		startService(intentservice);





		btn_start=(Button) findViewById(R.id.Start);
		btn_end=(Button) findViewById(R.id.End);
		btn_ensure=(Button) findViewById(R.id.Ensure);

		mode1=(Button) findViewById(R.id.button3);
		mode2=(Button) findViewById(R.id.button2);

		et1=(EditText) findViewById(R.id.et1);

		t1=(TextView) findViewById(R.id.t1);
		show=(TextView) findViewById(R.id.arg_show);
		show.setMovementMethod(ScrollingMovementMethod.getInstance());
		btn_start.setOnClickListener(new StartClassListener());
		btn_start.setVisibility(View.INVISIBLE);
		btn_end.setOnClickListener(new EndClassListener());
		btn_end.setVisibility(View.INVISIBLE);
		btn_ensure.setOnClickListener(new EnsureClassListener());
		mode1.setOnClickListener(new ModeClassListener1());
		mode2.setOnClickListener(new ModeClassListener2());

		sb1=(SeekBar) super.findViewById(R.id.sb1);
		sb1.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImp1());
		sb1.setProgress(MyService.ACCMAX);

		sb2=(SeekBar) super.findViewById(R.id.sb2);
		sb2.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImp2());
		sb2.setProgress(MyService.ACCMIN);

		sb3=(SeekBar) super.findViewById(R.id.sb3);
		sb3.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImp3());
		sb3.setProgress(MyService.GYRMIN);









		show.setText("accmax="+MyService.ACCMAX+"  accmin="+MyService.ACCMIN+"  gyrmin="+MyService.GYRMIN);







//		screenListener = new HoldService( MainActivity.this ) ;
//		screenListener.begin(new HoldService.ScreenStateListener() {
//			@Override
//			public void onScreenOn() {
//
//
//				Log.d(TAG,"SCREEN ON");
//			}
//
//			@Override
//			public void onScreenOff() {
//				Log.d(TAG,"SCREEN OFF");
//
//
//				if(isStarted){
//					if(!sm.registerListener(MainActivity.this,sensor[0],SensorManager.SENSOR_DELAY_UI)){
//						sm.registerListener(MainActivity.this,sensor[0],SensorManager.SENSOR_DELAY_UI);
//						sm.registerListener(MainActivity.this,sensor[1],SensorManager.SENSOR_DELAY_UI);
//						sm.registerListener(MainActivity.this,sensor[2],SensorManager.SENSOR_DELAY_UI);
//					}
//
//					if(save_gravity_first_time){
//
//
//
//
//						sj.start();
//						save_gravity_first_time=false;
//					}
//
//
//					new Thread() {
//						@Override
//						public void run() {
//							updateTime = new Timer("NEW");
//
//							tt =new TimerTask() {
//
//								@Override
//								public void run() {
//									// TODO Auto-generated method stub
//									if(!sm.registerListener(MainActivity.this,sensor[0],SensorManager.SENSOR_DELAY_UI)){
//										sm.registerListener(MainActivity.this,sensor[0],SensorManager.SENSOR_DELAY_UI);
//										sm.registerListener(MainActivity.this,sensor[1],SensorManager.SENSOR_DELAY_UI);
//										sm.registerListener(MainActivity.this,sensor[2],SensorManager.SENSOR_DELAY_UI);
//									}
//									restart();
//									//Log.d(TAG, "goTHREAD************************************");
//
//								}
//							};
//							updateTime.scheduleAtFixedRate(tt, 0, 200);
//						}
//					}.start();
//				}
//
//
//			}
//
//
//		});


	}

	private class OnSeekBarChangeListenerImp1 implements SeekBar.OnSeekBarChangeListener{
		//触发操作，拖动
		public void onProgressChanged(SeekBar seekBar, int progress,
									  boolean fromUser) {
			MyService.ACCMAX=seekBar.getProgress();
			show.setText("accmax="+MyService.ACCMAX+"  accmin="+MyService.ACCMIN+"  gyrmin="+MyService.GYRMIN);
		}

		//表示进度条刚开始拖动，开始拖动时候触发的操作
		public void onStartTrackingTouch(SeekBar seekBar) {
			MyService.ACCMAX=seekBar.getProgress();
			show.setText("accmax="+MyService.ACCMAX+"  accmin="+MyService.ACCMIN+"  gyrmin="+MyService.GYRMIN);
		}

		//停止拖动时候
		public void onStopTrackingTouch(SeekBar seekBar) {
// TODO Auto-generated method stub
			MyService.ACCMAX=seekBar.getProgress();

			show.setText("accmax="+MyService.ACCMAX+"  accmin="+MyService.ACCMIN+"  gyrmin="+MyService.GYRMIN);
		}
	}


	private class OnSeekBarChangeListenerImp2 implements SeekBar.OnSeekBarChangeListener{
		//触发操作，拖动
		public void onProgressChanged(SeekBar seekBar, int progress,
									  boolean fromUser) {
			MyService.ACCMIN=seekBar.getProgress();
			show.setText("accmax="+MyService.ACCMAX+"  accmin="+MyService.ACCMIN+"  gyrmin="+MyService.GYRMIN);
		}

		//表示进度条刚开始拖动，开始拖动时候触发的操作
		public void onStartTrackingTouch(SeekBar seekBar) {
			MyService.ACCMIN=seekBar.getProgress();
			show.setText("accmax="+MyService.ACCMAX+"  accmin="+MyService.ACCMIN+"  gyrmin="+MyService.GYRMIN);
		}

		//停止拖动时候
		public void onStopTrackingTouch(SeekBar seekBar) {
// TODO Auto-generated method stub
			MyService.ACCMIN=seekBar.getProgress();
			show.setText("accmax="+MyService.ACCMAX+"  accmin="+MyService.ACCMIN+"  gyrmin="+MyService.GYRMIN);
		}
	}

	private class OnSeekBarChangeListenerImp3 implements SeekBar.OnSeekBarChangeListener{
		//触发操作，拖动
		public void onProgressChanged(SeekBar seekBar, int progress,
									  boolean fromUser) {
			MyService.GYRMIN=seekBar.getProgress();
			show.setText("accmax="+MyService.ACCMAX+"  accmin="+MyService.ACCMIN+"  gyrmin="+MyService.GYRMIN);
		}

		//表示进度条刚开始拖动，开始拖动时候触发的操作
		public void onStartTrackingTouch(SeekBar seekBar) {
			MyService.GYRMIN=seekBar.getProgress();
			show.setText("accmax="+MyService.ACCMAX+"  accmin="+MyService.ACCMIN+"  gyrmin="+MyService.GYRMIN);
		}

		//停止拖动时候
		public void onStopTrackingTouch(SeekBar seekBar) {
// TODO Auto-generated method stub
			MyService.GYRMIN=seekBar.getProgress();
			show.setText("accmax="+MyService.ACCMAX+"  accmin="+MyService.ACCMIN+"  gyrmin="+MyService.GYRMIN);
		}
	}

	public void updateGUI(){
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				MyService.timer1 =SystemClock.uptimeMillis();
				//for moving
//				if(!sm.registerListener(MainActivity.this,sensor[0],SensorManager.SENSOR_DELAY_UI)){
//					sm.registerListener(MainActivity.this,sensor[0],SensorManager.SENSOR_DELAY_UI);
//					sm.registerListener(MainActivity.this,sensor[1],SensorManager.SENSOR_DELAY_UI);
//					sm.registerListener(MainActivity.this,sensor[2],SensorManager.SENSOR_DELAY_UI);
//				}

				t1.setText("t="+MyService.timer1+"\n  acc_x="+MyService.acc[0]+"  acc_y="+MyService.acc[1]+"  acc_z="+MyService.acc[2]
						+"\n  gyr_x="+MyService.gyr[0]+"  gyr_y="+MyService.gyr[1]+"  gyr_z="+MyService.gyr[2]+"\n  acc_sum="+MyService.alldata[6]+
						"\n  gra_x="+MyService.gra[0]+"  gra_y="+MyService.gra[1]+"  gra_z="+MyService.gra[2]+"\n  gra_sum="+MyService.alldata[11]+
						"\n  gravity angle change="+MyService.gravity_angle_change+
						"\n  before_after_angle="+MyService.before_after_angle
						//"\n processed_gravity_vector: [0]"+PGV.LPF_general.presentValue[0]+" [1]"+PGV.LPF_general.presentValue[1]+" [2]"+PGV.LPF_general.presentValue[2]
				);

				show.setText("accmax="+MyService.ACCMAX+"  accmin="+MyService.ACCMIN+"  gyrmin="+MyService.GYRMIN);

				try {
					if(MyService.judgeResult){
						Log.d(TAG,"SCREEN ON******judge");
						if(MyService.fos!=null){
							Calendar happentime=Calendar.getInstance();
							MyService.fos.write(("t="+MyService.timer1+"  screen-on-mode happen time: ").toString().getBytes());
							MyService.fos.write((happentime.getTime().toString()).getBytes());
							MyService.fos.write(("  anglesum: "+MyService.anglesum).toString().getBytes());
							byte []newLine="\r\n".getBytes();
							MyService.fos.write(newLine);

							for(int i=(MyService.indexOfMin-4);i<MyService.OUTPUT_SIZE+(MyService.indexOfMin-4);i++){
								MyService.fos.write(((("  acc_x="+MyService.suspector.get(i)[0]+"  acc_y="+MyService.suspector.get(i)[1]+"  acc_z="+MyService.suspector.get(i)[2]
										+"  gyr_x="+MyService.suspector.get(i)[3]+"  gyr_y="+MyService.suspector.get(i)[4]+"  gyr_z="+MyService.suspector.get(i)[5]
										+"  gyr_sum="+MyService.suspector.get(i)[7]+"  acc_sum="+MyService.suspector.get(i)[6]).toString())).getBytes());
								byte []newLine1="\r\n".getBytes();
								MyService.fos.write(newLine1);
							}
							byte []newLine2=(MyService.mutetime+"\r\n").getBytes();
							MyService.fos.write(newLine2);
							MyService.fos.flush();
						}
						else{
							try {
								MyService.fos = new FileOutputStream(MyService.fi.getAbsolutePath(),true);
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						MyService.judgeResult=false;
					}



				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			}

		});
	}
@Override
protected void onResume() {
	// TODO Auto-generated method stub
	super.onResume();
	
}
@Override
protected void onPause() {
	// TODO Auto-generated method stub
	super.onPause();


}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	class StartClassListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			MyService.first_get_gravity=true;
			MyService.isStarted=true;
			MyService.updateTime = new Timer("Acc");

			//service
			intent.putExtra("control", 1);
			sendBroadcast(intent);
			Log.d(TAG, "music on************************************");

			MyService.tt =new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub


					updateGUI();

				}
			};
			MyService.updateTime.scheduleAtFixedRate(MyService.tt, 0, 200);
			//timer2=SystemClock.uptimeMillis();



			try {
				MyService.fos = new FileOutputStream(MyService.fi.getAbsolutePath(),true);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			btn_start.setVisibility(View.INVISIBLE);
			btn_end.setVisibility(View.VISIBLE);
		}

	}

	class ModeClassListener1 implements OnClickListener{

		@Override
		public void onClick(View v) {
			Log.d(TAG,"mode1");
			sb1.setProgress(10);
			sb2.setProgress(9);
			sb3.setProgress(1);
			MyService.ACCMAX=10;
			MyService.ACCMIN=9;
			MyService.GYRMIN=1;
			MyService.GRAMIN=30;
		}
	}

	class ModeClassListener2 implements OnClickListener{

		@Override
		public void onClick(View v) {
			Log.d(TAG,"mode2");
			sb1.setProgress(18);
			sb2.setProgress(7);
			sb3.setProgress(7);
			MyService.ACCMAX=18;
			MyService.ACCMIN=7;
			MyService.GYRMIN=7;
			MyService.GRAMIN=55;
		}
	}

	class EnsureClassListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String etacc=et1.getText().toString();
			if(!etacc.equals("")){
				String filestr ="";
				filestr +="/storage/sdcard0/accdata/"+et1.getText().toString();
				if(!filestr.contains(".")){
					filestr+=".txt";
					MyService.fi=new File(filestr);
					if(!MyService.fi.exists()){
						System.out.println("creating it now!");
						try {
							MyService.fi.createNewFile();
							 Toast.makeText(MainActivity.this, "File isn't exits.Now,create it.",
									 Toast.LENGTH_LONG).show();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							 Toast.makeText(MainActivity.this, "Failed to create new file!", Toast.LENGTH_LONG).show();
						        e.printStackTrace();
						        System.out.println(e.toString());
						        finish();
						}
					}else{
				        Toast.makeText(MainActivity.this, "the file has existed!", Toast.LENGTH_LONG).show();
				       }
					btn_start.setVisibility(View.VISIBLE);
				}else{
			        et1.setText("file name is not correct!");
			       }
			}else{
				Toast.makeText(MainActivity.this, "file name can not be null!", Toast.LENGTH_LONG).show();
			}
		}
		
	}
	class EndClassListener implements OnClickListener{

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			MyService.suspector_coming=false;
			MyService.suspector_coming_influence=5;
			MyService.have_saved_general_gravity_vector=false;

			//save_gravity_first_time=true;

			MyService.isStarted=false;

			//service
			intent.putExtra("control", 2);
			sendBroadcast(intent);

			try {
				MyService.mycalendar=Calendar.getInstance();
				MyService.fos.write(("t="+MyService.timer1+"   test end time: ").toString().getBytes());
				MyService.fos.write((MyService.mycalendar.getTime().toString()).getBytes());
				MyService.fos.write("*********************************************************************".getBytes());
			    byte []newLine="\r\n".getBytes();
				MyService.fos.write(newLine);
				MyService.fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MyService.tt.cancel();
			MyService.updateTime.cancel();



			btn_end.setVisibility(View.INVISIBLE);
			btn_start.setVisibility(View.VISIBLE);
		}
		
	}





	//释放设备电源锁


	public class ActivityReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 获取Intent中的update消息，update代表播放状态
			int update = intent.getIntExtra("update", -1);
			switch (update) {
				case 0x11: {

					status = 0x11;
					break;
				}

				// 控制系统进入播放状态
				case 0x12: {
					// 播放状态下设置使用按钮

					// 设置当前状态
					status = 0x12;
					break;
				}
				// 控制系统进入暂停状态

			}
		}

	}







	}


