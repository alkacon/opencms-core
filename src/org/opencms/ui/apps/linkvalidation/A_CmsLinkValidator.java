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

package org.opencms.ui.apps.linkvalidation;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.ui.components.CmsResourceTable.I_ResourcePropertyProvider;
import org.opencms.ui.components.CmsResourceTableProperty;
import org.opencms.util.CmsStringUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.vaadin.v7.data.Item;
import com.vaadin.v7.event.ItemClickEvent.ItemClickListener;

/**
 * Validator for links.<p>
 */
public abstract class A_CmsLinkValidator implements I_ResourcePropertyProvider {

    /** Property.*/
    protected CmsResourceTableProperty property;

    /**
     * Empty constructor.<p>
     */
    public A_CmsLinkValidator() {

    }

    /**
     * @see org.opencms.ui.components.CmsResourceTable.I_ResourcePropertyProvider#addItemProperties(com.vaadin.v7.data.Item, org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.util.Locale)
     */
    public void addItemProperties(Item resourceItem, CmsObject cms, CmsResource resource, Locale locale) {

        String val = "";
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(failMessage(resource))) {
            val = failMessage(resource);
        }
        resourceItem.getItemProperty(property).setValue(val);

    }

    /**
     * Returns resources which fail the validations.<p>
     *
     * @param resources to check
     * @return List of failed resources
     */
    public abstract List<CmsResource> failedResources(List<String> resources);

    /**
     *  Get fail message for resource.
     * @param resource to get message for
     * @return Message
     * */
    public abstract String failMessage(CmsResource resource);

    /** Get click listener
     * @return ItemClickListener or null
     * */
    public abstract ItemClickListener getClickListener();

    /** Get property Name.
     * @return Name of property
     * */
    public abstract String getPropertyName();

    /** Get all properties.
     * @return Map of table properties
     * */
    public abstract Map<CmsResourceTableProperty, Integer> getTableProperties();

    /**Get  table property.
     * @return property
     * */
    public CmsResourceTableProperty getTableProperty() {

        return property;
    }

}
