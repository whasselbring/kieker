/***************************************************************************
 * Copyright 2012 by
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

package kieker.tools.traceAnalysis.filter.visualization;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.annotation.Property;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.ComponentAllocationDependencyGraph;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.ComponentAllocationDependencyGraphFormatter;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.ComponentAssemblyDependencyGraph;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.ComponentAssemblyDependencyGraphFormatter;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.ContainerDependencyGraph;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.ContainerDependencyGraphFormatter;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.OperationAllocationDependencyGraph;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.OperationAllocationDependencyGraphFormatter;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.OperationAssemblyDependencyGraph;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.OperationAssemblyDependencyGraphFormatter;
import kieker.tools.traceAnalysis.filter.visualization.exception.GraphFormattingException;
import kieker.tools.traceAnalysis.filter.visualization.graph.AbstractGraph;

/**
 * Generic graph writer plugin to generate graph specifications and save them to disk. This plugin uses
 * a formatter registry (see {@link #FORMATTER_REGISTRY}) to determine the appropriate formatter for a
 * given graph.
 * 
 * @author Holger Knoche
 * 
 */
@Plugin(name = "Graph writer plugin",
		description = "Generic plugin for writing graphs to files",
		configuration = {
			@Property(name = GraphWriterConfiguration.CONFIG_PROPERTY_NAME_INCLUDE_WEIGHTS, defaultValue = "true"),
			@Property(name = GraphWriterConfiguration.CONFIG_PROPERTY_NAME_SHORTLABELS, defaultValue = "true"),
			@Property(name = GraphWriterConfiguration.CONFIG_PROPERTY_NAME_SELFLOOPS, defaultValue = "false")
		})
public class GraphWriterPlugin extends AbstractFilterPlugin {
	private static final Log LOG = LogFactory.getLog(GraphWriterPlugin.class);

	private final GraphWriterConfiguration gConfiguration;

	private static final String ENCODING = "UTF-8";

	/**
	 * Name of the plugin's graph input port.
	 */
	public static final String INPUT_PORT_NAME_GRAPHS = "inputGraph";

	private static final String NO_SUITABLE_FORMATTER_MESSAGE_TEMPLATE = "No formatter type defined for graph type %s.";
	private static final String INSTANTIATION_ERROR_MESSAGE_TEMPLATE = "Could not instantiate formatter type %s for graph type %s.";
	private static final String WRITE_ERROR_MESSAGE_TEMPLATE = "Graph could not be written to file %s.";

	private static final Map<Class<? extends AbstractGraph<?, ?, ?>>, Class<? extends AbstractGraphFormatter<?>>> FORMATTER_REGISTRY =
			new HashMap<Class<? extends AbstractGraph<?, ?, ?>>, Class<? extends AbstractGraphFormatter<?>>>(); // NOPMD (UseConcurrentHashMap)

	static {
		FORMATTER_REGISTRY.put(ComponentAllocationDependencyGraph.class, ComponentAllocationDependencyGraphFormatter.class);
		FORMATTER_REGISTRY.put(ComponentAssemblyDependencyGraph.class, ComponentAssemblyDependencyGraphFormatter.class);
		FORMATTER_REGISTRY.put(OperationAllocationDependencyGraph.class, OperationAllocationDependencyGraphFormatter.class);
		FORMATTER_REGISTRY.put(OperationAssemblyDependencyGraph.class, OperationAssemblyDependencyGraphFormatter.class);
		FORMATTER_REGISTRY.put(ContainerDependencyGraph.class, ContainerDependencyGraphFormatter.class);
	}

	/**
	 * Creates a new writer plugin using the given configuration.
	 * 
	 * @param configuration
	 *            The configuration to use
	 */
	public GraphWriterPlugin(final Configuration configuration) {
		super(configuration);
		this.gConfiguration = new GraphWriterConfiguration(configuration);
	}

	public Configuration getCurrentConfiguration() {
		return this.gConfiguration.getConfiguration();
	}

	private static void handleInstantiationException(final Class<?> graphClass, final Class<?> formatterClass, final Exception exception) {
		throw new GraphFormattingException(String.format(INSTANTIATION_ERROR_MESSAGE_TEMPLATE, formatterClass.getName(), graphClass.getName()), exception);
	}

	private static AbstractGraphFormatter<?> createFormatter(final AbstractGraph<?, ?, ?> graph) {
		final Class<? extends AbstractGraphFormatter<?>> formatterClass = FORMATTER_REGISTRY.get(graph.getClass());

		if (formatterClass == null) {
			throw new GraphFormattingException(String.format(NO_SUITABLE_FORMATTER_MESSAGE_TEMPLATE, graph.getClass().getName()));
		}

		try {
			final Constructor<? extends AbstractGraphFormatter<?>> constructor = formatterClass.getConstructor();
			return constructor.newInstance();
		} catch (final SecurityException e) {
			GraphWriterPlugin.handleInstantiationException(graph.getClass(), formatterClass, e);
		} catch (final NoSuchMethodException e) {
			GraphWriterPlugin.handleInstantiationException(graph.getClass(), formatterClass, e);
		} catch (final IllegalArgumentException e) {
			GraphWriterPlugin.handleInstantiationException(graph.getClass(), formatterClass, e);
		} catch (final InstantiationException e) {
			GraphWriterPlugin.handleInstantiationException(graph.getClass(), formatterClass, e);
		} catch (final IllegalAccessException e) {
			GraphWriterPlugin.handleInstantiationException(graph.getClass(), formatterClass, e);
		} catch (final InvocationTargetException e) {
			GraphWriterPlugin.handleInstantiationException(graph.getClass(), formatterClass, e);
		}

		// This should never happen, because all catch clauses indirectly throw exceptions
		return null;
	}

	private String getOutputFileName(final AbstractGraphFormatter<?> formatter) {
		String outputFileName = this.gConfiguration.getOutputFileName();

		if ((outputFileName == null) || (outputFileName.trim().length() == 0)) { // NOPMD(InefficientEmptyStringCheck) // does the job
			outputFileName = formatter.getDefaultFileName();
		}

		return outputFileName;
	}

	/**
	 * Formats a given graph and saves the generated specification to disk. The file name to save the
	 * output to is specified by the configuration option {@link #CONFIG_PROPERTY_NAME_OUTPUT_FILE_NAME}.
	 * 
	 * @param graph
	 *            The graph to save
	 */
	@InputPort(name = INPUT_PORT_NAME_GRAPHS, eventTypes = { AbstractGraph.class })
	public void writeGraph(final AbstractGraph<?, ?, ?> graph) {
		final AbstractGraphFormatter<?> graphFormatter = GraphWriterPlugin.createFormatter(graph);

		final String specification = graphFormatter.createFormattedRepresentation(graph, this.gConfiguration);
		final String outputFileName = this.gConfiguration.getOutputPath() + this.getOutputFileName(graphFormatter);

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileName), ENCODING));
			writer.write(specification);
			writer.flush();
		} catch (final IOException e) {
			throw new GraphFormattingException(String.format(WRITE_ERROR_MESSAGE_TEMPLATE, outputFileName), e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (final IOException e) {
					LOG.error(String.format(WRITE_ERROR_MESSAGE_TEMPLATE, outputFileName), e);
				}
			}
		}
	}

}