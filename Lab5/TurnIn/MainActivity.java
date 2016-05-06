package michaelpeterson.lab5;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.BundleCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final long serialVersionUID = 0L;

    private BouncyWeightView _bouncyWeightView;
    private Timer _animationTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        _bouncyWeightView = ((BouncyWeightView) findViewById(R.id.bouncyWeightView));

        if(savedInstanceState != null && savedInstanceState.containsKey("bouncyView"))
            _bouncyWeightView.copyState((BouncyWeightView)savedInstanceState.getSerializable("bouncyView"));
        else
            _bouncyWeightView.setStateRun();

        final TextView textViewState = ((TextView) findViewById(R.id.textViewPlayState));

        Button thisButton = ((Button) findViewById(R.id.button));
        thisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bouncyWeightView.toggleState();
                textViewState.setText(_bouncyWeightView.getPlayState().toString());
            }
        });
        textViewState.setText(_bouncyWeightView.getPlayState().toString());


        _animationTimer = new Timer();

        _animationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(_bouncyWeightView);
            }
        }, 0, 25);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle){
        if(bundle != null && _bouncyWeightView != null)
            bundle.putSerializable("bouncyView", _bouncyWeightView);
        _bouncyWeightView.setStatePause();
        _animationTimer.cancel();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(_bouncyWeightView != null)
            _bouncyWeightView.setStatePause();
        _animationTimer.cancel();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, "Michael Peterson Lab5", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
