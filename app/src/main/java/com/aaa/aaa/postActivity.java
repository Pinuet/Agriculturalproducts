package com.aaa.aaa;

import static com.aaa.aaa.Util.isStorageUrl;

import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aaa.aaa.adpater.commentListViewAdapter;
import com.aaa.aaa.listener.OnPostListener;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class postActivity extends AppCompatActivity {
    private FirebaseFirestore database;
    private FirebaseUser user;
    private commentListViewAdapter adapter;
    private RecyclerView recyclerView;
    private ImageView postImageView;
    private Date comment_time;
    private ArrayList<PostInfo> postInfo;
    private ArrayList<commentInfo> commentList;
    private String title, category, uid, postKey;
    private Date created;
    private ArrayList<String> contentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postpage);
        database = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        findViewById(R.id.commentWriteButton).setOnClickListener(onClickListener);
        postKey = (String) getIntent().getSerializableExtra("postpostKey");
        category = (String) getIntent().getSerializableExtra("postCategory");

        Toolbar tb = (Toolbar) findViewById(R.id.write_toolbar);
        setSupportActionBar(tb);//???????????? ????????? ?????????
        getSupportActionBar().setTitle(category);

        uid = (String) getIntent().getSerializableExtra("postUid");
        TextView uidTextView = findViewById(R.id.postUserTextView);

        Log.e("??????", "url:" + postKey);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        title = (String) getIntent().getSerializableExtra("postTitle");
        TextView titleTextView = findViewById(R.id.postTitleTextView);
        titleTextView.setText(title);

        created = (Date) getIntent().getSerializableExtra("postCreated");
        TextView TimeTextView = findViewById(R.id.postCreatedTextView);
        TimeTextView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(created));

        postImageView = (ImageView) findViewById(R.id.postImageView);
        database = FirebaseFirestore.getInstance();
        //FireStore?????? ????????? ?????? ????????????
        database.collection("user")
                // ??????????????? ?????? ????????? ????????????
                .whereEqualTo("uid", uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                uidTextView.setText(document.getData().get("name").toString());
                                if (document.getData().get("profile_pic").toString().equals("null")) {
                                    postImageView.setImageResource(R.drawable.default_profile);
                                } else {
                                    String url = document.getData().get("profile_pic").toString();
                                    Uri file = Uri.parse(url);
                                    Glide.with(postActivity.this).load(url).centerCrop().override(500).into(postImageView);
                                }
                            }
                        } else {

                        }
                    }
                });
        LinearLayout contentsLayout = findViewById(R.id.postcontentsLayout);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentsList = (ArrayList<String>) getIntent().getSerializableExtra("postContent");

        if (contentsLayout.getTag() == null || !contentsLayout.getTag().equals(contentsList)) {
            contentsLayout.setTag(contentsList);
            contentsLayout.removeAllViews();
            for (int i = 0; i < contentsList.size(); i++) {
                String contents = contentsList.get(i);
                if (isStorageUrl(contents)) {
                    ImageView imageView = new ImageView(this);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setAdjustViewBounds(true);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageView.setPadding(0, 0, 0, 30);
                    contentsLayout.addView(imageView);
                    Glide.with(this).load(contents).override(1000).thumbnail(0.1f).into(imageView);
                } else {
                    TextView textView = new TextView(this);
                    textView.setLayoutParams(layoutParams);
                    textView.setText(contents);
                    textView.setTextSize(18);
                    textView.setPadding(0, 0, 0, 30);
                    textView.setTextColor(Color.rgb(0, 0, 0));
                    contentsLayout.addView(textView);
                }
            }
        }

        /** ??????????????? ???(????????? ?????????) ??????**/
        commentList = new ArrayList<>();
        adapter = new commentListViewAdapter(postActivity.this, commentList);
        recyclerView = findViewById(R.id.commentRecyclerView);
        recyclerView.setHasFixedSize(true);
        adapter.setOnPostListener(onPostListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(postActivity.this));
        recyclerView.setAdapter(adapter);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.commentWriteButton:
                    commentUpload();
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.default_post_menu, menu);
        String uid = (String) getIntent().getSerializableExtra("postUid");
        TextView uidTextView = findViewById(R.id.postUserTextView);
        MenuItem postdelete = menu.findItem(R.id.postDeleteMenu);
        if (user.getUid().equals(uid)) {
            postdelete.setVisible(true);
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        commentUpdate();
    }

    OnPostListener onPostListener = new OnPostListener() {
        @Override
        public void onDelete(String id) {
            database.collection("comment").document(id)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Util.showToast(postActivity.this, "????????? ?????????????????????.");
                            commentUpdate();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }

    };

    public void scrollDown() {
        ScrollView scrollView = (ScrollView) findViewById(R.id.postScrollView);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }


    //????????? ??????, ToolBar??? ????????? ????????? select ???????????? ???????????? ??????
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                // User chose the "Settings" item, show the app settings UI...
                finish();
                break;
            case R.id.postDeleteMenu:
                AlertDialog.Builder builder = new AlertDialog.Builder(postActivity.this);
                builder.setTitle("????????? ??????");
                builder.setMessage("???????????? ?????????????????????????");
                builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    public void onClick(
                            DialogInterface dialog, int id) {
                        String post_id = (String) getIntent().getSerializableExtra("postpostKey");
                        database = FirebaseFirestore.getInstance();
                        database.collection("post").document(post_id)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        toast("???????????? ?????????????????????.");
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        toast("????????? ?????? ??????");
                                    }
                                });
                    }
                });
                builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    public void onClick(
                            DialogInterface dialog, int id) {

                    }
                });
                builder.create().show();
        }
        return true;
    }

    private void commentUpdate() {
        String postKey = (String) getIntent().getSerializableExtra("postpostKey");
        CollectionReference collectionReference = database.collection("comment");
        collectionReference
                // ??????????????? ?????? ????????? ????????????
                .whereEqualTo("post_id", postKey)
                //????????? ??????
                .orderBy("comment_time", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            commentList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                commentList.add(new commentInfo(
                                        document.getData().get("comment_id").toString(),
                                        document.getData().get("comment_uid").toString(),
                                        document.getData().get("comment_content").toString(),
                                        document.getData().get("post_id").toString(),
                                        new Date(document.getDate("comment_time").getTime())));
                            }
                            adapter.notifyDataSetChanged();
                            //??????????????? ??? ??????

                        } else {
                        }
                    }
                });
    }

    private void commentUpload() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        final String comment_content = ((EditText) findViewById(R.id.commentEditText)).getText().toString();
        String comment_uid = user.getUid();
        String post_id = (String) getIntent().getSerializableExtra("postpostKey");
        //?????? ????????????
        comment_time = Calendar.getInstance().getTime();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        final DocumentReference commentRef = firebaseFirestore.collection("comment").document();
        String comment_id = commentRef.getId();
        if (comment_content.length() > 0) {
            commentInfo commentInfo = new commentInfo(comment_id, comment_uid, comment_content, post_id, comment_time);
            commentRef.set(commentInfo)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            toast("?????? ????????? ??????");
                            ((EditText) findViewById(R.id.commentEditText)).setText(null);
                            commentUpdate();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            toast("?????? ????????? ??????");
                        }
                    });
            Log.e("??????", "url:" + post_id);
        } else {
            toast("????????? ??????????????????.");
        }
    }

    //?????????????????? ??????
    public void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

}