package polak.adrian.blockgame;

import android.graphics.RectF;

/**
 * Created by adrian on 11.09.2018.
 */

public class Vaus {
    private RectF rect;

    private float length;
    private float height;

    private float x;
    private float y;

    private float speed;

    private float leftBorder;
    private float rightBorder;
    //kierunki ruchu
    public final int NONE = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    private int moveDirection = NONE;

    public Vaus(int screenX, int screenY){
        length = 130;
        height = 20;
        leftBorder = 0;
        rightBorder = screenX;
        x = screenX / 2 -65;
        y = screenY - height;
        rect = new RectF(x -65, y, x + 65, y + height);
        speed = 700;
    }

    public RectF getRect(){

        return rect;
    }

    public void setMoveDirection(int state){
        moveDirection = state;
    }

    public void update(long fps){
        if(moveDirection == RIGHT){
            x = x + speed / fps;
        }
        if(moveDirection == LEFT){
            x = x - speed / fps;
        }
        if(x >= leftBorder && (x + length) <= rightBorder){ //sprawdzenie czy nie wyjedziemy poza ekran
            rect.left = x;
            rect.right = x + length;
        }
    }
    public void ResetPosition(int screenWidth, int screenHeight){
        rect.left = screenWidth / 2 -65;
        rect.right = screenWidth / 2 + 65;
        rect.top = screenHeight -20;
        rect.bottom = screenHeight;
    }


}
