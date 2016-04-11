package michaelpeterson.cscd372.lab2;

import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static Integer[] imageResourceIds;
    private final String defaultTitle = "Lab2";
    private final String drawerOutTitle = "Select a Page";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //defaultTitle = getTitle().toString();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // If it hasn't been done already, get our list of image resource ids
        if(imageResourceIds == null) {
            TypedArray imageArray = getResources().obtainTypedArray(R.array.images);

            imageResourceIds = new Integer[imageArray.length()];
            for (int i = 0; i < imageResourceIds.length; ++i) {
                imageResourceIds[i] = imageArray.getResourceId(i, 0);
            }

            imageArray.recycle();
        }

        ArrayAdapter<Integer> arrayAdapter = new ArrayAdapterCustomRow(this, R.layout.nav_list_row, imageResourceIds);

        ListView listView = ((ListView) findViewById(R.id.left_drawer));
        listView.setAdapter(arrayAdapter);

        // wire up the item click listener
        listView.setOnItemClickListener(this);

        DrawerLayout drawerLayout = ((DrawerLayout) findViewById(R.id.drawer_layout));

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.app_name,
                R.string.open_drawer_description
        ){
            @Override
            public void onDrawerClosed(View view){
                super.onDrawerClosed(view);
                setTitle(R.string.app_name);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                setTitle(R.string.open_drawer_description);
                invalidateOptionsMenu();
            }
        };


        toggle.setHomeAsUpIndicator(R.drawable.drawer);
        toggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(toggle);

       // getSupportActionBar().setDisplayHomeAsUpEnabled(true) ;
        toggle.syncState() ;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ImageView mainImage = ((ImageView) findViewById(R.id.imageView));
        mainImage.setImageResource(imageResourceIds[position]);
    }

}
