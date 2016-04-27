package michaelpeterson.cscd372.lab4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;
//import android.opengl.Matrix;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class SevenSegment extends View {
    private int _currentValue;
    private ArrayList<Point> _segmentVertices;
    private float[][] _segmentTransfMatrices;
    private byte[] _valueStateMappings;
    private Point _lastKnownSize;
    private float _originalWidth;
    private float _originalHeight;
    private float _paddedOriginalWidth;
    private float _paddedOriginalHeight;
    private float _aspectRatio;


    public SevenSegment(Context context){
        super(context);
        initialize();
    }

    public SevenSegment(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        initialize();
    }

    public SevenSegment(Context context, AttributeSet attributeSet, int defStyle){
        super(context, attributeSet, defStyle);
        initialize();
    }

    private void initialize(){
        _currentValue = 10;

        // setup dimension data
        float padding = 2;
        _originalWidth  = 100;
        _originalHeight = 180;
        _paddedOriginalWidth  = _originalWidth + padding;
        _paddedOriginalHeight = _originalHeight + padding;
        _aspectRatio  = _paddedOriginalWidth / _paddedOriginalHeight;

        // setup the segment vertices
        _segmentVertices = new ArrayList<>();
        _segmentVertices.add(new Point(-40, 0));
        _segmentVertices.add(new Point(-30, 10));
        _segmentVertices.add(new Point( 30, 10));
        _segmentVertices.add(new Point( 40, 0));
        _segmentVertices.add(new Point( 30,-10));
        _segmentVertices.add(new Point(-30,-10));

        // setup the transformation matrices
        _segmentTransfMatrices = new float[][] {
                {1,  0,  0,   // Segment 0
                 0,  1,  0,
                 0,  0,  1},

                {0, -1, 40,   // Segment 1
                 1,  0,-40,
                 0,  0,  1},

                {1,  0,  0,   // Segment 2
                 0,  1, -80,
                 0,  0,  1},

                {0, -1, -40,  // Segment 3
                 1,  0, -40,
                 0,  0,  1},

                {0, -1, -40,  // Segment 4
                 1,  0,  40,
                 0,  0,  1},

                {1,  0,  0,   // Segment 5
                 0,  1,  80,
                 0,  0,  1},

                {0, -1,  40,  // Segment 6
                 1,  0,  40,
                 0,  0,  1}
        };

        // ---- transl and rot ----
        //  | 0, -1, translX|
        //  | 1,  0, translY|
        //  | 0,  0,       1|

        // ---- transl only ----
        //  | 1, 0, translX|
        //  | 0, 1, translY|
        //  | 0, 0,       1|

        // setup the value state mappings
        _valueStateMappings = new byte[]{
            // 6543210: <--- bit to segment mapping
            0b01111110,  // value: 0
            0b01000010,  // value: 1
            0b00110111,  // value: 2
            0b01100111,  // value: 3
            0b01001011,  // value: 4
            0b01101101,  // value: 5
            0b01111101,  // value: 6
            0b01000110,  // value: 7
            0b01111111,  // value: 8
            0b01001111,  // value: 9
            0b00000000   // value: 10
        };
    }

    public boolean set(int newValue){
        if(newValue < 0 || newValue > 10)
            throw new IllegalArgumentException("Illegal value of " + newValue + " passed to SevenSegment.set()  ( only values from 0 to 9 are valid)");


        if(newValue != _currentValue){
            _currentValue = newValue;
            this.invalidate();
            return true;
        }
        return false;
    }

    public int get(){return _currentValue;}

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight){
        if(_lastKnownSize == null)
            _lastKnownSize = new Point(width, height);
        else{
            _lastKnownSize.setX(width);
            _lastKnownSize.setY(height);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        float desiredWidth = (float) MeasureSpec.getSize(widthMeasureSpec);
        float desiredHeight  = (float) MeasureSpec.getSize(heightMeasureSpec);
        float finalHeight = 0;
        float finalWidth  = 0;
        float desiredAspectRatio = (float) desiredWidth / (float) desiredHeight;

        if(desiredAspectRatio > _aspectRatio || desiredWidth == 0){
            // we are keeping the desired height
            finalHeight = desiredHeight;
            finalWidth  = desiredHeight * _aspectRatio;
        }
        else if(desiredAspectRatio <= _aspectRatio || desiredHeight == 0) {
            // we are keeping the desired width
            finalWidth  = desiredWidth;
            finalHeight = desiredWidth / _aspectRatio;
        }
        setMeasuredDimension((int)finalWidth, (int)finalHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = (_lastKnownSize == null) ? canvas.getWidth()  : _lastKnownSize.getX();
        float height =(_lastKnownSize == null) ? canvas.getHeight() : _lastKnownSize.getY();

        float widthScaleFactor  = width  / _paddedOriginalWidth;
        float heightScaleFactor = height / _paddedOriginalHeight;

        // Create our base (template) path
        boolean haveFirstPoint = false;
        Path basePath = new Path();
        basePath.moveTo(_segmentVertices.get(0).getX(), _segmentVertices.get(0).getY());
        for(Point thisPoint : _segmentVertices)
            if(haveFirstPoint)
                basePath.lineTo(thisPoint.getX(), thisPoint.getY());
            else
                haveFirstPoint = true;
        basePath.close();

        Paint thisPaint = new Paint();
        thisPaint.setStrokeWidth(2);
        canvas.scale(widthScaleFactor, heightScaleFactor);
        canvas.translate(_paddedOriginalWidth / 2, _paddedOriginalHeight / 2);

        for(int i = 0; i < 7; ++i){
            // setup this segment
            Path thisPath = new Path();
            thisPath.addPath(basePath);
            Matrix thisTransMatrix = new Matrix();
            thisTransMatrix.setValues(_segmentTransfMatrices[i]);
            thisPath.transform(thisTransMatrix);

            // draw the fill
            thisPaint.setStyle(Paint.Style.FILL);
            thisPaint.setColor(((_valueStateMappings[_currentValue] & (1 << i)) != 0) ? Color.RED : Color.rgb(64, 0, 0));
            canvas.drawPath(thisPath, thisPaint);

            // draw outline
            thisPaint.setColor(Color.BLACK);
            thisPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(thisPath, thisPaint);

        }

    }



    private class Point{
        private float _x, _y;
        Point(float x, float y){
            _x = x;
            _y = y;
        }
        public float getX(){return _x;}
        public float getY(){return _y;}
        public void setY(float newY){_y = newY;}
        public void setX(float newX){_x = newX;}
    }
}
