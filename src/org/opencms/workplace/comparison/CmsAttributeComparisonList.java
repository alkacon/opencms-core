/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/comparison/CmsAttributeComparisonList.java,v $
 * Date   : $Date: 2005/11/18 09:05:28 $
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

package org.opencms.workplace.comparison;

import org.opencms.file.CmsFile;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.commons.CmsHistoryList;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListOrderEnum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * List for property comparison including columns for property name and the values. <p>
 * 
 * @author Jan Baudisch  
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsAttributeComparisonList extends CmsPropertyComparisonList {

    /** List id constant. */
    public static final String LIST_ID = "hiacl";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsAttributeComparisonList(CmsJspActionElement jsp) {

        this(LIST_ID, jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsAttributeComparisonList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Protected constructor.<p>
     * 
     * @param listId the id of the specialized list
     * @param jsp an initialized JSP action element
     */
    protected CmsAttributeComparisonList(String listId, CmsJspActionElement jsp) {

        super(
            jsp,
            listId,
            Messages.get().container(Messages.GUI_COMPARE_ATTRIBUTES_0),
            LIST_COLUMN_PROPERTY_NAME,
            CmsListOrderEnum.ORDER_ASCENDING,
            null);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() throws IOException, ServletException {

        // forward to the edit module screen  
        Map params = new HashMap();
        params.put(CmsHistoryList.PARAM_TAGID_1, getParamTagId1());
        params.put(CmsHistoryList.PARAM_TAGID_2, getParamTagId2());
        params.put(CmsHistoryList.PARAM_VERSION_1, getParamVersion1());
        params.put(CmsHistoryList.PARAM_VERSION_2, getParamVersion2());
        params.put(CmsHistoryList.PARAM_PATH_1, getParamPath1());
        params.put(CmsHistoryList.PARAM_PATH_2, getParamPath2());
        params.put(PARAM_COMPARE, "attributes");
        params.put(PARAM_RESOURCE, getParamResource());
        // forward to the difference screen
        getToolManager().jspForwardTool(this, "/history/comparison/difference", params);
        refreshList();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() throws CmsException {

        List ret = new ArrayList();
        CmsFile resource1;
        CmsFile resource2;
        if (CmsHistoryList.OFFLINE_PROJECT.equals(getParamVersion1())) {
            resource1 = getCms().readFile(getCms().getRequestContext().removeSiteRoot(getParamPath1()));
        } else {
            resource1 = getCms().readBackupFile(getCms().getRequestContext().removeSiteRoot(getParamPath1()), 
                Integer.parseInt(getParamTagId1()));
        }
        if (CmsHistoryList.OFFLINE_PROJECT.equals(getParamVersion2())) {
            resource2 = getCms().readFile(getCms().getRequestContext().removeSiteRoot(getParamPath2()));
        } else {
            resource2 = getCms().readBackupFile(getCms().getRequestContext().removeSiteRoot(getParamPath2()), 
                Integer.parseInt(getParamTagId2()));
        }
        Iterator diffs = new CmsResourceComparison(getCms(), resource1, resource2).getComparedAttributes().iterator();
        while (diffs.hasNext()) {
            CmsAttributeComparison comparison = (CmsAttributeComparison)diffs.next();
            CmsListItem item = getList().newItem(comparison.getName());
            item.set(LIST_COLUMN_PROPERTY_NAME, comparison.getName());
            item.set(LIST_COLUMN_VERSION_1, CmsStringUtil.trimToSize(comparison.getVersion1(), TRIM_AT_LENGTH));
            item.set(LIST_COLUMN_VERSION_2, CmsStringUtil.trimToSize(comparison.getVersion2(), TRIM_AT_LENGTH));
            item.set(LIST_COLUMN_TYPE, comparison.getType());
            ret.add(item);
        }
        return ret;
    }
}