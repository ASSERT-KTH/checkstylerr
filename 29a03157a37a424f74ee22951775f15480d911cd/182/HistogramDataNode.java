/*
 * Copyright (c) 2010-2012 Grid Dynamics Consulting Services, Inc, All Rights Reserved
 * http://www.griddynamics.com
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of
 * the Apache License; either
 * version 2.0 of the License, or any later version.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.griddynamics.jagger.util.statistics.percentiles;

import java.util.ArrayList;

// This class is a derivation of hist4j https://github.com/flaptor/hist4j

/**
 * The HistogramDataNode stores the histogram data for a range of values.
 * It knows the minimum and maximum values for which it counts the number of instances.
 * When the count exceeds the allowed limit it splits itself in two, increasing the
 * histogram resolution for this range.
 * @author Jorge Handl
 */
public class HistogramDataNode implements HistogramNode {

   // Attributes of a data node.
	private Cell cell = new Cell();

    /**
     * Creates an empty data node.
     */
    public HistogramDataNode () {
        reset();
    }

    /**
     * Creates a data node for the given range with the given instance count.
     * @param count the number of data instances in the given range.
     * @param minValue the start of the range of counted values.
     * @param maxValue the end of the range of counted values.
     */
    public HistogramDataNode (long count, double minValue, double maxValue) {
        reset();
        cell.count = count;
        cell.minValue = minValue;
        cell.maxValue = maxValue;
    }

    /**
     * Clears the data node.
     */
    public void reset () {
        cell.count = 0;
        cell.minValue = Double.MAX_VALUE;
        cell.maxValue = -Double.MAX_VALUE;
    }

    /**
     * Adds a value to the data node.<p>
     * If the value falls inside of the nodes' range and the count does not exceed the imposed limit, it simply increments the count.<br>
     * If the value falls outside of the nodes' range, it expands the range.<br>
     * If the count exceeds the limit, it splits in two assuming uniform distribution inside the node.<br>
     * If the value falls outside of the nodes' range AND the count exceeds the limit, it creates a new node for that value.
     * @param root a reference to the adaptive histogram instance that uses this structure.
     * @param value the value for which the count is to be incremented.
     * @return A reference to itself if no structural change happened, or a reference to the new fork node if this node was split.
     */
    public HistogramNode addValue (AdaptiveHistogram root, double value) {
        // "self" is what is returned to the caller. If this node needs to be replaced by a fork node,
        // this variable will hold the new fork node and it will be returned to the caller.
        // Otherwise, the node returned will be this, in which case nothing changes.
        HistogramNode self = this;
        if (value >= cell.minValue && value <= cell.maxValue) {  // the value falls within this nodes' range
            if (cell.count < root.getCountPerNodeLimit()  // there is enough room in this node for the new value
                    || cell.minValue == cell.maxValue) {  // or the node defines a zero-width range so it can't be split
            	cell.count++;
            } else {  // not enough room, distribute the value count among the new nodes, assuming uniform distribution
                double splitValue = (cell.minValue + cell.maxValue) / 2;
                long rightCount = cell.count / 2;
                long leftCount = rightCount;
                boolean countWasOdd = (leftCount + rightCount < cell.count);
                // assign the new value to the corresponding side. If the count is odd, add the extra item to the other side to keep balance
                if (value > splitValue) {
                    rightCount++;
                    leftCount += (countWasOdd?1:0);
                } else {
                    leftCount++;
                    rightCount += (countWasOdd?1:0);
                }
                // create a new subtree that will replace this node
                HistogramNode leftNode = new HistogramDataNode(leftCount, cell.minValue, splitValue);
                HistogramNode rightNode = new HistogramDataNode(rightCount, splitValue, cell.maxValue);
                self = new HistogramForkNode(splitValue, leftNode, rightNode);
            }
        } else {  // the value falls outside of this nodes' range
            if (cell.count < root.getCountPerNodeLimit()) {  // there is enough room in this node for the new value
            	cell.count++;
                // extend the range of this node, assuming that the tree structure above correctly directed
                // the given value to this node and therefore it lies at one of the borders of the tree.
                if (value < cell.minValue) cell.minValue = value;
                if (value > cell.maxValue) cell.maxValue = value;
            } else {  // not enough room, create a new sibling node for the new value and put both under a new fork node
                if (value < cell.minValue) {
                	cell.minValue = Math.min(cell.minValue, (value + cell.maxValue) / 2);
                    self = new HistogramForkNode(cell.minValue, new HistogramDataNode(1,value,cell.minValue), this);
                } else {
                	cell.maxValue = Math.max(cell.maxValue, (cell.minValue + value) / 2);
                    self = new HistogramForkNode(cell.maxValue, this, new HistogramDataNode(1,cell.maxValue,value));
                }
            }
        }
        return self;
    }

    /**
     * Returns the number of data points stored in the same bucket as a given value.
     * @param value the reference data point.
     * @return the number of data points stored in the same bucket as the reference point.
     */
    public long getCount (double value) {
        long res = 0;
        if (value >= cell.minValue && value <= cell.maxValue) {
            res = cell.count;
        }
        return res;
    }

    /**
     * Returns the cumulative density function for a given data point.
     * @param value the reference data point.
     * @return the cumulative density function for the reference point.
     */
    public long getAccumCount (double value) {
        long res = 0;
        if (value >= cell.minValue) {
            res = cell.count;
        }
        return res;
    }

    // Linear interpolation for double values.
    private double interpolate (double x0, double y0, double x1, double y1, double x) {
        return y0+((x-x0)*(y1-y0))/(x1-x0);
    }


    /**
     * Returns the data point where the running cumulative count reaches the target cumulative count.
     * It uses linear interpolation over the range of the node to get a better estimate of the true value.
     * @param accumCount an array containing:<br>
     *      - accumCount[0] the running cumulative count. <br>
     *      - accumCount[1] the target cumulative count.
     * @return the data point where the running cumulative count reaches the target cumulative count.
     */
    public Double getValueForAccumCount (long[] accumCount) {
        Double res = null;
        long runningAccumCount = accumCount[0];
        long targetAccumCount = accumCount[1];
        if (runningAccumCount <= targetAccumCount && runningAccumCount + cell.count >= targetAccumCount) {
            double val = interpolate((double)runningAccumCount, cell.minValue, (double)(runningAccumCount + cell.count), cell.maxValue, (double)targetAccumCount);
            res = new Double(val);
        }
        accumCount[0] += cell.count;
        return res;
    }

    /**
     * Applies a convertion function to the values stored in the histogram.
     * @param valueConversion a class that defines a function to convert the value.
     */
    public void apply (AdaptiveHistogram.ValueConversion valueConversion) {
    	cell.minValue = valueConversion.convertValue(cell.minValue);
    	cell.maxValue = valueConversion.convertValue(cell.maxValue);
    }

    /**
     * Build the table representing the histogram data adding this node's cell to it.
     */
    public void toTable (ArrayList<Cell> table) {
    	table.add(cell);
    }

}

