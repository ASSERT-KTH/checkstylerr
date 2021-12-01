package org.semanticweb.owlapi.rdf.rdfxml.parser;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.verifyNotNull;

import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.PrefixManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for triple logging functions.
 *
 * @author ignazio
 * @since 4.0.0
 */
public class TripleLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(TripleLogger.class);
    @Nullable
    private PrefixManager prefixManager;
    // Debug stuff
    private final AtomicInteger count = new AtomicInteger();

    /**
     * @param prefixManager prefix manager
     */
    public void setPrefixManager(@Nullable PrefixManager prefixManager) {
        this.prefixManager = prefixManager;
    }

    /**
     * @return triples counted
     */
    public int count() {
        return count.get();
    }

    /**
     * Log triples at debug level and increment triple count.
     *
     * @param s subject
     * @param p predicate
     * @param o object
     */
    public void logTriple(Object s, Object p, Object o) {
        justLog(s, p, o);
        incrementTripleCount();
    }

    /**
     * Log triples at debug level, including language and datatype, and
     * increment triple count.
     *
     * @param s subject
     * @param p predicate
     * @param o object
     * @param lang language
     * @param datatype datatype
     */
    public void logTriple(Object s, Object p, Object o, @Nullable Object lang,
        @Nullable Object datatype) {
        justLog(s, p, o, lang, datatype);
        incrementTripleCount();
    }

    /**
     * @param s subject
     * @param p predicate
     * @param o object
     * @param lang language
     * @param datatype datatype
     */
    public void justLog(Object s, Object p, Object o, @Nullable Object lang,
        @Nullable Object datatype) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("s={} p={} o={} l={} dt={}", shorten(s), shorten(p), shorten(o), lang,
                shorten(datatype));
        }
    }

    /**
     * @param s subject
     * @param p predicate
     * @param o object
     */
    public void justLog(Object s, Object p, Object o) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("s={} p={} o={}", shorten(s), shorten(p), shorten(o));
        }
    }

    private Object shorten(@Nullable Object o) {
        if (o == null) {
            return "null";
        }
        if (o instanceof String && (((String) o).startsWith("http:") || ((String) o)
            .startsWith("urn:"))) {
            return shorten(IRI.create((String) o));
        }
        if (prefixManager == null || !(o instanceof IRI)) {
            // quote strings and bnodes
            return "\"" + o + '"';
        }
        // there is a prefix manager and o is an IRI
        IRI i = (IRI) o;
        String result = verifyNotNull(prefixManager).getPrefixIRI(i);
        if (result == null) {
            result = i.toQuotedString();
        }
        return result;
    }

    /**
     * increment count and log.
     */
    private void incrementTripleCount() {
        if (count.incrementAndGet() % 10000 == 0) {
            LOGGER.debug("Parsed: {} triples", count);
        }
    }

    /**
     * log finl count.
     */
    public void logNumberOfTriples() {
        LOGGER.debug("Total number of triples: {}", count);
    }

    /**
     * @param id log ontology id
     */
    public static void logOntologyID(OWLOntologyID id) {
        LOGGER.debug("Loaded {}", id);
    }
}
