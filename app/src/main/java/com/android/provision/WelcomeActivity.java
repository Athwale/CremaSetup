package com.android.provision;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public final class WelcomeActivity extends android.app.Activity implements
        View.OnClickListener {

    public static final int REQUEST_1 = 1760968;

    private final File config_complete = new File(Environment.getExternalStoragePublicDirectory
            (Environment.DIRECTORY_DOWNLOADS), ".device_wizard_complete");

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

    private void copy_assets() {
        AssetManager assetManager = getAssets();
        String[] files;
        try {
            files = assetManager.list("");

            for(String filename : files) {
                File doc_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                File out_file = new File(doc_dir.getAbsolutePath(), filename);

                InputStream in = assetManager.open(filename);
                OutputStream out = new FileOutputStream(out_file);

                copy_file(in, out);
                in.close();
                out.flush();
                out.close();
                in = null;
                out = null;
            }
        } catch(IOException e) {
            Toast.makeText(this, "Copying books failed" + e,
                    Toast.LENGTH_LONG).show();
        }

        if (!this.config_complete.exists()) {
            try (FileOutputStream stream = new FileOutputStream(this.config_complete,
                    true)) {
                stream.write("Completed".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Toast.makeText(this, "Books copied", Toast.LENGTH_LONG).show();
    }
    private void copy_file(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public void do_provision() {
        // Set settings to finish device provisioning. This enables control buttons and normal
        // operation.
        try {
            Settings.Global.putInt(getContentResolver(), "device_provisioned", 1);
            Settings.Secure.putInt(getContentResolver(), "user_setup_complete", 1);

            // Disable and kill setup wizard
            getPackageManager().setApplicationEnabledSetting(getPackageName(),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {
        // Set screen lock, copy basic books, provision device
        this.copy_assets();

        try {
            Intent intent = new Intent();
            // Start by setting the password, rest is handled one by one in onActivityResult
            // Password:
            intent.setClassName("com.android.settings",
                    "com.android.settings.password.ScreenLockSuggestionActivity");
            startActivityForResult(intent, REQUEST_1);
        } catch (Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

        // Do this last to prevent user from exiting from device setup.
        this.do_provision();
    }
}
