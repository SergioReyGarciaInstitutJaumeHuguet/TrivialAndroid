package com.example.trivial;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    // Detector de gestos
    private GestureDetectorCompat mDetector;

    // MediaPlayer para efectos de sonido
    private MediaPlayer mpButtonClick;

    // Preferencias
    private SharedPreferences sharedPreferences;
    private boolean soundEffectsEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar detector de gestos
        mDetector = new GestureDetectorCompat(this, this);

        // Inicializar preferencias
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        soundEffectsEnabled = sharedPreferences.getBoolean("sound_effects_enabled", true);

        // Inicializar MediaPlayer para efectos de sonido
        mpButtonClick = MediaPlayer.create(this, R.raw.button_click);

        // Encontrar los botones
        Button btnStartGame = findViewById(R.id.btnStartGame);
        Button btnAppInfo = findViewById(R.id.btnAppInfo);
        Button btnPreferences = findViewById(R.id.btnPreferences);

        // Configurar listener para Iniciar Juego
        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reproducir efecto de sonido si está habilitado
                if (soundEffectsEnabled) {
                    playButtonSound();
                }

                // Navegar a la actividad de juego
                Intent intent = new Intent(MainActivity.this, QuizActivity.class);
                startActivity(intent);
            }
        });

        // Configurar listener para Información de App
        btnAppInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reproducir efecto de sonido si está habilitado
                if (soundEffectsEnabled) {
                    playButtonSound();
                }

                // Navegar a la actividad de información
                Intent intent = new Intent(MainActivity.this, AppInfoActivity.class);
                startActivity(intent);
            }
        });

        // Configurar listener para Preferencias
        btnPreferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reproducir efecto de sonido si está habilitado
                if (soundEffectsEnabled) {
                    playButtonSound();
                }

                // Navegar a la actividad de preferencias
                Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualizar preferencias
        soundEffectsEnabled = sharedPreferences.getBoolean("sound_effects_enabled", true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberar recursos del MediaPlayer
        if (mpButtonClick != null) {
            mpButtonClick.release();
            mpButtonClick = null;
        }
    }

    // Método para reproducir sonido de botón
    private void playButtonSound() {
        if (mpButtonClick != null) {
            // Reiniciar el sonido antes de reproducirlo
            mpButtonClick.seekTo(0);
            mpButtonClick.start();
        }
    }

    // Implementación de métodos de GestureDetector.OnGestureListener
    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // No implementado
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // No implementado
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // Detectar deslizamiento horizontal
        float diffX = e2.getX() - e1.getX();
        float diffY = e2.getY() - e1.getY();

        // Si el deslizamiento es más horizontal que vertical
        if (Math.abs(diffX) > Math.abs(diffY)) {
            // Deslizamiento hacia la derecha - Iniciar juego
            if (diffX > 0) {
                if (soundEffectsEnabled) {
                    playButtonSound();
                }
                Intent intent = new Intent(MainActivity.this, QuizActivity.class);
                startActivity(intent);
                return true;
            }
            // Deslizamiento hacia la izquierda - Información de la app
            else {
                if (soundEffectsEnabled) {
                    playButtonSound();
                }
                Intent intent = new Intent(MainActivity.this, AppInfoActivity.class);
                startActivity(intent);
                return true;
            }
        }
        // Si el deslizamiento es más vertical que horizontal
        else {
            // Deslizamiento hacia arriba - Preferencias
            if (diffY < 0) {
                if (soundEffectsEnabled) {
                    playButtonSound();
                }
                Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
                startActivity(intent);
                return true;
            }
        }
        return false;
    }

    // Interceptar eventos de tacto y pasarlos al detector de gestos
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.mDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }
}