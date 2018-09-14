package polak.adrian.blockgame;

import android.graphics.RectF;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
/**
 * Created by adrian on 11.09.2018.
 */

public class Bullet {
    RectF rect;
    float xVelocity;
    float yVelocity;
    float bulletWidth = 20;
    float bulletHeight = 20;

    public Bullet(int screenX, int screenY) {
        xVelocity = 200; //początkowy kierunek piłeczki - na ukos w prawo do góry
        yVelocity = -400; //ujemne bo punkt 0,0 jest w lewym górnym rogu ekranu
        rect = new RectF();
    }

    public RectF getRect() {
        return rect;
    }
    /*
     przeliczenie współrzędnych piłki na podstawie prędkości i aktualnej klatki
      */
    public void update(long fps){
        rect.left = rect.left + (xVelocity / fps);
        rect.top = rect.top +(yVelocity / fps);
        rect.right = rect.left + bulletWidth;
        rect.bottom = rect.top + bulletHeight;
    }

    /*
    odbicie piłki góra/dół
     */
    public void reverseYVelocity(){
        yVelocity = -yVelocity;
    }

    /*
     odbicie piłki lewo/prawo
     */
    public void reverseXVelocity(){
        xVelocity = -xVelocity;
    }

    /*
     losowość odbicia lewo/prawo po kontakcie z obiektem Vaus
     ( kontakt ze ścianą boczną ZAWSZE zmienia kierunek! )
     */
    public void swapXAngle(){
        Random generator = new Random();
        int condition = generator.nextInt(2);
        if(condition == 0){
            reverseXVelocity();
        }
    }

    public void repositionY(float y){ //korekta pozycji piłki
        rect.bottom = y;
        rect.top = y - bulletHeight;
    }

    public void repositionX(float x){ //korekta pozycji piłki
        rect.left = x;
        rect.right = x + bulletWidth;
    }
    /*
        wyzerowanie pozycji piłki
     */
    public void reset(int x, int y){
        rect.left = x / 2 -10;
        rect.right = x / 2 + 10;
        rect.top = y - 40;
        rect.bottom = y - bulletHeight;
    }
}
