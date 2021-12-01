package com.ctrip.framework.apollo.metaservice.service;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author : kl
 * Service discovery nacos implementation
 **/
@Service
@Profile({"nacos-discovery"})
public class NacosDiscoveryService implements DiscoveryService {

    private final static Logger logger = LoggerFactory.getLogger(NacosDiscoveryService.class);

    private NamingService namingService;

    @NacosInjected
    public void setNamingService(NamingService namingService) {
        this.namingService = namingService;
    }

    @Override
    public List<ServiceDTO> getServiceInstances(String serviceId) {
        try {
            List<Instance> instances = namingService.selectInstances(serviceId,true);
            List<ServiceDTO> serviceDTOList = Lists.newLinkedList();
            instances.forEach(instance -> {
                ServiceDTO serviceDTO = this.toServiceDTO(instance, serviceId);
                serviceDTOList.add(serviceDTO);
            });
            return serviceDTOList;
        } catch (NacosException ex) {
            logger.error(ex.getMessage(),ex);
        }
        return Collections.emptyList();
    }

    private ServiceDTO toServiceDTO(Instance instance, String appName) {
        ServiceDTO service = new ServiceDTO();
        service.setAppName(appName);
        service.setInstanceId(instance.getInstanceId());
        String homePageUrl = "http://" + instance.getIp() + ":" + instance.getPort() + "/";
        service.setHomepageUrl(homePageUrl);
        return service;
    }
}
