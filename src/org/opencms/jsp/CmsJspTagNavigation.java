/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagNavigation.java,v $
 * Date   : $Date: 2011/04/11 15:37:15 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.util.CmsJspNavigationBean;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.util.CmsStringUtil;

import javax.servlet.jsp.PageContext;

/**
 * Implementation of the <code>&lt;cms:navigation var="..." /&gt;</code> tag, 
 * used to access OpenCms VFS navigation information on a JSP with the EL.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0
 */
public class CmsJspTagNavigation extends CmsJspScopedVarBodyTagSuport {

    /** Constants for <code>type</code> attribute interpretation. */
    public enum TypeUse {
        /** Bread crumb navigation. */
        BREAD_CRUMB("breadCrumb"),
        /** Navigation for folder. */
        FOR_FOLDER("forFolder"),
        /** Navigation for resource. */
        FOR_RESOURCE("forResource"),
        /** Navigation for a site. */
        FOR_SITE("forSite"),
        /** Navigation tree for folder. */
        TREE_FOR_FOLDER("treeForFolder");

        /** Property name. */
        private String m_name;

        /** Constructor.<p>
         * @param name the string representation of the constant  
         **/
        private TypeUse(String name) {

            m_name = name;
        }

        /**
         * Parses a string into an enumeration element.<p>
         * 
         * @param name the name of the element
         * 
         * @return the element with the given name or <code>null</code> if not found
         */
        public static TypeUse parse(String name) {

            for (TypeUse fileUse : TypeUse.values()) {
                if (fileUse.getName().equals(name)) {
                    return fileUse;
                }
            }
            return null;
        }

        /** 
         * Returns the name.<p>
         * 
         * @return the name
         */
        public String getName() {

            return m_name;
        }
    }

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 8589202895748764705L;

    /** The CmsObject for the current user. */
    protected transient CmsObject m_cms;

    /** The optional end level for the navigation. */
    protected String m_endLevel;

    /** The optional parameter for the navigation. */
    protected String m_param;

    /** The optional resource for the navigation. */
    protected String m_resource;

    /** The optional start level for the navigation. */
    protected String m_startLevel;

    /** The navigation type. */
    protected TypeUse m_type;

    /**
     * Empty constructor, required for JSP tags.<p> 
     */
    public CmsJspTagNavigation() {

        super();
    }

    /**
     * Constructor used for scriptlet code.<p> 
     * 
     * @param context the JSP page context
     */
    public CmsJspTagNavigation(PageContext context) {

        setPageContext(context);
        init();
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws CmsIllegalArgumentException {

        // initialize the content load tag
        init();
        return SKIP_BODY;
    }

    /**
     * Returns the (optional) end level for the navigation.<p>
     * 
     * @return the (optional) end level for the navigation
     */
    public String getEndLevel() {

        return m_endLevel;
    }

    /**
     * Returns the optional parameter for the navigation.<p>
     * 
     * @return the optional parameter for the navigation
     */
    public String getParam() {

        return m_param;
    }

    /**
     * Returns the (optional) resource for the navigation.<p>
     * 
     * @return the (optional) resource for the navigation
     */
    public String getResource() {

        return m_resource;
    }

    /**
     * Returns the (optional) start level for the navigation.<p>
     * 
     * @return the (optional) start level for the navigation
     */
    public String getStartLevel() {

        return m_startLevel;
    }

    /**
     * Returns the selected navigation type.<p>
     * 
     * This must match one of the elements in {@link TypeUse}.<p>
     * 
     * @return the selected navigation type
     */
    public String getType() {

        return m_type == null ? null : m_type.getName();
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        m_cms = null;
        m_startLevel = null;
        m_endLevel = null;
        m_resource = null;
        m_type = null;
        super.release();
    }

    /**
     * Sets the (optional) end level for the navigation.<p>
     * 
     * @param endLevel the (optional) end level for the navigation
     */
    public void setEndLevel(String endLevel) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(endLevel)) {
            m_endLevel = endLevel.trim();
        }
    }

    /**
     * Sets the optional parameter for the navigation.<p>
     * 
     * @param param the optional parameter for the navigation to set
     */
    public void setParam(String param) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(param)) {
            m_param = param.trim();
        }
    }

    /**
     * Sets the (optional) resource for the navigation.<p>
     * 
     * @param resource the (optional) resource for the navigation
     */
    public void setResource(String resource) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(resource)) {
            m_resource = resource.trim();
        }
    }

    /**
     * Sets the (optional) start level for the navigation.<p>
     * 
     * @param startLevel the (optional) start level for the navigation
     */
    public void setStartLevel(String startLevel) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(startLevel)) {
            m_startLevel = startLevel.trim();
        }
    }

    /**
     * Sets the selected navigation type.<p>
     * 
     * This must match one of the elements in {@link TypeUse}.<p>
     * 
     * @param type the navigation type to set
     */
    public void setType(String type) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(type)) {
            m_type = TypeUse.parse(type.trim());
        }
    }

    /**
     * Initializes this formatter tag.<p> 
     */
    protected void init() {

        // initialize OpenCms access objects
        CmsFlexController controller = CmsFlexController.getController(pageContext.getRequest());
        m_cms = controller.getCmsObject();

        int todo; // provide error handling in case of wrong "type" or other parameters
        int startLevel = m_startLevel == null ? Integer.MIN_VALUE : Integer.parseInt(m_startLevel);
        int endLevel = m_endLevel == null ? Integer.MIN_VALUE : Integer.parseInt(m_endLevel);

        // load navigation bean in the JSP context
        CmsJspNavigationBean bean = new CmsJspNavigationBean(m_cms, m_type, startLevel, endLevel, m_resource, m_param);
        storeAttribute(getVar(), bean);
    }
}