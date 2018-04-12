/***************************************************************************
 * Copyright 2017 Kieker Project (http://kieker-monitoring.net)
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

package kieker.analysis.plugin.reader.newio.deserializer;

import java.util.List;

import kieker.common.registry.reader.ReaderRegistry;

/**
 * Rudimentary string registry for use by the binary format decoder.
 *
 * @author Holger Knoche
 *
 * @since 1.13
 */
public class DeserializerStringRegistry extends ReaderRegistry<String> {

	/**
	 * Creates a new deserializer string registry.
	 * @param values The values to use
	 */
	public DeserializerStringRegistry(final List<String> values) {
		super();
		long key = 0;
		for (final String value : values) {
			this.register(key++, value);
		}
	}

	public String get(final int i) {
		return this.get(i);
	}

}
