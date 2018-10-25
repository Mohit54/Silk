package com.android.silksoft;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.EM;

public class MainActivity extends Activity {
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_FILE = 3;
    private static final String TAG = "SilkSoft::Activity";
    private String countString;
    private Uri mImageCaptureUri;
    private ImageView mImageView;
    private BaseLoaderCallback mOpenCVCallBack;

    /* renamed from: com.android.silksoft.MainActivity.2 */
    class C00212 implements OnClickListener {
        C00212() {
        }

        public void onClick(DialogInterface dialog, int item) {
            Intent intent;
            if (item == 0) {
                intent = new Intent("android.media.action.IMAGE_CAPTURE");
                MainActivity.this.mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));
                System.out.print(MainActivity.this.mImageCaptureUri);
                intent.putExtra("output", MainActivity.this.mImageCaptureUri);
                try {
                    intent.putExtra("return-data", true);
                    MainActivity.this.startActivityForResult(intent, MainActivity.PICK_FROM_CAMERA);
                    return;
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    return;
                }
            }
            intent = new Intent();
            intent.setType("image/*");
            intent.setAction("android.intent.action.GET_CONTENT");
            MainActivity.this.startActivityForResult(Intent.createChooser(intent, "Complete action using"), MainActivity.PICK_FROM_FILE);
        }
    }

    /* renamed from: com.android.silksoft.MainActivity.3 */
    class C00223 implements View.OnClickListener {
        private final /* synthetic */ AlertDialog val$dialog;

        C00223(AlertDialog alertDialog) {
            this.val$dialog = alertDialog;
        }

        public void onClick(View v) {
            this.val$dialog.show();
        }
    }

    /* renamed from: com.android.silksoft.MainActivity.1 */
    class C00401 extends BaseLoaderCallback {
        C00401(Activity $anonymous0) {
            super($anonymous0);
        }

        public void onManagerConnected(int status) {
            switch (status) {
                case EM.START_AUTO_STEP /*0*/:
                default:
                    super.onManagerConnected(status);
            }
        }
    }

    public MainActivity() {
        this.countString = "ImageTitle23.jpg";
        this.mOpenCVCallBack = new C00401(this);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0023R.layout.activity_main);
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, this.mOpenCVCallBack)) {
            Log.e(TAG, "Cannot connect to OpenCV Manager");
        }
        String[] items = new String[CROP_FROM_CAMERA];
        items[0] = "Take from camera";
        items[PICK_FROM_CAMERA] = "Select from gallery";
        ArrayAdapter<String> adapter = new ArrayAdapter(this, 17367057, items);
        Builder builder = new Builder(this);
        builder.setTitle("Select Image");
        builder.setAdapter(adapter, new C00212());
        AlertDialog dialog = builder.create();
        Button button = (Button) findViewById(C0023R.id.btn_crop);
        this.mImageView = (ImageView) findViewById(C0023R.id.iv_photo);
        button.setOnClickListener(new C00223(dialog));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(C0023R.menu.activity_main, menu);
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            switch (requestCode) {
                case PICK_FROM_CAMERA /*1*/:
                    doConvert();
                case CROP_FROM_CAMERA /*2*/:
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        this.mImageView.setImageBitmap((Bitmap) extras.getParcelable("data"));
                    }
                    File f = new File(this.mImageCaptureUri.getPath());
                    if (f.exists()) {
                        f.delete();
                    }
                case PICK_FROM_FILE /*3*/:
                    this.mImageCaptureUri = data.getData();
                    doConvert();
                default:
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = new String[PICK_FROM_CAMERA];
        proj[0] = "_data";
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow("_data");
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void doConvert() {
        try {
            Bitmap bitmap = getBinary(BitmapFactory.decodeStream(getContentResolver().openInputStream(this.mImageCaptureUri)));
            this.mImageView = (ImageView) findViewById(C0023R.id.iv_photo);
            if (this.mImageView != null) {
                this.mImageView.setImageBitmap(bitmap);
            }
            File oldFileName = new File(getRealPathFromURI(this.mImageCaptureUri));
            File newFileName = new File(Environment.getExternalStorageDirectory(), this.countString);
            if (oldFileName.renameTo(newFileName)) {
                Log.v("SilkSoft", "file " + newFileName + " was  seccessfully: ");
                Toast.makeText(this, "saved: " + newFileName, PICK_FROM_CAMERA).show();
                return;
            }
            Log.v("SilkSoft", "file " + newFileName + " was  Failure: ");
            Toast.makeText(this, "Could not save try again", PICK_FROM_CAMERA).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap getBinary(Bitmap b) {
        Bitmap img = null;
        try {
            Mat src = new Mat();
            Utils.bitmapToMat(b, src);
            if (src.empty()) {
                return null;
            }
            Mat hsv = new Mat();
            Mat gray = new Mat();
            Mat top = new Mat();
            Mat pimg = new Mat();
            Mat img1 = new Mat();
            Mat img2 = new Mat();
            Imgproc.cvtColor(src, img2, 6);
            Imgproc.GaussianBlur(img2, img1, new Size(5.0d, 5.0d), 0.0d);
            img1.convertTo(hsv, -1, 0.62d, 90.0d);
            Imgproc.erode(hsv, pimg, Imgproc.getStructuringElement(CROP_FROM_CAMERA, new Size(1.0d, 1.0d)));
            Imgproc.Canny(pimg, gray, 40.0d, 120.0d, PICK_FROM_FILE, false);
            Imgproc.GaussianBlur(gray, top, new Size(1.0d, 1.0d), 0.0d);
            List<MatOfPoint> contours = new ArrayList();
            Imgproc.findContours(top, contours, new Mat(), PICK_FROM_CAMERA, CROP_FROM_CAMERA);
            Imgproc.drawContours(top, contours, -1, new Scalar(255.0d, 0.0d, 0.0d), CROP_FROM_CAMERA);
            Imgproc.drawContours(top, contours, -1, new Scalar(255.0d, 0.0d, 0.0d), CROP_FROM_CAMERA);
            this.countString = "silk_" + contours.size() + ".jpg";
            System.out.println(this.countString);
            ((Button) findViewById(C0023R.id.btn_crop)).setText(this.countString);
            img = Bitmap.createBitmap(top.cols(), top.rows(), Config.ARGB_8888);
            Utils.matToBitmap(top, img);
            return img;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
