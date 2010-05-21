/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/ui/Attic/CmsPropertiesTab.java,v $
 * Date   : $Date: 2010/05/21 14:27:39 $
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

package org.opencms.ade.galleries.client.preview.ui;

import org.opencms.ade.galleries.client.ui.Messages;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.CmsPushButton;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The widget to display the properties of the selected resource.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.
 */
public class CmsPropertiesTab extends Composite {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    /* default */interface I_CmsPropertiesTabUiBinder extends UiBinder<Widget, CmsPropertiesTab> {
        // GWT interface, nothing to do here
    }

    /** Text metrics key. */
    private static final String TM_PREVIEW_TAB_PROPERTIES = "PropertiesTab";

    /** The ui-binder instance for this class. */
    private static I_CmsPropertiesTabUiBinder uiBinder = GWT.create(I_CmsPropertiesTabUiBinder.class);

    /** The properties panel. */
    @UiField
    FlowPanel m_properties;

    /** The save button. */
    @UiField
    CmsPushButton m_saveButton;

    /** The select button. */
    @UiField
    CmsPushButton m_selectButton;

    /**
     * The constructor.<p>
     * 
     * @param height the properties tab height
     * @param width the properties tab width
     * @param properties the properties to display
     */
    public CmsPropertiesTab(int height, int width, Map<String, String> properties) {

        initWidget(uiBinder.createAndBindUi(this));

        fillProperties(height, width, properties);

        // buttons        
        m_selectButton.setText(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_SELECT_0));
        m_saveButton.setText(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_SAVE_0));

    }

    /**
     * Updates the size of the dialog after the window was resized.<p>
     * 
     * @param width the new width
     * @param height the new height
     */
    public void updateHeight(int width, int height) {

        // TODO: implement
    }

    /**
     * Will be triggered, when the save button is clicked.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_saveButton")
    void onSaveClick(ClickEvent event) {

        //TODO: implement
    }

    /**
     * Will be triggered, when the select button is clicked.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_selectButton")
    void onSelectClick(ClickEvent event) {

        //TODO: implement
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

    /**
     * The generic function to display the resource properties.<p>
     * 
     * @param height the height of the properties panel
     * @param width the width of the properties panel
     * @param properties the properties values
     */
    private void fillProperties(int height, int width, Map<String, String> properties) {

        // width of a property form
        int pannelWidth = calculateWidth(width);
        Iterator<Entry<String, String>> it = properties.entrySet().iterator();
        boolean left = true;
        while (it.hasNext()) {

            Entry<String, String> entry = it.next();
            CmsPropertyForm property = new CmsPropertyForm(
                "id",
                pannelWidth,
                entry.getKey(),
                entry.getValue(),
                TM_PREVIEW_TAB_PROPERTIES);
            if (left) {
                property.setFormStyle(I_CmsLayoutBundle.INSTANCE.previewDialogCss().propertyLeft());
                left = false;
            } else {
                property.setFormStyle(I_CmsLayoutBundle.INSTANCE.previewDialogCss().propertyRight());
                left = true;
            }
            m_properties.add(property);

            // TODO: set the calculated height
        }
    }
}