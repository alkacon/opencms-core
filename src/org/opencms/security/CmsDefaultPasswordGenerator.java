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

package org.opencms.security;

import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Default Password Generator class.<p>
 */
public class CmsDefaultPasswordGenerator implements I_CmsPasswordGenerator {

    /**Capital letters. */
    private static final String[] Capitals = {
        "A",
        "B",
        "C",
        "D",
        "E",
        "F",
        "G",
        "H",
        "I",
        "J",
        "K",
        "L",
        "M",
        "N",
        "O",
        "P",
        "Q",
        "R",
        "S",
        "T",
        "U",
        "V",
        "W",
        "X",
        "Y",
        "Z"};

    /**Letters. */
    private static final String[] Letters = {
        "a",
        "b",
        "c",
        "d",
        "e",
        "f",
        "g",
        "h",
        "i",
        "j",
        "k",
        "l",
        "m",
        "n",
        "o",
        "p",
        "q",
        "r",
        "s",
        "t",
        "u",
        "v",
        "w",
        "x",
        "y",
        "z"};

    /**Numbers. */
    private static final String[] Numbers = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

    /**Special chars. */
    private static final String[] Specials = {
        "!",
        "?",
        "=",
        "*",
        "+",
        "-",
        "#",
        "$",
        "%",
        "&",
        ":",
        "(",
        ")",
        "[",
        "]"};

    /**
     * Get a random password.<p>
     *
     * @return random password
     */
    public static String getRandomPWD() {

        return getRandomPWD(10, 2, 2);
    }

    /**
     * Get a random password.<p>
     * @param countTotal Desired password length
     * @param countCapitals minimal count of Capital letters
     * @param countSpecials count of special chars
     *
     * @return random password
     */
    public static String getRandomPWD(int countTotal, int countCapitals, int countSpecials) {

        CmsDefaultPasswordGenerator generator = new CmsDefaultPasswordGenerator();
        return generator.getRandomPassword(countTotal, countCapitals, countSpecials);
    }

    /**
     * @see org.opencms.security.I_CmsPasswordGenerator#getRandomPassword()
     */
    public String getRandomPassword() {

        return getRandomPassword(10, 2, 2);
    }

    /**
     * Generates a random password.<p>
     *
     * @param countTotal Total password length
     * @param countCapitals Minimal count of capitals
     * @param countSpecials count of special chars
     * @return random password
     */
    public String getRandomPassword(int countTotal, int countCapitals, int countSpecials) {

        String res = "";

        String[] Normals = ArrayUtils.addAll(ArrayUtils.addAll(Capitals, Letters), Numbers);
        Random rand = new Random();
        for (int i = 0; i < (countTotal - countCapitals - countSpecials); i++) {
            res = res + Normals[rand.nextInt(Normals.length)];
        }
        for (int i = 0; i < countSpecials; i++) {
            int pos = rand.nextInt(res.length());
            res = res.substring(0, pos) + Specials[rand.nextInt(Specials.length)] + res.substring(pos);
        }
        for (int i = 0; i < countCapitals; i++) {
            int pos = rand.nextInt(res.length());
            res = res.substring(0, pos) + Capitals[rand.nextInt(Capitals.length)] + res.substring(pos);
        }
        return res;
    }

}
