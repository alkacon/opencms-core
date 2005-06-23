/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/extractors/I_CmsExtractionResult.java,v $
 * Date   : $Date: 2005/06/23 11:11:28 $
 * Version: $Revision: 1.6 $
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

package org.opencms.search.extractors;

import java.util.Map;

/**
 * The result of a document text extraction.<p>
 * 
 * This data structure contains the extracted text as well as (optional) 
 * meta information extracted from the document.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsExtractionResult {

    /** Key to access the document author name in the meta information map (the value is a <code>{@link String}</code> object). */
    String META_AUTHOR = "author";

    /** Key to access the document catrgory in the meta information map (the value is a <code>{@link String}</code> object). */
    String META_CATEGORY = "category";

    /** Key to access the document comments in the meta information map (the value is a <code>{@link String}</code> object). */
    String META_COMMENTS = "comments";

    /** Key to access the document company name in the meta information map (the value is a <code>{@link String}</code> object). */
    String META_COMPANY = "company";

    /** Key to access the document creator name in the meta information map (the value is a <code>{@link String}</code> object). */
    String META_CREATOR = "creator";

    /** Key to access the document creation date in the meta information map (the value is a <code>{@link java.util.Date}</code> object). */
    String META_DATE_CREATED = "creation date";

    /** Key to access the document date of last modification in the meta information map (the value is a <code>{@link java.util.Date}</code> object). */
    String META_DATE_LASTMODIFIED = "last modification date";

    /** Key to access the document keywords in the meta information map (the value is a <code>{@link String}</code> object). */
    String META_KEYWORDS = "keywords";

    /** Key to access the document manager name in the meta information map (the value is a <code>{@link String}</code> object). */
    String META_MANAGER = "manager";

    /** Key to access the document producer name in the meta information map (the value is a <code>{@link String}</code> object). */
    String META_PRODUCER = "producer";

    /** Key to access the document subject in the meta information map (the value is a <code>{@link String}</code> object). */
    String META_SUBJECT = "subject";

    /** Key to access the document title in the meta information map (the value is a <code>{@link String}</code> object). */
    String META_TITLE = "title";

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
     * by the extractor. The key is always a String, and should be one of the constants 
     * defined in the <code>{@link I_CmsExtractionResult}</code> interface. For example
     * <code>{@link I_CmsExtractionResult#META_TITLE}</code> will contain the document title as 
     * a String.<p>
     *
     * @return the extracted meta information
     */
    Map getMetaInfo();
}