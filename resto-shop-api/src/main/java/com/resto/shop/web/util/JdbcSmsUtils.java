package com.resto.shop.web.util;

import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.model.DayAppraiseMessageWithBLOBs;
import com.resto.shop.web.model.DayDataMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yz on 2017-06-12.
 */
public class JdbcSmsUtils {
    // 数据库用户名
    private static final String USERNAME = "root";
    // 数据库密码
    private static final String PASSWORD = "123456";
    // 驱动信息
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    // 数据库地址
    private static final String URL = "jdbc:mysql://101.200.190.249:3306/test?useUnicode=true&characterEncoding=utf8";

    private static Connection connection;
    private static PreparedStatement pstmt;

    static   Logger log = LoggerFactory.getLogger(JdbcSmsUtils.class);

    /**
     * 获得数据库的连接
     *
     * @return
     */
    public static Connection getConnection() {
        try {
            Class.forName(DRIVER);
            log.info("---数据库连接成功---");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }


    /**
     * 释放数据库连接
     */
    public static void close() {
        try {
            if (pstmt != null) {
                pstmt.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    /**
     * 增加、删除、改
     *
     * @param sql
     * @param params
     * @return
     * @throws SQLException
     */
    public static boolean updateByPreparedStatement(String sql, List<Object> params) throws SQLException {
        boolean flag = false;
        int result = -1;
        pstmt = connection.prepareStatement(sql);
        int index = 1;
        if (params != null && !params.isEmpty()) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(index++, params.get(i));
            }
        }
        result = pstmt.executeUpdate();
        flag = result > 0 ? true : false;
        return flag;
    }

    /**
     *
     * @param dayDataMessage
     * @return
     * 插入日结短信数据
     */
    public  static  void saveDayDataMessage(DayDataMessage dayDataMessage) {
        //初始化连接
        getConnection();
        String sql = "INSERT INTO tb_day_data_message(id,shop_id,type,shop_name,week_day,date,times,wether,temperature,order_number,order_sum,customer_order_number,customer_order_sum,customer_order_ratio,back_customer_order_ratio,new_customer_order_ratio,new_cuostomer_order_num,new_customer_order_sum,new_normal_customer_order_num,new_normal_customer_order_sum,new_share_customer_order_num,new_share_customer_order_sum,back_customer_order_num,back_customer_order_sum,back_two_customer_order_num,back_two_customer_order_sum,back_two_more_customer_order_num,back_two_more_customer_order_sum,discount_total,red_pack,coupon,charge_reward,discount_ratio,takeaway_total,bussiness_total,month_total)VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        List<Object> params = new ArrayList<>();
        params.add(ApplicationUtils.randomUUID());//id
        params.add(dayDataMessage.getShopId());//shop_id
        params.add(dayDataMessage.getType()); //type
        params.add(dayDataMessage.getShopName());//shop_name
        params.add(dayDataMessage.getWeekDay());//week_day
        params.add(dayDataMessage.getDate());//date
        params.add(dayDataMessage.getTimes());//times
        params.add(dayDataMessage.getWether());//wether
        params.add(dayDataMessage.getTemperature());//temperature
        params.add(dayDataMessage.getOrderNumber());//10 order_number
        params.add(dayDataMessage.getOrderSum());//order_sum
        params.add(dayDataMessage.getCustomerOrderNumber());//customer_order_number
        params.add(dayDataMessage.getCustomerOrderSum());//customer_order_sum
        params.add(dayDataMessage.getCustomerOrderRatio());//customer_order_ratio
        params.add(dayDataMessage.getBackCustomerOrderRatio());//back_customer_order_ratio
        params.add(dayDataMessage.getNewCustomerOrderRatio());//new_customer_order_ratio
        params.add(dayDataMessage.getNewShareCustomerOrderNum());//new_cuostomer_order_num
        params.add(dayDataMessage.getNewShareCustomerOrderSum());//new_customer_order_sum
        params.add(dayDataMessage.getNewNormalCustomerOrderNum());//new_normal_customer_order_num
        params.add(dayDataMessage.getNewNormalCustomerOrderSum());//20 new_normal_customer_order_sum
        params.add(dayDataMessage.getNewShareCustomerOrderNum());//new_share_customer_order_num
        params.add(dayDataMessage.getNewShareCustomerOrderSum());//new_share_customer_order_sum
        params.add(dayDataMessage.getBackCustomerOrderNum());//back_customer_order_num
        params.add(dayDataMessage.getBackCustomerOrderSum());//back_customer_order_sum
        params.add(dayDataMessage.getBackTwoCustomerOrderNum());//back_two_customer_order_num
        params.add(dayDataMessage.getBackTwoCustomerOrderSum());//back_two_customer_order_sum
        params.add(dayDataMessage.getBackTwoMoreCustomerOrderNum());//back_two_more_customer_order_num
        params.add(dayDataMessage.getBackTwoMoreCustomerOrderSum());//back_two_more_customer_order_sum
        params.add(dayDataMessage.getDiscountTotal());//discount_total
        params.add(dayDataMessage.getRedPack());//red_pack
        params.add(dayDataMessage.getCoupon());//coupon
        params.add(dayDataMessage.getChargeReward());//charge_reward
        params.add(dayDataMessage.getDiscountRatio());//discount_ratio
        params.add(dayDataMessage.getTakeawayTotal());//takeaway_total
        params.add(dayDataMessage.getBussinessTotal());//bussiness_total
        params.add(dayDataMessage.getMonthTotal());//month_total
        try {
            updateByPreparedStatement(sql,params);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close();
    }


    /**
     * 日结分数短信存储
     * @param ds
     */
    public  static void  saveDayAppraise(DayAppraiseMessageWithBLOBs ds){
        init(ds);
        //初始化连接
        getConnection();
        String sql = "insert into tb_day_appraise_message (id,shop_id,shop_name, date, week_day, wether, temperature, type, five_star, four_star, one_three_star, day_satisfaction, xun_satisfaction, month_satisfaction, red_list, bad_list) values (?,?,?,?,?,?,?, ?,?,?,?,?,?,?,?,?)";
        List<Object> params = new ArrayList<>();
        params.add(ApplicationUtils.randomUUID());
        params.add(ds.getShopId());
        params.add(ds.getShopName());
        params.add(ds.getDate());
        params.add(ds.getWeekDay());
        params.add(ds.getWether());
        params.add(ds.getTemperature());
        params.add(ds.getType());
        params.add(ds.getFiveStar());
        params.add(ds.getFourStar());
        params.add(ds.getOneThreeStar());
        params.add(ds.getDaySatisfaction());
        params.add(ds.getXunSatisfaction());
        params.add(ds.getMonthSatisfaction());
        params.add(ds.getRedList());
        params.add(ds.getBadList());
        try {
            updateByPreparedStatement(sql,params);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close();//释放连接
    }

    private static void init(DayAppraiseMessageWithBLOBs ds) {
        if(ds.getDaySatisfaction()==null){
            ds.setDaySatisfaction("无");
        }
        if(ds.getXunSatisfaction()==null){
            ds.setXunSatisfaction("无");
        }

    }


    public static void main(String[] args) throws SQLException {
        saveDayDataMessage(null);
    }







}
