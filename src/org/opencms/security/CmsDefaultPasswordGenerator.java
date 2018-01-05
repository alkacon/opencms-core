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

public class CmsDefaultPasswordGenerator implements I_CmsPasswordGenerator {

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
    private static final String[] Numbers = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
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
        "§",
        "(",
        ")",
        "[",
        "]"};

    public static String getRandomPWD() {

        CmsDefaultPasswordGenerator generator = new CmsDefaultPasswordGenerator();
        return generator.getRandomPassword();
    }

    public String getRandomPassword() {

        String res = "";

        String[] Normals = ArrayUtils.addAll(ArrayUtils.addAll(Capitals, Letters), Numbers);
        Random rand = new Random();
        for (int i = 0; i < 6; i++) {
            res = res + Normals[rand.nextInt(Normals.length)];
        }
        for (int i = 0; i < 2; i++) {
            int pos = rand.nextInt(res.length());
            res = res.substring(0, pos) + Specials[rand.nextInt(Specials.length)] + res.substring(pos);
        }
        int pos = rand.nextInt(res.length());
        res = res.substring(0, pos) + Capitals[rand.nextInt(Capitals.length)] + res.substring(pos);
        pos = rand.nextInt(res.length());
        res = res.substring(0, pos) + Letters[rand.nextInt(Letters.length)] + res.substring(pos);
        return res;
    }

}
