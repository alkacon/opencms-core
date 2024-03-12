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

package org.opencms.rmi;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsShell;
import org.opencms.main.CmsShellCommandException;
import org.opencms.main.I_CmsShellCommands;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * RMI object which wraps a CmsShell and can be used for shell command execution.
 */
public class CmsRemoteShell extends UnicastRemoteObject implements I_CmsRemoteShell {

    /**
     * Stores remote shell instances which haven't been unregistered yet.<p>
     */
    static class InstanceStore {

        /** The list of shell instances. */
        private List<CmsRemoteShell> m_instances = Lists.newArrayList();

        /**
         * Adds a new instance.<p>
         *
         * @param shell the instance to add
         */
        public synchronized void add(CmsRemoteShell shell) {

            m_instances.add(shell);
        }

        /**
         * Removes and unexports an instance.<p>
         *
         * @param cmsRemoteShell the instance to remove
         */
        @SuppressWarnings("synthetic-access")
        public synchronized void remove(CmsRemoteShell cmsRemoteShell) {

            try {
                UnicastRemoteObject.unexportObject(cmsRemoteShell, true);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            m_instances.remove(cmsRemoteShell);
        }

        /**
         * Removes and unexports all instances.<p>
         */
        @SuppressWarnings("synthetic-access")
        public synchronized void removeAll() {

            for (CmsRemoteShell shell : m_instances) {
                try {
                    UnicastRemoteObject.unexportObject(shell, true);
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            m_instances.clear();

        }

    }

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRemoteShell.class);

    /** Serial version id. */
    private static final long serialVersionUID = -243325251951003282L;

    /** Stores instances which have yet to be unexported. */
    private static InstanceStore m_instanceStore = new InstanceStore();

    /** Byte array stream used to capture output of shell commands; will be cleared for each individual command. */
    private ByteArrayOutputStream m_baos = new ByteArrayOutputStream();

    /** Random id string for debugging purposes. */
    private String m_id;

    /** The output stream used to capture the shell command output. */
    private PrintStream m_out;

    /** The wrapped shell instance. */
    private CmsShell m_shell;

    /**
     * Creates a new instance.<p>
     *
     * @param additionalCommandsNames comma separated list of full qualified names of classes with additional shell
     *                               commands (may be null)
     * @param port the port to use
     *
     * @throws CmsException if something goes wrong
     * @throws RemoteException if RMI stuff goes wrong
     */
    public CmsRemoteShell(String additionalCommandsNames, int port)
    throws CmsException, RemoteException {

        super(port);
        m_id = RandomStringUtils.randomAlphanumeric(8);
        List<I_CmsShellCommands> additionalCommands = new ArrayList<>();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(additionalCommandsNames)) {
            String[] classNames = additionalCommandsNames.split(",");
            for (String className : classNames) {
                try {
                    className = className.trim();

                    Class<?> commandsCls = Class.forName(className);
                    if (I_CmsShellCommands.class.isAssignableFrom(commandsCls)) {
                        additionalCommands.add((I_CmsShellCommands) commandsCls.getDeclaredConstructor().newInstance());
                        LOG.info("Class " + className + " has been loaded and added to additional commands.");
                    } else {
                        LOG.error("Error: Class " + className + " does not implement I_CmsShellCommands");
                    }
                } catch (ClassNotFoundException e) {
                    final String errMsg = "Error: Could not find the class " + className;
                    LOG.error(errMsg, e);
                    throw new IllegalArgumentException(errMsg, e);
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                         InvocationTargetException e) {
                    final String errMsg = "Error instantiating the class " + className + ". " + e.getLocalizedMessage();
                    LOG.error(errMsg, e);
                    throw new IllegalArgumentException(errMsg, e);
                }
            }
        }

        CmsObject cms = OpenCms.initCmsObject("Guest");
        m_out = new PrintStream(m_baos, true);
        m_shell = new CmsShell(cms, "${user}@${project}:${siteroot}|${uri}>", additionalCommands, m_out, m_out);
        m_instanceStore.add(this);
    }

    /**
     * Removes and unexports all instances.<p>
     */
    public static void unregisterAll() {

        m_instanceStore.removeAll();
    }

    /**
     * @see org.opencms.rmi.I_CmsRemoteShell#end()
     */
    public void end() {

        m_instanceStore.remove(this);
    }

    /**
     * @see org.opencms.rmi.I_CmsRemoteShell#executeCommand(java.lang.String, java.util.List)
     */
    public CmsShellCommandResult executeCommand(String cmd, List<String> params) {

        LOG.debug(m_id + " executing " + cmd + " " + params);
        CmsShellCommandResult result = new CmsShellCommandResult();
        m_baos.reset();
        boolean hasError = false;
        try {
            CmsShell.pushShell(m_shell);
            m_shell.executeCommand(cmd, params);
        } catch (CmsShellCommandException e) {
            hasError = true;
            LOG.warn(m_id + " " + e.getLocalizedMessage(), e);
        } finally {
            CmsShell.popShell();
            m_out.flush();
        }
        hasError |= m_shell.hasReportError();
        result.setExitCalled(m_shell.isExitCalled());
        result.setHasError(hasError);
        result.setErrorCode(m_shell.getErrorCode());
        result.setPrompt(m_shell.getPrompt());
        result.setEcho(m_shell.hasEcho());
        try {
            String outputString = new String(m_baos.toByteArray(), "UTF-8");
            result.setOutput(outputString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @see org.opencms.rmi.I_CmsRemoteShell#getPrompt()
     */
    public String getPrompt() {

        return m_shell.getPrompt();
    }

}
