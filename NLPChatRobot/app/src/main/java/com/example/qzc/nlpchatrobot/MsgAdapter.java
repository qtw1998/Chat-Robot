package com.example.qzc.nlpchatrobot;

import android.graphics.Bitmap;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;


public class MsgAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //define the different robot types
    public static final int ROBOT_1 = 11;
    public static final int ROBOT_2 = 12;
    private int robotType = ROBOT_1;

    public int getRobotType(){
        return robotType;
    }
    public void setRobotType(int robotType){
        this.robotType = robotType;
    }


    private Bitmap rightImgBitmap;
    public void setRightImgBitmap(Bitmap bm){
        this.rightImgBitmap = bm;
    }


    // the msg list to fill the holders
    private List<Msg> mMsgList;

    public MsgAdapter(List<Msg> msgList){
        mMsgList = msgList;
    }


    // define different view holder types
    private static final int TYPE_LEFT_TEXT = 21;
    private static final int TYPE_RIGHT_TEXT = 22;
    private static final int TYPE_RIGHT_PHOTO = 23;

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
            rightIcon = (ImageView) view.findViewById(R.id.right_icon_for_text);
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
            rightIcon = (ImageView) view.findViewById(R.id.right_icon_for_img);
        }
    }


    // get the item type in the position of the recycle view to set the corresponding view holder
    @Override
    public int getItemViewType(int position) {
        Msg msg = mMsgList.get(position);
        int msgType = msg.getType();
        switch (msgType){
            case Msg.TYPE_RECEIVED:
                return TYPE_LEFT_TEXT;
            case Msg.TYPE_SENT:
                return TYPE_RIGHT_TEXT;
            case Msg.TYPE_PHOTO:
                return TYPE_RIGHT_PHOTO;
            default:
                // to correspond with default case of the onCreateViewHolder function
                return TYPE_RIGHT_TEXT;
        }
    }




    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType){
            case TYPE_LEFT_TEXT:
                View leftTextView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.left_msg_item, viewGroup, false);
                return new LeftTextViewHolder(leftTextView);
            case TYPE_RIGHT_TEXT:
                View rightTextView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.right_msg_item, viewGroup, false);
                return new RightTextViewHolder(rightTextView);
            case TYPE_RIGHT_PHOTO:
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
            switch (robotType){
                case ROBOT_1:
                    ((LeftTextViewHolder) holder).leftIcon.setImageResource(R.drawable.robot_1);
                    break;
                case ROBOT_2:
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
            ((RightImageViewHolder) holder).rightImageView.setImageBitmap(rightImgBitmap);
        }
    }

    @Override
    public int getItemCount() {
        return mMsgList.size();
    }


}
