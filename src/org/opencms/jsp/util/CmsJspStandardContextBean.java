/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/util/CmsJspStandardContextBean.java,v $
 * Date   : $Date: 2011/04/15 08:08:54 $
 * Version: $Revision: 1.2 $
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

import org.opencms.ade.detailpage.CmsDetailPageResourceHandler;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.CmsJspBean;
import org.opencms.jsp.Messages;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;

import java.util.Locale;

import javax.servlet.ServletRequest;

/**
 * Allows convenient access to the most important OpenCms functions on a JSP page,
 * indented to be used from a JSP with the JSTL or EL.<p>
 * 
 * This bean is available by default in the context of an OpenCms managed JSP.<p> 
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0
 */
public final class CmsJspStandardContextBean {

    /** The attribute name of the cms object.*/
    public static final String ATTRIBUTE_CMS_OBJECT = "__cmsObject";

    /** The attribute name of the standard JSP context bean. */
    public static final String ATTRIBUTE_JSP_STANDARD_CONTEXT_BEAN = "cms";

    /** OpenCms user context. */
    private CmsObject m_cms;

    /** The container the currently rendered element is part of. */
    private CmsContainerBean m_container;

    /** The current detail content id if available. */
    private CmsUUID m_detailContentId;

    /** The currently rendered element. */
    private CmsContainerElementBean m_element;

    /** The currently displayed container page. */
    private CmsContainerPageBean m_page;

    /** The VFS content access bean. */
    private CmsJspVfsAccessBean m_vfsBean;

    /**
     * Creates a new standard JSP context bean.
     * 
     * @param req the current servlet request
     */
    private CmsJspStandardContextBean(ServletRequest req) {

        CmsFlexController controller = CmsFlexController.getController(req);
        if (controller != null) {
            m_cms = controller.getCmsObject();
        } else {
            m_cms = (CmsObject)req.getAttribute(ATTRIBUTE_CMS_OBJECT);
        }
        if (m_cms == null) {
            // cms object unavailable - this request was not initialized properly
            throw new CmsRuntimeException(Messages.get().container(
                Messages.ERR_MISSING_CMS_CONTROLLER_1,
                CmsJspBean.class.getName()));
        }

        m_detailContentId = CmsDetailPageResourceHandler.getDetailId(req);
    }

    /**
     * Creates a new instance of the standard JSP context bean.<p>
     * 
     * To prevent multiple creations of the bean during a request, the OpenCms request context 
     * attributes are used to cache the created VFS access utility bean.<p>
     * 
     * @param req the current servlet request
     * 
     * @return a new instance of the standard JSP context bean
     */
    public static CmsJspStandardContextBean getInstance(ServletRequest req) {

        Object attribute = req.getAttribute(ATTRIBUTE_JSP_STANDARD_CONTEXT_BEAN);
        CmsJspStandardContextBean result;
        if ((attribute != null) && (attribute instanceof CmsJspStandardContextBean)) {
            result = (CmsJspStandardContextBean)attribute;
        } else {
            result = new CmsJspStandardContextBean(req);
            req.setAttribute(ATTRIBUTE_JSP_STANDARD_CONTEXT_BEAN, result);
        }
        return result;
    }

    /**
     * Returns the container the currently rendered element is part of.<p>
     * 
     * @return the currently the currently rendered element is part of
     */
    public CmsContainerBean getContainer() {

        return m_container;
    }

    /**
     * Returns the current detail content id if available.<p>
     * 
     * @return the current detail content id if available
     */
    public CmsUUID getDetailContentId() {

        return m_detailContentId;
    }

    /**    
     * Returns the currently rendered element.<p>
     * 
     * @return the currently rendered element
     */
    public CmsContainerElementBean getElement() {

        return m_element;
    }

    /**
     * Returns the current locale.<p>
     * 
     * @return the current locale
     */
    public Locale getLocal() {

        return getRequestContext().getLocale();
    }

    /**
     * Returns the currently displayed container page.<p>
     * 
     * @return the currently displayed container page
     */
    public CmsContainerPageBean getPage() {

        return m_page;
    }

    /**
     * Returns the request context.<p>
     * @return the request context
     */
    public CmsRequestContext getRequestContext() {

        return m_cms.getRequestContext();
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

    /**
     * Sets the container the currently rendered element is part of.<p>
     *
     * @param container the container the currently rendered element is part of
     */
    public void setContainer(CmsContainerBean container) {

        m_container = container;
    }

    /**
     * Sets the currently rendered element.<p>
     *
     * @param element the currently rendered element to set
     */
    public void setElement(CmsContainerElementBean element) {

        m_element = element;
    }

    /**
     * Sets the currently displayed container page.<p>
     *
     * @param page the currently displayed container page to set
     */
    public void setPage(CmsContainerPageBean page) {

        m_page = page;
    }
}