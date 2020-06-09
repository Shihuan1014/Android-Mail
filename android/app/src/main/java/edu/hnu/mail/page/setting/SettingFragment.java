package edu.hnu.mail.page.setting;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import edu.hnu.mail.R;
import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.page.login.LoginFragment;
import edu.hnu.mail.page.login.ServerFragment;
import edu.hnu.mail.page.mailbox.MailBoxFragment;
import edu.hnu.mail.page.userinfo.UserInfoFragment;
import edu.hnu.mail.ui.mails.OnItemClickListener;
import edu.hnu.mail.ui.mails.UserListAdapter;

public class SettingFragment extends Fragment {

    //顶部
    private ImageView buttonBack;

    private LinearLayout layoutAddUser;

    private RecyclerView userListView;

    private UserListAdapter userListAdapter;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_setting,container,false);
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        root.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){
                    getActivity().getSupportFragmentManager().popBackStack();
                    return true;
                }
                return false;
            }
        });
        initData();
        initView(root);
        initListener();
        return root;
    }

    private void initView(View root) {

        //顶部
        buttonBack = root.findViewById(R.id.button_back);

        layoutAddUser = root.findViewById(R.id.add_user);
        userListView = root.findViewById(R.id.user_list);
        userListAdapter = new UserListAdapter();
        userListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                        .beginTransaction();
                transaction.replace(R.id.fragment,new UserInfoFragment());
                transaction.addToBackStack(getActivity().getClass().getName());
                UserInfo.currentIndex = position;
                transaction.commit();
            }
        });
        userListView.setAdapter(userListAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        userListView.setLayoutManager(linearLayoutManager);
        userListAdapter.setUserList(UserInfo.users);
    }

    private void initData(){

    }

    private void initListener(){

        //顶部
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        layoutAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment,new LoginFragment());
                transaction.addToBackStack(getActivity().getClass().getName());
                transaction.commit();
            }
        });
    }


    @Override
    public void onResume(){
        super.onResume();
        if (userListAdapter!=null){
            if(UserInfo.users.size() > 0){
                userListAdapter.notifyDataSetChanged();
            }else{
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }
}
