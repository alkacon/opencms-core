/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/legacy/Attic/CmsPageDocument.java,v $
 * Date   : $Date: 2005/06/23 10:47:13 $
 * Version: $Revision: 1.5 $
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
package com.opencms.legacy;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.search.A_CmsIndexResource;
import org.opencms.search.documents.A_CmsVfsDocument;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;

import com.opencms.template.*;

import org.htmlparser.parserapplications.StringExtractor;

/**
 * Lucene document factory class to extract index data from a cms resource 
 * of type <code>CmsResourceTypePage</code>.<p>
 * 
 * @version $Revision: 1.5 $ $Date: 2005/06/23 10:47:13 $
 * @author Carsten Weinholz 
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsPageDocument extends A_CmsVfsDocument {

    /**
     * Creates a new instance of this lucene document factory.<p>
     * 
     * @param name name of the documenttype
     */
    public CmsPageDocument (String name) {
        super(name);
    }
    
    /**
     * Gets the raw text content of a cms resource.<p>
     * 
     * @see org.opencms.search.documents.A_CmsVfsDocument#extractContent(org.opencms.file.CmsObject, org.opencms.search.A_CmsIndexResource, java.lang.String)
     */
    public I_CmsExtractionResult extractContent(CmsObject cms, A_CmsIndexResource indexResource, String language) throws CmsException {

        CmsResource resource = (CmsResource)indexResource.getData();
        String rawContent = null;
        
        try {
            CmsXmlTemplateFile file = new CmsXmlTemplateFile(cms, cms.getRequestContext().removeSiteRoot(resource.getRootPath()));        
            String content = file.getProcessedTemplateContent(null, null);
            
            StringExtractor extractor = new StringExtractor(content);
            rawContent = extractor.extractStrings(true);
        } catch (Exception exc) {
            throw new CmsLegacyException("Reading resource " + resource.getRootPath() + " failed", exc);
        }
        
        return new CmsExtractionResult(rawContent);
    }
}
