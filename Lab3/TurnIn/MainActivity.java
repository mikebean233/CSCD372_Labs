package michaelpeterson.cscd372.myapplication;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements ExpandableListView.OnChildClickListener{
    private ArrayList<Manufacturer> _manufacturers;

    public MainActivity(){_manufacturers = new ArrayList<Manufacturer>();}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(savedInstanceState != null && savedInstanceState.containsKey("Manufacturers")){
            _manufacturers = (ArrayList<Manufacturer>) savedInstanceState.getSerializable("Manufacturers");
        }
        else
            parseFile("manufacturers.txt");

        MikesListAdapter listAdapter = new MikesListAdapter(this, _manufacturers);
        ExpandableListView thisListView = ((ExpandableListView) findViewById(R.id.expandableListView));

        thisListView.setAdapter(listAdapter);
        thisListView.setOnChildClickListener(this);

    }



    private Boolean parseFile(String filename){
        AssetManager assetManager = getResources().getAssets();
        try {
            boolean result = false;

            InputStream inputStream = assetManager.open(filename);
            Scanner inputScanner = new Scanner(inputStream);

            // read in all of the lines that have a newline/carriage return at the end
            while(inputScanner.hasNextLine()) {
                String thisLine = inputScanner.nextLine();
                result = (parseSingleLine(thisLine) || result);
            }

            // Check to see if there is any mode data in the file (just in case the last line is
            // missing a newline/carriage return
            if(inputScanner.hasNext()){
                String thisLine = inputScanner.next();
                result = (parseSingleLine(thisLine) || result);
            }

            return result;
        }
        catch(Exception ex){
            return false;
        }

    }

    private boolean parseSingleLine(String thisLine){
        String thisToken = null;
        if(thisLine != null && !thisLine.isEmpty()){
            String[] components = thisLine.split(",");
            if(components != null && components.length >= 1) {
                // Create this manufacturer
                thisToken = components[0];

                if(thisToken == null || thisToken.isEmpty())
                    return false;

                Manufacturer thisManufacturer = new Manufacturer(thisToken.trim());
                _manufacturers.add(thisManufacturer);

                // Add all of the models in this line to the manufacturer
                for (int modelIndex = 1; modelIndex < components.length; ++modelIndex) {
                    thisToken = components[modelIndex];

                    if(thisToken == null || thisToken.isEmpty())
                        continue;

                    thisManufacturer.addModel(thisToken.trim());

                }// end for : loop through line contents)
                return true;
            }// end if(the line has no tokens)
            else
                return false;
        }// end if(the line is not empty)
        else
            return false;
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
            simpleToast("Michael Peterson lab3");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Manufacturer thisManufacturer = _manufacturers.get(groupPosition);
        String thisModel = thisManufacturer.getModelByPosition(childPosition);
        simpleToast(thisManufacturer + thisModel);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("Manufacturers", _manufacturers);
    }


    private void simpleToast(String text){
        Toast.makeText(this , text, Toast.LENGTH_SHORT).show();
    }
}
