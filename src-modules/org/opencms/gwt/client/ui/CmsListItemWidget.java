/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsListItemWidget.java,v $
 * Date   : $Date: 2011/05/20 11:54:40 $
 * Version: $Revision: 1.52 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.db.CmsResourceState;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsListItemWidgetCss;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsResourceStateUtil;
import org.opencms.gwt.client.util.CmsStyleVariable;
import org.opencms.gwt.shared.CmsAdditionalInfoBean;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasDoubleClickHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a UI list item.<p>
 * 
 * @author Tobias Herrmann
 * @author Michael Moossen
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.52 $
 * 
 * @since 8.0.0
 */
public class CmsListItemWidget extends Composite
implements HasMouseOutHandlers, HasClickHandlers, HasDoubleClickHandlers, HasMouseOverHandlers, I_CmsTruncable {

    /** Additional info item HTML. */
    public static class AdditionalInfoItem extends Composite implements I_CmsTruncable {

        /** Text metrics key. */
        private static final String TMA_TITLE = "AddInfoTitle";

        /** Text metrics key. */
        private static final String TMA_VALUE = "AddInfoValue";

        /** The title element. */
        private CmsLabel m_titleLabel;

        /** The value element. */
        private CmsLabel m_valueLabel;

        /**
         * Constructor.<p>
         * 
         * @param additionalInfo the info to display
         */
        public AdditionalInfoItem(CmsAdditionalInfoBean additionalInfo) {

            this(additionalInfo.getName(), additionalInfo.getValue(), additionalInfo.getStyle());
        }

        /**
         * Constructor.<p>
         * 
         * @param title info title
         * @param value info value
         * @param additionalStyle an additional class name
         */
        public AdditionalInfoItem(String title, String value, String additionalStyle) {

            super();
            FlowPanel panel = new FlowPanel();
            initWidget(panel);
            I_CmsListItemWidgetCss style = I_CmsLayoutBundle.INSTANCE.listItemWidgetCss();
            // create title
            m_titleLabel = new CmsLabel(CmsStringUtil.isEmptyOrWhitespaceOnly(title) ? "" : title + ":");
            m_titleLabel.addStyleName(style.itemAdditionalTitle());
            panel.add(m_titleLabel);
            // create value
            m_valueLabel = new CmsLabel();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
                m_valueLabel.setHTML(CmsDomUtil.Entity.nbsp.html());
            } else {
                m_valueLabel.setHTML(value);
            }
            m_valueLabel.addStyleName(style.itemAdditionalValue());
            if (additionalStyle != null) {
                m_valueLabel.addStyleName(additionalStyle);
            }
            panel.add(m_valueLabel);
        }

        /**
         * Returns the title element.<p>
         *
         * @return the title element
         */
        public CmsLabel getTitleLabel() {

            return m_titleLabel;
        }

        /**
         * Returns the value element.<p>
         *
         * @return the value element
         */
        public CmsLabel getValueLabel() {

            return m_valueLabel;
        }

        /**
         * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
         */
        public void truncate(String textMetricsPrefix, int widgetWidth) {

            // width fixed by css to 90 see I_CmsListItemWidgetCss#itemAdditionalTitle
            m_titleLabel.truncate(textMetricsPrefix + TMA_TITLE, 85);
            // the rest
            m_valueLabel.truncate(textMetricsPrefix + TMA_VALUE, widgetWidth - 100);
        }
    }

    /** Background color values. */
    public enum Background {
        /** Color blue. */
        BLUE,
        /** Default color. */
        DEFAULT,
        /** Color red. */
        RED,
        /** Color yellow. */
        YELLOW
    }

    /**
     * The interface for handling edits of the title field.<p>
     */
    public interface I_CmsTitleEditHandler {

        /** 
         * This method is called when the user has finished editing the title field.<p>
         * 
         * @param title the label containing the title 
         * @param box the 
         */
        void handleEdit(CmsLabel title, TextBox box);
    }

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    protected interface I_CmsListItemWidgetUiBinder extends UiBinder<CmsHoverPanel, CmsListItemWidget> {
        // GWT interface, nothing to do here
    }

    /** The CSS class to set the additional info open. */
    protected static final String OPENCLASS = I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().open();

    /** Text metrics key. */
    private static final String TM_SUBTITLE = "Subtitle";

    /** Text metrics key. */
    private static final String TM_TITLE = "Title";

    /** The ui-binder instance for this class. */
    private static I_CmsListItemWidgetUiBinder uiBinder = GWT.create(I_CmsListItemWidgetUiBinder.class);

    /** DIV for additional item info. */
    @UiField
    protected FlowPanel m_additionalInfo;

    /** Panel to hold buttons.*/
    @UiField
    protected FlowPanel m_buttonPanel;

    /** Panel to hold the content.*/
    @UiField
    protected FlowPanel m_contentPanel;

    /** The DIV showing the list icon. */
    @UiField
    protected SimplePanel m_iconPanel;

    /** The open-close button for the additional info. */
    protected CmsPushButton m_openClose;

    /** Sub title label. */
    @UiField
    protected CmsLabel m_subtitle;

    /** A label which is optionally displayed after the subtitle. */
    protected CmsLabel m_subtitleSuffix;

    /** Title label. */
    @UiField
    protected CmsLabel m_title;

    /** The title row, holding the title and the open-close button for the additional info. */
    @UiField
    protected FlowPanel m_titleRow;

    /** Variable for the background style. */
    private CmsStyleVariable m_backgroundStyle;

    /** The child width in px for truncation. */
    private int m_childWidth;

    /** The fixed icon classes which will always be added if the icon classes are set. */
    private String m_fixedIconClasses = "";

    /** The event handler registrations. */
    private List<HandlerRegistration> m_handlerRegistrations;

    /** The handler registration for the click handler on the title field. */
    private HandlerRegistration m_titleClickHandlerRegistration;

    /** A handler object for handling editing of the title field. */
    private I_CmsTitleEditHandler m_titleEditHandler;

    /** The text metrics prefix. */
    private String m_tmPrefix;

    /**
     * Constructor. Using a 'li'-tag as default root element.<p>
     * 
     * @param infoBean bean holding the item information
     */
    public CmsListItemWidget(CmsListInfoBean infoBean) {

        initWidget(uiBinder.createAndBindUi(this));
        m_handlerRegistrations = new ArrayList<HandlerRegistration>();
        m_backgroundStyle = new CmsStyleVariable(this);
        m_subtitleSuffix = new CmsLabel();
        m_subtitleSuffix.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().inlineBlock());
        init(infoBean);
    }

    /**
     * Adds an additional info item to the list.<p>
     * 
     * @param additionalInfo the additional info to display
     */
    public void addAdditionalInfo(CmsAdditionalInfoBean additionalInfo) {

        m_additionalInfo.add(new AdditionalInfoItem(additionalInfo));
        ensureOpenCloseAdditionalInfo();
    }

    /**
     * Adds a widget to the button panel.<p>
     * 
     * @param w the widget to add
     */
    public void addButton(Widget w) {

        m_buttonPanel.add(w);
    }

    /**
     * Adds a widget to the front of the button panel.<p>
     * 
     * @param w the widget to add
     */
    public void addButtonToFront(Widget w) {

        m_buttonPanel.insert(w, 0);
    }

    /**
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(ClickHandler)
     */
    public HandlerRegistration addClickHandler(ClickHandler handler) {

        return addDomHandler(handler, ClickEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasDoubleClickHandlers#addDoubleClickHandler(com.google.gwt.event.dom.client.DoubleClickHandler)
     */
    public HandlerRegistration addDoubleClickHandler(DoubleClickHandler handler) {

        return addDomHandler(handler, DoubleClickEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOutHandlers#addMouseOutHandler(com.google.gwt.event.dom.client.MouseOutHandler)
     */
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {

        HandlerRegistration req = addDomHandler(handler, MouseOutEvent.getType());
        m_handlerRegistrations.add(req);
        return req;

    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOverHandlers#addMouseOverHandler(com.google.gwt.event.dom.client.MouseOverHandler)
     */
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {

        HandlerRegistration req = addDomHandler(handler, MouseOverEvent.getType());
        m_handlerRegistrations.add(req);
        return req;
    }

    /**
     * Adds a style name to the subtitle label.<p>
     * 
     * @param styleName the style name to add 
     */
    public void addSubtitleStyleName(String styleName) {

        m_subtitle.addStyleName(styleName);
    }

    /**
     * Adds a style name to the title label.<p> 
     * 
     * @param styleName the style name to add 
     */
    public void addTitleStyleName(String styleName) {

        m_title.addStyleName(styleName);
    }

    /**
     * Hides the icon of the list item widget.<p>
     */
    public void clearIcon() {

        m_iconPanel.setVisible(false);
    }

    /**
     * Returns the button at the given position.<p>
     * 
     * @param index the button index
     * 
     * @return the button at the given position
     */
    public Widget getButton(int index) {

        return m_buttonPanel.getWidget(index);
    }

    /**
     * Returns the content panel.<p>
     *
     * @return the content panel
     */
    public FlowPanel getContentPanel() {

        return m_contentPanel;
    }

    /**
     * Returns the number of buttons.<p>
     * 
     * @return the number of buttons
     */
    public int getCountButtons() {

        return m_buttonPanel.getWidgetCount();
    }

    /**
     * Returns the subtitle label.<p>
     *
     * @return the subtitle label
     */
    public String getSubtitleLabel() {

        return m_subtitle.getText();
    }

    /**
     * Returns the label after the subtitle.<p>
     * 
     * @return the label after the subtitle
     */
    public CmsLabel getSubTitleSuffix() {

        return m_subtitleSuffix;
    }

    /**
     * Returns the title label text.<p>
     * 
     * @return the title label text
     */
    public String getTitleLabel() {

        return m_title.getText();
    }

    /**
     * Returns if additional info items are present.<p>
     * 
     * @return <code>true</code> if additional info items are present
     */
    public boolean hasAdditionalInfo() {

        return m_additionalInfo.getWidgetCount() > 0;
    }

    /**
     * Removes a widget from the button panel.<p>
     * 
     * @param w the widget to remove
     */
    public void removeButton(Widget w) {

        m_buttonPanel.remove(w);
    }

    /**
     * Removes all registered mouse event handlers including the context menu handler.<p>
     */
    public void removeMouseHandlers() {

        Iterator<HandlerRegistration> it = m_handlerRegistrations.iterator();
        while (it.hasNext()) {
            it.next().removeHandler();
        }
        m_handlerRegistrations.clear();
    }

    /**
     * Removes a style name from the subtitle label.<p>
     * 
     * @param styleName the style name to add 
     */
    public void removeSubtitleStyleName(String styleName) {

        m_subtitle.removeStyleName(styleName);
    }

    /**
     * Removes a style name from the title label.<p> 
     * 
     * @param styleName the style name to add 
     */
    public void removeTitleStyleName(String styleName) {

        m_title.removeStyleName(styleName);
    }

    /**
     * Sets the additional info value label at the given position.<p>
     * 
     * @param index the additional info index
     * @param label the new value to set
     */
    public void setAdditionalInfoValue(int index, String label) {

        ((AdditionalInfoItem)m_additionalInfo.getWidget(index)).getValueLabel().setText(label);
    }

    /**
     * Sets the additional info visible if present.<p>
     * 
     * @param visible <code>true</code> to show, <code>false</code> to hide 
     */
    public void setAdditionalInfoVisible(boolean visible) {

        if (m_openClose == null) {
            return;
        }
        if (visible) {
            addStyleName(CmsListItemWidget.OPENCLASS);
            m_openClose.setDown(true);
        } else {
            removeStyleName(CmsListItemWidget.OPENCLASS);
            m_openClose.setDown(false);
        }
    }

    /**
     * Sets the background color.<p>
     * 
     * @param background the color
     */
    public void setBackground(Background background) {

        switch (background) {
            case BLUE:
                m_backgroundStyle.setValue(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().itemBlue());
                break;
            case RED:
                m_backgroundStyle.setValue(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().itemRed());
                break;
            case YELLOW:
                m_backgroundStyle.setValue(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().itemYellow());
                break;
            case DEFAULT:
            default:
                m_backgroundStyle.setValue(null);
        }
    }

    /**
     * Sets the icon classes which will be always implicitly set if the method {@link #setIcon(String)} is called.<p>
     * 
     * @param fixedIconClasses the fixed icon classes, separated by spaces
     */
    public void setFixedIconClasses(String fixedIconClasses) {

        if (CmsStringUtil.isEmpty(fixedIconClasses)) {
            m_fixedIconClasses = "";
        } else {
            m_fixedIconClasses = fixedIconClasses;
        }
    }

    /**
     * Sets the icon of this item.<p>
     * 
     * @param image the image to use as icon
     */
    public void setIcon(Image image) {

        m_iconPanel.setVisible(true);
        if (image == null) {
            return;
        }
        m_iconPanel.setWidget(image);
    }

    /**
     * Sets the icon for this item using the given CSS classes.<p>
     * 
     * @param iconClasses the CSS classes
     */
    public void setIcon(String iconClasses) {

        m_iconPanel.setVisible(true);
        Panel iconWidget = new SimplePanel();
        m_iconPanel.setWidget(iconWidget);
        iconWidget.addStyleName(iconClasses + " " + m_fixedIconClasses);
    }

    /**
     * Sets the icon title.<p>
     * 
     * @param title the new icon title
     */
    public void setIconTitle(String title) {

        m_iconPanel.setTitle(title);
    }

    /**
     * Sets the subtitle label text.<p>
     * 
     * @param label the new subtitle to set
     */
    public void setSubtitleLabel(String label) {

        m_subtitle.setText(label);
    }

    /**
     * Sets the subtitle suffix text, and hides or displays the subtitle suffix depending on whether
     * the text is null or not null.<p>
     * 
     * @param text the text to put into the subtitle suffix 
     */
    public void setSubtitleSuffixText(String text) {

        if (text == null) {
            if (m_subtitleSuffix.getParent() != null) {
                m_subtitleSuffix.removeFromParent();
            }
        } else {
            if (m_subtitleSuffix.getParent() == null) {
                m_titleRow.add(m_subtitleSuffix);
            }
            m_subtitleSuffix.setText(text);
        }
        updateTruncation();
    }

    /**
     * Enables or disabled editing of the title field.<p>
     * 
     * @param editable if true, makes the title field editable 
     */
    public void setTitleEditable(boolean editable) {

        boolean alreadyEditable = m_titleClickHandlerRegistration != null;
        if (alreadyEditable == editable) {
            return;
        }
        if (!editable) {
            m_titleClickHandlerRegistration.removeHandler();
            m_titleClickHandlerRegistration = null;

        } else {
            m_titleClickHandlerRegistration = m_title.addClickHandler(new ClickHandler() {

                /**
                 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
                 */
                public void onClick(ClickEvent event) {

                    editTitle();
                }
            });
        }

    }

    /**
     * Sets the handler for editing the list item widget's title.
     * 
     * @param handler the new title editing handler 
     */
    public void setTitleEditHandler(I_CmsTitleEditHandler handler) {

        m_titleEditHandler = handler;
    }

    /**
     * Sets the title label text.<p>
     * 
     * @param label the new title to set
     */
    public void setTitleLabel(String label) {

        m_title.setText(label);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsPrefix, int widgetWidth) {

        m_childWidth = widgetWidth;
        m_tmPrefix = textMetricsPrefix;
        int width = widgetWidth - 4; // just to be on the save side
        if (m_openClose != null) {
            width -= 16;
        }
        if (m_iconPanel.isVisible()) {
            width -= 32;
        }
        if (width < 0) {
            // IE fails with a JS error if the width is negative 
            width = 0;
        }
        if (m_iconPanel.isVisible()) {
            m_titleRow.getElement().getStyle().setMarginLeft(32, Unit.PX);

        } else {
            m_titleRow.getElement().getStyle().clearMargin();
        }

        // m_titleRow.getElement().getStyle().setWidth(width, Unit.PX);
        m_title.truncate(textMetricsPrefix + TM_TITLE, width - 10);
        if (m_subtitleSuffix.getParent() != null) {
            m_subtitleSuffix.truncate(textMetricsPrefix + "_STSUFFIX", 150);
            m_subtitle.truncate(textMetricsPrefix + TM_SUBTITLE, width - 160);
        } else {
            m_subtitle.truncate(textMetricsPrefix + TM_SUBTITLE, width - 10);
        }
        for (Widget addInfo : m_additionalInfo) {
            ((AdditionalInfoItem)addInfo).truncate(textMetricsPrefix, widgetWidth - 10);
        }
    }

    /**
     * Updates the truncation of labels if needed.<p>
     * 
     * Use after changing any text on the widget.<p>
     */
    public void updateTruncation() {

        truncate(m_tmPrefix, m_childWidth);
    }

    /**
     * Internal method which is called when the user clicks on an editable title field.<p>
     */
    protected void editTitle() {

        m_title.setVisible(false);
        final TextBox box = new TextBox();
        box.setText(m_title.getText());
        box.getElement().setAttribute("size", "45");
        box.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().labelInput());
        final String originalTitle = m_title.getText();
        // wrap the boolean flag in an array so we can change it from the event handlers 
        final boolean[] checked = new boolean[] {false};

        box.addBlurHandler(new BlurHandler() {

            /**
             * @see com.google.gwt.event.dom.client.BlurHandler#onBlur(com.google.gwt.event.dom.client.BlurEvent)
             */
            public void onBlur(BlurEvent event) {

                if (checked[0]) {
                    return;
                }

                onEditTitleTextBox(box);
                checked[0] = true;
            }
        });

        box.addKeyPressHandler(new KeyPressHandler() {

            /**
             * @see com.google.gwt.event.dom.client.KeyPressHandler#onKeyPress(com.google.gwt.event.dom.client.KeyPressEvent)
             */
            public void onKeyPress(KeyPressEvent event) {

                if (checked[0]) {
                    return;
                }

                int keycode = event.getNativeEvent().getKeyCode();

                if ((keycode == 10) || (keycode == 13)) {
                    onEditTitleTextBox(box);
                    checked[0] = true;
                }
                if (keycode == 27) {
                    box.setText(originalTitle);
                    onEditTitleTextBox(box);
                    checked[0] = true;

                }
            }
        });
        m_titleRow.insert(box, 1);
        box.setFocus(true);
    }

    /**
     * Ensures the open close button for the additional info list is present.<p>
     */
    protected void ensureOpenCloseAdditionalInfo() {

        if (m_openClose == null) {
            m_openClose = new CmsPushButton(
                I_CmsImageBundle.INSTANCE.style().triangleRight(),
                I_CmsImageBundle.INSTANCE.style().triangleDown());
            m_openClose.setButtonStyle(ButtonStyle.TRANSPARENT, null);
            m_titleRow.insert(m_openClose, 0);
            m_openClose.addClickHandler(new ClickHandler() {

                /**
                 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
                 */
                public void onClick(ClickEvent event) {

                    setAdditionalInfoVisible(!getElement().getClassName().contains(CmsListItemWidget.OPENCLASS));
                }
            });
        }
    }

    /**
     * Constructor.<p>
     * 
     * @param infoBean bean holding the item information
     */
    protected void init(CmsListInfoBean infoBean) {

        m_iconPanel.setVisible(false);
        m_title.setText(infoBean.getTitle());
        m_subtitle.setText(infoBean.getSubTitle());

        // set the resource type icon if present
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(infoBean.getResourceType())) {
            setIcon(CmsIconUtil.getResourceIconClasses(infoBean.getResourceType(), false));
        }

        // create the state info
        CmsResourceState state = infoBean.getResourceState();
        if (state != null) {
            String stateKey = Messages.get().key(Messages.GUI_RESOURCE_STATE_0);
            String stateValue = CmsResourceStateUtil.getStateName(state);
            String stateStyle = CmsResourceStateUtil.getStateStyle(state);
            infoBean.addAdditionalInfo(stateKey, stateValue, stateStyle);
        }

        // set the additional info
        if (infoBean.hasAdditionalInfo()) {
            ensureOpenCloseAdditionalInfo();
            for (CmsAdditionalInfoBean additionalInfo : infoBean.getAdditionalInfo()) {
                m_additionalInfo.add(new AdditionalInfoItem(additionalInfo));
            }
        }
    }

    /**
     * Internal method which is called when the user has finished editing the title.
     * 
     * @param box the text box which has been edited 
     */
    protected void onEditTitleTextBox(TextBox box) {

        if (m_titleEditHandler != null) {
            m_titleEditHandler.handleEdit(m_title, box);
            return;
        }

        String text = box.getText();
        box.removeFromParent();
        m_title.setText(text);
        m_title.setVisible(true);

    }
}
