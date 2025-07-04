package com.atguigu.shargingjdbcdemo;

import com.atguigu.shargingjdbcdemo.entity.User;
import com.atguigu.shargingjdbcdemo.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
public class ReadWriteTest {

    @Autowired
    private UserMapper userMapper;

    /**
     * 插入数据，观察从库是否同步数据
     */
    @Test
    public void test() {
        User user = new User();
        user.setUname("张三丰");
        userMapper.insert(user);
        System.out.println("插入数据完成");
    }

    /**
     * 事务测试，在单元测试条件下，默认事务会进行回滚
     */
    @Transactional//开启事务
    @Test
    public void testTrans(){

        User user = new User();
        user.setUname("铁锤");
        userMapper.insert(user);

        List<User> users = userMapper.selectList(null);
        users.forEach(System.out::println);
    }
}
