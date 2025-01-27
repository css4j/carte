/*

 Copyright (c) 2020-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.report;

public class ReportConfigurationException extends ReportException {

	private static final long serialVersionUID = 1L;

	public ReportConfigurationException() {
		super();
	}

	public ReportConfigurationException(String message) {
		super(message);
	}

	public ReportConfigurationException(Throwable cause) {
		super(cause);
	}

	public ReportConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReportConfigurationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
