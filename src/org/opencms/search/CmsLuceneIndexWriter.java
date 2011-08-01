package org.opencms.search;

import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.opencms.search.fields.CmsSearchField;

/**
 * Delegates indexing to a lucene IndexWriter.
 * @author Florian Hopf, Synyx GmbH & Co. KG, hopf@synyx.de
 */
public class CmsLuceneIndexWriter implements I_CmsIndexWriter {

    private final IndexWriter indexWriter;

    public CmsLuceneIndexWriter(IndexWriter indexWriter) {
        this.indexWriter = indexWriter;

    }

    public void optimize() throws IOException {
        indexWriter.optimize();
    }

    public void commit() throws IOException {
        indexWriter.commit();
    }

    public void close() throws IOException {
        indexWriter.close();
    }

    public void updateDocument(String path, Document document) throws IOException {
        Term pathTerm = new Term(CmsSearchField.FIELD_PATH, path);
        indexWriter.updateDocument(pathTerm, document);
    }

    public void deleteDocuments(String rootPath) throws IOException {
        // search for an exact match on the document root path
        Term term = new Term(CmsSearchField.FIELD_PATH, rootPath);
        indexWriter.deleteDocuments(term);
    }
}
