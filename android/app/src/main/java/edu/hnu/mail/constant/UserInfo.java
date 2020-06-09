package edu.hnu.mail.constant;

import java.util.ArrayList;
import java.util.List;

import edu.hnu.mail.data.entity.Contact;
import edu.hnu.mail.data.entity.User;

public class UserInfo {
    public static List<User> users = new ArrayList<User>();
    public static int currentIndex = 0;
    public static int mainAccount = 0;

    //管理员用http操作，需要携带sessionId
    public static String sessionId;
    public static int getTotalMailCount(){
        int total = 0;
        for (User user : users){
            total += user.getMailInboxCount();
        }
        return total;
    }

    public static User getCurrentUser(){
        return users.get(currentIndex);
    }

    public static void deleteMail(int i){
        users.get(currentIndex).setMailInboxCount(users.get(currentIndex).getMailInboxCount() - i);
    }

}
