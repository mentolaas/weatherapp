package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    TextView city;
    TextView temp;
    TextView wind;
    TextView clock;
    TextView day;
    ImageView weather;
    TextView data2;

    String api_key = "2887ec495f09a0f4d0a958821d966cd9";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        city = findViewById(R.id.City);
        temp = findViewById(R.id.temperature);
        wind = findViewById(R.id.wind);
        clock = findViewById(R.id.time);
        day = findViewById(R.id.Data);
        weather = findViewById(R.id.weather);
        data2 = findViewById(R.id.data2);

        new ApiHandlerGeoLocation().execute("https://ipwho.is/");

    }

    @SuppressLint("StaticFieldLeak")
    class ApiHandlerGeoLocation extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            city.setText("Загрузка");
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            URL url = null;
            BufferedReader reader = null;

            try{
                url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder stringBuffer = new StringBuilder();
                String line = "";

                while((line = reader.readLine()) != null) {
                    stringBuffer.append(line).append("\n");
                }

                return stringBuffer.toString();

            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                } try{
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            return null;

        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject jsonObject = new JSONObject(result);
                city.setText(jsonObject.getString("city"));

                new ApiHandlerWeather().execute("https://api.openweathermap.org/data/2.5/weather?q=" + city.getText() + "&appid=" + api_key);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class ApiHandlerWeather extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            wind.setText("Загрузка");
            clock.setText("Загрузка");
            temp.setText("Загрузка");
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            URL url = null;
            BufferedReader reader = null;

            try{
                url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder stringBuffer = new StringBuilder();
                String line = "";

                while((line = reader.readLine()) != null) {
                    stringBuffer.append(line).append("\n");
                }

                return stringBuffer.toString();

            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                } try{
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            return null;

        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject jsonObject = new JSONObject(result);
                DayOfWeek dayOfWeek = DayOfWeek.from(LocalDate.now());
                int temperature = (int)Math.round(jsonObject.getJSONObject("main").getDouble("temp") - 273.15);
                int wind_speed = (int)jsonObject.getJSONObject("wind").getDouble("speed");
                day.setText(dayOfWeek.toString() + " " + Year.now().getValue());

                JSONObject jsonObject1 = new JSONObject(jsonObject.getJSONArray("weather").getString(0));

                if (jsonObject1.getString("main").equals("Clouds")) {
                    weather.setImageResource(R.mipmap.clouds);
                } else if (jsonObject1.getString("main").equals("Rain")) {
                    weather.setImageResource(R.mipmap.rain);
                } else if (jsonObject1.getString("main").equals("Clear")) {
                    weather.setImageResource(R.mipmap.sun);
                } else if (jsonObject1.getString("main").equals("Snow")) {
                    weather.setImageResource(R.mipmap.snow);
                } else {
                    weather.setImageResource(R.mipmap.sunclouds);
                }

                CompletableFuture.runAsync(()->{
                    for (int temper = 0; temper != temperature; temper++) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        temp.setText(String.valueOf(temper + "°"));
                    }
                });

                CompletableFuture.runAsync(()->{
                    for (double winds = 0; winds != wind_speed; winds++) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        wind.setText(String.valueOf(winds + "m/s"));
                    }
                });

                CompletableFuture.runAsync(()->{
                    while (true) {
                        try {
                            Thread.sleep(1000);
                            LocalTime time = LocalTime.now();
                            clock.setText(time.toString());

                            if (Integer.valueOf(time.getHour()) >= 21) {
                                data2.setText("Good Night, my friend!");
                            } else if (Integer.valueOf(time.getHour()) <= 10 ) {
                                data2.setText("Good morning, my friend!");
                            } else if (Integer.valueOf(time.getHour()) >= 10 && Integer.valueOf(time.getHour()) <= 20) {
                                data2.setText("Good day, my friend!");
                            }

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}