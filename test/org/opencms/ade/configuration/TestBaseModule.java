/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.configuration;

import org.opencms.ade.contenteditor.CmsContentTypeVisitor;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceBuilder;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.logging.log4j.core.appender.OpenCmsTestLogAppender;

import junit.framework.Test;

/**
 * Tests involving the resource types from the org.opencms.base module.
 *
 */
public class TestBaseModule extends OpenCmsTestCase {

    /**
     * Test constructor.<p>
     *
     * @param name the name of the test
     */
    public TestBaseModule(String name) {

        super(name);
    }

    /**
     * Returns the test suite.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        CmsConfigurationCache.DEBUG = true;
        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestBaseModule.class, "simpletest", "/");
    }

    /**
     * Tests that most labels in the base module's content types are defined.
     *
     * @throws Exception if something goes wrong
     */
    public void testEditorLabels() throws Exception {

        try {
            OpenCmsTestLogAppender.setBreakOnError(false);

            File moduleZip = createBaseModuleZip();
            CmsObject cms = getCmsObject();
            OpenCms.getModuleManager().replaceModule(
                cms,
                moduleZip.getAbsolutePath(),
                new CmsShellReport(cms.getRequestContext().getLocale()));
            System.out.flush();
            OpenCms.getADEManager().waitForCacheUpdate(false);

            // these types are edited using special editors, not through the XML content editor,
            // so we do not really need labels
            Set<String> schemaBlacklist = new HashSet<>(
                Arrays.asList(
                    "/system/modules/org.opencms.ade.containerpage/schemas/container_page.xsd",
                    "/system/modules/org.opencms.ade.containerpage/schemas/group_container.xsd",
                    "/system/modules/org.opencms.ade.containerpage/schemas/inheritance_config.xsd",
                    "/system/modules/org.opencms.ade.config/schemas/bundledescriptor.xsd"));
            CmsModule base = OpenCms.getModuleManager().getModule("org.opencms.base");

            CmsXmlEntityResolver entityResolver = new CmsXmlEntityResolver(cms);
            for (I_CmsResourceType type : base.getResourceTypes()) {
                String schema = type.getConfiguration().getString("schema", null);
                if (schema != null) {
                    if (!schemaBlacklist.contains(schema)) {
                        CmsXmlContentDefinition contentDef = CmsXmlContentDefinition.unmarshal(
                            "opencms:/" + schema,
                            entityResolver);
                        checkContentDefinition(null, schema, contentDef);
                    }
                }
            }
        } finally {
            OpenCmsTestLogAppender.setBreakOnError(true);

        }
    }

    /**
     * Waits until the configuration update task has been run.<p>
     *
     * @param online true if we should wait for the Online task, false for the Offline task
     */
    public void waitForUpdate(boolean online) {

        OpenCms.getADEManager().getCache(online).getWaitHandleForUpdateTask().enter(0);
    }

    /**
     * Helper method for checking that the labels are defined.
     *
     * @param visitor the visitor to use
     * @param schema the original resource type's schema
     * @param contentDef the current content definition
     *
     * @throws Exception if something goes wrong
     */
    private void checkContentDefinition(
        CmsContentTypeVisitor visitor,
        String schema,
        CmsXmlContentDefinition contentDef)
    throws Exception {

        final Locale contentLocale = Locale.ENGLISH;
        if (visitor == null) {
            CmsResourceBuilder builder = new CmsResourceBuilder();
            builder.setStructureId(new CmsUUID());
            builder.setResourceId(new CmsUUID());
            builder.setType(OpenCms.getResourceManager().getResourceType("xmlcontent"));
            builder.setRootPath("/dummy-file.xml");
            CmsFile dummyFile = new CmsFile(builder.buildResource());
            visitor = new CmsContentTypeVisitor(getCmsObject(), dummyFile, contentLocale);
            visitor.visitTypes(contentDef, Locale.ENGLISH);
        }
        for (I_CmsXmlSchemaType typeEntry : contentDef.getTypeSequence()) {
            String label = visitor.getLabel(typeEntry, null);
            if (label == null) {
                fail(schema + ": missing label for " + contentDef.getInnerName() + "." + typeEntry.getName());
            }
            if (typeEntry instanceof CmsXmlNestedContentDefinition) {
                checkContentDefinition(
                    visitor,
                    schema,
                    ((CmsXmlNestedContentDefinition)typeEntry).getNestedContentDefinition());
            }
        }
    }

}
