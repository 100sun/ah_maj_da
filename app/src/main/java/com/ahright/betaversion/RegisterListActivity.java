package com.ahright.betaversion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

import static com.ahright.betaversion.MainActivity.database;

public class RegisterListActivity extends AppCompatActivity {
    private static final String TAG = "RegisterListActivity";
    private static Context mContext;

    ListView menuListview;
    Adapter adapter;
    String name;
    EditText editText;
    ImageView plusblue;
    String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_todo);
        mContext = getApplicationContext();


        menuListview = (ListView) findViewById(R.id.menuListview);
        adapter = new Adapter();
        editText = (EditText) findViewById(R.id.editText);
        plusblue = findViewById(R.id.plusblue);

        //인텐트
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            name = bundle.getString("name");
            address = bundle.getString("address");
        }

        //초기에 한번 띄워주기
        ArrayList<String> result = database.selectAllTodo(name);
        if (result != null) {
            adapter.setItems(result);
            menuListview.setAdapter(adapter);
        }

        //editText color 파란색으로 변경
        int color = Color.parseColor("#4BA3F8");
        editText.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        //p
        plusblue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String todo = editText.getText().toString();
                database.insertTodoRecord(name, todo);
                ArrayList<String> result = database.selectAllTodo(name);
                adapter.setItems(result);
                menuListview.setAdapter(adapter);
                editText.setText(null);
            }
        });


        //확인 버튼 누를 시 main으로 넘어감
        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("address",address);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);

            }
        });

    }

    class Adapter extends BaseAdapter {
        ArrayList<String> items = new ArrayList<String>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void setItems(ArrayList<String> items) {
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
            ChecklistView view = new ChecklistView(mContext);
            String item = items.get(position);
            view.setTodoText(item);
            return view;
        }
    }

}


