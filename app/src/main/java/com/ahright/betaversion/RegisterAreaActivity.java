package com.ahright.betaversion;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import static com.ahright.betaversion.MainActivity.database;

public class RegisterAreaActivity extends AppCompatActivity {
    private static final String TAG = "RegisterAreaActivity";
    public static Context RegisterAreaInfoContext;

    //위치,날짜,장소별칭
    double latitude;
    double longitude;
    String Address;
    String StartDay = null;
    String EndDay = null;
    EditText editText;

    //아이콘 설정
    FrameLayout houseFrame;
    FrameLayout schoolFrame;
    FrameLayout companyFrame;
    FrameLayout ectFrame;
    ImageView houseCheck;
    ImageView schoolCheck;
    ImageView companyCheck;
    ImageView ectCheck;
    boolean check = false;
    EditText dateText;
    int Icon = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newname);
        RegisterAreaInfoContext = getApplicationContext();

        editText = findViewById(R.id.editText2);
        dateText = findViewById(R.id.editText3);
        houseFrame = findViewById(R.id.houseFrame);
        schoolFrame = findViewById(R.id.schoolFrame);
        companyFrame = findViewById(R.id.companyFrame);
        ectFrame = findViewById(R.id.ectFrame);

        houseCheck = findViewById(R.id.housechecked);
        schoolCheck = findViewById(R.id.schoolchecked);
        companyCheck = findViewById(R.id.companychecked);
        ectCheck = findViewById(R.id.ectchecked);

        getSupportActionBar().setTitle(Html.fromHtml("<font color='#000000'>아! 맞다</font>"));


        //같은 테이블 이름 검사할 arrayList
        ArrayList<AreaInfo> allAreaInfo = database.selectAllArea();
        final ArrayList<String> tableName = new ArrayList<>();
        for(int i = allAreaInfo.size()-1; i >= 0 ; i--){
            String myname = allAreaInfo.get(i).getName();
            tableName.add(0,myname);
        }

        //인텐트
        Intent intent = getIntent();
        if(intent!=null){
            Bundle bundle = intent.getExtras();
            latitude =bundle.getDouble("latitude");
            longitude =bundle.getDouble("longitude");
            Address = bundle.getString("address");
        }

        //아이콘 선택
        houseFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(houseCheck.getVisibility() == View.INVISIBLE && check == false){
                    houseCheck.setVisibility(View.VISIBLE);
                    Icon = 1;
                    check = true;
                } else if(houseCheck.getVisibility() == View.VISIBLE && check == true){
                    houseCheck.setVisibility(View.INVISIBLE);
                    check = false;
                    Icon = 4;
                }
            }
        });


        schoolFrame.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(schoolCheck.getVisibility() == View.INVISIBLE && check == false) {
                    schoolCheck.setVisibility(View.VISIBLE);
                    check = true;
                    Icon = 2;
                } else if(schoolCheck.getVisibility() == View.VISIBLE && check == true){
                    schoolCheck.setVisibility(View.INVISIBLE);
                    check = false;
                    Icon = 4;
                }

            }
        });

        companyFrame.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(companyCheck.getVisibility() == View.INVISIBLE && check == false) {
                    companyCheck.setVisibility(View.VISIBLE);
                    check = true;
                    Icon = 3;
                } else if(companyCheck.getVisibility() == View.VISIBLE && check == true){
                    companyCheck.setVisibility(View.INVISIBLE);
                    check = false;
                    Icon = 4;
                }

            }
        });


        ectFrame.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(ectCheck.getVisibility() == View.INVISIBLE && check == false) {
                    ectCheck.setVisibility(View.VISIBLE);
                    check = true;
                    Icon = 4;
                } else if(ectCheck.getVisibility() == View.VISIBLE && check == true){
                    ectCheck.setVisibility(View.INVISIBLE);
                    check = false;
                    Icon = 4;
                }

            }
        });



        if(StartDay==null&&EndDay==null){
            dateText.setText("날짜를 선택해주세요");
        }



        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),RegisterDateActivity.class);
                String name = editText.getText().toString();
                intent.putExtra("name",name);
                intent.putExtra("latitude",latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("address",Address);
                intent.putExtra("Icon" , Icon);
                startActivity(intent);
                finish();
            }
        });

        Intent dateIntent = getIntent();
        if(dateIntent != null){
            Bundle dateBundle = dateIntent.getExtras();
            StartDay = dateBundle.getString("startDate");
            EndDay = dateBundle.getString("endDate");
            latitude =dateBundle.getDouble("latitude");
            longitude =dateBundle.getDouble("longitude");
            Address = dateBundle.getString("address");
            Icon = dateBundle.getInt("Icon");
        }


        if(Icon != 0) {
            switch (Icon) {
                case 1 : houseCheck.setVisibility(View.VISIBLE);break;
                case 2 : schoolCheck.setVisibility(View.VISIBLE);break;
                case 3 : companyCheck.setVisibility(View.VISIBLE);break;
                    default:ectCheck.setVisibility(View.VISIBLE);
            }
            check = true;
        }


        int color = Color.parseColor("#000000"); // 날짜가 들어오면 text 검정색으로 고정


        if(StartDay != null && StartDay.equals(EndDay)){
            dateText.setText(StartDay);
            dateText.setTextColor(color);
        }else if(StartDay != null){
            dateText.setText(StartDay + "  ~  " + EndDay);
            dateText.setTextColor(color);
        }


        //등록버튼
        Button button = (Button) findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterListActivity.class);
                String Nickname = editText.getText().toString();
                boolean isSame = false;
                //공백이 있다면 replace
                Nickname = Nickname.replace(" ", "z");

                //같은 이름의 별칭이 있을 때 재등록 알림
                for (int i = 0; i < tableName.size(); i++) {
                    if (Nickname.equals(tableName.get(i))) {
                        isSame = true;
                        Toast.makeText(RegisterAreaInfoContext, "같은 별칭이 있습니다. 다른 이름을 등록해주세요", Toast.LENGTH_LONG).show();
                        editText.setText(null);
                        break;
                    }
                }

                if (isSame == false) {
                    database.insertAreaRecord(latitude, longitude, Address, Nickname, StartDay, EndDay, Icon);
                    database.createTodoTable(Nickname);
                    intent.putExtra("name", Nickname);
                    startActivity(intent);
                    finish();
                }
            }
        });


    }

}
