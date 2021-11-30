package org.radargun.config;

import java.util.List;

import static org.radargun.config.VmArgUtils.ensureArg;
import static org.radargun.config.VmArgUtils.replace;

public class FlightRecorder implements VmArg {
   @Property(doc = "Start flight recording for the benchmark.", optional = false)
   private boolean enabled = false;

   @Property(doc = "File for the recording.")
   private String filename;

   @Property(doc = "Settings file with recording configuration.")
   private String settings;

   @Override
   public void setArgs(List<String> args) {
      if (!enabled)
         return;
      StringBuilder recordingParams = new StringBuilder("=delay=10s,duration=24h");
      if (filename != null)
         recordingParams.append(",filename=").append(filename);
      if (settings != null)
         recordingParams.append(",settings=").append(settings);
      ensureArg(args, "-XX:+UnlockCommercialFeatures");
      ensureArg(args, "-XX:+FlightRecorder");
      replace(args, "-XX:StartFlightRecording", recordingParams.toString());
   }

   public boolean isEnabled() {
      return enabled;
   }

   public String getFilename() {
      return filename;
   }

   public String getSettings() {
      return settings;
   }

}