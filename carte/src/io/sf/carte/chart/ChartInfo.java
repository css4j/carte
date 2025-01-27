/*

 Copyright (c) 2020-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

/**
 * Base class for chart data.
 * <p>
 * WARNING: This code is nowhere near being complete nor API-stable. Use it at
 * your own risk, and contributions would be welcome.
 * </p>
 */
package io.sf.carte.chart;

import io.sf.carte.doc.dom.DOMNode;

/**
 * Encapsulation of chart information (including data).
 * <p>
 * WARNING: This code is nowhere near being complete nor API-stable. Use it at
 * your own risk, and contributions would be welcome.
 * </p>
 */
public class ChartInfo {

	protected final String id;

	private DOMNode caption = null;

	private DOMNode subcaption = null;

	public ChartInfo(String id) {
		super();
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public DOMNode getCaption() {
		return caption;
	}

	public void setCaption(DOMNode caption) {
		this.caption = caption;
	}

	public DOMNode getSubcaption() {
		return subcaption;
	}

	public void setSubcaption(DOMNode subcaption) {
		this.subcaption = subcaption;
	}

}
