package com.example.trivial;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.ColorStateList;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "QuizActivity";

    private TextView txtPregunta;
    private TextView txtScore;
    private Button btnRespuesta1;
    private Button btnRespuesta2;
    private Button btnRespuesta3;
    private Button btnRespuesta4;
    private Button btnSiguientePregunta;

    private List<Pregunta> listaPreguntas;
    private List<Pregunta> preguntasPendientes;
    private Pregunta preguntaActual;
    private int score = 0;
    private boolean preguntaRespondida = false;
    private Random random = new Random();
    private OkHttpClient client;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // Para música y sonidos
    private MediaPlayer backgroundMusic;
    private MediaPlayer correctSound;
    private MediaPlayer incorrectSound;
    private MediaPlayer buttonClickSound;

    // Preferencias de sonido
    private boolean backgroundMusicEnabled;
    private boolean soundEffectsEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Inicializar vistas
        txtPregunta = findViewById(R.id.txtPregunta);
        txtScore = findViewById(R.id.txtScore);
        btnRespuesta1 = findViewById(R.id.btnRespuesta1);
        btnRespuesta2 = findViewById(R.id.btnRespuesta2);
        btnRespuesta3 = findViewById(R.id.btnRespuesta3);
        btnRespuesta4 = findViewById(R.id.btnRespuesta4);
        btnSiguientePregunta = findViewById(R.id.btnSiguientePregunta);

        // Inicializar listas y cliente HTTP
        listaPreguntas = new ArrayList<>();
        preguntasPendientes = new ArrayList<>();
        client = new OkHttpClient();

        // Inicializar SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        // Obtener preferencias de sonido
        backgroundMusicEnabled = sharedPreferences.getBoolean("background_music_enabled", true);
        soundEffectsEnabled = sharedPreferences.getBoolean("sound_effects_enabled", true);

        // Inicializar MediaPlayers para sonidos
        initSounds();

        // Cargar preguntas automáticamente
        cargarPreguntas();

        // Configurar botón para siguiente pregunta
        btnSiguientePregunta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (soundEffectsEnabled) {
                    buttonClickSound.seekTo(0);
                    buttonClickSound.start();
                }

                if (!preguntasPendientes.isEmpty()) {
                    mostrarSiguientePregunta();
                } else {
                    Toast.makeText(QuizActivity.this, "No hay preguntas disponibles", Toast.LENGTH_SHORT).show();
                    mostrarSiguientePregunta();
                }
            }
        });

        // Configurar botones de respuesta
        configurarBotonesRespuesta();
    }

    private void initSounds() {
        // Inicializar sonidos
        backgroundMusic = MediaPlayer.create(this, R.raw.quiz_background);
        backgroundMusic.setLooping(true);

        correctSound = MediaPlayer.create(this, R.raw.correct_answer);
        incorrectSound = MediaPlayer.create(this, R.raw.incorrect_answer);
        buttonClickSound = MediaPlayer.create(this, R.raw.button_click);

        // Iniciar música de fondo si está habilitada
        if (backgroundMusicEnabled) {
            backgroundMusic.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Actualizar preferencias
        backgroundMusicEnabled = sharedPreferences.getBoolean("background_music_enabled", true);
        soundEffectsEnabled = sharedPreferences.getBoolean("sound_effects_enabled", true);

        // Control de música de fondo
        if (backgroundMusicEnabled && backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.start();
        } else if (!backgroundMusicEnabled && backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Pausar música si está activada
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Liberar recursos de MediaPlayer
        releaseMediaPlayers();
    }

    private void releaseMediaPlayers() {
        if (backgroundMusic != null) {
            backgroundMusic.release();
            backgroundMusic = null;
        }

        if (correctSound != null) {
            correctSound.release();
            correctSound = null;
        }

        if (incorrectSound != null) {
            incorrectSound.release();
            incorrectSound = null;
        }

        if (buttonClickSound != null) {
            buttonClickSound.release();
            buttonClickSound = null;
        }
    }

    private void configurarBotonesRespuesta() {
        View.OnClickListener respuestaListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Evitar múltiples clics en la misma pregunta
                if (preguntaRespondida) {
                    return;
                }

                preguntaRespondida = true;
                Button btnSeleccionado = (Button) v;
                String respuestaSeleccionada = btnSeleccionado.getText().toString();

                if (preguntaActual != null && respuestaSeleccionada.equals(preguntaActual.respuestaCorrecta)) {
                    // Respuesta correcta
                    score++;
                    txtScore.setText("Puntuación: " + score);

                    // Reproducir sonido de respuesta correcta
                    if (soundEffectsEnabled && correctSound != null) {
                        correctSound.seekTo(0);
                        correctSound.start();
                    }

                    // Cambiar color de fondo a verde
                    runOnUiThread(() -> btnSeleccionado.setBackgroundTintList(ContextCompat.getColorStateList(QuizActivity.this, R.color.colorCorrect)));

                    Toast.makeText(QuizActivity.this, "¡Correcto!", Toast.LENGTH_SHORT).show();

                    // Actualizar puntuación máxima en SharedPreferences
                    int maxScore = sharedPreferences.getInt("max_score", 0);
                    if (score > maxScore) {
                        editor.putInt("max_score", score);
                        editor.apply();
                        guardarPuntuacionEnServidor(score);
                    }
                } else {
                    // Respuesta incorrecta
                    // Reproducir sonido de respuesta incorrecta
                    if (soundEffectsEnabled && incorrectSound != null) {
                        incorrectSound.seekTo(0);
                        incorrectSound.start();
                    }

                    // Cambiar color de fondo a rojo
                    runOnUiThread(() -> btnSeleccionado.setBackgroundTintList(ContextCompat.getColorStateList(QuizActivity.this, R.color.colorIncorrect)));

                    // Mostrar cuál era la respuesta correcta
                    if (btnRespuesta1.getText().toString().equals(preguntaActual.respuestaCorrecta)) {
                        runOnUiThread(() -> btnRespuesta1.setBackgroundTintList(ContextCompat.getColorStateList(QuizActivity.this, R.color.colorCorrect)));
                    } else if (btnRespuesta2.getText().toString().equals(preguntaActual.respuestaCorrecta)) {
                        runOnUiThread(() -> btnRespuesta2.setBackgroundTintList(ContextCompat.getColorStateList(QuizActivity.this, R.color.colorCorrect)));
                    } else if (btnRespuesta3.getText().toString().equals(preguntaActual.respuestaCorrecta)) {
                        runOnUiThread(() -> btnRespuesta3.setBackgroundTintList(ContextCompat.getColorStateList(QuizActivity.this, R.color.colorCorrect)));
                    } else if (btnRespuesta4.getText().toString().equals(preguntaActual.respuestaCorrecta)) {
                        runOnUiThread(() -> btnRespuesta4.setBackgroundTintList(ContextCompat.getColorStateList(QuizActivity.this, R.color.colorCorrect)));
                    }

                    Toast.makeText(QuizActivity.this, "Incorrecto. La respuesta correcta es: " +
                            preguntaActual.respuestaCorrecta, Toast.LENGTH_SHORT).show();
                }

                // Habilitar botón para siguiente pregunta
                runOnUiThread(() -> btnSiguientePregunta.setEnabled(true));
            }
        };

        btnRespuesta1.setOnClickListener(respuestaListener);
        btnRespuesta2.setOnClickListener(respuestaListener);
        btnRespuesta3.setOnClickListener(respuestaListener);
        btnRespuesta4.setOnClickListener(respuestaListener);
    }

    private void cargarPreguntas() {
        // Obtener el número de preguntas configurado en preferencias
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final int questionCount = sharedPreferences.getInt("question_count", 5);

        // Modificar la URL para incluir el parámetro limit
        String url = "http://10.0.2.2/android/get.php?limit=" + questionCount;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(QuizActivity.this,
                            "Error al cargar preguntas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String jsonResponse = response.body().string();
                    procesarJSON(jsonResponse, questionCount);
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(QuizActivity.this,
                                "Error en la respuesta: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void procesarJSON(String jsonResponse, int questionCount) {
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            listaPreguntas.clear();
            preguntasPendientes.clear();

            // Cargar todas las preguntas disponibles
            List<Pregunta> todasLasPreguntas = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                Pregunta pregunta = new Pregunta();
                pregunta.id = obj.getString("id");
                pregunta.pregunta = obj.getString("pregunta");
                pregunta.respuestaCorrecta = obj.getString("respuestaCorrecta");
                pregunta.respuestasIncorrectas.add(obj.getString("respuestaIncorrecta1"));
                pregunta.respuestasIncorrectas.add(obj.getString("respuestaIncorrecta2"));
                pregunta.respuestasIncorrectas.add(obj.getString("respuestaIncorrecta3"));

                todasLasPreguntas.add(pregunta);
            }

            // Mezclar todas las preguntas
            Collections.shuffle(todasLasPreguntas);

            // Tomar solo el número configurado de preguntas
            int preguntasACargar = Math.min(questionCount, todasLasPreguntas.size());
            for (int i = 0; i < preguntasACargar; i++) {
                listaPreguntas.add(todasLasPreguntas.get(i));
                preguntasPendientes.add(todasLasPreguntas.get(i));
            }

            runOnUiThread(() -> {
                if (!preguntasPendientes.isEmpty()) {
                    Toast.makeText(QuizActivity.this,
                            "Se cargaron " + listaPreguntas.size() + " preguntas de " + todasLasPreguntas.size() + " disponibles", Toast.LENGTH_SHORT).show();
                    mostrarSiguientePregunta();
                } else {
                    Toast.makeText(QuizActivity.this,
                            "No hay preguntas disponibles", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                Toast.makeText(QuizActivity.this,
                        "Error al procesar JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void mostrarSiguientePregunta() {
        resetearBotonesRespuesta();

        if (preguntasPendientes.isEmpty()) {
            // Detenemos la música antes de ir a la siguiente actividad
            if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                backgroundMusic.pause();
            }

            // Mostrar la pantalla de resultados finales
            Intent intent = new Intent(QuizActivity.this, FinalActivity.class);
            intent.putExtra("score", score);
            intent.putExtra("totalQuestions", listaPreguntas.size());
            startActivity(intent);
            finish(); // Finalizar esta actividad para evitar que el usuario regrese al test
            return;
        }

        // Obtener una pregunta aleatoria de las pendientes
        int indiceAleatorio = random.nextInt(preguntasPendientes.size());
        preguntaActual = preguntasPendientes.get(indiceAleatorio);
        preguntasPendientes.remove(indiceAleatorio);

        // Mostrar la pregunta
        txtPregunta.setText(preguntaActual.pregunta);

        // Preparar todas las respuestas (correcta + incorrectas)
        List<String> todasLasRespuestas = new ArrayList<>();
        todasLasRespuestas.add(preguntaActual.respuestaCorrecta);
        todasLasRespuestas.addAll(preguntaActual.respuestasIncorrectas);

        // Mezclar las respuestas para mostrarlas en orden aleatorio
        Collections.shuffle(todasLasRespuestas);

        // Asignar respuestas a los botones
        btnRespuesta1.setText(todasLasRespuestas.get(0));
        btnRespuesta2.setText(todasLasRespuestas.get(1));
        btnRespuesta3.setText(todasLasRespuestas.get(2));
        btnRespuesta4.setText(todasLasRespuestas.get(3));

        // Desactivar el botón de siguiente pregunta hasta que se responda
        btnSiguientePregunta.setEnabled(false);
        preguntaRespondida = false;
    }


    private void resetearBotonesRespuesta() {
        // Crear un ColorStateList con el color #6854a4
        int color = Color.parseColor("#6854a4");
        ColorStateList colorStateList = ColorStateList.valueOf(color);

        // Aplicar el ColorStateList a todos los botones
        btnRespuesta1.setBackgroundTintList(colorStateList);
        btnRespuesta2.setBackgroundTintList(colorStateList);
        btnRespuesta3.setBackgroundTintList(colorStateList);
        btnRespuesta4.setBackgroundTintList(colorStateList);
    }

    private void guardarPuntuacionEnServidor(int puntuacion) {
        String url = "http://10.0.2.2/android/save.php";

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("puntos", String.valueOf(puntuacion))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(QuizActivity.this,
                            "Error al guardar puntuación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al guardar puntuación: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Respuesta del servidor: " + responseBody);

                    runOnUiThread(() -> {
                        Toast.makeText(QuizActivity.this,
                                "Puntuación guardada exitosamente", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(QuizActivity.this,
                                "Error al guardar puntuación: " + response.code(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error al guardar puntuación: " + response.code());
                    });
                }
            }
        });
    }

    // Clase interna para representar una pregunta
    private static class Pregunta {
        String id;
        String pregunta;
        String respuestaCorrecta;
        List<String> respuestasIncorrectas = new ArrayList<>();
    }
}