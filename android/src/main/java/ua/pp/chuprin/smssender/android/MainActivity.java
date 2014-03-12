package ua.pp.chuprin.smssender.android;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {
    static {
        System.loadLibrary("alljoyn_java");
    }

    private final Handler uiHandler = new UiActionsHandler();
    private Handler networkStatusHandler;
    private TextView pin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        pin = (TextView) findViewById(R.id.pin);

        HandlerThread networkThread = new HandlerThread("NetworkStatusHandler");
        networkThread.start();
        networkStatusHandler = new NetworkStatusHandler(networkThread.getLooper(), this.getApplicationContext(), uiHandler);
        networkStatusHandler.sendEmptyMessage(NetworkStatusHandler.CONNECT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.quit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkStatusHandler.sendEmptyMessage(NetworkStatusHandler.DISCONNECT);
    }

    public class UiActionsHandler extends Handler {
        public static final int UPDATE_PIN = 1;

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case UPDATE_PIN:
                    pin.setText((String) message.obj);
                    break;
            }
        }
    }
}
