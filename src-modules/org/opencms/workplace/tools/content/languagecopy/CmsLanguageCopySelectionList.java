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

package org.opencms.workplace.tools.content.languagecopy;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.tools.CmsToolDialog;
import org.opencms.workplace.tools.CmsToolManager;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * A list that displays resources and the existance of their language nodes.
 * <p>
 * 
 * @since 7.5.1
 * 
 */
public class CmsLanguageCopySelectionList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_NONE = "an";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "lcic";

    /** list action id constant. */
    public static final String LIST_COLUMN_ID = "li.id.languagecopyselection";

    /** list column id constant. */
    public static final String LIST_COLUMN_PATH = "lcp";

    /** list column id constant. */
    public static final String LIST_COLUMN_PREFIX_PROPERTY = "cnp-";

    /** list column id constant. */
    public static final String LIST_COLUMN_RESOURCETYPE = "lcrt";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_FULLPATH = "df";

    /** Multi action for copy. */
    public static final String LIST_MACTION_COPY = "mac";

    /** The request parameter for the paths to work on. */
    public static final String PARAM_PATHS = "paths";

    /** The request parameter for the paths to work on. */
    public static final String PARAM_SIBLINGS = "siblings";

    /** The request parameter for the source language. */
    public static final String PARAM_SOURCE_LANGUAGE = "sourcelanguage";

    /** The request parameter for the target language. */
    public static final String PARAM_TARGET_LANGUAGE = "targetlanguage";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLanguageCopySelectionList.class);

    /** The source language. */
    private String m_paramSourcelanguage;

    /** The target language. */
    private String m_paramTargetlanguage;

    /** The paths. */
    private String[] m_paths;

    /**
     * Public constructor.
     * <p>
     * 
     * @param jsp an initialized JSP action element
     * 
     * @throws CmsException if something goes wrong.
     * 
     * @throws FileNotFoundException if something goes wrong.
     */
    public CmsLanguageCopySelectionList(final CmsJspActionElement jsp)
    throws FileNotFoundException, CmsException {

        this(jsp, LIST_COLUMN_ID, Messages.get().container(Messages.GUI_LIST_LANGUAGECOPY_NAME_0));
    }

    /**
     * Public constructor.
     * <p>
     * 
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the list name
     * 
     * @throws CmsException if something goes wrong.
     * 
     * @throws FileNotFoundException if something goes wrong.
     */
    public CmsLanguageCopySelectionList(
        final CmsJspActionElement jsp,
        final String listId,
        final CmsMessageContainer listName)
    throws FileNotFoundException, CmsException {

        this(jsp, listId, listName, LIST_COLUMN_ID, CmsListOrderEnum.ORDER_ASCENDING, null);
    }

    /**
     * Public constructor.
     * <p>
     * 
     * @param jsp an initialized JSP action element
     * @param listId the id of the displayed list
     * @param listName the name of the list
     * @param sortedColId the a priory sorted column
     * @param sortOrder the order of the sorted column
     * @param searchableColId the column to search into
     * 
     * @throws CmsException if something goes wrong.
     * @throws FileNotFoundException if something goes wrong.
     */
    @SuppressWarnings("unused")
    public CmsLanguageCopySelectionList(
        final CmsJspActionElement jsp,
        final String listId,
        final CmsMessageContainer listName,
        final String sortedColId,
        final CmsListOrderEnum sortOrder,
        final String searchableColId)
    throws FileNotFoundException, CmsException {

        super(jsp, listId, listName, sortedColId, sortOrder, searchableColId);
    }

    /**
     * Public constructor with JSP variables.
     * <p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     * 
     * @throws CmsException if something goes wrong.
     * @throws FileNotFoundException if something goes wrong.
     */
    public CmsLanguageCopySelectionList(
        final PageContext context,
        final HttpServletRequest req,
        final HttpServletResponse res)
    throws FileNotFoundException, CmsException {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() throws IOException, ServletException, CmsRuntimeException {

        if (getParamListAction().equals(LIST_MACTION_COPY)) {

            // create absolute RFS path and store it in dialog object
            Map<String, String[]> params = new HashMap<String, String[]>();
            List<CmsListItem> items = this.getSelectedItems();
            List<String> paths = new LinkedList<String>();
            for (CmsListItem item : items) {
                paths.add(String.valueOf(item.get(LIST_COLUMN_PATH)));
            }
            params.put(
                CmsLanguageCopyFolderAndLanguageSelectDialog.PARAM_COPYRESOURCES,
                paths.toArray(new String[paths.size()]));
            // the source language
            params.put(PARAM_SOURCE_LANGUAGE, new String[] {getParamSourcelanguage()});
            // the target language
            params.put(PARAM_TARGET_LANGUAGE, new String[] {getParamTargetlanguage()});
            // set style to display report in correct layout
            params.put(PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});
            // set close link to get back to overview after finishing the import
            params.put(PARAM_CLOSELINK, new String[] {CmsToolManager.linkForToolPath(getJsp(), "/contenttools")});
            // redirect to the report output JSP
            getToolManager().jspForwardPage(
                this,
                CmsWorkplace.PATH_WORKPLACE + "admin/contenttools/languagecopy/report.jsp",
                params);
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @SuppressWarnings("unused")
    @Override
    public void executeListSingleActions() throws IOException, ServletException, CmsRuntimeException {

        // do nothing
    }

    /**
     * @return the resourcses to copy
     */
    public String[] getCopyResources() {

        List<CmsListItem> items = this.getSelectedItems();
        String paths = "";
        boolean initial = true;
        for (CmsListItem item : items) {
            if (!initial) {
                paths.concat(",");
            }
            paths.concat(String.valueOf(item.get(LIST_COLUMN_PATH)));
            initial = false;
        }
        return CmsStringUtil.splitAsArray(paths, ",");
    }

    /**
     * @return the paths
     */
    public String getParamPaths() {

        return CmsStringUtil.arrayAsString(m_paths, ",");
    }

    /**
    * @return the source language
    */
    public String getParamSourcelanguage() {

        return m_paramSourcelanguage;
    }

    /**
     * @return the target language
     */
    public String getParamTargetlanguage() {

        return m_paramTargetlanguage;
    }

    /**
     * @param paths
     *            the paths to set
     */
    public void setParamPaths(final String paths) {

        this.m_paths = CmsStringUtil.splitAsArray(paths, ",");
    }

    /**
     * @param sourceLanguage
     *            the source language
     */
    public void setParamSourcelanguage(String sourceLanguage) {

        m_paramSourcelanguage = sourceLanguage;
    }

    /**
     * @param targetLanguage
     *            the target language
     */
    public void setParamTargetlanguage(String targetLanguage) {

        m_paramTargetlanguage = targetLanguage;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(final String detailId) {

        // do nothing
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() {

        List<CmsListItem> result = new ArrayList<CmsListItem>();
        // get content
        CmsListItem item;
        int idCounter = 0;
        for (CmsResource resource : this.getResources()) {
            item = getList().newItem(resource.getRootPath());
            this.fillItem(resource, item, idCounter);
            idCounter++;
            result.add(item);
        }
        return result;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings,
     *      javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(final CmsWorkplaceSettings settings, final HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(final CmsListMetadata metadata) {

        // enforce re-invocation of this method because columns are varying and must not be cached:
        metadata.setVolatile(true);

        // add column for icon
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        iconCol.setName(Messages.get().container(Messages.GUI_LIST_LANGUAGECOPY_COL_ICON_NAME_0));
        iconCol.setHelpText(Messages.get().container(Messages.GUI_LIST_LANGUAGECOPY_COL_ICON_HELP_0));
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        iconCol.setWidth("16");
        iconCol.setSorteable(false);
        metadata.addColumn(iconCol);
        iconCol.setPrintable(true);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_PATH);
        nameCol.setName(Messages.get().container(Messages.GUI_LIST_LANGUAGECOPY_COL_PATH_NAME_0));
        nameCol.setHelpText(Messages.get().container(Messages.GUI_LIST_LANGUAGECOPY_COL_PATH_HELP_0));
        nameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        nameCol.setSorteable(true);
        metadata.addColumn(nameCol);
        nameCol.setPrintable(true);

        // add column for resource type
        CmsListColumnDefinition typeCol = new CmsListColumnDefinition(LIST_COLUMN_RESOURCETYPE);
        typeCol.setName(Messages.get().container(Messages.GUI_LIST_LANGUAGECOPY_COL_RESOURCETYPE_NAME_0));
        typeCol.setHelpText(Messages.get().container(Messages.GUI_LIST_LANGUAGECOPY_COL_RESOURCETYPE_HELP_0));
        typeCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        typeCol.setSorteable(true);
        metadata.addColumn(typeCol);
        typeCol.setPrintable(true);

        // add columns for languages:
        List<Locale> sysLocales = OpenCms.getLocaleManager().getAvailableLocales();
        CmsListColumnDefinition langCol;
        for (Locale locale : sysLocales) {
            langCol = new CmsListColumnDefinition(locale.toString());
            langCol.setName(Messages.get().container(
                Messages.GUI_LIST_LANGUAGECOPY_COL_LANGUAGE_NAME_1,
                new Object[] {locale.toString()}));
            langCol.setHelpText(Messages.get().container(Messages.GUI_LIST_LANGUAGECOPY_COL_LANGUAGE_HELP_0));
            langCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
            langCol.setSorteable(false);
            metadata.addColumn(langCol);
            langCol.setPrintable(true);
        }

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(final CmsListMetadata metadata) {

        // nothing to do here
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(final CmsListMetadata metadata) {

        // add copy multi action
        CmsListMultiAction deleteMultiAction = new CmsListMultiAction(LIST_MACTION_COPY);
        deleteMultiAction.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_MACTION_COPY_NAME_0));
        deleteMultiAction.setHelpText(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_MACTION_COPY_HELP_0));
        deleteMultiAction.setIconPath(ICON_MULTI_ADD);
        metadata.addMultiAction(deleteMultiAction);
    }

    /**
     * Fills a single item.
     * <p>
     * 
     * @param resource the corresponding resource.
     * @param item the item to fill.
     * @param id used for the ID column.
     */
    private void fillItem(final CmsResource resource, final CmsListItem item, final int id) {

        CmsObject cms = this.getCms();
        CmsXmlContent xmlContent;

        I_CmsResourceType type;
        String iconPath;

        // fill path column:
        String sitePath = cms.getSitePath(resource);
        item.set(LIST_COLUMN_PATH, sitePath);

        // fill language node existence column:
        item.set(LIST_COLUMN_PATH, sitePath);
        boolean languageNodeExists = false;
        String languageNodeHtml;

        List<Locale> sysLocales = OpenCms.getLocaleManager().getAvailableLocales();
        try {
            xmlContent = CmsXmlContentFactory.unmarshal(cms, cms.readFile(resource));
            for (Locale locale : sysLocales) {
                languageNodeExists = xmlContent.hasLocale(locale);
                if (languageNodeExists) {
                    languageNodeHtml = "<input type=\"checkbox\" checked=\"checked\" disabled=\"disabled\"/>";
                } else {
                    languageNodeHtml = "<input type=\"checkbox\" disabled=\"disabled\"/>";
                }
                item.set(locale.toString(), languageNodeHtml);

            }
        } catch (Throwable e1) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_ERR_LANGUAGECOPY_DETERMINE_LANGUAGE_NODE_1), e1);
            languageNodeHtml = "n/a";
            for (Locale locale : sysLocales) {
                item.set(locale.toString(), languageNodeHtml);
            }
        }

        // type column:
        type = OpenCms.getResourceManager().getResourceType(resource);
        item.set(LIST_COLUMN_RESOURCETYPE, type.getTypeName());

        // icon column with title property for tooltip:
        String title = "";
        try {
            CmsProperty titleProperty = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_TITLE, true);
            title = titleProperty.getValue();
        } catch (CmsException e) {
            LOG.warn(Messages.get().getBundle().key(Messages.LOG_WARN_LANGUAGECOPY_READPROP_1), e);
        }

        iconPath = getSkinUri()
            + CmsWorkplace.RES_PATH_FILETYPES
            + OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName()).getIcon();
        String iconImage;
        iconImage = "<img src=\"" + iconPath + "\" alt=\"" + type.getTypeName() + "\" title=\"" + title + "\" />";
        item.set(LIST_COLUMN_ICON, iconImage);
    }

    /**
     * Reads the resources available for processing based on the path parameters.<p>
     * 
     * @return the resources available for processing based on the path parameters.
     */
    private List<CmsResource> getResources() {

        List<CmsResource> result = new LinkedList<CmsResource>();
        CmsObject cms = this.getCms();
        CmsResourceFilter filter = CmsResourceFilter.ALL;
        try {
            for (String path : this.m_paths) {
                List<CmsResource> resources = cms.readResources(path, filter, true);
                // filter out any resource that is no XML content:
                for (CmsResource resource : resources) {
                    if (resource.isFile()) {
                        if (CmsResourceTypeXmlContent.isXmlContent(resource)) {
                            result.add(resource);
                        } else if (CmsResourceTypeXmlPage.isXmlPage(resource)) {
                            result.add(resource);
                        }
                    }
                }
            }
        } catch (CmsException e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_ERR_LANGUAGECOPY_READRESOURCES_0), e);
            result = Collections.emptyList();
        }

        return result;
    }
}
