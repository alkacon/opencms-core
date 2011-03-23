/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsHistoryList.java,v $
 * Date   : $Date: 2011/03/23 14:50:46 $
 * Version: $Revision: 1.21 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.commons;

import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.history.CmsHistoryProject;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPrincipal;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListItemSelectionAction;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListRadioMultiAction;
import org.opencms.workplace.list.CmsListResourceIconAction;
import org.opencms.workplace.list.I_CmsListFormatter;
import org.opencms.workplace.tools.CmsToolDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.apache.commons.logging.Log;

/**
 * Displays the history of a file.<p>
 * 
 * @author Jan Baudisch  
 * @author Armen Markarian 
 * 
 * @version $Revision: 1.21 $ 
 * 
 * @since 6.0.2 
 */
public class CmsHistoryList extends A_CmsListDialog {

    /** 
     * Wrapper class for the version which is either an integer or the string "offline".<p>
     */
    public static class CmsVersionWrapper implements Comparable {

        /** the version. */
        private Integer m_version;

        /** 
         * Constructs a new version wrapper.<p>
         * 
         * @param version the version of the file
         */
        public CmsVersionWrapper(int version) {

            m_version = new Integer(version);
        }

        /**
         * 
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Object o) {

            if (this == o) {
                return 0;
            }
            if (o instanceof CmsVersionWrapper) {
                CmsVersionWrapper version = (CmsVersionWrapper)o;
                Integer v1 = m_version;
                Integer v2 = version.getVersion();
                if (v1.intValue() < 0) {
                    v1 = new Integer(-1 * v1.intValue());
                }
                if (v2.intValue() < 0) {
                    v2 = new Integer(-1 * v2.intValue());
                }
                return ((Comparable)v1).compareTo(v2);
            }
            return 0;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {

            if (this == obj) {
                return true;
            }
            if (obj instanceof CmsVersionWrapper) {
                CmsVersionWrapper version = (CmsVersionWrapper)obj;
                return getVersion().equals(version.getVersion());
            }
            return false;
        }

        /**
         * Returns the version of the file.<p>
         * 
         * @return the version of the file
         */
        public Integer getVersion() {

            return m_version;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            return getVersion().hashCode();
        }

        /**
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return m_version.toString();
        }

    }

    /** list item detail id constant. */
    public static final String GUI_LIST_HISTORY_DETAIL_PROJECT_0 = "lhdp";

    /** List action export. */
    public static final String LIST_ACTION_RESTORE = "ar";

    /** list action id constant. */
    public static final String LIST_ACTION_VIEW = "av";

    /** list column id constant. */
    public static final String LIST_COLUMN_DATE_LAST_MODIFIED = "cm";

    /** list column id constant. */
    public static final String LIST_COLUMN_DATE_PUBLISHED = "cdp";

    /** list column id constant. */
    public static final String LIST_COLUMN_FILE_TYPE = "ct";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_PUBLISH_TAG = "cbt";

    /** list column id constant. */
    public static final String LIST_COLUMN_RESOURCE_PATH = "crp";

    /** List column delete. */
    public static final String LIST_COLUMN_RESTORE = "cr";

    /** list column id constant. */
    public static final String LIST_COLUMN_SEL1 = "cs1";

    /** list column id constant. */
    public static final String LIST_COLUMN_SEL2 = "cs2";

    /** list column id constant. */
    public static final String LIST_COLUMN_SIZE = "cs";

    /** list column id constant. */
    public static final String LIST_COLUMN_STRUCTURE_ID = "csi";

    /** List column export. */
    public static final String LIST_COLUMN_USER = "cu";

    /** list column id constant. */
    public static final String LIST_COLUMN_VERSION = "cv";

    /** List column export. */
    public static final String LIST_COLUMN_VIEW = "cp";

    /** list id constant. */
    public static final String LIST_ID = "him";

    /** list independent action id constant. */
    public static final String LIST_RACTION_SEL1 = "rs1";

    /** list independent action id constant. */
    public static final String LIST_RACTION_SEL2 = "rs2";

    /** parameter for the path of the first resource. */
    public static final String PARAM_ID_1 = "id1";

    /** parameter for the path of the second resource. */
    public static final String PARAM_ID_2 = "id2";

    /** parameter for the version of the first resource. */
    public static final String PARAM_VERSION_1 = "version1";

    /** parameter for the version of the second resource. */
    public static final String PARAM_VERSION_2 = "version2";

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "buttons/";

    /** list multi action id constant. */
    private static final String LIST_MACTION_COMPARE = "mc";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsHistoryList.class);

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsHistoryList(CmsJspActionElement jsp) {

        super(
            jsp,
            LIST_ID,
            Messages.get().container(Messages.GUI_HISTORY_0),
            LIST_COLUMN_VERSION,
            CmsListOrderEnum.ORDER_DESCENDING,
            null);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsHistoryList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Returns the version number from a version parameter.<p>
     * 
     * @param version might be negative for the online version
     * @param locale if the result is for display purposes, the locale has to be <code>!= null</code>
     * 
     * @return the display name
     */
    public static String getDisplayVersion(String version, Locale locale) {

        int ver = Integer.parseInt(version);
        if (ver == CmsHistoryResourceHandler.PROJECT_OFFLINE_VERSION) {
            return Messages.get().getBundle(locale).key(Messages.GUI_PROJECT_OFFLINE_0);
        }
        if (ver < 0) {
            ver *= -1;
            if (locale != null) {
                return Messages.get().getBundle(locale).key(Messages.GUI_PROJECT_ONLINE_1, new Integer(ver));
            }
        }
        return "" + ver;
    }

    /**
     * Returns the link to an historical file.<p>
     * 
     * @param cms the cms context
     * @param structureId the structure id of the file
     * @param version the version number of the file
     * 
     * @return the link to an historical file
     */
    public static String getHistoryLink(CmsObject cms, CmsUUID structureId, String version) {

        String resourcePath;
        CmsResource resource;
        try {
            resource = cms.readResource(structureId, CmsResourceFilter.ALL);
            resourcePath = resource.getRootPath();
        } catch (CmsException e) {
            throw new CmsRuntimeException(e.getMessageContainer(), e);
        }
        StringBuffer link = new StringBuffer();
        link.append(CmsHistoryResourceHandler.HISTORY_HANDLER);
        link.append(resourcePath);
        link.append('?');
        link.append(CmsHistoryResourceHandler.PARAM_VERSION);
        link.append('=');
        link.append(getVersion("" + version));
        return link.toString();
    }

    /**
     * Returns the version number from a version parameter.<p>
     * 
     * @param version might be negative for the online version
     * 
     * @return the positive value
     */
    public static int getVersion(String version) {

        int ver = Integer.parseInt(version);
        return Math.abs(ver);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#actionDialog()
     */
    @Override
    public void actionDialog() throws JspException, ServletException, IOException {

        super.actionDialog();
        // ensure the list is correcly sorted
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(getList().getSortedColumn())) {
            getList().setSortedColumn(LIST_COLUMN_VERSION);
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() throws IOException, ServletException {

        if (getParamListAction().equals(LIST_MACTION_COMPARE)) {
            CmsListItem item1 = getSelectedItems().get(0);
            CmsListItem item2 = getSelectedItems().get(1);
            Map params = new HashMap();
            if (((Comparable)item2.get(LIST_COLUMN_VERSION)).compareTo(item1.get(LIST_COLUMN_VERSION)) > 0) {
                params.put(PARAM_VERSION_1, item1.get(LIST_COLUMN_VERSION));
                params.put(PARAM_VERSION_2, item2.get(LIST_COLUMN_VERSION));
                params.put(PARAM_ID_1, item1.get(LIST_COLUMN_STRUCTURE_ID));
                params.put(PARAM_ID_2, item2.get(LIST_COLUMN_STRUCTURE_ID));
            } else {
                params.put(PARAM_VERSION_1, item2.get(LIST_COLUMN_VERSION));
                params.put(PARAM_VERSION_2, item1.get(LIST_COLUMN_VERSION));
                params.put(PARAM_ID_1, item2.get(LIST_COLUMN_STRUCTURE_ID));
                params.put(PARAM_ID_2, item1.get(LIST_COLUMN_STRUCTURE_ID));
            }
            params.put(PARAM_ACTION, DIALOG_INITIAL);
            params.put(PARAM_STYLE, CmsToolDialog.STYLE_NEW);
            params.put(PARAM_RESOURCE, getParamResource());
            getToolManager().jspForwardTool(this, "/history/comparison", params);
        }
        refreshList();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() throws IOException, ServletException {

        if (getParamListAction().equals(LIST_ACTION_RESTORE)) {
            try {
                performRestoreOperation();
                Map params = new HashMap();
                params.put(PARAM_ACTION, DIALOG_INITIAL);
                getToolManager().jspForwardPage(this, "/system/workplace/views/explorer/explorer_files.jsp", params);
            } catch (CmsException e) {
                LOG.error(e.getMessage(), e);
                throw new CmsRuntimeException(e.getMessageContainer());
            }
        }
        refreshList();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlStart()
     */
    @Override
    protected String defaultActionHtmlStart() {

        return getList().listJs() + dialogContentStart(getParamTitle());
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // get content
        List items = getList().getAllContent();
        Iterator itItems = items.iterator();
        CmsListItem item;
        while (itItems.hasNext()) {
            item = (CmsListItem)itItems.next();
            if (detailId.equals(GUI_LIST_HISTORY_DETAIL_PROJECT_0)) {
                fillDetailProject(item, detailId);
            }
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List getListItems() throws CmsException {

        List result = new ArrayList();
        List historicalVersions = new ArrayList();

        if (CmsStringUtil.isNotEmpty(getParamResource())) {
            historicalVersions = getCms().readAllAvailableVersions(getParamResource());
        }
        Iterator itVersions = historicalVersions.iterator();
        while (itVersions.hasNext()) {
            I_CmsHistoryResource histRes = (I_CmsHistoryResource)itVersions.next();

            // the publish tag for the history project            
            int publishTag = histRes.getPublishTag();

            CmsHistoryProject project = getCms().readHistoryProject(publishTag);
            String filetype = String.valueOf(histRes.getTypeId());
            String dateLastModified = getMessages().getDateTime(histRes.getDateLastModified());
            String datePublished = getMessages().getDateTime(project.getPublishingDate());

            CmsListItem item = getList().newItem("" + histRes.getVersion());

            int factor = 1;
            if (result.isEmpty() && !histRes.getState().isDeleted()) {
                factor = -1;
            }
            CmsVersionWrapper version = new CmsVersionWrapper(factor * histRes.getVersion());

            //version
            item.set(LIST_COLUMN_VERSION, version);
            // publish date
            item.set(LIST_COLUMN_DATE_PUBLISHED, datePublished);
            // last modification date
            item.set(LIST_COLUMN_DATE_LAST_MODIFIED, dateLastModified);
            // resource type           
            item.set(LIST_COLUMN_FILE_TYPE, filetype);
            // user
            String user = histRes.getUserLastModified().toString();
            try {
                user = CmsPrincipal.readPrincipalIncludingHistory(getCms(), histRes.getUserLastModified()).getName();
            } catch (CmsDbEntryNotFoundException e) {
                // ignore
            }
            item.set(LIST_COLUMN_USER, user);
            // path           
            item.set(LIST_COLUMN_RESOURCE_PATH, getCms().getRequestContext().removeSiteRoot(histRes.getRootPath()));
            // size 
            item.set(LIST_COLUMN_SIZE, new Integer(histRes.getLength()).toString());
            // invisible publish tag (for reading history project in fillDetails)
            item.set(LIST_COLUMN_PUBLISH_TAG, new Integer(publishTag));
            // invisible structure id           
            item.set(LIST_COLUMN_STRUCTURE_ID, histRes.getStructureId().toString());

            result.add(item);
        }

        if (result.isEmpty()) {
            CmsResource onlineResource = null;

            // this is to prevent problems after an update without keeping historical info
            CmsProject project = getCms().getRequestContext().currentProject();
            try {
                getCms().getRequestContext().setCurrentProject(getCms().readProject(CmsProject.ONLINE_PROJECT_ID));
                onlineResource = getCms().readResource(getParamResource(), CmsResourceFilter.IGNORE_EXPIRATION);

                CmsListItem item = getList().newItem("" + onlineResource.getVersion());
                //version
                item.set(LIST_COLUMN_VERSION, new CmsVersionWrapper(-1 * onlineResource.getVersion()));
                // publish date
                item.set(LIST_COLUMN_DATE_PUBLISHED, "-");
                // last modification date
                item.set(
                    LIST_COLUMN_DATE_LAST_MODIFIED,
                    getMessages().getDateTime(onlineResource.getDateLastModified()));
                // resource type           
                item.set(LIST_COLUMN_FILE_TYPE, String.valueOf(onlineResource.getTypeId()));
                // user   
                String user = onlineResource.getUserLastModified().toString();
                try {
                    user = CmsPrincipal.readPrincipalIncludingHistory(getCms(), onlineResource.getUserLastModified()).getName();
                } catch (CmsDbEntryNotFoundException e) {
                    // ignore
                }
                item.set(LIST_COLUMN_USER, user);
                // size 
                item.set(LIST_COLUMN_SIZE, new Integer(onlineResource.getLength()).toString());
                // path
                item.set(LIST_COLUMN_RESOURCE_PATH, getCms().getSitePath(onlineResource));
                // invisible structure id           
                item.set(LIST_COLUMN_STRUCTURE_ID, onlineResource.getStructureId().toString());

                result.add(item);
            } catch (CmsVfsResourceNotFoundException e) {
                // ignore, most likely the file is new
            } finally {
                getCms().getRequestContext().setCurrentProject(project);
            }
        }
        CmsResource offlineResource = getCms().readResource(getParamResource(), CmsResourceFilter.IGNORE_EXPIRATION);

        // hide the size for folders
        getList().getMetadata().getColumnDefinition(LIST_COLUMN_SIZE).setVisible(offlineResource.isFile());
        // hide the preview button for folders
        getList().getMetadata().getColumnDefinition(LIST_COLUMN_ICON).setVisible(offlineResource.isFile());

        // display offline version, if state is not unchanged
        if (!offlineResource.getState().isUnchanged()) {
            CmsListItem item = getList().newItem("" + CmsHistoryResourceHandler.PROJECT_OFFLINE_VERSION);
            //version
            item.set(LIST_COLUMN_VERSION, new CmsVersionWrapper(CmsHistoryResourceHandler.PROJECT_OFFLINE_VERSION));
            // publish date
            item.set(LIST_COLUMN_DATE_PUBLISHED, "-");
            // last modification date
            item.set(LIST_COLUMN_DATE_LAST_MODIFIED, getMessages().getDateTime(offlineResource.getDateLastModified()));
            // resource type           
            item.set(LIST_COLUMN_FILE_TYPE, String.valueOf(offlineResource.getTypeId()));
            // user
            String user = offlineResource.getUserLastModified().toString();
            try {
                user = CmsPrincipal.readPrincipalIncludingHistory(getCms(), offlineResource.getUserLastModified()).getName();
            } catch (CmsDbEntryNotFoundException e) {
                // ignore
            }
            item.set(LIST_COLUMN_USER, user);
            // size 
            item.set(LIST_COLUMN_SIZE, new Integer(offlineResource.getLength()).toString());
            // path
            item.set(LIST_COLUMN_RESOURCE_PATH, getCms().getSitePath(offlineResource));
            // invisible structure id           
            item.set(LIST_COLUMN_STRUCTURE_ID, offlineResource.getStructureId().toString());

            result.add(item);
        }

        boolean comparable = (result.size() > 1);
        getList().getMetadata().getColumnDefinition(LIST_COLUMN_SEL1).setVisible(comparable);
        getList().getMetadata().getColumnDefinition(LIST_COLUMN_SEL2).setVisible(comparable);
        getList().getMetadata().getMultiAction(LIST_MACTION_COMPARE).setVisible(comparable);

        return result;
    }

    /**
     * Restores a backed up resource version.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    protected void performRestoreOperation() throws CmsException {

        CmsUUID structureId = new CmsUUID((String)getSelectedItem().get(LIST_COLUMN_STRUCTURE_ID));
        int version = Integer.parseInt((getSelectedItems().get(0)).getId());
        if (version == CmsHistoryResourceHandler.PROJECT_OFFLINE_VERSION) {
            // it is not possible to restore the offline version
            return;
        }
        CmsResource res = getCms().readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
        checkLock(getCms().getSitePath(res));
        getCms().restoreResourceVersion(res.getStructureId(), version);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        CmsListColumnDefinition previewCol = new CmsListColumnDefinition(LIST_COLUMN_VIEW);
        previewCol.setName(Messages.get().container(Messages.GUI_HISTORY_COLS_VIEW_0));
        previewCol.setWidth("20");
        previewCol.setVisible(false);
        previewCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        previewCol.setSorteable(false);

        // create column for icon
        CmsListColumnDefinition restoreCol = new CmsListColumnDefinition(LIST_COLUMN_RESTORE);
        restoreCol.setName(Messages.get().container(Messages.GUI_HISTORY_COLS_RESTORE_0));
        restoreCol.setWidth("20");
        restoreCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        restoreCol.setListItemComparator(new CmsListItemActionIconComparator());
        restoreCol.setSorteable(false);

        // add icon action
        CmsListDirectAction restoreAction = new CmsListDirectAction(LIST_ACTION_RESTORE);
        restoreAction.setName(Messages.get().container(Messages.GUI_HISTORY_RESTORE_VERSION_0));
        restoreAction.setIconPath("tools/ex_history/buttons/restore.png");
        restoreAction.setConfirmationMessage(Messages.get().container(Messages.GUI_HISTORY_CONFIRMATION_0));
        restoreAction.setEnabled(true);

        restoreCol.addDirectAction(restoreAction);
        // add it to the list definition
        metadata.addColumn(restoreCol);

        // create column for icon
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        iconCol.setName(Messages.get().container(Messages.GUI_HISTORY_COLS_VIEW_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setListItemComparator(new CmsListItemActionIconComparator());

        // add icon action
        CmsListDirectAction fileAction = new CmsListResourceIconAction(
            LIST_ACTION_VIEW,
            LIST_COLUMN_FILE_TYPE,
            getCms()) {

            /**
             * @see org.opencms.workplace.list.CmsListResourceIconAction#defButtonHtml(CmsObject, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, java.lang.String, java.lang.String, java.lang.String, boolean)
             */
            @Override
            public String defButtonHtml(
                CmsObject cms,
                String id,
                String helpId,
                String name,
                String helpText,
                boolean enabled,
                String iconPath,
                String confirmationMessage,
                String onClick,
                boolean singleHelp) {

                StringBuffer jsCode = new StringBuffer(512);
                jsCode.append("window.open('");
                CmsVersionWrapper version = (CmsVersionWrapper)getItem().get(LIST_COLUMN_VERSION);

                // is the resource already a sibling already deleted?
                jsCode.append(OpenCms.getLinkManager().substituteLink(
                    cms,
                    getHistoryLink(
                        cms,
                        new CmsUUID(getItem().get(LIST_COLUMN_STRUCTURE_ID).toString()),
                        version.toString())));
                jsCode.append("','version','scrollbars=yes, resizable=yes, width=800, height=600')");
                return super.defButtonHtml(
                    cms,
                    id,
                    helpId,
                    name,
                    helpText,
                    enabled,
                    iconPath,
                    confirmationMessage,
                    jsCode.toString(),
                    singleHelp);
            }

        };
        fileAction.setName(Messages.get().container(Messages.GUI_HISTORY_PREVIEW_0));
        fileAction.setEnabled(true);
        iconCol.addDirectAction(fileAction);
        // add it to the list definition
        metadata.addColumn(iconCol);
        iconCol.setPrintable(false);

        // add column for version
        CmsListColumnDefinition versionCol = new CmsListColumnDefinition(LIST_COLUMN_VERSION);
        versionCol.setName(Messages.get().container(Messages.GUI_HISTORY_COLS_VERSION_0));
        versionCol.setWidth("5%");
        versionCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        versionCol.setFormatter(new I_CmsListFormatter() {

            /**
             * @see org.opencms.workplace.list.I_CmsListFormatter#format(java.lang.Object, java.util.Locale)
             */
            public String format(Object data, Locale locale) {

                if ((data == null) || !(data instanceof CmsVersionWrapper)) {
                    return "";
                }
                CmsVersionWrapper version = (CmsVersionWrapper)data;
                return CmsHistoryList.getDisplayVersion(version.toString(), locale);
            }

        });
        metadata.addColumn(versionCol);

        // add column for file type
        CmsListColumnDefinition groupCol = new CmsListColumnDefinition(LIST_COLUMN_FILE_TYPE);
        groupCol.setVisible(false);
        metadata.addColumn(groupCol);

        // add column for resource path
        CmsListColumnDefinition pathCol = new CmsListColumnDefinition(LIST_COLUMN_RESOURCE_PATH);
        pathCol.setName(Messages.get().container(Messages.GUI_HISTORY_COLS_RESOURCE_PATH_0));
        pathCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        pathCol.setWidth("40%");
        metadata.addColumn(pathCol);

        // add column for date published
        CmsListColumnDefinition datePublishedCol = new CmsListColumnDefinition(LIST_COLUMN_DATE_PUBLISHED);
        datePublishedCol.setName(Messages.get().container(Messages.GUI_HISTORY_COLS_DATE_PUBLISHED_0));
        datePublishedCol.setWidth("20%");
        datePublishedCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        metadata.addColumn(datePublishedCol);

        // add column for date last modified
        CmsListColumnDefinition nicenameCol = new CmsListColumnDefinition(LIST_COLUMN_DATE_LAST_MODIFIED);
        nicenameCol.setName(Messages.get().container(Messages.GUI_HISTORY_COLS_DATE_LAST_MODIFIED_0));
        nicenameCol.setWidth("20%");
        nicenameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        metadata.addColumn(nicenameCol);

        // add column for user modified
        CmsListColumnDefinition userCol = new CmsListColumnDefinition(LIST_COLUMN_USER);
        userCol.setName(Messages.get().container(Messages.GUI_HISTORY_COLS_USER_0));
        userCol.setWidth("30%");
        userCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        metadata.addColumn(userCol);

        // add column for date last modified
        CmsListColumnDefinition sizeCol = new CmsListColumnDefinition(LIST_COLUMN_SIZE);
        sizeCol.setName(Messages.get().container(Messages.GUI_HISTORY_COLS_SIZE_0));
        sizeCol.setWidth("10%");
        sizeCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        metadata.addColumn(sizeCol);

        // create column for radio button 1
        CmsListColumnDefinition radioSel1Col = new CmsListColumnDefinition(LIST_COLUMN_SEL1);
        radioSel1Col.setName(Messages.get().container(Messages.GUI_HISTORY_COLS_VERSION1_0));
        radioSel1Col.setWidth("20");
        radioSel1Col.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        radioSel1Col.setSorteable(false);

        // add item selection action
        CmsListItemSelectionAction sel1Action = new CmsListItemSelectionAction(LIST_RACTION_SEL1, LIST_MACTION_COMPARE);
        sel1Action.setName(Messages.get().container(Messages.GUI_HISTORY_FIRST_VERSION_0));
        sel1Action.setEnabled(true);
        radioSel1Col.addDirectAction(sel1Action);
        metadata.addColumn(radioSel1Col);

        // create column for radio button 2
        CmsListColumnDefinition radioSel2Col = new CmsListColumnDefinition(LIST_COLUMN_SEL2);
        radioSel2Col.setName(Messages.get().container(Messages.GUI_HISTORY_COLS_VERSION2_0));
        radioSel2Col.setWidth("20");
        radioSel2Col.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        radioSel2Col.setSorteable(false);

        // add item selection action
        CmsListItemSelectionAction sel2Action = new CmsListItemSelectionAction(LIST_RACTION_SEL2, LIST_MACTION_COMPARE);
        sel2Action.setName(Messages.get().container(Messages.GUI_HISTORY_SECOND_VERSION_0));
        sel2Action.setEnabled(true);
        radioSel2Col.addDirectAction(sel2Action);
        metadata.addColumn(radioSel2Col);

        // create invisible publish tag column to allow fillDetails to be able to read the proper 
        // historical project
        CmsListColumnDefinition publishTagCol = new CmsListColumnDefinition(LIST_COLUMN_PUBLISH_TAG);
        publishTagCol.setSorteable(false);
        publishTagCol.setVisible(false);
        metadata.addColumn(publishTagCol);

        // create invisible strcuture id column 
        CmsListColumnDefinition strIdCol = new CmsListColumnDefinition(LIST_COLUMN_STRUCTURE_ID);
        strIdCol.setSorteable(false);
        strIdCol.setVisible(false);
        metadata.addColumn(strIdCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add index source details
        CmsListItemDetails indexDetails = new CmsListItemDetails(GUI_LIST_HISTORY_DETAIL_PROJECT_0);
        indexDetails.setAtColumn(LIST_COLUMN_VERSION);
        indexDetails.setVisible(false);
        indexDetails.setShowActionName(Messages.get().container(Messages.GUI_LIST_HISTORY_DETAIL_PROJECT_NAME_SHOW_0));
        indexDetails.setShowActionHelpText(Messages.get().container(
            Messages.GUI_LIST_HISTORY_DETAIL_PROJECT_SHOW_HELP_0));
        indexDetails.setHideActionName(Messages.get().container(Messages.GUI_LIST_HISTORY_DETAIL_PROJECT_NAME_HIDE_0));
        indexDetails.setHideActionHelpText(Messages.get().container(
            Messages.GUI_LIST_HISTORY_DETAIL_PROJECT_HIDE_HELP_0));
        indexDetails.setName(Messages.get().container(Messages.GUI_LIST_HISTORY_DETAIL_PROJECT_INFO_0));
        indexDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_LIST_HISTORY_DETAIL_PROJECT_INFO_0)));
        metadata.addItemDetails(indexDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // add compare action
        CmsListRadioMultiAction compareAction = new CmsListRadioMultiAction(
            LIST_MACTION_COMPARE,
            Arrays.asList(new String[] {LIST_RACTION_SEL1, LIST_RACTION_SEL2}));
        compareAction.setName(Messages.get().container(Messages.GUI_HISTORY_COMPARE_0));
        compareAction.setIconPath("tools/ex_history/buttons/compare.png");
        metadata.addMultiAction(compareAction);
    }

    /**
     * Fills details of the project into the given item. <p> 
     * 
     * @param item the list item to fill 
     * 
     * @param detailId the id for the detail to fill
     * 
     */
    private void fillDetailProject(CmsListItem item, String detailId) {

        StringBuffer html = new StringBuffer();

        // search /read for the corresponding history project: it's tag id transmitted from getListItems() 
        // in a hidden column
        Object tagIdObj = item.get(LIST_COLUMN_PUBLISH_TAG);
        if (tagIdObj != null) {
            // it is null if the offline version with changes is shown here: now history project available then
            int tagId = ((Integer)tagIdObj).intValue();
            try {
                CmsHistoryProject project = getCms().readHistoryProject(tagId);
                // output of project info
                html.append(project.getName()).append("<br/>").append(project.getDescription());
            } catch (CmsException cmse) {
                html.append(cmse.getMessageContainer().key(this.getLocale()));
            }
        }
        item.set(detailId, html.toString());
    }
}