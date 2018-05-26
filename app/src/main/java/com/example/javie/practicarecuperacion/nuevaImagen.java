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
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;

import org.opencv.android.Utils;
import org.opencv.objdetect.CascadeClassifier;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class nuevaImagen extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback {

    ImageView imagen;
    TextView persona;
    TextView date;
    TextView latitud;
    TextView longitud;
    Button guardar;
    Button cancelar;
    Context yo;
    private static int RESULT_LOAD_IMAGE = 1;
    private CascadeClassifier cascadeClassifier;
    private int absoluteFaceSize;
    private static Bitmap bitmap = null;
    GoogleApiClient mGoogleApiClient = null;
    private static final String TAG = "MainActivity";
    SQLiteDatabase db;
    String personas;
    String formattedDate;
    String lat;
    String longi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_imagen);

        yo = this;

        imagen = (ImageView) findViewById(R.id.imageView2);

        //Set image from camera
        if (getIntent().hasExtra("picture")) {
            bitmap = BitmapFactory.decodeByteArray(getIntent().getByteArrayExtra("picture"), 0, getIntent().getByteArrayExtra("picture").length);
            imagen.setImageBitmap(bitmap);
        }

        //set personas
        persona = (TextView) findViewById(R.id.textView);
        if (getIntent().hasExtra("personas")) {
            personas = getIntent().getExtras().getString("personas");
            persona.setText(persona.getText() + ": " + personas);
        }

        //set date
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        formattedDate = df.format(c);
        date = (TextView) findViewById(R.id.textView2);
        date.setText(date.getText() + ": " + formattedDate);

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlert();
            }
        });

        //set location

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        //guardar
        guardar = (Button) findViewById(R.id.button);
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (personas == null){
                    Toast.makeText(yo, "Debes capturar una imagen", Toast.LENGTH_SHORT).show();
                }else{
                    guardar(personas, formattedDate, lat, longi);
                    NotificationManager mNotifyMgr =(NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(yo, "test")
                            .setSmallIcon(R.drawable.ic_launcher_background)
                            .setContentTitle("Imagen Capturada")
                            .setContentText(" Se han dibujado "+ personas + " sombreros.");
                    mNotifyMgr.notify(1, mBuilder.build());
                    Intent intent = new Intent(yo, MainActivity.class);
                    startActivity(intent);
                }
            }
        });

        //cancelar
        cancelar = (Button) findViewById(R.id.button2);
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(yo, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        latitud = (TextView) findViewById(R.id.textView3);
        longitud = (TextView) findViewById(R.id.textView4);
        Location mLastLocation;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            latitud.setText(latitud.getText() + ": " + String.valueOf(mLastLocation.getLatitude()));
            longitud.setText(longitud.getText() + ": " + String.valueOf(mLastLocation.getLongitude()));

            while(lat == null){
                lat = String.valueOf(mLastLocation.getLatitude());
                longi = String.valueOf(mLastLocation.getLongitude());

                System.out.println(lat);
                System.out.println(longi);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    public void guardar(String personas, String fecha, String latitud, String longitud){
        if (persona.getText() == "Personas"){
            Toast.makeText(yo, "Introduce una imagen", Toast.LENGTH_SHORT).show();
        }else{
            //to get the image from the ImageView (say iv)
            BitmapDrawable draw = (BitmapDrawable) imagen.getDrawable();
            Bitmap bitmap = draw.getBitmap();

            FileOutputStream outStream = null;
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/pictures");
            dir.mkdirs();
            String fileName = String.format("%d.jpg", System.currentTimeMillis());
            File outFile = new File(dir, fileName);
            String path = outFile.getAbsolutePath();
            System.out.println(path);
            try {
                outStream = new FileOutputStream(outFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            db = openOrCreateDatabase("practicafinal", Context.MODE_PRIVATE, null);
            db.execSQL("Create table if not exists fotos (imagen VARCHAR, personas VARCHAR, fecha DATE, latitud VARCHAR, longitud VARCHAR);");
            db.execSQL("Insert into fotos values ('" + path + "' , '" + personas + "' , '" + fecha + "', '" + lat + "', '" + longi + "');");
        }
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Elige una foto")
                .setMessage("Seleccionar foto de la galeria \no hacer una foto.")
                .setPositiveButton("Galeria", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(i, RESULT_LOAD_IMAGE);
                    }
                })
                .setNegativeButton("Camara", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent intent = new Intent(yo, opencvCamera.class);
                        startActivity(intent);
                    }
                });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap myImage = BitmapFactory.decodeFile(picturePath);

            Mat mat = new Mat(myImage.getHeight(), myImage.getWidth(), CvType.CV_8UC4);
            Bitmap bmp32 = myImage.copy(Bitmap.Config.ARGB_8888, true);
            Utils.bitmapToMat(bmp32, mat);
            MatOfRect faces = new MatOfRect();

            // Use the classifier to detect faces
            absoluteFaceSize = (int) (myImage.getHeight() * 0.2);
            initializeOpenCVDependencies();
            if (cascadeClassifier != null) {
                cascadeClassifier.detectMultiScale(mat, faces, 1.1, 2, 2, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
            }

            // If there are any faces found, draw a rectangle around it
            Rect[] facesArray = faces.toArray();
            System.out.println(facesArray.length);

            ImageView imageView = (ImageView) findViewById(R.id.imageView2);
            imageView.setImageBitmap(myImage);

        }


    }

    private void initializeOpenCVDependencies() {

        try {
            // Copy the resource into a temp file so OpenCV can load it
            InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);


            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            // Load the cascade classifier
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade", e);
        }
    }
}
