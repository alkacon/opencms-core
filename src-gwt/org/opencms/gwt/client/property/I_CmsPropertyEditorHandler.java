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

package org.opencms.gwt.client.property;

import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsClientTemplateBean;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Map;

/**
 * An interface for sitemap entry editor modes.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsPropertyEditorHandler {

    /**
     * Gets a list of the names of available properties.<p>
     *
     * @return a list of property names
     */
    List<String> getAllPropertyNames();

    /**
     * Returns the default file id, if available.<p>
     *
     * @return the default file id
     */
    CmsUUID getDefaultFileId();

    /**
     * Returns the default file properties, if available.<p>
     *
     * @return the default file properties
     */
    Map<String, CmsClientProperty> getDefaultFileProperties();

    /**
     * Returns the text which should be used for the title of the sitemap entry editor dialog.
     *
     * @return the dialog title for the sitemap entry editor
     */
    String getDialogTitle();

    /**
     * Returns the URL names which the new URL name of the entry must not be equal to.<p>
     *
     * @return a list of forbidden URL names
     */
    List<String> getForbiddenUrlNames();

    /**
     * Returns the structure id of the resource being edited.<p>
     *
     * @return the structure id of the resource being edited
     */
    CmsUUID getId();

    /**
     * Returns an inherited property value.<p>
     *
     * This is the value that the resource being edited would inherit if it didn't define its own value for that property.<p>
     *
     * @param name the name of the property
     * @return the inherited property
     */
    CmsClientProperty getInheritedProperty(String name);

    /**
     * Returns the class name which should be added when displaying resource info boxes.<p>
     *
     *
     * @return the class name to use for displaying resource info boxes
     */
    String getModeClass();

    /**
     * Returns the URL name with which the sitemap entry editor should be initialized.<p>
     *
     * @return the initial URL name
     */
    String getName();

    /**
     * Returns the properties of the resource being edited.<p>
     *
     * @return the properties of the resource being edited
     */
    Map<String, CmsClientProperty> getOwnProperties();

    /**
     * Returns the page info bean.<p>
     *
     * @return the page info bean
     */
    CmsListInfoBean getPageInfo();

    /**
     * Gets the path of the resource being edited.<p>
     *
     * @return the path of the resource being edited
     */
    String getPath();

    /**
     * Returns a map of beans representing the selectable templates.<p>
     *
     * @return a map of selectable templates
     */
    Map<String, CmsClientTemplateBean> getPossibleTemplates();

    /**
     * Handles the submit action for the sitemap entry editor.<p>
     *
     * @param newUrlName the new url name
     * @param vfsPath the new vfs path
     * @param propertyChanges the property changes
     * @param editedName if true, the URL name has been edited
     * @param reloadMode the information about which entry should reloaded
     */
    void handleSubmit(
        String newUrlName,
        String vfsPath,
        List<CmsPropertyModification> propertyChanges,
        boolean editedName,
        CmsReloadMode reloadMode);

    /**
     * Returns if the handled entry has an editable name.<p>
     *
     * @return <code>true</code> if the handled entry has an editable name
     */
    boolean hasEditableName();

    /**
     * Checks if the resource being edited is a folder.<p>
     *
     * @return true if the resource being edited is a folder
     */
    boolean isFolder();

    /**
     * Checks whether the property with the given name should be hidden.<p>
     *
     * @param key the property name
     *
     * @return true if the property should be hidden
     */
    boolean isHiddenProperty(String key);

    /**
     * Should return true if the sitemap editor is running in simple mode.<p>
     *
     * @return true if the sitemap editor is running in simple mode
     */
    boolean isSimpleMode();

    /**
     * Returns true if the property editor should use only ADE templates.<p>
     *
     * @return true if the property editor should use only ADE templates
     */
    boolean useAdeTemplates();

}
