package com.ahright.betaversion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static com.ahright.betaversion.MainActivity.database;

public class RegisterHomeActivity extends AppCompatActivity {
    private static final String TAG = "RegisterHome";
    TextView explanation;
    String address = "  ", gu = "   ";
    public SharedPreferences prefs;
    boolean isFirstRun;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // MapsActivity를 갔다오지 않은 경우(집의 주소를 받아오지 못한 경우)에는 로딩화면 다시 보이기
        Intent mapIntent = getIntent();
        Bundle homeAdBundle = mapIntent.getExtras();

        try {
            address = homeAdBundle.getString("address");
            gu = homeAdBundle.getString("gu");
        } catch (NullPointerException e) {
            Intent fintent = new Intent(this, LoadingActivity.class);
            startActivity(fintent);
        }

        setContentView(R.layout.activity_homead);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#000000'>아! 맞다</font>"));

        prefs = getSharedPreferences("Pref", MODE_PRIVATE);
        isFirstRun = prefs.getBoolean("isFirstRun", true);

        final Button input = findViewById(R.id.input);
        explanation = findViewById(R.id.explanation);
        final Button register = findViewById(R.id.register);

        // 집 주소 받아와서 input에 넣어주기
        input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
            }
        });
        input.setText(address);

        // 등록버튼 클릭 시 메인으로 집 정보 넘겨주고 데이터베이스에서 집 테이블 생성하기
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putBoolean("isFirstRun", false).apply();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("gu", gu);
                intent.putExtra("RegisterHome", address);
                Log.e(TAG, gu + " " + address);
                startActivity(intent);
                finish();
            }
        });
    }
}
