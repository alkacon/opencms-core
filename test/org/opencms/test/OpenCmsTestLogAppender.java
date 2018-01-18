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

package org.opencms.test;

import java.io.Serializable;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.OutputStreamManager;

/**
 * Simple extension of the log4j console appender that throws a
 * <code>RuntimeException</code> if an error (or fatal) event is logged,
 * causing the running test to fail.<p>
 *
 * @since 6.0.0
 *
 * @deprecated NOT TESTED for log4j2
 */
@Deprecated
public class OpenCmsTestLogAppender extends AbstractOutputStreamAppender<OutputStreamManager> {

    // indicates if a logged error / fatal message should cause a test to fail
    private static boolean m_breakOnError;

    private static I_CmsLogHandler m_handler;

    /**
     * Sets the "break on error" status.<p>
     *
     * @param value the "break on error" status to set
     */
    public static void setBreakOnError(boolean value) {

        m_breakOnError = value;
    }

    public static void setHandler(I_CmsLogHandler handler) {

        m_handler = handler;
    }

    protected OpenCmsTestLogAppender(
        String name,
        Layout<? extends Serializable> layout,
        Filter filter,
        boolean ignoreExceptions,
        boolean immediateFlush,
        OutputStreamManager manager) {

        super(name, layout, filter, ignoreExceptions, immediateFlush, manager);

    }

    /**
     * @see org.apache.logging.log4j.core.Appender#append(LogEvent event)
     */
    @Override
    public void append(LogEvent logEvent) {

        // first log the event as usual
        super.append(logEvent);
        if (m_handler != null) {
            m_handler.handleLogEvent(logEvent);
        }

        if (m_breakOnError) {
            int logLevel = logEvent.getLevel().intLevel();
            if ((logLevel == Level.ERROR.intLevel()) || (logLevel == Level.FATAL.intLevel())) {
                if (logEvent.getThrownProxy() != null) {
                    if (logEvent.getThrownProxy().getThrowable() != null) {
                        throw new RuntimeException(
                            logEvent.getMessage().getFormattedMessage(),
                            logEvent.getThrownProxy().getThrowable());
                    }
                }
                throw new RuntimeException(logEvent.getMessage().getFormattedMessage());
            }
        }
    }
}
