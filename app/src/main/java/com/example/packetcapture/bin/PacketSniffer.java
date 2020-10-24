package com.example.packetcapture.bin;

import android.util.Log;

import com.example.packetcapture.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedOutputStream;

public class PacketSniffer implements Runnable {

    private boolean looping;
    private Process process;
    private StateTracer tracer;

    private final String TAG = getClass().getSimpleName();
    private final String[] commands = {
            "su", "-c",
            "tcpdump", "-ttt", "-q", "-n", "-U", "udp", "port", "4500", "-i", "any"
            //,"-c 10"
    };

    public PacketSniffer() {
        looping = true;
        tracer = new StateTracer();
    }

    @Override
    public void run() {

        // 開啟 Process
        try {
            Log.d(TAG, "Listening.");
            process = new ProcessBuilder().command(commands).start();
        } catch (IOException e) {
            Log.e(TAG, "Unable to open shell");
            e.printStackTrace();
            return;
        }

        //初始化
        tracer.setState(StateTracer.State.NONE);

        // 傾聽 Stdout
        Log.d(TAG, "-- Captured Packets --");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        try {
            String line; //  tcpdump 每行輸出


            // 輸出格式:
            //   TT IP XX.XX.XX.XX.PORT > XX.XX.XX.XX.PORT: UDP, length NN
            while (looping && ((line = reader.readLine()) != null)) {
                String[] arr = line.trim().split(" +");

                if (arr.length < 8) continue;
                // 非 IP4 封包
                if (!arr[1].equals("IP")) continue;

                // 非 UDP 封包
                // if (!arr[6].startsWith("UDP")) continue;

                String tim = arr[0];
                String src = arr[2]; // X.X.X.X
                String dst = arr[4]; // X.X.X.X:

                // TODO - 根據 IP 過濾封包

                int len = Integer.valueOf(arr[7]);

                Log.d(TAG, new StringBuilder(64).append(tim)
                        .append("  ")
                        .append(src.startsWith("192") ? "UE" : "ePDG")
                        .append(" -> ")
                        .append(dst.startsWith("192") ? "UE" : "ePDG")
                        .append(" len = ")
                        .append(len).toString()
                );

                // 決策樹分析
                if (len >= 190){
                    // TODO - 正確的判斷傳送方向
                    int dir = dst.startsWith("192") ? StateTracer.Direction.DOWNWARD.value
                            : StateTracer.Direction.UPWARD.value;

                    int nextState = tracer.nextState(dir, len);
                    if ( nextState != tracer.getState() ){
                        // 狀態改變
                        tracer.setState(nextState);

                        Log.d(TAG, "State -> "+nextState);

                    }else {
                        Log.d(TAG, "State "+tracer.getState());
                    }

                }else{
                    // 判斷網路速率
                }

            }
            Log.d(TAG, "-- Captured Packets end --");

        } catch (IOException e) {}


        if (BuildConfig.DEBUG) {
            // 監聽 Stderr
            Log.d(TAG, "-- Error Channel --");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                String info;
                while ((info = br.readLine()) != null) {
                    Log.d(TAG, info);
                }

            } catch (IOException e) {}
            Log.d(TAG, "-- Error Channel ends --");
        }

        Log.d(TAG, "Stopped.");
    }

    public boolean isStopped() {
        return looping;
    }

    public void stop() {
        // TODO - 繼承 Thread 複寫 destroy 方法
        looping = false;
        if(process != null){
            process.destroy();
        }
    }

}
