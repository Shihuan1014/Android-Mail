package edu.hnu.mail.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import edu.hnu.mail.R;

public class CustomDialog extends Dialog {

    private TextView buttonCancel;
    private TextView buttonSave;
    private TextView buttonConfirm;
    private OnClickListener onClickListener;
    private TextView title;
    private TextView subTitle;

    //数据
    private String buttonCancelText = "取消";
    private String buttonSaveText = "保存邮件";
    private String buttonConfirmText = "离开";
    private String titleText = "离开写邮件";
    private String subTitleText = "已填写的邮件内容将丢失，或保存到草稿";
    private boolean buttonSaveVisible = false;
    private int xmlDialog = R.layout.dialog_leave_edit;

    public interface OnClickListener{
        void cancel();
        void save();
        void confirm();
    }

    public CustomDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
    }

    public void setXml(int xml){
        xmlDialog = xml;
    }

    public CustomDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected CustomDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setOnClickListener(OnClickListener clickListener){
        this.onClickListener = clickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(xmlDialog);
        //按空白处不能取消动画
        setCanceledOnTouchOutside(false);
        //初始化界面控件
        initView();
        //初始化界面控件的事件
        initEvent();
    }

    private void initView(){
        buttonCancel = findViewById(R.id.button_cancel);
        buttonCancel.setText(buttonCancelText);
        buttonConfirm = findViewById(R.id.button_confirm);
        buttonConfirm.setText(buttonConfirmText);
        buttonSave = findViewById(R.id.button_save);
        if(buttonSaveVisible){
            buttonSave.setVisibility(View.VISIBLE);
            buttonSave.setText(buttonSaveText);
        }
        title = findViewById(R.id.title);
        title.setText(titleText);
        subTitle = findViewById(R.id.subtitle);
        if (subTitle!=null)
        subTitle.setText(subTitleText);
    }

    private void initEvent(){
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.cancel();
            }
        });
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.save();
            }
        });
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.confirm();
            }
        });
    }

    public void showMiddleButton(){
        buttonSaveVisible = true;
    }

    public void setButtonCancelText(String text){
        buttonCancelText = text;
    }
    public void setButtonSaveText(String text){
        buttonSaveText = text;
    }
    public void setButtonConfirmText(String text){
        buttonConfirmText = text;
    }
    public void setTitle(String text){
        titleText = text;
    }
    public void setSubTitle(String text){
        subTitleText = text;
    }
}
