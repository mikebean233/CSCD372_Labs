package michaelpeterson.lab8;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnDragListener, View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Integer[] gridDrawables = new Integer[9];
        Arrays.fill(gridDrawables, R.drawable.blank);

        if(savedInstanceState != null)
            gridDrawables = (Integer[]) savedInstanceState.get("gridState");
        
        ChainableImageView.buildImageView((ImageView) findViewById(R.id.o)).setOnTouchListener(this).setTag(R.id.ImageIdKey, R.drawable.o);
        ChainableImageView.buildImageView((ImageView) findViewById(R.id.x)).setOnTouchListener(this).setTag(R.id.ImageIdKey, R.drawable.x);
        ChainableImageView.buildImageView((ImageView) findViewById(R.id.imageView00)).setOnDragListener(this).setTagAndDrawable(R.id.ImageIdKey, gridDrawables[0]);
        ChainableImageView.buildImageView((ImageView) findViewById(R.id.imageView01)).setOnDragListener(this).setTagAndDrawable(R.id.ImageIdKey, gridDrawables[1]);
        ChainableImageView.buildImageView((ImageView) findViewById(R.id.imageView02)).setOnDragListener(this).setTagAndDrawable(R.id.ImageIdKey, gridDrawables[2]);
        ChainableImageView.buildImageView((ImageView) findViewById(R.id.imageView10)).setOnDragListener(this).setTagAndDrawable(R.id.ImageIdKey, gridDrawables[3]);
        ChainableImageView.buildImageView((ImageView) findViewById(R.id.imageView11)).setOnDragListener(this).setTagAndDrawable(R.id.ImageIdKey, gridDrawables[4]);
        ChainableImageView.buildImageView((ImageView) findViewById(R.id.imageView12)).setOnDragListener(this).setTagAndDrawable(R.id.ImageIdKey, gridDrawables[5]);
        ChainableImageView.buildImageView((ImageView) findViewById(R.id.imageView20)).setOnDragListener(this).setTagAndDrawable(R.id.ImageIdKey, gridDrawables[6]);
        ChainableImageView.buildImageView((ImageView) findViewById(R.id.imageView21)).setOnDragListener(this).setTagAndDrawable(R.id.ImageIdKey, gridDrawables[7]);
        ChainableImageView.buildImageView((ImageView) findViewById(R.id.imageView22)).setOnDragListener(this).setTagAndDrawable(R.id.ImageIdKey, gridDrawables[8]);

        ((Button) findViewById(R.id.buttonReset)).setOnClickListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
        Integer[] gridDrawables = new Integer[]{
            (Integer) findViewById(R.id.imageView00).getTag(R.id.ImageIdKey),
            (Integer) findViewById(R.id.imageView01).getTag(R.id.ImageIdKey),
            (Integer) findViewById(R.id.imageView02).getTag(R.id.ImageIdKey),
            (Integer) findViewById(R.id.imageView10).getTag(R.id.ImageIdKey),
            (Integer) findViewById(R.id.imageView11).getTag(R.id.ImageIdKey),
            (Integer) findViewById(R.id.imageView12).getTag(R.id.ImageIdKey),
            (Integer) findViewById(R.id.imageView20).getTag(R.id.ImageIdKey),
            (Integer) findViewById(R.id.imageView21).getTag(R.id.ImageIdKey),
            (Integer) findViewById(R.id.imageView22).getTag(R.id.ImageIdKey)
        };
        outState.putSerializable("gridState", gridDrawables);
    }

    private void resetBoard(){
        ChainableImageView.buildImageView((ImageView) findViewById(R.id.imageView00)).setTagAndDrawable(R.id.ImageIdKey, R.drawable.blank);
        ChainableImageView.buildImageView((ImageView) findViewById(R.id.imageView01)).setTagAndDrawable(R.id.ImageIdKey, R.drawable.blank);
        ChainableImageView.buildImageView((ImageView) findViewById(R.id.imageView02)).setTagAndDrawable(R.id.ImageIdKey, R.drawable.blank);
        ChainableImageView.buildImageView((ImageView) findViewById(R.id.imageView10)).setTagAndDrawable(R.id.ImageIdKey, R.drawable.blank);
        ChainableImageView.buildImageView((ImageView) findViewById(R.id.imageView11)).setTagAndDrawable(R.id.ImageIdKey, R.drawable.blank);
        ChainableImageView.buildImageView((ImageView) findViewById(R.id.imageView12)).setTagAndDrawable(R.id.ImageIdKey, R.drawable.blank);
        ChainableImageView.buildImageView((ImageView) findViewById(R.id.imageView20)).setTagAndDrawable(R.id.ImageIdKey, R.drawable.blank);
        ChainableImageView.buildImageView((ImageView) findViewById(R.id.imageView21)).setTagAndDrawable(R.id.ImageIdKey, R.drawable.blank);
        ChainableImageView.buildImageView((ImageView) findViewById(R.id.imageView22)).setTagAndDrawable(R.id.ImageIdKey, R.drawable.blank);
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
            Toast.makeText(this,"Michael Peterson Lab8", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            View.DragShadowBuilder dragShadowBuilder = new View.DragShadowBuilder(v);
            v.startDrag(null, dragShadowBuilder, v, 0);
        }
        else
            return true;

        return false;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        if(event.getAction() == DragEvent.ACTION_DROP){
            int newResourceId = (Integer)((View)event.getLocalState()).getTag(R.id.ImageIdKey);
            int oldResourceId = (Integer)v.getTag(R.id.ImageIdKey);
            if(oldResourceId == R.drawable.blank){
                ((ImageView) v).setImageResource(newResourceId);
                v.setTag(R.id.ImageIdKey, newResourceId);
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        resetBoard();
    }
}
