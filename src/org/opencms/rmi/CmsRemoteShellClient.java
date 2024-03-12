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

package org.opencms.rmi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Client application used to connect locally to the CmsShell server.<p>
 */
public class CmsRemoteShellClient {

    /** Command parameter for passing an additional shell commands class name. */
    public static final String PARAM_ADDITIONAL = "additional";

    /** Command parameter for controlling the port to use for the initial RMI lookup. */
    public static final String PARAM_REGISTRY_PORT = "registryPort";

    /** Command parameter for controlling the host to use for the initial RMI lookup. */
    public static final String PARAM_REGISTRY_HOST = "registryHost";

    /** Command parameter for passing a shell script file name. */
    public static final String PARAM_SCRIPT = "script";

    /** The name of the additional commands classes. */
    private final String m_additionalCommands;

    /** True if echo mode is turned on. */
    private boolean m_echo;

    /** The error code which should be returned in case of errors. */
    private int m_errorCode;

    /** True if exit was called. */
    private boolean m_exitCalled;

    /** True if an error occurred. */
    private boolean m_hasError;

    /** The input stream to read the commands from. */
    private InputStream m_input;

    /** Controls whether shell is interactive. */
    private boolean m_interactive;

    /** The output stream. */
    private PrintStream m_out;

    /** The prompt. */
    private String m_prompt;

    /** The port used for the RMI registry. */
    private int m_registryPort;

    /** The host name used for the RMI registry. */
    private String m_registryHost;

    /** The RMI referencce to the shell server. */
    private I_CmsRemoteShell m_remoteShell;

    /**
     * Creates a new instance.<p>
     *
     * @param args the parameters
     * @throws IOException if something goes wrong
     */
    public CmsRemoteShellClient(String[] args)
    throws IOException {

        Map<String, String> params = parseArgs(args);
        String script = params.get(PARAM_SCRIPT);
        if (script == null) {
            m_interactive = true;
            m_input = System.in;
        } else {
            m_input = new FileInputStream(script);
        }
        m_additionalCommands = params.get(PARAM_ADDITIONAL);
        String port = params.get(PARAM_REGISTRY_PORT);
        m_registryPort = CmsRemoteShellConstants.DEFAULT_PORT;
        if (port != null) {
            try {
                m_registryPort = Integer.parseInt(port);
                if (m_registryPort < 0) {
                    System.out.println("Invalid port: " + port);
                    System.exit(1);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid port: " + port);
                System.exit(1);
            }
        }
        m_registryHost = params.get(PARAM_REGISTRY_HOST);
    }

    /**
     * Main method, which starts the shell client.<p>
     *
     * @param args the command line arguments
     * @throws Exception if something goes wrong
     */
    public static void main(String[] args) throws Exception {

        CmsRemoteShellClient client = new CmsRemoteShellClient(args);
        client.run();
    }

    /**
     * Validates, parses and returns the command line arguments.<p>
     *
     * @param args the command line arguments
     * @return the map of parsed arguments
     */
    public Map<String, String> parseArgs(String[] args) {

        Map<String, String> result = new HashMap<String, String>();
        Set<String> allowedKeys = new HashSet<String>(
            Arrays.asList(PARAM_ADDITIONAL, PARAM_SCRIPT, PARAM_REGISTRY_PORT, PARAM_REGISTRY_HOST));
        for (String arg : args) {
            if (arg.startsWith("-")) {
                int eqPos = arg.indexOf("=");
                if (eqPos >= 0) {
                    String key = arg.substring(1, eqPos);
                    if (!allowedKeys.contains(key)) {
                        wrongUsage();
                    }
                    String val = arg.substring(eqPos + 1);
                    result.put(key, val);
                } else {
                    wrongUsage();
                }
            } else {
                wrongUsage();
            }
        }
        return result;
    }

    /**
     * Main loop of the shell server client.<p>
     *
     * Reads commands from either stdin or a file, executes them remotely and displays the results.
     *
     * @throws Exception if something goes wrong
     */
    public void run() throws Exception {

        Registry registry = LocateRegistry.getRegistry(m_registryHost, m_registryPort);
        I_CmsRemoteShellProvider provider = (I_CmsRemoteShellProvider)(registry.lookup(
            CmsRemoteShellConstants.PROVIDER));
        m_remoteShell = provider.createShell(m_additionalCommands);
        m_prompt = m_remoteShell.getPrompt();
        m_out = new PrintStream(System.out);
        try {
            LineNumberReader lnr = new LineNumberReader(new InputStreamReader(m_input, "UTF-8"));
            while (!exitCalled()) {
                if (m_interactive || isEcho()) {
                    // print the prompt in front of the commands to process only when 'interactive'
                    printPrompt();
                }
                String line = lnr.readLine();
                if (line == null) {
                    break;
                }
                if (line.trim().startsWith("#")) {
                    m_out.println(line);
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
                        parameters.add(st.sval);
                    }
                }
                lineReader.close();

                if (parameters.size() == 0) {
                    // empty line, just need to check if echo is on
                    if (isEcho()) {
                        m_out.println();
                    }
                    continue;
                }

                // extract command and arguments
                String command = parameters.get(0);
                List<String> arguments = new ArrayList<String>(parameters.subList(1, parameters.size()));

                // execute the command with the given arguments
                executeCommand(command, arguments);

            }
            exit(0);
        } catch (Throwable t) {
            t.printStackTrace();
            if (m_errorCode != -1) {
                exit(m_errorCode);
            }
        }
    }

    /**
     * Executes a command remotely, displays the command output and updates the internal state.<p>
     *
     * @param command the command
     * @param arguments the arguments
     */
    private void executeCommand(String command, List<String> arguments) {

        try {
            CmsShellCommandResult result = m_remoteShell.executeCommand(command, arguments);
            m_out.print(result.getOutput());
            updateState(result);
            if (m_exitCalled) {
                exit(0);
            } else if (m_hasError && (m_errorCode != -1)) {
                exit(m_errorCode);
            }
        } catch (RemoteException r) {
            r.printStackTrace(System.err);
            exit(1);
        }
    }

    /**
     * Exits the shell with an error code, and if possible, notifies the remote shell that it is exiting.<p>
     *
     * @param errorCode the error code
     */
    private void exit(int errorCode) {

        try {
            m_remoteShell.end();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(errorCode);
    }

    /**
     * Returns true if the exit command has been called.<p>
     *
     * @return true if the exit command has been called
     */
    private boolean exitCalled() {

        return m_exitCalled;
    }

    /**
     * Returns true if echo mode is enabled.<p>
     *
     * @return true if echo mode is enabled
     */
    private boolean isEcho() {

        return m_echo;
    }

    /**
     * Prints the prompt.<p>
     */
    private void printPrompt() {

        System.out.print(m_prompt);
    }

    /**
     * Updates the internal client state based on the state received from the server.<p>
     *
     * @param result the result of the last shell command execution
     */
    private void updateState(CmsShellCommandResult result) {

        m_errorCode = result.getErrorCode();
        m_prompt = result.getPrompt();
        m_exitCalled = result.isExitCalled();
        m_hasError = result.hasError();
        m_echo = result.hasEcho();
    }

    /**
     * Displays text which shows the valid command line parameters, and then exits.
     */
    private void wrongUsage() {

        String usage = "Usage: java -cp $PATH_TO_OPENCMS_JAR org.opencms.rmi.CmsRemoteShellClient\n"
            + "    -script=[path to script] (optional) \n"
            + "    -registryPort=[port of RMI registry] (optional, default is "
            + CmsRemoteShellConstants.DEFAULT_PORT
            + ")\n"
            + "    -registryHost=[host of RMI registry] (optional, defaults to java.net.InetAddress.getLocalHost().getHostAddress())\n"
            + "    -additional=[additional commands class name] (optional)";
        System.out.println(usage);
        System.exit(1);
    }

}