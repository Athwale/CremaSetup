package com.android.provision;

import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Objects;


public final class WelcomeActivity extends android.app.Activity {

    private String start_code = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read activity start code.
        BufferedReader reader;
        try {
            final InputStream file = getAssets().open("start_code.ptx");
            reader = new BufferedReader(new InputStreamReader(file));
            this.start_code = reader.readLine();
            file.close();
        } catch (Exception e) {
            finish();
        }

        String code = getIntent().getStringExtra("start_code");
        if (!Objects.equals(code, this.start_code)) {
            finish();
        }

        // Hide buttons.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        // Hide status bar.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.welcome_activity);

        // Set screen lock, copy basic books, provision device
        this.copy_assets();
        // Do this last to prevent user from exiting from device setup.
        this.do_provision();
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
    }

    private void copy_assets() {
        AssetManager assetManager = getAssets();

        try {
            String[] files = assetManager.list("");
            File dow_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            for(String filename : files) {
                if (filename.endsWith(".pdf") || filename.endsWith(".apk")) {
                    File out_file = new File(dow_dir.getAbsolutePath(), filename);

                    InputStream in = assetManager.open(filename);
                    OutputStream out = new FileOutputStream(out_file);

                    copy_file(in, out);

                    out.flush();
                    in.close();
                    out.close();
                }
            }
        } catch(IOException | NullPointerException e) {
            Toast.makeText(this, "Copying books failed" + e,
                    Toast.LENGTH_LONG).show();
        }
    }
    private void copy_file(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public void do_provision() {
        try {
            // Finish device provisioning. This enables control buttons and normal operation.
            Settings.Global.putInt(getContentResolver(), "device_provisioned", 1);
            Settings.Secure.putInt(getContentResolver(), "user_setup_complete", 1);
            // Set default timeouts.
            Settings.System.putInt(getContentResolver(), "screen_off_timeout", 600000);
            Settings.System.putInt(getContentResolver(), "shutdown_timeout", 60);
            Settings.System.putInt(getContentResolver(), "wifi_off_timeout", 60);
            Settings.System.putInt(getContentResolver(), "bluetooth_off_timeout", 5);
            // Set some other stuff.
            Settings.System.putInt(getContentResolver(), "haptic_feedback_enabled", 1);
            Settings.System.putInt(getContentResolver(), "location_global_kill_switch", 1);
            Settings.System.putInt(getContentResolver(), "netstats_enabled", 0);
            Settings.System.putInt(getContentResolver(), "usb_mass_storage_enabled", 0);
            Settings.System.putInt(getContentResolver(), "lock_screen_allow_private_notifications", 0);
            Settings.System.putInt(getContentResolver(), "speak_password", 0);

            // Disable and kill setup wizard
            getPackageManager().setApplicationEnabledSetting(getPackageName(),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
