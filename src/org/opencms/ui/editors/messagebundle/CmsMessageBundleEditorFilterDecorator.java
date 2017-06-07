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

package org.opencms.ui.editors.messagebundle;

import org.opencms.i18n.CmsMessageException;
import org.opencms.main.CmsLog;

import java.util.Locale;

import org.apache.commons.logging.Log;

import org.tepi.filtertable.FilterDecorator;
import org.tepi.filtertable.numberfilter.NumberFilterPopupConfig;

import com.vaadin.server.Resource;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.UI;

/** Adjust the style of the filter bar of the table. */
class CmsMessageBundleEditorFilterDecorator implements FilterDecorator {

    /** Serialization id. - {@link java.io.Serializable} is a super interface of {@link FilterDecorator}. */
    private static final long serialVersionUID = -7166112054012313157L;
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMessageBundleEditorFilterDecorator.class);

    /**
     * @see org.tepi.filtertable.FilterDecorator#getAllItemsVisibleString()
     */
    public String getAllItemsVisibleString() {

        try {
            return Messages.get().getBundle(UI.getCurrent().getLocale()).getString(Messages.GUI_FILTER_SHOW_ALL);
        } catch (CmsMessageException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * @see org.tepi.filtertable.FilterDecorator#getBooleanFilterDisplayName(java.lang.Object, boolean)
     */
    public String getBooleanFilterDisplayName(Object propertyId, boolean value) {

        return null;
    }

    /**
     * @see org.tepi.filtertable.FilterDecorator#getBooleanFilterIcon(java.lang.Object, boolean)
     */
    public Resource getBooleanFilterIcon(Object propertyId, boolean value) {

        return null;
    }

    /**
     * @see org.tepi.filtertable.FilterDecorator#getClearCaption()
     */
    public String getClearCaption() {

        return null;
    }

    /**
     * @see org.tepi.filtertable.FilterDecorator#getDateFieldResolution(java.lang.Object)
     */
    public Resolution getDateFieldResolution(Object propertyId) {

        return null;
    }

    /**
     * @see org.tepi.filtertable.FilterDecorator#getDateFormatPattern(java.lang.Object)
     */
    public String getDateFormatPattern(Object propertyId) {

        return null;
    }

    /**
     * @see org.tepi.filtertable.FilterDecorator#getEnumFilterDisplayName(java.lang.Object, java.lang.Object)
     */
    public String getEnumFilterDisplayName(Object propertyId, Object value) {

        return null;
    }

    /**
     * @see org.tepi.filtertable.FilterDecorator#getEnumFilterIcon(java.lang.Object, java.lang.Object)
     */
    public Resource getEnumFilterIcon(Object propertyId, Object value) {

        return null;
    }

    /**
     * @see org.tepi.filtertable.FilterDecorator#getFromCaption()
     */
    public String getFromCaption() {

        return null;
    }

    /**
     * @see org.tepi.filtertable.FilterDecorator#getLocale()
     */
    public Locale getLocale() {

        return null;
    }

    /**
     * @see org.tepi.filtertable.FilterDecorator#getNumberFilterPopupConfig()
     */
    public NumberFilterPopupConfig getNumberFilterPopupConfig() {

        return null;
    }

    /**
     * @see org.tepi.filtertable.FilterDecorator#getSetCaption()
     */
    public String getSetCaption() {

        return null;
    }

    /**
     * @see org.tepi.filtertable.FilterDecorator#getTextChangeTimeout(java.lang.Object)
     */
    public int getTextChangeTimeout(Object propertyId) {

        return 500;
    }

    /**
     * @see org.tepi.filtertable.FilterDecorator#getToCaption()
     */
    public String getToCaption() {

        return null;
    }

    /**
     * @see org.tepi.filtertable.FilterDecorator#isTextFilterImmediate(java.lang.Object)
     */
    public boolean isTextFilterImmediate(Object propertyId) {

        return true;
    }

    /**
     * @see org.tepi.filtertable.FilterDecorator#usePopupForNumericProperty(java.lang.Object)
     */
    public boolean usePopupForNumericProperty(Object propertyId) {

        return false;
    }

}
