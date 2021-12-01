package com.databasepreservation.common.api.v1;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.services.ContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.ws.rs.Path;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
@Path(ViewerConstants.ENDPOINT_CONTEXT)
public class ContextResource implements ContextService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ContextResource.class);

  @Override
  public String getEnvironment() {
    return System.getProperty("env", "server");
  }

  @Override
  public String getClientMachine() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      LOGGER.debug("UnkownHostException");
    }
    return "";
  }
}
