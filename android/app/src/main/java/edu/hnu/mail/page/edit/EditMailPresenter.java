package edu.hnu.mail.page.edit;

import androidx.annotation.NonNull;

import java.util.List;

import edu.hnu.mail.data.entity.Contact;
import edu.hnu.mail.data.entity.Mail;
import edu.hnu.mail.data.source.ContactDataSource;
import edu.hnu.mail.data.source.MailBoxDataSource;
import edu.hnu.mail.data.source.MailDataSource;

public class EditMailPresenter implements EditMailContract.Presenter {

    private EditMailContract.View mView;

    /**
     * 注意，这个是一个接口，它可以实例化成本地邮件仓库和远程邮件仓库
     */
    private MailDataSource mModel;

    private ContactDataSource contactDataSource;

    /**
     * 构造Presenter
     * @param view 契约类的View接口，其定义了View的所有UI更新操作
     * @param model 本地仓库或远程仓库
     */
    public EditMailPresenter(@NonNull EditMailContract.View view, @NonNull MailDataSource model,
                             @NonNull ContactDataSource contactDataSource) {
        mView = view;
        mModel = model;
        this.contactDataSource = contactDataSource;
    }

    /**
     * Presenter的start方法，会被View层初始化时调用进行数据初始化
     */
    @Override
    public void start() {
        //do nothing here, cause we need a mailId to getMailInfo
    }

    @Override
    public void sendMail(Mail mail) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mModel.sendMail(mail, new MailDataSource.UpdateCallBack() {
                        @Override
                        public void onUpdateSuccess() {
                            mView.sendSuccess();
                        }

                        @Override
                        public void onUpdateFailure() {
                            mView.showError();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void saveMail(Mail mail) {
        mModel.addMail(mail, new MailDataSource.UpdateCallBack() {
            @Override
            public void onUpdateSuccess() {
                mView.saveSuccess();
            }

            @Override
            public void onUpdateFailure() {
                mView.showError();
            }
        });
    }

    @Override
    public void getMail(String userName,String mailBox,String uid) {
        Mail mail = new Mail();
        mail.setUserAddress(userName);
        mail.setMailBox(mailBox);
        mail.setUid(uid);
        mModel.getMailById(mail, new MailDataSource.LoadCallBack() {
            @Override
            public void onLoadSuccess(Mail mail) {
                mView.showMail(mail);
            }

            @Override
            public void onLoadFailure() {
                mView.showError();
            }
        });
    }

    @Override
    public void getContactList(){
        System.out.println("getContactListPresenter call");
        contactDataSource.getContactList(new ContactDataSource.LoadCallBack() {
            @Override
            public void onLoadSuccess(List<Contact> list) {
                mView.showContactList(list);
            }

            @Override
            public void onLoadFailure() {

            }
        });
    }
}
