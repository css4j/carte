/*

 Copyright (c) 2020-2026, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-2-Clause OR BSD-3-Clause

package io.sf.carte.report;

/**
 * A measurement has an identifier string, a value and an error (uncertainty
 * interval would be value ± error).
 * <p>
 * It can have a label and a color assigned.
 * </p>
 */
public class Measurement extends DiscreteValue {

	private double valueError;

	public Measurement(String id, double value, double valueError) {
		super(id, value);
		this.valueError = valueError;
	}

	public double getValueError() {
		return valueError;
	}

	public void setValueError(double valueError) {
		this.valueError = valueError;
	}

}
