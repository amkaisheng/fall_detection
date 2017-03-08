package com.example.txtsaved;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by action on 2016/11/16.
 */
public class LowPassFilter {
    final int SAVED_LENGTH=3;
    Double[] presentValue=new Double[3];
    public List<Double[]> gravity_value_saved = new ArrayList<Double[]>();
    //Double SUM_presentValue;
    public Double doLowPass(Double presentValue,Double newInput){
        presentValue = (Double) (0.2 * newInput + 0.8 * presentValue);
        return  presentValue;
    }
    public void clear_saved_data(){
        gravity_value_saved.clear();
        for(int i=0;i<3;i++){
            Double[] temp=new Double[3];
            for(int j=0;j<3;j++){
                temp[j]=(double)0;
            }

            gravity_value_saved.add(temp);
        }
        Double[] temp=new Double[3];
        for(int j=0;j<3;j++){
            temp[j]=(double)0;
        }
        presentValue=temp;
    }
    public void doAllLowPass(Double[] newInput){
        if(gravity_value_saved.size()>3){
            gravity_value_saved.remove(0);
        }
        Double[] temp=new Double[3];
        for(int i=0;i<3;i++){
            temp[i]=presentValue[i];
        }
        gravity_value_saved.add(temp);
        presentValue[0]=doLowPass(presentValue[0],newInput[0]);
        presentValue[1]=doLowPass(presentValue[1],newInput[1]);
        presentValue[2]=doLowPass(presentValue[2],newInput[2]);
        //SUM_presentValue=doLowPass(SUM_presentValue,newInput[3]);

    }
}
