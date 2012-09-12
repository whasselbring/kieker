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

package kieker.tools.traceAnalysis.repository;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import kieker.analysis.repository.AbstractRepository;
import kieker.analysis.repository.annotation.Repository;
import kieker.common.configuration.Configuration;

/**
 * Implementation of a description repository which stores descriptions for names.
 * 
 * @author Holger Knoche
 * 
 */
@Repository(name = "Description repository",
		description = "Stores descriptions for names")
public class DescriptionRepository extends AbstractRepository {

	private static final char DELIMITER = '=';

	private static final String ENCODING = "UTF-8";

	private final Map<String, String> descriptionMap;

	private static String[] splitLine(final String inputLine) {
		final int delimiterIndex = inputLine.indexOf(DELIMITER);
		if (delimiterIndex < 0) {
			return null;
		}

		final String[] returnValue = new String[2];
		returnValue[0] = inputLine.substring(0, delimiterIndex);
		returnValue[1] = inputLine.substring(delimiterIndex + 1);

		return returnValue;
	}

	public static DescriptionRepository createFromFile(final String fileName) throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), ENCODING));
			final Map<String, String> descriptionMap = new HashMap<String, String>(); // NOPMD ( UseConcurrentHashMap), returned as Collections.unmodifiableMap

			while (true) {
				final String currentLine = reader.readLine();
				if (currentLine == null) {
					break;
				}

				final String[] parts = DescriptionRepository.splitLine(currentLine);
				if (parts == null) {
					continue;
				}

				final String key = parts[0];
				final String description = parts[1];
				descriptionMap.put(key, description);
			}
			return new DescriptionRepository(new Configuration(), descriptionMap);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	public DescriptionRepository(final Configuration configuration, final Map<String, String> descriptionMap) {
		super(configuration);

		this.descriptionMap = descriptionMap;
	}

	public Configuration getCurrentConfiguration() {
		return this.configuration;
	}

	public Map<String, String> getDescriptionMap() {
		return Collections.unmodifiableMap(this.descriptionMap);
	}

}