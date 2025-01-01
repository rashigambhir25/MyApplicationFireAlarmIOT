package com.example.myapplicationfirealarm;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button alertButton = findViewById(R.id.alertButton);
        Button monitorButton = findViewById(R.id.monitorButton);

        // Fire Alert Button
        alertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndSendFireAlert();
            }
        });

        // Monitor Button to navigate to MonitoringActivity
        monitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MonitoringActivity.class);
                startActivity(intent);
            }
        });
    }

    // Check for SEND_SMS permission
    private void checkAndSendFireAlert() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not already granted
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, proceed to send SMS
            sendFireAlert();
        }
    }

    // Send fire alert SMS
    private void sendFireAlert() {
        String message = "Fire detected! Please take immediate action.";
        String[] emergencyContacts = {
                "+1234567890", // Fire Department
                "+0987654321", // Police Department
                "+1122334455"  // User Contact
        };

        try {
            SmsManager smsManager = SmsManager.getDefault();
            for (String contact : emergencyContacts) {
                smsManager.sendTextMessage(contact, null, message, null, null);
            }
            Toast.makeText(this, "Emergency alerts sent successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send alert: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Handle the result of the runtime permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed to send SMS
                sendFireAlert();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied to send SMS.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
