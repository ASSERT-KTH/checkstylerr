package com.ctrip.apollo.cat;

import java.io.IOException;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;

public class NullMessageManager extends ContainerHolder
    implements
      MessageManager,
      Initializable,
      LogEnabled {

  @Inject
  private MessageIdFactory m_factory;

  @Override
  public void enableLogging(Logger logger) {

  }

  @Override
  public void initialize() throws InitializationException {
    try {
      m_factory.initialize("localhost");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void add(Message message) {

  }

  @Override
  public void end(Transaction transaction) {

  }

  @Override
  public Transaction getPeekTransaction() {
    return null;
  }

  @Override
  public MessageTree getThreadLocalMessageTree() {
    return null;
  }

  @Override
  public boolean hasContext() {
    return false;
  }

  @Override
  public boolean isMessageEnabled() {
    return false;
  }

  @Override
  public boolean isCatEnabled() {
    return false;
  }

  @Override
  public boolean isTraceMode() {
    return false;
  }

  @Override
  public void reset() {

  }

  @Override
  public void setTraceMode(boolean traceMode) {

  }

  @Override
  public void setup() {

  }

  @Override
  public void start(Transaction transaction, boolean forked) {

  }

  @Override
  public void bind(String tag, String title) {

  }

  @Override
  public String getDomain() {
    return null;
  }

}
