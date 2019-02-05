package com.teleone.chatuser.Activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.teleone.chatuser.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class SendActivity extends AppCompatActivity {
    private TextView tV_sendto;
    private EditText eT_Message;
    private Button btn_Send;
    private String myID,hisID,hisName;
    private FirebaseFirestore mFireStore;
    ProgressBar progressBar;
    RequestQueue queue;
    String url ="https://54807e2c-4fb6-4eac-910e-0c017fc99617.pushnotifications.pusher.com/publish_api/v1/instances/54807e2c-4fb6-4eac-910e-0c017fc99617/publishes";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        Initi();
        //do stuff
        tV_sendto.append(" "+hisName);
        getMsg();
        btn_Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String message = eT_Message.getText().toString();
                if (!TextUtils.isEmpty(message)){
                    progressBar.setVisibility(View.VISIBLE);
                    Map<String,Object> notifications = new HashMap<>();
                    notifications.put("message",message);
                    notifications.put("from",myID);

                    /*mFireStore.collection("Users/"+hisID+"/Notifications").add(notifications).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            eT_Message.setText("");
                            pusher(mObject(user_name,message,hisID));
                            progressBar.setVisibility(View.GONE);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SendActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_LONG).show();

                            progressBar.setVisibility(View.GONE);

                        }
                    });*/
                    chat(message);
                }

            }
        });



    }
    private void chat(String msg){
        String myPerspective = "Chat/"+myID+"/"+hisID;
        String hisPerspective = "Chat/"+hisID+"/"+myID;
        Map<String,Object> chatMap = new HashMap<>();
        chatMap.put("msg",msg);
        chatMap.put("from",myID);

        mFireStore.collection(myPerspective).add(chatMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

                progressBar.setVisibility(View.GONE);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SendActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_LONG).show();

                progressBar.setVisibility(View.GONE);

            }
        });
        mFireStore.collection(hisPerspective).add(chatMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

                progressBar.setVisibility(View.GONE);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SendActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_LONG).show();

                progressBar.setVisibility(View.GONE);

            }
        });

    }

    private void getMsg(){
        mFireStore.collection("Chat/"+myID+"/"+hisID).addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                for (DocumentChange doc : documentSnapshots.getDocumentChanges()){
                    if (doc.getType()== DocumentChange.Type.ADDED){
                        String msg = doc.getDocument().getString("msg");
                        String from = doc.getDocument().getString("from");

                        Log.e("msg","msg : "+msg);
                        Log.e("msg","msg : "+from);
                        /*mList.add(new UsersModel(user_Id,name,image));
                        mAdapter.notifyDataSetChanged();*/

                    }

                }
            }
        });
    }

    private void Initi(){
        mFireStore = FirebaseFirestore.getInstance();
        tV_sendto = findViewById(R.id.Send_sendto);
        eT_Message = findViewById(R.id.Send_Message);
        btn_Send = findViewById(R.id.Send_doSend);
        progressBar= findViewById(R.id.Send_progress);

        //get extra
        hisID = getIntent().getStringExtra("userId");
        hisName = getIntent().getStringExtra("userName");
        myID = FirebaseAuth.getInstance().getUid();
    }

    private JSONObject mObject(String title, String body, String userID){
        JSONObject mainObject = new JSONObject();
        JSONObject fcm = new JSONObject();

        String toic= "'"+userID+"' in topics || 'all' in topics";

        try {
            mainObject.put("condition",toic);

            fcm.put("body",body);
            fcm.put("title",title);
            fcm.put("content_available",true);
            fcm.put("priority","high");

            mainObject.put("data",fcm);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return mainObject;
    }

    private void sendNote(JSONObject object){
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, "https://fcm.googleapis.com/fcm/send", object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(SendActivity.this, "Message Sent.", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "key=AIzaSyDWU-Zc-zD-UO2PqlsWnJKibIF4H3WaCeA");
                return params;
            }
        };

        queue = Volley.newRequestQueue(this);
        queue.add(jsonObjReq);
    }

}
