package com.example.rikym.thiefttracker;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class reporte extends Activity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ImageButton acerca;
    private DatePicker fecha;
    private TimePicker hora;
    private Button enviar;
    private Spinner insidente;
    private int tipoPos = 0;
    private double lat = 0;
    private double lon = 0;
    private int flag = 0;
    private static final int myPermiso = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fecha = (DatePicker)findViewById(R.id.fecha);
        hora = (TimePicker)findViewById(R.id.hora);
        acerca = (ImageButton) findViewById(R.id.acercade);
        acerca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(reporte.this, Acerca.class);
                startActivity(i);
            }
        });
        insidente = (Spinner)findViewById(R.id.tipo);
        List<String> tipos = new ArrayList<String>();
        tipos.add("Selecciona el tipo de incidente");
        tipos.add("Robo casa habitacion");
        tipos.add("Robo automovil");
        tipos.add("Asalto");
        tipos.add("Vandalismo");
        tipos.add("Violacion");
        tipos.add("Drogadictos/Borrachos");
        ArrayAdapter<String> t = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tipos);
        insidente.setAdapter(t);
        insidente.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipoPos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        enviar = (Button)findViewById(R.id.btnGuardar);
        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lat == 0 && lon == 0){
                    Toast.makeText(getApplicationContext(),"Error\nFaltan seleccionar un punto en el mapa",Toast.LENGTH_LONG).show();
                }else if(tipoPos == 0){
                    Toast.makeText(getApplicationContext(),"Error\nFaltan seleccionar un tipo de insidencia",Toast.LENGTH_LONG).show();
                }else{
                    registrarSuceso();
                    Intent i = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        });
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, myPermiso);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        LatLng posUsu = new LatLng(20.5957619, -100.3911671);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(posUsu));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng));
                lat = latLng.latitude;
                lon = latLng.longitude;
            }
        });
    }

    private void registrarSuceso(){
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                "http://nmrapp.hol.es/insertar_registro.php",
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response){
                        try{
                            System.out.println(response + "");
                            JSONObject respuesta = new JSONObject(response);
                            procesarRespuesta(respuesta);
                        }catch(JSONException jsone){
                            jsone.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.d("Registrado Fallido", error.toString());
                        Toast.makeText(getApplicationContext(), "No se pudo hacer el registro", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("latitud", Double.toString(lat));
                params.put("longitud", Double.toString(lon));
                params.put("fechaSuceso", fecha.getYear() + "-" + fecha.getMonth()+ "-" + fecha.getDayOfMonth()+ " " + hora.getCurrentHour() + ":" + hora.getCurrentMinute() + ":00");
                params.put("idTipo", Integer.toString(tipoPos));
                return params;
            }
        };
        RequestQueue rq = Volley.newRequestQueue(reporte.this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rq.add(stringRequest);
    }

    private void procesarRespuesta(JSONObject respuesta){
        try{
            String estado = respuesta.getString("estado");
            String mensaje = respuesta.getString("mensaje");
            switch (estado){
                case "1":
                    Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                    break;
                case "0":
                    Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                    break;
            }
            return;
        }catch(JSONException json){
            json.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case myPermiso: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                            @Override
                            public void onMyLocationChange(Location location) {
                                LatLng coordenadas = new LatLng(location.getLongitude(), location.getLatitude());
                                CameraUpdate camara = CameraUpdateFactory.newLatLngZoom(coordenadas, 15);
                                mMap.animateCamera(camara);
                            }
                        });
                    }
                } else {

                }
                return;
            }
        }
    }
}
