package com.example.javie.practicarecuperacion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;

import static org.opencv.core.CvType.CV_8UC1;

public class opencvCamera extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    JavaCameraView javaCameraView;
    Button capturar;
    private boolean showPreviews;
    Mat mRgba;
    Context yo;
    private static final String TAG="MainActivity";
    private CascadeClassifier cascadeClassifier;
    private int absoluteFaceSize;
    Rect[] facesArray;
    Mat m2;

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS:{
                    initializeOpenCVDependencies();
                    Log.i(TAG, "OpenCV loaded successfully");
                    javaCameraView.setMaxFrameSize(800, 600);
                    javaCameraView.enableView();
                    javaCameraView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return false;
                        }
                    });
                    javaCameraView.setCvCameraViewListener(opencvCamera.this);
                    capturar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showPreviews = !showPreviews;

                            MatOfRect faces = new MatOfRect();
                            if (cascadeClassifier != null) {
                                cascadeClassifier.detectMultiScale(mRgba, faces, 1.1, 2, 2, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
                            }
                            facesArray = faces.toArray();
                            String personicas = String.valueOf(facesArray.length);
                            for (int i = 0; i <facesArray.length; i++){
                                try {
                                    Bitmap icon = BitmapFactory.decodeResource(yo.getResources(), R.mipmap.hat);
                                    Mat mat2 = new Mat();
                                    Bitmap bmp32 = icon.copy(Bitmap.Config.ARGB_8888, true);
                                    Utils.bitmapToMat(bmp32, mat2);
                                    Mat mat3 = new Mat();
                                    Rect rect = new Rect(facesArray[i].x, facesArray[i].y - facesArray[i].height, facesArray[i].width, facesArray[i].height);
                                    Mat submat = mRgba.submat(rect);
                                    Imgproc.resize( mat2, mat3, facesArray[i].size());
                                    Mat mat4 = overtrayImage(submat, mat3);
                                    mat4.copyTo(submat);
                                }catch (Exception e) {
                                    Log.e("OpenCVActivity", "Error loading cascade", e);
                                }
                            }

                            int w = mRgba.width();
                            int h = mRgba.height();
                            Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(mRgba , bm);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bm.compress(Bitmap.CompressFormat.JPEG, 50, stream);

                            Intent intent = new Intent(yo, nuevaImagen.class);
                            intent.putExtra("personas", personicas);
                            intent.putExtra("picture", stream.toByteArray());
                            startActivity(intent);
                        }
                    });
                    break;
                }
                default:{
                    super.onManagerConnected(status);
                    break;
                }
            }
            super.onManagerConnected(status);
        }
    };

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opencv_camera);

        yo = this;
        capturar = (Button) findViewById(R.id.button3);

        javaCameraView = (JavaCameraView)findViewById(R.id.java_camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(javaCameraView!=null){
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(javaCameraView!=null){
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(OpenCVLoader.initDebug()){
            Log.d(TAG, "Opencv successfully");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else {
            Log.d(TAG, "Opencv not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        absoluteFaceSize = (int) (height * 0.2);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if(!showPreviews){
            mRgba = inputFrame.rgba();
            MatOfRect faces = new MatOfRect();

            // Use the classifier to detect faces
            if (cascadeClassifier != null) {
                cascadeClassifier.detectMultiScale(mRgba, faces, 1.1, 2, 2, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
            }

            // If there are any faces found, draw a rectangle around it
            facesArray = faces.toArray();

            for (int i = 0; i <facesArray.length; i++){
                try {
                    Bitmap icon = BitmapFactory.decodeResource(yo.getResources(), R.mipmap.hat);
                    Mat mat2 = new Mat();
                    Bitmap bmp32 = icon.copy(Bitmap.Config.ARGB_8888, true);
                    Utils.bitmapToMat(bmp32, mat2);
                    Mat mat3 = new Mat();
                    Rect rect = new Rect(facesArray[i].x, facesArray[i].y - facesArray[i].height, facesArray[i].width, facesArray[i].height);
                    Mat submat = mRgba.submat(rect);
                    Imgproc.resize( mat2, mat3, facesArray[i].size());
                    Mat mat4 = overtrayImage(submat, mat3);
                    mat4.copyTo(submat);
                }catch (Exception e) {
                    Log.e("OpenCVActivity", "Error loading cascade", e);
                }

            }
        }
        return mRgba;
    }

    private Mat overtrayImage( Mat background, Mat foreground ) {
        // The background and the foreground are assumed to be of the same size.
        Mat destination = new Mat( background.size(), background.type() );

        for ( int y = 0; y < ( int )( background.rows() ); ++y ) {
            for ( int x = 0; x < ( int )( background.cols() ); ++x ) {
                double b[] = background.get( y, x );
                double f[] = foreground.get( y, x );

                double alpha = f[3] / 255.0;

                double d[] = new double[4];
                for ( int k = 0; k < 4; ++k ) {
                    d[k] = f[k] * alpha + b[k] * ( 1.0 - alpha );
                }

                destination.put( y, x, d );
            }
        }

        return destination;
    }
}
