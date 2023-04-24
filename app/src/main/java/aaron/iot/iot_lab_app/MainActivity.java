package aaron.iot.iot_lab_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.w3c.dom.Text;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    MQTTHelper mqttHelper;
    Fragment sensors = new sensors();
    Fragment logs = new logs();
    Fragment devices = new devices();
    IoTModel model = new IoTModel();
    Retrofit retrofit;
    apiHolder api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide appNameBar
        getSupportActionBar().hide();

        startMQTT();

        // setup http client
        retrofit = new Retrofit.Builder()
                .baseUrl("my-url-to-http-server")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(apiHolder.class);

        // setup layouts
        setContentView(R.layout.activity_main);
        replaceFragment(new login());
        getBounds();

        // setup navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener((item)->{
                    navigator(item.getItemId());
                    return true;
                }
        );
    }

    public void navigator(int id) {
            switch (id) {
                case R.id.sensors_item:
                    replaceFragment(sensors);
                    loadSensorLayout();
                    break;
                case R.id.devices_item:
                    replaceFragment(devices);
                    loadDevicesLayout();
                    break;
                case R.id.logs_item:
                    replaceFragment(logs);
                    loadSettingsLayout();
                    break;
            }
    }

    public void handleSwitchLayout(@NonNull Boolean isChecked) {
        Switch pump = findViewById(R.id.pump_btn);
        Switch fan = findViewById(R.id.fan_btn);
        Switch light = findViewById(R.id.light_btn);

        if (isChecked){
            pump.setClickable(false);
            pump.setChecked(false);
            pump.setTrackDrawable(getDrawable(R.drawable.disable_track_switch));
            pump.setThumbDrawable(getDrawable(R.drawable.disable_switch_thumb));

            fan.setClickable(false);
            fan.setChecked(false);
            fan.setTrackDrawable(getDrawable(R.drawable.disable_track_switch));
            fan.setThumbDrawable(getDrawable(R.drawable.disable_switch_thumb));

            light.setClickable(false);
            light.setChecked(false);
            light.setTrackDrawable(getDrawable(R.drawable.disable_track_switch));
            light.setThumbDrawable(getDrawable(R.drawable.disable_switch_thumb));
        } else {
            pump.setClickable(true);
            pump.setTrackDrawable(getDrawable(R.drawable.custom_track));
            pump.setThumbDrawable(getDrawable(R.drawable.custom_switch));

            fan.setClickable(true);
            fan.setTrackDrawable(getDrawable(R.drawable.custom_track));
            fan.setThumbDrawable(getDrawable(R.drawable.custom_switch));

            light.setClickable(true);
            light.setTrackDrawable(getDrawable(R.drawable.custom_track));
            light.setThumbDrawable(getDrawable(R.drawable.custom_switch));
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.work_space, fragment);
        fragmentTransaction.commitNowAllowingStateLoss();
    }

    public void handleLogin (View view) {
        EditText password = findViewById(R.id.password);
        EditText username = findViewById(R.id.username);

        String passwordText = password.getText().toString();
        String usernameText = username.getText().toString();

        if(!(passwordText.equals("") || usernameText.equals(""))) {
            replaceFragment(sensors);
            loadSensorLayout();

            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
            CircleImageView avatar = findViewById(R.id.avatar);

            avatar.setVisibility(View.VISIBLE);
            bottomNavigationView.setVisibility(View.VISIBLE);
        }

    }

    public void handelLogout(View view) {
        CircleImageView avatar = findViewById(R.id.avatar);
        AppCompatButton logoutBtn = findViewById(R.id.logout_btn);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.getMenu().getItem(0).setChecked(true);
        bottomNavigationView.setVisibility(View.INVISIBLE);
        logoutBtn.setVisibility(View.INVISIBLE);
        avatar.setVisibility(View.INVISIBLE);

        replaceFragment(new login());
    }

    public void setLogoutBtnVisibility(View view) {
        AppCompatButton logoutBtn = findViewById(R.id.logout_btn);
        if(logoutBtn.getVisibility()==View.VISIBLE){
            logoutBtn.setVisibility(View.INVISIBLE);
        }
        else {
            logoutBtn.setVisibility(View.VISIBLE);
        }
    }

    public void updateSensorsLayout(String topic, String value) {
        if(findViewById(R.id.humi_data) != null) {
            if(topic.contains("sensor1")) {
                TextView txt = findViewById(R.id.humi_data);
                txt.setText(value);
            } else if(topic.contains("sensor2")) {
                TextView txt = findViewById(R.id.temp_data);
                txt.setText(value);
            } else if(topic.contains("sensor3")) {
                TextView txt = findViewById(R.id.light_data);
                txt.setText(value);
            } else if(topic.contains("ai")) {
                TextView ai = findViewById(R.id.ai_view);
                ai.setText(value);
            }
        }
    }

    public void loadSensorLayout() {
        TextView humi = findViewById(R.id.humi_data);
        TextView temp = findViewById(R.id.temp_data);
        TextView light = findViewById(R.id.light_data);
        TextView ai = findViewById(R.id.ai_view);
        humi.setText(model.humi);
        light.setText(model.light);
        temp.setText(model.temp);
        ai.setText(model.ai_value);
    }

    public void updateFreLayout(String topic, String value) {
        if(findViewById(R.id.gateway_fre)!=null && model.conn) {
            if(topic.contains("uart")){
                EditText uart_fre = findViewById(R.id.uart_fre);
                uart_fre.setHint(value + " seconds");
            } else {
                EditText gateway_fre = findViewById(R.id.gateway_fre);
                gateway_fre.setHint(value + " seconds");
            }
        }
    }

    public void loadSettingsLayout() {
        EditText gateway_fre = findViewById(R.id.gateway_fre);
        EditText uart = findViewById(R.id.uart_fre);
        EditText min_hu = findViewById(R.id.min_humi);
        EditText min_light = findViewById(R.id.min_light);
        EditText max_temp = findViewById(R.id.max_temp);

        min_hu.setText("");
        min_light.setText("");
        max_temp.setText("");

        gateway_fre.setHint(model.gateway_fre + " seconds");
        uart.setHint(model.uart_fre + " seconds");
        min_hu.setHint(model.min_humi + "%");
        min_light.setHint(model.min_light + " lux");
        max_temp.setHint(model.max_temp + "°C");
    }

    public void updateDevicesLayout(String topic, String value) {
        if(findViewById(R.id.fan_btn) != null && model.conn) {

            Boolean checked = value.equals("1");

            if(topic.contains("button1")) {
                Switch s = findViewById(R.id.pump_btn);
                s.setChecked(checked);
            } else if(topic.contains("button2")) {
                Switch s = findViewById(R.id.fan_btn);
                s.setChecked(checked);
            } else if(topic.contains("button3")) {
                Switch s = findViewById(R.id.light_btn);
                s.setChecked(checked);
             } else if(topic.contains("auto")) {
                Switch s = findViewById(R.id.auto_btn);
                s.setChecked(checked);
            }
        }
        if(topic.contains("connection")) {
            TextView connection = findViewById(R.id.connection_status);
            connection.setText(value);
        }
    }

    public void loadDevicesLayout() {
        Switch s1 = findViewById(R.id.pump_btn);
        Switch s2 = findViewById(R.id.fan_btn);
        Switch s3 = findViewById(R.id.light_btn);
        Switch s4 = findViewById(R.id.auto_btn);
        TextView conn = findViewById(R.id.connection_status);

        s1.setChecked(model.pump_btn.equals("1"));
        s2.setChecked(model.fan_btn.equals("1"));
        s3.setChecked(model.light_btn.equals("1"));
        s4.setChecked(model.auto_mode.equals("1"));
        conn.setText(model.connection);

        if(s4.isChecked()){
            s1.setClickable(false);
            s1.setTrackDrawable(getDrawable(R.drawable.disable_track_switch));
            s1.setThumbDrawable(getDrawable(R.drawable.disable_switch_thumb));
            s2.setClickable(false);
            s2.setTrackDrawable(getDrawable(R.drawable.disable_track_switch));
            s2.setThumbDrawable(getDrawable(R.drawable.disable_switch_thumb));
            s3.setClickable(false);
            s3.setTrackDrawable(getDrawable(R.drawable.disable_track_switch));
            s3.setThumbDrawable(getDrawable(R.drawable.disable_switch_thumb));
        }
    }

    public void startMQTT() {
        mqttHelper = new MQTTHelper(this);
        mqttHelper.setCallback(new MqttCallbackExtended() {

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("on_mess", "hello");
                if(topic.contains("sensor") || topic.contains("ai")){
                    model.setSensorsAndAi(topic, message.toString());
                    updateSensorsLayout(topic, message.toString());
                } else if(topic.contains("button") || topic.contains("connection")){
                    model.setBtnAndConnection(topic, message.toString());
                    updateDevicesLayout(topic, message.toString());
                } else if(topic.contains("frequency")){
                    model.setFrequency(topic, message.toString());
                    updateFreLayout(topic, message.toString());
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {}
            @Override
            public void connectionLost(Throwable cause) {}
        });
    }

    public void handleSubmitSettings(View view) {
        EditText gateway_fre = findViewById(R.id.gateway_fre);
        EditText uart_fre = findViewById(R.id.uart_fre);
        EditText min_hu = findViewById(R.id.min_humi);
        EditText max_temp = findViewById(R.id.max_temp);
        EditText min_light = findViewById(R.id.min_light);

        String gateway_text = gateway_fre.getText().toString();
        String uart_text = uart_fre.getText().toString();
        String min_hu_text  = min_hu.getText().toString();
        String max_temp_text = max_temp.getText().toString();
        String min_light_text = min_light.getText().toString();

        if(gateway_text.equals("") && uart_text.equals("") && min_hu_text.equals("") && max_temp_text.equals("") && min_light_text.equals("")) {
            showToast("Cannot leave all fields empty");
        } else {
            publishFre();
            publishBounds();
        }

    }

    public void publishFre() {
        EditText gateway_fre = findViewById(R.id.gateway_fre);
        EditText uart_fre = findViewById(R.id.uart_fre);
        String g = gateway_fre.getText().toString();
        String u = uart_fre.getText().toString();

        Integer vg = g.equals("")?model.gateway_fre:Integer.parseInt(g);
        Integer vu = u.equals("")?model.uart_fre:Integer.parseInt(u);


        if(model.conn) {
            if(vg < 5 || vg > 60 || vu < 5 || vu > 60) {
                showToast("Frequencies have not changed (should be in range 5-60)");
            }
            else {
                mqttHelper.publish("aaron_24/feeds/frequency", String.valueOf(vg));
                mqttHelper.publish("aaron_24/feeds/uart-frequency", String.valueOf(vu));
            }
        } else {
            showToast("Frequencies have not changed because of disconnection");
        }
        gateway_fre.setText("");
        uart_fre.setText("");
    }

    public void showToast(String message) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    public void publishBounds() {
        Button btn = findViewById(R.id.submit_button);

        EditText min_hu = findViewById(R.id.min_humi);
        EditText max_temp = findViewById(R.id.max_temp);
        EditText min_light = findViewById(R.id.min_light);

        Integer h = min_hu.getText().toString().equals("")?model.min_humi:Integer.parseInt(min_hu.getText().toString());
        Integer t = max_temp.getText().toString().equals("")?model.max_temp:Integer.parseInt(max_temp.getText().toString());
        Integer l = min_light.getText().toString().equals("")?model.min_light:Integer.parseInt(min_light.getText().toString());

        if(h==0 || t==0 || l==0){
            showToast("Bound values of sensors cannot be ZERO");
        }

        if(!(h==model.min_humi && t==model.max_temp && l==model.min_light)) {
            btn.setClickable(false);
            String message = String.valueOf(h)+"o"+String.valueOf(t)+"o"+String.valueOf(l);
            Call<String> call = api.setBound(message);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if(!response.isSuccessful()) {
                        Log.d("myAPI", "Fail");
                    }
                    btn.setClickable(true);
                    Log.d("myAPI", response.body());
                    model.setBounds(h, t, l);
                    loadSettingsLayout();
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    btn.setClickable(true);
                    Log.d("myAPI", "Fail");
                }
            });
        }
        else {
            showToast("The bounds have not changed");
            btn.setClickable(true);
            loadSettingsLayout();
        }
    }

    public void getBounds() {
        Call<String> call = api.getBounds();
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(!response.isSuccessful())
                    Log.d("myAPI", "fail");
                Log.d("myAPI", response.body());
                String[] values = response.body().split(",", -1);
                model.setBounds(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
            }
            @Override
            public void onFailure(Call<String> call, Throwable t)
                {Log.d("myAPI", "fail");}
        });
    }

    public void handleAutoMode(@NonNull View v) {
        Switch auto_mode = findViewById(v.getId());
            Call<Boolean> call = api.setAuto();
            call.enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    if(!response.isSuccessful())
                        Log.d("myAPI", "Fail");
                    Boolean res = response.body();
                    model.auto_mode = res?"1":"0";
                    auto_mode.setChecked(res);
                    handleSwitchLayout(res);
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    Log.d("myAPI", "Fail");
                }
            });

    }

    public void handlePump(View view) {
        Switch btn = findViewById(R.id.pump_btn);

        if(model.conn) {

            btn.setClickable(false);

            if(btn.isChecked()) {
                btn.setChecked(false);
                Call<String> call = api.pumpOn();
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if(!response.isSuccessful()) {
                            Log.d("myAPI", "Fail");
                        }
                        btn.setClickable(true);
                        Log.d("myAPI", "pump on");
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        btn.setClickable(true);
                        Log.d("myAPI", "Fail");
                    }
                });
            }
            else {
                btn.setChecked(true);
                Call<String> call = api.pumpOff();
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if(!response.isSuccessful()) {
                            Log.d("myAPI", "Fail");
                        }
                        btn.setClickable(true);
                        Log.d("myAPI", "pump off");
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        btn.setClickable(true);
                        Log.d("myAPI", "Fail");
                    }
                });
            }
        }
        else {
            btn.setChecked(false);
            showToast("Connection is not okay");
        }
    }

    public void handleFan(View view) {
        Switch btn = findViewById(R.id.fan_btn);

        if(model.conn) {

            btn.setClickable(false);

            if(btn.isChecked()) {
                btn.setChecked(false);
                Call<String> call = api.fanOn();
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if(!response.isSuccessful()) {
                            Log.d("myAPI", "Fail");
                        }
                        btn.setClickable(true);
                        Log.d("myAPI", "fan on");
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        btn.setClickable(true);
                        Log.d("myAPI", t.toString());
                    }
                });
            }
            else {
                btn.setChecked(true);
                Call<String> call = api.fanOff();
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if(!response.isSuccessful()) {
                            Log.d("myAPI", "Fail");
                        }
                        btn.setClickable(true);
                        Log.d("myAPI", "fan off");
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        btn.setClickable(true);
                        Log.d("myAPI", "Fail");
                    }
                });
            }
        }
        else {
            btn.setChecked(false);
            showToast("Connection is not okay");
        }
    }

    public void handleLight(View view) {
        Switch btn = findViewById(R.id.light_btn);

        if(model.conn) {

            btn.setClickable(false);

            if(btn.isChecked()) {
                btn.setChecked(false);
                Call<String> call = api.lightOn();
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if(!response.isSuccessful()) {
                            Log.d("myAPI", "Fail");
                        }
                        btn.setClickable(true);
                        Log.d("myAPI", "light on");
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        btn.setClickable(true);
                        Log.d("myAPI", t.toString());
                    }
                });
            }
            else {
                btn.setChecked(true);
                Call<String> call = api.lightOff();
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if(!response.isSuccessful()) {
                            Log.d("myAPI", "Fail");
                        }
                        btn.setClickable(true);
                        Log.d("myAPI", "light off");
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        btn.setClickable(true);
                        Log.d("myAPI", "Fail");
                    }
                });
            }
        }
        else {
            btn.setChecked(false);
            showToast("Connection is not okay");
        }
    }

}

