package edu.hnu.mail.page.maillist;

import java.util.Date;
import java.util.List;

import edu.hnu.mail.BasePresenter;
import edu.hnu.mail.BaseView;
import edu.hnu.mail.data.entity.Mail;

public class MailListContract {

    interface View extends BaseView<Presenter> {
        // 显示邮箱列表
        void showMailList(List<Mail> list);
        // 增加新的邮箱数量(在前面的基础上增加)
        void showNewMailList(List<Mail> list);
        // 显示加载错误
        void showLoadError();
        // 隐藏加载错误
        void hideLoadError();
        // 删除邮件
        void removeMail(int position);
        // 设置邮件是否已读
        void changeSeen(int position,int seen);
    }

    interface Presenter extends BasePresenter {
        void getNewMail() throws Exception;
        void setSeenMail(int position,Mail mail);
        void deleteMail(int position,Mail mail);
        void getNewMailLocal(Date date);
    }
}
