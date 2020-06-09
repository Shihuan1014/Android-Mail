package edu.hnu.mail.page;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.tencent.smtt.sdk.QbSdk;

import java.util.Date;

import edu.hnu.mail.R;
import edu.hnu.mail.page.edit.EditMailActivity;
import edu.hnu.mail.page.mailbox.MainFragment;
import edu.hnu.mail.page.setting.SettingFragment;
import edu.hnu.mail.util.StatusBarUtil;

/**
 * 该界面是第一个界面，为邮箱列表(收件箱、已发送、已删除、垃圾箱、草稿箱)
 * author: 周世焕
 */
public class MainActivity extends AppCompatActivity{
    //Floating Button
    private FloatingActionMenu actionMenu;
    private FloatingActionButton buttonEdit;
    private FloatingActionButton buttonSetting;
    public static long startAppTime = System.currentTimeMillis();

    public interface FragmentAction{
        void showMenu();
        void hideMenu();
    }

    @Override
    protected void onCreate(Bundle saveInstance){
        super.onCreate(saveInstance);
        overridePendingTransition(R.anim.activity_in_right,R.anim.activity_stay);
        setContentView(R.layout.activity_main);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        MainFragment mainFragment = new MainFragment();
        mainFragment.setFragmentActionListener(new FragmentAction() {
            @Override
            public void showMenu() {
                actionMenu.showMenu(false);
            }

            @Override
            public void hideMenu() {
                actionMenu.hideMenu(false);
            }
        });
        fragmentTransaction.replace(R.id.fragment, mainFragment);
        fragmentTransaction.commit();
        StatusBarUtil.whiteBgAndBlackFont(getWindow());

        actionMenu = findViewById(R.id.menu);
        actionMenu.setClosedOnTouchOutside(true);
        buttonEdit = findViewById(R.id.button_edit);
        buttonSetting = findViewById(R.id.button_setting);

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionMenu.close(false);
                Intent intent = new Intent(MainActivity.this, EditMailActivity.class);
                startActivity(intent);
            }
        });

        buttonSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionMenu.close(false);
                actionMenu.hideMenu(false);
                FragmentTransaction transaction = MainActivity.this.getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(
                        R.anim.fragment_slide_right_enter, R.anim.fragment_slide_left_exit,
                        R.anim.fragment_slide_left_enter, R.anim.fragment_slide_right_exit);
                transaction.replace(R.id.fragment,new SettingFragment());
                transaction.addToBackStack(MainActivity.this.getClass().getName());
                transaction.commit();
            }
        });

        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                // TODO Auto-generated method stub
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Log.d("app", " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                // TODO Auto-generated method stub
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(),  cb);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
//            getSupportFragmentManager().popBackStack();
//            System.out.println("MainActivity got");
//            return true;
//        }else {
//            return super.onKeyDown(keyCode, event);
//        }
//    }
}
