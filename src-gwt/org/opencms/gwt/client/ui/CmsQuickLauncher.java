/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsToolbarCss;
import org.opencms.gwt.shared.CmsQuickLaunchData;
import org.opencms.gwt.shared.CmsQuickLaunchParams;
import org.opencms.util.CmsStringUtil;

import java.util.Collection;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The user info toolbar button.<p>
 */
public class CmsQuickLauncher extends CmsMenuButton {

    /**
     * Abstract class for standard handling of quick launh items.<p>
     */
    public abstract static class A_QuickLaunchHandler implements I_QuickLaunchHandler {

        /**
         * @see org.opencms.gwt.client.ui.CmsQuickLauncher.I_QuickLaunchHandler#handleQuickLaunch(org.opencms.gwt.shared.CmsQuickLaunchData)
         */
        public void handleQuickLaunch(CmsQuickLaunchData data) {

            if (data.getErrorMessage() != null) {
                CmsAlertDialog alert = new CmsAlertDialog("" + data.getErrorTitle(), data.getErrorMessage());
                alert.center();
            } else if (data.isReload()) {
                Window.Location.reload();
            } else {
                Window.Location.assign(data.getDefaultUrl());
            }
        }
    }

    /**
     * The quick launch handler interface.<p>
     */
    public static interface I_QuickLaunchHandler {

        /**
         * Gets the quick launch parameters.<p>
         *
         * @return the quick launch parameters
         */
        CmsQuickLaunchParams getParameters();

        /**
         * Processes a click on a quick launch item.
         *
         * @param data the bean representing the quick launch item
         * */
        void handleQuickLaunch(CmsQuickLaunchData data);
    }

    /**
     * Button for an individual entry in the quick launch menu.<p>
     */
    public class QuickLaunchButton extends Composite {

        /**
         * Creates a new button instance for the given bean.<p>
         *
         * @param data the quick launch data bean
         */
        public QuickLaunchButton(final CmsQuickLaunchData data) {

            super();
            I_CmsToolbarCss toolbarCss = I_CmsLayoutBundle.INSTANCE.toolbarCss();

            initWidget(div(
                toolbarCss.quickButton(),
                div(
                    toolbarCss.quickButtonWrap(),
                    div(null, div(toolbarCss.quickButtonImageContainer(), new Image(data.getIconUrl()))),
                    div(null, new InlineLabel(data.getTitle())))));
            if (data.isLegacy()) {
                addStyleName(toolbarCss.quickButtonLegacy());
            }
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(data.getErrorMessage())) {
                setTitle(data.getErrorMessage());
                addStyleName(toolbarCss.quickButtonDeactivated());
            } else {
                addDomHandler(new ClickHandler() {

                    @SuppressWarnings("synthetic-access")
                    public void onClick(ClickEvent event) {

                        closeMenu();
                        m_handler.handleQuickLaunch(data);

                    }
                }, ClickEvent.getType());
            }
        }

        /**
         * Helper method which constructs a FlowPanel with the given CSS class and contents.<p>
         *
         * @param cls the CSS class (may be null)
         * @param components the child widgets
         *
         * @return the constructed FlowPanel
         */
        private FlowPanel div(String cls, Widget... components) {

            FlowPanel result = new FlowPanel();
            if (cls != null) {
                result.addStyleName(cls);
            }
            for (Widget comp : components) {
                result.add(comp);
            }
            return result;
        }
    }

    /** Html for the menu button. */
    public static final String BUTTON_HTML = "<span class='"
        + I_CmsLayoutBundle.INSTANCE.toolbarCss().toolbarFontButton()
        + "'>\ue617</span>";

    /** The quick launch handler. */
    private I_QuickLaunchHandler m_handler;

    /** The panel containing the individual quick launch buttons. */
    private Panel m_itemContainer;

    /**
     * Constructor.<p>
     */
    public CmsQuickLauncher() {

        super();
        setVisible(false); // only turn visible once the handler is set
        getPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().contextMenu());
        getPopup().setWidth(0);
        m_button.getUpFace().setHTML(BUTTON_HTML);

        m_button.setTitle(Messages.get().key(Messages.GUI_QUICK_LAUCNH_0));
        m_button.getElement().getStyle().setBottom(1, Unit.PX);

        setToolbarMode(true);

        FlowPanel panel = new FlowPanel();
        panel.addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().quickLaunchContainer());
        m_itemContainer = panel;
        setMenuWidget(panel);
        addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                toggleQuickLauncher();
            }
        });
    }

    /**
     * Sets the quick launch handler and makes the button visible.<p>
     *
     * @param handler the quick launch handler
     */
    public void setHandler(I_QuickLaunchHandler handler) {

        m_handler = handler;
        setVisible(true);
    }

    /**
     * Fills the quick launch menu with buttons corresponding to the given quick launch beans.<p>
     *
     * @param quickLaunchData the list of quick launch beans
     */
    protected void fillItems(Collection<CmsQuickLaunchData> quickLaunchData) {

        Panel container = getItemContainer();
        container.clear();
        for (CmsQuickLaunchData item : quickLaunchData) {
            container.add(new QuickLaunchButton(item));
        }
    }

    /**
     * Toggles the user info visibility.<p>
     */
    protected void toggleQuickLauncher() {

        if (isOpen()) {
            closeMenu();
        } else {
            CmsRpcAction<List<CmsQuickLaunchData>> action = new CmsRpcAction<List<CmsQuickLaunchData>>() {

                @SuppressWarnings("synthetic-access")
                @Override
                public void execute() {

                    start(150, false);
                    CmsCoreProvider.getVfsService().loadQuickLaunchItems(m_handler.getParameters(), this);
                }

                @Override
                protected void onResponse(List<CmsQuickLaunchData> result) {

                    stop(false);
                    fillItems(result);
                    openMenu();
                }

            };
            action.execute();
        }
    }

    /**
     * Gets the item container.<p>
     *
     * @return the item container
     */
    private Panel getItemContainer() {

        return m_itemContainer;
    }
}
