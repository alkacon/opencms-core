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

package org.opencms.workplace.tools.modules;

/**
 * Bean used to hold all information required to add a new XmlContent based resource type.<p>
 */
public class CmsResourceTypeInfoBean {

    /** The big icon image. */
    private String m_bigIcon;

    /** The description. */
    private String m_description;

    /** The resource type id. */
    private int m_id;

    /** The module name. */
    private String m_moduleName;

    /** The resource type name. */
    private String m_name;

    /** The nice name. */
    private String m_niceName;

    /** The resource type schema. */
    private String m_schema;

    /** The schema type name. */
    private String m_schemaTypeName;

    /** The small icon image. */
    private String m_smallIcon;

    /** The resource type title. */
    private String m_title;

    /**
     * Returns the big icon.<p>
     *
     * @return the big icon
     */
    public String getBigIcon() {

        return m_bigIcon;
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the id.<p>
     *
     * @return the id
     */
    public int getId() {

        return m_id;
    }

    /**
     * Returns the module name.<p>
     *
     * @return the module name
     */
    public String getModuleName() {

        return m_moduleName;
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the nice name.<p>
     *
     * @return the nice name
     */
    public String getNiceName() {

        return m_niceName;
    }

    /**
     * Returns the schema.<p>
     *
     * @return the schema
     */
    public String getSchema() {

        return m_schema;
    }

    /**
     * Returns the schema type name.<p>
     *
     * @return the schema type name
     */
    public String getSchemaTypeName() {

        return m_schemaTypeName;
    }

    /**
     * Returns the small icon.<p>
     *
     * @return the small icon
     */
    public String getSmallIcon() {

        return m_smallIcon;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Sets the big icon.<p>
     *
     * @param bigIcon the big icon to set
     */
    public void setBigIcon(String bigIcon) {

        m_bigIcon = bigIcon;
    }

    /**
     * Sets the description.<p>
     *
     * @param description the description to set
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * Sets the id.<p>
     *
     * @param id the id to set
     */
    public void setId(int id) {

        m_id = id;
    }

    /**
     * Sets the module name.<p>
     *
     * @param moduleName the module name to set
     */
    public void setModuleName(String moduleName) {

        m_moduleName = moduleName;
    }

    /**
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the nice name.<p>
     *
     * @param niceName the nice name to set
     */
    public void setNiceName(String niceName) {

        m_niceName = niceName;
    }

    /**
     * Sets the schema.<p>
     *
     * @param schema the schema to set
     */
    public void setSchema(String schema) {

        m_schema = schema;
    }

    /**
     * Sets the schema type name.<p>
     *
     * @param schemaTypeName the schema type name to set
     */
    public void setSchemaTypeName(String schemaTypeName) {

        m_schemaTypeName = schemaTypeName;
    }

    /**
     * Sets the small icon.<p>
     *
     * @param smallIcon the small icon to set
     */
    public void setSmallIcon(String smallIcon) {

        m_smallIcon = smallIcon;
    }

    /**
     * Sets the title.<p>
     *
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;
    }

}
