package michaelpeterson.cscd372.mpetersonlab1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SurveyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        TextView helloText = ((TextView) findViewById(R.id.textViewHello) );
        Intent thisIntent = getIntent();
        Context thisContext = this;
        String firstName = thisIntent.getStringExtra("firstName");
        helloText.setText("Hello " + firstName);


        ((Button) findViewById(R.id.buttonSubmit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ageString = ((TextView) findViewById(R.id.editTextAge)).getText().toString();
                if(ageString.isEmpty())
                    Toast.makeText(SurveyActivity.this, "Please enter an age", Toast.LENGTH_SHORT).show();
                else{
                    int age = Integer.parseInt(ageString);
                    Intent resultIntent = getIntent();
                    resultIntent.putExtra("age", age);

                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }
        });


    }
}
