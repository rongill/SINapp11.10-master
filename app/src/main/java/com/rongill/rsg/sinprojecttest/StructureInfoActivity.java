package com.rongill.rsg.sinprojecttest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class StructureInfoActivity extends AppCompatActivity {

    private ArrayList<String> shopsListview, foodListview, servicesListview, favoriteListview;
    private ListView expandableListview;
    private ArrayAdapter<String> expandableListviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_structure_info);

        expandableListview = (ListView)findViewById(R.id.expandable_listView);
        expandableListviewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        expandableListview.setAdapter(expandableListviewAdapter);
        expandableListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getBaseContext(), LocationInfoPage.class);
                intent.putExtra("LOCATION_NAME", (String)expandableListview.getItemAtPosition(position));
                startActivity(intent);
                finish();
            }
        });
        //TODO arrange in a separate function
        shopsListview = new ArrayList<>();
        foodListview = new ArrayList<>();
        servicesListview = new ArrayList<>();
        favoriteListview = new ArrayList<>();

        Intent intent = getIntent();
        ArrayList<Location> locationArrayList = (ArrayList<Location>)intent.getSerializableExtra("LOCATION_LIST");
        //init ArrayLists to their category.
        for(int i = 0; i < locationArrayList.size(); i++){
            switch (locationArrayList.get(i).getCategory()){
                case "shops":
                    shopsListview.add(locationArrayList.get(i).getName());
                    break;
                case "food":
                    foodListview.add(locationArrayList.get(i).getName());
                    break;
                case "services":
                    servicesListview.add(locationArrayList.get(i).getName());
            }
        }
    }

    //onClick method for the expandable views, setting the adapter to the correct data set
    public void expandCategoryList(View v){
        switch (v.getId()){
            case R.id.shops_expandable:
                expandableListviewAdapter.clear();
                expandableListviewAdapter.addAll(shopsListview);
                break;
            case R.id.food_expandable:
                expandableListviewAdapter.clear();
                expandableListviewAdapter.addAll(foodListview);
                break;
            case R.id.services_expandable:
                expandableListviewAdapter.clear();
                expandableListviewAdapter.addAll(servicesListview);
                break;
            case R.id.favorite_expandable:
                Toast.makeText(this, "Add some favorites to you list", Toast.LENGTH_LONG).show();
                break;
        }
    }
}
