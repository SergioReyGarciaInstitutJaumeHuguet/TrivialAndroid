package com.example.trivial;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Switch;
import android.widget.CompoundButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PreferencesActivity extends AppCompatActivity {
    private static final String TAG = "PreferencesActivity";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private OkHttpClient client;

    private SeekBar seekBarQuestions;
    private TextView txtQuestionCount;
    private TextView txtMaxScore;
    private Button btnResetMaxScore;
    private Button btnResetPreferences;

    // Controles para las preferencias de sonido
    private Switch switchBackgroundMusic;
    private Switch switchSoundEffects;

    // MediaPlayer para efectos de sonido en botones
    private MediaPlayer buttonClickSound;

    // Estado de reproducción
    private boolean soundEffectsEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        // Inicializar SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        client = new OkHttpClient();

        // Cargar preferencias de sonido
        soundEffectsEnabled = sharedPreferences.getBoolean("sound_effects_enabled", true);

        // Inicializar MediaPlayer para efectos de sonido
        buttonClickSound = MediaPlayer.create(this, R.raw.button_click);

        // Inicializar vistas
        seekBarQuestions = findViewById(R.id.seekBarQuestions);
        txtQuestionCount = findViewById(R.id.txtQuestionCount);
        txtMaxScore = findViewById(R.id.txtMaxScore);
        btnResetMaxScore = findViewById(R.id.btnResetMaxScore);
        btnResetPreferences = findViewById(R.id.btnResetPreferences);

        // Inicializar controles de sonido
        switchBackgroundMusic = findViewById(R.id.switchBackgroundMusic);
        switchSoundEffects = findViewById(R.id.switchSoundEffects);

        // Cargar preferencias existentes
        loadPreferences();

        // Configurar SeekBar para número de preguntas
        seekBarQuestions.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int questionCount = Math.max(5, Math.min(progress, 20)); // Ahora el mínimo es 5 y el máximo es 20

                seekBar.setProgress(questionCount); // Asegurar que nunca sea menor a 5 ni mayor a 20
                txtQuestionCount.setText("Preguntas actuales: " + questionCount);

                editor.putInt("question_count", questionCount);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Botón para reiniciar puntuación máxima
        btnResetMaxScore.setOnClickListener(v -> {
            if (soundEffectsEnabled && buttonClickSound != null) {
                buttonClickSound.seekTo(0);
                buttonClickSound.start();
            }

            editor.putInt("max_score", 0);
            editor.apply();
            resetScoreInServer();
            loadPreferences();
        });

        // Botón para restaurar valores predeterminados
        btnResetPreferences.setOnClickListener(v -> {
            if (soundEffectsEnabled && buttonClickSound != null) {
                buttonClickSound.seekTo(0);
                buttonClickSound.start();
            }

            editor.putInt("question_count", 5);
            editor.putInt("max_score", 0);
            editor.putBoolean("background_music_enabled", true);
            editor.putBoolean("sound_effects_enabled", true);
            editor.apply();
            resetScoreInServer();
            loadPreferences();

            // Actualizar switches
            switchBackgroundMusic.setChecked(true);
            switchSoundEffects.setChecked(true);
        });

        // Configurar switch para música de fondo
        switchBackgroundMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("background_music_enabled", isChecked);
                editor.apply();

                if (soundEffectsEnabled && buttonClickSound != null) {
                    buttonClickSound.seekTo(0);
                    buttonClickSound.start();
                }
            }
        });

        // Configurar switch para efectos de sonido
        switchSoundEffects.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("sound_effects_enabled", isChecked);
                editor.apply();
                soundEffectsEnabled = isChecked;

                // Si se están habilitando los efectos de sonido, reproducir un sonido
                if (isChecked && buttonClickSound != null) {
                    buttonClickSound.seekTo(0);
                    buttonClickSound.start();
                }
            }
        });
    }

    private void loadPreferences() {
        // Cargar número de preguntas
        int questionCount = sharedPreferences.getInt("question_count", 5);
        seekBarQuestions.setProgress(questionCount);
        txtQuestionCount.setText("Preguntas actuales: " + questionCount);

        // Cargar puntuación máxima
        int maxScore = sharedPreferences.getInt("max_score", 0);
        txtMaxScore.setText("Puntuación máxima actual: " + maxScore);

        // Cargar preferencias de sonido
        boolean backgroundMusicEnabled = sharedPreferences.getBoolean("background_music_enabled", true);
        boolean soundEffectsEnabled = sharedPreferences.getBoolean("sound_effects_enabled", true);

        // Configurar switches según preferencias
        switchBackgroundMusic.setChecked(backgroundMusicEnabled);
        switchSoundEffects.setChecked(soundEffectsEnabled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberar recursos del MediaPlayer
        if (buttonClickSound != null) {
            buttonClickSound.release();
            buttonClickSound = null;
        }
    }

    // Método para reiniciar la puntuación en el servidor
    private void resetScoreInServer() {
        String url = "http://10.0.2.2/android/reset_score.php";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(PreferencesActivity.this,
                            "Error al reiniciar puntuación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al reiniciar puntuación: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(PreferencesActivity.this,
                                "Puntuación reiniciada correctamente", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Puntuación reiniciada correctamente");
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(PreferencesActivity.this,
                                "Error al reiniciar puntuación: " + response.code(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error al reiniciar puntuación: " + response.code());
                    });
                }
            }
        });
    }
}