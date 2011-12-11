/***************************************************************************
 * Copyright 2011 by
 *  + Christian-Albrechts-University of Kiel
 *    + Department of Computer Science
 *      + Software Engineering Group 
 *  and others.
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

package kieker.tools.currentTimeEventGenerator;

import kieker.analysis.plugin.AbstractPlugin;
import kieker.analysis.plugin.port.AOutputPort;
import kieker.analysis.plugin.port.APlugin;
import kieker.common.configuration.Configuration;
import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;

/**
 * Generates time events with a given resolution based on the timestamps of
 * incoming {@link kieker.common.record.IMonitoringRecord}s.
 * 
 * <ol>
 * <li>The first record received via {@link #newTimestamp(long)} immediately leads to a new {@link TimestampEvent} with the given timestamp.</li>
 * <li>The timestamp of the first record is stored as {@link #firstTimestamp} and future events are generated at {@link #firstTimestamp} + i *
 * {@link #timerResolution}.</li>
 * <li>Future {@link kieker.common.record.IMonitoringRecord} may lead to future {@link TimestampEvent} as follows:
 * <ol>
 * <li>A newly incoming {@link kieker.common.record.IMonitoringRecord} with logging timestamp {@literal tstamp} leads to the new timer events satisfying
 * {@link #firstTimestamp} + i * {@link #timerResolution} {@literal <} {@literal tstamp}.</li>
 * </ol>
 * </li>
 * </ol>
 * 
 * 
 * It is guaranteed that the generated timestamps are in ascending order.
 * 
 * @author Andre van Hoorn
 * 
 */
@APlugin(outputPorts =
		@AOutputPort(name = CurrentTimeEventGenerator.CURRENT_TIME_OUTPUT_PORT_NAME, eventTypes = { TimestampEvent.class }, description = "Provides current time events"))
public class CurrentTimeEventGenerator extends AbstractPlugin {

	public static final String CURRENT_TIME_OUTPUT_PORT_NAME = "currentTimeOutputPort";
	public static final String CONFIG_TIME_RESOLUTION = CurrentTimeEventGenerator.class.getName() + ".timeResolution";
	private static final Log LOG = LogFactory.getLog(CurrentTimeEventGenerator.class);

	/**
	 * Timestamp of the record that was received first. Notice, that this is not
	 * necessarily the lowest timestamp.
	 */
	private volatile long firstTimestamp = -1;

	/**
	 * Maximum timestamp received so far.
	 */
	private volatile long maxTimestamp = -1;

	/**
	 * The timestamp of the most recent timer event.
	 */
	private volatile long mostRecentEventFired = -1;

	private final long timerResolution;

	/**
	 * Creates an event generator which generates time events with the given
	 * resolution in nanoseconds via the output port {@link #getCurrentTimeOutputPort()}.
	 * 
	 * @param timeResolution
	 */
	public CurrentTimeEventGenerator(final long timeResolution) {
		super(new Configuration());
		this.timerResolution = timeResolution;
	}

	/**
	 * Evaluates the given timestamp internal current time which may lead to
	 * newly generated events via {@link #getCurrentTimeOutputPort()}.
	 */
	public void newTimestamp(final long timestamp) {
		if (timestamp < 0) {
			CurrentTimeEventGenerator.LOG.warn("Received timestamp value < 0: " + timestamp);
			return;
		}

		if (this.firstTimestamp == -1) {
			/**
			 * Initial record
			 */
			this.maxTimestamp = timestamp;
			this.firstTimestamp = timestamp;
			super.deliver(CurrentTimeEventGenerator.CURRENT_TIME_OUTPUT_PORT_NAME, new TimestampEvent(timestamp));
			this.mostRecentEventFired = timestamp;
		} else if (timestamp > this.maxTimestamp) {
			this.maxTimestamp = timestamp;
			/**
			 * Fire timer event(s) if required.
			 */
			for (long nextTimerEventAt = this.mostRecentEventFired + this.timerResolution; timestamp >= nextTimerEventAt; nextTimerEventAt = this.mostRecentEventFired
					+ this.timerResolution) {
				super.deliver(CurrentTimeEventGenerator.CURRENT_TIME_OUTPUT_PORT_NAME, new TimestampEvent(nextTimerEventAt)); // NOPMD (new in loop)
				this.mostRecentEventFired = nextTimerEventAt;
			}
		}
	}

	@Override
	protected Configuration getDefaultConfiguration() {
		final Configuration configuration = new Configuration();

		configuration.setProperty(CurrentTimeEventGenerator.CONFIG_TIME_RESOLUTION, Long.toString(1000l));

		return configuration;
	}

	@Override
	public Configuration getCurrentConfiguration() {
		final Configuration configuration = new Configuration();

		configuration.setProperty(CurrentTimeEventGenerator.CONFIG_TIME_RESOLUTION, Long.toString(this.timerResolution));

		return configuration;
	}
}
