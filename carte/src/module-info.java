/*

 Copyright (c) 2020-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

module io.sf.carte.report {
	exports io.sf.carte.chart;
	exports io.sf.carte.chart.external;
	exports io.sf.carte.image;
	exports io.sf.carte.report;

	requires transitive io.sf.carte.css4j;
	requires io.sf.carte.echosvg.anim;
	requires io.sf.carte.echosvg.transcoder;
	requires java.desktop;
	requires static htmlparser;
	requires static org.jfree.jfreechart;
	requires static org.knowm.xchart;
}
