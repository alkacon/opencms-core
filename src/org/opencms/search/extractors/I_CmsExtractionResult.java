/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/extractors/I_CmsExtractionResult.java,v $
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

package org.opencms.search.extractors;

import java.util.Map;

/**
 * The result of a document text extraction.<p>
 * 
 * This data structure contains the extracted text as well as (optional) 
 * meta information extracted from the document.<p>
 */
public interface I_CmsExtractionResult {

    /** Key to access the document author name in the meta information map. */
    String META_AUTHOR = "Author";

    /** Key to access the document creator name in the meta information map. */
    String META_CREATOR = "Creator";

    /** Key to access the document creation date in the meta information map, this should be in HTTP header format. */
    String META_DATE_CREATED = "Creation date";

    /** Key to access the document date of last modification in the meta information map, this should be in HTTP header format. */
    String META_DATE_LASTMODIFIED = "Last modification date";

    /** Key to access the document description in the meta information map. */
    String META_DESCRIPTION = "Description";

    /** Key to access the document keywords in the meta information map. */
    String META_KEYWORDS = "Keywords";

    /** Key to access the document producer name in the meta information map. */
    String META_PRODUCER = "Producer";

    /** Key to access the document subject in the meta information map. */
    String META_SUBJECT = "Subject";

    /** Key to access the document title in the meta information map. */
    String META_TITLE = "Title";

    /**
     * Returns the extracted content as a String.<p>
     *
     * @return the extracted content as a String
     */
    String getContent();

    /**
     * Returns the extracted meta information.<p>
     *
     * The result Map contains all meta information extracted
     * by the extractor, both key and value in the map are Strings.<p> 
     *
     * @return the extracted meta information
     */
    Map getMetaInfo();
}