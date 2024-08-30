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

package org.opencms.ade.editprovider.client;

import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.CmsBroadcastTimer;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsQuickLauncher;
import org.opencms.gwt.client.ui.CmsQuickLauncher.A_QuickLaunchHandler;
import org.opencms.gwt.client.ui.CmsToolbar;
import org.opencms.gwt.client.ui.CmsToolbarContextButton;
import org.opencms.gwt.client.ui.I_CmsToolbarButton;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsToolbarButtonLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsDomUtil.Tag;
import org.opencms.gwt.client.util.CmsPositionBean;
import org.opencms.gwt.client.util.CmsStyleVariable;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsGwtConstants.QuickLaunch;
import org.opencms.gwt.shared.CmsQuickLaunchParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * The entry point class for the org.opencms.ade.editprovider.EditProvider module.<p>
 *
 * @since 8.0.0
 */
public class CmsDirectEditEntryPoint extends A_CmsEntryPoint {

    /** The map of button bar positions. */
    protected Map<String, CmsPositionBean> m_buttonPositions = new HashMap<String, CmsPositionBean>();

    /** The map of editable element positions. */
    protected Map<String, CmsPositionBean> m_positions = new HashMap<String, CmsPositionBean>();

    /** The toolbar. */
    protected CmsToolbar m_toolbar;

    /** A style variable to control toolbar visibility. */
    protected CmsStyleVariable m_toolbarVisibility;

    /** The dierect edit buttons. */
    private Map<String, CmsDirectEditButtons> m_directEditButtons = Maps.newHashMap();

    /**
     * Initializes the direct edit buttons.<p>
     */
    public void initializeButtons() {

        List<Element> editableElements = CmsDomUtil.getElementsByClass(CmsGwtConstants.CLASS_EDITABLE, Tag.ALL);
        List<CmsDirectEditButtons> editables = Lists.newArrayList();

        for (Element elem : editableElements) {
            CmsPositionBean pos = CmsDomUtil.getEditablePosition(elem);
            m_positions.put(elem.getId(), pos);
        }

        CmsEditablePositionCalculator posCalc = new CmsEditablePositionCalculator(m_positions);
        m_buttonPositions = posCalc.calculatePositions();

        for (Element elem : editableElements) {
            CmsDirectEditButtons directEdit = processEditableElement(elem);
            m_directEditButtons.put(elem.getId(), directEdit);
            editables.add(directEdit);
        }
        Window.addResizeHandler(new ResizeHandler() {

            public void onResize(ResizeEvent event) {

                repositionButtons();
            }
        });
    }

    /**
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();
        org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.directEditCss().ensureInjected();
        RootPanel.get().addStyleName(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().editButtonsVisible());
        installToolbar();
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                initializeButtons();

            }
        });
        CmsBroadcastTimer.start();
    }

    /**
     * Writes the tool-bar visibility into the session cache.<p>
     *
     * @param visible <code>true</code> if the tool-bar is visible
     */
    public void saveToolbarVisibility(final boolean visible) {

        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                CmsCoreProvider.getService().setToolbarVisible(visible, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(Void result) {

                //nothing to do
            }
        };
        action.execute();
    }

    /**
     * Adds the toolbar to the browser window.<p>
     */
    protected void installToolbar() {

        ClickHandler clickHandler = new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                I_CmsToolbarButton source = (I_CmsToolbarButton)event.getSource();
                source.onToolbarClick();
                if (source instanceof CmsPushButton) {
                    ((CmsPushButton)source).clearHoverState();
                }
            }
        };
        m_toolbar = new CmsToolbar();
        RootPanel root = RootPanel.get();
        m_toolbarVisibility = new CmsStyleVariable(m_toolbar);
        root.add(m_toolbar);
        boolean initiallyVisible = CmsCoreProvider.get().isToolbarVisible();
        if (initiallyVisible) {
            m_toolbarVisibility.setValue(I_CmsLayoutBundle.INSTANCE.toolbarCss().simpleToolbarShow());
        } else {
            m_toolbarVisibility.setValue(I_CmsLayoutBundle.INSTANCE.toolbarCss().toolbarHide());
        }

        CmsDirectEditToolbarHandler handler = new CmsDirectEditToolbarHandler(this);

        CmsToolbarPublishButton publish = new CmsToolbarPublishButton(handler);
        publish.addClickHandler(clickHandler);
        m_toolbar.addLeft(publish);

        CmsQuickLauncher ql = new CmsQuickLauncher();
        m_toolbar.addRight(ql);

        CmsToolbarSelectionButton selection = new CmsToolbarSelectionButton(handler);
        selection.setActive(initiallyVisible);
        selection.addClickHandler(clickHandler);
        m_toolbar.addLeft(selection);
        CmsToolbarContextButton contextMenuButton = new CmsToolbarContextButton(handler);
        contextMenuButton.setMenuContext(AdeContext.editprovider);
        contextMenuButton.addClickHandler(clickHandler);
        m_toolbar.addRight(contextMenuButton);
        handler.setContextMenuButton(contextMenuButton);
        m_toolbar.setQuickLaunchHandler(new A_QuickLaunchHandler() {

            public CmsQuickLaunchParams getParameters() {

                return new CmsQuickLaunchParams(
                    QuickLaunch.CONTEXT_PAGE,
                    CmsCoreProvider.get().getStructureId(),
                    null,
                    null,
                    CmsCoreProvider.get().getUri(),
                    CmsCoreProvider.get().getLastPageId());
            }
        });

    }

    /**
     * Checks if the toolbar is visible.<p>
     *
     * @return true if the toolbar is visible
     */
    protected boolean isToolbarVisible() {

        return m_toolbarVisibility.getValue().equals(I_CmsLayoutBundle.INSTANCE.toolbarCss().simpleToolbarShow());
    }

    /**
     * Adds the direct edit buttons for a single editable element.<p>
     *
     * @param elem the data container element
     *
     * @return the direct edit buttons widget which was created for the element
     */
    protected CmsDirectEditButtons processEditableElement(Element elem) {

        RootPanel root = RootPanel.get();
        CmsDirectEditButtons result = new CmsDirectEditButtons(elem, null);
        root.add(result);
        result.setPosition(m_positions.get(elem.getId()), m_buttonPositions.get(elem.getId()), elem.getParentElement());
        return result;
    }

    /**
     * Repositions the direct edit buttons.<p>
     */
    protected void repositionButtons() {

        for (Map.Entry<String, CmsDirectEditButtons> entry : m_directEditButtons.entrySet()) {
            CmsDirectEditButtons buttons = entry.getValue();
            Element tag = buttons.getMarkerTag();
            CmsPositionBean newPos = CmsDomUtil.getEditablePosition(tag);
            m_positions.put(tag.getId(), newPos);
        }
        CmsEditablePositionCalculator posCalc = new CmsEditablePositionCalculator(m_positions);
        m_buttonPositions = posCalc.calculatePositions();
        for (CmsDirectEditButtons buttons : m_directEditButtons.values()) {
            String id = buttons.getMarkerTag().getId();
            buttons.setPosition(
                m_positions.get(id),
                m_buttonPositions.get(id),
                buttons.getMarkerTag().getParentElement());
        }

    }

    /**
     * Toggles the visibility of the toolbar.<p>
     *
     * @param show <code>true</code> to show the toolbar
     */
    protected void toggleToolbar(boolean show) {

        if (show) {
            CmsToolbar.showToolbar(
                m_toolbar,
                true,
                m_toolbarVisibility,
                I_CmsLayoutBundle.INSTANCE.toolbarCss().simpleToolbarShow());
            saveToolbarVisibility(true);
        } else {
            CmsToolbar.showToolbar(
                m_toolbar,
                false,
                m_toolbarVisibility,
                I_CmsLayoutBundle.INSTANCE.toolbarCss().simpleToolbarShow());
            saveToolbarVisibility(false);
        }

    }

}
