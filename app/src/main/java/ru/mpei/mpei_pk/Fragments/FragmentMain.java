package ru.mpei.mpei_pk.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.andexert.expandablelayout.library.ExpandableLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import java.util.Locale;
import java.util.Map;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ailis.pherialize.MixedArray;
import de.ailis.pherialize.Pherialize;

import ru.mpei.mpei_pk.ProtocolMPEI;
import ru.mpei.mpei_pk.R;
import ru.mpei.mpei_pk.activities.MainActivity;
import ru.mpei.mpei_pk.adapters.ListNewsAdapter;
import ru.mpei.mpei_pk.dataTypes.ItemNews;

import static ru.mpei.mpei_pk.activities.MainActivity.timer;

public class FragmentMain extends Fragment{

    private ProtocolMPEI protocolMPEI;
    private MyTimerTask timerTask;
    private Context context;
    private static boolean runTimer = true;

    private ExpandableLayout expQueue;
    private ExpandableLayout expRooms;
    private ExpandableLayout expNews;


    public FragmentMain() {
        // Required empty public constructor
    }

    public static FragmentMain newInstance() {
        return new FragmentMain();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        protocolMPEI = new ProtocolMPEI(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            NavigationView navigation =  ((Activity)context).findViewById(R.id.nav_view);
            navigation.getMenu().getItem(0).setChecked(true);

            expQueue = ((Activity)context).findViewById(R.id.expandableQueue);
            expRooms = ((Activity)context).findViewById(R.id.expandableRooms);
            expNews = ((Activity)context).findViewById(R.id.expandableNews);

            TextView textView;
            FrameLayout layout;

            layout = expQueue.getHeaderLayout();
            textView = layout.findViewById(R.id.groupTitle);
            textView.setText("Состояние очереди");
            layout.findViewById(R.id.groupLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView iv = v.findViewById(R.id.groupIcon);
                    if (expQueue.isOpened()) {
                        iv.setImageResource(R.drawable.ic_expand);
                        expQueue.hide();
                    } else {
                        expQueue.show();
                        iv.setImageResource(R.drawable.ic_collapse);
                    }
                }
            });

            layout = expRooms.getHeaderLayout();
            textView = layout.findViewById(R.id.groupTitle);
            textView.setText("Электронная очередь");
            layout.findViewById(R.id.groupLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView iv = v.findViewById(R.id.groupIcon);
                    if (expRooms.isOpened()) {
                        iv.setImageResource(R.drawable.ic_expand);
                        expRooms.hide();
                    } else {
                        expRooms.show();
                        iv.setImageResource(R.drawable.ic_collapse);
                    }
                }
            });
            layout = expNews.getHeaderLayout();
            textView = layout.findViewById(R.id.groupTitle);
            textView.setText("Новости");
            layout.findViewById(R.id.groupLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView iv = v.findViewById(R.id.groupIcon);
                    if (expNews.isOpened()) {
                        iv.setImageResource(R.drawable.ic_expand);
                        expNews.hide();
                    } else {
                        expNews.show();
                        iv.setImageResource(R.drawable.ic_collapse);
                    }
                }
            });

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Date now = new Date();
                        Calendar calendar = Calendar.getInstance(new Locale("ru", "RU"));
                        calendar.setTime(now);
                        int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);

                        ((MainActivity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ProgressBar progressBar = ((MainActivity) context).findViewById(R.id.progressBarMain);
                                progressBar.setVisibility(ProgressBar.VISIBLE);
                            }
                        });
                        String queue_numbers, queue_load;
                        if (day > 0 && day <= 5 && hour >= 10 && (((hour < 17 && day == 5) || hour < 18))) {// || flzero)) {
                            queue_numbers = protocolMPEI.get_queue_numbers();
                            queue_load = protocolMPEI.get_queue_load();
                        } else {
                            queue_load = null;
                            queue_numbers = null;
                        }
                        try {
                            SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                            int token = sharedPref.getInt("newsToken", 0);
                            String news = protocolMPEI.get_news(Integer.toString(token));

                            if (news != null) {
                                MixedArray mixedArray = Pherialize.unserialize(news).toArray();
                                int currentToken;
                                try {
                                    currentToken = Integer.parseInt(mixedArray.getString("token"));
                                } catch (Exception e) {
                                    currentToken = 1;
                                }
                                if (currentToken > token) {
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putInt("newsToken", currentToken);
                                    editor.apply();

                                    sharedPref = context.getSharedPreferences("news", Context.MODE_PRIVATE);
                                    editor = sharedPref.edit();
                                    if (mixedArray.containsKey("Actual")) {
                                        MixedArray m = mixedArray.getArray("Actual");
                                        for (Object key : m.keySet()) {
                                            editor.putString(key.toString(), Pherialize.serialize(m.get(key)));
                                        }
                                    }
                                    if (mixedArray.containsKey("Deleted")) {
                                        MixedArray m = mixedArray.getArray("Deleted");
                                        for (Object key : m.keySet()) {
                                            editor.remove(m.getString(key));
                                        }
                                    }
                                    editor.apply();

                                }
                            }
                        } catch (Exception e) {
                            Log.e("FragmentMain", e.getMessage());
                        }
                        displayRooms(queue_numbers);
                        displayQueue(queue_load);

                        ((MainActivity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                drawNews();

                                ProgressBar progressBar = ((MainActivity) context).findViewById(R.id.progressBarMain);
                                progressBar.setVisibility(ProgressBar.INVISIBLE);

                                expQueue.show();
                            }
                        });
                        timerTask = new MyTimerTask();
                        timer.schedule(timerTask, 30000, 30000);
                    }catch (Exception e) {
                        Log.e("FragmentMain", e.getMessage());
                    }
                }
            }).start();

        } catch (Exception e) {
            Log.e("FragmentMain", e.getMessage());
        }
    }

    @Override
    public void onPause() {
        runTimer = false;
        super.onPause();
    }

    @Override
    public void onResume() {
        runTimer = true;
        super.onResume();
    }

    @Override
    public void onDestroy() {
        runTimer = false;
        try {
            if (timerTask != null) {
                timerTask.cancel();
                timerTask = null;
            }
        } catch (Exception e) {
            Log.e("FragmentMain", e.getMessage());
        }
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                if (runTimer) {
                    Date now = new Date();
                    Calendar calendar = Calendar.getInstance(new Locale("ru", "RU"));
                    calendar.setTime(now);
                    int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);

                    if (day > 0 && day <= 5 && hour >= 10 && (((hour < 17 && day == 5) || hour < 18))) {// || flzero)) {
                        String queue_load = protocolMPEI.get_queue_load();
                        String queue_numbers = protocolMPEI.get_queue_numbers();

                        displayQueue(queue_load);
                        displayRooms(queue_numbers);
                    }
                } else {
                    this.cancel();
                }
            } catch (Exception e) {
                Log.e("FragmentMain", e.getMessage());
            }
        }
    }
    private void displayQueue(final String queue)
    {
        try {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (queue != null) {
                        expQueue.setVisibility(View.VISIBLE);

                        int size = ((Activity)context).findViewById(R.id.mainWrapper).getWidth() / 10;
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size * 2);

                        LinearLayout linearLayout = expQueue.getContentLayout().findViewById(R.id.photosItemQueue);
                        linearLayout.removeAllViews();

                        int len;
                        String[] ar_info = queue.split("&");
                        try {
                            len = Integer.parseInt(ar_info[0]);
                        } catch (Exception e) {
                            len = 0;
                        }
                        if (len < 0) {
                            len = 0;
                        } else if (len > 10) {
                            len = 10;
                        }
                        for (int i = 1; i <= len; i++) {
                            ImageView imageView = new ImageView(context);
                            if (len <= 4) {
                                imageView.setImageResource(R.drawable.ic_person_green);
                            } else if (len <= 7) {
                                imageView.setImageResource(R.drawable.ic_person_yellow);
                            } else {
                                imageView.setImageResource(R.drawable.ic_person_red);
                            }
                            imageView.setLayoutParams(layoutParams);
                            linearLayout.addView(imageView);
                        }
                        for (int i = len + 1; i <= 10; i++) {
                            ImageView imageView = new ImageView(context);
                            imageView.setImageResource(R.drawable.ic_person_gray);
                            imageView.setLayoutParams(layoutParams);
                            linearLayout.addView(imageView);
                        }
                        String caption = ar_info[2] + " на " + ar_info[1];

                        TextView textChild = expQueue.getContentLayout().findViewById(R.id.textItemQueue);
                        textChild.setText(caption);

                    } else {
                        expQueue.setVisibility(View.GONE);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("FragmentMain", e.getMessage());
        }
    }
    private void displayRooms(String rooms)
    {
        try {
            if (rooms != null) {
                final ArrayList<View> tableRowList = new ArrayList<>();

                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);

                TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
                rowParams.span = 2;

                TableRow row = new TableRow(context);
                row.setGravity(Gravity.CENTER);

                TextView textView = new TextView(context);
                textView.setText(getResources().getText(R.string.goto_room_text));
                textView.setTextColor(context.getResources().getColor(R.color.colorBlack));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                textView.setGravity(Gravity.CENTER);
                row.addView(textView);
                tableRowList.add(row);

                tableRowList.add(getHorizontalBorder());

                String pattern = ".*\\[(.*?)]=(\\d{4})";
                Pattern r = Pattern.compile(pattern);

                String[] ar = rooms.split("&");
                int count;
                boolean fl;
                if ((ar.length - 2) % 4 == 0) {
                    fl = false;
                    count = (ar.length - 2) / 4;
                } else {
                    fl = true;
                    count = (ar.length - 2) / 4 + 1;
                }

                for (int i = 0; i < count; i++) {
                    row = new TableRow(context);
                    row.setGravity(Gravity.CENTER_VERTICAL);
                    for (int j = 0; j < 2; j++) {
                        String numbers = "";
                        if (fl && i == count - 1 && j == 1) {
                            numbers = "";
                        } else {
                            Matcher m;
                            try {
                                m = r.matcher(ar[1 + 4 * i + 2 * j]);
                                if (m.find()) {
                                    numbers = "<span style='color:red'><b>" + m.group(1) + "</b></span>" + m.group(2);
                                }
                                numbers += " - ";
                                m = r.matcher(ar[1 + 4 * i + 2 * j + 1]);
                                if (m.find()) {
                                    numbers += "<span style='color:red'><b>" + m.group(1) + "</b></span>" + m.group(2);
                                }
                            } catch (Exception e) {
                                Log.e("Regex", e.getMessage());
                            }
                        }
                        textView = new TextView(context);
                        textView.setText(Html.fromHtml(numbers));
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                        textView.setTextColor(context.getResources().getColor(R.color.colorBlack));
                        textView.setGravity(Gravity.CENTER);
                        textView.setLayoutParams(layoutParams);
                        row.addView(textView);

                    }

                    tableRowList.add(row);
                    tableRowList.add(getHorizontalBorder());
                }
                row = new TableRow(context);
                row.setGravity(Gravity.CENTER);

                textView = new TextView(context);
                textView.setText(String.format("Информация на %s", ar[ar.length - 1]));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                textView.setGravity(Gravity.CENTER);
                textView.setLayoutParams(rowParams);
                row.addView(textView);
                tableRowList.add(row);

                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TableLayout table = ((Activity)context).findViewById(R.id.queue_reserve_table);
                        table.removeAllViews();
                        table.setStretchAllColumns(true);
                        table.setShrinkAllColumns(true);
                        for (View v : tableRowList) {
                            table.addView(v);
                        }
                        expRooms.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        expRooms.setVisibility(View.GONE);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("FragmentMain", e.getMessage());
        }
    }
    private void drawNews() {
        try {
            SharedPreferences sharedPref = context.getSharedPreferences("news", Context.MODE_PRIVATE);
            Map<String, ?> map = sharedPref.getAll();
            ArrayList<ItemNews> news = null;
            if (map != null) {
                news = new ArrayList<>();
                int count = 0;
                for (String key : map.keySet()) {
                    if (count > 2) {
                        break;
                    }
                    if (map.get(key) instanceof String) {
                        MixedArray m = Pherialize.unserialize((String) map.get(key)).toArray();
                        if (m.getString("top").equals("1")) {
                            ItemNews item = new ItemNews();
                            item.setId(m.getString("id"));
                            item.setTitle(m.getString("ttl"));
                            news.add(item);
                            count++;
                        }
                    }
                }
            }
            if (news != null) {
                expNews.setVisibility(View.VISIBLE);
                RecyclerView recyclerView = ((Activity)context).findViewById(R.id.listViewNews);

                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(layoutManager);

                recyclerView.setHasFixedSize(true);
                ListNewsAdapter adapter = new ListNewsAdapter(context, news);
                recyclerView.setAdapter(adapter);

            } else {
                expNews.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e("FragmentMain", e.getMessage());
        }
    }
    private View getHorizontalBorder()
    {
        int border = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());
        View horizontalDivider = new View(context);
        horizontalDivider.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, border));
        horizontalDivider.setBackgroundColor(context.getResources().getColor(R.color.colorBlack));
        return horizontalDivider;
    }
}
