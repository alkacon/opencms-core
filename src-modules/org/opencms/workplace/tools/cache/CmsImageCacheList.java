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

package org.opencms.workplace.tools.cache;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOpenResourceAction;
import org.opencms.workplace.list.CmsListOrderEnum;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Image Cache content view.<p>
 *
 * @since 7.0.5
 */
public class CmsImageCacheList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ICON = "ai";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_LENGTH = "cl";

    /** list column id constant. */
    public static final String LIST_COLUMN_RESOURCE = "cr";

    /** list column id constant. */
    public static final String LIST_COLUMN_SIZE = "cs";

    /** List default action id constant. */
    public static final String LIST_DEFACTION_OPEN = "edo";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_SIZE = "ds";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_VARIATIONS = "dv";

    /** list id constant. */
    public static final String LIST_ID = "lfc";

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsImageCacheList(CmsJspActionElement jsp) {

        super(
            jsp,
            LIST_ID,
            Messages.get().container(Messages.GUI_IMAGECACHE_LIST_NAME_0),
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
    public CmsImageCacheList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlStart()
     */
    @Override
    public String defaultActionHtmlStart() {

        return getList().listJs() + dialogContentStart(getParamTitle());
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() throws CmsRuntimeException {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() {

        throwListUnsupportedActionException();
    }

    /**
     * Checks if the image size should be shown or not.<p>
     *
     * @return <code>true</code> if the image size should be shown
     */
    public boolean showSize() {

        CmsListItemDetails details = getList().getMetadata().getItemDetailDefinition(LIST_DETAIL_SIZE);
        return (details != null) && details.isVisible();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // get content
        CmsImageCacheHelper helper = new CmsImageCacheHelper(getCms(), true, showSize(), false);

        List entries = getList().getAllContent();
        Iterator itEntries = entries.iterator();
        while (itEntries.hasNext()) {
            CmsListItem item = (CmsListItem)itEntries.next();
            String resName = item.getId();
            StringBuffer html = new StringBuffer(512);
            try {
                // variations
                Iterator itVariations = helper.getVariations(resName).iterator();
                while (itVariations.hasNext()) {
                    String var = (String)itVariations.next();
                    html.append(var);
                    if (itVariations.hasNext()) {
                        html.append("<br>");
                    }
                    html.append("\n");
                }
            } catch (Exception e) {
                // ignore
            }
            item.set(LIST_DETAIL_VARIATIONS, html.toString());
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List getListItems() {

        List ret = new ArrayList();
        boolean showSize = showSize();
        getList().getMetadata().getColumnDefinition(LIST_COLUMN_SIZE).setVisible(showSize);
        String width = "80%";
        if (showSize) {
            width = "60%";
        }
        getList().getMetadata().getColumnDefinition(LIST_COLUMN_RESOURCE).setWidth(width);

        // get content
        CmsImageCacheHelper helper = new CmsImageCacheHelper(getCms(), false, showSize, false);
        Iterator itResources = helper.getAllCachedImages().iterator();
        while (itResources.hasNext()) {
            String resource = (String)itResources.next();
            CmsListItem item = getList().newItem(resource);
            String resName = resource;
            item.set(LIST_COLUMN_RESOURCE, resName);
            if (showSize) {
                item.set(LIST_COLUMN_SIZE, helper.getSize(resName));
            }
            item.set(LIST_COLUMN_LENGTH, helper.getLength(resName));
            ret.add(item);
        }

        return ret;
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
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // create column for icon display
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        iconCol.setName(Messages.get().container(Messages.GUI_IMAGECACHE_LIST_COLS_ICON_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setSorteable(false);

        CmsListDirectAction iconAction = new CmsListDirectAction(LIST_ACTION_ICON);
        iconAction.setName(Messages.get().container(Messages.GUI_IMAGECACHE_LIST_ACTION_ICON_NAME_0));
        iconAction.setIconPath("tools/cache/buttons/imageentry.png");
        iconAction.setEnabled(false);
        iconCol.addDirectAction(iconAction);
        // add it to the list definition
        metadata.addColumn(iconCol);

        // create column for resource name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_RESOURCE);
        nameCol.setName(Messages.get().container(Messages.GUI_IMAGECACHE_LIST_COLS_RESOURCE_0));
        nameCol.setWidth("60%");
        // add resource open action
        CmsListDefaultAction resourceOpenDefAction = new CmsListOpenResourceAction(
            LIST_DEFACTION_OPEN,
            LIST_COLUMN_RESOURCE);
        resourceOpenDefAction.setEnabled(true);
        nameCol.addDefaultAction(resourceOpenDefAction);

        // add it to the list definition
        metadata.addColumn(nameCol);

        // create column for size
        CmsListColumnDefinition sizeCol = new CmsListColumnDefinition(LIST_COLUMN_SIZE);
        sizeCol.setName(Messages.get().container(Messages.GUI_IMAGECACHE_LIST_COLS_SIZE_0));
        sizeCol.setWidth("20%");
        // add it to the list definition
        metadata.addColumn(sizeCol);

        // create column for length
        CmsListColumnDefinition lengthCol = new CmsListColumnDefinition(LIST_COLUMN_LENGTH);
        lengthCol.setName(Messages.get().container(Messages.GUI_IMAGECACHE_LIST_COLS_LENGTH_0));
        lengthCol.setWidth("20%");
        // add it to the list definition
        metadata.addColumn(lengthCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add variations details
        CmsListItemDetails variationsDetails = new CmsListItemDetails(LIST_DETAIL_VARIATIONS);
        variationsDetails.setAtColumn(LIST_COLUMN_RESOURCE);
        variationsDetails.setVisible(false);
        variationsDetails.setShowActionName(
            Messages.get().container(Messages.GUI_IMAGECACHE_DETAIL_SHOW_VARIATIONS_NAME_0));
        variationsDetails.setShowActionHelpText(
            Messages.get().container(Messages.GUI_IMAGECACHE_DETAIL_SHOW_VARIATIONS_HELP_0));
        variationsDetails.setHideActionName(
            Messages.get().container(Messages.GUI_IMAGECACHE_DETAIL_HIDE_VARIATIONS_NAME_0));
        variationsDetails.setHideActionHelpText(
            Messages.get().container(Messages.GUI_IMAGECACHE_DETAIL_HIDE_VARIATIONS_HELP_0));
        variationsDetails.setName(Messages.get().container(Messages.GUI_IMAGECACHE_DETAIL_VARIATIONS_NAME_0));
        variationsDetails.setFormatter(
            new CmsListItemDetailsFormatter(
                Messages.get().container(Messages.GUI_IMAGECACHE_DETAIL_VARIATIONS_NAME_0)));
        metadata.addItemDetails(variationsDetails);

        // add size details
        CmsListItemDetails sizeDetails = new CmsListItemDetails(LIST_DETAIL_SIZE);
        sizeDetails.setShowActionName(Messages.get().container(Messages.GUI_IMAGECACHE_DETAIL_SHOW_SIZE_NAME_0));
        sizeDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_IMAGECACHE_DETAIL_SHOW_SIZE_HELP_0));
        sizeDetails.setHideActionName(Messages.get().container(Messages.GUI_IMAGECACHE_DETAIL_HIDE_SIZE_NAME_0));
        sizeDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_IMAGECACHE_DETAIL_HIDE_SIZE_HELP_0));
        sizeDetails.setName(Messages.get().container(Messages.GUI_IMAGECACHE_DETAIL_SIZE_NAME_0));
        sizeDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_IMAGECACHE_DETAIL_SIZE_NAME_0)));
        sizeDetails.setVisible(false);
        metadata.addItemDetails(sizeDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // no multi actions
    }
}
