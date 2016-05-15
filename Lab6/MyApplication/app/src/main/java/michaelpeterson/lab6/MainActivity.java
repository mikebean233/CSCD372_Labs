package michaelpeterson.lab6;

//import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
//import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity  implements MainFragment.OnFragmentInteractionListener{

    private enum Orientation
    {
        Landscape,
        Portrait
    }

    private String lastClickValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(savedInstanceState != null && savedInstanceState.containsKey("lastClickValue"))
            lastClickValue = savedInstanceState.getString("lastClickValue");

        if(getOrientation() == Orientation.Portrait && lastClickValue == null)
            getSupportFragmentManager().beginTransaction().add(R.id.PortHolder, new MainFragment()).commit();

        DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.DetailHolder);
        if(detailFragment != null)
           detailFragment.setText(lastClickValue);
    }

    private Orientation getOrientation(){
        return (getResources().getBoolean(R.bool.dual_pane)) ? Orientation.Landscape : Orientation.Portrait;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        if(outState != null && lastClickValue != null){
            outState.putString("lastClickValue", lastClickValue);
        }
    }


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
        if (id == R.id.action_about) {
            Toast.makeText(this, "Michael Peterson Lab6", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(String param) {
        lastClickValue = param;

        DetailFragment detailFragment = null;

        if(getOrientation() == Orientation.Landscape)
            detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.DetailHolder);
        else
            detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentByTag("DETAIL_FRAGMENT");

        Log.d("MainActivity.onFragmentInteraction", ((detailFragment == null) ? "no" : "") + " existing DetailFragment found");

        if(detailFragment == null)
            detailFragment = new DetailFragment();

        detailFragment.setText(param);

        if(getOrientation() == Orientation.Landscape)
            return;

        // Execute a replace transaction on the detailFragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.PortHolder, detailFragment, "DETAIL_FRAGMENT");
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
