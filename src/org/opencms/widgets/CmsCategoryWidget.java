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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.content.I_CmsXmlContentHandler.DisplayType;
import org.opencms.xml.types.A_CmsXmlContentValue;
import org.opencms.xml.types.CmsXmlCategoryValue;
import org.opencms.xml.types.CmsXmlDynamicCategoryValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;

/**
 * Provides a widget for a category based dependent select boxes.<p>
 *
 * @since 7.0.3
 */
public class CmsCategoryWidget extends A_CmsWidget implements I_CmsADEWidget {

    /** Configuration parameter to set the category to display. */
    public static final String CONFIGURATION_CATEGORY = "category";

    /** Configuration parameter to set the 'only leaf' flag parameter. */
    public static final String CONFIGURATION_ONLYLEAFS = "onlyleafs";

    /** Configuration parameter to set the 'property' parameter. */
    public static final String CONFIGURATION_PROPERTY = "property";

    /** Configuration parameter to set the 'selection type' parameter. */
    private static final String CONFIGURATION_PARENTSELECTION = "parentSelection";

    /** Configuration parameter to set the 'selection type' parameter. */
    private static final String CONFIGURATION_SELECTIONTYPE = "selectiontype";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCategoryWidget.class);

    /** The displayed category. */
    private String m_category;

    /** The 'only leaf' flag. */
    private String m_onlyLeafs;

    /** The value if the parents should be selected with the children. */
    private boolean m_parentSelection;

    /** The property to read the starting category from. */
    private String m_property;

    /** The selection type parsed from configuration string. */
    private String m_selectiontype = "multi";

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
    @Override
    public String getConfiguration() {

        StringBuffer result = new StringBuffer(8);

        if (m_category != null) {
            result.append(CONFIGURATION_CATEGORY);
            result.append("=");
            result.append(m_category);
        }
        // append 'only leafs' to configuration
        if (m_onlyLeafs != null) {
            if (result.length() > 0) {
                result.append("|");
            }
            result.append(CONFIGURATION_ONLYLEAFS);
            result.append("=");
            result.append(m_onlyLeafs);
        }
        // append 'property' to configuration
        if (m_property != null) {
            if (result.length() > 0) {
                result.append("|");
            }
            result.append(CONFIGURATION_PROPERTY);
            result.append("=");
            result.append(m_property);
        }
        // append 'selectionType' to configuration
        if (m_selectiontype != null) {
            if (result.length() > 0) {
                result.append("|");
            }
            result.append(CONFIGURATION_SELECTIONTYPE);
            result.append("=");
            result.append(m_selectiontype);
        }
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getConfiguration(org.opencms.file.CmsObject, org.opencms.xml.types.A_CmsXmlContentValue, org.opencms.i18n.CmsMessages, org.opencms.file.CmsResource, java.util.Locale)
     */
    public String getConfiguration(
        CmsObject cms,
        A_CmsXmlContentValue schemaType,
        CmsMessages messages,
        CmsResource resource,
        Locale contentLocale) {

        StringBuffer result = new StringBuffer();

        // NOTE: set starting category as "category=" - independently if it is set via "property" or "category" config option.
        String startingCategory = this.getStartingCategory(cms, cms.getSitePath(resource));
        if (startingCategory.length() > 1) {
            result.append(CONFIGURATION_CATEGORY).append("=").append(startingCategory);
        }
        // append 'only leafs' to configuration
        if (m_onlyLeafs != null) {
            if (result.length() > 0) {
                result.append("|");
            }
            result.append(CONFIGURATION_ONLYLEAFS);
            result.append("=");
            result.append(m_onlyLeafs);
        }
        // append 'selection type' to configuration in case of the schemaType
        if (!m_selectiontype.equals("single")
            && (schemaType.getTypeName().equals(CmsXmlCategoryValue.TYPE_NAME)
                || schemaType.getTypeName().equals(CmsXmlDynamicCategoryValue.TYPE_NAME))) {
            m_selectiontype = "multi";
        } else {
            m_selectiontype = "single";
        }

        if (m_selectiontype != null) {
            if (result.length() > 0) {
                result.append("|");
            }
            result.append(CONFIGURATION_SELECTIONTYPE);
            result.append("=");
            result.append(m_selectiontype);
        }

        if (m_parentSelection) {
            if (result.length() > 0) {
                result.append("|");
            }
            result.append(CONFIGURATION_PARENTSELECTION);
        }
        CmsCategoryService catService = CmsCategoryService.getInstance();
        List<String> categoriesList = catService.getCategoryRepositories(cms, cms.getSitePath(resource));
        Iterator<String> it = categoriesList.iterator();
        String catList = "|CategoryList=";
        int i = 0;
        while (it.hasNext()) {
            if (i > 0) {
                catList += ",";
            }
            String rootPath = it.next();
            catList += rootPath;
            i++;
        }

        return result.append(catList).toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getCssResourceLinks(org.opencms.file.CmsObject)
     */
    public List<String> getCssResourceLinks(CmsObject cms) {

        // nothing to do
        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getDefaultDisplayType()
     */
    public DisplayType getDefaultDisplayType() {

        return DisplayType.wide;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
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
        CmsCategory selected = null;
        try {
            String name = param.getStringValue(cms);
            selected = CmsCategoryService.getInstance().getCategory(cms, name);
        } catch (CmsException e) {
            // ignore
        }

        StringBuffer result = new StringBuffer(16);
        List<List<CmsSelectWidgetOption>> levels = new ArrayList<List<CmsSelectWidgetOption>>();
        try {
            // write arrays of categories
            result.append("<script language='javascript'>\n");
            String referencePath = null;
            try {
                referencePath = cms.getSitePath(getResource(cms, param));
            } catch (Exception e) {
                // ignore, this can happen if a new resource is edited using direct edit
            }
            String startingCat = getStartingCategory(cms, referencePath);
            List<CmsCategory> cats = CmsCategoryService.getInstance().readCategories(
                cms,
                startingCat,
                true,
                referencePath);
            int baseLevel;
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(startingCat)) {
                baseLevel = 0;
            } else {
                baseLevel = CmsResource.getPathLevel(startingCat);
                if (!(startingCat.startsWith("/") && startingCat.endsWith("/"))) {
                    baseLevel++;
                }
            }
            int level;
            Set<String> done = new HashSet<String>();
            List<CmsSelectWidgetOption> options = new ArrayList<CmsSelectWidgetOption>();
            String jsId = CmsStringUtil.substitute(param.getId(), ".", "");
            for (level = baseLevel + 1; !cats.isEmpty(); level++) {
                if (level != (baseLevel + 1)) {
                    result.append("var cat" + (level - baseLevel) + jsId + " = new Array(\n");
                }
                Iterator<CmsCategory> itSubs = cats.iterator();
                while (itSubs.hasNext()) {
                    CmsCategory cat = itSubs.next();
                    String title = cat.getTitle();
                    String titleJs = StringEscapeUtils.escapeJavaScript(title);
                    String titleHtml = StringEscapeUtils.escapeHtml(title);
                    if ((CmsResource.getPathLevel(cat.getPath()) + 1) == level) {
                        itSubs.remove();
                        if (done.contains(cat.getPath())) {
                            continue;
                        }
                        if (level != (baseLevel + 1)) {
                            result.append(
                                "new Array('"
                                    + cat.getId()
                                    + "', '"
                                    + CmsCategoryService.getInstance().readCategory(
                                        cms,
                                        CmsResource.getParentFolder(cat.getPath()),
                                        referencePath).getId()
                                    + "', '"
                                    + titleJs
                                    + "'),\n");
                        }
                        if ((level == (baseLevel + 1))
                            || ((selected != null)
                                && selected.getPath().startsWith(CmsResource.getParentFolder(cat.getPath())))) {
                            if (levels.size() < (level - baseLevel)) {
                                options = new ArrayList<CmsSelectWidgetOption>();
                                levels.add(options);
                                options.add(
                                    new CmsSelectWidgetOption(
                                        "",
                                        true,
                                        Messages.get().getBundle(widgetDialog.getLocale()).key(
                                            Messages.GUI_CATEGORY_SELECT_0)));
                            }
                            options.add(new CmsSelectWidgetOption(cat.getId().toString(), false, titleHtml));
                        }
                        done.add(cat.getPath());
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
            result.append(
                "<input id='"
                    + param.getId()
                    + "' name='"
                    + param.getId()
                    + "' type='hidden' value='"
                    + (selected != null ? selected.getId().toString() : "")
                    + "'>\n");

            for (int i = 1; i < (level - baseLevel); i++) {
                result.append("<span id='" + param.getId() + "cat" + i + "IdDisplay'");
                if (levels.size() >= i) {
                    options = levels.get(i - 1);
                } else {
                    result.append(" style='display:none'");
                    options = new ArrayList<CmsSelectWidgetOption>();
                    options.add(
                        new CmsSelectWidgetOption(
                            "",
                            true,
                            Messages.get().getBundle(widgetDialog.getLocale()).key(Messages.GUI_CATEGORY_SELECT_0)));
                }
                result.append(">");
                result.append(
                    buildSelectBox(
                        param.getId(),
                        i,
                        options,
                        (selected != null
                        ? CmsCategoryService.getInstance().readCategory(
                            cms,
                            CmsResource.getPathPart(selected.getPath(), i + baseLevel),
                            referencePath).getId().toString()
                        : ""),
                        param.hasError(),
                        (i == (level - baseLevel - 1))));
                result.append("</span>&nbsp;");
            }
            result.append("</td>");
        } catch (CmsException e) {
            result.append(e.getLocalizedMessage());
        }
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getInitCall()
     */
    public String getInitCall() {

        // nothing to do
        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getJavaScriptResourceLinks(org.opencms.file.CmsObject)
     */
    public List<String> getJavaScriptResourceLinks(CmsObject cms) {

        // nothing to do
        return null;
    }

    /**
     * Returns the starting category depending on the configuration options.<p>
     *
     * @param cms the cms context
     * @param referencePath the right resource path
     *
     * @return the starting category
     */
    public String getStartingCategory(CmsObject cms, String referencePath) {

        String ret = "";
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_category) && CmsStringUtil.isEmptyOrWhitespaceOnly(m_property)) {
            ret = "/";
        } else if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_property)) {
            ret = m_category;
        } else {
            // use the given property from the right file
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(referencePath)) {
                try {
                    ret = cms.readPropertyObject(referencePath, m_property, true).getValue("/");
                } catch (CmsException ex) {
                    // should never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(ex.getLocalizedMessage(), ex);
                    }
                }
            }
        }
        if (!ret.endsWith("/")) {
            ret += "/";
        }
        if (ret.startsWith("/")) {
            ret = ret.substring(1);
        }
        return ret;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getWidgetName()
     */
    public String getWidgetName() {

        return CmsCategoryWidget.class.getName();
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#isCompactViewEnabled()
     */
    @Override
    public boolean isCompactViewEnabled() {

        return false;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#isInternal()
     */
    public boolean isInternal() {

        return false;
    }

    /**
     * Check if only leaf selection is allowed.<p>
     *
     * @return <code>true</code>, if only leaf selection is allowed
     */
    public boolean isOnlyLeafs() {

        return Boolean.valueOf(m_onlyLeafs).booleanValue();
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
    @Override
    public void setConfiguration(String configuration) {

        // we have to validate later, since we do not have any cms object here
        m_category = "";
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
            int onlyLeafsIndex = configuration.indexOf(CONFIGURATION_ONLYLEAFS);
            if (onlyLeafsIndex != -1) {
                // only leafs is given
                String onlyLeafs = configuration.substring(onlyLeafsIndex + CONFIGURATION_ONLYLEAFS.length() + 1);
                if (onlyLeafs.indexOf('|') != -1) {
                    // cut eventual following configuration values
                    onlyLeafs = onlyLeafs.substring(0, onlyLeafs.indexOf('|'));
                }
                m_onlyLeafs = onlyLeafs;
            }
            int parentSelectionIndex = configuration.indexOf(CONFIGURATION_PARENTSELECTION);
            if (parentSelectionIndex != -1) {
                // parent selection is given
                m_parentSelection = true;
            }
            int propertyIndex = configuration.indexOf(CONFIGURATION_PROPERTY);
            if (propertyIndex != -1) {
                // property is given
                String property = configuration.substring(propertyIndex + CONFIGURATION_PROPERTY.length() + 1);
                if (property.indexOf('|') != -1) {
                    // cut eventual following configuration values
                    property = property.substring(0, property.indexOf('|'));
                }
                m_property = property;
            }
            int selectionTypeIndex = configuration.indexOf(CONFIGURATION_SELECTIONTYPE);
            if (selectionTypeIndex != -1) {
                String selectionType = configuration.substring(
                    selectionTypeIndex + CONFIGURATION_SELECTIONTYPE.length() + 1);
                if (selectionType.indexOf('|') != -1) {
                    // cut eventual following configuration values
                    selectionType = selectionType.substring(0, selectionType.indexOf('|'));
                }
                m_selectiontype = selectionType;
            }
        }
        super.setConfiguration(configuration);
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#setEditorValue(org.opencms.file.CmsObject, java.util.Map, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    public void setEditorValue(
        CmsObject cms,
        Map<String, String[]> formParameters,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        super.setEditorValue(cms, formParameters, widgetDialog, param);
        String id = param.getStringValue(cms);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(id)) {
            return;
        }
        try {
            CmsCategory cat = CmsCategoryService.getInstance().getCategory(cms, cms.readResource(new CmsUUID(id)));
            String referencePath = null;
            try {
                referencePath = cms.getSitePath(getResource(cms, param));
            } catch (Exception e) {
                // ignore, this can happen if a new resource is edited using direct edit
            }
            if (cat.getPath().startsWith(getStartingCategory(cms, referencePath))) {
                param.setStringValue(cms, cat.getRootPath());
            } else {
                param.setStringValue(cms, "");
            }
        } catch (CmsException e) {
            // invalid value
            param.setStringValue(cms, "");
        }
    }

    /**
     * Generates html code for the category selection.<p>
     *
     * @param baseId the widget id
     * @param level the category deep level
     * @param options the list of {@link CmsSelectWidgetOption} objects
     * @param selected the selected option
     * @param hasError if to display error message
     * @param last if it is the last level
     *
     * @return html code
     */
    protected String buildSelectBox(
        String baseId,
        int level,
        List<CmsSelectWidgetOption> options,
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

        Iterator<CmsSelectWidgetOption> i = options.iterator();
        while (i.hasNext()) {
            CmsSelectWidgetOption option = i.next();
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

    /**
     * Returns the default locale in the content of the given resource.<p>
     *
     * @param cms the cms context
     * @param resource the resource path to get the default locale for
     *
     * @return the default locale of the resource
     */
    protected Locale getDefaultLocale(CmsObject cms, String resource) {

        Locale locale = OpenCms.getLocaleManager().getDefaultLocale(cms, resource);
        if (locale == null) {
            List<Locale> locales = OpenCms.getLocaleManager().getAvailableLocales();
            if (locales.size() > 0) {
                locale = locales.get(0);
            } else {
                locale = Locale.ENGLISH;
            }
        }
        return locale;
    }

    /**
     * Returns the right resource, depending on the locale.<p>
     *
     * @param cms the cms context
     * @param param the widget parameter
     *
     * @return the resource to get/set the categories for
     */
    protected CmsResource getResource(CmsObject cms, I_CmsWidgetParameter param) {

        I_CmsXmlContentValue value = (I_CmsXmlContentValue)param;
        CmsFile file = value.getDocument().getFile();
        String resourceName = cms.getSitePath(file);
        if (CmsWorkplace.isTemporaryFile(file)) {
            StringBuffer result = new StringBuffer(resourceName.length() + 2);
            result.append(CmsResource.getFolderPath(resourceName));
            result.append(CmsResource.getName(resourceName).substring(1));
            resourceName = result.toString();
        }
        try {
            List<CmsResource> listsib = cms.readSiblings(resourceName, CmsResourceFilter.ALL);
            for (int i = 0; i < listsib.size(); i++) {
                CmsResource resource = listsib.get(i);
                // get the default locale of the resource
                Locale locale = getDefaultLocale(cms, cms.getSitePath(resource));
                if (locale.equals(value.getLocale())) {
                    // get the property for the right locale
                    return resource;
                }
            }
        } catch (CmsVfsResourceNotFoundException e) {
            // may hapen if editing a new resource
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return file;
    }
}
