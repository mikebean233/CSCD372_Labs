package michaelpeterson.lab7;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.View;

import java.io.Serializable;
import java.util.Stack;

public class BouncyWeightView extends View implements Runnable, Serializable{
    private transient Drawable _weightDrawable;
    private final Point _originalCanvasDimensions = new Point(600, 800);
    private Point _lastKnownSize;
    private Point _weightStartingPos;
    private float _aspectRatio;
    private long _lastTime;
    private final int _pixelsPerMeter = 25;
    private int _springLinks = 10;
    private final int _weightWidth = 100;
    private final float _springWidth = 10;
    private volatile long _timeSpentPaused;
    private long _lastToggleTime;
    private Stack<PlayState> _stateStack;

    // Physics variables
    private float _hooksConstant = 1.5f;             // N/m
    private final float _gravitationalConstant = 9.80665f; // meters / second^2
    private float _weightMass = 1.0f;                // kilograms
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

    public BouncyWeightView setWeightDrawable(Drawable newDrawable){
        if(newDrawable == null)
            throw new NullPointerException();

        storeState();
        setStatePause();
        _weightDrawable = newDrawable;
        restoreState();
        invalidate();
        return this;
    }

    public BouncyWeightView setSpringStiffness(Float newStiffness){
        if(newStiffness == null)
            throw new NullPointerException();

        if(newStiffness == _hooksConstant)
            return this;

        storeState();
        setStatePause();
        _hooksConstant = newStiffness;
        restoreState();
        return this;
    }

    public BouncyWeightView setCoilCount(Integer newCoilCount){
        if(newCoilCount == null)
            throw new NullPointerException();

        if(newCoilCount == _springLinks)
            return this;

        storeState();
        setStatePause();
        _springLinks = newCoilCount;
        restoreState();
        return this;
    }

    public BouncyWeightView setInitialDisplacement(Integer newInitDisplacement){
        if(newInitDisplacement == null)
            throw new NullPointerException();

        float newDispInViewPixels = ((float)newInitDisplacement * (float) _pixelsPerMeter);
        if(newDispInViewPixels == _weightStartingPos.getY())
            return this;

        storeState();
        setStatePause();
        _weightStartingPos.setY(newDispInViewPixels);
        reset();
        restoreState();
        return this;
    }

    public void reset(){
        storeState();
        setStatePause();
        _velocity = 0;
        _currentWeightPos = ((Point)_weightStartingPos.clone()).shiftY(_weightWidth / 2);
        invalidate();
        restoreState();
    }

    public void copyState(BouncyWeightView oldState){
        if(oldState == null)
            return;
        _lastKnownSize     = (Point) oldState._lastKnownSize.clone();
        _lastTime          = oldState._lastTime;
        _timeSpentPaused   = oldState._timeSpentPaused;
        _lastToggleTime    = oldState._lastToggleTime;
        _currentWeightPos  = oldState._currentWeightPos;
        _velocity          = oldState._velocity;
        _playState         = oldState._playState;
        _stateStack        = oldState._stateStack;
        _hooksConstant     = oldState._hooksConstant;
        _springLinks       = oldState._springLinks;
        _weightStartingPos = oldState._weightStartingPos;
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
    
    public BouncyWeightView toggleState(){
        if(_playState == PlayState.paused)
            setStateRun();
        else
            setStatePause();

        return this;
    }

    public BouncyWeightView setStatePause(){
        long currentTime = System.currentTimeMillis();
        if(_playState == PlayState.running){
            _playState = PlayState.paused;
            _lastToggleTime = currentTime;
        }

        return this;
    }

    public BouncyWeightView setStateRun(){
        long currentTime = System.currentTimeMillis();
        if(_playState == PlayState.paused){
            _playState = PlayState.running;
            _timeSpentPaused += currentTime - _lastToggleTime;
            _lastToggleTime = currentTime;
        }

        return this;
    }

    public BouncyWeightView storeState(){
        _stateStack.push(_playState);
        return this;
    }

    public BouncyWeightView restoreState(){
        if(_stateStack.size() == 0)
            return this;
        PlayState storedState = _stateStack.pop();

        if(storedState == PlayState.paused)
            setStatePause();
        else
            setStateRun();
        return this;
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
        _stateStack = new Stack<>();
        _playState = PlayState.paused;
        _lastTime = System.currentTimeMillis();
        _lastToggleTime = _lastTime;
        _weightStartingPos = new Point(_originalCanvasDimensions.getX() / 2, (float)(_originalCanvasDimensions.getY() * 0.75 ));
        _currentWeightPos = ((Point)_weightStartingPos.clone()).shiftY(_weightWidth / 2);
        _aspectRatio = _originalCanvasDimensions.getX() / _originalCanvasDimensions.getY();
        _velocity = 0.0f;

        // Create the starting weight Drawable
        ShapeDrawable weightShape = new ShapeDrawable((new RectShape()));
        weightShape.getPaint().setColor(Color.BLUE);
        weightShape.getPaint().setStrokeWidth(2);
        weightShape.getPaint().setStyle(Paint.Style.STROKE);
        weightShape.setBounds(1, 1, 1, 1);
        _weightDrawable = weightShape;
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        float width  = (_lastKnownSize == null) ? canvas.getWidth()  : _lastKnownSize.getX();
        float height = (_lastKnownSize == null) ? canvas.getHeight() : _lastKnownSize.getY();
        float widthScaleFactor  = width  / _originalCanvasDimensions.getX();
        float heightScaleFactor = height / _originalCanvasDimensions.getY();

        // Calculate the new position
        if(_playState == PlayState.running) {
            long currentTime = System.currentTimeMillis() - _timeSpentPaused;
            float timeElapsedInSeconds = (float)(currentTime - _lastTime) / 1000.0f;
            _lastTime = currentTime;

            float relativePositionInMeters = (_currentWeightPos.getY() - _weightWidth / 2) / (float)_pixelsPerMeter;
            float acceleration = _gravitationalConstant - (relativePositionInMeters * _hooksConstant) / _weightMass;
            _velocity += acceleration * timeElapsedInSeconds;
            _currentWeightPos.shiftY((_velocity * timeElapsedInSeconds) * _pixelsPerMeter);
        }

        // draw the weight
        canvas.scale(widthScaleFactor, heightScaleFactor);

        RectF thisRect = new RectF(-1,-1,1,1);
        ChainableMatrix.identity().chainScale(_weightWidth, _weightWidth).mapRect(thisRect);
        ChainableMatrix.identity().chainTranslate(_currentWeightPos.getX(), _currentWeightPos.getY()).mapRect(thisRect);

        _weightDrawable.setBounds((int) thisRect.left, (int) thisRect.top, (int)thisRect.right, (int)thisRect.bottom ); ;
        _weightDrawable.draw(canvas);

        // draw the spring
        Paint thisPaint = new Paint();
        thisPaint.setStrokeWidth(3);
        thisPaint.setStyle(Paint.Style.STROKE);
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
