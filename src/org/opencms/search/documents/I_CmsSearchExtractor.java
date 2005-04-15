/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/I_CmsSearchExtractor.java,v $
 * Date   : $Date: 2005/04/15 15:51:08 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.search.A_CmsIndexResource;
import org.opencms.search.extractors.I_CmsExtractionResult;

/**
 * Comment for <code>I_CmsSearchExtractor</code>.<p>
 */
public interface I_CmsSearchExtractor {

    /**
     * Extractes the content of a given resource according to the resource file type.<p>
     * 
     * @param cms the cms object
     * @param resource a cms resource
     * @param language the requested language
     * @return the extracted content of the resource
     * @throws CmsException if somethin goes wrong
     */
    I_CmsExtractionResult extractContent(CmsObject cms, A_CmsIndexResource resource, String language) throws CmsException;

}