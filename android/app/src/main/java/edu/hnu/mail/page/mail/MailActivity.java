package edu.hnu.mail.page.mail;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import edu.hnu.mail.R;
import edu.hnu.mail.data.entity.Mail;
import edu.hnu.mail.util.StatusBarUtil;

/**
 * 该界面是第一个界面，为邮箱列表(收件箱、已发送、已删除、垃圾箱、草稿箱)
 * author: 周世焕
 */
public class MailActivity extends AppCompatActivity{

    private MailContract.View view;
    private MailContract.Presenter presenter;
    private MailFragment mailFragment;

    @Override
    protected void onCreate(Bundle saveInstance){
        super.onCreate(saveInstance);
        setContentView(R.layout.activity_mail);
        overridePendingTransition(R.anim.activity_in_right,R.anim.activity_stay);
        StatusBarUtil.whiteBgAndBlackFont(getWindow());
        String mailId = getIntent().getStringExtra("mailId");
        int mailType = getIntent().getIntExtra("mailType",1);
        String mailBox = getIntent().getStringExtra("mailBox");
        int mailNo = getIntent().getIntExtra("mailNo",0);
        String userName = getIntent().getStringExtra("userName");
        initData(userName,mailId,mailNo,mailBox,mailType);
    }

    private void initData(String userName,String mailId,int mailNo,String mailBox,int mailType){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mailFragment = new MailFragment(userName,mailId,mailNo,mailBox,mailType);
        fragmentTransaction.replace(R.id.fragment, mailFragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                int s = event.getPointerCount();
                if(s>1){
                    mailFragment.setWebViewDisallow(true);
                }else{

                }
                break;
            case MotionEvent.ACTION_CANCEL:
            default:
                mailFragment.setWebViewDisallow(false);
                break;
        }
        return super.dispatchTouchEvent(event);
    }
}
