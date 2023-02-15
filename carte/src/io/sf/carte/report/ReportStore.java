/*

 Copyright (c) 2020-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.report;

import java.io.IOException;
import java.io.OutputStream;

import io.sf.carte.chart.ChartInfo;
import io.sf.carte.doc.dom.DOMDocument;
import io.sf.carte.doc.dom.DOMElement;

/**
 * <p>
 * WARNING: This code is nowhere near being complete nor API-stable. Use it at
 * your own risk, and contributions would be welcome.
 * </p>
 */
public interface ReportStore {

	default void init(DOMElement configRoot) throws ReportException, IOException {
	}

	/**
	 * Store a report.
	 * <p>
	 * This method may vary its semantics depending on the implementation. Most
	 * implementations shall store a SVG {@code document} in certain facility, while
	 * in other cases the {@code document} will be the medium.
	 * </p>
	 * 
	 * @param chartInfo the chart information and data.
	 * @param document  the document to store (or to store the data in).
	 * @throws ReportException
	 * @throws IOException
	 */
	void store(ChartInfo chartInfo, DOMDocument document) throws ReportException, IOException;

	default OutputStream getOutputStream(String uri) throws ReportException, IOException {
		throw new UnsupportedOperationException();
	}

}
