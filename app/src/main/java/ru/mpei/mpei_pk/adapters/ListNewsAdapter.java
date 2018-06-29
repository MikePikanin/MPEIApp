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
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.setTag(news.get(position));

        ItemNews item = news.get(position);

        holder.title.setText(item.getTitle());
        //holder.body.setText(item.getBody());

    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView title;
        //public TextView body;

        public ViewHolder(View itemView) {
            super(itemView);
            ItemNews item = (ItemNews) itemView.getTag();

            title = (TextView) itemView.findViewById(R.id.listNewsTitle);
            //body = (TextView) itemView.findViewById(R.id.listNewsBody);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    ItemNews item = (ItemNews) view.getTag();
                    Bundle args = new Bundle();
                    args.putString("newsId",item.getId());
                    Fragment fragment = (Fragment) FragmentNews.newInstance();
                    fragment.setArguments(args);
                    FragmentManager fragmentManager = context.getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.flContent, fragment)
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
