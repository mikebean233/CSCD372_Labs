package michaelpeterson.lab7;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.preference.PreferenceManager;

public class SharedPreferencesHelper{

    public static Object getPreferenceValue(Context context, String key){
        return getPreferenceValue(context, getPreferenceId(context, key));
    }

    public static Object getPreferenceValue(Context context, int id){
        if(context == null)
            throw new NullPointerException();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Resources resources = context.getResources();
        switch(id){
            case R.string.preferenceKey_springCoilCount:
                return sharedPref.getInt(resources.getString(id), Integer.parseInt(resources.getString(R.string.preferenceKey_springCoilCount_default)));
            case R.string.preferenceKey_massShape:
                return sharedPref.getString(resources.getString(id), resources.getString(R.string.preferenceKey_massShape_default));
            case R.string.preferenceKey_springInitDisp:
                return sharedPref.getInt(resources.getString(id), Integer.parseInt(resources.getString(R.string.preferenceKey_springInitDisp_default)));
            case R.string.preferenceKey_springStiffness:
                String defValue = resources.getString(R.string.preferenceKey_springStiffness_default, 1, 1);
                return Float.parseFloat(sharedPref.getString(resources.getString(id), defValue));
            default:
                throw new IllegalArgumentException();
        }
    }

    static Drawable getDrawableFromPictureType(Context context, String key){
        if(context == null || key == null)
            throw new NullPointerException();
        Resources resources = context.getResources();
        ShapeDrawable shapeDrawable;

        if (key.equals(resources.getString(R.string.mass_shape_type_Circle))) {
            shapeDrawable = new ShapeDrawable(new OvalShape());
        } else if(key.equals(resources.getString(R.string.mass_shape_type_Rectangle))) {
            shapeDrawable = new ShapeDrawable(new RectShape());
        } else if(key.equals(resources.getString(R.string.mass_shape_type_RoundedRectangle))) {
            shapeDrawable = new ShapeDrawable(new RoundRectShape(new float[]{35,35,35,35,35,35,35,35}, null, null));
        } else if(key.equals(resources.getString(R.string.mass_shape_type_Picture))) {
            return new BitmapDrawable(resources, BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher));
        } else {
            throw new IllegalArgumentException();
        }

        shapeDrawable.getPaint().setColor(Color.BLUE);
        shapeDrawable.getPaint().setStyle(Paint.Style.STROKE);
        shapeDrawable.getPaint().setStrokeWidth(2);
        return shapeDrawable;
    }

    static int getPreferenceId(Context context, String key) {
        if(context == null || key == null)
            throw new NullPointerException();

        Resources resources = context.getResources();

        if (key == resources.getString(R.string.preferenceKey_massShape)) {
            return R.string.preferenceKey_massShape;
        } else if (key == resources.getString(R.string.preferenceKey_springCoilCount)) {
            return R.string.preferenceKey_springCoilCount;
        } else if (key == resources.getString(R.string.preferenceKey_springInitDisp)) {
            return R.string.preferenceKey_springInitDisp;
        } else if (key == resources.getString(R.string.preferenceKey_springStiffness)) {
            return R.string.preferenceKey_springStiffness;
        } else
            throw new IllegalArgumentException();
    }
}
