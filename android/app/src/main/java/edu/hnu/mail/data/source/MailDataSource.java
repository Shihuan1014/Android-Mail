package edu.hnu.mail.data.source;

import androidx.annotation.NonNull;

import java.util.List;

import edu.hnu.mail.data.entity.Mail;

public interface MailDataSource {

    interface LoadCallBack{

        void onLoadSuccess(Mail mail);

        void onLoadFailure();
    }

    interface UpdateCallBack{
        void onUpdateSuccess();
        void onUpdateFailure();
    }

    /**
     * 根据id获取邮件信息
     * @param mail
     * @param loadCallBack
     */
    void getMailById(Mail mail,@NonNull LoadCallBack loadCallBack);

    /**
     * 删除邮件
     * @param mail
     * @param updateCallBack
     */
    void deleteMailById(Mail mail,@NonNull UpdateCallBack updateCallBack);

    /**
     * 添加邮件
     * @param mail
     * @param updateCallBack
     */
    void addMail(Mail mail,@NonNull UpdateCallBack updateCallBack);

    /**
     * 发送邮件
     * @param mail
     * @param updateCallBack
     */
    void sendMail(Mail mail,@NonNull UpdateCallBack updateCallBack);

    /**
     * 星标邮件
     */
    void setMailFlag(Mail mail,@NonNull UpdateCallBack updateCallBack);

    /**
     * 已读
     */
    void setMailSeen(Mail mail,@NonNull UpdateCallBack updateCallBack);
}
