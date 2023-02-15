/*

 Copyright (c) 2020-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.chart;

import static org.junit.Assert.*;

import org.junit.Test;

import io.sf.carte.doc.dom.CSSDOMImplementation;
import io.sf.carte.doc.dom.DOMDocument;
import io.sf.carte.doc.dom.DOMElement;

public class SVGHelperTest {

	@Test
	public void testGetOwnerSVGElement() {
		CSSDOMImplementation domImpl = new CSSDOMImplementation();
		DOMDocument document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
		DOMElement docElement = document.getDocumentElement();
		assertEquals("svg", docElement.getLocalName());
		DOMElement innerSVG = document.createElementNS("http://www.w3.org/2000/svg", "svg");
		docElement.appendChild(innerSVG);
		DOMElement element = document.createElementNS("http://www.w3.org/2000/svg", "rect");
		innerSVG.appendChild(element);
		assertSame(innerSVG, SVGHelper.getOwnerSVGElement(element));
	}

}
