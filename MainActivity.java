package com.example.chatting_socket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private Handler mHandler;
    InetAddress serverAddr;
    Socket socket;
    PrintWriter sendWriter;
    private String ip = "10.0.2.2"; // 에뮬레이터에서 로컬 서버로 연결
    private int port = 8888;

    TextView textView;
    String UserID;
    Button connectbutton;
    Button chatbutton;
    LinearLayout chatView;  // TextView에서 LinearLayout로 변경
    EditText message;
    String sendmsg;
    String read;

    @Override
    protected void onStop() {
        super.onStop();
        try {
            sendWriter.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();
        textView = (TextView) findViewById(R.id.textView);
        chatView = (LinearLayout) findViewById(R.id.chatView);  // LinearLayout로 캐스팅
        message = (EditText) findViewById(R.id.message);
        Intent intent = getIntent();
        UserID = intent.getStringExtra("username");
        textView.setText(UserID);
        chatbutton = (Button) findViewById(R.id.chatbutton);

        new Thread() {
            public void run() {
                try {
                    InetAddress serverAddr = InetAddress.getByName(ip);
                    socket = new Socket(serverAddr, port);
                    sendWriter = new PrintWriter(socket.getOutputStream());
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while(true){
                        read = input.readLine();

                        if(read != null){
                            mHandler.post(new msgUpdate(read));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        chatbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendmsg = message.getText().toString();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            sendWriter.println(UserID + ">" + sendmsg);
                            sendWriter.flush();
                            message.setText("");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
    }

    class msgUpdate implements Runnable {
        private String msg;

        public msgUpdate(String str) {
            this.msg = str;
        }

        @Override
        public void run() {
            // 새로운 메시지를 표시할 LinearLayout 생성
            LinearLayout messageLayout = new LinearLayout(MainActivity.this);
            messageLayout.setOrientation(LinearLayout.VERTICAL);
            messageLayout.setPadding(10, 10, 10, 10);
            messageLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            // 사용자 이름 텍스트
            TextView userNameTextView = new TextView(MainActivity.this);
            // 사용자 이름과 메시지를 분리하여 표시
            String[] parts = msg.split(">", 2);
            userNameTextView.setText(parts[0]);  // 사용자 이름
            userNameTextView.setTextSize(16);
            userNameTextView.setPadding(10, 10, 10, 5);

            // 메시지 텍스트
            TextView messageTextView = new TextView(MainActivity.this);
            if (parts.length > 1) {
                messageTextView.setText(parts[1]);  // 메시지
            }
            messageTextView.setTextSize(18);
            messageTextView.setBackgroundResource(android.R.drawable.edit_text);
            messageTextView.setPadding(10, 10, 10, 10);

            // 레이아웃에 텍스트뷰 추가
            messageLayout.addView(userNameTextView);
            messageLayout.addView(messageTextView);

            // 채팅 뷰에 메시지 추가
            chatView.addView(messageLayout);
        }
    }
}
