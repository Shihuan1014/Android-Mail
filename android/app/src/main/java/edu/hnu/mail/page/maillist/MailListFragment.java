package edu.hnu.mail.page.maillist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.logging.Logger;

import edu.hnu.mail.R;
import edu.hnu.mail.constant.MailType;
import edu.hnu.mail.data.entity.Mail;
import edu.hnu.mail.data.source.local.LocalMailListDataSource;
import edu.hnu.mail.page.edit.EditMailActivity;
import edu.hnu.mail.page.mail.MailActivity;
import edu.hnu.mail.ui.mails.MailListAdapter;
import edu.hnu.mail.ui.mails.OnItemClickListener;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * Fragment比Activity更加灵活
 */
//public class MailListFragment extends Fragment implements MailListContract.View {
//
//    private Logger logger = Logger.getLogger("MailListFragment");
//    private boolean needLoadData = true;
//
//    public MailListFragment(int type){
//        this.mailType = type;
//    }
//
//    //MVP相关
//        private MailListContract.Presenter presenter;
//    //UI相关
//        //刷新组件
//        private PtrClassicFrameLayout ptrClassicFrameLayout;
//        //顶部
//        private TextView mailBoxName;
//        private TextView mailCount;
//        private ImageView buttonBack;
//        private ImageView buttonEdit;
//        //列表
//        private RecyclerView mailListView;
//        private MailListAdapter mailListAdapter;
//        //加载框
//        private LinearLayout layoutLoading;
//        private ProgressBar loadingAnim;
//        private TextView noMail;
//    //数据相关
//        private int count = 0;
//        private int clickItemIndex = 0;
//    // 哪一种邮箱的邮件列表
//    private int mailType = MailType.INBOX;
//
//    @Override
//    public View onCreateView(
//            @NonNull LayoutInflater inflater, ViewGroup container,
//            Bundle savedInstanceState) {
//        System.out.println("OnCreateView");
//        View root = inflater.inflate(R.layout.fragment_mail_list,container,false);
//        initView(root);
//        initListener();
//        return root;
//    }
//
//    private void initView(View root){
//        //初始化下拉刷新控件
//        ptrClassicFrameLayout = root.findViewById(R.id.ptr_frame);
//        ptrClassicFrameLayout.setPtrHandler(new PtrHandler() {
//            @Override
//            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
//                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
//            }
//
//            @Override
//            public void onRefreshBegin(PtrFrameLayout frame) {
//                //调用presenter获取数据
//                presenter.getNewMail();
//            }
//        });
//        //初始化顶部的文字(loading或者emailAddress)
//        mailBoxName = root.findViewById(R.id.mailbox_name);
//        mailCount = root.findViewById(R.id.mail_count);
//        mailCount.setText("(" + String.valueOf(count) + ")");
//        switch (mailType){
//            case MailType.INBOX:
//                mailBoxName.setText("收件箱");
//                break;
//            case MailType.DRAFT:
//                mailBoxName.setText("草稿箱");
//                break;
//            case MailType.SENT:
//                mailBoxName.setText("已发送");
//                break;
//            case MailType.DELETED:
//                mailBoxName.setText("已删除");
//                break;
//            case MailType.RUBBISH:
//                mailBoxName.setText("垃圾邮件");
//                break;
//        }
//        buttonBack = root.findViewById(R.id.button_back);
//        buttonEdit = root.findViewById(R.id.button_edit);
//        //初始化邮件列表
//        mailListView = root.findViewById(R.id.mail_list);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mailListView.getContext());
//        mailListView.setLayoutManager(linearLayoutManager);
//        mailListAdapter = new MailListAdapter(mailType);
//        mailListAdapter.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(int position) {
//                clickItemIndex = position;
//                Intent intent = new Intent(getContext(), MailActivity.class);
//                intent.putExtra("mailId",mailListAdapter.getMail(position).getUid());
//                intent.putExtra("mailType",mailType);
//                intent.putExtra("mailNo",position);
//                startActivity(intent);
//            }
//        });
//        mailListView.getItemAnimator().setChangeDuration(0);
//        mailListView.setAdapter(mailListAdapter);
////        mailListView.setNestedScrollingEnabled(true);
//        //加载中
//        layoutLoading = root.findViewById(R.id.loading);
//        loadingAnim = root.findViewById(R.id.loading_anim);
//        noMail = root.findViewById(R.id.no_mail);
//    }
//
//    private void initListener(){
//        buttonBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getActivity().finish();
//            }
//        });
//
//        buttonEdit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getContext(), EditMailActivity.class);
//                startActivity(intent);
//            }
//        });
//    }
//
//    public void updateMail(int function){
//
//    }
//    /**
//     * 获得Presenter, 之后可实现View -> Presenter的调用
//     * @param presenter
//     */
//    @Override
//    public void setPresenter(MailListContract.Presenter presenter) {
//        this.presenter = presenter;
//    }
//
//    /**
//     * 初始数据显示，直接赋值给Adapter的list进行渲染。并重新渲染
//     * @param list
//     */
//    @Override
//    public void showMailList(List<Mail> list) {
//        if(list!=null && list.size() != 0){
//            mailListAdapter.setMailList(list);
//            mailListAdapter.notifyDataSetChanged();
//            layoutLoading.setVisibility(View.GONE);
//            count = list.size();
//        }else{
//            layoutLoading.setVisibility(View.VISIBLE);
//            loadingAnim.setVisibility(View.GONE);
//            noMail.setVisibility(View.VISIBLE);
//            count = 0;
//        }
//        mailCount.setText("(" + String.valueOf(count) + ")");
//    }
//
//    /**
//     * 新增数据显示，在Adapter的list前面插入这些新数据。并重新渲染
//     * @param list
//     */
//    @Override
//    public void showNewMailList(List<Mail> list) {
//        for (Mail mail:list){
//            logger.info(mail.toString());
//        }
//        mailListAdapter.addMailList(list);
//        count += list.size();
//        mailCount.setText("(" + String.valueOf(count) + ")");
//        ptrClassicFrameLayout.refreshComplete();
//    }
//
//    /**
//     * 显示加载错误
//     */
//    @Override
//    public void showLoadError() {
//        layoutLoading.setVisibility(View.VISIBLE);
//        loadingAnim.setVisibility(View.GONE);
//        noMail.setVisibility(View.VISIBLE);
//        ptrClassicFrameLayout.refreshComplete();
//    }
//
//    /**
//     * 隐藏加载错误
//     */
//    @Override
//    public void hideLoadError() {
//
//    }
//
//
//    /**
//     * Fragment生命周期开始时, 通知Presenter的Start()开始初始化数据
//     */
//    @Override
//    public void onResume() {
//        super.onResume();
//        System.out.println("OnResume");
//        if (needLoadData){
//            presenter = new MailListPresenter(this, new LocalMailListDataSource(getContext()),mailType);
//            presenter.start();
//            needLoadData = false;
//            System.out.println("加载数据");
//        }
//    }
//}
