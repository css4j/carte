/*

 Copyright (c) 2020-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.report;

import io.sf.carte.doc.dom.DOMNode;

/**
 * A discrete value has an identifier string and a value.
 * <p>
 * It can have a label and a color assigned.
 * </p>
 */
public class DiscreteValue {

	private final String id;

	private DOMNode label;

	private String color;

	private double value;

	public DiscreteValue(String id, double value) {
		super();
		this.id = id;
		this.value = value;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		if (color.length() != 0) {
			this.color = color;
		}
	}

	public DOMNode getLabelParent() {
		return label;
	}

	public void setLabelParent(DOMNode labelParent) {
		this.label = labelParent;
	}

	public String getId() {
		return id;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

}
