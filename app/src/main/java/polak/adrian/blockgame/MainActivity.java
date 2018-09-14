package polak.adrian.blockgame;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.util.Random;

public class MainActivity extends Activity {
    Arkanoid gameView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        gameView = new Arkanoid(this);
        setContentView(gameView);
    }

    // Wyjście z gry
    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    // powrót do gry
    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }

    public class Arkanoid extends SurfaceView implements Runnable{

        Thread game = null; //wątek gry
        SurfaceHolder holder;
        volatile boolean running; // czy gra aktualnie działa
        boolean paused = true;

        Canvas canvas;
        Paint paint;

        long fps;
        private long measuredFrame; //do obliczenia fps gry
        private int POINTS_PER_BLOCK = 10;
        int screenWidth;
        int screenHeight;

        Vaus paddle; //belka do odbijania piłeczki
        Bullet ball; //piłeczka

        ToneGenerator sounds = new ToneGenerator(AudioManager.STREAM_ALARM, 50);

        Alien[] bricks = new Alien[100];
        int numBricks = 0;


        int score = 0;
        int lives = 5;


        public Arkanoid(Context context) {
            super(context);

            holder = getHolder();
            paint = new Paint();

            Point size = new Point();
            Display display = getWindowManager().getDefaultDisplay();
            display.getSize(size);

            screenWidth = size.x;
            screenHeight = size.y;
            paddle = new Vaus(screenWidth, screenHeight);
            ball = new Bullet(screenWidth, screenHeight);

        }

        public void renderBlocks(){
            int brickWidth = screenWidth / 8;
            int brickHeight = screenHeight / 10;

            numBricks = 0;
            for (int column = 0; column < 8; column++){
                for(int row = 0; row < 3; row++){
                    bricks[numBricks] = new Alien(row, column, brickWidth, brickHeight);
                    numBricks++;
                }
            }
        }

        public void restart(){

            ball.reset(screenWidth, screenHeight);

            renderBlocks();

            if(lives == 0){
                score = 0;
                lives = 5;
            }
        }

        /*
        obsługa sterowania - wykrywanie dotyku ekranu
         */
        @Override
        public boolean onTouchEvent(MotionEvent event) {

            switch (event.getAction() & MotionEvent.ACTION_MASK) { //sprawdzamy typ interakcji gracza
                case MotionEvent.ACTION_DOWN: { //gracz dotknął ekranu
                    paused = false; //odpauzowujemy grę
                    if (event.getX() > screenWidth / 2) { // po prawej stronie
                        paddle.setMoveDirection(paddle.RIGHT);
                    } else { // po lewej stronie
                        paddle.setMoveDirection(paddle.LEFT);
                    }
                    break;
                }
                case MotionEvent.ACTION_UP: { // gracz puścił ekran
                    paddle.setMoveDirection(paddle.NONE);
                    break;
                }
            }
            return true;
        }

        @Override
        public void run() {
            while(running){
                long startFrameTime = System.currentTimeMillis();
                if(!paused){
                    update();
                }
                draw();
                measuredFrame = System.currentTimeMillis() - startFrameTime;
                if(measuredFrame >= 1){
                    fps = 1000 / measuredFrame;
                }
            }
        }

        public void checkCollisions(){
            if (RectF.intersects(paddle.getRect(), ball.getRect())) { // czy piłka odbita przez gracza
                ball.swapXAngle(); // zmiana kierunku (losowa)
                ball.reverseYVelocity(); //zmiana kierunku lotu piłki
                ball.repositionY(paddle.getRect().top - 2); //przesunięcie piłki bezpośrednio nad pozycję gracza - bez tego piłka 'przykleiłaby' się do belki
                                                            // tzn w kolejnych obiegach checkCollisions za każdym razem wpadałaby w ten blok instrukcji i tylko zmieniałaby kierunek lotu stojąc w miejscu
                sounds.startTone(ToneGenerator.TONE_DTMF_C, 200); //efekt dźwiękowy
            }

            for (int i = 0; i < numBricks; i++) {//sprawdzenie czy piłka właśnie uderza w którykolwiek z klocków
                if (bricks[i].isVisible()) {
                    if (RectF.intersects(bricks[i].getRect(), ball.getRect())) {
                        bricks[i].setVisible(false); //ukrywamy trafiony klocek
                        ball.reverseYVelocity(); //zmieniamy kierunek piłki
                        score = score + POINTS_PER_BLOCK; // 10 pkt za klocek
                        sounds.startTone(ToneGenerator.TONE_DTMF_A, 200); //efekt dźwiękowy
                    }
                }
            }

            if (ball.getRect().bottom > screenHeight) { //piłka dotknęła dolnej krawędzi ekranu
                ball.reverseYVelocity(); //zmiana kierunku piłki
                ball.repositionY(screenHeight - 2); //przesunięcie w głąb ekranu - żeby nie wpaść tu ponownie
                ball.reset(screenWidth,screenHeight);
                paused = true;
                paddle.ResetPosition(screenWidth,screenHeight);

                lives--;
                sounds.startTone(ToneGenerator.TONE_CDMA_ABBR_REORDER, 200);

                if (lives == 0) { //koniec gry
                    sounds.startTone(ToneGenerator.TONE_CDMA_ABBR_REORDER, 1500);
                    paused = true;
                    restart();
                }
            }

            if (ball.getRect().top < 0) { //piłka dotknęła górnej krawędzi ekranu
                ball.reverseYVelocity(); // zmiana kierunku
                ball.repositionY(12); //przesunięcie w głąb ekranu - żeby nie wpaść tu ponownie
                sounds.startTone(ToneGenerator.TONE_PROP_BEEP, 200);
            }

            if (ball.getRect().left < 0) { // lewa krawędź
                ball.reverseXVelocity(); // zmiana kierunku
                ball.repositionX(2); //przesunięcie w głąb ekranu - żeby nie wpaść tu ponownie
                sounds.startTone(ToneGenerator.TONE_PROP_BEEP, 200);
            }

            if (ball.getRect().right > screenWidth - 10) { //prawa krawędź
                ball.reverseXVelocity(); // zmiana kierunku

                ball.repositionX(screenWidth - 70); //przesunięcie w głąb ekranu - żeby nie wpaść tu ponownie
                sounds.startTone(ToneGenerator.TONE_PROP_BEEP, 200);
            }
        }

        public void update(){
            paddle.update(fps); // przeliczenie współrzędnych piłki
            ball.update(fps);  //p rzeliczenie współrzędnych belki
            checkCollisions(); // sprawdzanie kolizji
            if (score == numBricks * 10){ //sprawdzenie czy zbiliśmy wszystkie klocki
                paused = true;
                restart();
            }
        }

        public void draw(){ //wyrenderowanie grafiki w grze
            if (holder.getSurface().isValid()) {
                // Lock the canvas ready to draw
                canvas = holder.lockCanvas();

                // Draw the background color
                canvas.drawColor(Color.argb(255, 98, 187, 193));

                // Choose the brush color for drawing
                paint.setColor(Color.argb(255, 255, 255, 255));

                // Draw the paddle
                canvas.drawRect(paddle.getRect(), paint);

                // Draw the ball
                canvas.drawRect(ball.getRect(), paint);

                // Change the brush color for drawing
                paint.setColor(Color.argb(255, 249, 88, 183));

                for (int i = 0; i < numBricks; i++) { // rysowanie widocznych klocków
                    if (bricks[i].isVisible()) {
                        canvas.drawRect(bricks[i].getRect(), paint);
                    }
                }

                paint.setColor(Color.argb(255, 255, 255, 255));

                paint.setTextSize(40);
                canvas.drawText("Punkty: " + score + "   Życia: " + lives, 10, 50, paint);

                if (score == numBricks * 10) {
                    paint.setTextSize(90);
                    canvas.drawText("KLIKNIJ ABY ROZPOCZĄĆ", 10, screenHeight/ 2, paint);
                }

                if (lives <= 0) {
                    paint.setTextSize(90);
                    canvas.drawText("KONIEC GRY!", 10, screenHeight / 2, paint);
                }

                // Draw everything to the screen
                holder.unlockCanvasAndPost(canvas);
            }
        }


        public void pause() {
            running = false;
            try {
                game.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
            }
        }

        // uruchamiamy nowy wątek z grą
        public void resume() {
            running = true;
            game = new Thread(this);
            game.start();
        }

    }


}
