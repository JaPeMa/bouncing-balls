package com.example.pelotas;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    List<Ball> collidedBalls;

    SensorManager sensorMgr;
    Sensor sensor;

    // Control de la velocitat
    float velocitat = 2.0f;
    float iniciX, iniciY;

    // Mides per fer càlculs
    int statusBar, width, height;

    List<Ball> balls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

// La bola

        balls = new ArrayList<>();
        Ball ballToInsert;
        ballToInsert = new Ball();

        ballToInsert.ball = findViewById(R.id.imageView);
        ballToInsert.xSpeed = 5f - (float) Math.random() * 10;
        ballToInsert.ySpeed = 5f - (float) Math.random() * 10;
        ballToInsert.id = 1;

        balls.add(ballToInsert);

        Ball ballToInsert2 = new Ball();

        ballToInsert2.ball = findViewById(R.id.imageView3);
        ballToInsert2.xSpeed = 5f - (float) Math.random() * 10;
        ballToInsert2.ySpeed = 5f - (float) Math.random() * 10;
        ballToInsert2.id = 2;

        balls.add(ballToInsert2);

// Obtenim les dimensions de la pantalla
        DisplayMetrics display = this.getBaseContext().getResources().getDisplayMetrics();
        width = display.widthPixels;
        height = display.heightPixels;


// Mida de l'statusBar per calcular l'alçada de l'aplicació
        statusBar = getResources().getDimensionPixelSize(getResources().getIdentifier("status_bar_height", "dimen", "android"));

// Inicialitzem a 0 les variables per controlar les pulsacions tàctils
        iniciX = iniciY = 0;

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                int index = 0;
                for (Ball ball : balls){
                    moveBall(ball.xSpeed, ball.ySpeed, ball, index);
                    index++;
                }
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 1000, 30);

        // Inicialitzem el sensor de l'acceleròmetre
        sensorMgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorMgr.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            //moveBall(event.values[1], event.values[0]);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void moveBall(float x, float y, Ball ball, int index) {

        float novaPosicioX = ball.ball.getX() + x * velocitat;
// Coordenada x

// Si el moviment és cap a la dreta
        if (x > 0) {

// Comprovem que no surti de les dimensions de la pantalla en assignar la nova posició
            if (novaPosicioX + ball.ball.getWidth() <= width) {
                ball.ball.setX(novaPosicioX);

            }
// Si en surt, establim la posició màxima en horitzontal perquè es pugui veure la imatge.
            else ball.xSpeed = ball.xSpeed * -1;
        }

// Fem el mateix pel moviment cap a l'esquerra
        else {
            if (novaPosicioX >= 0) {
                ball.ball.setX(novaPosicioX);

            } else ball.xSpeed = ball.xSpeed * -1;

        }

        float novaPosicioY = ball.ball.getY() + y * velocitat;

// El concepte és el mateix que a la X però hem de tenir en compte la barra d'estat

        if (y > 0)
            if (novaPosicioY + ball.ball.getHeight() + statusBar <= height) {
                ball.ball.setY(novaPosicioY);

            } else ball.ySpeed = ball.ySpeed * -1;
        else {
            if (novaPosicioY >= 0) {
                ball.ball.setY(novaPosicioY);

            } else ball.ySpeed = ball.ySpeed * -1;

        }

        //chocar con otras bolas
        for (int i = 0; i < balls.subList(index, balls.size()).size(); i++) {
            if (i != index && balls.subList(index, balls.size()).size() > 1) {
                Ball otherBall = balls.subList(index, balls.size()).get(i);

                //Puede que por las variables parezca igual, pero creeme que he tenido que hacer mucho en esto para que
                //funcione la colisión horizontal

                int xa1 = (int) ball.ball.getX();
                int xa2 = (int) ball.ball.getX() + ball.ball.getWidth();

                int xb1 = (int) otherBall.ball.getX();
                int xb2 = (int) otherBall.ball.getX() + otherBall.ball.getWidth();

                int ya1 = (int) ball.ball.getY();
                int ya2 = (int) ball.ball.getY() + ball.ball.getHeight();

                int yb1 = (int) otherBall.ball.getY();
                int yb2 = (int) otherBall.ball.getY() + otherBall.ball.getHeight();

                if(((xb1 - xa2) * (xb2 - xa1) <= 0) && ((yb1 - ya2)*(yb2- ya1) <= 0)) {

                    int verticalDistance = ya1 - yb1;
                    verticalDistance = verticalDistance < 0 ? verticalDistance * -1 : verticalDistance;

                    int acceptableToLateralCollision = (int)(ball.ball.getWidth()*(80/100.0f));



                    if(((xa1 >= xb1 && xa1 <= xb2) || (xb1 >= xa1 && xb1 <= xa2)) && verticalDistance >= acceptableToLateralCollision) {
                        ball.ySpeed = ball.ySpeed * -1;
                        otherBall.ySpeed = otherBall.ySpeed * -1;
                    }else{
                        ball.xSpeed = ball.xSpeed * -1;
                        otherBall.xSpeed = otherBall.xSpeed * -1;
                    }

                }

//                otherBall.ball.setX(width - otherBall.ball.getWidth());

            }
        }


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

// Registrem l'event al TextView
            iniciX = event.getX();
            iniciY = event.getY();


        } else if (event.getAction() == MotionEvent.ACTION_UP) {

// Registrem l'event al TextView
            float finalX = event.getX();
            float finalY = event.getY();

//Comrpovem si el moviment ha estat vertical
            if (Math.abs(finalX - iniciX) < Math.abs(finalY - iniciY)) {

// Establim el límit inferior en 0.5f
                if (finalY > iniciY) {
                    if (velocitat > 0.5)
                        velocitat -= 0.5f;
// Establim el límit superior en 5.0f
                } else {
                    if (velocitat < 5.0)
                        velocitat += 0.5f;
                }
                Toast.makeText(this, "Velocitat: " + String.valueOf(velocitat), Toast.LENGTH_SHORT).show();

            }
        }
        return super.onTouchEvent(event);
    }

}