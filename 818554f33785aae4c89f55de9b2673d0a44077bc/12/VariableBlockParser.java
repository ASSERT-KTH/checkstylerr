package com.synaptix.toast.runtime.core.parse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.synaptix.toast.dao.domain.BlockType;
import com.synaptix.toast.dao.domain.impl.test.block.IBlock;
import com.synaptix.toast.dao.domain.impl.test.block.VariableBlock;
import com.synaptix.toast.dao.domain.impl.test.block.line.BlockLine;
import com.synaptix.toast.runtime.parse.IBlockParser;

/**
 * Parser for variable blocks.
 * Parse all lines beginning with $
 */
public class VariableBlockParser implements IBlockParser {

    private static String VARIABLE_ASSIGNATION_SEPARATOR = ":=";

    @Override
    public BlockType getBlockType() {
        return BlockType.VARIABLE;
    }

    @Override
    public IBlock digest(List<String> strings, String path) {
        VariableBlock variableBlock = new VariableBlock();

        for (Iterator<String> iterator = strings.iterator(); iterator.hasNext(); ) {
            final String line = iterator.next();

            if (!isFirstLineOfBlock(line)) { // line is parsable
                return variableBlock;
            }


            String[] textLine = line.split(VARIABLE_ASSIGNATION_SEPARATOR);
           
            List<String> variableParts = new ArrayList<>();
            String variableName = textLine[0].trim();
            variableParts.add(variableName);

            if (isVarMultiLine(line)) {
                StringBuilder variableValue = new StringBuilder();
                variableBlock.addTextLine(line);
                while (iterator.hasNext()) {
                	final String nextLine = iterator.next();
                	variableBlock.addTextLine(nextLine);

                    if (!nextLine.startsWith("\"\"\"")) {
                    	variableValue.append(nextLine.replace("\n", " ").replace("\t", " ")).append(" ");
                    } else {
                        break;
                    }
                }
                variableParts.add(variableValue.toString());
            } else if (isVarLine(line)) {
            	String variableValue = textLine[1].trim();
            	variableParts.add(variableValue);
            	variableBlock.addTextLine(line);
            }
            BlockLine blockLine = new BlockLine();
            blockLine.setCells(variableParts);
            variableBlock.addline(blockLine);
        }
        return variableBlock;
    }

    @Override
    public boolean isFirstLineOfBlock(String line) {
        return isVarLine(line) || isVarMultiLine(line);
    }

    private boolean isVarMultiLine(String line) {
        return line != null && line.startsWith("$")
                && line.contains(VARIABLE_ASSIGNATION_SEPARATOR)
                && line.contains("\"\"\"");
    }

    private boolean isVarLine(String line) {
        return line != null && line.startsWith("$")
                && line.contains(VARIABLE_ASSIGNATION_SEPARATOR)
                && !line.contains("\"\"\"");
    }
}
