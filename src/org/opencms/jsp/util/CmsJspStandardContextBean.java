/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/util/CmsJspStandardContextBean.java,v $
 * Date   : $Date: 2011/04/13 10:21:37 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.CmsJspBean;
import org.opencms.jsp.Messages;
import org.opencms.main.CmsRuntimeException;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;

import javax.servlet.jsp.PageContext;

/**
 * Allows convenient access to the most important OpenCms functions on a JSP page,
 * indented to be used from a JSP with the JSTL or EL.<p>
 * 
 * This bean is available by default in the context of an OpenCms managed JSP.<p> 
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0
 */
public class CmsJspStandardContextBean {

    /** The attribute name of the standard JSP context bean. */
    public static final String ATTRIBUTE_JSP_STANDARD_CONTEXT_BEAN = "cms";

    /** OpenCms user context. */
    private CmsObject m_cms;

    /** OpenCms JSP controller. */
    private CmsFlexController m_controller;

    /** The VFS content access bean. */
    private CmsJspVfsAccessBean m_vfsBean;

    /**
     * Creates a new standard JSP context bean.
     * 
     * @param context the current JSP page context object
     */
    private CmsJspStandardContextBean(PageContext context) {

        m_controller = CmsFlexController.getController(context.getRequest());
        if (m_controller == null) {
            // controller not found - this request was not initialized properly
            throw new CmsRuntimeException(Messages.get().container(
                Messages.ERR_MISSING_CMS_CONTROLLER_1,
                CmsJspBean.class.getName()));
        }
        m_cms = m_controller.getCmsObject();
    }

    /**
     * Creates a new instance of the standard JSP context bean.<p>
     * 
     * To prevent multiple creations of the bean during a request, the OpenCms request context 
     * attributes are used to cache the created VFS access utility bean.<p>
     * 
     * @param context the current JSP page context object
     * 
     * @return a new instance of the standard JSP context bean
     */
    public static CmsJspStandardContextBean create(PageContext context) {

        Object attribute = context.getRequest().getAttribute(ATTRIBUTE_JSP_STANDARD_CONTEXT_BEAN);
        CmsJspStandardContextBean result;
        if (attribute != null) {
            result = (CmsJspStandardContextBean)attribute;
        } else {
            result = new CmsJspStandardContextBean(context);
            context.getRequest().setAttribute(ATTRIBUTE_JSP_STANDARD_CONTEXT_BEAN, result);
        }
        return result;
    }

    /**
     * Returns the container the currently rendered element is part of.<p>
     * 
     * @return the currently the currently rendered element is part of
     */
    public CmsContainerBean getContainer() {

        // TODO: fill this
        return null;
    }

    /**    
     * Returns the currently rendered element.<p>
     * 
     * @return the currently rendered element
     */
    public CmsContainerBean getElement() {

        // TODO: fill this
        return null;
    }

    /**
     * Returns the currently displayed container page.<p>
     * 
     * @return the currently displayed container page
     */
    public CmsContainerPageBean getPage() {

        // TODO: fill this
        return null;
    }

    /**
     * Returns an initialized VFS access bean.<p>
     * 
     * @return an initialized VFS access bean
     */
    public CmsJspVfsAccessBean getVfs() {

        if (m_vfsBean == null) {
            // create a new VVFS access bean
            m_vfsBean = CmsJspVfsAccessBean.create(m_cms);
        }
        return m_vfsBean;
    }
}