package com.osen.cloud.service.data.coldchain.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.osen.cloud.common.entity.dev_coldchain.ColdChainHistory;
import com.osen.cloud.common.entity.system_device.Device;
import com.osen.cloud.common.utils.SecurityUtil;
import com.osen.cloud.common.utils.TableUtil;
import com.osen.cloud.model.coldchain.ColdChainHistoryMapper;
import com.osen.cloud.service.data.coldchain.ColdChainHistoryService;
import com.osen.cloud.service.device.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User: PangYi
 * Date: 2019-10-24
 * Time: 15:14
 * Description: 冷链实时数据通用服务接口实现类
 */
@Service
public class ColdChainHistoryServiceImpl extends ServiceImpl<ColdChainHistoryMapper, ColdChainHistory> implements ColdChainHistoryService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private DeviceService deviceService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void insertHistory(ColdChainHistory coldChainHistory) {
        super.save(coldChainHistory);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createNewTable(String tableName) {
        baseMapper.createNewTable(tableName);
    }

    @Override
    public ColdChainHistory getRealtime(String deviceNo) {
        BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(TableUtil.Cold_RealTime);
        Boolean key = operations.hasKey(deviceNo);
        if (key) {
            String json = (String) operations.get(deviceNo);
            return JSON.parseObject(json, ColdChainHistory.class);
        }
        return null;
    }

    @Override
    public List<ColdChainHistory> getDataToday(String deviceNo) {
        // 当前时间
        LocalDateTime nowToday = LocalDateTime.now();
        // 开始时间
        LocalDateTime start = LocalDateTime.of(nowToday.getYear(), nowToday.getMonthValue(), nowToday.getDayOfMonth(), 0, 0, 0, 0);
        LambdaQueryWrapper<ColdChainHistory> query = Wrappers.<ColdChainHistory>lambdaQuery();
        query.select(ColdChainHistory::getDateTime, ColdChainHistory::getDeviceNo, ColdChainHistory::getT01, ColdChainHistory::getH01, ColdChainHistory::getT02, ColdChainHistory::getH02, ColdChainHistory::getT03, ColdChainHistory::getH03, ColdChainHistory::getT04, ColdChainHistory::getH04);
        query.eq(ColdChainHistory::getDeviceNo, deviceNo);
        query.between(ColdChainHistory::getDateTime, start, nowToday).orderByAsc(ColdChainHistory::getDateTime);
        List<ColdChainHistory> chainHistories = new ArrayList<>(0);
        try {
            chainHistories = super.list(query);
        } catch (Exception e) {
            return chainHistories;
        }
        return chainHistories;
    }

    @Override
    public List<ColdChainHistory> queryHistoryByDate(LocalDateTime start, LocalDateTime end, String deviceNo) {
        List<ColdChainHistory> chainHistories = new ArrayList<>(0);
        // 查询条件
        LambdaQueryWrapper<ColdChainHistory> query = Wrappers.<ColdChainHistory>lambdaQuery();
        query.eq(ColdChainHistory::getDeviceNo, deviceNo);
        query.between(ColdChainHistory::getDateTime, start, end).orderByAsc(ColdChainHistory::getDateTime);
        try {
            chainHistories = super.list(query);
        } catch (Exception e) {
            return chainHistories;
        }
        return chainHistories;
    }

    @Override
    public List<ColdChainHistory> queryHistoryByDate(LocalDateTime start, LocalDateTime end, String deviceNo, String monitor) {
        List<ColdChainHistory> chainHistories = new ArrayList<>(0);
        // 查询条件
        LambdaQueryWrapper<ColdChainHistory> query = Wrappers.<ColdChainHistory>lambdaQuery();
        switch (monitor) {
            case "m01":
                query.select(ColdChainHistory::getT01, ColdChainHistory::getH01, ColdChainHistory::getDateTime);
                break;
            case "m02":
                query.select(ColdChainHistory::getT02, ColdChainHistory::getH02, ColdChainHistory::getDateTime);
                break;
            case "m03":
                query.select(ColdChainHistory::getT03, ColdChainHistory::getH03, ColdChainHistory::getDateTime);
                break;
            case "m04":
                query.select(ColdChainHistory::getT04, ColdChainHistory::getH04, ColdChainHistory::getDateTime);
                break;
        }
        query.eq(ColdChainHistory::getDeviceNo, deviceNo);
        query.between(ColdChainHistory::getDateTime, start, end).orderByAsc(ColdChainHistory::getDateTime);
        try {
            chainHistories = super.list(query);
        } catch (Exception e) {
            return chainHistories;
        }
        return chainHistories;
    }

    @Override
    public List<ColdChainHistory> getRealtimeToUser() {
        List<ColdChainHistory> coldChainHistories = new ArrayList<>(0);
        String username = SecurityUtil.getUsername();
        List<Device> devices = (List<Device>) deviceService.finaAllDeviceToUser(username).get("devices");
        for (Device device : devices) {
            Boolean hasKey = stringRedisTemplate.boundHashOps(TableUtil.Cold_RealTime).hasKey(device.getDeviceNo());
            if (hasKey) {
                String json = (String) stringRedisTemplate.boundHashOps(TableUtil.Cold_RealTime).get(device.getDeviceNo());
                ColdChainHistory coldChainHistory = JSON.parseObject(json, ColdChainHistory.class);
                coldChainHistories.add(coldChainHistory);
            }
        }
        return coldChainHistories;
    }

    @Override
    public List<ColdChainHistory> getLocusToUser(LocalDateTime start, LocalDateTime end, String deviceNo) {
        List<ColdChainHistory> chainHistories = new ArrayList<>(0);
        // 查询条件
        LambdaQueryWrapper<ColdChainHistory> query = Wrappers.<ColdChainHistory>lambdaQuery();
        query.select(ColdChainHistory::getLongitude, ColdChainHistory::getLatitude);
        query.eq(ColdChainHistory::getDeviceNo, deviceNo);
        query.between(ColdChainHistory::getDateTime, start, end).orderByAsc(ColdChainHistory::getDateTime);
        try {
            chainHistories = super.list(query);
        } catch (Exception e) {
            return chainHistories;
        }
        return chainHistories;
    }

    @Override
    public ColdChainHistory getOneData(String deviceNo, LocalDateTime dateTime) {
        LambdaQueryWrapper<ColdChainHistory> wrapper = Wrappers.<ColdChainHistory>lambdaQuery().select(ColdChainHistory::getDeviceNo).eq(ColdChainHistory::getDeviceNo, deviceNo).eq(ColdChainHistory::getDateTime, dateTime);
        try {
            return super.getOne(wrapper, true);
        } catch (Exception e) {
            return null;
        }
    }
}
