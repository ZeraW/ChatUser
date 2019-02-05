package com.teleone.chatuser.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.teleone.chatuser.MainActivity;
import com.teleone.chatuser.R;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PICK_IMAGE = 1;
    private Button backToLogin,doSignUp;
    private EditText eT_signUpName,eT_signUpPasswrod,eT_signUpEmail;
    private CircleImageView iV_signUpImage;
    private Uri mImageUri;
    private StorageReference mStorge;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFireStore;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Init();
        FirebaseMessaging.getInstance().subscribeToTopic("weather");

    }

    private void Init() {
        //firebase related stuff
        mStorge = FirebaseStorage.getInstance().getReference().child("images");
        mAuth = FirebaseAuth.getInstance();
        mFireStore = FirebaseFirestore.getInstance();

        mImageUri = null;
        backToLogin = findViewById(R.id.SignUp_backToLogin);
        progressBar = findViewById(R.id.progressBar2);
        doSignUp = findViewById(R.id.SignUp_doSignUp);
        eT_signUpName = findViewById(R.id.SignUp_Name);
        eT_signUpEmail = findViewById(R.id.SignUp_Email);
        eT_signUpPasswrod = findViewById(R.id.SignUp_Password);
        iV_signUpImage = findViewById(R.id.SignUp_profile_image);
        backToLogin.setOnClickListener(this);
        iV_signUpImage.setOnClickListener(this);
        doSignUp.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Intent intent;
        switch (id) {
            case R.id.SignUp_backToLogin:
                finish();
                break;
            case R.id.SignUp_profile_image:
                intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE);
                break;
            case R.id.SignUp_doSignUp:
                SignUp();
                break;

        }
    }

    private void SignUp() {
        final String name = eT_signUpName.getText().toString();
        String email = eT_signUpEmail.getText().toString();
        String password = eT_signUpPasswrod.getText().toString();


        //check email validations
        String regEx = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}\\b";
        Pattern p = Pattern.compile(regEx);
        Matcher valitMail = p.matcher(email);

        if (name.isEmpty()){
            eT_signUpName.setError("Required Field");
            eT_signUpName.requestFocus();
        }else if (!valitMail.find()) {
            eT_signUpEmail.setError("Email Incorrect");
            eT_signUpEmail.requestFocus();
        }else if (password.isEmpty()){
            eT_signUpPasswrod.setError("Required Field");
            eT_signUpPasswrod.requestFocus();
        }else if (password.length()<6){
            eT_signUpPasswrod.setError("password must be at least 6 characters");
            eT_signUpPasswrod.requestFocus();
        }else if (mImageUri==null){
            Toast.makeText(this, "Please Select Profile Picture", Toast.LENGTH_SHORT).show();
        }else {
            progressBar.setVisibility(View.VISIBLE);

            if (!TextUtils.isEmpty(name)&&!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)){
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //get the user id
                            final String user_Id = mAuth.getCurrentUser().getUid();

                            //storage ref for image
                            final StorageReference user_profile = mStorge.child(user_Id+".jpg");

                            //////////////////////////////

                            UploadTask uploadTask = user_profile.putFile(mImageUri);

                            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }
                                    // Continue with the task to get the download URL
                                    return user_profile.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Uri downloadUri = task.getResult();
                                        Map<String,Object> userMap = new HashMap<>();
                                        //get the uploaded img url
                                        if (downloadUri != null) {
                                            //put the data into map
                                            String imgURL = downloadUri.toString();
                                            userMap.put("name",name);
                                            userMap.put("image",imgURL);
                                            userMap.put("status","off");
                                        }
                                        //here where are we going to store all of the users
                                        mFireStore.collection("Users").document(user_Id).set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressBar.setVisibility(View.GONE);
                                                SendUserToMainActivity();

                                            }
                                        });

                                    } else {
                                        Toast.makeText(RegisterActivity.this, "ErrorIMG"+task.getException(), Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });

                        }else {
                            Toast.makeText(RegisterActivity.this, "Error"+task.getException(), Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);

                        }

                    }
                });
            }
        }
    }

    private void SendUserToMainActivity() {
        Intent intent = new Intent(this,ChatActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            mImageUri = data.getData();
            iV_signUpImage.setImageURI(mImageUri);
        }
    }


}
