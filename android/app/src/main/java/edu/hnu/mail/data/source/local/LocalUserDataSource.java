package edu.hnu.mail.data.source.local;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import edu.hnu.mail.constant.DefaultServer;
import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.data.dao.AttachmentDao;
import edu.hnu.mail.data.dao.DaoManager;
import edu.hnu.mail.data.dao.DaoSession;
import edu.hnu.mail.data.dao.DataDao;
import edu.hnu.mail.data.dao.MailDao;
import edu.hnu.mail.data.dao.UserDao;
import edu.hnu.mail.data.entity.Attachment;
import edu.hnu.mail.data.entity.Data;
import edu.hnu.mail.data.entity.User;
import edu.hnu.mail.data.source.UserDataSource;
import edu.hnu.mail.util.HttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LocalUserDataSource implements UserDataSource {

    private DaoSession daoSession;
    private UserDao userDao;
    private MailDao mailDao;
    private DataDao dataDao;
    private AttachmentDao attachmentDao;

    // 请务必使用此构造函数
    public LocalUserDataSource(Context context){
        daoSession = DaoManager.getInstance(context).getDaoSession();
        userDao = daoSession.getUserDao();
        mailDao = daoSession.getMailDao();
        dataDao = daoSession.getDataDao();
        attachmentDao = daoSession.getAttachmentDao();
    }

    @Override
    public void getAllUser(LoadCallBack loadCallBack) {
        List<User> list = userDao.queryBuilder()
                .list();
        for (User user : list){
            long count = mailDao.queryBuilder()
                    .where(MailDao.Properties.UserAddress.eq(user.getPopUserName()),
                            MailDao.Properties.MailBox.eq("INBOX"),
                            MailDao.Properties.Deleted.eq(0))
                    .count();
            user.setMailInboxCount((int) count);
        }
        loadCallBack.onLoadSuccess(list);
    }

    @Override
    public void deleteUserByKey(long id,UpdateCallBack updateCallBack) {
        User user = userDao.queryBuilder().
                where(UserDao.Properties.UserId.eq(id))
                .build().unique();
        String strSql = "delete from " + mailDao.getTablename()
                + " where (" + MailDao.Properties.UserAddress.columnName + " = '" +
                user.getPopUserName() + "' and " + MailDao.Properties.MailBox.columnName
                + " = 'INBOX') or (" + MailDao.Properties.UserAddress.columnName + " = '" +
                user.getSmtpUserName() + "' and " + MailDao.Properties.MailBox.columnName
                + " = 'SENT')";
        userDao.deleteByKey(id);
        daoSession.getDatabase().execSQL(strSql);
        strSql = "delete from " + dataDao.getTablename()
                + " where (" + MailDao.Properties.UserAddress.columnName + " = '" +
                user.getPopUserName() + "' and " + MailDao.Properties.MailBox.columnName
                + " = 'INBOX') or (" + MailDao.Properties.UserAddress.columnName + " = '" +
                user.getSmtpUserName() + "' and " + MailDao.Properties.MailBox.columnName
                + " = 'SENT')";
        daoSession.getDatabase().execSQL(strSql);

        strSql = "delete from " + attachmentDao.getTablename()
                + " where (" + MailDao.Properties.UserAddress.columnName + " = '" +
                user.getPopUserName() + "' and " + MailDao.Properties.MailBox.columnName
                + " = 'INBOX') or (" + MailDao.Properties.UserAddress.columnName + " = '" +
                user.getSmtpUserName() + "' and " + MailDao.Properties.MailBox.columnName
                + " = 'SENT')";
        daoSession.getDatabase().execSQL(strSql);
        updateCallBack.onUpdateSuccess();
    }

    @Override
    public void addUser(User user,UpdateCallBack updateCallBack){
        long i = userDao.queryBuilder()
                .where(UserDao.Properties.PopUserName.eq(user.getPopUserName()),
                        UserDao.Properties.SmtpUserName.eq(user.getSmtpUserName()))
                .count();
        if (i == 0){
            userDao.insert(user);
        }
        updateCallBack.onUpdateSuccess();
    }

    @Override
    public void updateNickName(User user, UpdateCallBack updateCallBack){
        userDao.update(user);
        updateCallBack.onUpdateSuccess();
    }

    @Override
    public void updatePassword(User user,String newPassWord, UpdateCallBack updateCallBack) {
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        bodyBuilder.add("userName",user.getUserName());
        bodyBuilder.add("oldPass",user.getPopPassword());
        bodyBuilder.add("newPass",newPassWord);
        System.out.println("发起");
        Request.Builder builder = new Request.Builder()
                .url(DefaultServer.ADMIN_HOST+"/user").put(bodyBuilder.build());
        Request request = builder.build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                updateCallBack.onUpdateFailure();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if(jsonObject.getInt("status") == 200){
                        updateCallBack.onUpdateSuccess();
                        user.setSmtpPassword(newPassWord);
                        user.setPopPassword(newPassWord);
                        userDao.update(user);
                    }else{
                        updateCallBack.onUpdateFailure();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    updateCallBack.onUpdateFailure();
                }
            }
        });
    }
}
