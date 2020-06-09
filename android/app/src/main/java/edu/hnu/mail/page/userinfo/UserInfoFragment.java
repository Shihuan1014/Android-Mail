package edu.hnu.mail.page.userinfo;

import android.os.Bundle;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import edu.hnu.mail.R;
import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.data.entity.User;
import edu.hnu.mail.data.source.local.LocalUserDataSource;
import edu.hnu.mail.data.source.local.RemoteMailBoxDataSource;
import edu.hnu.mail.page.mailbox.MailBoxPresenter;
import edu.hnu.mail.page.manager.ManageFragment;
import edu.hnu.mail.page.setting.ServerFragment;
import edu.hnu.mail.page.setting.SettingFragment;
import edu.hnu.mail.util.CustomDialog;

public class UserInfoFragment extends Fragment implements UserInfoContact.View {

    private UserInfoPresenter presenter;

    private ImageView buttonBack;
    private TextView username;
    private TextView nickName;
    private LinearLayout layoutNickName;
    private LinearLayout layoutServer;
    private LinearLayout layoutManager;
    private LinearLayout layoutPassword;
    private TextView buttonDelete;
    private User user;
    private CustomDialog deleteDialog;
    private CustomDialog nicknameDialog;
    private CustomDialog passwordDialog;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user_info,container,false);
        presenter = new UserInfoPresenter(this, new LocalUserDataSource(getContext()));
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

    private void initData(){
        user = UserInfo.getCurrentUser();
    }

    private void initView(View root) {
        buttonBack = root.findViewById(R.id.button_back);
        username = root.findViewById(R.id.username);
        username.setText(user.getUserName());
        nickName = root.findViewById(R.id.nickname);
        nickName.setText(user.getNickName());
        layoutNickName = root.findViewById(R.id.layout_nickname);
        layoutServer = root.findViewById(R.id.layout_server);
        layoutPassword = root.findViewById(R.id.layout_password);
        buttonDelete = root.findViewById(R.id.button_delete);
        layoutManager = root.findViewById(R.id.layout_manager);
        if (user.getUserType() == 1){
            layoutManager.setVisibility(View.VISIBLE);
        }
    }

    private void initListener(){
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(deleteDialog==null){
                    deleteDialog = new CustomDialog(getContext());
                    deleteDialog.setTitle("删除用户"+user.getUserName()+"?");
                    deleteDialog.setButtonCancelText("取消");
                    deleteDialog.setButtonConfirmText("确认删除");
                    //真正删除
                    deleteDialog.setSubTitle("移除该用户在本应用的所有本地存储");
                    deleteDialog.setOnClickListener(new CustomDialog.OnClickListener() {
                        @Override
                        public void cancel() {
                            deleteDialog.dismiss();
                        }

                        @Override
                        public void save() {

                        }
                        @Override
                        public void confirm() {
                            deleteDialog.dismiss();
                            presenter.deleteUser(user.getUserId());
                        }
                    });
                }
                deleteDialog.show();
            }
        });

        layoutManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ManageFragment manageFragment = new ManageFragment();
                FragmentTransaction transaction = getActivity().
                        getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment,manageFragment);
                transaction.addToBackStack(getActivity().getClass().getName());
                transaction.commit();
            }
        });

        layoutNickName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNickNameDialog();
            }
        });

        layoutPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordDialog();
            }
        });

        layoutServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().
                        getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment,new ServerFragment(user));
                transaction.addToBackStack(getActivity().getClass().getName());
                transaction.commit();
            }
        });
    }


    private void showNickNameDialog(){
        if(nicknameDialog == null){
            nicknameDialog = new CustomDialog(getContext());
            nicknameDialog.setXml(R.layout.dialog_nickname);
            nicknameDialog.setTitle("修改发信名称");
            nicknameDialog.setButtonConfirmText("保存修改");
            nicknameDialog.setOnClickListener(new CustomDialog.OnClickListener() {
                @Override
                public void cancel() {
                    nicknameDialog.dismiss();
                }

                @Override
                public void save() {

                }

                @Override
                public void confirm() {
                    EditText e = nicknameDialog.findViewById(R.id.nickname);
                    user.setNickName(e.getText().toString());
                    presenter.updateNickName(user);
                }
            });
        }
        nicknameDialog.show();
        EditText e = nicknameDialog.findViewById(R.id.nickname);
        e.setText(user.getNickName());
        e.setSelection(user.getNickName().length());
    }

    private void showPasswordDialog(){
        if(passwordDialog == null){
            passwordDialog = new CustomDialog(getContext());
            passwordDialog.setXml(R.layout.dialog_nickname);
            passwordDialog.setTitle("修改密码");
            passwordDialog.setButtonConfirmText("保存修改");
            passwordDialog.setOnClickListener(new CustomDialog.OnClickListener() {
                @Override
                public void cancel() {
                    passwordDialog.dismiss();
                }

                @Override
                public void save() {

                }

                @Override
                public void confirm() {
                    EditText e = passwordDialog.findViewById(R.id.nickname);
                    presenter.updatePassword(user,e.getText().toString());
                }
            });
        }
        passwordDialog.show();
        EditText e = passwordDialog.findViewById(R.id.nickname);
        e.setText(user.getPopPassword());
        e.setSelection(user.getPopPassword().length());
    }

    @Override
    public void updateNickName(String nickName) {
        this.nickName.setText(nickName);
        nicknameDialog.dismiss();
        makeToast("修改成功");
    }

    @Override
    public void updatePassword(String password) {
        passwordDialog.dismiss();
        user.setPopPassword(password);
        user.setSmtpPassword(password);
        UserInfo.getCurrentUser().setPopPassword(password);
        UserInfo.getCurrentUser().setSmtpPassword(password);
        makeToast("修改成功");
    }

    @Override
    public void deleteSuccess() {
        try {
            UserInfo.getCurrentUser().getPop3Client().disconnect();
        }catch (Exception e){
            e.printStackTrace();
        }
        UserInfo.users.remove(UserInfo.currentIndex);
        UserInfo.currentIndex = 0;
        getActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    public void showFailure() {
        makeToast("操作失败");
    }

    @Override
    public void showSuccess() {

    }

    private void makeToast(String msg){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
