package com.example.javie.practicarecuperacion;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Context yo = this;
    final static int IMAGEN_CAPTURADA = 1;
    String pathFichero = null;
    private static final Integer PIDO_PERMISO = 1;
    private final int MY_PERMISSIONS = 100;
    SQLiteDatabase db;
    double longitudeBest = 0.0;
    double latitudeBest = 0.0;
    LocationManager locationManager;
    final Handler handler = new Handler();
    String rutaimg = "";

    ListView lista1;
    AdaptadorPersonalizado adaptadorPersonalizado;
    ArrayList<Lista> myList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, nuevaImagen.class);
                startActivity(intent);
            }
        });

        //PEDIR PERMISOS
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        int accessFinePermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        int accessStoragePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int cameraPermission = checkSelfPermission(Manifest.permission.CAMERA);

        if (cameraPermission == PackageManager.PERMISSION_GRANTED && accessFinePermission == PackageManager.PERMISSION_GRANTED && accessStoragePermission == PackageManager.PERMISSION_GRANTED) {
            //se realiza metodo si es necesario...
        } else {
            requestPermissions(perms, MY_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    System.out.println("Hay permiso");
                } else {

                }
                return;
            }
        }
    }

    public class Lista{
        String personas;
        String fecha;
        String lat;
        String longi;

        public String getPersonas() {
            return personas;
        }

        public void setPersonas(String personas) {
            this.personas = personas;
        }

        public String getFecha() {
            return fecha;
        }

        public void setFecha(String fecha) {
            this.fecha = fecha;
        }

        public String getLongi() {
            return longi;
        }

        public void setLongi(String longi) {
            this.longi = longi;
        }

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }
    }

    public class AdaptadorPersonalizado extends BaseAdapter {
        Context context;
        ArrayList<Lista> lista;
        LayoutInflater inflater = null;
        @Override
        public int getCount() {
            return this.lista.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = inflater.inflate(R.layout.lista, null);

            TextView personas = (TextView) view.findViewById(R.id.personas);
            TextView fecha = (TextView) view.findViewById(R.id.fecha);
            TextView latitud = (TextView) view.findViewById(R.id.latitud);
            TextView longitud = (TextView) view.findViewById(R.id.longitud);

            new Lista();
            Lista lista;
            lista = this.lista.get(i);
            personas.setText(lista.getPersonas());
            fecha.setText(lista.getFecha());
            latitud.setText(lista.getLat());
            longitud.setText(lista.getLongi());

            return view;

        }

        public AdaptadorPersonalizado(Context context, ArrayList<Lista> lista){
            this.context= context;
            this.lista = lista;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
