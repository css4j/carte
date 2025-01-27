/*

 Copyright (c) 2020-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.chart;

import java.io.IOException;

import io.sf.carte.report.ReportException;
import io.sf.carte.report.ReportStore;

/**
 * Subclasses featuring different chart libraries could implement this.
 * <p>
 * WARNING: This code is nowhere near being complete nor API-stable. Use it at
 * your own risk, and contributions would be welcome.
 * </p>
 */
public interface BarChartRenderer extends ChartRenderer<BarChartInfo> {

	@Override
	void writeChart(BarChartInfo chartInfo, Iterable<ReportStore> storeIt) throws ReportException, IOException;

}
