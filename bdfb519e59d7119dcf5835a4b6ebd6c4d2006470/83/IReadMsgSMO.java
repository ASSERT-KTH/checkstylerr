package com.java110.web.smo.msg;

import com.java110.core.context.IPageData;
import org.springframework.http.ResponseEntity;

/**
 * 查询 消息
 */
public interface IReadMsgSMO {

    /**
     * 阅读消息
     * @param pd 上下文对象
     * @return
     */
    ResponseEntity<String> readMsg(IPageData pd);
}
