/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/CmsDocumentGeneric.java,v $
 * Date   : $Date: 2005/06/23 11:11:29 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.file.CmsObject;
import org.opencms.search.A_CmsIndexResource;
import org.opencms.search.CmsIndexException;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;

/**
 * Lucene document factory class for indexing data from a generic <code>{@link org.opencms.file.CmsResource}</code>.<p> 
 * 
 * Since the document type is generic, no content extraction is performed for the resource.
 * However, meta data from the properties and attributes of the resource are indexed.<p>
 * 
 * The class is useful for example to have images appear in the search result if the title of the image 
 * matched the search query. It's also used if no specific extraction method is available for a binary document type.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.0.0 
 */
public class CmsDocumentGeneric extends A_CmsVfsDocument {

    /**
     * Creates a new instance of this lucene document factory.<p>
     * 
     * @param name name of the documenttype
     */
    public CmsDocumentGeneric(String name) {

        super(name);
    }

    /**
     * Just returns an empty extraction result since the content can't be extracted form a generic resource.<p>
     * 
     * @see org.opencms.search.documents.A_CmsVfsDocument#extractContent(org.opencms.file.CmsObject, org.opencms.search.A_CmsIndexResource, java.lang.String)
     */
    public I_CmsExtractionResult extractContent(CmsObject cms, A_CmsIndexResource resource, String language)
    throws CmsIndexException {

        if (resource == null) {
            throw new CmsIndexException(Messages.get().container(Messages.ERR_NO_RAW_CONTENT_1, language));
        }
        // just return an empty result set
        return new CmsExtractionResult("");
    }
}