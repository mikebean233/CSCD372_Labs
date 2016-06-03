package michaelpeterson.finalproject;

import android.graphics.Paint;
import android.graphics.Path;

public class Line extends PositionedPath{
    private Point _endPos;
    private Point _startPos;


    public Line(Point startPos, Point endPos, Paint paint) {
        super(new Path(), new Point(0,0), paint);
        _endPos   = endPos;
        _startPos = startPos;


        _originalPath.moveTo(startPos.getX(), startPos.getY());
        _originalPath.lineTo(endPos.getX(), endPos.getY());
        _originalPath.close();
    }

    public Point getEndPos(){  return _endPos;}
    public Point getStartPos(){return _startPos;}

    public Line setEndPos(Point newEndPos){
        if(newEndPos == null)
            throw new NullPointerException();

        _endPos = newEndPos;

        this.rewind();
        _originalPath.moveTo(_startPos.getX(), _startPos.getY());
        _originalPath.lineTo(_endPos.getX(), _endPos.getY());

        return this;
    }

    public Line setStartPos(Point newStartPos){
        if(newStartPos == null)
            throw new NullPointerException();

        _endPos = newStartPos;

        this.rewind();
        _originalPath.moveTo(_startPos.getX(), _startPos.getY());
        _originalPath.lineTo(_endPos.getX(), _endPos.getY());

        return this;
    }
}
