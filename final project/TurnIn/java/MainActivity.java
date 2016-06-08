package michaelpeterson.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private GameFieldView _gameView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        _gameView = ((GameFieldView) findViewById(R.id.view));
        _gameView.start(this);

        if(savedInstanceState != null && savedInstanceState.containsKey("gameView")){
            GameFieldView oldState = (GameFieldView) savedInstanceState.getSerializable("gameView");
            _gameView.restoreState(oldState);
            oldState = null;
        }


        loadPreferences();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadPreferences();
    }

    private void loadPreferences(){
        _gameView.pause();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int initialScudsSpeed = sharedPreferences.getInt("initial_scud_speed", 15);
        int scudsPerLevel = sharedPreferences.getInt("scuds_per_level", 15);
        _gameView.updatePreferences(initialScudsSpeed, scudsPerLevel);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        //_gameView.pause();
    }

    @Override
    public void onPause() {
        super.onPause();
        //_gameView.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        _gameView.resume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //_gameView.resume();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        if(bundle!=null && _gameView != null)
             bundle.putSerializable("gameView", _gameView);
        //_gameView.pause();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent thisIntent = new Intent(this, SettingsActivity.class);
            _gameView.pause();
            startActivityForResult(thisIntent, 0);
            return true;
        }

        if(id == R.id.action_about){
            Toast.makeText(this, "Michael Peterson final project", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
