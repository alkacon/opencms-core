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

package org.opencms.ui.components;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Class for the info button used in toolbar.<p>
 */
public class CmsInfoButton extends Button {

    /**
     * Bean holding further Vaadin elements and theire position.<p>
     */
    class InfoElementBean {

        /**Position in layout. */
        private int m_pos;

        /**Component to add.*/
        private Component m_component;

        /**
         * public constructor.<p>
         *
         * @param pos position of element
         * @param comp vaadin component
         */
        InfoElementBean(int pos, Component comp) {

            m_pos = pos;
            m_component = comp;
        }

        /**
         * Gets component.
         *
         * @return Vaadin component
         */
        protected Component getComponent() {

            return m_component;
        }

        /**
         * Gets position of component.
         *
         * @return int position
         */
        protected int getPos() {

            return m_pos;
        }

    }

    /**vaadin serial id.*/
    private static final long serialVersionUID = 5718515094289838271L;

    /**Icon of button.*/
    private static final FontOpenCms ICON = FontOpenCms.INFO;

    /**Caption for information window.*/
    protected String m_windowCaption;

    Button m_addButton;

    /**Html lines to be shown in label.*/
    private List<String> m_htmlLines;

    /**List with additional vaadin elements to display and their position in VerticalLayout.*/
    private List<InfoElementBean> m_additionalElements = new ArrayList<InfoElementBean>();

    /**Clicklistener for the button. */
    private ClickListener m_clickListener;

    /**
     * public constructor.<p>
     */
    public CmsInfoButton() {

        super(ICON);
        ini(new ArrayList<String>());
    }

    /**
     * public constructor.<p>
     *
     * @param htmlLines lines to show
     */
    public CmsInfoButton(final List<String> htmlLines) {

        super(ICON);
        m_htmlLines = htmlLines;
        ini(htmlLines);
    }

    /**
     * public constructor.<p>
     *
     * @param infos map with information to display
     */
    public CmsInfoButton(Map<String, String> infos) {

        super(ICON);

        ini(getHtmlLines(infos));
    }

    /**
     * Adds a vaadin element to window at last position.<p>
     *
     * @param component to be added
     */
    public void addAdditionalElement(Component component) {

        m_additionalElements.add(new InfoElementBean(m_additionalElements.size() + 1, component));
        removeClickListener(m_clickListener);
        m_clickListener = getClickListener(m_htmlLines, m_additionalElements);
        addClickListener(m_clickListener);
    }

    /**
     * Adds a vaadin element to window.<p>
     *
     * @param component to be added
     * @param pos position in vertical layout
     */
    public void addAdditionalElement(Component component, int pos) {

        m_additionalElements.add(new InfoElementBean(pos, component));
        removeClickListener(m_clickListener);
        m_clickListener = getClickListener(m_htmlLines, m_additionalElements);
        addClickListener(m_clickListener);
    }

    /**
     * Get the info layout.<p>
     *
     * @return VerticalLayout
     */
    public VerticalLayout getInfoLayout() {

        return getLayout(m_htmlLines, m_additionalElements);
    }

    /**
     * Replaces current Map with new map.<p>
     *
     * @param data to replace the old map
     */
    public void replaceData(Map<String, String> data) {

        removeClickListener(m_clickListener);
        m_clickListener = getClickListener(getHtmlLines(data), m_additionalElements);
        addClickListener(m_clickListener);

    }

    public void setAdditionalButton(Button button) {

        m_addButton = button;
    }

    /**
     * Sets the caption of the information window.<p>
     *
     * @param caption to be set
     */
    public void setWindowCaption(String caption) {

        m_windowCaption = caption;
    }

    /**
     * The layout which is shown in window by triggering onclick event of button.<p>
     *
     * @param htmlLines to be shown
     * @param additionalElements further vaadin elements
     * @return vertical layout
     */
    protected VerticalLayout getLayout(final List<String> htmlLines, final List<InfoElementBean> additionalElements) {

        VerticalLayout layout = new VerticalLayout();
        Label label = new Label();
        label.setWidthUndefined();
        layout.setMargin(true);
        label.setContentMode(ContentMode.HTML);
        layout.addStyleName(OpenCmsTheme.INFO);
        String htmlContent = "";
        for (String line : htmlLines) {
            htmlContent += line;
        }
        label.setValue(htmlContent);

        layout.addComponent(label);
        for (InfoElementBean infoElement : additionalElements) {
            layout.addComponent(infoElement.getComponent(), infoElement.getPos());
        }
        layout.setWidthUndefined();
        return layout;
    }

    /**
     * Clicklistener for the button.<p>
     *
     * @param htmlLines to be shown in Label
     * @param additionalElements to be placed in the verticalllayout which holds the label
     * @return ClickListener
     */
    private ClickListener getClickListener(
        final List<String> htmlLines,
        final List<InfoElementBean> additionalElements) {

        return new Button.ClickListener() {

            private static final long serialVersionUID = -553128629431329217L;

            public void buttonClick(ClickEvent event) {

                final Window window = CmsBasicDialog.prepareWindow(CmsBasicDialog.DialogWidth.content);
                window.setCaption(
                    m_windowCaption == null
                    ? CmsVaadinUtils.getMessageText(Messages.GUI_INFO_BUTTON_CAPTION_0)
                    : m_windowCaption);
                window.setResizable(false);
                CmsBasicDialog dialog = new CmsBasicDialog();
                if (m_addButton != null) {
                    dialog.addButton(m_addButton, false);
                }
                VerticalLayout layout = getLayout(htmlLines, additionalElements);
                dialog.setContent(layout);

                Button button = new Button(CmsVaadinUtils.messageClose());
                button.addClickListener(new Button.ClickListener() {

                    private static final long serialVersionUID = 5789436407764072884L;

                    public void buttonClick(ClickEvent event1) {

                        window.close();

                    }
                });
                dialog.addButton(button);

                window.setContent(dialog);

                UI.getCurrent().addWindow(window);
            }
        };

    }

    /**
     * Creates html code from given map.<p>
     *
     * @param infos to be displayed
     * @return List of html lines
     */
    private List<String> getHtmlLines(Map<String, String> infos) {

        List<String> htmlLines = new ArrayList<String>();

        for (String key : infos.keySet()) {
            htmlLines.add(
                "<div style=\"display:flex;align-items:flex-end;\"><div class=\""
                    + OpenCmsTheme.INFO_ELEMENT_NAME
                    + "\">"
                    + key
                    + " :</div><div style=\"width:140px;\" class=\""
                    + OpenCmsTheme.INFO_ELEMENT_VALUE
                    + "\">"
                    + infos.get(key)
                    + "</div></div>");
        }
        m_htmlLines = htmlLines;
        return htmlLines;
    }

    /**
     * initializes the button.<p>
     *
     * @param htmlLines to show
     */
    private void ini(final List<String> htmlLines) {

        addStyleName(ValoTheme.BUTTON_BORDERLESS);
        addStyleName(OpenCmsTheme.TOOLBAR_BUTTON);

        m_clickListener = getClickListener(htmlLines, m_additionalElements);
        addClickListener(m_clickListener);
    }
}
