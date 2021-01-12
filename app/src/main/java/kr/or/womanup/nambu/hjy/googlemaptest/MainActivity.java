package kr.or.womanup.nambu.hjy.googlemaptest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity {
    String[] permissionList={
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    GoogleMap map;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){ //버전확인
            requestPermissions(permissionList,101);
            //권한 확인 후 onRequestPermissionsResulult 메소드 실행됨
        }else {
            mapInit();
        }
    }

    @Override //권한에 대한 것이 다 실행되고 이 메소드 실행됨
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int result:grantResults){
            if(result== PackageManager.PERMISSION_DENIED){// 하나라도 권한 거부가 되면
                return;
            }
        }
        mapInit(); //다 허용됐으면 실행됨
    }

    public  void  mapInit(){
        FragmentManager fragmentManager = getSupportFragmentManager(); //fragment가져오게 조와주는 매니저
        SupportMapFragment mapFragment = //맵이 들어갈 fragment layout의 code에 정의해놓음.
                (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);
        MapReadyCallback callback = new MapReadyCallback();
        mapFragment.getMapAsync(callback); //프레그먼트가 준비되면(map이 ready) callback의 onMapReady를 실행함

    }

    public  void currentLocation(){//현재 위치를 받아오는 함수
        //시스템에서 위치 관리해주는 것 요청
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        //시스템의 위치정보:  GPS, 네트워크로 가져오기 -> 둘다 안 될 경우 각각 마지막 위치 가져옴

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==
                    PackageManager.PERMISSION_DENIED){
                    return;  //권한 비허용이면 끝
            }

        }
        //GPS, 네트워크의 마지막 위치 가져오기
        Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location location2 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if(location1!=null){
            setLocation(location1);
        }else if(location2!=null){
            setLocation(location2);
        }

        CurrentLocationListener listener = new CurrentLocationListener();
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)==true) { //GPS가 사용가능할 때
            //1초마다, 최소 10m
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000, 10f, listener);
        }
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)==true){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000,10f,listener);
        }
    }
    
    public void setLocation(Location location){
        Log.d("nambu","위도 : "+location.getLatitude()); //위도 출력
        Log.d("nambu","경도 : "+location.getLongitude()); //경도 출력

        LatLng position = new LatLng(location.getLatitude(),location.getLongitude());

        CameraUpdate update = CameraUpdateFactory.newLatLng(position);
        map.moveCamera(update); //해당 위치로 카메라를 옮긴다.
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15f); //맵 확대
        map.animateCamera(zoom);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==
                    PackageManager.PERMISSION_DENIED){
                return;  //권한 비허용이면 끝
            }

        }
        map.setMyLocationEnabled(true); //권한 확인돼야함

    }

    class CurrentLocationListener implements LocationListener{
        @Override
        public void onLocationChanged(@NonNull Location location) {
            setLocation(location); //위치 바뀔 때마다 호출됨
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {}

        @Override
        public void onProviderDisabled(@NonNull String provider) {}
    }

    class MapReadyCallback implements OnMapReadyCallback{

        @Override
        public void onMapReady(GoogleMap googleMap) {
            map=googleMap; //mapFragment안에 googleMap이 들어있는데 그걸 가져옴
            currentLocation();
        }
    }
}