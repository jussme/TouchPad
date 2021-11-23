package com.example.touchpad;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.example.touchpad.communication.LogInServer;
import com.example.touchpad.communication.InputSender;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TouchPadActivity extends AppCompatActivity {
    private final View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        private final ExecutorService executorService = Executors.newSingleThreadExecutor();

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            try {
                return executorService.submit(() -> inputSender.sendInput(v, event)).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return false;
        }
    };

    private InputSender inputSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touchpad);

        InetSocketAddress address = (InetSocketAddress) getIntent()
                .getSerializableExtra(LogInServer.CLIENT_INET_SOCKET_ADDRESS);
        try {
            inputSender = new InputSender(address);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        View pane = (View) findViewById(R.id.touchpadPane);
        pane.setOnTouchListener(onTouchListener);
    }
}