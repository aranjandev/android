package com.example.basicwearos;

import com.google.common.collect.EvictingQueue;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Vector;

public class DataSampleProcessor {

    public Vector calcStats(Vector<Float> vec){
        Vector stats = new Vector(2);
        double input[] = ArrayUtils.toPrimitive(vec.toArray(new Double[vec.size()]));
        stats.set(0, StatUtils.mean(input));
        stats.set(1, StatUtils.percentile(input, 50));
        return stats;
    }
}
