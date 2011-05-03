/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/edit/Attic/CmsNavModeSitemapEntryEditor.java,v $
 * Date   : $Date: 2011/05/03 10:49:11 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.shared.CmsClientProperty;
import org.opencms.ade.sitemap.shared.CmsPathValue;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.I_CmsHasGhostValue;
import org.opencms.gwt.client.ui.input.form.CmsBasicFormField;
import org.opencms.gwt.client.ui.input.form.CmsSimpleFormFieldPanel;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Widget;

/**
 * Sitemap entry editor class for the navigation mode.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsNavModeSitemapEntryEditor extends A_CmsSitemapEntryEditor {

    /** The NavText property name. */
    public static final String PROP_NAVTEXT = "NavText";

    /**
     * Creates a new instance.<p>
     * 
     * @param handler the entry editor handler 
     */
    public CmsNavModeSitemapEntryEditor(I_CmsSitemapEntryEditorHandler handler) {

        super(handler);
    }

    /**
     * @see org.opencms.ade.sitemap.client.edit.A_CmsSitemapEntryEditor#buildFields()
     */
    @Override
    public void buildFields() {

        Map<String, CmsClientProperty> ownProps = m_entry.getOwnProperties();
        Map<String, CmsClientProperty> defaultFileProps = m_entry.getDefaultFileProperties();
        String entryId = m_entry.getId().toString();
        String defaultFileId = toStringOrNull(m_entry.getDefaultFileId());
        List<String> keys = new ArrayList<String>(m_propertyConfig.keySet());
        keys.remove(PROP_NAVTEXT);
        keys.add(0, PROP_NAVTEXT);
        for (String propName : keys) {
            buildSimpleField(entryId, defaultFileId, ownProps, defaultFileProps, propName);
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.edit.A_CmsSitemapEntryEditor#setupFieldContainer()
     */
    @Override
    protected void setupFieldContainer() {

        m_form.setWidget(new CmsSimpleFormFieldPanel());
    }

    /**
     * Builds a single form field.<p>
     * 

     * @param entryId the entry id 
     * @param defaultFileId the default file id 
     * @param ownProps the entry's own properties 
     * @param defaultFileProps the default file properties 
     * @param propName the property name 
     */
    private void buildSimpleField(
        String entryId,
        String defaultFileId,
        Map<String, CmsClientProperty> ownProps,
        Map<String, CmsClientProperty> defaultFileProps,
        String propName) {

        CmsXmlContentProperty propDef = m_propertyConfig.get(propName);
        CmsClientProperty fileProp = defaultFileProps == null ? null : defaultFileProps.get(propName);
        CmsClientProperty ownProp = ownProps.get(propName);
        CmsPathValue pathValue;
        if (!CmsClientProperty.isPropertyEmpty(fileProp)) {
            pathValue = fileProp.getPathValue().prepend(defaultFileId + "/" + propName);
        } else if (!CmsClientProperty.isPropertyEmpty(ownProp)) {
            pathValue = ownProp.getPathValue().prepend(entryId + "/" + propName);
        } else {
            String targetId = null;
            if (propDef.isPreferFolder() || (m_entry.getDefaultFileId() == null)) {
                targetId = entryId;
            } else {
                targetId = m_entry.getDefaultFileId().toString();
            }
            pathValue = new CmsPathValue("", targetId + "/" + propName + "/" + CmsClientProperty.PATH_STRUCTURE_VALUE);
        }
        boolean alwaysAllowEmpty = !propName.equals(CmsClientProperty.PROPERTY_NAVTEXT);
        //CHECK: should we really generally allow empty fields other than NavText to be empty? 

        CmsBasicFormField field = CmsBasicFormField.createField(
            propDef,
            pathValue.getPath(),
            this,
            new HashMap<String, String>(),
            alwaysAllowEmpty);
        CmsClientProperty inheritedProperty = CmsSitemapView.getInstance().getController().getInheritedPropertyObject(
            m_entry,
            propName);
        String inherited = (inheritedProperty == null) ? null : inheritedProperty.getEffectiveValue();
        if (inheritedProperty != null) {
            String message = Messages.get().key(
                Messages.GUI_PROPERTY_ORIGIN_2,
                inheritedProperty.getOrigin(),
                inherited);
            ((Widget)field.getWidget()).setTitle(message);
        }
        I_CmsFormWidget w = field.getWidget();
        // model binding not necessary here
        String initialValue = pathValue.getValue();

        boolean ghost = CmsStringUtil.isEmptyOrWhitespaceOnly(pathValue.getValue());
        if (w instanceof I_CmsHasGhostValue) {
            ((I_CmsHasGhostValue)w).setGhostValue(inherited, ghost);
            if (ghost) {
                initialValue = null;
            }
        }
        if (ghost && (inheritedProperty != null)) {
            String message = Messages.get().key(Messages.GUI_ORIGIN_INHERITED_1, inheritedProperty.getOrigin());
            field.getLayoutData().put("info", message);
        }
        m_form.addField(m_form.getWidget().getDefaultGroup(), field, initialValue);
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
