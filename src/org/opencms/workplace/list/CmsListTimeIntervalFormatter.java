/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListTimeIntervalFormatter.java,v $
 * Date   : $Date: 2005/06/15 16:01:31 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.list;

import org.opencms.i18n.CmsMessageContainer;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Simple formatter for time.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com)
 * @version $Revision: 1.1 $
 * @since 5.7.3
 */
public class CmsListTimeIntervalFormatter extends CmsListMacroFormatter {

    private static final NumberFormat NF2 = NumberFormat.getIntegerInstance();
    private static final NumberFormat NF3 = NumberFormat.getIntegerInstance();
    
    /**
     * Default constructor.<p>
     * 
     * Use hh:mm:ss.mmm style.<p>
     */
    public CmsListTimeIntervalFormatter() {

        this(Messages.get().container(Messages.GUI_LIST_INTERVAL_FORMAT_0));
    }

    /**
     * Customizable constructor.<p>
     * 
     * The mask can take up to 4 params:<p> 
     * <ul>
     *    <li>Millis</li>
     *    <li>Seconds</li>
     *    <li>Minutes</li>
     *    <li>Hours</li>
     * </ul><p>
     * 
     * @param mask the style for the interval
     */
    public CmsListTimeIntervalFormatter(CmsMessageContainer mask) {

        super(mask);
        NF2.setMinimumIntegerDigits(2);
        NF3.setMinimumIntegerDigits(3);
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListFormatter#format(java.lang.Object, java.util.Locale)
     */
    public String format(Object data, Locale locale) {

        if (data == null || !(data instanceof Long)) {
            return "";
        }
        long tmp = ((Long)data).longValue();
        int hrs = (int)(tmp / 3600000);
        tmp -= hrs * 3600000; // 1000 * 60 * 60
        int mins = (int)(tmp / 60000);
        tmp -= mins * 60000; // 1000 * 60
        int secs = (int)(tmp / 1000);
        int millis = (int)(tmp - secs * 1000);

        String locMask = getMask().key(locale);
        MessageFormat formatter = new MessageFormat(locMask, locale);
        
        return formatter.format(new Object[] {
            NF3.format(millis),
            NF2.format(secs),
            NF2.format(mins),
            NF2.format(hrs)});
    }
    
}
