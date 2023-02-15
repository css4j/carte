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
import java.util.Iterator;
import java.util.Random;

import javax.swing.BorderFactory;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import io.sf.carte.chart.AbstractChartRenderer;
import io.sf.carte.chart.BarChartInfo;
import io.sf.carte.chart.BarChartRenderer;
import io.sf.carte.report.DiscreteDataList;
import io.sf.carte.report.Measurement;
import io.sf.carte.report.ReportException;
import io.sf.carte.report.ReportStore;

/**
 * Implement ChartRenderer with JFreeChart.
 * <p>
 * WARNING: This code is nowhere near being complete nor API-stable. Use it at
 * your own risk, and contributions would be welcome.
 * </p>
 */
public class JFChartRenderer extends AbstractChartRenderer<BarChartInfo> implements BarChartRenderer {

	public JFChartRenderer() {
		super();
	}

	@Override
	public void writeChart(BarChartInfo chartInfo, Iterable<ReportStore> storeIt) throws ReportException, IOException {
		// Dataset
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		DiscreteDataList data = chartInfo.getMeasurements();
		String subcaption = innertText(chartInfo.getSubcaption());
		Iterator<Measurement> mit = data.iterator();
		while (mit.hasNext()) {
			Measurement me = mit.next();
			if (chartInfo.getUnitLegend() == null) {
				throw new RuntimeException();
			}
			dataset.addValue(me.getValue(), subcaption, me.getLabelParent().getTextContent().trim());
			// errData.add(me.getScoreError());
		}
		String caption = innertText(chartInfo.getCaption());
		JFreeChart chart = ChartFactory.createBarChart(caption, chartInfo.getXAxisTitle(),
				chartInfo.getUnitLegend(), dataset, PlotOrientation.VERTICAL, true, true, false);
		CategoryPlot plot = chart.getCategoryPlot();
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		Random rand = null;
		int sz = data.computeDataInfo().getCount();
		for (int i = 0; i < sz; i++) {
			String colorStr = data.get(i).getColor();
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
			renderer.setSeriesPaint(i, color);
		}
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		chartPanel.setBackground(Color.decode("#cde1ff"));
		// chartPanel.setSize(700, 500);
		String id = chartInfo.getId();
		for (ReportStore store : storeIt) {
			OutputStream os = store.getOutputStream("jfree-" + id + ".png");
			ChartUtils.writeChartAsPNG(os, chart, 700, 500);
		}
	}

}
