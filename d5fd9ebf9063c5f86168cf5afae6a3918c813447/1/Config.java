/*
 * Encog(tm) Java Examples v3.2
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-examples
 *
 * Copyright 2008-2013 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *   
 * For more information on Heaton Research copyrights, licenses 
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package marketPrediction;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.encog.ml.data.market.TickerSymbol;

/**
 * Basic config info for the market prediction example.
 * 
 * @author jeff
 * 
 */
public class Config {
	public static final int DEBUG_LEVEL = 2;
	
	public static final String TRAINING_FILE = "marketData.egb";
	public static final String NETWORK_FILE = "marketNetwork.eg";
//	public static final int TRAINING_MINUTES = 1;
	public static final double TRAIN_THRESH = 0.001;
	public static final int INPUT_WINDOW = 20;
	public static final int HIDDEN1_COUNT = 7*INPUT_WINDOW;
	public static final int HIDDEN2_COUNT = 7;
	public static final int PREDICT_WINDOW = 1;
	public static final TickerSymbol TICKER = new TickerSymbol("^GSPC");
	public static final TickerSymbol TICKER2 = new TickerSymbol("^VIX");
	public static final TickerSymbol TICKER3 = new TickerSymbol("^TNX");
	public static final TickerSymbol TICKER4 = new TickerSymbol("^IXIC");
	public static final TickerSymbol TICKER5 = new TickerSymbol("^UTY");
	public static final TickerSymbol TICKER6 = new TickerSymbol("^FVX");
	public static final TickerSymbol TICKER7 = new TickerSymbol("^BKX");
}
