/*

 Copyright (c) 2020-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.chart;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;

import org.w3c.dom.Node;
import org.w3c.dom.Text;

import io.sf.carte.doc.dom.DOMDocument;
import io.sf.carte.doc.dom.DOMElement;
import io.sf.carte.doc.dom.DOMNode;
import io.sf.carte.doc.geom.Rect;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.report.DataInfo;
import io.sf.carte.report.DataSerie;
import io.sf.carte.report.Measurement;

/**
 * Implement BarChartRenderer with SVG documents.
 * <p>
 * WARNING: This code is nowhere near being complete nor API-stable. Use it at
 * your own risk, and contributions would be welcome.
 * </p>
 */
public class SVGBarChartRenderer extends SVGChartRenderer<BarChartInfo> implements BarChartRenderer {

	private static final String SVG_TEMPLATE = "barchart.svg";

	public SVGBarChartRenderer() {
		super();
	}

	@Override
	protected String getDefaultTemplate() {
		return SVG_TEMPLATE;
	}

	@Override
	protected void setAxisTitles(DOMDocument svgChart, BarChartInfo chartInfo, double vwWidthPx, double vwHeightPx,
			Rect viewBox) {
		DOMElement element = svgChart.getElementById("y-title");
		element.setTextContent(chartInfo.getUnitLegend());
		element = svgChart.getElementById("x-title");
		element.setTextContent(chartInfo.getXAxisTitle());
	}

	@Override
	protected void plotGraph(DOMDocument svgChart, DOMElement svgElm, BarChartInfo chartInfo, double vwWidthPx,
			double vwHeightPx, Parser parser) throws ChartTemplateException {
		// Prepare the scale
		DOMElement element = svgChart.getElementById("chartbox");
		String s = element.getAttribute("x");
		double containerX0 = SVGHelper.parseLengthPcntToPixels(parser, s, vwWidthPx);
		s = element.getAttribute("y");
		double containerY0 = SVGHelper.parseLengthPcntToPixels(parser, s, vwHeightPx);
		s = element.getAttribute("height");
		double containerHeight = SVGHelper.parseLengthPcntToPixels(parser, s, vwHeightPx);
		s = element.getAttribute("width");
		double containerWidth = SVGHelper.parseLengthPcntToPixels(parser, s, vwWidthPx);
		Rect container = new Rect(containerX0, containerY0, containerWidth, containerHeight);
		// Global quantities
		DataInfo dataInfo = chartInfo.getMeasurements().computeDataInfo();
		double scaleMin;
		if (dataInfo.getMinValue() >= 0) {
			scaleMin = 0d;
		} else {
			double magnitudeFactor = Math.pow(10d,
					Math.round(Math.log10(dataInfo.getMaxValue() - dataInfo.getMinValue())) - 1) * 2;
			scaleMin = Math.floor(dataInfo.getMinValue() * 1.01d / magnitudeFactor) * magnitudeFactor;
		}
		// Print Y scale
		double magnitudeFactor = Math.pow(10d, Math.round(Math.log10(dataInfo.getMaxValue() - scaleMin)) - 2);
		double scaleMax = Math.ceil(dataInfo.getMaxValue() * 1.01d / magnitudeFactor) * magnitudeFactor;
		// Modify scale-related nodes
		plotYScale(svgChart, svgElm, container, scaleMax, scaleMin, magnitudeFactor);
		// Plot data points
		plotData(svgChart, svgElm, container, scaleMax, chartInfo.getMeasurements(), dataInfo);
	}

	private void plotYScale(DOMDocument svgChart, DOMElement svgElm, Rect container, double scaleMax, double scaleMin,
			double magnitudeFactor) throws ChartTemplateException {
		DOMElement element = svgChart.getElementById("scaleMin");
		if (element == null) {
			throw new ChartTemplateException("No 'scaleMin' element.");
		}
		element.removeAttribute("id");
		element.setAttribute("y", Long.toString(Math.round(container.getY() + container.getHeight() + 4)));
		element.setTextContent(Long.toString(Math.round(scaleMin)));

		int nMarks = (int) Math.round(scaleMax / magnitudeFactor);
		double yFactor = container.getHeight() / nMarks;
		float fontSize = element.getComputedStyle(null).getComputedFontSize() / 0.75f;
		if (yFactor <= fontSize) {
			nMarks = (int) Math.round(Math.floor(container.getHeight() / fontSize) * 0.05f);
			if (nMarks == 0) {
				nMarks = 1;
			}
			nMarks = nMarks * 5;
			yFactor = container.getHeight() / nMarks;
		}
		double scaleDelta = scaleMax / nMarks;
		for (int i = 1; i <= nMarks; i++) {
			double mark = container.getY() + container.getHeight() - i * yFactor + 4;
			DOMElement unitMark = element.cloneNode(false);
			unitMark.setAttribute("y", Long.toString(Math.round(mark)));
			unitMark.setTextContent(Long.toString(Math.round(scaleDelta * i + scaleMin)));
			Text text = svgElm.getOwnerDocument().createTextNode("\n");
			svgElm.insertBefore(text, element);
			svgElm.insertBefore(unitMark, text);
			element = unitMark;
		}
	}

	private void plotData(DOMDocument svgChart, DOMElement svgElm, Rect container, double scaleMax,
			DataSerie<Measurement> discreteDataList, DataInfo dataInfo) throws ChartTemplateException {
		int dataSize = dataInfo.getCount();
		// Bar width
		double barWidth = Math.rint((container.getWidth() - 20) / dataSize) - 10;
		String strBarWidth = Double.toString(barWidth);
		double leftmostBarX = container.getX() + (container.getWidth() - barWidth * dataSize) / dataSize * 0.4f;
		double valueFactor = (container.getHeight() - 4) / scaleMax;
		double itemWidth = container.getWidth() / dataSize;
		// Process datapoint elements
		DOMElement element = svgChart.getElementById("datapoint");
		/*
		 * The new nodes are to replace the "datapoint" element.
		 */
		// If next node is an empty Text, remove it for aesthetic reasons.
		Node inserBeforeMe = element.getNextSibling();
		if (inserBeforeMe.getNodeType() == Node.TEXT_NODE && ((Text) inserBeforeMe).isElementContentWhitespace()) {
			svgElm.removeChild(inserBeforeMe);
			inserBeforeMe = element.getNextSibling();
		}
		svgElm.removeChild(element);
		//
		DecimalFormat dfOneDecimal = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ROOT);
		dfOneDecimal.setMaximumFractionDigits(1);
		Random rand = null;
		int idx = 0;
		Iterator<Measurement> mit = discreteDataList.iterator();
		while (mit.hasNext()) {
			Measurement me = mit.next();
			// Create new set
			Iterator<DOMElement> it = element.elementIterator();
			while (it.hasNext()) {
				DOMElement dataElm = it.next();
				String tagname = dataElm.getTagName();
				DOMElement clone;
				if ("rect".equals(tagname)) {
					clone = dataElm.cloneNode(false);
					// x
					double x = leftmostBarX + itemWidth * idx;
					clone.setAttribute("x", dfOneDecimal.format(x));
					//
					clone.setAttribute("width", strBarWidth);
					double height = me.getValue() * valueFactor;
					clone.setAttribute("height", dfOneDecimal.format(height));
					// y
					double y = container.getY() + (container.getHeight() - height);
					clone.setAttribute("y", dfOneDecimal.format(y));
					//
					if ("firstBar".equals(clone.getAttribute("id"))) {
						String colorStr = me.getColor();
						if (colorStr == null) {
							// No color: generate one.
							if (rand == null) {
								rand = new Random();
							}
							colorStr = generateColor(rand);
						}
						clone.setAttribute("id", me.getId());
						clone.setAttribute("fill", colorStr);
					}
				} else if ("text".equals(tagname)) {
					clone = dataElm.cloneNode(false);
					// x
					double x = leftmostBarX + itemWidth * (idx + 0.455d);
					clone.setAttribute("x", dfOneDecimal.format(x));
					// Content
					DOMNode parent = me.getLabelParent();
					importAsSVG(svgChart, clone, parent, x, dfOneDecimal);
				} else if ("line".equals(tagname) && dataElm.getClassList().contains("errorBar")) {
					clone = dataElm.cloneNode(false);
					// x
					double x = leftmostBarX + itemWidth * (idx + 0.48d);
					String attrVal = dfOneDecimal.format(x);
					clone.setAttribute("x1", attrVal);
					clone.setAttribute("x2", attrVal);
					double y = container.getY() + (container.getHeight() - Math.round(me.getValue() * valueFactor));
					double error = me.getValueError() * valueFactor;
					clone.setAttribute("y1", dfOneDecimal.format(y - error));
					clone.setAttribute("y2", dfOneDecimal.format(y + error));
				} else {
					throw new ChartTemplateException("Unknown element in datapoint group: " + tagname);
				}
				Text text = svgElm.getOwnerDocument().createTextNode("\n");
				svgElm.insertBefore(clone, inserBeforeMe);
				svgElm.insertBefore(text, inserBeforeMe);
			}
			idx++;
		}
	}

	private void importAsSVG(DOMDocument svgChart, DOMElement svgParent, DOMNode foreignParent, double x,
			DecimalFormat dfOneDecimal) {
		float deltaY = -Float.MAX_VALUE;
		Iterator<DOMNode> nodeIt = foreignParent.getChildNodes().iterator();
		while (nodeIt.hasNext()) {
			Node foreignNode = nodeIt.next();
			Node imported;
			if (foreignNode.getNodeType() == Node.ELEMENT_NODE) {
				DOMElement foreignElm = (DOMElement) foreignNode;
				DOMElement childElm = svgChart.createElementNS("http://www.w3.org/2000/svg",
						foreignNode.getLocalName());
				childElm.setPrefix(svgChart.lookupPrefix("http://www.w3.org/2000/svg"));
				// Clone attributes
				copyAttributes(childElm, foreignElm);
				childElm.setAttribute("x", dfOneDecimal.format(x));
				if (deltaY == -Float.MAX_VALUE) {
					deltaY = childElm.getComputedStyle(null).getComputedFontSize() / 0.75f * 0.9f;
				} else {
					childElm.setAttribute("dy", dfOneDecimal.format(deltaY));
					deltaY += childElm.getComputedStyle(null).getComputedFontSize() / 0.75f * 0.9f;
				}
				if (foreignElm.hasChildNodes()) {
					importAsSVG(svgChart, childElm, foreignElm, x, dfOneDecimal);
				}
				imported = childElm;
			} else {
				imported = svgChart.importNode(foreignNode, true);
			}
			svgParent.appendChild(imported);
		}
	}

	private String generateColor(Random rand) {
		int r = rand.nextInt(256);
		int g = rand.nextInt(256);
		int b = rand.nextInt(256);
		StringBuilder sb = new StringBuilder();
		sb.append('#');
		String hex = Integer.toHexString(r);
		if (hex.length() == 1) {
			sb.append('0');
		}
		sb.append(hex);
		//
		hex = Integer.toHexString(g);
		if (hex.length() == 1) {
			sb.append('0');
		}
		sb.append(hex);
		//
		hex = Integer.toHexString(b);
		if (hex.length() == 1) {
			sb.append('0');
		}
		sb.append(hex);
		return sb.toString();
	}

	@Override
	public Class<?> getChartType() {
		return BarChartInfo.class;
	}

}
