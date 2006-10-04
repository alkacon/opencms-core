/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsDeleteBrokenRelationsList.java,v $
 * Date   : $Date: 2006/10/04 16:01:51 $
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

import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationDeleteValidator;
import org.opencms.site.CmsSiteManager;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;
import org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum;
import org.opencms.workplace.tools.CmsToolMacroResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Session list for broadcasting messages.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsDeleteBrokenRelationsList extends A_CmsListDialog {

    /** The delimiter that is used in the resource list request parameter. */
    public static final String DELIMITER_RESOURCES = "|";

    /** list column id constant. */
    public static final String LIST_COLUMN_RESOURCE = "cr";

    /** list action id constant. */
    public static final String LIST_DETAIL_RELATIONS = "dr";

    /** list id constant. */
    public static final String LIST_ID = "dbr";

    /** Request parameter name for the deletesiblings parameter. */
    public static final String PARAM_DELETE_SIBLINGS = "deletesiblings";

    /** Request parameter name for the resource list. */
    public static final String PARAM_RESOURCELIST = "resourcelist";

    /** The delete siblings parameter value. */
    private String m_deleteSiblings;

    /** The broken relations validator object. */
    private CmsRelationDeleteValidator m_validator;

    /** The resourcelist parameter value. */
    private String m_paramResourcelist;

    /** The list of resource names for the multi operation. */
    private List m_resourceList;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsDeleteBrokenRelationsList(CmsJspActionElement jsp) {

        super(
            jsp,
            LIST_ID,
            Messages.get().container(Messages.GUI_DELETE_BROKENRELATIONS_LIST_NAME_0),
            LIST_COLUMN_RESOURCE,
            CmsListOrderEnum.ORDER_ASCENDING,
            LIST_COLUMN_RESOURCE);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDeleteBrokenRelationsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.tools.CmsToolDialog#dialogTitle()
     */
    public String dialogTitle() {

        // build title
        StringBuffer html = new StringBuffer(512);
        CmsMessages message = org.opencms.workplace.list.Messages.get().getBundle(getLocale());
        html.append("<div class='screenTitle'>\n");
        html.append("\t<table width='100%' cellspacing='0'>\n");
        html.append("\t\t<tr>\n");
        html.append("\t\t\t<td>\n");
        html.append(getList().getName().key(getLocale()));
        html.append("\n\t\t\t</td>");
        html.append("\t\t\t<td class='uplevel'>\n\t\t\t\t");
        html.append(A_CmsHtmlIconButton.defaultButtonHtml(
            getJsp(),
            CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
            "id-print",
            message.key(org.opencms.workplace.list.Messages.GUI_ACTION_PRINT_NAME_0),
            message.key(org.opencms.workplace.list.Messages.GUI_ACTION_PRINT_HELP_0),
            true,
            "list/print.png",
            null,
            "print();"));
        html.append("\n\t\t\t</td>\n");
        html.append("\t\t</tr>\n");
        html.append("\t</table>\n");
        html.append("</div>\n");

        return CmsToolMacroResolver.resolveMacros(html.toString(), this);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    public void executeListMultiActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() {

        throwListUnsupportedActionException();
    }

    /**
     * Generates the printable output for the given list.<p>
     * 
     * @return html code
     */
    public String generateHtml() {

        StringBuffer result = new StringBuffer(2048);
        result.append(htmlStart(null));
        result.append(bodyStart("dialog", null));
        result.append(dialogStart());
        result.append(dialogContentStart(getParamTitle()));
        result.append(getList().printableHtml());
        result.append(dialogContentEnd());
        result.append(dialogEnd());
        result.append(bodyEnd());
        result.append(htmlEnd());
        return result.toString();
    }

    /**
     * Returns the broken relations validator object.<p>
     * 
     * @return a validator object
     */
    public CmsRelationDeleteValidator getValidator() {

        if (m_validator == null) {
            m_validator = new CmsRelationDeleteValidator(getCms(), getResourceList(), Boolean.valueOf(
                getParamDeleteSiblings()).booleanValue());
        }
        return m_validator;
    }

    /**
     * Returns the value of the boolean option to delete siblings.<p>
     * 
     * @return the value of the boolean option to delete siblings as a lower case string
     */
    public String getParamDeleteSiblings() {

        return m_deleteSiblings;
    }

    /**
     * Returns the value of the resourcelist parameter, or null if the parameter is not provided.<p>
     * 
     * This parameter selects the resources to perform operations on.<p>
     *  
     * @return the value of the resourcelist parameter or null, if the parameter is not provided
     */
    public String getParamResourcelist() {

        if (CmsStringUtil.isNotEmpty(m_paramResourcelist) && !"null".equals(m_paramResourcelist)) {
            return m_paramResourcelist;
        } else {
            return null;
        }
    }

    /**
     * Returns the resources that are defined for the dialog operation.<p>
     * 
     * For single resource operations, the list contains one item: the resource name found 
     * in the request parameter value of the "resource" parameter.<p>
     * 
     * @return the resources that are defined for the dialog operation
     */
    public List getResourceList() {

        if (m_resourceList == null) {
            // use lazy initializing
            if (getParamResourcelist() != null) {
                // found the resourcelist parameter
                m_resourceList = CmsStringUtil.splitAsList(getParamResourcelist(), DELIMITER_RESOURCES, true);
                Collections.sort(m_resourceList);
            } else {
                // this is a single resource operation, create list containing the resource name
                m_resourceList = new ArrayList(1);
                m_resourceList.add(getParamResource());
            }
        }
        return m_resourceList;
    }

    /**
     * Sets the value of the boolean option to delete siblings.<p>
     * 
     * @param value the value of the boolean option to delete siblings
     */
    public void setParamDeleteSiblings(String value) {

        m_deleteSiblings = value;
        m_validator = null;
    }

    /**
     * Sets the value of the resourcelist parameter.<p>
     * 
     * @param paramResourcelist the value of the resourcelist parameter
     */
    public void setParamResourcelist(String paramResourcelist) {

        m_paramResourcelist = paramResourcelist;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        // get content
        List resourceNames = getList().getAllContent();
        Iterator itResourceNames = resourceNames.iterator();
        while (itResourceNames.hasNext()) {
            CmsListItem item = (CmsListItem)itResourceNames.next();
            String resourceName = item.getId();

            StringBuffer html = new StringBuffer(128);
            if (detailId.equals(LIST_DETAIL_RELATIONS)) {
                // relations
                CmsRelationDeleteValidator.InfoEntry infoEntry = getValidator().getInfoEntry(resourceName);
                Iterator itRelations = infoEntry.getRelations().iterator();

                // show all links that will get broken
                while (itRelations.hasNext()) {
                    CmsRelation relation = (CmsRelation)itRelations.next();
                    String relationName = relation.getSourcePath();
                    if (relationName.startsWith(infoEntry.getSiteRoot())) {
                        // same site
                        relationName = relationName.substring(infoEntry.getSiteRoot().length());
                    } else {
                        // other site
                        String site = CmsSiteManager.getSiteRoot(relationName);
                        String siteName = site;
                        if (site != null) {
                            relationName = relationName.substring(site.length());
                            siteName = CmsSiteManager.getSite(site).getTitle();
                        } else {
                            siteName = "/";
                        }
                        relationName = key(Messages.GUI_DELETE_SITE_RELATION_2, new Object[] {siteName, relationName});
                    }
                    html.append(relationName);
                    html.append("&nbsp;<span style='color: #666666;'>(");
                    html.append(relation.getType().getLocalizedName(getLocale()));
                    html.append(")</span>");
                    if (itRelations.hasNext()) {
                        html.append("<br>");
                    }
                    html.append("\n");
                }
            } else {
                continue;
            }
            item.set(detailId, html.toString());
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() {

        List ret = new ArrayList();
        // get content
        // sort the resulting hash map
        List resourceList = new ArrayList(getValidator().keySet());
        Collections.sort(resourceList);
        Iterator itResources = resourceList.iterator();
        while (itResources.hasNext()) {
            String resourceName = (String)itResources.next();
            CmsRelationDeleteValidator.InfoEntry infoEntry = getValidator().getInfoEntry(resourceName);
            String resName = null;
            if (infoEntry.isSibling()) {
                if (!infoEntry.isInOtherSite()) {
                    resName = key(Messages.GUI_DELETE_SIBLING_RELATION_1, new Object[] {infoEntry.getResourceName()});
                } else {
                    String siblingName = key(
                        Messages.GUI_DELETE_SIBLING_RELATION_1,
                        new Object[] {infoEntry.getResourceName()});
                    resName = key(Messages.GUI_DELETE_SITE_RELATION_2, new Object[] {
                        infoEntry.getSiteName(),
                        siblingName});
                }
            } else {
                resName = getCms().getRequestContext().removeSiteRoot(resourceName);
            }

            CmsListItem item = getList().newItem(resourceName);
            item.set(LIST_COLUMN_RESOURCE, "<b>" + resName + "/<b>");
            ret.add(item);
        }
        return ret;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
        if (getParamDeleteSiblings() == null) {
            setParamDeleteSiblings(Boolean.toString(getSettings().getUserSettings().getDialogDeleteFileMode() == CmsResource.DELETE_REMOVE_SIBLINGS));
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        // create column for resources
        CmsListColumnDefinition resourcesCol = new CmsListColumnDefinition(LIST_COLUMN_RESOURCE);
        resourcesCol.setName(Messages.get().container(Messages.GUI_DELETE_BROKENRELATIONS_LIST_COLS_RESOURCE_0));
        resourcesCol.setHelpText(Messages.get().container(Messages.GUI_DELETE_BROKENRELATIONS_LIST_COLS_RESOURCE_HELP_0));
        resourcesCol.setWidth("100%");
        resourcesCol.setSorteable(false);

        metadata.addColumn(resourcesCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // create list item detail
        CmsListItemDetails relationsDetails = new CmsListItemDetails(LIST_DETAIL_RELATIONS);
        relationsDetails.setAtColumn(LIST_COLUMN_RESOURCE);
        relationsDetails.setVisible(true);
        relationsDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_DELETE_BROKENRELATIONS_LABEL_RELATIONS_0)));
        relationsDetails.setShowActionName(Messages.get().container(
            Messages.GUI_DELETE_BROKENRELATIONS_DETAIL_SHOW_RELATIONS_NAME_0));
        relationsDetails.setShowActionHelpText(Messages.get().container(
            Messages.GUI_DELETE_BROKENRELATIONS_DETAIL_SHOW_RELATIONS_HELP_0));
        relationsDetails.setHideActionName(Messages.get().container(
            Messages.GUI_DELETE_BROKENRELATIONS_DETAIL_HIDE_RELATIONS_NAME_0));
        relationsDetails.setHideActionHelpText(Messages.get().container(
            Messages.GUI_DELETE_BROKENRELATIONS_DETAIL_HIDE_RELATIONS_HELP_0));

        // add resources info item detail to meta data
        metadata.addItemDetails(relationsDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // noop
    }
}
