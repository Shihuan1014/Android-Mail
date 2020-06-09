package edu.hnu.mail.page.edit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import edu.hnu.mail.R;
import edu.hnu.mail.constant.EditorFormat;
import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.data.entity.Attachment;
import edu.hnu.mail.data.entity.Contact;
import edu.hnu.mail.data.entity.Mail;
import edu.hnu.mail.data.entity.User;
import edu.hnu.mail.page.contact.ContactContract;
import edu.hnu.mail.ui.mails.FileListAdapter;
import edu.hnu.mail.ui.mails.MailCompleteTextView;
import edu.hnu.mail.ui.mails.OnItemClickListener;
import edu.hnu.mail.util.CustomDialog;
import edu.hnu.mail.util.FileUtil;
import edu.hnu.mail.util.RichEditor;
import io.netty.handler.codec.base64.Base64Encoder;

public class EditMailFragment extends Fragment implements EditMailContract.View, View.OnLayoutChangeListener {

    //MVP相关
    private EditMailContract.Presenter presenter;

    private RichEditor richEditor;

    //顶部
    private TextView buttonBack;
    private TextView buttonSend;
    private boolean canSend = false;
    //邮件信息
    private MailCompleteTextView toEmail;
    private EditText subject;
    private TextView fromEmail;
    private TextView fromEmailEdit;
    private LinearLayout collapse;
    private LinearLayout expand;
    private String data;
    //底部工具栏
        private LinearLayout bottomToolBar;
        //字体
        private LinearLayout layoutFont;
        private ImageView buttonFont;
        private LinearLayout expandFont;
        private RadioGroup radioGroupFont;
        private int smallFontSize = 3;
        private int mediumFontSize = 4;
        private int largeFontSize = 5;
        //颜色
        private LinearLayout layoutColor;
        private ImageView buttonColor;
        private LinearLayout expandColor;
        private RadioGroup radioGroupColor;
        private final String black = "#050505",blue = "#3458DB", red = "#FF0000", gray = "#707070";
        //其他
        private LinearLayout layoutOther;
        private ImageView buttonBold;
        private boolean isBold = false;
        private ImageView buttonAlign;
        private boolean isAlign;
    //    private ImageView buttonList;
        private boolean isList;
        private ImageView buttonQuote;
        private boolean isQuote;
        private ImageView buttonImage;
        private ImageView buttonFile;
        private RecyclerView fileListView;
        private FileListAdapter fileListAdapter;

        private ImageView buttonSelectContact;
        private List<User> contactList;

    //滚动相关
    private ScrollView scrollView;
    private int richEditorHeight = 0;
    //Dialog
    private CustomDialog customDialog;
    private AlertDialog loadingDialog;
    private SelectSenderDialog selectSenderDialog;
    private SelectSenderDialog selectContactDialog;
    //屏幕高度
    private int screenHeight = 0;
    //软件盘弹起后所占高度阀值
    private int keyHeight = 0;
    private String userName;
    private String nickName;
    private String receiver;
    //是否为回复，若为回复邮件，则需要加载邮件回填
    private Mail oldMail;
    private int replyMode = 0;
    private String mailUid;
    private String mailBox;
    //是保存还是发送, 0 为保存，1为发送
    private int saveOrSend = 0;
    private ImageView tmpImageView;
    //是否为管理员群发
    private boolean isGroup = false;
    //
    private int currentIndex = 0;
    //小键盘优化
    View mRootView;
    InputMethodManager manager;
    int rootViewVisibleHeight;//纪录根视图的显示高度
    private Handler handler = new Handler();
    private Runnable showFilelist =new Runnable() {
        @Override
        public void run() {
            fileListView.setVisibility(View.VISIBLE);
        }
    };

    public void setMode(int replyMode,String mailUid,String mailBox,String nickName,String userName){
        this.replyMode = replyMode;
        this.mailUid = mailUid;
        this.userName = userName;
        this.mailBox = mailBox;
        this.nickName = nickName;
    }

    public void setCurrentIndex(int currentIndex){
        this.currentIndex = currentIndex;
    }

    public void setGroup(){
        isGroup = true;
    }

    public void setReceiver(String receiver){
        this.receiver = receiver;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_edit_mail,container,false);
        manager= ((InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE));
        mRootView = getActivity().getWindow().getDecorView();
        initSoftKeyboard();
        initView(root);
        presenter.getContactList();
        tmpImageView = root.findViewById(R.id.tmp_image);
        initListener();
        if(replyMode>0){
            initData();
        }else{
            richEditor.setHtml("<div><br/><div>");
        }
        screenHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        //阀值设置为屏幕高度的1/3
        keyHeight = screenHeight/3;
        return root;
    }

    private void initSoftKeyboard(){
        //监听视图树中全局布局发生改变或者视图树中的某个视图的可视状态发生改变
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //获取当前根视图在屏幕上显示的大小
                Rect r = new Rect();
                mRootView.getWindowVisibleDisplayFrame(r);

                int visibleHeight = r.height();
                System.out.println("" + visibleHeight);
                if (rootViewVisibleHeight == 0) {
                    rootViewVisibleHeight = visibleHeight;
                    return;
                }

                //根视图显示高度没有变化，可以看作软键盘显示／隐藏状态没有改变
                if (rootViewVisibleHeight == visibleHeight) {
                    return;
                }

                //根视图显示高度变小超过200，可以看作软键盘显示了
                if (rootViewVisibleHeight - visibleHeight > 200) {
                    rootViewVisibleHeight = visibleHeight;
                    fileListView.setVisibility(View.GONE);
                    return;
                }

                //根视图显示高度变大超过200，可以看作软键盘隐藏了
                if (visibleHeight - rootViewVisibleHeight > 200) {
                    rootViewVisibleHeight = visibleHeight;
                    return;
                }
            }
        });
    }
    private void initData(){
        presenter.getMail(userName,mailBox,mailUid);
    }

    private void initView(View v){
        richEditor = v.findViewById(R.id.richEditor);
        richEditor.setAlignLeft();
        richEditor.setFontSize(4);
        richEditor.setTextColor(Color.parseColor("#050505"));


        scrollView = v.findViewById(R.id.scrollView);
        //顶部
        if(userName==null){
            userName = UserInfo.users.get(UserInfo.currentIndex).getSmtpUserName();
            nickName = UserInfo.users.get(UserInfo.currentIndex).getNickName();
        }
        buttonSelectContact = v.findViewById(R.id.select_contact);
        buttonSelectContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectContactDialog();
            }
        });
        buttonBack = v.findViewById(R.id.button_back);
        buttonSend = v.findViewById(R.id.button_send);
        collapse = v.findViewById(R.id.collapse);
        fromEmail = v.findViewById(R.id.from_email);
        fromEmail.setText(userName);
        fromEmailEdit = v.findViewById(R.id.from_email_edit);
        fromEmailEdit.setText(userName);
        expand = v.findViewById(R.id.expand);
        //邮件信息
        toEmail = v.findViewById(R.id.to_email);
        if (receiver!=null){
            toEmail.setText(receiver);
            buttonSend.setTextColor(Color.parseColor("#1e88E5"));
            canSend = true;
        }
        if (isGroup){
            canSend = true;
            buttonSend.setTextColor(Color.parseColor("#1e88E5"));
        }
        InputFilter filter=new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if(source.equals(" "))return "";
                else return null;
            }
        };
        //禁止输入空格
        toEmail.setFilters(new InputFilter[]{filter});
        subject = v.findViewById(R.id.send_subject);
        //底部工具栏
        bottomToolBar = v.findViewById(R.id.bottom_toolbar);
        //字体
        layoutFont = v.findViewById(R.id.layout_font);
        buttonFont = v.findViewById(R.id.button_font);
        expandFont = v.findViewById(R.id.font_expand);
        radioGroupFont = v.findViewById(R.id.radioGroup_font);
        //颜色
        layoutColor = v.findViewById(R.id.layout_color);
        buttonColor = v.findViewById(R.id.button_color);
        expandColor = v.findViewById(R.id.color_expand);
        radioGroupColor = v.findViewById(R.id.radioGroup_color);
        //其他
        layoutOther = v.findViewById(R.id.layout_other);
        buttonBold = v.findViewById(R.id.button_bold);
        buttonAlign = v.findViewById(R.id.button_align);
//        buttonList = v.findViewById(R.id.button_list);
        buttonQuote = v.findViewById(R.id.button_quote);
        buttonImage= v.findViewById(R.id.button_image);
        buttonFile = v.findViewById(R.id.button_file);
        fileListView = v.findViewById(R.id.file_list);
        fileListAdapter = new FileListAdapter();
        fileListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //长按弹出dialog
                showBottomDialog(position);
            }
        });
        fileListAdapter.setOnItemClickListener2(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 0x12);
            }
        });
        fileListView.setAdapter(fileListAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),3);
        fileListView.setLayoutManager(gridLayoutManager);

        //群发邮件特殊显示
        if (isGroup){
            toEmail.setText("全体用户");
            toEmail.setEnabled(false);
            buttonSelectContact.setVisibility(View.GONE);
        }
    }

    private void initListener(){

        //展开或收回抄送、密送人
        collapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                collapse.setVisibility(View.GONE);
//                expand.setVisibility(View.VISIBLE);
                showSelectSenderDialog();
            }
        });

        //富文本编辑器受到焦点时，出现底部工具栏
        richEditor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    bottomToolBar.setVisibility(View.VISIBLE);
                }else{
                    bottomToolBar.setVisibility(View.GONE);
                }
                fileListView.setVisibility(View.GONE);
            }
        });
        richEditor.setOnDecorationChangeListener(new RichEditor.OnDecorationStateListener() {
            @Override
            public void onStateChangeListener(String text, List<String> types) {
                for (String type : types){
                    System.out.println(type);
                }
                upDateBottomToolBar(types);
            }
        });
        // 输入变化时触发
        richEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                updateHeight();
            }
        });

        richEditor.setOnInitialLoadListener(new RichEditor.AfterInitialLoadListener() {
            @Override
            public void onAfterInitialLoad(boolean isReady) {
                if (isReady == true){
                    updateHeight();
                }
            }
        });

        richEditor.setOnHtmlListener(new RichEditor.OnHtmlListener() {
            @Override
            public void onHtmlGet(String text) {
                if(saveOrSend == 0){
                    //保存
                    saveEmail(text);
                }else{
                    //发送
                    sendEmail(text);
                }
            }
        });
        // 退出activity
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(canSend){
                    showDialog();
                }else{
                    getActivity().finish();
                }
            }
        });

        //发送邮件
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canSend){
                    saveOrSend = 1;
                    richEditor.getFinalHtml();
//                    System.out.println(richEditor.getHtml());
                }
            }
        });

        toEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length()>0){
                    if(!canSend){
                        buttonSend.setTextColor(Color.parseColor("#1e88E5"));
                        canSend = true;
                    }
                }else{
                    canSend = false;
                    buttonSend.setTextColor(Color.parseColor("#601e88E5"));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //展开/收回字体选择
        buttonFont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expandFont.getVisibility() == View.VISIBLE){
                    expandFont.setVisibility(View.GONE);
                    layoutColor.setVisibility(View.VISIBLE);
                    layoutOther.setVisibility(View.VISIBLE);
                }else {
                    expandFont.setVisibility(View.VISIBLE);
                    layoutColor.setVisibility(View.GONE);
                    layoutOther.setVisibility(View.GONE);
                }
            }
        });

        radioGroupFont.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.font_small:
                        richEditor.setFontSize(smallFontSize);
                        break;
                    case R.id.font_medium:
                        richEditor.setFontSize(mediumFontSize);
                        break;
                    case R.id.font_large:
                        richEditor.setFontSize(largeFontSize);
                        break;
                    default:
                        break;
                }
                expandFont.setVisibility(View.GONE);
                layoutColor.setVisibility(View.VISIBLE);
                layoutOther.setVisibility(View.VISIBLE);
            }
        });

        //展开/收回颜料桶
        buttonColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expandColor.getVisibility() == View.VISIBLE){
                    expandColor.setVisibility(View.GONE);
                    layoutFont.setVisibility(View.VISIBLE);
                    layoutOther.setVisibility(View.VISIBLE);
                }else {
                    expandColor.setVisibility(View.VISIBLE);
                    layoutFont.setVisibility(View.GONE);
                    layoutOther.setVisibility(View.GONE);
                }
            }
        });

        radioGroupColor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radio_black:
                        richEditor.setTextColor(Color.parseColor("#050505"));
                        break;
                    case R.id.radio_blue:
                        richEditor.setTextColor(Color.parseColor("#3458DB"));
                        break;
                    case R.id.radio_red:
                        richEditor.setTextColor(Color.parseColor("#FF0000"));
                        break;
                    case R.id.radio_gray:
                        richEditor.setTextColor(Color.parseColor("#707070"));
                        break;
                    default:
                        break;
                }
                expandColor.setVisibility(View.GONE);
                layoutFont.setVisibility(View.VISIBLE);
                layoutOther.setVisibility(View.VISIBLE);
            }
        });

        // 粗体
        buttonBold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                richEditor.setBold();
                if(isBold){
                    isBold = false;
                    buttonBold.setImageResource(R.drawable.ic_bold);
                }else{
                    isBold = true;
                    buttonBold.setImageResource(R.drawable.ic_bold_selected);
                }
            }
        });

        // 居中
        buttonAlign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAlign){
                    isAlign = false;
                    richEditor.setAlignLeft();
                    buttonAlign.setImageResource(R.drawable.ic_center_selected2);
                }else {
                    isAlign = true;
                    richEditor.setAlignCenter();
                    buttonAlign.setImageResource(R.drawable.ic_center2);
                }
            }
        });

        buttonQuote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                richEditor.setBlockquote();
                if (isQuote){
                    isQuote = false;
                    buttonQuote.setImageResource(R.drawable.ic_quote_gray);
                }else{
                    isQuote = true;
                    buttonQuote.setImageResource(R.drawable.ic_quote_selected);
                }
            }
        });

        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, 0x11);
            }
        });

        buttonFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fileListView.getVisibility() == View.VISIBLE){
                    fileListView.setVisibility(View.GONE);
                }else{
                    richEditor.clearFocus();
                    bottomToolBar.setVisibility(View.GONE);
                    if(manager!=null)
                        manager.hideSoftInputFromWindow(v.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                    handler.postDelayed(showFilelist,100);
                }
            }
        });
    }

    private void updateHeight(){
        richEditor.measure(0,0);
        int measuredHeight = richEditor.getMeasuredHeight();
        ViewGroup.LayoutParams layoutParams = richEditor.getLayoutParams();
        layoutParams.height = measuredHeight;
        richEditor.setLayoutParams(layoutParams);
        int moveDistance = measuredHeight - richEditorHeight;
        int scrollY = scrollView.getScrollY();
        richEditorHeight = measuredHeight;
        scrollView.smoothScrollTo(0,scrollY + moveDistance);
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
        textView.setText("正在发送");

    }

    /**
     * 根据webView返回的更新消息进行工具栏更新
     * @param list
     */
    private void upDateBottomToolBar(List<String> list){
        for (String s:list){
            if(s.startsWith("size")){
                int i = s.charAt(5);
                System.out.println(s + " " + i + " " + s.substring(5));
                switch (i){
                    case 51:
                        System.out.println("更换为小字体");
                        radioGroupFont.check(R.id.font_small);
                        break;
                    case 52:
                        System.out.println("更换为中字体");
                        radioGroupFont.check(R.id.font_medium);
                        break;
                    case 53:
                        System.out.println("更换为大字体");
                        radioGroupFont.check(R.id.font_large);
                        break;
                    default:
                        break;
                }
            }
            else if(s.startsWith("color")){
                String color = s.substring(6);
                switch (color) {
                    case black:
                        radioGroupColor.check(R.id.radio_black);
                        break;
                    case blue:
                        radioGroupColor.check(R.id.radio_blue);
                        break;
                    case red:
                        radioGroupColor.check(R.id.radio_red);
                        break;
                    case gray:
                        radioGroupColor.check(R.id.radio_gray);
                        break;
                    default:
                        break;
                }
            }
            else if(s.startsWith("bold")){
                String bold = s.substring(5);
                switch (bold){
                    case "true":
                        System.out.println("updateBottomToolBar bold change: " + bold);
                        buttonBold.setImageResource(R.drawable.ic_bold_selected);
                        break;
                    case "false":
                        System.out.println("updateBottomToolBar bold change: " + bold);
                        buttonBold.setImageResource(R.drawable.ic_bold);
                        break;
                    default:
                        break;
                }
            }
            else if(s.startsWith("align")){
                String align = s.substring(6);
                switch (align){
                    case "left":
                        buttonAlign.setImageResource(R.drawable.ic_center2);
                        break;
                    case "center":
                        buttonAlign.setImageResource(R.drawable.ic_center_selected2);
                        break;
                    default:
                        break;
                }
            }
            else if(s.startsWith("blockquote")){
                String align = s.substring(11);
                switch (align){
                    case "true":
                        isQuote = true;
                        buttonQuote.setImageResource(R.drawable.ic_quote_selected);
                        break;
                    case "false":
                        isQuote = false;
                        buttonQuote.setImageResource(R.drawable.ic_quote_gray);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 发送邮件
     */
    private void sendEmail(String data){
        Mail mail = new Mail();
        mail.setUserAddress(fromEmail.getText().toString());
        // TODO: 2020/4/15 此处应在数据库找一找有没有这个邮件，他的名称是什么，没有则使用
        //  邮件作为名字
        mail.setTo(toEmail.getText().toString());
        mail.setToEmail(toEmail.getText().toString());
        mail.setSubject(subject.getText().toString());
        if (isGroup){
            mail.setIsGroup(1);
        }
        if (replyMode == 2){
            //告诉source，这封邮件之前是草稿，要删除草稿
            mail.setUid(oldMail.getUid());
            mail.setMailBox("SENT");
            mail.setDraft(1);
        }
        String s = EditorFormat.header+ data +EditorFormat.footer;
        mail.setData(s.getBytes());
        mail.setSendTime(new Date(System.currentTimeMillis()));
        List<Attachment> attachments = fileListAdapter.getAttachmentList();
        for(Attachment attachment: attachments){
            if(attachment.getUri()!=null){
                String base64 = getBase64FromUri(attachment.getUri());
                attachment.setBase64(base64);
                try {
                    save2File(attachment.getName(),base64.getBytes());
                    attachment.setPath(attachment.getName());
                    System.out.println(attachment.getName());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else if(attachment.getPath()!=null){
                attachment.setBase64(getBase64FromPath(attachment.getPath()));
            }
        }
        mail.setAttachmentList(attachments);
        mail.setFrom(nickName);
        mail.setFromEmail(fromEmail.getText().toString());
        mail.setMailBox("SENT");
        presenter.sendMail(mail);
        showLoading();
    }

    /**
     * 发送邮件
     */
    private void saveEmail(String data){
        Mail mail = new Mail();
        mail.setUserAddress(fromEmail.getText().toString());
        // TODO: 2020/4/15 此处应在数据库找一找有没有这个邮件，他的名称是什么，没有则使用
        //  邮件作为名字
        mail.setTo(toEmail.getText().toString());
        mail.setToEmail(toEmail.getText().toString());
        mail.setSubject(subject.getText().toString());
        if (isGroup){
            mail.setIsGroup(1);
        }
        if(data!=null){
            Document parse = Jsoup.parseBodyFragment(data);
            Elements imgs = parse.getElementsByTag("img");
            int i = 0;
            for(Element img : imgs){
                img.attr("src", img.attr("src").replace(" ","+"));
                System.out.println("替换了一个src");
            }
            String s = EditorFormat.header+ parse.body().toString()+EditorFormat.footer;
            mail.setData(s.getBytes());
            mail.setTextContent(Jsoup.clean(data, Whitelist.none()));
        }
        mail.setFrom(nickName);
        mail.setFromEmail(fromEmail.getText().toString());
        mail.setSendTime(new Date(System.currentTimeMillis()));
        mail.setMailBox("SENT");
        mail.setDraft(1);
        presenter.saveMail(mail);
    }

    @Override
    public void showSaveAsk() {

    }

    @Override
    public void showMail(Mail mail){
        oldMail = mail;
        String htmlText = null;
        //回复
        if (replyMode == 1) {
            toEmail.setText(mail.getFromEmail());
            subject.setText("回复：" + mail.getSubject());
            if (mail.getData()!=null) {
                Document parse = Jsoup.parseBodyFragment(new String(mail.getData()));
                Elements pngs = parse.select("img[src]");
                for (Element element : pngs) {
                    String imgUrl = element.attr("src");
                    if (imgUrl.trim().startsWith("data")) {
                        imgUrl = imgUrl.replace(" ", "+");
                        element.attr("src", imgUrl);
                    }
                }
                htmlText = "<div><br/><br/><div>" +
                        "<span style=\"font-size: 10.5pt; line-height: 1.5; background-color: transparent;\">" +
                        "---原始邮件---</span><br/></div></div>\n" + parse.body().html();
                richEditor.setHtml(htmlText);
            }
        }
        //草稿
        else if (replyMode == 2) {
            toEmail.setText(mail.getToEmail());
            subject.setText(mail.getSubject());
            if (mail.getData()!=null) {
                Document parse = Jsoup.parseBodyFragment(new String(mail.getData()));
                Elements pngs = parse.select("img[src]");
                for (Element element : pngs) {
                    String imgUrl = element.attr("src");
                    if (imgUrl.trim().startsWith("data")) {
                        imgUrl = imgUrl.replace(" ", "+");
                        element.attr("src", imgUrl);
                    }
                }
                htmlText = parse.html();
                richEditor.setHtml(htmlText);
            }
        }
    }

    @Override
    public void sendSuccess() {
        makeToast("发送成功");
        if (replyMode == 2){
            Intent intent = new Intent("deleteItem");
            intent.putExtra("position",currentIndex);
            getActivity().sendBroadcast(intent);
            loadingDialog.dismiss();
            loadingDialog = null;
            getActivity().finish();
        }else{
            loadingDialog.dismiss();
            loadingDialog = null;
            getActivity().finish();
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

    @Override
    public void saveSuccess() {
        makeToast("保存成功");
        getActivity().finish();
    }

    @Override
    public void showError() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(),"发生错误",Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });
    }

    @Override
    public void setPresenter(EditMailContract.Presenter presenter) {
        this.presenter = presenter;
    }

    /**
     * Fragment生命周期开始时, 通知Presenter的Start()开始初始化数据
     */
    @Override
    public void onResume() {
        super.onResume();
        try {
            presenter.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onBackPressed(){
        if(canSend){
            showDialog();
        }else{
            getActivity().finish();
        }
    }

    private void showDialog(){
        if(customDialog == null){
            customDialog = new CustomDialog(getContext());
            customDialog.showMiddleButton();
            customDialog.setOnClickListener(new CustomDialog.OnClickListener() {
                @Override
                public void cancel() {
                    customDialog.dismiss();
                }

                @Override
                public void save() {
                    richEditor.getFinalHtml();
                }

                @Override
                public void confirm() {
                    getActivity().finish();
                }
            });
        }
        customDialog.show();
    }

    private void showSelectSenderDialog(){
        if(selectSenderDialog == null){
            selectSenderDialog = new SelectSenderDialog(getContext());
            selectSenderDialog.setUserList(UserInfo.users);
            selectSenderDialog.setOnClickListener(new SelectSenderDialog.OnClickListener() {
                @Override
                public void click(int position) {
                    fromEmail.setText(UserInfo.users.get(position).getSmtpUserName());
                    nickName = UserInfo.users.get(position).getNickName();
                    selectSenderDialog.dismiss();
                }
            });
        }
        selectSenderDialog.show();
    }

    private void showSelectContactDialog(){
        System.out.println("click" + selectContactDialog);
        if(selectContactDialog!=null) {
            selectContactDialog.show();
        }else{
            Toast.makeText(getContext(),"通讯录无联系人",Toast.LENGTH_SHORT).show();
        }
    }


    private void showBottomDialog(int position){
        //1、使用Dialog、设置style
        final Dialog dialog = new Dialog(getContext(),R.style.BottomDialogStyle);
        dialog.setCanceledOnTouchOutside(true);
        //2、设置布局
        View view = View.inflate(getContext(),R.layout.dialog_file_option,null);
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
        buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileListAdapter.deleteAttachment(position);
                if(fileListAdapter.getLength() == 0){
                    buttonFile.setImageResource(R.drawable.ic_file);
                }
                dialog.dismiss();
            }
        });
        TextView buttonSecond = dialog.findViewById(R.id.second);
        buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String base64 = getBase64FromUri(fileListAdapter.getAttachment(position).getUri());
                richEditor.insertImage("data:image/jpeg;base64,"+base64,"image");
                dialog.dismiss();
            }
        });
    }
    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                               int oldTop, int oldRight, int oldBottom) {
        if(oldBottom!=0 && bottom!=0 && (oldBottom-bottom>keyHeight)){
            bottomToolBar.setVisibility(View.VISIBLE);
        }else if(oldBottom!=0 && bottom!=0 && (bottom-oldBottom>keyHeight)){
            bottomToolBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(customDialog!=null){
            customDialog.dismiss();
            customDialog = null;
        }
        if(loadingDialog!=null){
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }
    private InputStream getInputStreamFromUri(Context context,Uri uri){
        if (uri == null) {
            return null;
        }

        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_CONTENT.equals(scheme) ||
                ContentResolver.SCHEME_FILE.equals(scheme)) {
            InputStream stream = null;
            try {
                stream = context.getContentResolver().openInputStream(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return stream;
        }
        return null;
    }

    @Override
    public void showContactList(List<Contact> contacts){
        if (contacts.size() > 0) {
            selectContactDialog = new SelectSenderDialog(getContext());
            List<User> userList = new ArrayList<User>();
            for (Contact c : contacts) {
                User user = new User();
                user.setPopUserName(c.getMailAddress());
                user.setNickName(c.getUserName());
                userList.add(0, user);
            }
            contactList = userList;
            selectContactDialog.setUserList(userList);
            selectContactDialog.setOnClickListener(new SelectSenderDialog.OnClickListener() {
                @Override
                public void click(int position) {
                    if (toEmail.getText().toString().trim().length() > 0) {
                        toEmail.append(";");
                        toEmail.append(contactList.get(position).getPopUserName());
                    } else {
                        toEmail.append(contactList.get(position).getPopUserName());
                    }
                    selectContactDialog.dismiss();
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*结果回调*/
        if (requestCode == 0x11) {
            if (data != null) {
                String base64 = getBase64FromUri(data.getData());
                richEditor.insertImage("data:image/jpeg;base64,"+base64,"image");
            }
        }
        else if (requestCode == 0x12){
            if(data!=null){
                try {
                    Uri uri = data.getData();
                    System.out.println("文件："+uri.getAuthority());
//            /*这里要调用这个getPath方法来能过uri获取路径不能直接使用uri.getPath。
//            因为如果选的图片的话直接使用得到的path不是图片的本身路径*/
                    String name = getFileRealNameFromUri(getContext(),uri);
                    String end = name.substring(name.lastIndexOf(".") + 1,
                            name.length()).toLowerCase();
                    String fileType = "";
                    if (end.equals("jpg") || end.equals("gif") || end.equals("png") ||
                            end.equals("jpeg") || end.equals("bmp")) {
                        fileType = "image/"+end;
                    }else{
                        fileType = end;
                    }
                    /* 取得扩展名 */
                    Attachment attachment = new Attachment();
                    Cursor cursor = getContext().getContentResolver().query(uri, null,
                            null, null, null);
                    cursor.moveToFirst();
                    attachment.setFileType(fileType);
                    attachment.setName(name);
                    attachment.setUri(uri);
                    attachment.setSize(getSizeFromUri(uri));
                    fileListAdapter.addAttachment(attachment);
                    buttonFile.setImageResource(R.drawable.ic_file_selected);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getContext(),"暂不支持该格式的文件",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private String getBase64FromUri(Uri uri){
        System.out.println("选择相册：" + uri);
        InputStream inputStream = getInputStreamFromUri(getContext(),uri);
        String base64 = null;
        if(inputStream!=null){
            try {
                int l = inputStream.available();
                byte[] bytes = new byte[l];
                inputStream.read(bytes);
                System.out.println("长度0" + l);
                base64 = android.util.Base64.encodeToString(bytes, Base64.NO_WRAP);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return base64;
    }

    private int getSizeFromUri(Uri uri){
        System.out.println("选择相册：" + uri);
        InputStream inputStream = getInputStreamFromUri(getContext(),uri);
        int size = 0;
        try {
            size = inputStream.available();
        }catch (Exception e){
            e.printStackTrace();
        }
        return size;
    }

    private String getBase64FromPath(String path){
        File file = new File(path);
        if(!file.exists()){
            file = new File(getContext().getFilesDir(),path);
        }
        String base64 = null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] bytes = new byte[in.available()];
            base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return base64;
    }


    public static String getFileRealNameFromUri(Context context, Uri fileUri) {
        if (context == null || fileUri == null) return null;
        DocumentFile documentFile = DocumentFile.fromSingleUri(context, fileUri);
        if (documentFile == null) return null;
        return documentFile.getName();
    }

    /**
     * 转换文件大小成KB  M等
     */
    public static String FormentFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    /**
     * 工具：存储附件
     * @param fname
     * @param msg
     * @return
     */
    private boolean save2File(String fname, byte[] msg) throws IOException {
        File createFiles = new File(getContext().getFilesDir(), fname);
        createFiles.createNewFile();
        FileOutputStream fos = getContext().openFileOutput( fname, Context.MODE_PRIVATE );
        try{
            fos.write(msg);
            fos.flush();
            return true;
        }catch (FileNotFoundException e){
            e.printStackTrace();
            return false;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
        finally{
            if (fos != null) {
                try{
                    fos.close();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
