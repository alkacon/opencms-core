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

import org.opencms.ade.galleries.client.preview.CmsCroppingParamBean;
import org.opencms.ade.galleries.client.preview.CmsFocalPointController;
import org.opencms.ade.galleries.client.preview.CmsImageFormatHandler;
import org.opencms.ade.galleries.client.preview.CmsImagePreviewHandler;
import org.opencms.ade.galleries.client.preview.I_CmsPreviewHandler;
import org.opencms.ade.galleries.shared.CmsPoint;
import org.opencms.ade.galleries.shared.CmsResourceInfoBean;
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

    /** The tab handler. */
    private I_CmsPreviewHandler<?> m_handler;

    /** The image info widget. */
    private CmsImageInfoDisplay m_imageInfoDisplay = new CmsImageInfoDisplay(this::removeCrop, this::removePoint);

    /** The resource info. */
    private CmsResourceInfoBean m_info;

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
        Widget additional = handler.getAdditionalWidgetForPropertyTab();
        if (handler instanceof CmsImagePreviewHandler) {
            m_main.add(m_imageInfoDisplay);
            ((CmsImagePreviewHandler)handler).addImagePointChangeHandler(this::updateImageInfo);
            ((CmsImagePreviewHandler)handler).addCroppingChangeHandler(this::updateImageInfo);
        }
        m_main.add(m_propertiesPanel);
        if (additional != null) {
            m_main.add(additional);
        }

    }

    /**
     * The generic function to display the resource properties.<p>
     *
     * @param info the information
     */
    public void fillContent(CmsResourceInfoBean info) {

        m_info = info;
        // width of a property form
        m_propertiesPanel.clear();
        Map<String, String> properties = info.getProperties();
        String noEditReason = info.getNoEditReason();
        if (properties != null) {
            Iterator<Entry<String, String>> it = properties.entrySet().iterator();
            while (it.hasNext()) {

                Entry<String, String> entry = it.next();
                CmsPropertyForm property = new CmsPropertyForm(
                    entry.getKey(),
                    entry.getValue(),
                    info.getPropertyLabel(entry.getKey()),
                    noEditReason);
                property.addValueChangeHandler(this);
                m_propertiesPanel.add(property);
            }
        }
        updateImageInfo();
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
     * @see org.opencms.ade.galleries.client.preview.ui.A_CmsPreviewDetailTab#getHandler()
     */
    @Override
    protected I_CmsPreviewHandler<?> getHandler() {

        return m_handler;
    }

    /**
     * Gets the current cropping as a string.<p>
     *
     * @return the string representing the currently selected cropping
     */
    private String getCrop() {

        CmsImagePreviewHandler handler = getImagePreviewHandler();
        if (handler != null) {
            CmsCroppingParamBean crop = handler.getCroppingParam();
            if ((crop != null) && crop.isCropped()) {
                return "" + crop.getCropWidth() + " x " + crop.getCropHeight();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Gets the image preview handler, or null if there is no image preview handler.<p>
     *
     * @return the image preview handler or null
     */
    private CmsImagePreviewHandler getImagePreviewHandler() {

        if (m_handler == null) {
            return null;
        }
        if (m_handler instanceof CmsImagePreviewHandler) {
            return (CmsImagePreviewHandler)m_handler;
        }
        return null;
    }

    /**
     * Gets the currently selected focal point as a string.<p>
     *
     * @return the focal point string
     */
    private String getPoint() {

        CmsImagePreviewHandler handler = getImagePreviewHandler();
        if (handler == null) {
            return null;
        }

        CmsPoint point = handler.getImageInfo().getFocalPoint();
        if (point == null) {
            return null;
        }
        int x = (int)point.getX();
        int y = (int)point.getY();
        return "" + x + " , " + y;
    }

    /**
     * Removes the cropping.
     */
    private void removeCrop() {

        CmsImageFormatHandler formatHandler = getImagePreviewHandler().getFormatHandler();
        formatHandler.onRemoveCropping();
        updateImageInfo();
    }

    /**
     * Removes the focal point.
     */
    private void removePoint() {

        CmsFocalPointController controller = getImagePreviewHandler().getFocalPointController();
        controller.reset();
    }

    /**
     * Updates the image information.
     */
    private void updateImageInfo() {

        String crop = getCrop();
        String point = getPoint();
        m_imageInfoDisplay.fillContent(m_info, crop, point);
    }
}