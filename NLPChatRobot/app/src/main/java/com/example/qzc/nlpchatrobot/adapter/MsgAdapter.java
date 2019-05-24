package com.example.qzc.nlpchatrobot.adapter;


import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.qzc.nlpchatrobot.R;
import com.example.qzc.nlpchatrobot.database_management.Msg;

import java.util.List;


public class MsgAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // the msg list to fill the holders
    private List<Msg> mMsgList;

    public MsgAdapter(List<Msg> msgList){
        mMsgList = msgList;
    }


    //a view holder for the left text: the user receives a text message
    static class LeftTextViewHolder extends RecyclerView.ViewHolder {

        LinearLayout leftLayout;
        TextView leftTextView;
        ImageView leftIcon;

        LeftTextViewHolder(View view){
            super(view);
            leftLayout = (LinearLayout) view.findViewById(R.id.left_msg_layout);
            leftTextView = (TextView) view.findViewById(R.id.left_msg);
            leftIcon = (ImageView) view.findViewById(R.id.left_icon);
        }
    }


    //a view holder for the right text: the user sends a text message
    static class RightTextViewHolder extends RecyclerView.ViewHolder{

        LinearLayout rightLayout;
        TextView rightTextView;
        ImageView rightIcon;

        RightTextViewHolder(View view){
            super(view);
            rightLayout = (LinearLayout) view.findViewById(R.id.right_msg_layout);
            rightTextView = (TextView) view.findViewById(R.id.right_msg);
            rightIcon = (ImageView) view.findViewById(R.id.right_icon);
        }
    }

    //a view holder for the right image: the user sends an image
    static class RightImageViewHolder extends RecyclerView.ViewHolder{

        LinearLayout rightLayout;
        ImageView rightImageView, rightIcon;

        RightImageViewHolder(View view){
            super(view);
            rightLayout = (LinearLayout) view.findViewById(R.id.right_image_layout);
            rightImageView = (ImageView) view.findViewById(R.id.right_image);
            rightIcon = (ImageView) view.findViewById(R.id.right_icon);
        }
    }


    // get the item type in the position of the recycle view to set the corresponding view holder
    @Override
    public int getItemViewType(int position) {
        Msg msg = mMsgList.get(position);
        return msg.getType();
    }




    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType){
            case Msg.TYPE_RECEIVED:
                View leftTextView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.left_msg_item, viewGroup, false);
                return new LeftTextViewHolder(leftTextView);
            case Msg.TYPE_SENT:
                View rightTextView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.right_msg_item, viewGroup, false);
                return new RightTextViewHolder(rightTextView);
            case Msg.TYPE_PHOTO:
                View rightImageView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.right_image_item, viewGroup, false);
                return new RightImageViewHolder(rightImageView);

            default:
                //@NonNull requirement
                View defaultTextView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.right_msg_item, viewGroup, false);
                return new RightTextViewHolder(defaultTextView);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Msg msg = mMsgList.get(position);
        if (holder instanceof LeftTextViewHolder){
            ((LeftTextViewHolder) holder).leftTextView.setText(msg.getContent());
            switch (msg.getRobotType()){
                case Msg.ROBOT_1:
                    ((LeftTextViewHolder) holder).leftIcon.setImageResource(R.drawable.robot_1);
                    break;
                case Msg.ROBOT_2:
                    ((LeftTextViewHolder) holder).leftIcon.setImageResource(R.drawable.robot_2);
                    break;
                default:
                    break;
            }
        }

        else if(holder instanceof RightTextViewHolder){
            ((RightTextViewHolder) holder).rightTextView.setText(msg.getContent());
        }

        else if(holder instanceof RightImageViewHolder){
            ((RightImageViewHolder) holder).rightImageView.setImageBitmap(BitmapFactory.decodeFile(msg.getContent()));
        }
    }

    @Override
    public int getItemCount() {
        return mMsgList.size();
    }


}
