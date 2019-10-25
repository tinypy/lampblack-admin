package com.osen.cloud.common.utils;

import cn.hutool.core.util.StrUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User: PangYi
 * Date: 2019-08-30
 * Time: 10:08
 * Description: 全局静态变量
 */
public class ConstUtil {

    // 用户初始密码，123456
    public static final String INIT_PASSWORD = "$2a$10$/v9gg7DcPhaPNJUK3djGU.XTHZodHEkBffJ.8.GAHI1bxmdzW6ufK";

    // 监听端口
    public static final Integer SERVER_PORT = 8888;

    // 请求成功编码
    public static Integer OK = 2002;

    // 请求失败编码
    public static Integer UNOK = 2004;

    // 日期格式化
    public static String DATE_FORMAT = "yyyyMMddHHmmss";

    // 日期格式化
    public static String QUERY_DATE = "yyyy-MM-dd-HH-mm-ss";

    // 油烟设备连接主键key，保存最新的实时数据
    public static String DATA_KEY = "lampblack_realtime_db";

    // 油烟设备设备主键key，设备与连接ID关联
    public static String DEVICE_KEY = "lampblack_connection_db";

    // 油烟设备报警主键key，保存最新的报警数据
    public static String ALARM_KEY = "lampblack_alarm_db";

    // 关闭状态
    public static Integer CLOSE_STATUS = 1;

    // 开合状态
    public static Integer OPEN_STATUS = 2;

    // 实时数据表
    public static String REALTIME_TB = "data_history";

    // 分钟数据表
    public static String MINUTE_TB = "data_minute";

    // 小时数据表
    public static String HOUR_TB = "data_hour";

    // 每天数据表
    public static String DAY_TB = "data_day";

    // 分页数
    public static Integer PAGE_NUMBER = 8;

    public static String ADMIN = "ROLE_ADMIN"; // 超级管理员

    public static String SCOTT = "ROLE_SCOTT"; // 终端用户

    public static String PROXY = "ROLE_PROXY"; // 代理商

    /**
     * 动态生成当前月份表名
     *
     * @param tableName 原表名
     * @return 新表名
     */
    public static String currentTableName(String tableName) {
        LocalDateTime dateTime = LocalDateTime.now();
        int year = dateTime.getYear();
        int monthValue = dateTime.getMonthValue();
        String month = "";
        if (monthValue < 10)
            month = "0" + monthValue;
        else
            month = "" + monthValue;
        // 格式：基本表名_年月
        return tableName + "_" + year + month;
    }

    /**
     * 生成下一个月表名
     *
     * @param tableName 表名
     * @return 新表名
     */
    public static String createNextTableName(String tableName) {
        LocalDateTime localDateTime = LocalDateTime.now();
        // 下一个月
        localDateTime = localDateTime.plusMonths(1);
        int year = localDateTime.getYear();
        int monthValue = localDateTime.getMonthValue();
        String month = "";
        if (monthValue < 10)
            month = "0" + monthValue;
        else
            month = "" + monthValue;
        // 格式：基本表名_年月
        return tableName + "_" + year + month;
    }

    /**
     * 解析数据表日期查询
     *
     * @param startDatetime 开始时间
     * @param endDatetime   结束时间
     * @param prefixTable   数据表前缀
     * @return 信息
     */
    public static List<String> queryTableName(LocalDateTime startDatetime, LocalDateTime endDatetime, String prefixTable) {
        List<String> tables = new ArrayList<>(0);
        String month = "";
        // 开始时间
        int startDateYear = startDatetime.getYear();
        int startDateMonthValue = startDatetime.getMonthValue();
        // 结束时间
        int endDateYear = endDatetime.getYear();
        int endDateMonthValue = endDatetime.getMonthValue();
        // 构建数据表
        if ((startDateYear == endDateYear) && (startDateMonthValue == endDateMonthValue)) {
            if (startDateMonthValue < 10)
                month = "0" + startDateMonthValue;
            else
                month = "" + startDateMonthValue;
            String tableName = prefixTable + "_" + startDateYear + month;
            tables.add(tableName);
        } else {
            // 添加时间
            if (startDateMonthValue < 10)
                month = "0" + startDateMonthValue;
            else
                month = "" + startDateMonthValue;
            String startTableName = prefixTable + "_" + startDateYear + month;
            tables.add(startTableName);

            if (endDateMonthValue < 10)
                month = "0" + endDateMonthValue;
            else
                month = "" + endDateMonthValue;
            String endTableName = prefixTable + "_" + endDateYear + month;
            tables.add(endTableName);
        }
        return tables;
    }

    /**
     * 时间日前后比较，判断表名是否有效
     *
     * @param tableName 表名
     * @return 信息
     */
    public static boolean compareToTime(String tableName) {
        String sub = StrUtil.sub(tableName, -2, tableName.length());
        int time = Integer.valueOf(sub);
        LocalDate one = LocalDate.of(2019, 8, 1);
        LocalDate two = LocalDate.of(2019, time, 1);
        return !two.isAfter(one);
    }

}
