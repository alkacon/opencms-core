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

package org.opencms.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

/** Tests for lazy message initialization and delegation. */
public class TestCmsLazyMessages {

    /** Concurrent callers share a single successful initialization. */
    @Test
    public void testConcurrentInitialization() throws Exception {

        AtomicInteger calls = new AtomicInteger();
        CmsLazyMessages lazy = new CmsLazyMessages(Locale.ENGLISH, () -> {
            calls.incrementAndGet();
            return createMessages();
        });
        var executor = Executors.newFixedThreadPool(8);
        try {
            var tasks = IntStream.range(0, 32).mapToObj(i -> (Callable<String>)() -> lazy.key("hello")).collect(
                Collectors.toList());
            for (var result : executor.invokeAll(tasks)) {
                assertEquals("Hello", result.get());
            }
            assertEquals(1, calls.get());
        } finally {
            executor.shutdownNow();
        }
    }

    /** Message operations retain the delegate's lookup and formatting behavior. */
    @Test
    public void testDelegation() throws Exception {

        AtomicInteger calls = new AtomicInteger();
        CmsMessages delegate = createMessages();
        CmsLazyMessages lazy = new CmsLazyMessages(Locale.ENGLISH, () -> {
            calls.incrementAndGet();
            return delegate;
        });
        assertEquals("Hello", lazy.getString("hello"));
        assertEquals("Hello", lazy.key("hello"));
        assertNull(lazy.key("missing", true));
        assertEquals("fallback", lazy.keyDefault("missing", "fallback"));
        assertEquals("Hello World", lazy.key("greeting", "World"));
        assertEquals("Hello World", lazy.keyWithParams("greeting|World"));
        assertEquals("missing!", lazy.key("missing", new Object[0], key -> key + "!"));
        assertSame(delegate.getResourceBundle(), lazy.getResourceBundle());
        assertTrue(lazy.isInitialized());
        assertEquals(delegate.isUncacheable(), lazy.isUncacheable());
        assertEquals(delegate.getDate(0L), lazy.getDate(0L));
        assertEquals(delegate.getDateTime(0L), lazy.getDateTime(0L));
        assertEquals(1, calls.get());
    }

    /** A failed factory call does not poison subsequent initialization. */
    @Test
    public void testFailedInitialization() {

        AtomicInteger calls = new AtomicInteger();
        CmsLazyMessages lazy = new CmsLazyMessages(Locale.ENGLISH, () -> {
            if (calls.incrementAndGet() == 1) {
                throw new IllegalStateException("Temporary failure");
            }
            return createMessages();
        });
        assertThrows(IllegalStateException.class, () -> lazy.key("hello"));
        assertEquals("Hello", lazy.key("hello"));
        assertEquals("Hello", lazy.key("hello"));
        assertEquals(2, calls.get());
    }

    /** Metadata and collection membership must not initialize the delegate. */
    @Test
    public void testLazyMetadataAndIdentity() {

        AtomicInteger calls = new AtomicInteger();
        CmsMessages delegate = createMessages();
        CmsLazyMessages first = new CmsLazyMessages(Locale.ENGLISH, () -> {
            calls.incrementAndGet();
            return delegate;
        });
        CmsLazyMessages second = new CmsLazyMessages(Locale.ENGLISH, () -> {
            calls.incrementAndGet();
            return delegate;
        });
        assertEquals(Locale.ENGLISH, first.getLocale());
        assertEquals(first, first);
        assertNotEquals(first, second);
        assertNotEquals(first, delegate);
        assertNotEquals(delegate, first);
        assertEquals(System.identityHashCode(first), first.hashCode());
        assertNotNull(first.toString());

        CmsMultiMessages multi = new CmsMultiMessages(Locale.ENGLISH);
        multi.addMessages(first);
        multi.addMessages(second);
        multi.addMessages(first);
        assertEquals(2, multi.getMessages().size());
        assertEquals(0, calls.get());
        assertEquals("Hello", multi.key("hello"));
        assertEquals(1, calls.get());
        assertNotEquals(first, delegate);
        assertNotEquals(delegate, first);
    }

    /**
     * Creates an in-memory delegate without loading OpenCms configuration.
     *
     * @return the delegate
     */
    private CmsMessages createMessages() {

        CmsMessages result = new CmsMessages();
        result.setLocale(Locale.ENGLISH);
        result.setBundleName("test");
        result.setResourceBundle(new ListResourceBundle() {

            @Override
            protected Object[][] getContents() {

                return new Object[][] {{"hello", "Hello"}, {"greeting", "Hello {0}"}};
            }
        });
        return result;
    }
}
