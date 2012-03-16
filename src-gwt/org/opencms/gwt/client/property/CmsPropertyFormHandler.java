/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.ui.input.form.I_CmsFormHandler;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsPropertyModification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Specialized form handler class for editing properties.<p>
 * 
 * @since 8.0.0
 */
public class CmsPropertyFormHandler implements I_CmsFormHandler {

    /** The property editor handler. */
    private I_CmsPropertyEditorHandler m_handler;

    /** The dialog used for editing the properties. */
    private CmsFormDialog m_dialog;

    /**
     * Creates a new instance.<p>
     * 
     * @param handler the property editor handler
     * @param dialog the dialog used for editing the properties 
     */
    public CmsPropertyFormHandler(I_CmsPropertyEditorHandler handler, CmsFormDialog dialog) {

        m_dialog = dialog;
        m_handler = handler;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.I_CmsFormHandler#onSubmitForm(java.util.Map, java.util.Set)
     */
    public void onSubmitForm(Map<String, String> fieldValues, Set<String> editedFields) {

        CmsReloadMode reloadMode = getReloadMode(fieldValues, editedFields);
        Map<String, String> changedPropValues = removeTabSuffixes(fieldValues);
        Set<String> editedModels = removeTabSuffixes(editedFields);
        changedPropValues.keySet().retainAll(editedModels);
        List<CmsPropertyModification> propChanges = getPropertyChanges(changedPropValues);
        if (!m_handler.hasEditableName()) {
            // The root element's name can't be edited 
            m_dialog.hide();
            m_handler.handleSubmit(
                "",
                null,
                propChanges,
                editedFields.contains(A_CmsPropertyEditor.FIELD_URLNAME),
                reloadMode);
            return;
        }
        final String urlNameValue = getAndRemoveValue(fieldValues, A_CmsPropertyEditor.FIELD_URLNAME);
        fieldValues.remove(A_CmsPropertyEditor.FIELD_LINK);
        //final CmsLinkBean link = m_linkSelector.getLinkBean();
        //CmsClientSitemapEntry.setRedirect(fieldValues, link);
        m_handler.handleSubmit(
            urlNameValue,
            null,
            propChanges,
            editedFields.contains(A_CmsPropertyEditor.FIELD_URLNAME),
            reloadMode);
    }

    /** 
     * Helper method which retrieves a value for a given key from a map and then deletes the entry for the key.<p>
     * 
     * @param map the map from which to retrieve the value 
     * @param key the key
     * 
     * @return the removed value  
     */
    protected String getAndRemoveValue(Map<String, String> map, String key) {

        String value = map.get(key);
        if (value != null) {
            map.remove(key);
        }
        return value;
    }

    /**
     * Converts a map of field values to a list of property changes.<p>
     * 
     * @param fieldValues the field values 
     * @return the property changes
     */
    protected List<CmsPropertyModification> getPropertyChanges(Map<String, String> fieldValues) {

        List<CmsPropertyModification> result = new ArrayList<CmsPropertyModification>();
        for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.contains("/")) {
                CmsPropertyModification propChange = new CmsPropertyModification(key, value);
                result.add(propChange);
            }
        }
        return result;
    }

    /** 
     * Removes the tab suffix from a field id.<p>
     * 
     * @param fieldId a field id 
     * 
     * @return the field id without the suffix 
     */
    protected String removeTabSuffix(String fieldId) {

        return fieldId.replaceAll("#.*$", "");
    }

    /**
     * Removes the tab suffixes from each field id of a collection.<p> 
     * 
     * @param fieldIds the field ids from which to remove the tab suffix
     *   
     * @return a new collection of field ids without tab suffixes 
     */
    protected Set<String> removeTabSuffixes(Collection<String> fieldIds) {

        Set<String> result = new HashSet<String>();
        for (String fieldId : fieldIds) {
            result.add(removeTabSuffix(fieldId));
        }
        return result;
    }

    /**
     * Removes the tab suffixes from the keys of a map.<p>
     * 
     * @param fieldValues a map of field values 
     * 
     * @return a new map of field values, with tab suffixes removed from the keys
     */
    protected Map<String, String> removeTabSuffixes(Map<String, String> fieldValues) {

        Map<String, String> result = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
            String key = entry.getKey();
            String newKey = removeTabSuffix(key);
            result.put(newKey, entry.getValue());
        }
        return result;
    }

    /**
     * Check if a field name belongs to one of a given list of properties.<p>
     * 
     * @param fieldName the field name 
     * @param propNames the property names 
     * 
     * @return true if the field name matches one of the property names 
     */
    private boolean checkContains(String fieldName, String... propNames) {

        for (String propName : propNames) {
            if (fieldName.contains("/" + propName + "/")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the reload mode to use for the given changes.<p>
     * 
     * @param fieldValues the field values 
     * @param editedFields the set of edited fields
     * 
     * @return the reload mode 
     */
    private CmsReloadMode getReloadMode(Map<String, String> fieldValues, Set<String> editedFields) {

        for (String fieldName : editedFields) {
            if (checkContains(fieldName, CmsClientProperty.PROPERTY_DEFAULTFILE, CmsClientProperty.PROPERTY_NAVPOS)) {
                return CmsReloadMode.reloadParent;

            }
            if (checkContains(fieldName, CmsClientProperty.PROPERTY_NAVTEXT)) {
                return CmsReloadMode.reloadEntry;
            }
        }
        return CmsReloadMode.none;
    }
}
