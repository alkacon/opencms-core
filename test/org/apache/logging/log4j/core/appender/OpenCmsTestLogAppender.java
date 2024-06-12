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

package org.apache.logging.log4j.core.appender;

import org.opencms.test.I_CmsLogHandler;

import java.io.Serializable;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Simple extension of the log4j console appender that throws a
 * <code>RuntimeException</code> if an error (or fatal) event is logged,
 * causing the running test to fail.<p>
 *
 * @since 11.0.0
 *
 */
@Plugin(name = "TestLogAppender", category = "Core", elementType = "appender", printObject = true)
public class OpenCmsTestLogAppender extends AbstractOutputStreamAppender<OutputStreamManager> {

    /** Indicates if a logged error / fatal message should cause a test to fail. */
    private static boolean m_breakOnError;

    /** Instance counter used for name generation. */
    private static int m_count = 0;

    /** Current log handler. */
    private static I_CmsLogHandler m_handler;

    /**
     * Constructor.<p>
     *
     * @param name appender name
     * @param layout the log layout
     * @param filter the log filter
     * @param manager the output stream manager
     */
    protected OpenCmsTestLogAppender(
        String name,
        Layout<? extends Serializable> layout,
        Filter filter,
        OutputStreamManager manager) {

        super(name, layout, filter, false, true, manager);

    }

    /**
     * Factory method used by log4j2 to create appender instances when reading the log4j2 configuration.<p>
     *
     * @param name the appender name
     * @param layout the log layout
     * @param filter the filter
     *
     * @return the appender instance
     */
    @SuppressWarnings("resource")
    @PluginFactory
    public static OpenCmsTestLogAppender createAppender(
        @PluginAttribute("name") String name,
        @PluginElement("Layout") Layout<? extends Serializable> layout,
        @PluginElement("Filter") final Filter filter) {

        if (name == null) {
            LOGGER.error("No name provided for MyCustomAppenderImpl");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new OpenCmsTestLogAppender(
            name,
            layout,
            filter,
            new OutputStreamManager(System.out, "OpenCmsTestLogAppender_" + m_count++, layout, true));
    }

    /**
     * Sets the "break on error" status.<p>
     *
     * @param value the "break on error" status to set
     */
    public static void setBreakOnError(boolean value) {

        m_breakOnError = value;
    }

    /**
     * Sets the current log handler.<p>
     *
     * @param handler the handler
     */
    public static void setHandler(I_CmsLogHandler handler) {

        m_handler = handler;
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
            if (Thread.currentThread().getClass().getName().endsWith("CmsPublishThread")) {
                return;
            }

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
