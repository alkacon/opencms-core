package org.opencms.search;

import java.io.IOException;
import org.apache.lucene.document.Document;

/**
 * Provides index manipulation operations.
 * @author Florian Hopf, Synyx GmbH & Co. KG, hopf@synyx.de
 */
public interface I_CmsIndexWriter {

    /**
     * Optimizes the index.
     * @throws IOException
     */
    void optimize() throws IOException;

    /**
     * Commit all previous operations.
     * @throws IOException
     */
    void commit() throws IOException;

    /**
     * Close this IndexWriter.
     * @throws IOException
     */
    void close() throws IOException;

    /**
     * Update a document in the index.
     * @param path
     * @param document
     * @throws IOException
     */
    void updateDocument(String path, Document document) throws IOException;

    /**
     * Delete a document from the index.
     * @param rootPath
     * @throws IOException
     */
    void deleteDocuments(String rootPath) throws IOException;

}
