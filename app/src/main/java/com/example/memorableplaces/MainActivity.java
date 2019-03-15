package com.example.memorableplaces;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    //on declaring these array list static we can access them in any activity of the project as any changes in other activity made would be seen in them too. As static variables are allocated memory on;y once.
    static ArrayList<String> places=new ArrayList<String>();
    static ArrayList<LatLng> locations=new ArrayList<LatLng>();
    static ArrayAdapter<String> arrayAdapter;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=(ListView)findViewById(R.id.listView);
        ArrayList<String> latitudes=new ArrayList<String>();
        ArrayList<String> longitudes=new ArrayList<String>();

        places.clear();
        locations.clear();
        longitudes.clear();
        latitudes.clear();

        SharedPreferences sharedPreferences=this.getSharedPreferences("com.example.memorableplaces", Context.MODE_PRIVATE);

        try
        {
            latitudes=(ArrayList<String>)ObjectSerializer.deserialize(sharedPreferences.getString("LATITUDES",new ArrayList<String>().toString()));
            longitudes=(ArrayList<String>)ObjectSerializer.deserialize(sharedPreferences.getString("LONGITUDES",new ArrayList<String>().toString()));
            places=(ArrayList<String>)ObjectSerializer.deserialize(sharedPreferences.getString("PLACES",new ArrayList<String>().toString()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (places.size() > 0 && latitudes.size() > 0 && longitudes.size() > 0)
        {
            if (places.size() == latitudes.size() && places.size() == longitudes.size())
            {
                for(int i=0;i<latitudes.size();i++)
                {
                    LatLng latLng=new LatLng(Double.parseDouble(latitudes.get(i)),Double.parseDouble(longitudes.get(i)));
                    locations.add(latLng);
                }
            }
        }
        else
        {
            places.add("ADD A NEW PLACE");
            locations.add(new LatLng(0,0));
        }
        arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,places);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent=new Intent(getApplicationContext(),MapActivity.class);
                    intent.putExtra("position",position);
                    startActivity(intent);
            }
        });
    }
}
