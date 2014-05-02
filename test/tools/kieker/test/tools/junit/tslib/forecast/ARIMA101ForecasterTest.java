/***************************************************************************
 * Copyright 2012 Kieker Project (http://kieker-monitoring.net)
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
 ***************************************************************************/

package kieker.test.tools.junit.tslib.forecast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import kieker.tools.tslib.ITimeSeries;
import kieker.tools.tslib.TimeSeries;
import kieker.tools.tslib.forecast.IForecastResult;
import kieker.tools.tslib.forecast.arima.ARIMA101Forecaster;

import kieker.test.common.junit.AbstractKiekerTest;

/**
 * @author Tillmann Carlos Bielefeld
 * 
 */
public class ARIMA101ForecasterTest extends AbstractKiekerTest {

	private final long startTime = 98890787;
	private final long deltaTimeMillis = 1000;
	private final int confidenceLevel = 90;
	private final int steps = 1;

	/**
	 * Creates an instance of this class.
	 */
	public ARIMA101ForecasterTest() {
		// Default constructor
	}

	/**
	 * Test of the ARIMA101 Forecaster via Rserve.
	 */
	@Test
	public void testARIMAPredictor() {
		
//		  Test values resulting from this calculation:
		  
		 // var_1 <<- c(1.0,2.0,3.0,4.0)
		 // var_2<<-arima(var_1,c(1,0,1))
		 // var_3<<-predict(var_2,h=1,level=c(90))
		 // dprint(var_3)
		 // dprint(var_3$pred[1])
		 // dprint(var_3$pred[1] + var_3$se[1])
		 // dprint(var_3$pred[1] - var_3$se[1])
		 
		final Double[] values = { 1.0, 2.0, 3.0, 4.0 };
		final List<Double> expectedValues = new ArrayList<Double>(values.length);
		for (final Double curVal : values) {
			expectedValues.add(curVal);
		}

		final TimeSeries<Double> ts =
				new TimeSeries<Double>(this.startTime, this.deltaTimeMillis, TimeUnit.MILLISECONDS);
		ts.appendAll(values);

		final ARIMA101Forecaster forecaster = new ARIMA101Forecaster(ts, this.confidenceLevel);
		final IForecastResult forecast = forecaster.forecast(this.steps);

		final ITimeSeries<Double> forecastSeries = forecast.getForecast();
		final double expectedForecast = 4.210429;
		this.assertEqualsWithTolerance("Unexpected forecast value", expectedForecast, 0.1, forecastSeries.getPoints().get(0).getValue());

		final ITimeSeries<Double> upperSeries = forecast.getUpper();
		final double expectedUpper = 4.852857;
		this.assertEqualsWithTolerance("Unexpected upper value", expectedUpper, 0.1, upperSeries.getPoints().get(0).getValue());

		final ITimeSeries<Double> lowerSeries = forecast.getLower();
		final double expectedLower = 3.568001;
		this.assertEqualsWithTolerance("Unexpected lower value", expectedLower, 0.1, lowerSeries.getPoints().get(0).getValue());
	}

	private void assertEqualsWithTolerance(final String message, final double expected, final double tolerance, final double actual) {
		if (!((expected - tolerance) <= actual) || !(actual <= (expected + tolerance))) {
			Assert.fail(String.format(message + ". Expected value %s with tolerance %s; found %s", expected, tolerance, actual));
		}
	}
}
