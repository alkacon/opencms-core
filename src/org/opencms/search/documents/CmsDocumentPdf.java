/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/CmsDocumentPdf.java,v $
 * Date   : $Date: 2005/03/23 19:08:22 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
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
import org.opencms.search.A_CmsIndexResource;
import org.opencms.search.CmsIndexException;
import org.opencms.search.extractors.CmsExtractorPdf;
import org.opencms.search.extractors.I_CmsExtractionResult;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.pdfbox.exceptions.CryptographyException;
import org.pdfbox.exceptions.InvalidPasswordException;

/**
 * Lucene document factory class to extract index data from a cms resource 
 * containing Adobe pdf data.<p>
 * 
 * @version $Revision: 1.1 $ $Date: 2005/03/23 19:08:22 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsDocumentPdf extends CmsVfsDocument {

    /**
     * Creates a new instance of this lucene document factory.<p>
     * 
     * @param name name of the documenttype
     */
    public CmsDocumentPdf(String name) {

        super(name);
    }

    /**
     * Returns the raw text content of a given vfs resource containing Adobe pdf data.<p>
     * 
     * @see org.opencms.search.documents.CmsVfsDocument#extractContent(org.opencms.file.CmsObject, org.opencms.search.A_CmsIndexResource, java.lang.String)
     */
    public I_CmsExtractionResult extractContent(CmsObject cms, A_CmsIndexResource indexResource, String language) throws CmsException {

        CmsResource resource = (CmsResource)indexResource.getData();
        CmsFile file = readFile(cms, resource);

        try {
            return CmsExtractorPdf.getExtractor().extractText(file.getContents());
        } catch (Exception e) {
            if (e instanceof CryptographyException) {
                throw new CmsIndexException("Decrypting resource " + resource.getRootPath() + " failed.", e);
            }
            if (e instanceof InvalidPasswordException) {
                // default password "" was wrong.
                throw new CmsIndexException("Resource " + resource.getRootPath() + " is password protected.", e);
            }
            throw new CmsIndexException("Extracting text from resource "
                + resource.getRootPath()
                + " failed: "
                + e.getMessage(), e);
        }
    }

    /**
     * Generates a new lucene document instance from contents of the given resource.<p>
     * 
     * @see org.opencms.search.documents.I_CmsDocumentFactory#newInstance(org.opencms.file.CmsObject, org.opencms.search.A_CmsIndexResource, java.lang.String)
     */
    public Document newInstance(CmsObject cms, A_CmsIndexResource resource, String language) throws CmsException {

        Document document = super.newInstance(cms, resource, language);
        I_CmsExtractionResult content = extractContent(cms, resource, language);
                
        document.add(Field.Text(I_CmsDocumentFactory.DOC_CONTENT, content.getContent()));

        return document;
    }
}