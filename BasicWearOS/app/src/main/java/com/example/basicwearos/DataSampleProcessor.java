package com.example.basicwearos;

import com.google.common.collect.EvictingQueue;
import java.util.Iterator;

public class DataSampleProcessor {
    private Float[][] allChannelData;
    private static int N_CHANNELS = 0;
    private static int TS_LEN = 0;

    public DataSampleProcessor(int nChannels, int qSz) {
        N_CHANNELS = nChannels;
        TS_LEN = qSz;
        allChannelData = new Float[N_CHANNELS][];
        for(int i=0; i<N_CHANNELS; i++){
            allChannelData[i] = new Float[TS_LEN];
            for(int j=0; j<TS_LEN; j++){
                allChannelData[i][j] = new Float(0);
            }
        }
    }

    public boolean setData(EvictingQueue<Float> dataQ, int channel){
        if(channel >= N_CHANNELS){
            return false;
        }
        Iterator<Float> item = dataQ.iterator();
        int t = 0;
        while(item.hasNext()){
            allChannelData[channel][t++] = item.next();
        }
        return true;
    }

    // calc all channel mean
    public float[] calcAllMean() throws Exception{
        float[] means = new float[N_CHANNELS];
        for(int c=0; c<N_CHANNELS; c++) {
            means[c] = 0.0f;
            for (int i = 0; i < TS_LEN; i++) {
                try {
                    means[c] += allChannelData[c][i];
                }
                catch (ArrayIndexOutOfBoundsException ae)
                {
                    throw ae;
                }
            }
            means[c] = means[c] / TS_LEN;
        }
        return means;
    }
}
