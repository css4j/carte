/*

 Copyright (c) 2020-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import io.sf.carte.chart.ChartInfo;
import io.sf.carte.doc.dom.DOMDocument;
import io.sf.carte.doc.dom.DOMElement;

/**
 * <p>
 * WARNING: This code is nowhere near being complete nor API-stable. Use it at
 * your own risk, and contributions would be welcome.
 * </p>
 */
public class FileStore implements ReportStore {

	private File chartDir = null;

	public FileStore() {
		super();
	}

	@Override
	public void init(DOMElement configRoot) throws ReportException {
		Iterator<DOMElement> it = configRoot.elementIterator("directory");
		String directory;
		if (!it.hasNext() || (directory = it.next().getTextContent().trim()).length() == 0) {
			throw new ReportConfigurationException("No <directory> for store '" + configRoot.getAttribute("id") + "'.");
		}
		directory = ReportHelper.parseFilespec(directory, "");
		chartDir = new File(directory);
		if (!chartDir.exists()) {
			chartDir.mkdirs();
		}
	}

	@Override
	public void store(ChartInfo chartInfo, DOMDocument svgChart) throws ReportException, IOException {
		String id = chartInfo.getId();
		File file = new File(chartDir, id + ".svg");
		FileWriter wri = new FileWriter(file);
		wri.write(svgChart.toString());
		wri.close();
	}

	@Override
	public OutputStream getOutputStream(String uri) throws ReportException, IOException {
		File file = new File(chartDir, uri);
		File parentDir = file.getParentFile();
		if (parentDir != null && !parentDir.exists()) {
			parentDir.mkdirs();
		}
		FileOutputStream os = new FileOutputStream(file);
		return os;
	}

}
