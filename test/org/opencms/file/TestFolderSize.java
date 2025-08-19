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
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file;

import org.opencms.file.quota.CmsFolderReportEntry;
import org.opencms.file.quota.CmsFolderSizeEntry;
import org.opencms.file.quota.CmsFolderSizeOptions;
import org.opencms.file.quota.CmsFolderSizeTracker;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import junit.framework.Test;

/**
 * Tests for features related to measuring folder sizes.
 */
public class TestFolderSize extends OpenCmsTestCase {

    public TestFolderSize(String name) {

        super(name);
    }

    /**
     * Returns the test suite.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        return generateSetupTestWrapper(TestFolderSize.class, "simpletest", "/");
    }

    public void testDelete() throws Exception {

        CmsFolderSizeTracker tracker = OpenCms.getFolderSizeTracker(false);
        byte[] data = new byte[100];
        CmsObject cms = getCmsObject();
        String folder = getName();
        cms.createResource(folder, 0);
        cms.createResource(folder + "/file1", 1, data, new ArrayList<>());
        cms.createResource(folder + "/file2", 1, data, new ArrayList<>());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
        cms.createResource(folder + "/file3", 1, data, new ArrayList<>());
        tracker.processUpdates();
        assertEquals(300, tracker.getTotalFolderSize("/sites/default/" + folder));
        cms.lockResourceTemporary(folder + "/file2");
        cms.deleteResource(folder + "/file2", CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.lockResourceTemporary(folder + "/file3");
        cms.deleteResource(folder + "/file3", CmsResource.DELETE_PRESERVE_SIBLINGS);
        tracker.processUpdates();
        assertEquals(200, tracker.getTotalFolderSize("/sites/default/" + folder));
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        tracker.processUpdates();
        assertEquals(100, tracker.getTotalFolderSize("/sites/default/" + folder));
    }

    public void testExtendedFolderName() throws Exception {

        byte[] data = new byte[100];
        CmsObject cms = getCmsObject();
        String folder = getName();
        cms.createResource(folder, 0);
        cms.createResource(folder + "_extended", 0);
        cms.createResource(folder + "_extended/file1", 1, data, new ArrayList<>());
        List<CmsFolderSizeEntry> entries = cms.readFolderSizeStats(
            new CmsFolderSizeOptions(
                CmsStringUtil.joinPaths(cms.getRequestContext().getSiteRoot(), getName()),
                false,
                true));
        assertEquals(0, entries.stream().collect(Collectors.summingLong(entry -> entry.getSize())).longValue());

    }

    public void testFolderSize() throws Exception {

        byte[] data = new byte[100];
        CmsObject cms = getCmsObject();
        String folder = getName();
        cms.createResource(folder, 0);
        cms.createResource(folder + "/file1", 1, data, new ArrayList<>());
        cms.createResource(folder + "/file2", 1, data, new ArrayList<>());
        cms.createResource(folder + "/subfolder", 0);
        cms.createResource(folder + "/subfolder/file1", 1, data, new ArrayList<>());
        List<CmsFolderSizeEntry> entries = cms.readFolderSizeStats(
            new CmsFolderSizeOptions(
                CmsStringUtil.joinPaths(cms.getRequestContext().getSiteRoot(), getName()),
                false,
                true));
        assertEquals(2, entries.size());
        assertEquals(
            2 * data.length,
            entries.stream().filter(entry -> entry.getRootPath().endsWith(folder + "/")).findFirst().get().getSize());
        assertEquals(
            data.length,
            entries.stream().filter(entry -> entry.getRootPath().endsWith("subfolder/")).findFirst().get().getSize());

    }

    public void testSingleSize() throws Exception {

        byte[] data = new byte[100];
        CmsObject cms = getCmsObject();
        String folder = getName();
        cms.createResource(folder, 0);
        cms.createResource(folder + "/file1", 1, data, new ArrayList<>());
        cms.createResource(folder + "/file2", 1, data, new ArrayList<>());
        cms.createResource(folder + "/subfolder", 0);
        cms.createResource(folder + "/subfolder/file1", 1, data, new ArrayList<>());
        List<CmsFolderSizeEntry> entries = cms.readFolderSizeStats(
            new CmsFolderSizeOptions(
                CmsStringUtil.joinPaths(cms.getRequestContext().getSiteRoot(), getName()),
                false,
                false));
        assertEquals(1, entries.size());
        assertEquals(2 * data.length, entries.get(0).getSize());
        entries = cms.readFolderSizeStats(
            new CmsFolderSizeOptions(
                CmsStringUtil.joinPaths(cms.getRequestContext().getSiteRoot(), getName(), "subfolder"),
                false,
                false));
        assertEquals(1, entries.size());
        assertEquals(data.length, entries.get(0).getSize());

        assertEquals(
            data.length,
            entries.stream().filter(entry -> entry.getRootPath().endsWith("subfolder/")).findFirst().get().getSize());

    }

    public void testTracker() throws Exception {

        byte[] data = new byte[100];
        CmsObject cms = getCmsObject();
        String folder = getName();
        cms.createResource(folder, 0);
        cms.createResource(folder + "/file1", 1, data, new ArrayList<>());
        cms.createResource(folder + ".ext", 0);
        cms.createResource(folder + ".ext/file1", 1, data, new ArrayList<>());

        cms.createResource(folder + "/alpha", 0);
        cms.createResource(folder + "/alpha/file", 1, data, new ArrayList<>());
        cms.createResource(folder + "/beta", 0);
        cms.createResource(folder + "/beta/file", 1, data, new ArrayList<>());
        cms.createResource(folder + "/gamma", 0);
        cms.createResource(folder + "/gamma/file", 1, data, new ArrayList<>());
        String site = "/sites/default/";
        List<String> folders = Arrays.asList(
            site + folder + "/",
            site + folder + "/alpha/",
            site + folder + "/beta/",
            site + folder + ".ext/");
        CmsFolderSizeTracker tracker = OpenCms.getFolderSizeTracker(false);
        tracker.reload();
        assertEquals(400, tracker.getTotalFolderSize(folders.get(0)));
        assertEquals(100, tracker.getTotalFolderSize(folders.get(1)));
        assertEquals(100, tracker.getTotalFolderSize(folders.get(2)));
        assertEquals(100, tracker.getTotalFolderSize(folders.get(3)));
        assertEquals(200, tracker.getTotalFolderSizeExclusive(folders.get(0), folders));
        assertEquals(100, tracker.getTotalFolderSizeExclusive(folders.get(1), folders));
        assertEquals(100, tracker.getTotalFolderSizeExclusive(folders.get(2), folders));
        assertEquals(100, tracker.getTotalFolderSizeExclusive(folders.get(3), folders));
    }

    public void testTrackerInterval() throws Exception {

        CmsFolderSizeTracker tracker = OpenCms.getFolderSizeTracker(false);
        assertEquals(1000, tracker.getTimerInterval());
    }

    public void testTrackerOnline() throws Exception {

        byte[] data = new byte[100];
        CmsObject cms = getCmsObject();
        String folder = getName();
        cms.createResource(folder, 0);
        cms.createResource(folder + "/file1", 1, data, new ArrayList<>());
        cms.createResource(folder + ".ext", 0);
        cms.createResource(folder + ".ext/file1", 1, data, new ArrayList<>());

        cms.createResource(folder + "/alpha", 0);
        cms.createResource(folder + "/alpha/file", 1, data, new ArrayList<>());
        cms.createResource(folder + "/beta", 0);
        cms.createResource(folder + "/beta/file", 1, data, new ArrayList<>());
        cms.createResource(folder + "/gamma", 0);
        cms.createResource(folder + "/gamma/file", 1, data, new ArrayList<>());
        String site = "/sites/default/";
        List<String> folders = Arrays.asList(
            site + folder + "/",
            site + folder + "/alpha/",
            site + folder + "/beta/",
            site + folder + ".ext/");
        CmsFolderSizeTracker tracker = OpenCms.getFolderSizeTracker(true);
        tracker.processUpdates();
        assertEquals(0, tracker.getTotalFolderSize(folders.get(0)));
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
        tracker.processUpdates();
        assertEquals(400, tracker.getTotalFolderSize(folders.get(0)));

    }

    public void testTrackerReportMethod() throws Exception {

        byte[] data = new byte[100];
        CmsObject cms = getCmsObject();
        String folder = getName();
        cms.createResource(folder, 0);
        cms.createResource(folder + "/file1", 1, data, new ArrayList<>());
        cms.createResource(folder + ".ext", 0);
        cms.createResource(folder + ".ext/file1", 1, data, new ArrayList<>());

        cms.createResource(folder + "/alpha", 0);
        cms.createResource(folder + "/alpha/file", 1, data, new ArrayList<>());
        cms.createResource(folder + "/beta", 0);
        cms.createResource(folder + "/beta/file", 1, data, new ArrayList<>());
        cms.createResource(folder + "/gamma", 0);
        cms.createResource(folder + "/gamma/file", 1, data, new ArrayList<>());
        String site = "/sites/default/";
        List<String> folders = Arrays.asList(
            site + folder + "/",
            site + folder + "/alpha/",
            site + folder + "/beta/",
            site + folder + ".ext/");
        CmsFolderSizeTracker tracker = OpenCms.getFolderSizeTracker(false);
        tracker.reload();
        Map<String, CmsFolderReportEntry> report = tracker.getFolderReport(folders);
        CmsFolderReportEntry root = report.get(folders.get(0));
        CmsFolderReportEntry alpha = report.get(folders.get(1));
        CmsFolderReportEntry beta = report.get(folders.get(2));
        CmsFolderReportEntry ext = report.get(folders.get(3));
        assertEquals(400, root.getTreeSize());
        assertEquals(200, root.getTreeSizeExclusive());
        assertEquals(100, alpha.getTreeSize());
        assertEquals(100, alpha.getTreeSizeExclusive());
        assertEquals(100, beta.getTreeSize());
        assertEquals(100, beta.getTreeSizeExclusive());
        assertEquals(100, ext.getTreeSize());
        assertEquals(100, ext.getTreeSizeExclusive());

    }

}
