package michaelpeterson.lab8;

import android.view.View;
import android.widget.ImageView;

public class ChainableImageView{
    private ImageView _imageView;

    private ChainableImageView(ImageView thisView){
        _imageView = thisView;
    }

    public static ChainableImageView buildImageView(ImageView thisView){
        if(thisView == null)
            throw new NullPointerException();
        return new ChainableImageView(thisView);
    }

    public ChainableImageView setOnTouchListener(View.OnTouchListener listener){
        _imageView.setOnTouchListener(listener);
        return this;
    }

    public ChainableImageView setOnDragListener(View.OnDragListener listener){
        _imageView.setOnDragListener(listener);
        return this;
    }

    public ChainableImageView setTag(int key, Object tag){
        _imageView.setTag(key, tag);
        return this;
    }

    public Object getTag(int key){
        return _imageView.getTag(key);
    }

    public ChainableImageView setTagAndDrawable(int key, int drawableResourceId){
        _imageView.setTag(key, drawableResourceId);
        _imageView.setImageResource(drawableResourceId);
        return this;
    }

    public ImageView getView(){return _imageView;}
}
