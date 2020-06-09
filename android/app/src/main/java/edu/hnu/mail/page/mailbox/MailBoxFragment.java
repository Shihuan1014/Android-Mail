package edu.hnu.mail.page.mailbox;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import edu.hnu.mail.R;
import edu.hnu.mail.constant.MailType;
import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.data.source.local.LocalUserDataSource;
import edu.hnu.mail.data.source.local.RemoteMailBoxDataSource;
import edu.hnu.mail.page.edit.EditMailActivity;
import edu.hnu.mail.page.maillist.MailListActivity;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

public class MailBoxFragment extends Fragment implements MailBoxContract.View{

    //MVP相关
    private MailBoxContract.Presenter presenter;
    //UI相关
    private PtrClassicFrameLayout ptrClassicFrameLayout;
    //顶部
    private TextView topText;
    private ImageView buttonBack;
//    private LinearLayout topLoading;
    private ImageView buttonEdit;
    //邮箱类型
    private LinearLayout inBoxLayout;
    private TextView inboxCount;
//    private LinearLayout groupLayout;
//    private TextView groupCount;
    private LinearLayout flagLayout;
    private TextView flagCount;
    private LinearLayout draftBoxLayout;
    private TextView draftBoxCount;
    private LinearLayout sentBoxLayout;
    private TextView sentBoxCount;
    private LinearLayout deletedBoxLayout;
    private TextView deletedBoxCount;
    //数据相关
    private int[] countList;
    private String userName;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_all_mailbox,container,false);
        presenter = new MailBoxPresenter(this, new RemoteMailBoxDataSource(getContext()),
                new LocalUserDataSource(getContext()));
        userName = UserInfo.getCurrentUser().getPopUserName();
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
        initView(root);
        initListener();
        return root;
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
                try {
                    presenter.initUserMailBox(UserInfo.getCurrentUser().getPopUserName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //初始化顶部的文字(loading或者emailAddress)
        topText = root.findViewById(R.id.top_text);
        topText.setText(userName);
        buttonBack = root.findViewById(R.id.button_back);
//        topLoading = root.findViewById(R.id.top_loading);
        buttonEdit = root.findViewById(R.id.button_edit);
        //初始化各个收件箱控件
        inBoxLayout = root.findViewById(R.id.inbox);
        inboxCount = root.findViewById(R.id.inbox_count);
        flagLayout = root.findViewById(R.id.flag);
        flagCount = root.findViewById(R.id.flag_mail_count);
//        groupLayout = root.findViewById(R.id.group);
//        groupCount = root.findViewById(R.id.group_mail_count);
        draftBoxLayout = root.findViewById(R.id.draft);
        draftBoxCount = root.findViewById(R.id.draft_count);
        sentBoxLayout = root.findViewById(R.id.sent);
        sentBoxCount = root.findViewById(R.id.sent_count);
        deletedBoxLayout = root.findViewById(R.id.deleted);
        deletedBoxCount = root.findViewById(R.id.deleted_count);
    }

    private void initListener(){

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        inBoxLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MailListActivity.class);
                intent.putExtra("type", MailType.INBOX);
                startActivity(intent);
            }
        });
        flagLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MailListActivity.class);
                intent.putExtra("type", MailType.FLAG);
                startActivity(intent);
            }
        });
        draftBoxLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MailListActivity.class);
                intent.putExtra("type",MailType.DRAFT);
                startActivity(intent);
            }
        });
        sentBoxLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MailListActivity.class);
                intent.putExtra("type",MailType.SENT);
                startActivity(intent);
            }
        });
        deletedBoxLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MailListActivity.class);
                intent.putExtra("type",MailType.DELETED);
                startActivity(intent);
            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), EditMailActivity.class);
                startActivity(intent);
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
        try {
            ptrClassicFrameLayout.refreshComplete();
//            topLoading.setVisibility(View.GONE);
            topText.setVisibility(View.VISIBLE);
            countList = mailCountList;
            inboxCount.setText(String.valueOf(mailCountList[0]));
            flagCount.setText(String.valueOf(mailCountList[1]));
//            groupCount.setText(String.valueOf(mailCountList[2]));
            draftBoxCount.setText(String.valueOf(mailCountList[3]));
            sentBoxCount.setText(String.valueOf(mailCountList[4]));
            deletedBoxCount.setText(String.valueOf(mailCountList[5]));
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 加上最新获得的邮件，显示新的邮件数量
     * @param newMailCountList
     */
    @Override
    public void showNewMailCountList(int[] newMailCountList) {

    }

    @Override
    public void showUserMailData() {

    }

    /**
     * 显示加载错误
     */
    @Override
    public void showLoadError() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "加载错误", Toast.LENGTH_SHORT).show();
                ptrClassicFrameLayout.refreshComplete();
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

    }


    /**
     * Fragment生命周期开始时, 通知Presenter的Start()开始初始化数据
     */
    @Override
    public void onResume() {
        super.onResume();
        System.out.println("MailBoxFragment出现");
        presenter.initUserMailBox(UserInfo.getCurrentUser().getPopUserName());
    }
}
