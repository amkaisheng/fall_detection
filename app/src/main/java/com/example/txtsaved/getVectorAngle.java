package com.example.txtsaved;

/**
 * Created by action on 2016/11/12.
 */
public class getVectorAngle {
    public static Double getVectorSum(Double[] vectorA){
        Double sum=Math.sqrt(vectorA[0]*vectorA[0]+vectorA[1]*vectorA[1]+vectorA[2]*vectorA[2]);
        return sum;
    }

    public static Double getAngle(Double[] vectorA,Double[] vectorB){
        Double sumA=getVectorSum(vectorA);
        Double sumB=getVectorSum(vectorB);
        Double cosin=(vectorA[0]*vectorB[0]+vectorA[1]*vectorB[1]+vectorA[2]*vectorB[2])/(sumA*sumB);
        if(cosin>1.0&cosin<1.01){
            cosin=0.9999;
        }
        return Math.acos(cosin)*57.2958;
    }

    public static void main(String[] args){
        Double[] vA={-8.18,4.75,1.37};
        Double[] vB={0.44,4.9,8.45};
        Double result=getAngle(vA,vB);
        System.out.println(result);
        System.out.println(getVectorSum(vA));
        System.out.println(getVectorSum(vB));

    }

}
