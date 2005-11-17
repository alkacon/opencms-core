/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsHistoryList.java,v $
 * Date   : $Date: 2005/11/17 11:49:12 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.file.CmsBackupProject;
import org.opencms.file.CmsBackupResource;
import org.opencms.file.CmsBackupResourceHandler;
import org.opencms.file.CmsFile;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListItemSelectionAction;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListRadioMultiAction;
import org.opencms.workplace.list.CmsListResourceIconAction;
import org.opencms.workplace.tools.CmsToolDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Displays the history of a file.<p>
 * 
 * @author Jan Baudisch  
 * @author Armen Markarian 
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.0.2 
 */
public class CmsHistoryList extends A_CmsListDialog {

    /** 
     * Wrapper class for the version which is either an integer or the string "offline".<p>
     */
    public class VersionWrapper implements Comparable {
        
        private Object m_version;
        
        /** 
         * Constructs a new version wrapper.<p>
         * @param version the version of the file
         */
        public VersionWrapper(Object version) {
            
            m_version = version;
        }
        
        /**
         * 
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Object o) {
            
            VersionWrapper version = (VersionWrapper)o;
            if (String.class.equals(version.getVersion().getClass()) && Integer.class.equals(getVersion().getClass())) {
                return -1;
            } else if (Integer.class.equals(version.getVersion().getClass()) && String.class.equals(getVersion().getClass())) {
                return 1;
            } else {
                return ((Comparable)m_version).compareTo(version.getVersion());   
            }
        }
        
        /**
         * Returns the version of the file.<p>
         * @return the version of the file
         */
        public Object getVersion() {
            
            return m_version;
        }
        
        /**
         * 
         * @see java.lang.Object#toString()
         */
        public String toString() {
            
            return m_version.toString();   
        }
        
    }

    /** list action id constant. */
    public static final String LIST_ACTION_ICON = "ai";
    
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

    /** List column delete. */
    public static final String LIST_COLUMN_RESTORE = "cr";

    /** list column id constant. */
    public static final String LIST_COLUMN_SEL1 = "cs1";
    
    /** list column id constant. */
    public static final String LIST_COLUMN_SEL2 = "cs2";

    /** list column id constant. */
    public static final String LIST_COLUMN_SIZE = "cs";

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

    /** constant for the offline project.<p> */
    public static final String OFFLINE_PROJECT = "offline";

    /** list column id constant. */
    public static final String PARAM_TAGID_1 = "tagid1";

    /** list column id constant. */
    public static final String PARAM_TAGID_2 = "tagid2";

    /** list column id constant. */
    public static final String PARAM_VERSION_1 = "version1";

    /** list column id constant. */
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

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_HISTORY_0),
            LIST_COLUMN_VERSION, CmsListOrderEnum.ORDER_DESCENDING, null);
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
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    public void executeListMultiActions() throws IOException, ServletException {

        if (getParamListAction().equals(LIST_MACTION_COMPARE)) {
            CmsListItem item1 = (CmsListItem)getSelectedItems().get(0);
            CmsListItem item2 = (CmsListItem)getSelectedItems().get(1);
            Map params = new HashMap();
            params.put(PARAM_TAGID_1, item1.getId());
            params.put(PARAM_TAGID_2, item2.getId());
            params.put(PARAM_VERSION_1, item1.get(LIST_COLUMN_VERSION));
            params.put(PARAM_VERSION_2, item2.get(LIST_COLUMN_VERSION));
            params.put(PARAM_ACTION, DIALOG_INITIAL);
            params.put(PARAM_STYLE, CmsToolDialog.STYLE_NEW);
            params.put(PARAM_RESOURCE, getParamResource());
            getToolManager().jspForwardTool(this, "/history/comparison", params);
        }
        listSave();
    }
    
    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
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
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlStart()
     */
    protected String defaultActionHtmlStart() {

        return getList().listJs(getLocale()) + dialogContentStart(getParamTitle());
    }
    
    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        // no-op
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() throws CmsException {
        
        List result = new ArrayList();
        String userName = "";
        List backupFileHeaders = getCms().readAllBackupFileHeaders(getParamResource());           
        Iterator i = backupFileHeaders.iterator();
        while (i.hasNext()) {
            CmsBackupResource file = (CmsBackupResource)i.next();                
            // the tagId for the Backup project            
            int tagId = file.getTagId();
            //String version = .toString();
            CmsBackupProject project = getCms().readBackupProject(tagId);
            String versionId = new Integer(project.getVersionId()).toString();
            String filetype = String.valueOf(file.getTypeId());
            String dateLastModified = getMessages().getDateTime(file.getDateLastModified());             
            String datePublished = getMessages().getDateTime(project.getPublishingDate());
            try {
                userName = getCms().readUser(file.getUserLastModified()).getName();
            } catch (CmsException e) {
                userName = file.getLastModifiedByName();
            }
            CmsListItem item = getList().newItem(versionId);
            //version
            item.set(LIST_COLUMN_VERSION, new VersionWrapper(new Integer(file.getVersionId())));
            // filename
            item.set(LIST_COLUMN_DATE_PUBLISHED, datePublished);
            // nicename
            item.set(LIST_COLUMN_DATE_LAST_MODIFIED, dateLastModified);
            // group           
            item.set(LIST_COLUMN_FILE_TYPE, filetype);
            // user           
            item.set(LIST_COLUMN_USER, userName);
            // size 
            item.set(LIST_COLUMN_SIZE, new Integer(file.getLength()).toString());
            result.add(item);
        }
        CmsFile onlineFile = getCms().readFile(getParamResource());
        CmsListItem item = getList().newItem("-1");
        userName = getCms().readUser(onlineFile.getUserLastModified()).getName();
        //version
        item.set(LIST_COLUMN_VERSION, new VersionWrapper(OFFLINE_PROJECT));
        // filename
        item.set(LIST_COLUMN_DATE_PUBLISHED, "-");
        // nicename
        item.set(LIST_COLUMN_DATE_LAST_MODIFIED, getMessages().getDateTime(onlineFile.getDateLastModified()));
        // group           
        item.set(LIST_COLUMN_FILE_TYPE, String.valueOf(onlineFile.getTypeId()));
        // user           
        item.set(LIST_COLUMN_USER, userName);
        // size 
        item.set(LIST_COLUMN_SIZE, new Integer(onlineFile.getLength()).toString());
        result.add(item);
        
        return result;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * Restores a backed up resource version.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    protected void performRestoreOperation() throws CmsException {

        int tagId = Integer.parseInt(((CmsListItem)getSelectedItems().get(0)).getId());
        String resourcename = getCms().getSitePath(getCms().readResource(getParamResource()));
        checkLock(getParamResource());
        getCms().restoreResourceBackup(resourcename, tagId);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {
        
        CmsListColumnDefinition previewCol = new CmsListColumnDefinition(LIST_COLUMN_VIEW);
        previewCol.setName(Messages.get().container(Messages.GUI_HISTORY_COLS_VIEW_0));
        previewCol.setWidth("20");
        previewCol.setVisible(false);
        previewCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        previewCol.setSorteable(false);

        // add icon action
        CmsListDirectAction iconAction = new CmsListDirectAction(LIST_ACTION_ICON);
        iconAction.setName(Messages.get().container(Messages.GUI_HISTORY_PREVIEW_0));
        iconAction.setIconPath("tools/history/buttons/preview.png");
        iconAction.setEnabled(true);
        previewCol.addDirectAction(iconAction);
        // add it to the list definition
        metadata.addColumn(previewCol);
        
        // create column for icon
        CmsListColumnDefinition restoreCol = new CmsListColumnDefinition(LIST_COLUMN_RESTORE);
        restoreCol.setName(Messages.get().container(Messages.GUI_HISTORY_COLS_RESTORE_0));
        restoreCol.setWidth("20");
        restoreCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        restoreCol.setListItemComparator(new CmsListItemActionIconComparator());
        restoreCol.setSorteable(false);

        // add icon action
        CmsListDirectAction restoreAction = new CmsListDirectAction(LIST_ACTION_RESTORE) {
            // do not show icon for offline version
            public boolean isVisible() {
                return !"-1".equals(getItem().getId().toString());
            }
        };
        restoreAction.setName(Messages.get().container(Messages.GUI_HISTORY_RESTORE_VERSION_0));
        restoreAction.setIconPath("tools/history/buttons/restore.png");
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
        CmsListDirectAction fileAction = new CmsListResourceIconAction(LIST_ACTION_VIEW, LIST_COLUMN_FILE_TYPE, getCms()) {
            public String defButtonHtml(CmsJspActionElement jsp, String id, String helpId, String name, String helpText,
                boolean enabled, String iconPath, String confirmationMessage, String onClick, boolean singleHelp) {
                StringBuffer jsCode = new StringBuffer(512);
                jsCode.append("window.open('");
                StringBuffer link = new StringBuffer(1024);
                String versionId = getItem().getId().toString();
                if ("-1".equals(versionId)) {
                    // offline version
                    link.append(jsp.getRequest().getParameter(PARAM_RESOURCE));
                } else {
                    // backup version
                    link.append(CmsBackupResourceHandler.BACKUP_HANDLER);
                    link.append(jsp.getRequestContext().addSiteRoot(jsp.getRequest().getParameter(PARAM_RESOURCE)));
                    link.append('?');
                    link.append(CmsBackupResourceHandler.PARAM_VERSIONID);
                    link.append('=');
                    link.append(versionId);
                }
                jsCode.append(jsp.link(link.toString()));
                jsCode.append("','version','scrollbars=yes, resizable=yes, width=800, height=600')");
                return super.defButtonHtml(jsp, id, helpId, name, helpText, enabled,
                    iconPath, confirmationMessage, jsCode.toString(), singleHelp);
            }
        };
        fileAction.setName(Messages.get().container(Messages.GUI_HISTORY_PREVIEW_0));
        fileAction.setEnabled(true);
        iconCol.addDirectAction(fileAction);
        // add it to the list definition
        metadata.addColumn(iconCol);
        
        // add column for version
        CmsListColumnDefinition versionCol = new CmsListColumnDefinition(LIST_COLUMN_VERSION);
        versionCol.setName(Messages.get().container(Messages.GUI_HISTORY_COLS_VERSION_0));
        versionCol.setWidth("5%");
        versionCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        metadata.addColumn(versionCol);

        // add column for file type
        CmsListColumnDefinition groupCol = new CmsListColumnDefinition(LIST_COLUMN_FILE_TYPE);
        groupCol.setName(Messages.get().container(Messages.GUI_HISTORY_COLS_FILE_TYPE_0));
        groupCol.setWidth("10%");
        groupCol.setVisible(false);
        groupCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        metadata.addColumn(groupCol);
        
        // add column for date published
        CmsListColumnDefinition datePublishedCol = new CmsListColumnDefinition(LIST_COLUMN_DATE_PUBLISHED);
        datePublishedCol.setName(Messages.get().container(Messages.GUI_HISTORY_COLS_DATE_PUBLISHED_0));
        datePublishedCol.setWidth("30%");
        datePublishedCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        metadata.addColumn(datePublishedCol);

        // add column for date last modified
        CmsListColumnDefinition nicenameCol = new CmsListColumnDefinition(LIST_COLUMN_DATE_LAST_MODIFIED);
        nicenameCol.setName(Messages.get().container(Messages.GUI_HISTORY_COLS_DATE_LAST_MODIFIED_0));
        nicenameCol.setWidth("30%");
        nicenameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        metadata.addColumn(nicenameCol);
        
        // add column for user modified
        CmsListColumnDefinition userCol = new CmsListColumnDefinition(LIST_COLUMN_USER);
        userCol.setName(Messages.get().container(Messages.GUI_HISTORY_COLS_USER_0));
        userCol.setWidth("12%");
        userCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        metadata.addColumn(userCol);
        
        // add column for date last modified
        CmsListColumnDefinition sizeCol = new CmsListColumnDefinition(LIST_COLUMN_SIZE);
        sizeCol.setName(Messages.get().container(Messages.GUI_HISTORY_COLS_SIZE_0));
        sizeCol.setWidth("13%");
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
    }
    
    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // no-op
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // add compare action
        CmsListRadioMultiAction compareAction = new CmsListRadioMultiAction(
            LIST_MACTION_COMPARE,
            Arrays.asList(new String[] {LIST_RACTION_SEL1, LIST_RACTION_SEL2}));
        compareAction.setName(Messages.get().container(Messages.GUI_HISTORY_COMPARE_0));
        compareAction.setIconPath("tools/history/buttons/compare.png");
        metadata.addMultiAction(compareAction);
    }
}