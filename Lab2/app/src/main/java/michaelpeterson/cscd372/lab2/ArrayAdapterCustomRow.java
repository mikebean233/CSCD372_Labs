package michaelpeterson.cscd372.lab2;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Layout;
import android.view.View;
import android.view.*;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

public class ArrayAdapterCustomRow extends ArrayAdapter<Integer> {
    private int layoutResourceId;
    private static String[] imageNames;

    ArrayAdapterCustomRow(Context context, int layoutId, Integer[] items){
        super(context, layoutId, items);
        layoutResourceId = layoutId;
        if(imageNames == null)
            imageNames = context.getResources().getStringArray(R.array.imageNames);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Context context = getContext();

        View returnView = LayoutInflater.from(context).inflate(layoutResourceId, parent, false);

        ImageView thumbnail   = ((ImageView) returnView.findViewById(R.id.imageView2));
        TextView  description = ((TextView)  returnView.findViewById(R.id.rowTextView));

        Integer thisDataItem = (Integer)super.getItem(position);
        thumbnail.setImageResource(thisDataItem);

        description.setText(imageNames[position]);
        return returnView;
    }
}
