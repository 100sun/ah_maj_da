package com.ahright.betaversion;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static Context MainContext, mContext;
    static Database database;
    Intent foregroundServiceIntent;

    //about UI
    static Drawable alpha;
    static ImageView img;
    static TextView textView;
    static TextView textView2;
    //set Dust and Weather
    TextView DustAlert;
    String DustMainGrade;
    ImageView DustImage, WeatherImage;
    static Handler handler;
    String RegisterHome, dustIcon, weatherIcon= "04d";

    // for RegisterHome();
    FrameLayout houseCardView;
    public SharedPreferences prefs;
    String myCity;
    static String city;
    static ProgressBar progressBarForHouse;


    //for ListView
    static ListView menuListview;
    static MainViewAdapter mainViewAdapter;
    boolean isClick = false;
    String ClickItem = "";

    // for receiverMaker();
    LocationManager locManager;
    ProximityReceiver receiver;
    PendingIntent proximityIntent;
    IntentFilter filter;
    ArrayList<String> alias = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#000000'>아! 맞다</font>"));

        //About Api
        DustImage = findViewById(R.id.DustImage);
        WeatherImage = findViewById(R.id.WeatherImage);
        DustAlert = findViewById(R.id.DustAlert);
        // 날씨 API 등록
/*        resetApi(getApplicationContext());
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                ArrayList<String> list = (ArrayList<String>) msg.obj;
                dustIcon = list.get(0);
                weatherIcon = list.get(1);

                if (weatherIcon.contains("n")) {
                    weatherIcon = weatherIcon.replace("n", "d");
                }

                Log.e("Handler", "메세지 받음, dustIcon: " + dustIcon + ", weatherIcon: " + weatherIcon);
            }
        };

        Glide.with(MainActivity.this).load("http://openweathermap.org/img/w/" + weatherIcon + ".png").into(WeatherImage);
*/
        // 로딩화면
        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);

        // 앱 첫 실행인지 유무에 따른 집 등록
        prefs = getSharedPreferences("Pref", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

        if (isFirstRun == true) {
            Intent homeIntent = new Intent(getApplicationContext(), RegisterHomeActivity.class);
            startActivity(homeIntent);
        }
        else{
            if (null == UndeadService.serviceIntent) {
                foregroundServiceIntent = new Intent(this, UndeadService.class);
                startService(foregroundServiceIntent);
               // Toast.makeText(getApplicationContext(), "start service", Toast.LENGTH_LONG).show();
            } else {
                foregroundServiceIntent = UndeadService.serviceIntent;
              //  Toast.makeText(getApplicationContext(), "already", Toast.LENGTH_LONG).show();
            }
        }

        // 집 주소에서 "oo구" 받아오기
        try {
            Intent mapIntent = getIntent();
            Bundle homeAdBundle = mapIntent.getExtras();
            city = homeAdBundle.getString("gu");
            RegisterHome = homeAdBundle.getString("RegisterHome");
        } catch (NullPointerException e) {
            Log.e(TAG, "초기설정화면 불필요");
        }

        database = Database.getInstance(this);
        boolean isOpen = database.open();
        if (isOpen) {
            Log.d(TAG, "MainActivity database is open.");
        } else {
            Log.d(TAG, "MainActivity database is not open.");
        }

        // Database에 집 추가
        try {
            if (city != null) {
                AreaInfo result = database.selectSpecificArea("집");
                if (result.getStartDate().equals("a")) {
                    database.execSQL("UPDATE " + database.TABLE_AREA_INFO + " SET STARTDATE = '" + city + "' WHERE NAME = '" + "집" + "'");
                    database.execSQL("UPDATE " + database.TABLE_AREA_INFO + " SET ADDRESS = '" + RegisterHome + "' WHERE NAME = '" + "집" + "'");
                }
            }
        } catch (NullPointerException e) {
            myCity = "서울";
        }

        //집 주소 등록
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        // main 화면의 집 cardview 구성
        houseCardView = findViewById(R.id.houseCardView);
        progressBarForHouse = findViewById(R.id.progressBar);

        houseCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DetailViewActivity.class);
                intent.putExtra("name", "집");
                Log.e("houseCardView", "집 넘어감");
                startActivityForResult(intent, 101);
            }
        });

        // 집 할일 체크에 따른 달성률 표시
        ArrayList<String> lists = database.selectAllTodo("집");
        ArrayList<Integer> checkedLists = database.selectAllChecked("집");
        int allCount = lists.size();
        int checkedCount = 0;
        for (int i = 0; i < checkedLists.size(); i++) {
            if (checkedLists.get(i) == 1)
                checkedCount++;
        }

        progressBarForHouse.setMax(allCount);
        progressBarForHouse.setProgress(checkedCount);

        //set start UI
        alpha = ((ImageView) findViewById(R.id.imageView)).getDrawable();
        img = (ImageView) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);
        //set ListView
        MainContext = getApplicationContext();
        mContext = getApplicationContext();
        menuListview = findViewById(R.id.menuListview);
        mainViewAdapter = new MainViewAdapter();
        menuListview.setAdapter(mainViewAdapter);

        //onclick 되면 별칭별 할 일을 보여주는 화면으로 넘어간
        menuListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                AreaInfo result = (AreaInfo) mainViewAdapter.getItem(position);
                String name = result.getName();
                Intent intent = new Intent(getApplicationContext(), DetailViewActivity.class);
                intent.putExtra("name", name);

                startActivityForResult(intent, 101);

                Log.d("onClick", name);
            }
        });

        //길게 눌렀을 때 삭제
        menuListview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                AreaInfo area = (AreaInfo) mainViewAdapter.getItem(position);
                String name = area.getName();

                if (isClick == false) {
                    ClickItem = name;
                    isClick = true;
                    Toast.makeText(MainContext, "한 번 더 꾹 누르면 삭제됩니다", Toast.LENGTH_LONG).show();
                    Log.e("CLick ", "한 번 더 눌어야 함");
                    return true;
                }

                if (isClick == true && ClickItem.equals(name)) {
                    Log.e(TAG, name + "삭제되어야함");
                    database.deleteAreaRecord(name);
                    updateUI();
                    Log.e(TAG, "삭제 receiverMAker call");
                    receiverMaker();
                    Log.e("LongClick 삭제", ClickItem + "삭제됨%%%%%%%%%%%%");
                    isClick = false;
                } else if (!ClickItem.equals(name)) {
                    ClickItem = name;
                    isClick = true;
                    Toast.makeText(MainContext, "한 번 더 꾹 누르면 삭제됩니다", Toast.LENGTH_LONG).show();
                    Log.e("Click", "다른 거 눌려서 한 번 더 눌려야 함");
                }


                return true;
            }
        });

        //plus button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivityForResult(intent, 101);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        resetApi(getApplicationContext());
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                ArrayList<String> list = (ArrayList<String>) msg.obj;
                dustIcon = list.get(0);
                weatherIcon = list.get(1);

                if (weatherIcon.contains("n")) {
                    weatherIcon = weatherIcon.replace("n", "d");
                }

                Log.e("Handler", "메세지 받음, dustIcon: " + dustIcon + ", weatherIcon: " + weatherIcon);
            }
        };

        Glide.with(MainActivity.this).load("http://openweathermap.org/img/w/" + weatherIcon + ".png").into(WeatherImage);

        AreaInfo result2 = database.selectSpecificArea("집");
        DustMainGrade = dustIcon;
        if (!result2.getStartDate().equals("a")) {
            Log.e(TAG, result2.getStartDate());
            myCity = result2.getStartDate();
            DustAlert.setText("현재 " + myCity);
        }

        if (DustMainGrade != null) {
            switch (DustMainGrade) {
                case "1":
                    DustImage.setImageResource(R.drawable.good);
                    break;
                case "4":
                    DustImage.setImageResource(R.drawable.verybad);
                    break;
                case "3":
                    DustImage.setImageResource(R.drawable.bad);
                    break;
                default:
                    DustImage.setImageResource(R.drawable.soso);
            }
        }

        updateUI();  //ListView Update
        receiverMaker();  //add location ProximityAlert

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != foregroundServiceIntent) {
            stopService(foregroundServiceIntent);
            foregroundServiceIntent = null;
        }
    }


    /***************************** about UI ****************************/
    static void updateUI() {

        ArrayList<AreaInfo> area = database.selectAllArea();
        ArrayList<String> homeTodo = database.selectAllTodo("집");
        mainViewAdapter.setItems(area);
        mainViewAdapter.notifyDataSetChanged();

        if (area.size() < 2 && homeTodo.size() < 1) {
            alpha.setAlpha(50);
            img.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
            textView2.setVisibility(View.VISIBLE);
        } else {
            img.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
            textView2.setVisibility(View.GONE);
        }


        ArrayList<String> lists = database.selectAllTodo("집");
        ArrayList<Integer> checkedLists = database.selectAllChecked("집");
        int allCount = lists.size();
        int checkedCount = 0;
        for (int i = 0; i < checkedLists.size(); i++) {
            if (checkedLists.get(i) == 1)
                checkedCount++;
        }

        progressBarForHouse.setMax(allCount);
        progressBarForHouse.setProgress(checkedCount);

    }

    //main cardView adapter
    class MainViewAdapter extends BaseAdapter {
        ArrayList<AreaInfo> items = new ArrayList<AreaInfo>();

        @Override
        public int getCount() {
            return items.size();
        }


        public void setItems(ArrayList<AreaInfo> items) {
            this.items = items;
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            MainView view = new MainView(MainContext);

            AreaInfo result = (AreaInfo) items.get(position);
            String tableName = result.getName();
            tableName = tableName.replace("z", " ");
            view.setTextView(tableName);

            if (result.getStartDate().equals(result.getEndDate()))
                view.setDateText(result.getStartDate());
            else
                view.setDateText(result.getStartDate() + " ~ " + result.getEndDate());

            // PROGRESSBAR
            ArrayList<String> lists = database.selectAllTodo(result.getName());
            ArrayList<Integer> checkedLists = database.selectAllChecked(result.getName());
            int allCount = lists.size();
            int checkedCount = 0;
            for (int i = 0; i < checkedLists.size(); i++)
                if (checkedLists.get(i) == 1) checkedCount++;
            view.setAchievement(allCount, checkedCount);
            view.setImageIconView(result.getIcon());

            if (result.getName().equals("집")) {
                view.setHouseView("집");
            }

            return view;
        }
    }


    /*******************About Api***************************/
    public static void resetApi(Context context) {
        AlarmManager resetAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent resetIntent = new Intent(context, UpdateApiReceiver.class);

        PendingIntent resetSender = PendingIntent.getBroadcast(context, 100, resetIntent, 0);

        // 자정 시간
        Calendar resetCal = Calendar.getInstance();
        resetCal.setTimeInMillis(System.currentTimeMillis());
        resetCal.set(Calendar.HOUR_OF_DAY, 0);
        resetCal.set(Calendar.MINUTE, 0);
        resetCal.set(Calendar.SECOND, 0);

        try {
            resetAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    resetCal.getTimeInMillis() ,
                     30 * 60 * 1000,
                    resetSender);
        } catch (Exception e) {
            Log.e("Alarm 등록", "Exception in resetApi()");
        }

        //
        SimpleDateFormat format = new SimpleDateFormat("MM/dd kk:mm:ss");
        String setResetTime = format.format(new Date(resetCal.getTimeInMillis() + 30* 60 * 1000));

        Log.d("resetApi", "ResetHour : " + setResetTime);
    }


    /***************************** about MAP ****************************/
    // 장소 근접경보알림함수에 등록
    public void receiverMaker() {
        // 등록된 장소 위치 데이터베이스에서 가져오기
        ArrayList<AreaInfo> result = database.selectAllArea();
        alias = new ArrayList<>();

        Intent intent = new Intent("com.ahright.betaversion");

        try {
            for (int i = 0; i < result.size(); i++) {
                intent.putExtra("alias", result.get(i).getName());
                intent.putExtra("id", i);
                proximityIntent = PendingIntent.getBroadcast(getApplicationContext(), i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                // 집은 반경10 m, 나머지 장소는 반경 300m
                if (alias.equals("집"))
                    locManager.addProximityAlert(result.get(i).getLatitude(), result.get(i).getLongitude(), 10, -1, proximityIntent);
                else
                    locManager.addProximityAlert(result.get(i).getLatitude(), result.get(i).getLongitude(), 200, -1, proximityIntent);
            }
        } catch (SecurityException e) {

            e.printStackTrace();
        }

        // ProximityReceiver 등록 코드
        filter = new IntentFilter("com.ahright.betaversion");
        receiver = new ProximityReceiver();
        registerReceiver(receiver, filter);

    }

}

