package com.hlj.proj.service.demo;

import com.hlj.proj.api.demo.DemoEntityService;
import com.hlj.proj.common.page.PageDTO;
import com.hlj.proj.data.dao.db.demo.DemoEntityRepository;
import com.hlj.proj.data.pojo.demo.DemoEntity;
import com.hlj.proj.data.dao.mybatis.manager.DemoEntityManager;
import com.hlj.proj.data.pojo.demo.DemoEntityPage;
import com.hlj.proj.data.pojo.demo.DemoEntityQuery;
import com.hlj.proj.dto.Demo.DemoDTO;
import com.hlj.proj.enums.StatusEnum;
import com.hlj.proj.utils.BeanUtils;
import com.hlj.proj.utils.EmptyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


/**
 * @Desc:
 * @Author HealerJean
 * @Date 2018/9/17  下午2:39.
 */
@Service
@Slf4j
public class DemoEntityServiceImpl implements DemoEntityService {


    @Autowired
    private DemoEntityManager demoEntityManager;

    @Autowired
    private DemoEntityRepository demoEntityRepository;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public DemoDTO addDemoEntity(DemoDTO demoDTO) {
        DemoEntity demoEntity = BeanUtils.dtoToDemo(demoDTO);
        demoEntity.setDelFlag(StatusEnum.生效.code);
        demoEntityRepository.save(demoEntity);
        demoDTO.setId(demoEntity.getId());
        return demoDTO;
    }

    @Override
    public DemoDTO findById(Long id) {
        DemoEntity demoEntity = demoEntityRepository.findById(id).orElse(null);
        return demoEntity == null ? null : BeanUtils.demoToDTO(demoEntity);
    }

    @Override
    public List<DemoDTO> queryDemoList(DemoDTO demoDTO) {
        List collect = null;
        DemoEntityQuery query = BeanUtils.dtoToDemoQuery(demoDTO);
        List<DemoEntity> list = demoEntityManager.queryList(query);
        if (!EmptyUtil.isEmpty(list)) {
            collect = list.stream().map(BeanUtils::demoToDTO).collect(Collectors.toList());
        }
        return collect;
    }

    @Override
    public PageDTO<DemoDTO> queryDemoPage(DemoDTO demoDTO) {

        List<DemoDTO> collect = null;
        DemoEntityQuery query = BeanUtils.dtoToDemoQuery(demoDTO);
        DemoEntityPage page = demoEntityManager.queryPageList(query);

        List<DemoEntity> types = page.getValues();
        if (types != null) {
            collect = types.stream().map(BeanUtils::demoToDTO).collect(toList());
        }
        return BeanUtils.toPageDTO(page, collect);
    }
}
