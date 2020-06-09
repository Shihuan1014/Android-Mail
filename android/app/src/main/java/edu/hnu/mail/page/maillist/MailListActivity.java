package edu.hnu.mail.page.maillist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import edu.hnu.mail.R;
import edu.hnu.mail.constant.DefaultServer;
import edu.hnu.mail.constant.MailType;
import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.data.entity.Mail;
import edu.hnu.mail.data.entity.User;
import edu.hnu.mail.data.source.local.LocalMailDataSource;
import edu.hnu.mail.data.source.local.LocalMailListDataSource;
import edu.hnu.mail.page.edit.EditMailActivity;
import edu.hnu.mail.page.mail.MailActivity;
import edu.hnu.mail.ui.mails.MailListAdapter;
import edu.hnu.mail.ui.mails.OnItemClickListener;
import edu.hnu.mail.util.CustomDialog;
import edu.hnu.mail.util.HttpUtil;
import edu.hnu.mail.util.StatusBarUtil;
import edu.hnu.mail.util.TouchPtrHandler;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

/**
 * 该界面是第一个界面，为邮箱列表(收件箱、已发送、已删除、垃圾箱、草稿箱)
 * author: 周世焕
 */
public class MailListActivity extends AppCompatActivity implements MailListContract.View{

    private MailListContract.View view;
//    private MailListFragment mailListFragment;
    private Logger logger = Logger.getLogger("MailListFragment");
    private boolean needLoadData = true;
    //MVP相关
    private MailListContract.Presenter presenter;
    //UI相关
    //刷新组件
    private PtrClassicFrameLayout ptrClassicFrameLayout;
    private TouchPtrHandler touchPtrHandler;
    //顶部
    private TextView mailBoxName;
    private TextView mailCount;
    private ImageView buttonBack;
    private ImageView buttonEdit;
    //列表
    private RecyclerView mailListView;
    private MailListAdapter mailListAdapter;
    //加载框
    private LinearLayout layoutLoading;
    private ProgressBar loadingAnim;
    private TextView noMail;
    //数据相关
    private int count = 0;
    private int clickItemIndex = 0;
    // 哪一种邮箱的邮件列表
    private int mailType = MailType.INBOX;
    //广播接收
    private MyBroadCastReceiver broadCastReceiver;
    //滚动优化
    private static final float minXlength = 10;
    private float startX, startY;
    /** 是锁定状态 */
    private boolean isLockStatus = false;
    /** 是否可下拉（纵向下拉距离超过横向滑动距离且纵向下拉超过一定距离才判断为下拉状态，否则判定位滑动状态） */
    private boolean disableScroll = false;
    private CustomDialog refuseDialog;

    @Override
    protected void onCreate(Bundle saveInstance){
        super.onCreate(saveInstance);
        setContentView(R.layout.fragment_mail_list);
        overridePendingTransition(R.anim.activity_in_right,R.anim.activity_stay);
        Intent intent = getIntent();
        int type = intent.getIntExtra("type",1);
        mailType = type;
        StatusBarUtil.whiteBgAndBlackFont(getWindow());
        broadCastReceiver = new MyBroadCastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("setFlag");
        intentFilter.addAction("setSeen");
        intentFilter.addAction("deleteItem");
        registerReceiver(broadCastReceiver,intentFilter);
        initView();
        initListener();
    }


    private void initView(){
        //初始化下拉刷新控件
        ptrClassicFrameLayout = findViewById(R.id.ptr_frame);
        ptrClassicFrameLayout.disableWhenHorizontalMove(true);
        touchPtrHandler = new TouchPtrHandler() {
//            @Override
//            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
//                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
//            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                //调用presenter获取数据
                try {
                    presenter.getNewMail();
                }catch (Exception e){
                    ptrClassicFrameLayout.refreshComplete();
                }
            }
        };
        ptrClassicFrameLayout.setPtrHandler(touchPtrHandler);

        //初始化顶部的文字(loading或者emailAddress)
        mailBoxName = findViewById(R.id.mailbox_name);
        mailCount = findViewById(R.id.mail_count);
        mailCount.setText("(" + String.valueOf(count) + ")");
        switch (mailType){
            case MailType.INBOX:
                mailBoxName.setText(UserInfo.getCurrentUser().getPopUserName()+"的收件箱");
                break;
            case MailType.DRAFT:
                mailBoxName.setText("草稿箱");
                break;
            case MailType.FLAG:
                mailBoxName.setText("星标邮件");
                break;
            case MailType.SENT:
                mailBoxName.setText("已发送");
                break;
            case MailType.DELETED:
                mailBoxName.setText("已删除");
                break;
            case MailType.ALL_INBOX:
                mailBoxName.setText("收件箱");
                break;
        }
        buttonBack = findViewById(R.id.button_back);
        buttonEdit = findViewById(R.id.button_edit);
        //初始化邮件列表
        mailListView = findViewById(R.id.mail_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mailListView.getContext());
        mailListView.setLayoutManager(linearLayoutManager);
        mailListAdapter = new MailListAdapter(mailType);
        //设置item点击
        mailListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                clickItemIndex = position;
                Intent intent = new Intent(MailListActivity.this, MailActivity.class);
                intent.putExtra("mailId",mailListAdapter.getMail(position).getUid());
                intent.putExtra("mailType",mailType);
                intent.putExtra("mailNo",position);
                intent.putExtra("mailBox",mailListAdapter.getMail(position).getMailBox());
                intent.putExtra("userName",mailListAdapter.getMail(position).getUserAddress());
                startActivity(intent);
            }
        });
        //设置侧边划出菜单点击事件
        mailListAdapter.setOnSideClickListener(new MailListAdapter.OnSideClickListener() {
            @Override
            public void seenClick(int position,Mail mail) {
                presenter.setSeenMail(position,mail);
            }

            @Override
            public void refuseClick(int position,Mail mail) {
                if (refuseDialog == null){
                    refuseDialog = new CustomDialog(MailListActivity.this);
                    refuseDialog.setButtonConfirmText("确认拒收");
                    refuseDialog.setTitle("拒收");
                    refuseDialog.setSubTitle("拒收后，你将不再收到来自"+mail.getFromEmail()+"的邮件");
                    refuseDialog.setOnClickListener(new CustomDialog.OnClickListener() {
                        @Override
                        public void cancel() {
                            refuseDialog.dismiss();
                        }

                        @Override
                        public void save() {

                        }

                        @Override
                        public void confirm() {
                            FormBody.Builder builder = new FormBody.Builder();
                            builder.add("username",mail.getToEmail());
                            String pass = null;
                            for (User user : UserInfo.users){
                                if (user.getUserName().equalsIgnoreCase(mail.getToEmail())){
                                    pass = user.getPopPassword();
                                    break;
                                }
                            }
                            builder.add("password",pass);
                            builder.add("blockAddress",mail.getFromEmail());
                            HttpUtil.httpPost(DefaultServer.ADMIN_HOST + "/block", builder.build(),
                                    new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    e.printStackTrace();
                                    makeToast("网络错误");
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    try {
                                        JSONObject jsonObject = new JSONObject(response.body().string());
                                        if (jsonObject.getInt("status") == 200 ||
                                                jsonObject.getInt("status") == 400){
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(MailListActivity.this,
                                                            "拒收成功",Toast.LENGTH_SHORT).show();
                                                    presenter.deleteMail(position,mail);
                                                }
                                            });
                                            return;
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    makeToast("拒收失败，可能已经拒收");
                                }
                            });
                            refuseDialog.dismiss();
                        }
                    });
                }
                refuseDialog.show();
            }

            @Override
            public void deleteClick(int position,Mail mail) {
                presenter.deleteMail(position,mail);
            }
        });
        mailListView.setAdapter(mailListAdapter);
        mailListView.setNestedScrollingEnabled(true);
        //加载中
        layoutLoading = findViewById(R.id.loading);
        loadingAnim = findViewById(R.id.loading_anim);
        noMail = findViewById(R.id.no_mail);
    }

    private void makeToast(String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MailListActivity.this,msg,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initListener(){
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MailListActivity.this, EditMailActivity.class);
                startActivity(intent);
            }
        });
    }

    public void updateMail(int function){

    }
    /**
     * 获得Presenter, 之后可实现View -> Presenter的调用
     * @param presenter
     */
    @Override
    public void setPresenter(MailListContract.Presenter presenter) {
        this.presenter = presenter;
    }

    /**
     * 初始数据显示，直接赋值给Adapter的list进行渲染。并重新渲染
     * @param list
     */
    @Override
    public void showMailList(List<Mail> list) {
        if(list!=null && list.size() != 0){
            mailListAdapter.setMailList(list);
            mailListAdapter.notifyDataSetChanged();
            layoutLoading.setVisibility(View.GONE);
            count = list.size();
        }else{
            layoutLoading.setVisibility(View.VISIBLE);
            loadingAnim.setVisibility(View.GONE);
            noMail.setVisibility(View.VISIBLE);
            count = 0;
        }
        mailCount.setText("(" + String.valueOf(count) + ")");
    }

    /**
     * 新增数据显示，在Adapter的list前面插入这些新数据。并重新渲染
     * @param list
     */
    @Override
    public void showNewMailList(List<Mail> list) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(list!=null && list.size()>0){
                    for (Mail mail:list){
                        logger.info(mail.toString());
                    }
                    mailListAdapter.addMailList(list);
                    count += list.size();
                    mailCount.setText("(" + String.valueOf(count) + ")");
                    mailListView.smoothScrollToPosition(0);
                }
                ptrClassicFrameLayout.refreshComplete();
            }
        });
    }

    /**
     * 显示加载错误
     */
    @Override
    public void showLoadError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layoutLoading.setVisibility(View.GONE);
                loadingAnim.setVisibility(View.GONE);
                System.out.println("here");
                Toast.makeText(MailListActivity.this,"获取邮件时发生错误，请检查网络",
                        Toast.LENGTH_SHORT).show();
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
    public void removeMail(int position) {
        mailCount.setText("(" + String.valueOf(--count) + ")");
        mailListAdapter.removeMail(position);
    }

    @Override
    public void changeSeen(int position, int seen) {
        mailListAdapter.setSeen(position,seen);
    }


    /**
     * Fragment生命周期开始时, 通知Presenter的Start()开始初始化数据
     */
    @Override
    public void onResume() {
        super.onResume();
        System.out.println("OnResume");
        if(needLoadData){
            presenter = new MailListPresenter(this,
                    new LocalMailListDataSource(MailListActivity.this),
                    new LocalMailDataSource(MailListActivity.this),mailType);
            presenter.start();
            needLoadData = false;
            System.out.println("加载数据");
        }else{
            try {
                presenter.getNewMailLocal(mailListAdapter.getMail(0).getSendTime());
            }catch (Exception e){
                //可能会出现错误，比如maillistAdapter的maillist空了，被删了
                e.printStackTrace();
            }
        }
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isLockStatus) {
                    float xGap = Math.abs(event.getX() - startX);
                    float yGap = Math.abs(event.getY() - startY);
                    if (xGap > yGap && xGap > minXlength){
                        disableScroll = false;
                        isLockStatus = true;
                    } else if (yGap > xGap && yGap >minXlength) {
                        disableScroll = true;
                        isLockStatus = true;
                    }
                }
                if (touchPtrHandler != null) {
                    touchPtrHandler.setCanScrollUp(disableScroll);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            default:
                isLockStatus = false;
                disableScroll = false;
                if (touchPtrHandler != null) touchPtrHandler.setCanScrollUp(true);
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    public class MyBroadCastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int position = intent.getIntExtra("position",1);
            int argv = intent.getIntExtra("argv",0);
            switch (action){
                case "setFlag":
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mailListAdapter.setFlag(position,argv);
                        }
                    });
                    break;
                case "setSeen":
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mailListAdapter.setSeen(position,argv);
                        }
                    });
                    break;
                case "deleteItem":
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mailCount.setText("(" + String.valueOf(--count) + ")");
                            mailListAdapter.removeMail(position);
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(broadCastReceiver);
    }
}
