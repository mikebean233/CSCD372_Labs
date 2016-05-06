package michaelpeterson.lab5;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.io.Serializable;

public class BouncyWeightView extends View implements Runnable, Serializable{
    private Point[] _weightVertices;
    private transient Path _originalWeightPath;
    private final Point _originalCanvasDimensions = new Point(600, 800);
    private Point _lastKnownSize;
    private Point _weightStartingPos;
    private float _aspectRatio;
    private long _lastTime;
    private final int _pixelsPerMeter = 25;
    private final int _springLinks = 10;
    private final int _weightWidth = 100;
    private final float _springWidth = 10;
    private volatile long _timeSpentPaused;
    private long _lastToggleTime;

    // Physics variables
    private final float _hooksConstant = 1.5f;             // N/m
    private final float _gravitationalConstant = 9.80665f; // meters / second^2
    private final float _weightMass = 1.0f;                // kilograms
    private Point _currentWeightPos;
    private float _velocity = 0.0f;

    public enum PlayState{
        running(0),
        paused(1);

        private final int _value;

        private PlayState(int value){
            _value = value;
        }

        @Override
        public String toString(){
            switch(_value){
                case 0:
                    return "running";
                case 1:
                    return "paused";
            }
            return "invalid";
        }

        public int getValue(){return _value;}
    }

    private PlayState _playState;

    public PlayState getPlayState(){return _playState;}

    public void copyState(BouncyWeightView oldState){
        if(oldState == null)
            return;
        _lastKnownSize    = (Point) oldState._lastKnownSize.clone();
        _lastTime         = oldState._lastTime;
        _timeSpentPaused  = oldState._timeSpentPaused;
        _lastToggleTime   = oldState._lastToggleTime;
        _currentWeightPos = oldState._currentWeightPos;
        _velocity         = oldState._velocity;
        _playState        = oldState._playState;
    }

    public BouncyWeightView(Context context) {
        super(context);
        initialize();
    }

    @Override
    public void run() {
        if (_playState == PlayState.running) {
            invalidate();
        }
    }
    
    public void toggleState(){
        if(_playState == PlayState.paused)
            setStateRun();
        else
            setStatePause();
    }

    public void setStatePause(){
        long currentTime = System.currentTimeMillis();
        if(_playState == PlayState.running){
            _playState = PlayState.paused;
            _lastToggleTime = currentTime;
        }
    }

    public void setStateRun(){
        long currentTime = System.currentTimeMillis();
        if(_playState == PlayState.paused){
            _playState = PlayState.running;
            _timeSpentPaused += currentTime - _lastToggleTime;
            _lastToggleTime = currentTime;
        }
    }

    public BouncyWeightView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        initialize();
    }

    public BouncyWeightView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        initialize();
    }

    public void initialize(){
        _playState = PlayState.paused;
        _lastTime = System.currentTimeMillis();
        _lastToggleTime = _lastTime;
        _weightStartingPos = new Point(_originalCanvasDimensions.getX() / 2, (float)(_originalCanvasDimensions.getY() * 0.75 ));
        _currentWeightPos = (Point)_weightStartingPos.clone();
        _aspectRatio = _originalCanvasDimensions.getX() / _originalCanvasDimensions.getY();
        _velocity = 0.0f;

        // setup the weight vertices
        _weightVertices = new Point[]{
            (new Point( 1, -1)),
            (new Point( 1,  1)),
            (new Point(-1,  1)),
            (new Point(-1, -1))
        };

        // Create the starting weight Path
        _originalWeightPath = new Path();
        _originalWeightPath.moveTo(_weightVertices[0].getX(), _weightVertices[0].getY());
        for(int i = 1; i < _weightVertices.length; ++i)
            _originalWeightPath.lineTo(_weightVertices[i].getX(), _weightVertices[i].getY());
        _originalWeightPath.close();
        _originalWeightPath.transform(ChainableMatrix.identity().chainScale(_weightWidth, _weightWidth).getMatrix());
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        float width  = (_lastKnownSize == null) ? canvas.getWidth()  : _lastKnownSize.getX();
        float height = (_lastKnownSize == null) ? canvas.getHeight() : _lastKnownSize.getY();
        float widthScaleFactor  = width  / _originalCanvasDimensions.getX();
        float heightScaleFactor = height / _originalCanvasDimensions.getY();

        Paint thisPaint = new Paint();

        thisPaint.setStrokeWidth(5);
        thisPaint.setStyle(Paint.Style.STROKE);
        thisPaint.setColor(Color.BLUE);

        canvas.scale(widthScaleFactor, heightScaleFactor);

        // Calculate the new position
        if(_playState == PlayState.running) {
            long currentTime = System.currentTimeMillis() - _timeSpentPaused;
            float timeElapsedInSeconds = (float)(currentTime - _lastTime) / 1000.0f;
            _lastTime = currentTime;

            float relativePositionInMeters = (_weightStartingPos.getY() - _currentWeightPos.getY()) / _pixelsPerMeter;
            float acceleration = _gravitationalConstant - relativePositionInMeters * _hooksConstant / _weightMass;
            _velocity += acceleration * timeElapsedInSeconds;
            float newPositionOffsetInMeters = relativePositionInMeters + _velocity * timeElapsedInSeconds;
            _currentWeightPos.setY(_weightStartingPos.getY() - (newPositionOffsetInMeters * _pixelsPerMeter));
        }

        // draw the weight
        Path weightPath = new Path();
        weightPath.addPath(_originalWeightPath);
        weightPath.transform(ChainableMatrix.identity().chainTranslate(_currentWeightPos.getX(), _currentWeightPos.getY()).getMatrix());
        canvas.drawPath(weightPath, thisPaint);

        // draw the spring
        thisPaint.setStrokeWidth(3);
        thisPaint.setColor(Color.rgb(127,0,0));
        float totalSpringLength = _currentWeightPos.getY() - _weightWidth;
        float linkLength = (totalSpringLength / (_springLinks + 1));
        for(int i = 1; i <= _springLinks; ++i){
            float thisChainPos = i * (totalSpringLength / (_springLinks + 1));
            RectF thisOvalRect = new RectF(_currentWeightPos.getX() - _springWidth, thisChainPos - linkLength, _currentWeightPos.getX() + _springWidth, thisChainPos + linkLength);
            canvas.drawOval(thisOvalRect,thisPaint);
        }

    }

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

        float desiredAspectRatio = (desiredHeight == 0.0f) ? _aspectRatio : desiredWidth / desiredHeight;

        if(desiredAspectRatio > _aspectRatio || desiredWidth == 0){
            // we are keeping the desired height
            finalHeight = desiredHeight;
            finalWidth  = desiredHeight * _aspectRatio;
        }
        else if(desiredAspectRatio <= _aspectRatio || desiredHeight == 0) {
            // we are keeping the desired width
            finalWidth = desiredWidth;
            finalHeight = desiredWidth / _aspectRatio;
        }
        _lastKnownSize = new Point(finalWidth, finalHeight);
        setMeasuredDimension((int)finalWidth, (int)finalHeight);
    }
}
