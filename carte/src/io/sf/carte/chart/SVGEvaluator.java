/*

 Copyright (c) 2020-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.chart;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.property.PercentageEvaluator;

class SVGEvaluator extends PercentageEvaluator {

	private final double viewportPx;

	SVGEvaluator(double viewportPx) {
		super();
		this.viewportPx = viewportPx;
	}

	@Override
	protected float percentage(CSSTypedValue value, short resultType) throws DOMException {
		return value.getFloatValue(resultType) * (float) viewportPx;
	}

}
