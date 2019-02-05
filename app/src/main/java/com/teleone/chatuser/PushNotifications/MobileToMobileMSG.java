package com.teleone.chatuser.PushNotifications;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hima on 11/15/2018.
 */

public class MobileToMobileMSG {

    private Context context;

    public MobileToMobileMSG(Context context) {
        this.context = context;
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
                Toast.makeText(context, "Message Sent.", Toast.LENGTH_LONG).show();
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
                params.put("Authorization", "key=AIzaSyDZkWm3Cf1w9Kk7jsMHL8VXUf5k1IvaATc");
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(jsonObjReq);
    }

    public void sendMSG(String title, String body, String userID){
        sendNote(mObject(title,body,userID));
    }
}
