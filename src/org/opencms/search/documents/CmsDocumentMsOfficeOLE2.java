/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
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
import org.opencms.search.I_CmsSearchIndex;
import org.opencms.search.extractors.CmsExtractorMsOfficeOLE2;
import org.opencms.search.extractors.I_CmsExtractionResult;

/**
 * Lucene document factory class to extract text data from a VFS resource that is an OLE 2 MS Office document.<p>
 *
 * Supported formats are MS Word (.doc), MS PowerPoint (.ppt) and MS Excel (.xls).<p>
 *
 * The OLE 2 format was introduced in Microsoft Office version 97 and was the default format until Office version 2007
 * and the new XML-based OOXML format.<p>
 *
 * @since 8.0.1
 */
public class CmsDocumentMsOfficeOLE2 extends A_CmsVfsDocument {

    /**
     * Creates a new instance of this lucene document factory.<p>
     *
     * @param name name of the documenttype
     */
    public CmsDocumentMsOfficeOLE2(String name) {

        super(name);
    }

    /**
     * Returns the raw text content of a given vfs resource containing MS Word data.<p>
     *
     * @see org.opencms.search.documents.I_CmsSearchExtractor#extractContent(CmsObject, CmsResource, I_CmsSearchIndex)
     */
    public I_CmsExtractionResult extractContent(CmsObject cms, CmsResource resource, I_CmsSearchIndex index)
    throws CmsIndexException, CmsException {

        logContentExtraction(resource, index);
        CmsFile file = readFile(cms, resource);
        try {
            return CmsExtractorMsOfficeOLE2.getExtractor().extractText(file.getContents());
        } catch (Throwable e) {
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