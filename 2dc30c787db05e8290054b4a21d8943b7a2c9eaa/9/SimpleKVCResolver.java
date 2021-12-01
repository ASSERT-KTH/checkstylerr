package com.ctrip.apollo.portal.service.txtresolver;

import com.ctrip.apollo.core.dto.ItemChangeSets;
import com.ctrip.apollo.core.dto.ItemDTO;
import com.ctrip.apollo.core.utils.StringUtils;
import com.ctrip.apollo.portal.util.BeanUtils;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * config item format is K:V##C
 *
 * @Autor lepdou
 */

@Component
public class SimpleKVCResolver implements ConfigTextResolver {

  private static final String KV_SEPARATOR = ":";
  private static final String VC_SEPARATOR = "##";
  private static final String ITEM_SEPARATOR = "\n";


  @Override
  public TextResolverResult resolve(String configText, List<ItemDTO> baseItems) {
    TextResolverResult result = new TextResolverResult();

    if (StringUtils.isEmpty(configText)) {
      result.setCode(TextResolverResult.Code.SIMPLE_KVC_TEXT_EMPTY);
      return result;
    }
    Map<String, ItemDTO> baseKeyMapItem = BeanUtils.mapByKey("key", baseItems);

    String[] items = configText.split(ITEM_SEPARATOR);

    ItemChangeSets changeSets = new ItemChangeSets();

    int lineCounter = 1;
    int kvSeparator, vcSeparator;
    String key, value, comment;
    for (String item : items) {
      kvSeparator = item.indexOf(KV_SEPARATOR);
      vcSeparator = item.indexOf(VC_SEPARATOR);
      if (kvSeparator == -1 || vcSeparator == -1) {
        result.setCode(TextResolverResult.Code.SIMPLTE_KVC_INVALID_FORMAT);
        result.setExtensionMsg(" line:" + lineCounter);
        return result;
      }

      key = item.substring(0, kvSeparator).trim();
      value = item.substring(kvSeparator + 1, vcSeparator).trim();
      comment = item.substring(vcSeparator + 2, item.length()).trim();

      ItemDTO baseItem = baseKeyMapItem.get(key);
      if (baseItem == null) {//new item
        changeSets.addCreatedItem(buildItem(key, value, comment));
      } else if (!value.equals(baseItem.getValue()) || !comment.equals(baseItem.getComment())) {//update item
        changeSets.addupdateItem(buildItem(key, value, comment));
      }

      //deleted items:items in baseItems but not in configText
      baseKeyMapItem.remove(key);

      lineCounter ++;
    }

    //deleted items
    for (Map.Entry<String, ItemDTO> entry : baseKeyMapItem.entrySet()) {
      changeSets.addDeletedItem(entry.getValue());
    }

    result.setCode(TextResolverResult.Code.OK);
    result.setChangeSets(changeSets);
    return result;
  }

  private ItemDTO buildItem(String key, String value, String comment) {
    ItemDTO item = new ItemDTO();
    item.setKey(key);
    item.setValue(value);
    item.setComment(comment);
    return item;
  }


}
