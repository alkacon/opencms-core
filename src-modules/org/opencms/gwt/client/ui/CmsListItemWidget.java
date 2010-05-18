/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsListItemWidget.java,v $
 * Date   : $Date: 2010/05/18 12:31:13 $
 * Version: $Revision: 1.21 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsListItemWidgetCss;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
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
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a UI list item.<p>
 * 
 * @author Tobias Herrmann
 * @author Michael Moossen
 * 
 * @version $Revision: 1.21 $
 * 
 * @since 8.0.0
 */
public class CmsListItemWidget extends Composite implements HasMouseOutHandlers, HasMouseOverHandlers, I_CmsTruncable {

    /** Additional info item HTML. */
    protected static class AdditionalInfoItem extends CmsSimplePanel implements I_CmsTruncable {

        /** Text metrics key. */
        private static final String TMA_TITLE = "AddInfoTitle";

        /** Text metrics key. */
        private static final String TMA_VALUE = "AddInfoValue";

        /** The title element. */
        private CmsLabel m_titleElem;

        /** The value element. */
        private CmsLabel m_valueElem;

        /**
         * Constructor.<p>
         * 
         * @param title info title
         * @param value info value
         * @param additionalStyle an additional class name
         */
        AdditionalInfoItem(String title, String value, String additionalStyle) {

            super();
            I_CmsListItemWidgetCss style = I_CmsLayoutBundle.INSTANCE.listItemWidgetCss();
            // create title
            m_titleElem = new CmsLabel(title + ":");
            m_titleElem.addStyleName(style.itemAdditionalTitle());
            add(m_titleElem);
            // create value
            m_valueElem = new CmsLabel(value);
            if ((value == null) || (value.trim().length() == 0)) {
                m_valueElem.setHTML(CmsDomUtil.Entity.nbsp.html());
            }
            m_valueElem.addStyleName(style.itemAdditionalValue());
            if (additionalStyle != null) {
                m_valueElem.addStyleName(additionalStyle);
            }
            add(m_valueElem);
        }

        /**
         * Returns the title element.<p>
         *
         * @return the title element
         */
        public CmsLabel getTitleElem() {

            return m_titleElem;
        }

        /**
         * Returns the value element.<p>
         *
         * @return the value element
         */
        public CmsLabel getValueElem() {

            return m_valueElem;
        }

        /**
         * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
         */
        public void truncate(String textMetricsPrefix, int widgetWidth) {

            // width fixed by css to 90 see I_CmsListItemWidgetCss#itemAdditionalTitle
            m_titleElem.truncate(textMetricsPrefix + TMA_TITLE, 85);
            // the rest
            m_valueElem.truncate(textMetricsPrefix + TMA_VALUE, widgetWidth - 100);
        }
    }

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    protected interface I_CmsListItemWidgetUiBinder extends UiBinder<CmsHTMLHoverPanel, CmsListItemWidget> {
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
    protected CmsSimplePanel m_additionalInfo;

    /** Panel to hold buttons.*/
    @UiField
    protected FlowPanel m_buttonPanel;

    /** Panel to hold the content.*/
    @UiField
    protected CmsSimplePanel m_contentPanel;

    /** The DIV showing the list icon. */
    @UiField
    protected SimplePanel m_iconPanel;

    /** The open-close button for the additional info. */
    protected CmsPushButton m_openClose;

    /** Sub title label. */
    @UiField
    protected CmsLabel m_subtitle;

    /** Title label. */
    @UiField
    protected CmsLabel m_title;

    /** The title row, holding the title and the open-close button for the additional info. */
    @UiField
    protected FlowPanel m_titleRow;

    /** The child width in px for truncation. */
    private int m_childWidth;

    /** The event handler registrations. */
    private List<HandlerRegistration> m_handlerRegistrations;

    /** The text metrics prefix. */
    private String m_tmPrefix;

    /**
     * Constructor. Using a 'li'-tag as default root element.<p>
     * 
     * @param infoBean bean holding the item information
     */
    public CmsListItemWidget(CmsListInfoBean infoBean) {

        m_handlerRegistrations = new ArrayList<HandlerRegistration>();
        init(infoBean);
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
    public CmsSimplePanel getContentPanel() {

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
     * Sets the additional info value label at the given position.<p>
     * 
     * @param index the additional info index
     * @param label the new value to set
     */
    public void setAdditionalInfoValue(int index, String label) {

        ((AdditionalInfoItem)m_additionalInfo.getWidget(index)).getValueElem().setText(label);
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
        iconWidget.addStyleName(iconClasses);
        m_iconPanel.setWidget(iconWidget);
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
        m_title.truncate(textMetricsPrefix + TM_TITLE, width);
        m_subtitle.truncate(textMetricsPrefix + TM_SUBTITLE, width);
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
     * Constructor.<p>
     * 
     * @param infoBean bean holding the item information
     */
    protected void init(CmsListInfoBean infoBean) {

        CmsHTMLHoverPanel itemContent = uiBinder.createAndBindUi(this);
        initWidget(itemContent);
        m_iconPanel.setVisible(false);
        m_title.setText(infoBean.getTitle());
        m_subtitle.setText(infoBean.getSubTitle());
        if ((infoBean.getAdditionalInfo() != null) && (infoBean.getAdditionalInfo().size() > 0)) {
            m_openClose = new CmsPushButton(I_CmsButton.UiIcon.triangle_1_e, I_CmsButton.UiIcon.triangle_1_s);
            m_openClose.setShowBorder(false);
            m_titleRow.insert(m_openClose, 0);
            final CmsListItemWidget widget = this;
            m_openClose.addClickHandler(new ClickHandler() {

                /**
                 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
                 */
                public void onClick(ClickEvent event) {

                    if (widget.getStyleName().contains(CmsListItemWidget.OPENCLASS)) {
                        widget.removeStyleName(CmsListItemWidget.OPENCLASS);
                        m_openClose.setDown(false);
                    } else {
                        widget.addStyleName(CmsListItemWidget.OPENCLASS);
                        m_openClose.setDown(true);
                    }
                }
            });
            Iterator<Entry<String, String>> it = infoBean.getAdditionalInfo().entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, String> entry = it.next();
                String valueStyle = infoBean.getValueStyle(entry.getKey());
                AdditionalInfoItem info = new AdditionalInfoItem(entry.getKey(), entry.getValue(), valueStyle);
                m_additionalInfo.add(info);
            }
        }
    }
}
