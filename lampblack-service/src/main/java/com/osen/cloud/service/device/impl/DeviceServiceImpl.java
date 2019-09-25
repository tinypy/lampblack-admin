package com.osen.cloud.service.device.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.osen.cloud.common.entity.Device;
import com.osen.cloud.common.entity.User;
import com.osen.cloud.common.entity.UserDevice;
import com.osen.cloud.common.except.type.ServiceException;
import com.osen.cloud.common.utils.ConstUtil;
import com.osen.cloud.model.device.DeviceMapper;
import com.osen.cloud.service.device.DeviceService;
import com.osen.cloud.service.user.UserService;
import com.osen.cloud.service.user_device.UserDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.osen.cloud.common.enums.InfoMessage.InsertDevice_Error;

/**
 * User: PangYi
 * Date: 2019-09-16
 * Time: 15:06
 * Description: 设备服务接口实现类
 */
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {

    @Autowired
    private UserDeviceService userDeviceService;

    @Autowired
    private UserService userService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addDevice(Device device, Integer userId) {
        if (this.findDeviceNo(device.getDeviceNo()) != null)
            throw new ServiceException(InsertDevice_Error.getCode(), InsertDevice_Error.getMessage());
        device.setCreateTime(LocalDateTime.now());
        device.setUpdateTime(LocalDateTime.now());
        if (super.save(device)) {
            Integer deviceID = device.getId();
            UserDevice userDevice = new UserDevice();
            userDevice.setDeviceId(deviceID);
            userDevice.setUserId(userId);
            return userDeviceService.save(userDevice);
        }
        return false;
    }

    @Override
    public Device findDeviceNo(String deviceNo) {
        LambdaQueryWrapper<Device> wrapper = Wrappers.<Device>lambdaQuery().eq(Device::getDeviceNo, deviceNo);
        return super.getOne(wrapper, true);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateDeviceStatus(Integer isLive, String deviceNo) {
        LambdaUpdateWrapper<Device> updateWrapper = Wrappers.<Device>lambdaUpdate().set(Device::getIsLive, isLive).eq(Device::getDeviceNo, deviceNo).set(Device::getUpdateTime, LocalDateTime.now());
        return super.update(updateWrapper);
    }

    @Override
    public Map<String, Object> findDeviceByUserAccount(Map<String, Object> params) {
        Map<String, Object> resultMap = new HashMap<>(0);
        List<Device> devices = new ArrayList<>(0);
        String username = (String) params.get("account");
        // 获取指定用户
        User user = userService.findByUsername(username);
        if (user == null)
            throw new ServiceException(ConstUtil.UNOK, "无法查询指定用户信息");
        // 获取用户设备关联列表
        int pageNumber = (int) params.get("pageNumber");
        Page<UserDevice> userDevicePage = new Page<>(pageNumber, ConstUtil.PAGE_NUMBER);
        LambdaQueryWrapper<UserDevice> userDeviceLambdaQueryWrapper = Wrappers.<UserDevice>lambdaQuery().eq(UserDevice::getUserId, user.getId());
        IPage<UserDevice> userDeviceIPage = userDeviceService.page(userDevicePage, userDeviceLambdaQueryWrapper);
        // 总记录数
        long total = userDeviceIPage.getTotal();
        if (total > 0) {
            // 获取指定用户设备列表
            List<Integer> deviceId = new ArrayList<>();
            for (UserDevice userDevice : userDeviceIPage.getRecords()) {
                deviceId.add(userDevice.getDeviceId());
            }
            // 查询设备列表
            devices = (List<Device>) super.listByIds(deviceId);
        }
        // 封装数据
        resultMap.put("total", total);
        resultMap.put("devices", devices);
        return resultMap;
    }

    @Override
    public Map<String, Object> findAllDeviceToSystem(Map<String, Object> params) {
        Map<String, Object> resultMap = new HashMap<>(0);
        // 根据公司名称模糊查询
        LambdaQueryWrapper<Device> wrapper = Wrappers.<Device>lambdaQuery();
        String name = (String) params.get("deviceName");
        if (StringUtils.isNotEmpty(name))
            wrapper.like(Device::getName, name);
        // 时间升序排列
        wrapper.orderByAsc(Device::getCreateTime);
        // 分页查询
        int pageNumber = (int) params.get("pageNumber");
        Page<Device> devicePage = new Page<>(pageNumber, ConstUtil.PAGE_NUMBER + 2);
        IPage<Device> deviceIPage = super.page(devicePage, wrapper);
        // 总记录数
        long total = deviceIPage.getTotal();
        // 总记录
        List<Device> devices = deviceIPage.getRecords();
        // 封装数据
        resultMap.put("total", total);
        resultMap.put("devices", devices);
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> deleteToDeviceNo(String deviceNo) {
        Map<String, Object> resultMap = new HashMap<>(0);
        // 查询指定设备
        LambdaQueryWrapper<Device> queryWrapper = Wrappers.<Device>lambdaQuery().eq(Device::getDeviceNo, deviceNo);
        Device selectOne = baseMapper.selectOne(queryWrapper);
        if (selectOne == null)
            throw new ServiceException(ConstUtil.UNOK, "无法查询指定设备信息");
        // 查询用户与设备关联列表
        LambdaQueryWrapper<UserDevice> deviceLambdaQueryWrapper = Wrappers.<UserDevice>lambdaQuery().eq(UserDevice::getDeviceId, selectOne.getId());
        List<UserDevice> userToDevice = userDeviceService.findUserToDevice(deviceLambdaQueryWrapper);
        if (userToDevice == null) {
            // 无指定用户关联，执行删除设备
            super.removeById(selectOne.getId());
            resultMap.put("tips", "成功删除 " + selectOne.getDeviceNo() + " 设备");
            resultMap.put("code", ConstUtil.OK);
        } else {
            StringBuilder stringBuffer = new StringBuilder();
            stringBuffer.append("设备：").append(selectOne.getDeviceNo()).append(" 与以下账号已关联");
            stringBuffer.append("[ ");
            // 已有用户与该设备关联
            for (UserDevice userDevice : userToDevice) {
                User user = userService.getById(userDevice.getUserId());
                stringBuffer.append(user.getAccount()).append(" , ");
            }
            stringBuffer.append(" ]");
            resultMap.put("tips", stringBuffer.toString());
            resultMap.put("code", ConstUtil.UNOK);
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addDevice(Device device) {
        if (this.findDeviceNo(device.getDeviceNo()) != null)
            throw new ServiceException(InsertDevice_Error.getCode(), InsertDevice_Error.getMessage());
        device.setCreateTime(LocalDateTime.now());
        device.setUpdateTime(LocalDateTime.now());
        // 默认不在线
        device.setIsLive(ConstUtil.CLOSE_STATUS);
        return super.save(device);
    }

}
