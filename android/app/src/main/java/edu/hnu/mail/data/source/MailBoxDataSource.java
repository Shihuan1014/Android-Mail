package edu.hnu.mail.data.source;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;

public interface MailBoxDataSource {

    interface LoadCallBack{

        void onLoadSuccess(int[] list);

        void onLoadFailure();

        void showLoading();
    }

    /**
     * 初始化时，获得邮箱数据(收件箱、草稿箱、已发送、已删除、垃圾箱)的邮件数
     * @param loadCallBack
     */
    void getMailBox(@NonNull LoadCallBack loadCallBack);

    /**
     * 用户下拉刷新时获取新数据，因为POP3是不保持连接的，所以初始化后需要用户自行获取新邮件
     * @param loadCallBack
     */
    void getNewMailBox(@NonNull LoadCallBack loadCallBack);

    /**
     * 获得某个用户的所有邮箱的邮件数
     * @param userName
     * @param loadCallBack
     */
    void getUserMailBox(String userName,@NonNull LoadCallBack loadCallBack);
}
