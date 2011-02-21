/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/edit/Attic/CmsNavModePropertyFormBuilder.java,v $
 * Date   : $Date: 2011/02/21 11:21:48 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.client.edit;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.shared.CmsClientProperty;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsPathValue;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.I_CmsHasGhostValue;
import org.opencms.gwt.client.ui.input.form.CmsBasicFormField;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Property form builder for the navigation mode.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsNavModePropertyFormBuilder extends A_CmsPropertyFormBuilder {

    /** The NavText property name. */
    public static final String PROP_NAVTEXT = "NavText";

    /**
     * @see org.opencms.ade.sitemap.client.edit.A_CmsPropertyFormBuilder#buildFields(org.opencms.ade.sitemap.shared.CmsClientSitemapEntry)
     */
    @Override
    public void buildFields(CmsClientSitemapEntry entry) {

        Map<String, CmsClientProperty> ownProps = entry.getOwnProperties();
        Map<String, CmsClientProperty> defaultFileProps = entry.getDefaultFileProperties();
        String entryId = entry.getId().toString();
        String defaultFileId = toStringOrNull(entry.getDefaultFileId());
        List<String> keys = new ArrayList<String>(m_propertyDefs.keySet());
        keys.remove(PROP_NAVTEXT);
        keys.add(0, PROP_NAVTEXT);
        for (String propName : keys) {
            buildSimpleField(entry, entryId, defaultFileId, ownProps, defaultFileProps, propName);
        }
    }

    /**
     * Builds a single form field.<p>
     * 
     * @param entry the entry 
     * @param entryId the entry id 
     * @param defaultFileId the default file id 
     * @param ownProps the entry's own properties 
     * @param defaultFileProps the default file properties 
     * @param propName the property name 
     */
    private void buildSimpleField(
        CmsClientSitemapEntry entry,
        String entryId,
        String defaultFileId,
        Map<String, CmsClientProperty> ownProps,
        Map<String, CmsClientProperty> defaultFileProps,
        String propName) {

        CmsXmlContentProperty propDef = m_propertyDefs.get(propName);
        CmsClientProperty fileProp = defaultFileProps.get(propName);
        CmsClientProperty ownProp = ownProps.get(propName);
        CmsPathValue pathValue;
        if (fileProp != null) {
            pathValue = fileProp.getPathValue().prepend(defaultFileId + "/" + propName);
        } else if (ownProp != null) {
            pathValue = ownProp.getPathValue().prepend(entryId + "/" + propName);
        } else {
            String targetId = null;
            if (propDef.isPreferFolder() || (entry.getDefaultFileId() == null)) {
                targetId = entryId;
            } else {
                targetId = entry.getDefaultFileId().toString();
            }
            pathValue = new CmsPathValue("", targetId + "/" + propName + "/" + CmsClientProperty.PATH_STRUCTURE_VALUE);
        }
        boolean alwaysAllowEmpty = !propName.equals(CmsClientProperty.PROPERTY_NAVTEXT);
        //CHECK: should we really generally allow empty fields other than NavText to be empty? 

        CmsBasicFormField field = CmsBasicFormField.createField(
            propDef,
            pathValue.getPath(),
            m_widgetFactory,
            new HashMap<String, String>(),
            alwaysAllowEmpty);
        String inherited = CmsSitemapView.getInstance().getController().getInheritedProperty(entry, propName);
        I_CmsFormWidget w = field.getWidget();
        // model binding not necessary here
        if (w instanceof I_CmsHasGhostValue) {
            ((I_CmsHasGhostValue)w).setGhostValue(
                inherited,
                CmsStringUtil.isEmptyOrWhitespaceOnly(pathValue.getValue()));
        }
        m_form.addField(CmsSitemapEntryEditor.TAB_1, field, pathValue.getValue());
    }

    /**
     * Helper method which converts an object to a string and returns null if it is null.<p>
     * 
     * @param obj the object for which to return the string
     * 
     * @return the string representation or null 
     */
    private String toStringOrNull(Object obj) {

        return obj == null ? null : obj.toString();
    }
}
