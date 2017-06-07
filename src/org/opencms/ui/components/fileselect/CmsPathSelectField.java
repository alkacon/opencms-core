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

package org.opencms.ui.components.fileselect;

import org.opencms.file.CmsResource;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.ui.A_CmsUI;

/**
 * File path select field. This field will also allow paths that are not pointing to any VFS resource.<p>
 */
public class CmsPathSelectField extends A_CmsFileSelectField<String> {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Flag indicating if only root paths are used. */
    private boolean m_useRootPaths;

    /** Flag to indicate whether we are currently setting the internal value. */
    private boolean m_settingInternalValue;

    /**
     * Creates a new instance.<p>
     */
    public CmsPathSelectField() {
        m_textField.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {

                String value = (String)(event.getProperty().getValue());
                if (!m_settingInternalValue) {
                    setInternalValue(value);
                    fireValueChange(false);
                }

            }

        });

    }

    /**
     * @see com.vaadin.ui.AbstractField#getType()
     */
    @Override
    public Class<? extends String> getType() {

        return String.class;
    }

    /**
     * Gets the value.<p>
     *
     * @return the value
     */
    @Override
    public String getValue() {

        return m_textField.getValue();
    }

    /**
     * Returns if only root paths are used.<p>
     *
     * @return <code>true</code> if only root paths are used
     */
    public boolean isUseRootPaths() {

        return m_useRootPaths;
    }

    /**
     * Sets if only root paths should be used.<p>
     *
     * @param useRootPaths <code>true</code> to use root paths only
     */
    public void setUseRootPaths(boolean useRootPaths) {

        m_useRootPaths = useRootPaths;
    }

    /**
     * Sets the value.<p>
     *
     * @param value the new value
     */
    @Override
    public void setValue(String value) {

        setValue(false, value);
    }

    /**
     * @see com.vaadin.ui.AbstractField#setInternalValue(java.lang.Object)
     */
    @Override
    protected void setInternalValue(String newValue) {

        m_settingInternalValue = true;
        try {
            super.setInternalValue(newValue);
            m_textField.setValue(newValue);
        } finally {
            m_settingInternalValue = false;
        }
    }

    /**
     * @see org.opencms.ui.components.fileselect.A_CmsFileSelectField#setResourceValue(org.opencms.file.CmsResource)
     */
    @Override
    protected void setResourceValue(CmsResource resource) {

        CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
        if (!m_useRootPaths
            && (site != null)
            && A_CmsUI.getCmsObject().getRequestContext().getSiteRoot().equals(site.getSiteRoot())) {
            setValue(true, A_CmsUI.getCmsObject().getSitePath(resource));
        } else {
            setValue(true, resource.getRootPath());
        }
    }

    /**
     * Sets the value.<p>
     *
     * @param fireChange <code>true</code> to fire the value change event
     * @param value the value to set
     */
    protected void setValue(boolean fireChange, String value) {

        m_textField.setValue(value);
        fireValueChange(false);
    }
}
