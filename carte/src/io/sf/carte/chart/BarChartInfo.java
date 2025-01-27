/*

 Copyright (c) 2020-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.chart;

import io.sf.carte.report.DiscreteDataList;
import io.sf.carte.report.ReportConfigurationException;
import io.sf.carte.report.ReportDataException;

/**
 * Encapsulation of bar chart information (including data).
 * <p>
 * WARNING: This code is nowhere near being complete nor API-stable. Use it at
 * your own risk, and contributions would be welcome.
 * </p>
 */
public class BarChartInfo extends ChartInfo {

	private String xAxisTitle = null;

	private String measurementUnit = null;

	private String unitLegend = null;

	private DiscreteDataList measurements = null;

	public BarChartInfo(String id) {
		super(id);
	}

	public String getXAxisTitle() {
		return xAxisTitle;
	}

	public void setXAxisTitle(String xAxisTitle) {
		this.xAxisTitle = xAxisTitle;
	}

	public void setValueUnit(String unit) throws ReportDataException {
		if (unit == null || unit.length() == 0) {
			throw new ReportDataException("No unit.");
		}
		this.measurementUnit = unit;
	}

	public String getValueUnit() {
		return measurementUnit;
	}

	public String getUnitLegend() {
		return unitLegend;
	}

	public void setUnitLegend(String legend) throws ReportConfigurationException {
		if (legend == null || legend.length() == 0) {
			throw new ReportConfigurationException("Empty <unit-title> element.");
		}
		this.unitLegend = legend;
	}

	public DiscreteDataList getMeasurements() {
		return measurements;
	}

	public void setMeasurements(DiscreteDataList measurements) {
		this.measurements = measurements;
	}

}
