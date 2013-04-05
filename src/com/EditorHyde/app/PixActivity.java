package com.EditorHyde.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 3/27/13
 * Time: 10:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class PixActivity extends Activity {

    private static final int PICK_IMAGE = 1;
    public static final int THUMBNAIL_WIDTH = 64;
    public static final int RESIZED_WIDTH = 200;
    private Context ctx;
    private Bitmap bitmap;
    private ProgressDialog pd;
    private ImageView imgView;

    String theRepo;
    String authToken;
    String theLogin;
    int theTransformIndex;
    int theSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ctx = this;

        Bundle extras = getIntent().getExtras();

        theRepo = extras.getString("repo");
        theLogin = extras.getString("login");
        theTransformIndex = extras.getInt( "transformIndex" );
        theSize = extras.getInt( "size" );

        String [] images;
        images = extras.getStringArray( "images" );
        SharedPreferences sp = this.getSharedPreferences( MainActivity.APP_ID, MODE_PRIVATE);
        authToken = sp.getString("authToken", null);

        setContentView(R.layout.pix_grid_layout);

        final GridView gridview = (GridView) findViewById(R.id.gridview);
        ImageAdapter ia = new ImageAdapter(PixActivity.this);
        ia.setImages( RemoteFileCache.getImages() );
        gridview.setAdapter( ia );

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // send back the image
                RemoteImage ri =  (RemoteImage)gridview.getItemAtPosition(position);
                Bundle bundle = new Bundle();
                bundle.putString("imageUrl", ri.getUrl() );
                bundle.putInt( "transformIndex", theTransformIndex );
                bundle.putInt( "size", theSize );
                Intent intent = new Intent();
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        Button btn = (Button)findViewById(R.id.uploadImage);
        btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();

            }
        });


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImageUri = data.getData();
                    String filePath = null;

                    try {
                        // OI FILE Manager
                        String filemanagerstring = selectedImageUri.getPath();

                        // MEDIA GALLERY
                        String selectedImagePath = getPath(selectedImageUri);

                        if (selectedImagePath != null) {
                            filePath = selectedImagePath;
                        } else if (filemanagerstring != null) {
                            filePath = filemanagerstring;
                        } else {
                            Toast.makeText(getApplicationContext(), "Unknown path",
                                    Toast.LENGTH_LONG).show();
                            Log.e("Bitmap", "Unknown path");
                        }

                        if (filePath != null) {
                            decodeFile(filePath);
                        } else {
                            bitmap = null;
                        }

                        pd = ProgressDialog.show(PixActivity.this, "Uploading",
                                "Please wait...", true);
                        new ImageUploadTask().execute();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Internal error",
                                Toast.LENGTH_LONG).show();
                        Log.e(e.getClass().getName(), e.getMessage(), e);
                    }
                }
                break;
            default:
        }
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }



    private void captureImage() {
        try {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent, "Select Picture"),
                    PICK_IMAGE);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.exception_message),
                    Toast.LENGTH_LONG).show();
            Log.e(e.getClass().getName(), e.getMessage(), e);
        }

    }

    class ImageUploadTask extends AsyncTask<Void, Integer, Boolean> {
        private static final int UPLOADING_FULL_SIZE = 1;
        private static final int GENERATING_RESIZED = 2;
        private static final int UPLOADING_RESIZED = 3;
        private static final int GENERATING_THUMBNAIL = 4;
        private static final int UPLOADING_THUMBNAIL = 5;

        RemoteImage newImage;

        @Override
        protected Boolean doInBackground(Void... unused) {

            Boolean rv = true;
            String newSha;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            byte[] data = bos.toByteArray();

            // Convert to base64
            String base64ed = Base64.encodeToString(data, Base64.DEFAULT);

            SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd-hh-mm-ss" );
            String prefix = sdf.format( new Date() );

            String filename = "assets/images/" + prefix + "-image.png";

            publishProgress( UPLOADING_FULL_SIZE );
            ThGitClient.SaveFile(authToken, theRepo, theLogin, base64ed, filename, "Image added using Teddy Hyde on Android");

            // add thumbnail and resized

            publishProgress( GENERATING_THUMBNAIL );
            int thumbnailWidth = THUMBNAIL_WIDTH;
            int thumbnailHeight = (int)( (float)bitmap.getHeight() / (float)bitmap.getWidth() * THUMBNAIL_WIDTH );
            Bitmap thumb = Bitmap.createScaledBitmap( bitmap, thumbnailWidth, thumbnailHeight, false);

            bos = new ByteArrayOutputStream();
            thumb.compress(Bitmap.CompressFormat.PNG, 100, bos);
            data = bos.toByteArray();

            // Convert to base64
            base64ed = Base64.encodeToString(data, Base64.DEFAULT);

            String thumbFilename = "assets/images/" + prefix + "-image-thumb.png";

            publishProgress( UPLOADING_THUMBNAIL );
            ThGitClient.SaveFile(authToken, theRepo, theLogin, base64ed, thumbFilename, "Image thumbnail added using Teddy Hyde on Android");

            publishProgress( GENERATING_RESIZED );
            int resizedWidth = RESIZED_WIDTH;
            int resizedHeight = (int)( (float)bitmap.getHeight() / (float)bitmap.getWidth() * 200 );
            Bitmap resized = Bitmap.createScaledBitmap( bitmap, resizedWidth, resizedHeight, false);

            bos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.PNG, 100, bos);
            data = bos.toByteArray();

            // Convert to base64
            base64ed = Base64.encodeToString(data, Base64.DEFAULT);

            String resizedFilename = "assets/images/" + prefix + "-image-resized.png";

            publishProgress( UPLOADING_RESIZED );
            ThGitClient.SaveFile(authToken, theRepo, theLogin, base64ed, resizedFilename, "Resized image added using Teddy Hyde on Android");

            // Save the image to add to our list
            newImage = new RemoteImage( thumbFilename, thumb );

            return rv;
        }

        @Override
        protected void onProgressUpdate(Integer... progresses) {
            int progress = progresses[0];

            if( UPLOADING_FULL_SIZE == progress ) {
                pd.setMessage( "Generating resized image...");
            }
            else if( GENERATING_RESIZED == progress ) {
                pd.setMessage( "Generating resized...");
            }
            else if( UPLOADING_RESIZED == progress ) {
                pd.setMessage( "Uploading resized...");
            }
            else if( GENERATING_THUMBNAIL == progress ) {
                pd.setMessage( "Generating thumbnail...");
            }
            else if( UPLOADING_THUMBNAIL == progress ) {
                pd.setMessage( "Uploading thumbnail...");
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (pd.isShowing())
                pd.dismiss();

            if( !result ) {
                Toast.makeText( PixActivity.this, "Unable to upload image, please try again later", Toast.LENGTH_LONG );
            }

            // If we have a size parameter, then we need to allow them to choose. If not, we just needed to upload the image
            if( 0 == theSize ) {
                finish();
            }
            else {
                // Add it to the images
                RemoteFileCache.addImage( newImage );
            }

        }
    }

    public void decodeFile(String filePath) {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 1024;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        bitmap = BitmapFactory.decodeFile(filePath, o2);


    }


}
