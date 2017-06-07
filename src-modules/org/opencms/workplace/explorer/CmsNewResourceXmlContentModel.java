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

import org.opencms.db.CmsResourceState;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.editors.CmsPreEditorAction;
import org.opencms.workplace.list.A_CmsSelectResourceList;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.I_CmsListResourceCollector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * The new resource xmlcontent model file selection dialog handles the selection of a model file for a new xmlcontent.<p>
 *
 * Creates a list of model files to choose from and forwards either to the new resource dialog or to the editor.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/newresource_xmlcontent_modelfile.jsp</li>
 * </ul>
 *
 * @since 6.5.4
 */
public class CmsNewResourceXmlContentModel extends A_CmsSelectResourceList {

    /** List detail description info. */
    public static final String LIST_DETAIL_DESCRIPTION = "dd";

    /** List id constant. */
    public static final String LIST_ID = "nrxm";

    /** Absolute path to the model file dialog. */
    public static final String VFS_PATH_MODELDIALOG = CmsWorkplace.VFS_PATH_COMMONS
        + "newresource_xmlcontent_modelfile.jsp";

    /** Absolute path to thenew resource dialog. */
    public static final String VFS_PATH_NEWRESOURCEDIALOG = CmsWorkplace.VFS_PATH_COMMONS
        + "newresource_xmlcontent.jsp";

    /** The internal collector instance. */
    private I_CmsListResourceCollector m_collector;

    /** The flag indicating if the ".html" suffix should be added automatically. */
    private String m_paramAppendSuffixHtml;

    /** The back link URL used when displaying the dialog in pre editor mode. */
    private String m_paramBackLink;

    /** The selected model file for the new resource. */
    private String m_paramModelFile;

    /** The flag indicating if properties should be edited afterwards. */
    private String m_paramNewResourceEditProps;

    /** The resource type name to create. */
    private String m_paramNewResourceType;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsNewResourceXmlContentModel(CmsJspActionElement jsp) {

        super(
            jsp,
            LIST_ID,
            Messages.get().container(Messages.GUI_NEWRESOURCE_XMLCONTENT_CHOOSEMODEL_0),
            LIST_COLUMN_TITLE,
            CmsListOrderEnum.ORDER_ASCENDING,
            null);

        // set the style to show common workplace dialog layout
        setParamStyle("");

        // prevent paging, usually there are only few model files
        getList().setMaxItemsPerPage(Integer.MAX_VALUE);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsNewResourceXmlContentModel(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Overrides the default action, parameters have to be set and the forward targets may be different.<p>
     *
     * @see org.opencms.workplace.list.A_CmsListDialog#actionDialog()
     */
    @Override
    public void actionDialog() throws JspException, ServletException, IOException {

        if (getAction() == ACTION_CONTINUE) {

            // get the selected resource name
            String resource = getSelectedResourceName();
            if (CmsStringUtil.isNotEmpty(resource)
                && !key(Messages.GUI_NEWRESOURCE_XMLCONTENT_NO_MODEL_0).equals(resource)) {
                // something was selected, set the model file
                setParamModelFile(resource);
            }

            if (CmsPreEditorAction.isPreEditorMode(this)) {
                // forward back to editor with different parameters!
                Map<String, String[]> params = new HashMap<String, String[]>(1);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamModelFile())) {
                    // model file was selected, put it to parameters for editor notification
                    params.put(CmsNewResourceXmlContent.PARAM_MODELFILE, new String[] {getParamModelFile()});
                }
                CmsPreEditorAction.sendForwardToEditor(this, params);
            } else {
                // forward back to new xmlcontent dialog, set action parameter before
                setParamAction(CmsNewResource.DIALOG_SUBMITFORM);
                sendForward(nextUrl(), paramsAsParameterMap());
            }
            return;
        } else if (getAction() == ACTION_CANCEL) {

            if (isPreEditor()) {
                // in pre editor mode, execute common close action
                actionClose();
            } else {
                // in workplace, forward back to new xmlcontent dialog with necessary parameters
                Map<String, String[]> params = new HashMap<String, String[]>(3);
                params.put(PARAM_ACTION, new String[] {DIALOG_CANCEL});
                if (CmsStringUtil.isNotEmpty(getParamFramename())) {
                    params.put(PARAM_FRAMENAME, new String[] {getParamFramename()});
                }
                if (CmsStringUtil.isNotEmpty(getParamCloseLink())) {
                    params.put(PARAM_CLOSELINK, new String[] {getParamCloseLink()});
                }
                sendForward(VFS_PATH_NEWRESOURCEDIALOG, params);
            }
            return;
        }
        super.actionDialog();
    }

    /**
     * The buttons in the new resource dialog depend on various preconditions.<p>
     *
     * Variations:
     * <ul>
     * <li>dialog is opened in direct edit mode</li>
     * <li>"edit properties" has been selected or not</li>
     * </ul>
     *
     * @see org.opencms.workplace.list.A_CmsSelectResourceList#dialogButtons()
     */
    @Override
    public String dialogButtons() {

        if (Boolean.valueOf(getParamNewResourceEditProps()).booleanValue()
            || CmsPreEditorAction.isPreEditorMode(this)) {
            // show next button, after that the properties are edited
            return dialogButtons(new int[] {BUTTON_NEXT, BUTTON_CANCEL}, new String[] {"", ""});
        }
        return dialogButtons(new int[] {BUTTON_FINISH, BUTTON_CANCEL}, new String[] {"", ""});
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#getCollector()
     */
    @Override
    public I_CmsListResourceCollector getCollector() {

        if (m_collector == null) {
            List<CmsResource> modelFiles = new ArrayList<CmsResource>();
            String folderPath = getSettings().getExplorerResource();
            if (isPreEditor()) {
                // in pre edit mode, we have to use the resource parameter to get the current folder
                if (CmsStringUtil.isNotEmpty(getParamResource())) {
                    folderPath = CmsResource.getFolderPath(getParamResource());
                }
            }
            List<CmsResource> realModelFiles = CmsResourceTypeXmlContent.getModelFiles(
                getCms(),
                folderPath,
                getParamNewResourceType());
            // create a dummy resource object to add to be able to select "none" as model file
            int dummyType = realModelFiles.get(0).getTypeId();
            String resPath = key(Messages.GUI_NEWRESOURCE_XMLCONTENT_NO_MODEL_0);
            CmsResource dummy = new CmsResource(
                CmsUUID.getConstantUUID(CmsNewResourceXmlContent.VALUE_NONE + "s"),
                CmsUUID.getConstantUUID(CmsNewResourceXmlContent.VALUE_NONE + "r"),
                resPath,
                dummyType,
                false,
                0,
                null,
                CmsResourceState.STATE_UNCHANGED,
                0,
                getCms().getRequestContext().getCurrentUser().getId(),
                0,
                getCms().getRequestContext().getCurrentUser().getId(),
                0,
                Long.MAX_VALUE,
                0,
                0,
                0,
                0);
            modelFiles.add(dummy);
            // add the real model files to the list
            modelFiles.addAll(realModelFiles);
            m_collector = new CmsNewResourceXmlContentModelCollector(this, modelFiles);
        }
        return m_collector;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsSelectResourceList#getListTitle()
     */
    @Override
    public String getListTitle() {

        return key(Messages.GUI_NEWRESOURCE_XMLCONTENT_CHOOSEMODEL_0);
    }

    /**
     * Returns the parameter to check if a ".html" suffix should be added to the new resource name.<p>
     *
     * @return the parameter to check if a ".html" suffix should be added to the new resource name
     */
    public String getParamAppendSuffixHtml() {

        return m_paramAppendSuffixHtml;
    }

    /**
     * Returns the back link URL used when displaying the dialog in pre editor mode.<p>
     *
     * @return the back link URL used when displaying the dialog in pre editor mode
     */
    public String getParamBackLink() {

        return m_paramBackLink;
    }

    /**
     * Returns the parameter that specifies the model file name.<p>
     *
     * @return the parameter that specifies the model file name
     */
    public String getParamModelFile() {

        return m_paramModelFile;
    }

    /**
     * Returns the new resource edit properties flag parameter.<p>
     *
     * @return the new resource edit properties flag parameter
     */
    public String getParamNewResourceEditProps() {

        return m_paramNewResourceEditProps;
    }

    /**
     * Returns the new resource type parameter.<p>
     *
     * @return the new resource type parameter
     */
    public String getParamNewResourceType() {

        return m_paramNewResourceType;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsSelectResourceList#nextUrl()
     */
    @Override
    public String nextUrl() {

        return VFS_PATH_COMMONS + "newresource_xmlcontent.jsp";

    }

    /**
     * Sets the parameter to check if a ".html" suffix should be added to the new resource name.<p>
     *
     * @param paramAppendSuffixHtml the parameter to check if a ".html" suffix should be added to the new resource name
     */
    public void setParamAppendSuffixHtml(String paramAppendSuffixHtml) {

        m_paramAppendSuffixHtml = paramAppendSuffixHtml;
    }

    /**
     * Sets the back link URL used when displaying the dialog in pre editor mode.<p>
     *
     * @param paramBackLink the back link URL used when displaying the dialog in pre editor mode
     */
    public void setParamBackLink(String paramBackLink) {

        m_paramBackLink = paramBackLink;
    }

    /**
     * Sets the parameter that specifies the model file name.<p>
     *
     * @param paramMasterFile the parameter that specifies the model file name
     */
    public void setParamModelFile(String paramMasterFile) {

        m_paramModelFile = paramMasterFile;
    }

    /**
     * Sets the new resource edit properties flag parameter.<p>
     *
     * @param newResourceEditProps the new resource edit properties flag parameter
     */
    public void setParamNewResourceEditProps(String newResourceEditProps) {

        m_paramNewResourceEditProps = newResourceEditProps;
    }

    /**
     * Sets the new resource type parameter.<p>
     *
     * @param newResourceType the new resource type parameter
     */
    public void setParamNewResourceType(String newResourceType) {

        m_paramNewResourceType = newResourceType;
    }

    /**
     * Closes the dialog and forwards to the previed page in direct editor mode.<p>
     *
     * @throws IOException if forwarding fails
     * @throws ServletException if forwarding fails
     */
    protected void actionClose() throws IOException, ServletException {

        // editor is in direct edit mode
        if (CmsStringUtil.isNotEmpty(getParamBackLink())) {
            // set link to the specified back link target
            setParamCloseLink(getJsp().link(getParamBackLink()));
        } else {
            // set link to the edited resource
            setParamCloseLink(getJsp().link(getParamResource()));
        }
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        // load the common JSP close dialog
        sendForward(FILE_DIALOG_CLOSE, Collections.<String, String[]> emptyMap());
    }

    /**
     * @see org.opencms.workplace.list.A_CmsSelectResourceList#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // get listed model files
        List<CmsListItem> modelFiles = getList().getAllContent();
        Iterator<CmsListItem> i = modelFiles.iterator();
        while (i.hasNext()) {
            CmsListItem item = i.next();
            String resName = (String)item.get(LIST_COLUMN_NAME);
            String description = "";
            if (detailId.equals(LIST_DETAIL_DESCRIPTION)) {
                // set description detail
                try {
                    description = getJsp().property(CmsPropertyDefinition.PROPERTY_DESCRIPTION, resName, "");
                } catch (Exception e) {
                    // ignore it, because the dummy file throws an exception in any case
                }
            } else {
                continue;
            }
            item.set(detailId, description);
        }

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#isColumnVisible(int)
     */
    @Override
    protected boolean isColumnVisible(int colFlag) {

        // only show icon, name, title and datelastmodified columns
        if (colFlag == LIST_COLUMN_TYPEICON.hashCode()) {
            return true;
        }
        if (colFlag == LIST_COLUMN_NAME.hashCode()) {
            return true;
        }
        if (colFlag == CmsUserSettings.FILELIST_TITLE) {
            return true;
        }
        if (colFlag == CmsUserSettings.FILELIST_DATE_LASTMODIFIED) {
            return true;
        }
        // other columns are hidden
        return false;
    }

    /**
     * Removes the default "preview file" action from the file name column.<p>
     *
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        super.setColumns(metadata);

        Iterator<CmsListColumnDefinition> it = metadata.getColumnDefinitions().iterator();
        while (it.hasNext()) {
            CmsListColumnDefinition colDefinition = it.next();
            if (colDefinition.getId().equals(LIST_COLUMN_NAME)) {

                // remove default "preview file" action from file name column
                colDefinition.removeDefaultAction(LIST_DEFACTION_OPEN);
            } else if (colDefinition.getId().equals(LIST_COLUMN_TYPEICON)) {

                // remove sorting on icon column
                colDefinition.setSorteable(false);

                // remove column name
                colDefinition.setName(
                    org.opencms.workplace.list.Messages.get().container(
                        org.opencms.workplace.list.Messages.GUI_EXPLORER_LIST_COLS_EMPTY_0));
            }
        }

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // create list item detail: description
        CmsListItemDetails modelFileDescription = new CmsListItemDetails(LIST_DETAIL_DESCRIPTION);
        modelFileDescription.setAtColumn(LIST_COLUMN_NAME);
        modelFileDescription.setVisible(false);
        modelFileDescription.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_MODELFILES_LABEL_DESCRIPTION_0)));
        modelFileDescription.setShowActionName(
            Messages.get().container(Messages.GUI_MODELFILES_DETAIL_SHOW_DESCRIPTION_NAME_0));
        modelFileDescription.setShowActionHelpText(
            Messages.get().container(Messages.GUI_MODELFILES_DETAIL_SHOW_DESCRIPTION_HELP_0));
        modelFileDescription.setHideActionName(
            Messages.get().container(Messages.GUI_MODELFILES_DETAIL_HIDE_DESCRIPTION_NAME_0));
        modelFileDescription.setHideActionHelpText(
            Messages.get().container(Messages.GUI_MODELFILES_DETAIL_HIDE_DESCRIPTION_HELP_0));

        metadata.addItemDetails(modelFileDescription);

    }

}
