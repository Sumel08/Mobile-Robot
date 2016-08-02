package com.lemus.oscar.mobilerobot;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;


public class ViewMap extends AppCompatActivity {

    private MapView mvOSM;
    private IMapController mpOSM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_map);


        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        ArrayList<String> longitudeAL = extras.getStringArrayList("longitude");
        ArrayList<String> latitudeAL = extras.getStringArrayList("latitude");

        mvOSM = (MapView) findViewById(R.id.mvOSM);
        mvOSM.setTileSource(TileSourceFactory.MAPNIK);

        //mvOSM.setBuiltInZoomControls(true);
        mvOSM.getController().setZoom(16);
        mvOSM.setMultiTouchControls(true);
        mvOSM.getController().setCenter(new GeoPoint(Double.parseDouble(latitudeAL.get(0)), Double.parseDouble(longitudeAL.get(0))));



        ArrayList<OverlayItem> anotherOverlayItemArray;
        anotherOverlayItemArray = new ArrayList<OverlayItem>();


        for (int i=0; i<longitudeAL.size(); i++) {
            anotherOverlayItemArray.add(new OverlayItem("algo", "más", new GeoPoint(Double.parseDouble(latitudeAL.get(i)), Double.parseDouble(longitudeAL.get(i)))));
        }

        /*anotherOverlayItemArray.add(new OverlayItem("0, 0", "0, 0", new GeoPoint(16.742758333, -93.146316667)));
        anotherOverlayItemArray.add(new OverlayItem("US", "US", new GeoPoint(16.742773333, -93.146318333)));
        anotherOverlayItemArray.add(new OverlayItem("China", "China", new GeoPoint(16.743121667, -93.14642)));
        anotherOverlayItemArray.add(new OverlayItem("United Kingdom", "United Kingdom", new GeoPoint(16.743115, -93.146425)));
        anotherOverlayItemArray.add(new OverlayItem("Germany", "Germany", new GeoPoint(16.743461667, -93.146725)));
        anotherOverlayItemArray.add(new OverlayItem("Korea", "Korea", new GeoPoint(16.743461667, -93.146725)));
        anotherOverlayItemArray.add(new OverlayItem("India", "India", new GeoPoint(16.743325, -93.145816667)));
        anotherOverlayItemArray.add(new OverlayItem("Russia", "Russia", new GeoPoint(16.743106667, -93.145668333)));
        anotherOverlayItemArray.add(new OverlayItem("France", "France", new GeoPoint(16.743093333, -93.145685)));
        anotherOverlayItemArray.add(new OverlayItem("Canada", "Canada", new GeoPoint(16.743056667, -93.145743333)));

        anotherOverlayItemArray.add(new OverlayItem("Canada", "Canada", new GeoPoint(16.743051667, -93.14576)));
        anotherOverlayItemArray.add(new OverlayItem("Canada", "Canada", new GeoPoint(16.743031667, -93.145788333)));
        anotherOverlayItemArray.add(new OverlayItem("Canada", "Canada", new GeoPoint(16.743028333, -93.145806667)));
        anotherOverlayItemArray.add(new OverlayItem("Canada", "Canada", new GeoPoint(16.74302, -93.14584666)));
        anotherOverlayItemArray.add(new OverlayItem("Canada", "Canada", new GeoPoint(16.743016667, -93.145866667)));
        anotherOverlayItemArray.add(new OverlayItem("Canada", "Canada", new GeoPoint(16.743011667, -93.145901667)));
        anotherOverlayItemArray.add(new OverlayItem("Canada", "Canada", new GeoPoint(16.743015, -93.145911667)));*/

        ItemizedOverlayWithFocus<OverlayItem> anotherItemizedIconOverlay = new ItemizedOverlayWithFocus<OverlayItem>(this, anotherOverlayItemArray, myOnItemGestureListener);
        mvOSM.getOverlays().add(anotherItemizedIconOverlay);
    }

    ItemizedIconOverlay.OnItemGestureListener<OverlayItem> myOnItemGestureListener = new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {

        @Override
        public boolean onItemLongPress(int arg0, OverlayItem arg1) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean onItemSingleTapUp(int index, OverlayItem item) {
            Toast.makeText(
                    ViewMap.this,
                    item.getSnippet() + "\n" + item.getTitle() + "\n"
                            + item.getPoint().getLatitudeE6() + " : "
                            + item.getPoint().getLongitudeE6(),
                    Toast.LENGTH_LONG).show();

            return true;
        }

    };
}
