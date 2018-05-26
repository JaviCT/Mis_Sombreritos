package com.example.javie.practicarecuperacion;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.support.v4.app.NotificationCompat;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Context yo = this;
    private final int MY_PERMISSIONS = 100;
    SQLiteDatabase db;
    LocationManager locationManager;

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

        myList = new ArrayList<Lista>();
        lista1 = (ListView) findViewById(R.id.lista1);
        HacerConsulta hacerConsulta = new HacerConsulta();
        hacerConsulta.execute(null, null, null);
        final ImageView ver = (ImageView) findViewById(R.id.ver);

        lista1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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

        if (!checkLocation()){
            return;
        }
    }

    private boolean checkLocation() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Activar la ubicación")
                .setMessage("Su ubicación esta desactivada.\npor favor active su ubicación.")
                .setPositiveButton("Configuración de ubicación", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
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

    private class HacerConsulta extends AsyncTask<Object, Integer, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Object[] objects) {
            db = openOrCreateDatabase("practicafinal", Context.MODE_PRIVATE, null);
            db.execSQL("Create table if not exists fotos (imagen VARCHAR, personas VARCHAR, fecha DATE, latitud VARCHAR, longitud VARCHAR);");
            Cursor cursor= db.rawQuery("Select * from fotos;",null);
            if (myList.size() != cursor.getColumnCount()){
                myList.removeAll(myList);
            }
            if(cursor.getCount() == 0){
                System.out.println("no encontrado");
            }
            else{
                while(cursor.moveToNext()){
                    Lista lista = new Lista();
                    lista.setImage(cursor.getString(0));
                    lista.setPersonas(cursor.getString(1));
                    lista.setFecha(cursor.getString(2));
                    lista.setLat(cursor.getString(3));
                    lista.setLongi(cursor.getString(4));
                    myList.add(lista);
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //Collections.reverse(listaMensajes);
                    adaptadorPersonalizado = new AdaptadorPersonalizado(yo, myList);
                    lista1.invalidateViews();
                    lista1.setAdapter(adaptadorPersonalizado);
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            NotificationManager mNotifyMgr =(NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(yo, "test")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle("Lista cargada correctamente")
                    .setContentText("Hay "+ myList.size() + " imagenes capturadas");
            mNotifyMgr.notify(1, mBuilder.build());
        }
    }

    public class Lista{
        String image;
        String personas;
        String fecha;
        String lat;
        String longi;

        public String getImage(){
            return image;
        }

        public void setImage(String image){
            this.image = image;
        }

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

            ImageView image = (ImageView) view.findViewById(R.id.imagen);
            TextView personas = (TextView) view.findViewById(R.id.personas);
            TextView fecha = (TextView) view.findViewById(R.id.fecha);
            TextView latitud = (TextView) view.findViewById(R.id.latitud);
            TextView longitud = (TextView) view.findViewById(R.id.longitud);

            new Lista();
            Lista lista;
            lista = this.lista.get(i);
            image.setImageURI(Uri.fromFile(new File(lista.getImage())));
            personas.setText("Personas: " + lista.getPersonas());
            fecha.setText("Fecha: " + lista.getFecha());
            latitud.setText("Latitud: " + lista.getLat());
            longitud.setText("Longitud: " + lista.getLongi());

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
