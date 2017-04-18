/***************************************************************************
 * Copyright 2015 Kieker Project (http://kieker-monitoring.net)
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

package kieker.analysisteetime.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import kieker.analysisteetime.util.RunningMedian;

public class MedianCalculator<T> implements Calculator<T> {

	private final static Property AVERAGE_PROPERTY = Properties.AVERAGE;

	private final Map<Object, RunningMedian<Long>> runningMedians = new HashMap<>();
	private final Function<T, Long> valueAccessor;

	public MedianCalculator(final Function<T, Long> valueAccessor) {
		this.valueAccessor = valueAccessor;
	}

	@Override
	public void calculate(final Statistic statistic, final T input, final Object modelObject) {
		final RunningMedian<Long> runningMedian = this.runningMedians.computeIfAbsent(modelObject, o -> RunningMedian.forLong());
		runningMedian.add(this.valueAccessor.apply(input));
		final long newMedian = runningMedian.getMedian();
		statistic.setProperty(AVERAGE_PROPERTY, newMedian);
	}

}
