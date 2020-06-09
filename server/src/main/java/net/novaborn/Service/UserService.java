package net.novaborn.Service;

import net.novaborn.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface UserService {
    public void add(String userName,String password,String phone,String nickName);
    public User get(int id);
    public void delete(int id);
    public List<User> getUserList();
    public List<User> search(String userName);
    public int getAuthor(String userName);
    public void modifyUser(String userName,String password,String phone,String nickName);
    public void setAuthor(String userName,int author);
}
