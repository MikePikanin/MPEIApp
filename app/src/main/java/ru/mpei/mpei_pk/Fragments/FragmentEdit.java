package ru.mpei.mpei_pk.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import ru.mpei.mpei_pk.ProtocolMPEI;
import ru.mpei.mpei_pk.R;
import ru.mpei.mpei_pk.activities.MainActivity;
import ru.mpei.mpei_pk.activities.WebActivity;

public class FragmentEdit extends Fragment {

    public FragmentEdit() {
        // Required empty public constructor
    }

    public static FragmentEdit newInstance() {//int VisitType, int EducationLevel) {
        return new FragmentEdit();
    }
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        NavigationView navigation = ((Activity)context).findViewById(R.id.nav_view);
        navigation.getMenu().getItem(2).setChecked(true);
        super.onResume();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((Activity)context).findViewById(R.id.openWebBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //Авторизация
                            ((MainActivity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ProgressBar progressBar = ((MainActivity) context).findViewById(R.id.progressBarMain);
                                    progressBar.setVisibility(ProgressBar.VISIBLE);
                                }
                            });
                            ProtocolMPEI protocolMPEI = new ProtocolMPEI(context);
                            String ticket = protocolMPEI.ticketAuth();

                            SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                            String nickname = sharedPref.getString("nickname", "");

                            String url = "https://www.pkmpei.ru/index.php?cmd=ticket&logon_name=" + nickname + "&logon_ticket=" + ticket;

                            ((MainActivity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ProgressBar progressBar = ((MainActivity) context).findViewById(R.id.progressBarMain);
                                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                                }
                            });

                            Intent intent = new Intent(context, WebActivity.class);
                            intent.putExtra("url", url);
                            startActivity(intent);
                        }
                    }).start();
                } catch (Exception e) {
                    Log.e("FragmentEdit", e.getMessage());
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
