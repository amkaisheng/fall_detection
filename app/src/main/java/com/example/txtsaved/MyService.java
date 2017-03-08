package com.example.txtsaved;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;

import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by gift on 2017/2/28.
 */

public class MyService extends Service implements SensorEventListener {


    MyReceiver serviceReceiver;
    MediaPlayer mPlayer;
    //String TAG = "service";


    //move
    static public SensorManager sm;
    static public Sensor[] sensor = new Sensor[3];
    static Float acc[] = new Float[3];
    static Float gyr[] = new Float[3];
    static Float gra[] = new Float[3];
    static Double gravity_last[] = new Double[4];
    static Double gravity_now[] = new Double[4];
    static Float alldata[] = new Float[12];

    static int mutetime_CaculateAngle = 10;
    static boolean[] flag = {false, false, false};
    static final int SIZE = 30;
    static final int OUTPUT_SIZE = 20;
    static Float anglesum = null;
    static Float judge_anglesum = null;
    static boolean judgeResult;
    static int mutetime = 0;
    String TAG1 = "service";
    static boolean isStarted = false;
    static boolean suspector_coming = false;
    static int suspector_coming_influence = 5;
    static int ACCMAX = 18;
    static int ACCMIN = 6;
    static int GYRMIN = 7;
    static int GRAMIN = 50;
    static Float data_ACCMIN = (float) 7;
    static Float data_ACCMAX = (float) 13;

    static Double gravity_angle_change = (double) 0;
    static Double angle_change_max = (double) 0;
    static Double before_after_angle = (double) -1;
    static Double[] before_falling = new Double[3];
    static Double[] after_falling = new Double[3];
    static Double[] trusty_before_falling = new Double[3];
    static int indexOfMin = -1;
    static int indexOfMax = -1;
    static boolean first_get_gravity = true;
    static boolean save_gravity_first_time = true;
    static boolean have_saved_general_gravity_vector = false;
    static Double max_angle_of_suspector = (double) 0;

    ProcessGravityVector PGV;
    ProcessGravityVector pgv_new = new ProcessGravityVector();
    MediaPlayer player;
    situationJudge sj = new situationJudge();
    int situationThreadID = 0;
    boolean datalock_beforefall = false;
    boolean have_falled_before = false;
    boolean is_FromFallToRecover = false;

    static List<Float[]> suspector = new ArrayList<Float[]>();
    static List<Float[]> needToFixSuspector = new ArrayList<Float[]>();
    static List<Double> angle_change_save = new ArrayList<Double>();

    static File fi = null;
    static File fidir = null;
    static FileOutputStream fos = null;
    static Calendar mycalendar;
    static Timer updateTime;
    static Timer gravitySaveTime;
    static TimerTask tt = null;
    public static long timer1 = 0;


    //here moved data overed

    int status = 0x11;

    int flog = 0;

    //String filename;
    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        flog = 0;
        mPlayer.stop();
        mPlayer.release();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        flog = 1;


        Log.e("Service", "onStart");
        serviceReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.CTL_ACTION);
        registerReceiver(serviceReceiver, filter);


        //moved data
        sm = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        sensor[0] = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, sensor[0], SensorManager.SENSOR_DELAY_UI);
        sensor[1] = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sm.registerListener(this, sensor[1], SensorManager.SENSOR_DELAY_UI);
        sensor[2] = sm.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sm.registerListener(this, sensor[2], SensorManager.SENSOR_DELAY_UI);

        player = MediaPlayer.create(this, R.raw.music);
        player.setLooping(true);
        player.setVolume(1.0f, 1.0f);

        PGV = new ProcessGravityVector();

        fidir = new File("/storage/");
        if (!fidir.exists()) {
            fidir.mkdir();
        }
        fidir = new File("/storage/sdcard0/");
        if (!fidir.exists()) {
            fidir.mkdir();
        }
        fidir = new File("/storage/sdcard0/accdata");
        if (!fidir.exists()) {
            fidir.mkdir();
        }


        super.onCreate();
    }


    @Override
    public void onStart(Intent intent, int startId) {
        if (flog == 2) {
            Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
            sendIntent.putExtra("update", status);
            //sendIntent.putExtra("current", current);
            sendBroadcast(sendIntent);
        }
        flog = 2;
    }


    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int control = intent.getIntExtra("control", -1);
            switch (control) {
                case 1: {
                    // 如果原来处于停止的状态
                    if (status == 0x11) {


                        Log.d(TAG1, "service start");


                        if (isStarted) {
                            if (!sm.registerListener(MyService.this, sensor[0], SensorManager.SENSOR_DELAY_UI)) {
                                sm.registerListener(MyService.this, sensor[0], SensorManager.SENSOR_DELAY_UI);
                                sm.registerListener(MyService.this, sensor[1], SensorManager.SENSOR_DELAY_UI);
                                sm.registerListener(MyService.this, sensor[2], SensorManager.SENSOR_DELAY_UI);
                            }

                            if (save_gravity_first_time) {


                                sj.start();
                                save_gravity_first_time = false;
                            }


                            new Thread() {
                                @Override
                                public void run() {
                                    updateTime = new Timer("NEW");

                                    tt = new TimerTask() {

                                        @Override
                                        public void run() {
                                            // TODO Auto-generated method stub


                                            if (!sm.registerListener(MyService.this, sensor[0], SensorManager.SENSOR_DELAY_UI)) {
                                                Log.d(TAG1, "sensor closed automatically************************************");
                                                sm.registerListener(MyService.this, sensor[0], SensorManager.SENSOR_DELAY_UI);
                                                sm.registerListener(MyService.this, sensor[1], SensorManager.SENSOR_DELAY_UI);
                                                sm.registerListener(MyService.this, sensor[2], SensorManager.SENSOR_DELAY_UI);
                                            }
                                            restart();
                                            //Log.d(TAG, "goTHREAD************************************");

                                        }
                                    };
                                    updateTime.scheduleAtFixedRate(tt, 0, 200);
                                }
                            }.start();
                        }


                        //原始状态服务停止，接下来要启动后台进程


                        status = 0x12;
                    }
                    // 原来处于播放状态
                    else if (status == 0x12) {

                    }

                    break;
                }
                case 2: {
                    // 如果原来正在运行service


                    MyService.sm.unregisterListener(MyService.this,MyService.sensor[0]);
                    MyService.sm.unregisterListener(MyService.this,MyService.sensor[1]);
                    MyService.sm.unregisterListener(MyService.this,MyService.sensor[2]);

                    player.pause();

                    if(tt!=null){
                        tt.cancel();
                    }
                    if(updateTime!=null){
                        updateTime.cancel();
                    }


                    Log.d(TAG1, "service off************************************");
                    status = 0x11;

                    break;
                }

            }
            Intent sendIntent = new Intent(MainActivity.UPDATE_ACTION);
            sendIntent.putExtra("update", status);
            //	sendIntent.putExtra("current", current);
            sendBroadcast(sendIntent);
        }
    }

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
            // TODO Auto-generated method stub

        }


        @SuppressLint("NewApi")
        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub

            //Log.d(TAG, "goSENSOR************************************");
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                for (int i = 0; i < 3; i++) {
                    acc[i] = event.values[i];
                    alldata[i] = event.values[i];
                    flag[0] = true;
                }
            }
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                for (int i = 0; i < 3; i++) {
                    gyr[i] = event.values[i];
                    alldata[i + 3] = event.values[i];
                    flag[1] = true;
                }
            }
            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                if (mutetime_CaculateAngle == 10) {
                    mutetime_CaculateAngle = 0;
                    if (first_get_gravity) {
                        for (int i = 0; i < 3; i++) {
                            gra[i] = event.values[i];
                            alldata[i + 8] = event.values[i];
                            flag[2] = true;
                        }
                        for (int count = 0; count < 3; count++) {
                            gravity_now[count] = (double) gra[count];
                            gravity_last[count] = (double) gra[count];
                        }
                        gravity_now[3] = (double) getVectorSum(gra);
                        gravity_last[3] = (double) getVectorSum(gra);
                        first_get_gravity = false;
                    } else {
                        for (int i = 0; i < 3; i++) {
                            gra[i] = event.values[i];
                            alldata[i + 8] = event.values[i];
                            flag[2] = true;
                        }
                        for (int count = 0; count < 3; count++) {
                            gravity_last[count] = gravity_now[count];
                        }
                        gravity_last[3] = gravity_now[3];

                        for (int count = 0; count < 3; count++) {
                            gravity_now[count] = (double) gra[count];
                        }
                        gravity_now[3] = (double) getVectorSum(gra);

                    }

                } else {
                    mutetime_CaculateAngle++;
                }

            }

            if (flag[0] & flag[1]) {

                alldata[6] = getVectorSum(acc);
                alldata[7] = getVectorSum(gyr);

                Float[] tempData = new Float[8];
                for (int count = 0; count < tempData.length; count++) {
                    tempData[count] = alldata[count];
                }

                if (suspector.size() >= SIZE) {
                    suspector.remove(0);
                }
                suspector.add(tempData);

                flag[0] = false;
                flag[1] = false;

                if (mutetime == 0) {
                    judgeResult = prejudge_acc(suspector);
                    if (judgeResult) {
                        Log.d(TAG1, "get suspector_coming from onsensorchanged************************************");
                        suspector_coming = true;
                        suspector_coming_influence = 5;
                        mutetime = SIZE;

                        saveSuspector();
                        Float[] result = new Float[3];
                        result = fixPositionByMinAcc();
                        data_ACCMIN = result[0];
                        data_ACCMAX = result[1];
                        anglesum = result[1];
                    }
                } else {
                    mutetime--;
                }


            }


        }

        private Float getVectorSum(Float[] vector) {
            Float vectorsum = (float) Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);
            return vectorsum;
        }

        private int ROUND = 2;

        private Float[] fixPositionByMinAcc() {

            Float beginMin = needToFixSuspector.get(6)[6];
            Float beginMax = (float) 0;
            Float[] result = new Float[3];
            Float angleMIN2MAX = (float) 0;
            indexOfMin = 6;
            for (int i = 6 - ROUND; i <= 6 + ROUND; i++) {
                if (needToFixSuspector.get(i)[6] < beginMin) {
                    indexOfMin = i;
                    beginMin = needToFixSuspector.get(i)[6];
                }
            }
            for (int i = indexOfMin; i <= 10 + indexOfMin; i++) {
                if (needToFixSuspector.get(i)[6] > beginMax) {
                    indexOfMax = i;
                    beginMax = needToFixSuspector.get(i)[6];
                }
            }
            for (int i = (indexOfMin - 2); i <= indexOfMax; i++) {
                angleMIN2MAX = +needToFixSuspector.get(i)[7];
            }
            result[0] = beginMin;
            result[1] = beginMax;
            result[2] = angleMIN2MAX;

            return result;
        }

        private void saveSuspector() {

            needToFixSuspector.clear();
            for (int i = 0; i < suspector.size(); i++) {
                Float[] temp = new Float[8];
                temp = suspector.get(i);
                needToFixSuspector.add(temp);
            }
        }


        private void judge_recover2stand(ProcessGravityVector newpgv) {
            Log.d(TAG1, "lpf_new present" + " step one" + newpgv.LPF_general.presentValue[0] + " [1]" + newpgv.LPF_general.presentValue[1] + " [2]" + newpgv.LPF_general.presentValue[2]);
            newpgv.doProcess(gravity_now);

            if (newpgv.LPF_general.gravity_value_saved.size() >= 3) {
                ProcessGravityVector.copy_Double3_data(newpgv.LPF_general.gravity_value_saved.get(0), after_falling);
                Log.d(TAG1, "caculating angle" + " after[0]" + after_falling[0] + " [1]" + after_falling[1] + " [2]" + after_falling[2]);
                before_after_angle = getVectorAngle.getAngle(before_falling, after_falling);
            }


        }


        private boolean prejudge_acc(List<Float[]> item) {
            boolean JudgeResult = false;
            boolean stepone = false;
            boolean steptwo = false;
            boolean stepthree = false;
            float angle = (float) 0.0;
            int index_min_acc = -1;
            int index_max_acc = -1;
            if (item.size() < SIZE) {
                JudgeResult = false;
            } else {
                for (int i = 6; i < 7; i++) {
                    if (item.get(i)[6] < ACCMIN) {
                        stepone = true;
                        index_min_acc = i;
                        break;
                    }
                }
                if (stepone) {
                    for (int i = index_min_acc; i < index_min_acc + 7; i++) {
                        //item.get(i)[6]>14
                        if (item.get(i)[6] > ACCMAX) {
                            steptwo = true;
                            index_max_acc = i;
                            break;
                        }
                    }
                }
                if (steptwo) {
                    //Log.d(TAG, suspector.get(suspector.size()-1)[7]+"  gorestart************************************");
                    for (int i = index_min_acc; i < index_max_acc + 1; i++) {
                        angle += item.get(i)[7];
                    }

                    //angle>7
                    if (angle > GYRMIN) {
                        judge_anglesum = angle;
                        stepthree = true;
                    }


                }
                if (stepthree) {
                    angle_change_max = (double) 0;
                    for (int i = 0; i < angle_change_save.size(); i++) {
                        if (angle_change_save.get(i) > angle_change_max) {
                            angle_change_max = angle_change_save.get(i);
                        }
                    }

                    if (angle_change_max > GRAMIN) {
                        JudgeResult = true;
                    }


                }
            }

            return JudgeResult;
        }

        private class situationJudge extends Thread {

            public void run() {
                gravitySaveTime = new Timer("CaculateAngle" + situationThreadID);


                tt = new TimerTask() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                        //Log.d(TAG, "thread is going****************************************************************,music: "+player.isPlaying());

                        if (isStarted & !first_get_gravity) {
                            Log.d(TAG1, "DOING suspector_coming " + suspector_coming + " infulence: " + suspector_coming_influence);

                            gravity_angle_change = getVectorAngle.getAngle(gravity_last, gravity_now);
                            Log.d(TAG1, "gravity_now [0]" + gravity_now[0] + " [1]" + gravity_now[1] + " [2]" + gravity_now[2]);
                            if (suspector_coming) {


                                suspector_coming_influence--;
                                if (suspector_coming_influence == 0) {
                                    if (max_angle_of_suspector > GRAMIN) {
                                        suspector_coming_influence = 5;
                                        max_angle_of_suspector = (double) 0;
                                        datalock_beforefall = true;
                                        //Log.d(TAG1, "3((((((((((((((((((((((((((((((((((((((((((((((((( ");
                                        //have_saved_general_gravity_vector=false;
                                    } else {
                                        suspector_coming = false;
                                        suspector_coming_influence = 5;
                                        max_angle_of_suspector = (double) 0;
                                        //Log.d(TAG1, "2((((((((((((((((((((((((((((((((((((((((((((((((( ");

                                        pgv_new = new ProcessGravityVector();

                                    }
                                }

                                //Log.d(TAG, "DOING2");
                                if (!have_saved_general_gravity_vector) {
                                    Log.d(TAG1, "change the before_falling!!!!!!!!!!!!!!!!!! ");
                                    suspector_coming_influence = 5;
                                    if (!have_falled_before) {
                                        ProcessGravityVector.copy_Double3_data(PGV.LPF_general.gravity_value_saved.get(0), before_falling);
                                    } else {
                                        Log.d(TAG1, "1((((((((((((((((((((((((((((((((((((((((((((((((( ");
                                        ProcessGravityVector.copy_Double3_data(trusty_before_falling, before_falling);
                                        have_falled_before = false;
                                    }

                                    pgv_new.LPF_general.clear_saved_data();
                                    //Log.d(TAG,"check pgv_new"+ pgv_new.LPF_general.gravity_value_saved.get(0)[0]+" [1]"+ pgv_new.LPF_general.gravity_value_saved.get(0)[1]+" [2]"+ pgv_new.LPF_general.gravity_value_saved.get(0)[2]);

                                }
                                have_saved_general_gravity_vector = true;
                                judge_recover2stand(pgv_new);
                                //Log.d(TAG1, "DOING" + " before[0]" + before_falling[0] + " [1]" + before_falling[1] + " [2]" + before_falling[2]);
                                //Log.d(TAG1, "DOING" + " after[0]" + after_falling[0] + " [1]" + after_falling[1] + " [2]" + after_falling[2]);
                                //Log.d(TAG1, "before after angle " + before_after_angle);
                                if (max_angle_of_suspector < before_after_angle) {
                                    max_angle_of_suspector = before_after_angle;
                                }
                                if (before_after_angle > GRAMIN && !have_falled_before) {
                                    have_falled_before = true;
                                    //is_FromFallToRecover=true;
                                    ProcessGravityVector.copy_Double3_data(before_falling, trusty_before_falling);
                                    if(!player.isPlaying()){
                                        player.start();
                                    }

                                    //Log.d(TAG1,"passed music play command!!!!!!!!!!!!!!!!!!!!!!!!!");
                                } else {
                                    if (before_after_angle < 20 && before_after_angle >= 0) {
                                        have_falled_before = false;
                                        have_saved_general_gravity_vector = false;
                                        pgv_new = new ProcessGravityVector();


                                        if(player.isPlaying()){
                                            player.pause();
                                        }
                                    }

                                }

                                if (is_FromFallToRecover && !player.isPlaying() && !suspector_coming) {
                                    is_FromFallToRecover = false;
                                    have_saved_general_gravity_vector = false;
                                }


                            } else {
                                PGV.doProcess(gravity_now);
                                if (have_saved_general_gravity_vector || have_falled_before) {
                                    have_saved_general_gravity_vector = false;
                                    have_falled_before = false;
                                }
                            }

                            //Log.d(TAG,"saved 1st gravity: [0]"+PGV.LPF_general.gravity_value_saved.get(0)[0]+" [1]"+PGV.LPF_general.gravity_value_saved.get(0)[1]+" [2]"+PGV.LPF_general.gravity_value_saved.get(0)[2]);

                            Double temp = new Double(gravity_angle_change);
                            if (angle_change_save.size() >= 5) {
                                angle_change_save.remove(0);
                            }
                            angle_change_save.add(temp);
                        }


                    }
                };

                gravitySaveTime.scheduleAtFixedRate(tt, 0, 600);

            }

        }


        private void restart() {
            String TAG = "AKS";


            try {

                //Log.d(TAG, suspector.get(suspector.size()-1)[6]+"  gorestart************************************");
                if (judgeResult) {
                    //Log.d(TAG, "gorejudge************************************");
                    fos = new FileOutputStream(fi.getAbsolutePath(), true);
                    if (fos != null) {
                        //Log.d(TAG, "gorenewline************************************");
                        Calendar happentime = Calendar.getInstance();
                        fos.write(("screen-off-mode  happen time: ").toString().getBytes());
                        fos.write((happentime.getTime().toString()).getBytes());
                        fos.write(("  judge_anglesum: " + judge_anglesum + " gravity_angle_change:" + angle_change_max + " SUSPECTOR:" + needToFixSuspector.size()
                                + " accmin:" + data_ACCMIN + " INDEX_accmin:" + indexOfMin
                                +" before_after_angle"+before_after_angle
                                ).toString().getBytes());
                        byte[] newLine = "\r\n".getBytes();
                        fos.write(newLine);

                        for (int i = (indexOfMin - 4); i < OUTPUT_SIZE + (indexOfMin - 4); i++) {
                            fos.write(((("  acc_x=" + needToFixSuspector.get(i)[0] + "  acc_y=" + needToFixSuspector.get(i)[1] + "  acc_z=" + needToFixSuspector.get(i)[2]
                                    + "  gyr_x=" + needToFixSuspector.get(i)[3] + "  gyr_y=" + needToFixSuspector.get(i)[4] + "  gyr_z=" + needToFixSuspector.get(i)[5]
                                    + "  gyr_sum=" + needToFixSuspector.get(i)[7] + "  acc_sum=" + needToFixSuspector.get(i)[6]).toString())).getBytes());
                            byte[] newLine1 = "\r\n".getBytes();
                            fos.write(newLine1);
                        }

                        byte[] newLine2 = (mutetime + "\r\n").getBytes();
                        fos.write(newLine2);
                        fos.flush();
                    } else {
                        //Log.d(TAG, "gorenewline null************************************");
                        try {
                            fos = new FileOutputStream(fi.getAbsolutePath(), true);

                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    judgeResult = false;
                }


            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


    }





