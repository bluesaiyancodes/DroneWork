package com.silverdynsoftware.tellosecond;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.silverdynsoftware.tellosecond.greenRobot.eventsGreenRobot;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler;
import nl.bravobit.ffmpeg.FFmpeg;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class MainActivity extends AppCompatActivity {

    TextView tvMessage;
    TextView tvStatus;
    TextView tvVideo;

    private DatagramSocket socketMainSending;
    private InetAddress inetAddressMainSending;
    public static final int portMainSending = 8889;
    public static final String addressMainSending = "192.168.10.1";

    DatagramSocket socketStatusServer;
    DatagramSocket socketStreamOnServer;

    // Status Receiver
    StatusDatagramReceiver statusDatagramReceiver;
    StreamOnDatagramReceiver streamOnDatagramReceiver;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvMessage = findViewById(R.id.tvMessage);
        tvStatus = findViewById(R.id.tvStatus);
        tvVideo = findViewById(R.id.tvVideo);
        enableStrictMode();
    }

    public void enableStrictMode()
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }



    public void on_click_btnInitialize(View v) {
        try {
            socketMainSending = new DatagramSocket();
            inetAddressMainSending = getInetAddressByName(addressMainSending); // InetAddress.getByName(addressMainSending);
            if (inetAddressMainSending == null) {
                tvMessage.setText("inet Address Main Sending is null");
            } else {
                tvMessage.setText("Initialize without error");
            }

            socketStatusServer = new DatagramSocket(null);
            InetSocketAddress addressStatus = new InetSocketAddress("0.0.0.0", 8890);
            socketStatusServer.bind(addressStatus);


            socketStreamOnServer = new DatagramSocket(null);
            InetSocketAddress addressStreamOn = new InetSocketAddress("0.0.0.0", 11111);
            socketStreamOnServer.bind(addressStreamOn);

        } catch (IOException e) {
            tvMessage.setText("Error on initialize: " + e.getMessage());
        }
    }

    public void on_click_btnSendCommand(View v) {
        SendOneCommand sendOneCommand = new SendOneCommand();
        sendOneCommand.doInBackground("command");
    }

    public void on_click_btnGetBattery(View v) {
        SendOneCommand sendOneCommand = new SendOneCommand();
        sendOneCommand.doInBackground("battery?");
    }

    public void on_click_btnGetTemp(View v) {
        SendOneCommand sendOneCommand = new SendOneCommand();
        sendOneCommand.doInBackground("temp?");
    }

    public void on_click_btnStartStatus(View v) {
        SendOneCommand sendOneCommand = new SendOneCommand();
        sendOneCommand.doInBackground("command");
        statusDatagramReceiver = new StatusDatagramReceiver();
        statusDatagramReceiver.start();
    }

    public void on_click_btnEndStatus(View v) {
        statusDatagramReceiver.kill();
    }

    public void on_click_btnStartVideo(View v) {
        SendOneCommand sendOneCommand = new SendOneCommand();
        sendOneCommand.doInBackground("streamon");
        streamOnDatagramReceiver = new StreamOnDatagramReceiver();
        streamOnDatagramReceiver.start();
    }

    public void on_click_btnStopVideo(View v) {
        SendOneCommand sendOneCommand = new SendOneCommand();
        sendOneCommand.doInBackground("streamoff");
        streamOnDatagramReceiver.kill();
    }

    public void on_click_btnSendStreamOnCommandOnly(View v) {
        SendOneCommand sendOneCommand = new SendOneCommand();
        sendOneCommand.doInBackground("streamon");
    }

    public class SendOneCommand extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            String command = strings[0];
            byte[] buf = strings[0].getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, inetAddressMainSending, portMainSending);
            try {
                socketMainSending.send(packet);
                buf =new byte[500];
                packet = new DatagramPacket(buf, buf.length);
                socketMainSending.receive(packet);
                String doneText = new String(packet.getData(), 0,packet.getLength(), StandardCharsets.UTF_8);
                EventBus.getDefault().post(new eventsGreenRobot.lastReceivedMessage(command + ": " + doneText));
            } catch (IOException e) {
                EventBus.getDefault().post(new eventsGreenRobot.lastReceivedMessage(command + ": IOException - " + e.getMessage()));
            } catch (Exception e) {
                EventBus.getDefault().post(new eventsGreenRobot.lastReceivedMessage(command + ": Exception - " + e.getMessage()));
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(eventsGreenRobot.lastReceivedMessage event) {
        String lastMessage = (String) event.lastMessage;
        tvMessage.setText(lastMessage);
    };

    public static InetAddress getInetAddressByName(String name)
    {
        AsyncTask<String, Void, InetAddress> task = new AsyncTask<String, Void, InetAddress>()
        {

            @Override
            protected InetAddress doInBackground(String... params)
            {
                try
                {
                    return InetAddress.getByName(params[0]);
                }
                catch (UnknownHostException e)
                {
                    return null;
                }
            }
        };
        try
        {
            return task.execute(name).get();
        }
        catch (InterruptedException e)
        {
            return null;
        }
        catch (ExecutionException e)
        {
            return null;
        }

    }

    private class StatusDatagramReceiver extends Thread {
        private boolean bKeepRunning = true;
        private String lastMessage = "";

        @Override
        public void run() {
            String message;
            byte[] lmessage = new byte[500];
            DatagramPacket packet = new DatagramPacket(lmessage, lmessage.length);

            try {

                while(bKeepRunning) {
                    socketStatusServer.receive(packet);
                    message = new String(lmessage, 0, packet.getLength());
                    lastMessage = message;
                    EventBus.getDefault().post(new eventsGreenRobot.lastReceivedStatus("Status: " + lastMessage));
                }

                if (socketStatusServer == null) {
                    socketStatusServer.close();
                }

            } catch (IOException ioe){
                EventBus.getDefault().post(new eventsGreenRobot.lastReceivedStatus("Status: IOException - " + ioe.getMessage()));
            }

        }

        public void kill() {
            bKeepRunning = false;
        }
    }

    private class StreamOnDatagramReceiver extends Thread {
        private boolean bKeepRunning = true;
        private String lastMessage = "";

        @Override
        public void run() {
            String message;
            byte[] lmessage = new byte[50000];
            DatagramPacket packet = new DatagramPacket(lmessage, lmessage.length);

            try {

                while(bKeepRunning) {
                    socketStreamOnServer.receive(packet);
                    message = new String(lmessage, 0, packet.getLength());
                    lastMessage = message;
                    EventBus.getDefault().post(new eventsGreenRobot.StreamOnReceivedMessage("Video: " + lastMessage));
                }

                if (socketStreamOnServer == null) {
                    socketStreamOnServer.close();
                }

            } catch (IOException ioe){
                EventBus.getDefault().post(new eventsGreenRobot.StreamOnReceivedMessage("Video: IOException - " + ioe.getMessage()));
            }

        }

        public void kill() {
            bKeepRunning = false;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(eventsGreenRobot.StreamOnReceivedMessage event) {
        String streamMessage = (String) event.streamMessage;
        tvVideo.setText(streamMessage);
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(eventsGreenRobot.lastReceivedStatus event) {
        String lastStatus = (String) event.lastStatus;
        tvStatus.setText(lastStatus);
    };

    public void on_click_btnTestFfmpeg(View v) {
        if (FFmpeg.getInstance(this).isSupported()) {
            tvMessage.setText("FFmpeg is supported!!!");
        } else {
            tvMessage.setText("FFmpeg is not supported.");
        }
    }

    public void on_click_btnTestVersion(View v) {
        String[] cmd = {"-version"};
        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        // to execute "ffmpeg -version" command you just need to pass "-version"
        ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

            @Override
            public void onStart() {}

            @Override
            public void onProgress(String message) {}

            @Override
            public void onFailure(String message) {

            }

            @Override
            public void onSuccess(String message) {
                tvMessage.setText("Version: " + message);
            }

            @Override
            public void onFinish() {}

        });
    }

    public void on_click_btnCaptureVideoStream(View v) {
        // Original java line for FFMpeg
        // Runtime.getRuntime().exec("ffmpeg -i udp://0.0.0.0:11111 -f sdl Tello");

        String commandString = " -i udp://0.0.0.0:11111 -f sdl Tello ";
        String[] cmd;
        cmd = commandString.split(" ");
        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        // to execute "ffmpeg -version" command you just need to pass "-version"

        ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

            @Override
            public void onStart() {}

            @Override
            public void onProgress(String message) {}

            @Override
            public void onFailure(String message) {
                tvMessage.setText("FFMPEG streaming command failure: " + message);
            }

            @Override
            public void onSuccess(String message) {
                tvMessage.setText("FFMPEG streaming command success: " + message);
            }

            @Override
            public void onFinish() {}

        });
    }




}
