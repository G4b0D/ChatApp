package com.example.chat.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chat.R;
import com.example.chat.common.nodeNames;
import com.example.chat.login.LoginActivity;
import com.example.chat.password.ChangePasswordActivity;
import com.example.chat.signup.SignUpActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    private TextInputEditText etEmail,etName;
    private String email,password;
    private ImageView ivProfile;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference fileStorage;
    private Uri localFileUri, serverFileUri;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        etEmail = findViewById(R.id.etEmail);
        firebaseAuth = FirebaseAuth.getInstance();
        etName = findViewById(R.id.etName);
        ivProfile = findViewById(R.id.ivProfile);
        fileStorage = FirebaseStorage.getInstance().getReference();
        firebaseUser =   firebaseAuth.getCurrentUser();

        if (firebaseUser != null)
        {
            etName.setText(firebaseUser.getDisplayName());
            etEmail.setText(firebaseUser.getEmail());
            serverFileUri = firebaseUser.getPhotoUrl();

            if (serverFileUri != null)
            {
                Glide.with(this)
                        .load(serverFileUri)
                        .placeholder(R.drawable.default_profile_picture)
                        .error(R.drawable.default_profile_picture)
                        .into(ivProfile);
            }
        }
    }



    public void btnSaveClick(View view)
    {
        if(etName.getText().toString().trim().equals(""))
        {
            etName.setError(getString(R.string.enter_username));
        }
        else
        {
            if(localFileUri != null) {
                updateNameAndPP();
            }
            else {
                updateOnlyName();
            }
        }
    }


public void btnLogoutClick(View view){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        startActivity(new Intent(ProfileActivity.this,LoginActivity.class));
        finish();


}

    public void changeImage(View view)
    {
        if(serverFileUri == null){
            pickImage();
        }
        else
        {
            PopupMenu popupMenu = new PopupMenu(this,view);
            popupMenu.getMenuInflater().inflate(R.menu.menu_picture,popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();

                    if(id == R.id.mnuChangePicture)
                    {
                        pickImage();
                    }
                    else if (id == R.id.mnuRemovePic)
                    {
                        removePhoto();
                    }

                    return false;
                }
            });
            popupMenu.show();
        }
    }

    private void pickImage(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,101);
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},102);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101)
        {
            if(resultCode == RESULT_OK)
            {
                localFileUri = data.getData();
                ivProfile.setImageURI(localFileUri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 102)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,101);
            }
            else
            {
                Toast.makeText(this, R.string.acces_to_gallery_required,Toast.LENGTH_SHORT).show();
            }
        }
    }





    private void removePhoto()
    {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(etName.getText().toString().trim())
                .setPhotoUri(null)
                .build();
        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    String userID = firebaseUser.getUid();
                    databaseReference = FirebaseDatabase.getInstance().getReference().child(nodeNames.USERS);

                    HashMap<String,String> hashMap = new HashMap<>();
                    hashMap.put(nodeNames.NAME, etName.getText().toString().trim());
                    hashMap.put(nodeNames.PHOTO, "");

                    databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ProfileActivity.this, R.string.photo_removed,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(ProfileActivity.this,getString(R.string.failed_update_profile,task.getException()),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    private void updateNameAndPP(){
        String strFileName = firebaseUser.getUid() + ".jpg";

        final StorageReference fileRef = fileStorage.child("images/"+strFileName);

        fileRef.putFile(localFileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            serverFileUri = uri;

                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(etName.getText().toString().trim())
                                    .setPhotoUri(serverFileUri)
                                    .build();
                            firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        String userID = firebaseUser.getUid();
                                        databaseReference = FirebaseDatabase.getInstance().getReference().child(nodeNames.USERS);

                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put(nodeNames.NAME, etName.getText().toString().trim());
                                        hashMap.put(nodeNames.PHOTO, serverFileUri.getPath());

                                        databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    finish();
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        Toast.makeText(ProfileActivity.this,getString(R.string.failed_update_profile,task.getException()),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }


    private void updateOnlyName(){
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(etName.getText().toString().trim())
                .build();
        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    String userID = firebaseUser.getUid();
                    databaseReference = FirebaseDatabase.getInstance().getReference().child(nodeNames.USERS);

                    HashMap<String,String> hashMap = new HashMap<>();
                    hashMap.put(nodeNames.NAME, etName.getText().toString().trim());
                    databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                finish();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(ProfileActivity.this,getString(R.string.failed_update_profile,task.getException()),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void btnChangePasswordClick(View view)
    {
        startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
    }
}