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

package org.opencms.workplace.list;

import org.opencms.i18n.CmsMessageContainer;

import java.util.Date;
import java.util.Locale;

/**
 * Formatter for dates.<p>
 *
 * The 'never' message will be displayed if the date is null or <code>{@link Date#getTime()}==0</code>.<p>
 *
 * @since 6.0.0
 */
public class CmsListDateMacroFormatter extends CmsListMacroFormatter {

    /** Constant for never message. */
    private final CmsMessageContainer m_never;

    /** Constant for never time. */
    private final long m_neverTime;

    /**
     * Default constructor that sets the mask to use.<p>
     *
     * @param mask pattern for <code>{@link java.text.MessageFormat}</code>
     * @param never message (without args) for the 'never' message
     */
    public CmsListDateMacroFormatter(CmsMessageContainer mask, CmsMessageContainer never) {

        this(mask, never, 0);
    }

    /**
     * Default constructor that sets the mask to use.<p>
     *
     * @param mask pattern for <code>{@link java.text.MessageFormat}</code>
     * @param never message (without args) for the 'never' message
     * @param neverTime the time considered as 'never', default is <code>0</code>
     */
    public CmsListDateMacroFormatter(CmsMessageContainer mask, CmsMessageContainer never, long neverTime) {

        super(mask);
        m_never = never;
        m_neverTime = neverTime;
    }

    /**
     * Returns a default date formatter object.<p>
     *
     * @return a default date formatter object
     */
    public static I_CmsListFormatter getDefaultDateFormatter() {

        return new CmsListDateMacroFormatter(
            Messages.get().container(Messages.GUI_LIST_DATE_FORMAT_1),
            Messages.get().container(Messages.GUI_LIST_DATE_FORMAT_NEVER_0));
    }

    /**
     * Returns a default date formatter object.<p>
     *
     * @param never time considered as never
     *
     * @return a default date formatter object
     */
    public static I_CmsListFormatter getDefaultDateFormatter(long never) {

        return new CmsListDateMacroFormatter(
            Messages.get().container(Messages.GUI_LIST_DATE_FORMAT_1),
            Messages.get().container(Messages.GUI_LIST_DATE_FORMAT_NEVER_0),
            never);
    }

    /**
     * @see org.opencms.workplace.list.CmsListMacroFormatter#format(java.lang.Object, java.util.Locale)
     */
    @Override
    public String format(Object data, Locale locale) {

        if (data == null) {
            return m_never.key(locale);
        }
        if (data instanceof Date) {
            if (((Date)data).getTime() == m_neverTime) {
                return m_never.key(locale);
            }
        }
        return super.format(data, locale);
    }
}
