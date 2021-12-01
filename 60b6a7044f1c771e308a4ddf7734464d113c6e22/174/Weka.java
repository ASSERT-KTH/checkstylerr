package ca.queensu.cs.aggregate;


import io.netty.buffer.DrillBuf;

import javax.inject.Inject;

import org.apache.drill.exec.expr.DrillAggFunc;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.FunctionTemplate.FunctionScope;
import org.apache.drill.exec.expr.annotations.FunctionTemplate.NullHandling;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.annotations.Workspace;
import org.apache.drill.exec.expr.holders.BitHolder;
import org.apache.drill.exec.expr.holders.IntHolder;
import org.apache.drill.exec.expr.holders.NullableVarCharHolder;
import org.apache.drill.exec.expr.holders.ObjectHolder;
import org.apache.drill.exec.expr.holders.RepeatedVarCharHolder;
import org.apache.drill.exec.expr.holders.VarCharHolder;


public class Weka {

	/**
	 * @author shadi
	 * 
	 * select qdm_info_weka('?') 
	 * from `output100M.csv` as mydata;
	 * 
	 * OR
	 * 
	 * Select qdm_info_weka('nb') 
	 * from `output100M.csv` as mydata;
	 *
	 */
	@FunctionTemplate(name = "qdm_info_weka", scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE)
	public static class WekaInfoSupportedArgs implements DrillAggFunc{

		@Param  VarCharHolder operation;
		@Workspace  VarCharHolder operationHolder;
		@Workspace  IntHolder operationStringLength;
		@Inject DrillBuf tempBuff;
		@Output VarCharHolder out;

		@Override
		public void setup() {
			operationHolder = new VarCharHolder();
			operationHolder.start = operationHolder.end = 0; 
			operationHolder.buffer = tempBuff;

			operationStringLength = new IntHolder();
			operationStringLength.value=0;
		}

		@Override
		public void add() {
			byte[] operationBuf = new byte[operation.end - operation.start];
			operation.buffer.getBytes(operation.start, operationBuf, 0, operation.end - operation.start);
			operationHolder.buffer.setBytes(0, operationBuf);
			operationStringLength.value = (operation.end - operation.start);

		}

		@Override
		public void output() {
			//			System.out.println("In WekaTrainSupportedArgs output");
			out.buffer = tempBuff;
			byte[] operationBuf = new byte[operationStringLength.value];
			operationHolder.buffer.getBytes(0, operationBuf, 0, operationStringLength.value);
			String function = new String(operationBuf, com.google.common.base.Charsets.UTF_8).toLowerCase();

			String helpText = "";

			org.reflections.Reflections reflections = new org.reflections.Reflections("weka.classifiers"); 
			java.util.Set<Class<? extends weka.classifiers.Classifier>> subTypes = 
					reflections.getSubTypesOf(weka.classifiers.Classifier.class);

			java.util.Iterator<Class<? extends weka.classifiers.Classifier>> subTypesIterator = subTypes.iterator();
			boolean done = false;
			while(subTypesIterator.hasNext() && !done){
				String className = subTypesIterator.next().toString().substring(6);
				//					System.out.println(className.substring(className.indexOf("weka")));
				try{
					Class c = Class.forName(className.substring(className.indexOf("weka")));

					Object t = c.newInstance();
					Class clazz = Class.forName(c.getCanonicalName());
					Class[] interfaces = clazz.getInterfaces();
					String interfacesImplemented = "";
					for(int i=0;i<interfaces.length;i++){
						interfacesImplemented+=interfaces[i].getSimpleName()+" - ";
					}	
					for(Class superClazz = clazz.getSuperclass(); superClazz!=null; superClazz = superClazz.getSuperclass()){
						interfaces = superClazz.getInterfaces();
						for(int i=0;i<interfaces.length;i++){
							interfacesImplemented+=interfaces[i].getSimpleName()+" - ";
						}	
					}
					helpText += "qdm_weka_train(\'"+c.getSimpleName()+"\',arguments,comma-separated features (label is the last column))\n\r";
					helpText += "Type qdm_weka_train(\'"+c.getSimpleName()+"\') for help\n\r";
					if(interfacesImplemented.contains("Aggregateable"))
						helpText +="Aggregateable"+"\n\r";
					if(interfacesImplemented.contains("UpdateableClassifier"))
						helpText +="Updateable"+"\n\r";
					helpText+="---------------------------------------------------------------------------\n\r";
					if(function.equalsIgnoreCase(c.getSimpleName())){
						helpText = "qdm_weka_train(\'"+c.getSimpleName()+"\',arguments,comma-separated features (label is the last column))\n\r";
						if(interfacesImplemented.contains("Aggregateable"))
							helpText +="Aggregateable"+"\n\r";
						if(interfacesImplemented.contains("UpdateableClassifier"))
							helpText +="Updateable"+"\n\r";
						helpText+="---------------------------------------------------------------------------\n\r";

						try{
							java.lang.reflect.Method m = c.getMethod("globalInfo");
							helpText+=":"+m.invoke(t)+"\n\r";
							m = c.getMethod("listOptions");
							java.util.Enumeration<weka.core.Option> e = (java.util.Enumeration<weka.core.Option>)m.invoke(t);

							while(e.hasMoreElements()){
								weka.core.Option tmp = ((weka.core.Option)e.nextElement());
								helpText+=tmp.name()+" : "+tmp.description()+"\n\r";
							}

							helpText+="-classes {c1,c2,c3}"+" : "+"List possible classes for the dataset. If not specified class becomes NUMERIC"+"\n\r"+"\n\r"+"\n\r";
							done = true;
						} catch (Exception e){
							e.printStackTrace();
						}

					}
				} catch (Exception e){

				}
			}

			if(helpText.length() == 0){
				helpText+=":"+"No Args";
			}

			helpText="info||"+helpText;
			out.buffer = out.buffer.reallocIfNeeded(helpText.length()+100);
			out.buffer.setBytes(0,helpText.getBytes(com.google.common.base.Charsets.UTF_8));
			out.start=0;
			out.end=helpText.length();
		}

		@Override
		public void reset() {
			operationHolder.start = operationHolder.end = 0; 

		}

	}


	/**
	 * @author shadi
	 * 
	 * 
	 * qdm_ladp() assigns a sample number to each record so that they can be divided among the worker nodes where a classifier is trained on each
	 * VOTE is then used to combine all workers classiers. 
	 * 
	 * Trian model xyz as 
	 * select qdm_ensemble_weka(mymodel) 
	 * from 
	 * 		(select qdm_ensemble_weka('alg','args', mydata.columns[0],....) as mymodel 
	 * 		from 
	 * 			(select org_data.columns, qdm_ladp(number of workers, org_data.columns[0]) as sample 
	 * 			from `output100M100.csv` as org_data) as mydata 
	 * 			group by sample
	 * 		);
	 * 
	 *
	 */

	@FunctionTemplate(name = "qdm_ladp", scope = FunctionScope.SIMPLE, nulls = NullHandling.INTERNAL)
	public static class WekaLADP1Column implements DrillSimpleFunc{

	@Param	IntHolder NumWorkers;
	@Param  NullableVarCharHolder features;


	@Output VarCharHolder out;
	@Inject DrillBuf tempBuff;

	@Workspace VarCharHolder currVal;
	@Workspace MapHolder<String, Integer> nextSample;

	public void setup() {

		currVal = new VarCharHolder();
		nextSample = new MapHolder<String, Integer>();
		nextSample.map =  new java.util.HashMap<String, Integer>();

	}

	public void eval() {
		byte[] temp = new byte[features.end - features.start];
		features.buffer.getBytes(features.start, temp, 0, features.end - features.start);
		String rowData = new String(temp, com.google.common.base.Charsets.UTF_8);

		String[] attributes = rowData.split(",");
		String label = attributes[attributes.length-1];

		if(nextSample.map.get(label) == null){
			nextSample.map.put(label,0);
		}

		int tmp = Integer.parseInt(""+nextSample.map.get(label));
		String sample = ""+(tmp % NumWorkers.value);
		nextSample.map.put(label,tmp+1);


		out.buffer = tempBuff;
		out.buffer = out.buffer.reallocIfNeeded(sample.getBytes().length);
		out.buffer.setBytes(0, sample.getBytes());//.setBytes(0,outbuff);
		out.start=0;
		out.end=sample.getBytes().length;
	}

}

	/**
	 * @author shadi
	 * 
	 * 
	 * qdm_ladp() assigns a sample number to each record so that they can be divided among the worker nodes where a classifier is trained on each
	 * VOTE is then used to combine all workers classiers. 
	 * 
	 * Trian model xyz as 
	 * select qdm_ensemble_weka(mymodel) 
	 * from 
	 * 		(select qdm_ensemble_weka('alg','args', mydata.columns[0],....) as mymodel 
	 * 		from 
	 * 			(select org_data.columns, qdm_ladp(number of workers, org_data.columns[0],....) as sample 
	 * 			from `output100M100.csv` as org_data) as mydata 
	 * 			group by sample
	 * 		);
	 * 
	 *
	 */

	@FunctionTemplate(name = "qdm_ladp", scope = FunctionScope.SIMPLE, nulls = NullHandling.INTERNAL)
	public static class WekaLADP implements DrillSimpleFunc{
		@Param	IntHolder NumWorkers;
		@Param  VarCharHolder features;


		@Output VarCharHolder out;
		@Inject DrillBuf tempBuff;

		@Workspace VarCharHolder currVal;
		@Workspace MapHolder<String, Integer> nextSample;

		public void setup() {

			currVal = new VarCharHolder();
			nextSample = new MapHolder<String, Integer>();
			nextSample.map =  new java.util.HashMap<String, Integer>();

		}

		public void eval() {
			byte[] temp = new byte[features.end - features.start];
			features.buffer.getBytes(features.start, temp, 0, features.end - features.start);
			String rowData = new String(temp, com.google.common.base.Charsets.UTF_8);

			String[] attributes = rowData.split(",");
			String label = attributes[attributes.length-1];

			if(nextSample.map.get(label) == null){
				nextSample.map.put(label,0);
			}

			int tmp = Integer.parseInt(""+nextSample.map.get(label));
			String sample = ""+(tmp % NumWorkers.value);
			nextSample.map.put(label,tmp+1);


			out.buffer = tempBuff;
			out.buffer = out.buffer.reallocIfNeeded(sample.getBytes().length);
			out.buffer.setBytes(0, sample.getBytes());//.setBytes(0,outbuff);
			out.start=0;
			out.end=sample.getBytes().length;
		}
	}


	/**
	 * @author shadi
	 * 
	 * 
	 * qdm_shuffle() assigns a sample number to each record so that they can be divided among the worker nodes where a classiefier is trained on each
	 * VOTE is then used to combine all workers classiers. 
	 * 
	 * Trian model xyz as 
	 * select qdm_ensemble_weka(mymodel) 
	 * from 
	 * 		(select qdm_ensemble_weka('alg','args', mydata.columns) as mymodel 
	 * 			(select org_data.columns, qdm_shuffle(number of workers, org_data.columns) as sample 
	 * 			from `output100M100.csv` as org_data) as mydata 
	 * 			group by sample
	 * 		);
	 * Dataset Size
	 * Label count
	 * select count(*) from `output100M100.csv`
	 * select label, count(*) from `output100M100.csv` group by label
	 */

	@FunctionTemplate(name = "qdm_ladp", scope = FunctionScope.SIMPLE, nulls = NullHandling.INTERNAL)
	public static class WekaLADPRepeated implements DrillSimpleFunc{
		@Param	IntHolder NumWorkers;
		@Param  RepeatedVarCharHolder features;


		@Output VarCharHolder out;
		@Inject DrillBuf tempBuff;

		@Workspace VarCharHolder currVal;
		@Workspace MapHolder<String, Integer> nextSample;

		public void setup() {

			currVal = new VarCharHolder();
			nextSample = new MapHolder<String, Integer>();
			nextSample.map =  new java.util.HashMap<String, Integer>();

		}

		public void eval() {	
			java.lang.StringBuilder rowBuilder = new java.lang.StringBuilder();
			for (int i = features.start; i < features.end; i++) {
				features.vector.getAccessor().get(i, currVal);
				rowBuilder.append(org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(currVal.start, currVal.end, currVal.buffer)+",");
			} 
			String rowData = rowBuilder.substring(0, rowBuilder.length()-1);

			String[] attributes = rowData.split(",");
			String label = attributes[attributes.length-1];

			if(nextSample.map.get(label) == null){
				nextSample.map.put(label,0);
			}

			int tmp = Integer.parseInt(""+nextSample.map.get(label));
			String sample = ""+(tmp % NumWorkers.value);
			nextSample.map.put(label,tmp+1);


			out.buffer = tempBuff;
			out.buffer = out.buffer.reallocIfNeeded(sample.getBytes().length);
			out.buffer.setBytes(0, sample.getBytes());//.setBytes(0,outbuff);
			out.start=0;
			out.end=sample.getBytes().length;
		}
	}


	
	/**
	 * @author shadi
	 * 
	 * Train model xzy as 
	 * select qdm_Ensemble_weka('NaiveBayesUpdateable','-classes {1,2}', mydata.columns[0], sample) 
	 * from `output100M.csv` as mydata;
	 *
	 */

	@FunctionTemplate(name = "qdm_ensemble_weka", scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE)
	public static class WekaTrainEnsemble1_1Column implements DrillAggFunc{

	@Param  VarCharHolder operation;
	@Param  VarCharHolder arguments;
	@Param  NullableVarCharHolder features;
	@Param  VarCharHolder sample;

	@Output VarCharHolder out;
	@Inject DrillBuf tempBuff;
	@Workspace ObjectHolder classifier;
	@Workspace ObjectHolder function;
	@Workspace ObjectHolder arffHeader;
	@Workspace ObjectHolder newArffHeader;
	@Workspace ObjectHolder instancesHolder;
	@Workspace ObjectHolder writerHolder;
	@Workspace ObjectHolder optionsHolder;
	@Workspace ObjectHolder pathsHolder;
	@Workspace ObjectHolder nextPartition;
	@Workspace  BitHolder firstRun;
	@Workspace  BitHolder fillInstances;
	@Workspace IntHolder updatable;
	@Workspace IntHolder aggregatable;
	@Workspace IntHolder class0Count;
	@Workspace IntHolder class1Count;
	@Workspace ObjectHolder writingPosition;

	public void setup() {
		classifier = new ObjectHolder();
		function = new ObjectHolder();
		arffHeader = new ObjectHolder();
		newArffHeader = new ObjectHolder();
		firstRun = new BitHolder();
		fillInstances = new BitHolder();
		instancesHolder = new ObjectHolder();
		instancesHolder.obj = null;
		writerHolder = new ObjectHolder();
		writerHolder.obj = null;
		optionsHolder = new ObjectHolder();
		optionsHolder.obj = null;
		pathsHolder = new ObjectHolder();
		pathsHolder.obj = null;
		nextPartition = new ObjectHolder();
		nextPartition.obj =  new java.util.HashMap<String, Integer>();
		classifier.obj=null;
		function.obj=null;
		arffHeader.obj=null;
		newArffHeader.obj=null;
		firstRun.value=0;
		fillInstances.value=1;
		updatable = new IntHolder();
		updatable.value=-1;
		aggregatable = new IntHolder();
		aggregatable.value=-1;
		class0Count = new IntHolder();
		class0Count.value=0;
		class1Count = new IntHolder();
		class1Count.value=0;
		writingPosition = new ObjectHolder();
		writingPosition.obj= new Long[]{0L, 0L};
	}

	@Override
	public void add() {

		byte[] temp = new byte[sample.end - sample.start];
		sample.buffer.getBytes(sample.start, temp, 0, sample.end - sample.start);



		java.lang.StringBuilder rowBuilder = new java.lang.StringBuilder();
		VarCharHolder currVal = new VarCharHolder();
		
		byte[] temp2 = new byte[features.end - features.start];
		features.buffer.getBytes(features.start, temp2, 0, features.end - features.start);
		String rowData = new String(temp2, com.google.common.base.Charsets.UTF_8);

		
		
//		for (int i = features.start; i < features.end; i++) {
//			features.vector.getAccessor().get(i, currVal);
//			rowBuilder.append(org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(currVal.start, currVal.end, currVal.buffer)+",");
//		}
//		String rowData = rowBuilder.substring(0, rowBuilder.length()-1);
		
		String [] options = null;
		if(firstRun.value==0){
			firstRun.value = 1;
			System.out.println("Sample#"+new String(temp, com.google.common.base.Charsets.UTF_8));
			byte[] operationBuf = new byte[operation.end - operation.start];
			operation.buffer.getBytes(operation.start, operationBuf, 0, operation.end - operation.start);
			function.obj = new String(operationBuf, com.google.common.base.Charsets.UTF_8).toLowerCase();
			java.util.StringTokenizer st = new java.util.StringTokenizer(rowData, ",");
			int attributesCount = st.countTokens();
			java.lang.StringBuilder stBuilder = new java.lang.StringBuilder();

			byte[] argsBuf = new byte[arguments.end - arguments.start];
			arguments.buffer.getBytes(arguments.start, argsBuf, 0, arguments.end - arguments.start);
			String classType = "numeric";
			try {
				options = weka.core.Utils.splitOptions((new String(argsBuf, com.google.common.base.Charsets.UTF_8)));
				for(int i=0;i<options.length;i++){
					if(options[i].indexOf("classes")>0){
						classType = options[i+1];
						options[i]="";
						options[i+1]="";
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			//				stBuilder.append(function.value+"||"+options+"\n");
			stBuilder.append("@"+"RELATION Drill\n");
			for(int i=0; i< attributesCount-1;i++)
			{
				stBuilder.append("@"+"ATTRIBUTE att"+i+" numeric\n");
			}
			stBuilder.append("@"+"ATTRIBUTE class "+classType+"\n");
			stBuilder.append("@"+"DATA\n");
			arffHeader.obj = stBuilder.toString();

			org.reflections.Reflections reflections = new org.reflections.Reflections("weka.classifiers"); 
			java.util.Set<Class<? extends weka.classifiers.Classifier>> subTypes = 
					reflections.getSubTypesOf(weka.classifiers.Classifier.class);

			java.util.Iterator<Class<? extends weka.classifiers.Classifier>> subTypesIterator = subTypes.iterator();
			boolean done = false;
			while(subTypesIterator.hasNext() && !done){
				String className = subTypesIterator.next().toString().substring(6);
				//					System.out.println(className.substring(className.indexOf("weka")));
				try {
					Class c = Class.forName(className.substring(className.indexOf("weka")));
					if(((String)function.obj).equalsIgnoreCase(c.getSimpleName())){
						function.obj = c.getCanonicalName();
						done =true;
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

			}


			try {
				Class<?> c = Class.forName(((String)function.obj));

				Class[] interfaces = c.getInterfaces();
				updatable.value = 0;
				aggregatable.value = 0;
				for(int i=0;i<interfaces.length;i++){
					if(interfaces[i].getSimpleName().contains("UpdateableClassifier")){
						updatable.value = 1;
					} else if(interfaces[i].getSimpleName().contains("Aggregateable")){
						aggregatable.value = 1;
					}
				}

				if(updatable.value == 0 || aggregatable.value == 0){
					for(Class superClazz = c.getSuperclass(); superClazz!=null; superClazz = superClazz.getSuperclass()){
						interfaces = superClazz.getInterfaces();
						for(int j=0;j<interfaces.length;j++){
							if(interfaces[j].getSimpleName().contains("UpdateableClassifier")){
								updatable.value = 1;
							} else if(interfaces[j].getSimpleName().contains("Aggregateable")){
								aggregatable.value = 1;
							}
						}	
					}
				}

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			writerHolder.obj = new java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>();

			try{

				pathsHolder.obj = new String[] {"1_"+System.currentTimeMillis()+".arff","2_"+System.currentTimeMillis()+".arff"};
				((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).
				add(java.nio.channels.AsynchronousFileChannel.open(java.nio.file.Paths.get(((String[])pathsHolder.obj)[0]), java.nio.file.StandardOpenOption.WRITE, java.nio.file.StandardOpenOption.CREATE));

				((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).
				add(java.nio.channels.AsynchronousFileChannel.open(java.nio.file.Paths.get(((String[])pathsHolder.obj)[1]), java.nio.file.StandardOpenOption.WRITE, java.nio.file.StandardOpenOption.CREATE));


			} catch(Exception ex){
				ex.printStackTrace();
			}
		}

		//Start every run
		try {
			weka.core.Instances instances = new weka.core.Instances(new java.io.StringReader(((String)arffHeader.obj)+rowData));

			instances.setClassIndex(instances.numAttributes() - 1);
			
			if(instances.get(0).classValue() == 0.0){
				class0Count.value++;
			}else {
				class1Count.value++;
			}

			Class<?> c = Class.forName(((String)function.obj));


			if(classifier.obj == null) {
				try{

					optionsHolder.obj = options;

					//						System.out.println("In WekaTrainAgg1Updateable create MODEL");
					classifier.obj = (weka.classifiers.Classifier) c.newInstance(); // new weka.classifiers.bayes.NaiveBayesUpdateable();

					java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
					m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj), new Object[] {options});

					m = c.getMethod("buildClassifier", weka.core.Instances.class);
					m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj),instances);

					//						System.out.println("In WekaTrainEnsemble1Updateable build MODEL done");

					if(updatable.value != 1) {
						//TODO: handle first record for NonUpdatable_NonAggregatable, Currently first record is discarded
						if(instancesHolder.obj == null){
							instancesHolder.obj = instances;
						} else {

							((weka.core.Instances)instancesHolder.obj).add(instances.get(0));
						}
						//							System.out.println("In WekaTrainEnsemble1Updateable build MODEL Not updatable add instances");
					}

				}catch(Exception e){
					e.printStackTrace();
				}
			} else {


				if(updatable.value == 1) {
					//					((weka.classifiers.bayes.NaiveBayesUpdateable)classifier.classifier).updateClassifier(instances.instance(0));

					//							System.out.println("In WekaTrainEnsemble1Updateable add MODEL updatable");
					java.lang.reflect.Method m = c.getMethod("updateClassifier", weka.core.Instance.class);
					m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj),instances.instance(0));
					//							System.out.println("In WekaTrainEnsemble1Updateable updatable MODEL updated");
					//							NaiveBayesUpdateable
				} else {
					//							ZeroR



					if(instancesHolder.obj == null){

						instancesHolder.obj = instances;
					} else {

						if(aggregatable.value > 0 ){

							((weka.core.Instances)instancesHolder.obj).add(instances.get(0));

							if(((weka.core.Instances)instancesHolder.obj).size() > 100000){

								if(aggregatable.value == 1){

									// Remove useless attributes
									//									weka.filters.unsupervised.attribute.RemoveUseless m_AttFilter = new weka.filters.unsupervised.attribute.RemoveUseless();
									//								    m_AttFilter.setInputFormat(((weka.core.Instances)instancesHolder.obj));
									//								    weka.core.Instances train = weka.filters.Filter.useFilter(((weka.core.Instances)instancesHolder.obj), m_AttFilter);
									//								    newArffHeader.obj = train.toString();
									//								    
									//								    newArffHeader.obj = ((String)newArffHeader.obj).substring(0, ((String)newArffHeader.obj).indexOf("DATA")+4)+"\n";
									//								    System.out.println("new header:\n"+(String)newArffHeader.obj);
									//								    



									classifier.obj = (weka.classifiers.Classifier) c.newInstance(); 

									java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
									m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj), new Object[] {options});

									m = c.getMethod("buildClassifier", weka.core.Instances.class);
									m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj),((weka.core.Instances)instancesHolder.obj));
									//											System.out.println("Init Aggregation");
									aggregatable.value = 2;

								} else if(aggregatable.value == 2) {

									//									for(int i=0; i<((weka.core.Instances)instancesHolder.obj).numAttributes();i++){
									//										if(((String)newArffHeader.obj).indexOf(((weka.core.Instances)instancesHolder.obj).attribute(i).name()) < 0){
									//											((weka.core.Instances)instancesHolder.obj).deleteAttributeAt(i);
									//										}
									//									}
									//									System.out.println("new isntances for aggregation \n"+((weka.core.Instances)instancesHolder.obj).toString());

									weka.classifiers.Classifier newClassifier = (weka.classifiers.Classifier) c.newInstance(); 

									java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
									m.invoke(Class.forName(((String)function.obj)).cast(newClassifier), new Object[] {options});

									m = c.getMethod("buildClassifier", weka.core.Instances.class);
									m.invoke(Class.forName(((String)function.obj)).cast(newClassifier),((weka.core.Instances)instancesHolder.obj));
									System.out.println("Aggregation instance class index is:"+((weka.core.Instances)instancesHolder.obj).classIndex());

									try{
										try{
											m = c.getMethod("aggregate",c);
											m.invoke(Class.forName((String)function.obj).cast(classifier.obj),Class.forName((String)function.obj).cast(newClassifier));
											//									System.out.println("In WekaTrainAgg2Updateable add MODEL aggregated");
										} catch (java.lang.reflect.InvocationTargetException e){
											System.out.println("Aggregation fail: Doing voting instead of aggregation");

											weka.classifiers.Classifier firstClassifier = (weka.classifiers.Classifier)classifier.obj;

											classifier.obj = new weka.classifiers.meta.Vote();
											((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(firstClassifier);
											aggregatable.value = 3;
										

											((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);

											e.printStackTrace();
										}
									} catch (java.lang.NoSuchMethodException ex){
										try{
											m = c.getMethod("aggregate",c.getSuperclass());
											m.invoke(Class.forName((String)function.obj).cast(classifier.obj),Class.forName((String)function.obj).cast(newClassifier));
											//									System.out.println("In WekaTrainAgg2Updateable add MODEL parent aggregated");
										
										//											System.out.println("Do Aggregation");
										} catch (java.lang.reflect.InvocationTargetException e){
											System.out.println("Aggregation fail: Doing voting instead of aggregation");


											weka.classifiers.Classifier firstClassifier = (weka.classifiers.Classifier)classifier.obj;

											classifier.obj = new weka.classifiers.meta.Vote();
											((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(firstClassifier);
											aggregatable.value = 3;
											

											((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);

											e.printStackTrace();
										}
									}
								} else {
									System.out.println("Aggregation failed, doing voting in loop!");
									weka.classifiers.Classifier newClassifier = (weka.classifiers.Classifier) c.newInstance(); 

									java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
									m.invoke(Class.forName(((String)function.obj)).cast(newClassifier), new Object[] {options});

									m = c.getMethod("buildClassifier", weka.core.Instances.class);
									m.invoke(Class.forName(((String)function.obj)).cast(newClassifier),((weka.core.Instances)instancesHolder.obj));
									System.out.println("Aggregation instance class index is:"+((weka.core.Instances)instancesHolder.obj).classIndex());
									
									((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);
								}

								// reinitialize instancesHolder.obj
								((weka.core.Instances)instancesHolder.obj).delete();
								instancesHolder.obj = instances;

							} 
						} else {
							//TODO: need handing for memory allocation
							String[] attributes = rowData.split(",");
							String label = attributes[attributes.length-1];

							if(((java.util.HashMap<String, Integer>)nextPartition.obj).get(label) == null){
								((java.util.HashMap<String, Integer>)nextPartition.obj).put(label,0);
							}

							int tmp = Integer.parseInt(""+((java.util.HashMap<String, Integer>)nextPartition.obj).get(label));
							((java.util.HashMap<String, Integer>)nextPartition.obj).put(label,((tmp+1) % 2));


							java.nio.charset.Charset cs = java.nio.charset.Charset.forName("UTF-8");

							java.nio.ByteBuffer dataBuffer = java.nio.ByteBuffer.wrap((rowData+"\n").getBytes(cs));

							
						
							((java.nio.channels.AsynchronousFileChannel)((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).get(tmp)).write(dataBuffer, ((Long[])writingPosition.obj)[tmp]);
							((Long[])writingPosition.obj)[tmp] = ((Long[])writingPosition.obj)[tmp] + (rowData+"\n").getBytes(cs).length;

							if(fillInstances.value==1){								
								try{

									((weka.core.Instances)instancesHolder.obj).add(instances.get(0));

								} catch(Exception error){
									error.printStackTrace();

									fillInstances.value = 0;

									int MegaBytes = 1024 * 1024;
									System.out.println("In 1:  Instances has: "+((weka.core.Instances)instancesHolder.obj).size()+ " records causing Out of memory error");
									long totalMemory = Runtime.getRuntime().totalMemory() / MegaBytes;
									long maxMemory = Runtime.getRuntime().maxMemory() / MegaBytes;
									long freeMemory = Runtime.getRuntime().freeMemory() / MegaBytes;


									System.out.println("totalMemory in JVM shows current size of java heap:"+totalMemory);
									System.out.println("maxMemory in JVM: " + maxMemory);
									System.out.println("freeMemory in JVM: " + freeMemory);
									System.out.println("Used Memory in JVM: " + (totalMemory - freeMemory));

									((weka.core.Instances)instancesHolder.obj).delete();


								}
							}
						}
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	@Override
	public void output() {
		try {

			if(instancesHolder.obj != null){
				System.out.println((classifier.obj != null)+" - "+ ((String)function.obj)+" - In WekaTrainEnsemble1Updateable output rebuild MODEL using instances:"+((weka.core.Instances)instancesHolder.obj).numInstances());
				Class<?> c = Class.forName(((String)function.obj));

				if(aggregatable.value > 0){
					System.out.println("In ensemble1 agg in out");

					if(aggregatable.value == 1){
						classifier.obj = (weka.classifiers.Classifier) c.newInstance(); 

						java.lang.reflect.Method m = c.getMethod("buildClassifier", weka.core.Instances.class);
						m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj),((weka.core.Instances)instancesHolder.obj));
						System.out.println("Init Aggregation");
						aggregatable.value = 2;

					} else if (aggregatable.value == 2){
						//							for(int i=0; i<((weka.core.Instances)instancesHolder.obj).numAttributes();i++){
						//								if(((String)newArffHeader.obj).indexOf(((weka.core.Instances)instancesHolder.obj).attribute(i).name()) < 0){
						//									((weka.core.Instances)instancesHolder.obj).deleteAttributeAt(i);
						//								}
						//							}
						//							System.out.println("new isntances for aggregation \n"+((weka.core.Instances)instancesHolder.obj).toString());
						//						    

						weka.classifiers.Classifier newClassifier = (weka.classifiers.Classifier) c.newInstance(); 


						java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
						m.invoke(Class.forName(((String)function.obj)).cast(newClassifier), new Object[] {(String[])optionsHolder.obj});


						m = c.getMethod("buildClassifier", weka.core.Instances.class);
						m.invoke(Class.forName(((String)function.obj)).cast(newClassifier),((weka.core.Instances)instancesHolder.obj));
						//										System.out.println("Aggregation instance class index is:"+((weka.core.Instances)instancesHolder.obj).classIndex());

						try{
							try{
								m = c.getMethod("aggregate",c);
								m.invoke(Class.forName((String)function.obj).cast(classifier.obj),Class.forName((String)function.obj).cast(newClassifier));
								//									System.out.println("In WekaTrainAgg2Updateable add MODEL aggregated");
							} catch (java.lang.reflect.InvocationTargetException e){
								System.out.println("Aggregation fail: Doing voting instead of aggregation");

								weka.classifiers.Classifier firstClassifier = (weka.classifiers.Classifier)classifier.obj;
								
								classifier.obj = new weka.classifiers.meta.Vote();
								((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(firstClassifier);
								aggregatable.value = 3;
								

								((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);
				
								e.printStackTrace();
							}
						} catch (java.lang.NoSuchMethodException ex){
							try{
								m = c.getMethod("aggregate",c.getSuperclass());
								m.invoke(Class.forName((String)function.obj).cast(classifier.obj),Class.forName((String)function.obj).cast(newClassifier));
								//									System.out.println("In WekaTrainAgg2Updateable add MODEL parent aggregated");
							} catch (java.lang.reflect.InvocationTargetException e){
								System.out.println("Aggregation fail: Doing voting instead of aggregation");

								weka.classifiers.Classifier firstClassifier = (weka.classifiers.Classifier)classifier.obj;
								
								classifier.obj = new weka.classifiers.meta.Vote();
								((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(firstClassifier);
								aggregatable.value = 3;
								

								((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);
				
								e.printStackTrace();
							}
						}
						System.out.println("Do Aggregation");

					} else {
						System.out.println("Aggregation failed, doing voting in out!");
						weka.classifiers.Classifier newClassifier = (weka.classifiers.Classifier) c.newInstance(); 


						java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
						m.invoke(Class.forName(((String)function.obj)).cast(newClassifier), new Object[] {(String[])optionsHolder.obj});


						m = c.getMethod("buildClassifier", weka.core.Instances.class);
						m.invoke(Class.forName(((String)function.obj)).cast(newClassifier),((weka.core.Instances)instancesHolder.obj));
						
						((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);
					}

					// reinitialize instancesHolder.obj
					((weka.core.Instances)instancesHolder.obj).delete();
					instancesHolder.obj = null;

					System.out.println("In ensemble1 agg in out DONE");

				} else {

					//TODO: Handle the memory for this case

					java.io.File f = new java.io.File(((String[])pathsHolder.obj)[0]);

					int MegaBytes = 1024 * 1024;
					long maxMemory = Runtime.getRuntime().maxMemory() / MegaBytes;
					long fileSize = f.length() / MegaBytes;
					long numSubSamples = fileSize*2/maxMemory;

					System.out.println("In function:  data size: "+(2*fileSize)+ " with "+maxMemory+" Max Memory.");

					if(2*fileSize*2/maxMemory < 2){
						try{

							System.out.println("Attempting to train using all data of size:"+ ((weka.core.Instances)instancesHolder.obj).size());
							java.lang.reflect.Method m = c.getMethod("buildClassifier", weka.core.Instances.class);

							m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj),(weka.core.Instances)instancesHolder.obj);
							System.out.println("Attempt SUCCESS to train using all data of size:"+ ((weka.core.Instances)instancesHolder.obj).size());

						} catch(Exception error){
							error.printStackTrace();
						}
					} else if (numSubSamples == 1) {
						//If file can fit in memory train 2 models one on each file
						System.out.println("In 4: Train using all data failed where Instances has: "+((weka.core.Instances)instancesHolder.obj).size()+ " records causing Out of memory error ("+maxMemory+"MB RAM)");
						System.out.println("Now trying to train from the 2 subpartion files");


						//								classifier.obj = this.trainLDAPfile((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj);


						System.out.println("In function:  file size: "+fileSize+ " with "+maxMemory+" Max Memory. Num SubSamples = "+numSubSamples);


						classifier.obj = new weka.classifiers.meta.Vote();

						for(int i=0;i<2;i++){
							System.out.println("Using only the 2 files. Reading file"+((String[])pathsHolder.obj)[i]);

							java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(((String[])pathsHolder.obj)[i]));

							((weka.core.Instances)instancesHolder.obj).delete();

							System.out.println("Before Instance has "+ ((weka.core.Instances)instancesHolder.obj).size()+" instances");

							String record = "";
							int failCount = 0;
							long startTime = System.currentTimeMillis();
							while ((record = br.readLine()) != null) {
								try {
									//										((weka.core.Instances)instancesHolder.obj).addAll(new weka.core.Instances(new java.io.StringReader(((String)arffHeader.obj)+record)));
									((weka.core.Instances)instancesHolder.obj).add(new weka.core.Instances(new java.io.StringReader(((String)arffHeader.obj)+record)).get(0));
								} catch (Exception e) {
									// TODO Auto-generated catch block
									//													e.printStackTrace();
									System.out.println("Failed at this record\n"+record);
									failCount++;
								}

							}
							long endTime = System.currentTimeMillis();

							System.out.println("After Instance has "+ ((weka.core.Instances)instancesHolder.obj).size()+" instances. loaded in "+((endTime-startTime)/1000)+" sec");
							System.out.println("Failed to load: "+failCount+" instances");

							startTime = System.currentTimeMillis();
							try{
								weka.classifiers.Classifier subClassifier = (weka.classifiers.Classifier) c.newInstance(); 

								java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
								m.invoke(Class.forName(((String)function.obj)).cast(subClassifier), new Object[] {(String[])optionsHolder.obj});

								System.out.println("Building subClassifier");
								m = c.getMethod("buildClassifier", weka.core.Instances.class);
								m.invoke(Class.forName(((String)function.obj)).cast(subClassifier),((weka.core.Instances)instancesHolder.obj));


								System.out.println("add subClassifier to voter");
								((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(subClassifier);							
								System.out.println("subClassifier added ");

							} catch (Exception ex){
								ex.printStackTrace();
							}

							endTime = System.currentTimeMillis();

							System.out.println("subclassifier trained in "+((endTime-startTime)/1000)+" sec");

							br.close();
							((java.nio.channels.AsynchronousFileChannel)((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).get(i)).close();
							java.io.File file = new java.io.File(((String[])pathsHolder.obj)[i]);
							System.out.println("File "+((String[])pathsHolder.obj)[i]+(file.delete()?" Deleted":" Failed to Delete"));


						} 

					} else {
						//else read files, split using ladp to the number of samples and train models

						//combine models using voting

						System.out.println("More partions needed!!!");

					}
				} 

				System.out.println("In WekaTrainEnsemble1Updateable output rebuilding MODEL updated");
			} 

			((java.nio.channels.AsynchronousFileChannel)((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).get(0)).close();
			((java.nio.channels.AsynchronousFileChannel)((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).get(1)).close();
			java.io.File file = new java.io.File(((String[])pathsHolder.obj)[0]);
			System.out.println("File "+((String[])pathsHolder.obj)[0]+(file.delete()?" Deleted":" Failed to Delete"));
			file = new java.io.File(((String[])pathsHolder.obj)[1]);
			System.out.println("File "+((String[])pathsHolder.obj)[1]+(file.delete()?" Deleted":" Failed to Delete"));

			
			System.out.println("In Ensemble 1 out: We had C0 = "+class0Count.value+" instances and C1 = "+class1Count.value+" instances");

			java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();

			weka.core.SerializationHelper.write(os, classifier.obj);


			byte[] data = os.toByteArray();
			tempBuff = tempBuff.reallocIfNeeded(data.length);
			out.buffer = tempBuff;
			out.buffer.setBytes(0, data);//.setBytes(0,outbuff);
			out.start=0;
			out.end=data.length;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void reset() {
	}

	
}
	
	
	/**
	 * @author shadi
	 * 
	 * Train model xzy as 
	 * select qdm_Ensemble_weka('NaiveBayesUpdateable','-classes {1,2}', mydata.columns[0],...., sample) 
	 * from `output100M.csv` as mydata;
	 *
	 */

	@FunctionTemplate(name = "qdm_ensemble_weka", scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE)
	public static class WekaTrainEnsemble1Columns implements DrillAggFunc{

	@Param  VarCharHolder operation;
	@Param  VarCharHolder arguments;
	@Param  VarCharHolder features;
	@Param  VarCharHolder sample;

	@Output VarCharHolder out;
	@Inject DrillBuf tempBuff;
	@Workspace ObjectHolder classifier;
	@Workspace ObjectHolder function;
	@Workspace ObjectHolder arffHeader;
	@Workspace ObjectHolder newArffHeader;
	@Workspace ObjectHolder instancesHolder;
	@Workspace ObjectHolder writerHolder;
	@Workspace ObjectHolder optionsHolder;
	@Workspace ObjectHolder pathsHolder;
	@Workspace ObjectHolder nextPartition;
	@Workspace  BitHolder firstRun;
	@Workspace  BitHolder fillInstances;
	@Workspace IntHolder updatable;
	@Workspace IntHolder aggregatable;
	@Workspace IntHolder class0Count;
	@Workspace IntHolder class1Count;
	@Workspace ObjectHolder writingPosition;

	public void setup() {
		classifier = new ObjectHolder();
		function = new ObjectHolder();
		arffHeader = new ObjectHolder();
		newArffHeader = new ObjectHolder();
		firstRun = new BitHolder();
		fillInstances = new BitHolder();
		instancesHolder = new ObjectHolder();
		instancesHolder.obj = null;
		writerHolder = new ObjectHolder();
		writerHolder.obj = null;
		optionsHolder = new ObjectHolder();
		optionsHolder.obj = null;
		pathsHolder = new ObjectHolder();
		pathsHolder.obj = null;
		nextPartition = new ObjectHolder();
		nextPartition.obj =  new java.util.HashMap<String, Integer>();
		classifier.obj=null;
		function.obj=null;
		arffHeader.obj=null;
		newArffHeader.obj=null;
		firstRun.value=0;
		fillInstances.value=1;
		updatable = new IntHolder();
		updatable.value=-1;
		aggregatable = new IntHolder();
		aggregatable.value=-1;
		class0Count = new IntHolder();
		class0Count.value=0;
		class1Count = new IntHolder();
		class1Count.value=0;
		writingPosition = new ObjectHolder();
		writingPosition.obj= new Long[]{0L, 0L};
	}

	@Override
	public void add() {

		byte[] temp = new byte[sample.end - sample.start];
		sample.buffer.getBytes(sample.start, temp, 0, sample.end - sample.start);



		java.lang.StringBuilder rowBuilder = new java.lang.StringBuilder();
		VarCharHolder currVal = new VarCharHolder();
		
		byte[] temp2 = new byte[features.end - features.start];
		features.buffer.getBytes(features.start, temp2, 0, features.end - features.start);
		String rowData = new String(temp2, com.google.common.base.Charsets.UTF_8);

		
		
//		for (int i = features.start; i < features.end; i++) {
//			features.vector.getAccessor().get(i, currVal);
//			rowBuilder.append(org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(currVal.start, currVal.end, currVal.buffer)+",");
//		}
//		String rowData = rowBuilder.substring(0, rowBuilder.length()-1);
		
		String [] options = null;
		if(firstRun.value==0){
			firstRun.value = 1;
			System.out.println("Sample#"+new String(temp, com.google.common.base.Charsets.UTF_8));
			byte[] operationBuf = new byte[operation.end - operation.start];
			operation.buffer.getBytes(operation.start, operationBuf, 0, operation.end - operation.start);
			function.obj = new String(operationBuf, com.google.common.base.Charsets.UTF_8).toLowerCase();
			java.util.StringTokenizer st = new java.util.StringTokenizer(rowData, ",");
			int attributesCount = st.countTokens();
			java.lang.StringBuilder stBuilder = new java.lang.StringBuilder();

			byte[] argsBuf = new byte[arguments.end - arguments.start];
			arguments.buffer.getBytes(arguments.start, argsBuf, 0, arguments.end - arguments.start);
			String classType = "numeric";
			try {
				options = weka.core.Utils.splitOptions((new String(argsBuf, com.google.common.base.Charsets.UTF_8)));
				for(int i=0;i<options.length;i++){
					if(options[i].indexOf("classes")>0){
						classType = options[i+1];
						options[i]="";
						options[i+1]="";
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			//				stBuilder.append(function.value+"||"+options+"\n");
			stBuilder.append("@"+"RELATION Drill\n");
			for(int i=0; i< attributesCount-1;i++)
			{
				stBuilder.append("@"+"ATTRIBUTE att"+i+" numeric\n");
			}
			stBuilder.append("@"+"ATTRIBUTE class "+classType+"\n");
			stBuilder.append("@"+"DATA\n");
			arffHeader.obj = stBuilder.toString();

			org.reflections.Reflections reflections = new org.reflections.Reflections("weka.classifiers"); 
			java.util.Set<Class<? extends weka.classifiers.Classifier>> subTypes = 
					reflections.getSubTypesOf(weka.classifiers.Classifier.class);

			java.util.Iterator<Class<? extends weka.classifiers.Classifier>> subTypesIterator = subTypes.iterator();
			boolean done = false;
			while(subTypesIterator.hasNext() && !done){
				String className = subTypesIterator.next().toString().substring(6);
				//					System.out.println(className.substring(className.indexOf("weka")));
				try {
					Class c = Class.forName(className.substring(className.indexOf("weka")));
					if(((String)function.obj).equalsIgnoreCase(c.getSimpleName())){
						function.obj = c.getCanonicalName();
						done =true;
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

			}


			try {
				Class<?> c = Class.forName(((String)function.obj));

				Class[] interfaces = c.getInterfaces();
				updatable.value = 0;
				aggregatable.value = 0;
				for(int i=0;i<interfaces.length;i++){
					if(interfaces[i].getSimpleName().contains("UpdateableClassifier")){
						updatable.value = 1;
					} else if(interfaces[i].getSimpleName().contains("Aggregateable")){
						aggregatable.value = 1;
					}
				}

				if(updatable.value == 0 || aggregatable.value == 0){
					for(Class superClazz = c.getSuperclass(); superClazz!=null; superClazz = superClazz.getSuperclass()){
						interfaces = superClazz.getInterfaces();
						for(int j=0;j<interfaces.length;j++){
							if(interfaces[j].getSimpleName().contains("UpdateableClassifier")){
								updatable.value = 1;
							} else if(interfaces[j].getSimpleName().contains("Aggregateable")){
								aggregatable.value = 1;
							}
						}	
					}
				}

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			writerHolder.obj = new java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>();

			try{

				pathsHolder.obj = new String[] {"1_"+System.currentTimeMillis()+".arff","2_"+System.currentTimeMillis()+".arff"};
				((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).
				add(java.nio.channels.AsynchronousFileChannel.open(java.nio.file.Paths.get(((String[])pathsHolder.obj)[0]), java.nio.file.StandardOpenOption.WRITE, java.nio.file.StandardOpenOption.CREATE));

				((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).
				add(java.nio.channels.AsynchronousFileChannel.open(java.nio.file.Paths.get(((String[])pathsHolder.obj)[1]), java.nio.file.StandardOpenOption.WRITE, java.nio.file.StandardOpenOption.CREATE));


			} catch(Exception ex){
				ex.printStackTrace();
			}
		}

		//Start every run
		try {
			weka.core.Instances instances = new weka.core.Instances(new java.io.StringReader(((String)arffHeader.obj)+rowData));

			instances.setClassIndex(instances.numAttributes() - 1);
			
			if(instances.get(0).classValue() == 0.0){
				class0Count.value++;
			}else {
				class1Count.value++;
			}

			Class<?> c = Class.forName(((String)function.obj));


			if(classifier.obj == null) {
				try{

					optionsHolder.obj = options;

					//						System.out.println("In WekaTrainAgg1Updateable create MODEL");
					classifier.obj = (weka.classifiers.Classifier) c.newInstance(); // new weka.classifiers.bayes.NaiveBayesUpdateable();

					java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
					m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj), new Object[] {options});

					m = c.getMethod("buildClassifier", weka.core.Instances.class);
					m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj),instances);

					//						System.out.println("In WekaTrainEnsemble1Updateable build MODEL done");

					if(updatable.value != 1) {
						//TODO: handle first record for NonUpdatable_NonAggregatable, Currently first record is discarded
						if(instancesHolder.obj == null){
							instancesHolder.obj = instances;
						} else {

							((weka.core.Instances)instancesHolder.obj).add(instances.get(0));
						}
						//							System.out.println("In WekaTrainEnsemble1Updateable build MODEL Not updatable add instances");
					}

				}catch(Exception e){
					e.printStackTrace();
				}
			} else {


				if(updatable.value == 1) {
					//					((weka.classifiers.bayes.NaiveBayesUpdateable)classifier.classifier).updateClassifier(instances.instance(0));

					//							System.out.println("In WekaTrainEnsemble1Updateable add MODEL updatable");
					java.lang.reflect.Method m = c.getMethod("updateClassifier", weka.core.Instance.class);
					m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj),instances.instance(0));
					//							System.out.println("In WekaTrainEnsemble1Updateable updatable MODEL updated");
					//							NaiveBayesUpdateable
				} else {
					//							ZeroR



					if(instancesHolder.obj == null){

						instancesHolder.obj = instances;
					} else {

						if(aggregatable.value > 0 ){

							((weka.core.Instances)instancesHolder.obj).add(instances.get(0));

							if(((weka.core.Instances)instancesHolder.obj).size() > 100000){

								if(aggregatable.value == 1){

									// Remove useless attributes
									//									weka.filters.unsupervised.attribute.RemoveUseless m_AttFilter = new weka.filters.unsupervised.attribute.RemoveUseless();
									//								    m_AttFilter.setInputFormat(((weka.core.Instances)instancesHolder.obj));
									//								    weka.core.Instances train = weka.filters.Filter.useFilter(((weka.core.Instances)instancesHolder.obj), m_AttFilter);
									//								    newArffHeader.obj = train.toString();
									//								    
									//								    newArffHeader.obj = ((String)newArffHeader.obj).substring(0, ((String)newArffHeader.obj).indexOf("DATA")+4)+"\n";
									//								    System.out.println("new header:\n"+(String)newArffHeader.obj);
									//								    



									classifier.obj = (weka.classifiers.Classifier) c.newInstance(); 

									java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
									m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj), new Object[] {options});

									m = c.getMethod("buildClassifier", weka.core.Instances.class);
									m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj),((weka.core.Instances)instancesHolder.obj));
									//											System.out.println("Init Aggregation");
									aggregatable.value = 2;

								} else if(aggregatable.value == 2) {

									//									for(int i=0; i<((weka.core.Instances)instancesHolder.obj).numAttributes();i++){
									//										if(((String)newArffHeader.obj).indexOf(((weka.core.Instances)instancesHolder.obj).attribute(i).name()) < 0){
									//											((weka.core.Instances)instancesHolder.obj).deleteAttributeAt(i);
									//										}
									//									}
									//									System.out.println("new isntances for aggregation \n"+((weka.core.Instances)instancesHolder.obj).toString());

									weka.classifiers.Classifier newClassifier = (weka.classifiers.Classifier) c.newInstance(); 

									java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
									m.invoke(Class.forName(((String)function.obj)).cast(newClassifier), new Object[] {options});

									m = c.getMethod("buildClassifier", weka.core.Instances.class);
									m.invoke(Class.forName(((String)function.obj)).cast(newClassifier),((weka.core.Instances)instancesHolder.obj));
									System.out.println("Aggregation instance class index is:"+((weka.core.Instances)instancesHolder.obj).classIndex());

									try{
										try{
											m = c.getMethod("aggregate",c);
											m.invoke(Class.forName((String)function.obj).cast(classifier.obj),Class.forName((String)function.obj).cast(newClassifier));
											//									System.out.println("In WekaTrainAgg2Updateable add MODEL aggregated");
										} catch (java.lang.reflect.InvocationTargetException e){
											System.out.println("Aggregation fail: Doing voting instead of aggregation");

											weka.classifiers.Classifier firstClassifier = (weka.classifiers.Classifier)classifier.obj;

											classifier.obj = new weka.classifiers.meta.Vote();
											((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(firstClassifier);
											aggregatable.value = 3;
										

											((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);

											e.printStackTrace();
										}
									} catch (java.lang.NoSuchMethodException ex){
										try{
											m = c.getMethod("aggregate",c.getSuperclass());
											m.invoke(Class.forName((String)function.obj).cast(classifier.obj),Class.forName((String)function.obj).cast(newClassifier));
											//									System.out.println("In WekaTrainAgg2Updateable add MODEL parent aggregated");
										
										//											System.out.println("Do Aggregation");
										} catch (java.lang.reflect.InvocationTargetException e){
											System.out.println("Aggregation fail: Doing voting instead of aggregation");


											weka.classifiers.Classifier firstClassifier = (weka.classifiers.Classifier)classifier.obj;

											classifier.obj = new weka.classifiers.meta.Vote();
											((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(firstClassifier);
											aggregatable.value = 3;
											

											((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);

											e.printStackTrace();
										}
									}
								} else {
									System.out.println("Aggregation failed, doing voting in loop!");
									weka.classifiers.Classifier newClassifier = (weka.classifiers.Classifier) c.newInstance(); 

									java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
									m.invoke(Class.forName(((String)function.obj)).cast(newClassifier), new Object[] {options});

									m = c.getMethod("buildClassifier", weka.core.Instances.class);
									m.invoke(Class.forName(((String)function.obj)).cast(newClassifier),((weka.core.Instances)instancesHolder.obj));
									System.out.println("Aggregation instance class index is:"+((weka.core.Instances)instancesHolder.obj).classIndex());
									
									((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);
								}

								// reinitialize instancesHolder.obj
								((weka.core.Instances)instancesHolder.obj).delete();
								instancesHolder.obj = instances;

							} 
						} else {
							//TODO: need handing for memory allocation
							String[] attributes = rowData.split(",");
							String label = attributes[attributes.length-1];

							if(((java.util.HashMap<String, Integer>)nextPartition.obj).get(label) == null){
								((java.util.HashMap<String, Integer>)nextPartition.obj).put(label,0);
							}

							int tmp = Integer.parseInt(""+((java.util.HashMap<String, Integer>)nextPartition.obj).get(label));
							((java.util.HashMap<String, Integer>)nextPartition.obj).put(label,((tmp+1) % 2));


							java.nio.charset.Charset cs = java.nio.charset.Charset.forName("UTF-8");

							java.nio.ByteBuffer dataBuffer = java.nio.ByteBuffer.wrap((rowData+"\n").getBytes(cs));

							
						
							((java.nio.channels.AsynchronousFileChannel)((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).get(tmp)).write(dataBuffer, ((Long[])writingPosition.obj)[tmp]);
							((Long[])writingPosition.obj)[tmp] = ((Long[])writingPosition.obj)[tmp] + (rowData+"\n").getBytes(cs).length;

							if(fillInstances.value==1){								
								try{

									((weka.core.Instances)instancesHolder.obj).add(instances.get(0));

								} catch(Exception error){
									error.printStackTrace();

									fillInstances.value = 0;

									int MegaBytes = 1024 * 1024;
									System.out.println("In 1:  Instances has: "+((weka.core.Instances)instancesHolder.obj).size()+ " records causing Out of memory error");
									long totalMemory = Runtime.getRuntime().totalMemory() / MegaBytes;
									long maxMemory = Runtime.getRuntime().maxMemory() / MegaBytes;
									long freeMemory = Runtime.getRuntime().freeMemory() / MegaBytes;


									System.out.println("totalMemory in JVM shows current size of java heap:"+totalMemory);
									System.out.println("maxMemory in JVM: " + maxMemory);
									System.out.println("freeMemory in JVM: " + freeMemory);
									System.out.println("Used Memory in JVM: " + (totalMemory - freeMemory));

									((weka.core.Instances)instancesHolder.obj).delete();


								}
							}
						}
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	@Override
	public void output() {
		try {

			if(instancesHolder.obj != null){
				System.out.println((classifier.obj != null)+" - "+ ((String)function.obj)+" - In WekaTrainEnsemble1Updateable output rebuild MODEL using instances:"+((weka.core.Instances)instancesHolder.obj).numInstances());
				Class<?> c = Class.forName(((String)function.obj));

				if(aggregatable.value > 0){
					System.out.println("In ensemble1 agg in out");

					if(aggregatable.value == 1){
						classifier.obj = (weka.classifiers.Classifier) c.newInstance(); 

						java.lang.reflect.Method m = c.getMethod("buildClassifier", weka.core.Instances.class);
						m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj),((weka.core.Instances)instancesHolder.obj));
						System.out.println("Init Aggregation");
						aggregatable.value = 2;

					} else if (aggregatable.value == 2){
						//							for(int i=0; i<((weka.core.Instances)instancesHolder.obj).numAttributes();i++){
						//								if(((String)newArffHeader.obj).indexOf(((weka.core.Instances)instancesHolder.obj).attribute(i).name()) < 0){
						//									((weka.core.Instances)instancesHolder.obj).deleteAttributeAt(i);
						//								}
						//							}
						//							System.out.println("new isntances for aggregation \n"+((weka.core.Instances)instancesHolder.obj).toString());
						//						    

						weka.classifiers.Classifier newClassifier = (weka.classifiers.Classifier) c.newInstance(); 


						java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
						m.invoke(Class.forName(((String)function.obj)).cast(newClassifier), new Object[] {(String[])optionsHolder.obj});


						m = c.getMethod("buildClassifier", weka.core.Instances.class);
						m.invoke(Class.forName(((String)function.obj)).cast(newClassifier),((weka.core.Instances)instancesHolder.obj));
						//										System.out.println("Aggregation instance class index is:"+((weka.core.Instances)instancesHolder.obj).classIndex());

						try{
							try{
								m = c.getMethod("aggregate",c);
								m.invoke(Class.forName((String)function.obj).cast(classifier.obj),Class.forName((String)function.obj).cast(newClassifier));
								//									System.out.println("In WekaTrainAgg2Updateable add MODEL aggregated");
							} catch (java.lang.reflect.InvocationTargetException e){
								System.out.println("Aggregation fail: Doing voting instead of aggregation");

								weka.classifiers.Classifier firstClassifier = (weka.classifiers.Classifier)classifier.obj;
								
								classifier.obj = new weka.classifiers.meta.Vote();
								((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(firstClassifier);
								aggregatable.value = 3;
								

								((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);
				
								e.printStackTrace();
							}
						} catch (java.lang.NoSuchMethodException ex){
							try{
								m = c.getMethod("aggregate",c.getSuperclass());
								m.invoke(Class.forName((String)function.obj).cast(classifier.obj),Class.forName((String)function.obj).cast(newClassifier));
								//									System.out.println("In WekaTrainAgg2Updateable add MODEL parent aggregated");
							} catch (java.lang.reflect.InvocationTargetException e){
								System.out.println("Aggregation fail: Doing voting instead of aggregation");

								weka.classifiers.Classifier firstClassifier = (weka.classifiers.Classifier)classifier.obj;
								
								classifier.obj = new weka.classifiers.meta.Vote();
								((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(firstClassifier);
								aggregatable.value = 3;
								

								((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);
				
								e.printStackTrace();
							}
						}
						System.out.println("Do Aggregation");

					} else {
						System.out.println("Aggregation failed, doing voting in out!");
						weka.classifiers.Classifier newClassifier = (weka.classifiers.Classifier) c.newInstance(); 


						java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
						m.invoke(Class.forName(((String)function.obj)).cast(newClassifier), new Object[] {(String[])optionsHolder.obj});


						m = c.getMethod("buildClassifier", weka.core.Instances.class);
						m.invoke(Class.forName(((String)function.obj)).cast(newClassifier),((weka.core.Instances)instancesHolder.obj));
						
						((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);
					}

					// reinitialize instancesHolder.obj
					((weka.core.Instances)instancesHolder.obj).delete();
					instancesHolder.obj = null;

					System.out.println("In ensemble1 agg in out DONE");

				} else {

					//TODO: Handle the memory for this case

					java.io.File f = new java.io.File(((String[])pathsHolder.obj)[0]);

					int MegaBytes = 1024 * 1024;
					long maxMemory = Runtime.getRuntime().maxMemory() / MegaBytes;
					long fileSize = f.length() / MegaBytes;
					long numSubSamples = fileSize*2/maxMemory;

					System.out.println("In function:  data size: "+(2*fileSize)+ " with "+maxMemory+" Max Memory.");

					if(2*fileSize*2/maxMemory < 2){
						try{

							System.out.println("Attempting to train using all data of size:"+ ((weka.core.Instances)instancesHolder.obj).size());
							java.lang.reflect.Method m = c.getMethod("buildClassifier", weka.core.Instances.class);

							m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj),(weka.core.Instances)instancesHolder.obj);
							System.out.println("Attempt SUCCESS to train using all data of size:"+ ((weka.core.Instances)instancesHolder.obj).size());

						} catch(Exception error){
							error.printStackTrace();
						}
					} else if (numSubSamples == 1) {
						//If file can fit in memory train 2 models one on each file
						System.out.println("In 4: Train using all data failed where Instances has: "+((weka.core.Instances)instancesHolder.obj).size()+ " records causing Out of memory error ("+maxMemory+"MB RAM)");
						System.out.println("Now trying to train from the 2 subpartion files");


						//								classifier.obj = this.trainLDAPfile((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj);


						System.out.println("In function:  file size: "+fileSize+ " with "+maxMemory+" Max Memory. Num SubSamples = "+numSubSamples);


						classifier.obj = new weka.classifiers.meta.Vote();

						for(int i=0;i<2;i++){
							System.out.println("Using only the 2 files. Reading file"+((String[])pathsHolder.obj)[i]);

							java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(((String[])pathsHolder.obj)[i]));

							((weka.core.Instances)instancesHolder.obj).delete();

							System.out.println("Before Instance has "+ ((weka.core.Instances)instancesHolder.obj).size()+" instances");

							String record = "";
							int failCount = 0;
							long startTime = System.currentTimeMillis();
							while ((record = br.readLine()) != null) {
								try {
									//										((weka.core.Instances)instancesHolder.obj).addAll(new weka.core.Instances(new java.io.StringReader(((String)arffHeader.obj)+record)));
									((weka.core.Instances)instancesHolder.obj).add(new weka.core.Instances(new java.io.StringReader(((String)arffHeader.obj)+record)).get(0));
								} catch (Exception e) {
									// TODO Auto-generated catch block
									//													e.printStackTrace();
									System.out.println("Failed at this record\n"+record);
									failCount++;
								}

							}
							long endTime = System.currentTimeMillis();

							System.out.println("After Instance has "+ ((weka.core.Instances)instancesHolder.obj).size()+" instances. loaded in "+((endTime-startTime)/1000)+" sec");
							System.out.println("Failed to load: "+failCount+" instances");

							startTime = System.currentTimeMillis();
							try{
								weka.classifiers.Classifier subClassifier = (weka.classifiers.Classifier) c.newInstance(); 

								java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
								m.invoke(Class.forName(((String)function.obj)).cast(subClassifier), new Object[] {(String[])optionsHolder.obj});

								System.out.println("Building subClassifier");
								m = c.getMethod("buildClassifier", weka.core.Instances.class);
								m.invoke(Class.forName(((String)function.obj)).cast(subClassifier),((weka.core.Instances)instancesHolder.obj));


								System.out.println("add subClassifier to voter");
								((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(subClassifier);							
								System.out.println("subClassifier added ");

							} catch (Exception ex){
								ex.printStackTrace();
							}

							endTime = System.currentTimeMillis();

							System.out.println("subclassifier trained in "+((endTime-startTime)/1000)+" sec");

							br.close();
							((java.nio.channels.AsynchronousFileChannel)((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).get(i)).close();
							java.io.File file = new java.io.File(((String[])pathsHolder.obj)[i]);
							System.out.println("File "+((String[])pathsHolder.obj)[i]+(file.delete()?" Deleted":" Failed to Delete"));


						} 

					} else {
						//else read files, split using ladp to the number of samples and train models

						//combine models using voting

						System.out.println("More partions needed!!!");

					}
				} 

				System.out.println("In WekaTrainEnsemble1Updateable output rebuilding MODEL updated");
			} 

			((java.nio.channels.AsynchronousFileChannel)((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).get(0)).close();
			((java.nio.channels.AsynchronousFileChannel)((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).get(1)).close();
			java.io.File file = new java.io.File(((String[])pathsHolder.obj)[0]);
			System.out.println("File "+((String[])pathsHolder.obj)[0]+(file.delete()?" Deleted":" Failed to Delete"));
			file = new java.io.File(((String[])pathsHolder.obj)[1]);
			System.out.println("File "+((String[])pathsHolder.obj)[1]+(file.delete()?" Deleted":" Failed to Delete"));

			
			System.out.println("In Ensemble 1 out: We had C0 = "+class0Count.value+" instances and C1 = "+class1Count.value+" instances");

			java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();

			weka.core.SerializationHelper.write(os, classifier.obj);


			byte[] data = os.toByteArray();
			tempBuff = tempBuff.reallocIfNeeded(data.length);
			out.buffer = tempBuff;
			out.buffer.setBytes(0, data);//.setBytes(0,outbuff);
			out.start=0;
			out.end=data.length;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void reset() {
	}

	
}

	/**
	 * @author shadi
	 * 
	 * Train model xzy as 
	 * select qdm_Ensemble_weka('NaiveBayesUpdateable','-classes {1,2}', mydata.columns, sample) 
	 * from `output100M.csv` as mydata;
	 *
	 */

	@FunctionTemplate(name = "qdm_ensemble_weka", scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE)
	public static class WekaTrainEnsemble1 implements DrillAggFunc{

		@Param  VarCharHolder operation;
		@Param  VarCharHolder arguments;
		@Param  RepeatedVarCharHolder features;
		@Param  VarCharHolder sample;

		@Output VarCharHolder out;
		@Inject DrillBuf tempBuff;
		@Workspace ObjectHolder classifier;
		@Workspace ObjectHolder function;
		@Workspace ObjectHolder arffHeader;
		@Workspace ObjectHolder newArffHeader;
		@Workspace ObjectHolder instancesHolder;
		@Workspace ObjectHolder writerHolder;
		@Workspace ObjectHolder optionsHolder;
		@Workspace ObjectHolder pathsHolder;
		@Workspace ObjectHolder nextPartition;
		@Workspace  BitHolder firstRun;
		@Workspace  BitHolder fillInstances;
		@Workspace IntHolder updatable;
		@Workspace IntHolder aggregatable;
		@Workspace IntHolder class0Count;
		@Workspace IntHolder class1Count;
		@Workspace ObjectHolder writingPosition;

		public void setup() {
			classifier = new ObjectHolder();
			function = new ObjectHolder();
			arffHeader = new ObjectHolder();
			newArffHeader = new ObjectHolder();
			firstRun = new BitHolder();
			fillInstances = new BitHolder();
			instancesHolder = new ObjectHolder();
			instancesHolder.obj = null;
			writerHolder = new ObjectHolder();
			writerHolder.obj = null;
			optionsHolder = new ObjectHolder();
			optionsHolder.obj = null;
			pathsHolder = new ObjectHolder();
			pathsHolder.obj = null;
			nextPartition = new ObjectHolder();
			nextPartition.obj =  new java.util.HashMap<String, Integer>();
			classifier.obj=null;
			function.obj=null;
			arffHeader.obj=null;
			newArffHeader.obj=null;
			firstRun.value=0;
			fillInstances.value=1;
			updatable = new IntHolder();
			updatable.value=-1;
			aggregatable = new IntHolder();
			aggregatable.value=-1;
			class0Count = new IntHolder();
			class0Count.value=0;
			class1Count = new IntHolder();
			class1Count.value=0;
			writingPosition = new ObjectHolder();
			writingPosition.obj= new Long[]{0L, 0L};
		}

		@Override
		public void add() {

			byte[] temp = new byte[sample.end - sample.start];
			sample.buffer.getBytes(sample.start, temp, 0, sample.end - sample.start);



			java.lang.StringBuilder rowBuilder = new java.lang.StringBuilder();
			VarCharHolder currVal = new VarCharHolder();
			for (int i = features.start; i < features.end; i++) {
				features.vector.getAccessor().get(i, currVal);
				rowBuilder.append(org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(currVal.start, currVal.end, currVal.buffer)+",");
			}
			String rowData = rowBuilder.substring(0, rowBuilder.length()-1);
			String [] options = null;
			if(firstRun.value==0){
				firstRun.value = 1;
				System.out.println("Sample#"+new String(temp, com.google.common.base.Charsets.UTF_8));
				byte[] operationBuf = new byte[operation.end - operation.start];
				operation.buffer.getBytes(operation.start, operationBuf, 0, operation.end - operation.start);
				function.obj = new String(operationBuf, com.google.common.base.Charsets.UTF_8).toLowerCase();
				java.util.StringTokenizer st = new java.util.StringTokenizer(rowData, ",");
				int attributesCount = st.countTokens();
				java.lang.StringBuilder stBuilder = new java.lang.StringBuilder();

				byte[] argsBuf = new byte[arguments.end - arguments.start];
				arguments.buffer.getBytes(arguments.start, argsBuf, 0, arguments.end - arguments.start);
				String classType = "numeric";
				try {
					options = weka.core.Utils.splitOptions((new String(argsBuf, com.google.common.base.Charsets.UTF_8)));
					for(int i=0;i<options.length;i++){
						if(options[i].indexOf("classes")>0){
							classType = options[i+1];
							options[i]="";
							options[i+1]="";
						}
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				//				stBuilder.append(function.value+"||"+options+"\n");
				stBuilder.append("@"+"RELATION Drill\n");
				for(int i=0; i< attributesCount-1;i++)
				{
					stBuilder.append("@"+"ATTRIBUTE att"+i+" numeric\n");
				}
				stBuilder.append("@"+"ATTRIBUTE class "+classType+"\n");
				stBuilder.append("@"+"DATA\n");
				arffHeader.obj = stBuilder.toString();

				org.reflections.Reflections reflections = new org.reflections.Reflections("weka.classifiers"); 
				java.util.Set<Class<? extends weka.classifiers.Classifier>> subTypes = 
						reflections.getSubTypesOf(weka.classifiers.Classifier.class);

				java.util.Iterator<Class<? extends weka.classifiers.Classifier>> subTypesIterator = subTypes.iterator();
				boolean done = false;
				while(subTypesIterator.hasNext() && !done){
					String className = subTypesIterator.next().toString().substring(6);
					//					System.out.println(className.substring(className.indexOf("weka")));
					try {
						Class c = Class.forName(className.substring(className.indexOf("weka")));
						if(((String)function.obj).equalsIgnoreCase(c.getSimpleName())){
							function.obj = c.getCanonicalName();
							done =true;
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

				}


				try {
					Class<?> c = Class.forName(((String)function.obj));

					Class[] interfaces = c.getInterfaces();
					updatable.value = 0;
					aggregatable.value = 0;
					for(int i=0;i<interfaces.length;i++){
						if(interfaces[i].getSimpleName().contains("UpdateableClassifier")){
							updatable.value = 1;
						} else if(interfaces[i].getSimpleName().contains("Aggregateable")){
							aggregatable.value = 1;
						}
					}

					if(updatable.value == 0 || aggregatable.value == 0){
						for(Class superClazz = c.getSuperclass(); superClazz!=null; superClazz = superClazz.getSuperclass()){
							interfaces = superClazz.getInterfaces();
							for(int j=0;j<interfaces.length;j++){
								if(interfaces[j].getSimpleName().contains("UpdateableClassifier")){
									updatable.value = 1;
								} else if(interfaces[j].getSimpleName().contains("Aggregateable")){
									aggregatable.value = 1;
								}
							}	
						}
					}

				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				writerHolder.obj = new java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>();

				try{

					pathsHolder.obj = new String[] {"1_"+System.currentTimeMillis()+".arff","2_"+System.currentTimeMillis()+".arff"};
					((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).
					add(java.nio.channels.AsynchronousFileChannel.open(java.nio.file.Paths.get(((String[])pathsHolder.obj)[0]), java.nio.file.StandardOpenOption.WRITE, java.nio.file.StandardOpenOption.CREATE));

					((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).
					add(java.nio.channels.AsynchronousFileChannel.open(java.nio.file.Paths.get(((String[])pathsHolder.obj)[1]), java.nio.file.StandardOpenOption.WRITE, java.nio.file.StandardOpenOption.CREATE));


				} catch(Exception ex){
					ex.printStackTrace();
				}
			}

			//Start every run
			try {
				weka.core.Instances instances = new weka.core.Instances(new java.io.StringReader(((String)arffHeader.obj)+rowData));

				instances.setClassIndex(instances.numAttributes() - 1);
				
				if(instances.get(0).classValue() == 0.0){
					class0Count.value++;
				}else {
					class1Count.value++;
				}

				Class<?> c = Class.forName(((String)function.obj));


				if(classifier.obj == null) {
					try{

						optionsHolder.obj = options;

						//						System.out.println("In WekaTrainAgg1Updateable create MODEL");
						classifier.obj = (weka.classifiers.Classifier) c.newInstance(); // new weka.classifiers.bayes.NaiveBayesUpdateable();

						java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
						m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj), new Object[] {options});

						m = c.getMethod("buildClassifier", weka.core.Instances.class);
						m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj),instances);

						//						System.out.println("In WekaTrainEnsemble1Updateable build MODEL done");

						if(updatable.value != 1) {
							//TODO: handle first record for NonUpdatable_NonAggregatable, Currently first record is discarded
							if(instancesHolder.obj == null){
								instancesHolder.obj = instances;
							} else {

								((weka.core.Instances)instancesHolder.obj).add(instances.get(0));
							}
							//							System.out.println("In WekaTrainEnsemble1Updateable build MODEL Not updatable add instances");
						}

					}catch(Exception e){
						e.printStackTrace();
					}
				} else {


					if(updatable.value == 1) {
						//					((weka.classifiers.bayes.NaiveBayesUpdateable)classifier.classifier).updateClassifier(instances.instance(0));

						//							System.out.println("In WekaTrainEnsemble1Updateable add MODEL updatable");
						java.lang.reflect.Method m = c.getMethod("updateClassifier", weka.core.Instance.class);
						m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj),instances.instance(0));
						//							System.out.println("In WekaTrainEnsemble1Updateable updatable MODEL updated");
						//							NaiveBayesUpdateable
					} else {
						//							ZeroR



						if(instancesHolder.obj == null){

							instancesHolder.obj = instances;
						} else {

							if(aggregatable.value > 0 ){

								((weka.core.Instances)instancesHolder.obj).add(instances.get(0));

								if(((weka.core.Instances)instancesHolder.obj).size() > 100000){

									if(aggregatable.value == 1){

										// Remove useless attributes
										//									weka.filters.unsupervised.attribute.RemoveUseless m_AttFilter = new weka.filters.unsupervised.attribute.RemoveUseless();
										//								    m_AttFilter.setInputFormat(((weka.core.Instances)instancesHolder.obj));
										//								    weka.core.Instances train = weka.filters.Filter.useFilter(((weka.core.Instances)instancesHolder.obj), m_AttFilter);
										//								    newArffHeader.obj = train.toString();
										//								    
										//								    newArffHeader.obj = ((String)newArffHeader.obj).substring(0, ((String)newArffHeader.obj).indexOf("DATA")+4)+"\n";
										//								    System.out.println("new header:\n"+(String)newArffHeader.obj);
										//								    



										classifier.obj = (weka.classifiers.Classifier) c.newInstance(); 

										java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
										m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj), new Object[] {options});

										m = c.getMethod("buildClassifier", weka.core.Instances.class);
										m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj),((weka.core.Instances)instancesHolder.obj));
										//											System.out.println("Init Aggregation");
										aggregatable.value = 2;

									} else if(aggregatable.value == 2) {

										//									for(int i=0; i<((weka.core.Instances)instancesHolder.obj).numAttributes();i++){
										//										if(((String)newArffHeader.obj).indexOf(((weka.core.Instances)instancesHolder.obj).attribute(i).name()) < 0){
										//											((weka.core.Instances)instancesHolder.obj).deleteAttributeAt(i);
										//										}
										//									}
										//									System.out.println("new isntances for aggregation \n"+((weka.core.Instances)instancesHolder.obj).toString());

										weka.classifiers.Classifier newClassifier = (weka.classifiers.Classifier) c.newInstance(); 

										java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
										m.invoke(Class.forName(((String)function.obj)).cast(newClassifier), new Object[] {options});

										m = c.getMethod("buildClassifier", weka.core.Instances.class);
										m.invoke(Class.forName(((String)function.obj)).cast(newClassifier),((weka.core.Instances)instancesHolder.obj));
										System.out.println("Aggregation instance class index is:"+((weka.core.Instances)instancesHolder.obj).classIndex());

										try{
											try{
												m = c.getMethod("aggregate",c);
												m.invoke(Class.forName((String)function.obj).cast(classifier.obj),Class.forName((String)function.obj).cast(newClassifier));
												//									System.out.println("In WekaTrainAgg2Updateable add MODEL aggregated");
											} catch (java.lang.reflect.InvocationTargetException e){
												System.out.println("Aggregation fail: Doing voting instead of aggregation");

												weka.classifiers.Classifier firstClassifier = (weka.classifiers.Classifier)classifier.obj;

												classifier.obj = new weka.classifiers.meta.Vote();
												((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(firstClassifier);
												aggregatable.value = 3;
											

												((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);

												e.printStackTrace();
											}
										} catch (java.lang.NoSuchMethodException ex){
											try{
												m = c.getMethod("aggregate",c.getSuperclass());
												m.invoke(Class.forName((String)function.obj).cast(classifier.obj),Class.forName((String)function.obj).cast(newClassifier));
												//									System.out.println("In WekaTrainAgg2Updateable add MODEL parent aggregated");
											
											//											System.out.println("Do Aggregation");
											} catch (java.lang.reflect.InvocationTargetException e){
												System.out.println("Aggregation fail: Doing voting instead of aggregation");
	
	
												weka.classifiers.Classifier firstClassifier = (weka.classifiers.Classifier)classifier.obj;

												classifier.obj = new weka.classifiers.meta.Vote();
												((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(firstClassifier);
												aggregatable.value = 3;
												
	
												((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);
	
												e.printStackTrace();
											}
										}
									} else {
										System.out.println("Aggregation failed, doing voting in loop!");
										weka.classifiers.Classifier newClassifier = (weka.classifiers.Classifier) c.newInstance(); 

										java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
										m.invoke(Class.forName(((String)function.obj)).cast(newClassifier), new Object[] {options});

										m = c.getMethod("buildClassifier", weka.core.Instances.class);
										m.invoke(Class.forName(((String)function.obj)).cast(newClassifier),((weka.core.Instances)instancesHolder.obj));
										System.out.println("Aggregation instance class index is:"+((weka.core.Instances)instancesHolder.obj).classIndex());
										
										((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);
									}

									// reinitialize instancesHolder.obj
									((weka.core.Instances)instancesHolder.obj).delete();
									instancesHolder.obj = instances;

								} 
							} else {
								//TODO: need handing for memory allocation
								String[] attributes = rowData.split(",");
								String label = attributes[attributes.length-1];

								if(((java.util.HashMap<String, Integer>)nextPartition.obj).get(label) == null){
									((java.util.HashMap<String, Integer>)nextPartition.obj).put(label,0);
								}

								int tmp = Integer.parseInt(""+((java.util.HashMap<String, Integer>)nextPartition.obj).get(label));
								((java.util.HashMap<String, Integer>)nextPartition.obj).put(label,((tmp+1) % 2));


								java.nio.charset.Charset cs = java.nio.charset.Charset.forName("UTF-8");

								java.nio.ByteBuffer dataBuffer = java.nio.ByteBuffer.wrap((rowData+"\n").getBytes(cs));

								
							
								((java.nio.channels.AsynchronousFileChannel)((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).get(tmp)).write(dataBuffer, ((Long[])writingPosition.obj)[tmp]);
								((Long[])writingPosition.obj)[tmp] = ((Long[])writingPosition.obj)[tmp] + (rowData+"\n").getBytes(cs).length;

								if(fillInstances.value==1){								
									try{

										((weka.core.Instances)instancesHolder.obj).add(instances.get(0));

									} catch(Exception error){
										error.printStackTrace();

										fillInstances.value = 0;

										int MegaBytes = 1024 * 1024;
										System.out.println("In 1:  Instances has: "+((weka.core.Instances)instancesHolder.obj).size()+ " records causing Out of memory error");
										long totalMemory = Runtime.getRuntime().totalMemory() / MegaBytes;
										long maxMemory = Runtime.getRuntime().maxMemory() / MegaBytes;
										long freeMemory = Runtime.getRuntime().freeMemory() / MegaBytes;


										System.out.println("totalMemory in JVM shows current size of java heap:"+totalMemory);
										System.out.println("maxMemory in JVM: " + maxMemory);
										System.out.println("freeMemory in JVM: " + freeMemory);
										System.out.println("Used Memory in JVM: " + (totalMemory - freeMemory));

										((weka.core.Instances)instancesHolder.obj).delete();


									}
								}
							}
						}
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		@Override
		public void output() {
			try {

				if(instancesHolder.obj != null){
					System.out.println((classifier.obj != null)+" - "+ ((String)function.obj)+" - In WekaTrainEnsemble1Updateable output rebuild MODEL using instances:"+((weka.core.Instances)instancesHolder.obj).numInstances());
					Class<?> c = Class.forName(((String)function.obj));

					if(aggregatable.value > 0){
						System.out.println("In ensemble1 agg in out");

						if(aggregatable.value == 1){
							classifier.obj = (weka.classifiers.Classifier) c.newInstance(); 

							java.lang.reflect.Method m = c.getMethod("buildClassifier", weka.core.Instances.class);
							m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj),((weka.core.Instances)instancesHolder.obj));
							System.out.println("Init Aggregation");
							aggregatable.value = 2;

						} else if (aggregatable.value == 2){
							//							for(int i=0; i<((weka.core.Instances)instancesHolder.obj).numAttributes();i++){
							//								if(((String)newArffHeader.obj).indexOf(((weka.core.Instances)instancesHolder.obj).attribute(i).name()) < 0){
							//									((weka.core.Instances)instancesHolder.obj).deleteAttributeAt(i);
							//								}
							//							}
							//							System.out.println("new isntances for aggregation \n"+((weka.core.Instances)instancesHolder.obj).toString());
							//						    

							weka.classifiers.Classifier newClassifier = (weka.classifiers.Classifier) c.newInstance(); 


							java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
							m.invoke(Class.forName(((String)function.obj)).cast(newClassifier), new Object[] {(String[])optionsHolder.obj});


							m = c.getMethod("buildClassifier", weka.core.Instances.class);
							m.invoke(Class.forName(((String)function.obj)).cast(newClassifier),((weka.core.Instances)instancesHolder.obj));
							//										System.out.println("Aggregation instance class index is:"+((weka.core.Instances)instancesHolder.obj).classIndex());

							try{
								try{
									m = c.getMethod("aggregate",c);
									m.invoke(Class.forName((String)function.obj).cast(classifier.obj),Class.forName((String)function.obj).cast(newClassifier));
									//									System.out.println("In WekaTrainAgg2Updateable add MODEL aggregated");
								} catch (java.lang.reflect.InvocationTargetException e){
									System.out.println("Aggregation fail: Doing voting instead of aggregation");
	
									weka.classifiers.Classifier firstClassifier = (weka.classifiers.Classifier)classifier.obj;
									
									classifier.obj = new weka.classifiers.meta.Vote();
									((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(firstClassifier);
									aggregatable.value = 3;
									
	
									((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);
					
									e.printStackTrace();
								}
							} catch (java.lang.NoSuchMethodException ex){
								try{
									m = c.getMethod("aggregate",c.getSuperclass());
									m.invoke(Class.forName((String)function.obj).cast(classifier.obj),Class.forName((String)function.obj).cast(newClassifier));
									//									System.out.println("In WekaTrainAgg2Updateable add MODEL parent aggregated");
								} catch (java.lang.reflect.InvocationTargetException e){
									System.out.println("Aggregation fail: Doing voting instead of aggregation");
	
									weka.classifiers.Classifier firstClassifier = (weka.classifiers.Classifier)classifier.obj;
									
									classifier.obj = new weka.classifiers.meta.Vote();
									((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(firstClassifier);
									aggregatable.value = 3;
									
	
									((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);
					
									e.printStackTrace();
								}
							}
							System.out.println("Do Aggregation");

						} else {
							System.out.println("Aggregation failed, doing voting in out!");
							weka.classifiers.Classifier newClassifier = (weka.classifiers.Classifier) c.newInstance(); 


							java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
							m.invoke(Class.forName(((String)function.obj)).cast(newClassifier), new Object[] {(String[])optionsHolder.obj});


							m = c.getMethod("buildClassifier", weka.core.Instances.class);
							m.invoke(Class.forName(((String)function.obj)).cast(newClassifier),((weka.core.Instances)instancesHolder.obj));
							
							((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);
						}

						// reinitialize instancesHolder.obj
						((weka.core.Instances)instancesHolder.obj).delete();
						instancesHolder.obj = null;

						System.out.println("In ensemble1 agg in out DONE");

					} else {

						//TODO: Handle the memory for this case

						java.io.File f = new java.io.File(((String[])pathsHolder.obj)[0]);

						int MegaBytes = 1024 * 1024;
						long maxMemory = Runtime.getRuntime().maxMemory() / MegaBytes;
						long fileSize = f.length() / MegaBytes;
						long numSubSamples = fileSize*2/maxMemory;

						System.out.println("In function:  data size: "+(2*fileSize)+ " with "+maxMemory+" Max Memory.");

						if(2*fileSize*2/maxMemory < 2){
							try{

								System.out.println("Attempting to train using all data of size:"+ ((weka.core.Instances)instancesHolder.obj).size());
								java.lang.reflect.Method m = c.getMethod("buildClassifier", weka.core.Instances.class);

								m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj),(weka.core.Instances)instancesHolder.obj);
								System.out.println("Attempt SUCCESS to train using all data of size:"+ ((weka.core.Instances)instancesHolder.obj).size());

							} catch(Exception error){
								error.printStackTrace();
							}
						} else if (numSubSamples == 1) {
							//If file can fit in memory train 2 models one on each file
							System.out.println("In 4: Train using all data failed where Instances has: "+((weka.core.Instances)instancesHolder.obj).size()+ " records causing Out of memory error ("+maxMemory+"MB RAM)");
							System.out.println("Now trying to train from the 2 subpartion files");


							//								classifier.obj = this.trainLDAPfile((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj);


							System.out.println("In function:  file size: "+fileSize+ " with "+maxMemory+" Max Memory. Num SubSamples = "+numSubSamples);


							classifier.obj = new weka.classifiers.meta.Vote();

							for(int i=0;i<2;i++){
								System.out.println("Using only the 2 files. Reading file"+((String[])pathsHolder.obj)[i]);

								java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(((String[])pathsHolder.obj)[i]));

								((weka.core.Instances)instancesHolder.obj).delete();

								System.out.println("Before Instance has "+ ((weka.core.Instances)instancesHolder.obj).size()+" instances");

								String record = "";
								int failCount = 0;
								long startTime = System.currentTimeMillis();
								while ((record = br.readLine()) != null) {
									try {
										//										((weka.core.Instances)instancesHolder.obj).addAll(new weka.core.Instances(new java.io.StringReader(((String)arffHeader.obj)+record)));
										((weka.core.Instances)instancesHolder.obj).add(new weka.core.Instances(new java.io.StringReader(((String)arffHeader.obj)+record)).get(0));
									} catch (Exception e) {
										// TODO Auto-generated catch block
										//													e.printStackTrace();
										System.out.println("Failed at this record\n"+record);
										failCount++;
									}

								}
								long endTime = System.currentTimeMillis();

								System.out.println("After Instance has "+ ((weka.core.Instances)instancesHolder.obj).size()+" instances. loaded in "+((endTime-startTime)/1000)+" sec");
								System.out.println("Failed to load: "+failCount+" instances");

								startTime = System.currentTimeMillis();
								try{
									weka.classifiers.Classifier subClassifier = (weka.classifiers.Classifier) c.newInstance(); 

									java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
									m.invoke(Class.forName(((String)function.obj)).cast(subClassifier), new Object[] {(String[])optionsHolder.obj});

									System.out.println("Building subClassifier");
									m = c.getMethod("buildClassifier", weka.core.Instances.class);
									m.invoke(Class.forName(((String)function.obj)).cast(subClassifier),((weka.core.Instances)instancesHolder.obj));


									System.out.println("add subClassifier to voter");
									((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(subClassifier);							
									System.out.println("subClassifier added ");

								} catch (Exception ex){
									ex.printStackTrace();
								}

								endTime = System.currentTimeMillis();

								System.out.println("subclassifier trained in "+((endTime-startTime)/1000)+" sec");

								br.close();
								((java.nio.channels.AsynchronousFileChannel)((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).get(i)).close();
								java.io.File file = new java.io.File(((String[])pathsHolder.obj)[i]);
								System.out.println("File "+((String[])pathsHolder.obj)[i]+(file.delete()?" Deleted":" Failed to Delete"));


							} 

						} else {
							//else read files, split using ladp to the number of samples and train models

							//combine models using voting

							System.out.println("More partions needed!!!");

						}
					} 

					System.out.println("In WekaTrainEnsemble1Updateable output rebuilding MODEL updated");
				} 

				((java.nio.channels.AsynchronousFileChannel)((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).get(0)).close();
				((java.nio.channels.AsynchronousFileChannel)((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).get(1)).close();
				java.io.File file = new java.io.File(((String[])pathsHolder.obj)[0]);
				System.out.println("File "+((String[])pathsHolder.obj)[0]+(file.delete()?" Deleted":" Failed to Delete"));
				file = new java.io.File(((String[])pathsHolder.obj)[1]);
				System.out.println("File "+((String[])pathsHolder.obj)[1]+(file.delete()?" Deleted":" Failed to Delete"));

				
				System.out.println("In Ensemble 1 out: We had C0 = "+class0Count.value+" instances and C1 = "+class1Count.value+" instances");

				java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();

				weka.core.SerializationHelper.write(os, classifier.obj);


				byte[] data = os.toByteArray();
				tempBuff = tempBuff.reallocIfNeeded(data.length);
				out.buffer = tempBuff;
				out.buffer.setBytes(0, data);//.setBytes(0,outbuff);
				out.start=0;
				out.end=data.length;
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public void reset() {
		}

		
	}
	
	
	/**
	 * @author shadi
	 * 
	 * Train model xzy as 
	 * select qdm_Ensemble_weka('NaiveBayesUpdateable','-classes {1,2}', mydata.columns) 
	 * from `output100M.csv` as mydata;
	 *
	 */

	@FunctionTemplate(name = "qdm_ensemble_weka", scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE)
	public static class WekaTrainEnsemble1NoSample implements DrillAggFunc{

		@Param  VarCharHolder operation;
		@Param  VarCharHolder arguments;
		@Param  RepeatedVarCharHolder features;
		//		@Param  VarCharHolder sample;

		@Output VarCharHolder out;
		@Inject DrillBuf tempBuff;
		@Workspace ObjectHolder classifier;
		@Workspace ObjectHolder function;
		@Workspace ObjectHolder arffHeader;
		@Workspace ObjectHolder newArffHeader;
		@Workspace ObjectHolder instancesHolder;
		@Workspace ObjectHolder writerHolder;
		@Workspace ObjectHolder optionsHolder;
		@Workspace ObjectHolder pathsHolder;
		@Workspace ObjectHolder nextPartition;
		@Workspace  BitHolder firstRun;
		@Workspace  BitHolder fillInstances;
		@Workspace IntHolder updatable;
		@Workspace IntHolder aggregatable;
		@Workspace IntHolder class0Count;
		@Workspace IntHolder class1Count;
		@Workspace ObjectHolder writingPosition;

		public void setup() {
			classifier = new ObjectHolder();
			function = new ObjectHolder();
			arffHeader = new ObjectHolder();
			newArffHeader = new ObjectHolder();
			firstRun = new BitHolder();
			fillInstances = new BitHolder();
			instancesHolder = new ObjectHolder();
			instancesHolder.obj = null;
			writerHolder = new ObjectHolder();
			writerHolder.obj = null;
			optionsHolder = new ObjectHolder();
			optionsHolder.obj = null;
			pathsHolder = new ObjectHolder();
			pathsHolder.obj = null;
			nextPartition = new ObjectHolder();
			nextPartition.obj =  new java.util.HashMap<String, Integer>();
			classifier.obj=null;
			function.obj=null;
			arffHeader.obj=null;
			newArffHeader.obj=null;
			firstRun.value=0;
			fillInstances.value=1;
			updatable = new IntHolder();
			updatable.value=-1;
			aggregatable = new IntHolder();
			aggregatable.value=-1;
			class0Count = new IntHolder();
			class0Count.value=0;
			class1Count = new IntHolder();
			class1Count.value=0;
			writingPosition = new ObjectHolder();
			writingPosition.obj= new Long[]{0L, 0L};
		}

		@Override
		public void add() {

			//			byte[] temp = new byte[sample.end - sample.start];
			//			sample.buffer.getBytes(sample.start, temp, 0, sample.end - sample.start);



			java.lang.StringBuilder rowBuilder = new java.lang.StringBuilder();
			VarCharHolder currVal = new VarCharHolder();
			for (int i = features.start; i < features.end; i++) {
				features.vector.getAccessor().get(i, currVal);
				rowBuilder.append(org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(currVal.start, currVal.end, currVal.buffer)+",");
			}
			String rowData = rowBuilder.substring(0, rowBuilder.length()-1);
			String [] options = null;
			if(firstRun.value==0){
				System.out.println("In No sample");
				firstRun.value = 1;
				byte[] operationBuf = new byte[operation.end - operation.start];
				operation.buffer.getBytes(operation.start, operationBuf, 0, operation.end - operation.start);
				function.obj = new String(operationBuf, com.google.common.base.Charsets.UTF_8).toLowerCase();
				java.util.StringTokenizer st = new java.util.StringTokenizer(rowData, ",");
				int attributesCount = st.countTokens();
				java.lang.StringBuilder stBuilder = new java.lang.StringBuilder();

				byte[] argsBuf = new byte[arguments.end - arguments.start];
				arguments.buffer.getBytes(arguments.start, argsBuf, 0, arguments.end - arguments.start);
				String classType = "numeric";
				try {
					options = weka.core.Utils.splitOptions((new String(argsBuf, com.google.common.base.Charsets.UTF_8)));
					for(int i=0;i<options.length;i++){
						if(options[i].indexOf("classes")>0){
							classType = options[i+1];
							options[i]="";
							options[i+1]="";
						}
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				//				stBuilder.append(function.value+"||"+options+"\n");
				stBuilder.append("@"+"RELATION Drill\n");
				for(int i=0; i< attributesCount-1;i++)
				{
					stBuilder.append("@"+"ATTRIBUTE att"+i+" numeric\n");
				}
				stBuilder.append("@"+"ATTRIBUTE class "+classType+"\n");
				stBuilder.append("@"+"DATA\n");
				arffHeader.obj = stBuilder.toString();

				org.reflections.Reflections reflections = new org.reflections.Reflections("weka.classifiers"); 
				java.util.Set<Class<? extends weka.classifiers.Classifier>> subTypes = 
						reflections.getSubTypesOf(weka.classifiers.Classifier.class);

				java.util.Iterator<Class<? extends weka.classifiers.Classifier>> subTypesIterator = subTypes.iterator();
				boolean done = false;
				while(subTypesIterator.hasNext() && !done){
					String className = subTypesIterator.next().toString().substring(6);
					//					System.out.println(className.substring(className.indexOf("weka")));
					try {
						Class c = Class.forName(className.substring(className.indexOf("weka")));
						if(((String)function.obj).equalsIgnoreCase(c.getSimpleName())){
							function.obj = c.getCanonicalName();
							done =true;
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

				}


				try {
					Class<?> c = Class.forName(((String)function.obj));

					Class[] interfaces = c.getInterfaces();
					updatable.value = 0;
					aggregatable.value = 0;
					for(int i=0;i<interfaces.length;i++){
						if(interfaces[i].getSimpleName().contains("UpdateableClassifier")){
							updatable.value = 1;
						} else if(interfaces[i].getSimpleName().contains("Aggregateable")){
							aggregatable.value = 1;
						}
					}

					if(updatable.value == 0 || aggregatable.value == 0){
						for(Class superClazz = c.getSuperclass(); superClazz!=null; superClazz = superClazz.getSuperclass()){
							interfaces = superClazz.getInterfaces();
							for(int j=0;j<interfaces.length;j++){
								if(interfaces[j].getSimpleName().contains("UpdateableClassifier")){
									updatable.value = 1;
								} else if(interfaces[j].getSimpleName().contains("Aggregateable")){
									aggregatable.value = 1;
								}
							}	
						}
					}

				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				writerHolder.obj = new java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>();

				try{

					pathsHolder.obj = new String[] {"1_"+System.currentTimeMillis()+".arff","2_"+System.currentTimeMillis()+".arff"};
					((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).
					add(java.nio.channels.AsynchronousFileChannel.open(java.nio.file.Paths.get(((String[])pathsHolder.obj)[0]), java.nio.file.StandardOpenOption.WRITE, java.nio.file.StandardOpenOption.CREATE));

					((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).
					add(java.nio.channels.AsynchronousFileChannel.open(java.nio.file.Paths.get(((String[])pathsHolder.obj)[1]), java.nio.file.StandardOpenOption.WRITE, java.nio.file.StandardOpenOption.CREATE));


				} catch(Exception ex){
					ex.printStackTrace();
				}
			}

			//Start every run
			try {
				weka.core.Instances instances = new weka.core.Instances(new java.io.StringReader(((String)arffHeader.obj)+rowData));

				instances.setClassIndex(instances.numAttributes() - 1);
				
				if(instances.get(0).classValue() == 0.0){
					class0Count.value++;
				}else {
					class1Count.value++;
				}

				Class<?> c = Class.forName(((String)function.obj));

			
				if(classifier.obj == null) {
					try{

						optionsHolder.obj = options;

						//						System.out.println("In WekaTrainAgg1Updateable create MODEL");
						classifier.obj = (weka.classifiers.Classifier) c.newInstance(); // new weka.classifiers.bayes.NaiveBayesUpdateable();

						java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
						m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj), new Object[] {options});

						m = c.getMethod("buildClassifier", weka.core.Instances.class);
						m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj),instances);

						//						System.out.println("In WekaTrainEnsemble1Updateable build MODEL done");

						if(updatable.value != 1) {
							//TODO: handle first record for NonUpdatable_NonAggregatable, Currently first record is discarded
							if(instancesHolder.obj == null){
								instancesHolder.obj = instances;
							} else {

								((weka.core.Instances)instancesHolder.obj).add(instances.get(0));
							}
							//							System.out.println("In WekaTrainEnsemble1Updateable build MODEL Not updatable add instances");
						}

					}catch(Exception e){
						e.printStackTrace();
					}
				} else {


					if(updatable.value == 1) {
						//					((weka.classifiers.bayes.NaiveBayesUpdateable)classifier.classifier).updateClassifier(instances.instance(0));

						//							System.out.println("In WekaTrainEnsemble1Updateable add MODEL updatable");
						java.lang.reflect.Method m = c.getMethod("updateClassifier", weka.core.Instance.class);
						m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj),instances.instance(0));
						//							System.out.println("In WekaTrainEnsemble1Updateable updatable MODEL updated");
						//							NaiveBayesUpdateable
					} else {
						//							ZeroR



						if(instancesHolder.obj == null){

							instancesHolder.obj = instances;
						} else {

							if(aggregatable.value > 0 ){

								((weka.core.Instances)instancesHolder.obj).add(instances.get(0));

								if(((weka.core.Instances)instancesHolder.obj).size() > 100000){

									if(aggregatable.value == 1){

										// Remove useless attributes
										//									weka.filters.unsupervised.attribute.RemoveUseless m_AttFilter = new weka.filters.unsupervised.attribute.RemoveUseless();
										//								    m_AttFilter.setInputFormat(((weka.core.Instances)instancesHolder.obj));
										//								    weka.core.Instances train = weka.filters.Filter.useFilter(((weka.core.Instances)instancesHolder.obj), m_AttFilter);
										//								    newArffHeader.obj = train.toString();
										//								    
										//								    newArffHeader.obj = ((String)newArffHeader.obj).substring(0, ((String)newArffHeader.obj).indexOf("DATA")+4)+"\n";
										//								    System.out.println("new header:\n"+(String)newArffHeader.obj);
										//								    



										classifier.obj = (weka.classifiers.Classifier) c.newInstance(); 

										java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
										m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj), new Object[] {options});

										m = c.getMethod("buildClassifier", weka.core.Instances.class);
										m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj),((weka.core.Instances)instancesHolder.obj));
										//											System.out.println("Init Aggregation");
										aggregatable.value = 2;

									} else if(aggregatable.value == 2) {

										//									for(int i=0; i<((weka.core.Instances)instancesHolder.obj).numAttributes();i++){
										//										if(((String)newArffHeader.obj).indexOf(((weka.core.Instances)instancesHolder.obj).attribute(i).name()) < 0){
										//											((weka.core.Instances)instancesHolder.obj).deleteAttributeAt(i);
										//										}
										//									}
										//									System.out.println("new isntances for aggregation \n"+((weka.core.Instances)instancesHolder.obj).toString());

										weka.classifiers.Classifier newClassifier = (weka.classifiers.Classifier) c.newInstance(); 

										java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
										m.invoke(Class.forName(((String)function.obj)).cast(newClassifier), new Object[] {options});

										m = c.getMethod("buildClassifier", weka.core.Instances.class);
										m.invoke(Class.forName(((String)function.obj)).cast(newClassifier),((weka.core.Instances)instancesHolder.obj));
										System.out.println("Aggregation instance class index is:"+((weka.core.Instances)instancesHolder.obj).classIndex());

										try{
											try{
												m = c.getMethod("aggregate",c);
												m.invoke(Class.forName((String)function.obj).cast(classifier.obj),Class.forName((String)function.obj).cast(newClassifier));
												//									System.out.println("In WekaTrainAgg2Updateable add MODEL aggregated");
											} catch (java.lang.reflect.InvocationTargetException e){
												System.out.println("Aggregation fail: Doing voting instead of aggregation");

												weka.classifiers.Classifier firstClassifier = (weka.classifiers.Classifier)classifier.obj;

												classifier.obj = new weka.classifiers.meta.Vote();
												((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(firstClassifier);
												aggregatable.value = 3;
											

												((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);

												e.printStackTrace();
											}
										} catch (java.lang.NoSuchMethodException ex){
											try{
												m = c.getMethod("aggregate",c.getSuperclass());
												m.invoke(Class.forName((String)function.obj).cast(classifier.obj),Class.forName((String)function.obj).cast(newClassifier));
												//									System.out.println("In WekaTrainAgg2Updateable add MODEL parent aggregated");
											
											//											System.out.println("Do Aggregation");
											} catch (java.lang.reflect.InvocationTargetException e){
												System.out.println("Aggregation fail: Doing voting instead of aggregation");
	
	
												weka.classifiers.Classifier firstClassifier = (weka.classifiers.Classifier)classifier.obj;

												classifier.obj = new weka.classifiers.meta.Vote();
												((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(firstClassifier);
												aggregatable.value = 3;
												
	
												((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);
	
												e.printStackTrace();
											}
										}
									} else {
										System.out.println("Aggregation failed, doing voting in loop!");
										weka.classifiers.Classifier newClassifier = (weka.classifiers.Classifier) c.newInstance(); 

										java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
										m.invoke(Class.forName(((String)function.obj)).cast(newClassifier), new Object[] {options});

										m = c.getMethod("buildClassifier", weka.core.Instances.class);
										m.invoke(Class.forName(((String)function.obj)).cast(newClassifier),((weka.core.Instances)instancesHolder.obj));
										System.out.println("Aggregation instance class index is:"+((weka.core.Instances)instancesHolder.obj).classIndex());
										
										((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);
									}

									// reinitialize instancesHolder.obj
									((weka.core.Instances)instancesHolder.obj).delete();
									instancesHolder.obj = instances;

								} 
							} else {
								//TODO: need handing for memory allocation
								String[] attributes = rowData.split(",");
								String label = attributes[attributes.length-1];

								if(((java.util.HashMap<String, Integer>)nextPartition.obj).get(label) == null){
									((java.util.HashMap<String, Integer>)nextPartition.obj).put(label,0);
								}

								int tmp = Integer.parseInt(""+((java.util.HashMap<String, Integer>)nextPartition.obj).get(label));
								((java.util.HashMap<String, Integer>)nextPartition.obj).put(label,((tmp+1) % 2));


								java.nio.charset.Charset cs = java.nio.charset.Charset.forName("UTF-8");

								java.nio.ByteBuffer dataBuffer = java.nio.ByteBuffer.wrap((rowData+"\n").getBytes(cs));

								//								long position = ((java.nio.channels.AsynchronousFileChannel)((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).get(tmp)).size();

								
								((java.nio.channels.AsynchronousFileChannel)((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).get(tmp)).write(dataBuffer, ((Long[])writingPosition.obj)[tmp]);
								((Long[])writingPosition.obj)[tmp] = ((Long[])writingPosition.obj)[tmp] + (rowData+"\n").getBytes(cs).length;

								if(fillInstances.value==1){								
									try{

										((weka.core.Instances)instancesHolder.obj).add(instances.get(0));

									} catch(Exception error){
										error.printStackTrace();

										fillInstances.value = 0;

										int MegaBytes = 1024 * 1024;
										System.out.println("In 1:  Instances has: "+((weka.core.Instances)instancesHolder.obj).size()+ " records causing Out of memory error");
										long totalMemory = Runtime.getRuntime().totalMemory() / MegaBytes;
										long maxMemory = Runtime.getRuntime().maxMemory() / MegaBytes;
										long freeMemory = Runtime.getRuntime().freeMemory() / MegaBytes;


										System.out.println("totalMemory in JVM shows current size of java heap:"+totalMemory);
										System.out.println("maxMemory in JVM: " + maxMemory);
										System.out.println("freeMemory in JVM: " + freeMemory);
										System.out.println("Used Memory in JVM: " + (totalMemory - freeMemory));

										((weka.core.Instances)instancesHolder.obj).delete();


									}
								}
							}
						}
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		@Override
		public void output() {
			try {

				if(instancesHolder.obj != null){
					System.out.println((classifier.obj != null)+" - "+ ((String)function.obj)+" - In WekaTrainEnsemble1Updateable output rebuild MODEL using instances:"+((weka.core.Instances)instancesHolder.obj).numInstances());
					Class<?> c = Class.forName(((String)function.obj));

					if(aggregatable.value > 0){
						System.out.println("In ensemble1 agg in out");

						if(aggregatable.value == 1){
							classifier.obj = (weka.classifiers.Classifier) c.newInstance(); 

							java.lang.reflect.Method m = c.getMethod("buildClassifier", weka.core.Instances.class);
							m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj),((weka.core.Instances)instancesHolder.obj));
							System.out.println("Init Aggregation");
							aggregatable.value = 2;

						} else if (aggregatable.value == 2){
							//							for(int i=0; i<((weka.core.Instances)instancesHolder.obj).numAttributes();i++){
							//								if(((String)newArffHeader.obj).indexOf(((weka.core.Instances)instancesHolder.obj).attribute(i).name()) < 0){
							//									((weka.core.Instances)instancesHolder.obj).deleteAttributeAt(i);
							//								}
							//							}
							//							System.out.println("new isntances for aggregation \n"+((weka.core.Instances)instancesHolder.obj).toString());
							//						    

							weka.classifiers.Classifier newClassifier = (weka.classifiers.Classifier) c.newInstance(); 


							java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
							m.invoke(Class.forName(((String)function.obj)).cast(newClassifier), new Object[] {(String[])optionsHolder.obj});


							m = c.getMethod("buildClassifier", weka.core.Instances.class);
							m.invoke(Class.forName(((String)function.obj)).cast(newClassifier),((weka.core.Instances)instancesHolder.obj));
							//										System.out.println("Aggregation instance class index is:"+((weka.core.Instances)instancesHolder.obj).classIndex());

							try{
								try{
									m = c.getMethod("aggregate",c);
									m.invoke(Class.forName((String)function.obj).cast(classifier.obj),Class.forName((String)function.obj).cast(newClassifier));
									//									System.out.println("In WekaTrainAgg2Updateable add MODEL aggregated");
								} catch (java.lang.reflect.InvocationTargetException e){
									System.out.println("Aggregation fail: Doing voting instead of aggregation");
	
									weka.classifiers.Classifier firstClassifier = (weka.classifiers.Classifier)classifier.obj;
									
									classifier.obj = new weka.classifiers.meta.Vote();
									((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(firstClassifier);
									aggregatable.value = 3;
									
	
									((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);
					
									e.printStackTrace();
								}
							} catch (java.lang.NoSuchMethodException ex){
								try{
									m = c.getMethod("aggregate",c.getSuperclass());
									m.invoke(Class.forName((String)function.obj).cast(classifier.obj),Class.forName((String)function.obj).cast(newClassifier));
									//									System.out.println("In WekaTrainAgg2Updateable add MODEL parent aggregated");
								} catch (java.lang.reflect.InvocationTargetException e){
									System.out.println("Aggregation fail: Doing voting instead of aggregation");
	
									weka.classifiers.Classifier firstClassifier = (weka.classifiers.Classifier)classifier.obj;
									
									classifier.obj = new weka.classifiers.meta.Vote();
									((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(firstClassifier);
									aggregatable.value = 3;
									
	
									((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);
					
									e.printStackTrace();
								}
							}
							System.out.println("Do Aggregation");

						} else {
							System.out.println("Aggregation failed, doing voting in out!");
							weka.classifiers.Classifier newClassifier = (weka.classifiers.Classifier) c.newInstance(); 


							java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
							m.invoke(Class.forName(((String)function.obj)).cast(newClassifier), new Object[] {(String[])optionsHolder.obj});


							m = c.getMethod("buildClassifier", weka.core.Instances.class);
							m.invoke(Class.forName(((String)function.obj)).cast(newClassifier),((weka.core.Instances)instancesHolder.obj));
							
							((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(newClassifier);
						}

						// reinitialize instancesHolder.obj
						((weka.core.Instances)instancesHolder.obj).delete();
						instancesHolder.obj = null;

						System.out.println("In ensemble1 agg in out DONE");

					} else {

						//TODO: Handle the memory for this case

						java.io.File f = new java.io.File(((String[])pathsHolder.obj)[0]);

						int MegaBytes = 1024 * 1024;
						long maxMemory = Runtime.getRuntime().maxMemory() / MegaBytes;
						long fileSize = f.length() / MegaBytes;
						long numSubSamples = fileSize*2/maxMemory;

						System.out.println("In function:  data size: "+(2*fileSize)+ " with "+maxMemory+" Max Memory.");

						if(2*fileSize*2/maxMemory < 2){
							try{

								System.out.println("Attempting to train using all data of size:"+ ((weka.core.Instances)instancesHolder.obj).size());
								java.lang.reflect.Method m = c.getMethod("buildClassifier", weka.core.Instances.class);

								m.invoke(Class.forName(((String)function.obj)).cast(classifier.obj),(weka.core.Instances)instancesHolder.obj);
								System.out.println("Attempt SUCCESS to train using all data of size:"+ ((weka.core.Instances)instancesHolder.obj).size());

							} catch(Exception error){
								error.printStackTrace();
							}
						} else if (numSubSamples == 1) {
							//If file can fit in memory train 2 models one on each file
							System.out.println("In 4: Train using all data failed where Instances has: "+((weka.core.Instances)instancesHolder.obj).size()+ " records causing Out of memory error ("+maxMemory+"MB RAM)");
							System.out.println("Now trying to train from the 2 subpartion files");


							//								classifier.obj = this.trainLDAPfile((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj);


							System.out.println("In function:  file size: "+fileSize+ " with "+maxMemory+" Max Memory. Num SubSamples = "+numSubSamples);


							classifier.obj = new weka.classifiers.meta.Vote();

							for(int i=0;i<2;i++){
								System.out.println("Using only the 2 files. Reading file"+((String[])pathsHolder.obj)[i]);

								java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(((String[])pathsHolder.obj)[i]));

								((weka.core.Instances)instancesHolder.obj).delete();

								System.out.println("Before Instance has "+ ((weka.core.Instances)instancesHolder.obj).size()+" instances");

								String record = "";
								int failCount = 0;
								long startTime = System.currentTimeMillis();
								while ((record = br.readLine()) != null) {
									try {
										//										((weka.core.Instances)instancesHolder.obj).addAll(new weka.core.Instances(new java.io.StringReader(((String)arffHeader.obj)+record)));
										((weka.core.Instances)instancesHolder.obj).add(new weka.core.Instances(new java.io.StringReader(((String)arffHeader.obj)+record)).get(0));
									} catch (Exception e) {
										// TODO Auto-generated catch block
										//													e.printStackTrace();
										System.out.println("Failed at this record\n"+record);
										failCount++;
									}

								}
								long endTime = System.currentTimeMillis();

								System.out.println("After Instance has "+ ((weka.core.Instances)instancesHolder.obj).size()+" instances. loaded in "+((endTime-startTime)/1000)+" sec");
								System.out.println("Failed to load: "+failCount+" instances");

								startTime = System.currentTimeMillis();
								try{
									weka.classifiers.Classifier subClassifier = (weka.classifiers.Classifier) c.newInstance(); 

									java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
									m.invoke(Class.forName(((String)function.obj)).cast(subClassifier), new Object[] {(String[])optionsHolder.obj});

									System.out.println("Building subClassifier");
									m = c.getMethod("buildClassifier", weka.core.Instances.class);
									m.invoke(Class.forName(((String)function.obj)).cast(subClassifier),((weka.core.Instances)instancesHolder.obj));


									System.out.println("add subClassifier to voter");
									((weka.classifiers.meta.Vote)classifier.obj).addPreBuiltClassifier(subClassifier);							
									System.out.println("subClassifier added ");

								} catch (Exception ex){
									ex.printStackTrace();
								}

								endTime = System.currentTimeMillis();

								System.out.println("subclassifier trained in "+((endTime-startTime)/1000)+" sec");

								br.close();
								((java.nio.channels.AsynchronousFileChannel)((java.util.ArrayList<java.nio.channels.AsynchronousFileChannel>)writerHolder.obj).get(i)).close();
								java.io.File file = new java.io.File(((String[])pathsHolder.obj)[i]);
								System.out.println("File "+((String[])pathsHolder.obj)[i]+(file.delete()?" Deleted":" Failed to Delete"));


							} 

						} else {
							//else read files, split using ladp to the number of samples and train models

							//combine models using voting

							System.out.println("More partions needed!!!");

						}
					} 

					System.out.println("In WekaTrainEnsemble1Updateable output rebuilding MODEL updated");
				} 

				System.out.println("In Ensemble 1 NO SAMPLE out: We had C0 = "+class0Count.value+" instances and C1 = "+class1Count.value+" instances");

				java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();

				weka.core.SerializationHelper.write(os, classifier.obj);


				byte[] data = os.toByteArray();
				tempBuff = tempBuff.reallocIfNeeded(data.length);
				out.buffer = tempBuff;
				out.buffer.setBytes(0, data);//.setBytes(0,outbuff);
				out.start=0;
				out.end=data.length;
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public void reset() {
		}

	}




	/**
	 * @author shadi
	 * 
	 * AGGREGATOR FOR
	 * Train model xzy as 
	 * select qdm_ensemble_weka(mymodel) 
	 * from `output100M.csv` as mydata;
	 * 
	 */

	@FunctionTemplate(name = "qdm_ensemble_weka", scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE)
	public static class WekaTrainEnsemble2 implements DrillAggFunc{

		@Param  VarCharHolder model;
		@Output VarCharHolder out;
		@Inject DrillBuf tempBuff;
		@Workspace ObjectHolder classifierAgg;
		//		@Workspace LinkedListHolder<weka.core.Instances> instancesList;
		@Workspace ObjectHolder function;
		@Workspace IntHolder aggregatable;
		@Workspace BitHolder firstRun;




		public void setup() {
			classifierAgg = new ObjectHolder();
			function = new ObjectHolder();
			aggregatable = new IntHolder();
			//			instancesList = new LinkedListHolder<weka.core.Instances>();
			//			instancesList.list = new java.util.LinkedList<weka.core.Instances>();
			//			instancesList.algorithm=null;
			//			instancesList.options = null;
			classifierAgg.obj=null;
			function.obj=null;
			aggregatable.value=-1;
			firstRun.value = 0;


		}

		@Override
		public void add() {
			System.out.println("In WekaTrainEnsemble2");
			byte[] classifierBuf = new byte[model.end - model.start];
			model.buffer.getBytes(model.start, classifierBuf, 0, model.end - model.start);


			String input = new String(classifierBuf, com.google.common.base.Charsets.UTF_8);
			//			System.out.println("In WekaTrainAgg2Updateable add (input): "+input);
			//			System.out.println("In WekaTrainAgg2Updateable add (input legnth): "+input.length()+" - input.contains('|Info|'): "+input.indexOf("|Info|"));

			//			if(input.length()>100 && !input.contains("|Info|") && input.indexOf("weka.classifiers")>-1){
			//			System.out.println("In WekaTrainEnsemble2Updateable add In Model agg");

			if(firstRun.value == 0){
				firstRun.value = 1;
				int i = input.indexOf("weka.classifiers")+18;
				String className = input.substring(input.indexOf("weka.classifiers"),i);
				while("abcdefghijklmnopqrstuvwxyz1234567890.".contains(input.substring(i,i+1).toLowerCase())){
					className = input.substring(input.indexOf("weka.classifiers"),i++);
					//					System.out.println("className: "+className);
				}
				System.out.println("In WekaTrainEnsemble2 "+ className);
				className = input.substring(input.indexOf("weka.classifiers"),i+5);

				while(className.length()>0){
					System.out.println("In WekaTrainEnsemble2 "+ className);
					try{
						Class<?> c = Class.forName(className);
						break;
					} catch(Exception ex){
						className = className.substring(0,className.length()-1);
					}
				}
				function.obj = className;
			}
			//				System.out.println("In WekaTrainAgg2Updateable class name = "+function.value);

			//				System.out.println("In WekaTrainAgg2Updateable add MODEL");
			try{
				java.io.InputStream cis = new java.io.ByteArrayInputStream(classifierBuf);
				try {
					//						Class.forName(function.value).cast(classifierAgg.classifier);

					Class<?> c = Class.forName((String)function.obj);

					if(aggregatable.value<0){
						Class[] interfaces = c.getInterfaces();
						String interfacesImplemented = "";
						for(int j=0;j<interfaces.length;j++){
							interfacesImplemented+=interfaces[j].getSimpleName()+" - ";
						}	
						for(Class superClazz = c.getSuperclass(); superClazz!=null; superClazz = superClazz.getSuperclass()){
							interfaces = superClazz.getInterfaces();
							for(int j=0;j<interfaces.length;j++){
								interfacesImplemented+=interfaces[j].getSimpleName()+" - ";
							}	
						}

						if(interfacesImplemented.contains("Aggregateable")){
							aggregatable.value=1;
						} else {
							aggregatable.value=0;
						}
					}

					//						classifier = (weka.classifiers.bayes.NaiveBayesUpdateable) weka.core.SerializationHelper.read(cis);						
					weka.classifiers.Classifier classifier = (weka.classifiers.Classifier) weka.core.SerializationHelper.read(cis);

					//						System.out.println("In WekaTrainAgg2Updateable add MODEL read");

					if(classifierAgg.obj==null){
						System.out.println("In WekaTrainEnsembleAgg2Updateable add MODEL new ");
						if(aggregatable.value==1){
							System.out.println("In WekaTrainEnsembleAgg2Updateable add MODEL new agg");
							classifierAgg.obj = classifier;
						} else if(aggregatable.value==0){
							System.out.println("In WekaTrainEnsembleAgg2Updateable add MODEL new vote ");
							classifierAgg.obj = new weka.classifiers.meta.Vote();
							((weka.classifiers.meta.Vote)classifierAgg.obj).addPreBuiltClassifier(classifier);
						}
						//							System.out.println("In WekaTrainAgg2Updateable add MODEL new  set");
					} else {
						System.out.println("In WekaTrainEnsembleAgg2Updateable add MODEL update");
						// aggregate classifiers
						//							((weka.classifiers.bayes.NaiveBayesUpdateable) classifierAgg.classifier).aggregate((weka.classifiers.bayes.NaiveBayesUpdateable)classifier);


						if(aggregatable.value == 1){
							System.out.println("In WekaTrainEnsembleAgg2Updateable add MODEL aggregatable");
							try{
								try{
									java.lang.reflect.Method m = c.getMethod("aggregate",c);
									m.invoke(Class.forName((String)function.obj).cast(classifierAgg.obj),Class.forName((String)function.obj).cast(classifier));
									//									System.out.println("In WekaTrainAgg2Updateable add MODEL aggregated");
								} catch (java.lang.reflect.InvocationTargetException e){
									System.out.println("Aggregation fail: Doing voting instead of aggregation");
	
									weka.classifiers.Classifier firstClassifier = (weka.classifiers.Classifier)classifierAgg.obj;
									
									classifierAgg.obj = new weka.classifiers.meta.Vote();
									((weka.classifiers.meta.Vote)classifierAgg.obj).addPreBuiltClassifier(firstClassifier);
									aggregatable.value = 0;
									
	
									((weka.classifiers.meta.Vote)classifierAgg.obj).addPreBuiltClassifier(classifier);
					
									e.printStackTrace();
								}
							} catch (java.lang.NoSuchMethodException ex){
								try{
									java.lang.reflect.Method m = c.getMethod("aggregate",c.getSuperclass());
									m.invoke(Class.forName((String)function.obj).cast(classifierAgg.obj),Class.forName((String)function.obj).cast(classifier));
									//									System.out.println("In WekaTrainAgg2Updateable add MODEL parent aggregated");
								} catch (java.lang.reflect.InvocationTargetException e){
									System.out.println("Aggregation fail: Doing voting instead of aggregation");
	
									weka.classifiers.Classifier firstClassifier = (weka.classifiers.Classifier)classifierAgg.obj;
									
									classifierAgg.obj = new weka.classifiers.meta.Vote();
									((weka.classifiers.meta.Vote)classifierAgg.obj).addPreBuiltClassifier(firstClassifier);
									aggregatable.value = 0;
									
	
									((weka.classifiers.meta.Vote)classifierAgg.obj).addPreBuiltClassifier(classifier);
					
									e.printStackTrace();
								}
							}
						} else if (aggregatable.value==0){
							System.out.println("In WekaTrainEnsembleAgg2Updateable add MODEL NOT aggregatable");
							((weka.classifiers.meta.Vote)classifierAgg.obj).addPreBuiltClassifier(classifier);
						}
					}



				} catch (Exception e) {
					e.printStackTrace();
				}
			}catch(Exception e){
				e.printStackTrace();
			}



		}

		@Override
		public void output() {
			try {

				if(aggregatable.value==1){
					System.out.println("In WekaTrainEnsembleAgg2Updateable Finalizing aggregation");
					Class<?> c = Class.forName((String)function.obj);

					try{

						java.lang.reflect.Method m = c.getMethod("finalizeAggregation",c);
						m.invoke(Class.forName((String)function.obj).cast(classifierAgg.obj));

					} catch (java.lang.NoSuchMethodException ex){
						try{
							java.lang.reflect.Method m = c.getMethod("finalizeAggregation",c.getSuperclass());
							m.invoke(Class.forName((String)function.obj).cast(classifierAgg.obj));
							//									System.out.println("In WekaTrainAgg2Updateable add MODEL parent aggregated");
						} catch (java.lang.NoSuchMethodException e){
							
						}
					}
				}
				System.out.println("In WekaTrainEnsembleAgg2Updateable out writing agg model");
				java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
				weka.core.SerializationHelper.write(os, classifierAgg.obj);
				tempBuff = tempBuff.reallocIfNeeded(os.toByteArray().length);
				out.buffer = tempBuff;
				out.buffer.setBytes(0, os.toByteArray());//.setBytes(0,outbuff);
				out.start=0;
				out.end=os.toByteArray().length;
				//				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		@Override
		public void reset() {
		}

	}






	/**
	 * @author shadi
	 * 
	 * Train model xzy as 
	 * select qdm_train_weka('nb','-classes {1,2}', mydata.columns[1], mydata.columns[2], mydata.columns[3], mydata.columns[4], mydata.columns[5]) 
	 * from `output100M.csv` as mydata;
	 *
	 */


	@FunctionTemplate(name = "qdm_train_weka", scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE)
	public static class WekaTrainAgg1UpdateableColumns implements DrillAggFunc{
		@Param  VarCharHolder operation;
		@Param  VarCharHolder arguments;
		@Param  VarCharHolder features;
		@Output VarCharHolder out;
		@Inject DrillBuf tempBuff;
		@Workspace WekaUpdatableClassifierHolder classifier;
		@Workspace StringHolder function;
		@Workspace StringHolder arffHeader;
		//		@Workspace LinkedListHolder<weka.core.Instances> instancesList; 
		@Workspace ObjectHolder instancesHolder;
		@Workspace  BitHolder firstRun;
		@Workspace VarCharHolder currVal;
		@Workspace IntHolder updatable;
		@Workspace IntHolder aggregatable;

		public void setup() {
			classifier = new WekaUpdatableClassifierHolder();
			function = new StringHolder();
			arffHeader = new StringHolder();
			firstRun = new BitHolder();
			instancesHolder = new ObjectHolder();
			instancesHolder.obj = null;
			//			instancesList = new LinkedListHolder<weka.core.Instances>();
			//			instancesList.list = new java.util.LinkedList<weka.core.Instances>();
			//			instancesList.algorithm=null;
			//			instancesList.options = null;
			classifier.classifier=null;
			function.value=null;
			arffHeader.value=null;
			firstRun.value=0;
			currVal = new VarCharHolder();
			updatable = new IntHolder();
			updatable.value=-1;
			aggregatable = new IntHolder();
			aggregatable.value=-1;
		}

		@Override
		public void add() {

			byte[] temp = new byte[features.end - features.start];
			features.buffer.getBytes(features.start, temp, 0, features.end - features.start);
			String rowData = new String(temp, com.google.common.base.Charsets.UTF_8);

			String [] options = null;
			if(firstRun.value==0){
				firstRun.value = 1;
				byte[] operationBuf = new byte[operation.end - operation.start];
				operation.buffer.getBytes(operation.start, operationBuf, 0, operation.end - operation.start);
				function.value = new String(operationBuf, com.google.common.base.Charsets.UTF_8).toLowerCase();
				java.util.StringTokenizer st = new java.util.StringTokenizer(rowData, ",");
				int attributesCount = st.countTokens();
				java.lang.StringBuilder stBuilder = new java.lang.StringBuilder();

				byte[] argsBuf = new byte[arguments.end - arguments.start];
				arguments.buffer.getBytes(arguments.start, argsBuf, 0, arguments.end - arguments.start);
				String classType = "numeric";
				try {
					options = weka.core.Utils.splitOptions((new String(argsBuf, com.google.common.base.Charsets.UTF_8)));
					for(int i=0;i<options.length;i++){
						if(options[i].indexOf("classes")>0){
							classType = options[i+1];
							options[i]="";
							options[i+1]="";
						}
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				//				stBuilder.append(function.value+"||"+options+"\n");
				stBuilder.append("@"+"RELATION Drill\n");
				for(int i=0; i< attributesCount-1;i++)
				{
					stBuilder.append("@"+"ATTRIBUTE att"+i+" numeric\n");
				}
				stBuilder.append("@"+"ATTRIBUTE class "+classType+"\n");
				stBuilder.append("@"+"DATA\n");
				arffHeader.value = stBuilder.toString();

				org.reflections.Reflections reflections = new org.reflections.Reflections("weka.classifiers"); 
				java.util.Set<Class<? extends weka.classifiers.Classifier>> subTypes = 
						reflections.getSubTypesOf(weka.classifiers.Classifier.class);

				java.util.Iterator<Class<? extends weka.classifiers.Classifier>> subTypesIterator = subTypes.iterator();
				boolean done = false;
				while(subTypesIterator.hasNext() && !done){
					String className = subTypesIterator.next().toString().substring(6);
					//					System.out.println(className.substring(className.indexOf("weka")));
					try {
						Class c = Class.forName(className.substring(className.indexOf("weka")));
						if(function.value.equalsIgnoreCase(c.getSimpleName())){
							function.value = c.getCanonicalName();
							done =true;
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

				}


				try {
					Class<?> c = Class.forName(function.value);

					Class[] interfaces = c.getInterfaces();
					updatable.value = 0;
					aggregatable.value = 0;
					for(int i=0;i<interfaces.length;i++){
						if(interfaces[i].getSimpleName().contains("UpdateableClassifier")){
							updatable.value = 1;
						} else if(interfaces[i].getSimpleName().contains("Aggregateable")){
							aggregatable.value = 1;
						}
					}

					if(updatable.value == 0 || aggregatable.value == 0){
						for(Class superClazz = c.getSuperclass(); superClazz!=null; superClazz = superClazz.getSuperclass()){
							interfaces = superClazz.getInterfaces();
							for(int j=0;j<interfaces.length;j++){
								if(interfaces[j].getSimpleName().contains("UpdateableClassifier")){
									updatable.value = 1;
								} else if(interfaces[j].getSimpleName().contains("Aggregateable")){
									aggregatable.value = 1;
								}
							}	
						}
					}

				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			}

			//Start every run
			try {
				weka.core.Instances instances = new weka.core.Instances(new java.io.StringReader(arffHeader.value+rowData));

				instances.setClassIndex(instances.numAttributes() - 1);

				Class<?> c = Class.forName(function.value);

				//				if(updatable.value == 1 && aggregatable.value == 1){
				if(classifier.classifier == null) {
					try{

						//						System.out.println("In WekaTrainAgg1Updateable create MODEL");
						classifier.classifier = (weka.classifiers.Classifier) c.newInstance(); // new weka.classifiers.bayes.NaiveBayesUpdateable();
						//						classifier.classifier = new weka.classifiers.bayes.NaiveBayesUpdateable();
						//						System.out.println("In WekaTrainAgg1Updateable options MODEL");
						//						((weka.classifiers.bayes.NaiveBayesUpdateable)classifier.classifier).setOptions(options);
						java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
						m.invoke(Class.forName(function.value).cast(classifier.classifier), new Object[] {options});
						//						System.out.println("In WekaTrainAgg1Updateable options MODEL done");

						//						classifier.classifier.buildClassifier(instances);
						//						System.out.println("In WekaTrainAgg1Updateable build MODEL");
						//						((weka.classifiers.bayes.NaiveBayesUpdateable)classifier.classifier).setOptions(options);
						m = c.getMethod("buildClassifier", weka.core.Instances.class);
						m.invoke(Class.forName(function.value).cast(classifier.classifier),instances);
						//						System.out.println("In WekaTrainAgg1Updateable build MODEL done");

						if(updatable.value != 1) {
							if(instancesHolder.obj == null){
								instancesHolder.obj = instances;
							} else {
								((weka.core.Instances)instancesHolder.obj).add(instances.get(0));
							}
						}

					}catch(Exception e){
						e.printStackTrace();
					}
				} else {
					try{

						if(updatable.value == 1) {
							//					((weka.classifiers.bayes.NaiveBayesUpdateable)classifier.classifier).updateClassifier(instances.instance(0));

							//							System.out.println("In WekaTrainAgg1Updateable add MODEL updatable");
							java.lang.reflect.Method m = c.getMethod("updateClassifier", weka.core.Instance.class);
							m.invoke(Class.forName(function.value).cast(classifier.classifier),instances.instance(0));
							//							System.out.println("In WekaTrainAgg1Updateable updatable MODEL updated");
							//							NaiveBayesUpdateable
						} else {
							//							ZeroR

							if(instancesHolder.obj == null){
								instancesHolder.obj = instances;
							} else {
								((weka.core.Instances)instancesHolder.obj).add(instances.get(0));
							}

							//							System.out.println("In WekaTrainAgg1Updateable rebuild MODEL using instances:"+((weka.core.Instances)instancesHolder.obj).numInstances());
							//							java.lang.reflect.Method m = c.getMethod("buildClassifier", weka.core.Instances.class);
							//							m.invoke(Class.forName(function.value).cast(classifier.classifier),((weka.core.Instances)instancesHolder.obj));
							//							System.out.println("In WekaTrainAgg1Updateable rebuilding MODEL updated");
						}

					}catch(Exception ex){
						ex.printStackTrace();

					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		@Override
		public void output() {
			try {

				if(instancesHolder.obj != null){
					System.out.println((classifier.classifier != null)+" - "+ function.value+" - In WekaTrainAgg1Updateable output rebuild MODEL using instances:"+((weka.core.Instances)instancesHolder.obj).numInstances());
					Class<?> c = Class.forName(function.value);
					java.lang.reflect.Method m = c.getMethod("buildClassifier", weka.core.Instances.class);
					m.invoke(Class.forName(function.value).cast(classifier.classifier),((weka.core.Instances)instancesHolder.obj));
					System.out.println("In WekaTrainAgg1Updateable output rebuilding MODEL updated");

				} 


				java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();

				weka.core.SerializationHelper.write(os, classifier.classifier);

				//				if(classifier.classifier!=null && instancesList.list.size() == 0){
				//					weka.core.SerializationHelper.write(os, classifier.classifier);
				//				} else {
				//					//TODO: Handle small datasets. Train model here. 
				//					
				//					weka.core.SerializationHelper.write(os, instancesList);
				//				}
				byte[] data = os.toByteArray();
				out.buffer = tempBuff;
				out.buffer = out.buffer.reallocIfNeeded(data.length);
				out.buffer.setBytes(0, data);//.setBytes(0,outbuff);
				out.start=0;
				out.end=data.length;
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public void reset() {
		}
	}


	/**
	 * @author shadi
	 * 
	 * Train model xzy as 
	 * select qdm_train_weka('nb','-classes {1,2}', mydata.columns) 
	 * from `output100M.csv` as mydata;
	 *
	 */

	@FunctionTemplate(name = "qdm_train_weka", scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE)
	public static class WekaTrainAgg1Updateable implements DrillAggFunc{

		@Param  VarCharHolder operation;
		@Param  VarCharHolder arguments;
		@Param  RepeatedVarCharHolder features;
		@Output VarCharHolder out;
		@Inject DrillBuf tempBuff;
		@Workspace WekaUpdatableClassifierHolder classifier;
		@Workspace StringHolder function;
		@Workspace StringHolder arffHeader;
		//		@Workspace LinkedListHolder<weka.core.Instances> instancesList; 
		@Workspace ObjectHolder instancesHolder;
		@Workspace  BitHolder firstRun;
		@Workspace VarCharHolder currVal;
		@Workspace IntHolder updatable;
		@Workspace IntHolder aggregatable;

		public void setup() {
			classifier = new WekaUpdatableClassifierHolder();
			function = new StringHolder();
			arffHeader = new StringHolder();
			firstRun = new BitHolder();
			instancesHolder = new ObjectHolder();
			instancesHolder.obj = null;
			//			instancesList = new LinkedListHolder<weka.core.Instances>();
			//			instancesList.list = new java.util.LinkedList<weka.core.Instances>();
			//			instancesList.algorithm=null;
			//			instancesList.options = null;
			classifier.classifier=null;
			function.value=null;
			arffHeader.value=null;
			firstRun.value=0;
			currVal = new VarCharHolder();
			updatable = new IntHolder();
			updatable.value=-1;
			aggregatable = new IntHolder();
			aggregatable.value=-1;
		}

		@Override
		public void add() {
			java.lang.StringBuilder rowBuilder = new java.lang.StringBuilder();
			for (int i = features.start; i < features.end; i++) {
				features.vector.getAccessor().get(i, currVal);
				rowBuilder.append(org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(currVal.start, currVal.end, currVal.buffer)+",");
			}
			String rowData = rowBuilder.substring(0, rowBuilder.length()-1);
			String [] options = null;
			if(firstRun.value==0){
				firstRun.value = 1;
				byte[] operationBuf = new byte[operation.end - operation.start];
				operation.buffer.getBytes(operation.start, operationBuf, 0, operation.end - operation.start);
				function.value = new String(operationBuf, com.google.common.base.Charsets.UTF_8).toLowerCase();
				java.util.StringTokenizer st = new java.util.StringTokenizer(rowData, ",");
				int attributesCount = st.countTokens();
				java.lang.StringBuilder stBuilder = new java.lang.StringBuilder();

				byte[] argsBuf = new byte[arguments.end - arguments.start];
				arguments.buffer.getBytes(arguments.start, argsBuf, 0, arguments.end - arguments.start);
				String classType = "numeric";
				try {
					options = weka.core.Utils.splitOptions((new String(argsBuf, com.google.common.base.Charsets.UTF_8)));
					for(int i=0;i<options.length;i++){
						if(options[i].indexOf("classes")>0){
							classType = options[i+1];
							options[i]="";
							options[i+1]="";
						}
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				//				stBuilder.append(function.value+"||"+options+"\n");
				stBuilder.append("@"+"RELATION Drill\n");
				for(int i=0; i< attributesCount-1;i++)
				{
					stBuilder.append("@"+"ATTRIBUTE att"+i+" numeric\n");
				}
				stBuilder.append("@"+"ATTRIBUTE class "+classType+"\n");
				stBuilder.append("@"+"DATA\n");
				arffHeader.value = stBuilder.toString();

				org.reflections.Reflections reflections = new org.reflections.Reflections("weka.classifiers"); 
				java.util.Set<Class<? extends weka.classifiers.Classifier>> subTypes = 
						reflections.getSubTypesOf(weka.classifiers.Classifier.class);

				java.util.Iterator<Class<? extends weka.classifiers.Classifier>> subTypesIterator = subTypes.iterator();
				boolean done = false;
				while(subTypesIterator.hasNext() && !done){
					String className = subTypesIterator.next().toString().substring(6);
					//					System.out.println(className.substring(className.indexOf("weka")));
					try {
						Class c = Class.forName(className.substring(className.indexOf("weka")));
						if(function.value.equalsIgnoreCase(c.getSimpleName())){
							function.value = c.getCanonicalName();
							done =true;
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

				}


				try {
					Class<?> c = Class.forName(function.value);

					Class[] interfaces = c.getInterfaces();
					updatable.value = 0;
					aggregatable.value = 0;
					for(int i=0;i<interfaces.length;i++){
						if(interfaces[i].getSimpleName().contains("UpdateableClassifier")){
							updatable.value = 1;
						} else if(interfaces[i].getSimpleName().contains("Aggregateable")){
							aggregatable.value = 1;
						}
					}

					if(updatable.value == 0 || aggregatable.value == 0){
						for(Class superClazz = c.getSuperclass(); superClazz!=null; superClazz = superClazz.getSuperclass()){
							interfaces = superClazz.getInterfaces();
							for(int j=0;j<interfaces.length;j++){
								if(interfaces[j].getSimpleName().contains("UpdateableClassifier")){
									updatable.value = 1;
								} else if(interfaces[j].getSimpleName().contains("Aggregateable")){
									aggregatable.value = 1;
								}
							}	
						}
					}

				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			}

			//Start every run
			try {
				weka.core.Instances instances = new weka.core.Instances(new java.io.StringReader(arffHeader.value+rowData));

				instances.setClassIndex(instances.numAttributes() - 1);

				Class<?> c = Class.forName(function.value);

				//				if(updatable.value == 1 && aggregatable.value == 1){
				if(classifier.classifier == null) {
					try{

						//						System.out.println("In WekaTrainAgg1Updateable create MODEL");
						classifier.classifier = (weka.classifiers.Classifier) c.newInstance(); // new weka.classifiers.bayes.NaiveBayesUpdateable();
						//						classifier.classifier = new weka.classifiers.bayes.NaiveBayesUpdateable();
						//						System.out.println("In WekaTrainAgg1Updateable options MODEL");
						//						((weka.classifiers.bayes.NaiveBayesUpdateable)classifier.classifier).setOptions(options);
						java.lang.reflect.Method m = c.getMethod("setOptions", String[].class);
						m.invoke(Class.forName(function.value).cast(classifier.classifier), new Object[] {options});
						//						System.out.println("In WekaTrainAgg1Updateable options MODEL done");

						//						classifier.classifier.buildClassifier(instances);
						//						System.out.println("In WekaTrainAgg1Updateable build MODEL");
						//						((weka.classifiers.bayes.NaiveBayesUpdateable)classifier.classifier).setOptions(options);
						m = c.getMethod("buildClassifier", weka.core.Instances.class);
						m.invoke(Class.forName(function.value).cast(classifier.classifier),instances);
						//						System.out.println("In WekaTrainAgg1Updateable build MODEL done");

						if(updatable.value != 1) {
							if(instancesHolder.obj == null){
								instancesHolder.obj = instances;
							} else {
								((weka.core.Instances)instancesHolder.obj).add(instances.get(0));
							}
						}

					}catch(Exception e){
						e.printStackTrace();
					}
				} else {
					try{

						if(updatable.value == 1) {
							//					((weka.classifiers.bayes.NaiveBayesUpdateable)classifier.classifier).updateClassifier(instances.instance(0));

							//							System.out.println("In WekaTrainAgg1Updateable add MODEL updatable");
							java.lang.reflect.Method m = c.getMethod("updateClassifier", weka.core.Instance.class);
							m.invoke(Class.forName(function.value).cast(classifier.classifier),instances.instance(0));
							//							System.out.println("In WekaTrainAgg1Updateable updatable MODEL updated");
							//							NaiveBayesUpdateable
						} else {
							//							ZeroR

							if(instancesHolder.obj == null){
								instancesHolder.obj = instances;
							} else {
								((weka.core.Instances)instancesHolder.obj).add(instances.get(0));
							}

							//							System.out.println("In WekaTrainAgg1Updateable rebuild MODEL using instances:"+((weka.core.Instances)instancesHolder.obj).numInstances());
							//							java.lang.reflect.Method m = c.getMethod("buildClassifier", weka.core.Instances.class);
							//							m.invoke(Class.forName(function.value).cast(classifier.classifier),((weka.core.Instances)instancesHolder.obj));
							//							System.out.println("In WekaTrainAgg1Updateable rebuilding MODEL updated");
						}

					}catch(Exception ex){
						ex.printStackTrace();

					}
				}
 

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		@Override
		public void output() {
			try {

				if(instancesHolder.obj != null){
					System.out.println((classifier.classifier != null)+" - "+ function.value+" - In WekaTrainAgg1Updateable output rebuild MODEL using instances:"+((weka.core.Instances)instancesHolder.obj).numInstances());
					Class<?> c = Class.forName(function.value);
					java.lang.reflect.Method m = c.getMethod("buildClassifier", weka.core.Instances.class);
					m.invoke(Class.forName(function.value).cast(classifier.classifier),((weka.core.Instances)instancesHolder.obj));
					System.out.println("In WekaTrainAgg1Updateable output rebuilding MODEL updated");

				} 


				java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();

				weka.core.SerializationHelper.write(os, classifier.classifier);

				//				if(classifier.classifier!=null && instancesList.list.size() == 0){
				//					weka.core.SerializationHelper.write(os, classifier.classifier);
				//				} else {
				//					//TODO: Handle small datasets. Train model here. 
				//					
				//					weka.core.SerializationHelper.write(os, instancesList);
				//				}
				byte[] data = os.toByteArray();
				out.buffer = tempBuff;
				out.buffer = out.buffer.reallocIfNeeded(data.length);
				out.buffer.setBytes(0, data);//.setBytes(0,outbuff);
				out.start=0;
				out.end=data.length;
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		@Override
		public void reset() {
		}
	}


	/**
	 * @author shadi
	 * 
	 * AGGREGATOR FOR
	 * Train model xzy as 
	 * select qdm_train_weka('nb','-classes {1,2}', columns) 
	 * from `output100M.csv` as mydata;
	 * 
	 */

	@FunctionTemplate(name = "qdm_train_weka", scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE)
	public static class WekaTrainAgg2Updateable implements DrillAggFunc{

		@Param  VarCharHolder model;
		@Output VarCharHolder out;
		@Inject DrillBuf tempBuff;
		@Workspace WekaUpdatableClassifierHolder classifierAgg;
		//		@Workspace LinkedListHolder<weka.core.Instances> instancesList;
		@Workspace StringHolder function;
		@Workspace IntHolder aggregatable;
		@Workspace BitHolder firstRun;




		public void setup() {
			classifierAgg = new WekaUpdatableClassifierHolder();
			function = new StringHolder();
			aggregatable = new IntHolder();
			//			instancesList = new LinkedListHolder<weka.core.Instances>();
			//			instancesList.list = new java.util.LinkedList<weka.core.Instances>();
			//			instancesList.algorithm=null;
			//			instancesList.options = null;
			classifierAgg.classifier=null;
			function.value=null;
			aggregatable.value=-1;
			firstRun.value = 0;


		}

		@Override
		public void add() {
			//			System.out.println("In WekaTrainAgg2Updateable add");
			byte[] classifierBuf = new byte[model.end - model.start];
			model.buffer.getBytes(model.start, classifierBuf, 0, model.end - model.start);


			String input = new String(classifierBuf, com.google.common.base.Charsets.UTF_8);
			//			System.out.println("In WekaTrainAgg2Updateable add (input): "+input);
			//			System.out.println("In WekaTrainAgg2Updateable add (input legnth): "+input.length()+" - input.contains('|Info|'): "+input.indexOf("|Info|"));

			//			if(input.length()>100 && !input.contains("|Info|") && input.indexOf("weka.classifiers")>-1){
			System.out.println("In WekaTrainAgg2Updateable add In Model agg");

			if(firstRun.value == 0){
				firstRun.value = 1;
				int i = input.indexOf("weka.classifiers")+18;
				String className = input.substring(input.indexOf("weka.classifiers"),i);
				while("abcdefghijklmnopqrstuvwxyz1234567890.".contains(input.substring(i,i+1).toLowerCase())){
					className = input.substring(input.indexOf("weka.classifiers"),i++);
					//					System.out.println("className: "+className);
				}
				className = input.substring(input.indexOf("weka.classifiers"),i+5);
				while(className.length()>0){
					System.out.println("In WekaTrainAgg2Updateable "+ className);
					try{
						Class<?> c = Class.forName(className);
						break;
					} catch(Exception ex){
						className = className.substring(0,className.length()-1);
					}
				}

				function.value = className;
			}
			//				System.out.println("In WekaTrainAgg2Updateable class name = "+function.value);

			//				System.out.println("In WekaTrainAgg2Updateable add MODEL");
			try{
				java.io.InputStream cis = new java.io.ByteArrayInputStream(classifierBuf);
				try {
					//						Class.forName(function.value).cast(classifierAgg.classifier);

					Class<?> c = Class.forName(function.value);

					if(aggregatable.value<0){
						Class[] interfaces = c.getInterfaces();
						String interfacesImplemented = "";
						for(int j=0;j<interfaces.length;j++){
							interfacesImplemented+=interfaces[j].getSimpleName()+" - ";
						}	
						for(Class superClazz = c.getSuperclass(); superClazz!=null; superClazz = superClazz.getSuperclass()){
							interfaces = superClazz.getInterfaces();
							for(int j=0;j<interfaces.length;j++){
								interfacesImplemented+=interfaces[j].getSimpleName()+" - ";
							}	
						}

						if(interfacesImplemented.contains("Aggregateable")){
							aggregatable.value=1;
						} else {
							aggregatable.value=0;
						}
					}

					//						classifier = (weka.classifiers.bayes.NaiveBayesUpdateable) weka.core.SerializationHelper.read(cis);						
					weka.classifiers.Classifier classifier = (weka.classifiers.Classifier) weka.core.SerializationHelper.read(cis);

					//						System.out.println("In WekaTrainAgg2Updateable add MODEL read");

					if(classifierAgg.classifier==null){
						System.out.println("In WekaTrainAgg2Updateable add MODEL new ");
						if(aggregatable.value==1){
							System.out.println("In WekaTrainAgg2Updateable add MODEL new agg");
							classifierAgg.classifier = classifier;
						} else if(aggregatable.value==0){
							System.out.println("In WekaTrainAgg2Updateable add MODEL new vote ");
							classifierAgg.classifier = new weka.classifiers.meta.Vote();
							((weka.classifiers.meta.Vote)classifierAgg.classifier).addPreBuiltClassifier(classifier);
						}
						//							System.out.println("In WekaTrainAgg2Updateable add MODEL new  set");
					} else {
						System.out.println("In WekaTrainAgg2Updateable add MODEL update");
						// aggregate classifiers
						//							((weka.classifiers.bayes.NaiveBayesUpdateable) classifierAgg.classifier).aggregate((weka.classifiers.bayes.NaiveBayesUpdateable)classifier);


						if(aggregatable.value==1){
							System.out.println("In WekaTrainAgg2Updateable add MODEL aggregatable");
							try{
								java.lang.reflect.Method m = c.getMethod("aggregate",c);
								m.invoke(Class.forName(function.value).cast(classifierAgg.classifier),Class.forName(function.value).cast(classifier));
								//									System.out.println("In WekaTrainAgg2Updateable add MODEL aggregated");
							} catch (java.lang.NoSuchMethodException ex){
								java.lang.reflect.Method m = c.getMethod("aggregate",c.getSuperclass());
								m.invoke(Class.forName(function.value).cast(classifierAgg.classifier),Class.forName(function.value).cast(classifier));
								//									System.out.println("In WekaTrainAgg2Updateable add MODEL parent aggregated");
							}
						} else if (aggregatable.value==0){
							System.out.println("In WekaTrainAgg2Updateable add MODEL NOT aggregatable");
							((weka.classifiers.meta.Vote)classifierAgg.classifier).addPreBuiltClassifier(classifier);
						}
					}



				} catch (Exception e) {
					e.printStackTrace();
				}
			}catch(Exception e){
				e.printStackTrace();
			}



		}

		@Override
		public void output() {
			try {
				System.out.println("In WekaTrainAgg2Updateable out writing agg model");
				java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
				weka.core.SerializationHelper.write(os, classifierAgg.classifier);
				out.buffer = tempBuff;
				out.buffer = out.buffer.reallocIfNeeded(os.toByteArray().length);
				out.buffer.setBytes(0, os.toByteArray());//.setBytes(0,outbuff);
				out.start=0;
				out.end=os.toByteArray().length;
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		@Override
		public void reset() {
		}

	}




	/**
	 * @author shadi
	 * 
	 * Train model xzy as 
	 * select qdm_update_weka('nb','-classes {1,2}', mymodel.columns[0], mydata.columns[1], mydata.columns[2], mydata.columns[3], mydata.columns[4], mydata.columns[5]) 
	 * from `output100M.csv` as mydata applying nb100M_3 as mymodel;
	 *
	 */
	@FunctionTemplate(name = "qdm_update_weka", scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE)
	public static class WekaUpdateTrain implements DrillAggFunc{

		@Param  VarCharHolder operation;
		@Param  VarCharHolder arguments;
		@Param 	NullableVarCharHolder classifierTxt;
		@Param  VarCharHolder features;
		@Output VarCharHolder out;
		@Inject DrillBuf tempBuff;
		@Workspace WekaUpdatableClassifierHolder classifier;
		@Workspace StringHolder function;
		@Workspace StringHolder arffHeader;
		@Workspace  BitHolder firstRun;

		public void setup() {
			classifier = new WekaUpdatableClassifierHolder();
			function = new StringHolder();
			arffHeader = new StringHolder();
			firstRun = new BitHolder();
			classifier.classifier=null;
			function.value=null;
			arffHeader.value=null;
			firstRun.value=0;
		}

		@Override
		public void add() {
			byte[] temp = new byte[features.end - features.start];
			features.buffer.getBytes(features.start, temp, 0, features.end - features.start);
			String rowData = new String(temp, com.google.common.base.Charsets.UTF_8);
			String [] options = null;
			if(firstRun.value==0){
				firstRun.value = 1;
				byte[] operationBuf = new byte[operation.end - operation.start];
				operation.buffer.getBytes(operation.start, operationBuf, 0, operation.end - operation.start);
				function.value = new String(operationBuf, com.google.common.base.Charsets.UTF_8).toLowerCase();

				try{
					
					byte[] classifierBuf = new byte[classifierTxt.end - classifierTxt.start];
					classifierTxt.buffer.getBytes(classifierTxt.start, classifierBuf, 0, classifierTxt.end - classifierTxt.start);


					//////////////////////////////////////////////////////////////////////////////////////////
					
					String inputPath = new String(classifierBuf, com.google.common.base.Charsets.UTF_8);
									
					System.out.println(inputPath);


					org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();



					org.apache.hadoop.fs.Path path = new org.apache.hadoop.fs.Path(inputPath);
					System.out.println("Path = "+path.toUri());
					org.apache.hadoop.fs.FileSystem fs = path.getFileSystem(conf);
					System.out.println("Got FS = "+fs.getScheme());
					org.apache.hadoop.fs.FSDataInputStream inputStream = fs.open(path);
					System.out.println("Got file of size: "+inputStream.available());

					java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
					byte[] b = new byte[1];
					while(inputStream.read(b)!=-1){
						bo.write(b);
					}

					byte[] outbuff = bo.toByteArray();


					System.out.println("Data Loaded = "+outbuff.length);
					


					////////////////////////////////////////////////////////////////////////////////////////


					java.io.InputStream cis = new java.io.ByteArrayInputStream(outbuff);
					
//					java.io.InputStream cis = new java.io.ByteArrayInputStream(classifierBuf);
									
					
					
					try {
						classifier.classifier = (weka.classifiers.Classifier) weka.core.SerializationHelper.read(cis);
						
					
					} catch (Exception e) {
						e.printStackTrace();
					}
				}catch(Exception e){
					e.printStackTrace();
				}

				java.util.StringTokenizer st = new java.util.StringTokenizer(rowData, ",");
				int attributesCount = st.countTokens();
				java.lang.StringBuilder stBuilder = new java.lang.StringBuilder();
				stBuilder.append("@"+"RELATION Drill\n");
				for(int i=0; i< attributesCount-1;i++)
				{
					stBuilder.append("@"+"ATTRIBUTE att"+i+" numeric\n");
				}
				byte[] argsBuf = new byte[arguments.end - arguments.start];
				arguments.buffer.getBytes(arguments.start, argsBuf, 0, arguments.end - arguments.start);
				String classType = "numeric";
				try {
					options = weka.core.Utils.splitOptions((new String(argsBuf, com.google.common.base.Charsets.UTF_8)));
					for(int i=0;i<options.length;i++){
						if(options[i].indexOf("classes")>0){
							classType = options[i+1];
							options[i]="";
							options[i+1]="";
						}
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				stBuilder.append("@"+"ATTRIBUTE class "+classType+"\n");
				stBuilder.append("@"+"DATA\n");
				arffHeader.value = stBuilder.toString();
			}
			try {
				weka.core.Instances instances = new weka.core.Instances(new java.io.StringReader(arffHeader.value+rowData));
				instances.setClassIndex(instances.numAttributes() - 1);

				Class<?> c = Class.forName(function.value);
				java.lang.reflect.Method m = c.getMethod("updateClassifier", weka.core.Instance.class);
				m.invoke(Class.forName(function.value).cast(classifier.classifier),instances.instance(0));

			} catch (Exception e) {
				e.printStackTrace();
			}


		}
		@Override
		public void output() {
			try {
				java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
				weka.core.SerializationHelper.write(os, classifier.classifier);
				out.buffer = tempBuff;
				out.buffer = out.buffer.reallocIfNeeded(os.toByteArray().length);
				out.buffer.setBytes(0, os.toByteArray());//.setBytes(0,outbuff);
				out.start=0;
				out.end=os.toByteArray().length;
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		@Override
		public void reset() {
		}
	}


	/**
	 * @author shadi
	 * 
	 * Create table xzy as 
	 * select qdm_score_weka('nb','-classes {1,2}', mymodel.columns[0], mydata.columns[0]) 
	 * from `output100M.csv` as mydata applying nb100M_3 as mymodel;
	 *
	 */

	@FunctionTemplate(name = "qdm_score_weka", scope = FunctionScope.SIMPLE, nulls = NullHandling.INTERNAL)
	public static class WekaScoreUpdateable1Column implements DrillSimpleFunc{
		

//	@Param  VarCharHolder operation;
	@Param  VarCharHolder arguments;
	@Param 	NullableVarCharHolder classifierTxt;
	@Param  NullableVarCharHolder features;
	@Output VarCharHolder out;
	@Inject DrillBuf tempBuff;
	@Workspace WekaUpdatableClassifierHolder classifier;
//	@Workspace String function;
	@Workspace String[] classes;
	@Workspace StringHolder arffHeader;
	@Workspace  BitHolder firstRun;

	public void setup() {
		classifier = new WekaUpdatableClassifierHolder();
		arffHeader = new StringHolder();
		firstRun = new BitHolder();
		classifier.classifier=null;
		arffHeader.value=null;
		firstRun.value=0;
	}

	public void eval() {
		byte[] temp = new byte[features.end - features.start];
		features.buffer.getBytes(features.start, temp, 0, features.end - features.start);
		String rowData = new String(temp, com.google.common.base.Charsets.UTF_8);
		if(firstRun.value==0){
			firstRun.value=1;
//			byte[] operationBuf = new byte[operation.end - operation.start];
//			operation.buffer.getBytes(operation.start, operationBuf, 0, operation.end - operation.start);
//			function = new String(operationBuf, com.google.common.base.Charsets.UTF_8).toLowerCase();
			try{

				byte[] classifierBuf = new byte[classifierTxt.end - classifierTxt.start];
				classifierTxt.buffer.getBytes(classifierTxt.start, classifierBuf, 0, classifierTxt.end - classifierTxt.start);


				//////////////////////////////////////////////////////////////////////////////////////////
				
				String inputPath = new String(classifierBuf, com.google.common.base.Charsets.UTF_8);
								
				System.out.println(inputPath);


				org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();



				org.apache.hadoop.fs.Path path = new org.apache.hadoop.fs.Path(inputPath);
				System.out.println("Path = "+path.toUri());
				org.apache.hadoop.fs.FileSystem fs = path.getFileSystem(conf);
				System.out.println("Got FS = "+fs.getScheme());
				org.apache.hadoop.fs.FSDataInputStream inputStream = fs.open(path);
				System.out.println("Got file of size: "+inputStream.available());

				java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
				byte[] b = new byte[1];
				while(inputStream.read(b)!=-1){
					bo.write(b);
				}

				byte[] outbuff = bo.toByteArray();


				System.out.println("Data Loaded = "+outbuff.length);
				


				////////////////////////////////////////////////////////////////////////////////////////


				java.io.InputStream cis = new java.io.ByteArrayInputStream(outbuff);
				
//				java.io.InputStream cis = new java.io.ByteArrayInputStream(classifierBuf);
				
				
				try {
					classifier.classifier = (weka.classifiers.Classifier) weka.core.SerializationHelper.read(cis);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			java.util.StringTokenizer st = new java.util.StringTokenizer(rowData, ",");
			int attributesCount = st.countTokens();
			java.lang.StringBuilder stBuilder = new java.lang.StringBuilder();
			stBuilder.append("@"+"RELATION Drill\n");
			for(int i=0; i< attributesCount-1;i++)
			{
				stBuilder.append("@"+"ATTRIBUTE att"+i+" numeric\n");
			}
			byte[] argsBuf = new byte[arguments.end - arguments.start];
			arguments.buffer.getBytes(arguments.start, argsBuf, 0, arguments.end - arguments.start);
			//				String[] classes = null;
			String[] options = null;
			String classType = "numeric";
			try {
				options = weka.core.Utils.splitOptions((new String(argsBuf, com.google.common.base.Charsets.UTF_8)));
				for(int i=0;i<options.length;i++){
					if(options[i].indexOf("classes")>0){
						classType = options[i+1];
						classes = options[i+1].substring(1, options[i+1].length()-1).split(",");
						options[i]="";
						options[i+1]="";
					}
				}

			} catch (Exception e1) {
				e1.printStackTrace();
			}
			//				arffHeader+="@"+"ATTRIBUTE class "+classType+"\n";
			stBuilder.append("@"+"ATTRIBUTE class "+classType+"\n");
			stBuilder.append("@"+"DATA\n");
			arffHeader.value = stBuilder.toString();
		}

		try {
//			System.out.println(arffHeader.value+rowData/*+",0"*/);
			weka.core.Instances instances = new weka.core.Instances(new java.io.StringReader(arffHeader.value+rowData/*+",0"*/));
			
			instances.setClassIndex(instances.numAttributes() - 1);
			String output="";
			double[] predictions = classifier.classifier.distributionForInstance(instances.instance(0));
			if(predictions.length==1){
				if(classes!=null){
					output=classes[(int)predictions[0]];
				} else {
					output = ""+predictions[0];
				}
			} else {
				//					java.util.List b = java.util.Arrays.asList(org.apache.commons.lang.ArrayUtils.toObject(predictions));
				//					double max = -1;
				//					for(int i=0;i<predictions.length;i++){
				//						if(predictions[i]>max){
				//							max=predictions[i];
				//							output=""+ (i+1);							
				//						}
				//					}
				if(classes!=null){
					//						output= classes[b.indexOf(java.util.Collections.max(b))];
					double max = -1;
					for(int i=0;i<predictions.length;i++){
						if(predictions[i]>max){
							max=predictions[i];
							output=classes[i];
						}
					}
				}else{
					//						output= ""+(b.indexOf(java.util.Collections.max(b))+1);
					double max = -1;
					for(int i=0;i<predictions.length;i++){
						if(predictions[i]>max){
							max=predictions[i];
							output=""+ (i+1);
						}
					}
				}
			}

			out.buffer = tempBuff;
			out.buffer = out.buffer.reallocIfNeeded(output.getBytes().length);
			out.buffer.setBytes(0, output.getBytes());//.setBytes(0,outbuff);
			out.start=0;
			out.end=output.getBytes().length;
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

	/**
	 * @author shadi
	 * 
	 * Create table xzy as 
	 * select qdm_score_weka('nb','-classes {1,2}', mymodel.columns[0], mydata.columns[0],....) 
	 * from `output100M.csv` as mydata applying nb100M_3 as mymodel;
	 *
	 */

	@FunctionTemplate(name = "qdm_score_weka", scope = FunctionScope.SIMPLE, nulls = NullHandling.INTERNAL)
	public static class WekaScoreUpdateable implements DrillSimpleFunc{
//		@Param  VarCharHolder operation;
		@Param  VarCharHolder arguments;
		@Param 	NullableVarCharHolder classifierTxt;
		@Param  VarCharHolder features;
		@Output VarCharHolder out;
		@Inject DrillBuf tempBuff;
		@Workspace WekaUpdatableClassifierHolder classifier;
//		@Workspace String function;
		@Workspace String[] classes;
		@Workspace StringHolder arffHeader;
		@Workspace  BitHolder firstRun;

		public void setup() {
			classifier = new WekaUpdatableClassifierHolder();
			arffHeader = new StringHolder();
			firstRun = new BitHolder();
			classifier.classifier=null;
			arffHeader.value=null;
			firstRun.value=0;
		}

		public void eval() {
			byte[] temp = new byte[features.end - features.start];
			features.buffer.getBytes(features.start, temp, 0, features.end - features.start);
			String rowData = new String(temp, com.google.common.base.Charsets.UTF_8);
			if(firstRun.value==0){
				firstRun.value=1;
//				byte[] operationBuf = new byte[operation.end - operation.start];
//				operation.buffer.getBytes(operation.start, operationBuf, 0, operation.end - operation.start);
//				function = new String(operationBuf, com.google.common.base.Charsets.UTF_8).toLowerCase();
				try{

					byte[] classifierBuf = new byte[classifierTxt.end - classifierTxt.start];
					classifierTxt.buffer.getBytes(classifierTxt.start, classifierBuf, 0, classifierTxt.end - classifierTxt.start);


					//////////////////////////////////////////////////////////////////////////////////////////
					
					String inputPath = new String(classifierBuf, com.google.common.base.Charsets.UTF_8);
									
					System.out.println(inputPath);


					org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();



					org.apache.hadoop.fs.Path path = new org.apache.hadoop.fs.Path(inputPath);
					System.out.println("Path = "+path.toUri());
					org.apache.hadoop.fs.FileSystem fs = path.getFileSystem(conf);
					System.out.println("Got FS = "+fs.getScheme());
					org.apache.hadoop.fs.FSDataInputStream inputStream = fs.open(path);
					System.out.println("Got file of size: "+inputStream.available());

					java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
					byte[] b = new byte[1];
					while(inputStream.read(b)!=-1){
						bo.write(b);
					}

					byte[] outbuff = bo.toByteArray();


					System.out.println("Data Loaded = "+outbuff.length);
					


					////////////////////////////////////////////////////////////////////////////////////////


					java.io.InputStream cis = new java.io.ByteArrayInputStream(outbuff);
					
//					java.io.InputStream cis = new java.io.ByteArrayInputStream(classifierBuf);
					
					
					try {
						classifier.classifier = (weka.classifiers.Classifier) weka.core.SerializationHelper.read(cis);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				java.util.StringTokenizer st = new java.util.StringTokenizer(rowData, ",");
				int attributesCount = st.countTokens();
				java.lang.StringBuilder stBuilder = new java.lang.StringBuilder();
				stBuilder.append("@"+"RELATION Drill\n");
				for(int i=0; i< attributesCount;i++)
				{
					stBuilder.append("@"+"ATTRIBUTE att"+i+" numeric\n");
				}
				byte[] argsBuf = new byte[arguments.end - arguments.start];
				arguments.buffer.getBytes(arguments.start, argsBuf, 0, arguments.end - arguments.start);
				//				String[] classes = null;
				String[] options = null;
				String classType = "numeric";
				try {
					options = weka.core.Utils.splitOptions((new String(argsBuf, com.google.common.base.Charsets.UTF_8)));
					for(int i=0;i<options.length;i++){
						if(options[i].indexOf("classes")>0){
							classType = options[i+1];
							classes = options[i+1].substring(1, options[i+1].length()-1).split(",");
							options[i]="";
							options[i+1]="";
						}
					}

				} catch (Exception e1) {
					e1.printStackTrace();
				}
				//				arffHeader+="@"+"ATTRIBUTE class "+classType+"\n";
				stBuilder.append("@"+"ATTRIBUTE class "+classType+"\n");
				stBuilder.append("@"+"DATA\n");
				arffHeader.value = stBuilder.toString();
			}

			try {
				weka.core.Instances instances = new weka.core.Instances(new java.io.StringReader(arffHeader.value+rowData+",0"));
				instances.setClassIndex(instances.numAttributes() - 1);
				String output="";
				double[] predictions = classifier.classifier.distributionForInstance(instances.instance(0));
				if(predictions.length==1){
					if(classes!=null){
						output=classes[(int)predictions[0]];
					} else {
						output = ""+predictions[0];
					}
				} else {
					//					java.util.List b = java.util.Arrays.asList(org.apache.commons.lang.ArrayUtils.toObject(predictions));
					//					double max = -1;
					//					for(int i=0;i<predictions.length;i++){
					//						if(predictions[i]>max){
					//							max=predictions[i];
					//							output=""+ (i+1);							
					//						}
					//					}
					if(classes!=null){
						//						output= classes[b.indexOf(java.util.Collections.max(b))];
						double max = -1;
						for(int i=0;i<predictions.length;i++){
							if(predictions[i]>max){
								max=predictions[i];
								output=classes[i];
							}
						}
					}else{
						//						output= ""+(b.indexOf(java.util.Collections.max(b))+1);
						double max = -1;
						for(int i=0;i<predictions.length;i++){
							if(predictions[i]>max){
								max=predictions[i];
								output=""+ (i+1);
							}
						}
					}
				}

				out.buffer = tempBuff;
				out.buffer = out.buffer.reallocIfNeeded(output.getBytes().length);
				out.buffer.setBytes(0, output.getBytes());//.setBytes(0,outbuff);
				out.start=0;
				out.end=output.getBytes().length;
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}


	/**
	 * @author shadi
	 * 
	 * Create table xzy as 
	 * select qdm_score_weka('-classes {1,2}', mymodel.columns[0], mydata.columns) 
	 * from `output100M.csv` as mydata applying nb100M_3 as mymodel;
	 *
	 */

	@FunctionTemplate(name = "qdm_score_weka", scope = FunctionScope.SIMPLE, nulls = NullHandling.INTERNAL)
	public static class WekaScoreUpdateableRepeated implements DrillSimpleFunc{
//		@Param  VarCharHolder operation;
		@Param  VarCharHolder arguments;
		@Param 	NullableVarCharHolder classifierTxt;
		@Param  RepeatedVarCharHolder features;
		@Output VarCharHolder out;
		@Inject DrillBuf tempBuff;
		@Workspace WekaUpdatableClassifierHolder classifier;
//		@Workspace String function;
		@Workspace String[] classes;
		@Workspace StringHolder arffHeader;
		@Workspace  BitHolder firstRun;
		@Workspace VarCharHolder currVal;

		public void setup() {
			classifier = new WekaUpdatableClassifierHolder();
			arffHeader = new StringHolder();
			firstRun = new BitHolder();
			classifier.classifier=null;
			arffHeader.value=null;
			firstRun.value=0;
			currVal = new VarCharHolder();
		}

		public void eval() {
			byte[] temp = new byte[features.end - features.start];		
			java.lang.StringBuilder rowBuilder = new java.lang.StringBuilder();
			for (int i = features.start; i < features.end; i++) {
				features.vector.getAccessor().get(i, currVal);
				rowBuilder.append(org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(currVal.start, currVal.end, currVal.buffer)+",");
			} 
			String rowData = rowBuilder.substring(0, rowBuilder.length()-1);
			if(firstRun.value==0){
				firstRun.value=1;
//				byte[] operationBuf = new byte[operation.end - operation.start];
//				operation.buffer.getBytes(operation.start, operationBuf, 0, operation.end - operation.start);
//				function = new String(operationBuf, com.google.common.base.Charsets.UTF_8).toLowerCase();
				try{
					byte[] classifierBuf = new byte[classifierTxt.end - classifierTxt.start];
					classifierTxt.buffer.getBytes(classifierTxt.start, classifierBuf, 0, classifierTxt.end - classifierTxt.start);
					
//					classifierTxt.vector.getAccessor().get(classifierTxt.start, currVal);
//					byte[] classifierBuf = new byte[currVal.end - currVal.start];
//					currVal.buffer.getBytes(currVal.start, classifierBuf, 0, currVal.end - currVal.start);
					

					//////////////////////////////////////////////////////////////////////////////////////////
					
					String inputPath = new String(classifierBuf, com.google.common.base.Charsets.UTF_8);
									
					System.out.println(inputPath);


					org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();



					org.apache.hadoop.fs.Path path = new org.apache.hadoop.fs.Path(inputPath);
					System.out.println("Path = "+path.toUri());
					org.apache.hadoop.fs.FileSystem fs = path.getFileSystem(conf);
					System.out.println("Got FS = "+fs.getScheme());
					org.apache.hadoop.fs.FSDataInputStream inputStream = fs.open(path);
					System.out.println("Got file of size: "+inputStream.available());

					java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
					byte[] b = new byte[1];
					while(inputStream.read(b)!=-1){
						bo.write(b);
					}

					byte[] outbuff = bo.toByteArray();


					System.out.println("Data Loaded = "+outbuff.length);
					


					////////////////////////////////////////////////////////////////////////////////////////


					java.io.InputStream cis = new java.io.ByteArrayInputStream(outbuff);
					
//					java.io.InputStream cis = new java.io.ByteArrayInputStream(classifierBuf);
					
					try {
						classifier.classifier = (weka.classifiers.Classifier) weka.core.SerializationHelper.read(cis);
						System.out.println("Model LOADED");

					} catch (Exception e) {
						e.printStackTrace();
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				java.util.StringTokenizer st = new java.util.StringTokenizer(rowData, ",");
				int attributesCount = st.countTokens();
				java.lang.StringBuilder stBuilder = new java.lang.StringBuilder();
				stBuilder.append("@"+"RELATION Drill\n");
				for(int i=0; i< attributesCount-1;i++)
				{
					stBuilder.append("@"+"ATTRIBUTE att"+i+" numeric\n");
				}
				byte[] argsBuf = new byte[arguments.end - arguments.start];
				arguments.buffer.getBytes(arguments.start, argsBuf, 0, arguments.end - arguments.start);
				//				String[] classes = null;
				String[] options = null;
				String classType = "numeric";
				try {
					options = weka.core.Utils.splitOptions((new String(argsBuf, com.google.common.base.Charsets.UTF_8)));
					for(int i=0;i<options.length;i++){				
						if(options[i].indexOf("classes")>0){
							classType = options[i+1];
							classes = options[i+1].substring(1, options[i+1].length()-1).split(",");
							options[i]="";
							options[i+1]="";
						}
					}

				} catch (Exception e1) {
					e1.printStackTrace();
				}
				//				arffHeader+="@"+"ATTRIBUTE class "+classType+"\n";
				stBuilder.append("@"+"ATTRIBUTE class "+classType+"\n");
				stBuilder.append("@"+"DATA\n");
				arffHeader.value = stBuilder.toString();
			}

			try {
				//System.out.println(arffHeader.value+rowData/*+",0"*/);
				weka.core.Instances instances = new weka.core.Instances(new java.io.StringReader(arffHeader.value+rowData));
				//				System.out.println("Classifier NEXT Run "+(classifier.classifier == null?"NULL":"Loaded"));
				instances.setClassIndex(instances.numAttributes() - 1);
				String output="";
				double[] predictions = classifier.classifier.distributionForInstance(instances.instance(0));
				if(predictions.length==1){
					if(classes!=null){
						output=classes[(int)predictions[0]];
					} else {
						output = ""+predictions[0];
					}
				} else {
					//					java.util.List b = java.util.Arrays.asList(org.apache.commons.lang.ArrayUtils.toObject(predictions));
					//					double max = -1;
					//					for(int i=0;i<predictions.length;i++){
					//						if(predictions[i]>max){
					//							max=predictions[i];
					//							output=""+ (i+1);							
					//						}
					//					}
					if(classes!=null){
						//						output= classes[b.indexOf(java.util.Collections.max(b))];
						double max = -1;
						for(int i=0;i<predictions.length;i++){
							if(predictions[i]>max){
								max=predictions[i];
								output=classes[i];
							}
						}
					}else{
						//						output= ""+(b.indexOf(java.util.Collections.max(b))+1);
						double max = -1;
						for(int i=0;i<predictions.length;i++){
							if(predictions[i]>max){
								max=predictions[i];
								output=""+ (i+1);
							}
						}
					}
				}

				out.buffer = tempBuff;
				out.buffer = out.buffer.reallocIfNeeded(output.getBytes().length);
				out.buffer.setBytes(0, output.getBytes());//.setBytes(0,outbuff);
				out.start=0;
				out.end=output.getBytes().length;
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}


}

