package com.formulasearchengine.mathosphere.mlp.text;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class WikidataLinkMap implements Serializable {

  private static final Logger LOGGER = LogManager.getLogger(WikidataLinkMap.class.getName());
  private final Map<String, String> map;

  public WikidataLinkMap(String fn) {
    map = buildMap(fn, true);
  }

  public WikidataLinkMap(String fn, boolean unique) {
    map = buildMap(fn, unique);
  }

  private static Map<String, String> buildMap(String fn, boolean unique) {
    Map<String, String> keys = new HashMap<>();
    ImmutableMap.Builder<String, String> title2Data = ImmutableMap.builder();
    try {
      FileReader in = new FileReader(fn);
      Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
      for (CSVRecord record : records) {
        String title = record.get(0);
        String item = record.get(1);
        if (!unique) {
          if (keys.containsKey(title)) {
            int itemNew = Integer.parseInt(item.replaceAll("Q(\\d+)", "$1"));
            int itemOld = Integer.parseInt(keys.get(title).replaceAll("Q(\\d+)", "$1"));
            if (itemNew > itemOld) {
              continue;
            }
          }
          keys.put(title, item);
        } else {
          title2Data.put(title, item);
        }
      }
    } catch (java.io.IOException e) {
      LOGGER.error("title2Data-problem");
      e.printStackTrace();
    }
    if (!unique) {
      title2Data.putAll(keys);
    }
    return title2Data.build();
  }

  public String title2Data(String in) {
    in = in.replaceAll("\\[\\[([^\\|]+)\\|?(.*?)\\]\\]", "$1").trim().toLowerCase();
    if (map.containsKey(in)) {
      return map.get(in);
    } else {
      // some heuristics to improve mapping
      in = in.replaceAll("('s|\\(.*?\\))", "").trim();
    }
    return map.get(in.trim().toLowerCase());
  }


  /**
   * Writes the list in memory to a file.
   *
   * @param fn Filename of the output file
   * @return boolean if the writing process was successful
   */
  public boolean writeFile(String fn) {
    try {
      OutputStream out = new FileOutputStream(fn);
      writeObject(out);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  private void writeObject(OutputStream out) throws IOException {
    OutputStreamWriter writer = new OutputStreamWriter(out);
    CSVPrinter printer = CSVFormat.DEFAULT.withRecordSeparator("\n").print(writer);
    for (Map.Entry<String, String> m : map.entrySet()) {
      String[] output = {m.getKey(), m.getValue()};
      printer.printRecord(output);
    }
    writer.flush();
    out.flush();
  }

//  private void writeObject(java.io.ObjectOutputStream out)
//      throws IOException{
//    this.writeObject((OutputStream) out);
//  }
//  private void readObject(java.io.ObjectInputStream in)
//      throws IOException, ClassNotFoundException{
//
//  }
//  private void readObjectNoData()
//      throws ObjectStreamException{
//
//  }

}
