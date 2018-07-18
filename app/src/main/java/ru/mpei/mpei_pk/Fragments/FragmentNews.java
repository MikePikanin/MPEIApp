package ru.mpei.mpei_pk.Fragments;

import android.app.Activity;
import android.content.Context;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import de.ailis.pherialize.MixedArray;
import de.ailis.pherialize.Pherialize;
import ru.mpei.mpei_pk.R;
import ru.mpei.mpei_pk.adapters.NewsExpListAdapter;
import ru.mpei.mpei_pk.dataTypes.ItemNews;

public class FragmentNews extends Fragment {


    public FragmentNews() {
        // Required empty public constructor
    }

    public static FragmentNews newInstance() {//int VisitType, int EducationLevel) {
        return new FragmentNews();
    }
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        NavigationView navigation = ((Activity)context).findViewById(R.id.nav_view);
        navigation.getMenu().getItem(3).setChecked(true);
        super.onResume();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            Bundle args = getArguments();
            String newsId;
            if (args != null && args.containsKey("newsId")) {
                newsId = args.getString("newsId");
            } else {
                newsId = null;
            }
            SharedPreferences sharedPref = context.getSharedPreferences("news", Context.MODE_PRIVATE);
            Map<String, ?> map = sharedPref.getAll();
            ArrayList<ArrayList<ItemNews>> news = null;
            ArrayList<String> titles = null;
            int num = -1;
            if (map != null) {
                news = new ArrayList<>();
                titles = new ArrayList<>();
                int counter = 0;
                for (String key : map.keySet()) {
                    if (map.get(key) instanceof String) {

                        MixedArray m = Pherialize.unserialize((String) map.get(key)).toArray();

                        ItemNews item = new ItemNews();
                        item.setBody(m.getString("txt"));
                        item.setId(m.getString("id"));
                        item.setTitle(m.getString("ttl"));

                        if (newsId != null && item.getId().equals(newsId)) {
                            num = counter;
                        }
                        ArrayList<ItemNews> arItem = new ArrayList<>();
                        arItem.add(item);
                        news.add(arItem);
                        titles.add(item.getTitle());
                    }
                    counter++;
                }
            }
            if (news != null) {
                NewsExpListAdapter newsAdapter = new NewsExpListAdapter(context, titles, news);
                ExpandableListView expView = ((Activity)context).findViewById(R.id.expViewNews);
                expView.setAdapter(newsAdapter);
                if (num > 0) {
                    expView.expandGroup(num);
                }
            } else {

                TextView textView = ((Activity)context).findViewById(R.id.newsErrorView);
                textView.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            Log.e("FragmentNews", e.getMessage());
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }
}
