/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/edit/Attic/CmsVfsModePropertyFormBuilder.java,v $
 * Date   : $Date: 2011/02/21 11:21:48 $
 * Version: $Revision: 1.2 $
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
import org.opencms.ade.sitemap.shared.CmsClientProperty.Mode;
import org.opencms.gwt.client.ui.input.CmsDefaultStringModel;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.I_CmsHasGhostValue;
import org.opencms.gwt.client.ui.input.form.CmsBasicFormField;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * The property form builder for the VFS mode of the sitemap editor.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsVfsModePropertyFormBuilder extends A_CmsPropertyFormBuilder {

    /** The field id counter.  */
    public static int fieldIdSuffixCounter;

    /** The map of tab names. */
    private static Map<CmsClientProperty.Mode, String> tabs;

    /** The map of models of the fields. */
    Map<String, CmsDefaultStringModel> m_models = new HashMap<String, CmsDefaultStringModel>();
    /** Flag which indicates whether the resource properties should be editable. */
    private boolean m_showResourceProperties;

    static {
        tabs = Maps.newHashMap();
        tabs.put(Mode.effective, "TAB1");
        tabs.put(Mode.structure, "TAB2");
        tabs.put(Mode.resource, "TAB3");
    }

    /**
     * @see org.opencms.ade.sitemap.client.edit.A_CmsPropertyFormBuilder#buildFields(org.opencms.ade.sitemap.shared.CmsClientSitemapEntry)
     */
    @Override
    public void buildFields(CmsClientSitemapEntry entry) {

        internalBuildConfiguredFields(entry);
        internalBuildOtherFields(entry);
    }

    /**
     * Sets the "show resource properties" flag which controls whether the resource value fields should be built.<p>
     * 
     * @param showResourceProperties if true, the resource value fields will be build 
     */
    public void setShowResourceProperties(boolean showResourceProperties) {

        m_showResourceProperties = showResourceProperties;
    }

    /**
     * Builds a single form field.<p>
     * 
     * @param entry the entry 
     * @param entryId the entry id 
     * @param ownProps the entry's own properties 
     * @param propName the property name
     * @param mode the mode which controls which kind of field will be built  
     */
    private void buildField(
        CmsClientSitemapEntry entry,
        String entryId,
        Map<String, CmsClientProperty> ownProps,
        final String propName,
        CmsClientProperty.Mode mode) {

        CmsXmlContentProperty propDef = m_propertyDefs.get(propName);

        if (propDef == null) {
            propDef = new CmsXmlContentProperty(
                propName,
                "string",
                "string",
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        }

        if (mode != Mode.effective) {
            propDef = propDef.withNiceName(propName);
        }

        CmsClientProperty ownProp = ownProps.get(propName);
        CmsPathValue pathValue = CmsClientProperty.getPathValue(ownProp, mode).prepend(entryId + "/" + propName + "/");

        //CHECK: should fields other than NavText be really automatically allowed to be empty in navigation mode?
        String tab = tabs.get(mode);
        CmsBasicFormField field = CmsBasicFormField.createField(
            propDef,
            pathValue.getPath() + "_" + tab,
            m_widgetFactory,
            new HashMap<String, String>(),
            true);
        String inherited = CmsSitemapView.getInstance().getController().getInheritedProperty(entry, propName);
        I_CmsFormWidget w = field.getWidget();
        CmsDefaultStringModel model = createStringModel(pathValue);
        field.bind(model);
        if (w instanceof I_CmsHasGhostValue) {
            ((I_CmsHasGhostValue)w).setGhostValue(
                inherited,
                CmsStringUtil.isEmptyOrWhitespaceOnly(pathValue.getValue()));
        }
        m_form.addField(tab, field, pathValue.getValue());
    }

    /**
     * Creates a string model for a given property path value, and returns the same model if the same path value is passed in.<p>
     * 
     * @param pathValue the path value
     *  
     * @return the model for that path value 
     */
    private CmsDefaultStringModel createStringModel(CmsPathValue pathValue) {

        String path = pathValue.getPath();
        if (m_models.containsKey(path)) {
            return m_models.get(path);
        }
        CmsDefaultStringModel model = new CmsDefaultStringModel(path);
        m_models.put(path, model);
        return model;
    }

    /**
     * Builds the fields for the configured properties in the first tab.<p>
     * 
     * @param entry the entry for which to build the fields 
     */
    private void internalBuildConfiguredFields(CmsClientSitemapEntry entry) {

        Map<String, CmsClientProperty> ownProps = entry.getOwnProperties();
        String entryId = entry.getId().toString();
        List<String> keys = new ArrayList<String>(m_propertyDefs.keySet());
        keys.remove(CmsClientProperty.PROPERTY_NAVTEXT);
        keys.add(0, CmsClientProperty.PROPERTY_NAVTEXT);
        for (String propName : keys) {
            buildField(entry, entryId, ownProps, propName, Mode.effective);
        }
    }

    /**
     * Builds the fields for the "structure" and "resource" tabs.<p>
     * 
     * @param entry the entry for which to build the fields 
     */
    private void internalBuildOtherFields(CmsClientSitemapEntry entry) {

        Map<String, CmsClientProperty> ownProps = entry.getOwnProperties();
        String entryId = entry.getId().toString();
        for (String propName : m_propertyNames) {
            buildField(entry, entryId, ownProps, propName, Mode.structure);
            if (m_showResourceProperties) {
                buildField(entry, entryId, ownProps, propName, Mode.resource);
            }
        }
    }

}
