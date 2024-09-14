package com.example.vibesyncapp;

import static android.app.Activity.RESULT_OK;

import static java.util.Currency.getInstance;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.Manifest;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.security.Key;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

   FirebaseAuth firebaseAuth;
   FirebaseUser user;
   FirebaseDatabase database;
   DatabaseReference dbref;
   StorageReference storageReference;
   String storagepath="User_Profile_Cover_Imgs/";

   ImageView avtariv,coveriv;
   TextView nametv,emailtv,phonetv;
   FloatingActionButton fab;
   ProgressDialog pd;
    Uri image_uri;
    String profileOrCoverPhoto;



    private static final int Camera_request_code=100;
    private static final int Storage_request_code=200;
    private static final int ImagepickGallery_request_code=300;
    private static final int ImagepickCamera_request_code=400;

    String camerapermissions[];
    String storagepermissions[];


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_profile, container, false);
        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();
        database=FirebaseDatabase.getInstance();
        dbref=database.getReference("Users");
        storageReference= FirebaseStorage.getInstance().getReference();

        avtariv=view.findViewById(R.id.avatarIV);
        nametv=view.findViewById(R.id.nametv);
        emailtv=view.findViewById(R.id.emailtv);
        phonetv=view.findViewById(R.id.phonetv);
        coveriv=view.findViewById(R.id.coveriv);
        fab=view.findViewById(R.id.fab);
        pd=new ProgressDialog(getActivity());

        camerapermissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagepermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        Query query=dbref.orderByChild("email").equalTo(user.getEmail());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds:snapshot.getChildren())
                {
                    String name=""+ds.child("name").getValue();
                    String email=""+ds.child("email").getValue();
                    String phone=""+ds.child("phone").getValue();
                    String image=""+ds.child("image").getValue();
                    String cover=""+ds.child("cover").getValue();

                    nametv.setText(name);
                    emailtv.setText(email);
                    phonetv.setText(phone);

                    try {
                        Picasso.get().load(image).into(avtariv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.profile3).into(avtariv);
                    }

                    try {
                        Picasso.get().load(cover).into(coveriv);
                    }catch (Exception e){

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String options[]={"Edit profile picture","Edit cover photo","Edit name","Edit phone"};

                AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                builder.setTitle("Choose Action");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0)
                        {
                            pd.setMessage("Updating Profile Picture");
                            profileOrCoverPhoto="image";
                            showImagePicDialog();
                        } else if (which==1) {

                            pd.setMessage("Updating Cover Photo");
                            profileOrCoverPhoto="cover";
                            showImagePicDialog();

                        } else if (which==2) {
                            pd.setMessage("Updating Name");
                            showNamePhoneUpdateDialog("name");

                        } else if (which==3) {

                            pd.setMessage("Updating Phone No");
                            showNamePhoneUpdateDialog("phone");

                        }
                    }
                });

                builder.create().show();

            }
        });




        return view;
    }



        private void showNamePhoneUpdateDialog(String key) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Update " + key);

            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setPadding(10, 10, 10, 10);

            EditText editText = new EditText(getActivity());
            editText.setHint("Enter " + key);

            // If updating phone number, set the input type to number
            if (key.equals("phone")) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }

            linearLayout.addView(editText);
            builder.setView(linearLayout);

            builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String value = editText.getText().toString().trim();

                    if (!TextUtils.isEmpty(value)) {
                        pd.show();
                        HashMap<String, Object> result = new HashMap<>();
                        result.put(key, value);

                        dbref.child(user.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                pd.dismiss();
                                Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                    } else {
                        Toast.makeText(getActivity(), "Please enter " + key, Toast.LENGTH_LONG).show();
                    }
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        }



    public void showImagePicDialog()
    {
        String options[]={"Camera","Gallery"};

        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image from");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0)
                {
                    if(!checkCameraPermission())
                    {
                           requestCameraPermission();
                    }else {
                        pickFromCamera();
                    }


                } else if (which==1) {

                   if(!checkStoragePermission())
                   {
                       requestStoragePermission();
                   }else {
                       pickFromGallery();
                   }

                }
            }
        });

        builder.create().show();
    }

    boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ permissions
            boolean readImages = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
            return readImages;
        } else {
            // For Android 12 and below
            return ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }


    void requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Request for Android 13+
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, Storage_request_code);
        } else {
            // For Android 12 and below
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Storage_request_code);
        }
    }


    boolean checkCameraPermission() {
        boolean camera = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            boolean readImages = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
            return camera && readImages;
        } else {
            return camera && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }


    void requestCameraPermission()
    {
        requestPermissions(camerapermissions,Camera_request_code);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode)
        {
            case Camera_request_code:
                if(grantResults.length>0)
                {
                    boolean cameraccepted=grantResults[0]== PackageManager.PERMISSION_GRANTED;
                    boolean writestorageaccepted=grantResults[1]== PackageManager.PERMISSION_GRANTED;

                    if(cameraccepted && writestorageaccepted)
                    {
                        pickFromCamera();
                    }

                    else{
                        Toast.makeText(getActivity(),"Please Enable Camera and Storage Permission",Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case Storage_request_code:
                if(grantResults.length>0)
                {

                    boolean writestorageaccepted=grantResults[0]== PackageManager.PERMISSION_GRANTED;

                    if( writestorageaccepted)
                    {
                        pickFromGallery();
                    }

                    else{
                        Toast.makeText(getActivity(),"Please Enable Storage Permission",Toast.LENGTH_LONG).show();
                    }
                }
                break;


        }
    }

    private void pickFromCamera() {
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"temp pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"temp Description");

        image_uri=getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,ImagepickCamera_request_code);
    }

    private void pickFromGallery() {

        Intent galleryIntent=new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,ImagepickGallery_request_code);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode==RESULT_OK)
        {

            if(requestCode==ImagepickGallery_request_code)
            {
              image_uri=data.getData();
              uploadProfileCoverphoto(image_uri);
            }

            if(requestCode==ImagepickCamera_request_code)
            {
                uploadProfileCoverphoto(image_uri);
            }



        }


    }

    private void uploadProfileCoverphoto(Uri uri) {

        pd.show();
        String filepathandName=storagepath+""+profileOrCoverPhoto+" "+user.getUid();
        StorageReference storageReference2=storageReference.child(filepathandName);
        storageReference2.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();

                while (!uriTask.isSuccessful());
                Uri downloaduri=uriTask.getResult();

                if (uriTask.isSuccessful())
                {
                    HashMap<String,Object> results=new HashMap<>();
                    results.put(profileOrCoverPhoto,downloaduri.toString());

                    dbref.child(user.getUid()).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            pd.dismiss();
                            Toast.makeText(getActivity(),"Image Updated...",Toast.LENGTH_LONG).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(),"Error updating image.",Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else {
                    pd.dismiss();
                    Toast.makeText(getActivity(),"Some error occured",Toast.LENGTH_LONG).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            pd.dismiss();
            Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();

            }
        });

    }
}