package com.example.javie.practicarecuperacion;

import android.support.v7.widget.ShareActionProvider;
import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Xml;
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

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;

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

    private String writeXml(){
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        db = openOrCreateDatabase("practicafinal", Context.MODE_PRIVATE, null);
        db.execSQL("Create table if not exists fotos (imagen VARCHAR, personas VARCHAR, fecha DATE, latitud VARCHAR, longitud VARCHAR);");
        Cursor cursor= db.rawQuery("Select * from fotos;",null);
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "imagenes");

            if(cursor.getCount() == 0){
                System.out.println(getString(R.string.not_found));
            }
            else{
                while(cursor.moveToNext()){
                    serializer.startTag("", "imagen");
                        serializer.startTag("", cursor.getColumnName(0));
                        serializer.text(cursor.getString(0));
                        serializer.endTag("", cursor.getColumnName(0));
                        serializer.startTag("", cursor.getColumnName(1));
                        serializer.text(cursor.getString(1));
                        serializer.endTag("", cursor.getColumnName(1));
                        serializer.startTag("", cursor.getColumnName(2));
                        serializer.text(cursor.getString(2));
                        serializer.endTag("", cursor.getColumnName(2));
                        serializer.startTag("", cursor.getColumnName(3));
                        serializer.text(cursor.getString(3));
                        serializer.endTag("", cursor.getColumnName(3));
                        serializer.startTag("", cursor.getColumnName(4));
                        serializer.text(cursor.getString(4));
                        serializer.endTag("", cursor.getColumnName(4));
                    serializer.endTag("", "imagen");
                }
            }

            serializer.endTag("", "imagenes");
            serializer.endDocument();
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkLocation() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.activate_location))
                .setMessage(getString(R.string.sent_location))
                .setPositiveButton(getString(R.string.conf_location), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
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

                    System.out.println(getString(R.string.permission));
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
                System.out.println(getString(R.string.not_found));
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
                    .setContentTitle(getString(R.string.correctly_loaded))
                    .setContentText(getString(R.string.there_are) + " " + myList.size() + " " + getString(R.string.saved_pictures));
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
            personas.setText(getString(R.string.persons) + ": " + lista.getPersonas());
            fecha.setText(getString(R.string.date) + ": " + lista.getFecha());
            latitud.setText(getString(R.string.latitude) + ": " + lista.getLat());
            longitud.setText(getString(R.string.longitude) + ": " + lista.getLongi());

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
        if (id == R.id.action_settings){
            //mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build( ));

            String filename = "myfile.xml";
            String fileContents = writeXml();
            System.out.println(fileContents);
            String path = yo.getExternalCacheDir().toString();
            System.out.println(path);

            File file = new File(yo.getExternalCacheDir(), filename);

            FileOutputStream outputStream;

            try {
                outputStream = new FileOutputStream(file);//openFileOutput(path + filename, Context.MODE_PRIVATE);
                outputStream.write(fileContents.getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(file.exists()) {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/xml");
                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path + "/" + filename));
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), 101);
            }else{
                Toast.makeText(yo, getString(R.string.no_file), Toast.LENGTH_SHORT).show();
            }

        }

        return super.onOptionsItemSelected(item);
    }
}
