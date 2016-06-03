package michaelpeterson.finalproject;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

public class PositionedPath extends Path implements Renderable<PositionedPath>{
    protected Point _position;
    protected Path _originalPath;
    //private float _scale;

    protected Paint _paint;

    public PositionedPath(Path path, Point position, Paint paint){
        super(path);
        if(path == null || position == null || paint == null)
            throw new NullPointerException();
        _originalPath = path;

        _position = position;
        //_scale = scale;
        _paint = paint;
    }

    public PositionedPath render(Canvas canvas, float scale){
        this.set(_originalPath);

        Matrix thisMatrix = new Matrix();
        thisMatrix.setScale(scale, scale);
        this.transform(thisMatrix);
        thisMatrix.setTranslate(_position.getX() * scale, _position.getY() * scale);
        this.transform(thisMatrix);

        canvas.drawPath(this, _paint);
        return this;
    }

    public PositionedPath setPaint(Paint newPaint){
        if(newPaint == null)
            throw new NullPointerException();

        _paint = newPaint;
        return this;
    }

    public Paint getPaint(){return _paint;}

    public Point getPosition(){return _position;}
    public PositionedPath setPosition(Point position){
        _position.setX(position.getX());
        _position.setY(position.getY());
        return this;
    }
}
