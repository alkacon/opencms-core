/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui.resourceinfo;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.CmsEditableData;
import org.opencms.gwt.client.CmsGwtConstants;
import org.opencms.gwt.client.ui.CmsFieldSet;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.CmsSimpleListItem;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.contenteditor.CmsContentEditorDialog;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuButton;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuHandler;
import org.opencms.gwt.client.ui.contextmenu.CmsLogout;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsResourceStatusBean;
import org.opencms.gwt.shared.CmsResourceStatusRelationBean;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/** 
 * Widget which shows which contents refer to a resource.<p> 
 */
public class CmsResourceRelationView extends Composite {

    /** Enum for the display mode. */
    public enum Mode {
        /** Display relation sources. */
        sources,
        /** Display relation targets. */
        targets
    }

    /** Set of context menu actions which we do not want to appear in the context menu for the relation source items. */
    protected static Set<String> m_filteredActions = new HashSet<String>();

    /** The panel containing the resource boxes. */
    protected CmsList<CmsListItem> m_list = new CmsList<CmsListItem>();

    /** Main panel. */
    protected FlowPanel m_panel = new FlowPanel();

    /** The popup which contains this widget. */
    protected CmsPopup m_popup;

    /** Container which is used to display the main resource information box. */
    SimplePanel m_infoBox = new SimplePanel();

    /** The edit button. */
    private CmsPushButton m_editButton;

    /** True if the resource boxes have already been created. */
    private boolean m_filled;

    /** The display mode. */
    private Mode m_mode;

    /** Timer for notifying the scroll panel of size changes. */
    private Timer m_resizeTimer = new Timer() {

        @Override
        public void run() {

            CmsDomUtil.resizeAncestor(m_list);
        }
    };

    /** The resource status from which we get the related resources to display. */
    private CmsResourceStatusBean m_statusBean;

    /** 
     * Creates a new widget instance.<p>
     * 
     * @param status the resource status from which we get the related resources to display.
     * @param mode the display mode (display relation sources or targets)  
     */
    public CmsResourceRelationView(CmsResourceStatusBean status, Mode mode) {

        initWidget(m_panel);
        m_panel.getElement().getStyle().setOverflow(Style.Overflow.HIDDEN);
        m_statusBean = status;
        m_mode = mode;
        CmsListItemWidget infoWidget = new CmsListItemWidget(status.getListInfo());
        CmsListItem infoItem = new CmsListItem(infoWidget);
        m_panel.add(infoItem);
        CmsFieldSet fieldset = new CmsFieldSet();
        CmsScrollPanel scrollPanel = GWT.create(CmsScrollPanel.class);
        scrollPanel.add(m_list);
        fieldset.getElement().getStyle().setMarginTop(10, Style.Unit.PX);
        scrollPanel.getElement().getStyle().setHeight(280, Style.Unit.PX);
        fieldset.setLegend(getLegend());
        fieldset.add(scrollPanel);
        m_panel.add(fieldset);
    }

    static {
        m_filteredActions.add(CmsGwtConstants.ACTION_TEMPLATECONTEXTS);
        m_filteredActions.add(CmsGwtConstants.ACTION_EDITSMALLELEMENTS);
        m_filteredActions.add(CmsLogout.class.getName());
    }

    /**
     * This method should be called when this view is shown.<p>
     */
    public void onSelect() {

        if (!m_filled) {
            // only produce the widgets when they actually need to be shown 
            fill();
            m_filled = true;
        }
        m_resizeTimer.schedule(1);
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
                    new CmsContextMenuHandler() {

                        @Override
                        public void refreshResource(CmsUUID structureId) {

                            Window.Location.reload();
                        }

                        @Override
                        protected I_CmsContextMenuEntry transformSingleEntry(
                            CmsContextMenuEntryBean entryBean,
                            CmsUUID structureId) {

                            if (m_filteredActions.contains(entryBean.getName())) {
                                return null;
                            } else {
                                return super.transformSingleEntry(entryBean, structureId);
                            }
                        }

                    });
                item.getListItemWidget().addButton(button);
                final boolean isContainerpage = CmsGwtConstants.TYPE_CONTAINERPAGE.equals(relationBean.getInfoBean().getResourceType());
                final boolean isXmlContent = relationBean.isXmlContent();
                final boolean isEditable = isXmlContent || isContainerpage;
                if (isEditable) {
                    final CmsResourceStatusRelationBean currentRelationBean = relationBean;

                    m_editButton = new CmsPushButton();
                    m_editButton.setImageClass(I_CmsImageBundle.INSTANCE.style().editIcon());
                    m_editButton.setButtonStyle(ButtonStyle.TRANSPARENT, null);
                    m_editButton.setTitle(org.opencms.gwt.client.Messages.get().key(
                        org.opencms.gwt.client.Messages.GUI_BUTTON_ELEMENT_EDIT_0));
                    m_editButton.setEnabled(true);
                    item.getListItemWidget().addButton(m_editButton);
                    m_editButton.addClickHandler(new ClickHandler() {

                        public void onClick(ClickEvent event) {

                            if (isContainerpage) {
                                Window.open(currentRelationBean.getLink(), "_blank", "");
                            } else {
                                CmsEditableData editableData = new CmsEditableData();
                                editableData.setElementLanguage(CmsCoreProvider.get().getLocale());
                                editableData.setStructureId(currentRelationBean.getStructureId());
                                editableData.setSitePath(currentRelationBean.getSitePath());
                                CmsDomUtil.ensureMouseOut((Widget)event.getSource());
                                FormElement form = CmsContentEditorDialog.generateForm(editableData, false, "_blank");
                                RootPanel.get().getElement().appendChild(form);
                                form.submit();
                                if (m_popup != null) {
                                    m_popup.hide();
                                }

                            }
                        }
                    });
                }

                if (isContainerpage) {
                    if (relationBean.getLink() != null) {
                        final String link = relationBean.getLink();
                        itemWidget.setIconCursor(Cursor.POINTER);
                        itemWidget.addIconClickHandler(new ClickHandler() {

                            public void onClick(ClickEvent e) {

                                Window.open(link, "_blank", "");
                            }
                        });
                    }
                }
                m_list.add(item);
            }

        }

    }

    /**
     * Gets the message to use for an empty relation list.<p>
     * 
     * @return the message to display for an empty relation list 
     */
    private String getEmptyMessage() {

        switch (m_mode) {
            case sources:
                return org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_USAGE_EMPTY_0);
            case targets:
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
            default:
                return org.opencms.gwt.client.Messages.get().key(
                    org.opencms.gwt.client.Messages.GUI_RESOURCE_INFO_TAB_TARGETS_0);
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
            default:
                return m_statusBean.getRelationSources();

        }
    }
}
