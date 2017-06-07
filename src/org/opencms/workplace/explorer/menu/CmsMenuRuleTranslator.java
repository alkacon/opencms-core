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

package org.opencms.workplace.explorer.menu;

import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides methods to translate the legacy rule Strings for the context menu entries to the new menu rule set definitions.<p>
 *
 * @since 6.5.6
 */
public class CmsMenuRuleTranslator {

    /** The legacy rule String for the direct publish item on files. */
    private static final String MENURULE_LEGACY_DP_FILE = "d d iaaa iaaa dddd";

    /** The legacy rule String for the show siblings entry. */
    private static final String MENURULE_LEGACY_SHOWSIBLINGS = "a a aaaa aaaa aaaa";

    /** The new menu rule set names used instead of the rule Strings. */
    private static final String[] MENURULES = new String[] {
        "copytoproject",
        "lock",
        "changelock",
        "unlock",
        "directpublish",
        "directpublish",
        "showlocks",
        "undochanges",
        "undochanges",
        "undelete",
        "copy",
        "standard",
        "permissions",
        "nondeleted",
        "showsiblings"};

    /** The legacy menu rule Strings which were used in OpenCms 6. */
    private static final String[] MENURULES_LEGACY_STRINGS = new String[] {
        "d a dddd dddd dddd", // copytoproject
        "d d aaaa dddd dddd", // ...
        "d d dddd dddd aaaa",
        "d d dddd aaaa dddd",
        "d d aaaa aaaa dddd", // directpublish (folder)
        MENURULE_LEGACY_DP_FILE, // directpublish (file)
        "d d aaaa aaaa aaaa",
        "d d iiid aaid dddd", // undochanges (folder)
        "d d iiid iaid dddd", // undochanges (file)
        "d d ddda ddda dddd",
        "d d aaai aaai dddd",
        "d d iiii aaai dddd",
        "a a iiii aaai dddd",
        "a a aaai aaai aaai", // ...
        MENURULE_LEGACY_SHOWSIBLINGS // showsiblings
    };

    /** The rules applying for locked resources. */
    private static final Object[] TRANS_LOCKEDLOCKRULES = new Object[] {
        new CmsMirPrSameLockedActive(),
        new CmsMirPrSameLockedInvisible(),
        new CmsMirPrSameLockedActiveNotDeletedAl(),
        new CmsMirPrSameLockedActiveChangedAl(),
        new CmsMirPrSameLockedActiveDeletedAl()};

    /** The legacy rule Strings applying for locked resources. */
    private static final String[] TRANS_LOCKEDLOCKRULES_LEGACY = new String[] {"aaaa", "dddd", "aaai", "aaid", "ddda"};

    /** The legacy rule Strings as List applying for locked resources. */
    private static final List<String> TRANS_LOCKEDLOCKRULES_LEGACY_LIST = Arrays.asList(TRANS_LOCKEDLOCKRULES_LEGACY);

    /** The rules applying for the Online project. */
    private static final Object[] TRANS_ONLINERULES = new Object[] {
        new CmsMirPrOnlineActive(),
        new CmsMirPrOnlineInactive(),
        new CmsMirPrOnlineInvisible()};

    /** The legacy rule Strings applying for the Online project. */
    private static final String[] TRANS_ONLINERULES_LEGACY = new String[] {"a", "i", "d"};

    /** The legacy rule Strings as List applying for the Online project. */
    private static final List<String> TRANS_ONLINERULES_LEGACY_LIST = Arrays.asList(TRANS_ONLINERULES_LEGACY);

    /** The rules applying for all other lock states. */
    private static final Object[] TRANS_OTHERLOCKRULES = new Object[] {
        new CmsMirPrSameOtherlockActive(),
        new CmsMirPrSameOtherlockInvisible(),
        new CmsMirActiveNonDeleted()};

    /** The legacy rule Strings applying for all other lock states. */
    private static final String[] TRANS_OTHERLOCKRULES_LEGACY = new String[] {"aaaa", "dddd", "aaai"};

    /** The legacy rule Strings as List applying for all other lock states. */
    private static final List<String> TRANS_OTHERLOCKRULES_LEGACY_LIST = Arrays.asList(TRANS_OTHERLOCKRULES_LEGACY);

    /** The rules applying for other projects. */
    private static final Object[] TRANS_OTHERPROJECTRULES = new Object[] {
        new CmsMirPrOtherActive(),
        new CmsMirPrOtherInactive(),
        new CmsMirPrOtherInvisible()};

    /** The legacy rule Strings applying for other pojects. */
    private static final String[] TRANS_OTHERPROJECTRULES_LEGACY = new String[] {"a", "i", "d"};

    /** The legacy rule Strings applying for other projects. */
    private static final List<String> TRANS_OTHERPROJECTRULES_LEGACY_LIST = Arrays.asList(
        TRANS_OTHERPROJECTRULES_LEGACY);

    /** The rules applying for unlocked resources. */
    private static final Object[] TRANS_UNLOCKEDRULES = new Object[] {
        new CmsMirPrSameUnlockedActive(),
        new CmsMirPrSameUnlockedInactiveNoAl(),
        new CmsMirPrSameUnlockedInvisible(),
        new CmsMirPrSameUnlockedActiveNotDeletedNoAl(),
        new CmsMirPrSameUnlockedActiveUnchanged(),
        new CmsMirPrSameUnlockedInactiveNotDeletedNoAl(),
        new CmsMirPrSameUnlockedActiveDeletedNoAl()};

    /** The legacy rule Strings applying for unlocked resources. */
    private static final String[] TRANS_UNLOCKEDRULES_LEGACY = new String[] {
        "aaaa",
        "iiii",
        "dddd",
        "aaai",
        "iaaa",
        "iiid",
        "ddda"};

    /** The legacy rule Strings as List applying for unlocked resources. */
    private static final List<String> TRANS_UNLOCKEDRULES_LEGACY_LIST = Arrays.asList(TRANS_UNLOCKEDRULES_LEGACY);

    /** The mappings from the legacy rule strings to the menu rule set names. */
    private Map<String, String> m_ruleMappings;

    /**
     * Empty constructor.<p>
     */
    public CmsMenuRuleTranslator() {

        // nothing to do here
    }

    /**
     * Creates a new menu rule set from the given legacy rule String.<p>
     *
     * @param legacyRules the legacy rule String to parse
     * @return a menu rule set from the given legacy rule String
     */
    public CmsMenuRule createMenuRule(String legacyRules) {

        legacyRules = substituteLegacyRules(legacyRules);
        CmsMenuRule menuRule = new CmsMenuRule();
        if (legacyRules.equals(substituteLegacyRules(MENURULE_LEGACY_DP_FILE))) {
            // special case: direct publish rule for files
            menuRule.addMenuItemRule((I_CmsMenuItemRule)TRANS_ONLINERULES[2]);
            menuRule.addMenuItemRule((I_CmsMenuItemRule)TRANS_OTHERPROJECTRULES[2]);
            menuRule.addMenuItemRule(new CmsMirDirectPublish());
        } else if (legacyRules.equals(substituteLegacyRules(MENURULE_LEGACY_SHOWSIBLINGS))) {
            // special case: show siblings
            menuRule.addMenuItemRule(new CmsMirShowSiblings());
        } else {
            // get the rule for the online project
            String currentRuleString = legacyRules.substring(0, 1);
            int ruleIndex = TRANS_ONLINERULES_LEGACY_LIST.indexOf(currentRuleString);
            if (ruleIndex != -1) {
                menuRule.addMenuItemRule((I_CmsMenuItemRule)TRANS_ONLINERULES[ruleIndex]);
            }
            // get the rule for other project
            currentRuleString = legacyRules.substring(1, 2);
            ruleIndex = TRANS_OTHERPROJECTRULES_LEGACY_LIST.indexOf(currentRuleString);
            if (ruleIndex != -1) {
                menuRule.addMenuItemRule((I_CmsMenuItemRule)TRANS_OTHERPROJECTRULES[ruleIndex]);
            }
            // get the rule for unlocked case
            currentRuleString = legacyRules.substring(2, 6);
            ruleIndex = TRANS_UNLOCKEDRULES_LEGACY_LIST.indexOf(currentRuleString);
            if (ruleIndex != -1) {
                menuRule.addMenuItemRule((I_CmsMenuItemRule)TRANS_UNLOCKEDRULES[ruleIndex]);
            }
            // get the rule for exclusive lock
            currentRuleString = legacyRules.substring(6, 10);
            ruleIndex = TRANS_LOCKEDLOCKRULES_LEGACY_LIST.indexOf(currentRuleString);
            if (ruleIndex != -1) {
                menuRule.addMenuItemRule((I_CmsMenuItemRule)TRANS_LOCKEDLOCKRULES[ruleIndex]);
            }
            // get the rule for all other states
            currentRuleString = legacyRules.substring(10);
            ruleIndex = TRANS_OTHERLOCKRULES_LEGACY_LIST.indexOf(currentRuleString);
            if (ruleIndex != -1) {
                menuRule.addMenuItemRule((I_CmsMenuItemRule)TRANS_OTHERLOCKRULES[ruleIndex]);
            }
        }
        // set a name for the rule
        menuRule.setName("rule_" + legacyRules.hashCode());
        return menuRule;
    }

    /**
     * Returns the name of the matching default rule set definition for the given legacy rule String.<p>
     *
     * If no matching rule set can be found, <code>null</code> is returned.<p>
     *
     * @param legacyRules the legacy rule String
     * @return the name of the matching default rule set definition for the given legacy rule String
     */
    public String getMenuRuleName(String legacyRules) {

        return getRuleMappings().get(substituteLegacyRules(legacyRules));
    }

    /**
     * Returns if a matching default rule set definition is present for the given legacy rule String.<p>
     *
     * @param legacyRules the legacy rule String
     * @return true if a matching default rule set definition is present for the given legacy rule String, otherwise false
     */
    public boolean hasMenuRule(String legacyRules) {

        String ruleName = getRuleMappings().get(substituteLegacyRules(legacyRules));
        if (CmsStringUtil.isNotEmpty(ruleName) && (OpenCms.getWorkplaceManager() != null)) {
            return OpenCms.getWorkplaceManager().getMenuRule(ruleName) != null;
        }
        return false;
    }

    /**
     * Returns the mappings of the legacy rule Strings to the default menu rule set names.<p>
     *
     * @return the mappings of the legacy rule Strings to the default menu rule set names
     */
    protected Map<String, String> getRuleMappings() {

        if (m_ruleMappings == null) {
            m_ruleMappings = new HashMap<String, String>(MENURULES_LEGACY_STRINGS.length);
            for (int i = 0; i < MENURULES_LEGACY_STRINGS.length; i++) {
                try {
                    String ruleName = MENURULES[i];
                    String legacyRule = substituteLegacyRules(MENURULES_LEGACY_STRINGS[i]);
                    m_ruleMappings.put(legacyRule, ruleName);
                } catch (Exception e) {
                    // ignore, should not happen
                }
            }
        }
        return m_ruleMappings;
    }

    /**
     * Removes all whitespaces from the given legacy rule String.<p>
     *
     * @param legacyRules the legacy rule String to substitute
     * @return the legacy rule String without whitespaces
     */
    protected String substituteLegacyRules(String legacyRules) {

        return CmsStringUtil.substitute(legacyRules, " ", "");
    }

}
