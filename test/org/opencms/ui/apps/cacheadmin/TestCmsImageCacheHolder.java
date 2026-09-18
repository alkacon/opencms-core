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

package org.opencms.ui.apps.cacheadmin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Tests cache app image listing helpers. */
public class TestCmsImageCacheHolder {

    /** Tests extracting a safe storage prefix from cache app searches. */
    @Test
    public void testGetCacheKeyPrefix() {

        assertEquals("sites/default/", CmsImageCacheHolder.getCacheKeyPrefix("/sites/default/*"));
        assertEquals("sites/default/", CmsImageCacheHolder.getCacheKeyPrefix("/sites/default/image.jpg"));
        assertEquals("sites/default/", CmsImageCacheHolder.getCacheKeyPrefix("/sites/default/folder*/*"));
        assertEquals("sites/default/folder/", CmsImageCacheHolder.getCacheKeyPrefix("/sites/default/folder/"));
        assertEquals("", CmsImageCacheHolder.getCacheKeyPrefix("*image.jpg"));
        assertEquals("", CmsImageCacheHolder.getCacheKeyPrefix("image.jpg"));
        assertEquals("", CmsImageCacheHolder.getCacheKeyPrefix("/*"));
    }

    /** Tests decoding current external image cache keys without a VFS lookup. */
    @Test
    public void testGetVfsNameFromExternalCacheKey() {

        String hash = "0123456789abcdef0123456789abcdef";
        assertEquals(
            "/sites/default/folder/my_image.jpg",
            CmsImageCacheHolder.getVfsNameFromExternalCacheKey(
                "sites/default/folder/my_image_-123456789_" + hash + ".jpg"));
        assertEquals(
            "/sites/default/image.png",
            CmsImageCacheHolder.getVfsNameFromExternalCacheKey("/sites/default/image_42_" + hash + ".png"));
        assertEquals("", CmsImageCacheHolder.getVfsNameFromExternalCacheKey("sites/default/image_42.png"));
        assertEquals(
            "",
            CmsImageCacheHolder.getVfsNameFromExternalCacheKey(
                "sites/default/image_42_0123456789ABCDEF0123456789ABCDEF.png"));
    }
}
