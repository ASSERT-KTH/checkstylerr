package com.ctrip.framework.apollo.openapi.util;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceLockDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceLockDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenReleaseDTO;
import com.ctrip.framework.apollo.portal.entity.vo.NamespaceVO;

import org.springframework.util.CollectionUtils;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OpenApiBeanUtils {

  private static Gson gson = new Gson();
  private static Type type = new TypeToken<Map<String, String>>() {
  }.getType();

  public static OpenItemDTO transformFromItemDTO(ItemDTO item) {
    Preconditions.checkArgument(item != null);
    return BeanUtils.transfrom(OpenItemDTO.class, item);
  }

  public static ItemDTO transformToItemDTO(OpenItemDTO openItemDTO) {
    Preconditions.checkArgument(openItemDTO != null);
    return BeanUtils.transfrom(ItemDTO.class, openItemDTO);
  }


  public static OpenReleaseDTO transformFromReleaseDTO(ReleaseDTO release) {
    Preconditions.checkArgument(release != null);

    OpenReleaseDTO openReleaseDTO = BeanUtils.transfrom(OpenReleaseDTO.class, release);

    Map<String, String> configs = gson.fromJson(release.getConfigurations(), type);

    openReleaseDTO.setConfigurations(configs);
    return openReleaseDTO;
  }

  public static OpenNamespaceDTO transformFromNamespaceVO(NamespaceVO namespaceVO) {
    Preconditions.checkArgument(namespaceVO != null);

    OpenNamespaceDTO openNamespaceDTO = BeanUtils.transfrom(OpenNamespaceDTO.class, namespaceVO
        .getBaseInfo());

    //app namespace info
    openNamespaceDTO.setFormat(namespaceVO.getFormat());
    openNamespaceDTO.setComment(namespaceVO.getComment());
    openNamespaceDTO.setPublic(namespaceVO.isPublic());

    //items
    List<OpenItemDTO> items = new LinkedList<>();
    List<NamespaceVO.ItemVO> itemVOs = namespaceVO.getItems();
    if (!CollectionUtils.isEmpty(itemVOs)) {
      items.addAll(itemVOs.stream().map(itemVO -> transformFromItemDTO(itemVO.getItem())).collect
          (Collectors.toList()));
    }
    openNamespaceDTO.setItems(items);
    return openNamespaceDTO;

  }

  public static List<OpenNamespaceDTO> batchTransformFromNamespaceVOs(List<NamespaceVO>
                                                                          namespaceVOs) {
    if (CollectionUtils.isEmpty(namespaceVOs)) {
      return Collections.emptyList();
    }

    List<OpenNamespaceDTO> openNamespaceDTOs =
        namespaceVOs.stream().map(OpenApiBeanUtils::transformFromNamespaceVO)
            .collect(Collectors.toCollection(LinkedList::new));

    return openNamespaceDTOs;
  }

  public static OpenNamespaceLockDTO transformFromNamespaceLockDTO(String namespaceName,
                                                                   NamespaceLockDTO
                                                                       namespaceLock) {
    OpenNamespaceLockDTO lock = new OpenNamespaceLockDTO();

    lock.setNamespaceName(namespaceName);

    if (namespaceLock == null) {
      lock.setLocked(false);
    } else {
      lock.setLocked(true);
      lock.setLockedBy(namespaceLock.getDataChangeCreatedBy());
    }

    return lock;
  }

}
