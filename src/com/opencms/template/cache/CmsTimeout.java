/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsTimeout.java,v $
* Date   : $Date: 2005/02/18 14:23:16 $
* Version: $Revision: 1.7 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001-2005  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsTimeout {

    // USED FOR MODUS 0
    // this is time a element is valid (in millisec)
    private long m_timeinterval;
    // here is the time 0:00 for this day, it is recalculated only if needed
    private static long m_daystart = 0;
    // 24 hours in millisec (24*60*60*1000)
    private static final long C_24_HOURS = 86400000;

    // Constructor for modus 0
    public CmsTimeout(int minutes) {
        m_timeinterval = minutes * 60 * 1000;
    }

    /**
     * The proxy cache is set for 300 seconds. If this Timeout shows problems with
     * this interval the method returns false. In modus 0 <=> timeintervals < 300.
     * @return false if the element is not proxycacheable.
     */
    public boolean isProxyCacheable(){

        // MODUS 0
        if(m_timeinterval < 5*60*1000){
            return false;
        }

        return true;
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