package com.example.packetcapture.bin;

import android.util.Log;

import com.example.packetcapture.VowifiService;

import java.io.IOException;
import java.io.PipedInputStream;

public class StateTracer{

    // Tag for Logcat
    private final String TAG = getClass().getSimpleName();

    // Decision Tree parameters
    private final int[] lChilds = {1, 2, 3, 4, 5, -1, -1, -1, 9, 10, 11, -1, -1, -1, 15, -1, -1, 18, 19, 20, -1, -1, 23, -1, -1, 26, 27, 28, -1, -1, -1, 32, -1, 34, -1, 36, -1, -1, 39, 40, -1, -1, 43, 44, -1, -1, -1};
    private final int[] rChilds = {38, 17, 8, 7, 6, -1, -1, -1, 14, 13, 12, -1, -1, -1, 16, -1, -1, 25, 22, 21, -1, -1, 24, -1, -1, 31, 30, 29, -1, -1, -1, 33, -1, 35, -1, 37, -1, -1, 42, 41, -1, -1, 46, 45, -1, -1, -1};
    private final double[] thresholds = {9.5, 4.5, 0.0, 1.5, 0.5, -2.0, -2.0, -2.0, 3.0, 1.5, 326.0, -2.0, -2.0, -2.0, 430.0, -2.0, -2.0, 6.5, 430.0, 5.5, -2.0, -2.0, 902.0, -2.0, -2.0, 742.0, 0.0, 7.5, -2.0, -2.0, -2.0, 878.0, -2.0, 1054.0, -2.0, 7.5, -2.0, -2.0, 11.5, 486.0, -2.0, -2.0, 462.0, 12.5, -2.0, -2.0, -2.0};
    private final int[] indices = {2, 2, 0, 2, 2, -2, -2, -2, 2, 2, 1, -2, -2, -2, 1, -2, -2, 2, 1, 2, -2, -2, 1, -2, -2, 1, 0, 2, -2, -2, -2, 1, -2, 1, -2, 2, -2, -2, 2, 1, -2, -2, 1, 2, -2, -2, -2};
    private final int[][] classes = {{5, 9, 29, 9, 5, 1053, 3, 25, 3, 3, 1315, 12, 3}, {4, 9, 29, 9, 5, 1053, 3, 25, 3, 3, 0, 0, 0}, {4, 9, 29, 9, 3, 1, 0, 0, 0, 0, 0, 0, 0}, {4, 8, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {4, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {4, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 1, 29, 0, 3, 1, 0, 0, 0, 0, 0, 0, 0}, {0, 1, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 3, 1, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 2, 1052, 3, 25, 3, 3, 0, 0, 0}, {0, 0, 0, 0, 2, 1052, 0, 3, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 1, 1052, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 1049, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 1, 0, 0, 3, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 3, 22, 3, 3, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 1, 20, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 2, 2, 3, 3, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 2, 2, 3, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1315, 12, 3}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1315, 3, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1315, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0}, {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 3}, {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0}, {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3}};

    // States during VoWifi conversation
    public enum State {
        NONE, INVITE1, TRYING2, SESSION3, PRACK4, OK5, RINGING6, OK7, OK8, OK9, ACK10, VOICE11, BYE12, OK13;
    }

    // Directions indicate the packet flow
    public enum Direction {
        UPWARD(1), DOWNWARD(-1);

        public final int value;
        Direction(int value){
            this.value = value;
        }
    }

    // Current state
    private int state;

    public StateTracer(){
        state = State.NONE.ordinal();
    }

//    @Override
//    public void run() {
//        int oldstate;
//        byte[] packet = new byte[3];
//        try{
//            // byte[0] as Direction, byte[1-2] as length
//            while( pipe.read(packet, 0, 3) != -1 ){
//                double dir = packet[0];
//                double len = (((int)packet[1])<<8) & ((int)packet[2]);
//
//                Log.d(TAG, String.format("Read pipe: [%d, %d]", (int)dir, (int)len));
//
//                oldstate = state;
//                state = nextState(new double[]{dir, len, state}, 0);
//
//                if (oldstate != state){
//                    //State change
//                    Log.d(TAG, "State: " + oldstate + " -> " + state);
//                }else{
//                    Log.d(TAG, "State: " + state);
//                }
//            }
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//
//    }

    public int nextState(Direction dir, int length) {
        return this.nextState(new double[]{dir.value, length, state}, 0);
    }

    int nextState(int dir, int length){
        return this.nextState(new double[]{dir, length, state}, 0);
    }

    protected int nextState(double[] features, int node) {
        if (this.thresholds[node] != -2) {
            if (features[this.indices[node]] <= this.thresholds[node]) {
                return nextState(features, this.lChilds[node]);
            } else {
                return nextState(features, this.rChilds[node]);
            }
        }
        return findMax(this.classes[node]);
    }

    private int findMax(int[] nums) {
        int index = 0;
        for (int i = 0; i < nums.length; i++) {
            index = nums[i] > nums[index] ? i : index;
        }
        return index;
    }

    public void setState(State stateEnum){
        setState(stateEnum.ordinal());
    }

    protected void setState(int state){
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
