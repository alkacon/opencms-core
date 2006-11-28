/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/extractors/CmsExtractionResult.java,v $
 * Date   : $Date: 2006/11/28 16:20:44 $
 * Version: $Revision: 1.5.8.2 $
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

import org.opencms.util.CmsStringUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The result of a document text extraction.<p>
 * 
 * This data structure contains the extracted text as well as (optional) 
 * meta information extracted from the document.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.5.8.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsExtractionResult implements I_CmsExtractionResult, Serializable {

    /** UID rerquired for safe serialization. */
    private static final long serialVersionUID = 1465447302192195154L;

    /** The extracted individual content items. */
    private Map m_contentItems;

    /**
     * Creates a new extration result without meta information and without additional fields.<p>
     * 
     * @param content the extracted content
     */
    public CmsExtractionResult(String content) {

        this(content, null);
        m_contentItems.put(ITEM_RAW, content);
    }

    /**
     * Creates a new extration result.<p>
     * 
     * @param content the extracted content
     * @param contentItems the individual extracted content items
     */
    public CmsExtractionResult(String content, Map contentItems) {

        if (contentItems != null) {
            m_contentItems = contentItems;
        } else {
            m_contentItems = new HashMap();
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(content)) {
            m_contentItems.put(ITEM_CONTENT, content);
        }
    }

    /**
     * @see org.opencms.search.extractors.I_CmsExtractionResult#getContent()
     */
    public String getContent() {

        return (String)m_contentItems.get(ITEM_CONTENT);
    }

    /**
     * @see org.opencms.search.extractors.I_CmsExtractionResult#getContentItems()
     */
    public Map getContentItems() {

        return m_contentItems;
    }

    /**
     * @see org.opencms.search.extractors.I_CmsExtractionResult#release()
     */
    public void release() {

        if (!m_contentItems.isEmpty()) {
            m_contentItems.clear();
        }
        m_contentItems = null;
    }
}