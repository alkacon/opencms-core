/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.jcl.LogFactoryImpl;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * Checks that all supported logging APIs send events to Log4j Core exactly once.
 */
public class TestLoggingBridges {

    private static final class RecordingAppender extends AbstractAppender {

        private final List<LogEvent> m_events = new CopyOnWriteArrayList<>();

        private RecordingAppender() {

            super("logging-route-test", null, null, false, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {

            m_events.add(event.toImmutable());
        }

        private long count(String message) {

            return m_events.stream().filter(event -> message.equals(event.getMessage().getFormattedMessage())).count();
        }
    }

    @Test
    public void testLoggingRoutes() throws Exception {

        // Loading the core installs the production JUL bridge.
        Class.forName(OpenCmsCore.class.getName());

        org.apache.logging.log4j.core.Logger root = (org.apache.logging.log4j.core.Logger)LogManager.getRootLogger();
        Level oldLevel = root.getContext().getConfiguration().getRootLogger().getLevel();
        RecordingAppender appender = new RecordingAppender();
        appender.start();
        root.addAppender(appender);
        Configurator.setRootLevel(Level.INFO);
        try {
            assertInstanceOf(LogFactoryImpl.class, LogFactory.getFactory());
            LogFactory.getLog(TestLoggingBridges.class).info("jcl-route-test");
            LoggerFactory.getLogger(TestLoggingBridges.class).info("slf4j-route-test");
            java.util.logging.Logger.getLogger(TestLoggingBridges.class.getName()).info("jul-route-test");

            assertEquals(1, appender.count("jcl-route-test"));
            assertEquals(1, appender.count("slf4j-route-test"));
            assertEquals(1, appender.count("jul-route-test"));
        } finally {
            Configurator.setRootLevel(oldLevel);
            root.removeAppender(appender);
            appender.stop();
        }
    }

}
