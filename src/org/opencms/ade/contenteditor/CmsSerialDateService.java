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

package org.opencms.ade.contenteditor;

import org.opencms.acacia.shared.CmsSerialDateUtil;
import org.opencms.acacia.shared.I_CmsSerialDateValue;
import org.opencms.acacia.shared.rpc.I_CmsSerialDateService;
import org.opencms.gwt.CmsGwtService;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsPair;
import org.opencms.widgets.serialdate.CmsSerialDateBeanFactory;
import org.opencms.widgets.serialdate.CmsSerialDateValue;
import org.opencms.widgets.serialdate.I_CmsSerialDateBean;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.SortedSet;

/** Implementation of the serial date RPC service. */
public class CmsSerialDateService extends CmsGwtService implements I_CmsSerialDateService {

    /** Serialization id. */
    private static final long serialVersionUID = -5078405766510438917L;

    /** Date formatter for status messages. */
    private DateFormat m_dateFormat;

    /**
     * @see org.opencms.acacia.shared.rpc.I_CmsSerialDateService#getDates(java.lang.String)
     */
    public Collection<CmsPair<Date, Boolean>> getDates(String config) {

        I_CmsSerialDateBean bean = CmsSerialDateBeanFactory.createSerialDateBean(config);
        if (null != bean) {
            Collection<Date> dates = bean.getDates();
            Collection<Date> exceptions = bean.getExceptions();
            Collection<CmsPair<Date, Boolean>> result = new ArrayList<>(dates.size() + exceptions.size());
            for (Date d : dates) {
                result.add(new CmsPair<Date, Boolean>(d, Boolean.TRUE));
            }
            for (Date d : exceptions) {
                result.add(new CmsPair<Date, Boolean>(d, Boolean.FALSE));
            }
            return result;
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * @see org.opencms.acacia.shared.rpc.I_CmsSerialDateService#getStatus(java.lang.String)
     */
    public CmsPair<Boolean, String> getStatus(String config) {

        I_CmsSerialDateValue value = new CmsSerialDateValue(config);
        Locale l = OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject());
        CmsMessages messages = Messages.get().getBundle(l);
        if (value.isValid()) {
            I_CmsSerialDateBean bean = CmsSerialDateBeanFactory.createSerialDateBean(value);
            if (bean.hasTooManyDates()) {
                return new CmsPair<Boolean, String>(
                    Boolean.FALSE,
                    messages.key(
                        Messages.GUI_SERIALDATE_STATUS_TOO_MANY_DATES_2,
                        Integer.valueOf(CmsSerialDateUtil.getMaxEvents()),
                        formatDate(bean.getDates().last())));
            } else {
                SortedSet<Date> dates = bean.getDates();
                String message;
                if (dates.isEmpty()) {
                    message = messages.key(Messages.GUI_SERIALDATE_EMPTY_EVENT_SERIES_0);
                } else if (dates.size() == 1) {
                    message = messages.key(Messages.GUI_SERIALDATE_SINGLE_EVENT_1, formatDate(dates.first()));
                } else {
                    message = messages.key(
                        Messages.GUI_SERIALDATE_MULTIPLE_EVENTS_3,
                        Integer.valueOf(dates.size()),
                        formatDate(dates.first()),
                        formatDate(dates.last()));
                }
                return new CmsPair<Boolean, String>(Boolean.TRUE, message);
            }

        }
        return new CmsPair<Boolean, String>(
            Boolean.FALSE,
            messages.key(Messages.GUI_SERIALDATE_INVALID_SERIES_SPECIFICATION_0));
    }

    /**
     * Format the date for the status messages.
     *
     * @param date the date to format.
     *
     * @return the formatted date.
     */
    private String formatDate(Date date) {

        if (null == m_dateFormat) {
            m_dateFormat = DateFormat.getDateInstance(
                0,
                OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject()));
        }
        return m_dateFormat.format(date);
    }

}
