package com.cnrc.ricediseaseidentifier;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1;
    private Button btn_openCam;
    private void setBtn_openCam(){
        this.btn_openCam = (Button) findViewById(R.id.btn_OpenCam);
    }
    private ImageView imageView;
    private void setImageView(){
        this.imageView = (ImageView) findViewById(R.id.imgView);
    }
    Intent cameraIntent;


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //bind xml layout widgets:
        setBtn_openCam();
        setImageView();

        //btnOnclick:
        btn_openCamOnClick();
    }

    private void btn_openCamOnClick() {
        btn_openCam.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //test:
                        System.out.println("Button Works.");
                        //OpenCamIntent
                        openCamIntentActivities();
                    }
                }
        );
    }

    private void openCamIntentActivities(){
        //create an instance of Cam Intent:
        createCamIntentInstance();
        //Set filePath of the photo captured from cam:
        setCamFilePath();
        startActivityForResult(cameraIntent, CAMERA_REQUEST);

    }

    private void createCamIntentInstance(){
        this.cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    }

    private void setCamFilePath(){
        //CREATE A FILE DIRECTORY FOR THE PHOTO
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //CREATE A METHOD THAT WILL CREATE YOUR FILENAME: (EX. PICTUREDIRECTORY/<pictureName>)
        String pictureName = getPictureName();
        //Combine the pictureDirectory and getPictureName in order to create a correct path to your Storage directory.
        File imageFile = new File(pictureDirectory, pictureName);
        //URI (Unified Resouce Idenfifier):A String of characters used to identify a resource/filePath.
        Uri pictureUri = Uri.fromFile(imageFile);
        //Now save the extra output(Picture from cam, that is) into the file object we created(which is a filepath,technically).
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
    }

    private String getPictureName(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        return "ricePhoto" + timestamp + ".jpg";
    }
    //static call to check if openCV is Loaded.


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            //Leave it for later:
            if (requestCode == CAMERA_REQUEST) {
                //set thumbnail for the layout
            }
        }
    }

    static{
        if(OpenCVLoader.initDebug()){
            Log.i("MainActivity", "OpenCV Loaded");
        }
        else
        {
            Log.i("MainActivity", "OpenCV failed to load");
        }
    }



}
