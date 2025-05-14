package com.example.trivial;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class AppInfoActivity extends AppCompatActivity {

    private MediaPlayer backgroundMusic;
    private boolean backgroundMusicEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);

        // Obtener preferencias de música
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        backgroundMusicEnabled = sharedPreferences.getBoolean("background_music_enabled", true);

        // Inicializar música de fondo
        backgroundMusic = MediaPlayer.create(this, R.raw.info_background);
        backgroundMusic.setLooping(true);

        // Iniciar música si está habilitada
        if (backgroundMusicEnabled) {
            backgroundMusic.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Obtener preferencias actualizadas
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        backgroundMusicEnabled = sharedPreferences.getBoolean("background_music_enabled", true);

        // Controlar la reproducción de música
        if (backgroundMusicEnabled && backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.start();
        } else if (!backgroundMusicEnabled && backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Pausar música cuando la actividad no está visible
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Liberar recursos de MediaPlayer
        if (backgroundMusic != null) {
            backgroundMusic.release();
            backgroundMusic = null;
        }
    }
}