package michaelpeterson.lab5;


import android.graphics.Matrix;

public class ChainableMatrix extends Matrix {
    private Matrix _thisMatrix;

    private ChainableMatrix(){_thisMatrix = new Matrix();}

    public static ChainableMatrix identity(){
        ChainableMatrix thisInstance = new ChainableMatrix();
        thisInstance._thisMatrix.setValues(new float[]{1,0,0,0,1,0,0,0,1});
        return thisInstance;
    }

    public Matrix getMatrix(){return _thisMatrix;}

    public ChainableMatrix chainScale(float scaleX, float scaleY){
        _thisMatrix.setScale(scaleX, scaleY);
        return this;
    }

    public ChainableMatrix chainTranslate(float translX, float translY){
        _thisMatrix.setTranslate(translX, translY);
        return this;
    }

    public ChainableMatrix chainRotate(float degrees, float px, float py){
        _thisMatrix.setRotate(degrees, px, py);
        return this;
    }
}
