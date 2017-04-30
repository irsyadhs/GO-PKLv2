package go_pkl.irsyaddhs.cs.upi.edu.go_pkl;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static go_pkl.irsyaddhs.cs.upi.edu.go_pkl.AppConfig.*;

public class MainMenu extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private static final int MY_PERMISSIONS_REQUEST = 99;//int bebas, maks 1 byte
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Marker me;
    ArrayList<Marker> pMarker = new ArrayList<Marker>();
    static final int ACT2_REQUEST = 99; //request code
    public String terimaBanding;
    public int nameExist;
    Intent i = new Intent(this, RegisterAct.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        buildGoogleApiClient();
        createLocationRequest();
        SharedPreferences sp = getSharedPreferences(SP,MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("id",1);

        int status = sp.getInt("stt", 0);

        if(status == 0){
            startActivityForResult(i, ACT2_REQUEST);
            ed.putInt("stt", 1);


        }else if(status == 1){
            sp.getString("un", "");
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        // /cek request code
        if(requestCode == ACT2_REQUEST) {
            final String[] terima = data.getStringArrayExtra("strings");
            terimaBanding = terima[0];
            handlePlayer(SELECT_PEMBELI, 0);

            if (nameExist == 1) {
                AlertDialog ad = new AlertDialog.Builder(this).create();
                ad.setMessage("Username already existed");
                ad.show();
                //startActivityForResult(i, ACT2_REQUEST);
            } else if (nameExist == 0) {
                SharedPreferences sp = getSharedPreferences(SP, MODE_PRIVATE);
                SharedPreferences.Editor ed = sp.edit();
                ed.putString("un", terima[0]);

                StringRequest postRequest = new StringRequest(Request.Method.POST, INSERT_PEMBELI,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // menampilkan respone
                                Log.d("Response POST", response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // error
                                Log.e(TAG, "onErrorResponse: Error= " + error);
                                Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());
                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        // Menambahkan parameters post
                        Map<String, String> params = new HashMap<String, String>();

                        params.put("id", "");
                        params.put("nama", terima[0]);
                        params.put("lat", "");
                        params.put("long", "");
                        params.put("req", "");

                        return params;
                    }
                };
                AppController.getInstance().addToRequestQueue(postRequest);

            }
        }
    }
    /**
     * Request location every 10 second
     * Request location high accuracy
     */
    protected void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        // 10 detik sekali meminta lokasi 10000ms = 10 detik
        mLocationRequest.setInterval(10000);
        // tapi tidak boleh lebih cepat dari 5 detik
        mLocationRequest.setFastestInterval(500);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * enable google location services API
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission diberikan, mulai ambil lokasi
                buildGoogleApiClient();

            } else {
                //permssion tidak diberikan, tampilkan pesan
                AlertDialog ad = new AlertDialog.Builder(this).create();
                ad.setMessage("Tidak mendapat ijin, tidak dapat mengambil lokasi");
                ad.show();
            }
            return;
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }
    private void handlePlayer(String slct, final int st){
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, slct, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.i(TAG, "onResponse: playerResult= " + response.toString());
                        parsePlayer(response, st);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //menampilkan error pada logcat
                        Log.e(TAG, "onErrorResponse: Error= " + error);
                        Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());

                    }
                }
        );

        AppController.getInstance().addToRequestQueue(request);
    }
    private void parsePlayer(JSONObject result, int st){
        if(st == 1) {
            String id, name, tipe;
            double latitude, longitude;


            try {
                JSONArray jsonArray = result.getJSONArray("users");

                if (result.getString("success").equalsIgnoreCase("1")) {

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject pedagang = jsonArray.getJSONObject(i);

                        id = pedagang.getString("id");

                        name = pedagang.getString("nama");
                        tipe = pedagang.getString("tipe");

                        latitude = pedagang.getDouble("lat");
                        longitude = pedagang.getDouble("long");
                        LatLng latLng = new LatLng(latitude, longitude);


                        MarkerOptions mo = new MarkerOptions().position(latLng).title(name).snippet(tipe).icon(BitmapDescriptorFactory.fromResource(R.drawable.markersellers));
                        Log.d("opsi", mo.toString());
                        pMarker.add(mMap.addMarker(mo));

                    }
                } else if (result.getString("success").equalsIgnoreCase("0")) {

                }
            } catch (JSONException e) {

                e.printStackTrace();
                Log.e(TAG, "parseLocationResult: Error=" + e.getMessage());
            }
        }else if(st == 0){
            String name;
            int stop;
            stop = 0;
            try {
                JSONArray jsonArray = result.getJSONArray("users");

                if (result.getString("success").equalsIgnoreCase("1")) {
                    int i;
                    i = 0;
                    while((i < jsonArray.length()) && (stop == 0)) {
                        JSONObject pembeli = jsonArray.getJSONObject(i);
                        name = pembeli.getString("nama");
                        if(name == terimaBanding){
                            nameExist = 1;
                            stop = 1;
                        }else{
                            nameExist = 0;
                        }
                        i++;
                    }
                } else if (result.getString("success").equalsIgnoreCase("0")) {

                }
            } catch (JSONException e) {

                e.printStackTrace();
                Log.e(TAG, "parseLocationResult: Error=" + e.getMessage());
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            // tampilkan dialog minta ijin
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST);
//            mMap.setMyLocationEnabled(true);
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            location.setLatitude(-6.860422);
            location.setLongitude(107.589905);
        }
        me = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title("You"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),17));
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        handlePlayer(SELECT_PEDAGANG, 1);
        for(int i = 0;i < pMarker.size(); i++){

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        me.remove();
//        mMap.clear();
        me =  mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title("You"));
        for(int i=0;i<pMarker.size();i++){
//            mMap.addMarker()
        }
        final Location loc = location;
        StringRequest postRequest = new StringRequest(Request.Method.POST, UPDATE_PEMBELI,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // menampilkan respone
                        Log.d("Response POST", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.e(TAG, "onErrorResponse: Error= " + error);
                        Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                // Menambahkan parameters post
                Map<String, String>  params = new HashMap<String, String>();
                SharedPreferences sp = getSharedPreferences(SP,MODE_PRIVATE);
                params.put("nama",sp.getString("un",""));
                params.put("lat", String.valueOf(loc.getLatitude()));
                params.put("long", String.valueOf(loc.getLongitude()));

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(postRequest);

    }
}
