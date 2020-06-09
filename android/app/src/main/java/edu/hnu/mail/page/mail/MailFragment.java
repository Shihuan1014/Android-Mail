package edu.hnu.mail.page.mail;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.hnu.mail.R;
import edu.hnu.mail.constant.MailType;
import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.data.entity.Attachment;
import edu.hnu.mail.data.entity.Mail;
import edu.hnu.mail.data.source.local.LocalMailDataSource;
import edu.hnu.mail.page.edit.EditMailActivity;
import edu.hnu.mail.ui.mails.AttachmentListAdapter;
import edu.hnu.mail.ui.mails.OnItemClickListener;
import edu.hnu.mail.util.CustomDialog;
import edu.hnu.mail.util.HtmlUtil;
import edu.hnu.mail.util.ScrollWebView;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * Fragment比Activity更加灵活
 */
public class MailFragment extends Fragment implements MailContract.View {

    //MVP相关
    private MailContract.Presenter presenter;
    //UI相关
        private PtrClassicFrameLayout ptrClassicFrameLayout;
        //顶部
        private ImageView buttonBack;
//        private ImageView buttonBefore;
//        private ImageView buttonNext;
        //邮件相关
        private ImageView flagIcon;
        private ImageView unSeenIcon;
        private TextView subject;
        private TextView toName;
        private TextView fromName2;
        private TextView toEmail;
        private TextView fromName;
        private TextView fromEmail;
        private TextView sendTime;
        private TextView buttonShowDetail;
        private TextView buttonHideDetail;
        private LinearLayout shortInfoLayout;
        private LinearLayout detailInfoLayout;
        //底部操作栏
        private ImageView buttonFlag;
        private ImageView buttonDelete;
        private ImageView buttonShare;
        private ImageView buttonMore;
        private ImageView buttonEdit;
        private ImageView buttonFile;
        //富文本
        private com.tencent.smtt.sdk.WebView webView;
        private LinearLayout loadingLayout;
        private LinearLayout webViewLayout;
        //附件
        private RecyclerView attachmentList;
        private AttachmentListAdapter attachmentListAdapter;
        //删除的dialog
        private CustomDialog deleteDialog;
        private boolean loaded = false;
        //显示源码
        private TextView originText;
        //原来的webView大小
        private int originWebViewWidth = 0;
        private int originWebViewHeight = 0;
    //数据相关
    private Mail mail;
    private String mailId;
    private String mailBox;
    private int mailType;
    private int currentIndex;
    private String userName;
    //删除
    private boolean deleted = false;
    //富文本显示(其实就是网页，只不过这样会快一些，先加载一个低级的)
    private Handler handler;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateHeight();
        }
    };

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public MailFragment(){

    }

    public MailFragment(String userName,String mailId,int index,String mailBox,int mailType){
        this.mailId = mailId;
        this.currentIndex = index;
        this.mailType = mailType;
        this.userName = userName;
        this.mailBox = mailBox;
        handler = new Handler();
    }

    public void setWebViewDisallow(boolean allow){
        webView.requestDisallowInterceptTouchEvent(allow);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_mail,container,false);
        initView(root);
        initToolbar();
        initListener();
        return root;
    }

    private void initView(View root){
        loadingLayout = root.findViewById(R.id.loading);
        webViewLayout = root.findViewById(R.id.webViewLayout);
        //初始化顶部的按钮
        buttonBack = root.findViewById(R.id.button_back);
//        buttonBefore = root.findViewById(R.id.button_before);
//        buttonNext = root.findViewById(R.id.button_next);
        //初始化邮件收取信息
        flagIcon = root.findViewById(R.id.flag_icon);
        unSeenIcon = root.findViewById(R.id.seen_icon);
        subject = root.findViewById(R.id.send_subject);
        toName = root.findViewById(R.id.to_name);
        toEmail = root.findViewById(R.id.to_email);
        fromName = root.findViewById(R.id.from_name);
        fromName2 = root.findViewById(R.id.from_name2);
        fromEmail = root.findViewById(R.id.from_email);
        sendTime = root.findViewById(R.id.send_time);
        //初始化隐藏显示按钮
        buttonShowDetail = root.findViewById(R.id.show_detail);
        buttonHideDetail = root.findViewById(R.id.hide_detail);
        //初始化两种显示(简要、详细)
        shortInfoLayout = root.findViewById(R.id.send_short);
        detailInfoLayout = root.findViewById(R.id.send_detail);
        webView = root.findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // 解决http及https混合情况下页面加载问题
        }
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        try {
            if (Build.VERSION.SDK_INT >= 16) {
                Class<?> clazz = webSettings.getClass();
                Method method = clazz.getMethod(
                        "setAllowUniversalAccessFromFileURLs", boolean.class);
                if (method != null) {
                    method.invoke(webSettings, true);
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(true); // 支持缩放
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url)
            {
                super.onPageFinished(view, url);
                // 加载完成
                    //加载完成
                if(loaded){
                    loadingLayout.setVisibility(View.GONE);
                    webViewLayout.setVisibility(View.VISIBLE);
                    updateHeight();
                    loaded = false;
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });
        //初始化底部操作栏
        buttonFlag = root.findViewById(R.id.button_star);
        buttonShare = root.findViewById(R.id.button_share);
        if(mailType == 3 || mailType == 4){
            buttonShare.setVisibility(View.GONE);
        }
//        buttonMore = root.findViewById(R.id.button_more);
        buttonDelete = root.findViewById(R.id.button_delete);
        buttonEdit = root.findViewById(R.id.button_edit);
        buttonFile = root.findViewById(R.id.button_file);

        attachmentList = root.findViewById(R.id.attachment_list);
        attachmentList.setNestedScrollingEnabled(true);
        attachmentListAdapter = new AttachmentListAdapter();
        attachmentListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                lookBigImage(attachmentListAdapter.getAttachmentPath(position));
            }
        });
        attachmentList.setAdapter(attachmentListAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        attachmentList.setLayoutManager(linearLayoutManager);
        originText = root.findViewById(R.id.origin_text);
    }

    private void updateHeight(){
        webView.measure(0,0);
        ViewGroup.LayoutParams layoutParams = webView.getLayoutParams();
        if (layoutParams.height > 300) {
            if(layoutParams.height > originWebViewHeight){
                originWebViewHeight = layoutParams.height;
                webView.setLayoutParams(layoutParams);
            }
        }
        handler.postDelayed(runnable,500);
    }

    /**
     * 初始化事件监听
     */
    private void initListener(){


        buttonFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomDialog();
            }
        });
        //显示详细收发人信息
        buttonHideDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shortInfoLayout.setVisibility(View.VISIBLE);
                detailInfoLayout.setVisibility(View.GONE);
            }
        });
        //只显示发件人
        buttonShowDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shortInfoLayout.setVisibility(View.GONE);
                detailInfoLayout.setVisibility(View.VISIBLE);
            }
        });

        //摧毁
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        //删除
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //标记删除
                if(mailType==1 || mailType == MailType.ALL_INBOX){
                    presenter.deleteMail(mail);
                }else{
                    if(mailType == MailType.ALL_FLAG){
                        if(mail.getDeleted() == 1){
                            presenter.deleteMail(mail);
                        }else{
                            if(deleteDialog==null){
                                deleteDialog = new CustomDialog(getContext());
                                deleteDialog.setTitle("彻底删除");
                                deleteDialog.setButtonCancelText("取消");
                                deleteDialog.setButtonConfirmText("确认删除");
                                //真正删除
                                deleteDialog.setSubTitle("彻底删除后，邮件将不可恢复");
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
                                        presenter.deleteMail(mail);
                                    }
                                });
                            }
                            deleteDialog.show();
                        }
                    }
                    else{
                        if(deleteDialog==null){
                            deleteDialog = new CustomDialog(getContext());
                            deleteDialog.setTitle("彻底删除");
                            deleteDialog.setButtonCancelText("取消");
                            deleteDialog.setButtonConfirmText("确认删除");
                            //真正删除
                            deleteDialog.setSubTitle("彻底删除后，邮件将不可恢复");
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
                                    presenter.deleteMail(mail);
                                }
                            });
                        }
                        deleteDialog.show();
                    }
                }
            }
        });

        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),EditMailActivity.class);
                intent.putExtra("mailId",mail.getUid());
                intent.putExtra("userName",mail.getUserAddress());
                intent.putExtra("nickName",mail.getFrom());
                intent.putExtra("mailBox",mail.getMailBox());
                getActivity().startActivity(intent);
            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),EditMailActivity.class);
                intent.putExtra("mailId",mail.getUid());
                intent.putExtra("userName",mail.getUserAddress());
                intent.putExtra("mailBox",mail.getMailBox());
                intent.putExtra("nickName",mail.getFrom());
                intent.putExtra("currentIndex",currentIndex);
                if (mail.getIsGroup() == 1){
                    intent.putExtra("isGroup",true);
                }
                getActivity().startActivity(intent);
            }
        });
    }

    /**
     * 查看大图
     * @param url
     */
    private void lookBigImage(String url){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View imgEntryView = inflater.inflate(R.layout.dialog_image_look, null);
// 加载自定义的布局文件
        final AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
        imgEntryView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                dialog.cancel();
            }
        });
        ImageView img = (ImageView) imgEntryView.findViewById(R.id.large_image);
        File createFiles = new File(getContext().getFilesDir(),
                url);
        if(createFiles.exists()){
            Glide.with(getContext())
                    .load(createFiles)
                    .error(R.drawable.ic_file_cover)
                    .into(img);
            dialog.setView(imgEntryView); // 自定义dialog
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
        }else{
            createFiles = new File(url);
            if(createFiles.exists()) {
                Glide.with(getContext())
                        .load(createFiles)
                        .error(R.drawable.ic_file_cover)
                        .into(img);
                dialog.setView(imgEntryView); // 自定义dialog
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.show();
            }else{
                Toast.makeText(getContext(),"文件打开失败",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 获得Presenter, 之后可实现View -> Presenter的调用
     * @param presenter
     */
    @Override
    public void setPresenter(MailContract.Presenter presenter) {
        this.presenter = presenter;
    }


    /**
     * 邮件加载完成，显示邮件
     * @param mail
     */
    @Override
    public void showMail(Mail mail){
        this.mail = mail;
        subject.setText(mail.getSubject());
        fromName.setText(mail.getFrom());
        fromEmail.setText(mail.getFromEmail());
        toName.setText(mail.getTo());
        fromName2.setText(mail.getFrom());
        toEmail.setText(mail.getToEmail());
        if (mail.getSendTime()!=null){
            sendTime.setText(simpleDateFormat.format(mail.getSendTime()));
        }
        loaded = true;
        byte[] bytes = mail.getData();
        if (bytes!=null) {
            webView.loadDataWithBaseURL(null, getNewHtml(new String(mail.getData())), "text/html", "UTF-8",
                    null);
        }else {
            System.out.println("消息体为空");
        }
        if(mail.getFlag()==1){
            flagIcon.setVisibility(View.VISIBLE);
        }
        System.out.println("附件个数"+mail.getAttachmentList().size());
        attachmentListAdapter.setAttachmentList(mail.getAttachmentList());
        Intent intent = new Intent("setSeen");
        intent.putExtra("position",currentIndex);
        intent.putExtra("argv",1);
        getActivity().sendBroadcast(intent);
    }

    private String getNewHtml(String htmlText){
//        originText.setText(htmlText);
        System.out.println(htmlText);
        try {
            Document document = Jsoup.parse(htmlText);
            document.head().append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=0.5, maximum-scale=2.0, user-scalable=yes\" />\n" +
                    "<style>div{font-size:18px;} img{max-width:100%;}</style>");
            Elements pngs = document.select("img[src]");
            for (Element element : pngs) {
                String imgUrl = element.attr("src");
                if (imgUrl.trim().startsWith("data")) {
                    imgUrl = imgUrl.replace(" ","+");
                    element.attr("src", imgUrl);
                }
            }
            htmlText = document.html();
            System.out.println("新网页："+htmlText);
            return htmlText;
        }catch (Exception e){
            return htmlText;
        }
    }

    private void initToolbar(){
        if(mailType== MailType.DRAFT){
            buttonFlag.setVisibility(View.GONE);
            buttonShare.setVisibility(View.GONE);
            buttonEdit.setVisibility(View.VISIBLE);
        }
    }

    private void showBottomDialog(){
        //1、使用Dialog、设置style
        final Dialog dialog = new Dialog(getContext(),R.style.BottomDialogStyle);
        dialog.setCanceledOnTouchOutside(true);
        //2、设置布局
        View view = View.inflate(getContext(),R.layout.dialog_mail_flag,null);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        //设置弹出位置
        window.setGravity(Gravity.BOTTOM);
        //设置弹出动画
        window.setWindowAnimations(R.style.bottomDialogAnim);
        //设置对话框大小
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
        TextView buttonFirst = dialog.findViewById(R.id.first);
        if(flagIcon.getVisibility() == View.VISIBLE){
            buttonFirst.setText("取消星标");
        }else{
            buttonFirst.setText("星标邮件");
        }
        buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Mail tmpMail = new Mail();
                tmpMail.setUserAddress(mail.getUserAddress());
                tmpMail.setUid(mail.getUid());
                tmpMail.setMailBox(mail.getMailBox());
                if(flagIcon.getVisibility() == View.VISIBLE){
                    tmpMail.setFlag(0);
                }else{
                    tmpMail.setFlag(1);
                }
                presenter.setMailFlag(tmpMail);
                dialog.dismiss();
            }
        });
        TextView buttonSecond = dialog.findViewById(R.id.second);
        if(unSeenIcon.getVisibility() == View.VISIBLE){
            buttonSecond.setText("设为已读");
        }else{
            buttonSecond.setText("设为未读");
        }
        buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Mail tmpMail = new Mail();
                tmpMail.setUserAddress(mail.getUserAddress());
                tmpMail.setUid(mail.getUid());
                tmpMail.setMailBox(mail.getMailBox());
                if(unSeenIcon.getVisibility() == View.VISIBLE){
                    tmpMail.setSeen(1);
                }else{
                    tmpMail.setSeen(0);
                }
                presenter.setMailSeen(tmpMail);
                dialog.dismiss();
            }
        });
    }

    /**
     * 显示加载错误
     */
    @Override
    public void showLoadError() {
//        Toast.makeText(getContext(), "发生错误", Toast.LENGTH_SHORT).show();
        getActivity().finish();
//        ptrClassicFrameLayout.refreshComplete();
    }

    /**
     * 隐藏加载错误
     */
    @Override
    public void hideLoadError() {
//        ptrClassicFrameLayout.refreshComplete();
    }

    @Override
    public void deleteSuccess(){
        Intent intent = new Intent("deleteItem");
        intent.putExtra("position",currentIndex);
        if (mail.getMailBox().equalsIgnoreCase("INBOX") && mail.getDeleted() == 0){
            UserInfo.deleteMail(1);
        }
        getActivity().sendBroadcast(intent);
        getActivity().finish();
    }

    @Override
    public void setFlagSuccess(int flag) {
        Intent intent = new Intent("setFlag");
        intent.putExtra("position",currentIndex);
        if(flag == 1){
            Toast.makeText(getContext(),"添加星标成功",Toast.LENGTH_SHORT).show();
            flagIcon.setVisibility(View.VISIBLE);
            intent.putExtra("argv",1);
        }else{
            Toast.makeText(getContext(),"取消星标成功",Toast.LENGTH_SHORT).show();
            flagIcon.setVisibility(View.GONE);
            intent.putExtra("argv",0);
        }
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void setSeenSuccess(int seen) {
        Intent intent = new Intent("setSeen");
        intent.putExtra("position",currentIndex);
        if(seen == 0){
            Toast.makeText(getContext(),"设为未读邮件",Toast.LENGTH_SHORT).show();
            intent.putExtra("argv",0);
            unSeenIcon.setVisibility(View.VISIBLE);
        }else{
            Toast.makeText(getContext(),"设为已读邮件",Toast.LENGTH_SHORT).show();
            intent.putExtra("argv",1);
            unSeenIcon.setVisibility(View.GONE);
        }
        getActivity().sendBroadcast(intent);

    }


    /**
     * Fragment生命周期开始时, 通知Presenter的Start()开始初始化数据
     */
    @Override
    public void onResume() {
        super.onResume();
        presenter = new MailPresenter(this, new LocalMailDataSource(getContext()));
        Mail tmp = new Mail();
        tmp.setUid(mailId);
        tmp.setUserAddress(userName);
        tmp.setMailBox(mailBox);
        presenter.getMail(tmp);
    }

}
