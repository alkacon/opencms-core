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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.widgets;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;

/**
 * Describes errors that occur in the context of the OpenCms widgets.<p>
 *
 * @since 6.0.0
 */
public class CmsWidgetException extends CmsException {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -7003923645953106868L;

    /** The widget that caused the error. */
    private transient Object m_widget;

    /**
     * Creates a new localized Exception.<p>
     *
     * @param container the localized message container to use
     */
    public CmsWidgetException(CmsMessageContainer container) {

        this(container, null, null);
    }

    /**
     * Creates a new localized Exception.<p>
     *
     * @param container the localized message container to use
     * @param widget the widget that caused the error
     */
    public CmsWidgetException(CmsMessageContainer container, Object widget) {

        super(container);
        m_widget = widget;
    }

    /**
     * Creates a new localized Exception that also containes a root cause.<p>
     *
     * @param container the localized message container to use
     * @param cause the Exception root cause
     */
    public CmsWidgetException(CmsMessageContainer container, Throwable cause) {

        this(container, cause, null);
    }

    /**
     * Creates a new localized Exception that also containes a root cause.<p>
     *
     * @param container the localized message container to use
     * @param cause the Exception root cause
     * @param widget the widget that caused the error
     */
    public CmsWidgetException(CmsMessageContainer container, Throwable cause, Object widget) {

        super(container, cause);
        m_widget = widget;
    }

    /**
     * @see org.opencms.main.CmsException#createException(org.opencms.i18n.CmsMessageContainer, java.lang.Throwable)
     */
    @Override
    public CmsException createException(CmsMessageContainer container, Throwable cause) {

        if (cause instanceof CmsWidgetException) {
            return new CmsWidgetException(container, cause, ((CmsWidgetException)cause).getWidget());
        }
        return new CmsWidgetException(container, cause);
    }

    /**
     * Returns the widget that caused the error.<p>
     *
     * If the widget has not been set, this will return <code>null</code>.<p>
     *
     * @return the widget that caused the error
     */
    public Object getWidget() {

        return m_widget;
    }
}
