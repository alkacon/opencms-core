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

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsDomUtil.Method;

import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Frame dialog utility class.<p>
 * 
 * Use to render the dialog content within an iFrame on top of a regular {@link org.opencms.gwt.client.ui.CmsPopup}.
 * May also be used to wrap the popup if no iFrame is needed.<p>
 * 
 * Provides function to show an iFrame dialog.<p>
 * 
 * @since 8.5
 */
public class CmsFrameDialog {

    /** The name of the close function. */
    public static final String CLOSE_FUNCTION = "cmsDialogClose";

    /** The dialog height. */
    public static final int DIALOG_HEIGHT = 300;

    /** The dialog width. */
    public static final int DIALOG_WIDTH = 200;

    /** The name of the enable dialog close function. */
    public static final String ENABLE_CLOSE_FUNCTION = "cmsEnableDialogClose";

    /** The name of the dialog height function. */
    public static final String HEIGHT_FUNCTION = "cmsDialogHeight";

    /** The name of the IFrame used for displaying the upload hook page. */
    public static final String IFRAME_NAME = "upload_hook";

    /** The name of the dialog title function. */
    public static final String TITLE_FUNCTION = "cmsDialogTitle";

    /** The name of the dialog width function. */
    public static final String WIDTH_FUNCTION = "cmsUploadHookDialogWidth";

    /** The button panel. */
    private FlowPanel m_buttonPanel;

    /** The content widget. */
    private Widget m_content;

    /** The panel holding the content widget. */
    private SimplePanel m_contentPanel;

    /** Flag indicating if this dialog is displayed within an iFrame on top of a popup. */
    private boolean m_isFrame;

    /** Flag indicating that the dialog is showing.< */
    private boolean m_isShowing;

    /** The main panel. */
    private FlowPanel m_main;

    /** The popup. */
    private CmsPopup m_popup;

    /**
     * Constructor.<p>
     */
    public CmsFrameDialog() {

        m_isFrame = hasParentFrame();
        if (m_isFrame) {
            m_main = new FlowPanel();
            m_main.addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().frameDialog());
            m_contentPanel = new SimplePanel();
            m_contentPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupMainContent());
            m_contentPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().contentPadding());
            m_main.add(m_contentPanel);

        } else {
            m_popup = new CmsPopup();
            m_popup.setGlassEnabled(true);
        }
    }

    /**
     * Returns if this dialog has a parent frame.<p>
     * 
     * @return <code>true</code> if the parent frame is available
     */
    public static native boolean hasParentFrame() /*-{
        if ($wnd.parent[@org.opencms.gwt.client.ui.CmsFrameDialog::CLOSE_FUNCTION]) {
            return true;
        }
        return false;

    }-*/;

    /**
     * Shows an iFrame dialog popup.<p>
     * 
     * @param title the dialog title
     * @param dialogUri the dialog URI
     * @param parameters the dialog post parameters
     * @param closeHandler the dialog close handler
     * 
     * @return the opened popup
     */
    public static CmsPopup showFrameDialog(
        String title,
        String dialogUri,
        Map<String, String> parameters,
        CloseHandler<PopupPanel> closeHandler) {

        CmsPopup popup = new CmsPopup(title);
        popup.removePadding();
        popup.addStyleName(I_CmsLayoutBundle.INSTANCE.contentEditorCss().contentEditor());
        popup.setGlassEnabled(true);
        CmsIFrame editorFrame = new CmsIFrame(IFRAME_NAME, "");
        popup.add(editorFrame);
        final FormElement formElement = CmsDomUtil.generateHiddenForm(dialogUri, Method.post, IFRAME_NAME, parameters);
        RootPanel.getBodyElement().appendChild(formElement);
        exportDialogFunctions(popup);
        popup.addCloseHandler(new CloseHandler<PopupPanel>() {

            public void onClose(CloseEvent<PopupPanel> event) {

                formElement.removeFromParent();
                removeExportedFunctions();
            }
        });
        if (closeHandler != null) {
            popup.addCloseHandler(closeHandler);
        }
        popup.center();
        formElement.submit();
        return popup;
    }

    /**
     * Removes exported functions from the window context.<p>
     */
    protected static native void removeExportedFunctions() /*-{
        $wnd[@org.opencms.gwt.client.ui.CmsFrameDialog::CLOSE_FUNCTION] = null;
        $wnd[@org.opencms.gwt.client.ui.CmsFrameDialog::HEIGHT_FUNCTION] = null;
        $wnd[@org.opencms.gwt.client.ui.CmsFrameDialog::WIDTH_FUNCTION] = null;
        $wnd[@org.opencms.gwt.client.ui.CmsFrameDialog::TITLE_FUNCTION] = null;
    }-*/;

    /**
     * Installs the Javascript function which should be called by the child iframe when the dialog should be closed.<p>
     * 
     * @param popup the popup
     */
    private static native void exportDialogFunctions(final CmsPopup popup) /*-{
        var self = this;
        $wnd[@org.opencms.gwt.client.ui.CmsFrameDialog::CLOSE_FUNCTION] = function() {
            popup.@org.opencms.gwt.client.ui.CmsPopup::hide()();
        };
        $wnd[@org.opencms.gwt.client.ui.CmsFrameDialog::HEIGHT_FUNCTION] = function(
                height) {
            popup.@org.opencms.gwt.client.ui.CmsPopup::setHeight(I)(height);
            if (popup.@org.opencms.gwt.client.ui.CmsPopup::isShowing()) {
                popup.@org.opencms.gwt.client.ui.CmsPopup::center()();
            }
        };
        $wnd[@org.opencms.gwt.client.ui.CmsFrameDialog::WIDTH_FUNCTION] = function(
                width) {
            popup.@org.opencms.gwt.client.ui.CmsPopup::setWidth(I)(width);
            if (popup.@org.opencms.gwt.client.ui.CmsPopup::isShowing()) {
                popup.@org.opencms.gwt.client.ui.CmsPopup::center()();
            }
        };
        $wnd[@org.opencms.gwt.client.ui.CmsFrameDialog::TITLE_FUNCTION] = function(
                title) {
            popup.@org.opencms.gwt.client.ui.CmsPopup::setCaption(Ljava/lang/String;)(title);
        };
        $wnd[@org.opencms.gwt.client.ui.CmsFrameDialog::ENABLE_CLOSE_FUNCTION] = function(
                title) {
            popup.@org.opencms.gwt.client.ui.CmsPopup::addDialogClose(Lcom/google/gwt/user/client/Command;)(null);
        };
    }-*/;

    /**
     * Adds a new button to the button bar.<p>
     * 
     * @param button the button to add
     */
    public void addButton(Widget button) {

        if (m_isFrame) {
            initButtonPanel();
            m_buttonPanel.add(button);
        } else {
            m_popup.addButton(button);
        }
    }

    /**
     * Adds a new button to the button bar at the specified index position.<p>
     * 
     * @param button the button to add
     * @param index the index position
     */
    public void addButton(Widget button, int index) {

        if (m_isFrame) {
            initButtonPanel();
            m_buttonPanel.insert(button, index);
        } else {
            m_popup.addButton(button, index);
        }
    }

    /**
     * Enables the dialog close button on the popup.<p>
     */
    public void enableDialogClose() {

        if (m_isFrame) {
            enableParentDialogClose();
        } else {
            m_popup.addDialogClose(null);
        }
    }

    /**
     * Hides the dialog.<p>
     */
    public void hide() {

        if (m_isFrame) {
            hideParent();
        } else {
            m_popup.hide();
        }
    }

    /**
     * Returns if the popup is showing and the content is rendered.<p>
     * 
     * @return <code>true</code> if the popup and content are showing
     */
    public boolean isShowing() {

        if (m_isFrame) {
            return m_isShowing;
        } else {
            return m_popup.isShowing();
        }
    }

    /**
     * Removes the given button from the button bar.<p>
     * 
     * @param button the button to remove
     */
    public void removeButton(Widget button) {

        if (m_isFrame) {
            if (m_buttonPanel != null) {
                m_buttonPanel.remove(button);
            }
        } else {
            m_popup.removeButton(button);
        }
    }

    /**
     * Sets the content widget.<p>
     * 
     * @param content the content widget
     */
    public void setContent(Widget content) {

        if (m_content != null) {
            m_content.removeFromParent();
        }
        if (m_isFrame) {
            m_contentPanel.setWidget(content);
        } else {
            m_popup.setMainContent(content);
        }
        m_content = content;
    }

    /**
     * Sets the popup height.<p>
     * 
     * @param height the height
     */
    public void setHeight(int height) {

        if (m_isFrame) {
            setParentHeight(height);
        } else {
            m_popup.setHeight(height);
            if (m_popup.isShowing()) {
                m_popup.center();
            }
        }
    }

    /**
     * Sets the dialog title.<p>
     * 
     * @param title the title
     */
    public void setTitle(String title) {

        if (m_isFrame) {
            setParentTitle(title);
        } else {
            m_popup.setCaption(title);
        }
    }

    /**
     * Sets the popup width.<p>
     * 
     * @param width the width
     */
    public void setWidth(int width) {

        if (m_isFrame) {
            setParentWidth(width);
        } else {
            m_popup.setWidth(width);
            if (m_popup.isShowing()) {
                m_popup.center();
            }
        }
    }

    /**
     * Shows the dialog.<p>
     */
    public void show() {

        if (m_isFrame) {
            RootPanel root = RootPanel.get();
            root.getElement().getStyle().setMargin(0, Unit.PX);
            root.getElement().getStyle().setPadding(0, Unit.PX);
            RootPanel.get().add(m_main);
            m_isShowing = true;
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                public void execute() {

                    adjustContentSize();
                }
            });
        } else {
            m_popup.center();
        }

    }

    /**
     * Adjusts the content panel size according to the button panel height.<p>
     */
    protected void adjustContentSize() {

        if (m_isFrame && m_isShowing) {
            if (m_buttonPanel != null) {
                m_contentPanel.getElement().getStyle().setBottom(m_buttonPanel.getOffsetHeight() + 6, Unit.PX);
            } else {
                m_contentPanel.getElement().getStyle().clearBottom();
            }
        }
    }

    /**
     * Enables the dialog close button on the parent frame popup.<p>
     */
    private native void enableParentDialogClose() /*-{
        $wnd.parent[@org.opencms.gwt.client.ui.CmsFrameDialog::ENABLE_CLOSE_FUNCTION]
                ();
    }-*/;

    /**
     * Hides the parent dialog.<p>
     */
    private native void hideParent() /*-{
        $wnd.parent[@org.opencms.gwt.client.ui.CmsFrameDialog::CLOSE_FUNCTION]
                ();
    }-*/;

    /**
     * Initializes the button panel within frame mode.<p>
     */
    private void initButtonPanel() {

        if ((m_buttonPanel == null) && m_isFrame) {
            m_buttonPanel = new FlowPanel();
            m_buttonPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupButtonPanel());
            m_main.add(m_buttonPanel);
        }
    }

    /**
     * Sets the parent dialog height.<p>
     * 
     * @param height the height to set
     */
    private native void setParentHeight(int height) /*-{
        $wnd.parent[@org.opencms.gwt.client.ui.CmsFrameDialog::HEIGHT_FUNCTION]
                (height);
    }-*/;

    /**
     * Sets the title of the parent dialog.<p>
     * 
     * @param title the title
     */
    private native void setParentTitle(String title) /*-{
        $wnd.parent[@org.opencms.gwt.client.ui.CmsFrameDialog::TITLE_FUNCTION]
                (title);
    }-*/;

    /**
     * Sets the parent dialog width.<p>
     * 
     * @param width the width to set
     */
    private native void setParentWidth(int width) /*-{
        $wnd.parent[@org.opencms.gwt.client.ui.CmsFrameDialog::WIDTH_FUNCTION]
                (width);
    }-*/;
}
