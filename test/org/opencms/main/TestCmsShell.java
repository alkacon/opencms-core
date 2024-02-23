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

import org.apache.logging.log4j.core.appender.OpenCmsTestLogAppender;
import org.opencms.file.CmsObject;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.util.CmsFileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test cases for the OpenCms shell.<p>
 *
 * @since 6.0.0
 */
public class TestCmsShell extends OpenCmsTestCase {

    public static final String PROMPT = "${user}@${project}>";

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsShell(String arg0) {

        super(arg0);
    }

    /**
     * Tests the Junit OpenCms VFS test setup using the "base" test class.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCmsSetup() throws Throwable {

        CmsObject cms;

        // setup OpenCms using the base test class
        cms = setupOpenCms("simpletest", "/");
        // check the returned CmsObject
        assertEquals(cms.getRequestContext().getCurrentUser(), cms.readUser("Admin"));
        assertEquals(cms.getRequestContext().getCurrentProject(), cms.readProject("Offline"));
        assertEquals(cms.getRequestContext().getSiteRoot(), "/sites/default");

        // check the CmsObject initialization
        cms = getCmsObject();
        // check the returned CmsObject
        assertEquals(cms.getRequestContext().getCurrentUser(), cms.readUser("Admin"));
        assertEquals(cms.getRequestContext().getCurrentProject(), cms.readProject("Offline"));
        assertEquals(cms.getRequestContext().getSiteRoot(), "/sites/default");

        // remove OpenCms
        removeOpenCms();
    }

    /**
     * Tests the CmsShell and setup procedure.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCmsShell() throws Throwable {

        // create a new database first
        setupDatabase();
        // turn off exceptions on error logging during setup (won't work otherwise due to non-existing "offline" project)
        OpenCmsTestLogAppender.setBreakOnError(false);

        // create a shell instance
        CmsShell shell = new CmsShell(
            getTestDataPath("WEB-INF" + File.separator),
            null,
            null,
            PROMPT,
            null,
            System.out,
            System.err,
            false);

        // open the test script
        File script;
        FileInputStream stream;

        // start the shell with the base script
        script = new File(getTestDataPath("scripts/script_base.txt"));
        stream = new FileInputStream(script);
        shell.execute(stream);
        stream.close();

        // add the default folders by script
        script = new File(getTestDataPath("scripts/script_default_folders.txt"));
        stream = new FileInputStream(script);
        shell.execute(stream);
        stream.close();

        // log in the Admin user and switch to the setup project
        CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("_setupProject"));

        // import the "simpletest" files
        importResources(cms, "simpletest", "/");

        // publish the current project by script
        script = new File(getTestDataPath("scripts/script_publish.txt"));
        stream = new FileInputStream(script);
        shell.execute(stream);
        stream.close();

        // get the name of the folder for the backup configuration files
        File configBackupDir = new File(getTestDataPath("WEB-INF/" + CmsSystemInfo.FOLDER_CONFIG_DEFAULT + "backup/"));

        // exit the shell
        shell.exit();

        // remove the database
        removeDatabase();

        // remove the backup configuration files
        CmsFileUtil.purgeDirectory(configBackupDir);
    }

    private static I_CmsShellCommands createCmsShellCommands(String identifier, final String markInit, final String markShellExit, final String markShellStart, String markInErrOutputStream) {
        return new I_CmsShellCommands() {
            private CmsShell shell;

            @Override
            public void initShellCmsObject(CmsObject cms, CmsShell shell) {
                this.shell = shell;
                shell.getOut().println(markInit + identifier);
            }

            @Override
            public void shellExit() {
                // shellExit can (and in fact is) invoked before initShellCmsObject
                if (shell != null) {
                    shell.getOut().println(markShellExit + identifier);
                }
            }

            @Override
            public void shellStart() {
                shell.getOut().println(markShellStart + identifier);
                shell.getErr().println(markInErrOutputStream + identifier);
            }
        };
    }

    /**
     * Tests the CmsShell and setup procedure.<p>
     *
     */
    public void testShouldLoadCmsShellWithAllAdditionalCommandsClasses() {

        // create a new database first
        setupDatabase();
        // turn off exceptions on error logging during setup (won't work otherwise due to non-existing "offline" project)
        OpenCmsTestLogAppender.setBreakOnError(false);

        final ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(baosOut);
        final ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
        final PrintStream err = new PrintStream(baosErr);


        final String MARK_INIT = "initShellCmsObject on ";
        final String MARK_SHELL_EXIT = "shellExit on ";
        final String MARK_SHELL_START = "shellStart on ";
        final String MARK_IN_ERR_OUTPUT_STREAM = "mark in error OutputStream on ";
        List<String> commandsIds = Arrays.asList("0001", "0002", "0003");
        List<I_CmsShellCommands> cmsShellCommands = commandsIds.stream()
                .map(id -> createCmsShellCommands(id, MARK_INIT, MARK_SHELL_EXIT, MARK_SHELL_START, MARK_IN_ERR_OUTPUT_STREAM))
                .collect(Collectors.toList());

        // create a shell instance
        final CmsShell shell = new CmsShell(
                getTestDataPath("WEB-INF" + File.separator),
            null,
            null,
                PROMPT,
                cmsShellCommands,
            out,
            err,
            false);

        // get the name of the folder for the backup configuration files
        File configBackupDir = new File(getTestDataPath("WEB-INF/" + CmsSystemInfo.FOLDER_CONFIG_DEFAULT + "backup/"));

        // exit the shell
        shell.exit();

        // remove the database
        removeDatabase();

        // remove the backup configuration files
        CmsFileUtil.purgeDirectory(configBackupDir);

        final String resultOut = baosOut.toString();
        final String resultErr = baosErr.toString();
        System.out.println("out: ----------------------------");
        System.out.println(resultOut);
        System.out.println("---------------------------------");
        System.out.println("err: ----------------------------");
        System.out.println(resultErr);
        System.out.println("---------------------------------");

        assertContains(resultOut, "OpenCms WEB-INF path:");
        assertContains(resultOut, "OpenCms property file:");
        for (String id: commandsIds) {
            assertContains(resultOut, MARK_INIT + id);
            assertContains(resultOut, MARK_SHELL_EXIT + id);
            assertContains(resultOut, MARK_SHELL_START + id);
            assertContains(resultErr, MARK_IN_ERR_OUTPUT_STREAM + id);
        }
    }
}
