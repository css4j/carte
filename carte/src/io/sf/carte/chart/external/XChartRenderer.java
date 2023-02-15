/*

 Copyright (c) 2020-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.chart.external;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler.LegendPosition;

import io.sf.carte.chart.AbstractChartRenderer;
import io.sf.carte.chart.BarChartInfo;
import io.sf.carte.chart.BarChartRenderer;
import io.sf.carte.report.Measurement;
import io.sf.carte.report.ReportException;
import io.sf.carte.report.ReportStore;

/**
 * Implement ChartRenderer with XChart.
 * <p>
 * WARNING: This code is nowhere near being complete nor API-stable. Use it at
 * your own risk, and contributions would be welcome.
 * </p>
 */
public class XChartRenderer extends AbstractChartRenderer<BarChartInfo> implements BarChartRenderer {

	public XChartRenderer() {
		super();
	}

	@Override
	public void writeChart(BarChartInfo chartInfo, Iterable<ReportStore> storeIt) throws ReportException, IOException {
		// xchart chart
		CategoryChartBuilder chartBuilder = new CategoryChartBuilder();
		chartBuilder.width(700);
		chartBuilder.height(500);
		String caption = innertText(chartInfo.getCaption());
		chartBuilder.title(caption);
		chartBuilder.xAxisTitle(chartInfo.getXAxisTitle());
		chartBuilder.yAxisTitle(chartInfo.getUnitLegend());
		CategoryChart chart = chartBuilder.build();
		chart.getStyler().setLegendPosition(LegendPosition.InsideN);
		chart.getStyler().setLabelsVisible(false);
		int sz = chartInfo.getMeasurements().computeDataInfo().getCount();
		List<String> xData = new ArrayList<>(sz);
		List<Double> yData = new ArrayList<>(sz);
		List<Double> errData = new ArrayList<>(sz);
		Color[] xColors = new Color[sz];
		Random rand = null;
		int idx = 0;
		Iterator<Measurement> mit = chartInfo.getMeasurements().iterator();
		while (mit.hasNext()) {
			Measurement me = mit.next();
			xData.add(me.getLabelParent().getTextContent().trim());
			yData.add(me.getValue());
			errData.add(me.getValueError());
			String colorStr = me.getColor();
			Color color;
			if (colorStr != null) {
				color = Color.decode(colorStr);
			} else {
				if (rand == null) {
					rand = new Random();
				}
				float r = rand.nextFloat();
				float g = rand.nextFloat();
				float b = rand.nextFloat();
				color = new Color(r, g, b);
			}
			xColors[idx] = color;
			idx++;
		}
		String subcaption = innertText(chartInfo.getSubcaption());
		chart.addSeries(subcaption, xData, yData, errData);
		chart.getStyler().setSeriesColors(xColors);
		chart.getStyler().setLocale(Locale.US);
		chart.getStyler().setPlotBackgroundColor(Color.decode("#cde1ff"));
		chart.getStyler().setChartBackgroundColor(Color.decode("#cde1ff"));
		String id = chartInfo.getId();
		for (ReportStore store : storeIt) {
			OutputStream os = store.getOutputStream("xchart-" + id + ".png");
			BitmapEncoder.saveBitmap(chart, os, BitmapFormat.PNG);
			os.close();
		}
	}

}
