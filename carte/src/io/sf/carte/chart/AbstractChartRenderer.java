/*

 Copyright (c) 2020-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.chart;

import org.w3c.dom.Node;

import io.sf.carte.doc.dom.DOMElement;
import io.sf.carte.doc.dom.DOMNode;

/**
 * Abstract base class for chart renderers.
 * <p>
 * WARNING: This code is nowhere near being complete nor API-stable. Use it at
 * your own risk, and contributions would be welcome.
 * </p>
 * 
 * @param <T> the type of chart metadata.
 */
abstract public class AbstractChartRenderer<T extends ChartInfo> implements ChartRenderer<T> {

	protected AbstractChartRenderer() {
		super();
	}

	protected static String innertText(DOMNode parent) {
		String rowDesc;
		if (parent.getNodeType() == Node.ELEMENT_NODE) {
			rowDesc = ((DOMElement) parent).getInnerText();
		} else {
			rowDesc = parent.getTextContent();
		}
		return rowDesc.trim();
	}

}
