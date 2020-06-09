package edu.hnu.mail.data.source.local;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.List;

import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.data.dao.DaoManager;
import edu.hnu.mail.data.dao.DaoSession;
import edu.hnu.mail.data.dao.MailDao;
import edu.hnu.mail.data.entity.Mail;
import edu.hnu.mail.data.source.MailBoxDataSource;

/**
 * 获取本地的邮箱列表
 */
public class LocalMailBoxDataSource implements MailBoxDataSource {

    private DaoSession daoSession;
    private MailDao mailDao;

    public LocalMailBoxDataSource(Context context){
        daoSession = DaoManager.getInstance(context).getDaoSession();
        mailDao = daoSession.getMailDao();
    }

    /**
     * 发起统计各个邮箱数据的邮件数(如收件箱193封邮件)，将整数列表交给回调
     * @param loadCallBack
     */
    @Override
    public void getMailBox(@NonNull LoadCallBack loadCallBack) {
        String userName = UserInfo.users.get(UserInfo.currentIndex).getPopUserName();
        int[] list = new int[5];
        list[0] = 0;
        list[1] = 0;
        list[2] = 0;
        list[3] = 0;
        list[4] = 0;
        Long i = mailDao.queryBuilder()
                .where(MailDao.Properties.UserAddress.eq(userName),
                        MailDao.Properties.MailBox.eq("INBOX"),
                        MailDao.Properties.Deleted.eq(0),
                        MailDao.Properties.RealDeleted.eq(0)
                        )
                .count();
        list[0] = i.intValue();
        i = mailDao.queryBuilder()
                .where(MailDao.Properties.UserAddress.eq(userName),
                        MailDao.Properties.MailBox.eq("SENT"),
                        MailDao.Properties.Draft.eq(1),
                        MailDao.Properties.RealDeleted.eq(0))
                .count();
        list[1] = i.intValue();
        i = mailDao.queryBuilder()
                .where(MailDao.Properties.UserAddress.eq(userName),
                        MailDao.Properties.MailBox.eq("SENT"),
                        MailDao.Properties.Deleted.eq(0),
                        MailDao.Properties.Draft.eq(0),
                        MailDao.Properties.RealDeleted.eq(0)
                )
                .count();
        list[2] = i.intValue();
        i = mailDao.queryBuilder()
                .where(MailDao.Properties.UserAddress.eq(userName),
                        MailDao.Properties.Deleted.eq(1),
                        MailDao.Properties.RealDeleted.eq(0))
                .count();
        list[3] = i.intValue();
        i = mailDao.queryBuilder()
                .where(MailDao.Properties.UserAddress.eq(userName),
                        MailDao.Properties.MailBox.eq("RUBBISH"),
                        MailDao.Properties.RealDeleted.eq(0))
                .count();
        list[4] = i.intValue();
        //获取成功回调
        loadCallBack.onLoadSuccess(list);

        //获取失败回调
//        loadCallBack.onLoadFailure();
    }

    /**
     *  从远程仓库获得查询新邮件，并存入本地数据库
     * @param loadCallBack
     */
    @Override
    public void getNewMailBox(@NonNull LoadCallBack loadCallBack) {
        // TODO: 2020/4/12 待完成数据获取
        int[] list = new int[5];
        list[0] = 0;
        list[1] = 0;
        list[2] = 0;
        list[3] = 0;
        list[4] = 0;
        //获取成功回调
        loadCallBack.onLoadSuccess(list);
        //获取失败回调
//        loadCallBack.onLoadFailure();
    }

    @Override
    public void getUserMailBox(String userName, @NonNull LoadCallBack loadCallBack) {

    }
}
