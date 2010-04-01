/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsListItemWidget.java,v $
 * Date   : $Date: 2010/04/01 09:26:31 $
 * Version: $Revision: 1.5 $
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
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsStringUtil;
import org.opencms.gwt.client.util.CmsTextMetrics;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.Iterator;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a UI list item.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.0
 */
public class CmsListItemWidget extends Composite {

    /** Additional info item HTML. */
    protected static class AdditionalInfoItem extends HTML {

        /**
         * Constructor.<p>
         * 
         * @param title info title
         * @param value info value
         * @param additionalStyle an additional class name
         */
        AdditionalInfoItem(String title, String value, String additionalStyle) {

            super(DOM.createDiv());
            Element titleElem = DOM.createDiv();
            titleElem.setInnerText(title + ":");
            titleElem.addClassName(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().itemAdditionalTitle());
            Element valueElem = DOM.createDiv();
            valueElem.setInnerText(value);
            valueElem.addClassName(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().itemAdditionalValue());
            if (additionalStyle != null) {
                valueElem.addClassName(additionalStyle);
            }
            getElement().appendChild(titleElem);
            getElement().appendChild(valueElem);
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
                    /* defer until children have been (hopefully) layouted. */
                    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {

                        /**
                         * @see com.google.gwt.user.client.Command#execute()
                         */
                        public void execute() {

                            NodeList<Node> childNodes = m_additionalDiv.getChildNodes();
                            for (int i = 0; i < childNodes.getLength(); i++) {
                                Node addInfo = childNodes.getItem(i);
                                Element element = addInfo.getChild(1).<Element> cast();
                                fixElement(element);
                            }
                        }
                    });
                    m_init = true;
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

    /** Debug log displayed within the client window. */
    private static CmsDebugLog m_debug;

    /**
     * Returns the debug log.<p>
     * 
     * @return the debug log
     */
    public static CmsDebugLog getDebug() {

        if (m_debug == null) {
            m_debug = new CmsDebugLog();
            RootPanel.get().add(m_debug);
        }
        return m_debug;
    }

    /**
     * Truncates long text and sets the original text to the title attribute.<p> 
     * 
     * @param element the element to fix
     */
    protected void fixElement(Element element) {

        // measure the actual text width
        CmsTextMetrics tm = CmsTextMetrics.get();
        tm.bind(element);
        String text = element.getInnerText();
        int textWidth = tm.getWidth(text);
        tm.release();

        // the current element width
        int elementWidth = CmsDomUtil.getCurrentStyleInt(element, CmsDomUtil.Style.width);

        getDebug().printLine("fixElement: ");
        getDebug().printLine("text: " + text);
        getDebug().printLine("elemWidth: " + elementWidth);
        getDebug().printLine("textWidth: " + textWidth);

        if (elementWidth == 0) {
            // HACK: clientWidth seems to be from time to time zero :(
            // see #onLoad
            elementWidth = textWidth;
        }
        if (elementWidth < textWidth) {
            // if the text does not have enough space, fix it
            int maxChars = (int)((float)elementWidth / (float)textWidth * text.length());
            if (maxChars < 1) {
                maxChars = 1;
            }
            String newText = text.substring(0, maxChars - 1);
            if (text.startsWith("/")) {
                // file name?
                newText = CmsStringUtil.formatResourceName(text, maxChars);
            } else if (maxChars > 2) {
                // enough space for ellipsis?
                newText += "&hellip;";
            }
            if (newText.isEmpty()) {
                // if empty, it will break the layout
                newText = "&nbsp;";
            }
            // use html instead of text because of the entities
            element.setInnerHTML(newText);
            // add tooltip with the original text
            element.setAttribute("title", text);
        }
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
        m_titleDiv.setText(infoBean.getTitle());
        m_subTitleDiv.setText(infoBean.getSubTitle());
        if ((infoBean.getAdditionalInfo() != null) && (infoBean.getAdditionalInfo().size() > 0)) {
            m_openClose = new CmsImageButton(CmsImageButton.ICON.triangle_1_e, CmsImageButton.ICON.triangle_1_s, false);
            m_titleRow.insert(m_openClose, 0);
            m_openClose.addClickHandler(new OpenCloseHandler(this, m_openClose));
            Iterator<Entry<String, String>> it = infoBean.getAdditionalInfo().entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, String> entry = it.next();
                String valueStyle = infoBean.getValueStyle(entry.getKey());
                AdditionalInfoItem info = new AdditionalInfoItem(entry.getKey(), entry.getValue(), valueStyle);
                m_additionalDiv.appendChild(info.getElement());
            }
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    protected void onLoad() {

        super.onLoad();

        /* defer until children have been (hopefully) layouted. */
        Timer t = new Timer() {

            @Override
            public void run() {

                fixElement(m_titleDiv.getElement());
                fixElement(m_subTitleDiv.getElement());
            }
        };
        // HACK: clientWidth seems to be from time to time zero :(
        // specially if waiting less than 300ms, see #fixElement
        t.schedule(300);
    }
}
