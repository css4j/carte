/*

 Copyright (c) 2020-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.chart.ChartInfo;
import io.sf.carte.doc.dom.AttributeNamedNodeMap;
import io.sf.carte.doc.dom.CSSDOMImplementation;
import io.sf.carte.doc.dom.DOMDocument;
import io.sf.carte.doc.dom.DOMElement;
import io.sf.carte.doc.dom.DOMNode;
import io.sf.carte.doc.dom.ElementList;
import io.sf.carte.doc.dom.NodeFilter;
import io.sf.carte.doc.dom.NodeIterator;
import io.sf.carte.doc.dom.XMLDocumentBuilder;
import io.sf.carte.doc.style.css.CSSStyleRule;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.LinkStyle;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.image.ForkException;
import io.sf.carte.image.ImageConversionException;
import io.sf.carte.image.PNGOptimizer;
import io.sf.carte.image.SVGtoRaster;
import io.sf.carte.util.Visitor;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

/**
 * <p>
 * WARNING: This code is nowhere near being complete nor API-stable. Use it at
 * your own risk, and contributions would be welcome.
 * </p>
 */
public class DocumentStore implements ReportStore {

	private DOMDocument document = null;

	private File docFile = null;

	private String fallbackURI = null;

	private String fallbackDir = null;

	private boolean optimizeFallback = false;

	private static final String CARTEITEM_CLASS = "carteitem";

	// Build raw set
	private static final Set<String> attributesThatLink = new HashSet<>(8);

	static {
		/*
		 * We consider as "raw text" both the 'real' raw text (script, style) and
		 * elements that preserve whitespace. We do not include <style> here as it has
		 * its own dedicated element.
		 */
		String[] rawText = { "fill", "stroke", "filter", "clip-path", "cursor", "marker-end", "marker-mid",
				"marker-start" };
		Collections.addAll(attributesThatLink, rawText);
	}

	public DocumentStore() {
		super();
	}

	@Override
	public void init(DOMElement configRoot) throws ReportException, IOException {
		// Read document
		Iterator<DOMElement> it = configRoot.elementIterator("pathname");
		String pathname;
		if (!it.hasNext() || (pathname = it.next().getTextContent().trim()).length() == 0) {
			throw new ReportConfigurationException("No pathname for store '" + configRoot.getAttribute("id") + "'.");
		}
		pathname = ReportHelper.parseFilespec(pathname, "");
		docFile = new File(pathname);
		FileInputStream is = new FileInputStream(docFile);
		if (pathname.endsWith(".html") || pathname.endsWith(".xhtml")) {
			CSSDOMImplementation domImpl = new CSSDOMImplementation();
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
			parser.setReportingDoctype(true);
			parser.setCommentPolicy(XmlViolationPolicy.ALLOW);
			parser.setXmlnsPolicy(XmlViolationPolicy.ALLOW);
			XMLDocumentBuilder docbuilder = new XMLDocumentBuilder(domImpl);
			docbuilder.setHTMLProcessing(true);
			docbuilder.setXMLReader(parser);
			InputSource source = new InputSource(new InputStreamReader(is, StandardCharsets.UTF_8));
			try {
				document = (DOMDocument) docbuilder.parse(source);
			} catch (SAXException e) {
				throw new ReportConfigurationException("Could not parse file at: " + pathname, e);
			}
		} else {
			try {
				document = ReportHelper.readXMLFile(is);
			} catch (SAXException e) {
				throw new ReportConfigurationException("Could not parse file at: " + pathname, e);
			}
		}
		is.close();
		// Fallback image directory and uri
		it = configRoot.elementIterator("fallback");
		if (it.hasNext()) {
			DOMElement elem = it.next();
			fallbackDir = elem.getTextContent().trim();
			fallbackURI = elem.getAttribute("baseuri").trim();
			if (fallbackDir.length() == 0) {
				if (fallbackURI.length() != 0) {
					throw new ReportConfigurationException(
							"No <fallback> specified, but there is a 'baseuri' attribute.");
				}
				fallbackDir = null;
			}
			// Optimize?
			if (elem.hasAttribute("optimize") && !"false".equalsIgnoreCase(elem.getAttribute("optimize"))) {
				optimizeFallback = true;
			} else {
				optimizeFallback = false;
			}
		} else {
			fallbackURI = null;
		}
	}

	@Override
	public void store(ChartInfo chartInfo, DOMDocument svgChart) throws ReportException, IOException {
		String id = chartInfo.getId();
		DOMElement chartRoot = document.getElementById(id);
		if (chartRoot == null) {
			throw new ReportException("No element with id '" + id + "'.");
		}
		ElementList list = chartRoot.getElementsByTagNameNS("*", "svg");
		Iterator<DOMElement> it = list.iterator();
		DOMElement svg = null;
		while (it.hasNext()) {
			DOMElement element = it.next();
			if (element.getClassList().contains(CARTEITEM_CLASS)) {
				svg = element;
				break;
			}
		}
		if (svg == null) {
			throw new ReportException("No SVG element with class '" + CARTEITEM_CLASS + "' inside id '" + id + "'.");
		}
		DOMNode parent = svg.getParentNode();
		DOMElement imported = (DOMElement) document.importNode(svgChart.getDocumentElement(), true);
		imported.getClassList().add(CARTEITEM_CLASS);
		// Replace IDs to avoid collisions
		replaceIDs(id, imported);
		// Fallback
		writeFallback(id, svgChart, imported);
		// Insert the new SVG in the document
		parent.replaceChild(imported, svg);
		// Update table
		HTMLTableStore htmlStore = new HTMLTableStore();
		htmlStore.store(chartInfo, document);
		// Save
		saveDocument();
	}

	private void replaceIDs(String id, DOMElement svg) {
		/*
		 * Look for 'id' attributes and prefix them with <id>. Replace
		 * attributesThatLink: fill, stroke, filter, clip-path, cursor, marker-end,
		 * marker-mid, marker-start (and also href) with url(#id) values when they point
		 * to one of the replaced IDs.
		 */
		String prefix = id + '-';
		IdAttributeFilter idfilter = new IdAttributeFilter(prefix);
		NodeIterator it = svg.getOwnerDocument().createNodeIterator(svg, NodeFilter.SHOW_ELEMENT, idfilter);
		it.hasNext();
		//
		NodeFilter replaceFilter = new ReplaceIdFilter(prefix, idfilter.getIdSet());
		it = svg.getOwnerDocument().createNodeIterator(svg, NodeFilter.SHOW_ELEMENT, replaceFilter);
		it.hasNext();
		// Now replace style sheet(s)
		PrefixConditionVisitor prefixVisitor = new PrefixConditionVisitor(prefix);
		Visitor<CSSStyleRule> ruleVisitor = new SelectorRuleVisitor(prefixVisitor);
		Iterator<DOMElement> styleIt = svg.getElementsByTagNameNS("*", "style").iterator();
		while (styleIt.hasNext()) {
			DOMElement style = styleIt.next();
			AbstractCSSStyleSheet sheet = (AbstractCSSStyleSheet) ((LinkStyle<?>) style).getSheet();
			if (sheet != null) {
				sheet.acceptStyleRuleVisitor(ruleVisitor);
				style.normalize();
			}
		}
	}

	/**
	 * If an element has the ID attribute set, prefixes it.
	 */
	static class IdAttributeFilter implements NodeFilter {

		private final String prefix;

		private final HashSet<String> idset = new HashSet<>();

		IdAttributeFilter(String prefix) {
			super();
			this.prefix = prefix;
		}

		@Override
		public short acceptNode(Node node) {
			AttributeNamedNodeMap attrs = ((DOMElement) node).getAttributes();
			if (!attrs.isEmpty()) {
				for (Attr attr : attrs) {
					if (attr.isId()) {
						String id = attr.getValue().trim();
						if (id.length() != 0) {
							idset.add(id);
							attr.setValue(prefix + id);
							break;
						}
					}
				}
			}
			return NodeFilter.FILTER_SKIP_NODE;
		}

		public HashSet<String> getIdSet() {
			return idset;
		}

	}

	/**
	 * If the node is an element that contains the {@code href} attribute or any
	 * attribute used to link, prefixes the URI.
	 */
	class ReplaceIdFilter implements NodeFilter {

		private final String prefix;

		private final Set<String> idset;

		private final Parser parser = new CSSParser();

		private final ValueFactory valueFactory = new ValueFactory();

		ReplaceIdFilter(String prefix, Set<String> idset) {
			super();
			this.prefix = prefix;
			this.idset = idset;
		}

		@Override
		public short acceptNode(Node node) {
			AttributeNamedNodeMap attrs = ((DOMElement) node).getAttributes();
			if (!attrs.isEmpty()) {
				for (Attr attr : attrs) {
					if ("href".equalsIgnoreCase(attr.getLocalName())) {
						String value = attr.getValue();
						if (idset.contains(value)) {
							attr.setValue(prefix + value);
						}
					} else if (attributesThatLink.contains(attr.getLocalName().toLowerCase(Locale.ROOT))) {
						String value = attr.getValue();
						StyleValue cssValue;
						try {
							cssValue = valueFactory.parseProperty(value, parser);
						} catch (DOMException e) {
							continue;
						}
						if (cssValue.getPrimitiveType() == Type.URI) {
							CSSTypedValue typed = (CSSTypedValue) cssValue;
							String strUri = typed.getStringValue().trim();
							String id;
							if (strUri.length() > 1 && strUri.charAt(0) == '#'
									&& idset.contains(id = strUri.substring(1))) {
								typed.setStringValue(Type.URI, '#' + prefix + id);
								attr.setValue(typed.getCssText());
							}
						}
					}
				}
			}
			return NodeFilter.FILTER_SKIP_NODE;
		}

	}

	private void writeFallback(String id, DOMDocument svgChart, DOMElement imported)
			throws ReportException, IOException {
		if (fallbackDir != null) {
			String fallback = ReportHelper.parseFilespec(fallbackDir, id);
			File fbDir = new File(fallback);
			boolean relativeDir = !fbDir.isAbsolute();
			String fbURI;
			if (fallbackURI.length() == 0) {
				if (relativeDir) {
					fbURI = fallbackDir;
				} else {
					throw new ReportConfigurationException("No 'baseuri' attribute.");
				}
			} else {
				fbURI = ReportHelper.parseFilespec(fallbackURI, id);
			}
			File docParent;
			if (relativeDir && (docParent = docFile.getParentFile()) != null) {
				fbDir = new File(docParent, fallback);
			}
			String imgSrc = getFallbackImageBaseURI(fbURI, id);
			setFallbackImage(imported, imgSrc);
			File destfile = getFallbackImageFile(fbDir, id);
			exportToRaster(svgChart, destfile);
		}
	}

	private void setFallbackImage(DOMElement svgElm, String imgSrc) {
		// Add image fallback as the last child element
		// See https://css-tricks.com/a-complete-guide-to-svg-fallbacks/
		DOMElement lastChild = svgElm.getLastElementChild();
		if ("image".equals(lastChild.getLocalName())) {
			lastChild.setAttribute("src", imgSrc);
		} else {
			DOMElement imageElm = document.createElementNS(svgElm.getNamespaceURI(), "image");
			imageElm.setAttribute("src", imgSrc);
			imageElm.setAttributeNS("https://www.w3.org/1999/xlink", "xlink:href", "");
			svgElm.appendChild(imageElm);
		}
	}

	private void exportToRaster(DOMDocument svgChart, File destfile) throws ReportException, IOException {
		try {
			SVGtoRaster.saveAsPNG(svgChart, destfile);
		} catch (ImageConversionException e) {
			throw new ReportException(e);
		}
		if (optimizeFallback) {
			PNGOptimizer opti = new PNGOptimizer();
			try {
				opti.optimize(destfile.getAbsolutePath(), destfile, 0);
			} catch (ForkException e) {
				throw new ReportException(e);
			}
		}
	}

	private File getFallbackImageFile(File fbDir, String id) throws ReportConfigurationException {
		return new File(fbDir, id + ".png");
	}

	private String getFallbackImageBaseURI(String fbURI, String id) {
		return fbURI + '/' + id + ".png";
	}

	private void saveDocument() throws IOException {
		FileWriter wri = new FileWriter(docFile.getAbsolutePath(), StandardCharsets.UTF_8);
		wri.write(document.toString());
		wri.close();
	}

}
