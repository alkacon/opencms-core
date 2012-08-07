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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;
import org.opencms.gwt.shared.CmsLinkBean;
import org.opencms.util.CmsStringUtil;

import java.util.Map;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Basic gallery widget for forms.<p>
 * 
 * @since 8.0.0
 * 
 */
public class CmsVfsSelection extends Composite implements I_CmsFormWidget, I_CmsHasInit {

    /** Inner class for the open button. */
    protected class OpenButton extends CmsPushButton {

        /**
         * Default constructor.<p>
         * @param imageClass 
         */
        public OpenButton(String imageClass) {

            super(imageClass);
            setStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().openVfsButton());

        }

    }

    /** The filelink mode of this widget. */
    public static final String FILE_LINK = "file_link";

    /** The imagelink mode of this widget. */
    public static final String IMAGE_LINK = "image_link";

    /** The widget type identifier for this widget. */
    private static final String WIDGET_TYPE = "vfsselection";

    /** The error display for this widget. */
    private CmsErrorWidget m_error = new CmsErrorWidget();

    /** The root panel containing the other components of this widget. */
    Panel m_panel = new FlowPanel();

    /** The internal text area widget used by this widget. */
    TextBox m_textBox;

    /** The container for the text area. */
    FlowPanel m_textBoxContainer = new FlowPanel();

    /** The default rows set. */
    int m_defaultRows;

    /** The faid panel. */
    protected Panel m_faidpanel = new SimplePanel();

    /** The button to to open the selection. */
    private OpenButton m_openSelection;

    /** THe popup frame. */
    protected CmsFramePopup m_popup;

    /** A counter used for giving text box widgets ids. */
    private static int idCounter;

    /** The x-coords of the popup. */
    protected int m_xcoordspopup;

    /** The y-coords of the popup. */
    protected int m_ycoordspopup;

    /***/
    protected HandlerRegistration m_previewHandlerRegistration;

    /***/
    private String m_id;

    /***/
    protected String m_oldValue = "";

    /**
     * TextBox widget to open the gallery selection.<p>
     * @param iconImage the image of the icon shown in the 
     * @param type 
     */
    public CmsVfsSelection(String iconImage, String type) {

        super();
        m_textBox = new TextBox();
        m_id = "CmsVfsSelection_" + (idCounter++);
        if (type.equals(FILE_LINK)) {
            m_textBox.getElement().setId("CmsVfsSelection_" + (idCounter++));
        } else if (type.equals(IMAGE_LINK)) {
            m_textBox.getElement().setId("img.CmsVfsSelection_" + (idCounter++));
        }
        m_openSelection = new OpenButton(iconImage);

        m_textBoxContainer.add(m_openSelection);
        creatFaider();
        initWidget(m_panel);
        m_panel.add(m_textBoxContainer);
        m_faidpanel.setStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().vfsInputBoxFaider());
        m_faidpanel.getElement().getStyle().setRight(21, Unit.PX);
        m_faidpanel.getElement().getStyle().setCursor(Cursor.TEXT);
        m_faidpanel.getElement().getStyle().setBottom(7, Unit.PX);

        m_textBoxContainer.add(m_textBox);
        m_faidpanel.addDomHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                m_textBox.setFocus(true);
            }
        }, ClickEvent.getType());
        m_panel.add(m_error);
        m_textBoxContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());

        m_textBox.addMouseUpHandler(new MouseUpHandler() {

            public void onMouseUp(MouseUpEvent event) {

                m_textBoxContainer.remove(m_faidpanel);
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
        m_textBox.addBlurHandler(new BlurHandler() {

            public void onBlur(BlurEvent event) {

                if ((m_textBox.getValue().length() * 6.88) > m_textBox.getOffsetWidth()) {
                    m_textBoxContainer.add(m_faidpanel);
                    setTitle(m_textBox.getValue());
                }
                m_textBox.setCursorPos(0);
            }
        });

        m_openSelection.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

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
     * Initializes this class.<p>
     */
    public static void initClass() {

        // registers a factory for creating new instances of this widget
        CmsWidgetFactoryRegistry.instance().registerFactory(WIDGET_TYPE, new I_CmsFormWidgetFactory() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory#createWidget(java.util.Map)
             */
            public I_CmsFormWidget createWidget(Map<String, String> widgetParams) {

                return new CmsTextArea();
            }
        });
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

        if (m_textBox.getText() == null) {
            return "";
        }
        return m_textBox.getValue();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    public String getFormValueAsString() {

        return (String)getFormValue();
    }

    /**
     * Returns the text contained in the text area.<p>
     * 
     * @return the text in the text area
     */
    public String getText() {

        return m_textBox.getValue();
    }

    /**
     * Returns the textarea of this widget.<p>
     * 
     * @return the textarea
     */
    public TextBox getTextArea() {

        return m_textBox;
    }

    /**
     * Returns the text box container of this widget.<p>
     * 
     * @return the text box container
     */
    public FlowPanel getTextAreaContainer() {

        return m_textBoxContainer;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        return m_textBox.isEnabled();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        m_textBox.setText("");
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

        m_textBox.setEnabled(enabled);
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
            m_textBox.setText(strValue);
            creatFaider();
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
     * Sets the text in the text area.<p>
     * 
     * @param text the new text
     */
    public void setText(String text) {

        m_textBox.setValue(text);
    }

    /**
     * @param handler
     */
    public void addValueChangeHandler(ValueChangeHandler<String> handler) {

        m_textBox.addValueChangeHandler(handler);
    }

    /**
     * @see com.google.gwt.user.client.ui.UIObject#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title) {

        if ((title.length() * 6.88) > m_panel.getOffsetWidth()) {
            m_textBox.getElement().setTitle(title);
        } else {
            m_textBox.getElement().setTitle("");
        }
    }

    /**
     * Opens the popup of this widget.<p>
     * */
    protected void open() {

        m_oldValue = m_textBox.getValue();
        if (m_popup == null) {
            String title = org.opencms.gwt.client.Messages.get().key(
                org.opencms.gwt.client.Messages.GUI_GALLERY_SELECT_DIALOG_TITLE_0);
            m_popup = new CmsFramePopup(title, buildGalleryUrl());

            m_popup.setCloseHandler(new Runnable() {

                public void run() {

                    String textboxValue = m_textBox.getText();

                    if (!m_oldValue.equals(textboxValue)) {
                        m_textBox.setValue("", true);
                        m_textBox.setValue(textboxValue, true);
                    }

                    if (m_previewHandlerRegistration != null) {
                        m_previewHandlerRegistration.removeHandler();
                        m_previewHandlerRegistration = null;
                    }
                }
            });
            m_popup.setModal(false);
            m_popup.setId(m_id);
            m_popup.setHeight(530);
            m_popup.setWidth(715);
            m_popup.getFrame().setSize("710px", "530px");
            m_popup.addDialogClose(new Command() {

                public void execute() {

                    close();

                }
            });
        } else {
            m_popup.getFrame().setUrl(buildGalleryUrl());
        }
        m_popup.showRelativeTo(m_textBox);
        m_previewHandlerRegistration = Event.addNativePreviewHandler(new CloseEventPreviewHandler());

        m_xcoordspopup = m_popup.getPopupLeft();
        m_ycoordspopup = m_popup.getPopupTop();

    }

    /**
     * Close the popup of this widget.<p>
     * */
    protected void close() {

        m_popup.hideDelayed();
    }

    /**
     * Adds the fader if necessary.<p> 
     * */
    private void creatFaider() {

        if ((m_textBox.getValue().length() * 6.88) > m_textBox.getOffsetWidth()) {
            m_textBoxContainer.add(m_faidpanel);
        }
    }

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
                    int x_coord = nativeEvent.getClientX();
                    int y_coord = (nativeEvent.getClientY() + Window.getScrollTop());

                    if (((x_coord > (m_xcoordspopup + 715)) || (x_coord < (m_xcoordspopup)))
                        || ((y_coord > ((m_ycoordspopup + 530))) || (y_coord < ((m_ycoordspopup - m_textBox.getOffsetHeight()))))) {
                        close();
                    }
                    break;
                case Event.ONKEYUP:
                    if (m_textBox.getValue().length() > 0) {
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

    /**
     * Sets the link from a bean.<p>
     * 
     * @param link the link bean 
     */
    public void setLinkBean(CmsLinkBean link) {

        if (link == null) {
            link = new CmsLinkBean("", true);
        }
        m_textBox.setValue(link.getLink());
    }

    /**
     * Returns the selected link as a bean.<p>
     * 
     * @return the selected link as a bean 
     */
    public CmsLinkBean getLinkBean() {

        String link = m_textBox.getValue();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(link)) {
            return null;
        }
        return new CmsLinkBean(m_textBox.getText(), true);
    }

    /**
     * Creates the URL for the gallery dialog IFrame.<p>
     * 
     * @return the URL for the gallery dialog IFrame 
     */
    protected String buildGalleryUrl() {

        String basePath = "/system/modules/org.opencms.ade.galleries/gallery.jsp";
        basePath += "?dialogmode=widget&fieldid=" + m_id;
        String pathparameter = m_textBox.getText();
        if (pathparameter.indexOf("/") > -1) {
            basePath += "&gallerypath=" + pathparameter.substring(0, pathparameter.lastIndexOf("/") + 1);
        }

        return CmsCoreProvider.get().link(basePath);
    }
}
