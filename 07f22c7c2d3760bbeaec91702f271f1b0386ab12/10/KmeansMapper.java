package edu.iu.kmeans.regroupallgather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.CollectiveMapper;

import edu.iu.harp.example.DoubleArrPlus;
import edu.iu.harp.partition.Partition;
import edu.iu.harp.partition.Table;
import edu.iu.harp.resource.DoubleArray;
import edu.iu.kmeans.common.KMeansConstants;

public class KmeansMapper extends
  CollectiveMapper<String, String, Object, Object> {

  private int numMappers;
  private int vectorSize;
  private int iteration;
  private int numPoints;
  private double MSE;

  @Override
  protected void setup(Context context)
    throws IOException, InterruptedException {
    LOG.info("start setup"
      + new SimpleDateFormat("yyyyMMdd_HHmmss")
        .format(
          Calendar.getInstance().getTime()));
    long startTime = System.currentTimeMillis();
    Configuration configuration =
      context.getConfiguration();
    numMappers = configuration
      .getInt(KMeansConstants.NUM_MAPPERS, 10);
    vectorSize = configuration
      .getInt(KMeansConstants.VECTOR_SIZE, 20);
    iteration = configuration
      .getInt(KMeansConstants.NUM_ITERATONS, 1);
    long endTime = System.currentTimeMillis();
    LOG.info(
      "config (ms) :" + (endTime - startTime));
  }

  protected void mapCollective(
    KeyValReader reader, Context context)
    throws IOException, InterruptedException {
    LOG.info("Start collective mapper.");
    long startTime = System.currentTimeMillis();
    List<String> pointFiles =
      new ArrayList<String>();
    while (reader.nextKeyValue()) {
      String key = reader.getCurrentKey();
      String value = reader.getCurrentValue();
      LOG.info(
        "Key: " + key + ", Value: " + value);
      pointFiles.add(value);
    }
    Configuration conf =
      context.getConfiguration();
    runKmeans(pointFiles, conf, context);
    LOG.info("Total iterations in master view: "
      + (System.currentTimeMillis() - startTime));
  }

  private void broadcastCentroids(
    Table<DoubleArray> cenTable)
    throws IOException {
    // broadcast centroids
    boolean isSuccess = false;
    try {
      isSuccess =
        broadcast("main", "broadcast-centroids",
          cenTable, this.getMasterID(), false);
    } catch (Exception e) {
      LOG.error("Fail to bcast.", e);
    }
    if (!isSuccess) {
      throw new IOException("Fail to bcast");
    }
  }

  private double computation(
    Table<DoubleArray> cenTable,
    Table<DoubleArray> previousCenTable,
    ArrayList<DoubleArray> dataPoints) {
    double err = 0;
    for (DoubleArray aPoint : dataPoints) {
      // for each data point, find the nearest
      // centroid
      double minDist = -1;
      double tempDist = 0;
      int nearestPartitionID = -1;
      for (Partition ap : previousCenTable
        .getPartitions()) {
        DoubleArray aCentroid =
          (DoubleArray) ap.get();
        tempDist = calcEucDistSquare(aPoint,
          aCentroid, vectorSize);
        if (minDist == -1 || tempDist < minDist) {
          minDist = tempDist;
          nearestPartitionID = ap.id();
        }
      }
      err += minDist;

      // for the certain data point, found the
      // nearest centroid.
      // add the data to a new cenTable.
      double[] partial =
        new double[vectorSize + 1];
      for (int j = 0; j < vectorSize; j++) {
        partial[j] = aPoint.get()[j];
      }
      partial[vectorSize] = 1;

      if (cenTable.getPartition(
        nearestPartitionID) == null) {
        Partition<DoubleArray> tmpAp =
          new Partition<DoubleArray>(
            nearestPartitionID, new DoubleArray(
              partial, 0, vectorSize + 1));
        cenTable.addPartition(tmpAp);

      } else {
        Partition<DoubleArray> apInCenTable =
          cenTable
            .getPartition(nearestPartitionID);
        for (int i = 0; i < vectorSize + 1; i++) {
          apInCenTable.get().get()[i] +=
            partial[i];
        }
      }
    }
    System.out.println("Errors: " + err);
    return err;
  }

  private void calcMSE(Configuration conf)
    throws IOException {
    double[] arrMSE = 
      new double[2];
    arrMSE[0] = numPoints;
    arrMSE[1] = MSE;
    Table<DoubleArray> mseTable =
        new Table<>(0, new DoubleArrPlus());
    Partition<DoubleArray> tmpAp =
        new Partition<DoubleArray>(
          0, new DoubleArray(
            arrMSE, 0, 2));
    mseTable.addPartition(tmpAp);

    // allreduce
    allreduce("main", "allreduce-mse",
          mseTable);

    //get result
    double[] finalArrMSE = mseTable.getPartition(0).get().get();
    double finalMSE = finalArrMSE[1]/finalArrMSE[0];

    mseTable.release();

    //save
    if (this.isMaster()){
      FileSystem fs = FileSystem.get(conf);
      Path path = new Path(conf.get(KMeansConstants.WORK_DIR) + "/evaluation");
      FSDataOutputStream output =
        fs.create(path, true);
      BufferedWriter writer = new BufferedWriter(
        new OutputStreamWriter(output));
      writer.write("MSE : " + finalMSE + "\n");
      writer.close();
    }
  }


  private void runKmeans(List<String> fileNames,
    Configuration conf, Context context)
    throws IOException {
    // -----------------------------------------------------
    // Load centroids
    // for every partition in the centoid table,
    // we will use the last element to store the
    // number of points
    // which are clustered to the particular
    // partitionID
    Table<DoubleArray> cenTable =
      new Table<>(0, new DoubleArrPlus());
    if (this.isMaster()) {
      loadCentroids(cenTable, vectorSize,
        conf.get(KMeansConstants.CFILE), conf);
    }

    System.out.println("After loading centroids");
    printTable(cenTable);

    // broadcast centroids
    broadcastCentroids(cenTable);

    // after broadcasting
    System.out
      .println("After brodcasting centroids");
    printTable(cenTable);

    // load data
    ArrayList<DoubleArray> dataPoints =
      loadData(fileNames, vectorSize, conf);
    numPoints = dataPoints.size();

    Table<DoubleArray> previousCenTable = null;
    // iterations
    for (int iter = 0; iter < iteration; iter++) {
      previousCenTable = cenTable;
      cenTable =
        new Table<>(0, new DoubleArrPlus());

      System.out.println("Iteraton No." + iter);

      // compute new partial centroid table using
      // previousCenTable and data points
      MSE = computation(cenTable, previousCenTable,
        dataPoints);

      /****************************************/
      // regroup and allgather to synchronized
      // centroids
      regroup("main", "regroup_" + iter, cenTable,
        null);
      // we can calculate new centroids
      calculateCentroids(cenTable);
      allgather("main", "allgather_" + iter,
        cenTable);
      /****************************************/

      printTable(cenTable);

    }
    // output results
    calcMSE(conf);
    if (this.isMaster()) {
      outputCentroids(cenTable, conf, context);
    }

  }

  // output centroids
  private void outputCentroids(
    Table<DoubleArray> cenTable,
    Configuration conf, Context context) {
    String output = "";
    for (Partition<DoubleArray> ap : cenTable
      .getPartitions()) {
      double res[] = ap.get().get();
      for (int i = 0; i < vectorSize; i++)
        output += res[i] + "\t";
      output += "\n";
    }
    try {
      context.write(null, new Text(output));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void calculateCentroids(
    Table<DoubleArray> cenTable) {
    for (Partition<DoubleArray> partialCenTable : cenTable
      .getPartitions()) {
      double[] doubles =
        partialCenTable.get().get();
      for (int h = 0; h < vectorSize; h++) {
        doubles[h] /= doubles[vectorSize];
      }
      doubles[vectorSize] = 0;
    }
    System.out
      .println("after calculate new centroids");
    printTable(cenTable);
  }

  // calculate Euclidean distance.
  private double calcEucDistSquare(
    DoubleArray aPoint, DoubleArray otherPoint,
    int vectorSize) {
    double dist = 0;
    for (int i = 0; i < vectorSize; i++) {
      dist += Math.pow(
        aPoint.get()[i] - otherPoint.get()[i], 2);
    }
    return Math.sqrt(dist);
  }

  // load centroids from HDFS
  private void loadCentroids(
    Table<DoubleArray> cenTable, int vectorSize,
    String cFileName, Configuration configuration)
    throws IOException {
    Path cPath = new Path(cFileName);
    FileSystem fs = FileSystem.get(configuration);
    FSDataInputStream in = fs.open(cPath);
    BufferedReader br = new BufferedReader(
      new InputStreamReader(in));
    String line = "";
    String[] vector = null;
    int partitionId = 0;
    while ((line = br.readLine()) != null) {
      vector = line.split("\\s+");
      if (vector.length != vectorSize) {
        System.out.println(
          "Errors while loading centroids .");
        System.exit(-1);
      } else {
        double[] aCen =
          new double[vectorSize + 1];

        for (int i = 0; i < vectorSize; i++) {
          aCen[i] = Double.parseDouble(vector[i]);
        }
        aCen[vectorSize] = 0;
        Partition<DoubleArray> ap =
          new Partition<DoubleArray>(partitionId,
            new DoubleArray(aCen, 0,
              vectorSize + 1));
        cenTable.addPartition(ap);
        partitionId++;
      }
    }
  }

  // load data form HDFS
  private ArrayList<DoubleArray> loadData(
    List<String> fileNames, int vectorSize,
    Configuration conf) throws IOException {
    ArrayList<DoubleArray> data =
      new ArrayList<DoubleArray>();
    for (String filename : fileNames) {
      FileSystem fs = FileSystem.get(conf);
      Path dPath = new Path(filename);
      FSDataInputStream in = fs.open(dPath);
      BufferedReader br = new BufferedReader(
        new InputStreamReader(in));
      String line = "";
      String[] vector = null;
      while ((line = br.readLine()) != null) {
        vector = line.split("\\s+");

        if (vector.length != vectorSize) {
          System.out.println(
            "Errors while loading data.");
          System.exit(-1);
        } else {
          double[] aDataPoint =
            new double[vectorSize];

          for (int i = 0; i < vectorSize; i++) {
            aDataPoint[i] =
              Double.parseDouble(vector[i]);
          }
          DoubleArray da = new DoubleArray(
            aDataPoint, 0, vectorSize);
          data.add(da);
        }
      }
    }
    return data;
  }

  // for testing
  private void
    printTable(Table<DoubleArray> dataTable) {
    for (Partition<DoubleArray> ap : dataTable
      .getPartitions()) {

      double res[] = ap.get().get();
      System.out.print("ID: " + ap.id() + ":");
      for (int i = 0; i < res.length; i++)
        System.out.print(res[i] + "\t");
      System.out.println();
    }
  }
}
