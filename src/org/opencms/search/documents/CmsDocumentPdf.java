/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.search.documents;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.search.CmsIndexException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.extractors.CmsExtractorPdf;
import org.opencms.search.extractors.I_CmsExtractionResult;

import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;

/**
 * Lucene document factory class to extract index data from a cms resource
 * containing Adobe pdf data.<p>
 *
 * @since 6.0.0
 */
public class CmsDocumentPdf extends A_CmsVfsDocument {

    /**
     * Creates a new instance of this lucene document factory.<p>
     *
     * @param name name of the documenttype
     */
    public CmsDocumentPdf(String name) {

        super(name);
    }

    /**
     * Returns the raw text content of a given vfs resource containing Adobe PDF data.<p>
     *
     * @see org.opencms.search.documents.I_CmsSearchExtractor#extractContent(CmsObject, CmsResource, CmsSearchIndex)
     */
    public I_CmsExtractionResult extractContent(CmsObject cms, CmsResource resource, CmsSearchIndex index)
    throws CmsIndexException, CmsException {

        logContentExtraction(resource, index);
        CmsFile file = readFile(cms, resource);
        try {
            return CmsExtractorPdf.getExtractor().extractText(file.getContents());
        } catch (Exception e) {
            if (e.getClass().getSimpleName().equals("EncryptedDocumentException")) {
                throw new CmsIndexException(
                    Messages.get().container(Messages.ERR_DECRYPTING_RESOURCE_1, resource.getRootPath()),
                    e);
            }
            if (e instanceof InvalidPasswordException) {
                // default password "" was wrong.
                throw new CmsIndexException(
                    Messages.get().container(Messages.ERR_PWD_PROTECTED_1, resource.getRootPath()),
                    e);
            }
            throw new CmsIndexException(
                Messages.get().container(Messages.ERR_TEXT_EXTRACTION_1, resource.getRootPath()),
                e);
        }
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#isLocaleDependend()
     */
    public boolean isLocaleDependend() {

        return false;
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#isUsingCache()
     */
    public boolean isUsingCache() {

        return true;
    }
}