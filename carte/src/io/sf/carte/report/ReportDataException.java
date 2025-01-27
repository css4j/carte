/*

 Copyright (c) 2020-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.report;

public class ReportDataException extends ReportException {

	private static final long serialVersionUID = 1L;

	public ReportDataException() {
		super();
	}

	public ReportDataException(String message) {
		super(message);
	}

	public ReportDataException(Throwable cause) {
		super(cause);
	}

	public ReportDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReportDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
