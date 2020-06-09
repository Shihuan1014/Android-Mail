package net.novaborn.controller;

import net.novaborn.dao.MailboxMapper;
import net.novaborn.dao.UserMapper;
import net.novaborn.entity.Mailbox;
import net.novaborn.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: 周世焕
 * @time: 2020-05-20 15:48
 */
@Controller
public class AdminController {
    @Autowired
    UserMapper userMapper;
    @Autowired
    MailboxMapper mailboxMapper;

    @RequestMapping(value = "login",method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> login(HttpServletRequest request,String username, String password){
        Map<String,Object> map = new HashMap<String, Object>();
        User user = userMapper.selectAdmin(username,password);
        System.out.println(username+" " +password);
        if(user!=null){
            request.getSession().setAttribute("admin",user);
            map.put("status",200);
        }else{
            map.put("status",500);
        }
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }

    @RequestMapping(value = "sendEmailToAll",method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> sendEmail(HttpServletRequest request,String admin,String data){
        Map<String,Object> map = new HashMap<String, Object>();
        User admin1 = (User) request.getSession().getAttribute("admin");
        if(admin1==null) {
            map.put("status",400);
        }else{
            List<User> userList=userMapper.selectAll();
            for (User user:userList) {
                Mailbox mailbox = new Mailbox();
                mailbox.setFrom(admin);
                mailbox.setId(admin1.getId());
                mailbox.setDate(new Date(System.currentTimeMillis()));
                mailbox.setSize(data.length());
                mailbox.setTo(user.getUserName());
                mailbox.setData(data);
                mailboxMapper.insert(mailbox);
            }
            map.put("status",200);
        }
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }

    @RequestMapping(value = "changeAuthor",method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> changeAuthor(HttpServletRequest request,String userAddress,int author){
        Map<String,Object> map = new HashMap<String, Object>();
        User admin1 = (User) request.getSession().getAttribute("admin");
        if(admin1==null) {
            map.put("status",400);
        }else{
            int i = userMapper.updateAuthor(userAddress,author);
            if (i>0){
                map.put("status",200);
            }else{
                map.put("status",500);
            }
        }
        System.out.println(map);
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }
}
