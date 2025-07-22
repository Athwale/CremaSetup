package com.android.provision;

import android.content.Intent;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StartActivity extends android.app.Activity {

    private String start_code = null;
    private static final int S_CODE = 658456;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BufferedReader reader;
        // Read activity start code.
        try {
            final InputStream file = getAssets().open("start_code.ptx");
            reader = new BufferedReader(new InputStreamReader(file));
            this.start_code = reader.readLine();
            file.close();
        } catch (Exception e) {
            finish();
        }

        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.putExtra("start_code", this.start_code);
        startActivityForResult(intent, S_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == S_CODE) {
            finishActivity(S_CODE);
            this.finish();
        }
    }
}
