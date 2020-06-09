package net.novaborn.controller;

import net.novaborn.config.ServerConfig;
import net.novaborn.dao.MailMapper;
import net.novaborn.dao.UserMapper;
import net.novaborn.entity.Block;
import net.novaborn.entity.User;
import net.novaborn.entity.UserExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: 周世焕
 * @time: 2020-05-20 15:58
 */
@Controller
public class UserController {

    @Autowired
    UserMapper userMapper;
    @Autowired
    MailMapper mailMapper;
    @Autowired
    ServerConfig serverConfig;

    @RequestMapping(value = "user",method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> add(HttpServletRequest request, String username, String password){
        Map<String,Object> map = new HashMap<String, Object>();
        System.out.println(username);
        System.out.println(username.endsWith(serverConfig.getDomain()) + " " + serverConfig.getDomain());
        if (username.endsWith(serverConfig.getDomain())){
            User user = new User();
            user.setUserName(username);
            user.setPassword(password);
            user.setNickName(username);
            UserExample userExample = new UserExample();
            userExample.createCriteria().andUserNameEqualTo(username);
            List<User> user1 = userMapper.selectByExample(userExample);
            if(user1 == null || user1.size() == 0){
                int i = userMapper.insertSelective(user);
                if(i > 0){
                    map.put("status",200);
                }else{
                    map.put("status",500);
                    map.put("errorMsg","注册失败");
                }
            }else{
                map.put("status",500);
                map.put("errorMsg","用户名已存在");
            }
        }else{
            map.put("status",500);
            map.put("errorMsg","邮箱地址不符合规则");
        }
        System.out.println(map);
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }

    @RequestMapping(value = "user",method = RequestMethod.PUT)
    public ResponseEntity<Map<String,Object>> update(HttpServletRequest request, String userName,String oldPass,String newPass){
        Map<String,Object> map = new HashMap<String, Object>();
        System.out.println(userName + " " + oldPass + " " + newPass);
        int i = userMapper.updatePassword(userName,oldPass,newPass);
        if(i>0){
            map.put("status",200);
        }else{
            map.put("status",500);
            map.put("errorMsg","原密码错误或用户不存在");
        }
        System.out.println(map);
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }

    @RequestMapping(value = "user",method = RequestMethod.DELETE)
    public ResponseEntity<Map<String,Object>> delete(HttpServletRequest request, int userId){
        Map<String,Object> map = new HashMap<String, Object>();
        if(request.getSession().getAttribute("admin")!=null){
            int j = mailMapper.deleteAllMailOfUser(userId);
            int i = userMapper.deleteByPrimaryKey(userId);
            if(i > 0){
                map.put("status",200);
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
        }
        map.put("status",500);
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }

    @RequestMapping(value = "user",method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> get(HttpServletRequest request, String username, String password){
        Map<String,Object> map = new HashMap<String, Object>();
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }

    @RequestMapping(value = "userlist",method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> getUserList(HttpServletRequest request){
        Map<String,Object> map = new HashMap<String, Object>();
        if(request.getSession().getAttribute("admin")!=null){
            List<User> userList = userMapper.selectAll();
            if(userList!=null){
                map.put("status",200);
                map.put("userlist",userList);
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
        }
        map.put("status",500);
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }


    @RequestMapping(value = "block",method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> getBlockList(HttpServletRequest request,String username,String password){
        Map<String,Object> map = new HashMap<String, Object>();
        UserExample userExample = new UserExample();
        userExample.createCriteria().andUserNameEqualTo(username)
                .andPasswordEqualTo(password);
        int i = userMapper.selectByExample(userExample).size();
        if (i > 0){
            List<Block> blocks = userMapper.selectBlockOfUser(username);
            map.put("status",200);
            map.put("blocklist",blocks);
            return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
        }
        map.put("status",500);
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }

    @RequestMapping(value = "block",method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> addBlock(HttpServletRequest request,String username,String password,
                                                       String blockAddress){
        Map<String,Object> map = new HashMap<String, Object>();
        UserExample userExample = new UserExample();
        userExample.createCriteria().andUserNameEqualTo(username)
                .andPasswordEqualTo(password);
        int i = userMapper.selectByExample(userExample).size();
        if (i > 0){
            try {
                int j = userMapper.insertBlock(username,blockAddress);
                map.put("status",200);
            }catch (Exception e){
                map.put("status",400);
                e.printStackTrace();
            }
            return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
        }
        map.put("status",500);
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }
}
