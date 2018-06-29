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

        TextView tvView = (TextView)findViewById(R.id.userInfoTextView);

        Intent intent = getIntent();

        String userNic = intent.getStringExtra("userNic");
        String userPwd = intent.getStringExtra("userPwd");

        String data = "Вы успешно зарегистрированы!\nПожалуйста, запомните Ваши данные для входа" +
                "в свой аккаунт:\nЛогин - " + userNic + "\nПароль - " + userPwd;
        tvView.setText(data);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
