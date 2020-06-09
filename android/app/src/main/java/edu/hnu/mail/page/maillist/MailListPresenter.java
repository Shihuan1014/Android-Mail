package edu.hnu.mail.page.maillist;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.List;

import edu.hnu.mail.constant.MailType;
import edu.hnu.mail.data.entity.Mail;
import edu.hnu.mail.data.source.MailBoxDataSource;
import edu.hnu.mail.data.source.MailDataSource;
import edu.hnu.mail.data.source.MailListDataSource;

public class MailListPresenter implements MailListContract.Presenter {

    private MailListContract.View mView;
    /**
     * 注意，这个是一个接口，它可以实例化成本地邮件仓库和远程邮件仓库
     */
    private MailListDataSource mModel;
    private MailDataSource mailDataSource;

    //邮箱类型
    private int type;

    /**
     * 构造Presenter
     * @param view 契约类的View接口，其定义了View的所有UI更新操作
     * @param model 本地仓库或远程仓库
     */
    public MailListPresenter(@NonNull MailListContract.View view, @NonNull MailListDataSource model,
                             @NonNull MailDataSource mailDataSource,
                             int type) {
        mView = view;
        mModel = model;
        this.mailDataSource = mailDataSource;
        this.type = type;
    }

    @Override
    public void start() {
        /**
         * 初始化Model层数据，填写成功回调和失败回调
         */
        mModel.getMailList(new MailListDataSource.LoadCallBack() {
            /**
             * 邮箱获得成功，需要通知View层更新界面
             * @param list
             */
            @Override
            public void onLoadSuccess(List<Mail> list) {
                mView.showMailList(list);
            }

            /**
             * 邮箱获得失败，需要通知View层提示失败信息
             */
            @Override
            public void onLoadFailure() {
                mView.showLoadError();
            }
        },type);
    }

    @Override
    public void getNewMail() throws Exception {
        boolean isAllInbox = false;
        if(type== MailType.ALL_INBOX){
            isAllInbox = true;
        }
        mModel.getNewMailList(new MailListDataSource.LoadCallBack() {
            @Override
            public void onLoadSuccess(List<Mail> list) {
                mView.showNewMailList(list);
            }

            @Override
            public void onLoadFailure() {
                mView.showLoadError();
            }
        },isAllInbox);
    }

    @Override
    public void getNewMailLocal(Date date){
        mModel.getNewMailListLocal(new MailListDataSource.LoadCallBack() {
            @Override
            public void onLoadSuccess(List<Mail> list) {
                mView.showNewMailList(list);
            }

            @Override
            public void onLoadFailure() {
                mView.showLoadError();
            }
        },type,date);
    }

    @Override
    public void setSeenMail(int position,Mail mail) {
        mailDataSource.setMailSeen(mail, new MailDataSource.UpdateCallBack() {
            @Override
            public void onUpdateSuccess() {
                mView.changeSeen(position,mail.getSeen());
            }

            @Override
            public void onUpdateFailure() {
                mView.showLoadError();
            }
        });
    }

    @Override
    public void deleteMail(int position,Mail mail) {
        mailDataSource.deleteMailById(mail, new MailDataSource.UpdateCallBack() {
            @Override
            public void onUpdateSuccess() {
                mView.removeMail(position);
            }

            @Override
            public void onUpdateFailure() {
                mView.showLoadError();
            }
        });
    }
}
