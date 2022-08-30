/*

 Copyright (c) 2020-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.image;

public class ImageConversionException extends Exception {

	private static final long serialVersionUID = 1L;

	public ImageConversionException() {
		super();
	}

	public ImageConversionException(String message) {
		super(message);
	}

	public ImageConversionException(Throwable cause) {
		super(cause);
	}

	public ImageConversionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImageConversionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
