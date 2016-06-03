package michaelpeterson.finalproject;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

public class Circle extends PositionedPath{
    private float _radius;
    public Circle(Point position, Paint paint, float radius) {
        super(new Path(), position, paint);

        _originalPath.addCircle(0, 0,radius,Direction.CW);
        _radius = radius;
    }

    public float getRadius(){return _radius;}

    public Circle setRadius(float newRadius){
        _radius = newRadius;

        _originalPath.rewind();
        _originalPath.addCircle(0, 0,_radius,Direction.CW);
        return this;
    }
}
