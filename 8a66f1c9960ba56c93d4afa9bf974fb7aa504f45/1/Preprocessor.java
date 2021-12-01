package com.bakdata.conquery.models.preproc;

import com.bakdata.conquery.ConqueryConstants;
import com.bakdata.conquery.io.HCFile;
import com.bakdata.conquery.io.csv.CSV;
import com.bakdata.conquery.io.jackson.Jackson;
import com.bakdata.conquery.models.config.ConqueryConfig;
import com.bakdata.conquery.models.exceptions.JSONException;
import com.bakdata.conquery.models.exceptions.ParsingException;
import com.bakdata.conquery.models.preproc.outputs.AutoOutput;
import com.bakdata.conquery.models.preproc.outputs.Output;
import com.bakdata.conquery.models.types.CType;
import com.bakdata.conquery.models.types.parser.Parser;
import com.bakdata.conquery.models.types.parser.specific.string.MapTypeGuesser;
import com.bakdata.conquery.models.types.parser.specific.string.StringParser;
import com.bakdata.conquery.models.types.specific.StringTypeEncoded.Encoding;
import com.bakdata.conquery.util.io.ConqueryFileUtil;
import com.bakdata.conquery.util.io.ConqueryMDC;
import com.bakdata.conquery.util.io.LogUtil;
import com.bakdata.conquery.util.io.ProgressBar;
import com.google.common.io.CountingInputStream;
import com.jakewharton.byteunits.BinaryByteUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;

@Slf4j
@RequiredArgsConstructor
@Getter
public class Preprocessor {

	private final ConqueryConfig config;
	private final ImportDescriptor descriptor;
	private final AtomicLong errorCounter = new AtomicLong(0L);
	private long totalCsvSize;

	public boolean requiresProcessing() {
		ConqueryMDC.setLocation(descriptor.toString());
		if(descriptor.getInputFile().getPreprocessedFile().exists()) {
			log.info("EXISTS ALREADY");
			int currentHash = descriptor.calculateValidityHash();
			try (HCFile outFile = new HCFile(descriptor.getInputFile().getPreprocessedFile(), false)) {
				try (InputStream is = outFile.readHeader()) {
					PPHeader header = Jackson.BINARY_MAPPER.readValue(is, PPHeader.class);
					if(header.getValidityHash()==currentHash) {
						log.info("\tHASH STILL VALID");
						return false;
					}
					else {
						log.info("\tHASH OUTDATED");
					}
				}
			}
			catch(Exception e) {
				log.warn("\tHEADER READING FAILED", e);
			}
		}
		else {
			log.info("DOES NOT EXIST");
		}
		
		for(Input input : descriptor.getInputs()) {
			totalCsvSize += input.getSourceFile().length();
		}
		
		return true;
	}
	
	public void preprocess(ProgressBar totalProgress) throws IOException, JSONException, ParsingException {
		ConqueryMDC.setLocation(descriptor.toString());

		//create temporary folders and check for correct permissions
		File tmp = ConqueryFileUtil.createTempFile(descriptor.getInputFile().getPreprocessedFile().getName(), ConqueryConstants.EXTENSION_PREPROCESSED.substring(1));
		if(!Files.isWritable(tmp.getParentFile().toPath())) {
			throw new IllegalArgumentException("No write permission in "+LogUtil.printPath(tmp.getParentFile()));
		}
		if(!Files.isWritable(descriptor.getInputFile().getPreprocessedFile().toPath().getParent())) {
			throw new IllegalArgumentException("No write permission in "+LogUtil.printPath(descriptor.getInputFile().getPreprocessedFile().toPath().getParent()));
		}
		//delete target file if it exists
		if(descriptor.getInputFile().getPreprocessedFile().exists()) {
			FileUtils.forceDelete(descriptor.getInputFile().getPreprocessedFile());
		}


		log.info("PREPROCESSING START in {}", descriptor.getInputFile().getDescriptionFile());
		Preprocessed result = new Preprocessed(config.getPreprocessor(), descriptor);

		try (HCFile outFile = new HCFile(tmp, true)) {
			
			long lineId = config.getCsv().isSkipHeader()?1:0;
			for(int inputSource=0;inputSource<descriptor.getInputs().length;inputSource++) {
				Input input = descriptor.getInputs()[inputSource];
				final String name = descriptor.toString()+":"+descriptor.getTable()+"["+inputSource+"]";
				ConqueryMDC.setLocation(name);

				try(CountingInputStream countingIn = new CountingInputStream(new FileInputStream(input.getSourceFile()))) {
					long progress = 0;
					try(CSV csv = new CSV(
						config.getCsv(),
						CSV.isGZipped(input.getSourceFile())?new GZIPInputStream(countingIn):countingIn
					)){
						Iterator<String[]> it = csv.iterateContent();
	
						while(it.hasNext()) {
							String[] row = it.next();
							Integer primary = getPrimary((StringParser) result.getPrimaryColumn().getParser(), row, lineId, inputSource, input.getPrimary());
							if(primary != null) {
								int primaryId = result.addPrimary(primary);
								parseRow(primaryId, result.getColumns(), row, input, lineId, result, inputSource);
							}
							
							//report progress
							long newProgress = countingIn.getCount();
							totalProgress.addCurrentValue(newProgress - progress);
							progress = newProgress;
							lineId++;
						}
	
						if (input.checkAutoOutput()) {
							List<AutoOutput.OutRow> outRows = input.getAutoOutput().finish();
							for (AutoOutput.OutRow outRow : outRows) {
								result.addRow(outRow.getPrimaryId(), outRow.getTypes(), outRow.getData());
							}
						}
					}
				}
			}
			//find the optimal subtypes
			log.info("finding optimal column types");
			log.info("\t{}.{}: {} -> {}", result.getName(), result.getPrimaryColumn().getName(), result.getPrimaryColumn().getParser(), result.getPrimaryColumn().getType());
			
			StringParser parser = (StringParser)result.getPrimaryColumn().getParser();
			parser.setEncoding(Encoding.UTF8);
			result.getPrimaryColumn().setType(new MapTypeGuesser(parser).createGuess().getType());
			for(PPColumn c:result.getColumns()) {
				c.findBestType();
				log.info("\t{}.{}: {} -> {}", result.getName(), c.getName(), c.getParser(), c.getType());
			}
			//estimate memory weight
			log.info("estimated total memory consumption: {} + n*{}", 
				BinaryByteUnit.format(
					Arrays.stream(result.getColumns()).map(PPColumn::getType).mapToLong(CType::estimateMemoryConsumption).sum()
					+ result.getPrimaryColumn().getType().estimateMemoryConsumption()
				),
				BinaryByteUnit.format(
					Arrays.stream(result.getColumns()).map(PPColumn::getType).mapToLong(CType::estimateTypeSize).sum()
					+ result.getPrimaryColumn().getType().estimateTypeSize()
				)
			);
			for(PPColumn c:ArrayUtils.add(result.getColumns(), result.getPrimaryColumn())) {
				long typeConsumption = c.getType().estimateTypeSize();
				log.info("\t{}.{}: {}{}",
					result.getName(),
					c.getName(),
					BinaryByteUnit.format(c.getType().estimateMemoryConsumption()),
					typeConsumption==0?"":(" + n*"+BinaryByteUnit.format(typeConsumption))
				);
			}

			try (com.esotericsoftware.kryo.io.Output out = new com.esotericsoftware.kryo.io.Output(outFile.writeContent())) {
				result.writeToFile(out);
			}

			try (OutputStream out = outFile.writeHeader()) {
				result.writeHeader(out);
			}
		}


		//if successful move the tmp file to the target location
		FileUtils.moveFile(tmp, descriptor.getInputFile().getPreprocessedFile());
		log.info("PREPROCESSING DONE in {}", descriptor.getInputFile().getDescriptionFile());
	}

	private void parseRow(int primaryId, PPColumn[] columns, String[] row, Input input, long lineId, Preprocessed result, int inputSource) {
		try {

			if (input.checkAutoOutput()) {
				List<AutoOutput.OutRow> outRows = input.getAutoOutput().createOutput(primaryId, row, columns, inputSource, lineId);
				for (AutoOutput.OutRow outRow : outRows) {
					result.addRow(primaryId, columns, outRow.getData());
				}
			}
			else {
				if (input.filter(row)) {
					for (Object[] outRow : generateOutput(input, columns, row, inputSource, lineId)) {
						result.addRow(primaryId, columns, outRow);
					}
				}
			}
		}
		catch (ParsingException e) {
			long errors = errorCounter.getAndIncrement();
			if (errors < config.getPreprocessor().getMaximumPrintedErrors()) {
				log.warn("Failed to parse line:" + lineId + " content:" + Arrays.toString(row), e);
			}
			else if (errors == config.getPreprocessor().getMaximumPrintedErrors()) {
				log.warn("More erroneous lines occurred. Only the first " + config.getPreprocessor().getMaximumPrintedErrors() + " were printed.");
			}

		}
		catch (Exception e) {
			throw new IllegalStateException("failed while processing line:" + lineId + " content:" + Arrays.toString(row), e);
		}
	}

	private Integer getPrimary(StringParser primaryType, String[] row, long lineId, int source, Output primaryOutput) {
		try {
			List<Object> primary = primaryOutput.createOutput(primaryType, row, source, lineId);
			if(primary.size()!=1 || !(primary.get(0) instanceof Integer)) {
				throw new IllegalStateException("The returned primary was the illegal value "+primary+" in "+Arrays.toString(row));
			}
			return (int)primary.get(0);
		} catch (ParsingException e) {
			long errors = errorCounter.getAndIncrement();
			if(errors<config.getPreprocessor().getMaximumPrintedErrors()) {
				log.warn("Failed to parse primary from line:"+lineId+" content:"+Arrays.toString(row), e);
			}
			else if(errors == config.getPreprocessor().getMaximumPrintedErrors()) {
				log.warn("More erroneous lines occurred. Only the first "+config.getPreprocessor().getMaximumPrintedErrors()+" were printed.");
			}
			return null;
		}
	}

	private static List<Object[]> generateOutput(Input input, PPColumn[] columns, String[] row, int source, long lineId) throws ParsingException {
		List<Object[]> resultRows = new ArrayList<>();
		int oid = 0;
		for(int c = 0; c<input.getOutput().length; c++) {
			Output out = input.getOutput()[c];
			Parser<?> parser = columns[c].getParser();

			List<Object> result;
			result = out.createOutput(parser, row, source, lineId);
			if(result==null) {
				throw new IllegalStateException(out+" returned null result for "+Arrays.toString(row));
			}


			//if the result is a single NULL and we don't want to include such rows
			if(result.size()==1 && result.get(0)==null && out.isRequired()) {
				return Collections.emptyList();
			}
			else {
				if(resultRows.isEmpty()) {
					for(Object v:result) {
						Object[] newRow = new Object[input.getOutput().length];
						newRow[oid]=v;
						resultRows.add(newRow);
					}
				}
				else {
					if(result.size()==1) {
						for(Object[] resultRow:resultRows) {
							resultRow[oid]=result.get(0);
						}
					}
					else {
						List<Object[]> newResultRows = new ArrayList<>(resultRows.size()*result.size());
						for(Object v:result) {
							for(Object[] resultRow:resultRows) {
								Object[] newResultRow = Arrays.copyOf(resultRow,resultRow.length);
								newResultRow[oid]=v;
								newResultRows.add(newResultRow);
							}
						}
						resultRows = newResultRows;
					}
				}
			}
			oid++;
		}
		return resultRows;
	}
}