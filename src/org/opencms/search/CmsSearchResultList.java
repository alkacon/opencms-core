/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchResultList.java,v $
 * Date   : $Date: 2005/06/23 10:47:12 $
 * Version: $Revision: 1.4 $
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

package org.opencms.search;

import java.util.ArrayList;
import java.util.Map;

/**
 * A search result object returned as result of a search in
 * <code>{@link org.opencms.search.CmsSearchIndex}</code>.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSearchResultList extends ArrayList {

    /** The (otional) categories found in the last the search. */
    private Map m_categories;

    /** The total size of all results found in the last search. */
    private int m_hitCount;

    /**
     * Creates a new result list with a default initial capacity of 100.<p>
     */
    public CmsSearchResultList() {

        this(100);
    }

    /**
     * Creates a new result list with the specified initial capacity.<p>
     * 
     * @param initialCapacity the initial capacity
     */
    public CmsSearchResultList(int initialCapacity) {

        super(initialCapacity);
    }

    /**
     * Returns the (otional) categories found in the last the search, or <code>null</code> 
     * if the category list was not requested in the search.<p>
     *
     * @return the (otional) categories found in the last the search
     * 
     * @see CmsSearch#getCalculateCategories()
     */
    public Map getCategories() {

        return m_categories;
    }

    /**
     * Returns the hit count of all results found in the last search.<p>
     * 
     * Since this list will only contain the result objects for the current display page,
     * the size of the list is usually much less then the hit count of all results found.<p>
     * 
     * @return the hit count of all results found in the last search
     */
    public int getHitCount() {

        return m_hitCount;
    }

    /**
     * Sets the categories found in the last the search.<p>
     *
     * @param categories the categories to set
     * 
     * @see CmsSearch#setCalculateCategories(boolean)
     */
    public void setCategories(Map categories) {

        m_categories = categories;
    }

    /**
     * Sets the hit count of all results found in the last search.<p>
     *
     * Since this list will only contain the result objects for the current display page,
     * the size of the list is usually much less then the hit count of all results found.<p>
     *
     *  @param hitCount the hit count to set
     */
    public void setHitCount(int hitCount) {

        m_hitCount = hitCount;
    }
}