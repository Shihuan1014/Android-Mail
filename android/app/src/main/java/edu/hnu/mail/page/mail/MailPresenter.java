package edu.hnu.mail.page.mail;

import androidx.annotation.NonNull;

import edu.hnu.mail.data.entity.Mail;
import edu.hnu.mail.data.source.MailDataSource;

public class MailPresenter implements MailContract.Presenter {

    private MailContract.View mView;

    /**
     * 注意，这个是一个接口，它可以实例化成本地邮件仓库和远程邮件仓库
     */
    private MailDataSource mModel;

    /**
     * 构造Presenter
     * @param view 契约类的View接口，其定义了View的所有UI更新操作
     * @param model 本地仓库或远程仓库
     */
    public MailPresenter(@NonNull MailContract.View view, @NonNull MailDataSource model) {
        mView = view;
        mModel = model;
    }

    /**
     * Presenter的start方法，会被View层初始化时调用进行数据初始化
     */
    @Override
    public void start() {
        //do nothing here, cause we need a mailId to getMailInfo
    }

    @Override
    public void getMail(Mail mail) {
        /**
         * 初始化Model层数据，填写成功回调和失败回调
         */
        mModel.getMailById(mail,new MailDataSource.LoadCallBack() {
            /**
             * 邮箱获得成功，需要通知View层更新界面
             * @param mail
             */
            @Override
            public void onLoadSuccess(Mail mail) {
                mView.showMail(mail);
            }

            /**
             * 邮箱获得失败，需要通知View层提示失败信息
             */
            @Override
            public void onLoadFailure() {
                mView.showLoadError();
            }
        });
    }

    @Override
    public void deleteMail(Mail mail) {
        /**
         * 初始化Model层数据，填写成功回调和失败回调
         */
        mModel.deleteMailById(mail, new MailDataSource.UpdateCallBack() {
            @Override
            public void onUpdateSuccess() {
                mView.deleteSuccess();
            }

            @Override
            public void onUpdateFailure() {

            }
        });
    }

    @Override
    public void setMailFlag(Mail mail){
        mModel.setMailFlag(mail, new MailDataSource.UpdateCallBack() {
            @Override
            public void onUpdateSuccess() {
                mView.setFlagSuccess(mail.getFlag());
            }

            @Override
            public void onUpdateFailure() {
                mView.showLoadError();
            }
        });
    }

    @Override
    public void setMailSeen(Mail mail){
        mModel.setMailSeen(mail, new MailDataSource.UpdateCallBack() {
            @Override
            public void onUpdateSuccess() {
                mView.setSeenSuccess(mail.getSeen());
            }

            @Override
            public void onUpdateFailure() {
                mView.showLoadError();
            }
        });
    }
}
