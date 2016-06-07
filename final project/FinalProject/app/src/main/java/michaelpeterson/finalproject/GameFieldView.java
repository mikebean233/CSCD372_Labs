package michaelpeterson.finalproject;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class GameFieldView extends View implements Runnable, View.OnTouchListener {
    private enum GameState{
        paused(0),
        inPlay(1),
        gameOver(2),
        newGame(3),
        levelStart(4),
        levelResults(5);

        private final int _value;
        private GameState(int value){
            _value = value;
        }

        @Override
        public String toString(){
            switch(_value){
                case 0:
                    return "paused";
                case 1:
                    return "inPlay";
                case 2:
                    return "gameOver";
                case 3:
                    return "newGame";
                case 4:
                    return "levelStart";
                case 5:
                    return "levelResults";
                default:
                    return "invalid";
            }
        }
    }


    private enum SiloGroup{left, center, right, none}

    private GameState _state;
    private GameLevel _currentLevel;
    //private SiloGroup _siloPreference;
    private GameFieldView _this;
    private ArrayList<City> _cities;
    private ArrayList<Silo> _silos;
    private ArrayList<Renderable> _renderList;
    private ArrayList<Missile> _enemyMissiles;
    private ArrayList<Missile> _allMissiles;
    private ArrayList<GroundAsset> _groundAssets;
    private ArrayList<GameLevel> _levels;
    private PositionedPath _terrainPath;
    private Point _originalDimensions = new Point(256, 231);
    private Point _lastKnownSizeDimensions;
    private float _aspectRatio;
    private float _currentScaleFactor;
    private long _timeSpentPaused;
    private long _currentLevelStartTime;
    private boolean _infinitePlayerMissiles = true;
    private int _playerMissileSpeed;
    private int _enemyMissileSpeed;
    private int _noLevels;
    private int _currentLevelNumber;
    private int _soundIdBoom;
    private int _soundIdLaunch;
    private int _soundIdLevel;
    private int _currentScore;
    private int _stoppedMissileCount;
    private Status _status;
    private GameState _stateBeforePause;
    private HashMap<GameState, Long> _stateStartTime;
    private HashMap<GameState, Long> _stateLength; // (seconds) obviously this does not apply for pause and inPlay
    private HashMap<GameState, GameState> _stateTransitionMap;
    private SoundPool _soundPool;
    public double calcDistance(Point a, Point b)
    {
        return Math.sqrt(Math.pow(b.getX() - a.getX(), 2.0f) + Math.pow(b.getY() - a.getY(), 2.0f));
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        //setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        float desiredWidth = (float) MeasureSpec.getSize(widthMeasureSpec);
        float desiredHeight  = (float) MeasureSpec.getSize(heightMeasureSpec);
        float finalHeight = 0;
        float finalWidth  = 0;
        float desiredAspectRatio = (float) desiredWidth / (float) desiredHeight;

        if(desiredAspectRatio > _aspectRatio || desiredWidth == 0){
            // we are keeping the desired height
            finalHeight = desiredHeight;
            finalWidth  = desiredHeight / _aspectRatio;
        }
        else if(desiredAspectRatio <= _aspectRatio || desiredHeight == 0) {
            // we are keeping the desired width
            finalWidth  = desiredWidth;
            finalHeight = desiredWidth / _aspectRatio;
        }
        setMeasuredDimension((int)finalWidth, (int)finalHeight);
        _lastKnownSizeDimensions = new Point(finalWidth, finalHeight);
        _currentScaleFactor = _lastKnownSizeDimensions.getX() / _originalDimensions.getX();
    }

    public GameFieldView(Context context){
        super(context);
        initialize();
    }
    public GameFieldView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        initialize();
    }

    public GameFieldView(Context context, AttributeSet attributeSet, int defStyle){
        super(context, attributeSet, defStyle);
        initialize();
    }

    public void pause(){
        if(_state != GameState.paused) {
            if (_stateBeforePause != GameState.paused)
                _stateBeforePause = _state;
            changeState(GameState.paused);
        }
    }

    public void resume(){
        if(_state == GameState.paused) {
            _timeSpentPaused += getAdjustedTimeInMillis() - _stateStartTime.get(GameState.paused);
            changeState(_stateBeforePause);
        }
    }

    public void togglePause(){
        if(_state == GameState.paused)
            resume();
        else
            pause();
    }


    private Long changeState(GameState newState){
        Log.d("--------------", _state + " -> " + newState);
        long oldTime = _stateStartTime.get(_state);
        _stateStartTime.put(_state = newState, getAdjustedTimeInMillis());
        if(newState == GameState.levelStart)
            _soundPool.play(_soundIdLevel, 1, 1, 10, 0, 1);
        return oldTime;
    }

    private long getTimeInCurrentState(){
        if(_stateStartTime.containsKey(_state))
            return getAdjustedTimeInMillis() - _stateStartTime.get(_state);

        return -1;
    }

    private void updateGameItems(){
        long currentAdjustedTime = getAdjustedTimeInMillis();
        boolean haveActiveMissiles = false;

        _status.update(_currentScore, _currentLevelNumber, _state);

        // inPlay
        if(_state == GameState.inPlay){
            ArrayList<Missile> explodingMissiles = new ArrayList<>();
            ArrayList<Missile> launchingMissiles = new ArrayList<>();

            // Update our missiles and check for collisions
            for (Missile thisMissile : _allMissiles) {
                // Update the missile
                thisMissile.timerCLick(currentAdjustedTime);

                // Check to see if any exploding missiles are hitting ground assets
                if(thisMissile.isExploding()){
                    haveActiveMissiles = true;
                    explodingMissiles.add(thisMissile);
                    if(thisMissile.getType() == MissileType.enemy){
                        for(GroundAsset thisAsset : _groundAssets)
                            thisAsset.testMissileHit(thisMissile);
                    }
                }
                else if(thisMissile.isLaunching()) {
                    launchingMissiles.add(thisMissile);
                    haveActiveMissiles = true;
                }
            }

            // Detonate any missiles that are being hit by an explosion
            for(Missile explodingMissile : explodingMissiles){
                for(Missile launchingMissile : launchingMissiles){
                    if(launchingMissile.getType() == MissileType.enemy && explodingMissile.checkForCollision(launchingMissile.getLocation())) {
                        launchingMissile.detonate();
                        _stoppedMissileCount++;
                    }
                }
            }

            // Launch enemy missiles
            int noMissilesDue = _currentLevel.getOverdueMissileCount(currentAdjustedTime - _currentLevelStartTime);
            while(noMissilesDue-- > 0)
                launchRandomEnemyMissile();



        }

        //level Start
        if(_state == GameState.levelStart){

        }




        // Update the Game State

        // Check to see if the game is over or the level is finished
        if(_state == GameState.inPlay) {
            int standingCityCount = 0;
            for (City thisCity : _cities)
                standingCityCount += (thisCity.getState() == GroundAssetState.visible) ? 1 : 0;

            // Check to see if the level is finished (all enemy missiles have been launched and no missiles are active)
            if(!haveActiveMissiles){
                if (standingCityCount == 0)
                    changeState(GameState.gameOver);
                else if(_currentLevel.isFinished())
                    startNewLevel(false);

                return;
            }
        }

        // If we are in a new game, reset the score and start at level 1
        if(_state == GameState.newGame){
            startNewGame();
            return;
        }

        // If we are in a timed game state (levelStart, revelResults, or gameOver) and the state should be finished, move on to the next state

        if(_state != GameState.inPlay) {
            try {
                if (getTimeInCurrentState() >= _stateLength.get(_state))
                    changeState(_stateTransitionMap.get(_state));
            } catch (Exception ex) {
                Log.d("", "", ex);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas){
        updateGameItems();

        for (Renderable thisRenderable : _renderList)
            thisRenderable.render(canvas, _currentScaleFactor);
    }

    public boolean addRenderable(Renderable newItem){
        if(newItem == null)
            throw new NullPointerException();

        if (!_renderList.contains(newItem)) {
            _renderList.add(newItem);
            return true;
        }

        return false;
    }

    public boolean registerMissile(Missile thisMissile){
        if(thisMissile == null)
            throw new NullPointerException();
           if(!_allMissiles.contains(thisMissile)) {
                _allMissiles.add(thisMissile);
                addRenderable(thisMissile);
                return true;
            }
        return false;
    }

    public boolean registerGroundAsset(GroundAsset thisAsset){
        if(thisAsset == null)
            throw new NullPointerException();
        if(!_groundAssets.contains(thisAsset)) {
            _groundAssets.add(thisAsset);
            addRenderable(thisAsset);
            return true;
        }
         return false;
    }

    public void start(final Activity activity){
        Timer animationTimer = new Timer();
        final GameFieldView self = this;
        animationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(self);
            }
        }, 0, 15);


    }
    private void initialize(){
        this.setOnTouchListener(this);
        _this = this;
        _aspectRatio = _originalDimensions.getX() / _originalDimensions.getY();
        _lastKnownSizeDimensions = _originalDimensions.clone();
        _currentScaleFactor = 1.0f;
        setLayerType(this.LAYER_TYPE_SOFTWARE, null);
        //_siloPreference = SiloGroup.none;
        _infinitePlayerMissiles = false;
        _allMissiles = new ArrayList<>();
        _playerMissileSpeed = 150;
        _enemyMissileSpeed = 15;
        _currentLevelNumber = -1;
        _soundPool = new SoundPool(100, AudioManager.STREAM_MUSIC, 0);
        _soundIdBoom    = _soundPool.load(getContext(), R.raw.boom,1);
        _soundIdLaunch  = _soundPool.load(getContext(), R.raw.launch, 1);
        _soundIdLevel   = _soundPool.load(getContext(), R.raw.levelend, 1);
        _stateStartTime = new HashMap<>();
        _currentScore = 0;
        _stoppedMissileCount = 0;

        // Game State
        _state = GameState.newGame;
        _stateLength = new HashMap<>();
        _stateTransitionMap = new HashMap<>();
        _stateLength.put(GameState.levelStart, 3000l );
        _stateLength.put(GameState.levelResults, 3000l );
        _stateLength.put(GameState.gameOver, 5000l );
        _stateTransitionMap.put(GameState.gameOver, GameState.newGame);
        _stateTransitionMap.put(GameState.levelResults, GameState.levelStart);
        _stateTransitionMap.put(GameState.levelStart, GameState.inPlay);
        _stateStartTime.put(GameState.newGame, 0l);
        _stateStartTime.put(GameState.levelStart, 0l);
        _stateStartTime.put(GameState.inPlay, 0l);
        _stateStartTime.put(GameState.levelResults, 0l);
        _stateStartTime.put(GameState.gameOver, 0l);
        _stateStartTime.put(GameState.paused, 0l);


        // Enemy Missiles
        _enemyMissiles = new ArrayList<>();
        for(int i = 0; i < _originalDimensions.getX(); i += 5){
            _enemyMissiles.add(new Missile(MissileType.enemy, true, this));
        }

        // Terrain
        _terrainPath = new PositionedPath(buildPath(VisibleGameObject.terrain), new Point(0,0), PaintBuilder.buildPaint(Paint.Style.FILL, Color.rgb(0xff, 0xff, 0), 1));

        // Status
        _status = new Status();

        // Build our render list
        _renderList = new ArrayList<>();
        _renderList.add(_terrainPath);
        _renderList.add(_status);
        // Build our ground assets
        _groundAssets = new ArrayList<>();
        buildGroundAssets();

        buildLevels();

        for (Missile thisMissile : _enemyMissiles)
            registerMissile(thisMissile);

        startNewGame();
    }

    private void buildLevels(){
        _levels = new ArrayList<>();
        //                        noEnemyMissiles enemyMissileSpeed  levelLength (seconds)
        _levels.add(new GameLevel(      10,               150,             15 ));
        _levels.add(new GameLevel(      15,               200,             15 ));
        _levels.add(new GameLevel(      20,               250,             17 ));
        _levels.add(new GameLevel(      25,               300,             20 ));
        _levels.add(new GameLevel(30, 35, 22));
        _levels.add(new GameLevel(35, 40, 25));
        _levels.add(new GameLevel(40, 45, 27));
        _levels.add(new GameLevel(45, 50, 30));

    }

    private void startNewLevel(boolean newGame){
        if(++_currentLevelNumber > _levels.size()){
            startNewGame();
            return;
        }
        if(!newGame) {
            // Update the score
            // Destroyed scud count
            _currentScore += _stoppedMissileCount * 20;
            // Remaining player missiles
            for (Silo thisSilo : _silos)
                _currentScore += (thisSilo.getState() == GroundAssetState.invisible) ? 0 : 40;
            // Remaining Cities
            for (City thisCity : _cities)
                _currentScore += (thisCity.getState() == GroundAssetState.invisible) ? 0 : 100;
        }else
            _currentScore = 0;

        _stoppedMissileCount = 0;

        _currentLevel = _levels.get(_currentLevelNumber);
        _enemyMissileSpeed = _currentLevel.getEnemyMissileSpeed();
        reviveSilos();
        _currentLevelStartTime = getAdjustedTimeInMillis();
        Log.d("Missile Command", "Starting Level " + _currentLevelNumber);
        if(newGame)
            changeState(GameState.levelStart);
        else
            changeState(GameState.levelResults);
    }

    public void startNewGame(){
        _currentScore = 0;
        _currentLevelNumber = -1;
        reviveSilos();
        reviveCities();
        buildLevels(); // We don't want exactly the same missile distribution the next time around, do we?
        startNewLevel(true);
    }

    private void buildGroundAssets(){
        buildCities();
        buildSilos();
    }

    private void buildCities(){
        // Cities
        _cities = new ArrayList<>();

        _cities.add(new City(new Point(44, 215)));
        _cities.add(new City(new Point(71, 216)));
        _cities.add(new City(new Point(95, 217)));
        _cities.add(new City(new Point(148, 215)));
        _cities.add(new City(new Point(180, 212)));
        _cities.add(new City(new Point(208, 216)));

        for(City thisCity : _cities)
            registerGroundAsset(thisCity);
    }

    private void reviveSilos(){
        for(Silo thisSilo: _silos)
            thisSilo.reset();
    }

    private void reviveCities(){
        for(City thisCity : _cities)
            thisCity.reset();
    }
    private void buildSilos(){
        // Silos
        _silos = new ArrayList<>();

        // Left
        _silos.add(new Silo(new Point(20.5f, 208.5f), SiloGroup.left, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(17.5f, 211.5f), SiloGroup.left, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(23.5f, 211.5f), SiloGroup.left, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(14.5f, 214.5f), SiloGroup.left, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(20.5f, 214.5f), SiloGroup.left, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(26.5f, 214.5f), SiloGroup.left, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(11.5f, 217.5f), SiloGroup.left, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(17.5f, 217.5f), SiloGroup.left, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(23.5f, 217.5f), SiloGroup.left, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(29.5f, 217.5f), SiloGroup.left, _infinitePlayerMissiles));
        // Center
        _silos.add(new Silo(new Point(123.5f, 208.5f), SiloGroup.center, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(120.5f, 211.5f), SiloGroup.center, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(126.5f, 211.5f), SiloGroup.center, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(117.5f, 214.5f), SiloGroup.center, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(123.5f, 214.5f), SiloGroup.center, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(129.5f, 214.5f), SiloGroup.center, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(114.5f, 217.5f), SiloGroup.center, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(120.5f, 217.5f), SiloGroup.center, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(126.5f, 217.5f), SiloGroup.center, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(132.5f, 217.5f), SiloGroup.center, _infinitePlayerMissiles));
        // Right
        _silos.add(new Silo(new Point(240.5f, 208.5f), SiloGroup.right, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(237.5f, 211.5f), SiloGroup.right, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(243.5f, 211.5f), SiloGroup.right, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(234.5f, 214.5f), SiloGroup.right, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(240.5f, 214.5f), SiloGroup.right, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(246.5f, 214.5f), SiloGroup.right, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(231.5f, 217.5f), SiloGroup.right, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(237.5f, 217.5f), SiloGroup.right, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(243.5f, 217.5f), SiloGroup.right, _infinitePlayerMissiles));
        _silos.add(new Silo(new Point(249.5f, 217.5f), SiloGroup.right, _infinitePlayerMissiles));

        for(Silo thisSilo : _silos) {
            registerGroundAsset(thisSilo);
            registerMissile(thisSilo.getMissile());
        }
    }

    private Point screenToGameCoords(Point screenPos){
        return new Point(screenPos.getX() / _currentScaleFactor, screenPos.getY() / _currentScaleFactor);
    }

    private float gameToScreenCoords(float gameCoord){
        return gameCoord * _currentScaleFactor;
    }

    @Override
    public void run() {
        this.invalidate();
    }

    private boolean launchRandomEnemyMissile(){
        // Get a list of target candidates
        ArrayList<GroundAsset> candidates = new ArrayList<>();
        for(GroundAsset thisAsset : _groundAssets)
            if(thisAsset.getState() == GroundAssetState.visible)
                candidates.add(thisAsset);

        // Pick a random visible target on the ground and launch a missile at it
        if(!candidates.isEmpty()) {
            GroundAsset chosenTarget = candidates.get(SharedRandom.nextInt(candidates.size()));
            Point randomStartLoc = new Point(SharedRandom.nextFloat() * _originalDimensions.getX(), 8);
            for(Missile thisMissile : _enemyMissiles)
                if(thisMissile.tryLaunch(randomStartLoc, chosenTarget.getPosition().clone(),_enemyMissileSpeed))
                    return true;

        }
        return false;
    }

    private void handleTouch(Point position){
        // in play
        if(_status.pauseResumeButtonHit(position)){
            togglePause();
        }
        else if(_state == GameState.inPlay) {
            // Find first silo that's visible
            Silo nearestSilo = _silos.get(0);
            for(Silo thisSilo: _silos){
                if(thisSilo.getState() == GroundAssetState.visible){
                    nearestSilo = thisSilo;
                    break;
                }
            }

            if(nearestSilo != null) {
                double nearestDistance = calcDistance(nearestSilo.getPosition(), position);
                for (Silo thisSilo : _silos) {
                    if (thisSilo.getState() == GroundAssetState.visible && calcDistance(thisSilo.getPosition(), position) < nearestDistance) {
                        nearestSilo = thisSilo;
                        nearestDistance = calcDistance(thisSilo.getPosition(), position);
                    }
                }
                nearestSilo.tryLaunchMissile(position, _playerMissileSpeed);
            }

        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                handleTouch(screenToGameCoords(new Point(event.getX(), event.getY())));
                break;
            default:
                return false;
        }

        return true;
    }

    private enum VisibleGameObject{
        terrain,
        silo,
        city,
        crosshair
    }
    
    private Path buildPath(VisibleGameObject objectType){
        ArrayList<Point> points = new ArrayList<>();
        Path thisPath = new Path();
        boolean haveFirstPoint = false;

        switch(objectType){
            case crosshair:
                /*
                // Plus
                points.add(new Point(-.5f, -.5f)); // 0
                points.add(new Point(-.5f,-3.5f)); // 1
                points.add(new Point( .5f,-3.5f)); // 2
                points.add(new Point( .5f, -.5f)); // 3
                points.add(new Point(3.5f, -.5f)); // 4
                points.add(new Point(3.5f,  .5f)); // 5
                points.add(new Point( .5f,  .5f)); // 6
                points.add(new Point( .5f, 3.5f)); // 7
                points.add(new Point(-.5f, 3.5f)); // 8
                points.add(new Point(-.5f,  .5f)); // 9
                points.add(new Point(-3.5f, .5f)); // 10
                points.add(new Point(-3.5f,-.5f)); // 11
                */
                // X
                points.add(new Point(-1.5f, -1.5f));
                points.add(new Point( 1.5f,  1.5f));
                points.add(new Point( 0.0f,  0.0f));
                points.add(new Point(-1.5f,  1.5f));
                points.add(new Point( 1.5f, -1.5f));
                points.add(new Point( 0.0f,  0.0f));


                break;
            case terrain:
                points.add(new Point(0,   215));// 0
                points.add(new Point(1,   215));// 1
                points.add(new Point(1,   214));// 2
                points.add(new Point(3,   214));// 3
                points.add(new Point(3,   213));// 4
                points.add(new Point(4,   213));// 5
                points.add(new Point(4,   212));// 6
                points.add(new Point(5,   212));// 7
                points.add(new Point(5,   211));// 8
                points.add(new Point(6,   211));// 9
                points.add(new Point(6,   210));// 10
                points.add(new Point(8,   210));// 11
                points.add(new Point(8,   209));// 12
                points.add(new Point(9,   209));// 13
                points.add(new Point(9,   208));// 14
                points.add(new Point(10,  208));// 15
                points.add(new Point(10,  207));// 16
                points.add(new Point(11,  207));// 17
                points.add(new Point(11,  206));// 18
                points.add(new Point(12,  206));// 19
                points.add(new Point(12,  205));// 20
                points.add(new Point(14,  205));// 21
                points.add(new Point(14,  206));// 22
                points.add(new Point(15,  206));// 23
                points.add(new Point(15,  207));// 24
                points.add(new Point(28,  207));// 25
                points.add(new Point(28,  206));// 26
                points.add(new Point(29,  206));// 27
                points.add(new Point(29,  205));// 28
                points.add(new Point(31,  205));// 29
                points.add(new Point(31,  206));// 30
                points.add(new Point(32,  206));// 31
                points.add(new Point(32,  207));// 32
                points.add(new Point(33,  207));// 33
                points.add(new Point(33,  208));// 34
                points.add(new Point(34,  208));// 35
                points.add(new Point(34,  209));// 36
                points.add(new Point(35,  209));// 37
                points.add(new Point(35,  210));// 38
                points.add(new Point(36,  210));// 39
                points.add(new Point(36,  212));// 40
                points.add(new Point(37,  212));// 41
                points.add(new Point(37,  214));// 42
                points.add(new Point(36,  214));// 43
                points.add(new Point(36,  215));// 44
                points.add(new Point(37,  215));// 45
                points.add(new Point(37,  216));// 46
                points.add(new Point(51,  216));// 47
                points.add(new Point(51,  215));// 48
                points.add(new Point(52,  215));// 49
                points.add(new Point(52,  214));// 50
                points.add(new Point(55,  214));// 51
                points.add(new Point(55,  213));// 52
                points.add(new Point(56,  213));// 53
                points.add(new Point(56,  212));// 54
                points.add(new Point(58,  212));// 55
                points.add(new Point(58,  213));// 56
                points.add(new Point(59,  213));// 57
                points.add(new Point(59,  214));// 58
                points.add(new Point(60,  214));// 59
                points.add(new Point(60,  215));// 60
                points.add(new Point(61,  215));// 61
                points.add(new Point(61,  216));// 62
                points.add(new Point(64,  216));// 63
                points.add(new Point(64,  217));// 64
                points.add(new Point(78,  217));// 65
                points.add(new Point(78,  216));// 66
                points.add(new Point(79,  216));// 67
                points.add(new Point(79,  215));// 68
                points.add(new Point(88,  215));// 69
                points.add(new Point(87,  216));// 70
                points.add(new Point(87,  217));// 71
                points.add(new Point(88,  217));// 72
                points.add(new Point(88,  218));// 73
                points.add(new Point(102, 218));// 74
                points.add(new Point(102, 217));// 75
                points.add(new Point(103, 217));// 76
                points.add(new Point(103, 215));// 77
                points.add(new Point(107, 215));// 78
                points.add(new Point(107, 214));// 79
                points.add(new Point(111, 214));// 80
                points.add(new Point(111, 213));// 81
                points.add(new Point(112, 213));// 82
                points.add(new Point(112, 212));// 83
                points.add(new Point(113, 212));// 84
                points.add(new Point(113, 211));// 85
                points.add(new Point(114, 211));// 86
                points.add(new Point(114, 210));// 87
                points.add(new Point(115, 210));// 88
                points.add(new Point(115, 208));// 89
                points.add(new Point(116, 208));// 90
                points.add(new Point(116, 207));// 91
                points.add(new Point(117, 207));// 92
                points.add(new Point(117, 205));// 93
                points.add(new Point(119, 205));// 94
                points.add(new Point(119, 206));// 95
                points.add(new Point(120, 206));// 96
                points.add(new Point(120, 207));// 97
                points.add(new Point(128, 207));// 98
                points.add(new Point(128, 206));// 99
                points.add(new Point(129, 206));// 100
                points.add(new Point(129, 205));// 101
                points.add(new Point(131, 205));// 102
                points.add(new Point(131, 206));// 103
                points.add(new Point(132, 206));// 104
                points.add(new Point(132, 207));// 105
                points.add(new Point(133, 207));// 106
                points.add(new Point(133, 209));// 107
                points.add(new Point(134, 209));// 108
                points.add(new Point(134, 211));// 109
                points.add(new Point(135, 211));// 110
                points.add(new Point(135, 212));// 111
                points.add(new Point(136, 212));// 112
                points.add(new Point(136, 214));// 113
                points.add(new Point(140, 214));// 114
                points.add(new Point(140, 215));// 115
                points.add(new Point(141, 215));// 116
                points.add(new Point(141, 216));// 117
                points.add(new Point(155, 216));// 118
                points.add(new Point(155, 215));// 119
                points.add(new Point(168, 215));// 120
                points.add(new Point(168, 214));// 121
                points.add(new Point(170, 214));// 122
                points.add(new Point(170, 213));// 123
                points.add(new Point(171, 213));// 124
                points.add(new Point(171, 212));// 125
                points.add(new Point(173, 212));// 126
                points.add(new Point(173, 213));// 127
                points.add(new Point(187, 213));// 128
                points.add(new Point(187, 212));// 129
                points.add(new Point(189, 212));// 130
                points.add(new Point(189, 213));// 131
                points.add(new Point(190, 213));// 132
                points.add(new Point(190, 215));// 133
                points.add(new Point(193, 215));// 134
                points.add(new Point(193, 216));// 135
                points.add(new Point(201, 216));// 136
                points.add(new Point(201, 217));// 137
                points.add(new Point(215, 217));// 138
                points.add(new Point(215, 216));// 139
                points.add(new Point(222, 216));// 140
                points.add(new Point(222, 215));// 141
                points.add(new Point(223, 215));// 142
                points.add(new Point(223, 214));// 143
                points.add(new Point(225, 214));// 144
                points.add(new Point(225, 213));// 145
                points.add(new Point(226, 213));// 146
                points.add(new Point(226, 212));// 147
                points.add(new Point(227, 212));// 148
                points.add(new Point(227, 211));// 149
                points.add(new Point(228, 211));// 150
                points.add(new Point(228, 210));// 151
                points.add(new Point(229, 210));// 152
                points.add(new Point(229, 209));// 153
                points.add(new Point(230, 209));// 154
                points.add(new Point(230, 207));// 155
                points.add(new Point(231, 207));// 156
                points.add(new Point(231, 206));// 157
                points.add(new Point(233, 206));// 158
                points.add(new Point(233, 205));// 159
                points.add(new Point(235, 205));// 160
                points.add(new Point(235, 207));// 161
                points.add(new Point(249, 207));// 162
                points.add(new Point(249, 206));// 163
                points.add(new Point(250, 206));// 164
                points.add(new Point(250, 205));// 165
                points.add(new Point(252, 205));// 166
                points.add(new Point(252, 206));// 167
                points.add(new Point(253, 206));// 168
                points.add(new Point(253, 207));// 169
                points.add(new Point(254, 207));// 170
                points.add(new Point(255, 208));// 171
                points.add(new Point(255, 213));// 172
                points.add(new Point(256, 213));// 173
                points.add(new Point(256, 215));// 174
                points.add(new Point(255, 215));// 175
                points.add(new Point(255, 216));// 176
                points.add(new Point(256, 216));// 177
                points.add(new Point(256, 231));// 178
                points.add(new Point(0,   231));// 179
                
                break;
            case city:
                points.add(new Point(-7, 0));
                points.add(new Point(-6, 0));
                points.add(new Point(-6,-2));
                points.add(new Point(-5,-2));
                points.add(new Point(-5,-1));
                points.add(new Point(-4,-1));
                points.add(new Point(-4, 0));
                points.add(new Point(-3, 0));
                points.add(new Point(-3,-2));
                points.add(new Point(-2,-2));
                points.add(new Point(-2,-1));
                points.add(new Point(-1,-1));
                points.add(new Point(-1,-2));
                points.add(new Point( 0,-2));
                points.add(new Point( 0,-4));
                points.add(new Point( 1,-4));
                points.add(new Point( 1,-3));
                points.add(new Point( 2,-3));
                points.add(new Point( 2,-1));
                points.add(new Point( 3,-1));
                points.add(new Point( 3, 0));
                points.add(new Point( 4, 0));
                points.add(new Point( 4,-2));
                points.add(new Point( 5,-2));
                points.add(new Point( 5,-1));
                points.add(new Point( 6,-1));
                points.add(new Point( 6, 0));
                points.add(new Point( 7, 0));
                points.add(new Point( 7, 1));
                points.add(new Point( 2, 1));
                points.add(new Point( 2, 0));
                points.add(new Point( 1, 0));
                points.add(new Point( 1,-1));
                points.add(new Point( 0,-1));
                points.add(new Point( 0, 0));
                points.add(new Point(-2, 0));
                points.add(new Point(-2, 1));
                points.add(new Point(-7, 1));
                break;
            case silo:
                points.add(new Point(-0.5f, -2.5f)); // 0
                points.add(new Point( 0.5f, -2.5f)); // 1
                points.add(new Point( 0.5f,  0.5f)); // 2
                points.add(new Point( 1.5f,  0.5f)); // 3
                points.add(new Point( 1.5f,  2.5f)); // 4
                points.add(new Point( 0.5f,  2.5f)); // 5
                points.add(new Point( 0.5f,  1.5f)); // 6
                points.add(new Point(-0.5f,  1.5f)); // 7
                points.add(new Point(-0.5f,  2.5f)); // 8
                points.add(new Point(-1.5f,  2.5f)); // 9
                points.add(new Point(-1.5f,  0.5f)); // 10
                points.add(new Point(-0.5f,  0.5f)); // 11

                break;
            default:
                throw new IllegalArgumentException();
        }

        thisPath.moveTo(points.get(0).getX(), points.get(0).getY());
        for(Point thisPoint: points){
            if(haveFirstPoint)
                thisPath.lineTo(thisPoint.getX(), thisPoint.getY());
            else
                haveFirstPoint = true;
        }
        thisPath.close();
        return thisPath;
    }

    private long getAdjustedTimeInMillis(){
        return System.currentTimeMillis() - _timeSpentPaused;
    }

    /* --------------------------------------------------------------*
    *                         Status Display                         *
    * ---------------------------------------------------------------*/
    private class Status implements Renderable<Status>{
        private Drawable _levelIndicator;
        //private Drawable _levelValue;
        private Drawable _scoreIndicator;
        //private Drawable _scoreValue;

        private int _currentLevel;
        private int _currentScore;

        // Status Bar
        private int _levelLabelWidth = 36;
        private int _scoreLabelWidth = 39;
        private Point _levelLabelLocation;
        private Point _scoreLabelLocation;

        // Level Start
        private int _levelStartLabelWidth = 112;
        private Point _levelStartLabelLocation;

        // Game Over
        private int _gameOverLabelWidth = 165;
        private int _gameOverLabelHeight = 70;
        private Point _gameOverLabelLocation;

        // New Score
        private int _newScoreLabelWidth = 69;
        private Point _newScoreLabel; 

        // Paused
        private int _pausedLabelWidth = 47;
        private Point _pausedLabelLocation;

        // PauseResume Button
        private int _pauseResumeButtonWidth = 98;
        private int _pauseResumeButtonHeight;
        private Point _pauseResumeButtonLocation;
        
        private int _numberWidth = 7;
        Resources _resources;

        public void update(int score, int level, GameState state){
            _currentScore = score;
            _currentLevel = level + 1;

        }


        public Status(){
            _resources = getContext().getResources();
            _levelIndicator = new BitmapDrawable(_resources, BitmapFactory.decodeResource(_resources, R.drawable.level));
            _scoreIndicator = new BitmapDrawable(_resources, BitmapFactory.decodeResource(_resources, R.drawable.score));

            _levelLabelLocation = new Point(0,1);
            _scoreLabelLocation = new Point(70,1);
            _gameOverLabelLocation = new Point(50, 76);
            _levelStartLabelLocation = new Point(66,66);
            _newScoreLabel = new Point(66,66);
            _pausedLabelLocation = new Point(102,66);
            _pauseResumeButtonLocation = new Point(158, 0);
            _pauseResumeButtonHeight = _numberWidth*2 + 8;
        }

        public boolean pauseResumeButtonHit(Point touchLocation){
            return  (touchLocation.getX() >= _pauseResumeButtonLocation.getX()) &&
                    (touchLocation.getY() >= _pauseResumeButtonLocation.getY()) &&
                    (touchLocation.getX() <= _pauseResumeButtonLocation.getX() + _pauseResumeButtonWidth) &&
                    (touchLocation.getY() <= _pauseResumeButtonLocation.getY() + _pauseResumeButtonHeight);
        }

        @Override
        public Status render(Canvas canvas, float scale) {
            // In Play
            if(_state == GameState.inPlay) {
                _levelIndicator.setBounds((int) (_levelLabelLocation.getX() * scale), (int) (_levelLabelLocation.getY() * scale), (int) ((_levelLabelLocation.getX() + _levelLabelWidth) * scale), (int) ((_levelLabelLocation.getY() + _numberWidth) * scale));
                _scoreIndicator.setBounds((int) (_scoreLabelLocation.getX() * scale), (int) (_scoreLabelLocation.getY() * scale), (int) ((_scoreLabelLocation.getX() + _scoreLabelWidth) * scale), (int) ((_scoreLabelLocation.getY() + _numberWidth) * scale));
                _levelIndicator.draw(canvas);
                _scoreIndicator.draw(canvas);
                drawNumber(_currentLevel, new Point(_levelLabelLocation.getX() + _levelLabelWidth + 3, _levelLabelLocation.getY()), canvas, scale);
                drawNumber(_currentScore, new Point(_scoreLabelLocation.getX() + _scoreLabelWidth + 3, _scoreLabelLocation.getY()), canvas, scale);
            }

            // levelStart
            if(_state == GameState.levelStart){
                Drawable levelStartLabel = new BitmapDrawable(_resources, BitmapFactory.decodeResource(_resources, R.drawable.newlevel));
                //_scoreIndicator.setBounds((int)(_scoreLabelLocation.getX()      * scale), (int)(_scoreLabelLocation.getY()      * scale),(int)((_scoreLabelLocation.getX()       + _scoreLabelWidth)      * scale), (int)(_numberWidth * scale));
                levelStartLabel.setBounds((int)(_levelStartLabelLocation.getX() * scale), (int)(_levelStartLabelLocation.getY() * scale), (int)((_levelStartLabelLocation.getX() + _levelStartLabelWidth) * scale), (int)((_levelStartLabelLocation.getY() + _numberWidth) * scale));
                levelStartLabel.draw(canvas);
                drawNumber(_currentLevel, new Point(_levelStartLabelLocation.getX() + _levelStartLabelWidth + 3, _levelStartLabelLocation.getY()), canvas, scale);
            }

            // Game Over
            if(_state == GameState.gameOver){
                Drawable gameOverLabel = new BitmapDrawable(_resources, BitmapFactory.decodeResource(_resources, R.drawable.theend));
                gameOverLabel.setBounds((int) (_gameOverLabelLocation.getX() * scale), (int)(_gameOverLabelLocation.getY() * scale), (int)((_gameOverLabelLocation.getX() + _gameOverLabelWidth) * scale),  (int)((_gameOverLabelLocation.getY() + _gameOverLabelHeight) * scale));
                gameOverLabel.draw(canvas);
            }

            // Level Results
            if(_state == GameState.levelResults){
                Drawable levelResultsLabel = new BitmapDrawable(_resources, BitmapFactory.decodeResource(_resources, R.drawable.newscore));
                levelResultsLabel.setBounds((int) (_newScoreLabel.getX() * scale), (int)(_newScoreLabel.getY() * scale), (int)((_newScoreLabel.getX() + _newScoreLabelWidth) * scale),  (int)((_newScoreLabel.getY() + _numberWidth) * scale));
                levelResultsLabel.draw(canvas);
                
                drawNumber(_currentScore, new Point(_newScoreLabel.getX() + _newScoreLabelWidth + 3, _newScoreLabel.getY()), canvas, scale);
            }

            // Paused
            if(_state == GameState.paused) {
                Drawable pausedLabel = new BitmapDrawable(_resources, BitmapFactory.decodeResource(_resources, R.drawable.paused));
                pausedLabel.setBounds((int) (_pausedLabelLocation.getX() * scale), (int) (_pausedLabelLocation.getY() * scale), (int) ((_pausedLabelLocation.getX() + _pausedLabelWidth) * scale), (int) ((_pausedLabelLocation.getY() + _numberWidth) * scale));
                pausedLabel.draw(canvas);
            }

            Drawable pauseResumeButton = new BitmapDrawable(_resources, BitmapFactory.decodeResource(_resources, (_state == GameState.paused) ? R.drawable.resume : R.drawable.pause));
            pauseResumeButton.setBounds((int)(_pauseResumeButtonLocation.getX() * scale), (int)(_pauseResumeButtonLocation.getY() * scale), (int)((_pauseResumeButtonLocation.getX() + _pauseResumeButtonWidth) * scale), (int)((_pauseResumeButtonLocation.getY() + _pauseResumeButtonHeight) * scale));
            pauseResumeButton.draw(canvas);


            return null;
        }

        private void drawNumber(int number, Point startingLocation, Canvas canvas, float scale){
            String numberString = (new Integer(number)).toString();
            int scaledWidth = (int)(scale * _numberWidth);
            int scaledHeight = (int)(scale * _numberWidth);
            int scaledStartingX = (int)(scale * startingLocation.getX());
            int scaledStartingY = (int)(scale * startingLocation.getY());
            int digitSpacing = 1;
            int scaledDigitSpacing = (int) (scale * digitSpacing);

            for (int i = 0; i < numberString.length(); ++i) {
                Drawable charDrawable = getBitmapFromChar(numberString.charAt(i));
                charDrawable.setBounds(scaledStartingX + i * (scaledWidth + scaledDigitSpacing), scaledStartingY, scaledStartingX + i * (scaledWidth + scaledDigitSpacing) + scaledWidth, scaledStartingY + scaledHeight);
                charDrawable.draw(canvas);
            }

        }

        Drawable getBitmapFromChar(char input){
            int resourceId = 0;
            switch(input){
                case '0':
                    resourceId = R.drawable.number_0;
                break;
                case '1':
                    resourceId = R.drawable.number_1;
                    break;
                case '2':
                    resourceId = R.drawable.number_2;
                    break;
                case '3':
                    resourceId = R.drawable.number_3;
                    break;
                case '4':
                    resourceId = R.drawable.number_4;
                    break;
                case '5':
                    resourceId = R.drawable.number_5;
                    break;
                case '6':
                    resourceId = R.drawable.number_6;
                    break;
                case '7':
                    resourceId = R.drawable.number_7;
                    break;
                case '8':
                    resourceId = R.drawable.number_8;
                    break;
                case '9':
                    resourceId = R.drawable.number_9;
                    break;
            }
            return new BitmapDrawable(_resources, BitmapFactory.decodeResource(_resources, resourceId));
        }


    }
/*
    private class TextPath extends PositionedPath implements Renderable {
        String text;

        public TextPath(String text, int height){
            _drawable =
        }

        @Override
        public Object render(Canvas canvas, float scale) {
            return null;
        }
    }
*/



    /* --------------------------------------------------------------*
    *                         GameLevel                              *
    * ---------------------------------------------------------------*/
    private class GameLevel
    {
        private long _levelLength; // Seconds
        private int _enemyMissileSpeed;
        private int _levelNo;
        private int _noLaunchedEnemyMissiles;
        private int _noEnemyMissiles;
        private Long[] _missileSchedule; // Stores the time (in milliseconds) after level start that enemy missiles are schedules to fire
        public GameLevel(int noEnemyMissiles, int enemyMissileSpeed, long levelLength)
        {
            _noEnemyMissiles = noEnemyMissiles;
            _enemyMissileSpeed = enemyMissileSpeed;
            _levelLength = levelLength;
            _levelNo = _noLevels++;
            _noLaunchedEnemyMissiles = 0;

            // Generate the missile schedule
            long levelLengthInMillis = _levelLength * 1000;
            _missileSchedule = new Long[noEnemyMissiles];
            for(int i = 0; i < _noEnemyMissiles; ++i){
                _missileSchedule[i] = (long) (SharedRandom.nextFloat() * (float)(levelLengthInMillis));
            }

            Arrays.sort(_missileSchedule);
        }

        public int getNoEnemyMissiles() { return _noEnemyMissiles; }
        public int getEnemyMissileSpeed() { return _enemyMissileSpeed; }
        public int getLevelNo() { return _levelNo; }
        public long getLevelLength(){return _levelLength;}
        public int getOverdueMissileCount(long timeSinceLevelStart){
            int noScheduledSoFar = 0;
            int overdueCount;
            for(int i = 0; i < _noEnemyMissiles && _missileSchedule[i] <= timeSinceLevelStart; ++i){
                noScheduledSoFar ++;
            }
            overdueCount = noScheduledSoFar - _noLaunchedEnemyMissiles;
            _noLaunchedEnemyMissiles = noScheduledSoFar;
            return overdueCount;
        }
        public boolean isFinished(){
            return _noLaunchedEnemyMissiles == _noEnemyMissiles;
        }
     }

    /* ---------------------------------------------------------------*
    *                         Ground Asset                            *
    * ----------------------------------------------------------------*/

    private enum GroundAssetState{visible, invisible}
    private abstract class GroundAsset implements Renderable<GroundAsset>{
        protected Point _position;
        protected PositionedPath _path;
        protected GroundAssetState _state = GroundAssetState.visible;

        private GroundAsset(Point position){
            if(position == null)
                throw new NullPointerException();
            _position = position;
        }

        @Override
        public GroundAsset render(Canvas canvas, float scale) {
            if(_state == GroundAssetState.visible)
                _path.render(canvas, scale);
            return this;
        }

        public void reset(){
            _state = GroundAssetState.visible;
        }

        public boolean testMissileHit(Missile missile){
            if(missile == null)
                throw new NullPointerException();

            if(missile.checkForCollision(_position) && _state == GroundAssetState.visible){
                missileHit();
                missile.detonate();
                return true;
            }
            return false;
        }
        public Point getPosition(){return _position;}
        protected abstract void missileHit();
        public GroundAssetState getState(){return _state;};
    }

    // Silo
    private class Silo extends GroundAsset{
        private Missile _missile;
        private SiloGroup _group;
        private boolean _reusable;

        public Silo(Point position, SiloGroup group, boolean reusable){
            super(position);
            _group = group;
            _reusable = reusable;
            _missile = new Missile(MissileType.player,false, _this);
            _path = new PositionedPath( buildPath(VisibleGameObject.silo), position, PaintBuilder.buildPaint(Paint.Style.FILL, Color.BLUE, 1));
        }

        public Missile getMissile(){return _missile;}

        @Override
        public void reset(){
            super.reset();
            _missile.cleanup();
        }
        public SiloGroup getGroup(){return _group;}

        public boolean tryLaunchMissile(Point destination, double speed){
            boolean wasSuccess = _state == GroundAssetState.visible && _missile.tryLaunch(_position, destination, speed);
            if(!_reusable && wasSuccess)
                _state = GroundAssetState.invisible;
            return wasSuccess;
        }

        protected void missileHit(){
            _state = GroundAssetState.invisible;
        }
    }

    private class City extends GroundAsset{
        public City(Point position){
            super(position);

            _path = new PositionedPath( buildPath(VisibleGameObject.city), position, PaintBuilder.buildPaint(Paint.Style.FILL, Color.rgb(0, 0xff, 0xff), 1));
        }

        protected void missileHit(){
            _state = GroundAssetState.invisible;
        }
    }

    /* --------------------------------------------------------------*
    *                         Missile                                *
    * ---------------------------------------------------------------*/
    public enum MissileState{ready, dead,launching,exploding}
    public enum MissileType{player, enemy}

    private class Missile implements Renderable<Missile>{
        private Point _destination;
        private Point _currentLocation;
        private Point _launchpadLocation;
        private MissileState _state;
        private MissileType _type;
        private Paint   _tailColor;
        private Line    _tail;
        private Circle _explosion;
        private double _speed;
        private double _launchTime;
        private double _explodeStartTime;
        private double _blastRadius;
        private double _explosionDuration;
        private double _launchAngle;
        private double _expectedArrivalTime;
        private int _frameNo;
        private boolean _relaunchable;
        private PositionedPath _crosshair;
        //private MediaPlayer _launchSoundPlayer;
        //private MediaPlayer _explosionSoundPlayer;
        private GameFieldView _caller;

        public boolean isReady() { return _state == MissileState.ready; }
        public boolean isExploding() { return _state == MissileState.exploding; }
        public boolean isLaunching() { return _state == MissileState.launching; }
        public boolean isDead() { return _state == MissileState.dead; }

        public void detonate()
        {
            if(_state != MissileState.launching)
                return;
            _explodeStartTime = _caller.getAdjustedTimeInMillis();
            _state = MissileState.exploding;
            //_launchSoundPlayer.Stop();
            //_explosionSoundPlayer.Play();
            _soundPool.play(_soundIdBoom, 1, 1, 10, 0, 1);
        }

        public boolean tryLaunch(Point launchpadLocation, Point destination, double speed)
        {
            if(launchpadLocation == null || destination == null)
                throw new NullPointerException();

            if (_state == MissileState.ready)
            {
                _launchpadLocation = launchpadLocation.clone();
                _destination = destination.clone();
                _crosshair = new PositionedPath(buildPath(VisibleGameObject.crosshair),_destination,PaintBuilder.buildPaint(Paint.Style.STROKE, Color.BLUE,1));
                if(_type == MissileType.player)
                    _soundPool.play(_soundIdLaunch, 1, 1, 10, 0, 1);
                //_launchSoundPlayer.Play();
                //_this.registerMissile(this);
                _state = MissileState.launching;
                _tail = new Line(launchpadLocation.clone(), launchpadLocation.clone(), _tailColor);
                _launchTime = _caller.getAdjustedTimeInMillis();
                _speed = speed;

                double distanceToTarget = calcDistance(_destination, _launchpadLocation);
                double expectedTravelTimeElapsed = distanceToTarget / (_speed / 1000);
                _expectedArrivalTime = _launchTime + expectedTravelTimeElapsed;

                if(_launchpadLocation.getX() >= _destination.getX())
                    _launchAngle = Math.atan((_launchpadLocation.getY() - _destination.getY())/(_launchpadLocation.getX() - _destination.getX()));
                else
                    _launchAngle = Math.atan((_launchpadLocation.getY() - _destination.getY()) / (_destination.getX() - _launchpadLocation.getX()));

                _explosion = new Circle(destination.clone(), PaintBuilder.buildPaint(Paint.Style.FILL, Color.WHITE, 1),1);
                return true;
            }
            return false;
        }

        public void timerCLick(double currentTimeInMillis) {
            if(_state != MissileState.dead)
                ++_frameNo;

            double currentTimeInSeconds = currentTimeInMillis / 1000;

            if(_state ==  MissileState.launching) {
                double launchTimeInSeconds = _launchTime / 1000;
                double distance = (currentTimeInSeconds - launchTimeInSeconds) * _speed;
                double currentY = _launchpadLocation.getY() - distance * Math.sin(_launchAngle);

                double mulp = ( _launchpadLocation.getX() < _destination.getX())? 1 : -1;
                double currentX = _launchpadLocation.getX() +  distance * mulp * Math.cos(_launchAngle);

                _currentLocation.setX((float)currentX).setY((float) currentY);// = new Point( (float)currentX, (float)currentY);

                _tail.setEndPos(_currentLocation.clone());
                _tail.setStartPos(_launchpadLocation.clone());

                _explosion.setPosition(_currentLocation);

                // determine if the missile has reached its destination
                if (currentTimeInMillis >= _expectedArrivalTime || calcDistance(_currentLocation, _destination) < 2) {
                    detonate();
                    return;
                }

            }
            if(_state == MissileState.exploding) {
                double millisSinceExplosion = currentTimeInMillis - _explodeStartTime;
                if(millisSinceExplosion > _explosionDuration * 1000) {
                    //_explosionSoundPlayer.Stop();
                    _state = (_relaunchable) ? MissileState.ready : MissileState.dead;
                    //_this.unRegisterMissile(this);
                    return;
                }

                _explosion.setRadius((float) Math.abs(_blastRadius * Math.sin(Math.PI * (millisSinceExplosion / (_explosionDuration * 1000)))));
                _explosion.setPosition(_currentLocation);
            }

            if(_state == MissileState.launching || _state == MissileState.exploding) {
                int currentExplosionColor = 0;
                int frameNoMod = _frameNo % 10;
                // white
                if (frameNoMod == 0 || frameNoMod == 4 || frameNoMod == 8)
                    currentExplosionColor = Color.WHITE;
                // red
                else if (frameNoMod == 1 || frameNoMod == 5 || frameNoMod == 9)
                    currentExplosionColor = Color.RED;
                // yellow
                else if (frameNoMod == 2 || frameNoMod == 6)
                    currentExplosionColor = Color.YELLOW;
                // orange
                else if (frameNoMod == 3 || frameNoMod == 7)
                    currentExplosionColor = Color.rgb(0xFF, 0xA5, 0x00);

                _explosion.getPaint().setColor(currentExplosionColor);
                _crosshair.getPaint().setColor(currentExplosionColor);
            }

        }
        public Circle getExplosion() { return _explosion; }
        public MissileType getType(){return _type;}
        public Point getLocation() { return _currentLocation; }
        public boolean checkForCollision(Point otherObjectLocation){
            if(otherObjectLocation == null)
                throw new NullPointerException();

            return _state == MissileState.exploding && calcDistance(_currentLocation, otherObjectLocation) <= _explosion.getRadius();
        }
        public Missile(MissileType type, boolean relaunchable, GameFieldView caller) {
            _caller = caller;
            _destination = new Point(0, 0);
            _currentLocation = new Point(0,0);
            _launchpadLocation = new Point(0, 0);
            _state = MissileState.ready;
            _type = type;
            _tailColor = PaintBuilder.buildPaint(Paint.Style.STROKE, (type == MissileType.enemy) ? Color.RED : Color.BLUE, 1);
            _speed = 0;
            _launchTime = 0.0;
            _explodeStartTime = 0.0;
            _blastRadius = 12.5;
            _explosionDuration = 2;
            _frameNo = 0;
            _relaunchable = relaunchable;

            //String currentPath = Environment.CurrentDirectory;

            //_launchSoundPlayer = new MediaPlayer();
            //_explosionSoundPlayer = new MediaPlayer();

            //_launchSoundPlayer.Open(new Uri(@currentPath + "\\sounds\\missile.wav"));
            //_explosionSoundPlayer.Open(new Uri(@currentPath + "\\sounds\\boom.wav")); ;
        }

        public void cleanup() {
            _state = MissileState.ready;
        }

        @Override
        public Missile render(Canvas canvas, float scale) {
            if(canvas == null)
                throw new NullPointerException();

            if(_state == MissileState.launching || _state == MissileState.exploding) {
                _tail.render(canvas, scale);
                _explosion.render(canvas, scale);
            }

            if(_state == MissileState.launching && _type == MissileType.player)
                _crosshair.render(canvas, scale);

            return this;
        }
    }
}
