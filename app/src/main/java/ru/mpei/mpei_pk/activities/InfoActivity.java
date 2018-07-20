package ru.mpei.mpei_pk.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import ru.mpei.mpei_pk.R;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        TextView tvView = findViewById(R.id.userInfoTextView);

        Intent intent = getIntent();

        String message = intent.getStringExtra("message");

        tvView.setText(message);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
