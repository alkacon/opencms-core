/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/search/CmsSearchWorkplaceBean.java,v $
 * Date   : $Date: 2006/04/19 09:05:00 $
 * Version: $Revision: 1.1.2.1 $
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
import org.opencms.util.CmsStringUtil;

/**
 * Bean to handle search parameters in the workplace.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.3.0 
 */
public class CmsSearchWorkplaceBean {

    /** The field Content parameter value. */
    private boolean m_fieldContent;

    /** The field Meta parameter value. */
    private boolean m_fieldMeta;

    /** The query. */
    private String m_query;

    /** The sort Order. */
    private String m_sortOrder;

    /**
     * Returns the field Content parameter value.<p>
     *
     * @return the field Content parameter value
     */
    public boolean getFieldContent() {

        return m_fieldContent;
    }

    /**
     * Returns the field Meta parameter value.<p>
     *
     * @return the field Meta parameter value
     */
    public boolean getFieldMeta() {

        return m_fieldMeta;
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
     * Sets the field Content parameter value.<p>
     *
     * @param fieldContent the field Content parameter value to set
     */
    public void setFieldContent(boolean fieldContent) {

        m_fieldContent = fieldContent;
    }

    /**
     * Sets the field Meta parameter value.<p>
     *
     * @param fieldMeta the field Meta parameter value to set
     */
    public void setFieldMeta(boolean fieldMeta) {

        m_fieldMeta = fieldMeta;
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

    /**
     * Validates the state of the search parameter.<p>
     */
    public void validate() {

        if (!getFieldMeta() && !getFieldContent()) {
            throw new CmsIllegalStateException(Messages.get().container(Messages.ERR_VALIDATE_SEARCH_PARAMS_0));
        }
    }
}
