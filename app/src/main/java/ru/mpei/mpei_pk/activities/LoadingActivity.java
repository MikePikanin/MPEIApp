package ru.mpei.mpei_pk.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import ru.mpei.mpei_pk.ProtocolMPEI;
import ru.mpei.mpei_pk.R;

public class LoadingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //вызываем функцию родителя
        super.onCreate(savedInstanceState);
        //заполняем запускаемый activity из layout activity_main
        setContentView(R.layout.activity_loading);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Открываем Shared Preferences и проверяем, есть ли авторизированный пользователь.
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        Boolean isAuthorizedUser = sharedPref.getBoolean("isAuthorizedUser", false);

        if (isAuthorizedUser) {
            //Если такой пользователь есть, то попытка быстрой авторизации.
            final Context context = this;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    //Авторизация
                    ProtocolMPEI protocolMPEI = new ProtocolMPEI(context);
                    Intent intent;
                    if (protocolMPEI.quickAuth()) {
                        intent = new Intent(context, MainActivity.class);
                    } else {
                        intent = new Intent(context, LoginActivity.class);
                    }
                    startActivity(intent);

                    ((Activity)context).finish();
                }
            }).start();
        }
        else {
            //В противном случае - вызов окна авторизации.
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            this.finish();
        }
    }

}
