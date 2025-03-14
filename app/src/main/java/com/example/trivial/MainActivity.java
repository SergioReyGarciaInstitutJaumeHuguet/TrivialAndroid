package com.example.trivial;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Encontrar los botones
        Button btnStartGame = findViewById(R.id.btnStartGame);
        Button btnAppInfo = findViewById(R.id.btnAppInfo);
        Button btnPreferences = findViewById(R.id.btnPreferences);

        // Configurar listener para Iniciar Juego
        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la actividad de juego
                Intent intent = new Intent(MainActivity.this, QuizActivity.class);
                startActivity(intent);
            }
        });

        // Configurar listener para Información de App
        btnAppInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la actividad de información
                Intent intent = new Intent(MainActivity.this, AppInfoActivity.class);
                startActivity(intent);
            }
        });

        // Configurar listener para Preferencias
        btnPreferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la actividad de preferencias
                Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
                startActivity(intent);
            }
        });
    }
}