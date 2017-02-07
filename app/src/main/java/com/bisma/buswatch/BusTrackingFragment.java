package com.bisma.buswatch;


import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bisma.buswatch.model.BusInfo;
import com.bisma.buswatch.model.BusInfoList;
import com.bisma.buswatch.service.APIService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Retrofit;

public class BusTrackingFragment extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    private final Handler handler = new Handler();
    private final int delay = 1000; //milliseconds

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bus_tracking, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
                googleMap.setMyLocationEnabled(true);

                // For dropping a marker at a point on the Map
                LatLng nus = new LatLng(1.2966, 103.7764);
                LatLng holyCross = new LatLng(1.3077, 103.7708);

                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.blue_bus);


                googleMap.addMarker(new MarkerOptions().position(nus).title("nus").snippet("Marker Description")
                    .icon(icon));
                googleMap.addMarker(new MarkerOptions().position(holyCross).title("holy cross").snippet("Marker Description"));


                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(nus).zoom(9).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        drawMarker();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    private void getBusInfo(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        APIService service = retrofit.create(APIService.class);
        Call<BusInfoList> call = service.getBusInfo();


        call.enqueue(new Callback<BusInfoList>() {
            @Override
            public void onResponse(Call<BusInfoList> call, Response<BusInfoList> response) {
                final List<BusInfo> busInfoList = response.body().getBus_infos();

                mMapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        //BusInfo busInfo = busInfoList.get(0);
                        for (int i = 0; i < busInfoList.size(); i++) {
                            BusInfo busUnit = busInfoList.get(i);
                            LatLng busCoordinate = new LatLng(Double.parseDouble(busUnit.getLatitude()), Double.parseDouble(busUnit.getLongitude()));
                            googleMap.addMarker(new MarkerOptions().position(busCoordinate).title("bus unit " + i).snippet("Marker Description").rotation(Float.parseFloat(busUnit.getHeading())));
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<BusInfoList> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void drawMarker() {

        handler.postDelayed(new Runnable(){
            public void run(){
                getBusInfo();
                handler.postDelayed(this, delay);
            }
        }, delay);
    }
}