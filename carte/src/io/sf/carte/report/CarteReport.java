/*

 Copyright (c) 2020-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.report;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import org.xml.sax.SAXException;

import io.sf.carte.chart.ChartList;
import io.sf.carte.doc.dom.CSSDOMImplementation;
import io.sf.carte.doc.dom.DOMDocument;
import io.sf.carte.doc.dom.DOMElement;
import io.sf.carte.doc.dom.ElementList;
import io.sf.carte.doc.dom.XMLDocumentBuilder;

/**
 * Process a set of reports.
 * <p>
 * WARNING: This code is nowhere near being complete nor API-stable. Use it at
 * your own risk, and contributions would be welcome.
 * </p>
 */
public class CarteReport {

	private DOMDocument config;

	public CarteReport(File pathToConfig) throws ReportConfigurationException, IOException {
		super();
		try {
			this.config = readXMLFile(pathToConfig);
		} catch (SAXException e) {
			throw new ReportConfigurationException("Unable to parse " + pathToConfig.getAbsolutePath(), e);
		}
		checkConfig();
	}

	public CarteReport(DOMDocument config) throws ReportConfigurationException {
		super();
		this.config = config;
		checkConfig();
	}

	private void checkConfig() throws ReportConfigurationException {
		HashSet<String> idset = new HashSet<>();
		ElementList list = config.getElementsByTagName("dataset");
		for (DOMElement element : list) {
			String id = element.getAttribute("id");
			if (id.length() == 0) {
				throw new ReportConfigurationException("<dataset> element without 'id' attribute.");
			}
			if (!idset.add(id)) {
				throw new ReportConfigurationException("<dataset> element with duplicate 'id' attribute: " + id);
			}
		}
	}

	public DOMDocument getConfiguration() {
		return config;
	}

	public void processReportSet(ChartList<?> charts) throws ReportException, IOException {
		DOMElement reportsetElm = config.getElementsByTagName("reportset").item(0);
		if (reportsetElm == null) {
			// Create one
			reportsetElm = config.createElement("reportset");
			config.getDocumentElement().appendChild(reportsetElm);
		}
		boolean hasReport = false;
		Iterator<DOMElement> it = reportsetElm.elementIterator("report");
		while (it.hasNext()) {
			DOMElement reportElm = it.next();
			charts.processReport(reportElm);
			hasReport = true;
		}
		if (!hasReport) {
			throw new ReportConfigurationException("No reports declared in <reportset>.");
		}
	}

	private static DOMDocument readXMLFile(File path) throws SAXException, IOException {
		XMLDocumentBuilder builder = new XMLDocumentBuilder(new CSSDOMImplementation());
		DOMDocument document = (DOMDocument) builder.parse(path);
		document.setDocumentURI(path.toURI().toString());
		return document;
	}

}
