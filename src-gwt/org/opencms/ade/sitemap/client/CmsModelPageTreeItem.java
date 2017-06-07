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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.sitemap.client;

import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.hoverbar.CmsEditModelPageMenuEntry;
import org.opencms.ade.sitemap.client.ui.css.I_CmsSitemapLayoutBundle;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsModelPageEntry;
import org.opencms.gwt.client.property.CmsReloadMode;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

/**
 * Tree item for the model page editor mode.<p>
 */
public class CmsModelPageTreeItem extends CmsTreeItem {

    /**
     * List item widget that displays additional infos dynamically.<p>
     */
    protected class CmsModelPageListItemWidget extends CmsListItemWidget {

        /**
         * Constructor.<p>
         *
         * @param infoBean the data to display
         */
        public CmsModelPageListItemWidget(CmsListInfoBean infoBean) {

            super(infoBean);
            ensureOpenCloseAdditionalInfo();
        }

    }

    /** The folder entry id. */
    private CmsUUID m_entryId;

    /** The parent model flag. */
    private boolean m_isParentModel;

    /** The model group flag. */
    private boolean m_isModelGroup;

    /** The disabled flag. */
    private boolean m_disabled;

    /**
     * Creates the fake model page tree item used as a root for the tree view.<p>
     *
     * @param isModelGroup in case of a model group page
     * @param title the title
     * @param subTitle the sub title
     */
    public CmsModelPageTreeItem(boolean isModelGroup, String title, String subTitle) {

        super(true);
        m_isModelGroup = isModelGroup;
        CmsListInfoBean infoBean = new CmsListInfoBean(title, subTitle, null);
        CmsListItemWidget content = new CmsListItemWidget(infoBean);
        content.setIcon(
            CmsIconUtil.getResourceIconClasses(
                isModelGroup ? CmsGwtConstants.TYPE_MODELGROUP_PAGE : CmsGwtConstants.TYPE_MODELPAGE,
                false));
        initContent(content);
    }

    /**
     * Constructor.<p>
     *
     * @param modelpage the model page
     * @param isModelGroup in case of a model group page
     * @param isParentModel the parent model flag
     */
    public CmsModelPageTreeItem(CmsModelPageEntry modelpage, boolean isModelGroup, boolean isParentModel) {

        super(true);
        m_isModelGroup = isModelGroup;
        initContent(createListWidget(modelpage));
        m_entryId = modelpage.getStructureId();
        m_isParentModel = isParentModel;
        setDisabled(modelpage.isDisabled());
    }

    /**
     * Creates the fake model page tree item used as a root for the tree view.<p>
     *
     * @param isModelGroup in case of a model group page
     * @param title the title
     * @param subTitle the sub title
     *
     * @return the root tree item
     */
    public static CmsModelPageTreeItem createRootItem(boolean isModelGroup, String title, String subTitle) {

        return new CmsModelPageTreeItem(isModelGroup, title, subTitle);
    }

    /**
     * Returns the folder entry id.<p>
     *
     * @return the folder entry id
     */
    public CmsUUID getEntryId() {

        return m_entryId;
    }

    /**
     * Returns the site path.<p>
     *
     * @return the site path
     */
    public String getSitePath() {

        // the site path is displayed as the sub title
        return getListItemWidget().getSubtitleLabel();
    }

    /**
     * Returns if the model page entry is disabled.<p>
     *
     * @return <code>true</code> if the model page entry is disabled
     */
    public boolean isDisabled() {

        return m_disabled;
    }

    /**
     * Returns whether the entry represents a model group page.<p>
     *
     * @return <code>true</code> if the entry represents a model group page
     */
    public boolean isModelGroup() {

        return m_isModelGroup;
    }

    /**
     * Returns if this model page entry is inherited from the parent configuration.<p>
     *
     * @return <code>true</code> if this model page entry is inherited from the parent configuration
     */
    public boolean isParentModel() {

        return m_isParentModel;
    }

    /**
     * Sets the model page entry disabled.<p>
     *
     * @param disabled <code>true</Code> to disable
     */
    public void setDisabled(boolean disabled) {

        m_disabled = disabled;
        if (m_disabled) {
            getListItemWidget().addStyleName(I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().hiddenNavEntry());
        } else {
            getListItemWidget().removeStyleName(I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().hiddenNavEntry());
        }
    }

    /**
     * Updates the site path info.<p>
     *
     * @param sitePath the new site path
     */
    public void updateSitePath(String sitePath) {

        getListItemWidget().setSubtitleLabel(sitePath);
    }

    /**
     * Handles direct editing of the gallery title.<p>
     *
     * @param editEntry the edit entry
     * @param newTitle the new title
     */
    void handleEdit(CmsClientSitemapEntry editEntry, final String newTitle) {

        if (CmsStringUtil.isEmpty(newTitle)) {
            String dialogTitle = Messages.get().key(Messages.GUI_EDIT_TITLE_ERROR_DIALOG_TITLE_0);
            String dialogText = Messages.get().key(Messages.GUI_TITLE_CANT_BE_EMPTY_0);
            CmsAlertDialog alert = new CmsAlertDialog(dialogTitle, dialogText);
            alert.center();
            return;
        }
        String oldTitle = editEntry.getPropertyValue(CmsClientProperty.PROPERTY_TITLE);
        if (!oldTitle.equals(newTitle)) {
            CmsPropertyModification propMod = new CmsPropertyModification(
                getEntryId(),
                CmsClientProperty.PROPERTY_TITLE,
                newTitle,
                true);
            final List<CmsPropertyModification> propChanges = new ArrayList<CmsPropertyModification>();
            propChanges.add(propMod);
            CmsSitemapController controller = CmsSitemapView.getInstance().getController();
            controller.edit(editEntry, propChanges, CmsReloadMode.reloadEntry);
        }
    }

    /**
     * Creates the list item widget for the given folder.<p>
     *
     * @param modelPage the model page bean
     *
     * @return the list item widget
     */
    private CmsListItemWidget createListWidget(final CmsModelPageEntry modelPage) {

        CmsListItemWidget result = new CmsModelPageListItemWidget(modelPage.getListInfoBean());
        if (modelPage.isDefault()) {
            Widget label = result.getShortExtraInfoLabel();
            label.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().subtitleSuffix());
            result.setExtraInfo("(*)");
        }
        result.setIcon(
            CmsIconUtil.getResourceIconClasses(
                m_isModelGroup ? CmsGwtConstants.TYPE_MODELGROUP_PAGE : CmsGwtConstants.TYPE_MODELPAGE,
                modelPage.getRootPath(),
                false));
        if (m_isModelGroup || CmsEditModelPageMenuEntry.checkVisible(modelPage.getStructureId())) {
            result.addIconClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    CmsEditModelPageMenuEntry.editModelPage(modelPage.getSitePath(), isModelGroup());
                }
            });
        }
        return result;
    }
}
