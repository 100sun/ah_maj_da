package com.ahright.betaversion;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

// for DetailViewActivity
public class ChecklistView extends LinearLayout {
    CheckBox checkBox;
    TextView todoText;

    public ChecklistView(Context context) {
        super(context);
        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.check_item, this, true);
        checkBox = findViewById(R.id.hicheck);
        todoText = findViewById(R.id.TodoInDetailViewActivity);
    }

    public void setCheckBox(Integer integer){
        if(integer == 0)
            checkBox.setChecked(false);
        else
            checkBox.setChecked(true);
    }

    public void setTodoText(String today){
        todoText.setText(today);
    }

}
