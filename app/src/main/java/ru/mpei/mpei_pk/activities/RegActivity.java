package ru.mpei.mpei_pk.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ru.mpei.mpei_pk.ProtocolMPEI;
import ru.mpei.mpei_pk.R;

public class RegActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);
        //Установка значений выпадающего списка с вопросами для восстановления пароля
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.passRestoreQuestions));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = findViewById(R.id.spinnerQuestion);
        spinner.setAdapter(adapter);
        //Настройка выбора даты рождения
        DatePicker dt = findViewById(R.id.simpleDatePicker);
        dt.setMaxDate(new Date().getTime());
        dt.updateDate(2000, 0 ,1);
        //Обработчик нажатия кнопки зарегистрироваться
        Button regBtn = findViewById(R.id.reg_btn);
        final Context context = this;
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Получение данных
                String firstName = ((EditText)findViewById(R.id.etFirstName)).getText().toString();
                String lastName = ((EditText)findViewById(R.id.etLastName)).getText().toString();
                String middleName = ((EditText)findViewById(R.id.etMiddleName)).getText().toString();
                String email = ((EditText)findViewById(R.id.etEmail)).getText().toString();
                EditText etQuestion = findViewById(R.id.etOwnQuestion);
                String question;
                if (etQuestion.getText().toString().isEmpty()) {
                    question = ((Spinner)findViewById(R.id.spinnerQuestion)).getSelectedItem().toString();
                } else {
                    question = etQuestion.getText().toString();
                }
                String answer = ((EditText)findViewById(R.id.etAnswer)).getText().toString();
                DatePicker dt = findViewById(R.id.simpleDatePicker);

                Calendar calendar = Calendar.getInstance();
                calendar.set(dt.getYear(), dt.getMonth(), dt.getDayOfMonth());
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", new Locale("ru","RU"));
                String birthDate = dateFormat.format(calendar.getTime());

                //Проверка, на введеные поля.
                if (firstName.isEmpty() || lastName.isEmpty() || middleName.isEmpty() ||
                        question.isEmpty() || answer.isEmpty() || email.isEmpty()) {
                    Toast.makeText( v.getContext(),  "Заполнены не все поля!", Toast.LENGTH_LONG).show();
                }
                else {
                    //Подготовка данных
                    final Map <String, String> userInfo = new HashMap<>();
                    userInfo.put("surname", lastName);
                    userInfo.put("name", firstName);
                    userInfo.put("patronymic", middleName);
                    userInfo.put("question", question);
                    userInfo.put("answer", answer);
                    userInfo.put("birthDate", birthDate);
                    userInfo.put("email", email);
                    userInfo.put("capcha", "r2e4");
                    //Вывод полосы загрузки.
                    ProgressBar progressBar = findViewById(R.id.progressBarReg);
                    progressBar.setVisibility(ProgressBar.VISIBLE);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //Авторизация
                            ProtocolMPEI protocolMPEI = new ProtocolMPEI(context);
                            HashMap<String, String> info = protocolMPEI.reg(userInfo);
                            if (info != null) {
                                Intent intent = new Intent(context, MainActivity.class);
                                startActivity(intent);

                                intent = new Intent(context, InfoActivity.class);
                                String userInfo = String.format(getResources().getString(R.string.user_info), info.get("userNic"), info.get("userPwd"));
                                intent.putExtra("message", userInfo);
                                startActivity(intent);

                                ((Activity)context).finish();

                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Сокрытие полосы загрузки.
                                        ProgressBar progressBar = findViewById(R.id.progressBarLogin);
                                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                                        EditText passTXT = findViewById(R.id.passwordTXT);
                                        passTXT.setText("");
                                    }
                                });
                            }
                        }
                    }).start();
                }
            }
        });
        //Обработчик нажатия кнопки перехода на страницу авторизации
        TextView tv = findViewById(R.id.gotoLoginPageTextView);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegActivity.this, LoginActivity.class);
                startActivity(intent);
                RegActivity.super.finish();
            }
        });
    }
    @Override
    public void onBackPressed() {
        //При нажатии на кнопку назад, возвращаемся на страницу авторизации
        Intent intent = new Intent(RegActivity.this, LoginActivity.class);
        startActivity(intent);
        RegActivity.super.finish();
    }
}
