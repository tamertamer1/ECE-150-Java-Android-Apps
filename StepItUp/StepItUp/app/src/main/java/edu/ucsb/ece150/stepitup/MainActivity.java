package edu.ucsb.ece150.stepitup;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import java.util.Locale;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.hardware.SensorManager;
import android.widget.TextView;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import androidx.core.app.NotificationCompat;
import android.media.RingtoneManager;
import android.net.Uri;
import android.app.NotificationChannel;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;


public class MainActivity extends AppCompatActivity implements SensorEventListener  {
    private StepDetector mStepDetector = new StepDetector();
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mStepCounterSensor;
    private TextView textViewStepCounter;
    private TextView textViewTotalSteps;
    private TextView textViewStepsPerHour;
    private TextView textViewGoalsCompleted;
    private EditText editTextStepGoal;
    private Button buttonSave;
    private Button buttonRestart;
    private int originalStepGoal;
    private int stepsCounted;
    private int goalsCompleted;
    private SharedPreferences sharedPref;
    private volatile boolean running = true;
    private long stepTimestamp = System.currentTimeMillis();
    private int stepsLastHour = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // [TODO] Create a thread to calculate steps/hr

        // [TODO] Initialize UI elements
        textViewStepCounter = findViewById(R.id.textViewBuiltInStepCounter);
        textViewTotalSteps = findViewById(R.id.textViewTotalSteps);
        textViewStepsPerHour = findViewById(R.id.textViewStepsPerHour);
        textViewGoalsCompleted = findViewById(R.id.textViewGoalsCompleted);
        editTextStepGoal = findViewById(R.id.editTextNumber);
        buttonSave = findViewById(R.id.buttonSave);
        buttonRestart = findViewById(R.id.buttonRestart);
        sharedPref = getSharedPreferences("StepItUpPrefs", Context.MODE_PRIVATE);
        originalStepGoal = sharedPref.getInt("originalStepGoal", Integer.parseInt(editTextStepGoal.getText().toString()));
        editTextStepGoal.setText(String.valueOf(originalStepGoal));
        stepsCounted = sharedPref.getInt("stepsCounted", 0);
        goalsCompleted = sharedPref.getInt("goalsCompleted", 0);
        startStepPerHourCalculation();
        // [TODO] Setup button behavior
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int stepGoal = Integer.parseInt(editTextStepGoal.getText().toString());
                saveStepGoalToPreferences(stepGoal);
            }
        });
        buttonRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stepsCounted = originalStepGoal;
                editTextStepGoal.setText(String.valueOf(stepsCounted));
                saveStepCountToPreferences(stepsCounted);
            }
        });
        // [TODO] Request ACTIVITY_RECOGNITION permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION},1);
            }
        }
        // [TODO] Initialize accelerometer and step counter sensors
        mSensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer=mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }
        if (mAccelerometer!=null){
            mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_GAME);
        }
        if (mStepCounterSensor!=null){
            mSensorManager.registerListener(this,mStepCounterSensor,SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            boolean stepDetected = mStepDetector.detectStep(x, y, z);
            if (stepDetected) {
                handleStep(event);
            }
        }
        else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            handleStep(event);
        }
    }
    private void saveStepGoalToPreferences(int stepGoal) {
        SharedPreferences sharedPref = getSharedPreferences("StepItUpPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("originalStepGoal", stepGoal);
        editor.apply();
    }
    private void saveStepCountToPreferences(int stepsCounted) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("stepsCounted", stepsCounted);
        editor.apply();
    }

    private void saveGoalsCompletedToPreferences(int goalsCompleted) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("goalsCompleted", goalsCompleted);
        editor.apply();
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Unused
    }

    private void sendNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "step_it_up_channel_01";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "StepItUp Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(notificationChannel);
        }

        // Create an intent that will open MainActivity when the notification is tapped
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background) // replace with your app icon
                .setContentTitle("Goal Reached")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent); // Set the intent that will fire when the user taps the notification

        // Notify the user
        notificationManager.notify(0, notificationBuilder.build());
    }

    private void startStepPerHourCalculation() {
        Thread stepsPerHourThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    try {
                        // Wait for one hour
                        Thread.sleep(3600000);
                        int currentSteps = stepsCounted - stepsLastHour;
                        stepsLastHour = stepsCounted;

                        long timeNow = System.currentTimeMillis();
                        long timeElapsed = timeNow - stepTimestamp;
                        stepTimestamp = timeNow;
                        // Calculate steps per hour - this is a simple approximation
                        double stepsPerHour = (currentSteps / (timeElapsed / 3600000.0));

                        // Update the TextView on the UI thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView textViewStepsPerHour = findViewById(R.id.textViewStepsPerHour);
                                textViewStepsPerHour.setText(String.format(Locale.US, "Steps/Hour: %.2f", stepsPerHour));
                            }
                        });
                    } catch (InterruptedException e) {
                        running = false;
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        stepsPerHourThread.start();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
    }
    private void handleStep(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            stepsCounted++;
            textViewStepCounter.setText("Built-in Step Counter: " + stepsCounted);
            saveStepCountToPreferences(stepsCounted);
        }
        int stepsDetected = Integer.parseInt(textViewTotalSteps.getText().toString().split(": ")[1]);
        stepsDetected++;
        textViewTotalSteps.setText("Total Steps: " + stepsDetected);

        if (stepsDetected % originalStepGoal == 0) {
            goalsCompleted++;
            textViewGoalsCompleted.setText("Goals Completed: " + goalsCompleted);
            saveGoalsCompletedToPreferences(goalsCompleted);
        }
        int remainingSteps = Math.max(originalStepGoal - stepsDetected, 0);
        editTextStepGoal.setText(String.valueOf(remainingSteps));

        // If the goal is reached, stop decrementing by not calling this method anymore
        if(remainingSteps == 0) {
            sendNotification();
        }
    }
}
