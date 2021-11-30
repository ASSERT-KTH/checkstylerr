package com.hlj.proj.service.demo;

import com.hlj.proj.api.demo.DemoEntityService;
import com.hlj.proj.common.page.PageDTO;
import com.hlj.proj.data.dao.db.demo.DemoEntityRepository;
import com.hlj.proj.data.dao.mybatis.demo.DemoEntityMapper;
import com.hlj.proj.data.dao.mybatis.demo.query.DemoEntityQuery;
import com.hlj.proj.data.pojo.demo.DemoEntity;
import com.hlj.proj.dto.Demo.DemoDTO;
import com.hlj.proj.enums.StatusEnum;
import com.hlj.proj.utils.BeanUtils;
import com.hlj.proj.utils.EmptyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @Desc:
 * @Author HealerJean
 * @Date 2018/9/17  下午2:39.
 */
@Service
@Slf4j
public class DemoEntityServiceImpl implements DemoEntityService {

    @Resource
    private DemoEntityMapper demoEntityMapper;

    @Resource
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
        DemoEntityQuery query = BeanUtils.dtoToDemoQuery(demoDTO);
        List<DemoDTO> collect = null;
        List<DemoEntity> list = demoEntityMapper.queryList(query);
        if (!EmptyUtil.isEmpty(list)) {
            collect = list.stream().map(BeanUtils::demoToDTO).collect(Collectors.toList());
        }
        return collect;
    }

    @Override
    public PageDTO<DemoDTO> queryDemoPage(DemoDTO demoDTO) {

        PageDTO<DemoDTO> collect = null;
        Pageable pageable = new PageRequest(demoDTO.getPageNo(), demoDTO.getPageSize());
        DemoEntityQuery query = BeanUtils.dtoToDemoQuery(demoDTO);
        Long count = demoEntityMapper.countQueryContion(query);
        if (count == 0) {
            collect = BeanUtils.toPageDTO(null);
        } else {
            query.setItemCount(count);
            List<DemoEntity> data = demoEntityMapper.queryList(query);
            List<DemoDTO> dtoDatas = data.stream().map(BeanUtils::demoToDTO).collect(Collectors.toList());
            collect = BeanUtils.toPageDTO(new PageImpl<>(dtoDatas, pageable, count));
        }
        return collect;
    }
}
