package edu.hnu.mail.data.source;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import edu.hnu.mail.data.entity.Mail;

public interface MailListDataSource {
    interface LoadCallBack{

        void onLoadSuccess(List<Mail> list);

        void onLoadFailure();
    }

    /**
     * 初始化时，获得邮件列表
     * @param loadCallBack
     */
    void getMailList(@NonNull LoadCallBack loadCallBack,int type);

    /**
     * 用户下拉刷新时获取新数据，因为POP3是不保持连接的，所以初始化后需要用户自行获取新邮件
     * @param loadCallBack
     */
    void getNewMailList(@NonNull LoadCallBack loadCallBack,boolean isAllInboxMail) throws Exception;

    void getNewMailListLocal(@NonNull LoadCallBack loadCallBack, int type, Date date);
}
