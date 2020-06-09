package edu.hnu.mail.page.edit;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import edu.hnu.mail.R;
import edu.hnu.mail.data.source.local.LocalContactDataSource;
import edu.hnu.mail.data.source.local.LocalMailDataSource;
import edu.hnu.mail.util.StatusBarUtil;

public class EditMailActivity extends AppCompatActivity {

    EditMailFragment fragment;
    private EditMailPresenter presenter;

    @Override
    protected void onCreate(Bundle saveInstance){
        super.onCreate(saveInstance);
        setContentView(R.layout.activity_edit_mail);
        overridePendingTransition(R.anim.activity_in_right,R.anim.activity_stay);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragment = new EditMailFragment();
        Intent intent = getIntent();
        String mailId = intent.getStringExtra("mailId");
        String userName = intent.getStringExtra("userName");
        String mailBox =intent.getStringExtra("mailBox");
        String receiver = intent.getStringExtra("receiver");
        String nickName = intent.getStringExtra("nickName");
        int currentIndex = intent.getIntExtra("currentIndex",0);
        if (mailId!=null){
            if(mailBox.equalsIgnoreCase("INBOX")){
                fragment.setMode(1,mailId,mailBox,nickName,userName);
            }else{
                fragment.setMode(2,mailId,mailBox,nickName,userName);
            }
        }
        boolean isGroup = intent.getBooleanExtra("isGroup",false);
        if (isGroup){
            fragment.setGroup();
        }
        fragment.setReceiver(receiver);
        presenter = new EditMailPresenter(fragment, new LocalMailDataSource(this),
                new LocalContactDataSource(this));
        fragment.setPresenter(presenter);
        fragmentTransaction.replace(R.id.fragment, fragment);
        fragmentTransaction.commit();
        StatusBarUtil.whiteBgAndBlackFont(getWindow());


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            fragment.onBackPressed();
            return true;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }
}