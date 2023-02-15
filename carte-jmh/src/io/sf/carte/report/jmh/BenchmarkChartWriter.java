/*

 Copyright (c) 2020-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.report.jmh;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.sf.carte.chart.BarChartInfo;
import io.sf.carte.chart.ChartList;
import io.sf.carte.doc.dom.DOMDocument;
import io.sf.carte.doc.dom.DOMElement;
import io.sf.carte.doc.dom.DOMNode;
import io.sf.carte.doc.dom.ElementList;
import io.sf.carte.report.CarteReport;
import io.sf.carte.report.DiscreteDataList;
import io.sf.carte.report.Measurement;
import io.sf.carte.report.ReportConfigurationException;
import io.sf.carte.report.ReportDataException;
import io.sf.carte.report.ReportException;
import io.sf.jclf.app.CommandLine;

/**
 * Generate charts for benchmarks and save them to files.
 * <p>
 * WARNING: This code is nowhere near being complete nor API-stable. Use it at
 * your own risk, and contributions would be welcome.
 * </p>
 */
public class BenchmarkChartWriter {

	private CarteReport report;

	public BenchmarkChartWriter(File pathToConfig) throws ReportConfigurationException, IOException {
		super();
		report = new CarteReport(pathToConfig);
	}

	public BenchmarkChartWriter(DOMDocument config) throws ReportConfigurationException {
		super();
		report = new CarteReport(config);
	}

	public void writeCharts(String[] jsonfilenames) throws ReportException, IOException {
		JsonNode[] jsonTrees = new JsonNode[jsonfilenames.length];
		final ObjectMapper objectMapper = new ObjectMapper();
		for (int i = 0; i < jsonfilenames.length; i++) {
			File jsonfile = new File(jsonfilenames[i]);
			FileReader re = new FileReader(jsonfile);
			jsonTrees[i] = objectMapper.readTree(re);
		}
		writeCharts(jsonTrees);
	}

	void writeCharts(JsonNode[] jsonTrees) throws ReportException, IOException {
		ChartList<BarChartInfo> charts = prepareCharts(jsonTrees);
		report.processReportSet(charts);
	}

	private ChartList<BarChartInfo> prepareCharts(JsonNode[] jsonTrees) throws ReportException {
		HashMap<String, BarChartInfo> chartMap = new HashMap<>();
		for (JsonNode jsonTree : jsonTrees) {
			int sz = jsonTree.size();
			for (int i = 0; i < sz; i++) {
				JsonNode methodNode = jsonTree.get(i);
				JsonNode benchmarkNode = methodNode.get("benchmark");
				if (benchmarkNode == null) {
					throw new ReportDataException("No benchmark node.");
				}
				String qMethod = benchmarkNode.asText();
				int dotIdx = qMethod.lastIndexOf('.');
				if (dotIdx == -1 || dotIdx == qMethod.length() - 1) {
					throw new ReportDataException("Malformed method data.");
				}
				String classname = qMethod.substring(0, dotIdx);
				String methodname = qMethod.substring(dotIdx + 1);
				ElementList list = report.getConfiguration().getElementsByTagName("class");
				for (DOMElement element : list) {
					String itemClass = element.getTextContent().trim();
					if (classname.equals(itemClass)) {
						DOMNode parent = element.getParentNode();
						if (parent.getNodeType() != Node.ELEMENT_NODE) {
							throw new ReportConfigurationException("Malformed configuration file.");
						}
						DOMElement parentElm = (DOMElement) parent;
						if (matchesMethod(methodname, parentElm)) {
							String id = parentElm.getAttribute("id");
							JsonNode primaryMetric = methodNode.get("primaryMetric");
							if (primaryMetric == null) {
								throw new ReportDataException("No primaryMetric node.");
							}
							JsonNode score = primaryMetric.get("score");
							JsonNode scoreError = primaryMetric.get("scoreError");
							JsonNode scoreUnit = primaryMetric.get("scoreUnit");
							if (score == null) {
								throw new ReportDataException("No score node.");
							}
							if (scoreError == null) {
								throw new ReportDataException("No scoreError node.");
							}
							if (scoreUnit == null) {
								throw new ReportDataException("No scoreUnit node.");
							}
							// Get the chart or create one
							BarChartInfo currentChart = chartMap.get(id);
							if (currentChart == null) {
								currentChart = createChart(id, parentElm);
								chartMap.put(id, currentChart);
								currentChart.setValueUnit(scoreUnit.asText());
								setChartConfig(currentChart);
							}
							if (!currentChart.getValueUnit().equals(scoreUnit.asText())) {
								throw new ReportDataException("Expected unit '" + currentChart.getValueUnit()
										+ "', attempted to add '" + scoreUnit.asText() + "'.");
							}
							Measurement measure = currentChart.getMeasurements().add(methodname, score.doubleValue(),
									scoreError.doubleValue());
							setMeasurementConfig(measure, parentElm);
						}
					}
				}
			}
		}
		ChartList<BarChartInfo> chartSet = new ChartList<>(chartMap.values());
		return chartSet;
	}

	private static boolean matchesMethod(String methodname, DOMElement benchmarkElm) {
		ElementList includeElms = benchmarkElm.getElementsByTagName("include");
		DOMElement includeElm;
		if ((includeElm = includeElms.item(0)) != null) {
			String s = includeElm.getTextContent();
			StringTokenizer st = new StringTokenizer(s, ",");
			while (st.hasMoreTokens()) {
				String reStr = st.nextToken().trim();
				if (Pattern.matches(reStr, methodname)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	private static void setMeasurementConfig(Measurement measure, DOMElement benchmarkElm)
			throws ReportConfigurationException {
		ElementList pList = benchmarkElm.getChildren();
		for (DOMElement childElm : pList) {
			String name = childElm.getTagName();
			if ("methods".equals(name)) {
				if (scanMethods(measure, childElm)) {
					return;
				}
				break;
			}
		}
		DOMElement parentElem = parentElement(benchmarkElm);
		pList = parentElem.getChildren();
		for (DOMElement childElm : pList) {
			String name = childElm.getTagName();
			if ("methods".equals(name)) {
				if (scanMethods(measure, childElm)) {
					return;
				}
				break;
			}
		}
		DocumentFragment fragment = benchmarkElm.getOwnerDocument().createDocumentFragment();
		Text text = benchmarkElm.getOwnerDocument().createTextNode(measure.getId());
		fragment.appendChild(text);
		measure.setLabelParent((DOMNode) fragment);
	}

	private static boolean scanMethods(Measurement measure, DOMElement methodsElm) throws ReportConfigurationException {
		ElementList mList = methodsElm.getChildren();
		for (DOMElement methodElm : mList) {
			String name = methodElm.getTagName();
			if ("method".equals(name)) {
				String reStr = methodElm.getAttribute("regex");
				if (reStr.length() == 0) {
					throw new ReportConfigurationException("No regex in method element: " + methodElm.getStartTag());
				}
				if (Pattern.matches(reStr, measure.getId())) {
					String color = methodElm.getAttribute("color");
					measure.setLabelParent(methodElm);
					measure.setColor(color);
					return true;
				}
			}
		}
		return false;
	}

	private static DOMElement parentElement(DOMElement benchmarkElm) throws ReportConfigurationException {
		DOMNode parentNode = benchmarkElm.getParentNode();
		if (parentNode.getNodeType() != Node.ELEMENT_NODE) {
			throw new ReportConfigurationException("Malformed configuration file.");
		}
		return (DOMElement) parentNode;
	}

	private void setChartConfig(BarChartInfo chart) throws ReportConfigurationException {
		DOMElement unitsElm;
		if ((unitsElm = report.getConfiguration().getElementsByTagName("units").item(0)) == null) {
			throw new ReportConfigurationException("No <units> element.");
		}
		ElementList unitList = unitsElm.getChildren();
		for (DOMElement element : unitList) {
			if ("unit-title".equals(element.getTagName())) {
				String unitId = element.getAttribute("unit-id");
				if (unitId == null || unitId.length() == 0) {
					throw new ReportConfigurationException("No 'unit-id' attribute.");
				}
				if (chart.getValueUnit().equals(unitId)) {
					chart.setUnitLegend(element.getTextContent().trim());
				}
			}
		}
		DiscreteDataList measurements = new DiscreteDataList();
		chart.setMeasurements(measurements);
	}

	private BarChartInfo createChart(String id, DOMElement parentElm) {
		BarChartInfo chart = new BarChartInfo(id);
		DOMNode caption = null, subcaption = null;
		String xAxisTitle = null;
		ElementList pList = parentElm.getChildren();
		for (DOMElement childElm : pList) {
			String name = childElm.getTagName();
			if ("caption".equals(name)) {
				caption = childElm;
			} else if ("subcaption".equals(name)) {
				subcaption = childElm;
			} else if ("x-axis-title".equals(name)) {
				xAxisTitle = childElm.getTextContent().trim();
			}
		}
		// If caption or X-title are not set, look at the root element
		if (caption == null) {
			DOMElement docElm = report.getConfiguration().getDocumentElement();
			Iterator<DOMElement> it = docElm.elementIterator("caption");
			if (it.hasNext()) {
				caption = it.next();
			}
		}
		if (xAxisTitle == null) {
			DOMElement docElm = report.getConfiguration().getDocumentElement();
			Iterator<DOMElement> it = docElm.elementIterator("x-axis-title");
			if (it.hasNext()) {
				xAxisTitle = it.next().getTextContent().trim();
			}
		}
		//
		chart.setCaption(caption);
		chart.setSubcaption(subcaption);
		chart.setXAxisTitle(xAxisTitle);
		return chart;
	}

	public static void main(String[] args) throws ReportException, SAXException, IOException {
		CommandLine cmdline = new CommandLine(args);
		String paramFile = cmdline.getAssignParam("config");
		//
		File configFile;
		if (paramFile == null) {
			configFile = new File("benchmark.xml");
			if (!configFile.exists()) {
				fail("You need to specify --config=<path-to-config-file>.");
				return;
			}
		} else {
			configFile = new File(paramFile);
			if (!configFile.exists()) {
				fail("File does not exist: " + paramFile);
				return;
			}
		}
		//
		String[] arguments = cmdline.getLastArguments();
		BenchmarkChartWriter writer = new BenchmarkChartWriter(configFile);
		writer.writeCharts(arguments);
	}

	private static void fail(String message) {
		System.err.println(message);
		System.exit(1);
	}

}
