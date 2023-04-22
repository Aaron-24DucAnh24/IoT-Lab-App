package aaron.iot.iot_lab_app;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;
import java.util.concurrent.ThreadLocalRandom;


public class MQTTHelper {
    public MqttAndroidClient mqttAndroidClient;

    public final String[] Topics = {
            "aaron_24/feeds/button1", "aaron_24/feeds/button2", "aaron_24/feeds/button3",
            "aaron_24/feeds/sensor1", "aaron_24/feeds/sensor2", "aaron_24/feeds/sensor3",
            "aaron_24/feeds/frequency", "aaron_24/feeds/uart-frequency",
            "aaron_24/feeds/connection", "aaron_24/feeds/ai"
    };
    final String clientId = "aaron_24";
    final String username = "aaron_24";
    final String password = "";
    final String serverUri = "tcp://io.adafruit.com:1883";

    public MQTTHelper(Context context){
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt", s);
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Mqtt", mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        connect();
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    public MqttMessage getMqttMessage(@NonNull String value) {
        MqttMessage msg = new MqttMessage();
        msg.setId(ThreadLocalRandom.current().nextInt(0, 10000 + 1));
        msg.setQos(0);
        msg.setRetained(true);
        byte[] b = value.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);
        return msg;
    }

    public void publish(String topic, String value) {
        try {
            mqttAndroidClient.publish(topic, getMqttMessage(value));
        }
        catch (MqttException err) {
            System.err.println("Exception get first value");
            err.printStackTrace();
        }
    }

    private void connect(){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    Log.d("Mqtt", "Successfully");
                    subscribeToTopic();
                    getFirstValue();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connect to: " + serverUri + exception.toString());
                }
            });
        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    private void getFirstValue() {
        for(int i = 0; i < Topics.length; i++) {
            try {
                MqttMessage message = new MqttMessage();
                MqttMessage msg = new MqttMessage();
                msg.setId(ThreadLocalRandom.current().nextInt(0, 10000 + 1));
                msg.setQos(0);
                msg.setRetained(false);
                mqttAndroidClient.publish(Topics[i] + "/get", message);
            }
            catch (MqttException ex) {
                System.err.println("Exception get first value");
                ex.printStackTrace();
            }
        }
    }

    private void subscribeToTopic() {
        for(int i = 0; i < Topics.length; i++) {
            try {
                mqttAndroidClient.subscribe(Topics[i], 0, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d("Subscribe", "Successfully");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.d("Subscribe", "Fail");
                    }
                });

            } catch (MqttException ex) {
                System.err.println("Exception subscribing");
                ex.printStackTrace();
            }
        }
    }

}