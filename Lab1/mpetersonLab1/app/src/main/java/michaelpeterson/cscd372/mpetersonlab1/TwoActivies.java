package michaelpeterson.cscd372.mpetersonlab1;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import android.widget.EditText;

public class TwoActivies extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_activies);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Context thisContext = this;

        ((Button) findViewById(R.id.buttonTakeSurvey)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editTextFirstName = ((EditText) findViewById(R.id.editTextFirstName));
                String firstName = String.valueOf(editTextFirstName.getText());
                // we don't have a name
                if(firstName == null || firstName.isEmpty())
                    Toast.makeText(TwoActivies.this, "Please enter a name", Toast.LENGTH_SHORT).show();

                // we do have a name
                else{
                    Intent surveyIntent = new Intent(thisContext, SurveyActivity.class);
                    //surveyIntent.putExtra("firstName", firstName);
                    surveyIntent.putExtra("firstName", firstName);
                    startActivityForResult(surveyIntent, 0);
                }
            }
        });

        ((Button) findViewById(R.id.buttonGotoWebsite)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Uri thisUri = Uri.parse("https://sites.google.com/site/pschimpf99/");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, thisUri);
                startActivity(browserIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_two_activies, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
