package eu.excitementproject.eop.core;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Logger;
import java.io.*;
import java.lang.reflect.Constructor;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.JCas;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import org.w3c.dom.*;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.TEDecision;
import eu.excitementproject.eop.common.component.distance.DistanceComponentException;
import eu.excitementproject.eop.common.component.distance.DistanceValue;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.configuration.NameValueTable;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.core.component.distance.*;
import eu.excitement.type.entailment.Pair;
import eu.excitementproject.eop.lap.PlatformCASProber;


/**
 * The <code>EditDistanceEDA</code> class implements the <code>EDABasic</code> interface.
 * Given a certain configuration, it can be trained over a specific data set in order to optimize its
 * performance. In the training phase this class produces a distance model for the data set, which
 * includes a distance threshold that best separates the positive and negative examples in the training data.
 * During the test phase it applies the calculated threshold, so that pairs resulting in a distance below the
 * threshold are classified as ENTAILMENT, while pairs above the threshold are classified as NONENTAILMENT.
 * <code>EditDistanceEDA</code> uses <code>FixedWeightEditDistance</code> for calculating edit distance
 * between each pair of T and H.
 * 
 * @author Roberto Zanoli
 * 
 */
public class EditDistanceEDA<T extends TEDecision>
		implements EDABasic<EditDistanceTEDecision> {
	
	/**
	 * the threshold that has to be learnt on a training set and then used
	 * to annotate examples in the test set
	 */
	protected double threshold;
	
	/**
	 * the accuracy obtained on the training data set
	 */
	protected double trainingAccuracy;
	
	/**
	 * the edit distance component to be used
	 */
	protected FixedWeightEditDistance component;
	
	/**
	 * the logger
	 */
	static Logger logger = Logger.getLogger(EditDistanceEDA.class.getName());

	/**
	 * the language
	 */
	protected String language;

	/**
	 * the training data directory
	 */
	protected String trainDIR;

	/**
	 * the test data directory
	 */
	protected String testDIR;

	/**
	 * if the model produced during the training phase
	 * has to be saved into the configuration file itself or
	 * if it should stay in the memory for further calculation 
	 * (e.g. EditDistancePSOEDA uses this modality)
	 */
	protected boolean writeModel;
	
	/**
	 * weight for match
	 */
    protected double mMatchWeight;
    
    /**
	 * weight for delete
	 */
    protected double mDeleteWeight;
    
    /**
	 * weight for insert
	 */
    protected double mInsertWeight;
    
    /**
	 * weight for substitute
	 */
    protected double mSubstituteWeight;
    
    /**
	 * measure to optimize: accuracy or f1 measure
	 */
    protected String measureToOptimize;
    
	/**
	 * if the EDA has to write the learnt model at the end of the training phase
	 * 
	 * @param write true for saving the model
	 *
	 * @return
	 */
	public void setWriteModel(boolean write) {
		
		this.writeModel = write;
		
	}
	
	/**
	 * set the weight of the match edit distant operation
	 * 
	 * @param mMatchWeight the value of the edit distant operation
	 *
	 * @return
	 */
	public void setmMatchWeight(double mMatchWeight) {
    	
    	this.mMatchWeight = mMatchWeight;
    	
    }
	
	/**
	 * get the weight of the match edit distant operation
	 * 
	 * @return the weight
	 */
	public double getmMatchWeight() {
    	
    	return this.mMatchWeight;
    	
    }
    
	/**
	 * set the weight of the delete edit distant operation
	 * 
	 * @param mDeleteWeight the value of the edit distant operation
	 *
	 * @return
	 */
    public void setmDeleteWeight(double mDeleteWeight) {
    	
    	this.mDeleteWeight = mDeleteWeight;
    	
    }
    
    /**
	 * get the weight of the delete edit distant operation
	 * 
	 * @return the weight
	 */
	public double getmDeleteWeight() {
    	
    	return this.mDeleteWeight;
    	
    }
    
    /**
	 * set the weight of the insert edit distant operation
	 * 
	 * @param mInsertWeight the value of the edit distant operation
	 *
	 * @return
	 */
    public void setmInsertWeight(double mInsertWeight) {
    	
    	this.mInsertWeight = mInsertWeight;
    	
    }
    
    /**
	 * get the weight of the insert edit distant operation
	 * 
	 * @return
	 */
    public double getmInsertWeight() {
    	
    	return this.mInsertWeight;
    	
    }
    
    /**
	 * set the weight of the substitute edit distant operation
	 * 
	 * @param mSubstituteWeight the value of the edit distant operation
	 *
	 * @return
	 */
    public void setmSubstituteWeight(double mSubstituteWeight) {
    	
    	this.mSubstituteWeight = mSubstituteWeight;
    	
    }
    
    /**
	 * get the weight of the insert edit distant operation
	 * 
	 * @return
	 */
    public double getmSubstituteWeight() {
    	
    	return this.mSubstituteWeight;
    	
    }
    
    /**
	 * set the measure to be optimize during the training phase
	 * 
	 * @param measureToOptimize the measure to be optimized
	 * 
	 * @return
	 */
    public void setMeasureToOptimize(String measureToOptimize) {
    	
    	this.measureToOptimize = measureToOptimize;
    	
    }
    
    /**
	 * get the measure to be optimize during the training phase
	 * 
	 * @return the name of the measure
	 */
    public String getMeasureToOptimize() {
    	
    	return this.measureToOptimize;
    	
    }
    
    /**
	 * get the type of component (i.e. EditDistanceEDA)
	 * 
	 * @return the type of component
	 */
    protected String getType() {
    	
    	return this.getClass().getCanonicalName();
    	
    }
    
	/**
	 * get the language the EDA has to work with
	 * 
	 * @return the language
	 */
	public String getLanguage() {
		
		return this.language;
		
	}

	/**
	 * get the component used by the EDA
	 * 
	 * @return the component
	 */
	public FixedWeightEditDistance getComponent() {
		
		return this.component;
		
	}

	/**
	 * set the language the EDA has to work with
	 * 
	 * @param language the language
	 * 
	 * @return
	 */
	public void setLanguage(String language) {
		
		this.language = language;
		
	}

	/**
	 * get the training data directory
	 * 
	 * @return the training directory
	 */
	public String getTrainDIR() {
		
		return this.trainDIR;
		
	}
	
	/**
	 * set the training data directory
	 * 
	 */
	public void setTrainDIR(String trainDIR) {
		
		this.trainDIR = trainDIR;
		
	}
	
	/**
	 * get the test data directory
	 * 
	 * @return the test directory
	 */
	public String getTestDIR() {
		
		return this.testDIR;
		
	}
	
	/**
	 * set the test data directory
	 * 
	 */
	public void setTestDIR(String testDIR) {
		
		this.testDIR = testDIR;
		
	}
	
	/**
	 * get the threshold value
	 * 
	 * @return the threshold
	 */
	public double getThreshold() {
		
		return this.threshold;
		
	}
	
	/**
	 * get the accuracy obtained on the training data set
	 * 
	 * @return the accuracy
	 */
	protected double getTrainingAccuracy() {
		
		return this.trainingAccuracy;
		
	}
	
	/**
	 * Construct an edit distance EDA.
	 */
	public EditDistanceEDA() {
    	
		logger.info("Creating an instance of Edit Distance EDA");
		
		this.threshold = Double.NaN;
		this.component = null;
        this.writeModel = true;
        this.mMatchWeight = Double.NaN;
        this.mDeleteWeight = Double.NaN;
        this.mInsertWeight = Double.NaN;
        this.mSubstituteWeight = Double.NaN;
        this.trainDIR = null;
        this.trainDIR = null;
        this.language = null;
        this.measureToOptimize = null;
        
        logger.info("done.");
        
    }
	
	/**
	 * Construct an edit distance EDA with the weights of the edit distance operations set
	 * 
	 * @param mMatchWeight weight for match
	 * @param mDeleteWeight weight for delete
	 * @param mInsertWeight weight for insert
	 * @param mSubstituteWeight weight for substitute
	 * 
	 */
	public EditDistanceEDA(double mMatchWeight, double mDeleteWeight, double mInsertWeight, double mSubstituteWeight) {
		
		this();
		
		this.mMatchWeight = mMatchWeight;
	    this.mDeleteWeight = mDeleteWeight;
	    this.mInsertWeight = mInsertWeight;
	    this.mSubstituteWeight = mSubstituteWeight;
		
	}

	@Override
	public void initialize(CommonConfig config) throws ConfigurationException, EDAException, ComponentException {
		
		logger.info("Initialization ...");
		
		try {
        	
        	//checking the configuration file
			checkConfiguration(config);
			
			//getting the name value table of the EDA
			NameValueTable nameValueTable = config.getSection(this.getType());
			
			//setting the training directory
			if (this.trainDIR == null)
				this.trainDIR = nameValueTable.getString("trainDir");
			
			//setting the test directory
			if (this.testDIR == null)
				this.testDIR = nameValueTable.getString("testDir");
			
			//initializing the threshold value
			initializeThreshold(config);
			
			//initializing the weight of the edit distant operations
			initializeWeights(config);
			
			//component initialization
			String componentName  = nameValueTable.getString("components");
			if (component == null) {
				
				try {
					
					Class<?> componentClass = Class.forName(componentName);
					logger.info("Using:" + componentClass.getCanonicalName());
					Constructor<?> componentClassConstructor = componentClass.getConstructor(CommonConfig.class);
					this.component = (FixedWeightEditDistance) componentClassConstructor.newInstance(config);
					this.component.setmMatchWeight(mMatchWeight);
					this.component.setmDeleteWeight(mDeleteWeight);
					this.component.setmInsertWeight(mInsertWeight);
					this.component.setmSubstituteWeight(mSubstituteWeight);
					
					/*
					 * Initializing FixedWeightEditDistance without a configuration file
					FixedWeightEditDistance fwed = new FixedWeightTokenEditDistance(mMatchWeight, mDeleteWeight, 
							mInsertWeight, mSubstituteWeight, true, "IT", null);
					
					this.component = fwed;
					*/
					
					/*
					 * Initializing FixedWeightEditDistance without a configuration file and using
					 * wikipedia as an external resource.
					Map<String,String> resources = new HashMap<String,String>();
					resources.put("wikipedia", "jdbc:mysql://nathrezim:3306/wikilexresita#root#nat_2k12");
					FixedWeightEditDistance fwed = new FixedWeightTokenEditDistance(mMatchWeight, mDeleteWeight, 
							mInsertWeight, mSubstituteWeight, true, "IT", resources);
					
					this.component = fwed;
					*/
					
				} catch (Exception e) {
					throw new ComponentException(e.getMessage());
				}
				
			}
			
			//setting the measure to be optimized
			if (this.measureToOptimize == null)
				this.measureToOptimize = nameValueTable.getString("measure");
			
		} catch (ConfigurationException e) {
			throw e;
		} catch (Exception e) {
			throw new EDAException(e.getMessage());
		}
		
		logger.info("done.");
	
	}
	
	@Override
	public EditDistanceTEDecision process(JCas jcas) throws EDAException, ComponentException {
		
		String pairId = getPairId(jcas);
		
		//the distance between the T-H pair
		DistanceValue distanceValue = component.calculation(jcas);
		double distance = distanceValue.getDistance();
		
		// During the test phase the method applies the threshold, so that
		// pairs resulting in a distance below the threshold are classiﬁed as ENTAILMENT, while pairs 
		// above the threshold are classiﬁed as NONENTAILMENT.
		if (distance <= this.threshold)
			return new EditDistanceTEDecision(DecisionLabel.Entailment, pairId, threshold - distance);
		
		return new EditDistanceTEDecision(DecisionLabel.NonEntailment, pairId, distance - threshold);
		
	}
	
	@Override
	public void shutdown() {
		
		logger.info("Shutting down ...");
	        	
		if (component != null)
			((FixedWeightEditDistance)component).shutdown();
		
		this.threshold = Double.NaN;
		this.component = null;
        this.writeModel = true;
        this.mMatchWeight = Double.NaN;
        this.mDeleteWeight = Double.NaN;
        this.mInsertWeight = Double.NaN;
        this.mSubstituteWeight = Double.NaN;
        this.trainDIR = null;
        this.trainDIR = null;
        this.language = null;
        this.measureToOptimize = null;
        
        logger.info("done.");
		
	}
	
	@Override
	public void startTraining(CommonConfig config) throws ConfigurationException, EDAException, ComponentException {
		
		logger.info("Training ...");
		
		try {
			
			initialize(config);
			
			//contains the distance between each pair of T-H
			List<DistanceValue> distanceValueList = new ArrayList<DistanceValue>();
			//contains the entailment annotation between each pair of T-H
			List<String> entailmentValueList = new ArrayList<String>();
			//contains the entailment annotation and the distance between each pair of T-H
			List<Annotation> annotationList;
			
			File f = new File(trainDIR);
			if (f.exists() == false) {
				throw new ConfigurationException("trainDIR:" + f.getAbsolutePath() + " not found!");
			}
			
			int filesCounter = 0;
			for (File xmi : f.listFiles()) {
				if (!xmi.getName().endsWith(".xmi")) {
					continue;
				}
				
				JCas cas = PlatformCASProber.probeXmi(xmi, null);
					
				getDistanceValues(cas, distanceValueList);
				getEntailmentAnnotation(cas, entailmentValueList);
				
				filesCounter++;
			}
			
			annotationList = merge(distanceValueList, entailmentValueList);
		
			if (filesCounter == 0)
				throw new ConfigurationException("trainDIR:" + f.getAbsolutePath() + " empty!");
			
			//array of two elements; the first element is the calculated threshold whereas the
			//second one is the obtained accuracy
			double[] thresholdAndAccuracy = 
					//sequentialSearch(distanceValueList, entailmentValueList, measureToOptimize);
					sequentialSearch(annotationList, measureToOptimize);
			
			this.threshold = thresholdAndAccuracy[0];
			this.trainingAccuracy = thresholdAndAccuracy[1];
			
			//it saves the calculated model into the configuration file itself
			if (this.writeModel == true)
				saveModel(config);
			
		} catch (ConfigurationException e) {
			throw e;
		} catch (EDAException e) {
			throw e;
		} catch (ComponentException e) {
			throw e;
		} catch (Exception e) {
			throw new EDAException(e.getMessage());
		}
		
		logger.info("done.");
		
	}
	
	/**
     * Checks the configuration and raise exceptions if the provided
     * configuration is not compatible with this class
     * 
     * param config the configuration
     *
     * @throws ConfigurationException
     */
	protected void checkConfiguration(CommonConfig config) 
			throws ConfigurationException {
		
		if (config == null)
			throw new ConfigurationException("Configuration file not found.");
		
	}
	
	/**
     * Returns the threshold that best separates the positive and negative examples in the training data
     * 
     * @return the threshold and the accuracy
     * 
     * @throws ComponentException, EDAException, Exception
     */
	protected double[] sequentialSearch(List<Annotation> annotationList, String measureToOptimize) 
			throws ComponentException, EDAException, Exception {
		
		//double[0] is the calculated threshold
		//double[1] is the accuracy or the f1 measure
		double[] results = new double[2];
		
		double accuracy = 0.0; //accuracy = (tp+tn)/(tp + fp + fn + tn);
		double accuracyThreshold = -1.0; //the threshold used to obtain the calculated accuracy
		double maxAccuracy = 0.0; //the max value of accuracy
		double f1 = 0.0; //f1 measure = (2 * precision * recall)/(precision + recall);
		double f1Threshold = -1.0; //the threshold used to obtain the calculated f1
		double maxF1 = 0.0; //the max value of f1
		double recall = 0.0;
		double precision = 0.0;
		
		if ( (annotationList != null && annotationList.size() == 0) ||
				annotationList == null) {
			results[0] = 0.0;
			results[1] = 0.0;
			
			return results;
		}
		
		try {
			
			//A copy of the specified list sorted in increasing order from smallest
			List<Annotation> sortedAnnotationList = sortAnnotationList(annotationList);
			
			// true positive
			double tp = 0; 
			// false positive
			double fp = 0; 
			// true negative
			double tn = 0; 
			// false negative
			double fn = 0;
			
			int totNumberOfEntailment = 0;
			int totNumberOfNonEntailment = 0;
			for (int i = 0; i < sortedAnnotationList.size(); i++) {
				if (sortedAnnotationList.get(i).getEntailmentRelation().equals("ENTAILMENT"))
					totNumberOfEntailment++;
				else
					totNumberOfNonEntailment++;
				//System.out.println(sortedAnnotationList.get(i).getDistance().getDistance() + "\t" + sortedAnnotationList.get(i).getEntailmentRelation());
			}
			
			for (int j = -1; j < sortedAnnotationList.size(); j++) {
					
				double distanceValue = 0;
				String entailmentRelation = "";
				
				if ( j != -1 ) {
					
					distanceValue = sortedAnnotationList.get(j).getDistanceValue().getDistance();
					entailmentRelation = sortedAnnotationList.get(j).getEntailmentRelation();
				
					if (entailmentRelation.equals("ENTAILMENT"))
						tp = tp + 1;
					else
						fp = fp + 1;
				
				}
				
				fn = totNumberOfEntailment - tp;
				tn = totNumberOfNonEntailment - fp;
				
				accuracy = (tp + fp + fn + tn == 0) ? 0 : (tp+tn)/(tp + fp + fn + tn);
				precision = (tp + fp == 0) ? 1 : tp / (tp + fp);
				recall = (tp + fn == 0) ? 0 : tp / (tp + fn);
				f1 = (precision + recall == 0) ? 0: (2 * precision * recall)/(precision + recall);
				
				//System.out.println(f1 + "\t" + precision + "\t" + recall + "\t" + accuracy);
				
				if (accuracy > maxAccuracy) {
					maxAccuracy = accuracy;
					accuracyThreshold = distanceValue;
				}
				if (f1 > maxF1) {
					maxF1 = f1;
					f1Threshold = distanceValue;
				}
				
			}
			
		} catch(Exception e) {
			throw e;
		}
		
		if (measureToOptimize.equals("f1")) {
			results[0] = f1Threshold;
			results[1] = maxF1;
		}
		else {
			results[0] = accuracyThreshold;
			results[1] = maxAccuracy;
		}
		
		return results;
		
	}
	
	/**
     * Returns the pair identifier of the pair contained in the specified CAS
     *
     * @param jcas the CAS
     * 
     * @return the pair identifier
     */
	protected String getPairId(JCas jcas) {
		
		FSIterator<TOP> pairIter = jcas.getJFSIndexRepository().getAllIndexedFS(Pair.type);
		
		Pair p = null;
		if (pairIter.hasNext())
			p = (Pair)pairIter.next();
		
		if (p != null)
			return p.getPairID();
	
		return null;
		
	}
	
	/**
     * Returns a copy of the specified list sorted in increasing order from smallest
     * to largest
     *
     * @param distanceValues the list of distance values
     * 
     * @return a copy of the specified list sorted in increasing order
     */
	protected List<Annotation> sortAnnotationList(List<Annotation> annotationList) {
		
		List<Annotation> newAnnotationList = new ArrayList<Annotation>(annotationList);
		
		Collections.copy(newAnnotationList, annotationList);
				
		Collections.sort(newAnnotationList, new Comparator<Annotation>(){
			 
			public int compare(Annotation a1,  Annotation a2) {
                return Double.compare(a1.getDistanceValue().getDistance(),
                		a2.getDistanceValue().getDistance());
			}
			
			/// This method can arise this exception: Comparison method violates its general contract!
            // The reason is reported here: http://stackoverflow.com/questions/19182700/collections-sort-throws-comparison-method-violates-its-general-contract-excep
            /*
			public int compare(Annotation a1,  Annotation a2) {
                return a1.getDistanceValue().getDistance() > a2.getDistanceValue().getDistance() ? 1 :
                	a1.getDistanceValue().getDistance() == a2.getDistanceValue().getDistance() ? 0 : -1;
            }
            */
  
        });
		
		return newAnnotationList;
		
	}
	
	/**
     * Puts distance values calculating for each of the pair T and H
     * of the specified list of Cas into the distanceValues list. 
     * Each of the Cas of the list contains a single pair T-H
     *
     * @param jcas the list of CAS
     * @param distanceValues the list of the distance values
     * 
     * @throws DistanceComponentException
     */
	protected void getDistanceValues(JCas jcas, List<DistanceValue> distanceValues)
			throws DistanceComponentException {
	
		try {

				DistanceValue distanceValue = component.calculation(jcas);
				distanceValues.add(distanceValue);

		} catch(DistanceComponentException e) {
			throw e;
		}
			
	}
		
	/**
     * Puts the entailment annotations calculating of each of the pair T and H
     * of the specified list of Cas into the entailmentValueList list. 
     * Each of the Cas of the list contains a single pair T-H.
     *
     * @param jcas the list of CAS
     * @aram entailmentValueList the list of the entailment annotations
     * 
     * @throws Exception
     */
	protected void getEntailmentAnnotation(JCas jcas, List<String> entailmentValueList) 
			throws Exception {
			
		try {
			
				Pair p = null;
				FSIterator<TOP> pairIter = jcas.getJFSIndexRepository().getAllIndexedFS(Pair.type);
				p = (Pair) pairIter.next();
				String goldAnswer = p.getGoldAnswer();
				entailmentValueList.add(goldAnswer);
			
		} catch(Exception e) {
			throw e;
		}
			
				
	}	
	
	/**
     * For each T/H pair puts its entailment annotation value and distance into a list of Annotations. 
     *
     * @param distanceValues
     * @param entailmentValueList
     * 
     * @return a list of Annotation
     * 
     * @throws Exception
     */
	protected List<Annotation> merge(List<DistanceValue> distanceValues, List<String> entailmentValueList) 
			throws Exception {
			
		List<Annotation>  annotationList = new ArrayList<Annotation>();
		
		try {
			
			for (int i = 0; i < distanceValues.size(); i++) {
				Annotation annotation = new Annotation(distanceValues.get(i), entailmentValueList.get(i));
				annotationList.add(annotation);
			}
			
		} catch(Exception e) {
			throw e;
		}
			
		return annotationList;
				
	}	
	
	/**
     * Save the optimized parameters (e.g. threshold) into the configuration file itself
     */
	protected void saveModel(CommonConfig config) throws IOException {
    	
		logger.info("Writing model ...");
		
    	BufferedWriter writer = null;
    	
    	try {
    		
    		String newModel = updateConfigurationFile(config);
    		
	    	writer = new BufferedWriter(new OutputStreamWriter(
	                  new FileOutputStream(config.getConfigurationFileName()), "UTF-8"));

	    	PrintWriter printout = new PrintWriter(writer);
	    	printout.println(newModel);
	    	printout.close();
	    	
    	} catch (Exception e) {
    		throw new IOException(e.getMessage());
    	} finally {
    		if (writer != null)
    			writer.close();
    	}
    	
    	logger.info("done.");

    }
	
	/**
     * Initialize the threshold getting its value from the configuration file
     * 
     * @param config the configuration
     * 
     */
    protected void initializeThreshold(CommonConfig config) {

    	try{ 
    		
    		NameValueTable weightsTable = config.getSection("model");
    		
    		this.threshold = weightsTable.getDouble("threshold");
    		
    	} catch (Exception e) {
    		
    		logger.info("Could not find the threshold section in the configuration file.");
    	}
    	
    }
    
	/**
     * Initialize the weights of the edit distance operations getting them form the configuration file
     * 
     * If all the values of the 4 edit distance operation are defined they are used
     * to calculate edit distance. Otherwise the weights are read from the configuration file.  
     * 
     * @param config the configuration
     * 
     */
    protected void initializeWeights(CommonConfig config) {

    	try{ 
    		
    		NameValueTable weightsTable = config.getSection(this.getType());
    		
    		//if they weights have already been set use those weights 
        	if (Double.isNaN(this.mMatchWeight) == false && Double.isNaN(this.mDeleteWeight) == false &&
        			Double.isNaN(this.mInsertWeight) == false && Double.isNaN(this.mSubstituteWeight) == false)
        		return;
    		
    		this.mMatchWeight = weightsTable.getDouble("match");
    		this.mDeleteWeight = weightsTable.getDouble("delete");
    		this.mInsertWeight = weightsTable.getDouble("insert");
    		this.mSubstituteWeight = weightsTable.getDouble("substitute");
    		
    	} catch (Exception e) {
    		
    		logger.info("Could not find weights section in configuration file, using defaults");
    		this.mMatchWeight = 0.0;
    		this.mDeleteWeight = 0.0;
    		this.mInsertWeight = 1.0;
    		this.mSubstituteWeight = 1.0;
    		
    	}
    	
    }
    
    /**
     * Reads the configuration file and adds the calculated optimized parameters (i.e. the threshold
     * calculated on the training data set.)
     * 
     * @return the configuration file with the optimized parameters added
     * 
     */
    protected String updateConfigurationFile(CommonConfig config) throws IOException {
    
    	StreamResult result = new StreamResult(new StringWriter());
    	
    	try {
    			
    		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    		Document document = docBuilder.parse(new File(config.getConfigurationFileName()));

    		XPathFactory xpathFactory = XPathFactory.newInstance();
    		XPath xpath = xpathFactory.newXPath();
    		
    		//updating the threshold value and the training accuracy
    		Node thresholdNode = (Node) xpath.evaluate("//*[@name='model']/*[@name='threshold']", document, XPathConstants.NODE);
    		thresholdNode.setTextContent(String.valueOf(this.threshold));
    		Node trainingAccuracyNode = (Node) xpath.evaluate("//*[@name='model']/*[@name='trainingAccuracy']", document, XPathConstants.NODE);
    		trainingAccuracyNode.setTextContent(String.valueOf(this.getTrainingAccuracy()));
    		
    		TransformerFactory tf = TransformerFactory.newInstance();
    		Transformer t = tf.newTransformer();
    		t.transform(new DOMSource(document), result);
    			       			
    	}catch (Exception e) {
    		throw new IOException(e.getMessage());
    	}
    	
    	return result.getWriter().toString();
	    
    }
    
    /* 
     * This class represents the entailment annotation between a T/H pair with its edit distance.
     */
    protected class Annotation {
    	
    	DistanceValue distance;
    	String entailmentRelation;
    	
    	Annotation(DistanceValue distance, String entailmentRelation) {
    		
    		this.distance = distance;
    		this.entailmentRelation = entailmentRelation;
    		
    	}
    	
    	DistanceValue getDistanceValue() {
    		
    		return distance;
    		
    	}
    	
    	String getEntailmentRelation() {
    		
    		return entailmentRelation;
    		
    	}
    	
    }
	
}
