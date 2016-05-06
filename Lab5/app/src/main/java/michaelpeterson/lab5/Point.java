package michaelpeterson.lab5;

import java.io.Serializable;

public class Point implements Cloneable, Serializable {
    private float _x;
    private float _y;
    Point(float x, float y){
        _x = x;
        _y = y;
    }

    public float getX(){return _x;}
    public float getY(){return _y;}

    public void setX(float x){_x = x;}
    public void setY(float y){_y = y;}
    public Point scale(float scaleFactor){
        _x *= scaleFactor;
        _y *= scaleFactor;
        return this;
    }
    @Override
    public String toString(){return "(" + _x + ", " + _y + ")";}

    @Override
    public Object clone(){
        return new Point(_x, _y);
    }

}