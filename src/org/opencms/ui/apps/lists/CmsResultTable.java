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

package org.opencms.ui.apps.lists;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.search.CmsSearchResource;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.I_CmsContextProvider;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsResourceTableProperty;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.converter.StringToDateConverter;
import com.vaadin.v7.ui.AbstractSelect.ItemDescriptionGenerator;

/**
 * Table to display the list manager search results.<p>
 */
public class CmsResultTable extends CmsFileTable {

    /** Separator string used in item ids. */
    protected static final String ID_SEPARATOR = ":";

    /** The serial version id. */
    private static final long serialVersionUID = 5680421086123351830L;

    /** The content locale. */
    private Locale m_contentLocale;

    /** The date field key. */
    private String m_dateFieldKey;

    /**
     * Constructor.<p>
     *
     * @param contextProvider the context provider
     * @param tableColumns the table columns
     */
    public CmsResultTable(I_CmsContextProvider contextProvider, Map<CmsResourceTableProperty, Integer> tableColumns) {

        super(contextProvider, tableColumns);
        m_fileTable.setConverter(CmsListManager.INSTANCEDATE_PROPERTY, new StringToDateConverter() {

            private static final long serialVersionUID = 1L;

            @Override
            protected DateFormat getFormat(Locale locale) {

                return new SimpleDateFormat(CmsVaadinUtils.getMessageText(Messages.GUI_LISTMANAGER_DATE_FORMAT_0));
            }
        });
    }

    /**
     * Returns the first visible item id.<p>
     *
     * @return the first visible item id
     */
    public String getCurrentPageFirstItemId() {

        return (String)m_fileTable.getCurrentPageFirstItemId();
    }

    /**
     * Returns the date field key.<p>
     *
     * @return the date field key
     */
    public String getDateFieldKey() {

        return m_dateFieldKey;
    }

    /**
     * Returns the currently selected items.<p>
     *
     * @return the selected items
     */
    public List<Item> getSelectedItems() {

        Collection<?> ids = (Collection<?>)m_fileTable.getValue();
        List<Item> items = new ArrayList<>();
        for (Object id : ids) {
            if (m_container.containsId(id)) {
                items.add(m_container.getItem(id));
            }
        }
        return items;
    }

    /**
     * @see org.opencms.ui.components.CmsResourceTable#getUUIDFromItemID(java.lang.String)
     */
    @Override
    public CmsUUID getUUIDFromItemID(String itemId) {

        if (itemId.contains(ID_SEPARATOR)) {
            return super.getUUIDFromItemID(itemId.substring(0, itemId.indexOf(ID_SEPARATOR)));
        } else {
            return super.getUUIDFromItemID(itemId);
        }
    }

    /**
     * Sets the content locale.<p>
     *
     * @param locale the content locale
     */
    public void setContentLocale(Locale locale) {

        m_contentLocale = locale;
        m_dateFieldKey = CmsSearchField.FIELD_INSTANCEDATE + "_" + m_contentLocale.toString() + "_dt";
    }

    /**
     * Sets the first visible item id.<p>
     *
     * @param itemId the item id
     */
    public void setCurrentPageFirstItemId(String itemId) {

        m_fileTable.setCurrentPageFirstItemId(itemId);
    }

    /**
     * Set the item description generator which generates tooltips for cells and rows in the Table.<p>
     *
     * @param generator the generator to use
     */
    public void setsetItemDescriptionGenerator(ItemDescriptionGenerator generator) {

        m_fileTable.setItemDescriptionGenerator(generator);
    }

    /**
     * @see org.opencms.ui.components.CmsFileTable#update(java.util.Collection, boolean)
     */
    @Override
    public void update(Collection<CmsUUID> ids, boolean remove) {

        // not supported, requires a complete refresh of the search result
    }

    /**
     * @see org.opencms.ui.components.CmsResourceTable#fillItem(org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.util.Locale)
     */
    @Override
    protected void fillItem(CmsObject cms, CmsResource resource, Locale locale) {

        if (resource instanceof CmsSearchResource) {
            String instanceDate = ((CmsSearchResource)resource).getField(m_dateFieldKey);
            String itemId = resource.getStructureId().toString();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(instanceDate)) {
                itemId += ID_SEPARATOR + instanceDate;
            }
            Item resourceItem = m_container.getItem(itemId);
            if (resourceItem == null) {
                resourceItem = m_container.addItem(itemId);
            }
            fillItemDefault(resourceItem, cms, resource, locale);
            for (I_ResourcePropertyProvider provider : m_propertyProviders) {
                provider.addItemProperties(resourceItem, cms, resource, locale);
            }
        } else {
            super.fillItem(cms, resource, locale);
        }
    }

}
