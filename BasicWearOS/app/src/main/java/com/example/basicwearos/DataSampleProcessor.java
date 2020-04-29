package com.example.basicwearos;

import com.google.common.collect.EvictingQueue;

import java.util.Iterator;
import java.util.Vector;

public class DataSampleProcessor {

    // Simple mean calculation method
    public Vector<Float> calcMean(EvictingQueue<Float> que){
        Vector<Float> out = new Vector<Float>(1);
        if(que.size() == 0){
            out.add(0.0f);
            return out;
        }
        float sum = 0.0f;
        Iterator item = que.iterator();
        while(item.hasNext()){
            sum += ((Float)item.next()).floatValue();
        }
        out.add(sum / que.size());
        return out;
    }
}
