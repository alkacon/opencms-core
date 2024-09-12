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

package org.opencms.main;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsDataTypeUtil;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.benchmark.CmsBenchmarkTable;
import org.opencms.util.benchmark.CmsFileBenchmarkReceiver;

import java.awt.event.KeyEvent;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * A command line interface to access OpenCms functions which
 * is used for the initial setup and also can be used for scripting access to the OpenCms
 * repository without the Workplace.<p>
 *
 * The CmsShell has direct access to all methods in the "command objects".
 * Currently, the following classes are used as command objects:
 * <code>{@link org.opencms.main.CmsShellCommands}</code>,
 * <code>{@link org.opencms.file.CmsRequestContext}</code> and
 * <code>{@link org.opencms.file.CmsObject}</code>.<p>
 *
 * It is also possible to add a custom command object when calling the script API,
 * like in {@link CmsShell#CmsShell(String, String, String, String, List, PrintStream, PrintStream, boolean)}.<p>
 *
 * Only public methods in the command objects that use supported data types
 * as parameters can be called from the shell. Supported data types are:
 * <code>String, {@link org.opencms.util.CmsUUID}, boolean, int, long, double, float</code>.<p>
 *
 * If a method name is ambiguous, i.e. the method name with the same number of parameter exist
 * in more then one of the command objects, the method is only executed on the first matching method object.<p>
 *
 * @since 6.0.0
 *
 * @see org.opencms.main.CmsShellCommands
 * @see org.opencms.file.CmsRequestContext
 * @see org.opencms.file.CmsObject
 */
public class CmsShell {

    /**
     * Command object class.<p>
     */
    private class CmsCommandObject {

        /** The list of methods. */
        private Map<String, List<Method>> m_methods;

        /** The object to execute the methods on. */
        private Object m_object;

        /**
         * Creates a new command object.<p>
         *
         * @param object the object to execute the methods on
         */
        protected CmsCommandObject(Object object) {

            m_object = object;
            initShellMethods();
        }

        /**
         * Tries to execute a method for the provided parameters on this command object.<p>
         *
         * If methods with the same name and number of parameters exist in this command object,
         * the given parameters are tried to be converted from String to matching types.<p>
         *
         * @param command the command entered by the user in the shell
         * @param parameters the parameters entered by the user in the shell
         * @return true if a method was executed, false otherwise
         */
        @SuppressWarnings("synthetic-access")
        protected boolean executeMethod(String command, List<String> parameters) {

            m_hasReportError = false;
            // build the method lookup
            String lookup = buildMethodLookup(command, parameters.size());

            // try to look up the methods of this command object
            List<Method> possibleMethods = m_methods.get(lookup);
            if (possibleMethods == null) {
                return false;
            }

            // a match for the method name was found, now try to figure out if the parameters are ok
            Method onlyStringMethod = null;
            Method foundMethod = null;
            Object[] params = null;
            Iterator<Method> i;

            // first check if there is one method with only has String parameters, make this the fall back
            i = possibleMethods.iterator();
            while (i.hasNext()) {
                Method method = i.next();
                Class<?>[] clazz = method.getParameterTypes();
                boolean onlyString = true;
                for (int j = 0; j < clazz.length; j++) {
                    if (!(clazz[j].equals(String.class))) {
                        onlyString = false;
                        break;
                    }
                }
                if (onlyString) {
                    onlyStringMethod = method;
                    break;
                }
            }

            // now check a method matches the provided parameters
            // if so, use this method, else continue searching
            i = possibleMethods.iterator();
            while (i.hasNext()) {
                Method method = i.next();
                if (method == onlyStringMethod) {
                    // skip the String only signature because this would always match
                    continue;
                }
                // now try to convert the parameters to the required types
                Class<?>[] clazz = method.getParameterTypes();
                Object[] converted = new Object[clazz.length];
                boolean match = true;
                for (int j = 0; j < clazz.length; j++) {
                    String value = parameters.get(j);
                    try {
                        converted[j] = CmsDataTypeUtil.parse(value, clazz[j]);
                    } catch (Throwable t) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    // we found a matching method signature
                    params = converted;
                    foundMethod = method;
                    break;
                }

            }

            if ((foundMethod == null) && (onlyStringMethod != null)) {
                // no match found but String only signature available, use this
                params = parameters.toArray();
                foundMethod = onlyStringMethod;
            }

            if ((params == null) || (foundMethod == null)) {
                // no match found at all
                return false;
            }

            // now try to invoke the method
            try {
                Object result = foundMethod.invoke(m_object, params);
                if (result != null) {
                    if (result instanceof Collection<?>) {
                        Collection<?> c = (Collection<?>)result;
                        m_out.println(c.getClass().getName() + " (size: " + c.size() + ")");
                        int count = 0;
                        if (result instanceof Map<?, ?>) {
                            Map<?, ?> m = (Map<?, ?>)result;
                            Iterator<?> j = m.entrySet().iterator();
                            while (j.hasNext()) {
                                Map.Entry<?, ?> entry = (Map.Entry<?, ?>)j.next();
                                m_out.println(count++ + ": " + entry.getKey() + "= " + entry.getValue());
                            }
                        } else {
                            Iterator<?> j = c.iterator();
                            while (j.hasNext()) {
                                m_out.println(count++ + ": " + j.next());
                            }
                        }
                    } else {
                        m_out.println(result.toString());
                    }
                }
            } catch (InvocationTargetException ite) {
                m_out.println(
                    Messages.get().getBundle(getLocale()).key(
                        Messages.GUI_SHELL_EXEC_METHOD_1,
                        new Object[] {foundMethod.getName()}));
                ite.getTargetException().printStackTrace(m_out);
                if (m_errorCode != -1) {
                    throw new CmsShellCommandException(ite.getCause());
                }
            } catch (Throwable t) {
                m_out.println(
                    Messages.get().getBundle(getLocale()).key(
                        Messages.GUI_SHELL_EXEC_METHOD_1,
                        new Object[] {foundMethod.getName()}));
                t.printStackTrace(m_out);
                if (m_errorCode != -1) {
                    throw new CmsShellCommandException(t);
                }
            }
            if (m_hasReportError && (m_errorCode != -1)) {
                throw new CmsShellCommandException(true);
            }
            return true;
        }

        /**
         * Returns a signature overview of all methods containing the given search String.<p>
         *
         * If no method name matches the given search String, the empty String is returned.<p>
         *
         * @param searchString the String to search for, if null all methods are shown
         *
         * @return a signature overview of all methods containing the given search String
         */
        protected String getMethodHelp(String searchString) {

            StringBuffer buf = new StringBuffer(512);
            Iterator<String> i = m_methods.keySet().iterator();
            while (i.hasNext()) {
                List<Method> l = m_methods.get(i.next());
                Iterator<Method> j = l.iterator();
                while (j.hasNext()) {
                    Method method = j.next();
                    if ((searchString == null)
                        || (method.getName().toLowerCase().indexOf(searchString.toLowerCase()) > -1)) {
                        buf.append("* ");
                        buf.append(method.getName());
                        buf.append("(");
                        Class<?>[] params = method.getParameterTypes();
                        for (int k = 0; k < params.length; k++) {
                            String par = params[k].getName();
                            par = par.substring(par.lastIndexOf('.') + 1);
                            if (k != 0) {
                                buf.append(", ");
                            }
                            buf.append(par);
                        }
                        buf.append(")\n");
                    }
                }
            }
            return buf.toString();
        }

        /**
         * Returns the object to execute the methods on.<p>
         *
         * @return the object to execute the methods on
         */
        protected Object getObject() {

            return m_object;
        }

        /**
         * Builds a method lookup String.<p>
         *
         * @param methodName the name of the method
         * @param paramCount the parameter count of the method
         *
         * @return a method lookup String
         */
        private String buildMethodLookup(String methodName, int paramCount) {

            StringBuffer buf = new StringBuffer(32);
            buf.append(methodName.toLowerCase());
            buf.append(" [");
            buf.append(paramCount);
            buf.append("]");
            return buf.toString();
        }

        /**
         * Initializes the map of accessible methods.<p>
         */
        private void initShellMethods() {

            Map<String, List<Method>> result = new TreeMap<String, List<Method>>();

            Method[] methods = m_object.getClass().getMethods();
            for (int i = 0; i < methods.length; i++) {
                // only public methods directly declared in the base class can be used in the shell
                if ((methods[i].getDeclaringClass() == m_object.getClass())
                    && (methods[i].getModifiers() == Modifier.PUBLIC)) {

                    // check if the method signature only uses primitive data types
                    boolean onlyPrimitive = true;
                    Class<?>[] clazz = methods[i].getParameterTypes();
                    for (int j = 0; j < clazz.length; j++) {
                        if (!CmsDataTypeUtil.isParseable(clazz[j])) {
                            // complex data type methods can not be called from the shell
                            onlyPrimitive = false;
                            break;
                        }
                    }

                    if (onlyPrimitive) {
                        // add this method to the set of methods that can be called from the shell
                        String lookup = buildMethodLookup(methods[i].getName(), methods[i].getParameterTypes().length);
                        List<Method> l;
                        if (result.containsKey(lookup)) {
                            l = result.get(lookup);
                        } else {
                            l = new ArrayList<Method>(1);
                        }
                        l.add(methods[i]);
                        result.put(lookup, l);
                    }
                }
            }
            m_methods = result;
        }
    }

    /** Prefix for "additional" parameter. */
    public static final String SHELL_PARAM_ADDITIONAL_COMMANDS = "-additional=";

    /** Prefix for "base" parameter. */
    public static final String SHELL_PARAM_BASE = "-base=";

    /** Prefix for "servletMapping" parameter. */
    public static final String SHELL_PARAM_DEFAULT_WEB_APP = "-defaultWebApp=";

    /** Prefix for errorCode parameter. */
    public static final String SHELL_PARAM_ERROR_CODE = "-errorCode=";

    /** Command line parameter to prevent disabling of JLAN. */
    public static final String SHELL_PARAM_JLAN = "-jlan";

    /** Prefix for "script" parameter. */
    public static final String SHELL_PARAM_SCRIPT = "-script=";

    /** Prefix for "servletMapping" parameter. */
    public static final String SHELL_PARAM_SERVLET_MAPPING = "-servletMapping=";

    /**
     * Thread local which stores the currently active shell instance.
     *
     *  <p>We need multiple ones because shell commands may cause another nested shell to be launched (e.g. for module import scripts).
     */
    public static final ThreadLocal<ArrayList<CmsShell>> SHELL_STACK = ThreadLocal.withInitial(() -> new ArrayList<>());

    /** Boolean variable to disable JLAN. */
    private static boolean JLAN_DISABLED;

    /** The benchmark table. */
    private CmsBenchmarkTable m_benchmarkTable;

    /** The OpenCms context object. */
    protected CmsObject m_cms;

    /** Stream to write the error messages output to. */
    protected PrintStream m_err;

    /** The code which the process should exit with in case of errors; -1 means exit is not called. */
    protected int m_errorCode = -1;

    /** Stream to write the regular output messages to. */
    protected PrintStream m_out;

    /** Additional shell commands object. */
    private List<I_CmsShellCommands> m_additionalShellCommands;

    /** All shell callable objects. */
    private List<CmsCommandObject> m_commandObjects;

    /** If set to true, all commands are echoed. */
    private boolean m_echo;

    /** Indicates if the 'exit' command has been called. */
    private boolean m_exitCalled;

    /** Flag to indicate whether an error was added to a shell report during the last command execution. */
    private boolean m_hasReportError;

    /** Indicates if this is an interactive session with a user sitting on a console. */
    private boolean m_interactive;

    /** The messages object. */
    private CmsMessages m_messages;

    /** The OpenCms system object. */
    private OpenCmsCore m_opencms;

    /** The shell prompt format. */
    private String m_prompt;

    /** The current users settings. */
    private CmsUserSettings m_settings;

    /** Internal shell command object. */
    private I_CmsShellCommands m_shellCommands;

    /**
     * Creates a new CmsShell.<p>
     *
     * @param cms the user context to run the shell from
     * @param prompt the prompt format to set
     * @param additionalShellCommands optional objects for additional shell commands, or null
     * @param out stream to write the regular output messages to
     * @param err stream to write the error messages output to
     */
    public CmsShell(
        CmsObject cms,
        String prompt,
        List<I_CmsShellCommands> additionalShellCommands,
        PrintStream out,
        PrintStream err) {

        setPrompt(prompt);
        try {
            // has to be initialized already if this constructor is used
            m_opencms = null;
            Locale locale = getLocale();
            m_messages = Messages.get().getBundle(locale);
            m_cms = cms;

            // initialize the shell
            initShell(additionalShellCommands, out, err);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing CmsShell: " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Creates a new CmsShell using System.out and System.err for output of the messages.<p>
     *
     * @param webInfPath the path to the 'WEB-INF' folder of the OpenCms installation
     * @param servletMapping the mapping of the servlet (or <code>null</code> to use the default <code>"/opencms/*"</code>)
     * @param defaultWebAppName the name of the default web application (or <code>null</code> to use the default <code>"ROOT"</code>)
     * @param prompt the prompt format to set
     * @param additionalShellCommands optional object for additional shell commands, or null
     */
    public CmsShell(
        String webInfPath,
        String servletMapping,
        String defaultWebAppName,
        String prompt,
        I_CmsShellCommands additionalShellCommands) {

        this(
            webInfPath,
            servletMapping,
            defaultWebAppName,
            prompt,
            additionalShellCommands != null ? Arrays.asList(additionalShellCommands) : Collections.emptyList());
    }

    /**
     * Creates a new CmsShell using System.out and System.err for output of the messages.<p>
     *
     * @param webInfPath the path to the 'WEB-INF' folder of the OpenCms installation
     * @param servletMapping the mapping of the servlet (or <code>null</code> to use the default <code>"/opencms/*"</code>)
     * @param defaultWebAppName the name of the default web application (or <code>null</code> to use the default <code>"ROOT"</code>)
     * @param prompt the prompt format to set
     * @param additionalShellCommands optional objects for additional shell commands, or null
     */
    public CmsShell(
        String webInfPath,
        String servletMapping,
        String defaultWebAppName,
        String prompt,
        List<I_CmsShellCommands> additionalShellCommands) {

        this(
            webInfPath,
            servletMapping,
            defaultWebAppName,
            prompt,
            additionalShellCommands,
            System.out,
            System.err,
            false);
    }


    /**
     * Creates a new CmsShell.<p>
     *
     * @param webInfPath the path to the 'WEB-INF' folder of the OpenCms installation
     * @param servletMapping the mapping of the servlet (or <code>null</code> to use the default <code>"/opencms/*"</code>)
     * @param defaultWebAppName the name of the default web application (or <code>null</code> to use the default <code>"ROOT"</code>)
     * @param prompt the prompt format to set
     * @param additionalShellCommands optional list of objects for additional shell commands, or null
     * @param out stream to write the regular output messages to
     * @param err stream to write the error messages output to
     * @param interactive if <code>true</code> this is an interactive session with a user sitting on a console
     */
    public CmsShell(
        String webInfPath,
        String servletMapping,
        String defaultWebAppName,
        String prompt,
        List<I_CmsShellCommands> additionalShellCommands,
        PrintStream out,
        PrintStream err,
        boolean interactive) {

        setPrompt(prompt);
        setInteractive(interactive);
        if (CmsStringUtil.isEmpty(servletMapping)) {
            servletMapping = "/opencms/*";
        }
        if (CmsStringUtil.isEmpty(defaultWebAppName)) {
            defaultWebAppName = "ROOT";
        }
        try {
            // first initialize runlevel 1
            m_opencms = OpenCmsCore.getInstance();
            // Externalization: get Locale: will be the System default since no CmsObject is up  before
            // runlevel 2
            Locale locale = getLocale();
            m_messages = Messages.get().getBundle(locale);
            // search for the WEB-INF folder
            if (CmsStringUtil.isEmpty(webInfPath)) {
                out.println(m_messages.key(Messages.GUI_SHELL_NO_HOME_FOLDER_SPECIFIED_0));
                out.println();
                webInfPath = CmsFileUtil.searchWebInfFolder(System.getProperty("user.dir"));
                if (CmsStringUtil.isEmpty(webInfPath)) {
                    err.println(m_messages.key(Messages.GUI_SHELL_HR_0));
                    err.println(m_messages.key(Messages.GUI_SHELL_NO_HOME_FOLDER_FOUND_0));
                    err.println();
                    err.println(m_messages.key(Messages.GUI_SHELL_START_DIR_LINE1_0));
                    err.println(m_messages.key(Messages.GUI_SHELL_START_DIR_LINE2_0));
                    err.println(m_messages.key(Messages.GUI_SHELL_HR_0));
                    return;
                }
            }
            out.println(Messages.get().getBundle(locale).key(Messages.GUI_SHELL_WEB_INF_PATH_1, webInfPath));
            // set the path to the WEB-INF folder (the 2nd and 3rd parameters are just reasonable dummies)
            CmsServletContainerSettings settings = new CmsServletContainerSettings(
                webInfPath,
                defaultWebAppName,
                servletMapping,
                null,
                null);
            m_opencms.getSystemInfo().init(settings);
            // now read the configuration properties
            String propertyPath = m_opencms.getSystemInfo().getConfigurationFileRfsPath();
            out.println(m_messages.key(Messages.GUI_SHELL_CONFIG_FILE_1, propertyPath));
            out.println();
            CmsParameterConfiguration configuration = new CmsParameterConfiguration(propertyPath);

            // now upgrade to runlevel 2
            m_opencms = m_opencms.upgradeRunlevel(configuration);

            // create a context object with 'Guest' permissions
            m_cms = m_opencms.initCmsObject(m_opencms.getDefaultUsers().getUserGuest());

            // initialize the shell
            initShell(additionalShellCommands, out, err);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing CmsShell: " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Gets the top of thread-local shell stack, or null if it is empty.
     *
     * @return the top of the shell stack
     */
    public static CmsShell getTopShell() {

        ArrayList<CmsShell> shells = SHELL_STACK.get();
        if (shells.isEmpty()) {
            return null;
        }
        return shells.get(shells.size() - 1);

    }

    /**
     * Check if JLAN should be disabled.<p>
     *
     * @return true if JLAN should be disabled
     */
    public static boolean isJlanDisabled() {

        return JLAN_DISABLED;
    }

    /**
     * Main program entry point when started via the command line.<p>
     *
     * @param args parameters passed to the application via the command line
     */
    public static void main(String[] args) {

        JLAN_DISABLED = true;
        boolean wrongUsage = false;
        String webInfPath = null;
        String script = null;
        String servletMapping = null;
        String defaultWebApp = null;
        String additionalCommandsNames = null;
        int errorCode = -1;
        if (args.length > 4) {
            wrongUsage = true;
        } else {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.startsWith(SHELL_PARAM_BASE)) {
                    webInfPath = arg.substring(SHELL_PARAM_BASE.length());
                } else if (arg.startsWith(SHELL_PARAM_SCRIPT)) {
                    script = arg.substring(SHELL_PARAM_SCRIPT.length());
                } else if (arg.startsWith(SHELL_PARAM_SERVLET_MAPPING)) {
                    servletMapping = arg.substring(SHELL_PARAM_SERVLET_MAPPING.length());
                } else if (arg.startsWith(SHELL_PARAM_DEFAULT_WEB_APP)) {
                    defaultWebApp = arg.substring(SHELL_PARAM_DEFAULT_WEB_APP.length());
                } else if (arg.startsWith(SHELL_PARAM_ADDITIONAL_COMMANDS)) {
                    additionalCommandsNames = arg.substring(SHELL_PARAM_ADDITIONAL_COMMANDS.length());
                } else if (arg.startsWith(SHELL_PARAM_ERROR_CODE)) {
                    errorCode = Integer.valueOf(arg.substring(SHELL_PARAM_ERROR_CODE.length())).intValue();
                } else if (arg.startsWith(SHELL_PARAM_JLAN)) {
                    JLAN_DISABLED = false;
                } else {
                    System.out.println(Messages.get().getBundle().key(Messages.GUI_SHELL_WRONG_USAGE_0));
                    wrongUsage = true;
                }
            }
        }
        if (wrongUsage) {
            System.out.println(Messages.get().getBundle().key(Messages.GUI_SHELL_USAGE_1, CmsShell.class.getName()));
        } else {

            List<I_CmsShellCommands> additionalCommands = new ArrayList<>();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(additionalCommandsNames)) {
                String[] classNames = additionalCommandsNames.split(",");
                for (String className : classNames) {
                    try {
                        className = className.trim();

                        Class<?> commandClass = Class.forName(className);
                        if (I_CmsShellCommands.class.isAssignableFrom(commandClass)) {
                            additionalCommands.add(
                                (I_CmsShellCommands)commandClass.getDeclaredConstructor().newInstance());
                            System.out.println(
                                "Class " + className + " has been loaded and added to additional commands.");
                        } else {
                            // The class does not implement the required interface
                            System.err.println(
                                Messages.get().getBundle().key(
                                    Messages.GUI_SHELL_ERR_ADDITIONAL_COMMANDS_NOT_CMSSHELLCOMMANDS_1,
                                    className));
                            return;
                        }
                    } catch (ClassNotFoundException e) {
                        System.out.println(
                            Messages.get().getBundle().key(
                                Messages.GUI_SHELL_ERR_ADDITIONAL_COMMANDS_CLASS_NOT_FOUND_1,
                                className));
                        return;
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                    | InvocationTargetException e) {
                        System.out.println(
                            Messages.get().getBundle().key(
                                Messages.GUI_SHELL_ERR_ADDITIONAL_COMMANDS_DURING_INSTANTIATION_2,
                                className,
                                e.getLocalizedMessage()));
                        return;
                    }
                }
            }
            boolean interactive = true;
            FileInputStream stream = null;
            if (script != null) {
                try {
                    stream = new FileInputStream(script);
                } catch (IOException exc) {
                    System.out.println(Messages.get().getBundle().key(Messages.GUI_SHELL_ERR_SCRIPTFILE_1, script));
                }
            }
            if (stream == null) {
                // no script-file, use standard input stream
                stream = new FileInputStream(FileDescriptor.in);
                interactive = true;
            }
            CmsShell shell = new CmsShell(
                webInfPath,
                servletMapping,
                defaultWebApp,
                "${user}@${project}:${siteroot}|${uri}>",
                additionalCommands,
                System.out,
                System.err,
                interactive);
            shell.m_errorCode = errorCode;
            shell.execute(stream);
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Removes top of thread-local shell stack.
     */
    public static void popShell() {

        ArrayList<CmsShell> shells = SHELL_STACK.get();
        if (shells.size() > 0) {
            shells.remove(shells.size() - 1);
        }

    }

    /**
     * Pushes shell instance on thread-local stack.
     *
     * @param shell the shell to push
     */
    public static void pushShell(CmsShell shell) {

        SHELL_STACK.get().add(shell);
    }

    /**
    * If running in the context of a CmsShell, this method notifies the running shell instance that an error has occured in a report.<p>
    */
    public static void setReportError() {

        CmsShell instance = getTopShell();
        if (instance != null) {
            instance.m_hasReportError = true;
        }
    }

    /**
     * Executes the commands from the given input stream in this shell.<p>
     *
     * <ul>
     * <li>Commands in the must be separated with a line break '\n'.
     * <li>Only one command per line is allowed.
     * <li>String parameters must be quoted like this: <code>'string value'</code>.
     * </ul>
     *
     * @param inputStream the input stream from which the commands are read
     */
    public void execute(InputStream inputStream) {

        execute(new InputStreamReader(inputStream));
    }

    /**
     * Executes the commands from the given reader in this shell.<p>
     *
     * <ul>
     * <li>Commands in the must be separated with a line break '\n'.
     * <li>Only one command per line is allowed.
     * <li>String parameters must be quoted like this: <code>'string value'</code>.
     * </ul>
     *
     * @param reader the reader from which the commands are read
     */
    public void execute(Reader reader) {

        try {
            pushShell(this);
            LineNumberReader lnr = new LineNumberReader(reader);
            while (!m_exitCalled) {
                String line = lnr.readLine();
                if (m_interactive || m_echo) {
                    // print the prompt in front of the commands to process only when 'interactive'
                    if ((line != null) | m_interactive) {
                        printPrompt();
                    }
                }

                if (line == null) {
                    // if null the file has been read to the end
                    if (m_interactive) {
                        try {
                            Thread.sleep(500);
                        } catch (Throwable t) {
                            // noop
                        }
                    }
                    // end the while loop
                    break;
                }
                if (line.trim().startsWith("#")) {
                    m_out.println(line);
                    continue;
                }
                // In linux, the up and down arrows generate escape sequences that cannot be properly handled.
                // If a escape sequence is detected, OpenCms prints a warning message
                if (line.indexOf(KeyEvent.VK_ESCAPE) != -1) {
                    m_out.println(m_messages.key(Messages.GUI_SHELL_ESCAPE_SEQUENCES_NOT_SUPPORTED_0));
                    continue;
                }
                StringReader lineReader = new StringReader(line);
                StreamTokenizer st = new StreamTokenizer(lineReader);
                st.eolIsSignificant(true);
                st.wordChars('*', '*');
                // put all tokens into a List
                List<String> parameters = new ArrayList<String>();
                while (st.nextToken() != StreamTokenizer.TT_EOF) {
                    if (st.ttype == StreamTokenizer.TT_NUMBER) {
                        parameters.add(Integer.toString(Double.valueOf(st.nval).intValue()));
                    } else {
                        if (null != st.sval) {
                            parameters.add(st.sval);
                        }
                    }
                }
                lineReader.close();

                if (parameters.size() == 0) {
                    // empty line, just need to check if echo is on
                    if (m_echo) {
                        m_out.println();
                    }
                    continue;
                }

                // extract command and arguments
                String command = parameters.get(0);
                List<String> arguments = parameters.subList(1, parameters.size());

                // execute the command with the given arguments
                executeCommand(command, arguments);
            }
        } catch (Throwable t) {
            if (!(t instanceof CmsShellCommandException)) {
                // in case it's a shell command exception, the stack trace has already been written
                t.printStackTrace(m_err);
            }
            if (m_errorCode != -1) {
                System.exit(m_errorCode);
            }
        } finally {
            popShell();
        }
    }

    /**
     * Executes the commands from the given string in this shell.<p>
     *
     * <ul>
     * <li>Commands in the must be separated with a line break '\n'.
     * <li>Only one command per line is allowed.
     * <li>String parameters must be quoted like this: <code>'string value'</code>.
     * </ul>
     *
     * @param commands the string from which the commands are read
     */
    public void execute(String commands) {

        execute(new StringReader(commands));
    }

    /**
     * Executes a shell command with a list of parameters.<p>
     *
     * @param command the command to execute
     * @param parameters the list of parameters for the command
     */
    public void executeCommand(String command, List<String> parameters) {

        if (null == command) {
            return;
        }


        if (m_echo) {
            // echo the command to STDOUT
            m_out.print(command);
            boolean censorParams = Arrays.asList("login", "loginUser", "changePassword").contains(command);
            for (int i = 0; i < parameters.size(); i++) {
                m_out.print(" '");
                m_out.print(censorParams ? "******" : parameters.get(i));
                m_out.print("'");
            }
            m_out.println();
        }

        // prepare to lookup a method in CmsObject or CmsShellCommands
        boolean executed = false;
        Iterator<CmsCommandObject> i = m_commandObjects.iterator();
        while (!executed && i.hasNext()) {
            CmsCommandObject cmdObj = i.next();
            executed = cmdObj.executeMethod(command, parameters);
        }

        if (!executed) {
            // method not found
            m_out.println();
            StringBuffer commandMsg = new StringBuffer(command).append("(");
            for (int j = 0; j < parameters.size(); j++) {
                commandMsg.append("value");
                if (j < (parameters.size() - 1)) {
                    commandMsg.append(", ");
                }
            }
            commandMsg.append(")");

            m_out.println(m_messages.key(Messages.GUI_SHELL_METHOD_NOT_FOUND_1, commandMsg.toString()));
            m_out.println(m_messages.key(Messages.GUI_SHELL_HR_0));
            ((CmsShellCommands)m_shellCommands).help();
        }
    }

    /**
     * Exits this shell and destroys the OpenCms instance.<p>
     */
    public void exit() {

        if (m_exitCalled) {
            return;
        }
        m_exitCalled = true;
        try {
            if ((m_additionalShellCommands != null) && !m_additionalShellCommands.isEmpty()) {
                for (I_CmsShellCommands shellCommands : m_additionalShellCommands) {
                    if (shellCommands != null) {
                        shellCommands.shellExit();
                    }
                }
            } else if (null != m_shellCommands) {
                m_shellCommands.shellExit();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (m_opencms != null) {
            // if called by an in line script we don't want to kill the whole instance
            try {
                m_opencms.shutDown();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * Gets the benchmark table for the shell, lazily initializing it if it didn't exist yet.
     *
     * @return the benchmark table for the shell.
     */
    public CmsBenchmarkTable getBenchmarkTable() {

        if (m_benchmarkTable == null) {
            m_benchmarkTable = new CmsBenchmarkTable(new CmsFileBenchmarkReceiver());
        }
        return m_benchmarkTable;
    }

    /**
     * Returns the stream this shell writes its error messages to.<p>
     *
     * @return the stream this shell writes its error messages to
     */
    public PrintStream getErr() {

        return m_err;
    }

    /**
     * Gets the error code.<p>
     *
     * @return the error code
     */
    public int getErrorCode() {

        return m_errorCode;
    }

    /**
     * Private internal helper for localization to the current user's locale
     * within OpenCms. <p>
     *
     * @return the current user's <code>Locale</code>.
     */
    public Locale getLocale() {

        if (getSettings() == null) {
            return CmsLocaleManager.getDefaultLocale();
        }
        return getSettings().getLocale();
    }

    /**
     * Returns the localized messages object for the current user.<p>
     *
     * @return the localized messages object for the current user
     */
    public CmsMessages getMessages() {

        return m_messages;
    }

    /**
     * Returns the stream this shell writes its regular messages to.<p>
     *
     * @return the stream this shell writes its regular messages to
     */
    public PrintStream getOut() {

        return m_out;
    }

    /**
     * Gets the prompt.<p>
     *
     * @return the prompt
     */
    public String getPrompt() {

        String prompt = m_prompt;
        try {
            prompt = CmsStringUtil.substitute(prompt, "${user}", m_cms.getRequestContext().getCurrentUser().getName());
            prompt = CmsStringUtil.substitute(prompt, "${siteroot}", m_cms.getRequestContext().getSiteRoot());
            prompt = CmsStringUtil.substitute(
                prompt,
                "${project}",
                m_cms.getRequestContext().getCurrentProject().getName());
            prompt = CmsStringUtil.substitute(prompt, "${uri}", m_cms.getRequestContext().getUri());
        } catch (Throwable t) {
            // ignore
        }
        return prompt;
    }

    /**
     * Obtain the additional settings related to the current user.
     *
     * @return the additional settings related to the current user.
     */
    public CmsUserSettings getSettings() {

        return m_settings;
    }

    /**
     * Returns true if echo mode is on.<p>
     *
     * @return true if echo mode is on
     */
    public boolean hasEcho() {

        return m_echo;
    }

    /**
     * Checks whether a report error occurred during execution of the last command.<p>
     *
     * @return true if a report error occurred
     */
    public boolean hasReportError() {

        return m_hasReportError;
    }

    /**
     * Initializes the CmsShell.<p>
     *
     * @param additionalShellCommands optional objects for additional shell commands, or null
     * @param out stream to write the regular output messages to
     * @param err stream to write the error messages output to
     */
    public void initShell(List<I_CmsShellCommands> additionalShellCommands, PrintStream out, PrintStream err) {

        // set the output streams
        m_out = out;
        m_err = err;

        // initialize the settings of the user
        m_settings = initSettings();

        // initialize shell command object
        m_shellCommands = new CmsShellCommands();
        m_shellCommands.initShellCmsObject(m_cms, this);

        // initialize additional shell command object
        if ((additionalShellCommands != null) && !additionalShellCommands.isEmpty()) {
            m_additionalShellCommands = additionalShellCommands;
            for (I_CmsShellCommands shellCommands : additionalShellCommands) {
                if (shellCommands != null) {
                    shellCommands.initShellCmsObject(m_cms, this);
                    shellCommands.shellStart();
                }
            }
        } else {
            m_shellCommands.shellStart();
        }

        m_commandObjects = new ArrayList<CmsCommandObject>();
        if ((m_additionalShellCommands != null) && !m_additionalShellCommands.isEmpty()) {
            for (I_CmsShellCommands shellCommands : m_additionalShellCommands) {
                if (shellCommands != null) {
                    // get all shell callable methods from the additional shell command object
                    m_commandObjects.add(new CmsCommandObject(shellCommands));
                }
            }
        }
        // get all shell callable methods from the CmsShellCommands
        m_commandObjects.add(new CmsCommandObject(m_shellCommands));
        // get all shell callable methods from the CmsRequestContext
        m_commandObjects.add(new CmsCommandObject(m_cms.getRequestContext()));
        // get all shell callable methods from the CmsObject
        m_commandObjects.add(new CmsCommandObject(m_cms));
    }

    /**
     * Returns true if exit was called.<p>
     *
     * @return true if exit was called
     */
    public boolean isExitCalled() {

        return m_exitCalled;
    }

    /**
     * If <code>true</code> this is an interactive session with a user sitting on a console.<p>
     *
     * @return <code>true</code> if this is an interactive session with a user sitting on a console
     */
    public boolean isInteractive() {

        return m_interactive;
    }

    /**
     * Prints the shell prompt.<p>
     */
    public void printPrompt() {

        String prompt = getPrompt();
        m_out.print(prompt);
        m_out.flush();
    }

    /**
     * Set <code>true</code> if this is an interactive session with a user sitting on a console.<p>
     *
     * This controls the output of the prompt and some other info that is valuable
     * on the console, but not required in an automatic session.<p>
     *
     * @param interactive if <code>true</code> this is an interactive session with a user sitting on a console
     */
    public void setInteractive(boolean interactive) {

        m_interactive = interactive;
    }

    /**
     * Sets the locale of the current user.<p>
     *
     * @param locale the locale to set
     *
     * @throws CmsException in case the locale of the current user can not be stored
     */
    public void setLocale(Locale locale) throws CmsException {

        CmsUserSettings settings = getSettings();
        if (settings != null) {
            settings.setLocale(locale);
            settings.save(m_cms);
            m_messages = Messages.get().getBundle(locale);
        }
    }

    /**
     * Reads the given stream and executes the commands in this shell.<p>
     *
     * @param inputStream an input stream from which commands are read
     * @deprecated use {@link #execute(InputStream)} instead
     */
    @Deprecated
    public void start(FileInputStream inputStream) {

        setInteractive(true);
        execute(inputStream);
    }

    /**
     * Validates the given user and password and checks if the user has the requested role.<p>
     *
     * @param userName the user name
     * @param password the password
     * @param requiredRole the required role
     *
     * @return <code>true</code> if the user is valid
     */
    public boolean validateUser(String userName, String password, CmsRole requiredRole) {

        boolean result = false;
        try {
            CmsUser user = m_cms.readUser(userName, password);
            result = OpenCms.getRoleManager().hasRole(m_cms, user.getName(), requiredRole);
        } catch (CmsException e) {
            // nothing to do
        }
        return result;
    }

    /**
     * Shows the signature of all methods containing the given search String.<p>
     *
     * @param searchString the String to search for in the methods, if null all methods are shown
     */
    protected void help(String searchString) {

        String commandList;
        boolean foundSomething = false;
        m_out.println();

        Iterator<CmsCommandObject> i = m_commandObjects.iterator();
        while (i.hasNext()) {
            CmsCommandObject cmdObj = i.next();
            commandList = cmdObj.getMethodHelp(searchString);
            if (!CmsStringUtil.isEmpty(commandList)) {

                m_out.println(
                    m_messages.key(Messages.GUI_SHELL_AVAILABLE_METHODS_1, cmdObj.getObject().getClass().getName()));
                m_out.println(commandList);
                foundSomething = true;
            }
        }

        if (!foundSomething) {
            m_out.println(m_messages.key(Messages.GUI_SHELL_MATCH_SEARCHSTRING_1, searchString));
        }
    }

    /**
     * Initializes the internal <code>CmsWorkplaceSettings</code> that contain (amongst other
     * information) important information additional information about the current user
     * (an instance of {@link CmsUserSettings}).<p>
     *
     * This step is performed within the <code>CmsShell</code> constructor directly after
     * switching to run-level 2 and obtaining the <code>CmsObject</code> for the guest user as
     * well as when invoking the CmsShell command <code>login</code>.<p>
     *
     * @return the user settings for the current user.
     */
    protected CmsUserSettings initSettings() {

        m_settings = new CmsUserSettings(m_cms);
        return m_settings;
    }

    /**
     * Sets the echo status.<p>
     *
     * @param echo the echo status to set
     */
    protected void setEcho(boolean echo) {

        m_echo = echo;
    }

    /**
     * Sets the current shell prompt.<p>
     *
     * To set the prompt, the following variables are available:<p>
     *
     * <code>$u</code> the current user name<br>
     * <code>$s</code> the current site root<br>
     * <code>$p</code> the current project name<p>
     *
     * @param prompt the prompt to set
     */
    protected void setPrompt(String prompt) {

        m_prompt = prompt;
    }
}
