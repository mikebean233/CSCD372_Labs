package michaelpeterson.cscd372.lab0;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.lang.Override;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_about:
                (Toast.makeText(getApplicationContext(), "You Clicked About!", Toast.LENGTH_LONG)).show();
                break;
            default:
                Log.w("MainActivity", "MainActivity.onOptionsItemSelected() : Somehow a non existent menu item was selected  { menuItemId: " + item.getItemId() + ", menuItemTitle: " + item.getTitle() + "}");
        }

        return true;
    }
}
