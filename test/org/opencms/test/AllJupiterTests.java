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

package org.opencms.test;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Top-level Jupiter aggregator for migrated package suites.<p>
 *
 * Coexists with the legacy {@link AllTests} (which still drives unmigrated
 * packages via {@link AllTestsBridge}) until every package has been migrated.
 * At that point this class will be renamed to {@code AllTests} and the
 * legacy parent plus {@link AllTestsBridge} will be deleted.<p>
 *
 * Cross-package order MUST mirror the legacy {@link AllTests} order; insert
 * new entries at the same relative position they had in the legacy parent.
 */
@Suite
@SelectClasses({
    org.opencms.ade.configuration.AllTests.class,
    org.opencms.ade.containerpage.inherited.AllTests.class,
    org.opencms.ade.contenteditor.AllTests.class,
    org.opencms.ade.sitemap.AllTests.class,
    org.opencms.configuration.AllTests.class,
    org.opencms.db.AllTests.class,
    org.opencms.file.AllTests.class,
    org.opencms.file.collectors.AllTests.class,
    org.opencms.file.types.AllTests.class,
    org.opencms.file.wrapper.AllTests.class,
    org.opencms.jsp.util.AllTests.class,
    org.opencms.main.AllTests.class,
    org.opencms.module.AllTests.class,
    org.opencms.scheduler.AllTests.class,
    org.opencms.site.AllTests.class,
    org.opencms.util.AllTests.class,
    org.opencms.xml.AllTests.class,
    org.opencms.xml.containerpage.AllTests.class,
    org.opencms.xml.content.AllTests.class,
    org.opencms.xml.page.AllTests.class,
    org.opencms.widgets.AllTests.class,
    org.opencms.workplace.AllTests.class,
    org.opencms.ugc.AllTests.class,
    org.opencms.security.AllTests.class,
    org.opencms.jsp.decorator.AllTests.class
})
public final class AllJupiterTests {}
