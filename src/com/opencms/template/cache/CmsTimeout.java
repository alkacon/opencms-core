/*
* File   : $Source
* Date   : $Date
* Version: $Revision
*
* Copyright (C) 2000  The OpenCms Group
*
* This File is part of OpenCms -
* the Open Source Content Mananagement System
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.com
*
* You should have received a copy of the GNU General Public License
* long with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package com.opencms.template.cache;
/**
 * This class is used in the CmsCacheDirectives. If an element can be cached for
 * a specific time, a CmsTimeout must be set. It is planned to have diffrent modi
 * for this Class. In this moment we have only one mode:
 *
 * 0: use the constructor with int x. It means the element has to be new generated
 *      at 00:00 and then every x minutes.
 *
 * @author Hanjo Riege
 * @version 1.0
 */

public class CmsTimeout {

    // indicates the mode this CmsTimeout is running in
    private int modus;

    // USED FOR MODUS 0
    // this is time a element is valid (in millisec)
    private long m_timeinterval;
    // here is the time 0:00 for this day, it is recalculated only if needed
    private static long m_daystart = 0;
    // 24 hours in millisec (24*60*60*1000)
    private static final long C_24_HOURS = 86400000;

    // Constructor for modus 0
    public CmsTimeout(int minutes) {
        modus = 0;
        m_timeinterval = minutes * 60 * 1000;
    }

    /**
     * Returns the last time when the element had to be new generated.
     *
     * @return last change time.
     */
    public long getLastChange(){
        long time = System.currentTimeMillis();

        // MODUS 0 (the only one for now)

        // the time since 0:00 (should be < 24 hours)
        long daytime = time - m_daystart;
        if ( daytime > C_24_HOURS) {
            // the daystart has to be recalculated
            java.util.Calendar timeCal = java.util.Calendar.getInstance();
            timeCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            timeCal.set(java.util.Calendar.SECOND, 0);
            timeCal.set(java.util.Calendar.MINUTE, 0);
            timeCal.set(java.util.Calendar.MILLISECOND, 0);
            m_daystart = timeCal.getTime().getTime();
            daytime = time - m_daystart;
        }
        return m_daystart + (daytime - (daytime % m_timeinterval));

    }
}