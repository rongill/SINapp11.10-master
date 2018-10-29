package com.rongill.rsg.sinprojecttest;

import java.util.ArrayList;
import java.util.List;

public class LocationData {

    private static ArrayList<Location> locations ;
    static {
        //TODO add the static locations from the DATABASE
        locations =  new ArrayList<Location>();
        locations.add(new Location("Bathroom","services"));
        locations.add(new Location("Computer store","shops"));
        locations.add(new Location("Aldo","shops"));
        locations.add(new Location("Castro","shops"));
        locations.add(new Location("Bug","shops"));
        locations.add(new Location("South exit","services"));
        locations.add(new Location("North exit","services"));
        locations.add(new Location("Movie theater","services"));
        locations.add(new Location("Fox kids","shops"));
        locations.add(new Location("Renuar","shops"));
        locations.add(new Location("Cafe Cafe","food"));
        locations.add(new Location("Crib","food"));
        locations.add(new Location("Pizza Pino","food"));
        locations.add(new Location("Information","services"));
        locations.add(new Location("McDonalds","food"));
        locations.add(new Location("Cafe Nero","food"));

    }

    //returns a Location list of.
    public static ArrayList<Location> getLocationsArrayList(){

        return locations;
    }

    public static List<String> getLocations(){
        List<String> locationNames = new ArrayList<String>();
        for(int i=0 ; i < locations.size() ; i++ ){
            locationNames.add(locations.get(i).getName());
        }
        return locationNames;
    }

    //redundent?
    public static List<String> filterData(String searchString){
        List<String> searchResults = new ArrayList<String>();
        if(searchString != null){
            searchString = searchString.toLowerCase();

            for(Location rec : locations){
                if(rec.getName().toLowerCase().contains(searchString)){
                    searchResults.add(rec.getName());
                }
            }
        }
        return searchResults;
    }
}
