package edu.hnu.mail.page.mailbox;

import javax.mail.MessagingException;

import edu.hnu.mail.BasePresenter;
import edu.hnu.mail.BaseView;

public class MailBoxContract {

    interface View extends BaseView<Presenter> {
        // 显示邮箱列表
        void showMailCountList(int[] mailCountList);
        // 增加新的邮箱数量(在前面的基础上增加)
        void showNewMailCountList(int[] newMailCountList);
        //显示用户收件箱
        void showUserMailData();
        // 显示加载错误
        void showLoadError();
        // 隐藏加载错误
        void hideLoadError();
        // 跳转登录界面
        void showLoginFragment();
    }

    interface Presenter extends BasePresenter {
        void getNewMailCount();
        void initUserMailBox(String userName);
    }
}
