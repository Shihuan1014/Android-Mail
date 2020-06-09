package edu.hnu.mail.page.mail;

import edu.hnu.mail.BasePresenter;
import edu.hnu.mail.BaseView;
import edu.hnu.mail.data.entity.Mail;

public class MailContract {

    interface View extends BaseView<Presenter> {

        //渲染邮件
        void showMail(Mail mail);
        // 显示加载错误
        void showLoadError();
        // 隐藏加载错误
        void hideLoadError();
        //删除成功
        void deleteSuccess();
        //
        void setFlagSuccess(int flag);
        //
        void setSeenSuccess(int seen);
    }

    interface Presenter extends BasePresenter {
        void getMail(Mail mail);
        void deleteMail(Mail mail);
        void setMailFlag(Mail mail);
        void setMailSeen(Mail mail);
    }
}
