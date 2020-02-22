package com.ahright.betaversion;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.location.LocationManager.GPS_PROVIDER;
import static com.ahright.betaversion.MainActivity.database;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    public static final String TAG = "MAP";
    private GoogleMap mMap;
    SearchView searchView;
    Geocoder geocoder = new Geocoder(MapsActivity.this);
    RelativeLayout RelativeLayout_login;
    private Button next;
    private ImageButton current;
    // for PERMISSION
    boolean isPermitted = false;
    final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    protected LocationManager locationManager;
    Location curLoc;
    Double finalLatitude, finalLongitude;
    String finalGu, finalAd;
    // for HOUSE
    Intent houseIntent;
    private Intent main;
    String house = null;
    boolean houseBool = false;
    public SharedPreferences prefs;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        searchView = findViewById(R.id.sv_location);
        next = findViewById(R.id.next);
        current = findViewById(R.id.getCurrentLocation);
        RelativeLayout_login = findViewById(R.id.RelativeLayout_login);

        prefs = getSharedPreferences("Pref", MODE_PRIVATE);

        // 위치정보 접근 허가 요청하기
        requestRuntimePermission();



        // SupportMapFragment 받아오고 이 구글맵이 사용될 준비되었다는 알람 받기
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapFragment.getMapAsync(this);

        startLocationService();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        startLocationService();
    }

    /***************************** MAP ****************************/
    // 구글맵 API 받아와서 시작한 후 최근 현재 위치 정보 10초마다 업데이트 요청하기
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startLocationService() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

       /* locationManager.requestLocationUpdates(GPS_PROVIDER, 10000, 10, this);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                10000,
                10,
                this
        );*/
        curLoc = locationManager.getLastKnownLocation(GPS_PROVIDER);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        // 구글맵에서 터치한 위치 마커 표시해주기
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                Double latitude = point.latitude; // 위도
                Double longitude = point.longitude; // 경도
                try {
                    showLocation(latitude, longitude, "클릭하신 위치");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // searchView 에 text 입력할 경우 지오코딩으로 변환하여 위치 마커 표시해주기
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String newText) {
                List<Address> addressList;

                try {
                    String location = searchView.getQuery().toString();
                    addressList = geocoder.getFromLocationName(location, 10);
                    Address searchLoc = null;
                    searchLoc = addressList.get(0);
                    showLocation(searchLoc.getLatitude(), searchLoc.getLongitude(), location);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "잘못된 주소입니다. 올바른 지명을 입력해주세요.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(intent);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        current.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.e(TAG, String.valueOf(curLoc));
                    showLocation(curLoc.getLatitude(), curLoc.getLongitude(), "현 위치");
                } catch (Exception e1) {
                    e1.printStackTrace();
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(intent);
                }
            }
        });

        // 등록 버튼 누르면 마지막으로 누른 마커의 위치정보 넘겨주기
        next.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isFirstRun = prefs.getBoolean("isFirstRun", true);
                // 처음 실행 시에는 RegisterHome()에 넘겨주기
                if (isFirstRun) {
                    Intent homeIntent = new Intent(getApplicationContext(), RegisterHomeActivity.class);
                    homeIntent.putExtra("gu", finalGu);
                    homeIntent.putExtra("address", finalAd);
                    homeIntent.putExtra("homeReady", true);
                    database.db.execSQL("UPDATE " + database.TABLE_AREA_INFO + " SET LATITUDE = '" + finalLatitude + "' WHERE NAME = '" + "집" + "'");
                    database.db.execSQL("UPDATE " + database.TABLE_AREA_INFO + " SET LONGITUDE = '" + finalLongitude + "' WHERE NAME = '" + "집" + "'");

                    startActivity(homeIntent);
                    finish();

                } else {
                    Intent name = new Intent(getApplicationContext(), RegisterAreaActivity.class);
                    name.putExtra("latitude", finalLatitude);
                    name.putExtra("longitude", finalLongitude);
                    name.putExtra("address", finalAd);
                    startActivity(name);
                    finish();
                }
            }
        });

        // 초기 구글맵 실행 시 현위치가 기본 마커 ( 받아오지 못한 경우 성신여대가 기본 마커)
        try {
            LatLng curll = new LatLng(curLoc.getLatitude(), curLoc.getLongitude());
            Log.e(TAG, String.valueOf(curll));
            mMap.addMarker(new MarkerOptions().position(curll).title("현 위치"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(curll));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curll, 15));
        } catch (NullPointerException e) {
            LatLng sswu = new LatLng(37.591310299999996, 127.02213119999999);
            mMap.addMarker(new MarkerOptions().position(sswu).title("성신여대"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sswu));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sswu, 15));
        }

    }

    // 마커 표시하기
    private void showLocation(Double lat, Double lot, String title) throws IOException {
        Geocoder geo = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geo.getFromLocation(lat, lot, 1);
            finalAd = addresses.get(0).getAddressLine(0);
            String[] splitAd = finalAd.split(" ");
            finalGu = splitAd[2];

            finalLatitude = lat;
            finalLongitude = lot;

            LatLng curPoint = new LatLng(lat, lot);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 15));
            mMap.addMarker(new MarkerOptions().position(curPoint).title(title).snippet(finalAd));

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "잘못된 주소입니다. 올바른 지명을 입력해주세요.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(intent);
        }
    }

    /***************************** LocationListener ****************************/

    @Override
    public void onLocationChanged(Location location) {
        curLoc = location;
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }

    /***************************** PERMISSION ****************************/

    private void requestRuntimePermission() {
        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            isPermitted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isPermitted = true;
                } else {
                    isPermitted = false;
                }
                return;
            }
        }
    }
}