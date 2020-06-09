package edu.hnu.mail.page.manager;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

import edu.hnu.mail.R;
import edu.hnu.mail.constant.DefaultServer;
import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.data.entity.User;
import edu.hnu.mail.data.source.UserDataSource;
import edu.hnu.mail.data.source.local.LocalUserDataSource;
import edu.hnu.mail.data.source.remote.POP3LoginTest;
import edu.hnu.mail.data.source.remote.SMTPLoginTest;
import edu.hnu.mail.util.HttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class ServerFragment extends Fragment {

    private String userName;
    private String passWord;

    //顶部控制栏
    private TextView buttonCancel;
    private TextView buttonFinish;
    //域名和邮箱大小
    private EditText domain;
    private EditText maxMail;
    //收件服务器设置
    private EditText popPort;
    //发件服务器设置
    private EditText smtpPort;
    private EditText maxReceiver;

    private AlertDialog loadingDialog;

    private Handler handler;
    private final SMTPLoginTest smtpLoginTest = SMTPLoginTest.getInstance();
    private final POP3LoginTest pop3LoginTest = POP3LoginTest.getInstance();
    private UserDataSource userDataSource;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_pop_smtp,container,false);
        handler = new Handler();
        showLoading();
        initData();
        initView(root);
        initListener();
        return root;
    }

    public void initUser(String userName,String passWord){
        this.userName = userName;
        this.passWord = passWord;
    }

    /**
     * 加载中
     */
    private void showLoading(){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_loading, null);
// 加载自定义的布局文件
        loadingDialog = new AlertDialog.Builder(getContext()).create();
        loadingDialog.show();
        Window window = loadingDialog.getWindow();
        window.setContentView(view);
        WindowManager windowManager = getActivity().getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = display.getWidth() / 2;
        lp.height = display.getWidth() / 2;
        window.setAttributes(lp);
        TextView textView = view.findViewById(R.id.text);
        textView.setText("正在获取信息");

    }

    private void initData() {
        userDataSource = new LocalUserDataSource(getContext());
        HttpUtil.httpGet(DefaultServer.ADMIN_HOST + "/server", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                makeToast("网络错误");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismiss();
                    }
                },500);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if(jsonObject.getInt("status") == 200){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //域名和邮箱大小
                                try {
                                    domain.setText(jsonObject.getString("domain"));
                                    maxMail.setText(String.valueOf(jsonObject.getInt("maxMail") / 1024));
                                    popPort.setText(String.valueOf(jsonObject.getInt("popPort")));
                                    smtpPort.setText(String.valueOf(jsonObject.getInt("smtpPort")));
                                    maxReceiver.setText(String.valueOf(jsonObject.getInt("maxReceiver")));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }else{
                        makeToast("错误代码："+jsonObject.getInt("status"));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    makeToast("获取数据时发生错误");
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismiss();
                    }
                },500);
            }
        });
    }

    private void initView(View root){
        buttonCancel = root.findViewById(R.id.button_cancel);
        buttonFinish = root.findViewById(R.id.button_finish);
        if(UserInfo.users!=null && UserInfo.users.size() > 0){
            buttonCancel.setVisibility(View.VISIBLE );
        }
        popPort = root.findViewById(R.id.pop_port);
        smtpPort = root.findViewById(R.id.smtp_port);
        domain = root.findViewById(R.id.domain);
        maxMail = root.findViewById(R.id.maxMail);
        maxReceiver = root.findViewById(R.id.max_receiver);
    }


    private void initListener(){
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
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
                        String popPortText = popPort.getText().toString();
                        String smtpPortText = smtpPort.getText().toString();
                        String domainText = domain.getText().toString();
                        String maxReceiverText = maxReceiver.getText().toString();
                        String maxMailText = maxMail.getText().toString();
                        FormBody.Builder bodyBuilder = new FormBody.Builder();
                        bodyBuilder.add("popPort",popPortText);
                        bodyBuilder.add("smtpPort",smtpPortText);
                        bodyBuilder.add("domain",domainText);
                        bodyBuilder.add("maxReceiver",maxReceiverText);
                        bodyBuilder.add("maxMail",maxMailText);
                        HttpUtil.httpPost(DefaultServer.ADMIN_HOST + "/server", bodyBuilder.build(), new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                makeToast("onFailure修改错误");
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                System.out.println(response);
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    if(jsonObject.getInt("status") == 200){
                                        makeToast("修改成功");
                                        getActivity().getSupportFragmentManager().popBackStack();
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }).start();
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
}
