package edu.hnu.mail.data.source;

import java.util.List;

import edu.hnu.mail.data.entity.Mail;
import edu.hnu.mail.data.entity.User;

public interface UserDataSource {

    void getAllUser(LoadCallBack loadCallBack);

    void deleteUserByKey(long id, UpdateCallBack updateCallBack);

    void updateNickName(User user, UpdateCallBack updateCallBack);

    void updatePassword(User user,String newPassWord,UpdateCallBack updateCallBack);

    void addUser(User user,UpdateCallBack updateCallBack);

    interface LoadCallBack{

        void onLoadSuccess(List<User> users);

        void onLoadFailure();
    }

    interface UpdateCallBack{

        void onUpdateSuccess();

        void onUpdateFailure();

    }

}
