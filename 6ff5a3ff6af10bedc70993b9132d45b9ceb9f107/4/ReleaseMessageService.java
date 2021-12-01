package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.ReleaseMessage;
import com.ctrip.framework.apollo.biz.repository.ReleaseMessageRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ReleaseMessageService {
  @Autowired
  private ReleaseMessageRepository releaseMessageRepository;

  public ReleaseMessage findLatestReleaseMessageForMessages(Collection<String> messages) {
    return releaseMessageRepository.findTopByMessageInOrderByIdDesc(messages);
  }
}
