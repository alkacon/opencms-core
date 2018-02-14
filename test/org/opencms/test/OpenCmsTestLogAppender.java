/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.opencms.test;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.OutputStreamManager;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.core.util.CloseShieldOutputStream;
import org.apache.logging.log4j.core.util.Throwables;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.PropertiesUtil;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Appends log events to <code>System.out</code> or <code>System.err</code> using a layout specified by the user. The
 * default target is <code>System.out</code>.
 * <p>
 * TODO Accessing <code>System.out</code> or <code>System.err</code> as a byte stream instead of a writer bypasses the
 * JVM's knowledge of the proper encoding. (RG) Encoding is handled within the Layout. Typically, a Layout will generate
 * a String and then call getBytes which may use a configured encoding or the system default. OTOH, a Writer cannot
 * print byte streams.
 * </p>
 */
@Plugin(name = OpenCmsTestLogAppender.PLUGIN_NAME, category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public final class OpenCmsTestLogAppender extends AbstractOutputStreamAppender<OutputStreamManager> {

    public static final String PLUGIN_NAME = "OpenCmsTestLog";
    private static final String JANSI_CLASS = "org.fusesource.jansi.WindowsAnsiOutputStream";
    private static OpenCmsTestLogManagerFactory factory = new OpenCmsTestLogManagerFactory();
    private static final Target DEFAULT_TARGET = Target.SYSTEM_OUT;
    private static final AtomicInteger COUNT = new AtomicInteger();

    private final Target target;
    // indicates if a logged error / fatal message should cause a test to fail
    private static boolean m_breakOnError;

    private static I_CmsLogHandler m_handler;

    /**
     * Enumeration of console destinations.
     */
    public enum Target {

        /** Standard output. */
        SYSTEM_OUT {
            @Override
            public Charset getDefaultCharset() {
                // "sun.stdout.encoding" is only set when running from the console.
                return getCharset("sun.stdout.encoding", Charset.defaultCharset());
            }
        },

        /** Standard error output. */
        SYSTEM_ERR {
            @Override
            public Charset getDefaultCharset() {
                // "sun.stderr.encoding" is only set when running from the console.
                return getCharset("sun.stderr.encoding", Charset.defaultCharset());
            }
        };

        public abstract Charset getDefaultCharset();

        protected Charset getCharset(final String property, Charset defaultCharset) {
            return new PropertiesUtil(PropertiesUtil.getSystemProperties()).getCharsetProperty(property, defaultCharset);
        }

    }

    private OpenCmsTestLogAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter,
                            final OutputStreamManager manager, final boolean ignoreExceptions, final Target target) {
        super(name, layout, filter, ignoreExceptions, true, manager);
        this.target = target;
    }

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

    @Override
    public boolean ignoreExceptions() {
        return false == m_breakOnError;
    }

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

    /**
     * Creates a OpenCmsTestLog Appender.
     *
     * @param layout The layout to use (required).
     * @param filter The Filter or null.
     * @param targetStr The target ("SYSTEM_OUT" or "SYSTEM_ERR"). The default is "SYSTEM_OUT".
     * @param name The name of the Appender (required).
     * @param follow If true will follow changes to the underlying output stream.
     * @param ignore If {@code "true"} (default) exceptions encountered when appending events are logged; otherwise they
     *            are propagated to the caller.
     * @return The OpenCmsTestLogAppender.
     * @deprecated Deprecated in 2.7; use {@link #newBuilder()}.
     */
    @Deprecated
    public static OpenCmsTestLogAppender createAppender(Layout<? extends Serializable> layout,
                                                 final Filter filter,
                                                 final String targetStr,
                                                 final String name,
                                                 final String follow,
                                                 final String ignore) {
        if (name == null) {
            LOGGER.error("No name provided for OpenCmsTestLogAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        final boolean isFollow = Boolean.parseBoolean(follow);
        final boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);
        final Target target = targetStr == null ? DEFAULT_TARGET : Target.valueOf(targetStr);
        return new OpenCmsTestLogAppender(name, layout, filter, getManager(target, isFollow, false, layout), ignoreExceptions, target);
    }

    public static OpenCmsTestLogAppender createDefaultAppenderForLayout(final Layout<? extends Serializable> layout) {
        // this method cannot use the builder class without introducing an infinite loop due to DefaultConfiguration
        return new OpenCmsTestLogAppender("DefaultOpenCmsTestLog-" + COUNT.incrementAndGet(), layout, null,
                getDefaultManager(DEFAULT_TARGET, false, false, layout), true, DEFAULT_TARGET);
    }

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return new Builder<B>().asBuilder();
    }

    /**
     * Builds OpenCmsTestLogAppender instances.
     * @param <B> The type to build
     */
    public static class Builder<B extends Builder<B>> extends AbstractOutputStreamAppender.Builder<B>
            implements org.apache.logging.log4j.core.util.Builder<OpenCmsTestLogAppender> {

        @PluginBuilderAttribute
        @Required
        private Target target = DEFAULT_TARGET;

        @PluginBuilderAttribute
        private boolean follow;

        @PluginBuilderAttribute
        private boolean direct;

        public B setTarget(final Target aTarget) {
            this.target = aTarget;
            return asBuilder();
        }

        public B setFollow(final boolean shouldFollow) {
            this.follow = shouldFollow;
            return asBuilder();
        }

        public B setDirect(final boolean shouldDirect) {
            this.direct = shouldDirect;
            return asBuilder();
        }

        @Override
        public OpenCmsTestLogAppender build() {
            if (follow && direct) {
                throw new IllegalArgumentException("Cannot use both follow and direct on OpenCmsTestLogAppender '" + getName() + "'");
            }
            final Layout<? extends Serializable> layout = getOrCreateLayout(target.getDefaultCharset());
            return new OpenCmsTestLogAppender(getName(), layout, getFilter(), getManager(target, follow, direct, layout),
                    isIgnoreExceptions(), target);
        }
    }

    private static OutputStreamManager getDefaultManager(final Target target, final boolean follow, final boolean direct,
                                                         final Layout<? extends Serializable> layout) {
        final OutputStream os = getOutputStream(follow, direct, target);

        // LOG4J2-1176 DefaultConfiguration should not share OutputStreamManager instances to avoid memory leaks.
        final String managerName = target.name() + '.' + follow + '.' + direct + "-" + COUNT.get();
        return OutputStreamManager.getManager(managerName, new FactoryData(os, managerName, layout), factory);
    }

    private static OutputStreamManager getManager(final Target target, final boolean follow, final boolean direct,
                                                  final Layout<? extends Serializable> layout) {
        final OutputStream os = getOutputStream(follow, direct, target);
        final String managerName = target.name() + '.' + follow + '.' + direct;
        return OutputStreamManager.getManager(managerName, new FactoryData(os, managerName, layout), factory);
    }

    private static OutputStream getOutputStream(final boolean follow, final boolean direct, final Target target) {
        final String enc = Charset.defaultCharset().name();
        OutputStream outputStream;
        try {
            // @formatter:off
            outputStream = target == Target.SYSTEM_OUT ?
                    direct ? new FileOutputStream(FileDescriptor.out) :
                            (follow ? new PrintStream(new SystemOutStream(), true, enc) : System.out) :
                    direct ? new FileOutputStream(FileDescriptor.err) :
                            (follow ? new PrintStream(new SystemErrStream(), true, enc) : System.err);
            // @formatter:on
            outputStream = new CloseShieldOutputStream(outputStream);
        } catch (final UnsupportedEncodingException ex) { // should never happen
            throw new IllegalStateException("Unsupported default encoding " + enc, ex);
        }
        final PropertiesUtil propsUtil = PropertiesUtil.getProperties();
        if (!propsUtil.isOsWindows() || propsUtil.getBooleanProperty("log4j.skipJansi", true) || direct) {
            return outputStream;
        }
        try {
            // We type the parameter as a wildcard to avoid a hard reference to Jansi.
            final Class<?> clazz = LoaderUtil.loadClass(JANSI_CLASS);
            final Constructor<?> constructor = clazz.getConstructor(OutputStream.class);
            return new CloseShieldOutputStream((OutputStream) constructor.newInstance(outputStream));
        } catch (final ClassNotFoundException cnfe) {
            LOGGER.debug("Jansi is not installed, cannot find {}", JANSI_CLASS);
        } catch (final NoSuchMethodException nsme) {
            LOGGER.warn("{} is missing the proper constructor", JANSI_CLASS);
        } catch (final Exception ex) {
            LOGGER.warn("Unable to instantiate {} due to {}", JANSI_CLASS, Throwables.getRootCause(ex).toString().trim());
        }
        return outputStream;
    }

    /**
     * An implementation of OutputStream that redirects to the current System.err.
     */
    private static class SystemErrStream extends OutputStream {
        public SystemErrStream() {
        }

        @Override
        public void close() {
            // do not close sys err!
        }

        @Override
        public void flush() {
            System.err.flush();
        }

        @Override
        public void write(final byte[] b) throws IOException {
            System.err.write(b);
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            System.err.write(b, off, len);
        }

        @Override
        public void write(final int b) {
            System.err.write(b);
        }
    }

    /**
     * An implementation of OutputStream that redirects to the current System.out.
     */
    private static class SystemOutStream extends OutputStream {
        public SystemOutStream() {
        }

        @Override
        public void close() {
            // do not close sys out!
        }

        @Override
        public void flush() {
            System.out.flush();
        }

        @Override
        public void write(final byte[] b) throws IOException {
            System.out.write(b);
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            System.out.write(b, off, len);
        }

        @Override
        public void write(final int b) throws IOException {
            System.out.write(b);
        }
    }

    /**
     * Data to pass to factory method.Unable to instantiate
     */
    private static class FactoryData {
        private final OutputStream os;
        private final String name;
        private final Layout<? extends Serializable> layout;

        /**
         * Constructor.
         *
         * @param os The OutputStream.
         * @param type The name of the target.
         * @param layout A Serializable layout
         */
        public FactoryData(final OutputStream os, final String type, final Layout<? extends Serializable> layout) {
            this.os = os;
            this.name = type;
            this.layout = layout;
        }
    }

    /**
     * Factory to create the Appender.
     */
    private static class OpenCmsTestLogManagerFactory implements ManagerFactory<OutputStreamManager, FactoryData> {

        /**
         * Create an OutputStreamManager.
         *
         * @param name The name of the entity to manage.
         * @param data The data required to create the entity.
         * @return The OutputStreamManager
         */
        @Override
        public OutputStreamManager createManager(final String name, final FactoryData data) {
            return new OutputStreamManagerEx(data.os, data.name, data.layout, true);
        }
    }

    private static class OutputStreamManagerEx extends OutputStreamManager{
        protected OutputStreamManagerEx(OutputStream os, String streamName, Layout<?> layout, boolean writeHeader) {
            super(os, streamName, layout, writeHeader);
        }
    }
    public Target getTarget() {
        return target;
    }

}
