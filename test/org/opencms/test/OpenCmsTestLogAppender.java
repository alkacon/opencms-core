/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/test/OpenCmsTestLogAppender.java,v $
 * Date   : $Date: 2004/08/10 15:42:43 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.test;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Simple extension of the log4j console appender that throws a
 * <code>RuntimeException</code> if an error (or fatal) event is logged,
 * causing the running test to fail.<p> 
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.5.0
 */
public class OpenCmsTestLogAppender extends ConsoleAppender {

    // indicates if a logged error / faltal message should cause a test to fail
    private static boolean m_breakOnError = false;
    
    /**
     * Sets the "break on error" status.<p>
     * 
     * @param value the "break on error" status to set
     */
    public static void setBreakOnError(boolean value) {
        m_breakOnError = value;
    }
    
    /**
     * @see org.apache.log4j.WriterAppender#append(org.apache.log4j.spi.LoggingEvent)
     */
    public void append(LoggingEvent logEvent) {

        // first log the event as ususal
        super.append(logEvent);
        
        if (m_breakOnError) {
            int logLevel = logEvent.getLevel().toInt();
            switch (logLevel) {
                case Priority.ERROR_INT:
                case Priority.FATAL_INT:
                    throw new RuntimeException(logEvent.getRenderedMessage());
                default:
                    // noop
            }
        }
    }
}
