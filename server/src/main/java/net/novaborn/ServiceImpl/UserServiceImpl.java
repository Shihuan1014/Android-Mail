package net.novaborn.ServiceImpl;

import net.novaborn.Service.UserService;
import net.novaborn.dao.UserMapper;
import net.novaborn.entity.User;
import net.novaborn.entity.UserExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;

    @Override
    public void add(String userName, String password, String phone, String nickName) {
        User user = new User();
        user.setUserName(userName);
        user.setPassword(password);
        user.setPhone(phone);
        user.setNickName(nickName);
        userMapper.insert(user);
    }
    public void delete(int id){
        userMapper.deleteByPrimaryKey(id);
    }

    @Override
    public User get(int id) {
        return null;
    }

    @Override
    public List<User> getUserList() {
        UserExample userExample = new UserExample();
        return userMapper.selectByExample(userExample);
    }

    @Override
    public List<User> search(String userName) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andUserNameLike(userName);
        return userMapper.selectByExample(userExample);
    }

    @Override
    public int getAuthor(String userName) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andUserNameEqualTo(userName);
        return userMapper.selectByExample(userExample).get(0).getAuthor();
    }

    @Override
    public void modifyUser(String userName, String password, String phone, String nickName) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andUserNameEqualTo(userName);
        User user = new User();
        user.setUserName(userName);
        user.setPassword(password);
        user.setPhone(phone);
        user.setNickName(nickName);
        userMapper.updateByExampleSelective(user,userExample);
        userMapper.updateByPrimaryKeySelective(user);
    }

    @Override
    public void setAuthor(String userName,int author) {
        User user = new User();
        UserExample userExample = new UserExample();
        userExample.createCriteria().andUserNameEqualTo(userName);
        user.setAuthor(author);
        userMapper.updateByExampleSelective(user,userExample);
    }
}
