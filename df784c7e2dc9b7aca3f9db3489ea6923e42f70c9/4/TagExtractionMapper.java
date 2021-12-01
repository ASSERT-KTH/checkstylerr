package com.formulasearchengine.mathosphere.mlp.contracts;

import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import com.formulasearchengine.mathosphere.mlp.pojos.RawWikiDocument;
import com.formulasearchengine.mathosphere.mlp.text.MathConverter;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

/**
 * Created by Moritz on 04.08.2017.
 */
public class TagExtractionMapper implements FlatMapFunction<RawWikiDocument, String> {

    @Override
    public void flatMap(RawWikiDocument rawWikiDocument, Collector<String> collector) throws Exception {
        final MathConverter converter = new MathConverter(rawWikiDocument.text, rawWikiDocument.title);
        converter.getStrippedOutput();
        for (MathTag tag : converter.getMathTags()) {
            collector.collect(tag.getContent());
        }
    }
}
