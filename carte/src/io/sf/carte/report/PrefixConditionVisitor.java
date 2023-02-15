/*

 Copyright (c) 2020-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.report;

import io.sf.carte.doc.style.css.nsac.AttributeCondition;
import io.sf.carte.doc.style.css.nsac.Condition.ConditionType;
import io.sf.carte.doc.style.css.parser.AttributeConditionVisitor;

class PrefixConditionVisitor extends AttributeConditionVisitor {

	private final String prefix;

	PrefixConditionVisitor(String prefix) {
		super();
		this.prefix = prefix;
	}

	@Override
	public void visit(AttributeCondition condition) {
		ConditionType type = condition.getConditionType();
		if (type == ConditionType.ID || type == ConditionType.CLASS) {
			String oldName = condition.getValue();
			String newName = prefix + oldName;
			setConditionValue(condition, newName);
		}
	}

}
