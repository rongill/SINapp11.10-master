package com.rongill.rsg.sinprojecttest;

import java.util.ArrayList;
import java.util.List;

public class LocationData {

    private static List<String> locations ;
    static {
        locations =  new ArrayList<String>();
        locations.add("bathroom");
        locations.add("computer store");
        locations.add("aldo");
        locations.add("castro");
        locations.add("bug");
        locations.add("south exit");
        locations.add("north exit");
        locations.add("movie theater");
        locations.add("fox kids");
        locations.add("renuar");
        locations.add("cafe cafe");
        locations.add("crib");
        locations.add("blabla");
        locations.add("bogo");
        locations.add("shmnidets");
    }

    public static List<String> getLocations(){
        return locations;
    }

    public static List<String> filterData(String searchString){
        List<String> searchResults = new ArrayList<String>();
        if(searchString != null){
            searchString = searchString.toLowerCase();

            for(String rec : locations){
                if(rec.toLowerCase().contains(searchString)){
                    searchResults.add(rec);
                }
            }
        }
        return searchResults;
    }
}
