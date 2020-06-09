package net.novaborn.dao;

import java.util.List;

import net.novaborn.entity.Block;
import net.novaborn.entity.User;
import net.novaborn.entity.UserExample;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper {
    int countByExample(UserExample example);

    int deleteByExample(UserExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    List<User> selectByExample(UserExample example);

    User selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") User record, @Param("example") UserExample example);

    int updateByExample(@Param("record") User record, @Param("example") UserExample example);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    //////////////////////////////
    @Select("select * from User where userName=#{userName}")
    User selectByUserName(@Param("userName") String userName);
    //////////////////////////////



    @Select("select * from User where userName = #{username} and password = #{password} " +
            "and type = 1")
    User selectAdmin(String username,String password);


    @Select("select * from User where type = 0")
    List<User> selectAll();

    @Update("update user set password = #{newpass,jdbcType=VARCHAR} where username = #{username,jdbcType=VARCHAR} and " +
            "password = #{oldpass,jdbcType=VARCHAR}")
    int updatePassword(String username,String oldpass,String newpass);


    @Select("select * from block where userAddress = #{userAddress,jdbcType=VARCHAR}")
    List<Block> selectBlockOfUser(String userAddress);

    @Insert("insert into block(userAddress,blockAddress) values(#{userAddress,jdbcType=VARCHAR}" +
            ",#{blockAddress,jdbcType=VARCHAR})")
    int insertBlock(String userAddress,String blockAddress);

    @Select("select * from block where userAddress = #{userAddress,jdbcType=VARCHAR} and" +
            " blockAddress = #{blockAddress,jdbcType=VARCHAR}")
    Block selectBlock(String userAddress,String blockAddress);

    @Update("update user set author = #{author,jdbcType=INTEGER} where userName = #{userName,jdbcType=VARCHAR}")
    int updateAuthor(String userName,int author);
}