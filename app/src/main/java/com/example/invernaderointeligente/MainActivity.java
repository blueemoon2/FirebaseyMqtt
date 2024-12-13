package com.example.invernaderointeligente;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;q
import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private DatabaseReference referenciaFirebase;
    private TextInputEditText etTemperatura, etHumedad, etLuz;
    private Button btnEnviar, btnMostrar, btnIrSegunda;
    private ListView lvConfiguraciones;
    private List<String> configuracionesList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Conectar a Firebase
        referenciaFirebase = FirebaseDatabase.getInstance().getReference("configuraciones");

        // Inicializar campos de texto
        etTemperatura = findViewById(R.id.etTemperatura);
        etHumedad = findViewById(R.id.etHumedad);
        etLuz = findViewById(R.id.etLuz);

        // Inicializar botones
        btnEnviar = findViewById(R.id.btnEnviar);
        btnMostrar = findViewById(R.id.btnMostrar);
        btnIrSegunda = findViewById(R.id.btnIrSegunda);

        // Inicializar ListView
        lvConfiguraciones = findViewById(R.id.lvConfiguraciones);
        configuracionesList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, configuracionesList);
        lvConfiguraciones.setAdapter(adapter);

        // Acción para el botón "Enviar Datos"
        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener los datos de los campos
                String temperatura = etTemperatura.getText().toString();
                String humedad = etHumedad.getText().toString();
                String luz = etLuz.getText().toString();

                if (!temperatura.isEmpty() && !humedad.isEmpty() && !luz.isEmpty()) {
                    // Crear un objeto con los datos
                    Configuracion configuracion = new Configuracion(temperatura, humedad, luz);

                    // Guardar los datos en Firebase
                    referenciaFirebase.push().setValue(configuracion)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("Firebase", "Datos enviados exitosamente.");
                                } else {
                                    Log.e("Firebase", "Error al enviar datos: ", task.getException());
                                }
                            });
                }
            }
        });

        // Acción para el botón "Mostrar Configuraciones"
        btnMostrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Leer datos de Firebase
                referenciaFirebase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        configuracionesList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Configuracion configuracion = snapshot.getValue(Configuracion.class);
                            if (configuracion != null) {
                                configuracionesList.add("Temp: " + configuracion.getTemperatura() + "°C, Hum: " + configuracion.getHumedad() + "%, Luz: " + configuracion.getLuz() + " lux");
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("Firebase", "Error al leer datos: ", databaseError.toException());
                    }
                });
            }
        });

        // Acción para el botón "MQTT"
        btnIrSegunda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, mensajemqtt.class);
                startActivity(intent);
            }
        });
    }

    // Clase interna para representar los datos de configuración
    public static class Configuracion {
        private String temperatura;
        private String humedad;
        private String luz;

        public Configuracion() {
            // Constructor vacío requerido por Firebase
        }

        public Configuracion(String temperatura, String humedad, String luz) {
            this.temperatura = temperatura;
            this.humedad = humedad;
            this.luz = luz;
        }

        public String getTemperatura() {
            return temperatura;
        }

        public String getHumedad() {
            return humedad;
        }

        public String getLuz() {
            return luz;
        }
    }
}
