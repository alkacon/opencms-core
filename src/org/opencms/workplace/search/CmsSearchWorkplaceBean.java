/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/search/CmsSearchWorkplaceBean.java,v $
 * Date   : $Date: 2006/11/28 16:20:45 $
 * Version: $Revision: 1.1.2.2 $
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

package org.opencms.workplace.search;

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.search.CmsSearchParameters;
import org.opencms.util.CmsStringUtil;

import java.util.Iterator;

/**
 * Bean to handle search parameters in the workplace.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.3.0 
 */
public class CmsSearchWorkplaceBean {

    /** The comma separated list of fields to search parameter value. */
    private String m_fields;

    /** The query. */
    private String m_query;

    /** The sort Order. */
    private String m_sortOrder;

    /**
     * Default constructor.<p>
     */
    public CmsSearchWorkplaceBean() {

        StringBuffer fields = new StringBuffer();
        Iterator i = CmsSearchDialog.getFieldNames().iterator();
        while (i.hasNext()) {
            fields.append(i.next());
            if (i.hasNext()) {
                fields.append(',');
            }
        }

        m_fields = fields.toString();
        m_sortOrder = CmsSearchParameters.SORT_NAMES[0];
    }

    /**
     * Returns the fields parameter value.<p>
     *
     * @return the fields parameter value
     */
    public String getFields() {

        return m_fields;
    }

    /**
     * Returns the query.<p>
     *
     * @return the query
     */
    public String getQuery() {

        return m_query;
    }

    /**
     * Returns the sort Order.<p>
     *
     * @return the sort Order
     */
    public String getSortOrder() {

        return m_sortOrder;
    }

    /**
     * Sets the fields parameter value.<p>
     *
     * @param fields the fields parameter value to set
     */
    public void setFields(String fields) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(fields)) {
            throw new CmsIllegalStateException(Messages.get().container(Messages.ERR_VALIDATE_SEARCH_PARAMS_0));
        }
        m_fields = fields;
    }

    /**
     * Sets the query.<p>
     *
     * @param query the query to set
     */
    public void setQuery(String query) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(query)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_VALIDATE_SEARCH_QUERY_0));
        }
        m_query = query;
    }

    /**
     * Sets the sort Order.<p>
     *
     * @param sortOrder the sort Order to set
     */
    public void setSortOrder(String sortOrder) {

        m_sortOrder = sortOrder;
    }
}
