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

package kieker.test.analysis.junit.plugin.filter.flow;

import org.junit.Assert;
import org.junit.Test;

import kieker.analysis.AnalysisController;
import kieker.analysis.exception.AnalysisConfigurationException;
import kieker.analysis.plugin.filter.flow.EventRecordTraceReconstructionFilter;
import kieker.analysis.plugin.filter.flow.TraceEventRecords;
import kieker.common.configuration.Configuration;
import kieker.common.record.flow.trace.AbstractTraceEvent;

import kieker.test.analysis.util.plugin.filter.SimpleSinkFilter;
import kieker.test.analysis.util.plugin.filter.flow.BookstoreEventRecordFactory;

/**
 * @author Andre van Hoorn, Jan Waller
 */
public class TestEventRecordTraceReconstructionFilter { // NOCS (test class without constructor)

	private static final String SESSION_ID = "8yWpCvrJ2";
	private static final String HOSTNAME = "srv55";
	private static final long TRACE_ID = 978668L;
	private static final long START_TIME = 86756587L;

	/**
	 * Creates an {@link EventRecordTraceGenerationFilter} with the given parameter.
	 * 
	 * @param maxTraceDuration
	 * @param maxTraceTimeout
	 * @return
	 * @throws AnalysisConfigurationException
	 * @throws IllegalStateException
	 */
	private void runTest(final TraceEventRecords records, final long maxTraceDuration, final long maxTraceTimeout)
			throws IllegalStateException, AnalysisConfigurationException {
		final AnalysisController controller = new AnalysisController();

		final Configuration configuration = new Configuration();
		configuration.setProperty(EventRecordTraceReconstructionFilter.CONFIG_PROPERTY_NAME_MAX_TRACE_DURATION, Long.toString(maxTraceDuration));
		configuration.setProperty(EventRecordTraceReconstructionFilter.CONFIG_PROPERTY_NAME_MAX_TRACE_TIMEOUT, Long.toString(maxTraceTimeout));
		final EventRecordTraceReconstructionFilter traceFilter = new EventRecordTraceReconstructionFilter(configuration);

		final SimpleSinkFilter<TraceEventRecords> sinkPlugin = new SimpleSinkFilter<TraceEventRecords>(new Configuration());
		Assert.assertTrue(sinkPlugin.getList().isEmpty());

		controller.registerFilter(traceFilter);
		controller.registerFilter(sinkPlugin);
		controller.connect(traceFilter, EventRecordTraceReconstructionFilter.OUTPUT_PORT_NAME_TRACE_VALID, sinkPlugin, SimpleSinkFilter.INPUT_PORT_NAME);

		traceFilter.newEvent(records.getTrace());
		for (final AbstractTraceEvent e : records.getTraceEvents()) {
			traceFilter.newEvent(e);
		}
		traceFilter.terminate(false); // terminate w/o error; otherwise end of trace might not be triggered

		// Make sure that 1 trace generated
		Assert.assertEquals("No trace passed filter", 1, sinkPlugin.getList().size());
		Assert.assertEquals(records, sinkPlugin.getList().get(0));
	}

	private void runTestFailed(final TraceEventRecords records, final long maxTraceDuration, final long maxTraceTimeout)
			throws IllegalStateException, AnalysisConfigurationException {
		final AnalysisController controller = new AnalysisController();

		final Configuration configuration = new Configuration();
		configuration.setProperty(EventRecordTraceReconstructionFilter.CONFIG_PROPERTY_NAME_MAX_TRACE_DURATION, Long.toString(maxTraceDuration));
		configuration.setProperty(EventRecordTraceReconstructionFilter.CONFIG_PROPERTY_NAME_MAX_TRACE_TIMEOUT, Long.toString(maxTraceTimeout));
		final EventRecordTraceReconstructionFilter traceFilter = new EventRecordTraceReconstructionFilter(configuration);

		final SimpleSinkFilter<TraceEventRecords> sinkPlugin = new SimpleSinkFilter<TraceEventRecords>(new Configuration());
		Assert.assertTrue(sinkPlugin.getList().isEmpty());

		controller.registerFilter(traceFilter);
		controller.registerFilter(sinkPlugin);
		controller.connect(traceFilter, EventRecordTraceReconstructionFilter.OUTPUT_PORT_NAME_TRACE_VALID, sinkPlugin, SimpleSinkFilter.INPUT_PORT_NAME);

		traceFilter.newEvent(records.getTrace());
		for (final AbstractTraceEvent e : records.getTraceEvents()) {
			traceFilter.newEvent(e);
		}
		traceFilter.terminate(false); // terminate w/o error; otherwise end of trace might not be triggered

		// Make sure that 1 trace generated
		Assert.assertEquals("There should be no trace", 0, sinkPlugin.getList().size());
	}

	@Test
	public void testTraceMaxLong() throws IllegalStateException, AnalysisConfigurationException {
		final TraceEventRecords bookstoreTrace = BookstoreEventRecordFactory.validSyncTraceBeforeAfterEvents(START_TIME, TRACE_ID, SESSION_ID, HOSTNAME);
		Assert.assertEquals("Test invalid", START_TIME, bookstoreTrace.getTraceEvents()[0].getTimestamp());
		this.runTest(bookstoreTrace, Long.MAX_VALUE, Long.MAX_VALUE);
	}

	@Test
	public void testTraceShorterThanMaxDurationPasses() throws IllegalStateException, AnalysisConfigurationException {
		final TraceEventRecords bookstoreTrace = BookstoreEventRecordFactory.validSyncTraceBeforeAfterEvents(START_TIME, TRACE_ID, SESSION_ID, HOSTNAME);
		Assert.assertEquals("Test invalid", START_TIME, bookstoreTrace.getTraceEvents()[0].getTimestamp());
		final long traceDuration = bookstoreTrace.getTraceEvents()[bookstoreTrace.getTraceEvents().length - 1].getTimestamp() - START_TIME;
		this.runTest(bookstoreTrace, traceDuration + 1, Long.MAX_VALUE);
	}

	@Test
	public void testTraceShorterThanMaxTimeoutPasses() throws IllegalStateException, AnalysisConfigurationException {
		final TraceEventRecords bookstoreTrace = BookstoreEventRecordFactory.validSyncTraceBeforeAfterEvents(START_TIME, TRACE_ID, SESSION_ID, HOSTNAME);
		Assert.assertEquals("Test invalid", START_TIME, bookstoreTrace.getTraceEvents()[0].getTimestamp());
		this.runTest(bookstoreTrace, Long.MAX_VALUE, 100);
	}

	@Test
	public void testTraceLongerThanMaxDurationPasses() throws IllegalStateException, AnalysisConfigurationException {
		final TraceEventRecords bookstoreTrace = BookstoreEventRecordFactory.validSyncTraceBeforeAfterEvents(START_TIME, TRACE_ID, SESSION_ID, HOSTNAME);
		Assert.assertEquals("Test invalid", START_TIME, bookstoreTrace.getTraceEvents()[0].getTimestamp());
		final long traceDuration = bookstoreTrace.getTraceEvents()[bookstoreTrace.getTraceEvents().length - 1].getTimestamp() - START_TIME;
		this.runTestFailed(bookstoreTrace, traceDuration - 5, Long.MAX_VALUE);
	}

	// TODO: Timeout can only happen if more than one trace is sent simultaneously
	// @Test
	// public void testTraceLongerThanMaxTimeoutPasses() throws IllegalStateException, AnalysisConfigurationException {
	// final TraceEventRecords bookstoreTrace = BookstoreEventRecordFactory.validSyncTraceAdditionalCallEventsGap(START_TIME, TRACE_ID, SESSION_ID, HOSTNAME);
	// Assert.assertEquals("Test invalid", START_TIME, bookstoreTrace.getTraceEvents()[0].getTimestamp());
	// this.runTestFailed(bookstoreTrace, Long.MAX_VALUE, 1);
	// }
}