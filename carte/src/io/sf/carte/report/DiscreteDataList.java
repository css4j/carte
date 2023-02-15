/*

 Copyright (c) 2020-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.report;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Encapsulation of bar chart data.
 * <p>
 * WARNING: This code is nowhere near being complete nor API-stable. Use it at
 * your own risk, and contributions would be welcome.
 * </p>
 */
public class DiscreteDataList implements DataSerie<Measurement> {

	private final List<Measurement> measurements;

	public DiscreteDataList() {
		super();
		measurements = new ArrayList<>();
	}

	public DiscreteDataList(int initialSize) {
		super();
		measurements = new ArrayList<>(initialSize);
	}

	public Measurement add(String itemId, double value, double valueError) throws ReportDataException {
		Measurement m = new Measurement(itemId, value, valueError);
		measurements.add(m);
		return m;
	}

	@Override
	public Measurement get(int index) {
		return measurements.get(index);
	}

	@Override
	public DataInfo computeDataInfo() {
		// Determine maximum & minimum value
		double maxValue = -Double.MAX_VALUE;
		double minValue = Double.MAX_VALUE;
		double minError = Double.MAX_VALUE;
		Iterator<Measurement> mit = iterator();
		while (mit.hasNext()) {
			Measurement me = mit.next();
			double errVal = me.getValueError();
			double value = me.getValue() + errVal;
			if (value > maxValue) {
				maxValue = value;
			}
			if (value < minValue) {
				minValue = value;
			}
			if (errVal < minError) {
				minError = errVal;
			}
		}
		DataInfo info = new DataInfo(minValue, maxValue, measurements.size());
		info.setMinError(minError);
		return info;
	}

	@Override
	public Iterator<Measurement> iterator() {
		return measurements.iterator();
	}

}
