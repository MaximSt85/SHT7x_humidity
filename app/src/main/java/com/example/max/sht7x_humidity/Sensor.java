package com.example.max.sht7x_humidity;

/**
 * Created by Max on 15.08.2017.
 */

public class Sensor {
    private String humidity;
    private String temperature;

    public Sensor() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Sensor(String humidity, String temperature) {
        this.humidity = humidity;
        this.temperature = temperature;
    }

    public String getHumidity(){
        return humidity;
    }

    public String getTemperature(){
        return temperature;
    }
}
