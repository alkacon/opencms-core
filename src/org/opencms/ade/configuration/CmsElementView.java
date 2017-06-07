/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.ade.configuration;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.CmsXmlVarLinkValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Comparator;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Represents a element view for the container page editor.<p>
 */
public class CmsElementView {

    /**
     * The element view comparator.<p>
     */
    public static class ElementViewComparator implements Comparator<CmsElementView> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(CmsElementView o1, CmsElementView o2) {

            int result;
            if (o1.getOrder() == o2.getOrder()) {
                result = o1.m_title.compareTo(o2.m_title);
            } else {
                result = o1.getOrder() > o2.getOrder() ? 1 : -1;
            }
            return result;
        }
    }

    /** The default element view. */
    public static final CmsElementView DEFAULT_ELEMENT_VIEW = new CmsElementView();

    /** Default order if not configured. */
    public static final int DEFAULT_ORDER = 1060;

    /** The default element view title key. */
    public static final String GUI_ELEMENT_VIEW_DEFAULT_TITLE_0 = "GUI_ELEMENT_VIEW_DEFAULT_TITLE_0";

    /** The order node. */
    public static final String N_ORDER = "Order";

    /** The title node. */
    public static final String N_TITLE = "Title";

    /** The title key node. */
    public static final String N_TITLE_KEY = "TitleKey";

    /** Special value indicating that no view is selected (used for parent view selection). */
    public static final String PARENT_NONE = "view://null";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsElementView.class);

    /** The view title. */
    String m_title;

    /** The explorer type. */
    private CmsExplorerTypeSettings m_explorerType;

    /** Synthetic id for non-resource views. */
    private CmsUUID m_id;

    /** The order. */
    private int m_order;

    /** The parent view id. */
    private CmsUUID m_parentViewId;

    /** The view resource. */
    private CmsResource m_resource;

    /** The title localization key. */
    private String m_titleKey;

    /**
     * Creates a new element view based on the given explorer type.<p>
     *
     * @param explorerType the explorer type
     */
    public CmsElementView(CmsExplorerTypeSettings explorerType) {
        m_explorerType = explorerType;
        m_id = getExplorerTypeViewId(explorerType.getName());
        m_titleKey = explorerType.getKey();
        m_order = Integer.valueOf(explorerType.getNewResourceOrder()).intValue();
    }

    /**
     * Constructor.<p>
     *
     * @param cms the cms context
     * @param resource the group resource
     *
     * @throws Exception  if parsing the resource fails
     */
    public CmsElementView(CmsObject cms, CmsResource resource)
    throws Exception {

        m_resource = resource;
        init(cms);
    }

    /**
     * Creates a new view with the given id, but initializes no other fields.<p>
     *
     * @param id the id
     */
    public CmsElementView(CmsUUID id) {
        m_id = id;
    }

    /**
     * Constructor for the default element view.<p>
     */
    private CmsElementView() {

        // the default view
        m_title = "Content elements";
        m_titleKey = GUI_ELEMENT_VIEW_DEFAULT_TITLE_0;
        m_order = 1050;
    }

    /**
     * Helper method to compute the uuid for views based on explorer types.<p>
     *
     * @param typeName the explorer type name
     * @return the element view id computed from the type name
     */
    public static CmsUUID getExplorerTypeViewId(String typeName) {

        return CmsUUID.getConstantUUID("elementview-" + typeName);

    }

    /**
     * Gets the explorer type settings.<p>
     *
     * @return the explorer type
     */
    public CmsExplorerTypeSettings getExplorerType() {

        return m_explorerType;

    }

    /**
     * Returns the element view id.<p>
     *
     * @return the group id
     */
    public CmsUUID getId() {

        if (m_id != null) {
            return m_id;
        } else if (m_resource != null) {
            return m_resource.getStructureId();
        } else {
            // only in case of the default element view
            return CmsUUID.getNullUUID();
        }
    }

    /**
     * The order.<p>
     *
     * @return the order
     */
    public int getOrder() {

        return m_order;
    }

    /**
     * Gets the parent view id (null if there is no parent view).<p>
     *
     * @return the parent view id
     */
    public CmsUUID getParentViewId() {

        return m_parentViewId;
    }

    /**
     * Returns the element view resource.<p>
     *
     * @return the element view resource
     */
    public CmsResource getResource() {

        return m_resource;
    }

    /**
     * Returns the element view title.<p>
     *
     * @param cms the cms context
     * @param locale the locale
     *
     * @return the title
     */
    public String getTitle(CmsObject cms, Locale locale) {

        if (m_titleKey == null) {
            return m_title;
        } else {
            return OpenCms.getWorkplaceManager().getMessages(locale).key(m_titleKey);
        }
    }

    /**
     * Checks whether the current user has permissions to use the element view.<p>
     *
     * @param cms the cms context
     * @param folder used for permission checks for explorertype based views
     *
     * @return <code>true</code> if the current user has permissions to use the element view
     **/
    public boolean hasPermission(CmsObject cms, CmsResource folder) {

        if ((m_explorerType != null) && (folder != null)) {
            CmsPermissionSet permSet = m_explorerType.getAccess().getPermissions(cms, folder);
            boolean result = permSet.requiresViewPermission();
            return result;
        }

        try {
            if (m_resource != null) {
                return cms.hasPermissions(
                    m_resource,
                    CmsPermissionSet.ACCESS_VIEW,
                    false,
                    CmsResourceFilter.IGNORE_EXPIRATION.addRequireVisible());
            } else {
                return OpenCms.getRoleManager().hasRole(cms, CmsRole.ELEMENT_AUTHOR);
            }

        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return false;
    }

    /**
     * 'Other types' view, for everything that isn't assigned to any other view.<p>
     *
     * @return true if this is the 'other types' view
     */
    public boolean isOther() {

        return (m_explorerType != null) && m_explorerType.getName().equals("view_other");
    }

    /**
     * Parses the edit view resource.<p>
     *
     * @param cms the cms context
     * @throws Exception if parsing the resource fails
     */
    @SuppressWarnings("null")
    private void init(CmsObject cms) throws Exception {

        CmsFile configFile = cms.readFile(m_resource);
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, configFile);
        m_title = content.getValue(N_TITLE, CmsConfigurationReader.DEFAULT_LOCALE).getStringValue(cms);
        I_CmsXmlContentValue titleKey = content.getValue(N_TITLE_KEY, CmsConfigurationReader.DEFAULT_LOCALE);
        if ((titleKey != null) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(titleKey.getStringValue(cms))) {
            m_titleKey = titleKey.getStringValue(cms);
        }
        I_CmsXmlContentValue orderVal = content.getValue(N_ORDER, CmsConfigurationReader.DEFAULT_LOCALE);
        if (orderVal != null) {
            try {
                m_order = Integer.parseInt(orderVal.getStringValue(cms));
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                m_order = DEFAULT_ORDER;
            }
        } else {
            m_order = DEFAULT_ORDER;
        }

        I_CmsXmlContentValue parentView = content.getValue("Parent", CmsConfigurationReader.DEFAULT_LOCALE);
        if (parentView != null) {
            CmsUUID parentViewId = null;
            try {
                CmsXmlVarLinkValue elementViewValue = (CmsXmlVarLinkValue)parentView;
                String stringValue = elementViewValue.getStringValue(cms);
                if (CmsStringUtil.isEmpty(stringValue)) {
                    parentViewId = CmsUUID.getNullUUID();
                } else if (stringValue.equals(PARENT_NONE)) {
                    parentViewId = null;
                } else if (stringValue.startsWith(CmsConfigurationReader.VIEW_SCHEME)) {
                    parentViewId = new CmsUUID(stringValue.substring(CmsConfigurationReader.VIEW_SCHEME.length()));
                } else {
                    parentViewId = elementViewValue.getLink(cms).getStructureId();
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            m_parentViewId = parentViewId;
        }

    }
}
