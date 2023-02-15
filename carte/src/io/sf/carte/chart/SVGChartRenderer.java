/*

 Copyright (c) 2020-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.chart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import io.sf.carte.doc.dom.DOMDocument;
import io.sf.carte.doc.dom.DOMElement;
import io.sf.carte.doc.dom.DOMNode;
import io.sf.carte.doc.geom.Rect;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.report.ReportException;
import io.sf.carte.report.ReportHelper;
import io.sf.carte.report.ReportStore;

/**
 * Abstract base class to implement ChartRenderer with SVG documents.
 * <p>
 * WARNING: This code is nowhere near being complete nor API-stable. Use it at
 * your own risk, and contributions would be welcome.
 * </p>
 */
abstract public class SVGChartRenderer<T extends ChartInfo> extends AbstractChartRenderer<T> {

	private DOMDocument svg = null;

	protected SVGChartRenderer() {
		super();
	}

	@Override
	public void init(DOMElement configRoot) throws ReportException {
		// Set template
		String template = null;
		Iterator<DOMElement> it = configRoot.elementIterator("template");
		if (it.hasNext()) {
			String s = it.next().getTextContent().trim();
			if (s.length() != 0) {
				template = s;
			}
		}
		InputStream is;
		if (template == null) {
			is = readFilefromClasspath(getDefaultTemplate());
		} else {
			File templateFile = getTemplateFile(configRoot, template);
			try {
				is = new FileInputStream(templateFile);
			} catch (FileNotFoundException e) {
				throw new ReportException("Unable to read SVG template: " + templateFile.getAbsolutePath(), e);
			}
		}
		setTemplate(readXMLFileAndClose(is));
	}

	protected void setTemplate(DOMDocument templateDocument) {
		this.svg = templateDocument;
	}

	abstract protected String getDefaultTemplate();

	private File getTemplateFile(DOMElement configRoot, String template) {
		File templateFile = new File(template);
		if (!templateFile.isAbsolute()) {
			String basedir = configRoot.getAttribute("basedir").trim();
			if (basedir.length() == 0) {
				DOMElement parent = (DOMElement) configRoot.getParentNode();
				basedir = parent.getAttribute("basedir");
				if (basedir.length() == 0) {
					return templateFile;
				}
			}
			File dir = new File(basedir);
			templateFile = new File(dir, template);
		}
		return templateFile;
	}

	private static DOMDocument readXMLFileAndClose(InputStream is) throws ReportException {
		DOMDocument document;
		try {
			document = ReportHelper.readXMLFile(is);
		} catch (SAXException | IOException e) {
			throw new ReportException("Unable to parse SVG template.", e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		return document;
	}

	private static InputStream readFilefromClasspath(final String cssFilename) {
		return java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<InputStream>() {
			@Override
			public InputStream run() {
				return getClass().getResourceAsStream(cssFilename);
			}
		});
	}

	@Override
	public void writeChart(T chartInfo, Iterable<ReportStore> storeIt) throws ReportException, IOException {
		// fill svg template
		DOMDocument svgChart = cloneTemplate(svg);
		fillChart(svgChart, chartInfo);
		for (ReportStore store : storeIt) {
			store.store(chartInfo, svgChart);
		}
	}

	/**
	 * Clone the SVG template so the clone can be filled with actual data.
	 * 
	 * @param svgTemplate the SVG template.
	 * @return a clone of the SVG template, ready to be filled with data objects.
	 */
	protected DOMDocument cloneTemplate(DOMDocument svgTemplate) {
		return svgTemplate.cloneNode(true);
	}

	protected void fillChart(DOMDocument svgChart, T chartInfo) throws ReportException {
		// fill svg template
		DOMElement svgElm = svgChart.getDocumentElement();
		// Set title element for accessibility
		String title = innertText(chartInfo.getCaption());
		if (title != null && title.length() != 0) {
			DOMElement firstChild = svgElm.getFirstElementChild();
			if (firstChild != null && "title".equals(firstChild.getLocalName())) {
				firstChild.setTextContent(title);
			} else {
				DOMElement titleElm = svgChart.createElementNS(svgElm.getNamespaceURI(), "title");
				titleElm.setTextContent(title);
				svgElm.insertBefore(titleElm, firstChild);
			}
		}
		// Obtain viewport width, height in pixels
		String s = svgElm.getAttribute("height");
		CSSParser parser = new CSSParser();
		double vwHeightPx;
		try {
			vwHeightPx = SVGHelper.parseLengthToPixels(parser, s);
		} catch (ChartTemplateException e) {
			throw new ChartTemplateException("Unable to parse 'heigth' attribute.", e);
		}
		s = svgElm.getAttribute("width");
		double vwWidthPx;
		try {
			vwWidthPx = SVGHelper.parseLengthToPixels(parser, s);
		} catch (ChartTemplateException e) {
			throw new ChartTemplateException("Unable to parse 'width' attribute.", e);
		}
		// Viewbox
		s = svgElm.getAttribute("viewBox").trim();
		Rect viewBox;
		if (s.length() != 0) {
			viewBox = SVGHelper.parseViewBox(s);
		} else {
			viewBox = null;
		}
		//
		setCaptions(svgChart, chartInfo, vwWidthPx, vwHeightPx, viewBox);
		setAxisTitles(svgChart, chartInfo, vwWidthPx, vwHeightPx, viewBox);
		plotGraph(svgChart, svgElm, chartInfo, vwWidthPx, vwHeightPx, parser);
	}

	protected void setCaptions(DOMDocument svgChart, T chartInfo, double vwWidthPx, double vwHeightPx, Rect viewBox) {
		DOMElement element = svgChart.getElementById("caption");
		replaceChildAsSVG(svgChart, element, chartInfo.getCaption());
		element = svgChart.getElementById("subcaption");
		replaceChildAsSVG(svgChart, element, chartInfo.getSubcaption());
	}

	private void replaceChildAsSVG(DOMDocument svgChart, DOMElement svgParent, DOMNode foreignParent) {
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
				if (foreignElm.hasChildNodes()) {
					replaceChildAsSVG(svgChart, childElm, foreignElm);
				}
				imported = childElm;
			} else {
				imported = svgChart.importNode(foreignNode, true);
			}
			svgParent.removeAllChild();
			svgParent.appendChild(imported);
		}
	}

	void copyAttributes(DOMElement svgElm, DOMElement foreignElm) {
		Iterator<Attr> it = foreignElm.getAttributes().iterator();
		while (it.hasNext()) {
			Attr attr = it.next();
			svgElm.setAttributeNS(attr.getNamespaceURI(), attr.getName(), attr.getValue());
		}
	}

	abstract public Class<?> getChartType();

	abstract protected void setAxisTitles(DOMDocument svgChart, T chartInfo, double vwWidthPx, double vwHeightPx,
			Rect viewBox);

	abstract protected void plotGraph(DOMDocument svgChart, DOMElement svgElm, T chartInfo, double vwWidthPx,
			double vwHeightPx, Parser parser) throws ChartTemplateException;

}
