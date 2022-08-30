/*

 Copyright (c) 2020-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.report;

import io.sf.carte.doc.style.css.CSSStyleRule;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.parser.AttributeConditionVisitor;
import io.sf.carte.util.Visitor;

class SelectorRuleVisitor implements Visitor<CSSStyleRule> {

	private final AttributeConditionVisitor visitor;

	SelectorRuleVisitor(AttributeConditionVisitor visitor) {
		super();
		this.visitor = visitor;
	}

	@Override
	public void visit(CSSStyleRule rule) {
		SelectorList selist = rule.getSelectorList();
		visitor.visit(selist);
		rule.setSelectorList(selist);
	}

}
