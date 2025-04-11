/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.gwt.client.ui.resourceinfo;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.CmsEditableData;
import org.opencms.gwt.client.I_CmsDescendantResizeHandler;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsFieldSet;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.CmsSimpleListItem;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.contenteditor.CmsContentEditorDialog;
import org.opencms.gwt.client.ui.contenteditor.CmsContentEditorDialog.DialogOptions;
import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuButton;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuHandler;
import org.opencms.gwt.client.ui.contextmenu.CmsLogout;
import org.opencms.gwt.client.ui.contextmenu.CmsShowPage;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsResourceStatusBean;
import org.opencms.gwt.shared.CmsResourceStatusRelationBean;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Widget which shows which contents refer to a resource. <p>
 */
public class CmsResourceRelationView extends Composite implements I_CmsDescendantResizeHandler {

    /** Enum for the display mode. */
    public enum Mode {
        /** Display siblings. */
        siblings,
        /** Display relation sources. */
        sources,

        /** Display relation targets. */
        targets
    }

    /** Set of context menu actions which we do not want to appear in the context menu for the relation source items. */
    protected static Set<String> m_filteredActions = new HashSet<String>();

    /** The detail container path pattern. */
    private static final String DETAIL_CONTAINER_PATTERN = ".*\\/\\.detailContainers\\/.*";

    static {
        m_filteredActions.add(CmsGwtConstants.ACTION_TEMPLATECONTEXTS);
        m_filteredActions.add(CmsGwtConstants.ACTION_EDITSMALLELEMENTS);
        m_filteredActions.add(CmsGwtConstants.ACTION_SELECTELEMENTVIEW);
        m_filteredActions.add(CmsLogout.class.getName());
    }

    /** The panel containing the resource boxes. */
    protected CmsList<CmsListItem> m_list;

    /** List for relations from other sites. */
    protected CmsList<CmsListItem> m_otherSitesList;

    /** Main panel. */
    protected FlowPanel m_panel = new FlowPanel();

    /** The popup which contains this widget. */
    protected CmsPopup m_popup;

    /** The dialog scroll panels. */
    List<CmsScrollPanel> m_scrollPanels;

    /** The edit button. */
    private CmsPushButton m_editButton;

    /** The context menu handler. */
    private CmsContextMenuHandler m_menuHandler;

    /** The display mode. */
    private Mode m_mode;

    /** The resource status from which we get the related resources to display. */
    private CmsResourceStatusBean m_statusBean;

    /**
     * Creates a new widget instance.<p>
     *
     * @param status the resource status from which we get the related resources to display.
     * @param mode the display mode (display relation sources or targets)
     * @param menuHandler the context menu handler
     */
    public CmsResourceRelationView(CmsResourceStatusBean status, Mode mode, CmsContextMenuHandler menuHandler) {

        initWidget(m_panel);
        m_menuHandler = menuHandler;
        m_scrollPanels = new ArrayList<CmsScrollPanel>();
        m_statusBean = status;
        m_mode = mode;
        initContent(status);
    }

    /**
     * Initializes the content.<p>
     *
     * @param status the status data
     */
    public void initContent(CmsResourceStatusBean status) {

        m_statusBean = status;
        m_panel.clear();
        m_scrollPanels.clear();

        // wrap list info item in another panel to achieve layout uniformity with other similar widgets
        SimplePanel infoBoxPanel = new SimplePanel();
        infoBoxPanel.getElement().getStyle().setMarginTop(2, Style.Unit.PX);
        CmsListItemWidget infoWidget = new CmsListItemWidget(status.getListInfo());
        infoWidget.addOpenHandler(new OpenHandler<CmsListItemWidget>() {

            public void onOpen(OpenEvent<CmsListItemWidget> event) {

                CmsDomUtil.resizeAncestor(getParent());
            }
        });
        infoWidget.addCloseHandler(new CloseHandler<CmsListItemWidget>() {

            public void onClose(CloseEvent<CmsListItemWidget> event) {

                CmsDomUtil.resizeAncestor(getParent());
            }
        });
        CmsContextMenuButton menuButton = new CmsContextMenuButton(
            status.getStructureId(),
            m_menuHandler,
            AdeContext.resourceinfo);
        menuButton.addStyleName(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
        infoWidget.addButton(menuButton);
        m_panel.add(infoBoxPanel);
        infoBoxPanel.add(infoWidget);
        m_list = createList(getLegend());
        fill();
    }

    /**
     * @see org.opencms.gwt.client.I_CmsDescendantResizeHandler#onResizeDescendant()
     */
    public void onResizeDescendant() {

        Timer timer = new Timer() {

            @Override
            public void run() {

                for (CmsScrollPanel panel : m_scrollPanels) {
                    panel.onResizeDescendant();
                }

            }
        };
        timer.schedule(100);
    }

    /**
     * Sets the popup which contains this widget.<p>
     *
     * @param popup the popup
     */
    public void setPopup(CmsPopup popup) {

        m_popup = popup;
    }

    /**
     * Creates and renders the resource boxes for the related resources.<p>
     */
    protected void fill() {

        m_list.clear();
        List<CmsResourceStatusRelationBean> relationBeans = getRelationBeans();
        if (relationBeans.isEmpty()) {
            CmsSimpleListItem item = new CmsSimpleListItem();
            item.add(new Label(getEmptyMessage()));
            m_list.add(item);
        } else {
            for (CmsResourceStatusRelationBean relationBean : relationBeans) {
                CmsListItemWidget itemWidget = new CmsListItemWidget(relationBean.getInfoBean());
                CmsListItem item = new CmsListItem(itemWidget);
                CmsContextMenuButton button = new CmsContextMenuButton(
                    relationBean.getStructureId(),
                    m_menuHandler,
                    AdeContext.resourceinfo);
                item.getListItemWidget().addButton(button);
                final CmsResourceStatusRelationBean currentRelationBean = relationBean;
                final boolean isContainerpage = CmsGwtConstants.TYPE_CONTAINERPAGE.equals(
                    relationBean.getInfoBean().getResourceType());
                final boolean isXmlContent = relationBean.isXmlContent();
                final boolean isEditable = (isXmlContent || isContainerpage)
                    && relationBean.getPermissionInfo().hasWritePermission()
                    && !currentRelationBean.getSitePath().matches(DETAIL_CONTAINER_PATTERN);
                if (isEditable) {

                    m_editButton = new CmsPushButton();
                    m_editButton.setImageClass(I_CmsButton.PEN_SMALL);
                    m_editButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
                    m_editButton.setTitle(
                        org.opencms.gwt.client.Messages.get().key(
                            org.opencms.gwt.client.Messages.GUI_BUTTON_ELEMENT_EDIT_0));
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(relationBean.getPermissionInfo().getNoEditReason())) {
                        m_editButton.disable(relationBean.getPermissionInfo().getNoEditReason());
                    } else {
                        m_editButton.setEnabled(true);
                    }
                    item.getListItemWidget().addButton(m_editButton);
                    m_editButton.addClickHandler(new ClickHandler() {

                        public void onClick(ClickEvent event) {

                            if (isContainerpage) {
                                Window.open(currentRelationBean.getLink(), "_self", "");
                            } else {
                                CmsEditableData editableData = new CmsEditableData();
                                editableData.setElementLanguage(CmsCoreProvider.get().getLocale());
                                editableData.setStructureId(currentRelationBean.getStructureId());
                                editableData.setSitePath(currentRelationBean.getSitePath());
                                CmsContentEditorDialog.get().openEditDialog(
                                    editableData,
                                    false,
                                    null,
                                    new DialogOptions(),
                                    new I_CmsContentEditorHandler() {

                                        public void onClose(
                                            String sitePath,
                                            CmsUUID structureId,
                                            boolean isNew,
                                            boolean hasChangedSettings,
                                            boolean usedPublishDialog) {

                                            if (m_popup != null) {
                                                m_popup.hide();
                                            }
                                        }
                                    });
                                ((CmsPushButton)event.getSource()).clearHoverState();
                            }
                        }
                    });
                }

                if (isContainerpage) {
                    CmsUUID id = relationBean.getStructureId();
                    if ((relationBean.getLink() != null) && (id != null)) {
                        itemWidget.setIconCursor(Cursor.POINTER);
                        itemWidget.addIconClickHandler(new ClickHandler() {

                            public void onClick(ClickEvent e) {

                                CmsShowPage showPage = new CmsShowPage();
                                showPage.execute(id);
                            }
                        });
                    }
                }
                m_list.add(item);
            }
        }
        if ((m_mode == Mode.sources) && !m_statusBean.getOtherSiteRelationSources().isEmpty()) {
            m_otherSitesList = createList(Messages.get().key(Messages.GUI_RESOURCEINFO_OTHERSITES_LEGEND_0));
            for (CmsResourceStatusRelationBean relationBean : m_statusBean.getOtherSiteRelationSources()) {
                CmsListItemWidget itemWidget = new CmsListItemWidget(relationBean.getInfoBean());
                CmsListItem item = new CmsListItem(itemWidget);
                m_otherSitesList.add(item);
            }
        }

        m_list.truncate("RES_INFO", CmsPopup.DEFAULT_WIDTH - 5);
        if (m_otherSitesList != null) {
            m_otherSitesList.truncate("RES_INFO", CmsPopup.DEFAULT_WIDTH - 5);
        }

    }

    /**
     * Creates a relation item list wrapped in a field set and appends it to the dialog panel.<p>
     *
     * @param label the list label
     *
     * @return the list
     */
    private CmsList<CmsListItem> createList(String label) {

        CmsFieldSet fieldset = new CmsFieldSet();
        CmsScrollPanel scrollPanel = GWT.create(CmsScrollPanel.class);
        CmsList<CmsListItem> list = new CmsList<CmsListItem>();
        scrollPanel.add(list);
        m_scrollPanels.add(scrollPanel);
        fieldset.getElement().getStyle().setMarginTop(10, Style.Unit.PX);
        scrollPanel.getElement().getStyle().setHeight(CmsResourceInfoDialog.SCROLLPANEL_HEIGHT, Style.Unit.PX);
        fieldset.setLegend(label);
        fieldset.add(scrollPanel);
        m_panel.add(fieldset);
        return list;
    }

    /**
     * Gets the message to use for an empty relation list.<p>
     *
     * @return the message to display for an empty relation list
     */
    private String getEmptyMessage() {

        switch (m_mode) {
            case sources:
                if (m_statusBean.getSourcesError() != null) {
                    return m_statusBean.getSourcesError();
                }
                return org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_USAGE_EMPTY_0);
            case targets:
                if (m_statusBean.getTargetsError() != null) {
                    return m_statusBean.getTargetsError();
                }
                //$FALL-THROUGH$
            default:
                return org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_TARGETS_EMPTY_0);
        }

    }

    /**
     * Gets the label to use for the fieldset.<p>
     *
     * @return the label for the fieldset
     */
    private String getLegend() {

        switch (m_mode) {
            case sources:
                return org.opencms.gwt.client.Messages.get().key(
                    org.opencms.gwt.client.Messages.GUI_RESOURCE_INFO_TAB_USAGE_0);
            case targets:

                return org.opencms.gwt.client.Messages.get().key(
                    org.opencms.gwt.client.Messages.GUI_RESOURCE_INFO_TAB_TARGETS_0);
            case siblings:
            default:
                return org.opencms.gwt.client.Messages.get().key(
                    org.opencms.gwt.client.Messages.GUI_RESOURCE_INFO_TAB_SIBLINGS_0);

        }

    }

    /**
     * Gets the relation beans to display.<p>
     *
     * @return the list of relation beans to display
     */
    private ArrayList<CmsResourceStatusRelationBean> getRelationBeans() {

        switch (m_mode) {
            case targets:
                return m_statusBean.getRelationTargets();
            case sources:
                return m_statusBean.getRelationSources();
            case siblings:
            default:
                return m_statusBean.getSiblings();
        }
    }
}
