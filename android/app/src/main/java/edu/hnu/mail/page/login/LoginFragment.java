package edu.hnu.mail.page.login;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.hnu.mail.R;
import edu.hnu.mail.constant.DefaultServer;
import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.data.entity.User;
import edu.hnu.mail.data.source.UserDataSource;
import edu.hnu.mail.data.source.local.LocalUserDataSource;
import edu.hnu.mail.data.source.remote.POP3LoginTest;
import edu.hnu.mail.data.source.remote.SMTPLoginTest;
import edu.hnu.mail.page.mailbox.MailBoxFragment;
import edu.hnu.mail.page.manager.ManageFragment;
import edu.hnu.mail.ui.mails.MailCompleteTextView;
import edu.hnu.mail.util.HttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Response;

public class LoginFragment extends Fragment {

    private ImageView buttonBack;
    private TextView buttonChangeMail;
    private MailCompleteTextView userNameView;
    private String[] completeWords = new String[] { "北京"};
//    ArrayAdapter<String> adapter;
    private EditText passWordView;
    private TextView buttonSubmit;
    private TextView buttonRegister;
    private TextView buttonRegisterSubmit;
    private EditText registerUserName;
    private TextView domainTextView;
    private EditText registerPassWord;
    private TextView buttonLogin;
    private TextView buttonManager;
    private TextView buttonUser;
    private TextView mailCompany;

    private LinearLayout loginLayout;
    private LinearLayout registerLayout;
    private LinearLayout optionLayout;


    private final SMTPLoginTest smtpLoginTest = SMTPLoginTest.getInstance();
    private final POP3LoginTest pop3LoginTest = POP3LoginTest.getInstance();

    //模式
    private boolean isOurMailAccount = true;
    private boolean isOnlyAccount = false;
    private UserDataSource userDataSource;

    public void setIsOnlyAccount(boolean isOnlyAccount){
        this.isOnlyAccount = isOnlyAccount;
    }

    //条件满足登录吗
    private boolean canLogin = false;
    private Pattern mailPattern = Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");

    private boolean canRegister = false;
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login,container,false);
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        root.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){
                    System.out.println("Login Fragment got");
                    if(!isOnlyAccount){
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                    return true;
                }
                return false;
            }
        });
        initView(root);
//        adapter = new ArrayAdapter<String>(getContext(),R.layout.item_complete,completeWords);
//        userName.setThreshold(1);//这里是设置输入1个字就开始联想
//        userName.setAdapter(adapter);
        initData();
        initListener();
        return root;
    }

    private void initData() {
        userDataSource = new LocalUserDataSource(getContext());

        HttpUtil.httpGet(DefaultServer.ADMIN_HOST + "/server", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                makeToast("网络错误");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if(jsonObject.getInt("status") == 200){
                        DefaultServer.domain = jsonObject.getString("domain");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                domainTextView.setText("@"+DefaultServer.domain);
                            }
                        });
                    }
                }catch (Exception e){
                    makeToast("网络错误");
                    e.printStackTrace();
                }
            }
        });
    }

    private void initView(View root) {
        buttonBack = root.findViewById(R.id.button_back);
        if (isOnlyAccount){
            buttonBack.setVisibility(View.GONE);
        }
        buttonChangeMail = root.findViewById(R.id.button_change_mail);
        buttonSubmit = root.findViewById(R.id.button_submit);
        if(canLogin){
            buttonSubmit.setBackgroundColor(Color.parseColor("#4c98f2"));
        }
        buttonRegister = root.findViewById(R.id.button_register);
        buttonRegisterSubmit = root.findViewById(R.id.button_register_submit);
        registerUserName = root.findViewById(R.id.register_username);
        domainTextView = root.findViewById(R.id.domain);
        domainTextView.setText("@"+DefaultServer.domain);
        registerPassWord = root.findViewById(R.id.register_password);

        userNameView = root.findViewById(R.id.username);
        passWordView = root.findViewById(R.id.password);
        mailCompany = root.findViewById(R.id.mailCompany);
        if(!isOurMailAccount){
            buttonChangeMail.setText("HNU邮箱");
            mailCompany.setText("其它邮箱登录");
        }
        loginLayout = root.findViewById(R.id.login);
        registerLayout = root.findViewById(R.id.register);
        optionLayout = root.findViewById(R.id.layout_option);
        buttonLogin = root.findViewById(R.id.button_login);
        buttonManager = root.findViewById(R.id.button_manager);
        buttonUser = root.findViewById(R.id.button_user);
    }

    private void initListener(){
        userNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Matcher matcher = mailPattern.matcher(s);
                if (matcher.find()){
                    if(!canLogin){
                        buttonSubmit.setBackgroundColor(Color.parseColor("#4c98f2"));
                        canLogin = true;
                    }
                }else{
                    if(canLogin) {
                        canLogin = false;
                        buttonSubmit.setBackgroundColor(Color.parseColor("#90CAF9"));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
//                completeWords[0] = s.toString() + "@"+DefaultServer.domain;
//                adapter.notifyDataSetChanged();
            }
        });

        passWordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    submit();
                }
                return false;
            }
        });
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
                InputMethodManager manager =
                        ((InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE));
                if(manager!=null)
                    manager.hideSoftInputFromWindow(v.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        buttonChangeMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOurMailAccount){
                    buttonChangeMail.setText("HNU邮箱");
                    isOurMailAccount = false;
                    mailCompany.setText("其它邮箱");
                    optionLayout.setVisibility(View.GONE);
                }else{
                    buttonChangeMail.setText("其它邮箱");
                    isOurMailAccount = true;
                    mailCompany.setText("HNU校园邮箱");
                    optionLayout.setVisibility(View.VISIBLE);
                }
                InputMethodManager manager =
                        ((InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE));
                if(manager!=null)
                    manager.hideSoftInputFromWindow(v.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager manager =
                        ((InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE));
                if(manager!=null)
                    manager.hideSoftInputFromWindow(v.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonChangeMail.setVisibility(View.GONE);
                loginLayout.setVisibility(View.GONE);
                registerLayout.setVisibility(View.VISIBLE);
                InputMethodManager manager =
                        ((InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE));
                if(manager!=null)
                    manager.hideSoftInputFromWindow(v.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        registerUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Matcher matcher = mailPattern.matcher(s);
                if (s.length()>0){
                    if(!canRegister){
                        buttonRegisterSubmit.setBackgroundColor(Color.parseColor("#4c98f2"));
                        canRegister = true;
                    }
                }else{
                    if(canRegister) {
                        canRegister = false;
                        buttonRegisterSubmit.setBackgroundColor(Color.parseColor("#90CAF9"));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        buttonRegisterSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager manager =
                        ((InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE));
                if(manager!=null)
                    manager.hideSoftInputFromWindow(v.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                if(canRegister){
                    String username = registerUserName.getText().toString() + domainTextView.getText().toString();
                    String password = registerPassWord.getText().toString();
                    System.out.println(username + " " + password);
                    if(password!=null){
                        FormBody.Builder builder = new FormBody.Builder();
                        builder.add("username",username);
                        builder.add("password", password);
                        FormBody body = builder.build();
                        HttpUtil.httpPost(DefaultServer.ADMIN_HOST+"/user", body, new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(),"网络错误",Toast.LENGTH_SHORT);
                                    }
                                });
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    if(jsonObject.getInt("status") == 200){
                                        makeToast("注册成功并登录");
                                        successLogin(username,password,0);
                                    }else{
                                        makeToast("注册失败");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonChangeMail.setVisibility(View.VISIBLE);
                loginLayout.setVisibility(View.VISIBLE);
                registerLayout.setVisibility(View.GONE);
            }
        });

        buttonManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mailCompany.setText("管理员登录");
                buttonUser.setVisibility(View.VISIBLE);
                buttonManager.setVisibility(View.GONE);
                buttonChangeMail.setVisibility(View.GONE);
            }
        });

        buttonUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mailCompany.setText("HNU校园邮箱");
                buttonManager.setVisibility(View.VISIBLE);
                buttonUser.setVisibility(View.GONE);
                buttonChangeMail.setVisibility(View.VISIBLE);
            }
        });
    }

    private void submit(){
        if(canLogin){
            if(isOurMailAccount){
                if (buttonUser.getVisibility()==View.VISIBLE){
                    //管理员登录
                    // TODO: 2020-05-08 这里得加上Http验证管理员登录
                    FormBody.Builder builder = new FormBody.Builder();
                    builder.add("username",userNameView.getText().toString());
                    builder.add("password", passWordView.getText().toString());
                    FormBody body = builder.build();
                    HttpUtil.httpPost(DefaultServer.ADMIN_HOST + "/login", body, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            makeToast("网络错误");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().string());
                                System.out.println(jsonObject);
                                if(jsonObject.getInt("status") == 200){
                                    Headers headers = response.headers();
                                    try {
                                        List cookies = headers.values("Set-Cookie");
                                        String session = cookies.get(0).toString();
                                        String sessionid = session.substring(0, session.indexOf(";"));
                                        UserInfo.sessionId = sessionid;
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    successLogin(userNameView.getText().toString(),
                                            passWordView.getText().toString(),1);
                                    ManageFragment manageFragment = new ManageFragment();
                                    manageFragment.setType(1);
                                    FragmentTransaction transaction = getActivity().
                                            getSupportFragmentManager().beginTransaction();
                                    transaction.replace(R.id.fragment,manageFragment);
                                    transaction.addToBackStack(getActivity().getClass().getName());
                                    transaction.commit();
                                }else{
                                    makeToast("用户名或密码错误");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }else {
                    //普通用户登录
                    login(userNameView.getText().toString(), passWordView.getText().toString());
                }
            }else{
                ServerFragment serverFragment = new ServerFragment();
                serverFragment.initUser(userNameView.getText().toString(),passWordView.getText().toString());
                FragmentTransaction transaction = getActivity().
                        getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment,serverFragment);
                transaction.addToBackStack(getActivity().getClass().getName());
                transaction.commit();
            }
        }
    }

    public void makeToast(String msg){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void login(String userName,String passWord){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    pop3LoginTest.connect(DefaultServer.POP_HOST,DefaultServer.POP_PORT);
                    smtpLoginTest.connect(DefaultServer.SMTP_HOST,DefaultServer.SMTP_PORT);
                    if(pop3LoginTest.isConnected()){
                        if(!pop3LoginTest.login(userName,passWord)){
                            makeToast("POP3用户名或密码错误");
                            return;
                        }
                    }else {
                        makeToast("无法连接上POP3服务器");
                        return;
                    }

                    if(smtpLoginTest.isConnected()){
                        if(!smtpLoginTest.authLogin(userName,passWord)){
                            makeToast("SMTP用户名或密码错误");
                            return;
                        }
                    }else {
                        makeToast("无法连接上SMTP服务器");
                        return;
                    }
                    makeToast("配置成功");
                    // TODO: 2020/4/23 此处要将user存入userDao持久化数据，并写入UserInfo
                    successLogin(userName,passWord,0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void successLogin(String userName,String passWord,int userType){
        User user = new User();
        user.setUserId(new Random().nextLong());
        user.setUserName(userName);
        user.setNickName(userName.substring(0,userName.indexOf("@")));
        user.setPopHost(DefaultServer.POP_HOST);
        user.setPopUserName(userName);
        user.setPopPassword(passWord);
        user.setPopPort(DefaultServer.POP_PORT);
        user.setSmtpHost(DefaultServer.SMTP_HOST);
        user.setSmtpUserName(userName);
        user.setSmtpPassword(passWord);
        user.setSmtpPort(DefaultServer.SMTP_PORT);
        user.setUserType(userType);
        userDataSource.addUser(user, new UserDataSource.UpdateCallBack() {
            @Override
            public void onUpdateSuccess() {
                UserInfo.users.add(user);
                getActivity().getSupportFragmentManager().popBackStack();
            }

            @Override
            public void onUpdateFailure() {

            }
        });
    }
}
