package com.example.trivial;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class FinalActivity extends AppCompatActivity {

    private MediaPlayer victorySound;
    private MediaPlayer defeatSound;
    private boolean soundEffectsEnabled;

    private TextView txtFinalScore;
    private TextView txtResultMessage;
    private ImageView imgResult;
    private Button btnVolverMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final);

        // Inicializar vistas
        txtFinalScore = findViewById(R.id.txtFinalScore);
        txtResultMessage = findViewById(R.id.txtResultMessage);
        imgResult = findViewById(R.id.imgResult);
        btnVolverMenu = findViewById(R.id.btnVolverMenu);

        // Obtener preferencias de sonido
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        soundEffectsEnabled = sharedPreferences.getBoolean("sound_effects_enabled", true);

        // Inicializar sonidos
        victorySound = MediaPlayer.create(this, R.raw.victory_fanfare);
        defeatSound = MediaPlayer.create(this, R.raw.defeat_sound);

        // Obtener datos de la puntuación
        int score = getIntent().getIntExtra("score", 0);
        int totalQuestions = getIntent().getIntExtra("totalQuestions", 0);

        // Calcular si es victoria o derrota
        boolean isVictory = score >= Math.ceil(totalQuestions / 2.0);

        // Mostrar la puntuación final
        txtFinalScore.setText("Has terminado con " + score + "/" + totalQuestions + " puntos");

        // Configurar la pantalla según el resultado
        if (isVictory) {
            // Victoria
            txtResultMessage.setText("¡VICTORIA!");
            imgResult.setImageResource(R.drawable.victory_image);

            // Reproducir sonido de victoria si está habilitado
            if (soundEffectsEnabled && victorySound != null) {
                victorySound.start();
            }
        } else {
            // Derrota
            txtResultMessage.setText("¡HAS PERDIDO!");
            imgResult.setImageResource(R.drawable.defeat_image);

            // Reproducir sonido de derrota si está habilitado
            if (soundEffectsEnabled && defeatSound != null) {
                defeatSound.start();
            }
        }

        // Botón para volver al menú
        btnVolverMenu.setOnClickListener(v -> {
            // Reproducir sonido de clic si está habilitado
            if (soundEffectsEnabled) {
                MediaPlayer buttonClickSound = MediaPlayer.create(this, R.raw.button_click);
                buttonClickSound.start();
                buttonClickSound.setOnCompletionListener(mp -> mp.release());
            }

            Intent intent = new Intent(FinalActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Cerrar esta actividad
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberar recursos de MediaPlayer
        if (victorySound != null) {
            victorySound.release();
            victorySound = null;
        }
        if (defeatSound != null) {
            defeatSound.release();
            defeatSound = null;
        }
    }
}