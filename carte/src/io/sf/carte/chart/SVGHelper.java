/*

 Copyright (c) 2020-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.chart;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import io.sf.carte.doc.dom.DOMElement;
import io.sf.carte.doc.dom.DOMNode;
import io.sf.carte.doc.geom.Rect;
import io.sf.carte.doc.style.css.CSSExpressionValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueFactory;

class SVGHelper {

	static DOMElement getOwnerSVGElement(DOMElement element) {
		DOMNode parent = element.getParentNode();
		DOMElement owner = null;
		while (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
			DOMElement ancestor = (DOMElement) parent;
			if ("svg".equals(ancestor.getLocalName())) {
				owner = ancestor;
				break;
			}
			parent = parent.getParentNode();
		}
		return owner;
	}

	static double parseLengthToPixels(Parser parser, String value) throws ChartTemplateException {
		ValueFactory vf = new ValueFactory();
		StyleValue cssValue;
		try {
			cssValue = vf.parseProperty(value, parser);
		} catch (DOMException e) {
			throw new ChartTemplateException("Unable to parse value: " + value, e);
		}
		double valuePx;
		CSSValue.Type pType = cssValue.getPrimitiveType();
		if (pType == CSSValue.Type.NUMERIC) {
			CSSTypedValue typed = (CSSTypedValue) cssValue;
			if (typed.getUnitType() == CSSUnit.CSS_NUMBER) {
				valuePx = typed.getFloatValue(CSSUnit.CSS_NUMBER);
			} else {
				try {
					valuePx = typed.getFloatValue(CSSUnit.CSS_PX);
				} catch (DOMException e) {
					throw new ChartTemplateException("Invalid value: " + value, e);
				}
			}
		} else {
			throw new ChartTemplateException("Invalid value: " + value);
		}
		return valuePx;
	}

	static double parseLengthPcntToPixels(Parser parser, String value, double viewportPx)
			throws ChartTemplateException {
		ValueFactory vf = new ValueFactory();
		StyleValue cssValue;
		try {
			cssValue = vf.parseProperty(value, parser);
		} catch (DOMException e) {
			throw new ChartTemplateException("Unable to parse value: " + value, e);
		}
		double valuePx;
		CSSValue.Type pType = cssValue.getPrimitiveType();
		if (pType == CSSValue.Type.EXPRESSION) {
			SVGEvaluator evaluator = new SVGEvaluator(viewportPx);
			CSSTypedValue typed;
			try {
				typed = evaluator.evaluateExpression((CSSExpressionValue) cssValue);
			} catch (DOMException e) {
				throw new ChartTemplateException("Invalid value: " + value, e);
			}
			valuePx = typed.getFloatValue(CSSUnit.CSS_PX);
		} else if (pType == CSSValue.Type.NUMERIC) {
			CSSTypedValue typed = (CSSTypedValue) cssValue;
			if (typed.getUnitType() == CSSUnit.CSS_PERCENTAGE) {
				valuePx = typed.getFloatValue(CSSUnit.CSS_PERCENTAGE) * viewportPx;
			} else if (typed.getUnitType() == CSSUnit.CSS_NUMBER) {
				valuePx = typed.getFloatValue(CSSUnit.CSS_NUMBER);
			} else {
				try {
					valuePx = typed.getFloatValue(CSSUnit.CSS_PX);
				} catch (DOMException e) {
					throw new ChartTemplateException("Invalid value: " + value, e);
				}
			}
		} else {
			throw new ChartTemplateException("Invalid value: " + value);
		}
		return valuePx;
	}

	static Rect parseViewBox(String viewBox) throws ChartTemplateException {
		Rect rect = new Rect();
		StringTokenizer st = new StringTokenizer(viewBox, ", ");
		try {
			double d = parseNextToken(st);
			rect.setX(d);
			d = parseNextToken(st);
			rect.setY(d);
			d = parseNextToken(st);
			rect.setWidth(d);
			d = parseNextToken(st);
			rect.setHeight(d);
		} catch (NoSuchElementException e) {
			throw new ChartTemplateException("Invalid viewBox: " + viewBox);
		} catch (NumberFormatException e) {
			throw new ChartTemplateException("Invalid viewBox: " + viewBox, e);
		}
		return rect;
	}

	private static double parseNextToken(StringTokenizer st) {
		String s = st.nextToken();
		double d = Double.parseDouble(s);
		return d;
	}

}
