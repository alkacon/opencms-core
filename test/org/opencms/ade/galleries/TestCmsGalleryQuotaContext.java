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

package org.opencms.ade.galleries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsUser;

import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * Checks the gallery budget context for reused content and standalone editing.<p>
 */
public class TestCmsGalleryQuotaContext {

    @Test
    public void testContainerPageTakesPrecedenceOverContentLocation() {

        CmsRequestContext context = context();
        String page = "/sites/current/subsite/index.html";
        context.setAttribute(CmsRequestContext.ATTRIBUTE_ADE_CONTEXT_PATH, page);
        assertEquals(page, CmsGalleryService.getQuotaReferencePath(context, "/shared/.content/article.xml"));
        assertEquals(page, CmsGalleryService.getQuotaReferencePath(context, "/sites/other/.content/article.xml"));
        assertEquals(page, CmsGalleryService.getQuotaReferencePath(context, "/sites/current/.content/article.xml"));
        assertEquals(page, CmsGalleryService.getQuotaReferencePath(context, null));
    }

    @Test
    public void testStandaloneEditorKeepsReferenceContext() {

        CmsRequestContext context = context();
        String resource = "/sites/other/subsite/.content/article.xml";
        assertEquals(resource, CmsGalleryService.getQuotaReferencePath(context, resource));
        assertNull(CmsGalleryService.getQuotaReferencePath(context, null));
        context.setAttribute(CmsRequestContext.ATTRIBUTE_ADE_CONTEXT_PATH, " ");
        assertEquals(resource, CmsGalleryService.getQuotaReferencePath(context, resource));
    }

    private CmsRequestContext context() {

        CmsUser user = new CmsUser(null, "Tester", "", "", "", "", 0, 0, 0, null);
        return new CmsRequestContext(
            user,
            null,
            "/system/workplace/editors/acacia/editor.jsp",
            null,
            "/sites/current",
            false,
            Locale.ENGLISH,
            "UTF-8",
            "127.0.0.1",
            0,
            null,
            null,
            "",
            false);
    }
}
