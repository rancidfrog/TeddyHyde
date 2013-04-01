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
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 3/27/13
 * Time: 10:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class PixAuthenticationActivity extends Activity implements View.OnClickListener {

    private static final int PICK_IMAGE = 1;
    private Context ctx;
    private Bitmap bitmap;
    private ProgressDialog pd;
    private ImageView imgView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        SharedPreferences sp = getSharedPreferences("com.EditorHyde.app", MODE_PRIVATE);
        String pixAuthToken= sp.getString("pixAuthToken", null );

        ctx = this;

        if( null == pixAuthToken ) {

            setContentView(R.layout.picture_authentication);

            Button btnFacebookLogin = (Button) findViewById(R.id.fb_login_button);
            btnFacebookLogin.setOnClickListener(this);

        }
        else {
            setContentView(R.layout.pix_grid_layout);

            GridView gridview = (GridView) findViewById(R.id.gridview);
            gridview.setAdapter(new ImageAdapter(this));

            gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Toast.makeText( ctx, "" + position, Toast.LENGTH_SHORT).show();
                }
            });

        }

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

                        pd = ProgressDialog.show(PixAuthenticationActivity.this, "Uploading",
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

    @Override
    public void onClick(View v) {
        int id = v.getId();

        startFacebookLogin();

    }

    private void startFacebookLogin( ) {
        // start Facebook Login
        Session.openActiveSession(this, true, new Session.StatusCallback() {

            // callback when session changes state
            @Override
            public void call(final Session session, SessionState state, Exception exception) {
                if (session.isOpened()) {

                    final String fbToken = session.getAccessToken();

                    Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

                        // callback after Graph API response with user object
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            if (user != null) {
                                String fbId = user.getId();

                                new FbTokenValidatorTask().execute( fbToken,  fbId);

                                Toast.makeText( ctx, "Username: " + user.getFirstName(), Toast.LENGTH_SHORT );
                            }
                        }

                    });
                }
            }

        });
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

    class FbTokenValidatorTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... args) {
            String fbToken = args[0];
            String fbId = args[1];
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            String endpoint =  getString(R.string.WebServiceUrl) + getString(R.string.FbTokenAuthEndpoint);
            HttpPost httpPost = new HttpPost( endpoint );

            String sResponse = "";
            MultipartEntity entity = new MultipartEntity(
                    HttpMultipartMode.BROWSER_COMPATIBLE);
            try {

                entity.addPart("fbToken",   new StringBody("fbToken" ) );
                entity.addPart("fbId", new StringBody( "fbId" ) );
                httpPost.setEntity(entity);
                HttpResponse response = null;
                response = httpClient.execute(httpPost,
                        localContext);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                response.getEntity().getContent(), "UTF-8"));
                sResponse= reader.readLine();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return sResponse;

        }

        @Override
        protected void onPostExecute(String sResponse) {
            try {
                if (pd.isShowing())
                    pd.dismiss();

                if (sResponse != null) {
                    JSONObject JResponse = new JSONObject(sResponse);
                    int success = JResponse.getInt("success");
                    if (success == 0) {
                        Toast.makeText(getApplicationContext(), "Unable to create auth token, please try again later",
                                Toast.LENGTH_LONG).show();
                    } else {
                        String token = JResponse.getString("pixAuthToken");
                        SharedPreferences sp = getSharedPreferences("com.EditorHyde.app", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString( "pixAuthToken", token );
                    }

                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.exception_message),
                        Toast.LENGTH_LONG).show();
                Log.e(e.getClass().getName(), e.getMessage(), e);
            }
        }
    }

    class ImageUploadTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... unused) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                HttpPost httpPost = new HttpPost( getString(R.string.WebServiceUrl) );

                MultipartEntity entity = new MultipartEntity(
                        HttpMultipartMode.BROWSER_COMPATIBLE);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                byte[] data = bos.toByteArray();
                entity.addPart("photoId", new StringBody(getIntent()
                        .getStringExtra("photoId")));
                entity.addPart("returnformat", new StringBody("json"));
                entity.addPart("uploaded", new ByteArrayBody(data,
                        "myImage.jpg"));
                httpPost.setEntity(entity);
                HttpResponse response = httpClient.execute(httpPost,
                        localContext);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                response.getEntity().getContent(), "UTF-8"));

                String sResponse = reader.readLine();
                return sResponse;
            } catch (Exception e) {
                if (pd.isShowing())
                    pd.dismiss();
                Toast.makeText(getApplicationContext(),
                        getString(R.string.exception_message),
                        Toast.LENGTH_LONG).show();
                Log.e(e.getClass().getName(), e.getMessage(), e);
                return null;
            }

            // (null);
        }

        @Override
        protected void onProgressUpdate(Void... unsued) {

        }

        @Override
        protected void onPostExecute(String sResponse) {
            try {
                if (pd.isShowing())
                    pd.dismiss();

                if (sResponse != null) {
                    JSONObject JResponse = new JSONObject(sResponse);
                    int success = JResponse.getInt("SUCCESS");
                    String message = JResponse.getString("MESSAGE");
                    if (success == 0) {
                        Toast.makeText(getApplicationContext(), message,
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Photo uploaded successfully",
                                Toast.LENGTH_SHORT).show();

                    }
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.exception_message),
                        Toast.LENGTH_LONG).show();
                Log.e(e.getClass().getName(), e.getMessage(), e);
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

        imgView.setImageBitmap(bitmap);

    }


}