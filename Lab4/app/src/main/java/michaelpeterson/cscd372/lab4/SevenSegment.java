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
    int _currentValue;
    boolean[] _componentState;
    ArrayList<Point> _componentVertices;
    float[][] _componentTransMatrices;
    byte[] _valueStateMappings;

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
        _currentValue = 9;
        _componentState = new boolean[8];

        // setup the vertices
        _componentVertices = new ArrayList<>();
        _componentVertices.add(new Point(-40, 0));
        _componentVertices.add(new Point(-30, 10));
        _componentVertices.add(new Point( 30, 10));
        _componentVertices.add(new Point( 40, 0));
        _componentVertices.add(new Point( 30,-10));
        _componentVertices.add(new Point(-30,-10));

        // setup the transformation matrices
        _componentTransMatrices = new float[][] {
                {1,  0,  0,   // Component 0
                 0,  1,  0,
                 0,  0,  1},

                {0, -1, 40,   // Component 1
                 1,  0,-40,
                 0,  0,  1},

                {1,  0,  0,   // Component 2
                 0,  1, -80,
                 0,  0,  1},

                {0, -1, -40,  // Component 3
                 1,  0, -40,
                 0,  0,  1},

                {0, -1, -40,  // Component 4
                 1,  0,  40,
                 0,  0,  1},

                {1,  0,  0,   // Component 5
                 0,  1,  80,
                 0,  0,  1},

                {0, -1,  40,  // Component 6
                 1,  0,  40,
                 0,  0,  1}
        };

        // trans and rot
        //  0, -1, transx
        //  1, 0,  transy
        //  0, 0,  1

        // trans only
        //  1, 0, transx
        //  0, 1,  transy
        //  0, 0,  1

        // setup the value state mappings
        _valueStateMappings = new byte[]{
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
        };

        // component to bit: 76543210
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Create our base (template) path
        boolean haveFirstPoint = false;
        Path basePath = new Path();
        basePath.moveTo(_componentVertices.get(0).getX(), _componentVertices.get(0).getY());
        for(Point thisPoint : _componentVertices)
            if(haveFirstPoint)
                basePath.lineTo(thisPoint.getX(), thisPoint.getY());
            else
                haveFirstPoint = true;
        basePath.close();

        Paint thisPaint = new Paint();
        thisPaint.setColor(Color.BLUE);
        thisPaint.setStrokeWidth(2);
        thisPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        canvas.translate(50, 100);

        for(int i = 0; i < 7; ++i){
            Path thisPath = new Path();
            thisPath.addPath(basePath);
            Matrix thisTransMatrix = new Matrix();
            thisTransMatrix.setValues(_componentTransMatrices[i]);
            thisPath.transform(thisTransMatrix);

            thisPaint.setColor(((_valueStateMappings[_currentValue] & (1 << i)) != 0) ? Color.RED : Color.rgb(127, 0, 0));
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
    }
}
