package michaelpeterson.finalproject;

import android.graphics.Paint;

public class PaintBuilder {
    public static Paint buildPaint(Paint.Style style, int color, float strokeWidth){
        if(style == null)
            throw new NullPointerException();
        Paint thisPaint = new Paint();
        thisPaint.setStrokeWidth(strokeWidth);
        thisPaint.setColor(color);
        thisPaint.setStyle(style);
        return thisPaint;
    }

}
