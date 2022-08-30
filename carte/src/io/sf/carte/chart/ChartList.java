/*

 Copyright (c) 2020-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.chart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import io.sf.carte.doc.dom.DOMElement;
import io.sf.carte.report.ReportConfigurationException;
import io.sf.carte.report.ReportException;
import io.sf.carte.report.ReportStore;

/**
 * List of charts of the same type.
 * <p>
 * WARNING: This code is nowhere near being complete nor API-stable. Use it at
 * your own risk, and contributions would be welcome.
 * </p>
 */
public class ChartList<T extends ChartInfo> extends ArrayList<T> {

	private static final long serialVersionUID = 1L;

	private DOMElement reportElm;

	private ChartRenderer<T> renderer;

	public ChartList() {
		super();
	}

	public ChartList(int initialSize) {
		super(initialSize);
	}

	public ChartList(Collection<T> c) {
		super(c);
	}

	@SuppressWarnings("unchecked")
	public void processReport(DOMElement reportElm) throws ReportException, IOException {
		this.reportElm = reportElm;
		Iterator<DOMElement> domIt = reportElm.elementIterator("renderer");
		String rendererClass;
		if (domIt.hasNext()) {
			DOMElement rendererElm = domIt.next();
			rendererClass = rendererElm.getTextContent().trim();
		} else {
			throw new ReportConfigurationException("No renderer declared in <report>.");
		}
		// Initialize renderer
		renderer = (ChartRenderer<T>) instantiateClass(rendererClass);
		renderer.init(reportElm);
		// render & store
		Iterator<T> it = iterator();
		while (it.hasNext()) {
			T chartInfo = it.next();
			Iterable<ReportStore> storeIt = storageIterator();
			renderer.writeChart(chartInfo, storeIt);
		}
	}

	private Iterable<ReportStore> storageIterator() throws ReportException, IOException {
		DOMElement storageElm = reportElm.getOwnerDocument().getElementsByTagName("storage").item(0);
		boolean hasStore = false;
		Iterator<DOMElement> it = reportElm.elementIterator("store-id");
		LinkedList<ReportStore> stores = new LinkedList<>();
		while (it.hasNext()) {
			DOMElement elm = it.next();
			String storeId = elm.getTextContent().trim();
			DOMElement storeElm = elm.getOwnerDocument().getElementById(storeId);
			ReportStore store = initStore(storeElm);
			stores.add(store);
			hasStore = true;
		}
		if (!hasStore) {
			// Default is first under "storage"
			if (storageElm == null || !(it = storageElm.elementIterator("store")).hasNext()) {
				throw new ReportConfigurationException("No storage defined.");
			}
			DOMElement storeElm = it.next();
			ReportStore store = initStore(storeElm);
			stores.add(store);
		}
		return stores;
	}

	private ReportStore initStore(DOMElement storeElm) throws ReportException, IOException {
		String reportClass = storeElm.getAttribute("classname");
		if (reportClass.length() == 0) {
			throw new ReportConfigurationException(
					"No classname defined for storage '" + storeElm.getAttribute("id") + "'.");
		}
		ReportStore store = (ReportStore) instantiateClass(reportClass);
		store.init(storeElm);
		return store;
	}

	private Object instantiateClass(String className) throws ReportException {
		Object renderer;
		try {
			renderer = Class.forName(className).getConstructor().newInstance();
		} catch (Exception e) {
			throw new ReportException("Unable to instantiate: " + className, e);
		}
		return renderer;
	}

}
