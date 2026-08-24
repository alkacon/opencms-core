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

package org.opencms.ade.detailpage;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.test.mock.CmsMockHttpServletRequest;
import org.opencms.test.mock.CmsMockHttpServletResponse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Tests the request free detail page resolution of {@link CmsDetailPageUtil#resolveDetail(CmsObject, String)}
 * and the resource init handler built on it.<p>
 *
 * The 'ade-config' test setup configures the type <code>article1</code> of the site
 * <code>/sites/foo</code> to be rendered by the detail page <code>/sites/foo/main/blog/</code>, while
 * the site <code>/sites/bar</code> configures the same type without a detail page.<p>
 */
public class TestDetailPageResolution extends OpenCmsTestRunner {

    /** A detail content of the site which configures no detail page. */
    private static final String CONTENT_BAR = "/sites/bar/.content/blogentries/be_00001.xml";

    /** A detail content of the site which configures a detail page. */
    private static final String CONTENT_FOO = "/sites/foo/.content/blogentries/be_00001.xml";

    /** The detail page configured for the type article1 in the site /sites/bar. */
    private static final String PAGE_BAR = "/main/blog/";

    /** The detail page configured for the type article1 in the site /sites/foo. */
    private static final String PAGE_FOO = "/sites/foo/main/blog/index.html";

    /** The site which configures no detail page. */
    private static final String SITE_BAR = "/sites/bar";

    /** The site which configures a detail page. */
    private static final String SITE_FOO = "/sites/foo";

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "ade-config", "/");
    }

    /**
     * Tests that the resource init handler reports the detail content through the request and the
     * request context, and points the request context at the detail page.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testInitResourceReportsDetailContent() throws Exception {

        CmsObject cms = siteCms(SITE_FOO);
        CmsResource content = rootCms().readResource(CONTENT_FOO);
        cms.getRequestContext().setUri(PAGE_BAR + content.getStructureId());
        CmsMockHttpServletRequest req = new CmsMockHttpServletRequest();
        CmsMockHttpServletResponse res = new CmsMockHttpServletResponse();

        CmsResource page = new CmsDetailPageResourceHandler().initResource(null, cms, req, res);

        assertNotNull(page, "the detail page should be found");
        assertEquals(PAGE_FOO, page.getRootPath());
        assertNotNull(
            CmsDetailPageResourceHandler.getDetailResource(req),
            "the detail content should be in the request");
        assertEquals(CONTENT_FOO, CmsDetailPageResourceHandler.getDetailResource(req).getRootPath());
        assertEquals(content.getStructureId(), CmsDetailPageResourceHandler.getDetailId(req));
        assertNotNull(cms.getRequestContext().getDetailResource(), "the detail content should be in the context");
        assertEquals(CONTENT_FOO, cms.getRequestContext().getDetailResource().getRootPath());
        assertEquals("/main/blog/index.html", cms.getRequestContext().getUri());
    }

    /**
     * Tests that the resource init handler still resolves the detail page, but reports no detail
     * content, when it runs without a response, which is the pass that only determines the locale.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testInitResourceWithoutResponse() throws Exception {

        CmsObject cms = siteCms(SITE_FOO);
        CmsResource content = rootCms().readResource(CONTENT_FOO);
        cms.getRequestContext().setUri(PAGE_BAR + content.getStructureId());
        CmsMockHttpServletRequest req = new CmsMockHttpServletRequest();

        CmsResource page = new CmsDetailPageResourceHandler().initResource(null, cms, req, null);

        assertNotNull(page, "the detail page should be found");
        assertEquals(PAGE_FOO, page.getRootPath());
        assertNull(CmsDetailPageResourceHandler.getDetailResource(req), "no detail content without a response");
        assertNull(cms.getRequestContext().getDetailResource(), "no detail content without a response");
        assertEquals("/main/blog/index.html", cms.getRequestContext().getUri());
    }

    /**
     * Tests that a URI whose page is not configured as a detail page for the content does not
     * resolve, so a destructive operation can never be aimed at the page by mistake.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testPageIsNoDetailPage() throws Exception {

        CmsObject cms = siteCms(SITE_BAR);
        CmsResource content = rootCms().readResource(CONTENT_BAR);

        assertNull(
            CmsDetailPageUtil.resolveDetail(cms, PAGE_BAR + content.getStructureId()),
            "the site bar configures no detail page");
    }

    /**
     * Tests that resolving reports the detail page and the detail content when the URI addresses the
     * content by its structure id.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testResolveByStructureId() throws Exception {

        CmsObject cms = siteCms(SITE_FOO);
        CmsResource content = rootCms().readResource(CONTENT_FOO);

        CmsDetailResolution resolution = CmsDetailPageUtil.resolveDetail(cms, PAGE_BAR + content.getStructureId());

        assertNotNull(resolution, "the URI should resolve to a detail page");
        assertEquals(PAGE_FOO, resolution.getDetailPage().getRootPath());
        assertEquals(CONTENT_FOO, resolution.getDetailContent().getRootPath());
        assertEquals(content.getStructureId().toString(), resolution.getDetailName());
        assertFalse(resolution.isFunctionDetail(), "this is a detail content request");
        assertNull(resolution.getFunctionPage(), "a detail content request has no function page");
    }

    /**
     * Tests that resolving reports the detail page and the detail content when the URI addresses the
     * content by a mapped URL name, with and without a trailing slash.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testResolveByUrlName() throws Exception {

        CmsObject cms = siteCms(SITE_FOO);
        CmsResource content = rootCms().readResource(CONTENT_FOO);
        String urlName = cms.writeUrlNameMapping("resolve-by-url-name", content.getStructureId(), "en", false);

        CmsDetailResolution resolution = CmsDetailPageUtil.resolveDetail(cms, PAGE_BAR + urlName);

        assertNotNull(resolution, "the URI should resolve to a detail page");
        assertEquals(PAGE_FOO, resolution.getDetailPage().getRootPath());
        assertEquals(CONTENT_FOO, resolution.getDetailContent().getRootPath());
        assertEquals(urlName, resolution.getDetailName());

        CmsDetailResolution withSlash = CmsDetailPageUtil.resolveDetail(cms, PAGE_BAR + urlName + "/");

        assertNotNull(withSlash, "a trailing slash should resolve the same way");
        assertEquals(PAGE_FOO, withSlash.getDetailPage().getRootPath());
        assertEquals(CONTENT_FOO, withSlash.getDetailContent().getRootPath());
    }

    /**
     * Tests that resolving changes neither the request context URI nor the detail resource, so it can
     * be used as a query outside the request cycle.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testResolveHasNoSideEffects() throws Exception {

        CmsObject cms = siteCms(SITE_FOO);
        CmsResource content = rootCms().readResource(CONTENT_FOO);
        String uri = PAGE_BAR + content.getStructureId();
        cms.getRequestContext().setUri(uri);

        assertNotNull(CmsDetailPageUtil.resolveDetail(cms, uri), "the URI should resolve to a detail page");

        assertEquals(uri, cms.getRequestContext().getUri(), "resolving must not change the URI");
        assertNull(cms.getRequestContext().getDetailResource(), "resolving must not set the detail resource");
    }

    /**
     * Tests that a URI whose last segment is neither a URL name nor a structure id does not resolve.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testUnknownDetailName() throws Exception {

        CmsObject cms = siteCms(SITE_FOO);

        assertNull(
            CmsDetailPageUtil.resolveDetail(cms, PAGE_BAR + "this-name-is-mapped-to-nothing"),
            "an unmapped name is not a detail request");
    }

    /**
     * Returns a CMS context for the root site, used to read resources by their root path.<p>
     *
     * @return the CMS context
     *
     * @throws CmsException if creating the context fails
     */
    private CmsObject rootCms() throws CmsException {

        CmsObject cms = OpenCms.initCmsObject(getCmsObject());
        cms.getRequestContext().setSiteRoot("");
        return cms;
    }

    /**
     * Returns a CMS context for one site, so a URI can be resolved the way a request to that site
     * would be.<p>
     *
     * @param siteRoot the site root
     *
     * @return the CMS context
     *
     * @throws CmsException if creating the context fails
     */
    private CmsObject siteCms(String siteRoot) throws CmsException {

        CmsObject cms = OpenCms.initCmsObject(getCmsObject());
        cms.getRequestContext().setSiteRoot(siteRoot);
        return cms;
    }
}
