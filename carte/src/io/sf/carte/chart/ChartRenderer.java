/*

 Copyright (c) 2020-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.chart;

import java.io.IOException;

import io.sf.carte.doc.dom.DOMElement;
import io.sf.carte.report.ReportException;
import io.sf.carte.report.ReportStore;

/**
 * Chart renderer.
 * <p>
 * WARNING: This code is nowhere near being complete nor API-stable. Use it at
 * your own risk, and contributions would be welcome.
 * </p>
 * @param <T> the type of chart metadata.
 */
public interface ChartRenderer<T extends ChartInfo> {

	default void init(DOMElement configRoot) throws ReportException {
	}

	void writeChart(T chartInfo, Iterable<ReportStore> storeIt) throws ReportException, IOException;

}
