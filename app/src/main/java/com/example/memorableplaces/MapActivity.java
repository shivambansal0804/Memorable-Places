package com.example.memorableplaces;

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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1)
        {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
                {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                    Location lastknownlocation=locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                    updateLocationOnMap(lastknownlocation,"YOUR WERE HERE");
                }
            }
        }
    }

    public void updateLocationOnMap(Location location, String ans)
    {
        if(location!=null)
        {
            mMap.clear();
            LatLng userLocation=new LatLng(location.getLatitude(),location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(userLocation).title(ans));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent=getIntent();
        int position=intent.getIntExtra("position",0);
        if(position==0)
        {
            //Zoom on in userLocation
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener= new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    updateLocationOnMap(location,"YOUR ARE HERE");
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
            else
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastknownlocation=locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                updateLocationOnMap(lastknownlocation,"YOUR WERE LAST HERE");
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(),Integer.toString(MainActivity.locations.size())+" "+Integer.toString(MainActivity.places.size()),Toast.LENGTH_SHORT).show();
            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locations.get(position).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(position).longitude);

            updateLocationOnMap(placeLocation,MainActivity.places.get(position));
        }
    }
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder=new Geocoder(getApplicationContext(), Locale.getDefault());
        String result="";
        try
        {
            List<Address> addressList=geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(addressList!=null && addressList.size()>0)
            {
                if(addressList.get(0).getSubThoroughfare()!=null)
                {
                    result+=addressList.get(0).getSubThoroughfare().toString()+" ";
                }
                if(addressList.get(0).getThoroughfare()!=null)
                {
                    result+=addressList.get(0).getThoroughfare().toString()+" ";
                }
                if(addressList.get(0).getSubAdminArea()!=null)
                {
                    result+=addressList.get(0).getSubAdminArea().toString()+" ";
                }
                if(addressList.get(0).getCountryName()!=null)
                {
                    result+=addressList.get(0).getCountryName().toString();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if(result=="")
        {
            //hh=hour,mm=min,ss=seconds,
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy  HH:mm:ss");
             result+= df.format(Calendar.getInstance().getTime());
        }
        mMap.addMarker(new MarkerOptions().position(latLng).title(result));
        MainActivity.places.add(result);
        MainActivity.locations.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged();//in this statement we are saying that we have made some changes in the arrayList and implement them.
        //Updating the saved information on the permanent storage every time the user clicks it.
        SharedPreferences sharedPreferences=this.getSharedPreferences("com.example.memorableplaces",Context.MODE_PRIVATE);
        try {
            ArrayList<String> latitudes=new ArrayList<String>();
            ArrayList<String> longitudes=new ArrayList<String>();

            for(int i=0;i<MainActivity.locations.size();i++)
            {
                latitudes.add(Double.toString(MainActivity.locations.get(i).latitude));
                longitudes.add(Double.toString(MainActivity.locations.get(i).longitude));
            }
            sharedPreferences.edit().putString("LATITUDES",ObjectSerializer.serialize(latitudes)).apply();
            sharedPreferences.edit().putString("LONGITUDES",ObjectSerializer.serialize(longitudes)).apply();
            sharedPreferences.edit().putString("PLACES",ObjectSerializer.serialize(MainActivity.places)).apply();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Toast.makeText(this,"LOCATION ADDED SUCCESSFULLY",Toast.LENGTH_SHORT).show();
    }
}
