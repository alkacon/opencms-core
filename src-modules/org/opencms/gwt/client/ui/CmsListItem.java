/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsListItem.java,v $
 * Date   : $Date: 2010/03/16 13:20:28 $
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
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.Iterator;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a UI list item.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.0
 */
public class CmsListItem extends Composite {

    /** Tag names available. */
    public enum TagName {
        /** A 'div' tag. */
        DIV,

        /** A 'li' tag. */
        LI
    }

    /** Additional info item HTML. */
    protected static class AdditionalInfoItem extends HTML {

        /**
         * Constructor.<p>
         * 
         * @param title info title
         * @param value info value
         * @param valueStyle the style name for the info value, or null
         */
        AdditionalInfoItem(String title, String value, String valueStyle) {

            super(DOM.createDiv());
            Element titleSpan = DOM.createSpan();
            titleSpan.setInnerText(title + ":");
            titleSpan.addClassName(I_CmsLayoutBundle.INSTANCE.listItemCss().itemAdditionalTitle());
            Element valueSpan = DOM.createSpan();
            valueSpan.setInnerText(value);
            valueSpan.addClassName(I_CmsLayoutBundle.INSTANCE.listItemCss().itemAdditionalValue());
            if (valueStyle != null) {
                valueSpan.addClassName(valueStyle);
            }
            getElement().appendChild(titleSpan);
            getElement().appendChild(valueSpan);
        }
    }

    /**
     * Click-handler to open/close the additional info.<p>
     */
    protected class OpenCloseHandler implements ClickHandler {

        /** The button. */
        private CmsImageButton m_button;

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

            if (m_owner.getStyleName().contains(CmsListItem.OPENCLASS)) {
                m_owner.removeStyleName(CmsListItem.OPENCLASS);
                m_button.setDown(false);
            } else {
                m_owner.addStyleName(CmsListItem.OPENCLASS);
                m_button.setDown(true);
            }

        }

    }

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    interface I_CmsListItemUiBinder extends UiBinder<CmsHTMLHoverPanel, CmsListItem> {
        // GWT interface, nothing to do here
    }

    /** The CSS class to set the additional info open. */
    static final String OPENCLASS = I_CmsLayoutBundle.INSTANCE.listItemCss().open();

    /** The ui-binder instance for this class. */
    private static I_CmsListItemUiBinder uiBinder = GWT.create(I_CmsListItemUiBinder.class);

    /** DIV for additional item info. */
    @UiField
    DivElement m_additionalDiv;

    /** Panel to hold buttons.*/
    @UiField
    FlowPanel m_buttonPanel;

    /** The DIV showing the list icon. */
    @UiField
    SimplePanel m_iconPanel;

    /** Sub title label. */
    @UiField
    Label m_subTitleDiv;

    /** Title label. */
    @UiField
    Label m_titleDiv;

    /** The title row, holding the title and the open-close button for the additional info. */
    @UiField
    FlowPanel m_titleRow;

    /** The open-close button for the additional info. */
    private CmsImageButton m_openClose;

    private String m_rootId;

    /**
     * Constructor. Using a 'li'-tag as default root element.<p>
     * 
     * @param infoBean bean holding the item information
     */
    public CmsListItem(CmsListInfoBean infoBean) {

        this(infoBean, TagName.LI);
    }

    /**
     * Constructor.<p>
     * 
     * @param infoBean bean holding the item information
     * @param tagName the tag name to use for the root element
     */
    public CmsListItem(CmsListInfoBean infoBean, TagName tagName) {

        HTMLPanel panel = new HTMLPanel(tagName.name().toLowerCase(), "");
        m_rootId = HTMLPanel.createUniqueId();
        panel.getElement().setId(m_rootId);
        panel.setStyleName(I_CmsLayoutBundle.INSTANCE.listItemCss().listItem());
        CmsHTMLHoverPanel itemContent = uiBinder.createAndBindUi(this);
        panel.add(itemContent, m_rootId);
        initWidget(panel);
        I_CmsLayoutBundle.INSTANCE.listItemCss().ensureInjected();
        m_titleDiv.setText(infoBean.getTitle());
        m_subTitleDiv.setText(infoBean.getSubTitle());
        if ((infoBean.getAdditionalInfo() != null) && (infoBean.getAdditionalInfo().size() > 0)) {
            m_openClose = new CmsImageButton(CmsImageButton.ICON.triangle_1_e, CmsImageButton.ICON.triangle_1_s, false);
            m_titleRow.insert(m_openClose, 0);
            m_openClose.addClickHandler(new OpenCloseHandler(this, m_openClose));
            Iterator<Entry<String, String>> it = infoBean.getAdditionalInfo().entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, String> entry = it.next();
                AdditionalInfoItem info = new AdditionalInfoItem(
                    entry.getKey(),
                    entry.getValue(),
                    infoBean.getValueStyle(entry.getKey()));
                m_additionalDiv.appendChild(info.getElement());
            }
        }

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

}
