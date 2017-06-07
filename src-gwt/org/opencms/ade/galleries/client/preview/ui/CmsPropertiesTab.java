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

package org.opencms.ade.galleries.client.preview.ui;

import org.opencms.ade.galleries.client.preview.I_CmsPreviewHandler;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The widget to display the properties of the selected resource.<p>
 *
 * @since 8.0.
 */
public class CmsPropertiesTab extends A_CmsPreviewDetailTab implements ValueChangeHandler<String> {

    /** Text metrics key. */
    private static final String TM_PREVIEW_TAB_PROPERTIES = "PropertiesTab";

    /** The tab handler. */
    private I_CmsPreviewHandler<?> m_handler;

    /** The panel for the properties. */
    private FlowPanel m_propertiesPanel;

    /**
     * The constructor.<p>
     *
     * @param dialogMode the dialog mode
     * @param height the properties tab height
     * @param width the properties tab width
     * @param handler the tab handler to set
     */
    public CmsPropertiesTab(GalleryMode dialogMode, int height, int width, I_CmsPreviewHandler<?> handler) {

        super(dialogMode, height, width);
        m_handler = handler;
        m_propertiesPanel = new FlowPanel();
        m_propertiesPanel.addStyleName(
            org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle.INSTANCE.previewDialogCss().propertiesList());
        m_propertiesPanel.addStyleName(
            org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle.INSTANCE.previewDialogCss().clearFix());
        m_main.insert(m_propertiesPanel, 0);
    }

    /**
     * The generic function to display the resource properties.<p>
     *
     * @param properties the properties values
     * @param noEditReason the reason why the properties are not editable
     */
    public void fillProperties(Map<String, String> properties, String noEditReason) {

        // width of a property form
        int pannelWidth = calculateWidth(m_tabWidth);
        m_propertiesPanel.clear();
        if (properties != null) {
            Iterator<Entry<String, String>> it = properties.entrySet().iterator();
            boolean isLeft = true;
            while (it.hasNext()) {

                Entry<String, String> entry = it.next();
                CmsPropertyForm property = new CmsPropertyForm(
                    entry.getKey(),
                    pannelWidth,
                    entry.getValue(),
                    noEditReason,
                    TM_PREVIEW_TAB_PROPERTIES);
                if (isLeft) {
                    property.setFormStyle(I_CmsLayoutBundle.INSTANCE.previewDialogCss().propertyLeft());
                    isLeft = false;
                } else {
                    property.setFormStyle(I_CmsLayoutBundle.INSTANCE.previewDialogCss().propertyRight());
                    isLeft = true;
                }
                property.addValueChangeHandler(this);
                m_propertiesPanel.add(property);

                // TODO: set the calculated height of the scrolled panel with properties
            }
        }
        setChanged(false);
    }

    /**
     * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
     */
    public void onValueChange(ValueChangeEvent<String> event) {

        setChanged(true);
    }

    /**
     * Will be triggered, when the save button is clicked.<p>
     *
     * @param afterSaveCommand the command to execute after the properties have been saved
     */
    public void saveProperties(Command afterSaveCommand) {

        Map<String, String> properties = new HashMap<String, String>();
        for (Widget property : m_propertiesPanel) {
            CmsPropertyForm form = ((CmsPropertyForm)property);
            if (form.isChanged()) {
                properties.put(form.getId(), form.getValue());
            }
        }
        m_handler.saveProperties(properties, afterSaveCommand);
    }

    /**
     * Updates the size of the dialog after the window was resized.<p>
     *
     * @param width the new width
     * @param height the new height
     */
    public void updateSize(int width, int height) {

        m_tabHeight = height;
        m_tabWidth = width;
        // TODO: implement
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.ui.A_CmsPreviewDetailTab#getHandler()
     */
    @Override
    protected I_CmsPreviewHandler<?> getHandler() {

        return m_handler;
    }

    /**
     * Calculates the width of the properties panel without border, margin and padding.<p>
     *
     * '- 13px:'  2px - border, 2px - outer margin, 2px - inner margin, 2px border, 5px padding
     * '/ 2': two colums
     * '-18': some offset (The input field needs more place because of the border)
     *
     * @param width the width of the preview dialog containing all decorations
     * @return the width of the properties panel
     */
    private int calculateWidth(int width) {

        return ((width - 13) / 2) - 18;
    }
}