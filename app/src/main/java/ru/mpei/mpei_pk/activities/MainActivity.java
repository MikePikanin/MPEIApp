package ru.mpei.mpei_pk.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;

import android.support.annotation.NonNull;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Timer;

import ru.mpei.mpei_pk.Fragments.FragmentEdit;
import ru.mpei.mpei_pk.Fragments.FragmentMain;
import ru.mpei.mpei_pk.Fragments.FragmentNews;
import ru.mpei.mpei_pk.Fragments.FragmentQueue;
import ru.mpei.mpei_pk.ProtocolMPEI;
import ru.mpei.mpei_pk.R;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    //Статичный таймер, который используется в фрагментах для обновления информации, сам таймер не
    // останавливается, только изменяются его задачи TimerTask
    static public Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(ru.mpei.mpei_pk.R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //При инициализации открываем фрагмент с главной странцией
        Fragment fragment = FragmentMain.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment, fragment.getClass().getSimpleName()).commit();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fm.getBackStackEntryCount() > 0) {
            FrameLayout fl = findViewById(R.id.flContent);
            fl.removeAllViews();
            fm.popBackStack();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Выйти из приложения?")
                    .setMessage("Вы действительно хотите выйти?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            MainActivity.super.onBackPressed(); }
                    }).create().show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.nav_main: {
                //Фрагмент главной страницы
                fragment = FragmentMain.newInstance();
                break;
            }
            case R.id.nav_news: {
                //Фрагмент страницы новостей
                fragment = FragmentNews.newInstance();
                break;
            }
            case R.id.nav_queue: {
                //Фрагмент страницы постановки в очередь
                fragment = FragmentQueue.newInstance();
                break;
            }
            case R.id.nav_docs: {
                //Фрагмент страницы подачи документов
                fragment = FragmentEdit.newInstance();
                break;
            }
            case R.id.nav_exit: {
                final Context context = this;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ProtocolMPEI protocolMPEI = new ProtocolMPEI(getApplicationContext());
                            //Попытка выйти из аккаунта
                            protocolMPEI.exit();
                            //Удаляем токен регистрации Firebase
                            FirebaseInstanceId.getInstance().deleteInstanceId();
                            //Открываем страницу авторизации
                            Intent intent = new Intent(context, LoginActivity.class);
                            startActivity(intent);
                            ((Activity) context).finish();

                        } catch (Exception e) {
                            Log.e("MainActivity", e.getMessage());

                        }
                    }
                }).start();
                break;
            }
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment f = fragmentManager.findFragmentByTag(fragment.getClass().getSimpleName());
            FragmentTransaction transaction = fragmentManager.beginTransaction().replace(R.id.flContent, fragment, fragment.getClass().getSimpleName());
            //Проверяем, если такой фрагмент уже был открыт и не отображается в данный момент
            if (f == null) {
                transaction.addToBackStack(null);
            }
            transaction.commit();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawers();
        return true;
    }

}

