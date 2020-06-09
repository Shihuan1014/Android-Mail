package edu.hnu.mail.page.mailbox;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;

import edu.hnu.mail.R;
import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.data.entity.User;
import edu.hnu.mail.data.source.MailBoxDataSource;
import edu.hnu.mail.data.source.UserDataSource;
import edu.hnu.mail.data.source.local.LocalUserDataSource;
import edu.hnu.mail.data.source.remote.POP3Client;

public class MailBoxPresenter implements MailBoxContract.Presenter {

    private MailBoxContract.View mView;

    /**
     * 注意，这个是一个接口，它可以实例化成本地邮件仓库和远程邮件仓库
     */
    private MailBoxDataSource mModel;

    /**
     * 用户数据库
     */
    private UserDataSource userModel;

    /**
     * 构造Presenter
     * @param view 契约类的View接口，其定义了View的所有UI更新操作
     * @param model 本地仓库或远程仓库
     */
    public MailBoxPresenter(@NonNull MailBoxContract.View view,@NonNull MailBoxDataSource model,
                            @NonNull UserDataSource userModel) {
        mView = view;
        mModel = model;
        this.userModel = userModel;
    }

    /**
     * Presenter的start方法，会被View层初始化时调用进行数据初始化
     */
    @Override
    public void start() {
        userModel.getAllUser(new UserDataSource.LoadCallBack() {
            @Override
            public void onLoadSuccess(List<User> users) {
                if(users!=null && users.size()>0){
                    UserInfo.users.clear();
                    UserInfo.users.addAll(users);
                    mView.showUserMailData();
                    /**
                     * 初始化Model层数据，填写成功回调和失败回调
                     */
                    mModel.getMailBox(new MailBoxDataSource.LoadCallBack() {
                        /**
                         * 邮箱获得成功，需要通知View层更新界面
                         * @param list
                         */
                        @Override
                        public void onLoadSuccess(int[] list) {
                            mView.showMailCountList(list);
                            try {
                                getNewMailCount();
                            }catch (Exception e){
                                mView.showLoadError();
                                e.printStackTrace();
                            }
                        }

                        /**
                         * 邮箱获得失败，需要通知View层提示失败信息
                         */
                        @Override
                        public void onLoadFailure() {
                            mView.showLoadError();
                        }

                        @Override
                        public void showLoading() {

                        }
                    });
                }else{
                    mView.showLoginFragment();
                    return;
                }
            }

            @Override
            public void onLoadFailure() {
                mView.showLoginFragment();
                return;
            }
        });
    }

    /**
     * 给MailBoxFragment用的，用来初始化某用户的所有邮箱数量
     * @param userName
     */
    @Override
    public void initUserMailBox(String userName){
        mModel.getUserMailBox(userName, new MailBoxDataSource.LoadCallBack() {
            @Override
            public void onLoadSuccess(int[] list) {
                mView.showMailCountList(list);
            }

            @Override
            public void onLoadFailure() {
                mView.showLoadError();
            }

            @Override
            public void showLoading() {

            }
        });
    }


    public void getNewMailCount(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mModel.getNewMailBox(new MailBoxDataSource.LoadCallBack() {
                        @Override
                        public void onLoadSuccess(int[] list) {
                            mView.showNewMailCountList(list);
                        }

                        @Override
                        public void onLoadFailure() {
                            mView.showLoadError();
                        }

                        @Override
                        public void showLoading() {

                        }
                    });
                } catch (Exception e) {
                    mView.showLoadError();
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
