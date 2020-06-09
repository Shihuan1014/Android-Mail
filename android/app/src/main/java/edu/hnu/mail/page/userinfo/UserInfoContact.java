package edu.hnu.mail.page.userinfo;

import edu.hnu.mail.data.entity.User;

public class UserInfoContact {

    public interface View{
        //更新nickName
        void updateNickName(String nickName);

        //更新密码
        void updatePassword(String password);

        //删除用户成功，退出页面
        void deleteSuccess();

        //操作发生错误
        void showFailure();

        //操作成功
        void showSuccess();
    }

    public interface Presenter{
        void deleteUser(long userId);
        void updateNickName(User user);
        void updatePassword(User user,String newPassWord);
    }
}
