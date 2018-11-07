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
import org.opencms.gwt.client.I_CmsDescendantResizeHandler;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsTabContentWrapper;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.contextmenu.CmsDialogContextMenuHandler;
import org.opencms.gwt.client.ui.resourceinfo.CmsResourceRelationView.Mode;
import org.opencms.gwt.shared.CmsResourceStatusBean;
import org.opencms.gwt.shared.CmsResourceStatusTabId;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Dialog for displaying resource information.<p>
 */
public class CmsResourceInfoDialog extends CmsPopup {

    /**
     * Context menu handler for resource info boxes.<p>
     */
    public class ContextMenuHandler extends CmsDialogContextMenuHandler {

        /**
         * @see org.opencms.gwt.client.ui.contextmenu.CmsContextMenuHandler#refreshResource(org.opencms.util.CmsUUID)
         */
        @Override
        public void refreshResource(CmsUUID structureId) {

            if (m_structureId.equals(structureId)) {
                super.refreshResource(structureId);
            }
            reload();
        }
    }

    /** The scroll panel height. */
    protected static final int SCROLLPANEL_HEIGHT = 300;

    /** The detail content id. */
    CmsUUID m_detailContentId;

    /** If relation targets should be displayed. */
    boolean m_includeTargets;

    /** The content structure id. */
    CmsUUID m_structureId;

    /** The tab panel. */
    CmsTabbedPanel<CmsTabContentWrapper> m_tabPanel;

    /**
     * Creates the dialog for the given resource information.<p>
     *
     * @param statusBean the resource information to bean
     * @param includeTargets <code>true</code> if relation targets should be displayed
     * @param detailContentId the detail content id
     */
    public CmsResourceInfoDialog(CmsResourceStatusBean statusBean, boolean includeTargets, CmsUUID detailContentId) {

        super();
        setModal(true);
        setGlassEnabled(true);
        addDialogClose(null);
        setWidth(610);
        removePadding();
        m_includeTargets = includeTargets;
        m_detailContentId = detailContentId;
        m_structureId = statusBean.getStructureId();
        m_tabPanel = new CmsTabbedPanel<CmsTabContentWrapper>();
        m_tabPanel.setAutoResize(true);
        m_tabPanel.setAutoResizeHeightDelta(45);
        ContextMenuHandler menuHandler = new ContextMenuHandler();
        final List<CmsResourceRelationView> relationViews = new ArrayList<CmsResourceRelationView>();
        for (Map.Entry<CmsResourceStatusTabId, String> tabEntry : statusBean.getTabs().entrySet()) {
            switch (tabEntry.getKey()) {
                case tabRelationsFrom:
                    CmsResourceRelationView targets = new CmsResourceRelationView(
                        statusBean,
                        Mode.targets,
                        menuHandler);
                    setTabMinHeight(targets);
                    targets.setPopup(this);
                    m_tabPanel.add(new CmsTabContentWrapper(targets), tabEntry.getValue());
                    relationViews.add(targets);
                    break;
                case tabRelationsTo:
                    CmsResourceRelationView usage = new CmsResourceRelationView(statusBean, Mode.sources, menuHandler);
                    setTabMinHeight(usage);
                    usage.setPopup(this);
                    m_tabPanel.add(new CmsTabContentWrapper(usage), tabEntry.getValue());
                    relationViews.add(usage);
                    break;
                case tabStatus:
                    CmsResourceInfoView infoView = new CmsResourceInfoView(statusBean, menuHandler);
                    setTabMinHeight(infoView);
                    m_tabPanel.add(new CmsTabContentWrapper(infoView), tabEntry.getValue());
                    relationViews.add(null);
                    break;
                case tabSiblings:
                    if (statusBean.getSiblings().size() > 0) {
                        CmsResourceRelationView siblings = new CmsResourceRelationView(
                            statusBean,
                            Mode.siblings,
                            menuHandler);
                        setTabMinHeight(siblings);
                        m_tabPanel.add(new CmsTabContentWrapper(siblings), tabEntry.getValue());
                        relationViews.add(siblings);
                    }
                    break;
                default:
                    break;
            }
        }
        if (relationViews.get(0) != null) {
            relationViews.get(0).onResizeDescendant();
        }
        m_tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {

            public void onSelection(SelectionEvent<Integer> event) {

                Widget tabContent = m_tabPanel.getWidget(event.getSelectedItem().intValue()).getWidget();
                if (tabContent instanceof I_CmsDescendantResizeHandler) {
                    ((I_CmsDescendantResizeHandler)tabContent).onResizeDescendant();
                }
                delayedResize();
            }

        });
        setMainContent(m_tabPanel);
        List<CmsResourceStatusTabId> tabKeyList = Lists.newArrayList(statusBean.getTabs().keySet());
        int startTab = tabKeyList.indexOf(statusBean.getStartTab());
        m_tabPanel.selectTab(startTab);
    }

    /**
     * Loads the resource information for a resource and displays it in a dialog.<p>
     *
     * @param structureId the structure id of the resource for which the resource info should be loaded
     * @param includeTargets true if relation targets should also be displayed
     * @param detailContentId the structure id of the detail content if present
     * @param context additional parameters from the context used for displaying additional infos
     * @param closeHandler the close handler for the dialog (may be null if no close handler is needed)
     */
    public static void load(
        final CmsUUID structureId,
        final boolean includeTargets,
        final CmsUUID detailContentId,
        Map<String, String> context,
        final CloseHandler<PopupPanel> closeHandler) {

        load(structureId, includeTargets, detailContentId, null, context, closeHandler);
    }

    /**
     * Loads the resource information for a resource and displays it in a dialog.<p>
     *
     * @param structureId the structure id of the resource for which the resource info should be loaded
     * @param includeTargets true if relation targets should also be displayed
     * @param detailContentId the structure id of the detail content if present
     * @param startTab the start tab id
     * @param context additional parameters from the context used for displaying additional infos
     * @param closeHandler the close handler for the dialog (may be null if no close handler is needed)
     */
    public static void load(
        final CmsUUID structureId,
        final boolean includeTargets,
        final CmsUUID detailContentId,
        final String startTab,
        final Map<String, String> context,
        final CloseHandler<PopupPanel> closeHandler) {

        CmsRpcAction<CmsResourceStatusBean> action = new CmsRpcAction<CmsResourceStatusBean>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getVfsService().getResourceStatus(
                    structureId,
                    CmsCoreProvider.get().getLocale(),
                    includeTargets,
                    detailContentId,
                    context,
                    this);
            }

            @Override
            protected void onResponse(CmsResourceStatusBean result) {

                stop(false);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(startTab)) {
                    result.setStartTab(CmsResourceStatusTabId.valueOf(startTab));
                }
                CmsResourceInfoDialog dialog = new CmsResourceInfoDialog(result, includeTargets, detailContentId);
                if (closeHandler != null) {
                    dialog.addCloseHandler(closeHandler);
                }
                dialog.centerHorizontally(150);
            }
        };
        action.execute();
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    public void onLoad() {

        delayedResize();
    }

    /**
     * Re-initializes the dialog content.<p>
     *
     * @param statusBean the resource status
     */
    protected void reinitContent(CmsResourceStatusBean statusBean) {

        int selected = m_tabPanel.getSelectedIndex();
        for (int i = 0; i < m_tabPanel.getTabCount(); i++) {
            CmsTabContentWrapper wrapper = m_tabPanel.getWidget(i);
            if (wrapper.getWidget() instanceof CmsResourceInfoView) {
                ((CmsResourceInfoView)wrapper.getWidget()).initContent(statusBean);
            } else if (wrapper.getWidget() instanceof CmsResourceRelationView) {
                ((CmsResourceRelationView)wrapper.getWidget()).initContent(statusBean);
                if (i == selected) {
                    ((CmsResourceRelationView)wrapper.getWidget()).onResizeDescendant();
                }
            }
        }
    }

    /**
     * Schedules a resize operation.<p>
     */
    void delayedResize() {

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                resize();
            }
        });

    }

    /**
     * Reloads the dialog data.<p>
     */
    void reload() {

        CmsRpcAction<CmsResourceStatusBean> action = new CmsRpcAction<CmsResourceStatusBean>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getVfsService().getResourceStatus(
                    m_structureId,
                    CmsCoreProvider.get().getLocale(),
                    m_includeTargets,
                    m_detailContentId,
                    null,
                    this);
            }

            @Override
            protected void onResponse(CmsResourceStatusBean result) {

                reinitContent(result);
            }
        };
        action.execute();
    }

    /**
     * Resizes the tab panel.<p>
     */
    void resize() {

        m_tabPanel.onResizeDescendant();
    }

    /**
     * Sets the minimum height for a tab content widget.<p>
     *
     * @param w the minimum height for a tab content widget
     */
    private void setTabMinHeight(Widget w) {

        w.getElement().getStyle().setProperty("minHeight", "355px");
    }
}
