package com.ahright.betaversion;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import static com.ahright.betaversion.MainActivity.database;
import static com.ahright.betaversion.MainActivity.textView;

public class DetailViewActivity extends AppCompatActivity {
    private static final String TAG = "CardView";
    public static Context mContext;
    TextView tableName;
    ImageView plusButton;
    ImageView trashCan;
    ImageView tableIcon;
    ListView ForChecklistView;
    String mytable;
    String myTableChangeName;
    ArrayList<String> todo;
    ArrayList<Integer> Checking;
    CheckItemAdapter checkItemAdapter;
    TextView dataTextInList;
    TextView addressText;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_layout);
        mContext = getApplicationContext();

        tableName = findViewById(R.id.tabletextView);
        plusButton = findViewById(R.id.imageView3);
        trashCan = findViewById(R.id.imageView2);
        ForChecklistView = findViewById(R.id.Forcheckboxlist);
        dataTextInList = findViewById(R.id.dataTextInList);
        tableIcon = findViewById(R.id.imageWithTableName);
        addressText = findViewById(R.id.addressText);


        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            mytable = bundle.getString("name");
        }


        //table name에 맞는 정보 삽입
        String startday = database.selectSpecificArea(mytable).getStartDate();
        String endday = database.selectSpecificArea(mytable).getEndDate();
        Integer iconnumber = database.selectSpecificArea(mytable).getIcon();
        String Address = database.selectSpecificArea(mytable).getAddress();
        Checking = database.selectAllChecked(mytable);
        todo = database.selectAllTodo(mytable);
        myTableChangeName = mytable.replace("z"," ");


        //집일 경우 날짜와 쓰레기통 삭제
        if(mytable.equals("집")){
            dataTextInList.setVisibility(View.GONE);
            trashCan.setVisibility(View.GONE);
        }

        if (startday.equals(endday))
            dataTextInList.setText(startday);
        else
            dataTextInList.setText("[ " + startday + " ~ " + endday + " ]");


        tableName.setText(myTableChangeName);
        addressText.setText(Address);
        checkItemAdapter = new CheckItemAdapter();
        checkItemAdapter.setItems(Checking, todo);
        ForChecklistView.setAdapter(checkItemAdapter);
        ForChecklistView.setItemsCanFocus(false);
        setImageIcon(iconnumber);


        //글자누르면 수정
        ForChecklistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String oldTodo = (String) checkItemAdapter.getTextItem(i);
                modifyDialog(oldTodo);
            }
        });


        //집일 경우 꾹 누르면 삭제
        if(mytable.equals("집")) {
            ForChecklistView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Toast.makeText(mContext,"삭제되었습니다.",Toast.LENGTH_LONG).show();
                    String delete = (String) checkItemAdapter.getTextItem(i);
                    database.deleteTodoRecord(delete);

                    ArrayList<String> result = database.selectAllTodo(mytable);
                    ArrayList<Integer> checked = database.selectAllChecked(mytable);

                    checkItemAdapter.setItems(checked, result);
                    checkItemAdapter.notifyDataSetChanged();
                    return true;
                }
            });
        }

        //plusButton 누르면 추가
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeAlertDialoge();
            }
        });

        //trashcan 누르면 삭제
        trashCan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QuestionDialog();
            }
        });

    }

    public void makeAlertDialoge() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("할 일을 추가해주세요");
        final EditText ed = new EditText(this);
        builder.setView(ed);

        builder.setPositiveButton("추가", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String todo;
                todo = ed.getText().toString();
                database.insertTodoRecord(mytable, todo);
                checkItemAdapter.addItem(todo);
                checkItemAdapter.addItem(0);
                checkItemAdapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        builder.show();
    }

    public void modifyDialog(final String oldTodo) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("할 일을 수정해주세요");
        final EditText ed = new EditText(this);
        builder.setView(ed);

        builder.setPositiveButton("수정완료", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String newTodo;
                newTodo = ed.getText().toString();
                database.modifyTodo(mytable, oldTodo, newTodo);
                UpdateUI();
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        builder.show();
    }


    public void QuestionDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("정말 삭제하시겠습니까?");
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                database.deleteAreaRecord(mytable);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        builder.show();
    }


    public void UpdateUI() {
        ArrayList<String> result = database.selectAllTodo(mytable);
        ArrayList<Integer> checked = database.selectAllChecked(mytable);
        checkItemAdapter.setItems(checked, result);
        checkItemAdapter.notifyDataSetChanged();
    }


    public void setImageIcon(int num) {
        switch (num) {
            case 1:
                tableIcon.setImageResource(R.drawable.house);
                break;
            case 2:
                tableIcon.setImageResource(R.drawable.school);
                break;
            case 3:
                tableIcon.setImageResource(R.drawable.companynew);
                break;
            default:
                tableIcon.setImageResource(R.drawable.ect);
        }
    }


    class CheckItemAdapter extends BaseAdapter {
        ArrayList<Integer> items = new ArrayList<>();
        ArrayList<String> todoForme = new ArrayList<>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(Integer item) {
            items.add(item);
        }

        public void addItem(String todo) {
            todoForme.add(todo);
        }

        public void setItems(ArrayList<Integer> items, ArrayList<String> todoForme) {
            this.items = items;
            this.todoForme = todoForme;
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        public String getTextItem(int position) {
            return todoForme.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup viewGroup) {
            final ChecklistView view = new ChecklistView(mContext);
            Integer integer = items.get(position);
            String name = todoForme.get(position);

            view.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View myview) {
                    if (view.checkBox.isChecked()) {
                        view.checkBox.setChecked(true);
                        database.modifyChecked(mytable, todo.get(position), 0);
                        view.todoText.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                    } else {
                        view.checkBox.setChecked(false);
                        database.modifyChecked(mytable, todo.get(position), 1);
                        view.todoText.setPaintFlags(0);
                    }

                }
            });

            if (integer.equals(1)) {
                view.todoText.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                view.todoText.setPaintFlags(0);
            }

            view.setTodoText(name);
            view.setCheckBox(integer);
            return view;

        }
    }

}

