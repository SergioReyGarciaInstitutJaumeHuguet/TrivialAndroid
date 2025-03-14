package com.example.trivial;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        // Inicializar SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        client = new OkHttpClient();

        // Inicializar vistas
        seekBarQuestions = findViewById(R.id.seekBarQuestions);
        txtQuestionCount = findViewById(R.id.txtQuestionCount);
        txtMaxScore = findViewById(R.id.txtMaxScore);
        btnResetMaxScore = findViewById(R.id.btnResetMaxScore);
        btnResetPreferences = findViewById(R.id.btnResetPreferences);

        // Cargar preferencias existentes
        loadPreferences();

        // Configurar SeekBar para número de preguntas
        seekBarQuestions.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Asegurar que el mínimo sea 2
                int questionCount = Math.max(2, progress);

                // Si el progreso es menor que 2, forzar a 2
                if (progress < 2) {
                    seekBar.setProgress(2);
                }

                txtQuestionCount.setText("Preguntas actuales: " + questionCount);

                // Guardar número de preguntas
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
            editor.putInt("max_score", 0);
            editor.apply();
            resetScoreInServer();
            loadPreferences();
        });

        // Botón para restaurar valores predeterminados
        btnResetPreferences.setOnClickListener(v -> {
            editor.putInt("question_count", 5);
            editor.putInt("max_score", 0);
            editor.apply();
            resetScoreInServer();
            loadPreferences();
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