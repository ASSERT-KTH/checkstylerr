/* KRSSParser.java */
/* Generated By:JavaCC: Do not edit this line. KRSSParser.java */
package org.semanticweb.owlapi.krss1.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.vocab.Namespaces;

@SuppressWarnings("all")
public class KRSSParser implements KRSSParserConstants {

    private OWLOntology ontology;
    private OWLDataFactory df;
    private Map<String, IRI> string2IRI;
    private boolean ignoreAnnotationsAndDeclarations = false;
    private String base;

    public KRSSParser setOntology(OWLOntology ontology) {
        this.ontology = ontology;
        this.df = ontology.getOWLOntologyManager().getOWLDataFactory();
        string2IRI = new HashMap<String, IRI>();
        if (!ontology.isAnonymous()) {
            base = ontology.getOntologyID().getOntologyIRI() + "#";
        } else {
            base = Namespaces.OWL.toString();
        }
        return this;
    }

    protected void addAxiom(OWLAxiom ax) throws KRSSOWLParserException {
        if (ax == null) {
            return;
        }
        try {
            ontology.addAxiom(ax);
        } catch (OWLOntologyChangeException e) {
            throw new KRSSOWLParserException(e);
        }
    }

    public IRI getIRI(String s) {
        s = base + s;
        IRI iri = string2IRI.get(s);
        if (iri == null) {
            iri = IRI.create(s);
            string2IRI.put(s, iri);
        }
        return iri;
    }

    public void setIgnoreAnnotationsAndDeclarations(boolean b) {
        ignoreAnnotationsAndDeclarations = b;
    }

    final public void parse() throws ParseException, KRSSOWLParserException {
        OWLAxiom ax;
        label_1:
        while (true) {
            if (jj_2_1(2)) {
                ;
            } else {
                break label_1;
            }
            ax = TBoxStatement();
            addAxiom(ax);
        }
        if (jj_2_2(2)) {
            jj_consume_token(ENDTBOX);
        } else {
            ;
        }
        label_2:
        while (true) {
            switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
                case OPENPAR: {
                    ;
                    break;
                }
                default:
                    jj_la1[0] = jj_gen;
                    break label_2;
            }
            ABoxStatement();
        }
        if (jj_2_3(2)) {
            jj_consume_token(ENDABOX);
        } else {
            ;
        }
        jj_consume_token(0);
    }

    final public OWLAxiom TBoxStatement() throws ParseException {
        OWLAxiom ax;
        if (jj_2_4(2)) {
            ax = DefinePrimitiveConcept();
        } else if (jj_2_5(2)) {
            ax = DefineConcept();
        } else if (jj_2_6(2)) {
            ax = DefinePrimitiveRole();
        } else if (jj_2_7(2)) {
            ax = Transitive();
        } else if (jj_2_8(2)) {
            ax = Range();
        } else {
            jj_consume_token(-1);
            throw new ParseException();
        }
        return ax;
    }

    final public OWLAxiom DefinePrimitiveConcept() throws ParseException {
        OWLClassExpression subClass;
        OWLClassExpression superClass;
        jj_consume_token(OPENPAR);
        jj_consume_token(DEFINEPRIMITIVECONCEPT);
        subClass = ConceptName();
        superClass = ConceptExpression();
        jj_consume_token(CLOSEPAR);
        return df.getOWLSubClassOfAxiom(subClass, superClass);
    }

    final public OWLAxiom DefineConcept() throws ParseException {
        OWLClassExpression clsA;
        OWLClassExpression clsB;
        jj_consume_token(OPENPAR);
        jj_consume_token(DEFINECONCEPT);
        clsA = ConceptName();
        clsB = ConceptExpression();
        jj_consume_token(CLOSEPAR);
        return df.getOWLEquivalentClassesAxiom(clsA, clsB);
    }

    final public OWLAxiom DefinePrimitiveRole() throws ParseException {
        OWLObjectProperty subProp;
        OWLObjectProperty superProp;
        OWLAxiom ax = null;
        jj_consume_token(OPENPAR);
        jj_consume_token(DEFINEPRIMITIVEROLE);
        subProp = RoleName();
        superProp = RoleName();
        switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case 42: {
                jj_consume_token(42);
                RoleName();
                break;
            }
            default:
                jj_la1[1] = jj_gen;
                ;
        }
        jj_consume_token(CLOSEPAR);
        if (superProp != null) {
            ax = df.getOWLSubObjectPropertyOfAxiom(subProp, superProp);
        }
        return ax;
    }

    final public OWLAxiom Transitive() throws ParseException {
        OWLObjectProperty prop;
        jj_consume_token(OPENPAR);
        jj_consume_token(TRANSITIVE);
        prop = RoleName();
        jj_consume_token(CLOSEPAR);
        return df.getOWLTransitiveObjectPropertyAxiom(prop);
    }

    final public OWLAxiom Range() throws ParseException {
        OWLObjectProperty prop;
        OWLClassExpression rng;
        jj_consume_token(OPENPAR);
        jj_consume_token(RANGE);
        prop = RoleName();
        rng = ConceptExpression();
        jj_consume_token(CLOSEPAR);
        return df.getOWLObjectPropertyRangeAxiom(prop, rng);
    }

    final public OWLClassExpression ConceptExpression() throws ParseException {
        OWLClassExpression desc;
        switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case NAME: {
                desc = ConceptName();
                break;
            }
            default:
                jj_la1[2] = jj_gen;
                if (jj_2_9(2)) {
                    desc = And();
                } else if (jj_2_10(2)) {
                    desc = Or();
                } else if (jj_2_11(2)) {
                    desc = Not();
                } else if (jj_2_12(2)) {
                    desc = All();
                } else if (jj_2_13(2)) {
                    desc = Some();
                } else if (jj_2_14(2)) {
                    desc = AtLeast();
                } else if (jj_2_15(2)) {
                    desc = AtMost();
                } else if (jj_2_16(2)) {
                    desc = Exactly();
                } else {
                    jj_consume_token(-1);
                    throw new ParseException();
                }
        }
        return desc;
    }

    final public OWLClassExpression ConceptName() throws ParseException {
        IRI iri;
        iri = Name();
        return df.getOWLClass(iri);
    }

    final public Set<OWLClassExpression> ConceptSet() throws ParseException {
        Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>();
        OWLClassExpression desc;
        label_3:
        while (true) {
            desc = ConceptExpression();
            descs.add(desc);
            switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
                case OPENPAR:
                case NAME: {
                    ;
                    break;
                }
                default:
                    jj_la1[3] = jj_gen;
                    break label_3;
            }
        }
        return descs;
    }

    final public OWLClassExpression And() throws ParseException {
        Set<OWLClassExpression> operands;
        jj_consume_token(OPENPAR);
        jj_consume_token(AND);
        operands = ConceptSet();
        jj_consume_token(CLOSEPAR);
        return df.getOWLObjectIntersectionOf(operands);
    }

    final public OWLClassExpression Or() throws ParseException {
        Set<OWLClassExpression> operands;
        jj_consume_token(OPENPAR);
        jj_consume_token(OR);
        operands = ConceptSet();
        jj_consume_token(CLOSEPAR);
        return df.getOWLObjectUnionOf(operands);
    }

    final public OWLClassExpression Not() throws ParseException {
        OWLClassExpression operand;
        jj_consume_token(OPENPAR);
        jj_consume_token(NOT);
        operand = ConceptExpression();
        jj_consume_token(CLOSEPAR);
        return df.getOWLObjectComplementOf(operand);
    }

    final public OWLClassExpression All() throws ParseException {
        OWLObjectProperty prop;
        OWLClassExpression filler;
        jj_consume_token(OPENPAR);
        jj_consume_token(ALL);
        prop = RoleName();
        filler = ConceptExpression();
        jj_consume_token(CLOSEPAR);
        return df.getOWLObjectAllValuesFrom(prop, filler);
    }

    final public OWLClassExpression Some() throws ParseException {
        OWLObjectProperty prop;
        OWLClassExpression filler;
        jj_consume_token(OPENPAR);
        jj_consume_token(SOME);
        prop = RoleName();
        filler = ConceptExpression();
        jj_consume_token(CLOSEPAR);
        return df.getOWLObjectSomeValuesFrom(prop, filler);
    }

    final public OWLClassExpression AtLeast() throws ParseException {
        OWLObjectProperty prop;
        OWLClassExpression filler;
        int card;
        jj_consume_token(OPENPAR);
        jj_consume_token(ATLEAST);
        card = Integer();
        prop = RoleName();
        filler = ConceptExpression();
        jj_consume_token(CLOSEPAR);
        return df.getOWLObjectMinCardinality(card, prop, filler);
    }

    final public OWLClassExpression AtMost() throws ParseException {
        OWLObjectProperty prop;
        OWLClassExpression filler;
        int card;
        jj_consume_token(OPENPAR);
        jj_consume_token(ATMOST);
        card = Integer();
        prop = RoleName();
        filler = ConceptExpression();
        jj_consume_token(CLOSEPAR);
        return df.getOWLObjectMaxCardinality(card, prop, filler);
    }

    final public OWLClassExpression Exactly() throws ParseException {
        OWLObjectProperty prop;
        OWLClassExpression filler;
        int card;
        jj_consume_token(OPENPAR);
        jj_consume_token(EXACTLY);
        card = Integer();
        prop = RoleName();
        filler = ConceptExpression();
        jj_consume_token(CLOSEPAR);
        return df.getOWLObjectExactCardinality(card, prop, filler);
    }

    final public OWLObjectProperty RoleName() throws ParseException {
        IRI iri;
        iri = Name();
        return df.getOWLObjectProperty(iri);
    }

    final public OWLAxiom ABoxStatement() throws ParseException {
        OWLAxiom ax;
        if (jj_2_17(2)) {
            ax = Instance();
        } else if (jj_2_18(2)) {
            ax = Related();
        } else if (jj_2_19(2)) {
            ax = Equal();
        } else if (jj_2_20(2)) {
            ax = Distinct();
        } else {
            jj_consume_token(-1);
            throw new ParseException();
        }
        return ax;
    }

    final public OWLAxiom Instance() throws ParseException {
        OWLIndividual ind;
        OWLClassExpression type;
        jj_consume_token(OPENPAR);
        jj_consume_token(INSTANCE);
        ind = IndividualName();
        type = ConceptExpression();
        jj_consume_token(CLOSEPAR);
        return df.getOWLClassAssertionAxiom(type, ind);
    }

    final public OWLAxiom Related() throws ParseException {
        OWLIndividual subj;
        OWLObjectProperty prop;
        OWLIndividual obj;
        jj_consume_token(OPENPAR);
        jj_consume_token(RELATED);
        subj = IndividualName();
        prop = RoleName();
        obj = IndividualName();
        jj_consume_token(CLOSEPAR);
        return df.getOWLObjectPropertyAssertionAxiom(prop, subj, obj);
    }

    final public OWLAxiom Equal() throws ParseException {
        OWLIndividual indA, indB;
        jj_consume_token(OPENPAR);
        jj_consume_token(EQUAL);
        indA = IndividualName();
        indB = IndividualName();
        jj_consume_token(CLOSEPAR);
        return df.getOWLSameIndividualAxiom(indA, indB);
    }

    final public OWLAxiom Distinct() throws ParseException {
        OWLIndividual indA, indB;
        jj_consume_token(OPENPAR);
        jj_consume_token(DISTINCT);
        indA = IndividualName();
        indB = IndividualName();
        jj_consume_token(CLOSEPAR);
        return df.getOWLDifferentIndividualsAxiom(indA, indB);
    }

    final public OWLIndividual IndividualName() throws ParseException {
        IRI name;
        name = Name();
        return df.getOWLNamedIndividual(name);
    }

    final public IRI Name() throws ParseException {
        Token t;
        t = jj_consume_token(NAME);
        return getIRI(t.image);
    }

    final public int Integer() throws ParseException {
        Token t;
        t = jj_consume_token(INT);
        return Integer.parseInt(t.image);
    }

    private boolean jj_2_1(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_1());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(0, xla);
        }
    }

    private boolean jj_2_2(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_2());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(1, xla);
        }
    }

    private boolean jj_2_3(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_3());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(2, xla);
        }
    }

    private boolean jj_2_4(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_4());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(3, xla);
        }
    }

    private boolean jj_2_5(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_5());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(4, xla);
        }
    }

    private boolean jj_2_6(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_6());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(5, xla);
        }
    }

    private boolean jj_2_7(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_7());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(6, xla);
        }
    }

    private boolean jj_2_8(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_8());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(7, xla);
        }
    }

    private boolean jj_2_9(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_9());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(8, xla);
        }
    }

    private boolean jj_2_10(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_10());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(9, xla);
        }
    }

    private boolean jj_2_11(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_11());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(10, xla);
        }
    }

    private boolean jj_2_12(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_12());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(11, xla);
        }
    }

    private boolean jj_2_13(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_13());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(12, xla);
        }
    }

    private boolean jj_2_14(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_14());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(13, xla);
        }
    }

    private boolean jj_2_15(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_15());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(14, xla);
        }
    }

    private boolean jj_2_16(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_16());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(15, xla);
        }
    }

    private boolean jj_2_17(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_17());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(16, xla);
        }
    }

    private boolean jj_2_18(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_18());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(17, xla);
        }
    }

    private boolean jj_2_19(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_19());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(18, xla);
        }
    }

    private boolean jj_2_20(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        try {
            return (!jj_3_20());
        } catch (LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(19, xla);
        }
    }

    private boolean jj_3_9() {
        if (jj_3R_10()) {
            return true;
        }
        return false;
    }

    private boolean jj_3R_20() {
        if (jj_scan_token(OPENPAR)) {
            return true;
        }
        if (jj_scan_token(EQUAL)) {
            return true;
        }
        return false;
    }

    private boolean jj_3R_19() {
        if (jj_scan_token(OPENPAR)) {
            return true;
        }
        if (jj_scan_token(RELATED)) {
            return true;
        }
        return false;
    }

    private boolean jj_3_2() {
        if (jj_scan_token(ENDTBOX)) {
            return true;
        }
        return false;
    }

    private boolean jj_3R_9() {
        if (jj_scan_token(OPENPAR)) {
            return true;
        }
        if (jj_scan_token(RANGE)) {
            return true;
        }
        return false;
    }

    private boolean jj_3R_18() {
        if (jj_scan_token(OPENPAR)) {
            return true;
        }
        if (jj_scan_token(INSTANCE)) {
            return true;
        }
        return false;
    }

    private boolean jj_3R_8() {
        if (jj_scan_token(OPENPAR)) {
            return true;
        }
        if (jj_scan_token(TRANSITIVE)) {
            return true;
        }
        return false;
    }

    private boolean jj_3_17() {
        if (jj_3R_18()) {
            return true;
        }
        return false;
    }

    private boolean jj_3R_7() {
        if (jj_scan_token(OPENPAR)) {
            return true;
        }
        if (jj_scan_token(DEFINEPRIMITIVEROLE)) {
            return true;
        }
        return false;
    }

    private boolean jj_3R_17() {
        if (jj_scan_token(OPENPAR)) {
            return true;
        }
        if (jj_scan_token(EXACTLY)) {
            return true;
        }
        return false;
    }

    private boolean jj_3R_16() {
        if (jj_scan_token(OPENPAR)) {
            return true;
        }
        if (jj_scan_token(ATMOST)) {
            return true;
        }
        return false;
    }

    private boolean jj_3R_6() {
        if (jj_scan_token(OPENPAR)) {
            return true;
        }
        if (jj_scan_token(DEFINECONCEPT)) {
            return true;
        }
        return false;
    }

    private boolean jj_3_20() {
        if (jj_3R_21()) {
            return true;
        }
        return false;
    }

    private boolean jj_3R_5() {
        if (jj_scan_token(OPENPAR)) {
            return true;
        }
        if (jj_scan_token(DEFINEPRIMITIVECONCEPT)) {
            return true;
        }
        return false;
    }

    private boolean jj_3R_15() {
        if (jj_scan_token(OPENPAR)) {
            return true;
        }
        if (jj_scan_token(ATLEAST)) {
            return true;
        }
        return false;
    }

    private boolean jj_3_8() {
        if (jj_3R_9()) {
            return true;
        }
        return false;
    }

    private boolean jj_3_7() {
        if (jj_3R_8()) {
            return true;
        }
        return false;
    }

    private boolean jj_3_6() {
        if (jj_3R_7()) {
            return true;
        }
        return false;
    }

    private boolean jj_3_5() {
        if (jj_3R_6()) {
            return true;
        }
        return false;
    }

    private boolean jj_3R_14() {
        if (jj_scan_token(OPENPAR)) {
            return true;
        }
        if (jj_scan_token(SOME)) {
            return true;
        }
        return false;
    }

    private boolean jj_3_4() {
        if (jj_3R_5()) {
            return true;
        }
        return false;
    }

    private boolean jj_3R_4() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3_4()) {
            jj_scanpos = xsp;
            if (jj_3_5()) {
                jj_scanpos = xsp;
                if (jj_3_6()) {
                    jj_scanpos = xsp;
                    if (jj_3_7()) {
                        jj_scanpos = xsp;
                        if (jj_3_8()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean jj_3_3() {
        if (jj_scan_token(ENDABOX)) {
            return true;
        }
        return false;
    }

    private boolean jj_3_1() {
        if (jj_3R_4()) {
            return true;
        }
        return false;
    }

    private boolean jj_3R_13() {
        if (jj_scan_token(OPENPAR)) {
            return true;
        }
        if (jj_scan_token(ALL)) {
            return true;
        }
        return false;
    }

    private boolean jj_3_19() {
        if (jj_3R_20()) {
            return true;
        }
        return false;
    }

    private boolean jj_3R_12() {
        if (jj_scan_token(OPENPAR)) {
            return true;
        }
        if (jj_scan_token(NOT)) {
            return true;
        }
        return false;
    }

    private boolean jj_3R_11() {
        if (jj_scan_token(OPENPAR)) {
            return true;
        }
        if (jj_scan_token(OR)) {
            return true;
        }
        return false;
    }

    private boolean jj_3R_10() {
        if (jj_scan_token(OPENPAR)) {
            return true;
        }
        if (jj_scan_token(AND)) {
            return true;
        }
        return false;
    }

    private boolean jj_3_18() {
        if (jj_3R_19()) {
            return true;
        }
        return false;
    }

    private boolean jj_3_16() {
        if (jj_3R_17()) {
            return true;
        }
        return false;
    }

    private boolean jj_3_15() {
        if (jj_3R_16()) {
            return true;
        }
        return false;
    }

    private boolean jj_3_14() {
        if (jj_3R_15()) {
            return true;
        }
        return false;
    }

    private boolean jj_3_13() {
        if (jj_3R_14()) {
            return true;
        }
        return false;
    }

    private boolean jj_3R_21() {
        if (jj_scan_token(OPENPAR)) {
            return true;
        }
        if (jj_scan_token(DISTINCT)) {
            return true;
        }
        return false;
    }

    private boolean jj_3_12() {
        if (jj_3R_13()) {
            return true;
        }
        return false;
    }

    private boolean jj_3_11() {
        if (jj_3R_12()) {
            return true;
        }
        return false;
    }

    private boolean jj_3_10() {
        if (jj_3R_11()) {
            return true;
        }
        return false;
    }

    /**
     * Generated Token Manager.
     */
    public KRSSParserTokenManager token_source;
    JavaCharStream jj_input_stream;
    /**
     * Current token.
     */
    public Token token;
    /**
     * Next token.
     */
    public Token jj_nt;
    private int jj_ntk;
    private Token jj_scanpos, jj_lastpos;
    private int jj_la;
    private int jj_gen;
    final private int[] jj_la1 = new int[4];
    static private int[] jj_la1_0;
    static private int[] jj_la1_1;

    static {
        jj_la1_init_0();
        jj_la1_init_1();
    }

    private static void jj_la1_init_0() {
        jj_la1_0 = new int[]{0x8000, 0x0, 0x0, 0x8000,};
    }

    private static void jj_la1_init_1() {
        jj_la1_1 = new int[]{0x0, 0x400, 0x100, 0x100,};
    }

    final private JJCalls[] jj_2_rtns = new JJCalls[20];
    private boolean jj_rescan = false;
    private int jj_gc = 0;

    /**
     * Constructor.
     */
    public KRSSParser(Provider stream) {
        jj_input_stream = new JavaCharStream(stream, 1, 1);
        token_source = new KRSSParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 4; i++) {
            jj_la1[i] = -1;
        }
        for (int i = 0; i < jj_2_rtns.length; i++) {
            jj_2_rtns[i] = new JJCalls();
        }
    }

    /**
     * Constructor.
     */
    public KRSSParser(String dsl) throws ParseException, TokenMgrException {
        this(new StringProvider(dsl));
    }

    public void ReInit(String s) {
        ReInit(new StringProvider(s));
    }

    /**
     * Reinitialise.
     */
    public void ReInit(Provider stream) {
        if (jj_input_stream == null) {
            jj_input_stream = new JavaCharStream(stream, 1, 1);
        } else {
            jj_input_stream.ReInit(stream, 1, 1);
        }
        if (token_source == null) {
            token_source = new KRSSParserTokenManager(jj_input_stream);
        }

        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 4; i++) {
            jj_la1[i] = -1;
        }
        for (int i = 0; i < jj_2_rtns.length; i++) {
            jj_2_rtns[i] = new JJCalls();
        }
    }

    /**
     * Constructor with generated Token Manager.
     */
    public KRSSParser(KRSSParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 4; i++) {
            jj_la1[i] = -1;
        }
        for (int i = 0; i < jj_2_rtns.length; i++) {
            jj_2_rtns[i] = new JJCalls();
        }
    }

    /**
     * Reinitialise.
     */
    public void ReInit(KRSSParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 4; i++) {
            jj_la1[i] = -1;
        }
        for (int i = 0; i < jj_2_rtns.length; i++) {
            jj_2_rtns[i] = new JJCalls();
        }
    }

    private Token jj_consume_token(int kind) throws ParseException {
        Token oldToken;
        if ((oldToken = token).next != null) {
            token = token.next;
        } else {
            token = token.next = token_source.getNextToken();
        }
        jj_ntk = -1;
        if (token.kind == kind) {
            jj_gen++;
            if (++jj_gc > 100) {
                jj_gc = 0;
                for (int i = 0; i < jj_2_rtns.length; i++) {
                    JJCalls c = jj_2_rtns[i];
                    while (c != null) {
                        if (c.gen < jj_gen) {
                            c.first = null;
                        }
                        c = c.next;
                    }
                }
            }
            return token;
        }
        token = oldToken;
        jj_kind = kind;
        throw generateParseException();
    }

    @SuppressWarnings("serial")
    static private final class LookaheadSuccess extends java.lang.RuntimeException {

    }

    final private LookaheadSuccess jj_ls = new LookaheadSuccess();

    private boolean jj_scan_token(int kind) {
        if (jj_scanpos == jj_lastpos) {
            jj_la--;
            if (jj_scanpos.next == null) {
                jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
            } else {
                jj_lastpos = jj_scanpos = jj_scanpos.next;
            }
        } else {
            jj_scanpos = jj_scanpos.next;
        }
        if (jj_rescan) {
            int i = 0;
            Token tok = token;
            while (tok != null && tok != jj_scanpos) {
                i++;
                tok = tok.next;
            }
            if (tok != null) {
                jj_add_error_token(kind, i);
            }
        }
        if (jj_scanpos.kind != kind) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            throw jj_ls;
        }
        return false;
    }


    /**
     * Get the next Token.
     */
    final public Token getNextToken() {
        if (token.next != null) {
            token = token.next;
        } else {
            token = token.next = token_source.getNextToken();
        }
        jj_ntk = -1;
        jj_gen++;
        return token;
    }

    /**
     * Get the specific Token.
     */
    final public Token getToken(int index) {
        Token t = token;
        for (int i = 0; i < index; i++) {
            if (t.next != null) {
                t = t.next;
            } else {
                t = t.next = token_source.getNextToken();
            }
        }
        return t;
    }

    private int jj_ntk_f() {
        if ((jj_nt = token.next) == null) {
            return (jj_ntk = (token.next = token_source.getNextToken()).kind);
        } else {
            return (jj_ntk = jj_nt.kind);
        }
    }

    private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
    private int[] jj_expentry;
    private int jj_kind = -1;
    private int[] jj_lasttokens = new int[100];
    private int jj_endpos;

    private void jj_add_error_token(int kind, int pos) {
        if (pos >= 100) {
            return;
        }

        if (pos == jj_endpos + 1) {
            jj_lasttokens[jj_endpos++] = kind;
        } else if (jj_endpos != 0) {
            jj_expentry = new int[jj_endpos];

            for (int i = 0; i < jj_endpos; i++) {
                jj_expentry[i] = jj_lasttokens[i];
            }

            for (int[] oldentry : jj_expentries) {
                if (oldentry.length == jj_expentry.length) {
                    boolean isMatched = true;

                    for (int i = 0; i < jj_expentry.length; i++) {
                        if (oldentry[i] != jj_expentry[i]) {
                            isMatched = false;
                            break;
                        }

                    }
                    if (isMatched) {
                        jj_expentries.add(jj_expentry);
                        break;
                    }
                }
            }

            if (pos != 0) {
                jj_lasttokens[(jj_endpos = pos) - 1] = kind;
            }
        }
    }

    /**
     * Generate ParseException.
     */
    public ParseException generateParseException() {
        jj_expentries.clear();
        boolean[] la1tokens = new boolean[43];
        if (jj_kind >= 0) {
            la1tokens[jj_kind] = true;
            jj_kind = -1;
        }
        for (int i = 0; i < 4; i++) {
            if (jj_la1[i] == jj_gen) {
                for (int j = 0; j < 32; j++) {
                    if ((jj_la1_0[i] & (1 << j)) != 0) {
                        la1tokens[j] = true;
                    }
                    if ((jj_la1_1[i] & (1 << j)) != 0) {
                        la1tokens[32 + j] = true;
                    }
                }
            }
        }
        for (int i = 0; i < 43; i++) {
            if (la1tokens[i]) {
                jj_expentry = new int[1];
                jj_expentry[0] = i;
                jj_expentries.add(jj_expentry);
            }
        }
        jj_endpos = 0;
        jj_rescan_token();
        jj_add_error_token(0, 0);
        int[][] exptokseq = new int[jj_expentries.size()][];
        for (int i = 0; i < jj_expentries.size(); i++) {
            exptokseq[i] = jj_expentries.get(i);
        }
        return new ParseException(token, exptokseq, tokenImage, token_source == null ? null
            : KRSSParserTokenManager.lexStateNames[token_source.curLexState]);
    }

    private int trace_indent = 0;
    private boolean trace_enabled;

    /**
     * Trace enabled.
     */
    final public boolean trace_enabled() {
        return trace_enabled;
    }

    /**
     * Enable tracing.
     */
    final public void enable_tracing() {
    }

    /**
     * Disable tracing.
     */
    final public void disable_tracing() {
    }

    private void jj_rescan_token() {
        jj_rescan = true;
        for (int i = 0; i < 20; i++) {
            try {
                JJCalls p = jj_2_rtns[i];

                do {
                    if (p.gen > jj_gen) {
                        jj_la = p.arg;
                        jj_lastpos = jj_scanpos = p.first;
                        switch (i) {
                            case 0:
                                jj_3_1();
                                break;
                            case 1:
                                jj_3_2();
                                break;
                            case 2:
                                jj_3_3();
                                break;
                            case 3:
                                jj_3_4();
                                break;
                            case 4:
                                jj_3_5();
                                break;
                            case 5:
                                jj_3_6();
                                break;
                            case 6:
                                jj_3_7();
                                break;
                            case 7:
                                jj_3_8();
                                break;
                            case 8:
                                jj_3_9();
                                break;
                            case 9:
                                jj_3_10();
                                break;
                            case 10:
                                jj_3_11();
                                break;
                            case 11:
                                jj_3_12();
                                break;
                            case 12:
                                jj_3_13();
                                break;
                            case 13:
                                jj_3_14();
                                break;
                            case 14:
                                jj_3_15();
                                break;
                            case 15:
                                jj_3_16();
                                break;
                            case 16:
                                jj_3_17();
                                break;
                            case 17:
                                jj_3_18();
                                break;
                            case 18:
                                jj_3_19();
                                break;
                            case 19:
                                jj_3_20();
                                break;
                        }
                    }
                    p = p.next;
                } while (p != null);

            } catch (LookaheadSuccess ls) {
            }
        }
        jj_rescan = false;
    }

    private void jj_save(int index, int xla) {
        JJCalls p = jj_2_rtns[index];
        while (p.gen > jj_gen) {
            if (p.next == null) {
                p = p.next = new JJCalls();
                break;
            }
            p = p.next;
        }

        p.gen = jj_gen + xla - jj_la;
        p.first = token;
        p.arg = xla;
    }

    static final class JJCalls {

        int gen;
        Token first;
        int arg;
        JJCalls next;
    }

}