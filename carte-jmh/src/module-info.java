/*

 Copyright (c) 2020-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

/**
 * Automated generation of charts from JMH benchmarks.
 * <p>
 * WARNING: This code is nowhere near being complete nor API-stable. Use it at
 * your own risk, and contributions would be welcome.
 * </p>
 */
module io.sf.carte.report.jmh {
	exports io.sf.carte.report.jmh;

	requires io.sf.carte.report;
	requires transitive io.sf.carte.css4j;
	requires io.sf.jclf.core;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
}
