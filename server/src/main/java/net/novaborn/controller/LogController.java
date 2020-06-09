package net.novaborn.controller;

import net.novaborn.dao.LogMapper;
import net.novaborn.entity.Log;
import net.novaborn.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: 周世焕
 * @time: 2020-05-21 11:05
 */
@Controller
public class LogController {
    @Autowired
    LogMapper logMapper;

    @RequestMapping(value = "log",method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> getAll(HttpServletRequest request){
        Map<String,Object> map = new HashMap<String, Object>();
        if(request.getSession().getAttribute("admin")!=null){
            List<Log> userList = logMapper.selectAll();
            if(userList!=null){
                map.put("status",200);
                map.put("loglist",userList);
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
        }
        map.put("status",500);
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }

    @RequestMapping(value = "log",method = RequestMethod.DELETE)
    public ResponseEntity<Map<String,Object>> deleteAll(HttpServletRequest request){
        Map<String,Object> map = new HashMap<String, Object>();
        if(request.getSession().getAttribute("admin")!=null){
            int i = logMapper.deleteAll();
            map.put("status",200);
            return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
        }
        map.put("status",500);
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }

}
