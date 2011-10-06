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

package kieker.monitoring.probe.cxf;

import javax.xml.namespace.QName;

/**
 * Constants for the sessionIdentifier soap header.
 */

/**
 * @author Dennis Kieselhorst
 */
public interface SOAPHeaderConstants { // NOCS
	public static final String NAMESPACE_URI = "http://kieker.sf.net";
	public static final String SESSION_QUALIFIED_NAME = "sessionId";
	public static final String TRACE_QUALIFIED_NAME = "traceId";
	public static final String EOI_QUALIFIED_NAME = "eoi";
	public static final String ESS_QUALIFIED_NAME = "ess";
	public static final QName SESSION_IDENTIFIER_QNAME = new QName(SOAPHeaderConstants.NAMESPACE_URI, SOAPHeaderConstants.SESSION_QUALIFIED_NAME);
	public static final QName TRACE_IDENTIFIER_QNAME = new QName(SOAPHeaderConstants.NAMESPACE_URI, SOAPHeaderConstants.TRACE_QUALIFIED_NAME);
	public static final QName EOI_IDENTIFIER_QNAME = new QName(SOAPHeaderConstants.NAMESPACE_URI, SOAPHeaderConstants.EOI_QUALIFIED_NAME);
	public static final QName ESS_IDENTIFIER_QNAME = new QName(SOAPHeaderConstants.NAMESPACE_URI, SOAPHeaderConstants.ESS_QUALIFIED_NAME);
}
