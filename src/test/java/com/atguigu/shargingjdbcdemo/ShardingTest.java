package com.atguigu.shargingjdbcdemo;

import com.atguigu.shargingjdbcdemo.entity.*;
import com.atguigu.shargingjdbcdemo.mapper.DictMapper;
import com.atguigu.shargingjdbcdemo.mapper.OrderItemMapper;
import com.atguigu.shargingjdbcdemo.mapper.OrderMapper;
import com.atguigu.shargingjdbcdemo.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
public class ShardingTest {


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private DictMapper dictMapper;

    /**
     * 垂直分片：插入数据测试
     */
    @Test
    public void testInsertOrderAndUser(){

        User user = new User();
        user.setUname("强哥");
        userMapper.insert(user);

        Order order = new Order();
        order.setOrderNo("ATGUIGU001");
        order.setUserId(user.getId());
        order.setAmount(new BigDecimal(100));
        orderMapper.insert(order);

    }

    /**
     * 垂直分片：查询数据测试
     */
    @Test
    public void testSelectFromOrderAndUser(){
        User user = userMapper.selectById(1L);
        Order order = orderMapper.selectById(1L);
    }

    /**
     * 水平分片：垂直分库插入数据测试(仅仅只有分库，没有配置分库算法和分表)
     */
    @Test
    public void testInsertOrder(){
        Order order = new Order();
        order.setOrderNo("ATGUIGU001");
        order.setUserId(1L);
        order.setAmount(new BigDecimal(100));
        orderMapper.insert(order);
    }

    /**
     * 水平分片：水平分库插入数据测试
     * 因为OrderNo写死了，所以分表策略失效，所以数据会全部存储在不同库的order表中(因为OrderNo取模为奇数，所以数据会存储在order1表中)
     *
     */
    @Test
    public void testInsertOrderDatabaseStrategy(){

        for (long i = 0; i < 4; i++) {
            Order order = new Order();
            order.setOrderNo("ATGUIGU001");
            order.setUserId(i + 1);
            order.setAmount(new BigDecimal(100));
            orderMapper.insert(order);
        }

    }

    /**
     * 水平分片：水平分表插入数据测试
     * 因为UserId写死了，所以分库策略失效，所以数据会全部存储在相同库不同表中（因为user_id为偶数，所以会存储在server-order0库中）
     */
    @Test
    public void testInsertOrderTableStrategy(){

        for (long i = 0; i < 4; i++) {
            Order order = new Order();
            order.setOrderNo("ATGUIGU" + i);
            order.setUserId(2L);
            order.setAmount(new BigDecimal(100));
            orderMapper.insert(order);
        }
    }

    /**
     * 水平分片：水平分库分表插入数据测试
     * 注意:
     * 因为分库是通过user_id进行的，所以不能写死，分片算法是：奇数存储在server-order1,偶数存储在server-order0
     * 分表是通过orderNo进行的，也不能写死，分片算法是哈希取模：奇数存储在server-order1,偶数存储在server-order0
     * 预期结果:
     * 1.userId为偶数，orderNo为偶数，存储在server-order0.t_order0
     * 2.userId为偶数，orderNo为奇数，存储在server-order0.t_order1
     * 3.userId为奇数，orderNo为偶数，存储在server-order1.t_order0
     * 4.userId为奇数，orderNo为奇数，存储在server-order1.t_order1
     * 结论:
     * 下面程序运行后，数据为奇/偶，偶/奇，奇/偶，偶/奇四种情况都存储在不同的库和表中
     * 最终数据会在server-order1.t_order0和server-order0.t_order1中
     */
    @Test
    public void testInsertOrderStrategy(){
        for (long i = 0; i < 4; i++) {
            Order order = new Order();
            order.setOrderNo("ATGUIGU" + i);
            order.setUserId(i + 1);
            order.setAmount(new BigDecimal(100));
            orderMapper.insert(order);
        }
    }

    /**
     * 水平分片：分库(静态指定)分表插入数据测试
     */
    @Test
    public void testInsertOrderStrategy2(){

        for (long i = 1; i < 5; i++) {

            Order order = new Order();
            order.setOrderNo("ATGUIGU" + i);
            order.setUserId(1L);//对应server-order1
            order.setAmount(new BigDecimal(100));
            orderMapper.insert(order);
        }

        for (long i = 5; i < 9; i++) {

            Order order = new Order();
            order.setOrderNo("ATGUIGU" + i);
            order.setUserId(2L);//对应server-order0
            order.setAmount(new BigDecimal(100));
            orderMapper.insert(order);
        }
    }

    /**
     * 测试哈希取模
     */
    @Test
    public void testHash(){

        //注意hash取模的结果是整个字符串hash后再取模，和数值后缀是奇数还是偶数无关
        System.out.println("ATGUIGU1".hashCode() % 2);
        System.out.println("ATGUIGU2".hashCode() % 2);
        System.out.println("ATGUIGU3".hashCode() % 2);
        System.out.println("ATGUIGU4".hashCode() % 2);
    }

    /**
     * 水平分片：查询所有记录
     * 查询了两个数据源，每个数据源中使用UNION ALL连接两个表
     */
    @Test
    public void testShardingSelectAll(){

        List<Order> orders = orderMapper.selectList(null);
        orders.forEach(System.out::println);
    }

    /**
     * 水平分片：根据user_id查询记录
     * 查询了一个数据源，每个数据源中使用UNION ALL连接两个表
     */
    @Test
    public void testShardingSelectByUserId(){

        QueryWrapper<Order> orderQueryWrapper = new QueryWrapper<>();
        orderQueryWrapper.eq("user_id", 1L);
        List<Order> orders = orderMapper.selectList(orderQueryWrapper);
        orders.forEach(System.out::println);
    }

    /**
     * 测试关联表插入
     * 预期结果:
     * 1.userId为奇数，orderNo为奇数，存储在server-order1.t_order1, server-order1.t_order_item1
     * 2.userId为奇数，orderNo为偶数，存储在server-order1.t_order0, server-order1.t_order_item0
     * 2.userId为偶数，orderNo为奇数，存储在server-order0.t_order1, server-order0.t_order_item1
     * 3.userId为偶数，orderNo为偶数，存储在server-order0.t_order0, server-order0.t_order_item0
     * 结论:
     * 下面程序运行后，数据为奇/奇，奇/偶，偶/奇，偶/偶四种情况都存储在不同的库和表中
     * 也就是server-order0和server-order1库中的表t_order0, t_order1, t_order_item0, t_order_item1都会存储数据
     */
    @Test
    public void testInsertOrderAndOrderItem(){

        for (long i = 1; i < 3; i++) {

            Order order = new Order();
            order.setOrderNo("ATGUIGU" + i);
            order.setUserId(1L);
            orderMapper.insert(order);

            for (long j = 1; j < 3; j++) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderNo("ATGUIGU" + i);
                orderItem.setUserId(1L);
                orderItem.setPrice(new BigDecimal(10));
                orderItem.setCount(2);
                orderItemMapper.insert(orderItem);
            }
        }

        for (long i = 5; i < 7; i++) {

            Order order = new Order();
            order.setOrderNo("ATGUIGU" + i);
            order.setUserId(2L);
            orderMapper.insert(order);

            for (long j = 1; j < 3; j++) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderNo("ATGUIGU" + i);
                orderItem.setUserId(2L);
                orderItem.setPrice(new BigDecimal(1));
                orderItem.setCount(3);
                orderItemMapper.insert(orderItem);
            }
        }
    }

    /**
     * 测试关联表查询
     */
    @Test
    public void testGetOrderAmount(){

        List<OrderVo> orderAmountList = orderMapper.getOrderAmount();
        orderAmountList.forEach(System.out::println);
    }

    /**
     * 广播表：每个服务器中的t_dict同时添加了新数据
     */
    @Test
    public void testBroadcast(){

        Dict dict = new Dict();
        dict.setDictType("type1");
        dictMapper.insert(dict);
    }

    /**
     * 查询操作，只从一个节点获取数据
     * 随机负载均衡规则
     */
    @Test
    public void testSelectBroadcast(){

        List<Dict> dicts = dictMapper.selectList(null);
        dicts.forEach(System.out::println);
    }
}
