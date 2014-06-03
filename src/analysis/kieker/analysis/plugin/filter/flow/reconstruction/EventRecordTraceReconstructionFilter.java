/***************************************************************************
 * Copyright 2014 Kieker Project (http://kieker-monitoring.net)
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

package kieker.analysis.plugin.filter.flow.reconstruction;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.OutputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.annotation.Property;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.analysis.plugin.filter.flow.TraceEventRecords;
import kieker.common.configuration.Configuration;
import kieker.common.record.flow.IFlowRecord;
import kieker.common.record.flow.trace.AbstractTraceEvent;
import kieker.common.record.flow.trace.TraceMetadata;

/**
 * @author Jan Waller
 * 
 * @since 1.6
 */
@Plugin(
		name = "Trace Reconstruction Filter (Event)",
		description = "Filter to reconstruct event based (flow) traces",
		outputPorts = {
			@OutputPort(
					name = EventRecordTraceReconstructionFilter.OUTPUT_PORT_NAME_TRACE_VALID,
					description = "Outputs valid traces", eventTypes = { TraceEventRecords.class }),
			@OutputPort(
					name = EventRecordTraceReconstructionFilter.OUTPUT_PORT_NAME_TRACE_INVALID,
					description = "Outputs traces missing crucial records", eventTypes = { TraceEventRecords.class }) },
		configuration = {
			@Property(
					name = EventRecordTraceReconstructionFilter.CONFIG_PROPERTY_NAME_TIMEUNIT,
					defaultValue = EventRecordTraceReconstructionFilter.CONFIG_PROPERTY_VALUE_TIMEUNIT),
			@Property(
					name = EventRecordTraceReconstructionFilter.CONFIG_PROPERTY_NAME_MAX_TRACE_DURATION,
					defaultValue = EventRecordTraceReconstructionFilter.CONFIG_PROPERTY_VALUE_MAX_TIME),
			@Property(
					name = EventRecordTraceReconstructionFilter.CONFIG_PROPERTY_NAME_MAX_TRACE_TIMEOUT,
					defaultValue = EventRecordTraceReconstructionFilter.CONFIG_PROPERTY_VALUE_MAX_TIME) })
public final class EventRecordTraceReconstructionFilter extends AbstractFilterPlugin {
	/**
	 * The name of the output port delivering the valid traces.
	 */
	public static final String OUTPUT_PORT_NAME_TRACE_VALID = "validTraces";
	/**
	 * The name of the output port delivering the invalid traces.
	 */
	public static final String OUTPUT_PORT_NAME_TRACE_INVALID = "invalidTraces";
	/**
	 * The name of the input port receiving the trace records.
	 */
	public static final String INPUT_PORT_NAME_TRACE_RECORDS = "traceRecords";
	/**
	 * The name of the input port receiving the trace records.
	 */
	public static final String INPUT_PORT_NAME_TRACEEVENT_RECORDS = "traceEventRecords";
	/**
	 * The name of the input port receiving the trace records.
	 */
	public static final String INPUT_PORT_NAME_TIME_EVENT = "timestamps";

	/**
	 * The name of the property determining the time unit.
	 */
	public static final String CONFIG_PROPERTY_NAME_TIMEUNIT = "timeunit";
	/**
	 * The name of the property determining the maximal trace duration.
	 */
	public static final String CONFIG_PROPERTY_NAME_MAX_TRACE_DURATION = "maxTraceDuration";
	/**
	 * The name of the property determining the maximal trace timeout.
	 */
	public static final String CONFIG_PROPERTY_NAME_MAX_TRACE_TIMEOUT = "maxTraceTimeout";
	/**
	 * The default value of the properties for the maximal trace duration and timeout.
	 */
	public static final String CONFIG_PROPERTY_VALUE_MAX_TIME = "9223372036854775807"; // String.valueOf(Long.MAX_VALUE)
	/**
	 * The default value of the time unit property (nanoseconds).
	 */
	public static final String CONFIG_PROPERTY_VALUE_TIMEUNIT = "NANOSECONDS"; // TimeUnit.NANOSECONDS.name()

	private final TimeUnit timeunit;
	private final long maxTraceDuration;
	private final long maxTraceTimeout;
	private final boolean timeout;
	private long maxEncounteredLoggingTimestamp = -1;

	private final Map<Long, TraceBuffer> traceId2trace;

	/**
	 * Creates a new instance of this class using the given parameters.
	 * 
	 * @param configuration
	 *            The configuration for this component.
	 * @param projectContext
	 *            The project context for this component.
	 */
	public EventRecordTraceReconstructionFilter(final Configuration configuration, final IProjectContext projectContext) {
		super(configuration, projectContext);

		this.timeunit = super.recordsTimeUnitFromProjectContext;

		final String configTimeunitProperty = configuration.getStringProperty(CONFIG_PROPERTY_NAME_TIMEUNIT);
		TimeUnit configTimeunit;
		try {
			configTimeunit = TimeUnit.valueOf(configTimeunitProperty);
		} catch (final IllegalArgumentException ex) {
			this.log.warn(configTimeunitProperty + " is no valid TimeUnit! Using inherited value of " + this.timeunit.name() + " instead.");
			configTimeunit = this.timeunit;
		}

		this.maxTraceDuration = this.timeunit.convert(configuration.getLongProperty(CONFIG_PROPERTY_NAME_MAX_TRACE_DURATION), configTimeunit);
		this.maxTraceTimeout = this.timeunit.convert(configuration.getLongProperty(CONFIG_PROPERTY_NAME_MAX_TRACE_TIMEOUT), configTimeunit);
		this.timeout = !((this.maxTraceTimeout == Long.MAX_VALUE) && (this.maxTraceDuration == Long.MAX_VALUE));
		this.traceId2trace = new ConcurrentHashMap<Long, TraceBuffer>();
	}

	/**
	 * This method is the input port for the timeout.
	 * 
	 * @param timestamp
	 *            The timestamp
	 */
	@InputPort(
			name = INPUT_PORT_NAME_TIME_EVENT,
			description = "Input port for a periodic time signal",
			eventTypes = { Long.class })
	public void newEvent(final Long timestamp) {
		synchronized (this) {
			if (this.timeout) {
				this.processTimeoutQueue(timestamp);
			}
		}
	}

	/**
	 * This method is the input port for the new events for this filter.
	 * 
	 * @param traceEventRecords
	 *            The new record to handle.
	 */
	@InputPort(
			name = INPUT_PORT_NAME_TRACEEVENT_RECORDS,
			description = "Reconstruct traces from incoming traces",
			eventTypes = { TraceEventRecords.class })
	public void newTraceEventRecord(final TraceEventRecords traceEventRecords) {
		final TraceMetadata trace = traceEventRecords.getTraceMetadata();
		if (null != trace) {
			this.newEvent(trace);
		}
		for (final AbstractTraceEvent record : traceEventRecords.getTraceEvents()) {
			this.newEvent(record);
		}
	}

	/**
	 * This method is the input port for the new events for this filter.
	 * 
	 * @param record
	 *            The new record to handle.
	 */
	@InputPort(
			name = INPUT_PORT_NAME_TRACE_RECORDS,
			description = "Reconstruct traces from incoming flow records",
			eventTypes = { TraceMetadata.class, AbstractTraceEvent.class })
	public void newEvent(final IFlowRecord record) {
		final Long traceId;
		TraceBuffer traceBuffer;
		final long loggingTimestamp;
		if (record instanceof TraceMetadata) {
			traceId = ((TraceMetadata) record).getTraceId();
			traceBuffer = this.traceId2trace.get(traceId);
			if (traceBuffer == null) { // first record for this id!
				synchronized (this) {
					traceBuffer = this.traceId2trace.get(traceId);
					if (traceBuffer == null) { // NOCS (DCL)
						traceBuffer = new TraceBuffer();
						this.traceId2trace.put(traceId, traceBuffer);
					}
				}
			}
			traceBuffer.setTrace((TraceMetadata) record);
			loggingTimestamp = -1;
		} else if (record instanceof AbstractTraceEvent) {
			traceId = ((AbstractTraceEvent) record).getTraceId();
			traceBuffer = this.traceId2trace.get(traceId);
			if (traceBuffer == null) { // first record for this id!
				synchronized (this) {
					traceBuffer = this.traceId2trace.get(traceId);
					if (traceBuffer == null) { // NOCS (DCL)
						traceBuffer = new TraceBuffer();
						this.traceId2trace.put(traceId, traceBuffer);
					}
				}
			}
			traceBuffer.insertEvent((AbstractTraceEvent) record);
			loggingTimestamp = ((AbstractTraceEvent) record).getTimestamp();
		} else {
			return; // invalid type which should not happen due to the specified eventTypes
		}
		if (traceBuffer.isFinished()) {
			synchronized (this) { // has to be synchronized because of timeout cleanup
				this.traceId2trace.remove(traceId);
			}
			super.deliver(OUTPUT_PORT_NAME_TRACE_VALID, traceBuffer.toTraceEvents());
		}
		if (this.timeout) {
			synchronized (this) {
				// can we assume a rough order of logging timestamps? (yes, except with DB reader)
				if (loggingTimestamp > this.maxEncounteredLoggingTimestamp) {
					this.maxEncounteredLoggingTimestamp = loggingTimestamp;
				}
				this.processTimeoutQueue(this.maxEncounteredLoggingTimestamp);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void terminate(final boolean error) {
		synchronized (this) {
			for (final Entry<Long, TraceBuffer> entry : this.traceId2trace.entrySet()) {
				final TraceBuffer traceBuffer = entry.getValue();
				if (traceBuffer.isInvalid()) {
					super.deliver(OUTPUT_PORT_NAME_TRACE_INVALID, traceBuffer.toTraceEvents());
				} else {
					super.deliver(OUTPUT_PORT_NAME_TRACE_VALID, traceBuffer.toTraceEvents());
				}
			}
			this.traceId2trace.clear();
		}
	}

	// only called within synchronized! We assume timestamps >= 0
	private void processTimeoutQueue(final long timestamp) {
		final long duration = timestamp - this.maxTraceDuration;
		final long traceTimeout = timestamp - this.maxTraceTimeout;
		for (final Iterator<Entry<Long, TraceBuffer>> iterator = this.traceId2trace.entrySet().iterator(); iterator.hasNext();) {
			final TraceBuffer traceBuffer = iterator.next().getValue();
			if ((traceBuffer.getMaxLoggingTimestamp() <= traceTimeout) // long time no see
					|| (traceBuffer.getMinLoggingTimestamp() <= duration)) { // max duration is gone
				if (traceBuffer.isInvalid()) {
					super.deliver(OUTPUT_PORT_NAME_TRACE_INVALID, traceBuffer.toTraceEvents());
				} else {
					super.deliver(OUTPUT_PORT_NAME_TRACE_VALID, traceBuffer.toTraceEvents());
				}
				iterator.remove();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Configuration getCurrentConfiguration() {
		final Configuration configuration = new Configuration();
		configuration.setProperty(CONFIG_PROPERTY_NAME_TIMEUNIT, this.timeunit.name());
		configuration.setProperty(CONFIG_PROPERTY_NAME_MAX_TRACE_DURATION, String.valueOf(this.maxTraceDuration));
		configuration.setProperty(CONFIG_PROPERTY_NAME_MAX_TRACE_TIMEOUT, String.valueOf(this.maxTraceTimeout));
		return configuration;
	}

}