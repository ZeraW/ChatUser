package com.teleone.chatuser.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.teleone.chatuser.Models.ChatModel;
import com.teleone.chatuser.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Hima on 6/19/2018.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    Context context;
    private List<ChatModel> mList;
    String myID,hisID,hisName,hisImg;

    public ChatAdapter(Context context, List<ChatModel> mList, String myID, String hisID, String hisName, String hisImg) {
        this.context = context;
        this.mList = mList;
        this.myID = myID;
        this.hisID = hisID;
        this.hisName = hisName;
        this.hisImg = hisImg;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.message_single_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        ChatModel currentPosition = mList.get(position);
        holder.messageText.setText(currentPosition.getMsg());

        if (currentPosition.getFrom().equals(myID)){
            //holder.displayName.setText("me");
            holder.relativeLayout.setBackground(context.getResources().getDrawable(R.drawable.chatback2));
            holder.profileImage.setVisibility(View.GONE);
            holder.linearLayout.setGravity(Gravity.END);
            LinearLayout.LayoutParams relativeParams = (LinearLayout.LayoutParams)holder.relativepadding.getLayoutParams();
            relativeParams.setMargins(220, 0, 20, 0);  // left, top, right, bottom
            holder.relativepadding.setLayoutParams(relativeParams);

        }else {
            //holder.displayName.setText(hisName);
            holder.profileImage.setVisibility(View.VISIBLE);
            Picasso.with(context).load(R.drawable.icon).into(holder.profileImage);
            holder.linearLayout.setGravity(Gravity.START);
            holder.relativeLayout.setBackground(context.getResources().getDrawable(R.drawable.chatback1));

            LinearLayout.LayoutParams relativeParams = (LinearLayout.LayoutParams)holder.relativepadding.getLayoutParams();
            relativeParams.setMargins(20, 0, 220, 0);  // left, top, right, bottom
            holder.relativepadding.setLayoutParams(relativeParams);


        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView messageText;
        private CircleImageView profileImage;
        private TextView displayName;
        private ImageView messageImage;
        private LinearLayout linearLayout;
        private RelativeLayout relativeLayout,relativepadding;
        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            messageText = (TextView) mView.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) mView.findViewById(R.id.message_profile_layout);
            displayName = (TextView) mView.findViewById(R.id.name_text_layout);
            messageImage = (ImageView) mView.findViewById(R.id.message_image_layout);
            linearLayout =  mView.findViewById(R.id.message_single_layout);
            relativeLayout = mView.findViewById(R.id.message_relative_layout);
            relativepadding = mView.findViewById(R.id.relative_chatpadding);
        }
    }
}
