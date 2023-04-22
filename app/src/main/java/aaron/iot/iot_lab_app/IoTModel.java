package aaron.iot.iot_lab_app;

public class IoTModel {

    public IoTModel() {
    }

    public String humi = "100";
    public String temp = "100";
    public String light = "100";
    public String pump_btn;
    public String fan_btn;
    public String light_btn;
    public String auto_mode = "0";
    public String connection;
    public String ai_value = "";
    public boolean conn;
    public int gateway_fre;
    public int uart_fre;
    public int min_humi = 10;
    public int max_temp = 10;
    public int min_light = 10;


    public void setSensorsAndAi(String topic, String value) {
        if (topic.contains("sensor1")) humi = value;
        else if (topic.contains("sensor2")) temp = value;
        else if (topic.contains("sensor3")) light = value;
        else ai_value = value;
    }

    public void setBtnAndConnection(String topic, String value) {
        if (topic.contains("button1")) pump_btn = value;
        else if (topic.contains("button2")) fan_btn = value;
        else if (topic.contains("button3")) light_btn = value;
        else {
            connection = value;
            conn = connection.equals("OKAY");
        }
    }

    public void setFrequency(String topic, String value){
        if(topic.contains("uart")) {
            uart_fre = Integer.parseInt(value);
        } else {
            gateway_fre = Integer.parseInt(value);
        }
    }

    public void setBounds(Integer humi, Integer temp, Integer light) {
        min_humi = humi;
        max_temp = temp;
        min_light = light;
    }
}
