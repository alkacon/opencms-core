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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.css.I_CmsConstantsBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.shared.CmsBrokenLinkBean;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget containing that links to a sitemap item which the user wants to delete will be broken.
 *
 * @since 8.0.0
 */
public class CmsLinkWarningPanel extends Composite {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    protected interface I_CmsLinkWarningPanelUiBinder extends UiBinder<Widget, CmsLinkWarningPanel> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsLinkWarningPanelUiBinder uiBinder = GWT.create(I_CmsLinkWarningPanelUiBinder.class);

    /** The label containing the warning that links will be broken. */
    @UiField
    protected Label m_label;

    /** The panel containing the links that will be broken. */
    @UiField
    protected CmsList<CmsTreeItem> m_linkPanel;

    /**
     * Default constructor.<p>
     */
    public CmsLinkWarningPanel() {

        initWidget(uiBinder.createAndBindUi(this));
        m_label.setText(Messages.get().key(Messages.GUI_BROKEN_LINK_TEXT_0));
    }

    /**
     * Fills the panel with the tree list of broken links.<p>
     *
     * @param brokenLinkBeans the beans representing the broken links
     */
    public void fill(List<CmsBrokenLinkBean> brokenLinkBeans) {

        for (CmsBrokenLinkBean brokenLinkBean : brokenLinkBeans) {
            m_linkPanel.add(createTreeItem(brokenLinkBean));
        }
    }

    /**
     * Helper method for creating a list item widget based on a bean.<p>
     *
     * @param brokenLinkBean the bean with the data for the list item widget
     *
     * @return the new list item widget
     */
    protected CmsListItemWidget createListItemWidget(CmsBrokenLinkBean brokenLinkBean) {

        CmsListInfoBean info = new CmsListInfoBean();
        String title = brokenLinkBean.getTitle();
        if ((title == null) || title.equals("")) {
            title = Messages.get().key(Messages.GUI_BROKEN_LINK_NO_TITLE_0);
        }
        info.setTitle(title);
        info.setSubTitle(brokenLinkBean.getSubTitle());
        String type = brokenLinkBean.getType();
        if (type != null) {
            info.setResourceType(type);
        }
        for (Map.Entry<String, String> entry : brokenLinkBean.getInfo().entrySet()) {
            info.addAdditionalInfo(entry.getKey(), entry.getValue());
        }
        final CmsListItemWidget widget = new CmsListItemWidget(info);
        widget.addAttachHandler(new AttachEvent.Handler() {

            public void onAttachOrDetach(AttachEvent event) {

                if (event.isAttached()) {
                    widget.truncateAdditionalInfo("addinfo", widget.getOffsetWidth());
                }
            }
        });
        return widget;
    }

    /**
     * Helper method for creating a tree item from a bean.<p>
     *
     * @param brokenLinkBean the bean containing the data for the tree item
     *
     * @return a tree item
     */
    protected CmsTreeItem createTreeItem(CmsBrokenLinkBean brokenLinkBean) {

        CmsListItemWidget itemWidget = createListItemWidget(brokenLinkBean);
        CmsTreeItem item = new CmsTreeItem(false, itemWidget);
        for (CmsBrokenLinkBean child : brokenLinkBean.getChildren()) {
            CmsListItemWidget childItemWidget = createListItemWidget(child);
            Widget warningImage = FontOpenCms.WARNING.getWidget(20, I_CmsConstantsBundle.INSTANCE.css().colorWarning());
            warningImage.addStyleName(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
            childItemWidget.addButton(warningImage);
            childItemWidget.addTitleStyleName(I_CmsLayoutBundle.INSTANCE.linkWarningCss().deletedEntryLabel());
            CmsTreeItem childItem = new CmsTreeItem(false, childItemWidget);
            item.addChild(childItem);
        }
        return item;
    }

}
