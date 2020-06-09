package edu.hnu.mail.page.manager;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import edu.hnu.mail.R;
import edu.hnu.mail.ui.mails.MailCompleteTextView;

public class AddUserDialog extends Dialog {
    private OnClickListener onClickListener;
    private TextView buttonCancel;
    private TextView buttonConfirm;
    private EditText username;
    private EditText password;
    //数据

    public interface OnClickListener{
        void cancel();
        void confirm(String username,String password);
    }

    public AddUserDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
    }

    public AddUserDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected AddUserDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setOnClickListener(OnClickListener clickListener){
        this.onClickListener = clickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_user);
        //按空白处不能取消动画
        setCanceledOnTouchOutside(false);
        //初始化界面控件
        initView();
        //初始化界面控件的事件
        initEvent();
    }

    private void initView(){
        buttonCancel = findViewById(R.id.button_cancel);
        buttonConfirm = findViewById(R.id.button_confirm);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
    }

    private void initEvent(){
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.cancel();
            }
        });
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.confirm(username.getText().toString(),password.getText().toString());
            }
        });
    }
}
