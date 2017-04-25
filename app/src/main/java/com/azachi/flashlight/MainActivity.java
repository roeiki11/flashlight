package com.azachi.flashlight;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1999;
    private ImageButton flashLight;
    private Camera camera;
    private Camera.Parameters parameter;
    private boolean deviceHasFlash;
    private boolean isFlashLightOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        flashLight = (ImageButton) findViewById(R.id.flash_light);
        flashLight.setEnabled(false);
        ArrayList<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.WAKE_LOCK);
        }
        if (permissions.size() > 0) {
            String[] permissionArr = new String[permissions.size()];
            ActivityCompat.requestPermissions(this, permissions.toArray(permissionArr), PERMISSION_REQUEST_CODE);
        } else {
            init();
        }
        flashLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFlashLightOn) {
                    turnOnTheFlash();
                } else {
                    turnOffTheFlash();
                }
            }
        });
    }

    private void init() {
        deviceHasFlash = getApplication().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (!deviceHasFlash) {
            Toast.makeText(MainActivity.this, "Sorry, you device does not have any camera", Toast.LENGTH_LONG).show();
        } else {
            this.camera = Camera.open(0);
            parameter = this.camera.getParameters();
        }
        flashLight.setEnabled(true);
    }

    private void turnOffTheFlash() {
        if (this.camera != null) {
            parameter.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            this.camera.setParameters(parameter);
            this.camera.stopPreview();
            isFlashLightOn = false;
            flashLight.setImageResource(R.drawable.buttonoff);
        }
    }

    private void turnOnTheFlash() {
        if (this.camera != null) {
            parameter = this.camera.getParameters();
            parameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            this.camera.setParameters(parameter);
            this.camera.startPreview();
            isFlashLightOn = true;
            flashLight.setImageResource(R.drawable.buttonon);
        }
    }

    private void getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                parameter = camera.getParameters();
            } catch (RuntimeException e) {
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.camera != null) {
            this.camera.release();
            this.camera = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        turnOffTheFlash();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (deviceHasFlash) {
            turnOffTheFlash();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getCamera();
        Branch branch = Branch.getInstance();

        branch.initSession(new Branch.BranchReferralInitListener(){
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {
                    // params are the deep linked params associated with the link that the user clicked -> was re-directed to this app
                    // params will be empty if no data found
                    // ... insert custom logic here ...
                } else {
                    Log.i("MyApp", error.getMessage());
                }
            }
        }, this.getIntent().getData(), this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != PERMISSION_REQUEST_CODE) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(R.string.permissions)
                    .setTitle(R.string.permissions_message)
                    .setCancelable(false)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            init();
        }
    }
}
