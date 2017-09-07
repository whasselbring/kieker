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
package kieker.common.record.system;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import kieker.common.record.AbstractMonitoringRecord;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.io.IValueDeserializer;
import kieker.common.record.io.IValueSerializer;
import kieker.common.util.registry.IRegistry;


/**
 * @author Andre van Hoorn, Jan Waller
 * 
 * @since 1.3
 */
public class CPUUtilizationRecord extends AbstractMonitoringRecord implements IMonitoringRecord.Factory, IMonitoringRecord.BinaryFactory {
	private static final long serialVersionUID = -7851990890838902217L;

	/** Descriptive definition of the serialization size of the record. */
	public static final int SIZE = TYPE_SIZE_LONG // CPUUtilizationRecord.timestamp
			 + TYPE_SIZE_STRING // CPUUtilizationRecord.hostname
			 + TYPE_SIZE_STRING // CPUUtilizationRecord.cpuID
			 + TYPE_SIZE_DOUBLE // CPUUtilizationRecord.user
			 + TYPE_SIZE_DOUBLE // CPUUtilizationRecord.system
			 + TYPE_SIZE_DOUBLE // CPUUtilizationRecord.wait
			 + TYPE_SIZE_DOUBLE // CPUUtilizationRecord.nice
			 + TYPE_SIZE_DOUBLE // CPUUtilizationRecord.irq
			 + TYPE_SIZE_DOUBLE // CPUUtilizationRecord.totalUtilization
			 + TYPE_SIZE_DOUBLE // CPUUtilizationRecord.idle
	;
	
	public static final Class<?>[] TYPES = {
		long.class, // CPUUtilizationRecord.timestamp
		String.class, // CPUUtilizationRecord.hostname
		String.class, // CPUUtilizationRecord.cpuID
		double.class, // CPUUtilizationRecord.user
		double.class, // CPUUtilizationRecord.system
		double.class, // CPUUtilizationRecord.wait
		double.class, // CPUUtilizationRecord.nice
		double.class, // CPUUtilizationRecord.irq
		double.class, // CPUUtilizationRecord.totalUtilization
		double.class, // CPUUtilizationRecord.idle
	};
	
	
	/** default constants. */
	public static final long TIMESTAMP = 0L;
	public static final String HOSTNAME = "";
	public static final String CPU_ID = "";
	public static final double USER = 0.0;
	public static final double SYSTEM = 0.0;
	public static final double WAIT = 0.0;
	public static final double NICE = 0.0;
	public static final double IRQ = 0.0;
	public static final double TOTAL_UTILIZATION = 0.0;
	public static final double IDLE = 0.0;
	
	/** property name array. */
	private static final String[] PROPERTY_NAMES = {
		"timestamp",
		"hostname",
		"cpuID",
		"user",
		"system",
		"wait",
		"nice",
		"irq",
		"totalUtilization",
		"idle",
	};
	
	/** property declarations. */
	private long timestamp;
	private String hostname;
	private String cpuID;
	private double user;
	private double system;
	private double wait;
	private double nice;
	private double irq;
	private double totalUtilization;
	private double idle;
	
	/**
	 * Creates a new instance of this class using the given parameters.
	 * 
	 * @param timestamp
	 *            timestamp
	 * @param hostname
	 *            hostname
	 * @param cpuID
	 *            cpuID
	 * @param user
	 *            user
	 * @param system
	 *            system
	 * @param wait
	 *            wait
	 * @param nice
	 *            nice
	 * @param irq
	 *            irq
	 * @param totalUtilization
	 *            totalUtilization
	 * @param idle
	 *            idle
	 */
	public CPUUtilizationRecord(final long timestamp, final String hostname, final String cpuID, final double user, final double system, final double wait, final double nice, final double irq, final double totalUtilization, final double idle) {
		this.timestamp = timestamp;
		this.hostname = hostname == null?HOSTNAME:hostname;
		this.cpuID = cpuID == null?CPU_ID:cpuID;
		this.user = user;
		this.system = system;
		this.wait = wait;
		this.nice = nice;
		this.irq = irq;
		this.totalUtilization = totalUtilization;
		this.idle = idle;
	}

	/**
	 * This constructor converts the given array into a record.
	 * It is recommended to use the array which is the result of a call to {@link #toArray()}.
	 * 
	 * @param values
	 *            The values for the record.
	 *
	 * @deprecated since 1.13. Use {@link #CPUUtilizationRecord(IValueDeserializer)} instead.
	 */
	@Deprecated
	public CPUUtilizationRecord(final Object[] values) { // NOPMD (direct store of values)
		AbstractMonitoringRecord.checkArray(values, TYPES);
		this.timestamp = (Long) values[0];
		this.hostname = (String) values[1];
		this.cpuID = (String) values[2];
		this.user = (Double) values[3];
		this.system = (Double) values[4];
		this.wait = (Double) values[5];
		this.nice = (Double) values[6];
		this.irq = (Double) values[7];
		this.totalUtilization = (Double) values[8];
		this.idle = (Double) values[9];
	}

	/**
	 * This constructor uses the given array to initialize the fields of this record.
	 * 
	 * @param values
	 *            The values for the record.
	 * @param valueTypes
	 *            The types of the elements in the first array.
	 *
	 * @deprecated since 1.13. Use {@link #CPUUtilizationRecord(IValueDeserializer)} instead.
	 */
	@Deprecated
	protected CPUUtilizationRecord(final Object[] values, final Class<?>[] valueTypes) { // NOPMD (values stored directly)
		AbstractMonitoringRecord.checkArray(values, valueTypes);
		this.timestamp = (Long) values[0];
		this.hostname = (String) values[1];
		this.cpuID = (String) values[2];
		this.user = (Double) values[3];
		this.system = (Double) values[4];
		this.wait = (Double) values[5];
		this.nice = (Double) values[6];
		this.irq = (Double) values[7];
		this.totalUtilization = (Double) values[8];
		this.idle = (Double) values[9];
	}

	
	/**
	 * @param deserializer
	 *            The deserializer to use
	 */
	public CPUUtilizationRecord(final IValueDeserializer deserializer) {
		this.timestamp = deserializer.getLong();
		this.hostname = deserializer.getString();
		this.cpuID = deserializer.getString();
		this.user = deserializer.getDouble();
		this.system = deserializer.getDouble();
		this.wait = deserializer.getDouble();
		this.nice = deserializer.getDouble();
		this.irq = deserializer.getDouble();
		this.totalUtilization = deserializer.getDouble();
		this.idle = deserializer.getDouble();
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * @deprecated since 1.13. Use {@link #serialize(IValueSerializer)} with an array serializer instead.
	 */
	@Override
	@Deprecated
	public Object[] toArray() {
		return new Object[] {
			this.getTimestamp(),
			this.getHostname(),
			this.getCpuID(),
			this.getUser(),
			this.getSystem(),
			this.getWait(),
			this.getNice(),
			this.getIrq(),
			this.getTotalUtilization(),
			this.getIdle()
		};
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerStrings(final IRegistry<String> stringRegistry) {	// NOPMD (generated code)
		stringRegistry.get(this.getHostname());
		stringRegistry.get(this.getCpuID());
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serialize(final IValueSerializer serializer) throws BufferOverflowException {
		//super.serialize(serializer);
		serializer.putLong(this.getTimestamp());
		serializer.putString(this.getHostname());
		serializer.putString(this.getCpuID());
		serializer.putDouble(this.getUser());
		serializer.putDouble(this.getSystem());
		serializer.putDouble(this.getWait());
		serializer.putDouble(this.getNice());
		serializer.putDouble(this.getIrq());
		serializer.putDouble(this.getTotalUtilization());
		serializer.putDouble(this.getIdle());
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] getValueTypes() {
		return TYPES; // NOPMD
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getValueNames() {
		return PROPERTY_NAMES; // NOPMD
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSize() {
		return SIZE;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated This record uses the {@link kieker.common.record.IMonitoringRecord.Factory} mechanism. Hence, this method is not implemented.
	 */
	@Override
	@Deprecated
	public void initFromArray(final Object[] values) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (obj.getClass() != this.getClass()) return false;
		
		final CPUUtilizationRecord castedRecord = (CPUUtilizationRecord) obj;
		if (this.getLoggingTimestamp() != castedRecord.getLoggingTimestamp()) return false;
		if (this.getTimestamp() != castedRecord.getTimestamp()) return false;
		if (!this.getHostname().equals(castedRecord.getHostname())) return false;
		if (!this.getCpuID().equals(castedRecord.getCpuID())) return false;
		if (isNotEqual(this.getUser(), castedRecord.getUser())) return false;
		if (isNotEqual(this.getSystem(), castedRecord.getSystem())) return false;
		if (isNotEqual(this.getWait(), castedRecord.getWait())) return false;
		if (isNotEqual(this.getNice(), castedRecord.getNice())) return false;
		if (isNotEqual(this.getIrq(), castedRecord.getIrq())) return false;
		if (isNotEqual(this.getTotalUtilization(), castedRecord.getTotalUtilization())) return false;
		if (isNotEqual(this.getIdle(), castedRecord.getIdle())) return false;
		return true;
	}
	
	public final long getTimestamp() {
		return this.timestamp;
	}
	
	public final void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public final String getHostname() {
		return this.hostname;
	}
	
	public final void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	public final String getCpuID() {
		return this.cpuID;
	}
	
	public final void setCpuID(String cpuID) {
		this.cpuID = cpuID;
	}
	
	public final double getUser() {
		return this.user;
	}
	
	public final void setUser(double user) {
		this.user = user;
	}
	
	public final double getSystem() {
		return this.system;
	}
	
	public final void setSystem(double system) {
		this.system = system;
	}
	
	public final double getWait() {
		return this.wait;
	}
	
	public final void setWait(double wait) {
		this.wait = wait;
	}
	
	public final double getNice() {
		return this.nice;
	}
	
	public final void setNice(double nice) {
		this.nice = nice;
	}
	
	public final double getIrq() {
		return this.irq;
	}
	
	public final void setIrq(double irq) {
		this.irq = irq;
	}
	
	public final double getTotalUtilization() {
		return this.totalUtilization;
	}
	
	public final void setTotalUtilization(double totalUtilization) {
		this.totalUtilization = totalUtilization;
	}
	
	public final double getIdle() {
		return this.idle;
	}
	
	public final void setIdle(double idle) {
		this.idle = idle;
	}
}
