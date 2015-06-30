/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.acacia.shared;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Contains all information defining a content entity type.<p>
 */
public class CmsContentDefinition implements IsSerializable {

    /** The name of the native renderer. */
    public static final String NATIVE_RENDERER = "native";

    /** The member name for the form rendering function. */
    public static final String FUNCTION_RENDER_FORM = "renderForm";

    /** The member name for the inline rendering function. */
    public static final String FUNCTION_RENDER_INLINE = "renderInline";

    /** The parameter name for the initialization function. */
    public static final String PARAM_INIT_CALL = "init";

    /** The attribute configurations. */
    private Map<String, CmsAttributeConfiguration> m_configurations;

    /** The locale specific entities. */
    private Map<String, CmsEntity> m_entities;

    /** The entity. */
    private String m_entityId;

    /** Indicates if optional fields should be grouped together. */
    private boolean m_groupOptionalFields;

    /** The content locale. */
    private String m_locale;

    /** The tab information beans. */
    private List<CmsTabInfo> m_tabInfos;

    /** The types defining the entity. */
    private Map<String, CmsType> m_types;

    /**
     * Constructor.<p>
     *
     * @param entityId the entity id
     * @param entities the locale specific entities
     * @param configurations the attribute configurations
     * @param types the types
     * @param tabInfos the tab information beans
     * @param groupOptionalFields <code>true</code> if optional fields should be grouped together
     * @param locale the content locale
     */
    public CmsContentDefinition(
        String entityId,
        Map<String, CmsEntity> entities,
        Map<String, CmsAttributeConfiguration> configurations,
        Map<String, CmsType> types,
        List<CmsTabInfo> tabInfos,
        boolean groupOptionalFields,
        String locale) {

        m_entityId = entityId;
        m_entities = entities;
        m_configurations = configurations;
        m_types = types;
        m_tabInfos = tabInfos;
        m_groupOptionalFields = groupOptionalFields;
        m_locale = locale;
    }

    /**
     * Constructor. Used for serialization only.<p>
     */
    protected CmsContentDefinition() {

        // nothing to do
    }

    /**
     * Extracts the attribute index from the given attribute name where the index is appended to the name like 'attributename[1]'.<p>
     *
     * @param attributeName the attribute name
     *
     * @return the extracted index
     */
    public static int extractIndex(String attributeName) {

        int index = 0;
        // check if the value index is appended to the attribute name
        if (hasIndex(attributeName)) {
            try {
                String temp = attributeName.substring(attributeName.lastIndexOf("[") + 1, attributeName.length() - 1);

                index = Integer.parseInt(temp);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return index;
    }

    /**
     * Checks if the given XPATH component has an index.<p>
     *
     * @param pathComponent the path component
     *
     * @return true if the argument contains an index
     */
    public static boolean hasIndex(String pathComponent) {

        return pathComponent.endsWith("]") && pathComponent.contains("[");
    }

    /**
     * Removes an attribute index suffix from the given attribute name.<p>
     *
     * @param attributeName the attribute name
     *
     * @return the attribute name
     */
    public static String removeIndex(String attributeName) {

        if (hasIndex(attributeName)) {
            attributeName = attributeName.substring(0, attributeName.lastIndexOf("["));
        }
        return attributeName;
    }

    /**
     * Returns the attribute configurations.<p>
     *
     * @return the attribute configurations
     */
    public Map<String, CmsAttributeConfiguration> getConfigurations() {

        return m_configurations;
    }

    /**
     * Returns the locale specific entities of the content.<p>
     *
     * @return the locale specific entities of the content
     */
    public Map<String, CmsEntity> getEntities() {

        return m_entities;
    }

    /**
     * Returns the entity.<p>
     *
     * @return the entity
     */
    public CmsEntity getEntity() {

        return m_entities.get(m_entityId);
    }

    /**
     * Returns the entity id.<p>
     *
     * @return the entity id
     */
    public String getEntityId() {

        return m_entityId;
    }

    /**
     * Returns the entity type name.<p>
     *
     * @return the entity type name
     */
    public String getEntityTypeName() {

        return getEntity().getTypeName();
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public String getLocale() {

        return m_locale;
    }

    /**
     * Returns the tab information beans.<p>
     *
     * @return the tab information beans
     */
    public List<CmsTabInfo> getTabInfos() {

        return m_tabInfos;
    }

    /**
     * Returns the types.<p>
     *
     * @return the types
     */
    public Map<String, CmsType> getTypes() {

        return m_types;
    }

    /**
     * Returns if optional fields should be grouped together.<p>
     *
     * @return <code>true</code> if optional fields should be grouped together
     */
    public boolean isGroupOptionalFields() {

        return m_groupOptionalFields;
    }
}
