package ru.mpei.mpei_pk.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ru.mpei.mpei_pk.ProtocolMPEI;
import ru.mpei.mpei_pk.R;

public class LoginActivity extends AppCompatActivity{


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Если в памяти есть сохраненный ник, выводим в поле для ввода логина.
        SharedPreferences sharedPref = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String savedNickname = sharedPref.getString("nickname", null);
        if (savedNickname != null){
            EditText loginTXT = (EditText)findViewById(R.id.loginTXT);
            loginTXT.setText(savedNickname);
        }
        final Context context = this;
        //Обработчик нажатия на кнопку вход.
        Button logBtn = (Button) findViewById(R.id.loginBtn);
        logBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Получение логина и пароля.
                EditText loginTXT = (EditText)findViewById(R.id.loginTXT);
                final String login = loginTXT.getText().toString();
                EditText passTXT = (EditText)findViewById(R.id.passwordTXT);
                final String password = passTXT.getText().toString();
                //Проверка, на введеные поля.
                if (login.isEmpty() || password.isEmpty()) {
                    Toast.makeText( v.getContext(),  "Введите логин и пароль!", Toast.LENGTH_LONG).show();
                }
                else {
                    //Вывод полосы загрузки.
                    ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarLogin);
                    progressBar.setVisibility(ProgressBar.VISIBLE);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //Авторизация
                            ProtocolMPEI protocolMPEI = new ProtocolMPEI(context);
                            if (protocolMPEI.auth(login, password)) {
                                Intent intent = new Intent(context, MainActivity.class);
                                startActivity(intent);

                                ((Activity)context).finish();
                            } else {
                                //Сокрытие полосы загрузки.
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarLogin);
                                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                                        Toast.makeText(getApplicationContext(), "Не удалось выполнить вход", Toast.LENGTH_SHORT).show();
                                        EditText passTXT = (EditText) findViewById(R.id.passwordTXT);
                                        passTXT.setText("");
                                    }
                                });
                            }
                        }
                    }).start();
                }
            }
        });
        //Обработчик нажатия кнопки перехода на страницу регистрации.
        TextView gotoReg = (TextView) findViewById(R.id.gotoRegPageTextView);
        gotoReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegActivity.class);
                startActivity(intent);
                LoginActivity.super.finish();
            }
        });

    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Выйти из приложения?")
                .setMessage("Вы действительно хотите выйти?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1)
                        {
                            LoginActivity.super.onBackPressed();
                        }
                }).create().show();
    }
}
