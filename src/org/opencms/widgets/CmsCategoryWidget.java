/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/CmsCategoryWidget.java,v $
 * Date   : $Date: 2007/11/06 10:34:44 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2007 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.widgets;

import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.relations.CmsCategoryService;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.I_CmsMacroResolver;
import org.opencms.workplace.CmsWorkplace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides a widget for a category based dependent select boxes.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 7.0.3 
 */
public class CmsCategoryWidget extends A_CmsWidget {

    /** Configuration parameter to set the category to display. */
    public static final String CONFIGURATION_CATEGORY = "category";

    /** Configuration parameter to set the key prefix for localization macros. */
    public static final String CONFIGURATION_KEY_PREFIX = "keyprefix";

    /** The displayed category. */
    private String m_category;

    /** The used key prefix for localization macros. */
    private String m_keyPrefix;

    /**
     * Creates a new category widget.<p>
     */
    public CmsCategoryWidget() {

        // empty constructor is required for class registration
        super();
    }

    /**
     * Creates a category widget with the specified options.<p>
     * 
     * @param configuration the configuration for the widget
     */
    public CmsCategoryWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#getConfiguration()
     */
    public String getConfiguration() {

        StringBuffer result = new StringBuffer(8);

        // append category to configuration
        if (m_category != null) {
            if (result.length() > 0) {
                result.append("|");
            }
            result.append(CONFIGURATION_CATEGORY);
            result.append("=");
            result.append(m_category);
        }
        // append key prefix to configuration
        if (m_keyPrefix != null) {
            if (result.length() > 0) {
                result.append("|");
            }
            result.append(CONFIGURATION_KEY_PREFIX);
            result.append("=");
            result.append(m_keyPrefix);
        }
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(16);
        result.append("<script type=\"text/javascript\" src=\"");
        result.append(CmsWorkplace.getSkinUri());
        result.append("components/widgets/category.js\"></script>\n");
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        // get select box options from default value String
        CmsResource selected = null;
        try {
            String name = param.getStringValue(cms);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(name)) {
                selected = cms.readResource(name);
            }
        } catch (CmsException e1) {
            // ignore
        }

        StringBuffer result = new StringBuffer(16);
        List levels = new ArrayList();
        try {
            // write arrays of categories
            result.append("<script language='javascript'>\n");
            CmsFolder folder = cms.readFolder("/system/categories/" + m_category);
            List subresources = cms.readResources(
                cms.getSitePath(folder),
                CmsResourceFilter.DEFAULT.addRequireFolder(),
                true);
            int baseLevel = CmsResource.getPathLevel(folder.getRootPath());
            int level;
            List options = new ArrayList();
            String jsId = CmsStringUtil.substitute(param.getId(), ".", "");
            for (level = baseLevel + 1; !subresources.isEmpty(); level++) {
                if (level != (baseLevel + 1)) {
                    result.append("var cat" + (level - baseLevel) + jsId + " = new Array(\n");
                }
                Iterator itSubs = subresources.iterator();
                while (itSubs.hasNext()) {
                    CmsResource res = (CmsResource)itSubs.next();
                    if (CmsResource.getPathLevel(res.getRootPath()) == level) {
                        if (level != (baseLevel + 1)) {
                            result.append("new Array('"
                                + res.getStructureId()
                                + "', '"
                                + cms.readResource(CmsResource.getParentFolder(res.getRootPath())).getStructureId()
                                + "', '"
                                + cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue(
                                    CmsResource.getName(res.getRootPath()))
                                + "'),\n");
                        }
                        if ((level == (baseLevel + 1))
                            || ((selected != null) && selected.getRootPath().startsWith(
                                CmsResource.getParentFolder(res.getRootPath())))) {
                            if (levels.size() < (level - baseLevel)) {
                                options = new ArrayList();
                                levels.add(options);
                                options.add(new CmsSelectWidgetOption("", true, getSelectValue(cms, widgetDialog)));
                            }
                            options.add(new CmsSelectWidgetOption(
                                res.getStructureId().toString(),
                                false,
                                cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue(
                                    CmsResource.getName(res.getRootPath()))));
                        }
                        itSubs.remove();
                    }
                }
                if (level != (baseLevel + 1)) {
                    result.deleteCharAt(result.length() - 1);
                    result.deleteCharAt(result.length() - 1);
                    result.append(");\n");
                }
            }
            result.append("</script>\n");

            result.append("<td class=\"xmlTd\" >");
            result.append("<input id='"
                + param.getId()
                + "' name='"
                + param.getId()
                + "' type='hidden' value='"
                + (selected != null ? selected.getStructureId().toString() : "")
                + "'>\n");

            result.append("<table cellspacing='0' cellpadding='0' border='0'>");

            for (int i = 1; i < level - baseLevel; i++) {
                result.append("<tr id='" + param.getId() + "cat" + i + "IdDisplay'");
                if (levels.size() >= i) {
                    options = (List)levels.get(i - 1);
                } else {
                    result.append(" style='display:none'");
                    options = new ArrayList();
                    options.add(new CmsSelectWidgetOption("", true, "Select Value"));
                }
                result.append(">");
                result.append("<td width='150'>" + getCategoryName(cms, widgetDialog, i) + "</td>");
                result.append("<td width='450'style=\"height: 25px;\">");
                result.append(buildSelectBox(
                    param.getId(),
                    i,
                    options,
                    selected != null ? cms.readResource(CmsResource.getPathPart(selected.getRootPath(), i + baseLevel)).getStructureId().toString()
                    : "",
                    param.hasError(),
                    (i == (level - baseLevel - 1))));
                result.append("</td>");
                result.append("</tr>");
            }
            result.append("</table>");
            result.append("</td>");
        } catch (CmsException e) {
            result.append(e.getLocalizedMessage());
        }
        return result.toString();
    }

    private String getSelectValue(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return CmsMacroResolver.resolveMacros(""
            + I_CmsMacroResolver.MACRO_DELIMITER
            + I_CmsMacroResolver.MACRO_START
            + (m_keyPrefix == null ? "key.category.selectvalue" : "key." + m_keyPrefix + ".category.selectvalue")
            + I_CmsMacroResolver.MACRO_END, cms, widgetDialog.getMessages());
    }

    private String getCategoryName(CmsObject cms, I_CmsWidgetDialog widgetDialog, int level) {

        return CmsMacroResolver.resolveMacros(""
            + I_CmsMacroResolver.MACRO_DELIMITER
            + I_CmsMacroResolver.MACRO_START
            + (m_keyPrefix == null ? "key.category.name." + level : "key." + m_keyPrefix + ".category.name." + level)
            + I_CmsMacroResolver.MACRO_END, cms, widgetDialog.getMessages());
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsCategoryWidget(getConfiguration());
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#setConfiguration(java.lang.String)
     */
    public void setConfiguration(String configuration) {

        // we have to validate later, since we do not have any cms object here
        m_category = "/";
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration)) {
            int categoryIndex = configuration.indexOf(CONFIGURATION_CATEGORY);
            if (categoryIndex != -1) {
                // category is given
                String category = configuration.substring(CONFIGURATION_CATEGORY.length() + 1);
                if (category.indexOf('|') != -1) {
                    // cut eventual following configuration values
                    category = category.substring(0, category.indexOf('|'));
                }
                m_category = category;
            }
            int keyPrefixIndex = configuration.indexOf(CONFIGURATION_KEY_PREFIX);
            if (keyPrefixIndex != -1) {
                // key prefix is given
                String keyPrefix = configuration.substring(keyPrefixIndex + CONFIGURATION_KEY_PREFIX.length() + 1);
                if (keyPrefix.indexOf('|') != -1) {
                    // cut eventual following configuration values
                    keyPrefix = keyPrefix.substring(0, keyPrefix.indexOf('|'));
                }
                m_keyPrefix = keyPrefix;
            }
        }
        super.setConfiguration(configuration);
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#setEditorValue(org.opencms.file.CmsObject, java.util.Map, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public void setEditorValue(
        CmsObject cms,
        Map formParameters,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        super.setEditorValue(cms, formParameters, widgetDialog, param);
        String id = param.getStringValue(cms);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(id)) {
            try {
                CmsResource res = cms.readResource(new CmsUUID(id));
                param.setStringValue(cms, res.getRootPath());
                String resName = ((String[])formParameters.get("resource"))[0];
                // remove the file from all categories
                CmsRelationFilter filter = CmsRelationFilter.TARGETS;
                filter = filter.filterType(CmsRelationType.CATEGORY);
                filter = filter.filterResource(cms.readResource("/system/categories/" + m_category));
                filter = filter.filterIncludeChildren();
                cms.deleteRelationsFromResource(resName, filter);
                // add the file to the selected categories
                CmsCategoryService.getInstance().addResourceToCategory(
                    cms,
                    resName,
                    res.getRootPath().substring("/system/categories".length()));
            } catch (Exception e) {
                param.setStringValue(cms, "");
            }
        }
    }

    private String buildSelectBox(
        String baseId,
        int level,
        List options,
        String selected,
        boolean hasError,
        boolean last) {

        StringBuffer result = new StringBuffer(16);
        String id = baseId + "cat" + level + "Id";
        String childId = baseId + "cat" + (level + 1) + "Id";
        result.append("<select class=\"xmlInput");
        if (hasError) {
            result.append(" xmlInputError");
        }
        result.append("\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\" onchange=\"");
        if (last) {
            result.append("setWidgetValue('" + baseId + "');");
        } else {
            String jsId = CmsStringUtil.substitute(baseId, ".", "");
            result.append("setChildListBox(this, getElemById('" + childId + "'), cat" + (level + 1) + jsId + ");");
        }
        result.append("\">");

        Iterator i = options.iterator();
        while (i.hasNext()) {
            CmsSelectWidgetOption option = (CmsSelectWidgetOption)i.next();
            // create the option
            result.append("<option value=\"");
            result.append(option.getValue());
            result.append("\"");
            if ((selected != null) && selected.equals(option.getValue())) {
                result.append(" selected=\"selected\"");
            }
            result.append(">");
            result.append(option.getOption());
            result.append("</option>");
        }
        result.append("</select>");
        return result.toString();
    }
}