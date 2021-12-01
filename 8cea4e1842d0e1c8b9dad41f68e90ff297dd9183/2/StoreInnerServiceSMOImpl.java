package com.java110.store.smo.impl;

import com.java110.common.util.BeanConvertUtil;
import com.java110.core.base.smo.BaseServiceSMO;
import com.java110.core.smo.store.IStoreInnerServiceSMO;
import com.java110.dto.OwnerCarDto;
import com.java110.dto.PageDto;
import com.java110.dto.UserDto;
import com.java110.dto.store.StoreDto;
import com.java110.store.dao.IStoreServiceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @ClassName StoreInnerServiceSMOImpl 商户内部实现类
 * @Description TODO
 * @Author wuxw
 * @Date 2019/9/20 15:17
 * @Version 1.0
 * add by wuxw 2019/9/20
 **/
@Service("StoreInnerServiceSMOImpl")
public class StoreInnerServiceSMOImpl  extends BaseServiceSMO implements IStoreInnerServiceSMO {

    @Autowired
    private IStoreServiceDao storeServiceDaoImpl;

    @Override
    public List<StoreDto> getStores(StoreDto storeDto) {
        //校验是否传了 分页信息

        int page = storeDto.getPage();

        if (page != PageDto.DEFAULT_PAGE) {
            storeDto.setPage((page - 1) * storeDto.getRow());
            storeDto.setRow(page * storeDto.getRow());
        }

        List<StoreDto> storeDtos = BeanConvertUtil.covertBeanList(storeServiceDaoImpl.getStores(BeanConvertUtil.beanCovertMap(storeDto)), StoreDto.class);

        if (storeDtos == null || storeDtos.size() == 0) {
            return storeDtos;
        }

      /*  String[] userIds = getUserIds(ownerCars);
        //根据 userId 查询用户信息
        List<UserDto> users = userInnerServiceSMOImpl.getUserInfo(userIds);

        for (OwnerCarDto ownerCar : ownerCars) {
            refreshOwnerCar(ownerCar, users);
        }*/
        return storeDtos;
    }


    public IStoreServiceDao getStoreServiceDaoImpl() {
        return storeServiceDaoImpl;
    }

    public void setStoreServiceDaoImpl(IStoreServiceDao storeServiceDaoImpl) {
        this.storeServiceDaoImpl = storeServiceDaoImpl;
    }
}
