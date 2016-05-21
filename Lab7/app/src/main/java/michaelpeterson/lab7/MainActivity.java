package michaelpeterson.lab7;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
   // private static final long serialVersionUID = 0L;

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
            _bouncyWeightView.copyState((BouncyWeightView) savedInstanceState.getSerializable("bouncyView"));
        else
            _bouncyWeightView.setStateRun();

        Button toggleButton = ((Button) findViewById(R.id.button));
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bouncyWeightView.toggleState();
                updateStateText();
            }
        });

        Button resetButton = ((Button) findViewById(R.id.buttonReset));
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bouncyWeightView.reset();
            }
        });
        updateStateText();
        startTimer();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        _bouncyWeightView.restoreState();
        updateStateText();
    }

    private void updateStateText(){
        if(_bouncyWeightView == null)
            return;

        TextView textViewState = ((TextView) findViewById(R.id.textViewPlayState));
        if(textViewState != null)
            textViewState.setText(_bouncyWeightView.getPlayState().toString());
    }

    private void startTimer(){
        if(_animationTimer == null)
            _animationTimer = new Timer();
        else
            return;

        _animationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(_bouncyWeightView);
            }
        }, 0, 25);
    }

    @Override
    public void onStop(){
        super.onStop();
        _bouncyWeightView.storeState().setStatePause();
        updateStateText();
    }

    @Override
    public void onResume(){
        super.onResume();
        _bouncyWeightView.restoreState();
        updateStateText();
        updateViewSettings();
    }

    public void updateViewSettings(){
        if(_bouncyWeightView == null)
            return;
        String  massShapeString  = (String)  SharedPreferencesHelper.getPreferenceValue(this, R.string.preferenceKey_massShape);
        Integer springCoilCount  = (Integer) SharedPreferencesHelper.getPreferenceValue(this, R.string.preferenceKey_springCoilCount);
        Integer initDisp         = (Integer) SharedPreferencesHelper.getPreferenceValue(this, R.string.preferenceKey_springInitDisp);
        Float   springStiffness  = (Float)   SharedPreferencesHelper.getPreferenceValue(this, R.string.preferenceKey_springStiffness);
        Drawable weightDrawable  =   SharedPreferencesHelper.getDrawableFromPictureType(this, massShapeString);
        _bouncyWeightView.setCoilCount(springCoilCount)
                .setInitialDisplacement(initDisp)
                .setSpringStiffness(springStiffness)
                .setWeightDrawable(weightDrawable);
    }

    @Override
    public void onPause(){
        super.onPause();
        _bouncyWeightView.storeState().setStatePause();
        updateStateText();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle){
        if(bundle != null && _bouncyWeightView != null)
            bundle.putSerializable("bouncyView", _bouncyWeightView);
        _bouncyWeightView.storeState().setStatePause();
        updateStateText();
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
        if(_bouncyWeightView == null)
            return;
        _bouncyWeightView.storeState().setStatePause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Toast.makeText(this, "Michael Peterson lab7", Toast.LENGTH_LONG).show();
            return true;
        }

        if(id == R.id.action_settings){
            Intent thisIntent = new Intent(this, SettingsActivity.class);
            startActivityForResult(thisIntent, 0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
