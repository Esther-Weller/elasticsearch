/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.esql.plan.logical;

import org.elasticsearch.xpack.ql.capabilities.Resolvables;
import org.elasticsearch.xpack.ql.expression.Attribute;
import org.elasticsearch.xpack.ql.expression.Expressions;
import org.elasticsearch.xpack.ql.expression.NamedExpression;
import org.elasticsearch.xpack.ql.plan.logical.LogicalPlan;
import org.elasticsearch.xpack.ql.plan.logical.UnaryPlan;
import org.elasticsearch.xpack.ql.tree.NodeInfo;
import org.elasticsearch.xpack.ql.tree.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Eval extends UnaryPlan {

    private final List<NamedExpression> fields;

    public Eval(Source source, LogicalPlan child, List<NamedExpression> fields) {
        super(source, child);
        this.fields = fields;
    }

    public List<NamedExpression> fields() {
        return fields;
    }

    @Override
    public List<Attribute> output() {
        Set<String> fieldNames = fields.stream().map(NamedExpression::name).collect(Collectors.toSet());
        List<Attribute> childOutput = child().output();
        List<Attribute> output = new ArrayList<>(childOutput.size() + fields.size());
        for (Attribute childAttr : childOutput) {
            if (fieldNames.contains(childAttr.name()) == false) {
                output.add(childAttr);
            }
        }
        output.addAll(Expressions.asAttributes(fields));
        return output;
    }

    @Override
    public boolean expressionsResolved() {
        return Resolvables.resolved(fields);
    }

    @Override
    public UnaryPlan replaceChild(LogicalPlan newChild) {
        return new Eval(source(), newChild, fields);
    }

    @Override
    protected NodeInfo<? extends LogicalPlan> info() {
        return NodeInfo.create(this, Eval::new, child(), fields);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Eval eval = (Eval) o;
        return child().equals(eval.child()) && Objects.equals(fields, eval.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fields);
    }
}
