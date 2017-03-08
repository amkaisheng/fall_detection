package com.example.txtsaved;

import android.media.MediaPlayer;
import android.util.Log;

/**
 * Created by action on 2016/11/16.
 */
public class ProcessGravityVector {
    String TAG="PGV_IN";
    boolean firsttime=true;
    boolean reach_threshold=false;
    final Double GRAVITY_ANGLE_CHANGE_THRESHOLD=(double)30;
    final int COMPARING_TIME=3;
    boolean isComparing=false;
    int comparingCount=0;
    Double newAngleError=(double)0;
    Double oldAngleError=(double)0;
    Double[] last_gravity=new Double[3];
    //Double anglechage=(double)0;
    LowPassFilter LPF_general=new LowPassFilter();
    LowPassFilter LPF_new=new LowPassFilter();
    public void doLowPass(LowPassFilter lpf, Double[] newInput){
        lpf.doAllLowPass(newInput);
    }
    public void beginComparingUnit(Double[] newInput){
        LPF_new.presentValue=newInput;
    }
    public void endComparingUnit(){
        newAngleError=(double)0;
        oldAngleError=(double)0;
        comparingCount=0;


    }

    public void cleardata(){
        newAngleError=(double)0;
        oldAngleError=(double)0;
        comparingCount=0;
        firsttime=true;
        reach_threshold=false;
        isComparing=false;
        LPF_general.gravity_value_saved.clear();
        LPF_new.gravity_value_saved.clear();


    }
    public void doProcess(Double[] newInput){
        if(firsttime){
            LPF_general.presentValue[0]=newInput[0];
            LPF_general.presentValue[1]=newInput[1];
            LPF_general.presentValue[2]=newInput[2];
            Log.d(TAG,"DOING"+" LPF[0]"+ LPF_general.presentValue[0]+" [1]"+ LPF_general.presentValue[1]+" [2]"+ LPF_general.presentValue[2]);
            firsttime=false;
        }
        else{
            Double anglechage=getVectorAngle.getAngle(LPF_general.presentValue,newInput);
            Log.d(TAG,"present value "+" LPF[0]"+ LPF_general.presentValue[0]+" [1]"+ LPF_general.presentValue[1]+" [2]"+ LPF_general.presentValue[2]);
            //Log.d(TAG,"NEW: [0]"+newInput[0]+" [1]"+newInput[1]+" [2]"+newInput[2]);
            if(anglechage>GRAVITY_ANGLE_CHANGE_THRESHOLD){
                reach_threshold=true;
                if(isComparing){
                    Double angleChangeWithNewProcess=getVectorAngle.getAngle(LPF_new.presentValue,newInput);
                    if(angleChangeWithNewProcess<GRAVITY_ANGLE_CHANGE_THRESHOLD){
                        comparingCount++;
                        doLowPass(LPF_new,newInput);
                        newAngleError+=angleChangeWithNewProcess;
                        oldAngleError+=anglechage;
                        if(comparingCount>=COMPARING_TIME){
                            comparingCount=0;
                            if(newAngleError<oldAngleError){
                                LPF_general.presentValue[0]=LPF_new.presentValue[0];
                                LPF_general.presentValue[1]=LPF_new.presentValue[1];
                                LPF_general.presentValue[2]=LPF_new.presentValue[2];
                                LPF_general.gravity_value_saved.addAll(LPF_new.gravity_value_saved);
                                isComparing=false;
                                newAngleError=(double)0;
                                oldAngleError=(double)0;
                            }
                        }
                    }
                    else{
                        endComparingUnit();
                    }

                }
                else{
                    isComparing=true;
                    beginComparingUnit(newInput);
                }

            }
            else{
                reach_threshold=false;
                if(isComparing){
                    comparingCount++;
                    Double anglechange_new=getVectorAngle.getAngle(LPF_new.presentValue,newInput);
                    doLowPass(LPF_new,newInput);
                    newAngleError+=anglechange_new;
                    oldAngleError+=anglechage;
                    if(comparingCount>=COMPARING_TIME){
                        comparingCount=0;
                        if(newAngleError<oldAngleError){
                            LPF_general.presentValue[0]=LPF_new.presentValue[0];
                            LPF_general.presentValue[1]=LPF_new.presentValue[1];
                            LPF_general.presentValue[2]=LPF_new.presentValue[2];
                            LPF_general.gravity_value_saved.addAll(LPF_new.gravity_value_saved);

                        }
                        isComparing=false;
                        newAngleError=(double)0;
                        oldAngleError=(double)0;
                    }

                }
            }
            doLowPass(LPF_general,newInput);
            Log.d(TAG,"saved 1st gravity: [0]"+LPF_general.gravity_value_saved.get(0)[0]+" [1]"+LPF_general.gravity_value_saved.get(0)[1]+" [2]"+LPF_general.gravity_value_saved.get(0)[2]);
        }

    }

    public static void copy_Double3_data(Double[] from,Double[] to){
        to[0]=from[0];
        to[1]=from[1];
        to[2]=from[2];
    }

    public static void main(String[] args){
        Double[][] testdata1={
                {0.9,2.4,9.4},
                {0.5,2.5,9.5},
                {0.20,2.33,9.52},
                {0.28,2.54,9.47},

                {9.24,0.40,3.27},
                {9.20,0.39,3.39},
                {9.13,0.43,3.56},
                {9.13,0.43,3.56},
                {9.13,0.43,3.56},
                {9.13,0.43,3.56},
                {9.24,0.40,3.27},
                {9.24,0.40,3.27}
        };

        Double[][] testdata2={
                {0.9,2.4,9.4},
                {0.5,2.5,9.5},
                {0.20,2.33,9.52},
                {0.28,2.54,9.47},

                {9.24,0.40,3.27},
                {0.20,2.33,9.52},
                {0.5,2.5,9.5},
                {0.20,2.33,9.52},
                {0.5,2.5,9.5},
                {0.20,2.33,9.52},
                {0.20,2.33,9.52},
        };



        Double[] vB={0.2,4.9,8.45};

        ProcessGravityVector PGV = new ProcessGravityVector();
        for(int i=0;i<testdata1.length;i++){
            PGV.doProcess(testdata1[i]);
            if(i>3){
                System.out.println(PGV.LPF_general.gravity_value_saved.get(0)[0]+" "+PGV.LPF_general.gravity_value_saved.get(1)[0]+" "+PGV.LPF_general.gravity_value_saved.get(2)[0]);
            }

        }
        //MediaPlayer player  =   new MediaPlayer();
        //player.setDataSource("");



    }
}
