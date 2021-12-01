package org.semanticweb.owlapitools.decomposition;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyRange;

/**
 * check whether class expressions are equivalent to bottom wrt given locality
 * class
 */
// XXX verify unused parameters
public class BotEquivalenceEvaluator extends SigAccessor implements OWLObjectVisitor {

    /**
     * keep the value here
     */
    boolean isBotEq = false;

    /**
     * @param l l
     */
    public BotEquivalenceEvaluator(LocalityChecker l) {
        super(l);
    }

    /**
     * non-empty Concept/Data expression
     *
     * @param c class
     * @return true iff C^I is non-empty
     */
    private boolean isBotDistinct(OWLObject c) {
        // TOP is non-empty
        if (localityChecker.isTopEquivalent(c)) {
            return true;
        }
        // built-in DT are non-empty
        // FIXME!! that's it for now
        return c instanceof OWLDatatype;
    }

    /**
     * cardinality of a concept/data expression interpretation
     *
     * @param c class
     * @param n cardinality
     * @return true if #C^I > n
     */
    private boolean isCardLargerThan(OWLObject c, int n) {
        if (n == 0) {
            return isBotDistinct(c);
        }
        if (c instanceof OWLDatatype) {
            return ((OWLDatatype) c).isBuiltIn() && !((OWLDatatype) c).getBuiltInDatatype()
                .isFinite();
        }
        // FIXME!! try to be more precise
        return false;
    }

    /**
     * @param n cardinality
     * @param r property
     * @param c class
     * @return true iff (<= n R.C) is botEq
     */
    private boolean isMaxBotEquivalent(int n, OWLPropertyExpression r, OWLPropertyRange c) {
        return isBotEquivalent(r) && isCardLargerThan(c, n);
    }

    /**
     * QCRs
     *
     * @param n cardinality
     * @param r property
     * @param c class
     * @return true iff (>= n R.C) is botEq
     */
    private boolean isMinBotEquivalent(int n, OWLPropertyExpression r, OWLPropertyRange c) {
        return n > 0 && (isBotEquivalent(r) || isBotEquivalent(c));
    }

    /**
     * @param expr expression
     * @return true iff an EXPRession is equivalent to bottom wrt defined policy
     */
    boolean isBotEquivalent(OWLObject expr) {
        if (expr.isBottomEntity()) {
            return true;
        }
        if (expr.isTopEntity()) {
            return false;
        }
        expr.accept(this);
        return isBotEq;
    }

    @Override
    public void visit(OWLClass expr) {
        isBotEq = !getSignature().topCLocal() && !getSignature().contains(expr);
    }

    @Override
    public void visit(OWLObjectComplementOf expr) {
        isBotEq = localityChecker.isTopEquivalent(expr.getOperand());
    }

    @Override
    public void visit(OWLObjectIntersectionOf expr) {
        isBotEq = expr.operands().anyMatch(this::isBotEquivalent);
    }

    @Override
    public void visit(OWLObjectUnionOf expr) {
        isBotEq = !expr.operands().anyMatch(p -> !isBotEquivalent(p));
    }

    @Override
    public void visit(OWLObjectOneOf expr) {
        isBotEq = expr.individuals().count() == 0;
    }

    @Override
    public void visit(OWLObjectHasSelf expr) {
        isBotEq = isBotEquivalent(expr.getProperty());
    }

    @Override
    public void visit(OWLObjectHasValue expr) {
        isBotEq = isBotEquivalent(expr.getProperty());
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom expr) {
        isBotEq = isMinBotEquivalent(1, expr.getProperty(), expr.getFiller());
    }

    @Override
    public void visit(OWLObjectAllValuesFrom expr) {
        isBotEq = localityChecker.isTopEquivalent(expr.getProperty()) && isBotEquivalent(
            expr.getFiller());
    }

    @Override
    public void visit(OWLObjectMinCardinality expr) {
        isBotEq = isMinBotEquivalent(expr.getCardinality(), expr.getProperty(), expr.getFiller());
    }

    @Override
    public void visit(OWLObjectMaxCardinality expr) {
        isBotEq = isMaxBotEquivalent(expr.getCardinality(), expr.getProperty(), expr.getFiller());
    }

    @Override
    public void visit(OWLObjectExactCardinality expr) {
        int n = expr.getCardinality();
        isBotEq =
            isMinBotEquivalent(n, expr.getProperty(), expr.getFiller()) || isMaxBotEquivalent(n,
                expr
                    .getProperty(), expr.getFiller());
    }

    @Override
    public void visit(OWLDataHasValue expr) {
        isBotEq = isBotEquivalent(expr.getProperty());
    }

    @Override
    public void visit(OWLDataSomeValuesFrom expr) {
        isBotEq = isMinBotEquivalent(1, expr.getProperty(), expr.getFiller());
    }

    @Override
    public void visit(OWLDataAllValuesFrom expr) {
        isBotEq = localityChecker.isTopEquivalent(expr.getProperty()) && !localityChecker
            .isTopEquivalent(expr
                .getFiller());
    }

    @Override
    public void visit(OWLDataMinCardinality expr) {
        isBotEq = isMinBotEquivalent(expr.getCardinality(), expr.getProperty(), expr.getFiller());
    }

    @Override
    public void visit(OWLDataMaxCardinality expr) {
        isBotEq = isMaxBotEquivalent(expr.getCardinality(), expr.getProperty(), expr.getFiller());
    }

    @Override
    public void visit(OWLDataExactCardinality expr) {
        int n = expr.getCardinality();
        isBotEq =
            isMinBotEquivalent(n, expr.getProperty(), expr.getFiller()) || isMaxBotEquivalent(n,
                expr
                    .getProperty(), expr.getFiller());
    }

    @Override
    public void visit(OWLObjectProperty expr) {
        isBotEq = !getSignature().topRLocal() && !getSignature().contains(expr);
    }

    @Override
    public void visit(OWLObjectInverseOf expr) {
        isBotEq = isBotEquivalent(expr.getInverse());
    }

    @Override
    public void visit(OWLDataProperty expr) {
        isBotEq = !getSignature().topRLocal() && !getSignature().contains(expr);
    }

    @Override
    public void visit(OWLDatatype node) {
        isBotEq = node.isBottomEntity();
    }

    @Override
    public void visit(OWLLiteral node) {
        isBotEq = false;
    }

    @Override
    public void visit(OWLDataComplementOf node) {
        isBotEq = localityChecker.isTopEquivalent(node.getDataRange());
    }

    @Override
    public void visit(OWLDataOneOf node) {
        isBotEq = node.values().count() == 0;
    }
}
