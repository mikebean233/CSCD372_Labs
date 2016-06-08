package michaelpeterson.finalproject;

import java.io.Serializable;

public class Point implements Serializable{
    private float _x;
    private float _y;

    public Point(float x, float y){
        _x = x;
        _y = y;
    }
    public float getX(){return _x;}
    public float getY(){return _y;}
    public Point setX(float x){_x = x; return this;}
    public Point setY(float y){_y = y; return this;}
    public Point translate(Point translVec){
        _x += translVec.getX();
        _y += translVec.getY();
        return this;
    }
    public Point clone(){return new Point(_x, _y);}

    @Override
    public String toString(){return "(" + _x + "," + _y + ")";}

}
