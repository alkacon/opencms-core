// ** I18N

// Calendar LT language
// Author: Martynas Majeris, <martynas@solmetra.lt>
// Updated 2010.06.10 by Nemunas Karalius <nemunas.karalius@jmsys.lt>
// Encoding: Windows-1257
// Distributed under the same terms as the calendar itself.

// For translators: please use UTF-8 if possible.  We strongly believe that
// Unicode is the answer to a real internationalized world.  Also please
// include your contact information in the header, as can be seen above.

// full day names
Calendar._DN = new Array
("Sekmadienis",
 "Pirmadienis",
 "Antradienis",
 "Treciadienis",
 "Ketvirtadienis",
 "Pentadienis",
 "Šeštadienis",
 "Sekmadienis");

// Please note that the following array of short day names (and the same goes
// for short month names, _SMN) isn't absolutely necessary.  We give it here
// for exemplification on how one can customize the short day names, but if
// they are simply the first N letters of the full name you can simply say:
//
//   Calendar._SDN_len = N; // short day name length
//   Calendar._SMN_len = N; // short month name length
//
// If N = 3 then this is not needed either since we assume a value of 3 if not
// present, to be compatible with translation files that were written before
// this feature.

// short day names
Calendar._SDN = new Array
("Sek",
 "Pir",
 "Ant",
 "Tre",
 "Ket",
 "Pen",
 "Šeš",
 "Sek");

// First day of the week. "0" means display Sunday first, "1" means display
// Monday first, etc.
Calendar._FD = 1;

// full month names
Calendar._MN = new Array
("Sausis",
 "Vasaris",
 "Kovas",
 "Balandis",
 "Geguže",
 "Birželis",
 "Liepa",
 "Rugpjutis",
 "Rugsejis",
 "Spalis",
 "Lapkritis",
 "Gruodis");

// short month names
Calendar._SMN = new Array
("Sau",
 "Vas",
 "Kov",
 "Bal",
 "Geg",
 "Bir",
 "Lie",
 "Rgp",
 "Rgs",
 "Spa",
 "Lap",
 "Gru");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "Apie kalendoriu";

Calendar._TT["ABOUT"] =
"DHTML Date/Time Selector\n" +
"(c) dynarch.com 2002-2005 / Author: Mihai Bazon\n" + // don't translate this this ;-)
"Naujausia versija rasite: http://www.dynarch.com/projects/calendar/\n" +
"Platinamas pagal GNU LGPL licencija. Aplankykite http://gnu.org/licenses/lgpl.html" +
"\n\n" +
"Datos pasirinkimas:\n" +
"- Metu pasirinkimas: \xab, \xbb\n" +
"- Menesio pasirinkimas: " + String.fromCharCode(0x2039) + ", " + String.fromCharCode(0x203a) + "\n" +
"- Nuspauskite ir laikykite peles klaviša greitesniam pasirinkimui.";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"Laiko pasirinkimas:\n" +
"- Spustelkite ant valandu arba minuciu - skaicus padides vienetu.\n" +
"- Jei spausite kartu su Shift, skaicius sumažes.\n" +
"- Greitam pasirinkimui spustelkite ir pajudinkite pele.";

Calendar._TT["PREV_YEAR"] = "Ankstesni metai (laikykite, jei norite meniu)";
Calendar._TT["PREV_MONTH"] = "Ankstesnis menuo (laikykite, jei norite meniu)";
Calendar._TT["GO_TODAY"] = "Pasirinkti šiandiena";
Calendar._TT["NEXT_MONTH"] = "Kitas menuo (laikykite, jei norite meniu)";
Calendar._TT["NEXT_YEAR"] = "Kiti metai (laikykite, jei norite meniu)";
Calendar._TT["SEL_DATE"] = "Pasirinkite data";
Calendar._TT["DRAG_TO_MOVE"] = "Tempkite";
Calendar._TT["PART_TODAY"] = " (šiandien)";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "Pirma savaites diena - %s";
//N.Karalius: not needed in this version? left for backward compatibility..?
Calendar._TT["MON_FIRST"] = "Pirma savaites diena - pirmadienis";
Calendar._TT["SUN_FIRST"] = "Pirma savaites diena - sekmadienis";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

Calendar._TT["CLOSE"] = "Uždaryti";
Calendar._TT["TODAY"] = "Šiandien";
Calendar._TT["TIME_PART"] = "Spustelkite arba tempkite jei norite pakeisti";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%Y-%m-%d";
Calendar._TT["TT_DATE_FORMAT"] = "%A, %Y-%m-%d";

Calendar._TT["WK"] = "sav";
Calendar._TT["TIME"] = "Laikas:";
