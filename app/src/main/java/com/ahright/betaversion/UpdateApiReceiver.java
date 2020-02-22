package com.ahright.betaversion;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;


import com.google.android.gms.common.api.Api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.ahright.betaversion.MainActivity.database;

public class UpdateApiReceiver extends BroadcastReceiver {
    static String TAG = "UpdateApiReceiver";
    static Database database2;
    static ArrayList<String> TodoList = new ArrayList<>();

    //PushAlarm
    static NotificationManager manager;
    static Notification noti;
    static NotificationCompat.Builder builder;
    private static String CHANNEL_ID = "channel2";
    private static String CHANNEL_NAME = "channel2";
    static String notifi; // 우산,미세먼지 검사해 알림 울릴지 말지 검사하는 용도

    //Dust
    static String Grade = "";
    static String CityName = "중구"; //default
    public static String DustURL = "http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty?" +
            "sidoName=서울&pageNo=1&numOfRows=40&" +
            "ServiceKey=JaSu0HNjpIDveKbzxLiGls6up3AU5Qbn%2FCxhG8uoXQr4SiP71Zq7l7%2FCRGrUffMuDkJuSLkMB8iBu9vBMoCJNQ%3D%3D&ver=1.3"; //default

    //Weather
    static boolean willBeRainy = false;
    static double Latitude = 37; //default
    static double Longitude = 126; //default
    static String currentWeather = "01n"; //default
    public static String weatherURL = "http://api.openweathermap.org/data/2.5/forecast?" +
            "lat=" + Latitude + "&lon=" + Longitude + "&mode=xml&units=metric&appid=d83f6597d3ec965b1721f8ee1f9bb4e2"; //default

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("UpdateApiReceiver", "onReceive()");

        //open Database
        database2 = Database.getInstance(context);
        boolean isOpen = database2.open();
        if (isOpen) {
            Log.d(TAG, " database is open.");
        } else {
            Log.d(TAG, "database is not open.");
        }

        //집의 구(ex.마포구), 위도, 경도 받아오기
        AreaInfo result = database2.selectSpecificArea("집");
        TodoList = database2.selectAllTodo("집");

        if (result.getStartDate() != null) {
            CityName = result.getStartDate();
        }
        Latitude = result.getLatitude();
        Longitude = result.getLongitude();

        //받아온 위도,경도로 Weather URL 변경
        weatherURL = "http://api.openweathermap.org/data/2.5/forecast?" +
                "lat=" + Latitude + "&lon=" + Longitude + "&mode=xml&units=metric&appid=d83f6597d3ec965b1721f8ee1f9bb4e2";


        //setting PushAlarm
        manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
            ));

            builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        } else {

            builder = new NotificationCompat.Builder(context);
        }
        Intent intent2 = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 365, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentText("아래로 당겨 오늘의 날씨와 미세먼지 정보를 확인하세요:)");
        builder.setOnlyAlertOnce(true);
        builder.setSound(android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION));
        builder.setSmallIcon(R.drawable.circle);
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);


                //about UpdateAPI
                if(MainActivity.city != null || !CityName.equals("a")) {
                    Api.setURL(DustURL, CityName, weatherURL);
                    Log.e("Test",weatherURL);
                };

    }


    /*************************API CLASS*********************/

    public static class Api {


        public static boolean setURL(String dustURL, String cityName, String weatherURL) {

            ConnectThread thread = new ConnectThread(dustURL, cityName, weatherURL);
            thread.start();

            return true;
        }


        // 소켓 연결할 스레드 정의
        static class ConnectThread extends Thread {
            String dustURL;
            String cityName;
            String weatherURL;

            public ConnectThread(String dustURL, String cityName, String weatherURL) {
                this.dustURL = dustURL;
                this.cityName = cityName;
                this.weatherURL = weatherURL;
            }

            public void run() {
                try {
                    final ArrayList<String> output = request(dustURL, cityName, weatherURL);

                    //MainActivity에 전달할 Message
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = output;
                    MainActivity.handler.sendMessage(msg);



                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.e(TAG, "Exception in CunnectThread");
                }

            }


            public static ArrayList<String> request(String dustURL, String StationName, String weatherURL) {
                ArrayList<Weathers> weathersArrayList = new ArrayList<>();
                ArrayList<Weathers> today = new ArrayList<>();

                //***************Dust
                try {
                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                    Document xml = documentBuilder.parse(dustURL);
                    Log.e("[DUST]api", "parse 빌드");

                    Element root = xml.getDocumentElement();
                    NodeList nodeList = root.getElementsByTagName("item");
                    String[] GradeValue = new String[40];
                    String[] StationNum = new String[40];

                    if (nodeList.getLength() == 0) return null;
                    Log.e("[DUST]getLength", "" + nodeList.getLength());
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node nodeItem = nodeList.item(i);

                        try {
                            StationNum[i] = getTagValue("stationName", (Element) nodeItem);
                            GradeValue[i] = getTagValue("khaiGrade", (Element) nodeItem);

                            if (StationName.equals(StationNum[i])) {

                                switch (GradeValue[i]){
                                    case "1" : Grade = "1";break;
                                    case "2" : Grade = "2";break;
                                    case "3" : Grade = "3";break;
                                    case "4" : Grade = "4";break;
                                    default:Grade = GradeValue[0];
                                }

                                if (GradeValue[i] == null) {
                                    Log.e("[DUST] Grade", " 주소 없어서 중구로 선택됨");
                                }
                            }


                        } catch (Exception e) {
                            Log.e("[DUST]알림", " 리퀘스트 안됨");
                        }
                    }

                    Log.e("[DUST] API CITY : ", StationName);
                    Log.e("[DUST] API GRADE : ", Grade);


                } catch (ParserConfigurationException e) {
                } catch (SAXException e) {
                } catch (IOException e) {
                } catch (Exception e) {
                }


                //*********weather
                try {
                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                    Document xml = documentBuilder.parse(weatherURL);
                    Log.e("[weather]api", "parse 빌드");

                    Element root = xml.getDocumentElement();
                    NodeList nodeList = root.getElementsByTagName("time");
                    if (nodeList.getLength() == 0) return null;
                    Log.e("[weather]getLength", "" + nodeList.getLength());

                    // 앞으로의 24시간에 대한 날씨만 추출
                    for (int i = 3; i < 11; i++) {
                        Node nodeItem = nodeList.item(i);

                        try {
                            Log.e("[weather]", ":" + i);

                            String date = nodeItem.getAttributes().item(0).getNodeValue(); //from값
                            String weather = nodeItem.getFirstChild().getAttributes().item(1).getNodeValue();//날씨
                            String icon = nodeItem.getFirstChild().getAttributes().item(2).getNodeValue();//icon 값
                            Log.e("[weather]", "날짜: " + date + ", " + "날씨: " + weather + "," + "아이콘: " + icon);

                            weathersArrayList.add(new Weathers(date, weather, icon));


                        } catch (Exception e) {
                        }
                    }

                    //현재 날씨를 받아옴
                    currentWeather = weathersArrayList.get(0).icon;

                    //날짜비교, '오늘' 날짜만 추려냄
                    for (int j = 0; j < 8; j++) {
                        String cmpDateStr = weathersArrayList.get(j).date;

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            // date
                            Date cmpDate = sdf.parse(cmpDateStr);
                            // now
                            // Date curDate = new Date(now);
                            long now = System.currentTimeMillis();
                            String curDatStr = sdf.format(new Date(now));
                            Date curDate = sdf.parse(curDatStr);
                            //cmp
                            if (cmpDate.compareTo(curDate) == 0) {
                                today.add(weathersArrayList.get(j));
                                Log.e("[weather] compareDate", cmpDateStr + "같습니다!");
                            } else {
                                Log.e("[weather] compareDate", cmpDateStr + "다릅니다!");
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    //오늘 비가 올 예정인지 검사
                    willBeRainy = false;

                    for (int j = 0; j < today.size(); j++) {
                        if (today.get(j).weather.contains("rain")) {
                            willBeRainy = true;
                        }
                    }



                } catch (ParserConfigurationException e) {
                } catch (SAXException e) {
                } catch (IOException e) {
                } catch (Exception e) {
                }


                //[DB] '집'의 TodoTable에 이미 해당 레코드가 존재하는지 확인
                boolean existWeather = false;
                boolean existDust = false;
                for (int i = 0; i < TodoList.size(); i++) {
                    if (TodoList.get(i).equals("마스크를 챙기세요!")) existDust = true;
                    if (TodoList.get(i).equals("우산을 챙기세요!")) existWeather = true;
                }

                //초기 알림메시지
                notifi = " ";


                //check Dust
                if (Grade.equals("3") || Grade.equals("4")) {
                    Log.e("[Dust] Danger ", "Grade : 3 or 4");

                    notifi = notifi + "오늘 미세먼지등급이 좋지않습니다. 마스크를 챙기세요!\n";
                    if (existDust == false) {
                        database.insertTodoRecord("집", "마스크를 챙기세요!");
                        Log.e("AlarmReciver", "insertTodoRecord [마스크를 챙기세요!]");
                    }

                } else {
                    Log.e("[Dust] OK ", "Grade : 1 or 2");
                    if (existDust == true)
                        database2.deleteTodoRecord("마스크를 챙기세요!");

                }

                //check Rainy
                if (willBeRainy == true) {
                    Log.e("[weather] willBeRainy", "true");

                    notifi = notifi + "오늘 비가 올 예정입니다. 우산을 챙기세요!";
                    if (existWeather == false) {
                        database2.insertTodoRecord("집", "우산을 챙기세요!");
                        Log.e("AlarmReciver", "insertTodoRecord [우산을 챙기세요!]");
                    }

                } else {
                    Log.e("[weather] willBeRainy", "false");
                    if (existWeather == true)
                        database2.deleteTodoRecord("우산을 챙기세요!");
                }

                //푸쉬알람 등록
                if (!notifi.equals(" ")) {
                    Log.e("AlarmReciever, 알림메시지:", notifi);
                    builder.setStyle(new NotificationCompat.BigTextStyle().bigText(notifi));
                    noti = builder.build();
                    manager.notify(100, noti);
                }

            //미세먼지 등급, 현재 날씨(아이콘) return
            ArrayList<String> list = new ArrayList<>();
                list.add(Grade);
                list.add(currentWeather);
                return list;
        }


            //태그의 값을 받아옴
            private static String getTagValue(String sTag, Element element) {
                try {
                    String result = element.getElementsByTagName(sTag).item(0).getTextContent();
                    return result;
                } catch (NullPointerException e) {
                    return "";
                } catch (Exception e) {
                    return "";
                }
            }

        }

        //날씨정보를 저장하는 class
        public static class Weathers {
            public String date;
            public String weather;
            public String icon;

            public Weathers(String date, String weather, String icon) {
                this.date = date;
                this.weather = weather;
                this.icon = icon;
            }
        }
    }


}
