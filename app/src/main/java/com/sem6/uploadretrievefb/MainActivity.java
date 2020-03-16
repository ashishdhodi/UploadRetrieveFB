package com.sem6.uploadretrievefb;

import androidx.annotation.NonNull;
        import androidx.annotation.Nullable;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.appcompat.widget.Toolbar;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import android.content.Intent;
        import android.database.Cursor;
        import android.net.Uri;
        import android.os.Bundle;
        import android.provider.OpenableColumns;
        import android.util.Log;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.Spinner;
        import android.widget.Toast;

        import com.google.android.gms.tasks.OnFailureListener;
        import com.google.android.gms.tasks.OnSuccessListener;
        import com.google.android.material.textfield.TextInputLayout;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;
        import com.google.firebase.firestore.DocumentReference;
        import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
        import com.google.firebase.storage.StorageReference;
        import com.google.firebase.storage.StorageTask;
        import com.google.firebase.storage.UploadTask;

        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static final String TAG = "TAG";
    private static final int RESULT_LOAD_IMAGE = 1;
    Toolbar toolbar;
    Button selectImageButton,createSellItem;
    TextInputLayout titleField,descriptionField,priceField;

    RecyclerView imagePreviewRecycler;

    Spinner categorySpinner;
    String category;

   // String storage_path = "All_Image_Uploads/";
  //  String database_path = "All_Image_Uploads_Database";

    List<String> fileNameList,fileDoneList;
    UploadListAdapter uploadListAdapter;
    StorageReference storageReference;
    FirebaseFirestore  firebaseFirestore;
    StorageTask mUploadTask;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        selectImageButton = findViewById(R.id.sell_select_image);
        createSellItem = findViewById(R.id.sell_createSellItem);
        imagePreviewRecycler = findViewById(R.id.sell_preview_recyler);

        titleField = findViewById(R.id.text_sell_title);
        descriptionField = findViewById(R.id.text_sell_Description);
        priceField = findViewById(R.id.text_sell_price);

        storageReference = FirebaseStorage.getInstance().getReference();


        firebaseFirestore = FirebaseFirestore.getInstance();

        categorySpinner = findViewById(R.id.category_spinner);
        categorySpinner.setPrompt("Select Category");

        fileNameList = new ArrayList<>();
        fileDoneList = new ArrayList<>();
        uploadListAdapter = new UploadListAdapter(fileNameList,fileDoneList);

        imagePreviewRecycler.setLayoutManager(new LinearLayoutManager(this));
        imagePreviewRecycler.setHasFixedSize(true);
        imagePreviewRecycler.setAdapter(uploadListAdapter);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.category,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(this);

        createSellItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Store the details to the fireStore
                final Map<String,Object> user = new HashMap<>();
                user.put("title",titleField.getEditText().getText().toString());
                user.put("description",descriptionField.getEditText().getText().toString().trim());
                user.put("category",category);
                user.put("price",priceField.getEditText().getText().toString().trim());
                DocumentReference documentReference = firebaseFirestore.collection("ImageDetails").document();
                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"" +
                                "OnSuccess : Details Uploaded");
                        startActivity(new Intent(MainActivity.this,sell.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"OnFailure : " + e.toString());
                    }
                });
            }
        });

        //Selecting Image
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUploadTask != null && mUploadTask.isInProgress()){
                    Toast.makeText(MainActivity.this, "Upload In Progress", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String fileName;
        final String[] downloadUrl = new String[1];
        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK){
            if(data.getClipData() != null){

                int totalItemsSelected = data.getClipData().getItemCount();

                for(int i =0;i<totalItemsSelected;i++){

                    Uri fileUri = data.getClipData().getItemAt(i).getUri();
                    fileName = getFileName(fileUri);
                    fileNameList.add(fileName);
                    fileDoneList.add("uploading");
                    uploadListAdapter.notifyDataSetChanged();

                    final StorageReference fileToUpload = storageReference.child("Images").child(fileName);

                    final int finalI = i;
                    mUploadTask = fileToUpload.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileToUpload.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String url = String.valueOf(uri);
                                    storeLink(url);
                                }
                            });
                            // Toast.makeText(add_for_sale.this, "Uploading works", Toast.LENGTH_SHORT).show();
                            fileDoneList.remove(finalI);
                            fileDoneList.add(finalI,"done");

                            uploadListAdapter.notifyDataSetChanged();

                        }
                    });
                   // ImageUploadInfo imageUploadInfo = new ImageUploadInfo(downloadUrl[0],fileName);


                }

                Toast.makeText(this, "Upload in Progress", Toast.LENGTH_SHORT).show();
            }
            else if(data.getData() != null){
                Toast.makeText(this, "Selected Single", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void storeLink(final String url) {
        FirebaseFirestore storeUrl = FirebaseFirestore.getInstance();
        Toast.makeText(MainActivity.this, url, Toast.LENGTH_LONG).show();
       // final Map<String,Object> image = new HashMap<>();
        //image.put("imageUrl",url);
        DocumentReference documentReference = storeUrl.collection("users").document("29t0Boxm0fa8UNkomuo5xPLwkx13");

        documentReference.update("imageUrl",url.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG,"" +
                        "OnSuccess : Image Link Saved ");
               // startActivity(new Intent(Register.this,LoginActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"OnFailure : " + e.toString());
            }
        });
    }

    public String getFileName(Uri uri){
        String result = null;
        if(uri.getScheme().equals("content")){
            Cursor cursor = getContentResolver().query(uri,null,null,null,null);
            try{
                if(cursor != null && cursor.moveToFirst()){
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }finally {
                cursor.close();
            }
        }
        if (result == null){
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1){
                result = result.substring(cut + 1);
            }

        }
        return result;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        category = parent.getItemAtPosition(position).toString();
        //Toast.makeText(this, category, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

