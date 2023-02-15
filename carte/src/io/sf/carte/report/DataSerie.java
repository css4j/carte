/*

 Copyright (c) 2020-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.report;

import java.util.Iterator;

public interface DataSerie<T extends DiscreteValue> {

	DataInfo computeDataInfo();

	Iterator<T> iterator();

	default T get(int index) {
		throw new UnsupportedOperationException();
	}

}
