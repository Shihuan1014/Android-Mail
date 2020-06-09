package edu.hnu.mail.page.edit;

import java.util.List;

import edu.hnu.mail.BasePresenter;
import edu.hnu.mail.BaseView;
import edu.hnu.mail.data.entity.Contact;
import edu.hnu.mail.data.entity.Mail;

public class EditMailContract {

    interface View extends BaseView<Presenter> {
        //弹出保存草稿的dialog
        void showSaveAsk();
        //发送成功
        void sendSuccess();
        //保存成功
        void saveSuccess();
        //失败
        void showError();
        //回填邮件
        void showMail(Mail mail);

        void showContactList(List<Contact> list);
    }

    interface Presenter extends BasePresenter {
        /**
         * 发送邮件
         * @param mail
         */
        void sendMail(Mail mail);

        /**
         * 保存草稿
         * @param mail
         */
        void saveMail(Mail mail);

        /**
         * 获得邮件用于回填
         * @param uid
         */
        void getMail(String userName,String mailBox,String uid);

        void getContactList();
    }
}
