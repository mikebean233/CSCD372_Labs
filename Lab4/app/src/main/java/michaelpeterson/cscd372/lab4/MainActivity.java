package michaelpeterson.cscd372.lab4;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public int[] _values;
    public SevenSegment[] _digits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final MainActivity thisActivity = this;
        final SevenSegment sevenSegment = ((SevenSegment) findViewById(R.id.sevenSegment0));
        if(_values == null)
            initialize();

        if(savedInstanceState != null && savedInstanceState.containsKey("values")){
            _values = savedInstanceState.getIntArray("values");
            updateDigits();
        }
        Button incrButton  = ((Button) findViewById(R.id.buttonIncr));
        incrButton.setOnClickListener(this);
    }

    private void initialize(){
        _values = new int[]{10, 0, 1, 2, 3};
        _digits = new SevenSegment[]{
                (SevenSegment)findViewById(R.id.sevenSegment0),
                (SevenSegment)findViewById(R.id.sevenSegment1),
                (SevenSegment)findViewById(R.id.sevenSegment2),
                (SevenSegment)findViewById(R.id.sevenSegment3),
                (SevenSegment)findViewById(R.id.sevenSegment4)
        };
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        bundle.putIntArray("values", _values);
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
            Toast.makeText(this, "Michael Peterson Lab4", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        for(int i = 0; i < 5; ++i)
            _values[i] = (_values[i] + 1) % 11;

        updateDigits();
    }

    private void updateDigits(){
        for(int i = 0; i < 5; ++i){
            _digits[i].set(_values[i]);
        }
    }
}
