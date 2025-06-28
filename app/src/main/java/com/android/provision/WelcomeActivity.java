package com.android.provision;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;


public final class WelcomeActivity extends android.app.Activity implements
        View.OnClickListener {

    public static final int REQUEST_1 = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide buttons.
        //View decorView = getWindow().getDecorView();
        //int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        //decorView.setSystemUiVisibility(uiOptions);
        // Hide status bar.
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //        WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.welcome_activity);

        Button mButton = findViewById(R.id.button);
        mButton.setOnClickListener(this);
        // todo capture home button
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
    }

    public void do_provision() {
        // Set settings to finish device provisioning. This enables control buttons and normal
        // operation.
        Context context = getApplicationContext();
        ContentResolver contentResolver = context.getContentResolver();
        try {
            Settings.Global.putInt(getContentResolver(), "device_provisioned", 1);
            Settings.Secure.putInt(getContentResolver(), "user_setup_complete", 1);

            // Disable and kill setup wizard
            //getPackageManager().setApplicationEnabledSetting(getPackageName(),
            //        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {

        try {
            Intent intent = new Intent();
            // Start by setting the password, rest is handled one by one in onActivityResult
            // Password:
            intent.setClassName("com.android.settings",
                    "settings.password.SetupChooseLockGeneric$InternalActivity");
            startActivityForResult(intent, REQUEST_1);
        } catch (Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

        // Do this last to prevent user from exiting from device setup.
        this.do_provision();
    }
}
