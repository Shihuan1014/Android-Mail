package edu.hnu.mail.page.setting;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.Random;

import edu.hnu.mail.R;
import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.data.entity.User;
import edu.hnu.mail.data.source.UserDataSource;
import edu.hnu.mail.data.source.local.LocalUserDataSource;
import edu.hnu.mail.data.source.remote.POP3LoginTest;
import edu.hnu.mail.data.source.remote.SMTPLoginTest;

public class ServerFragment extends Fragment {

    private String userName;
    private String passWord;

    //顶部控制栏
    private TextView buttonCancel;
    private TextView buttonFinish;
    //收件服务器设置
    private EditText popAddress;
    private EditText popUserName;
    private EditText popPassWord;
    private EditText popPort;
    //发件服务器设置
    private EditText smtpAddress;
    private EditText smtpUserName;
    private EditText smtpPassWord;
    private EditText smtpPort;
    private Handler handler = new Handler();
    private final SMTPLoginTest smtpLoginTest = SMTPLoginTest.getInstance();
    private final POP3LoginTest pop3LoginTest = POP3LoginTest.getInstance();
    private UserDataSource userDataSource;
    private User user;


    public ServerFragment(User user){
        this.user = user;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pop_smtp,container,false);
        initData();
        initView(root);
        initListener();
        return root;
    }

    private void initData() {
        userDataSource = new LocalUserDataSource(getContext());
    }

    private void initView(View root){
        buttonCancel = root.findViewById(R.id.button_cancel);
        buttonFinish = root.findViewById(R.id.button_finish);
        buttonCancel.setVisibility(View.VISIBLE);

        popAddress = root.findViewById(R.id.pop_address);
        popAddress.setText(user.getPopHost());
        popUserName = root.findViewById(R.id.pop_user_name);
        popUserName.setText(user.getUserName());
        popPassWord = root.findViewById(R.id.pop_password);
        popPassWord.setText(user.getPopPassword());
        popPort = root.findViewById(R.id.pop_port);
        popPort.setText(String.valueOf(user.getPopPort()));
        smtpAddress = root.findViewById(R.id.smtp_address);
        smtpAddress.setText(user.getSmtpHost());
        smtpUserName = root.findViewById(R.id.smtp_username);
        smtpUserName.setText(user.getSmtpUserName());
        smtpPassWord = root.findViewById(R.id.smtp_password);
        smtpPassWord.setText(user.getSmtpPassword());
        smtpPort = root.findViewById(R.id.smtp_port);
        smtpPort.setText(String.valueOf(user.getSmtpPort()));
    }
    private void initListener(){
        buttonFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager manager =
                        ((InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE));
                if(manager!=null)
                    manager.hideSoftInputFromWindow(v.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String popHostText = popAddress.getText().toString();
                            String popPortText = popPort.getText().toString();
                            String smtpHostText = smtpAddress.getText().toString();
                            String smtpPortText = smtpPort.getText().toString();
                            String popUserNameText = popUserName.getText().toString();

                            if(popHostText.length()==0 || smtpHostText.length()==0){
                                return;
                            }
                            if(popUserNameText.length()==0){
                                popUserNameText = userName;
                            }
                            String popPassWordText = popPassWord.getText().toString();
                            System.out.println(popPassWordText + popPassWordText.length());
                            if(popPassWordText.length()==0){
                                popPassWordText = passWord;
                            }
                            String smtpUserNameText = smtpUserName.getText().toString();
                            if(smtpUserNameText.length()==0){
                                smtpUserNameText = userName;
                            }
                            String smtpPassWordText = smtpPassWord.getText().toString();
                            if(smtpPassWordText.length()==0){
                                smtpPassWordText = passWord;
                            }

                            pop3LoginTest.connect(popHostText,Integer.valueOf(popPortText));
                            smtpLoginTest.connect(smtpHostText,Integer.valueOf(smtpPortText));
                            if(pop3LoginTest.isConnected()){
                                if(!pop3LoginTest.login(popUserNameText, popPassWordText)){
                                    try {
                                        pop3LoginTest.disconnect();
                                    }catch (Exception ee){
                                        ee.printStackTrace();
                                    }
                                    makeToast("POP3用户名或密码错误");
                                    return;
                                }
                            }else {
                                makeToast("无法连接上POP3服务器");
                                return;
                            }

                            if(smtpLoginTest.isConnected()){
                                if(!smtpLoginTest.authLogin(smtpUserNameText,smtpPassWordText)){
                                    makeToast("SMTP用户名或密码错误");
                                    try {
                                        smtpLoginTest.disconnect();
                                    }catch (Exception ee){
                                        ee.printStackTrace();
                                    }
                                    return;
                                }
                            }else {
                                makeToast("无法连接上SMTP服务器");
                                return;
                            }
                            makeToast("配置成功");
                            // TODO: 2020/4/23 此处要将user存入userDao持久化数据，并写入UserInfo
                            successLogin(popUserNameText,popPassWordText,smtpUserNameText,smtpPassWordText);
                        } catch (IOException e) {
                            e.printStackTrace();
                            makeToast("无法连接上服务器");
                        }
                    }
                }).start();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    public void makeToast(String msg){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void successLogin(String popUserName,String popPassWord,String smtpUserName,
                             String smtpPassword){
        user.setPopHost(popAddress.getText().toString());
        user.setPopUserName(popUserName);
        user.setPopPassword(popPassWord);
        user.setPopPort(Integer.parseInt(popPort.getText().toString()));
        user.setSmtpHost(smtpAddress.getText().toString());
        user.setSmtpUserName(smtpUserName);
        user.setSmtpPassword(smtpPassword);
        user.setSmtpPort(Integer.parseInt(smtpPort.getText().toString()));
        userDataSource.updateNickName(user, new UserDataSource.UpdateCallBack() {
            @Override
            public void onUpdateSuccess() {
                UserInfo.users.set(UserInfo.currentIndex,user);
                getActivity().getSupportFragmentManager().popBackStack();
            }

            @Override
            public void onUpdateFailure() {
                Toast.makeText(getContext(),"设置失败",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
