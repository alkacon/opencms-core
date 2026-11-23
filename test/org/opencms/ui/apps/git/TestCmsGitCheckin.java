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
 */

package org.opencms.ui.apps.git;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

/** Tests argument handling between the workplace, check-in script and Git. */
public class TestCmsGitCheckin {

    private static final String MESSAGE = "Update \"price\" $5 and $HOME\nDocument the `git` label";

    @TempDir
    Path folder;

    private CmsGitCheckin checkin;
    private Path config;
    private Path exportFolder;

    /** Checks the script arguments for default and overridden check-in options. */
    @Test
    void checkinOptions() {

        List<String> defaults = checkin.buildScriptCommand();
        assertEquals("bash", defaults.get(0));
        assertEquals("module-checkin.sh", Path.of(defaults.get(1)).getFileName().toString());
        assertEquals(
            List.of("--export-folder", exportFolder.toString(), "--export-mode", "0", config.toString()),
            defaults.subList(2, defaults.size()));

        checkin.addModuleToExport("example.one");
        checkin.setCommitMessage(MESSAGE);
        String name = "Pat \"PJ\" Jones";
        String email = "pat+$git@example.org";
        checkin.setGitUserName(name);
        checkin.setGitUserEmail(email);
        checkin.setCommit(true);
        checkin.setPush(false);
        List<String> command = checkin.buildScriptCommand();
        assertEquals(
            List.of(
                "--modules",
                "example.one",
                "-msg",
                MESSAGE,
                "--git-user-name",
                name,
                "--git-user-email",
                email,
                "--no-push",
                "--export-folder",
                exportFolder.toString(),
                "--export-mode",
                "0",
                "--commit",
                config.toString()),
            command.subList(2, command.size()));

        checkin.setGitUserName("");
        checkin.setGitUserEmail(" ");
        assertEquals(
            List.of("--ignore-default-git-user-name", "--ignore-default-git-user-email"),
            checkin.buildScriptCommand().subList(6, 8));
    }

    /** Checks which mode takes precedence when checkout and reset flags overlap. */
    @Test
    void checkoutAndResetModes() {

        checkin.setCommitMessage(MESSAGE);
        checkin.setCheckout(true);
        assertMode("--checkout");
        checkin.setResetHead(true);
        assertMode("--reset-head");
        checkin.setResetRemoteHead(true);
        assertMode("--reset-remote-head");
    }

    /** Verifies that Git receives the entire commit message as one literal argument. */
    @Test
    @EnabledOnOs(OS.LINUX)
    void scriptPassesLiteralMessageToGit() throws Exception {

        Path git = folder.resolve("git");
        Files.writeString(git, "#!/bin/sh\nif [ \"$1\" = commit ]; then printf '%s\\0' \"$@\" > \"$RECORD\"; fi\n");
        assertTrue(git.toFile().setExecutable(true));
        checkin.setCommitMessage(MESSAGE);
        checkin.setCommit(true);
        checkin.setCopyAndUnzip(false);
        checkin.setPush(false);
        List<String> command = checkin.buildScriptCommand();
        Path script = Path.of(
            System.getProperty("test.webapp.path", "webapp"),
            "WEB-INF/git-scripts/module-checkin.sh");
        command.set(1, script.toAbsolutePath().toString());
        ProcessBuilder builder = new ProcessBuilder(command).directory(folder.toFile()).redirectErrorStream(true);
        builder.environment().put("PATH", folder + ":" + builder.environment().get("PATH"));
        Path record = folder.resolve("commit.args");
        builder.environment().put("RECORD", record.toString());
        Path log = folder.resolve("script.log");
        Process process = builder.redirectOutput(log.toFile()).start();
        assertEquals(0, process.waitFor(), () -> "See " + log);
        assertEquals("commit\0-v\0-m\0" + MESSAGE + "\0", Files.readString(record));
    }

    /** Creates a check-in configuration with spaces in its file and export paths. */
    @BeforeEach
    void setUp() throws Exception {

        config = folder.resolve("check in.conf");
        exportFolder = Files.createDirectory(folder.resolve("module exports"));
        Files.writeString(config, "MODULE_EXPORT_FOLDER=\"" + exportFolder + "\"\nREPOSITORY_HOME=.\nMODULE_PATH=.\n");
        checkin = new CmsGitCheckin(null);
        assertTrue(checkin.setCurrentConfiguration(new CmsGitConfiguration(config.toFile())));
    }

    /** Checks that the script receives only the expected mode flag and configuration path. */
    private void assertMode(String mode) {

        List<String> command = checkin.buildScriptCommand();
        assertEquals("bash", command.get(0));
        assertEquals("module-checkin.sh", Path.of(command.get(1)).getFileName().toString());
        assertEquals(List.of(mode, config.toString()), command.subList(2, command.size()));
    }
}
