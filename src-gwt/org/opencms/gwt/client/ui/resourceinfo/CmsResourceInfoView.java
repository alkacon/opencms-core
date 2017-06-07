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

import org.opencms.db.CmsResourceState;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.I_CmsDescendantResizeHandler;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.contextmenu.CmsAbout;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuButton;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuHandler;
import org.opencms.gwt.client.ui.contextmenu.CmsEditUserSettings;
import org.opencms.gwt.client.ui.contextmenu.CmsLogout;
import org.opencms.gwt.client.ui.contextmenu.CmsOpenSeoDialog;
import org.opencms.gwt.client.ui.contextmenu.CmsShowWorkplace;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsResourceStateUtil;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsResourceStatusBean;
import org.opencms.util.CmsUUID;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget used to display various resource information to a user.<p>
 */
public class CmsResourceInfoView extends Composite implements I_CmsDescendantResizeHandler {

    /**
     * Context menu handler for resource info boxes.<p>
     */
    public static class ContextMenuHandler extends CmsContextMenuHandler {

        /** Set of context menu actions which we do not want to appear in the context menu for the relation source items. */
        protected static Set<String> m_filteredActions = new HashSet<String>();

        static {
            m_filteredActions.add(CmsGwtConstants.ACTION_TEMPLATECONTEXTS);
            m_filteredActions.add(CmsGwtConstants.ACTION_EDITSMALLELEMENTS);
            m_filteredActions.add(CmsGwtConstants.ACTION_SELECTELEMENTVIEW);
            m_filteredActions.add(CmsGwtConstants.ACTION_SHOWLOCALE);
            m_filteredActions.add(CmsGwtConstants.ACTION_VIEW_ONLINE);

            for (Class<?> cls : new Class[] {
                CmsEditUserSettings.class,
                CmsAbout.class,
                CmsShowWorkplace.class,
                CmsOpenSeoDialog.class,
                CmsLogout.class}) {
                m_filteredActions.add(cls.getName());
            }
        }

        /**
         * @see org.opencms.gwt.client.ui.contextmenu.CmsContextMenuHandler#refreshResource(org.opencms.util.CmsUUID)
         */
        @Override
        public void refreshResource(CmsUUID structureId) {

            Window.Location.reload();
        }

        /**
         * @see org.opencms.gwt.client.ui.contextmenu.CmsContextMenuHandler#transformSingleEntry(org.opencms.gwt.shared.CmsContextMenuEntryBean, org.opencms.util.CmsUUID)
         */
        @Override
        protected I_CmsContextMenuEntry transformSingleEntry(CmsContextMenuEntryBean entryBean, CmsUUID structureId) {

            if (m_filteredActions.contains(entryBean.getName())) {
                return null;
            } else {
                return super.transformSingleEntry(entryBean, structureId);
            }
        }

    }

    /**
     * The uiBinder interface for this widget.<p>
     */
    interface I_CmsResourceInfoViewUiBinder extends UiBinder<Widget, CmsResourceInfoView> {
        // empty
    }

    /** The uiBinder instance for this widget. */
    private static I_CmsResourceInfoViewUiBinder uiBinder = GWT.create(I_CmsResourceInfoViewUiBinder.class);

    /**
     * Text field for resource information.<p>
     */
    @UiField
    protected HasText m_dateCreated;

    /**
     * Text field for resource information.<p>
     */
    @UiField
    protected HasText m_dateExpired;

    /**
     * Text field for resource information.<p>
     */
    @UiField
    protected HasText m_dateLastModified;

    /**
     * Text field for resource information.<p>
     */
    @UiField
    protected HasText m_dateReleased;

    /**
     * The container for the file info box.<p>
     */
    @UiField
    protected SimplePanel m_infoBoxContainer;

    /**
     * The info panel.<p>
     */
    @UiField
    protected FlowPanel m_infoPanel;

    /**
     * Text field for resource information.<p>
     */
    @UiField
    protected HasText m_lastProject;

    /**
     * Text field for resource information.<p>
     */
    @UiField
    protected HasText m_locales;

    /**
     * Text field for resource information.<p>
     */
    @UiField
    protected HasText m_lockState;

    /**
     * Text field for resource information.<p>
     */
    @UiField
    protected HasText m_navText;

    /**
     * Text field for resource information.<p>
     */
    @UiField
    protected HasText m_permissions;

    /**
     * Text field for resource information.<p>
     */
    @UiField
    protected HasText m_resourceType;

    /**
     * Scroll panel for resource information.<p>
     */
    @UiField
    protected CmsScrollPanel m_scrollPanel;

    /**
     * Text field for resource information.<p>
     */
    @UiField
    protected HasText m_size;

    /**
     * Text field for resource information.<p>
     */
    @UiField
    protected HasText m_state;

    /**
     * Text field for resource information.<p>
     */
    @UiField
    protected HasText m_title;

    /**
     * Text field for resource information.<p>
     */
    @UiField
    protected HasText m_userCreated;

    /**
     * Text field for resource information.<p>
     */
    @UiField
    protected HasText m_userLastModified;

    /**
     * Creates a new widget instance.<p>
     *
     * @param status the resource information to display
     */
    public CmsResourceInfoView(CmsResourceStatusBean status) {

        initWidget(uiBinder.createAndBindUi(this));
        CmsListItemWidget infoBox = new CmsListItemWidget(status.getListInfo());
        infoBox.addOpenHandler(new OpenHandler<CmsListItemWidget>() {

            public void onOpen(OpenEvent<CmsListItemWidget> event) {

                CmsDomUtil.resizeAncestor(getParent());
            }
        });
        infoBox.addCloseHandler(new CloseHandler<CmsListItemWidget>() {

            public void onClose(CloseEvent<CmsListItemWidget> event) {

                CmsDomUtil.resizeAncestor(getParent());
            }
        });
        CmsContextMenuButton menuButton = new CmsContextMenuButton(status.getStructureId(), new ContextMenuHandler());
        menuButton.addStyleName(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
        infoBox.addButton(menuButton);
        m_infoBoxContainer.add(infoBox);
        m_dateCreated.setText(status.getDateCreated());
        m_dateExpired.setText(status.getDateExpired());
        m_dateLastModified.setText(status.getDateLastModified());
        m_dateReleased.setText(status.getDateReleased());
        m_lastProject.setText(status.getLastProject());
        m_lockState.setText(status.getLockState());
        CmsResourceState state = status.getStateBean();
        String stateStyle = CmsResourceStateUtil.getStateStyle(state);
        String stateText = CmsResourceStateUtil.getStateName(state);
        m_state.setText(makeSpan(stateStyle, stateText));
        m_title.setText(status.getTitle());
        m_navText.setText(status.getNavText());
        m_permissions.setText(status.getPermissions());
        m_resourceType.setText(status.getResourceType());
        m_size.setText("" + status.getSize() + " Bytes");
        m_userCreated.setText(status.getUserCreated());
        m_userLastModified.setText(status.getUserLastModified());
        m_scrollPanel.setHeight(CmsResourceInfoDialog.SCROLLPANEL_HEIGHT + "px");
        List<String> locales = status.getLocales();
        if (locales != null) {
            StringBuffer buffer = new StringBuffer();
            int index = 0;
            for (String locale : locales) {
                if (locale.equals(CmsCoreProvider.get().getLocale())) {
                    buffer.append("<b>");
                    buffer.append(locale);
                    buffer.append("</b>");
                } else {
                    buffer.append(locale);
                }
                if (index != (locales.size() - 1)) {
                    buffer.append(", ");
                }
                index += 1;
            }
            m_locales.setText(buffer.toString());
        }
        if (status.getAdditionalAttributes() != null) {
            for (Entry<String, String> entry : status.getAdditionalAttributes().entrySet()) {
                CmsResourceInfoLine line = new CmsResourceInfoLine();
                line.setLabel(entry.getKey());
                line.setText(entry.getValue());
                m_infoPanel.add(line);
            }
        }

    }

    /**
     * @see org.opencms.gwt.client.I_CmsDescendantResizeHandler#onResizeDescendant()
     */
    public void onResizeDescendant() {

        Timer timer = new Timer() {

            @Override
            public void run() {

                m_scrollPanel.onResizeDescendant();

            }
        };
        timer.schedule(100);
    }

    /**
     * Helper method to generate the HTML for a span with a CSS class and some text.<p>
     *
     * @param className the CSS class
     * @param text the text
     *
     * @return the HTML for the span
     */
    private String makeSpan(String className, String text) {

        return "<span class='" + className + "'>" + text + "</span>";
    }
}
