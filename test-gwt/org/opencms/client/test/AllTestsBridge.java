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
 */

package org.opencms.client.test;

import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

/**
 * JUnit 4 bridge so Gradle 9 discovers the legacy GWT JUnit 3 suite.
 */
@RunWith(AllTests.class)
public class AllTestsBridge {

    /**
     * Returns the GWT test suite.<p>
     *
     * @return the GWT test suite
     */
    public static junit.framework.Test suite() {

        return org.opencms.client.test.AllTests.suite();
    }
}
