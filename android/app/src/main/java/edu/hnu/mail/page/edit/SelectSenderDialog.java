package edu.hnu.mail.page.edit;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.hnu.mail.R;
import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.data.entity.User;
import edu.hnu.mail.ui.mails.OnItemClickListener;
import edu.hnu.mail.ui.mails.UserListAdapter;
import edu.hnu.mail.util.CustomDialog;

public class SelectSenderDialog extends Dialog {

    private RecyclerView recyclerView;
    private UserListAdapter userListAdapter;
    private OnClickListener onClickListener;
    private List<User> userList;

    public void setUserList(List<User> userList){
        this.userList = userList;
    }

    public SelectSenderDialog(@NonNull Context context) {
        super(context);
    }

    public interface OnClickListener{
        void click(int position);
    }

    public void setOnClickListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    public SelectSenderDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected SelectSenderDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_select_sender);
        //初始化界面控件
        initView();
        //初始化界面控件的事件
        initListener();
    }

    private void initView(){
        recyclerView = findViewById(R.id.user_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userListAdapter = new UserListAdapter();
        userListAdapter.setUserList(userList);
        recyclerView.setAdapter(userListAdapter);
    }

    private void initListener(){
        userListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                onClickListener.click(position);
            }
        });
    }
}
