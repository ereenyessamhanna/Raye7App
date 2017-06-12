package com.example.ereenyessam.raye7;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements  OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, RoutingListener {
    GoogleMap mGoogleMap;

    GoogleApiClient mGoogleApiClient;

    Marker markerTo;
    Marker markerFrom;

    Button mFindRouteBtn;
    Button mTraficRouteBtn;
    Button mSetDateAndTimeBtn;
    TextView mShowDateAndTime;


    PlaceAutocompleteFragment mFromFragement;
    PlaceAutocompleteFragment mToFragement;

    Polyline shape=null;
    PolylineOptions options;

    String mDateAndTime;
    int mYear;
    int mMonths;
    int mDay;
    int mHour;
    int mMin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(isOnline())
        {


            if (CheckGoogleServiveAvailabilty())
            {
                //if the if return true then we are connected to google playservice
                Toast.makeText(this, "Connect to Google playServices", Toast.LENGTH_LONG).show();

                makeMap();

                // draw the two fragement the source and the destination and set listener get the address from LatLng and make markers
                mFromFragement = (PlaceAutocompleteFragment)
                        getFragmentManager().findFragmentById(R.id.from);

                mFromFragement.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place) {

                        try {
                            geoLocateAddress(place.getLatLng().latitude, place.getLatLng().longitude, "Green");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Status status) {
                        Log.e("DestinationFragementError", "onError: Status = " + status.toString());
                        Toast.makeText(MainActivity.this, "failed: " + status.getStatusMessage(),
                                Toast.LENGTH_SHORT).show();

                    }
                });
                mFromFragement.setHint("where are you ");
                mFromFragement.getView().findViewById(R.id.place_autocomplete_clear_button).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        markerFrom.remove();
                        markerFrom = null;
                        mFromFragement.setText("");
                        RemoveTheShape();
                        v.setVisibility(View.GONE);
                    }
                });


                mToFragement = (PlaceAutocompleteFragment)
                        getFragmentManager().findFragmentById(R.id.to);
                mToFragement.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place) {

                        try {
                            geoLocateAddress(place.getLatLng().latitude, place.getLatLng().longitude, "Red");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override


                    public void onError(Status status) {

                        Log.e("SourceFragementError", "onError: Status = " + status.toString());
                        Toast.makeText(MainActivity.this, " failed: " + status.getStatusMessage(),
                                Toast.LENGTH_SHORT).show();

                    }
                });
                mToFragement.setHint("where are you going ");

                mToFragement.getView().findViewById(R.id.place_autocomplete_clear_button).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        markerTo.remove();
                        markerTo= null;
                        mToFragement.setText("");
                        RemoveTheShape();
                        v.setVisibility(View.GONE);
                    }
                });


            }
            else
            {

                Toast.makeText(this,"Cann't connect to playServices",Toast.LENGTH_LONG).show();

            }
        }
        else

        {



            Toast.makeText(this,"Please Check your Internet Connection",Toast.LENGTH_LONG).show();
        }


        mFindRouteBtn = (Button) findViewById(R.id.show_Route);
        mFindRouteBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (markerTo!=null &&markerFrom!=null)
                {
                    // get the lat and lng of the source and destination and pass them to findRoute parameter
                    double mSourceLatitude= markerFrom.getPosition().latitude;
                    double mSourceLongitude = markerFrom.getPosition().longitude;
                    double mDestLatitude = markerTo.getPosition().latitude;
                    double mDestLongitude = markerTo.getPosition().longitude;

                    findRoute(mSourceLatitude,mSourceLongitude,mDestLatitude,mDestLongitude);

                }
                else if (markerFrom == null)
                {
                    //if the  source not selected yet
                    Toast.makeText(MainActivity.this, "Please select the source place", Toast.LENGTH_LONG).show();

                }
                else if (markerTo==null)
                {
                    //if the destination not selected yet
                    Toast.makeText(MainActivity.this,"Please select the destination place",Toast.LENGTH_LONG).show();
                }




            }
        });

        mTraficRouteBtn = (Button) findViewById(R.id.show_traffic);
        mTraficRouteBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if (isOnline())
                {

                    mGoogleMap.setTrafficEnabled(true);

                }

            }
        });

        mSetDateAndTimeBtn= (Button) findViewById(R.id.Date_Time);
        mSetDateAndTimeBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (markerTo != null && markerFrom != null&& shape!=null)
                {
                    datePicker();
                } else {
                    Toast.makeText(MainActivity.this, "You must select the source and destination first", Toast.LENGTH_LONG).show();
                }

            }
        });


    }
    private void datePicker() {

        final Calendar mCalendarDate = Calendar.getInstance();
        mYear = mCalendarDate.get(Calendar.YEAR);
        mMonths = mCalendarDate.get(Calendar.MONTH);
        mDay = mCalendarDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog mDatePickerDialog = new DatePickerDialog(this,new DatePickerDialog.OnDateSetListener()
        {
                    @Override
                    public void onDateSet(DatePicker view, int year,int monthOfYear, int dayOfMonth)
                    {

                         mDateAndTime= dayOfMonth + "-" + monthOfYear  + "-" + year;
                        timePicker();
                    }
        }, mYear, mMonths, mDay);
              mDatePickerDialog.show();

    }


    private void timePicker()
    {
        final Calendar mCalendarTime =Calendar.getInstance();
        mHour = mCalendarTime.get(Calendar.HOUR_OF_DAY);
        mMin = mCalendarTime.get(Calendar.MINUTE);

        TimePickerDialog  mTimePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener()
        {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute)
            {
                mShowDateAndTime = (TextView) findViewById(R.id.show_date);
                mShowDateAndTime.setText(mDateAndTime+" "+hourOfDay + ":" + minute);

            }
        },mHour,mMin,false);
        mTimePickerDialog.show();
    }



    private boolean isOnline()
    {
            //check the internet connectivity
            //can't access any data if there is no internet

            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();



    }




    private void findRoute(double mSourceLat,double mSourceLng,double mDestLat, double mDestLng)
    {
        //put start and end in LatLng
        LatLng start = new LatLng(mSourceLat,mSourceLng);
        LatLng end = new LatLng(mDestLat,mDestLng);
        //make a route and excute it
        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.WALKING)
                .withListener(this)
                .waypoints(start, end)
                .build();
        routing.execute();


    }


    private void makeMap()
    {
        //draw map fragement
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

    }

    public boolean CheckGoogleServiveAvailabilty()
    {
        //Google APi Availability return if it is connected to google play service or not
        // is available return integer  according to availabilty
        // if it is available return true else return error message
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int isAvailable = availability.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS)
        {
            return true;
        } else if (availability.isUserResolvableError(isAvailable)) {
            Dialog error = availability.getErrorDialog(this, isAvailable, 0);
            error.show();
        } else {
            Toast.makeText(this, "Cann't connect to google play services", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleMap = googleMap;

        if (mGoogleMap != null) {
            mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    double lat = latLng.latitude;
                    double lng = latLng.longitude;
                    try {
                        if (markerTo != null) {
                            markerTo.remove();
                        }
                        geoLocateAddress(lat, lng, "Red");

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });


        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        // make current button enable onclicking on it get the current location


        mGoogleMap.setMyLocationEnabled(true);

        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {

                mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                        .addConnectionCallbacks(MainActivity.this)
                        .addOnConnectionFailedListener(MainActivity.this)
                        .addApi(LocationServices.API)
                        .build();
                mGoogleApiClient.connect();
                return false;
            }
        });





    }


    private void goToLocationZoom(double lat, double lng, int zoom) {

        //get the lat,lng and get the  camera on this place ,animate it

        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);

        mGoogleMap.animateCamera(update);

    }



    private void setMarker(String location, double lat, double lng, String Colour) {

        // add marker giving it the location string and the latlng if the marker colour is red we add marker to location if the marker colour is Green we add marker from location
        if (Colour == "Red") {

            if (markerTo != null)
            {
                markerTo.remove();
            }

            MarkerOptions mMarkerOptions1 = new MarkerOptions()
                    .title(location)
                    .position(new LatLng(lat, lng))
                    .snippet("I wanna go there");
            markerTo = mGoogleMap.addMarker(mMarkerOptions1);


        } else if (Colour == "Green") {
            if (markerFrom != null)
            {
                markerFrom.remove();

            }
            MarkerOptions mMarkerOptions = new MarkerOptions()
                    .title(location)
                    .position(new LatLng(lat, lng))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .snippet("I am here");
            markerFrom = mGoogleMap.addMarker(mMarkerOptions);


        }

        goToLocationZoom(lat,lng,15);

    }

    public void geoLocateAddress(double lat, double lng, String Colour) throws IOException {
        Geocoder geo = new Geocoder(this, Locale.getDefault());
        List<android.location.Address> latLngs = geo.getFromLocation(lat, lng, 1);

        int num = latLngs.get(0).getMaxAddressLineIndex();
        String fullAddress="";
        for (int i=0;i<num;i++)
        {
            fullAddress+=latLngs.get(0).getAddressLine(i)+" ";
        }

        if (Colour=="Red")
        {
           mToFragement.setText(fullAddress+latLngs.get(0).getCountryName());
        }

        if (Colour=="Green")
        {
            mFromFragement.setText(fullAddress+latLngs.get(0).getCountryName());

        }


        setMarker(fullAddress, lat, lng, Colour);

    }

    LocationRequest mLocationRequest;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Toast.makeText(this, "cannot connect", Toast.LENGTH_LONG).show();

        } else {
            LatLng mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            double lat = location.getLatitude();
            double lng = location.getLongitude();

            CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngZoom(mLatLng, 15);
            mGoogleMap.animateCamera(mCameraUpdate);

            try {

                geoLocateAddress(lat, lng, "Green");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }




    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {
        //makeRoute call onRouting Success



        int numOfRoutes= arrayList.size();
        int counter;

        options =new PolylineOptions()
                                .width(7)
                                .color(Color.RED)
                                .geodesic(true);
        //find number of routes and make routes according to it
        for (counter=0;counter<numOfRoutes;counter++)
        {

            List<LatLng> LatLng = arrayList.get(counter).getPoints();

           int numPointSize = arrayList.get(counter).getPoints().size();

            //find number of points of each route and draw the path using PolylineOptions

            for (int CounterPoints=0;CounterPoints<numPointSize;CounterPoints++)
            {

                LatLng mLatLng =LatLng.get(CounterPoints);
                options.add(mLatLng);
            }

        }
        RemoveTheShape();
        shape = mGoogleMap.addPolyline(options);




    }


    @Override
    public void onRoutingCancelled() {

    }
    private void RemoveTheShape() {

        if (shape!=null) {
            shape.remove();
        }


    }



}
