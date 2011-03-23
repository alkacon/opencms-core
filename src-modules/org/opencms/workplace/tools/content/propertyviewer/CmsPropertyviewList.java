/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/propertyviewer/CmsPropertyviewList.java,v $
 * Date   : $Date: 2011/03/23 14:52:02 $
 * Version: $Revision: 1.4 $
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

package org.opencms.workplace.tools.content.propertyviewer;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * A list that displays properties .
 * <p>
 * 
 * Caution: The list ID argument has to be dynamic to prevent caching causing exception in case of varying collumns.
 * <p>
 * 
 * @author Achim Westermann
 * @author Mario Jaeger
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 7.5.1
 */
public class CmsPropertyviewList extends A_CmsListDialog {

    /** Used for ID column formatting. */
    public static final NumberFormat ID_NUMBER_FORMAT = new DecimalFormat("00000");

    /** list action id constant. */
    public static final String LIST_ACTION_NONE = "an";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "lcic";

    /** list column id constant. */
    public static final String LIST_COLUMN_ID = "lcid";

    /** list column id constant. */
    public static final String LIST_COLUMN_PATH = "lcp";

    /** list column id constant. */
    public static final String LIST_COLUMN_PREFIX_PROPERTY = "cnp-";

    /** List detail all properties info. */
    public static final String LIST_DETAIL_ALL_PROPERTIES = "allpropertiesinfo";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_FULLPATH = "df";

    /** The request parameter for the properties to work on. */
    public static final String PARAM_PROPERTIES = "props";

    /** The request parameter for the paths to work on. */
    public static final String PARAM_RESOURCES = "paths";

    /** The request parameter for the paths to work on. */
    public static final String PARAM_SIBLINGS = "siblings";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPropertyviewList.class);

    /** Message for translation. */
    private CmsMessages m_messages;

    /** The paths. */
    private String[] m_paths;

    /** The properties. */
    private String[] m_props;

    /** Flag for showing siblings. */
    private boolean m_siblings;

    /**
     * Public constructor.
     * <p>
     * 
     * @param jsp an initialized JSP action element
     * 
     * @throws CmsException if something goes wrong.
     * @throws FileNotFoundException if something goes wrong.
     */
    public CmsPropertyviewList(CmsJspActionElement jsp)
    throws FileNotFoundException, CmsException {

        this(jsp, "proplist", Messages.get().container(Messages.GUI_LIST_PROPERTYVIEW_NAME_0));
    }

    /**
     * Public constructor.
     * <p>
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the list name
     * 
     * @throws CmsException if something goes wrong.
     * @throws FileNotFoundException if something goes wrong.
     */
    public CmsPropertyviewList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName)
    throws FileNotFoundException, CmsException {

        this(jsp, listId, listName, LIST_COLUMN_ID, CmsListOrderEnum.ORDER_ASCENDING, null);
    }

    /**
     * Public constructor.
     * <p>
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
    public CmsPropertyviewList(
        CmsJspActionElement jsp,
        String listId,
        CmsMessageContainer listName,
        String sortedColId,
        CmsListOrderEnum sortOrder,
        String searchableColId)
    throws FileNotFoundException, CmsException {

        super(jsp, listId, listName, sortedColId, sortOrder, searchableColId);
        m_messages = new CmsMessages(
            "org.opencms.workplace.tools.content.propertyviewer.messages",
            jsp.getRequestContext().getLocale());
    }

    /**
     * Public constructor.
     * <p>
     * 
     * Public constructor with JSP variables.
     * <p>
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     * 
     * @throws CmsException if something goes wrong.
     * @throws FileNotFoundException if something goes wrong.
     */
    public CmsPropertyviewList(final PageContext context, final HttpServletRequest req, final HttpServletResponse res)
    throws FileNotFoundException, CmsException {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @SuppressWarnings("unused")
    @Override
    public void executeListMultiActions() throws IOException, ServletException, CmsRuntimeException {

        // nothing to do
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @SuppressWarnings("unused")
    @Override
    public void executeListSingleActions() throws IOException, ServletException, CmsRuntimeException {

        // nothing to do
    }

    /**
     * @return the paths
     */
    public String getParamPaths() {

        return CmsStringUtil.arrayAsString(m_paths, ",");
    }

    /**
     * @return the props
     */
    public String getParamProps() {

        return CmsStringUtil.arrayAsString(m_props, ",");
    }

    /**
     * Returns true if siblings are shown.
     * <p>
     * 
     * @return true if siblings are shown.
     */
    public String getParamSiblings() {

        return Boolean.toString(m_siblings);
    }

    /**
     * @param paths the paths to set
     */
    public void setParamPaths(final String paths) {

        m_paths = CmsStringUtil.splitAsArray(paths, ',');
    }

    /**
     * @param props the props to set
     */
    public void setParamProps(final String props) {

        m_props = CmsStringUtil.splitAsArray(props, ',');
    }

    /**
     * Set if siblings should be shown.
     * <p>
     * 
     * @param showSiblings if siblings should be shown.
     */
    public void setParamSiblings(final String showSiblings) {

        m_siblings = Boolean.parseBoolean(showSiblings);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(final String detailId) {

        // nothing to do
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    @SuppressWarnings("unchecked")
    protected List getListItems() {

        List<CmsListItem> result = new ArrayList<CmsListItem>();
        // get content
        CmsListItem item;
        int idCounter = 0;
        CmsObject cms = getCms();
        for (CmsResource resource : getResources()) {
            item = getList().newItem(resource.getRootPath());
            fillItem(resource, item, false, idCounter);
            idCounter++;
            result.add(item);

            if (m_siblings) {
                try {
                    List<CmsResource> siblings = cms.readSiblings(cms.getSitePath(resource), CmsResourceFilter.ALL);
                    for (CmsResource sibling : siblings) {
                        // Don't render siblings that are in the path:
                        if (!isInPaths(sibling)) {
                            item = getList().newItem(sibling.getRootPath());
                            fillItem(sibling, item, true, idCounter);
                            idCounter++;
                            result.add(item);
                        }
                    }
                } catch (CmsException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().getBundle().key(
                            Messages.LOG_ERR_PROPERTYVIEWER_READSIBL_1,
                            resource.getRootPath()), e);
                    }
                }
            }
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
     * @see org.opencms.workplace.list.A_CmsListDialog#listRecovery(java.lang.String)
     */
    @Override
    protected synchronized void listRecovery(final String listId) {

        // nothing to do
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // enforce re-invocation of this method because columns are varying and must not be cached:
        metadata.setVolatile(true);

        // add column for icon
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        iconCol.setName(Messages.get().container(Messages.GUI_LIST_PROPERTYVIEW_COL_ICON_NAME_0));
        iconCol.setHelpText(Messages.get().container(Messages.GUI_LIST_PROPERTYVIEW_COL_ICON_HELP_0));
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        iconCol.setWidth("16");
        iconCol.setSorteable(false);
        metadata.addColumn(iconCol);
        iconCol.setPrintable(true);

        // add column for invisible ID (needed for sorting to show siblings below each other:
        CmsListColumnDefinition idCol = new CmsListColumnDefinition(LIST_COLUMN_ID);
        idCol.setName(Messages.get().container(Messages.GUI_LIST_PROPERTYVIEW_COL_ID_NAME_0));
        idCol.setHelpText(Messages.get().container(Messages.GUI_LIST_PROPERTYVIEW_COL_ID_HELP_0));
        idCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        idCol.setSorteable(false);
        idCol.setVisible(false);
        metadata.addColumn(idCol);
        idCol.setPrintable(true);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_PATH);
        nameCol.setName(Messages.get().container(Messages.GUI_LIST_PROPERTYVIEW_COL_PATH_NAME_0));
        nameCol.setHelpText(Messages.get().container(Messages.GUI_LIST_PROPERTYVIEW_COL_PATH_HELP_0));
        nameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        nameCol.setSorteable(false);
        metadata.addColumn(nameCol);
        nameCol.setPrintable(true);

        // add columns for properties:
        CmsListColumnDefinition propCol;
        for (String property : m_props) {
            propCol = new CmsListColumnDefinition(getPropertyColumnID(property));
            propCol.setName(Messages.get().container(
                Messages.GUI_LIST_PROPERTYVIEW_COL_PROPERTY_NAME_1,
                new Object[] {property}));
            propCol.setHelpText(Messages.get().container(Messages.GUI_LIST_PROPERTYVIEW_COL_PROPERTY_HELP_0));
            propCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
            propCol.setSorteable(false);
            metadata.addColumn(propCol);
            propCol.setPrintable(true);
        }

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // nothing to do here
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(final CmsListMetadata metadata) {

        // do nothing here
    }

    /**
     * Fills a single item.
     * <p>
     * @param resource the corresponding resource.
     * @param item the item to fill
     * @param isSibling if false no boldface markup will be marked.
     * @param id used for the ID column.
     */
    private void fillItem(final CmsResource resource, final CmsListItem item, final boolean isSibling, final int id) {

        item.set(LIST_COLUMN_ID, ID_NUMBER_FORMAT.format(id));
        I_CmsResourceType type;
        CmsObject cms = getCms();
        String iconPath;
        String pathValue;
        String sitePath = cms.getSitePath(resource);
        if (!isSibling) {
            sitePath = "<b>" + sitePath + "</b>";
        }
        item.set(LIST_COLUMN_PATH, sitePath);
        for (String property : m_props) {
            CmsProperty prop;
            try {
                prop = cms.readPropertyObject(resource, property, false);
                if (prop.isNullProperty()) {
                    pathValue = m_messages.key("GUI_LIST_PROPERTYVIEW_NOTFOUND_0");
                } else {
                    pathValue = prop.getValue();
                }
                item.set(getPropertyColumnID(property), pathValue);

            } catch (CmsException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(
                        Messages.LOG_ERR_PROPERTYVIEWER_READONEPROP_2,
                        property,
                        resource.getRootPath()), e);
                }
                item.set(getPropertyColumnID(property), "n/a");
            }
        }

        type = OpenCms.getResourceManager().getResourceType(resource);
        iconPath = getSkinUri()
            + "filetypes/"
            + OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName()).getIcon();
        String iconImage;

        if (isSibling) {
            iconImage = "<div style=\"background-image: url("
                + iconPath
                + "); padding:0px;background-position: 0px; margin:0px; background-repeat: no-repeat;\">"
                + "<img height=\"16\" border=\"0\" width=\"16\" src=\""
                + getSkinUri()
                + "/explorer/link.gif\"/></div>";
        } else {
            iconImage = "<img src=\"" + iconPath + "\" alt=\"icon\" />";
        }
        item.set(LIST_COLUMN_ICON, iconImage);
    }

    /**
     * Returns the list column constant for the given property.
     * <p>
     * @param property the property that will be shown in the column.
     *
     * @return the list column constant for the given property.
     */
    private String getPropertyColumnID(final String property) {

        String result = LIST_COLUMN_PREFIX_PROPERTY + property;
        return result;
    }

    /**
     * Internally reads the resources to use.<p>
     * 
     * @return the resources to use. 
     */
    @SuppressWarnings("unchecked")
    private List<CmsResource> getResources() {

        List<CmsResource> result = new LinkedList<CmsResource>();
        CmsObject cms = getCms();
        CmsResourceFilter filter = CmsResourceFilter.ALL;
        try {
            for (String path : m_paths) {
                List<CmsResource> resources = cms.readResources(path, filter, true);
                result.addAll(resources);
            }
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_ERR_PROPERTYVIEWER_READRESOURCES_0), e);
            }
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Checks if the resource is in the selected path.<p>
     * 
     * @param resource the resource to check
     * 
     * @return true, if the resource is in the selected path, otherwise false
     */
    private boolean isInPaths(final CmsResource resource) {

        boolean result = false;
        String resourcePath = getCms().getSitePath(resource);
        for (String path : m_paths) {
            if (resourcePath.startsWith(path)) {
                result = true;
                break;
            }
        }
        return result;

    }

}
