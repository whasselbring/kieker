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

package kieker.common.record.flow.trace.operation.constructor.object;

import java.nio.ByteBuffer;

import kieker.common.record.factory.IRecordFactory;
import kieker.common.util.registry.IRegistry;

/**
 * @author Jan Waller
 * 
 * @since 1.6
 */
public final class AfterConstructorObjectEventFactory implements IRecordFactory<AfterConstructorObjectEvent> {
	
	@Override
	public AfterConstructorObjectEvent create(final ByteBuffer buffer, final IRegistry<String> stringRegistry) {
		return new AfterConstructorObjectEvent(buffer, stringRegistry);
	}
	
	@Override
	public AfterConstructorObjectEvent create(final Object[] values) {
		return new AfterConstructorObjectEvent(values);
	}
}