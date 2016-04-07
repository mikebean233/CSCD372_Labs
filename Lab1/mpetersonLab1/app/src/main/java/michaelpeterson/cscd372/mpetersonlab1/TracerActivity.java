package michaelpeterson.cscd372.mpetersonlab1;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Iterator;
import java.util.Set;

public class TracerActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dealWithEventBundle("onCreate", savedInstanceState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        dealWithEventBundle("onRestart", null);
    }
    @Override
    protected void onStart() {
        super.onStart();
        dealWithEventBundle("onStart", null);
    }
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        dealWithEventBundle("onSaveInstanceState", savedInstanceState);
    }
    @Override
    protected void onStop() {
        super.onStop();
        dealWithEventBundle("onStop", null);
    }

    @Override
    protected void  onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bundle savedInstanceState = null;

        if(data != null){
            savedInstanceState = data.getExtras();
        }

        dealWithEventBundle("onActivityResult", savedInstanceState);
    }

    private void dealWithEventBundle(String methodName, Bundle savedInstanceState){
        if(savedInstanceState == null){
            notify(methodName + " NO state");
        }
        else{
            notify(methodName + " WITH state");

            Set<String> keys = savedInstanceState.keySet() ;
            for(String thisKey: keys)
                notify("key: " + thisKey);
        }
    }


    private void notify(String msg) {
        String strClass = this.getClass().getName();
        String[] strings = strClass.split("\\.");
        Notification.Builder notibuild = new Notification.Builder(this);
        notibuild.setContentTitle(msg);
        notibuild.setAutoCancel(true).setSmallIcon(R.mipmap.ic_launcher);
        notibuild.setContentText(strings[strings.length-1]);
        Notification noti = notibuild.build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), noti);
    }
}
