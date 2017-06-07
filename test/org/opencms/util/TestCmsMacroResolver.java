/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.util;

import org.opencms.i18n.CmsMessages;
import org.opencms.test.OpenCmsTestCase;

import java.util.Locale;

/**
 * Test cases for {@link org.opencms.util.CmsMacroResolver}.<p>
 *
 */
public class TestCmsMacroResolver extends OpenCmsTestCase {

    private static final String MACRO_TEST_I1 = "<div class=\'pathbar\'>&nbsp;</div>\r\n"
        + "<div class=\'screenTitle\'>\r\n"
        + "   <table width=\'100%\' cellspacing=\'0\'>\r\n"
        + "       <tr>\r\n"
        + "           <td>\r\n"
        + "${key."
        + org.opencms.xml.content.Messages.GUI_EDITOR_XMLCONTENT_VALIDATION_ERROR_2
        + "}\r\n"
        + "           </td>       </tr>\r\n"
        + "   </table>\r\n"
        + "</div>";

    private static final String MACRO_TEST_R1 = "<div class=\'pathbar\'>&nbsp;</div>\r\n"
        + "<div class=\'screenTitle\'>\r\n"
        + "   <table width=\'100%\' cellspacing=\'0\'>\r\n"
        + "       <tr>\r\n"
        + "           <td>\r\n"
        + "Invalid value \"{0}\" according to rule {1}\r\n"
        + "           </td>       </tr>\r\n"
        + "   </table>\r\n"
        + "</div>";

    /**
     * Tests some basic resolver functions.<p>
     */
    public void testBasicResolverFunctions() {

        String value = "VALUE";
        String processed = CmsMacroResolver.formatMacro(value);
        assertEquals("%(VALUE)", processed);
        assertTrue(CmsMacroResolver.isMacro(processed));
        assertEquals(value, CmsMacroResolver.stripMacro(processed));

        // check old macro syntax
        processed = "${VALUE}";
        assertTrue(CmsMacroResolver.isMacro(processed));
        assertEquals(value, CmsMacroResolver.stripMacro(processed));
    }

    /**
     * Tests macro util functions.<p>
     */
    public void testMacroUtils() {

        assertTrue(CmsMacroResolver.isMacro("%(newStyle)"));
        assertTrue(CmsMacroResolver.isMacro("${oldStyle}"));
        assertTrue(CmsMacroResolver.isMacro("%(newStyle)", "newStyle"));
        assertTrue(CmsMacroResolver.isMacro("${oldStyle}", "oldStyle"));
        assertFalse(CmsMacroResolver.isMacro("${oldStyle}", "newStyle"));
        assertFalse(CmsMacroResolver.isMacro("%(newStyle)", "oldStyle"));
        assertTrue(CmsMacroResolver.isMacro(CmsMacroResolver.formatMacro("macroName")));
        assertTrue(CmsMacroResolver.isMacro(CmsMacroResolver.formatMacro("macroName"), "macroName"));
    }

    /**
     * Tests the macro resolver "recursive" functions.<p>
     */
    public void testResolveLocalizedMacros() {

        CmsMacroResolver resolver = CmsMacroResolver.newInstance();

        // add the messages to the resolver
        CmsMessages messages = new CmsMessages(org.opencms.xml.content.Messages.get().getBundleName(), Locale.ENGLISH);
        resolver.setMessages(messages);

        // resgister some macros for the validation
        resolver.addMacro("onesecond", "This is the final result");
        resolver.addMacro("twofirst", "second");
        resolver.addMacro("three", "first");

        String value1, value2;
        String keyName;

        keyName = org.opencms.xml.content.Messages.GUI_EDITOR_XMLCONTENT_VALIDATION_ERROR_2;
        value1 = messages.key(keyName);
        value2 = resolver.resolveMacros("${key." + keyName + "}");
        assertEquals("Invalid value \"{0}\" according to rule {1}", value1);
        assertEquals(value1, value2);

        value1 = messages.key(keyName, new Object[] {"'value'", "'rule'"});
        value2 = resolver.resolveMacros("${key." + keyName + "|'value'|'rule'}");
        assertEquals("Invalid value \"'value'\" according to rule 'rule'", value1);
        assertEquals(value1, value2);

        value1 = messages.key(keyName, new Object[] {"This is the final result", "second"});
        value2 = resolver.resolveMacros("${key." + keyName + "|${one${two${three}}}|${two${three}}}");
        assertEquals("Invalid value \"This is the final result\" according to rule second", value1);
        assertEquals(value1, value2);

        keyName = org.opencms.xml.content.Messages.GUI_EDITOR_XMLCONTENT_VALIDATION_WARNING_2;
        value1 = messages.key(keyName);
        value2 = resolver.resolveMacros("${key." + keyName + "}");
        assertEquals("Bad value \"{0}\" according to rule {1}", value1);
        assertEquals(value1, value2);

        value1 = messages.key(keyName, new Object[] {"some value", "the rule"});
        value2 = resolver.resolveMacros("${key." + keyName + "|some value|the rule}");
        assertEquals("Bad value \"some value\" according to rule the rule", value1);
        assertEquals(value1, value2);

        value1 = messages.key(keyName, new Object[] {"This is the final result", "second"});
        value2 = resolver.resolveMacros("${key." + keyName + "|${one${two${three}}}|${two${three}}}");
        assertEquals("Bad value \"This is the final result\" according to rule second", value1);
        assertEquals(value1, value2);
    }

    /**
     * Tests the macro resolver main functions.<p>
     */
    public void testResolveMacros() {

        CmsMacroResolver resolver = CmsMacroResolver.newInstance();
        resolver.addMacro("test", "REPLACED");

        String content, result;

        content = "<<This is a prefix >>${test}<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>REPLACED<<This is a suffix>>", result);

        content = "${test}<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("REPLACED<<This is a suffix>>", result);

        content = "<<This is a prefix >>${test}";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>REPLACED", result);

        content = "<<This is a prefix >>$<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>$<<This is a suffix>>", result);

        content = "$<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("$<<This is a suffix>>", result);

        content = "${<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("${<<This is a suffix>>", result);

        content = "<<This is a prefix >>$";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>$", result);

        content = "<<This is a prefix >>${";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${", result);

        content = "<<This is a prefix >>${}<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >><<This is a suffix>>", result);

        content = "<<This is a prefix >>${unknown}<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >><<This is a suffix>>", result);

        content = "<<This is a prefix >>${test<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${test<<This is a suffix>>", result);

        content = "<<This is a prefix >>${";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${", result);

        content = "<<This is a prefix >>${${}";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${", result);

        content = "${$<<This $}{ is$}$ a {{pr${{${efix >>${${}";
        result = resolver.resolveMacros(content);
        assertEquals("${$<<This $}{ is$}$ a {{pr${{${efix >>${", result);

        // test for unknown macros

        content = "<<This is a prefix >>${unknown}<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >><<This is a suffix>>", result);

        content = "<<This is a prefix >>${unknown}";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>", result);

        content = "${unknown}<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a suffix>>", result);

        content = "${test}<<This is a prefix >>${test}${unknown}${test}<<This is a suffix>>${test}";
        result = resolver.resolveMacros(content);
        assertEquals("REPLACED<<This is a prefix >>REPLACEDREPLACED<<This is a suffix>>REPLACED", result);

        // set the "keep unknown macros" flag
        resolver.setKeepEmptyMacros(true);

        content = "<<This is a prefix >>${${}";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${${}", result);

        content = "<<This is a prefix >>${unknown}<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${unknown}<<This is a suffix>>", result);

        content = "<<This is a prefix >>${unknown}";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${unknown}", result);

        content = "${unknown}<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("${unknown}<<This is a suffix>>", result);

        content = "${test}<<This is a prefix >>${test}${unknown}${test}<<This is a suffix>>${test}";
        result = resolver.resolveMacros(content);
        assertEquals("REPLACED<<This is a prefix >>REPLACED${unknown}REPLACED<<This is a suffix>>REPLACED", result);
    }

    /**
     * Tests the macro resolver main functions, combined syntax.<p>
     */
    public void testResolveMacrosCombinedSyntax() {

        CmsMacroResolver resolver = CmsMacroResolver.newInstance();
        resolver.addMacro("test", "REPLACED");

        String content, result;

        content = "<<This is a prefix >>%(test)-${test}<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>REPLACED-REPLACED<<This is a suffix>>", result);

        content = "<<This is a prefix >>%{test}<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>%{test}<<This is a suffix>>", result);

        content = "<<This is a prefix >>%(test}-%{test)-%{test}-${test)-$(test}-$(test)<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        // assertEquals("<<This is a prefix >>%(test}-%{test)-%{test}-${test)-$(test}-$(test)<<This is a suffix>>", result);

        content = "${test}-%(test)<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("REPLACED-REPLACED<<This is a suffix>>", result);

        content = "<<This is a prefix >>${test}-%(test)";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>REPLACED-REPLACED", result);

        content = "<<This is a prefix >>%$<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>%$<<This is a suffix>>", result);

        content = "%$<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("%$<<This is a suffix>>", result);

        content = "%(${<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("%(${<<This is a suffix>>", result);

        content = "<<This is a prefix >>$%";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>$%", result);

        content = "<<This is a prefix >>%(${";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>%(${", result);

        content = "<<This is a prefix >>%()${}<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >><<This is a suffix>>", result);

        content = "<<This is a prefix >>%(unknown)${unknown}<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >><<This is a suffix>>", result);

        content = "<<This is a prefix >>%(test${test<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>%(test${test<<This is a suffix>>", result);

        content = "<<This is a prefix >>%${(%()${}";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>%${(", result);

        content = "<<This is a prefix >>%(a${b}c)<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >><<This is a suffix>>", result);

        content = "<<This is a prefix >>%(a${test}c)<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >><<This is a suffix>>", result);

        content = "<<This is a prefix >>${a%(test)c}<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >><<This is a suffix>>", result);

        content = "<<This is a prefix >>${a${test}c$}<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${aREPLACEDc$}<<This is a suffix>>", result);

        content = "<<This is a prefix >>%(a%(test)c%)<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>%(aREPLACEDc%)<<This is a suffix>>", result);

        content = "<<This is a prefix >>${a${test}c)<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${aREPLACEDc)<<This is a suffix>>", result);

        content = "<<This is a prefix >>%(a%(test)c}<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>%(aREPLACEDc}<<This is a suffix>>", result);
    }

    /**
     * Tests the macro resolver main functions, new syntax.<p>
     */
    public void testResolveMacrosNewSyntax() {

        CmsMacroResolver resolver = CmsMacroResolver.newInstance();
        resolver.addMacro("test", "REPLACED");

        String content, result;

        content = "<<This is a prefix >>%(test)<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>REPLACED<<This is a suffix>>", result);

        content = "%(test)<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("REPLACED<<This is a suffix>>", result);

        content = "<<This is a prefix >>%(test)";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>REPLACED", result);

        content = "<<This is a prefix >>%<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>%<<This is a suffix>>", result);

        content = "%<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("%<<This is a suffix>>", result);

        content = "%(<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("%(<<This is a suffix>>", result);

        content = "<<This is a prefix >>%";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>%", result);

        content = "<<This is a prefix >>%(";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>%(", result);

        content = "<<This is a prefix >>%()<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >><<This is a suffix>>", result);

        content = "<<This is a prefix >>%(unknown)<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >><<This is a suffix>>", result);

        content = "<<This is a prefix >>%(test<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>%(test<<This is a suffix>>", result);

        content = "<<This is a prefix >>%(";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>%(", result);

        content = "<<This is a prefix >>%(%()";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>%(", result);

        content = "%(%<<This %)( is%)% a ((pr%((%(efix >>%(%()";
        result = resolver.resolveMacros(content);
        assertEquals("%(%<<This %)( is%)% a ((pr%((%(efix >>%(", result);

        // test for unknown macros

        content = "<<This is a prefix >>%(unknown)<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >><<This is a suffix>>", result);

        content = "<<This is a prefix >>%(unknown)";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>", result);

        content = "%(unknown)<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a suffix>>", result);

        content = "%(test)<<This is a prefix >>%(test)%(unknown)%(test)<<This is a suffix>>%(test)";
        result = resolver.resolveMacros(content);
        assertEquals("REPLACED<<This is a prefix >>REPLACEDREPLACED<<This is a suffix>>REPLACED", result);

        // set the "keep unknown macros" flag
        resolver.setKeepEmptyMacros(true);

        content = "<<This is a prefix >>%(%()";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>%(%()", result);

        content = "<<This is a prefix >>%(unknown)<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>%(unknown)<<This is a suffix>>", result);

        content = "<<This is a prefix >>%(unknown)";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>%(unknown)", result);

        content = "%(unknown)<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("%(unknown)<<This is a suffix>>", result);

        content = "%(test)<<This is a prefix >>%(test)%(unknown)%(test)<<This is a suffix>>%(test)";
        result = resolver.resolveMacros(content);
        assertEquals("REPLACED<<This is a prefix >>REPLACED%(unknown)REPLACED<<This is a suffix>>REPLACED", result);
    }

    /**
     * Tests the macro resolver "nested macro" functions.<p>
     */
    public void testResolveNestedMacros() {

        CmsMacroResolver resolver = CmsMacroResolver.newInstance();
        resolver.addMacro("onesecond", "This is the final result");
        resolver.addMacro("twofirst", "second");
        resolver.addMacro("three", "first");

        String content, result;

        content = "${three}";
        result = resolver.resolveMacros(content);
        assertEquals("first", result);

        content = "${two${three}}";
        result = resolver.resolveMacros(content);
        assertEquals("second", result);

        content = "${one${two${three}}}";
        result = resolver.resolveMacros(content);
        assertEquals("This is the final result", result);

        content = "${one ${two${three}}}";
        result = resolver.resolveMacros(content);
        assertEquals("", result);

        resolver.setKeepEmptyMacros(true);
        content = "${one ${two${three}}}";
        result = resolver.resolveMacros(content);
        assertEquals("${one second}", result);
    }

    /**
     * Tests a minimal interface implementation.<p>
     */
    public void testResolverInterface() {

        I_CmsMacroResolver resolver = new I_CmsMacroResolver() {

            public String getMacroValue(String key) {

                if ("test".equals(key)) {
                    return "REPLACED";
                } else {
                    return null;
                }
            }

            public boolean isKeepEmptyMacros() {

                return true;
            }

            public String resolveMacros(String input) {

                return CmsMacroResolver.resolveMacros(input, this);
            }
        };

        String content, result;

        content = null;
        result = resolver.resolveMacros(null);
        assertEquals(null, result);

        content = "";
        result = resolver.resolveMacros(content);
        assertEquals("", result);

        content = "${test}";
        result = resolver.resolveMacros(content);
        assertEquals("REPLACED", result);

        content = "<<This is a prefix >>${test}<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>REPLACED<<This is a suffix>>", result);

        content = "${test}<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("REPLACED<<This is a suffix>>", result);

        content = "<<This is a prefix >>${test}";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>REPLACED", result);

        content = "<<This is a prefix >>$<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>$<<This is a suffix>>", result);

        content = "$<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("$<<This is a suffix>>", result);

        content = "<<This is a prefix >>$";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>$", result);

        content = "<<This is a prefix >>${}<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${}<<This is a suffix>>", result);

        content = "<<This is a prefix >>${test<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${test<<This is a suffix>>", result);

        content = "<<This is a prefix >>${unknown}<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${unknown}<<This is a suffix>>", result);

        content = "${test}<<This is a prefix >>${test}${unknown}${test}<<This is a suffix>>${test}";
        result = resolver.resolveMacros(content);
        assertEquals("REPLACED<<This is a prefix >>REPLACED${unknown}REPLACED<<This is a suffix>>REPLACED", result);

        content = "<<This is a prefix >>${unknown}";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is a prefix >>${unknown}", result);

        content = "${unknown}<<This is a suffix>>";
        result = resolver.resolveMacros(content);
        assertEquals("${unknown}<<This is a suffix>>", result);

        content = "Uncle Scrooge owns many $$$$";
        result = resolver.resolveMacros(content);
        assertEquals(content, result);

        content = "$$$$ is what uncle Scrooge owns";
        result = resolver.resolveMacros(content);
        assertEquals(content, result);

        content = "$$$$ is $ what $ uncle $$$ Scrooge $ owns $$$";
        result = resolver.resolveMacros(content);
        assertEquals(content, result);

        content = "$${test}$}${test} is ${ what ${test}{$} uncle ${${test} Scrooge $ owns ${${test}}";
        result = resolver.resolveMacros(content);
        assertEquals("$REPLACED$}REPLACED is ${ what REPLACED{$} uncle ${REPLACED Scrooge $ owns ${REPLACED}", result);
    }

    /**
     * Tests some issues encounteerd when introducing the new macro style.<p>
     */
    public void testResolverIssues() {

        CmsMacroResolver resolver = CmsMacroResolver.newInstance();
        resolver.addMacro("test", "REPLACED");

        String content, result;

        content = "<<This is % a prefix >>${test}- % $ -%(test)";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is % a prefix >>REPLACED- % $ -REPLACED", result);

        content = "<<This is % a prefix >>%(test)- $ % -${test}";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is % a prefix >>REPLACED- $ % -REPLACED", result);

        content = "<<This is $ a prefix >>%(test)- $ % -${test}";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is $ a prefix >>REPLACED- $ % -REPLACED", result);

        content = "<<This is $ a prefix >>${test}- % $ -%(test)";
        result = resolver.resolveMacros(content);
        assertEquals("<<This is $ a prefix >>REPLACED- % $ -REPLACED", result);

        // add the messages to the resolver
        CmsMessages messages = new CmsMessages(org.opencms.xml.content.Messages.get().getBundleName(), Locale.ENGLISH);
        resolver.setMessages(messages);

        result = resolver.resolveMacros(MACRO_TEST_I1);
        assertEquals(MACRO_TEST_R1, result);
    }
}
