/*

 Copyright (c) 2020-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.image;

public class ForkException extends Exception {

	private static final long serialVersionUID = 1L;

	public ForkException() {
		super();
	}

	public ForkException(String message) {
		super(message);
	}

	public ForkException(Throwable cause) {
		super(cause);
	}

	public ForkException(String message, Throwable cause) {
		super(message, cause);
	}

	public ForkException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
