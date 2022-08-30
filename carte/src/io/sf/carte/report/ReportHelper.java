/*

 Copyright (c) 2020-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.report;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.doc.dom.CSSDOMImplementation;
import io.sf.carte.doc.dom.DOMDocument;
import io.sf.carte.doc.dom.XMLDocumentBuilder;

public class ReportHelper {

	static String parseFilespec(String path, String dataId) {
		if (path.startsWith("${")) {
			if (path.startsWith("${user.home}")) {
				path = System.getProperty("user.home") + path.subSequence(12, path.length());
			} else if (path.startsWith("${java.io.tmpdir}")) {
				path = System.getProperty("java.io.tmpdir") + path.subSequence(18, path.length());
			}
		}
		int idx = path.indexOf("${dataset.id}");
		if (idx != -1) {
			path = path.subSequence(0, idx) + dataId + path.subSequence(idx + 13, path.length());
		}
		return path;
	}

	public static DOMDocument readXMLFile(InputStream is) throws SAXException, IOException {
		XMLDocumentBuilder builder = new XMLDocumentBuilder(new CSSDOMImplementation());
		InputStreamReader re = new InputStreamReader(is, StandardCharsets.UTF_8);
		InputSource source = new InputSource(re);
		DOMDocument document = (DOMDocument) builder.parse(source);
		return document;
	}

}
