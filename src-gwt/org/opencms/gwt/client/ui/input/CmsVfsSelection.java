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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.shared.CmsLinkBean;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

/**
 * Basic gallery widget for forms.<p>
 *
 * @since 8.0.0
 *
 */
public class CmsVfsSelection extends Composite implements I_CmsFormWidget, HasValueChangeHandlers<String> {

    /**
     * Event preview handler.<p>
     *
     * To be used while popup open.<p>
     */
    protected class CloseEventPreviewHandler implements NativePreviewHandler {

        /**
         * @see com.google.gwt.user.client.Event.NativePreviewHandler#onPreviewNativeEvent(com.google.gwt.user.client.Event.NativePreviewEvent)
         */
        public void onPreviewNativeEvent(NativePreviewEvent event) {

            Event nativeEvent = Event.as(event.getNativeEvent());
            switch (DOM.eventGetType(nativeEvent)) {
                case Event.ONMOUSEMOVE:
                    break;
                case Event.ONMOUSEUP:
                    break;
                case Event.ONMOUSEDOWN:
                    break;
                case Event.ONKEYUP:
                    if (m_selectionInput.m_textbox.getValue().length() > 0) {
                        close();
                    } else {
                        if (m_popup == null) {
                            open();
                        } else if (m_popup.isShowing()) {
                            close();
                        } else {
                            open();
                        }
                    }
                    break;
                case Event.ONMOUSEWHEEL:
                    close();
                    break;
                default:
                    // do nothing
            }
        }

    }

    /** The download mode of this widget. */
    public static final String DOWNLOAD = "download";

    /** The download link mode of this widget. */
    public static final String DOWNLOAD_LINK = "download_link";

    /** The file link mode of this widget. */
    public static final String FILE_LINK = "file_link";

    /** The group select mode of this widget. */
    public static final String GROUP = "group";

    /** The HTML mode of this widget. */
    public static final String HTML = "html";

    /** The image link mode of this widget. */
    public static final String IMAGE_LINK = "image_link";

    /** The link mode of this widget. */
    public static final String LINK = "link";

    /** The OrgUnit mode of this widget. */
    public static final String ORGUNIT = "orgunit";

    /** The principal mode of this widget. */
    public static final String PRINCIPAL = "principal";

    /** The table mode of this widget. */
    public static final String TABLE = "table";

    /** A counter used for giving text box widgets ids. */
    private static int idCounter;

    /** The old value. */
    protected String m_oldValue = "";

    /** The popup frame. */
    protected CmsFramePopup m_popup;

    /** The handler registration. */
    protected HandlerRegistration m_previewHandlerRegistration;

    /** The default rows set. */
    int m_defaultRows;

    /** The root panel containing the other components of this widget. */
    Panel m_panel = new FlowPanel();

    /** The container for the text area. */
    CmsSelectionInput m_selectionInput;

    /** The configuration string. */
    private String m_config;

    /** The error display for this widget. */
    private CmsErrorWidget m_error = new CmsErrorWidget();

    /** The field id. */
    private String m_id;

    /** The selection type. */
    private String m_type;

    /**
     * VsfSelection widget to open the gallery selection.<p>
     * @param iconImage the image of the icon shown in the
     * @param type the type of this widget
     * @param config the configuration for this widget
     */
    public CmsVfsSelection(String iconImage, String type, String config) {

        initWidget(m_panel);
        m_type = type;
        m_config = config;
        m_selectionInput = new CmsSelectionInput(iconImage);
        m_id = "CmsVfsSelection_" + (idCounter++);
        m_selectionInput.m_textbox.getElement().setId(m_id);

        m_panel.add(m_selectionInput);
        m_panel.add(m_error);

        m_selectionInput.m_textbox.addMouseUpHandler(new MouseUpHandler() {

            public void onMouseUp(MouseUpEvent event) {

                m_selectionInput.hideFader();
                setTitle("");
                if (m_popup == null) {
                    open();
                } else if (m_popup.isShowing()) {
                    close();
                } else {
                    open();
                }

            }

        });
        m_selectionInput.m_textbox.addBlurHandler(new BlurHandler() {

            public void onBlur(BlurEvent event) {

                if ((m_selectionInput.m_textbox.getValue().length()
                    * 6.88) > m_selectionInput.m_textbox.getOffsetWidth()) {
                    setTitle(m_selectionInput.m_textbox.getValue());
                }
                m_selectionInput.showFader();
            }
        });
        m_selectionInput.setOpenCommand(new Command() {

            public void execute() {

                if (m_popup == null) {
                    open();
                } else if (m_popup.isShowing()) {
                    close();
                } else {
                    open();
                }

            }
        });
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return m_selectionInput.m_textbox.addValueChangeHandler(handler);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getApparentValue()
     */
    public String getApparentValue() {

        return getFormValueAsString();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFieldType()
     */
    public FieldType getFieldType() {

        return I_CmsFormWidget.FieldType.STRING;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValue()
     */
    public Object getFormValue() {

        if (m_selectionInput.m_textbox.getText() == null) {
            return "";
        }
        return m_selectionInput.m_textbox.getValue();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    public String getFormValueAsString() {

        return (String)getFormValue();
    }

    /**
     * Returns the selected link as a bean.<p>
     *
     * @return the selected link as a bean
     */
    public CmsLinkBean getLinkBean() {

        String link = m_selectionInput.m_textbox.getValue();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(link)) {
            return null;
        }
        return new CmsLinkBean(m_selectionInput.m_textbox.getText(), true);
    }

    /**
     * Returns the text contained in the text area.<p>
     *
     * @return the text in the text area
     */
    public String getText() {

        return m_selectionInput.m_textbox.getValue();
    }

    /**
     * Returns the text box container of this widget.<p>
     *
     * @return the text box container
     */
    public CmsSelectionInput getTextAreaContainer() {

        return m_selectionInput;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        return m_selectionInput.m_textbox.isEnabled();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        m_selectionInput.m_textbox.setText("");
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setAutoHideParent(org.opencms.gwt.client.ui.I_CmsAutoHider)
     */
    public void setAutoHideParent(I_CmsAutoHider autoHideParent) {

        // nothing to do
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        m_selectionInput.m_textbox.setEnabled(enabled);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String errorMessage) {

        m_error.setText(errorMessage);
    }

    /**
     * Sets the value of the widget.<p>
     *
     * @param value the new value
     */
    public void setFormValue(Object value) {

        if (value == null) {
            value = "";
        }
        if (value instanceof String) {
            String strValue = (String)value;
            m_selectionInput.m_textbox.setText(strValue);
            setTitle(strValue);
        }

    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
    public void setFormValueAsString(String newValue) {

        setFormValue(newValue);
    }

    /**
     * Sets the link from a bean.<p>
     *
     * @param link the link bean
     */
    public void setLinkBean(CmsLinkBean link) {

        if (link == null) {
            link = new CmsLinkBean("", true);
        }
        m_selectionInput.m_textbox.setValue(link.getLink());
    }

    /**
     * Sets the name of the input field.<p>
     *
     * @param name of the input field
     * */
    public void setName(String name) {

        m_selectionInput.m_textbox.setName(name);

    }

    /**
     * Sets the text in the text area.<p>
     *
     * @param text the new text
     */
    public void setText(String text) {

        m_selectionInput.m_textbox.setValue(text);
    }

    /**
     * @see com.google.gwt.user.client.ui.UIObject#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title) {

        m_selectionInput.m_textbox.getElement().setTitle(title);
    }

    /**
     * Creates the URL for the gallery dialog IFrame.<p>
     *
     * @return the URL for the gallery dialog IFrame
     */
    protected String buildGalleryUrl() {

        String basePath = "";
        if (m_type.equals(LINK)
            || m_type.equals(HTML)
            || m_type.equals(TABLE)
            || m_type.equals(PRINCIPAL)
            || m_type.equals(GROUP)) {
            if (m_type.equals(LINK)) {
                basePath = "/system/workplace/galleries/linkgallery/index.jsp?dialogmode=widget&fieldid=" + m_id;
            } else if (m_type.equals(HTML)) {
                basePath = "/system/workplace/galleries/htmlgallery/index.jsp?dialogmode=widget&fieldid=" + m_id;
            } else if (m_type.equals(TABLE)) {
                basePath = "/system/workplace/galleries/tablegallery/index.jsp?dialogmode=widget&fieldid=" + m_id;
            } else if (m_type.equals(PRINCIPAL)) {
                basePath = "/system/workplace/commons/principal_selection.jsp?dialogmode=widget&useparent=true&fieldid="
                    + m_id;
            } else if (m_type.equals(GROUP)) {
                basePath = "/system/workplace/commons/group_selection.jsp?type=groupwidget&fieldid=" + m_id;
            } else {
                basePath = "/system/workplace/galleries/" + m_type + "gallery/index.jsp";
            }
        } else {
            basePath = "/system/workplace/commons/gallery.jsp";
            basePath += "?dialogmode=widget&fieldid=" + m_id;
        }

        String pathparameter = m_selectionInput.m_textbox.getText();
        if (pathparameter.indexOf("/") > -1) {
            basePath += "&currentelement=" + pathparameter;
        }
        basePath += m_config;

        //basePath += "&gwt.codesvr=127.0.0.1:9996"; //to start the hosted mode just remove commentary
        return CmsCoreProvider.get().link(basePath);
    }

    /**
     * Close the popup of this widget.<p>
     * */
    protected void close() {

        m_popup.hideDelayed();
        m_selectionInput.m_textbox.setFocus(true);
        m_selectionInput.m_textbox.setCursorPos(m_selectionInput.m_textbox.getText().length());
    }

    /**
     * Opens the popup of this widget.<p>
     * */
    protected void open() {

        m_oldValue = m_selectionInput.m_textbox.getValue();
        if (m_popup == null) {
            String title = org.opencms.gwt.client.Messages.get().key(
                org.opencms.gwt.client.Messages.GUI_GALLERY_SELECT_DIALOG_TITLE_0);
            m_popup = new CmsFramePopup(title, buildGalleryUrl());

            m_popup.setCloseHandler(new Runnable() {

                public void run() {

                    String textboxValue = m_selectionInput.m_textbox.getText();

                    if (!m_oldValue.equals(textboxValue)) {
                        m_selectionInput.m_textbox.setValue("", true);
                        m_selectionInput.m_textbox.setValue(textboxValue, true);
                    }

                    if (m_previewHandlerRegistration != null) {
                        m_previewHandlerRegistration.removeHandler();
                        m_previewHandlerRegistration = null;
                    }
                    m_selectionInput.m_textbox.setFocus(true);
                    m_selectionInput.m_textbox.setCursorPos(m_selectionInput.m_textbox.getText().length());
                }
            });
            m_popup.setModal(false);
            m_popup.setId(m_id);
            m_popup.setWidth(717);

            if (m_type.equals(DOWNLOAD)) {
                m_popup.getFrame().setSize("705px", "640px");
            } else if (m_type.equals(HTML)) {
                m_popup.getFrame().setSize("705px", "640px");
            } else if (m_type.equals(LINK)) {
                m_popup.getFrame().setSize("705px", "640px");
            } else if (m_type.equals(TABLE)) {
                m_popup.getFrame().setSize("705px", "640px");
            } else if (m_type.equals(PRINCIPAL)) {
                exportSetPrincipalFunction();
                m_popup.getFrame().setSize("705px", "320px");
            } else {
                m_popup.getFrame().setSize("705px", "485px");
            }

            m_popup.addDialogClose(new Command() {

                public void execute() {

                    close();

                }
            });
        } else {
            m_popup.getFrame().setUrl(buildGalleryUrl());
        }
        m_popup.setAutoHideEnabled(true);
        m_popup.center();
        if (m_previewHandlerRegistration == null) {
            m_previewHandlerRegistration = Event.addNativePreviewHandler(new CloseEventPreviewHandler());
        }
    }

    /**
     * Exporting the set principal function to the window scope.<p>
     */
    private native void exportSetPrincipalFunction()/*-{
        var self = this;
        $wnd.setPrincipalFormValue = function(typeFlag, principal) {
            self.@org.opencms.gwt.client.ui.input.CmsVfsSelection::setFormValueAsString(Ljava/lang/String;)(principal);
            self.@org.opencms.gwt.client.ui.input.CmsVfsSelection::close()();
        }
    }-*/;
}
