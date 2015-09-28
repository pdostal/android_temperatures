package pdostal.temperatures;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://mqtt2socketio.pdostal.cz");
        } catch (URISyntaxException e) {}
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSocket.on("mqtt", handleIncomingMessages);
        mSocket.connect();
    }

    private Emitter.Listener handleIncomingMessages = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override

                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String timestamp,topic,message;
                    try {
                        timestamp = data.getString("timestamp").toString();
                        topic = data.getString("topic").toString();
                        message = data.getString("message").toString();

                        if(topic.matches("^/weather/temperature$")){
                            TextView t = (TextView)findViewById(R.id.SHOutsideValue);
                            t.setText(message + " °C");
                        }
                        if(topic.matches("^/housing3/vpravo_dole$")){
                            TextView t = (TextView)findViewById(R.id.SHHousing3Value);
                            t.setText(message + " °C");
                        }
                        if(topic.matches("^/housing4/dvere_nahore$")){
                            TextView t = (TextView)findViewById(R.id.SHHousing4Value);
                            t.setText(message + " °C");
                        }
                        if(topic.matches("^/oskar/jezirko/vzduch/temperature$")){
                            TextView t = (TextView)findViewById(R.id.OskarOutsideValue);
                            t.setText(message + " °C");
                        }
                        if(topic.matches("^/oskar/jezirko/dno/temperature$")){
                            TextView t = (TextView)findViewById(R.id.OskarLagoonValue);
                            t.setText(message + " °C");
                        }
                        if(topic.matches("^/pdostalcz/tp-82n/message$")){
                            message = message.replaceAll("INT:([0-9]+.[0-9]+)C.*", "$1");

                            TextView t = (TextView)findViewById(R.id.PdostalRoomValue);
                            t.setText(message + " °C");
                        }

                        Log.d("timestamp", timestamp);
                        Log.d("message", message);
                        Log.d("topic", topic);
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        //mSocket.off("new message", onNewMessage);
    }
}
