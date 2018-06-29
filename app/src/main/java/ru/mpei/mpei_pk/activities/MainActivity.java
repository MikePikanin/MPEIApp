package ru.mpei.mpei_pk.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.support.annotation.NonNull;

import android.support.constraint.Group;
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

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Timer;

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
    static public Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(ru.mpei.mpei_pk.R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Fragment fragment = (Fragment) FragmentMain.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment, fragment.getClass().getSimpleName()).commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        timer = new Timer();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fm.getBackStackEntryCount() > 0) {
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
        Intent intent;
        Fragment fragment = null;
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        switch (item.getItemId()) {
            case R.id.nav_main: {
                fragment = (Fragment) FragmentMain.newInstance();
                break;
            }
            case R.id.nav_news: {
                fragment = (Fragment) FragmentNews.newInstance();
                break;
            }
            case R.id.nav_queue: {
                fragment = (Fragment) FragmentQueue.newInstance();
                break;
            }
            case R.id.nav_docs: {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Авторизация
                        ProtocolMPEI protocolMPEI = new ProtocolMPEI(getApplicationContext());
                        String ticket = protocolMPEI.ticketAuth();

                        SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                        String nickname = sharedPref.getString("nickname", "");

                        String url = "https://www.pkmpei.ru/index.php?cmd=ticket&logon_name=" + nickname + "&logon_ticket=" + ticket;
                        Intent intent = new Intent(getApplicationContext(), WebActivity.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                    }
                }).start();
                item.setChecked(false);
                break;
            }
            case R.id.nav_exit: {
                SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("isAuthorizedUser", false);
                editor.apply();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FirebaseInstanceId.getInstance().deleteInstanceId();
                        } catch (Exception e) {
                            Log.e("FCM", e.getMessage());
                        }
                    }
                }).start();
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                this.finish();
                return true;
            }
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment f = fragmentManager.findFragmentByTag(fragment.getClass().getSimpleName());
            FragmentTransaction transaction = fragmentManager.beginTransaction().replace(R.id.flContent, fragment);
            if (f == null || !f.isVisible()) {
                transaction.addToBackStack(null);
            }
            transaction.commit();
        }

        //drawer.closeDrawer(GravityCompat.START);
        drawer.closeDrawers();
        return true;
    }

}

