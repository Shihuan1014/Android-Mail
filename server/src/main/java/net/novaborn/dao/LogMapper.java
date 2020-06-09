package net.novaborn.dao;

import net.novaborn.entity.Log;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @description:
 * @author: 周世焕
 * @time: 2020-05-21 10:51
 */
@Repository
public interface LogMapper {

    @Insert("insert into log(type,log,created) values(#{type,jdbcType=VARCHAR}," +
            "#{log,jdbcType=VARCHAR},now())")
    int insertLog(String type,String log);

    @Select("select * from log order by created desc")
    List<Log> selectAll();

    @Delete("delete from log where 1 = 1")
    int deleteAll();
}
