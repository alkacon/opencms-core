/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/template3/Attic/CmsTemplateMenu.java,v $
 * Date   : $Date: 2009/12/09 10:41:01 $
 * Version: $Revision: 1.5 $
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

package org.opencms.frontend.template3;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsContainerPageBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;

/**
 * Helper class to build a menu navigation with ul and li.<p>
 * 
 * @author Peter Bonrad
 * @author Michael Moossen
 * 
 * @since 7.6
 * 
 * @version $Revision: 1.5 $ 
 */
public class CmsTemplateMenu extends CmsJspActionElement {

    /**
     * Transformer that a properties of a resource from the OpenCms VFS, 
     * the input is used as String for the property name to read.<p>
     */
    public class CmsPropertyLoaderSingleTransformer implements Transformer {

        /** The resource where the properties are read from. */
        private CmsResource m_resource;

        /** Indicates if properties should be searched when loaded. */
        private boolean m_search;

        /**
         * Creates a new property loading Transformer.<p>
         * 
         * @param resource the resource where the properties are read from
         * @param search indicates if properties should be searched when loaded
         */
        public CmsPropertyLoaderSingleTransformer(CmsResource resource, boolean search) {

            m_resource = resource;
            m_search = search;
        }

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            String result;
            try {
                // read the properties of the requested resource
                result = getCmsObject().readPropertyObject(m_resource, String.valueOf(input), m_search).getValue();
            } catch (CmsException e) {
                // in case of any error we assume the property does not exist
                result = null;
            }
            return result;
        }
    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTemplateMenu.class);

    /** Lazy map with the flags if the elements of the navigation have children. */
    private Map<CmsJspNavElement, Boolean> m_children;

    /** Lazy map with the current elements of the navigation. */
    private Map<CmsJspNavElement, Boolean> m_current;

    /** The list with the elements of the menu. */
    private List<CmsJspNavElement> m_elements;

    /** The list with the elements of the menu, without detail pages. */
    private List<CmsJspNavElement> m_navElements;

    /** Lazy map with the navigation text of the elements. */
    private Map<CmsJspNavElement, String> m_navText;

    /** Properties loaded from the OpenCms VFS. */
    private Map<String, String> m_properties;

    /** Properties loaded from the OpenCms VFS with search. */
    private Map<String, String> m_propertiesSearch;

    /**
     * Empty constructor, required for every JavaBean.<p>
     */
    public CmsTemplateMenu() {

        super();
    }

    /**
     * Constructor, with parameters.<p>
     * 
     * Use this constructor for the template.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsTemplateMenu(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this();
        init(context, req, res);
    }

    /**
     * Returns the list with the elements of the menu.<p>
     *
     * @return the list with the elements of the menu
     */
    public List<CmsJspNavElement> getElements() {

        if (m_navElements == null) {
            // only return no detail pages
            m_navElements = new ArrayList<CmsJspNavElement>(m_elements.size());
            Iterator<CmsJspNavElement> it = m_elements.iterator();
            while (it.hasNext()) {
                CmsJspNavElement navElem = it.next();
                if (!navElem.getProperties().containsKey("detail-page")) {
                    m_navElements.add(navElem);
                }
            }
        }
        return m_navElements;
    }

    /**
     * Returns a lazy initialized map that provides the current elements as a key in the Map.<p> 
     * 
     * @return a lazy initialized map
     */
    public Map<CmsJspNavElement, Boolean> getHasChildren() {

        if (m_children == null) {
            m_children = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                /**
                 * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
                 */
                public Object transform(Object input) {

                    CmsJspNavElement elem = (CmsJspNavElement)input;

                    int currentLevel = elem.getNavTreeLevel();
                    int index = getElements().indexOf(elem);

                    if (index < getElements().size() - 1) {
                        CmsJspNavElement next = getElements().get(index + 1);
                        if (next.getNavTreeLevel() > currentLevel) {
                            return Boolean.TRUE;
                        }
                    }

                    return Boolean.FALSE;
                }
            });
        }
        return m_children;
    }

    /**
     * Returns a lazy initialized map that provides the current elements as a key in the Map.<p> 
     * 
     * @return a lazy initialized map
     */
    public Map<CmsJspNavElement, Boolean> getIsCurrent() {

        if (m_current == null) {
            m_current = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                /**
                 * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
                 */
                public Object transform(Object input) {

                    CmsJspNavElement elem = (CmsJspNavElement)input;

                    CmsJspNavElement uriElem = getNavigation().getNavigationForResource();
                    // check if uri matches resource name
                    if (elem.getResourceName().equals(uriElem.getResourceName())) {
                        return Boolean.TRUE;
                    }

                    // check if URI is NOT in the navigation and so a parent folder will be marked as current
                    CmsJspNavElement navElem = uriElem;
                    while ((navElem != null)
                        && (!navElem.isInNavigation() || navElem.getProperties().containsKey("detail-page"))) {
                        String parentPath = CmsResource.getParentFolder(navElem.getResourceName());
                        if (parentPath == null) {
                            break;
                        }
                        navElem = getNavigation().getNavigationForResource(parentPath);
                    }

                    if ((navElem != null)
                        && (!uriElem.isInNavigation() || uriElem.getProperties().containsKey("detail-page"))) {
                        return Boolean.valueOf(elem.equals(navElem));
                    }

                    return Boolean.FALSE;
                }
            });
        }
        return m_current;
    }

    /** 
     * Returns if the current uri is a default file of a folder.<p>
     * 
     * @return if the current uri is a default file of a folder
     */
    public boolean getIsDefault() {

        if ((m_elements == null) || m_elements.isEmpty()) {
            return false;
        }
        CmsJspNavElement uriElem = getNavigation().getNavigationForResource();
        CmsJspNavElement lastElem = m_elements.get(m_elements.size() - 1);
        return uriElem.getResourceName().equals(lastElem.getResourceName())
            && uriElem.getProperties().containsKey("detail-page");
    }

    /**
     * Returns a lazy initialized map that provides the navigation text as a key in the Map.<p> 
     * 
     * @return a lazy initialized map
     */
    public Map<CmsJspNavElement, String> getNavText() {

        if (m_navText == null) {
            m_navText = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                /**
                 * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
                 */
                public Object transform(Object input) {

                    CmsJspNavElement elem = (CmsJspNavElement)input;

                    String text = elem.getProperty(CmsPropertyDefinition.PROPERTY_NAVTEXT);
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(text)) {
                        text = elem.getProperty(CmsPropertyDefinition.PROPERTY_TITLE);
                    }

                    return text;
                }
            });
        }
        return m_navText;
    }

    /**
     * Reads the properties from the current resource, without search.<p>
     * 
     * Usage example on a JSP with the EL, reading the title property of the current resource: 
     * <code>${cms.properties['Title']}<code>
     * 
     * @return a map that lazily reads all resource properties from the OpenCms VFS, without search
     * 
     * @see #getPropertiesSearch()
     */
    public Map<String, String> getProperties() {

        if (m_properties == null) {
            CmsResource resUri;
            try {
                resUri = getResource();
                m_properties = CmsCollectionsGenericWrapper.createLazyMap(new CmsPropertyLoaderSingleTransformer(
                    resUri,
                    false));
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return m_properties;
    }

    /**
     * Reads the properties from the current resource, with search.<p>
     * 
     * Usage example on a JSP with the EL, reading the title property of the current resource: 
     * <code>${cms.propertiesSearch['Title']}<code>
     * 
     * @return a map that lazily reads all resource properties from the OpenCms VFS, with search
     * 
     * @see #getProperties()
     */
    public Map<String, String> getPropertiesSearch() {

        if (m_propertiesSearch == null) {
            CmsResource resUri;
            try {
                resUri = getResource();
                m_propertiesSearch = CmsCollectionsGenericWrapper.createLazyMap(new CmsPropertyLoaderSingleTransformer(
                    resUri,
                    true));
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return m_propertiesSearch;
    }

    /**
     * Returns the top level of the navigation.<p>
     * 
     * @return the top level of the navigation
     */
    public int getTopLevel() {

        if ((m_elements == null) || m_elements.isEmpty()) {
            return 0;
        }
        CmsJspNavElement elem = m_elements.get(0);
        if (elem == null) {
            return 0;
        }

        return elem.getNavTreeLevel();
    }

    /**
     * Sets the list with the elements of the menu.<p>
     *
     * @param elements the list with the elements of the menu to set
     */
    public void setElements(List<CmsJspNavElement> elements) {

        m_elements = elements;
    }

    /**
     * Returns the current resource.<p>
     * 
     * @return the current resource
     * 
     * @throws CmsException if something goes wrong
     */
    private CmsResource getResource() throws CmsException {

        CmsObject cms = getCmsObject();
        HttpServletRequest req = getRequest();
        CmsResource resUri;
        if (req.getParameter(CmsContainerPageBean.TEMPLATE_ELEMENT_PARAMETER) != null) {
            CmsUUID id = new CmsUUID(req.getParameter(CmsContainerPageBean.TEMPLATE_ELEMENT_PARAMETER));
            resUri = cms.readResource(id);
        } else {
            resUri = cms.readResource(cms.getRequestContext().getUri());
        }
        return resUri;
    }

}
