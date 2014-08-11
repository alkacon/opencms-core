/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.contenteditor.client;

import org.opencms.acacia.client.CmsWidgetService;
import org.opencms.acacia.client.widgets.CmsFileWidget;
import org.opencms.acacia.client.widgets.CmsGalleryWidget;
import org.opencms.acacia.client.widgets.CmsImageGalleryWidget;
import org.opencms.acacia.client.widgets.CmsTextareaWidget;
import org.opencms.acacia.client.widgets.CmsTextboxWidget;
import org.opencms.acacia.client.widgets.CmsFormWidgetWrapper;
import org.opencms.acacia.client.widgets.I_CmsEditWidget;
import org.opencms.acacia.client.widgets.CmsTinyMCEWidget;
import org.opencms.util.CmsStringUtil;

import java.util.Collection;
import java.util.Map;

/**
 * Default OpenCms widget service implementation.<p>
 */
public class CmsDefaultWidgetService extends CmsWidgetService {

    /** The paths to be skipped during locale synchronization. */
    private Collection<String> m_skipPaths;

    /** The locale synchronization values. */
    private Map<String, String> m_syncValues;

    /**
     * @see org.opencms.acacia.client.CmsWidgetService#addChangedOrderPath(java.lang.String)
     */
    @Override
    public void addChangedOrderPath(String attributePath) {

        m_skipPaths.add(attributePath);
    }

    /**
     * @see org.opencms.acacia.client.CmsWidgetService#getDefaultAttributeValue(java.lang.String, java.lang.String)
     */
    @Override
    public String getDefaultAttributeValue(String attributeName, String simpleValuePath) {

        if (!isSkipValue(simpleValuePath) && m_syncValues.containsKey(simpleValuePath)) {
            return m_syncValues.get(simpleValuePath);
        }
        return super.getDefaultAttributeValue(attributeName, simpleValuePath);
    }

    /**
     * Returns the paths to be skipped during locale synchronization.<p>
     *
     * @return the paths to be skipped during locale synchronization
     */
    public Collection<String> getSkipPaths() {

        return m_skipPaths;
    }

    /**
     * Returns the locale synchronization values.<p>
     *
     * @return the locale synchronization values
     */
    public Map<String, String> getSyncValues() {

        return m_syncValues;
    }

    /**
     * Sets the paths to be skipped during locale synchronization.<p>
     *
     * @param skipPaths the paths to be skipped during locale synchronization to set
     */
    public void setSkipPaths(Collection<String> skipPaths) {

        m_skipPaths = skipPaths;
    }

    /**
     * Sets the locale synchronization values.<p>
     *
     * @param syncValues the locale synchronization values to set
     */
    public void setSyncValues(Map<String, String> syncValues) {

        m_syncValues = syncValues;
    }

    /**
     * @see org.opencms.acacia.client.CmsWidgetService#shouldRemoveLastValueAfterUnfocus(org.opencms.acacia.client.widgets.I_CmsEditWidget)
     */
    @Override
    public boolean shouldRemoveLastValueAfterUnfocus(I_CmsEditWidget widget) {

        if (widget instanceof CmsFormWidgetWrapper) {
            widget = ((CmsFormWidgetWrapper)widget).getEditWidget();
        }
        if ((widget instanceof CmsTextareaWidget)
            || (widget instanceof CmsTextboxWidget)
            || (widget instanceof CmsTinyMCEWidget)) {
            String value = widget.getValue();
            if (" ".equals(value)) {
                return false;
            }
            return CmsStringUtil.isEmptyOrWhitespaceOnly(value);
        } else if ((widget instanceof CmsFileWidget)
            || (widget instanceof CmsImageGalleryWidget)
            || (widget instanceof CmsGalleryWidget)) {
            return CmsStringUtil.isEmptyOrWhitespaceOnly(widget.getValue());
        }
        return false;
    }

    /**
     * Returns if the given path should be skipped for locale synchronization.<p>
     * 
     * @param valuePath the value path
     * 
     * @return <code>true</code> if the given path should be skipped for locale synchronization
     */
    private boolean isSkipValue(String valuePath) {

        if (m_skipPaths != null) {
            for (String skipPath : m_skipPaths) {
                if (valuePath.startsWith(skipPath)) {
                    return true;
                }
            }
        }
        return false;
    }

}
