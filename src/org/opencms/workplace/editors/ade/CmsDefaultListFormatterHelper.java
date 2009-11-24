/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsDefaultListFormatterHelper.java,v $
 * Date   : $Date: 2009/11/24 15:49:07 $
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

package org.opencms.workplace.editors.ade;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.xml.containerpage.CmsADEManager;
import org.opencms.xml.containerpage.CmsContainerElementBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Helper bean to implement default formatters for list elements.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 7.6
 * 
 */
public class CmsDefaultListFormatterHelper extends CmsJspActionElement {

    /** The element-bean. */
    private CmsContainerElementBean m_elementBean;

    /** The formatter-info-bean. */
    private CmsFormatterInfoBean m_formatterInfo;

    /** Indicates if the formatter is using the container-bean or formatter-info-bean as data-source. */
    private boolean m_isContainerBeanMode;

    /** The ade manager. */
    private CmsADEManager m_manager;

    /** The element's resource. */
    private CmsResource m_resource;

    /** The workplace locale from the current user's settings. */
    private Locale m_wpLocale;

    /**
     * Constructor, with parameters.
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     * @throws CmsException 
     */
    public CmsDefaultListFormatterHelper(PageContext context, HttpServletRequest req, HttpServletResponse res)
    throws CmsException {

        super(context, req, res);

        m_manager = OpenCms.getADEManager();

        m_formatterInfo = m_manager.getFormatterInfo(req);

        if (m_formatterInfo == null) {
            m_elementBean = m_manager.getCurrentElement(req);
            m_isContainerBeanMode = true;
        }
    }

    /**
     * Returns a list of additional info.<p>
     * 
     * @return the additional info.
     * @throws CmsException if something goes wrong
     */
    public List<CmsFieldInfoBean> getAdditionalInfo() throws CmsException {

        if (m_isContainerBeanMode) {
            List<CmsFieldInfoBean> result = new ArrayList<CmsFieldInfoBean>();
            result.add(new CmsFieldInfoBean("path", "path", getSitePath()));
            result.add(new CmsFieldInfoBean("type", "type", CmsWorkplaceMessages.getResourceTypeName(
                getWorkplaceLocale(),
                OpenCms.getResourceManager().getResourceType(getTypeName()).getTypeName())));

            result.add(new CmsFieldInfoBean("lastModified", "lastModified", OpenCms.getWorkplaceManager().getMessages(
                getWorkplaceLocale()).getDateTime(getResource().getDateLastModified())));
            return result;
        }
        return m_formatterInfo.getAdditionalInfo();
    }

    /** 
     * Returns the icon-path.<p>
     * 
     * @return the icon-path
     * @throws CmsException if something goes wrong
     */
    public String getIcon() throws CmsException {

        if (m_isContainerBeanMode) {
            return CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES
                + OpenCms.getWorkplaceManager().getExplorerTypeSetting(getTypeName()).getIcon());
        }
        return m_formatterInfo.getIcon();
    }

    /**
     * Returns the container page manager.<p>
     * 
     * @return the container page manager
     */
    public CmsADEManager getManager() {

        if (m_manager == null) {
            m_manager = OpenCms.getADEManager();
        }
        return m_manager;
    }

    /**
     * Returns the element's resource.<p>
     * 
     * @return the element's resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsResource getResource() throws CmsException {

        if (m_resource == null) {
            if (m_isContainerBeanMode) {
                m_resource = getCmsObject().readResource(m_elementBean.getElementId());
            } else {
                if (m_formatterInfo.getResource() != null) {
                    m_resource = m_formatterInfo.getResource();
                } else if (m_formatterInfo.getSitePath() != null) {
                    m_resource = getCmsObject().readResource(m_formatterInfo.getSitePath());
                } else if (m_formatterInfo.getResourceId() != null) {
                    m_resource = getCmsObject().readResource(m_formatterInfo.getResourceId());
                }
            }
        }
        return m_resource;
    }

    /**
     * The elements site-path.<p>
     * 
     * @return the site-path
     * @throws CmsException if something goes wrong
     */
    public String getSitePath() throws CmsException {

        if (m_isContainerBeanMode) {
            return getCmsObject().getSitePath(getResource());
        }
        return m_formatterInfo.getSitePath();
    }

    /**
     * Returns the sub-title info.<p>
     * 
     * @return the sub-title info
     * @throws CmsException if something goes wrong
     */
    public CmsFieldInfoBean getSubTitle() throws CmsException {

        if (m_isContainerBeanMode) {
            return new CmsFieldInfoBean("filename", "", getResource().getName());
        }
        return m_formatterInfo.getSubTitleInfo();
    }

    /**
     * Returns the title info.<p>
     * 
     * @return the title info
     * @throws CmsException if something goes wrong
     */
    public CmsFieldInfoBean getTitle() throws CmsException {

        if (m_isContainerBeanMode) {
            return new CmsFieldInfoBean(CmsPropertyDefinition.PROPERTY_TITLE, "", getCmsObject().readPropertyObject(
                getSitePath(),
                CmsPropertyDefinition.PROPERTY_TITLE,
                false).getValue(""));
        }
        return m_formatterInfo.getTitleInfo();
    }

    /**
     * Returns the resource type name of the element.<p>
     * 
     * @return the resource type name
     * @throws CmsException if something goes wrong
     */
    public String getTypeName() throws CmsException {

        if (m_isContainerBeanMode) {
            return OpenCms.getResourceManager().getResourceType(getResource().getTypeId()).getTypeName();
        }
        return m_formatterInfo.getResourceType().getTypeName();

    }

    /**
     * Checks if this element is a new or existing element, depending on the configuration file.<p>
     * 
     * @return <code>true</code> if this element is a new element, depending on the configuration file
     * 
     * @throws CmsException if something goes wrong
     */
    public boolean isNew() throws CmsException {

        CmsObject cms = getCmsObject();
        List<CmsResource> elems = getManager().getCreatableElements(cms, cms.getRequestContext().getUri(), getRequest());
        return elems.contains(getResource());
    }

    /**
     * Returns the workplace locale from the current user's settings.<p>
     * 
     * @return the workplace locale
     */
    protected Locale getWorkplaceLocale() {

        if (m_wpLocale == null) {
            m_wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject());
        }
        return m_wpLocale;
    }
}
