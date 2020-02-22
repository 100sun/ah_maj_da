package com.ahright.betaversion;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

//For MainCardAdapter
public class MainView extends LinearLayout {
    private static final String TAG = "MainView";
    ImageView imageView;
    TextView textView;
    TextView dateText;
    ProgressBar achievement;
    FrameLayout mainCardFrame;

    public MainView(Context context) {
        super(context);
        init(context);
    }

    public void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.main_card_view, this, true);
        imageView = findViewById(R.id.destination);
        textView = findViewById(R.id.mainTableName);
        dateText = findViewById(R.id.mainDateText);
        achievement = findViewById(R.id.progressBar);
        mainCardFrame = findViewById(R.id.mainCardFrame);
    }

    public void setTextView(String name) {
        textView.setText(name);
    }

    public void setDateText(String MyDate) {
        dateText.setText(MyDate);
    }

    public void setHouseView(String home){
        if(home.equals("집")){
           mainCardFrame.setVisibility(GONE);
        }
    }

    public void setAchievement(int allCount, int checkedCount) {
        achievement.setMax(allCount);
        achievement.setProgress(checkedCount);
    }

    public void setImageIconView(int num) {
        switch (num) {
            case 1:
                imageView.setImageResource(R.drawable.house);
                break;
            case 2:
                imageView.setImageResource(R.drawable.school);
                break;
            case 3:
                imageView.setImageResource(R.drawable.companynew);
                break;
            default:
                imageView.setImageResource(R.drawable.ect);
        }
    }

}
