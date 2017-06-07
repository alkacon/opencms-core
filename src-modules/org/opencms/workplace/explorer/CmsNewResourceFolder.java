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

package org.opencms.workplace.explorer;

import org.opencms.configuration.CmsDefaultUserSettings;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUriSplitter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.commons.CmsPropertyAdvanced;
import org.opencms.workplace.list.A_CmsListResourceTypeDialog;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.I_CmsListItemComparator;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * The new resource folder dialog handles the creation of a folder.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/newresource_folder.jsp
 * </ul>
 * <p>
 *
 * Displays a list with resource types to choose one for the index page.<p>
 *
 * @since 6.7.1
 */
public class CmsNewResourceFolder extends A_CmsListResourceTypeDialog {

    /** Default list of available resource types for the index page. */
    public static final String DEFAULT_AVAILABLE = "none|xmlpage";

    /** The marker for the default selected resource type. */
    public static final String DEFAULT_MARKER = "*";

    /** The id to use for the entry in the list, for which no index page should be created. */
    public static final String ID_NO_INDEX_PAGE = "__none";

    /** The name of the entry to take if no index page should be generated. */
    public static final String NAME_NO_INDEX_PAGE = "none";

    /** Request parameter name for the current folder name. */
    public static final String PARAM_CURRENTFOLDER = "currentfolder";

    /** Request parameter name for the index page resource type. */
    public static final String PARAM_INDEX_PAGE_TYPE = "indexpagetype";

    /** The name of the property where to find possible restypes for the index page. */
    public static final String PROPERTY_RESTYPES_INDEXPAGE = CmsResourceTypeFolder.CONFIGURATION_INDEX_PAGE_TYPE;

    /** Item comparator to ensure that special types go first. */
    private static final I_CmsListItemComparator LIST_ITEM_COMPARATOR = new I_CmsListItemComparator() {

        /**
         * @see org.opencms.workplace.list.I_CmsListItemComparator#getComparator(java.lang.String, java.util.Locale)
         */
        public Comparator<CmsListItem> getComparator(final String columnId, final Locale locale) {

            final Collator collator = Collator.getInstance(locale);

            return new Comparator<CmsListItem>() {

                /**
                 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
                 */
                @SuppressWarnings({"rawtypes", "unchecked"})
                public int compare(CmsListItem o1, CmsListItem o2) {

                    if ((o1 == o2)) {
                        return 0;
                    }
                    CmsListItem li1 = o1;
                    CmsListItem li2 = o2;

                    String id1 = li1.getId();
                    String id2 = li2.getId();
                    if (id1.equals(id2)) {
                        return 0;
                    } else if (id1.equals(ID_NO_INDEX_PAGE)) {
                        return -1;
                    } else if (id2.equals(ID_NO_INDEX_PAGE)) {
                        return 1;
                    }

                    Comparable c1 = (Comparable)li1.get(columnId);
                    Comparable c2 = (Comparable)li2.get(columnId);
                    if ((c1 instanceof String) && (c2 instanceof String)) {
                        return collator.compare(c1, c2);
                    } else if (c1 != null) {
                        if (c2 == null) {
                            return 1;
                        }
                        return c1.compareTo(c2);
                    } else if (c2 != null) {
                        return -1;
                    }
                    return 0;
                }
            };
        }

    };

    /** Parameter which contains the current folder. */
    private String m_paramCurrentFolder;

    /** Parameter to define if the edit property dialog should be shown. */
    private String m_paramNewResourceEditProps;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsNewResourceFolder(CmsJspActionElement jsp) {

        super(
            jsp,
            A_CmsListResourceTypeDialog.LIST_ID,
            Messages.get().container(Messages.GUI_NEWFOLDER_SELECT_INDEX_TYPE_0),
            A_CmsListResourceTypeDialog.LIST_COLUMN_NAME,
            CmsListOrderEnum.ORDER_ASCENDING,
            null);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsNewResourceFolder(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Creates the folder using the specified resource name.<p>
     *
     * @return if the resource was created successfully
     *
     * @throws JspException if inclusion of error dialog fails
     */
    public boolean actionCreateResource() throws JspException {

        try {
            // calculate the new resource Title property value
            String title = CmsNewResource.computeNewTitleProperty(getParamResource());

            // get the full resource name
            String fullResourceName = computeFullResourceName();

            // create the Title and Navigation properties if configured
            List<CmsProperty> properties = CmsNewResource.createResourceProperties(
                getCms(),
                fullResourceName,
                CmsResourceTypeFolder.getStaticTypeName(),
                title);

            // create the folder
            getCms().createResource(
                fullResourceName,
                OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.getStaticTypeName()).getTypeId(),
                null,
                properties);
            setParamResource(fullResourceName);

            return true;
        } catch (Throwable e) {

            // error creating folder, show error dialog
            setParamMessage(Messages.get().getBundle(getLocale()).key(Messages.ERR_CREATE_FOLDER_0));
            includeErrorpage(this, e);
        }

        return false;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#actionDialog()
     */
    @Override
    public void actionDialog() throws JspException, ServletException, IOException {

        if (getAction() == ACTION_CONTINUE) {
            if (actionCreateResource()) {
                actionEditProperties();
                return;
            }
        }

        super.actionDialog();
    }

    /**
     * Forwards to the property dialog if the resourceeditprops parameter is true.<p>
     *
     * If the parameter is not true, the dialog will be closed.<p>
     *
     * @throws IOException if forwarding to the property dialog fails
     * @throws ServletException if forwarding to the property dialog fails
     * @throws JspException if an inclusion fails
     */
    public void actionEditProperties() throws IOException, JspException, ServletException {

        boolean editProps = Boolean.valueOf(getParamNewResourceEditProps()).booleanValue();

        String indexPageType = getParamSelectedType();
        boolean createIndex = (CmsStringUtil.isNotEmptyOrWhitespaceOnly(indexPageType))
            && (!indexPageType.equals(ID_NO_INDEX_PAGE));
        if (editProps) {

            // edit properties of folder, forward to property dialog
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put(PARAM_RESOURCE, new String[] {getParamResource()});
            if (createIndex) {

                // set dialogmode to wizard - create index page to indicate the creation of the index page
                params.put(
                    CmsPropertyAdvanced.PARAM_DIALOGMODE,
                    new String[] {CmsPropertyAdvanced.MODE_WIZARD_CREATEINDEX});
                params.put(PARAM_INDEX_PAGE_TYPE, new String[] {indexPageType});
            } else {

                // set dialogmode to wizard
                params.put(CmsPropertyAdvanced.PARAM_DIALOGMODE, new String[] {CmsPropertyAdvanced.MODE_WIZARD});
            }
            sendForward(CmsPropertyAdvanced.URI_PROPERTY_DIALOG_HANDLER, params);
        } else if (createIndex) {

            // create an index file in the new folder, redirect to new xmlpage dialog
            String newFolder = getParamResource();
            if (!newFolder.endsWith("/")) {
                newFolder += "/";
            }

            // set the current explorer resource to the new created folder
            getSettings().setExplorerResource(newFolder, getCms());
            String newUri = PATH_DIALOGS
                + OpenCms.getWorkplaceManager().getExplorerTypeSetting(indexPageType).getNewResourceUri();
            CmsUriSplitter splitter = new CmsUriSplitter(newUri);
            Map<String, String[]> params = CmsRequestUtil.createParameterMap(splitter.getQuery());
            params.put(
                CmsPropertyAdvanced.PARAM_DIALOGMODE,
                new String[] {CmsPropertyAdvanced.MODE_WIZARD_CREATEINDEX});
            params.put(PARAM_ACTION, new String[] {CmsNewResource.DIALOG_NEWFORM});
            sendForward(splitter.getPrefix(), params);
        } else {

            // edit properties and create index file not checked, close the dialog and update tree
            List<String> folderList = new ArrayList<String>(1);
            folderList.add(CmsResource.getParentFolder(getParamResource()));
            getJsp().getRequest().setAttribute(REQUEST_ATTRIBUTE_RELOADTREE, folderList);
            actionCloseDialog();
        }
    }

    /**
     * Returns the current folder set by the http request.<p>
     *
     * If the request parameter value is null/empty then returns the default computed folder.<p>
     *
     * @return the current folder set by the request param or the computed current folder
     */
    public String getParamCurrentFolder() {

        if (CmsStringUtil.isEmpty(m_paramCurrentFolder)) {
            return computeCurrentFolder();
        }

        return m_paramCurrentFolder;
    }

    /**
     * Returns the paramNewResourceEditProps.<p>
     *
     * @return the paramNewResourceEditProps
     */
    public String getParamNewResourceEditProps() {

        return m_paramNewResourceEditProps;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceTypeDialog#getParamSelectedType()
     */
    @Override
    public String getParamSelectedType() {

        String item = super.getParamSelectedType();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(item)) {

            boolean existNoIndex = false;
            List<String> availableResTypes = getAvailableResTypes();

            // search for the default in the configuration
            Iterator<String> iter = availableResTypes.iterator();
            while (iter.hasNext()) {

                String entry = iter.next();

                // check if the no index page is available
                if (entry.equals(NAME_NO_INDEX_PAGE)) {
                    existNoIndex = true;
                }

                // check if the entry is the default
                if (entry.startsWith(DEFAULT_MARKER)) {

                    // strip leading asterisk
                    return entry.substring(1, entry.length());
                }
            }

            // if the no index page entry exists, use this as default
            if (existNoIndex) {
                return ID_NO_INDEX_PAGE;
            }

            // now use the first entry as the default
            if (availableResTypes.size() > 0) {
                return availableResTypes.get(0);
            }

            return null;
        }

        return item;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#paramsAsHidden()
     */
    @Override
    public String paramsAsHidden() {

        List<String> exclude = new ArrayList<String>();
        exclude.add(CmsNewResource.PARAM_NEWRESOURCEEDITPROPS);
        exclude.add(PARAM_RESOURCE);

        return paramsAsHidden(exclude);
    }

    /**
     * Sets the current folder.<p>
     *
     * @param paramCurrentFolder the current folder to set
     */
    public void setParamCurrentFolder(String paramCurrentFolder) {

        m_paramCurrentFolder = paramCurrentFolder;
    }

    /**
     * Sets the paramNewResourceEditProps.<p>
     *
     * @param paramNewResourceEditProps the paramNewResourceEditProps to set
     */
    public void setParamNewResourceEditProps(String paramNewResourceEditProps) {

        m_paramNewResourceEditProps = paramNewResourceEditProps;
    }

    /**
     * Appends the full path to the new resource name given in the resource parameter.<p>
     *
     * @return the full path of the new resource
     */
    protected String computeFullResourceName() {

        // return the full resource name
        // get the current folder
        String currentFolder = getParamCurrentFolder();
        if (CmsStringUtil.isEmpty(currentFolder)) {
            currentFolder = computeCurrentFolder();
        }

        String translatedFoldername = OpenCms.getResourceManager().getFolderTranslator().translateResource(
            getParamResource());

        return currentFolder + translatedFoldername;
    }

    /**
     * Returns the html code to add directly before the list inside the form element.<p>
     *
     * @return the html code to add directly before the list inside the form element
     */
    @Override
    protected String customHtmlBeforeList() {

        StringBuffer result = new StringBuffer();

        result.append(dialogBlockStart(key(Messages.GUI_NEWFOLDER_OPTIONS_0)));
        result.append("<table border=\"0\" width=\"100%\">\n");
        result.append("\t<tr>\n");
        result.append("\t\t<td style=\"white-space: nowrap;\" unselectable=\"on\">");
        result.append(key(Messages.GUI_RESOURCE_NAME_0));
        result.append("</td>\n");
        result.append("\t\t<td class=\"maxwidth\"><input name=\"");
        result.append(PARAM_RESOURCE);

        result.append("\" id=\"newresfield\" type=\"text\" value=\"");
        String resource = getParamResource();
        if (resource == null) {
            resource = "";
        }
        result.append(resource);
        result.append(
            "\" class=\"maxwidth\" onkeyup=\"checkValue();\" onchange=\"checkValue();\" onpaste=\"setTimeout(checkValue,4);\" ></td>\n");
        result.append("\t</tr>\n");
        result.append("\t<tr>\n");
        result.append("\t\t<td>&nbsp;</td>\n");
        result.append("\t\t<td style=\"white-space: nowrap;\" unselectable=\"on\" class=\"maxwidth\">\n");
        result.append("\t\t\t<input name=\"");
        result.append(CmsNewResource.PARAM_NEWRESOURCEEDITPROPS);
        result.append("\" id=\"newresedit\" type=\"checkbox\" value=\"true\"");
        result.append(" onclick=\"toggleButtonLabel();\"");
        if (Boolean.valueOf(getParamNewResourceEditProps()).booleanValue()) {
            result.append(" checked");
        }
        result.append(">&nbsp;");
        result.append(key(Messages.GUI_NEWFOLDER_EDITPROPERTIES_0));
        result.append("\n");
        result.append("\t\t</td>\n");
        result.append("\t</tr>\n");
        result.append("</table>\n");
        result.append(dialogBlockEnd());
        result.append("<br/>\n");
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#customHtmlEnd()
     */
    @Override
    protected String customHtmlEnd() {

        StringBuffer result = new StringBuffer(256);
        result.append(super.customHtmlEnd());

        // execute the javascript for de-/activating the continue button
        result.append("<script type='text/javascript'>\n");
        result.append("\ttoggleButtonLabel();\n");
        result.append("\tcheckValue();\n");
        result.append("</script>");

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#customHtmlStart()
     */
    @Override
    protected String customHtmlStart() {

        StringBuffer result = new StringBuffer(256);
        result.append(super.customHtmlStart());
        result.append("<script type='text/javascript'>\n");

        result.append("function toggleButtonLabel() {\n");
        result.append("\tvar theCheckBox = document.getElementById('newresedit');\n");
        result.append("\tvar theButton = document.getElementById('nextButton');\n");
        result.append("\tif (theCheckBox.checked == true) {\n");
        result.append("\t\ttheButton.value = '" + key(Messages.GUI_BUTTON_CONTINUE_0) + "';\n");
        result.append("\t} else {\n");
        result.append("\t\ttheButton.value = '" + key(Messages.GUI_BUTTON_ENDWIZARD_0) + "';\n");
        result.append("\t}\n");
        result.append("}\n\n");

        result.append("function checkValue() {\n");
        result.append("\tvar resName = document.getElementById(\"newresfield\").value;\n");
        result.append("\tvar theButton = document.getElementById(\"nextButton\");\n");
        result.append("\tif (resName.length == 0) {\n");
        result.append("\t\tif (theButton.disabled == false) {\n");
        result.append("\t\t\ttheButton.disabled = true;\n");
        result.append("\t\t}\n");
        result.append("\t} else {\n");
        result.append("\t\tif (theButton.disabled == true) {\n");
        result.append("\t\t\ttheButton.disabled = false;\n");
        result.append("\t\t}\n");
        result.append("\t}\n");
        result.append("}\n");

        result.append("</script>");

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsDialog#dialogButtonsHtml(java.lang.StringBuffer, int, java.lang.String)
     */
    @Override
    protected void dialogButtonsHtml(StringBuffer result, int button, String attribute) {

        switch (button) {
            case BUTTON_CONTINUE:
                result.append("<input name=\"set\" type=\"button\" value=\"");
                result.append(key(Messages.GUI_BUTTON_CONTINUE_0) + "\"");
                if (attribute.toLowerCase().indexOf("onclick") == -1) {
                    result.append(
                        " onclick=\"submitAction('" + DIALOG_CONTINUE + "', form, '" + getListId() + "-form');\"");
                }
                result.append(" class=\"dialogbutton\"");
                result.append(" id=\"nextButton\"");
                result.append(" disabled=\"disabled\"");
                result.append(attribute);
                result.append(">\n");
                break;
            default:
                super.dialogButtonsHtml(result, button, attribute);
        }
    }

    /**
     * Returns a list with all available resource types for the index page.<p>
     *
     * The information is first read from the property "restypes.indexpage".
     * If there nothing could be found, the global settings from the resource
     * type folder is taken. Only if there is nothing configured, the default
     * (No index page and xmlpage) will be taken.<p>
     *
     * @return a list with all available resource types for the index page
     */
    protected List<String> getAvailableResTypes() {

        String availableResTypes = null;

        // check for presence of property limiting the new resource types to create
        try {
            String propResTypes = getCms().readPropertyObject(
                getParamCurrentFolder(),
                PROPERTY_RESTYPES_INDEXPAGE,
                true).getValue();

            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(propResTypes)) {
                availableResTypes = propResTypes;
            }
        } catch (CmsException e) {
            // ignore this exception, this is a minor issue
        }

        // use global settings from resource type folder
        if (availableResTypes == null) {
            try {
                I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(
                    CmsResourceTypeFolder.getStaticTypeName());

                String folderResTypes = ((CmsResourceTypeFolder)resType).getIndexPageTypes();
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(folderResTypes)) {
                    availableResTypes = folderResTypes;
                }

            } catch (CmsException ex) {
                // ignore this exception, this is a minor issue
            }
        }

        // use default
        if (availableResTypes == null) {
            availableResTypes = DEFAULT_AVAILABLE;
        }

        // create list iterator of given available resource types
        return CmsStringUtil.splitAsList(availableResTypes, CmsProperty.VALUE_LIST_DELIMITER, true);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() {

        List<CmsListItem> ret = new ArrayList<CmsListItem>();

        List<String> newResTypes = getAvailableResTypes();
        Iterator<String> k = newResTypes.iterator();
        while (k.hasNext()) {
            String resType = k.next();

            // strip the leading asterisk for the default
            if (resType.startsWith(DEFAULT_MARKER)) {
                resType = resType.substring(1, resType.length());
            }

            // add the no index page entry
            if (resType.equals(NAME_NO_INDEX_PAGE)) {
                CmsListItem item = getList().newItem(ID_NO_INDEX_PAGE);
                item.set(LIST_COLUMN_NAME, key(Messages.GUI_NEWFOLDER_LIST_NO_INDEX_0));
                ret.add(item);
                continue;
            }

            // get settings for resource type
            CmsExplorerTypeSettings set = OpenCms.getWorkplaceManager().getExplorerTypeSetting(resType);
            if (set != null) {

                // add found setting to list
                CmsListItem item = getList().newItem(set.getName());
                item.set(LIST_COLUMN_NAME, key(set.getKey()));
                item.set(
                    LIST_COLUMN_ICON,
                    "<img src=\""
                        + getSkinUri()
                        + CmsWorkplace.RES_PATH_FILETYPES
                        + set.getIcon()
                        + "\" style=\"width: 16px; height: 16px;\" />");
                ret.add(item);
            }
        }

        return ret;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
        if (DIALOG_CONTINUE.equals(getParamAction())) {
            setAction(ACTION_CONTINUE);
        } else if (CmsNewResource.DIALOG_NEWFORM.equals(getParamAction())) {
            CmsDefaultUserSettings userSettings = OpenCms.getWorkplaceManager().getDefaultUserSettings();
            Boolean editPropsChecked = userSettings.getNewFolderEditProperties();
            setParamNewResourceEditProps(editPropsChecked.toString());
        }

        // set title
        String title = null;
        CmsExplorerTypeSettings set = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
            CmsResourceTypeFolder.getStaticTypeName());
        if ((set != null) && (CmsStringUtil.isNotEmptyOrWhitespaceOnly(set.getTitleKey()))) {
            title = getMessages().key(set.getTitleKey(), true);
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
            title = key(Messages.GUI_NEWRESOURCE_FOLDER_0);
        }
        setParamTitle(title);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceTypeDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        super.setColumns(metadata);

        CmsListColumnDefinition def = metadata.getColumnDefinition(LIST_COLUMN_NAME);
        def.setListItemComparator(LIST_ITEM_COMPARATOR);
    }
}
