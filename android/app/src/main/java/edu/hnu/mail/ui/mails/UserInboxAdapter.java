package edu.hnu.mail.ui.mails;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.hnu.mail.R;
import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.data.entity.User;
import edu.hnu.mail.data.source.remote.POP3;

public class UserInboxAdapter extends RecyclerView.Adapter<UserInboxAdapter.ViewHolder> {

    private List<User> userList;
    private OnItemClickListener onItemClickListener;
    //收件箱还是账户，区别在于显示 xxx的邮箱
    private boolean isInboxType = true;

    public void setInboxType(boolean type){
        this.isInboxType = type;
    }

    public void setUserList(List<User> userList){
        this.userList = userList;
        notifyDataSetChanged();
    }

    public void addUserMailCount(int count[]){
        int l = userList.size();
        for (int i = 0; i < l;i++){
            if (count[i]>0){
                notifyItemChanged(i);
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View root = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user,parent,false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        if(isInboxType){
            holder.userName.setText(user.getPopUserName()+"的收件箱");
        }else{
            holder.userName.setText(user.getPopUserName());
        }

        holder.mailCount.setVisibility(View.VISIBLE);
        holder.mailCount.setText(String.valueOf(user.getMailInboxCount()));
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener!=null) {
                    onItemClickListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList!=null?userList.size():0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private LinearLayout layout;
        private TextView userName;
        private TextView mailCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout_account);
            userName = itemView.findViewById(R.id.username);
            mailCount = itemView.findViewById(R.id.right_text);
        }
    }
}