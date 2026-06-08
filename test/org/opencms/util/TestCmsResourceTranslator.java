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

package org.opencms.util;

import org.opencms.test.OpenCmsTestRunner;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test cases for the resource translator.<p>
 *
 * @since 6.0.0
 */
public class TestCmsResourceTranslator extends OpenCmsTestRunner {

    // default rules (same as in "opencms.properties")
    private static String[] rules = {
        "s#/default/vfs/content/bodys/(.*)#/default/vfs/system/bodies/$1#",
        "s#/default/vfs/pics/system/(.*)#/default/vfs/system/workplace/resources/$1#",
        "s#/default/vfs/pics/(.*)#/default/vfs/system/galleries/pics/$1#",
        "s#/default/vfs/download/(.*)#/default/vfs/system/galleries/download/$1#",
        "s#/default/vfs/externallinks/(.*)#/default/vfs/system/galleries/externallinks/$1#",
        "s#/default/vfs/htmlgalleries/(.*)#/default/vfs/system/galleries/htmlgalleries/$1#",
        "s#/default/vfs/content/(.*)#/default/vfs/system/modules/default/$1#",
        "s#/default/vfs/moduledemos/(.*)#/default/vfs/system/moduledemos/$1#",
        "s#/default/vfs/system/workplace/config/language/(.*)#/default/vfs/system/workplace/locales/$1#",
        "s#/default/vfs/system/workplace/css/(.*)#/default/vfs/system/workplace/resources/$1#",
        "s#/default/vfs/system/workplace/templates/js/(.*)#/default/vfs/system/workplace/scripts/$1#",
        "s#[\\s]+#_#g",
        "s#[" + C_AUML_LOWER + "]#ae#g",
        "s#[" + C_AUML_UPPER + "]#Ae#g",
        "s#[" + C_OUML_LOWER + "]#oe#g",
        "s#[" + C_OUML_UPPER + "]#Oe#g",
        "s#[" + C_UUML_LOWER + "]#ue#g",
        "s#[" + C_UUML_UPPER + "]#Ue#g",
        "s#[" + C_SHARP_S + "]#ss#g",
        "s#[^0-9a-zA-Z_\\.\\-\\/]#!#g",
        "s#!+#x#g"};

    @BeforeAll
    public void setUpConfiguration() {

        initConfiguration();
    }

    /**
     * Tests for the resource name translation.<p>
     */
    @Test
    public void testTranslateResource() {

        CmsResourceTranslator translator = new CmsResourceTranslator(rules, false);
        String test;

        test = translator.translateResource("/default/vfs/content/bodys/test/index.html");
        assertEquals("/default/vfs/system/bodies/test/index.html", test);

        test = translator.translateResource("/default/vfs/system/workplace/templates/js/test.js");
        assertEquals("/default/vfs/system/workplace/scripts/test.js", test);

        translator = new CmsResourceTranslator(rules, true);
        test = translator.translateResource(
            "Sch" + C_OUML_LOWER + "ne " + C_UUML_UPPER + "bung mit Fu" + C_SHARP_S + ".js");
        assertEquals("Schoene_Uebung_mit_Fuss.js", test);
    }

}
