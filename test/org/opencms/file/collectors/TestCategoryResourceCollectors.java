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

package org.opencms.file.collectors;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;

import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Unit test for the {@link CmsCategoryResourceCollector}.<p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCategoryResourceCollectors extends OpenCmsTestRunner {

    /**
     * Initializes the resources needed for the tests.<p>
     *
     * @param cms the cms object
     *
     * @throws Exception if something goes wrong
     */
    public static void initResources(CmsObject cms) throws Exception {

        cms.createResource("/folder1", CmsResourceTypeFolder.getStaticTypeId());
        Thread.sleep(100); // this is needed for testing the date sorting collector

        // jsps
        cms.createResource("/file1", CmsResourceTypeJsp.getJSPTypeId(), null, null);
        Thread.sleep(100);
        cms.createResource("/folder1/file3", CmsResourceTypeJsp.getJSPTypeId(), null, null);
        Thread.sleep(100);
        cms.createResource("/file3", CmsResourceTypeJsp.getJSPTypeId(), null, null);
        Thread.sleep(100);
        cms.createResource("/folder1/file1", CmsResourceTypeJsp.getJSPTypeId(), null, null);
        Thread.sleep(100);
        cms.createResource("/file5", CmsResourceTypeJsp.getJSPTypeId(), null, null);
        Thread.sleep(100);

        // plains
        cms.createResource("/file2", CmsResourceTypePlain.getStaticTypeId(), null, null);
        Thread.sleep(100);
        cms.createResource("/folder1/file4", CmsResourceTypePlain.getStaticTypeId(), null, null);
        Thread.sleep(100);
        cms.createResource("/folder1/file2", CmsResourceTypePlain.getStaticTypeId(), null, null);
        Thread.sleep(100);
        cms.createResource("/file4", CmsResourceTypePlain.getStaticTypeId(), null, null);
        CmsCategoryService service = CmsCategoryService.getInstance();
        CmsCategory catBusiness = service.createCategory(
            cms,
            null,
            "business",
            "business title",
            "business description",
            null);
        Thread.sleep(100);
        CmsCategory catSports = service.createCategory(cms, null, "sports", "sports title", "sports description", null);
        Thread.sleep(100);

        // business
        service.addResourceToCategory(cms, "/file1", catBusiness.getPath());
        Thread.sleep(100);
        service.addResourceToCategory(cms, "/file5", catBusiness.getPath());
        Thread.sleep(100);
        service.addResourceToCategory(cms, "/folder1/file3", catBusiness.getPath());
        Thread.sleep(100);
        service.addResourceToCategory(cms, "/file4", catBusiness.getPath());
        Thread.sleep(100);
        service.addResourceToCategory(cms, "/folder1/file4", catBusiness.getPath());
        Thread.sleep(100);

        // sports
        service.addResourceToCategory(cms, "/file3", catSports.getPath());
        Thread.sleep(100);
        service.addResourceToCategory(cms, "/folder1/file1", catSports.getPath());
        Thread.sleep(100);
        service.addResourceToCategory(cms, "/file2", catSports.getPath());
        Thread.sleep(100);
        service.addResourceToCategory(cms, "/folder1/file2", catSports.getPath());
        Thread.sleep(100);
    }

    /**
     * Prints the given list of search results to STDOUT.<p>
     *
     * @param resources the list to print
     */
    public static void printResults(List resources) {

        Iterator i = resources.iterator();
        int count = 0;
        i = resources.iterator();
        System.out.println("\n\n--------------------------");
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            count++;
            System.out.print(CmsStringUtil.padRight("" + count, 4));
            System.out.print(CmsStringUtil.padRight(res.getRootPath(), 40));
            System.out.println(
                CmsStringUtil.padRight(
                    "" + CmsDateUtil.getDateTime(new Date(res.getDateLastModified()), DateFormat.LONG, Locale.GERMAN),
                    17));
        }
    }

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo);
        try {
            initResources(getCmsObject());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Test the collection of resources for a category in a given folder filtered by a resource type and no specified resource type.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(1)
    public void testCollectAllInFolderResourceType() throws Throwable {

        CmsObject cms = getCmsObject();
        String resTypeIdPlain = CmsResourceTypePlain.getStaticTypeName();
        echo("Testing allInFolderResourceType resource collector");

        I_CmsResourceCollector collector = new CmsCategoryResourceCollector();

        List resources = collector.getResults(
            cms,
            "allKeyValuePairFiltered",
            "resource=/folder1/|resourceType=" + resTypeIdPlain + "|categoryTypes=business/");

        assertEquals(1, resources.size());

        resources = collector.getResults(cms, "allKeyValuePairFiltered", "resource=/folder1/|categoryTypes=business/");

        assertEquals(2, resources.size());
    }

    /**
     * Test the collection of resources for given categories in a given folder sorted by the category.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(2)
    public void testCollectAllInFolderSortByCategory() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing allInFolderSortByCategory resource collector");

        I_CmsResourceCollector collector = new CmsCategoryResourceCollector();

        List resources = collector.getResults(
            cms,
            "allKeyValuePairFiltered",
            "resource=/folder1/|categoryTypes=business/,sports/|sortBy=category");

        CmsResource res;

        assertEquals(4, resources.size());

        res = (CmsResource)resources.get(0);
        assertEquals("/sites/default/folder1/file3", res.getRootPath());

        res = (CmsResource)resources.get(1);
        assertEquals("/sites/default/folder1/file4", res.getRootPath());

        res = (CmsResource)resources.get(2);
        assertEquals("/sites/default/folder1/file1", res.getRootPath());

        res = (CmsResource)resources.get(3);
        assertEquals("/sites/default/folder1/file2", res.getRootPath());
    }

    /**
     * Test the collection of resources for given categories in a given folder sorted by the date.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(3)
    public void testCollectAllInFolderSortByDate() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing allInFolderSortByDate resource collector");

        I_CmsResourceCollector collector = new CmsCategoryResourceCollector();

        List resources = collector.getResults(
            cms,
            "allKeyValuePairFiltered",
            "resource=/folder1/|categoryTypes=business/,sports/|sortBy=date");

        CmsResource res;

        printResults(resources);
        assertEquals(4, resources.size());

        res = (CmsResource)resources.get(0);
        assertEquals("/sites/default/folder1/file3", res.getRootPath());

        resources = collector.getResults(
            cms,
            "allKeyValuePairFiltered",
            "resource=/folder1/|categoryTypes=business/,sports/|sortBy=date|sortAsc=false");

        printResults(resources);
        assertEquals(4, resources.size());

        res = (CmsResource)resources.get(0);
        assertEquals("/sites/default/folder1/file3", res.getRootPath());

        resources = collector.getResults(
            cms,
            "allKeyValuePairFiltered",
            "resource=/folder1/|categoryTypes=business/,sports/|sortBy=date|sortAsc=true");

        printResults(resources);
        assertEquals(4, resources.size());

        res = (CmsResource)resources.get(0);
        assertEquals("/sites/default/folder1/file2", res.getRootPath());
    }

    /**
     * Test the collection of resources for a category / given categories in a given folder with inculding the sub tree of the folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(7)
    public void testCollectAllInFolderSubTree() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing allInFolder resource collector");

        I_CmsResourceCollector collector = new CmsCategoryResourceCollector();

        List resources = collector.getResults(
            cms,
            "allKeyValuePairFiltered",
            "resource=/|categoryTypes=business/|subTree=false");
        assertEquals(3, resources.size());

        resources = collector.getResults(
            cms,
            "allKeyValuePairFiltered",
            "resource=/|categoryTypes=business/|subTree=true");
        assertEquals(5, resources.size());

        resources = collector.getResults(
            cms,
            "allKeyValuePairFiltered",
            "resource=/|categoryTypes=business/,sports/|subTree=false");
        assertEquals(5, resources.size());

        resources = collector.getResults(
            cms,
            "allKeyValuePairFiltered",
            "resource=/|categoryTypes=business/,sports/|subTree=true");
        assertEquals(9, resources.size());
    }

    /**
     * Test the collection of resources for a category filtered by a resource type and no specified resource type.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(4)
    public void testCollectAllResourcesResourceType() throws Throwable {

        CmsObject cms = getCmsObject();
        String resTypeIdPlain = CmsResourceTypePlain.getStaticTypeName();
        echo("Testing allResourcesResourceType resource collector");

        I_CmsResourceCollector collector = new CmsCategoryResourceCollector();

        List resources = collector.getResults(
            cms,
            "allKeyValuePairFiltered",
            "resourceType=" + resTypeIdPlain + "|categoryTypes=business/");

        assertEquals(2, resources.size());

        resources = collector.getResults(cms, "allKeyValuePairFiltered", "categoryTypes=business/");

        assertEquals(5, resources.size());
    }

    /**
     * Test the collection of resources for given categories sorted by category.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(5)
    public void testCollectAllResourcesSortByCategory() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing allResourcesSortByCategory resource collector");

        I_CmsResourceCollector collector = new CmsCategoryResourceCollector();

        List resources = collector.getResults(
            cms,
            "allKeyValuePairFiltered",
            "categoryTypes=business/,sports/|sortBy=category");

        CmsResource res;

        assertEquals(9, resources.size());

        res = (CmsResource)resources.get(0);
        assertEquals("/sites/default/file1", res.getRootPath());

        res = (CmsResource)resources.get(1);
        assertEquals("/sites/default/file4", res.getRootPath());

        res = (CmsResource)resources.get(2);
        assertEquals("/sites/default/file5", res.getRootPath());

        res = (CmsResource)resources.get(3);
        assertEquals("/sites/default/folder1/file3", res.getRootPath());

        res = (CmsResource)resources.get(4);
        assertEquals("/sites/default/folder1/file4", res.getRootPath());

        res = (CmsResource)resources.get(5);
        assertEquals("/sites/default/file2", res.getRootPath());

        res = (CmsResource)resources.get(6);
        assertEquals("/sites/default/file3", res.getRootPath());

        res = (CmsResource)resources.get(7);
        assertEquals("/sites/default/folder1/file1", res.getRootPath());

        res = (CmsResource)resources.get(8);
        assertEquals("/sites/default/folder1/file2", res.getRootPath());
    }

    /**
     * Test the collection of resources for given categories sorted by date.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(6)
    public void testCollectAllResourcesSortByDate() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing allInFolderSortByDate resource collector");

        I_CmsResourceCollector collector = new CmsCategoryResourceCollector();

        List resources = collector.getResults(
            cms,
            "allKeyValuePairFiltered",
            "categoryTypes=business/,sports/|sortBy=date");

        printResults(resources);
        assertEquals(9, resources.size());

        CmsResource res = (CmsResource)resources.get(0);
        assertEquals("/sites/default/file1", res.getRootPath());

        resources = collector.getResults(
            cms,
            "allKeyValuePairFiltered",
            "categoryTypes=business/,sports/|sortBy=date|sortAsc=false");

        printResults(resources);
        assertEquals(9, resources.size());

        res = (CmsResource)resources.get(0);
        assertEquals("/sites/default/file1", res.getRootPath());

        resources = collector.getResults(
            cms,
            "allKeyValuePairFiltered",
            "categoryTypes=business/,sports/|sortBy=date|sortAsc=true");

        printResults(resources);
        assertEquals(9, resources.size());

        res = (CmsResource)resources.get(0);
        assertEquals("/sites/default/folder1/file2", res.getRootPath());
    }
}
