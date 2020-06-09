package edu.hnu.mail.page.userinfo;

import android.service.autofill.UserData;

import androidx.annotation.NonNull;

import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.data.entity.User;
import edu.hnu.mail.data.source.MailBoxDataSource;
import edu.hnu.mail.data.source.UserDataSource;
import edu.hnu.mail.page.mailbox.MailBoxContract;

public class UserInfoPresenter implements UserInfoContact.Presenter{

    private UserInfoContact.View mView;

    /**
     * 注意，这个是一个接口，它可以实例化成本地邮件仓库和远程邮件仓库
     */
    private UserDataSource mModel;

    /**
     * 构造Presenter
     * @param view 契约类的View接口，其定义了View的所有UI更新操作
     * @param userModel 本地仓库
     */
    public UserInfoPresenter(@NonNull UserInfoContact.View view, @NonNull UserDataSource userModel){
        mView = view;
        this.mModel = userModel;
    }


    @Override
    public void deleteUser(long userId) {
        mModel.deleteUserByKey(userId, new UserDataSource.UpdateCallBack() {
            @Override
            public void onUpdateSuccess() {
                mView.deleteSuccess();
            }

            @Override
            public void onUpdateFailure() {
                mView.showFailure();
            }
        });
    }

    @Override
    public void updatePassword(User user,String newPassWord){
        mModel.updatePassword(user,newPassWord, new UserDataSource.UpdateCallBack() {
            @Override
            public void onUpdateSuccess() {
                mView.updatePassword(newPassWord);
            }

            @Override
            public void onUpdateFailure() {
                mView.showFailure();
            }
        });
    }



    @Override
    public void updateNickName(User user) {
        mModel.updateNickName(user, new UserDataSource.UpdateCallBack() {
            @Override
            public void onUpdateSuccess() {
                mView.updateNickName(user.getNickName());
            }

            @Override
            public void onUpdateFailure() {
                mView.showFailure();
            }
        });
    }
}
