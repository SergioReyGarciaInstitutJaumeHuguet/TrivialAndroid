package com.example.trivial;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class FinalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final);

        // Obtener datos de la puntuación
        int score = getIntent().getIntExtra("score", 0);
        int totalQuestions = getIntent().getIntExtra("totalQuestions", 0);

        // Mostrar la puntuación final
        TextView txtFinalScore = findViewById(R.id.txtFinalScore);
        txtFinalScore.setText("Has terminado con " + score + "/" + totalQuestions + " puntos");

        // Botón para volver al menú
        Button btnVolverMenu = findViewById(R.id.btnVolverMenu);
        btnVolverMenu.setOnClickListener(v -> {
            Intent intent = new Intent(FinalActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Cerrar esta actividad
        });
    }
}
