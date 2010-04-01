/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsListItemWidget.java,v $
 * Date   : $Date: 2010/04/01 13:46:26 $
 * Version: $Revision: 1.6 $
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
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.Iterator;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a UI list item.<p>
 * 
 * @author Tobias Herrmann
 * @author Michael Moossen
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.0
 */
public class CmsListItemWidget extends Composite {

    /** Additional info item HTML. */
    protected static class AdditionalInfoItem extends CmsSimplePanel {

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
            m_titleElem.setTruncate(false);
            add(m_titleElem);
            // create value
            m_valueElem = new CmsLabel(value);
            m_valueElem.addStyleName(style.itemAdditionalValue());
            m_valueElem.setTruncate(false);
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
    }

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    protected interface I_CmsListItemWidgetUiBinder extends UiBinder<CmsHTMLHoverPanel, CmsListItemWidget> {
        // GWT interface, nothing to do here
    }

    /**
     * Click-handler to open/close the additional info.<p>
     */
    protected class OpenCloseHandler implements ClickHandler {

        /** The button. */
        private CmsImageButton m_button;

        /** If initialized. */
        private boolean m_init;

        /** The owner widget. */
        private Widget m_owner;

        /**
         * @param owner
         * @param button
         */
        public OpenCloseHandler(Widget owner, CmsImageButton button) {

            super();
            m_owner = owner;
            m_button = button;
        }

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            if (m_owner.getStyleName().contains(CmsListItemWidget.OPENCLASS)) {
                m_owner.removeStyleName(CmsListItemWidget.OPENCLASS);
                m_button.setDown(false);
            } else {
                m_owner.addStyleName(CmsListItemWidget.OPENCLASS);
                m_button.setDown(true);
                if (!m_init) {
                    m_init = true;
                    for (Widget w : m_additionalInfo) {
                        CmsLabel valueElem = ((AdditionalInfoItem)w).getValueElem();
                        valueElem.setTruncate(true);
                        valueElem.widthCheck();
                    }
                }
            }
        }
    }

    /** The CSS class to set the additional info open. */
    protected static final String OPENCLASS = I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().open();

    /** The ui-binder instance for this class. */
    private static I_CmsListItemWidgetUiBinder uiBinder = GWT.create(I_CmsListItemWidgetUiBinder.class);

    /** DIV for additional item info. */
    @UiField
    protected CmsSimplePanel m_additionalInfo;

    /** Panel to hold buttons.*/
    @UiField
    protected FlowPanel m_buttonPanel;

    /** The DIV showing the list icon. */
    @UiField
    protected SimplePanel m_iconPanel;

    /** Sub title label. */
    @UiField
    protected CmsLabel m_subTitle;

    /** Title label. */
    @UiField
    protected CmsLabel m_title;

    /** The title row, holding the title and the open-close button for the additional info. */
    @UiField
    protected FlowPanel m_titleRow;

    /** The open-close button for the additional info. */
    private CmsImageButton m_openClose;

    /** The root id. */
    private String m_rootId;

    /**
     * Constructor. Using a 'li'-tag as default root element.<p>
     * 
     * @param infoBean bean holding the item information
     */
    public CmsListItemWidget(CmsListInfoBean infoBean) {

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
     * Removes a widget from the button panel.<p>
     * 
     * @param w the widget to remove
     */
    public void removeButton(Widget w) {

        m_buttonPanel.remove(w);
    }

    /**
     * Sets the icon of this item.<p>
     * 
     * @param image the image to use as icon
     */
    public void setIcon(Image image) {

        m_iconPanel.setWidget(image);
    }

    /**
     * Constructor.<p>
     * 
     * @param infoBean bean holding the item information
     */
    protected void init(CmsListInfoBean infoBean) {

        HTMLPanel panel = new HTMLPanel(CmsDomUtil.Tag.div.name(), "");
        m_rootId = HTMLPanel.createUniqueId();
        panel.getElement().setId(m_rootId);
        panel.setStyleName(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().listItem());
        CmsHTMLHoverPanel itemContent = uiBinder.createAndBindUi(this);
        panel.add(itemContent, m_rootId);
        initWidget(panel);
        I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().ensureInjected();
        m_title.setText(infoBean.getTitle());
        m_subTitle.setText(infoBean.getSubTitle());
        if ((infoBean.getAdditionalInfo() != null) && (infoBean.getAdditionalInfo().size() > 0)) {
            m_openClose = new CmsImageButton(CmsImageButton.ICON.triangle_1_e, CmsImageButton.ICON.triangle_1_s, false);
            m_titleRow.insert(m_openClose, 0);
            m_openClose.addClickHandler(new OpenCloseHandler(this, m_openClose));
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
