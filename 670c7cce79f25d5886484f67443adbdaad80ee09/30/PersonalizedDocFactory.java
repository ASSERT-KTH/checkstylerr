package ai.preferred.cerebro.index.lsh.builder;

import ai.preferred.cerebro.index.ids.ExternalID;
import ai.preferred.cerebro.index.handler.VecHandler;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;

import ai.preferred.cerebro.index.lsh.exception.DocNotClearedException;
import ai.preferred.cerebro.index.lsh.exception.SameNameException;
import ai.preferred.cerebro.index.utils.IndexConst;
/**
 * This class handles the creation of Document object
 * to ensure that there is no conflict in field name
 * and that all the hashcoding behaves as intended.
 *
 * @author hpminh@apcs.vn
 */
public class PersonalizedDocFactory<TVector> {

    private LocalitySensitiveHash<TVector> hashFunc = null;
    private Document doc;
    private VecHandler<TVector> handler;

    /**
     * Instantiate with a set of hashing vectors.
     * @param splitVecs
     */
    public PersonalizedDocFactory(VecHandler<TVector> handler, TVector[] splitVecs){
        assert splitVecs.length > 0;
        hashFunc = new LocalitySensitiveHash<>(handler, splitVecs);
        this.handler = handler;
    }

    public PersonalizedDocFactory(){}



    public void setHashFunc(LocalitySensitiveHash<TVector> hashFunc) {
        this.hashFunc = hashFunc;
    }


    /**
     * Only use this function to construct a Document containing latent vector.
     * To add additional fields to the Document, use {@link #addField(IndexableField...)}.
     * Call {@link #getDoc()} to pass the Document to IndexWriter or before creating a new Document.
     *
     * @param ID unique ID of the document.
     * @param features the latent feature vector to index.
     * @throws DocNotClearedException this exception is triggered when
     * a call to {@link #createPersonalizedDoc(ExternalID, TVector)} is not paired with a
     * call to {@link #getDoc()}.
     */
    public void createPersonalizedDoc(ExternalID ID, TVector features) throws Exception {
        if(this.doc != null)
            throw new DocNotClearedException();
        if(this.hashFunc == null)
            throw new Exception("Hashing Vecs not provided");
        this.doc = new Document();
        StringField idField = new StringField(IndexConst.IDFieldName, new BytesRef(ID.getByteValues()), Field.Store.YES);
        doc.add(idField);
        /* Storing double vector */
        StoredField vecField = new StoredField(IndexConst.VecFieldName, handler.vecToBytes(features));
        doc.add(vecField);
        /* adding hashcode */
        BytesRef hashcode = hashFunc.getHashBit(features);
        doc.add(new StringField(IndexConst.HashFieldName, hashcode, Field.Store.YES));
    }


    /**
     * Call this function to construct a generic text-only Document.
     * Should you need to add latent vector later call getDoc
     * and start anew with the other createPersonalizedDoc method.
     *
     * @param ID unique ID of the document
     * @param fields the custom fields
     * @throws SameNameException this is triggered when one of your custom field has name
     * identical to Cerebro reserved word. See more detail at {@link IndexConst}.
     * @throws DocNotClearedException this exception is triggered when
     * a call to {@link #createPersonalizedDoc(ExternalID, TVector)} is not paired with a
     * call to {@link #getDoc()}.
     */
    public void createTextDoc(ExternalID ID, IndexableField... fields) throws SameNameException, DocNotClearedException{
        if(this.doc != null){
            throw new DocNotClearedException();
        }
        this.doc = new Document();
        StringField idField = new StringField(IndexConst.IDFieldName, new BytesRef(ID.getByteValues()), Field.Store.YES);
        doc.add(idField);
        for(IndexableField field : fields){
            if(checkReservedFieldName(field.name()))
                throw new SameNameException();
            this.doc.add(field);
        }
    }

    /**
     * After calling {@link #createPersonalizedDoc(ExternalID, TVector)} to createPersonalizedDoc a document with latent vector
     * if you still want add more custom fields to a Document then use this function.
     *
     * @param fields fields to add to the {@link Document}
     *               instance at the pointer {@link PersonalizedDocFactory#doc}
     * @throws SameNameException this is triggered when one of your custom field has name
     * identical to Cerebro reserved word. See more detail at {@link IndexConst}.
     *
     *
     *
     */
    public void addField(IndexableField... fields) throws SameNameException {
        for(IndexableField f : fields){
            /* Name of any other fields must not coincide with the name of any reserved field */
            if(checkReservedFieldName(f.name()))
                throw new SameNameException();
            this.doc.add(f);
        }
    }

    /**
     * After calling this function the pointer doc become null again.
     *
     * @return the Document object being built since the last {@link #createPersonalizedDoc(ExternalID, TVector)}
     * or {@link #createTextDoc(ExternalID, IndexableField...)} call.
     */
    public Document getDoc(){
        Document t = this.doc;
        this.doc = null;
        return t;
    }

    /**
     * Check if fieldname is similar to any of Cerebro's reserved keywords.
     *
     * @param fieldname the field's name to be checked.
     * @return true if the fieldname is the similar to one of the reserved words.
     */
    public static boolean checkReservedFieldName(String fieldname){
        boolean a = fieldname.equals(IndexConst.IDFieldName);
        boolean b = fieldname.equals(IndexConst.VecFieldName);
        boolean c = fieldname.equals(IndexConst.HashFieldName);
        return a || b || c ;
    }
}
