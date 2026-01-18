/*

 Copyright (c) 2020-2026, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-2-Clause OR BSD-3-Clause

package io.sf.carte.chart;

import io.sf.carte.report.ReportConfigurationException;

public class ChartTemplateException extends ReportConfigurationException {

	private static final long serialVersionUID = 1L;

	public ChartTemplateException() {
		super();
	}

	public ChartTemplateException(String message) {
		super(message);
	}

	public ChartTemplateException(Throwable cause) {
		super(cause);
	}

	public ChartTemplateException(String message, Throwable cause) {
		super(message, cause);
	}

	public ChartTemplateException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
