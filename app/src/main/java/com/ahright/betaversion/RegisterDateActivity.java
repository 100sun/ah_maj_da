package com.ahright.betaversion;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.OrientationHelper;

import com.applikeysolutions.cosmocalendar.view.CalendarView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class RegisterDateActivity extends AppCompatActivity {

    private static final String TAG = "RegisterDateActivity";
    private CalendarView calendarView;
    Button next;
    double latitude, longitude;
    String Address;
    int Icon;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        next = findViewById(R.id.next);

        // RegisterAreaActivity 에서 정보들 받아오기
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            latitude = bundle.getDouble("latitude");
            longitude = bundle.getDouble("longitude");
            Address = bundle.getString("address");
            Icon = bundle.getInt("Icon");
        }

        // 다음 버튼 클릭시 날짜 등록
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Calendar> days = calendarView.getSelectedDates();
                // 시작날짜
                Calendar startCalendar = days.get(0);
                final int startDay = startCalendar.get(Calendar.DAY_OF_MONTH);
                final int startMonth = startCalendar.get(Calendar.MONTH);
                final int startYear = startCalendar.get(Calendar.YEAR);
                String startDate = startYear + "-" + (startMonth + 1) + "-" + startDay;
                // 종료날짜
                Calendar endCalendar = days.get(days.size() - 1);
                final int endDay = endCalendar.get(Calendar.DAY_OF_MONTH);
                final int endMonth = endCalendar.get(Calendar.MONTH);
                final int endYear = endCalendar.get(Calendar.YEAR);
                String endDate = endYear + "-" + (endMonth + 1) + "-" + endDay;
                // date정보들까지 더해서 RegisterInfoActivity에 넘겨주기
                Intent intent = new Intent(getApplicationContext(), RegisterAreaActivity.class);
                intent.putExtra("startDate", startDate);
                intent.putExtra("endDate", endDate);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("address", Address);
                intent.putExtra("Icon", Icon);
                startActivity(intent);
                finish();
            }
        });
        initViews();
    }

    private void setSupportActionBar(Toolbar viewById) {
    }

    // 달력 초기화
    private void initViews() {
        calendarView = (CalendarView) findViewById(R.id.calendar_view);
        calendarView.setCalendarOrientation(OrientationHelper.HORIZONTAL);
    }

    // 메뉴바 연결
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    // 메뉴버튼 클릭 시
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // 버리기
            case R.id.clear_selections:
                clearSelectionsMenuClick();
                return true;
            // 보여주기
            case R.id.show_selections:
                Log.e(TAG, "getDate");
                String result = "";

                List<Calendar> days = calendarView.getSelectedDates();
                for (int i = 0; i < days.size(); i++) {
                    Calendar calendar = days.get(i);
                    final int day = calendar.get(Calendar.DAY_OF_MONTH);
                    final int month = calendar.get(Calendar.MONTH);
                    final int year = calendar.get(Calendar.YEAR);
                    String week = new SimpleDateFormat("EE").format(calendar.getTime());
                    String day_full = year + "년 " + (month + 1) + "월 " + day + "일 " + week + "요일";
                    result += (day_full + "\n");
                }
                Toast.makeText(RegisterDateActivity.this, result, Toast.LENGTH_LONG).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearSelectionsMenuClick() {
        calendarView.clearSelections();
    }
}