/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/edit/Attic/CmsSitemapPropertyFormBuilder.java,v $
 * Date   : $Date: 2011/02/14 10:02:24 $
 * Version: $Revision: 1.1 $
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
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.shared.CmsClientProperty;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsPathValue;
import org.opencms.gwt.client.ui.input.CmsDefaultStringModel;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.I_CmsHasGhostValue;
import org.opencms.gwt.client.ui.input.I_CmsStringModel;
import org.opencms.gwt.client.ui.input.form.CmsBasicFormField;
import org.opencms.gwt.client.ui.input.form.CmsForm;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetMultiFactory;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CmsSitemapPropertyFormBuilder {

    private boolean m_advanced;
    private CmsForm m_form;

    private I_CmsFormWidgetMultiFactory m_widgetFactory;

    private Map<String, CmsXmlContentProperty> m_propertyDefs;
    private List<String> m_propertyNames = null;

    private CmsSitemapEntryEditor m_editor;

    public void setEditor(CmsSitemapEntryEditor editor) {

        m_editor = editor;
    }

    public void buildFields(CmsClientSitemapEntry entry) {

        if (m_advanced) {
            buildAdvancedFields(entry);
        } else {
            buildSimpleFields(entry);
        }
    }

    public void setAdvanced(boolean advanced) {

        m_advanced = advanced;
    }

    public void setForm(CmsForm form) {

        m_form = form;
    }

    public void setPropertyDefinitions(Map<String, CmsXmlContentProperty> propertyDefs) {

        m_propertyDefs = propertyDefs;
    }

    public void setAllPropertyNames(Collection<String> propNames) {

        m_propertyNames = new ArrayList<String>(propNames);
        Collections.sort(m_propertyNames);
    }

    protected void buildAdvancedFields(CmsClientSitemapEntry entry) {

        Map<String, CmsDefaultStringModel> models = new HashMap<String, CmsDefaultStringModel>();
        internalBuildConfiguredFields(entry, models);
        internalBuildStructureAndResourceFields(entry, models);
    }

    private CmsDefaultStringModel createStringModel(CmsPathValue pathValue, Map<String, CmsDefaultStringModel> modelMap) {

        String path = pathValue.getPath();
        if (modelMap.containsKey(path)) {
            return modelMap.get(path);
        }
        CmsDefaultStringModel model = new CmsDefaultStringModel(path);
        modelMap.put(path, model);
        return model;
    }

    protected void internalBuildConfiguredFields(CmsClientSitemapEntry entry, Map<String, CmsDefaultStringModel> models) {

        Map<String, CmsClientProperty> ownProps = entry.getOwnInternalProperties();
        String entryId = entry.getId().toString();
        for (Map.Entry<String, CmsXmlContentProperty> mapEntry : m_propertyDefs.entrySet()) {
            String propName = mapEntry.getKey();
            CmsXmlContentProperty propDef = mapEntry.getValue();
            CmsClientProperty ownProp = ownProps.get(propName);
            if (ownProp == null) {
                ownProp = new CmsClientProperty(propName, "", "");
            }
            CmsPathValue pathValue;
            pathValue = ownProp.getPathValue().prepend(entryId + "/" + propName);
            I_CmsFormWidget widget = createWidget(propDef);
            CmsBasicFormField field = new CmsBasicFormField(
                pathValue.getPath() + "_" + (fieldIdSuffixCounter++),
                propDef.getDescription(),
                propDef.getNiceName(),
                propDef.getDefault(),
                widget);
            I_CmsStringModel model = createStringModel(pathValue, models);
            field.bind(model);
            m_form.addField(CmsSitemapEntryEditor.TAB_1, field, pathValue.getValue());
            setGhostValue(field, entry, propName);

        }
    }

    private CmsSitemapController getController() {

        return CmsSitemapView.getInstance().getController();
    }

    private void setGhostValue(I_CmsFormField field, CmsClientSitemapEntry entry, String name) {

        CmsSitemapController controller = getController();
        CmsClientSitemapEntry parent = CmsSitemapView.getInstance().getController().getParentEntry(entry);
        if ((parent != null) && (field instanceof I_CmsHasGhostValue)) {
            String ghostValue = controller.getInheritedProperty(entry, name);
            ((I_CmsHasGhostValue)field).setGhostValue(ghostValue, true);
        }

    }

    private CmsXmlContentProperty propertyDefinitionDefault(String name) {

        if (m_propertyDefs.containsKey(name)) {
            return m_propertyDefs.get(name);
        } else {
            return new CmsXmlContentProperty(name, "string", "string", "", null, null, null, name, null, null);
        }
    }

    private static int fieldIdSuffixCounter = 0;

    protected void internalBuildStructureAndResourceFields(
        CmsClientSitemapEntry entry,
        Map<String, CmsDefaultStringModel> models) {

        for (String propName : m_propertyNames) {
            CmsClientProperty prop = entry.getOwnInternalProperties().get(propName);
            CmsBasicFormField structureField = createStructureOrResourceField(
                entry.getId(),
                prop,
                propName,
                false,
                models);
            if (prop == null) {
                prop = new CmsClientProperty(propName, "", "");
            }

            m_form.addField(CmsSitemapEntryEditor.TAB_2, structureField, prop.getStructureValue());
            setGhostValue(structureField, entry, propName);

            CmsBasicFormField resourceField = createStructureOrResourceField(
                entry.getId(),
                prop,
                propName,
                true,
                models);
            m_form.addField(CmsSitemapEntryEditor.TAB_3, resourceField, prop.getResourceValue());
            setGhostValue(resourceField, entry, propName);
        }
    }

    private CmsBasicFormField createStructureOrResourceField(
        CmsUUID id,
        CmsClientProperty property,
        String name,
        boolean resource,
        Map<String, CmsDefaultStringModel> models) {

        CmsXmlContentProperty propDef = propertyDefinitionDefault(name);
        if (property == null) {
            property = new CmsClientProperty(name, "", "");
        }
        CmsPathValue pathValue;
        if (resource) {
            pathValue = new CmsPathValue(property.getResourceValue(), id.toString()
                + "/"
                + name
                + "/"
                + CmsClientProperty.PATH_RESOURCE_VALUE);
        } else {
            pathValue = new CmsPathValue(property.getStructureValue(), id.toString()
                + "/"
                + name
                + "/"
                + CmsClientProperty.PATH_STRUCTURE_VALUE);
        }
        I_CmsFormWidget widget = createWidget(propDef);
        I_CmsStringModel model = createStringModel(pathValue, models);
        CmsBasicFormField field = new CmsBasicFormField(
            pathValue.getPath() + "_" + (fieldIdSuffixCounter++),
            propDef.getDescription(),
            propDef.getNiceName(),
            propDef.getDefault(),
            widget);
        field.bind(model);
        return field;
    }

    public void setWidgetFactory(I_CmsFormWidgetMultiFactory factory) {

        m_widgetFactory = factory;
    }

    public I_CmsFormWidget createWidget(CmsXmlContentProperty propDef) {

        if (propDef != null) {
            String widgetConf = propDef.getWidgetConfiguration();
            if (widgetConf == null) {
                widgetConf = "";
            }
            return m_widgetFactory.createFormWidget(propDef.getWidget(), CmsStringUtil.splitAsMap(widgetConf, "|", ":"));
        } else {
            return new CmsTextBox();
        }
    }

    protected void buildSimpleFields(CmsClientSitemapEntry entry) {

        Map<String, CmsClientProperty> ownProps = entry.getOwnInternalProperties();
        Map<String, CmsClientProperty> defaultFileProps = entry.getDefaultFileInternalProperties();
        String entryId = entry.getId().toString();
        String defaultFileId = toStringOrNull(entry.getDefaultFileId());
        List<String> keys = new ArrayList<String>(m_propertyDefs.keySet());
        keys.remove("NavText");
        keys.add(0, "NavText");
        for (String propName : keys) {
            CmsXmlContentProperty propDef = m_propertyDefs.get(propName);
            CmsClientProperty fileProp = defaultFileProps.get(propName);
            CmsClientProperty ownProp = ownProps.get(propName);
            CmsPathValue pathValue;
            if (fileProp != null) {
                pathValue = fileProp.getPathValue().prepend(defaultFileId + "/" + propName);
            } else if (ownProp != null) {
                pathValue = ownProp.getPathValue().prepend(entryId + "/" + propName);
            } else {
                pathValue = new CmsPathValue("", entryId
                    + "/"
                    + propName
                    + "/"
                    + CmsClientProperty.PATH_STRUCTURE_VALUE);
            }
            I_CmsFormWidget widget = createWidget(propDef);
            CmsBasicFormField field = CmsBasicFormField.createField(
                propDef,
                pathValue.getPath(),
                m_widgetFactory,
                new HashMap<String, String>());
            String inherited = CmsSitemapView.getInstance().getController().getInheritedProperty(entry, propName);
            I_CmsFormWidget w = field.getWidget();

            //            CmsBasicFormField field = new CmsBasicFormField(
            //                pathValue.getPath(),
            //                propDef.getDescription(),
            //                propDef.getNiceName(),
            //                propDef.getDefault(),
            //                widget);
            // model binding not necessary here
            m_form.addField(CmsSitemapEntryEditor.TAB_1, field, pathValue.getValue());
            if (w instanceof I_CmsHasGhostValue) {
                ((I_CmsHasGhostValue)w).setGhostValue(
                    inherited,
                    CmsStringUtil.isEmptyOrWhitespaceOnly(pathValue.getValue()));
            }
        }
    }

    private String toStringOrNull(Object obj) {

        return obj == null ? null : obj.toString();
    }
}
