/*

 Copyright (c) 2020-2026, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-2-Clause OR BSD-3-Clause

package io.sf.carte.chart;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSNumberValue;
import io.sf.carte.doc.style.css.property.PercentageEvaluator;

class SVGEvaluator extends PercentageEvaluator {

	private final double viewportPx;

	SVGEvaluator(double viewportPx) {
		super();
		this.viewportPx = viewportPx;
	}

	@Override
	protected float percentage(CSSNumberValue value, short resultType) throws DOMException {
		return value.getFloatValue(resultType) * (float) viewportPx;
	}

}
