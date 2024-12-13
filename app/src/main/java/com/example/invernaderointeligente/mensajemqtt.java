package com.example.invernaderointeligente;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class mensajemqtt extends AppCompatActivity {

    private static final String mqttHost = "tcp://invernaderointeligente.cloud.shiftr.io:1883"; // Cambiar si usas otro broker
    private static final String IdUsuario = "AppAndroid";
    private static final String Topico = "Mensaje";
    private static final String User = "invernaderointeligente"; // Usuario del broker (si aplica)
    private static final String Pass = "pQ1AFIB7Ay4aVgRf"; // Contraseña del broker (si aplica)

    private TextView textView;
    private EditText editTextMessage;
    private Button botonEnvio;

    private MqttClient mqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mensajemqtt);

        textView = findViewById(R.id.textView);
        editTextMessage = findViewById(R.id.txtMensaje);
        botonEnvio = findViewById(R.id.botonEnvioMensaje);

        try {
            // Inicializar cliente MQTT
            mqttClient = new MqttClient(mqttHost, IdUsuario, null);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(User);
            options.setPassword(Pass.toCharArray());

            // Conectar al servidor MQTT
            mqttClient.connect(options);
            Toast.makeText(this, "Aplicación conectada al servidor MQTT", Toast.LENGTH_SHORT).show();

            // Callback para eventos MQTT
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d("MQTT", "Conexión perdida");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());
                    runOnUiThread(() -> textView.append("\nRecibido: " + payload));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d("MQTT", "Entrega completa");
                }
            });

            // Suscribirse al tema
            mqttClient.subscribe(Topico, 1);

        } catch (MqttException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al conectar al servidor MQTT", Toast.LENGTH_SHORT).show();
        }

        // Acción del botón para enviar mensajes
        botonEnvio.setOnClickListener(view -> {
            String mensaje = editTextMessage.getText().toString();
            try {
                if (mqttClient != null && mqttClient.isConnected()) {
                    mqttClient.publish(Topico, mensaje.getBytes(), 0, false);
                    textView.append("\nEnviado: " + mensaje);
                    Toast.makeText(mensajemqtt.this, "Mensaje enviado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mensajemqtt.this, "Error: no se pudo enviar el mensaje. La conexión MQTT no está activa", Toast.LENGTH_SHORT).show();
                }
            } catch (MqttException e) {
                e.printStackTrace();
                Toast.makeText(mensajemqtt.this, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
