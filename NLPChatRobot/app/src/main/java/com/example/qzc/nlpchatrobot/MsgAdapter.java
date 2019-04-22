package com.example.qzc.nlpchatrobot;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;


public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {

    private List<Msg> mMsgList;
    public static final int ROBOT_1 = 1;
    public static final int ROBOT_2 = 2;
    private int robotType = ROBOT_1;

    public int getRobotType(){
        return robotType;
    }
    public void setRobotType(int robotType){
        this.robotType = robotType;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout leftLayout, rightLayout;
        TextView leftTextView, rightTextView;
        ImageView leftIcon, rightIcon;

        public ViewHolder(View view){
            super(view);
            leftLayout = (LinearLayout) view.findViewById(R.id.left_msg_layout);
            rightLayout = (LinearLayout) view.findViewById(R.id.right_msg_layout);
            leftTextView = (TextView) view.findViewById(R.id.left_msg);
            rightTextView = (TextView) view.findViewById(R.id.right_msg);
            leftIcon = (ImageView) view.findViewById(R.id.left_icon);
            rightIcon = (ImageView) view.findViewById(R.id.right_icon);
        }
    }

    public MsgAdapter(List<Msg> msgList){
        mMsgList = msgList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.msg_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Msg msg = mMsgList.get(position);
        if (msg.getType() == Msg.TYPE_RECEIVED){
            switch (robotType){
                case ROBOT_1:
                    holder.leftIcon.setImageResource(R.drawable.robot_1);
                    break;
                case ROBOT_2:
                    holder.leftIcon.setImageResource(R.drawable.robot_2);
                    break;
                default:
                    break;
            }
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.INVISIBLE);
            holder.leftTextView.setText(msg.getContent());
        }
        else if (msg.getType() == Msg.TYPE_SENT){
            holder.leftLayout.setVisibility(View.INVISIBLE);
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.rightTextView.setText(msg.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return mMsgList.size();
    }
}
