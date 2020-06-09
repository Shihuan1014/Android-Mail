package edu.hnu.mail.page.manager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import edu.hnu.mail.R;
import edu.hnu.mail.constant.DefaultServer;
import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.data.entity.Contact;
import edu.hnu.mail.page.edit.EditMailActivity;
import edu.hnu.mail.page.login.LoginFragment;
import edu.hnu.mail.ui.mails.OnItemClickListener;
import edu.hnu.mail.util.CustomDialog;
import edu.hnu.mail.util.HttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.dyuproject.protostuff.CollectionSchema.MessageFactories.List;

public class ManageFragment extends Fragment {

    private ImageView buttonBack;
    private RecyclerView userListView;
    private UserListAdapter userListAdapter;
    private LinearLayout layoutLog;
    private LinearLayout layoutGroup;
    private LinearLayout layoutServer;
    private ImageView buttonAddUser;
    private Switch popSwitch;
    private Switch smtpSwitch;
    private AlertDialog dialog;
    private boolean needInitData = true;
    private boolean smtpStatus = false;
    private boolean popStatus = false;
    private AlertDialog loadingDialog;
    private AddUserDialog addUserDialog;
    private String receiver = "";
    private int type = 0;
    private Handler handler;

    public void setType(int type){
        this.type = type;
    }


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_manager,container,false);
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
        handler = new Handler();
        initView(root);
        showLoading();
        if(type == 0 || UserInfo.sessionId == null){
            login();
        }else {
            initData();
        }
        initListener();
//        initData();
        return root;
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

    private void showAddUser(){
        if(addUserDialog == null){
            addUserDialog = new AddUserDialog(getContext());
            addUserDialog.setOnClickListener(new AddUserDialog.OnClickListener() {
                @Override
                public void cancel() {
                    addUserDialog.dismiss();
                }


                @Override
                public void confirm(String username,String password) {
                    if(password.trim().length()>0 && username.trim().length()>0){
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
                                        makeToast("添加用户成功");
                                        User user = new User();
                                        user.setUserName(username);
                                        user.setPassword(password);
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                userListAdapter.addUser(user);
                                                addUserDialog.dismiss();
                                            }
                                        });
                                    }else{
                                        makeToast("添加用户失败");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }else{
                        makeToast("用户名或密码不能为空");
                    }
                }
            });
        }
        addUserDialog.show();
    }

    private void initView(View root) {
        buttonBack = root.findViewById(R.id.button_back);
        userListView = root.findViewById(R.id.user_list);
        userListAdapter = new UserListAdapter();
        userListView.setAdapter(userListAdapter);
        userListView.setLayoutManager(new LinearLayoutManager(getContext()));
        userListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // TODO: 2020-05-20 弹出对话框，有删除用户功能
                showOptionDialog(position);
            }
        });
        popSwitch = root.findViewById(R.id.pop_run_state_switch);
        smtpSwitch = root.findViewById(R.id.smtp_run_state_switch);
        layoutLog = root.findViewById(R.id.layout_log);
        layoutGroup = root.findViewById(R.id.layout_group);
        layoutServer = root.findViewById(R.id.layout_server);
        buttonAddUser = root.findViewById(R.id.add_user);
    }

    private void login(){
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("username",UserInfo.getCurrentUser().getUserName());
        builder.add("password", UserInfo.getCurrentUser().getPopPassword());
        FormBody body = builder.build();
        HttpUtil.httpPost(DefaultServer.ADMIN_HOST + "/login", body, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    makeToast("网络错误");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialog.dismiss();
                        }
                    },500);
                }catch (Exception ee){
                    ee.printStackTrace();
                }
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
                        initData();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void initListener(){
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        smtpSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !smtpStatus) {
                    HttpUtil.httpGet(DefaultServer.ADMIN_HOST + "/SmtpServerRun", new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            makeToast("开启失败，请检查网络连接");
                            smtpSwitch.setChecked(false);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            makeToast("成功开启SMTP服务器");
                            smtpStatus = true;
                        }
                    });
                } else if (!isChecked && smtpStatus){
                    HttpUtil.httpGet(DefaultServer.ADMIN_HOST + "/SmtpServerStop", new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            makeToast("关闭失败，请检查网络连接");
                            smtpSwitch.setChecked(true);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            makeToast("成功关闭SMTP服务器");
                            smtpStatus = false;
                        }
                    });
                }
            }
        });

        popSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !popStatus) {
                    HttpUtil.httpGet(DefaultServer.ADMIN_HOST + "/PopServerRun", new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            makeToast("开启失败，请检查网络连接");
                            popSwitch.setChecked(false);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            makeToast("成功开启POP服务器");
                            popStatus = true;
                        }
                    });
                } else if (!isChecked && popStatus) {
                    HttpUtil.httpGet(DefaultServer.ADMIN_HOST + "/PopServerStop", new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            makeToast("关闭失败，请检查网络连接");
                            popSwitch.setChecked(true);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            makeToast("成功关闭POP服务器");
                            popStatus = false;
                        }
                    });
                }
            }
        });

        layoutLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().
                        beginTransaction();
                transaction.replace(R.id.fragment,new LogInfoFragment());
                transaction.addToBackStack(getActivity().getClass().getName());
                transaction.commit();
            }
        });

        layoutGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), EditMailActivity.class);
                intent.putExtra("isGroup",true);
                getActivity().startActivity(intent);
            }
        });

        layoutServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                ServerFragment serverFragment = new ServerFragment();
                fragmentTransaction.replace(R.id.fragment,serverFragment);
                fragmentTransaction.addToBackStack(serverFragment.getClass().getName());
                fragmentTransaction.commit();
            }
        });

        buttonAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUser();
            }
        });
    }

    private void initData(){
        HttpUtil.httpGet(DefaultServer.ADMIN_HOST + "/userlist", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(getContext(),"网络错误",Toast.LENGTH_SHORT).show();
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
                    System.out.println(jsonObject);
                    if (jsonObject.getInt("status") == 200){
                        Gson gson = new Gson();
                        List<User> users = gson.fromJson(jsonObject.getString("userlist"),
                                new TypeToken<List<User>>(){}.getType());
                        for (User user : users){
                            receiver += user.getUserName() + ";";
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                userListAdapter.setUserList(users);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismiss();
                    }
                },500);
            }
        });

        HttpUtil.httpGet(DefaultServer.ADMIN_HOST + "/getPopStatus", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.body().string().equalsIgnoreCase("true")){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            popStatus = true;
                            popSwitch.setChecked(true);
                        }
                    });
                }else{
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            popStatus = false;
                            popSwitch.setChecked(false);
                        }
                    });
                }
            }
        });

        HttpUtil.httpGet(DefaultServer.ADMIN_HOST + "/getSmtpStatus", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.body().string().equalsIgnoreCase("true")){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            smtpStatus = true;
                            smtpSwitch.setChecked(true);
                        }
                    });
                }else{
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            smtpStatus = false;
                            smtpSwitch.setChecked(false);
                        }
                    });
                }
            }
        });
    }

    private void showOptionDialog(int position){
        if(dialog==null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.dialog_user_manage, null);
            LinearLayout buttonDelete = view.findViewById(R.id.button_delete);
            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OkHttpClient client = new OkHttpClient();
                    Request.Builder builder = new Request.Builder()
                            .url(DefaultServer.ADMIN_HOST+"/user?userId="+
                                    userListAdapter.getUser(position).getId()).delete();
                    if(UserInfo.sessionId!=null){
                        builder.addHeader("cookie", UserInfo.sessionId);
                    }
                    Request request = builder.build();
                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            makeToast("网络错误");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().string());
                                if(jsonObject.getInt("status") == 200){
                                    makeToast("删除成功");
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            userListAdapter.deleteUser(position);
                                        }
                                    });
                                }else{
                                    makeToast("服务器故障");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    dialog.dismiss();
                }
            });
// 加载自定义的布局文件
            LinearLayout buttonWrite = view.findViewById(R.id.button_write);
            buttonWrite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), EditMailActivity.class);
                    intent.putExtra("receiver",userListAdapter.getUser(position).getUserName());
                    getContext().startActivity(intent);
                    dialog.dismiss();
                }
            });
            dialog = new AlertDialog.Builder(getContext()).create();
            TextView abandonText = view.findViewById(R.id.abandon_text);
            int i = userListAdapter.getUser(position).getAuthor();
            LinearLayout buttonAbandon = view.findViewById(R.id.button_abandon);
            if (i == 0){
                abandonText.setText("禁用账户");
                buttonAbandon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HttpUtil.httpGet(DefaultServer.ADMIN_HOST + "/changeAuthor?userAddress="+
                                userListAdapter.getUser(position).getUserName()+"&author=-1", new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                                makeToast("网络错误");
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    if (jsonObject.getInt("status") == 200){
                                        userListAdapter.getUser(position).setAuthor(-1);
                                        makeToast("禁用成功");
                                    }else{
                                        makeToast("禁用失败");
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                    makeToast("禁用失败");
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                });
            }else{
                abandonText.setText("取消禁用");
                buttonAbandon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HttpUtil.httpGet(DefaultServer.ADMIN_HOST + "/changeAuthor?userAddress="+
                                userListAdapter.getUser(position).getUserName()+"&author=0", new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                                makeToast("网络错误");
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    if (jsonObject.getInt("status") == 200){
                                        userListAdapter.getUser(position).setAuthor(0);
                                        makeToast("取消成功");
                                    }else{
                                        makeToast("取消失败");
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                    makeToast("取消失败");
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                });
            }
            dialog.show();
            Window window = dialog.getWindow();
            WindowManager windowManager = getActivity().getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = display.getWidth();
            window.setAttributes(lp);
            window.setContentView(view);
            window.setGravity(Gravity.BOTTOM);  //此处可以设置dialog显示的位置
            window.setWindowAnimations(R.style.bottomDialogAnim);  //添加动画
        }else {
            LinearLayout buttonAbandon = dialog.findViewById(R.id.button_abandon);
            TextView abandonText = dialog.findViewById(R.id.abandon_text);
            int i = userListAdapter.getUser(position).getAuthor();
            if (i == 0){
                abandonText.setText("禁用账户");
                buttonAbandon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HttpUtil.httpGet(DefaultServer.ADMIN_HOST + "/changeAuthor?userAddress="+
                                userListAdapter.getUser(position).getUserName()+"&author=-1", new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                                makeToast("网络错误");
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    if (jsonObject.getInt("status") == 200){
                                        userListAdapter.getUser(position).setAuthor(-1);
                                        makeToast("禁用成功");
                                    }else{
                                        makeToast("禁用失败");
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                    makeToast("禁用失败");
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                });
            }else{
                abandonText.setText("取消禁用");
                buttonAbandon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HttpUtil.httpGet(DefaultServer.ADMIN_HOST + "/changeAuthor?userAddress="+
                                userListAdapter.getUser(position).getUserName()+"&author=0", new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                                makeToast("网络错误");
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    if (jsonObject.getInt("status") == 200){
                                        userListAdapter.getUser(position).setAuthor(0);
                                        makeToast("取消成功");
                                    }else{
                                        makeToast("取消失败");
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                    makeToast("取消失败");
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                });
            }

            dialog.show();
        }
    }

    private void makeToast(String msg){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT).show();
            }
        });
    }

//    @Override
//    public void onResume(){
//        super.onResume();
//        isPopInit = false;
//        isSmtpInit = false;
//    }
}
