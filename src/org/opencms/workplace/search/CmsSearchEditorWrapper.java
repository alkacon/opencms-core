/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/search/CmsSearchEditorWrapper.java,v $
 * Date   : $Date: 2006/08/19 13:40:59 $
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

import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsRequestUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.CmsToolDialog;
import org.opencms.workplace.tools.CmsToolManager;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Workplace class for /system/workplace/explorer/search/edit.jsp explorer tool.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSearchEditorWrapper extends CmsDialog {

    /** The fieldContent parameter value. */
    private String m_paramFieldcontent;

    /** The fieldMeta parameter value. */
    private String m_paramFieldmeta;

    /** The query parameter value. */
    private String m_paramQuery;

    /** The sortOrder parameter value. */
    private String m_paramSortorder;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSearchEditorWrapper(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSearchEditorWrapper(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Performs the dialog actions depending on the initialized action and displays the dialog form.<p>
     * 
     * @throws Exception if writing to the JSP out fails
     */
    public void displayDialog() throws Exception {

        initAdminTool();

        // close link!
        String uri = CmsWorkplace.VFS_PATH_VIEWS + "explorer/explorer_fs.jsp?uri=";
        Map params = new HashMap();
        params.put(CmsToolDialog.PARAM_ROOT, getParamRoot());
        params.put(CmsToolDialog.PARAM_PATH, getParamPath());
        params.put(CmsSearchResultsList.PARAM_QUERY, getParamQuery());
        params.put(CmsSearchResultsList.PARAM_SORT_ORDER, getParamSortorder());
        params.put(CmsSearchResultsList.PARAM_FIELD_CONTENT, getParamFieldcontent());
        params.put(CmsSearchResultsList.PARAM_FIELD_META, getParamFieldmeta());
        uri = CmsRequestUtil.appendParameters(CmsToolManager.VIEW_JSPPAGE_LOCATION, params, true);
        uri = CmsRequestUtil.appendParameter(
            CmsWorkplace.VFS_PATH_VIEWS + "explorer/explorer_fs.jsp",
            "uri",
            CmsEncoder.encode(CmsEncoder.encode(uri)));
        getSettings().setViewStartup(getJsp().link(uri));

        JspWriter out = getJsp().getJspContext().getOut();
        out.print(htmlStart());
        out.print(bodyStart(null));
        out.print("<form name='editor' method='post' target='_top' action='");
        out.print(getJsp().link("/system/workplace/editors/editor.jsp"));
        out.print("'>\n");
        out.print(paramsAsHidden());
        out.print("</form>\n");
        out.print("<script type='text/javascript'>\n");
        out.print("document.forms['editor'].submit();\n");
        out.print("</script>\n");
        out.print(dialogContentStart(getParamTitle()));
        out.print(dialogContentEnd());
        out.print(dialogEnd());
        out.print(bodyEnd());
        out.print(htmlEnd());
    }

    /**
     * Returns the fieldContent parameter value.<p>
     *
     * @return the fieldContent parameter value
     */
    public String getParamFieldcontent() {

        return m_paramFieldcontent;
    }

    /**
     * Returns the fieldMeta parameter value.<p>
     *
     * @return the fieldMeta parameter value
     */
    public String getParamFieldmeta() {

        return m_paramFieldmeta;
    }

    /**
     * Returns the query parameter value.<p>
     *
     * @return the query parameter value
     */
    public String getParamQuery() {

        return m_paramQuery;
    }

    /**
     * Returns the sortOrder parameter value.<p>
     *
     * @return the sortOrder parameter value
     */
    public String getParamSortorder() {

        return m_paramSortorder;
    }

    /**
     * Sets the fieldContent parameter value.<p>
     *
     * @param paramFieldContent the fieldContent parameter value to set
     */
    public void setParamFieldcontent(String paramFieldContent) {

        m_paramFieldcontent = paramFieldContent;
    }

    /**
     * Sets the fieldMeta parameter value.<p>
     *
     * @param paramFieldMeta the fieldMeta parameter value to set
     */
    public void setParamFieldmeta(String paramFieldMeta) {

        m_paramFieldmeta = paramFieldMeta;
    }

    /**
     * Sets the query parameter value.<p>
     *
     * @param paramQuery the query parameter value to set
     */
    public void setParamQuery(String paramQuery) {

        m_paramQuery = paramQuery;
    }

    /**
     * Sets the sortOrder parameter value.<p>
     *
     * @param paramSortOrder the sortOrder parameter value to set
     */
    public void setParamSortorder(String paramSortOrder) {

        m_paramSortorder = paramSortOrder;
    }
}
