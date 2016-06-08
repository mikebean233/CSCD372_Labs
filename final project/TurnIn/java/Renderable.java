package michaelpeterson.finalproject;

import android.graphics.Canvas;

public interface Renderable<T> {
    T render(Canvas canvas, float scale);
}
