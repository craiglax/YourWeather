package ngonim.xyz.yourweather;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String APIKEY = "0b7621a8ed42749a4d70136dab97e9f9";
    //TODO:hardcoded harare coordinates
    private double latitude = -17.824858;
    private double longitude = 31.053028;
    /*private String forecast =
            "https://api.darksky.net/forecast/0b7621a8ed42749a4d70136dab97e9f9/-17.824858,31.053028";*/
    private String forecast = "https://api.darksky.net/forecast/" + APIKEY +
            "/" + latitude + "," + longitude;
    private CurrentWeather mCurrentWeather;
    private TextView mTime;
    private TextView mTemperature;
    private TextView mHumidity;
    private TextView mPrecipitation;
    private TextView mSummary;
    private ImageView mIconView;
    private TextView mLocationText;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTime=(TextView)findViewById(R.id.timeLabel);
        mTemperature=(TextView)findViewById(R.id.temperatureLabel);
        mHumidity=(TextView)findViewById(R.id.humidityValue);
        mPrecipitation=(TextView)findViewById(R.id.precipValue);
        mIconView=(ImageView) findViewById(R.id.iconImageView);
        mSummary=(TextView)findViewById(R.id.summaryText);
        mLocationText=(TextView)findViewById(R.id.locationText);
        getForecast();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_refresh){
            getForecast();
            Toast.makeText(this, "Weather Updated", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getForecast() {
        if(isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecast)
                    .build();

            Call call = client.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure
                        (Call call, IOException e) {}

                @Override
                public void onResponse
                        (Call call, Response response)
                        throws IOException {
                    try {
                        final String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if(response.isSuccessful()) {
                            mCurrentWeather = getCurrentDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        } else {
                            errorAlert();
                        }

                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Exception caught", e);
                    }
                }
            });

        } else {
            Toast.makeText(this, "Network not available",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void updateDisplay() {
        mTemperature.setText(mCurrentWeather.getTemperatureInCelcius() + "");
        mPrecipitation.setText(mCurrentWeather.getPrecipitation() + "%");
        mHumidity.setText(mCurrentWeather.getHumidity() + "");
        mSummary.setText(mCurrentWeather.getSummary()+ "");
        Drawable drawable = getResources().getDrawable(mCurrentWeather.getIconId(), null);
        mIconView.setImageDrawable(drawable);
        mTime.setText(mCurrentWeather.getFormattedTime());
        mLocationText.setText(mCurrentWeather.getTimeZone());
    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject currently =  forecast.getJSONObject("currently");
        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setPrecipitation(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTimeZone(timezone);

        return currentWeather;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isAvailabe = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailabe = true;
        }
        return isAvailabe;
    }

    private void errorAlert() {
        AlertDialogFragment a = new AlertDialogFragment();
        a.show(getFragmentManager(), "error_dialog");
    }
}
