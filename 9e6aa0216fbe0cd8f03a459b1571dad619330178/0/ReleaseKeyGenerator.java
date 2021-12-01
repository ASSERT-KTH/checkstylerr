package com.ctrip.apollo.biz.utils;

import com.google.common.base.Joiner;

import com.ctrip.apollo.biz.entity.Namespace;
import com.ctrip.apollo.core.utils.ByteUtil;
import com.ctrip.apollo.core.utils.MachineUtil;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ReleaseKeyGenerator {
  private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
  private static final AtomicInteger releaseCounter = new AtomicInteger(new SecureRandom().nextInt());
  private static final Joiner KEY_JOINER = Joiner.on("-");

  /**
   * Generate the release key in the format: timestamp+appId+cluster+namespace+hash(ipAsInt+counter)
   *
   * @param namespace the namespace of the release
   * @return the unique release key
   */
  public static String generateReleaseKey(Namespace namespace) {
    String hexIdString =
        ByteUtil.toHexString(
            toByteArray(Objects.hash(namespace.getAppId(), namespace.getClusterName(),
                namespace.getNamespaceName()), MachineUtil.getMachineIdentifier(),
                releaseCounter.incrementAndGet()));

    return KEY_JOINER.join(TIMESTAMP_FORMAT.format(new Date()), hexIdString);
  }

  /**
   * Concat machine id, counter and key to byte array
   * Only retrieve lower 3 bytes of the id and counter and 2 bytes of the keyHashCode
   */
  private static byte[] toByteArray(int keyHashCode, int machineIdentifier, int counter) {
    byte[] bytes = new byte[8];
    bytes[0] = ByteUtil.int1(keyHashCode);
    bytes[1] = ByteUtil.int0(keyHashCode);
    bytes[2] = ByteUtil.int2(machineIdentifier);
    bytes[3] = ByteUtil.int1(machineIdentifier);
    bytes[4] = ByteUtil.int0(machineIdentifier);
    bytes[5] = ByteUtil.int2(counter);
    bytes[6] = ByteUtil.int1(counter);
    bytes[7] = ByteUtil.int0(counter);
    return bytes;
  }
}
