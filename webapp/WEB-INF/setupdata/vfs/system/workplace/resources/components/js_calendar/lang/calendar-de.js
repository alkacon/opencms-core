// Author: Hartwig Weinkauf h_weinkauf@gmx.de
// Überarbeitet und fehlende Texte hinzugefügt von Gerhard Neinert (gerhard at neinert punkt de)
// Feel free to use / redistribute under the GNU LGPL.
// ** I18N

// short day names
Calendar._SDN = new Array
("So",
 "Mo",
 "Di",
 "Mi",
 "Do",
 "Fr",
 "Sa",
 "So");

// full day names
Calendar._DN = new Array
("Sonntag",
 "Montag",
 "Dienstag",
 "Mittwoch",
 "Donnerstag",
 "Freitag",
 "Samstag",
 "Sonntag");

// short day names only use 2 letters instead of 3
Calendar._SDN_len = 2;

// full month names
Calendar._MN = new Array
("Januar",
 "Februar",
 "März",
 "April",
 "Mai",
 "Juni",
 "Juli",
 "August",
 "September",
 "Oktober",
 "November",
 "Dezember");

// short month names
Calendar._SMN = new Array
("Jan",
 "Feb",
 "Mär",
 "Apr",
 "Mai",
 "Jun",
 "Jul",
 "Aug",
 "Sep",
 "Okt",
 "Nov",
 "Dez");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "Über den Kalender";

Calendar._TT["ABOUT"] =
"DHTML Datum/Zeit Selector\n" +
"(c) dynarch.com 2002-2003\n" + // don't translate this this ;-)
"Download neueste Version: http://dynarch.com/mishoo/calendar.epl\n" +
"Distributed under GNU LGPL.  See http://gnu.org/licenses/lgpl.html for details." +
"\n\n" +
"Datumsauswahl:\n" +
"- Jahr auswählen mit \xab und \xbb\n" +
"- Monat auswählen mit " + String.fromCharCode(0x2039) + " und " + String.fromCharCode(0x203a) + "\n" +
"- Für Auswahl aus Liste Maustaste gedrückt halten.";

Calendar._TT["ABOUT_TIME"] = "\n\n" +
"Zeit wählen:\n" +
"- Stunde/Minute weiter mit Mausklick\n" +
"- Stunde/Minute zurück mit Shift-Mausklick\n" +
"- oder für schnellere Auswahl nach links oder rechts ziehen.";


Calendar._TT["TOGGLE"] = "Ersten Tag der Woche wählen";
Calendar._TT["PREV_YEAR"] = "Jahr zurück (halten: Auswahl)";
Calendar._TT["PREV_MONTH"] = "Monat zurück (halten: Auswahl)";
Calendar._TT["GO_TODAY"] = "Gehe zum heutigen Datum";
Calendar._TT["NEXT_MONTH"] = "Monat vor (halten: Auswahl)";
Calendar._TT["NEXT_YEAR"] = "Jahr vor (halten: Auswahl)";
Calendar._TT["SEL_DATE"] = "Datum auswählen";
Calendar._TT["DRAG_TO_MOVE"] = "Klicken & Halten z. Verschieben";
Calendar._TT["PART_TODAY"] = " (heute)";
Calendar._TT["MON_FIRST"] = "Woche mit Montag beginnen";
Calendar._TT["SUN_FIRST"] = "Woche mit Sonntag beginnen";
Calendar._TT["CLOSE"] = "Schließen";
Calendar._TT["TODAY"] = "Heute";
Calendar._TT["TIME_PART"] = "(Shift-)Klicken/Ziehen z. Ändern";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%d.%m.%Y";
Calendar._TT["TT_DATE_FORMAT"] = "%a, %e. %b";

Calendar._TT["WK"] = "KW";
