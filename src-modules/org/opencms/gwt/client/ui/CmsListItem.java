/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsListItem.java,v $
 * Date   : $Date: 2010/03/04 15:17:19 $
 * Version: $Revision: 1.1 $
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a UI list item.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsListItem extends Composite {

    /** The ui-binder instance for this class. */
    private static I_CmsListItemUiBinder uiBinder = GWT.create(I_CmsListItemUiBinder.class);

    /** The CSS class to set the additional info open. */
    static final String OPENCLASS = I_CmsLayoutBundle.INSTANCE.listItemCss().open();

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    interface I_CmsListItemUiBinder extends UiBinder<HTMLPanel, CmsListItem> {
        // GWT interface, nothing to do here
    }

    /** The DIV showing the list icon. */
    @UiField
    DivElement m_iconDiv;

    /** Title label. */
    @UiField
    Label m_titleDiv;

    /** Sub title label. */
    @UiField
    Label m_subTitleDiv;

    /** DIV for additional item info. */
    @UiField
    DivElement m_additionalDiv;

    /** The title row, holding the title and the open-close button for the additional info. */
    @UiField
    FlowPanel m_titleRow;

    /** The open-close button for the additional info. */
    private CmsImageButton m_openClose;

    /** Additional info item HTML. */
    protected static class AdditionalInfoItem extends HTML {

        /**
         * Constructor.<p>
         * 
         * @param title info title
         * @param value info value
         */
        AdditionalInfoItem(String title, String value) {

            super(DOM.createDiv());
            Element titleSpan = DOM.createSpan();
            titleSpan.setInnerText(title);
            Element valueSpan = DOM.createSpan();
            valueSpan.setInnerText(value);
            getElement().appendChild(titleSpan);
            getElement().appendChild(valueSpan);
        }
    }

    /**
     * Click-handler to open/close the additional info.<p>
     */
    protected class OpenCloseHandler implements ClickHandler {

        /** The owner widget. */
        private Widget m_owner;

        /** The button. */
        private CmsImageButton m_button;

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
     * Constructor.<p>
     * 
     * @param infoBean bean holding the item information
     */
    public CmsListItem(CmsListInfoBean infoBean) {

        initWidget(uiBinder.createAndBindUi(this));
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
                AdditionalInfoItem info = new AdditionalInfoItem(entry.getKey(), entry.getValue());
                m_additionalDiv.appendChild(info.getElement());
            }
        }

    }

}
