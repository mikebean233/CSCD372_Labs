package michaelpeterson.cscd372.lab2;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
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
import android.widget.Toast;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static Integer[] imageResourceIds;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    private ImageView mainImage;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if(toolbar == null)
            toolbar = (Toolbar) findViewById(R.id.toolbar);

        if(drawerLayout == null)
            drawerLayout = ((DrawerLayout) findViewById(R.id.drawer_layout));

        if(mainImage == null)
            mainImage = ((ImageView) findViewById (R.id.imageView));

        if(savedInstanceState != null) {
            mainImage.setImageResource(savedInstanceState.getInt("ImageResourceId"));
            mainImage.setTag(savedInstanceState.getInt("ImageResourceId"));
        }
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


        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true) ;
        toggle.syncState() ;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean aboutVisibleState;
        aboutVisibleState = !drawerLayout.isDrawerOpen(GravityCompat.START);
        menu.findItem(R.id.action_about).setVisible(aboutVisibleState);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            simpleToast("Michael Peterson Lab2");
        }
        if (toggle.onOptionsItemSelected(item)) return true ;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        if(savedInstanceState != null && mainImage.getTag() != null)
            savedInstanceState.putInt("ImageResourceId", ((Integer)mainImage.getTag()).intValue());

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mainImage.setImageResource(imageResourceIds[position]);
        mainImage.setTag(imageResourceIds[position]);
    }

    private void simpleToast(String text){
        Toast.makeText(this , text, Toast.LENGTH_SHORT).show();
    }
}
