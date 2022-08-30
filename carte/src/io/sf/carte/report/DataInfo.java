/*

 Copyright (c) 2020-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.report;

/**
 * Chart data information.
 * <p>
 * WARNING: This code is nowhere near being complete nor API-stable. Use it at
 * your own risk, and contributions would be welcome.
 * </p>
 */
public class DataInfo {

	private double maxValue;

	private double minValue;

	private double minError;

	private int dataCount = 0;

	public DataInfo() {
		super();
	}

	public DataInfo(double minValue, double maxValue, int dataCount) {
		super();
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.dataCount = dataCount;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public double getMinError() {
		return minError;
	}

	public void setMinError(double minError) {
		this.minError = minError;
	}

	public int getCount() {
		return dataCount;
	}

	public void setCount(int dataCount) {
		this.dataCount = dataCount;
	}

}
