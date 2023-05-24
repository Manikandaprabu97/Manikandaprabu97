package com.example.awcv;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {
    // Declare the Socket instance
    private Socket mSocket;

    {
        try {
            mSocket = IO.socket("http://ip_address.socket.io/port");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSocket.connected()) {
                    Toast.makeText(MainActivity.this, " Message sent to server", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Socket.IO not connected", Toast.LENGTH_SHORT).show();
                }
                sendMessageToServer("Hello, awcv from Mobile app!",getGetOnNewMessage);

            }
        });
        mSocket.connect();
        mSocket.on(Socket.EVENT_CONNECT, getOnSocketConnected);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onSocketConnectError);
        // Register the "new message" event listener
        mSocket.on("parameter", getGetOnNewMessage);
    }

    private void sendMessageToServer(String message, final Emitter.Listener callback) {
        mSocket.emit("status", message, callback);
        System.out.println("message is..." + message);
    }

    private Emitter.Listener getGetOnNewMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("response method call...");
                    JSONObject data = (JSONObject) args[0];
                    System.out.println("response from data//...." + data);

                    try {
                        JSONObject res = new JSONObject(String.valueOf(data));
                        String direction = res.getString("direction");
                        String time = res.getString("time");
                        String speed = res.getString("speed");
                        String toastMessage = "Direction is : " + direction + "\nSpeed is : " + speed + "\nTime is : " + time ;
                        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error parsing response....response error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    };
    Emitter.Listener getOnSocketConnected= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Socket.IO connected", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private Emitter.Listener onSocketConnectError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Socket.IO connect error", Toast.LENGTH_SHORT).show();
                    // Try to reconnect to the server.
                    mSocket.connect();
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Disconnect from the server and unregister the event listener
        mSocket.disconnect();
        mSocket.off("parameter", getGetOnNewMessage);
    }
}
