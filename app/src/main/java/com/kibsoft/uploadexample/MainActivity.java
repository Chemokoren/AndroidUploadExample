package com.kibsoft.uploadexample;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonChoose, buttonUpload, buttonDownload;
    private ImageView imageView;
    private EditText editText;

    private static final int STORAGE_PERMISSION_CODE=2342;
    private static final int PICK_IMAGE_REQUEST =23;
    private Uri filePath;
    private Bitmap bitmap;
    private static final String TAG = "SignupActivity";
    private RequestQueue requestQueue;

//    private static final String UPLOAD_URL="http://192.168.0.32/UploadExample/upload.php";
private static final String UPLOAD_URL="http://10.0.2.2:8000/api/user/changephoto";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestStoragePermission();

        buttonUpload=(Button)findViewById(R.id.buttonUpload);
        buttonChoose=(Button)findViewById(R.id.buttonChoose);
        buttonDownload=(Button)findViewById(R.id.buttonDownload);

        imageView=(ImageView)findViewById(R.id.image);
        editText =(EditText)findViewById(R.id.editTextName);

        buttonChoose.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
        buttonDownload.setOnClickListener(this);
        new DownloadImage().execute();
    }
    private void requestStoragePermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Permission not granted",Toast.LENGTH_LONG).show();
            }
        }
    }
    private void showFIleChooser(){
        Intent intent =new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode ==PICK_IMAGE_REQUEST && resultCode ==RESULT_OK && data !=null && data.getData() !=null){
                filePath=data.getData();
                try{
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);
                    imageView.setImageBitmap(bitmap);
                }catch (IOException e){

                }
        }
    }
    private String getPath(Uri uri){
        Cursor cursor =getContentResolver().query(uri,null,null,null,null);
        cursor.moveToFirst();
        String document_id =cursor.getString(0);
        document_id =document_id.substring(document_id.lastIndexOf(":")+1);
        cursor.close();

        cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID +" = ? ",new String[]{document_id},null
        );
        cursor.moveToFirst();
        String path =cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();
        return path;

    }
    private void uploadImage(){
        String name =editText.getText().toString().trim();
        String path =getPath(filePath);
        //filePath.substring(filePath.lastIndexOf(".") + 1); without dot
        try{
            String uploadid = UUID.randomUUID().toString();
            new MultipartUploadRequest(this,uploadid,UPLOAD_URL)
                    .addFileToUpload(path,"image")
                    .addParameter("name", name)
//                    .addParameter("extension", path.substring(path.lastIndexOf(".")))
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload();
        }catch (Exception e){

        }
    }
    private class DownloadImage extends AsyncTask<Void,Void,Bitmap>{
//        String name;
//        public DownloadImage(String name) {
//            this.name =name;
//        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            String SERVER_ADDRESS="http://192.168.0.32:8000/public/i/users/mimi.jpg";
            String url=SERVER_ADDRESS;
            try{
                URLConnection connection =new URL(url).openConnection();
                connection.setConnectTimeout(1000*30);
                connection.setReadTimeout(1000 *30);

                return BitmapFactory.decodeStream((InputStream)connection.getContent(), null, null);
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }


        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(bitmap !=null){
                imageView.setImageBitmap(bitmap);
            }
        }
    }
    public void returnLastAcademicSession(){
        if (requestQueue != null) {
            requestQueue = Volley.newRequestQueue(this);
            String endpoint = "";

            Log.e("endpoint", endpoint);
            StringRequest strReq = new StringRequest(Request.Method.GET,
                    endpoint, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.e("my response::::", response);
                    try {
                        JSONObject jsObject = new JSONObject(response);

                        String mes = jsObject.getString("response");
                        // Getting JSON Array node
                        JSONArray contacts = new JSONArray(mes);

                        JSONObject c = contacts.getJSONObject(0);
                        String _id = c.getString("id");
                        String _year = c.getString("year");
                        String _term = c.getString("term");
                        String _opening_date=c.getString("opening_date");
                        String _closing_date=c.getString("closing_date");




                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                    Log.e("Error it:", ""+error);
                    Toast.makeText(getApplicationContext(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            int socketTimeout = 0;
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

            strReq.setRetryPolicy(policy);

            //Adding request to request queue
            requestQueue.add(strReq);
        }
    }

    @Override
    public void onClick(View view) {
        if(view ==buttonChoose){
            showFIleChooser();
        }
        if(view ==buttonUpload){
            uploadImage();
        }

        if(view ==buttonDownload){
            new DownloadImage().execute();
        }

    }


}
