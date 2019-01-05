package lex.volley.com.functionalmapview;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Bookmark;
import com.esri.arcgisruntime.mapping.BookmarkList;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private MapView mv;
    private ArcGISMap myMap;
    private ListView cityList;
    private ArrayAdapter myAdapter;
    String [] cities = {
            "New York",
            "Chicago",
            "Denver",
            "Detroit",
            "Las Vegas",
            "Paris"
    };
    boolean myFlag;
    private BookmarkList myBookmarks;
    private Bookmark myBookmark;
    private ArrayList myBookmarksList;
    private Callout myCallout;
    private Viewpoint homeViewpoint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //add floating action button
        FloatingActionButton myFab = findViewById(R.id.myButton1);
        myFab.setOnClickListener(myFabOnClickListener);
        myMap = new ArcGISMap(Basemap.createStreets());
        homeViewpoint = new Viewpoint(37.754178, -122.448095, 271261);
        myMap.setInitialViewpoint(homeViewpoint);
        mv = findViewById(R.id.map1);
        // ArcGISMap map = new ArcGISMap(Basemap.Type.STREETS_VECTOR, 37.7531,-122.4479,11);
        mv.setMagnifierEnabled(true);
        mv.setCanMagnifierPanMap(true);
        mv.setMap(myMap);
        //add done loading listener so that map can only zoom when the map is done loading
        myMap.addDoneLoadingListener(new Runnable(){
            @Override
            public void run() {
                mv.setMagnifierEnabled(true);
                mv.setCanMagnifierPanMap(true);
            }});

        myBookmarks = myMap.getBookmarks();
        myBookmarksList = new ArrayList();
//create a default bookmarks and add all bookmark items to the ArrayList
        createBookmarks();
        //code to display the list view
        cityList = findViewById(R.id.listview);
        myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                myBookmarksList);
        cityList.setAdapter(myAdapter);

        //setting on click listener
        cityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mv.setViewpointAsync(myBookmarks.get(i).getViewpoint());
                Toast.makeText(getApplicationContext()
                        , myBookmarks.get(i).getName(),Toast.LENGTH_SHORT).show();
            }
        });

        cityList.setVisibility(View.GONE);

        //map view on touch listener
        mv.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mv){
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e){
                //this gets the points clicked on the screen by getting the x and y
                android.graphics.Point screenPt = new android.graphics.Point(Math.round(e.getX()),
                        Math.round(e.getY()));
                Point mapPt = mMapView.screenToLocation(screenPt);
                Point wgs84Pt = (Point) GeometryEngine.project(mapPt, SpatialReferences.getWgs84());

                TextView myCalloutWords = new TextView(getApplicationContext());
                myCalloutWords.setTextColor(Color.BLUE);
                myCalloutWords.setLines(3);
                myCalloutWords.setText("Lat: " + String.format(Locale.US,
                        "%.6f", wgs84Pt.getY()) +
                        ",\nLon: " + String.format(Locale.US,
                        "%.6f", wgs84Pt.getX()) + ",\nScale: " + mv.getMapScale());
                myCallout = mv.getCallout();

                myCallout.setLocation(mapPt);
                myCallout.setContent(myCalloutWords);
                if(myCallout.isShowing() ){
                    myCallout.dismiss();
                }else {
                    myCallout.show();
                }
                return true;
            }
        });

// Configure SearchView
        final SearchManager searchMgr = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);



    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        // Configure SearchView
        final SearchManager searchMgr = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search1).getActionView();
        searchView.setQueryHint("input address here");
        searchView.setSearchableInfo(searchMgr.getSearchableInfo(getComponentName()));
//display the submit button
        searchView.setSubmitButtonEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_item1:
                myMap.setBasemap(Basemap.createStreets());
                return true;
            case R.id.action_item2:
                myMap.setBasemap(Basemap.createImagery());
                return true;
            case R.id.action_item3:
                myMap.setBasemap(Basemap.createTopographic());
                return true;
            case R.id.action_item4:
                myMap.setBasemap(Basemap.createOpenStreetMap());
                return true;
            case R.id.bookmarks1:
                if (myFlag) {
                    cityList.setVisibility(View.GONE);
                    myFlag = false;
                }else {
                    cityList.setVisibility(View.VISIBLE);
                    myFlag = true;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createBookmarks() {
        Viewpoint viewpoint;
        Bookmark myBookmark;
//create first bookmark, add to bookmarks, and assign to bookmarkslist
        viewpoint = new Viewpoint(37.754178, -122.448095, 271261);
        myBookmark = new Bookmark(getResources().getString(R.string.san_francisco), viewpoint);
        myBookmarks.add(myBookmark);
//create second bookmark, add to bookmarks, and assign to bookmarkslist
        viewpoint = new Viewpoint(37.823785, -122.370654, 67820);
        myBookmark = new Bookmark(getResources().getString(R.string.treasure_island), viewpoint);
        myBookmarks.add(myBookmark);
//create third bookmark, add to bookmarks, and assign to bookmarkslist
        viewpoint = new Viewpoint(37.328353, -121.889616, 135630);
        myBookmark = new Bookmark(getResources().getString(R.string.san_jose), viewpoint);
        myBookmarks.add(myBookmark);
//create fourth bookmark, add to bookmarks, and assign to bookmarkslist
        viewpoint = new Viewpoint(33.761795, -118.238254, 542523);
        myBookmark = new Bookmark(getResources().getString(R.string.long_beach), viewpoint);
        myBookmarks.add(myBookmark);
//create fifth bookmark
        viewpoint = new Viewpoint(-1.542122, 37.212479, 45634.44589350457);
        myBookmark = new Bookmark(getResources().getString(R.string.katelembu), viewpoint);
        myBookmarks.add(myBookmark);
        for (int i = 0; i < myBookmarks.size(); i++) {
            myBookmarksList.add(i,myBookmarks.get(i).getName());
        }
    }

    //setting on click listener for the fab
    private View.OnClickListener myFabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mv.setViewpointAsync(homeViewpoint,6);
            Toast.makeText(MainActivity.this, "Back to Home Viewpoint!", Toast.LENGTH_LONG).show();
        }
    };
}

