/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsExplorerContextMenuItem.java,v $
 * Date   : $Date: 2005/06/27 23:22:20 $
 * Version: $Revision: 1.9 $
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

package org.opencms.workplace.explorer;

/**
 * Provides information about a single context menu item for a resource type in the OpenCms explorer view.<p>
 * 
 * An item can be a context menu entry or a separator line.<p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.0.0 
 */
public class CmsExplorerContextMenuItem implements Comparable {

    /** The name for an entry type. */
    public static final String TYPE_ENTRY = "entry";
    
    /** The name for a separator type. */
    public static final String TYPE_SEPARATOR = "separator";

    private String m_key;
    private Integer m_order;
    private String m_rules;
    private String m_target;
    private String m_type;
    private String m_uri;

    /**
     * Constructor that creates a single context menu entry with all necessary information.<p>
     * @param type the item type (entry oder separator)
     * @param key the key for localization
     * @param uri the URI of the dialog
     * @param rules the set of display rules
     * @param target the frame target of the entry (e.g. "_top")
     * @param order the order of the item
     */
    public CmsExplorerContextMenuItem(
        String type,
        String key,
        String uri,
        String rules,
        String target,
        Integer order) {

        m_type = type;
        m_key = key;
        m_uri = uri;
        m_rules = rules;
        m_order = order;
        m_target = target;
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {

        return new CmsExplorerContextMenuItem(m_type, m_key, m_uri, m_rules, m_target, m_order);
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {

        if (obj == this) {
            return 0;
        }
        if (obj instanceof CmsExplorerContextMenuItem) {
            return m_order.compareTo(((CmsExplorerContextMenuItem)obj).m_order);
        }
        return 0;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsExplorerContextMenuItem) {
            return ((CmsExplorerContextMenuItem)obj).m_uri.equals(m_uri);
        }
        return false;
    }

    /**
     * Returns the key for localization.<p>
     * 
     * @return the key for localization
     */
    public String getKey() {

        return m_key;
    }

    /**
     * Returns the sort order of this item.<p>
     * 
     * @return the sort order of this item
     */
    public Integer getOrder() {

        return m_order;
    }

    /**
     * Returns the set of display rules.<p>
     * 
     * @return the set of display rules
     */
    public String getRules() {

        return m_rules;
    }

    /**
     * Returns the frame target of the current item.<p>
     * 
     * @return the frame target of the current item
     */
    public String getTarget() {

        return m_target;
    }

    /**
     * Returns the type of the current item.<p>
     * 
     * @return the type of the current item
     */
    public String getType() {

        return m_type;
    }

    /**
     * Returns the dialog URI of the current item.<p>
     * 
     * @return the dialog URI of the current item
     */
    public String getUri() {

        return m_uri;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return getUri().hashCode();
    }

    /**
     * Sets the key for localization.<p>
     * 
     * @param key the key for localization
     */
    public void setKey(String key) {

        m_key = key;
    }

    /**
     * Returns the sort order of this item.<p>
     * 
     * @param order the sort order of this item
     */
    public void setOrder(Integer order) {

        m_order = order;
    }

    /**
     * Sets the set of display rules.<p>
     * 
     * @param rules the set of display rules
     */
    public void setRules(String rules) {

        m_rules = rules;
    }

    /**
     * Sets the frame target of the current item.<p>
     * 
     * @param target the frame target of the current item
     */
    public void setTarget(String target) {

        m_target = target;
    }

    /**
     * Sets the type of the current item.<p>
     * 
     * @param type the type of the current item
     */
    public void setType(String type) {

        m_type = type;
    }

    /**
     * Sets the dialog URI of the current item.<p>
     * 
     * @param uri the dialog URI of the current item
     */
    public void setUri(String uri) {

        m_uri = uri;
    }
}