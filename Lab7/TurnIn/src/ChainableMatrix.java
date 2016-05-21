package michaelpeterson.lab7;


import android.graphics.Matrix;

public class ChainableMatrix extends Matrix {
    private ChainableMatrix(){
       super();
    }

    public static ChainableMatrix identity(){
        ChainableMatrix thisInstance = new ChainableMatrix();
        thisInstance.setValues(new float[]{1,0,0,0,1,0,0,0,1});
        return thisInstance;
    }

    public ChainableMatrix chainScale(float scaleX, float scaleY){
        setScale(scaleX, scaleY);
        return this;
    }

    public ChainableMatrix chainTranslate(float translX, float translY){
        setTranslate(translX, translY);
        return this;
    }

    public ChainableMatrix chainRotate(float degrees, float px, float py){
        setRotate(degrees, px, py);
        return this;
    }
}
