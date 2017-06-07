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

package org.opencms.gwt.client.ui.preferences;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.CmsTabbedPanel.CmsTabbedPanelStyle;
import org.opencms.gwt.client.ui.I_CmsTruncable;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel;
import org.opencms.gwt.client.ui.input.form.CmsFormRow;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsUserSettingsBean;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Form panel for editing user settings.
 **/
public class CmsUserSettingsFormFieldPanel extends A_CmsFormFieldPanel {

    /** The ui binder interface for this class. */
    public interface I_CmsUserSettingsFormFieldPanelUiBinder extends UiBinder<Widget, CmsUserSettingsFormFieldPanel> {
        //empty
    }

    /**
     * Style imported from the ui.xml file.
     */
    interface ExternalStyle extends CssResource {

        /**
         * CSS class accessor.
         *
         * @return the CSS class
         **/
        String titleColumn();

    }

    /** The ui binder instance for this class. */
    public I_CmsUserSettingsFormFieldPanelUiBinder uiBinder = GWT.create(I_CmsUserSettingsFormFieldPanelUiBinder.class);

    /** Form field container. */
    @UiField
    protected FlowPanel m_basicSettingsPanel;

    /** Tab. */
    @UiField
    protected CmsScrollPanel m_basicTab;

    /** Form field container. */
    @UiField
    protected FlowPanel m_extendedSettingsPanel;

    /** Tab. */
    @UiField
    protected CmsScrollPanel m_extendedTab;

    /**
     * The style from the ui.xml  file. (Note: the field needs to
     */
    @UiField
    protected ExternalStyle style;

    /** The tab panel. */
    private CmsTabbedPanel<CmsScrollPanel> m_tabPanel = new CmsTabbedPanel<CmsScrollPanel>(
        CmsTabbedPanelStyle.buttonTabs);

    /**
     * Creates a new instance.<p>
     *
     * @param userSettings the bean containing the current user settings
     */
    public CmsUserSettingsFormFieldPanel(CmsUserSettingsBean userSettings) {

        uiBinder.createAndBindUi(this); // don't use the return value, since we use the created widgets as tabs for the tab panel
        m_tabPanel.add(m_basicTab, Messages.get().key(Messages.GUI_USERSETTINGS_TAB_BASIC_0));
        m_tabPanel.add(m_extendedTab, Messages.get().key(Messages.GUI_USERSETTINGS_TAB_EXTENDED_0));

        final FlowPanel[] tabs = {m_basicSettingsPanel, m_extendedSettingsPanel};

        m_tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {

            public void onSelection(SelectionEvent<Integer> event) {

                final Widget constTarget = tabs[event.getSelectedItem().intValue()];
                Timer timer = new Timer() {

                    @Override
                    public void run() {

                        CmsDomUtil.resizeAncestor(constTarget);

                    }
                };
                timer.schedule(1);
            }
        });
        initWidget(m_tabPanel);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel#getDefaultGroup()
     */
    @Override
    public String getDefaultGroup() {

        return null;
    }

    /**
     * Gets the tab panel.<p>
     *
     * @return the tab panel
     */
    public CmsTabbedPanel<CmsScrollPanel> getTabPanel() {

        return m_tabPanel;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel#renderFields(java.util.Collection)
     */
    @Override
    public void renderFields(Collection<I_CmsFormField> fields) {

        for (Panel panel : new Panel[] {m_basicSettingsPanel, m_extendedSettingsPanel}) {
            panel.clear();
        }
        for (I_CmsFormField field : fields) {
            CmsFormRow row = createRow(field);
            getContainerForField(field).add(row);
        }

    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsKey, int clientWidth) {

        storeTruncation(textMetricsKey, clientWidth);
        for (Panel container : new Panel[] {m_basicSettingsPanel, m_extendedSettingsPanel}) {
            for (Widget widget : container) {
                if (widget instanceof I_CmsTruncable) {
                    ((I_CmsTruncable)widget).truncate(textMetricsKey + ".row", clientWidth - 20);
                }
            }
        }
    }

    /**
     * Gets the container in which the field should be placed.<p>
     *
     * @param field the form field
     * @return the intended parent widget for the field
     */
    private Panel getContainerForField(I_CmsFormField field) {

        if (field.getLayoutData().containsKey("basic")) {
            return m_basicSettingsPanel;
        } else {
            return m_extendedSettingsPanel;
        }
    }

}
