package com.aaa.aaa;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;

public class mypageActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    Button logout, profile_pic;
    private View view;
    private FirebaseStorage storage;
    private static final int REQUEST_CODE = 0;
    private ImageView imageView;

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);
        getSupportActionBar().setTitle("회원 정보");
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        logout = findViewById(R.id.logoutButton);
        storage = FirebaseStorage.getInstance();
        logout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            toast("로그아웃에 성공하였습니다.");
        });

        imageView = findViewById(R.id.profileimageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_CODE);

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {

                    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
                    Uri file = data.getData();
                    StorageReference storageRef = storage.getReference();
                    StorageReference riversRef = storageRef.child("profilephoto/"+user.getUid()+"/profile.jpg");
                    UploadTask uploadTask = riversRef.putFile(file);
                    InputStream in = getContentResolver().openInputStream(data.getData());

                    Bitmap img = BitmapFactory.decodeStream(in);

                    in.close();

                    imageView.setImageBitmap(img);



                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                           toast("업로드 실패");
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                           toast("업로드 성공");
                        }
                    });
                } catch (Exception e) {

                }
            }
        }else{

        }
    }

    public void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }



}
