/*

 Copyright (c) 2020-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.sf.carte.doc.dom.DOMDocument;
import io.sf.carte.doc.dom.DOMElement;
import io.sf.carte.echosvg.anim.dom.SVGDOMImplementation;
import io.sf.carte.echosvg.transcoder.ErrorHandler;
import io.sf.carte.echosvg.transcoder.SVGAbstractTranscoder;
import io.sf.carte.echosvg.transcoder.TranscoderException;
import io.sf.carte.echosvg.transcoder.TranscoderInput;
import io.sf.carte.echosvg.transcoder.TranscoderOutput;
import io.sf.carte.echosvg.transcoder.image.PNGTranscoder;

public class SVGtoRaster {

	public static void saveAsPNG(DOMDocument svgChart, File destfile) throws ImageConversionException, IOException {
		org.w3c.dom.DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		org.w3c.dom.Document document = impl.createDocument(null, null, null);
		DOMElement svg = svgChart.getDocumentElement();
		org.w3c.dom.Node imported = document.importNode(svg, true);
		document.appendChild(imported);
		//
		PNGTranscoder trans = new PNGTranscoder();
		trans.addTranscodingHint(SVGAbstractTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, Float.valueOf(0.2645833f));
		//
		TranscoderInput input = new TranscoderInput(document);
		FileOutputStream ostream = new FileOutputStream(destfile);
		TranscoderOutput output = new TranscoderOutput(ostream);
		ErrorHandler handler = new DummyErrorHandler();
		trans.setErrorHandler(handler);
		try {
			trans.transcode(input, output);
		} catch (TranscoderException e) {
			throw new ImageConversionException(e);
		}
		ostream.flush();
		ostream.close();
	}

	static class DummyErrorHandler implements ErrorHandler {

		@Override
		public void error(TranscoderException ex) throws TranscoderException {
			throw ex;
		}

		@Override
		public void fatalError(TranscoderException ex) throws TranscoderException {
			throw ex;
		}

		@Override
		public void warning(TranscoderException ex) throws TranscoderException {
		}

	}
}
