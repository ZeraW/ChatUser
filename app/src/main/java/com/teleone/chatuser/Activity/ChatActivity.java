package com.teleone.chatuser.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;
import com.teleone.chatuser.Adapter.ChatAdapter;
import com.teleone.chatuser.Models.ChatModel;
import com.teleone.chatuser.PushNotifications.MobileToMobileMSG;
import com.teleone.chatuser.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends BaseActivity {
    private EditText eT_Message;
    private ImageButton btn_Send;
    private String myID, hisID, hisName, hisImg;
    private FirebaseFirestore mFireStore;
    ProgressBar progressBar;
    private RecyclerView mRecyclerView;
    private ChatAdapter mAdapter;
    private List<ChatModel> mList;
    private SwipeRefreshLayout mRefreshLayout;
    LinearLayoutManager linearLayoutManager;
    private CircleImageView hisProfileImage;
    private TextView hisnameText,hisStatusText;

    @Override
    protected void onStart() {
        super.onStart();
        mList.clear();
        getMsg();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Initi();

        btn_Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String message = eT_Message.getText().toString();
                if (!TextUtils.isEmpty(message)) {
                    chat(message);
                    eT_Message.setText("");
                }
            }
        });
        FirebaseMessaging.getInstance().subscribeToTopic("weather");

        if (FirebaseAuth.getInstance().getUid() != null) {

            FirebaseMessaging.getInstance().subscribeToTopic(FirebaseAuth.getInstance().getUid())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg = "win";
                            if (!task.isSuccessful()) {
                                msg = "lose";
                            }
                            Log.d("ddd", msg);
                            Toast.makeText(ChatActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });

        }

    }

    private void chat(final String msg) {
        Date currentTime = Calendar.getInstance().getTime();
        Log.e("time", "time : " + currentTime);
        String hisPerspective = "Chat/admin/" + myID;
        Map<String, Object> chatMap = new HashMap<>();
        chatMap.put("msg", msg);
        chatMap.put("from", myID);
        chatMap.put("time", currentTime);


        mFireStore.collection(hisPerspective).add(chatMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();


            }
        });

        //getstatus(hisID);

    }

    private void getMsg() {
        mList.clear();
        mFireStore.collection("Chat/admin/"+ myID).orderBy("time", Query.Direction.ASCENDING).addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        String msg = doc.getDocument().getString("msg");
                        String from = doc.getDocument().getString("from");

                        Log.e("msg", "msg : " + msg);
                        Log.e("msg", "msg : " + from);
                        mList.add(new ChatModel(msg, from));
                        mAdapter.notifyDataSetChanged();
                        mRecyclerView.smoothScrollToPosition(999999999);
                        mRefreshLayout.setRefreshing(false);

                    }

                }
            }
        });

        //getstatus(hisID);
    }

    private void Initi() {
        hisProfileImage = findViewById(R.id.his_imgview);
        hisStatusText=findViewById(R.id.his_status);
        hisnameText = findViewById(R.id.his_nameTxtview);
        mFireStore = FirebaseFirestore.getInstance();
        eT_Message = findViewById(R.id.chat_message_view);
        btn_Send = findViewById(R.id.chat_send_btn);
        progressBar = findViewById(R.id.Send_progress);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mList.clear();
                getMsg();
            }
        });


        //get extra
        hisID = getIntent().getStringExtra("userId");
        hisName = "TeleOne";
        hisImg = getIntent().getStringExtra("userImg");
        //hisStatus = getIntent().getStringExtra("status");
        Picasso.with(getApplicationContext()).load(R.drawable.icon).fit().centerInside().into(hisProfileImage);
        hisnameText.setText(hisName);



        myID = FirebaseAuth.getInstance().getUid();
        mList = new ArrayList<>();
        mAdapter = new ChatAdapter(ChatActivity.this, mList, myID, hisID, hisName, hisImg);
        mRecyclerView = findViewById(R.id.messages_list);
        mRecyclerView.hasFixedSize();


        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);


        //back
        ImageButton backbtn;
        backbtn = findViewById(R.id.backbtn);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }


    private String getname(){
        //how to get shared pref in service
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("users", MODE_PRIVATE);
        return prefs.getString("MyName", "No name defined");
    }
}
