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

package org.opencms.gwt.client.ui.input.form;

import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.form.CmsFieldTooltip.Data;
import org.opencms.util.CmsStringUtil;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A row in a properties form.<p>
 *
 * This widget contains both a label and a panel into which an input widget for the form field can be placed.
 * These widgets are next to each other horizontally.
 *
 * @since 8.0.0
 */
public class CmsFormRow extends Composite {

    /** The ui binder interface for this widget. */
    protected interface I_CmsFormRowUiBinder extends UiBinder<Widget, CmsFormRow> {
        // uibinder
    }

    /** The width of the label. */
    public static final int LABEL_WIDTH = 160;

    /** The width of the opener. */
    public static final int OPENER_WIDTH = 16;

    /** The default widget container width. */
    public static final int WIDGET_CONTAINER_WIDTH = 370;

    /** The required right margin. */
    public static final int WIDGET_MARGIN_RIGHT = 15;

    /** The CSS bundle used for this widget. */
    protected static I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();

    /** The ui binder instance for this form row. */
    private static I_CmsFormRowUiBinder uiBinder = GWT.create(I_CmsFormRowUiBinder.class);

    /** List of style names for the help icon. */
    public static List<String> ICON_STYLES = Arrays.asList(
        I_CmsButton.ICON_FONT,
        I_CmsButton.ICON_CIRCLE_HELP,
        I_CmsLayoutBundle.INSTANCE.buttonCss().cmsFontIconButton(),
        I_CmsLayoutBundle.INSTANCE.buttonCss().hoverBlack(),
        I_CmsLayoutBundle.INSTANCE.buttonCss().helpIcon());

    /** The label used for displaying the information icon. */
    @UiField
    protected Panel m_icon;

    /** The label for the form row. */
    @UiField
    protected Label m_label;

    /** The widget container for the form row. */
    @UiField
    protected Panel m_widgetContainer;

    /**
     * The default constructor.
     */
    public CmsFormRow() {

        Widget main = uiBinder.createAndBindUi(this);
        initWidget(main);
        main.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().highTextBoxes());
    }

    /**
     * Returns the width of the label as a string.<p>
     *
     * @return the width of the label as a string
     */
    public static String getLabelWidth() {

        return LABEL_WIDTH + "px";
    }

    /**
     * Returns the width of the opener as a string.<p>
     *
     * @return the width of the opener as a string
     */
    public static String getOpenerWidth() {

        return OPENER_WIDTH + "px";
    }

    /**
     * Returns the left margin of the widget container as a string.<p>
     *
     * @return the left margin of the widget container as a string
     */
    public static String getWidgetContainerLeftMargin() {

        return OPENER_WIDTH + LABEL_WIDTH + "px";
    }

    /**
     * Returns the left margin of the widget container as a string.<p>
     *
     * @return the left margin of the widget container as a string
     */
    public static String getWidgetContainerWidth() {

        return WIDGET_CONTAINER_WIDTH + "px";
    }

    /**
     * Installs the DOM event handlers for displaying tooltips on a help icon.<p>
     *
     * The supplier passed in should not create a new tooltip data instance each time,
     * but cache the different possible tooltip data instances.
     *
     * @param icon the help icon
     * @param dataSupplier provides the tooltip data at the time the DOM events occur
     */
    public static void installTooltipEventHandlers(final Panel icon, final Supplier<Data> dataSupplier) {

        icon.addDomHandler(new MouseOverHandler() {

            public void onMouseOver(MouseOverEvent event) {

                CmsFieldTooltip.getHandler().buttonHover(dataSupplier.get());
            }
        }, MouseOverEvent.getType());

        icon.addDomHandler(new MouseOutHandler() {

            public void onMouseOut(MouseOutEvent event) {

                CmsFieldTooltip.getHandler().buttonOut(dataSupplier.get());
            }

        }, MouseOutEvent.getType());

        icon.addDomHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                CmsFieldTooltip.getHandler().buttonClick(dataSupplier.get());

            }
        }, ClickEvent.getType());
    }

    /**
     * Gets the icon.<p>
     *
     * @return the icon
     */
    public Panel getIcon() {

        return m_icon;
    }

    /**
     * Returns the label for the form row.<p>
     *
     * @return the label for the form row
     */
    public Label getLabel() {

        return m_label;
    }

    /**
     * Returns the widget container for the form row.<p>
     *
     * @return the widget container for the form row
     */
    public Panel getWidgetContainer() {

        return m_widgetContainer;
    }

    /**
     * Initializes the style for the info button.<p>
     */
    public void initInfoStyle() {

        m_icon.addStyleName(I_CmsButton.ICON_FONT);
        m_icon.addStyleName(I_CmsButton.ICON_CIRCLE_HELP);
        m_icon.addStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsFontIconButton());
        m_icon.addStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().hoverBlack());
        m_icon.addStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().helpIcon());
    }

    /**
     * Shows the info icon and sets the information text as its title.<p>
     *
     * @param info the info
     * @param isHtml true if info should be interpreted as HTML rather than plain text
     */
    public void setInfo(final String info, final boolean isHtml) {

        if (info != null) {
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(info)) {
                initInfoStyle();
                final Data tooltipData = new CmsFieldTooltip.Data(m_icon, info, isHtml);
                final Panel icon = m_icon;
                final Supplier<Data> dataSupplier = Suppliers.ofInstance(tooltipData);
                installTooltipEventHandlers(icon, dataSupplier);
            }
        }
    }

}
