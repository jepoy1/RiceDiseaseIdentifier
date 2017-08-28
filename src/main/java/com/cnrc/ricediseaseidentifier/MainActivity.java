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

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private final String TAG= "MainActivity";
    private static final int CAMERA_REQUEST = 1;
    private Button btn_openCam;
    private void setBtn_openCam(){
        this.btn_openCam = (Button) findViewById(R.id.btn_OpenCam);
    }
    private ImageView imageView;
    private void setImageView(){
        this.imageView = (ImageView) findViewById(R.id.imgView);
    }

    private TextView txtView_ypercent;
    private void setTxtView_ypercent(){ this.txtView_ypercent = (TextView)findViewById(R.id.txtView_ypercent);}
    private TextView txtView_gpercent;
    private void setTxtView_gpercent(){ this.txtView_gpercent = (TextView)findViewById(R.id.txtView_gpercent);}
    private TextView txtView_lines;
    private void setTxtView_lines(){ this.txtView_lines = (TextView)findViewById(R.id.txtView_lines);}
    private TextView txtView_circles;
    private void setTxtView_circles(){ this.txtView_circles = (TextView)findViewById(R.id.txtView_circles);}

    Intent cameraIntent;
    Uri pictureUri;
    private String mImgPath;
    Bitmap imageViewBitmap;

    //fields for my openCV:
    Bitmap tempBmp;


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
        setTxtView_ypercent();
        setTxtView_gpercent();
        setTxtView_lines();
        setTxtView_circles();
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
        this.pictureUri = Uri.fromFile(imageFile);
        //Now save the extra output(Picture from cam, that is) into the file object we created(which is a filepath,technically).
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, this.pictureUri);
    }

    private String getPictureName(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        return "ricePhoto" + timestamp + ".jpg";
    }
    //static call to check if openCV is Loaded.



    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    //test:
                    Log.i(TAG, mImgPath + " on LoaderCallBackInterface.SUCCESS");
                    if(!isImgPathNull(mImgPath)){
                        //Instanciate Mat object using imread.
                        Bitmap bmp = setOpenCVbmp(tempBmp);
                        Mat x = new Mat();
                        Utils.bitmapToMat(bmp, x);
                        if(x.empty()){
                            Log.i(TAG, "fuck this shit! Details: " + x);
                        }else{
                            Log.i(TAG, "Fuck yes! Details: " + x);
                        }

                        //crop image:
                        Mat cropped = new Mat();
                        cropped = crop_image(x);

                        Mat bFilter = new Mat();
                        Mat lines = new Mat();
                        Mat circles = new Mat();

                        double pYellow = percent_yellow(cropped);
                        double pGreen = percent_green(pYellow);
                        bFilter = brownFilter(x);
                        lines = getLines(bFilter, 1.0, 0.5, 2);
                        circles = getCircles(x);

                        //Outputs:
                        txtView_ypercent.setText(" " +pYellow);
                        txtView_gpercent.setText(" " +pGreen);
                        txtView_lines.setText(" " +lines.size());
                        txtView_circles.setText(" "+circles.size());
                    }

                    /*
                    System.out.println("% Y = " + pYellow);
                    System.out.println("% G = " + pGreen);
                    System.out.println("lines = " + lines.size());
                    System.out.println("circles = " + circles.size());
                    */
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0,this, mLoaderCallback);
        // you may be tempted, to do something here, but it's *async*, and may take some time,
        // so any opencv call here will lead to unresolved native errors.
        if(!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0,this, mLoaderCallback)){
            Log.i(TAG, "OpenCVLoader did not load.");
        }
        else{
            Log.i(TAG, "OpenCVLoader Loaded in onResume().");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            //Leave it for later:
            if (requestCode == CAMERA_REQUEST) {
                setThumbnailCamPhoto();
                this.mImgPath = pictureUri.toString();
                Log.i(TAG, mImgPath + " on activity result.");
            }
        }
    }

    private void setThumbnailCamPhoto(){
        //get the address of the photo:
        Uri thumbnailUri = this.pictureUri;
        //declare a stream to read the image data from Local Directory:
        InputStream inputStream;
        //get an inputStream based on the imageURI:
        try {
            inputStream = getContentResolver().openInputStream(pictureUri);
            // get a bitmap from the stream.
            Bitmap image = BitmapFactory.decodeStream(inputStream);
            this.imageViewBitmap = image;
            // show the image to the user
            imageView.setImageBitmap(image);
            //to be used for openCV:
            this.tempBmp = setOpenCVbmp(image);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // show a message to the user indictating that the image is unavailable.
            Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
        }
    }
    private Bitmap setOpenCVbmp(Bitmap image){
        Bitmap result = null;

        int numPixels = image.getWidth() * image.getHeight();
        int[] pixels = new int[numPixels];

//        get jpeg pixels, each int is the color value of one pixel
        image.getPixels(pixels,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());

//        create bitmap in appropriate format
        result = Bitmap.createBitmap(image.getWidth(),image.getHeight(), Bitmap.Config.ARGB_8888);

//        Set RGB pixels
        result.setPixels(pixels, 0, result.getWidth(), 0, 0, result.getWidth(), result.getHeight());

        return result;
    }

    //helper methods:
    private boolean isImgPathNull(String mImgPath){
        //if mImagePath is null, do nothing. else proceed:
        if(mImgPath == null){
            Log.i(TAG, " on test null");
            return true;
        }
        else{
            if(mImgPath != null){
                Log.i(TAG, mImgPath + " <- path");
                return false;
            }
        }
        return mImgPath == null;
    }

    /*
        Image Processing;
        1. Image Segmentation;
        2. Feature extraction
        3. Rule-based classification
     */

    public static Mat crop_image(Mat image_input){
        Mat image_orig = image_input;
        Rect rectCrop = new Rect(image_orig.cols()/2-25, 0, 50, image_orig.rows());
        Mat cropped = image_orig.submat(rectCrop);
        Log.i("MainActivity", "image cropped");
        return cropped;
    }

    public static double percent_yellow(Mat cropped_image) {

        Mat grayscale = cropped_image;
        Imgproc.cvtColor(grayscale, grayscale, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(grayscale, grayscale,0,255, Imgproc.THRESH_OTSU);
        int x = Core.countNonZero(grayscale);
        double y = (double) x/(grayscale.cols()*grayscale.rows());

        return y;
    }

    public static double percent_green(double percent_yellow){
        return 1.0 - percent_yellow;
    }


    public static Mat brownFilter(Mat image){
        Mat bFilt = new Mat();

        Imgproc.cvtColor(image, bFilt, Imgproc.COLOR_RGB2HSV);
        Core.inRange(bFilt,new Scalar(90, 80, 60), new Scalar(255,255,255), bFilt);
        return bFilt;
    }


    public static Mat getLines(Mat image, double rho, double theta, int threshold) {

        Mat lines = new Mat();
        Imgproc.HoughLinesP(image, lines, rho, theta, threshold);
        return lines;
    }

    public static Mat getCircles(Mat image){

        Mat circles = new Mat();
        Mat im = image;
        Imgproc.cvtColor(image, im, Imgproc.COLOR_BGR2GRAY);
        Imgproc.HoughCircles(im, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 30, 100, 100, 30, 500);
        return circles;
    }

    /*
    static{
        if(OpenCVLoader.initDebug()){
            Log.i("MainActivity", "OpenCV Loaded");
        }
        else
        {
            Log.i("MainActivity", "OpenCV failed to load");
        }
    }
    */


}
