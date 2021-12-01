/**
 * 
 */
package org.semanticweb.owlapi.formats;

import org.eclipse.rdf4j.rio.RDFFormat;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * @since 4.0.0
 */
public class N3DocumentFormat extends RioRDFPrefixDocumentFormat {

    /**
     * RDF format for {@link RDFFormat#N3} documents.
     */
    public N3DocumentFormat() {
        super(RDFFormat.N3);
    }
}
