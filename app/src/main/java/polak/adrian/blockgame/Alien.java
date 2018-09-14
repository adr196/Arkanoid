package polak.adrian.blockgame;

import android.graphics.RectF;

/**
 * Created by adrian on 11.09.2018.
 */

public class Alien {

    private RectF rect;
    private boolean isVisible;
    private int padding = 2;

    public Alien(int x, int y, int width, int height) {
        isVisible = true;
        rect = new RectF(
                y * width + padding,
                x * height + padding,
                y * width + width - padding,
                x * height + height - padding
        );
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public RectF getRect() {
        return rect;
    }
}
