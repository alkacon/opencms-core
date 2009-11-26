/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/template3/Attic/CmsTemplateMenu.java,v $
 * Date   : $Date: 2009/11/26 11:36:25 $
 * Version: $Revision: 1.3 $
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

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.logging.Log;

/**
 * Helper class to build a menu navigation with ul and li.<p>
 * 
 * @author Peter Bonrad
 * @author Michael Moossen
 * 
 * @since 7.6
 * 
 * @version $Revision: 1.3 $ 
 */
public class CmsTemplateMenu extends CmsJspActionElement {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTemplateMenu.class);

    /** Lazy map with the flags if the elements of the navigation have children. */
    private Map<CmsJspNavElement, Boolean> m_children;

    /** Lazy map with the current elements of the navigation. */
    private Map<CmsJspNavElement, Boolean> m_current;

    /** The list with the elements of the menu. */
    private List<CmsJspNavElement> m_elements;

    /** Lazy map with the navigation text of the elements. */
    private Map<CmsJspNavElement, String> m_navText;

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

        return m_elements;
    }

    /**
     * Returns a lazy initialized map that provides the current elements as a key in the Map.<p> 
     * 
     * @return a lazy initialized map
     */
    @SuppressWarnings("unchecked")
    public Map<CmsJspNavElement, Boolean> getHasChildren() {

        if (m_children == null) {
            m_children = LazyMap.decorate(new HashMap<CmsJspNavElement, Boolean>(), new Transformer() {

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
    @SuppressWarnings("unchecked")
    public Map<CmsJspNavElement, Boolean> getIsCurrent() {

        if (m_current == null) {
            m_current = LazyMap.decorate(new HashMap<CmsJspNavElement, Boolean>(), new Transformer() {

                /** The log object for this class. */
                private final Log LOGG = CmsLog.getLog(CmsTemplateMenu.class);

                /**
                 * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
                 */
                public Object transform(Object input) {

                    CmsJspNavElement elem = (CmsJspNavElement)input;

                    String uri = getCmsObject().getRequestContext().getUri();
                    CmsJspNavElement uriElem = null;
                    try {
                        uriElem = new CmsJspNavElement(uri, CmsProperty.toMap(getCmsObject().readPropertyObjects(
                            uri,
                            false)));
                    } catch (CmsException e) {
                        // noop
                        LOGG.debug(e.getLocalizedMessage(), e);
                    }

                    // check if uri matches resource name
                    if (elem.getResourceName().equals(uri)) {
                        return Boolean.TRUE;
                    }

                    // check if the default file for the uri matches the resource name
                    String path = null;
                    try {
                        CmsResource resource = getCmsObject().readDefaultFile(elem.getResourceName());
                        path = getCmsObject().getSitePath(resource);
                    } catch (CmsException e) {
                        // resource not found or not enough permissions
                        LOGG.debug(e.getLocalizedMessage(), e);
                    }
                    if ((path == null) || ((uriElem != null) && uriElem.isInNavigation())) {
                        path = elem.getResourceName();
                    }

                    if (uri.equals(path)) {
                        return Boolean.TRUE;
                    }

                    // check if uri is in NOT in the navigation and so a parent folder will be marked as current
                    CmsJspNavElement navElem = uriElem;
                    while ((navElem != null) && !navElem.isInNavigation()) {

                        String parentPath = CmsResource.getParentFolder(navElem.getResourceName());
                        if (parentPath == null) {
                            break;
                        }
                        try {
                            navElem = new CmsJspNavElement(
                                parentPath,
                                CmsProperty.toMap(getCmsObject().readPropertyObjects(parentPath, false)));
                        } catch (CmsException e) {
                            LOGG.debug(e.getLocalizedMessage());
                            break;
                        }
                    }

                    if ((navElem != null) && (uriElem != null) && !uriElem.isInNavigation()) {
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

        String path = null;
        try {
            CmsResource resource = getCmsObject().readDefaultFile(getRequestContext().getUri());
            path = getCmsObject().getSitePath(resource);
        } catch (CmsException e) {
            // resource not found or not enough permissions
            LOG.debug(e.getLocalizedMessage(), e);
        }
        if (path != null) {
            return path.equals(getRequestContext().getUri());
        }
        return false;
    }

    /**
     * Returns a lazy initialized map that provides the navigation text as a key in the Map.<p> 
     * 
     * @return a lazy initialized map
     */
    @SuppressWarnings("unchecked")
    public Map<CmsJspNavElement, String> getNavText() {

        if (m_navText == null) {
            m_navText = LazyMap.decorate(new HashMap<CmsJspNavElement, String>(), new Transformer() {

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

}
