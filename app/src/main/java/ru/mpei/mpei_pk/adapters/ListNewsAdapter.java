package ru.mpei.mpei_pk.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import java.util.ArrayList;


import ru.mpei.mpei_pk.Fragments.FragmentNews;
import ru.mpei.mpei_pk.R;
import ru.mpei.mpei_pk.activities.MainActivity;
import ru.mpei.mpei_pk.dataTypes.ItemNews;

public class ListNewsAdapter extends RecyclerView.Adapter<ListNewsAdapter.ViewHolder> {
    private MainActivity context;
    private ArrayList<ItemNews> news;

    public ListNewsAdapter(Context context, ArrayList<ItemNews> news){
        this.context = (MainActivity)context;
        this.news = news;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_list_item_news, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.setTag(news.get(position));

        ItemNews item = news.get(position);

        holder.title.setText(item.getTitle());
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            ItemNews item = (ItemNews) itemView.getTag();

            title = (TextView) itemView.findViewById(R.id.listNewsTitle);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ItemNews item = (ItemNews) view.getTag();
                    Bundle args = new Bundle();
                    args.putString("newsId",item.getId());
                    Fragment fragment = FragmentNews.newInstance();
                    fragment.setArguments(args);
                    FragmentManager fragmentManager = context.getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.flContent, fragment, fragment.getClass().getSimpleName())
                            .addToBackStack(null)
                            .commit();
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return news.size();
    }

}
