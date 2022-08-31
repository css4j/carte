/*

 Copyright (c) 2020-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.report;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Locale;

import org.w3c.dom.Node;

import io.sf.carte.chart.BarChartInfo;
import io.sf.carte.chart.ChartInfo;
import io.sf.carte.doc.dom.DOMDocument;
import io.sf.carte.doc.dom.DOMElement;
import io.sf.carte.doc.dom.DOMNode;

/**
 * Stores data into an HTML table.
 * 
 * <p>
 * In the XML configuration file, you can use an optional {@code <css-classes>}
 * element where you tell which {@code class} attribute the numbers and units
 * will have (by default, {@code number} for numbers and no class for units).
 * </p>
 * 
 * <pre>
 * &lt;css-classes&gt;
 *     &lt;number&gt;numberclass&lt;/number&gt;
 *     &lt;unit&gt;unitclass&lt;/unit&gt;
 * &lt;/css-classes&gt;
 * </pre>
 * 
 * <p>
 * Then, it will retrieve all the HTML tables where the
 * <code>&lt;table&gt;</code> elements have a {@code carteitem} class, and then
 * replace its contents with the report data.
 * </p>
 * <p>
 * See {@link DocumentStore}.
 * </p>
 * <p>
 * WARNING: This code is nowhere near being complete nor API-stable. Use it at
 * your own risk, and contributions would be welcome.
 * </p>
 */
public class HTMLTableStore implements ReportStore {

	private static final String CARTEITEM_CLASS = "carteitem";

	private String numberClass = "number";

	private String unitClass = null;

	public HTMLTableStore() {
		super();
	}

	@Override
	public void init(DOMElement configRoot) throws ReportException, IOException {
		Iterator<DOMElement> it = configRoot.elementIterator("css-classes");
		if (it.hasNext()) {
			DOMElement classes = it.next();
			it = classes.elementIterator();
			while (it.hasNext()) {
				DOMElement element = it.next();
				if ("number".equals(element.getLocalName())) {
					String classname = element.getTextContent().trim();
					if (classname.length() != 0) {
						numberClass = classname;
					} else {
						numberClass = "number";
					}
				} else if ("unit".equals(element.getLocalName())) {
					String classname = element.getTextContent().trim();
					if (classname.length() != 0) {
						unitClass = classname;
					} else {
						unitClass = null;
					}
				}
			}
		}
	}

	@Override
	public void store(ChartInfo chartInfo, DOMDocument document) throws ReportException, IOException {
		String id = chartInfo.getId();
		DOMElement chartRoot = document.getElementById(id);
		if (chartRoot == null) {
			throw new ReportException("No element with id '" + id + "'.");
		}
		// Update table
		if (chartInfo instanceof BarChartInfo) {
			updateTable(chartRoot, (BarChartInfo) chartInfo);
		}
	}

	private void updateTable(DOMElement chartRoot, BarChartInfo chartInfo) throws ReportConfigurationException {
		Iterator<DOMElement> it = chartRoot.elementIterator("table");
		while (it.hasNext()) {
			DOMElement element = it.next();
			if (element.getClassList().contains(CARTEITEM_CLASS)) {
				DOMElement tbody = null;
				DOMElement row = element.getFirstElementChild();
				while (row != null) {
					String tag = row.getTagName();
					if (tag.equals("thead")) {
						row = row.getNextElementSibling();
					} else if (tag.equals("tbody")) {
						tbody = row;
						row = row.getFirstElementChild();
					} else if (tag.equals("tr")) {
						DOMElement child = row.getFirstElementChild();
						if (child != null) {
							if (child.getTagName().equals("td")) {
								processRows(row, chartInfo);
								return;
							} else {
								// If it is a <th> and there is no <thead>, put it in a new one.
								if (tbody != null && tbody.getPreviousElementSibling() == null
										&& child.getTagName().equals("th")) {
									DOMDocument document = chartRoot.getOwnerDocument();
									Iterator<DOMElement> headIt = element.elementIterator("thead");
									if (!headIt.hasNext()) {
										DOMElement thead = document.createElement("thead");
										element.insertBefore(thead, tbody);
										element.insertBefore(document.createTextNode("\n"), tbody);
										DOMElement nextRow = row.getNextElementSibling();
										row.getParentNode().removeChild(row);
										thead.appendChild(row);
										row = nextRow;
										continue;
									}
								}
								row = row.getNextElementSibling();
							}
						} else {
							break;
						}
					}
				}
			}
		}
	}

	private void processRows(DOMElement firstRow, BarChartInfo chartInfo) throws ReportConfigurationException {
		DOMNode node;
		// Remove old data
		DOMNode parent = firstRow.getParentNode();
		while ((node = firstRow.getNextSibling()) != null) {
			if (node.getNodeType() != Node.ELEMENT_NODE || "tr".equals(node.getNodeName())) {
				parent.removeChild(node);
			} else {
				break;
			}
		}
		DOMElement beforeMe = firstRow.getNextElementSibling();
		parent.removeChild(firstRow);
		//
		DiscreteDataList data = chartInfo.getMeasurements();
		DOMDocument document = firstRow.getOwnerDocument();
		// Prepare number format
		Locale locale = getLocale(document);
		DecimalFormat dformat = (DecimalFormat) NumberFormat.getNumberInstance(locale);
		DataInfo dataInfo = data.computeDataInfo();
		int ndigits = (int) Math.floor(Math.log10(dataInfo.getMinError()));
		if (ndigits > 1) {
			ndigits = 0;
		} else if (ndigits <= 0) {
			ndigits = 2 - ndigits;
		} // else ndigits = 1;
		dformat.setMaximumFractionDigits(ndigits);
		dformat.setMinimumFractionDigits(ndigits);
		//
		Iterator<Measurement> it = data.iterator();
		while (it.hasNext()) {
			DOMElement row = firstRow.cloneNode(true);
			updateRow(document, row, it.next(), dformat, chartInfo.getValueUnit());
			parent.insertBefore(row, beforeMe);
			parent.insertBefore(document.createTextNode("\n"), beforeMe);
		}
	}

	private void updateRow(DOMDocument document, DOMElement row, Measurement dataitem, DecimalFormat dformat,
			String unitStr) {
		DOMElement child = row.getFirstElementChild();
		if (child == null) {
			child = document.createElement("td");
			row.appendChild(child);
		}
		DOMNode labelParent = dataitem.getLabelParent();
		String rowDesc;
		if (labelParent.getNodeType() == Node.ELEMENT_NODE) {
			rowDesc = ((DOMElement) labelParent).getInnerText();
		} else {
			rowDesc = labelParent.getTextContent();
		}
		child.setTextContent(rowDesc.trim());
		//
		child = child.getNextElementSibling();
		if (child == null) {
			child = document.createElement("td");
			child.setAttribute("class", numberClass);
			row.appendChild(child);
		}
		child.setTextContent(dformat.format(dataitem.getValue()));
		//
		child = child.getNextElementSibling();
		if (child == null) {
			child = document.createElement("td");
			child.setAttribute("class", numberClass);
			row.appendChild(child);
		}
		child.setTextContent('±' + dformat.format(dataitem.getValueError()));
		//
		child = child.getNextElementSibling();
		if (child == null) {
			child = document.createElement("td");
			if (unitClass != null) {
				child.setAttribute("class", unitClass);
			}
			row.appendChild(child);
		}
		child.setTextContent(unitStr);
	}

	private Locale getLocale(DOMDocument document) {
		DOMElement docElm = document.getDocumentElement();
		if (docElm != null) {
			String lang = docElm.getAttribute("lang");
			if (lang.length() == 0) {
				lang = docElm.getAttribute("xml:lang");
			}
			if (lang.length() != 0) {
				return Locale.forLanguageTag(lang);
			}
		}
		return Locale.ROOT;
	}

}
