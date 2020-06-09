package edu.hnu.mail.page.mailbox;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;

import edu.hnu.mail.R;
import edu.hnu.mail.constant.DefaultServer;
import edu.hnu.mail.constant.MailType;
import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.data.entity.User;
import edu.hnu.mail.data.source.local.LocalUserDataSource;
import edu.hnu.mail.data.source.local.RemoteMailBoxDataSource;
import edu.hnu.mail.page.MainActivity;
import edu.hnu.mail.page.contact.ContactFragment;
import edu.hnu.mail.page.login.LoginFragment;
import edu.hnu.mail.page.login.ServerFragment;
import edu.hnu.mail.page.maillist.MailListActivity;
import edu.hnu.mail.page.manager.ManageFragment;
import edu.hnu.mail.ui.mails.OnItemClickListener;
import edu.hnu.mail.ui.mails.UserInboxAdapter;
import edu.hnu.mail.util.HttpUtil;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * Fragment比Activity更加灵活
 */
public class MainFragment extends Fragment implements MailBoxContract.View {

    //MVP相关
        private MailBoxContract.Presenter presenter;
    //UI相关
        private PtrClassicFrameLayout ptrClassicFrameLayout;
        //顶部
        private TextView topText;
        private LinearLayout topLoading;
//        private ImageView buttonCreate;
        //邮箱类型
        private LinearLayout inBoxLayout;
        private TextView inboxCount;
        private RecyclerView userInboxRecycleView;
        private LinearLayout flagLayout;
        private TextView flagCount;
        private UserInboxAdapter userInboxAdapter;
        //通讯录
        private LinearLayout contactLayout;
        //账户
        private RecyclerView accountRecycleView;
        private UserInboxAdapter accountAdapter;
        //Floating Button
        private FloatingActionMenu actionMenu;
        private FloatingActionButton buttonEdit;
        private FloatingActionButton buttonSetting;
//        private LinearLayout draftBoxLayout;
//        private TextView draftBoxCount;
//        private LinearLayout sentBoxLayout;
//        private TextView sentBoxCount;
//        private LinearLayout deletedBoxLayout;
//        private TextView deletedBoxCount;
    //数据相关
        private int totalCount = 0;
        private int totalFlagCount = 0;
        private int[] countList;

    //通知Activity
    private MainActivity.FragmentAction fragmentAction;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main,container,false);
        presenter = new MailBoxPresenter(this, new RemoteMailBoxDataSource(getContext()),
                new LocalUserDataSource(getContext()));
        initView(root);
        initListener();
        return root;
    }

    public void setFragmentActionListener(MainActivity.FragmentAction fragmentAction){
        this.fragmentAction = fragmentAction;
    }

    private void initView(View root){
        //初始化下拉刷新控件
        ptrClassicFrameLayout = root.findViewById(R.id.ptr_frame);
        ptrClassicFrameLayout.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return true;
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                //调用presenter获取数据
                presenter.getNewMailCount();
            }
        });
        //初始化顶部的文字(loading或者emailAddress)
        topText = root.findViewById(R.id.top_text);
        topText.setText("邮箱");
//        if(UserInfo.users!=null && UserInfo.users.size()>0){
//            String userName = UserInfo.users.get(0).getPopUserName();
//            topText.setText(userName);
//        }
        topLoading = root.findViewById(R.id.top_loading);
//        buttonCreate = root.findViewById(R.id.button_create);
        //初始化各个收件箱控件
        inBoxLayout = root.findViewById(R.id.inbox);
        inboxCount = root.findViewById(R.id.inbox_count);
        flagLayout = root.findViewById(R.id.flag_mail);
        flagCount = root.findViewById(R.id.flag_mail_count);
        userInboxRecycleView = root.findViewById(R.id.user_inbox);
        userInboxAdapter = new UserInboxAdapter();
        userInboxRecycleView.setAdapter(userInboxAdapter);
        LinearLayoutManager tmp = new LinearLayoutManager(getContext());
        userInboxRecycleView.setLayoutManager(tmp);
        accountRecycleView = root.findViewById(R.id.user_account);
        accountAdapter = new UserInboxAdapter();
        accountAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // TODO: 2020/4/28 前往查看所有邮箱种类 
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment,new MailBoxFragment());
                transaction.addToBackStack(getActivity().getClass().getName());
                fragmentAction.hideMenu();
                UserInfo.currentIndex = position;
                User user = UserInfo.getCurrentUser();
                if (user.getUserType() == 1){
                    FormBody.Builder builder = new FormBody.Builder();
                    builder.add("username",user.getUserName());
                    builder.add("password", user.getPopPassword());
                    FormBody body = builder.build();
                    HttpUtil.httpPost(DefaultServer.ADMIN_HOST + "/login", body, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().string());
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
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                transaction.commit();
            }
        });
        accountAdapter.setInboxType(false);
        accountRecycleView.setAdapter(accountAdapter);
        LinearLayoutManager tmp2 = new LinearLayoutManager(getContext());
        accountRecycleView.setLayoutManager(tmp2);

        //通讯录
        contactLayout = root.findViewById(R.id.contact);
    }

    private void initListener(){
        inBoxLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MailListActivity.class);
                intent.putExtra("type", MailType.ALL_INBOX);
                inboxCount.setTextColor(Color.parseColor("#8e8e8e"));
                TextPaint tp = inboxCount.getPaint();
                tp.setFakeBoldText(false);
                startActivity(intent);
            }
        });
        flagLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MailListActivity.class);
                intent.putExtra("type", MailType.ALL_FLAG);
                startActivity(intent);
            }
        });
        userInboxAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(getContext(),MailListActivity.class);
                intent.putExtra("type",1);
                UserInfo.currentIndex = position;
                startActivity(intent);
            }
        });

        contactLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().
                        beginTransaction();
                transaction.replace(R.id.fragment,new ContactFragment());
                fragmentAction.hideMenu();
                transaction.addToBackStack(getActivity().getClass().getName());
                transaction.commit();
            }
        });
    }

    /**
     * 获得Presenter, 之后可实现View -> Presenter的调用
     * @param presenter
     */
    @Override
    public void setPresenter(MailBoxContract.Presenter presenter) {
        this.presenter = presenter;
    }

    /**
     * 显示邮箱列表
     * @param mailCountList 各个邮箱的邮件数
     */
    @Override
    public void showMailCountList(int[] mailCountList) {
        inboxCount.setText(String.valueOf(mailCountList[0]));
        flagCount.setText(String.valueOf(mailCountList[1]));
        countList = mailCountList;

    }
    @Override
    public void showUserMailData(){
        accountAdapter.setUserList(UserInfo.users);
        userInboxAdapter.setUserList(UserInfo.users);
    }
    /**
     * 加上最新获得的邮件，显示新的邮件数量
     * @param newMailCountList 一个数组，代表着各个用户的新邮件数量
     */
    @Override
    public void showNewMailCountList(int[] newMailCountList) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ptrClassicFrameLayout.refreshComplete();
                topLoading.setVisibility(View.GONE);
                topText.setVisibility(View.VISIBLE);
                int total = 0;
                //累加起来，加到所有邮件数量
                for (int i = 0 ;i < newMailCountList.length;i ++){
                    total += newMailCountList[i];
                }
                if(total>0){
                    countList[0] += total;
                    inboxCount.setText(String.valueOf(countList[0]));
                    inboxCount.setTextColor(Color.parseColor("#3498DB"));
                    TextPaint tp = inboxCount.getPaint();
                    tp.setFakeBoldText(true);
                }
                try {
                    List<User> userList = UserInfo.users;
                    int l = userList.size();
                    for (int i = 0; i < l;i++) {
                        if (newMailCountList[i] > 0) {
                            userList.get(i).setMailInboxCount(userList.get(i).getMailInboxCount()
                                    + newMailCountList[i]);
                        }
                    }
                    userInboxAdapter.addUserMailCount(newMailCountList);
                    accountAdapter.addUserMailCount(newMailCountList);
                }catch (Exception e){
                    Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 显示加载错误
     */
    @Override
    public void showLoadError() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "加载错误,请检查网络", Toast.LENGTH_SHORT).show();
                ptrClassicFrameLayout.refreshComplete();
                topLoading.setVisibility(View.GONE);
                topText.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * 隐藏加载错误
     */
    @Override
    public void hideLoadError() {

    }

    @Override
    public void showLoginFragment(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().
                        beginTransaction();
                LoginFragment loginFragment = new LoginFragment();
                loginFragment.setIsOnlyAccount(true);
                transaction.replace(R.id.fragment,loginFragment);
                fragmentAction.hideMenu();
                transaction.addToBackStack(getActivity().getClass().getName());
                transaction.commit();
            }
        });
    }


    /**
     * Fragment生命周期开始时, 通知Presenter的Start()开始初始化数据
     */
    @Override
    public void onResume() {
        super.onResume();
        fragmentAction.showMenu();
        System.out.println("MainFragment出现");
        topLoading.setVisibility(View.VISIBLE);
        topText.setVisibility(View.GONE);
        try{
            presenter.start();
        }catch (Exception e){
            topLoading.setVisibility(View.GONE);
            topText.setVisibility(View.VISIBLE);
        }
    }
}
