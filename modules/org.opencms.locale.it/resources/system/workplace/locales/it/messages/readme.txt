Italian localization for OpenCms
Version 21.0.0
September 2026

Provided and maintained by Coranto Informatica


1. Overview

This module contains the Italian localization files for the OpenCms workplace.

The translation is maintained with the following priorities:
- clear and natural Italian for workplace users;
- consistent terminology across all message bundles;
- meaning-based translation of OpenCms concepts, instead of literal translation when that would sound unnatural or misleading;
- alignment with the Italian section of the Free Translation Project.

Reference documents:
- https://tp.linux.it/glossario.html
- https://tp.linux.it/buona_traduzione.html

Messages with the LOG and INIT prefixes are intentionally left untranslated.

Double quotation marks are written with the Unicode escapes “ and ” where needed, instead of raw Unicode characters, to keep a single consistent encoding across all bundles regardless of the tool used to edit them.

Apostrophes are written with the Unicode escape ’ where needed, instead of a raw ASCII apostrophe. This avoids conflicts with the java.text.MessageFormat quoting syntax: an unescaped ASCII apostrophe in a value that also contains a placeholder such as {0} is treated as a quoting character and can silently break the placeholder substitution.

All message bundles use CRLF line endings, consistent with the rest of the module.


2. Compatibility

This version targets OpenCms 21.0.0.


3. Maintainer

Coranto Informatica
Via Torricelli, 9
I-09047 Selargius (CA), Italy
https://www.coranto.it


4. Change history

1.0.0 - November 2, 2011
- First public release.

9.5.0 - January 23, 2015
- Updated the module for OpenCms 8.5.x, 9.0.x, and 9.5.
- Added and updated workplace, ADE, GWT, UGC, workflow, XML content, and administration tool bundles.

21.0.0 - September 2026
- Updated the Italian localization module for OpenCms 21.0.0.
- Completed a full systematic review of all 124 message bundles, comparing every key against the English source and the German localization.
- Added missing Italian message bundles and message keys where needed.
- Fixed a resource bundle located at an incorrect VFS/package path, which prevented its translations from being loaded by the Java resource bundle loader.
- Fixed the content-encoding property, incorrectly set to ISO-8859-1 for all message bundles despite their actual UTF-8 encoding.
- Updated terminology according to the Italian Free Translation Project guidelines.
- Standardized the OpenCms "sibling" concept as "risorsa collegata".
- Standardized "workplace" as an untranslated term throughout the module.
- Standardized the delete/cancel terminology: "eliminare" for distinct resources and objects, "cancellare" for history, logs, cache, and registry entries.
- Replaced literal calques such as "fallito" with the more natural "non riuscito" across error and report messages.
- Fixed several mistranslations, duplicate button labels, gender/number agreement errors, and punctuation inconsistencies.


5. Translation notes

The translation prefers concise action labels and natural confirmation questions. For example, UI confirmations may use infinitive forms such as "Eliminare la risorsa?" or "Pubblicare le risorse selezionate?" where this is clearer than a literal "Do you want to..." construction.

OpenCms-specific concepts are translated by meaning. In particular, "sibling" is translated as "risorsa collegata", because in OpenCms it refers to another linked resource entry for the same content, not to a family relationship.

"Workplace" is kept as an untranslated term throughout the module.

"Delete" is translated as "eliminare" for distinct resources or objects (files, folders, users, groups, and similar), and as "cancellare" for history, logs, cache, and registry entries, following the distinction already used in natural Italian ("la cronologia si cancella", not "si elimina").

Some established technical terms are kept in English when they are clearer for OpenCms administrators or common in Italian technical usage, for example "cache", "plugin", "workflow", and "widget".


6. Glossary

This section lists the preferred Italian translation for selected technical terms. Where a term can be rendered in more than one way depending on context, the applicable context is noted after a comma.

about: informazioni su OpenCms
account: account
admin view root: vista amministratore principale
authentication: autenticazione
bean: componente
boost (to): aumentare la rilevanza
broadcast: broadcast
browse (to): scorrere
bundle: raggruppamento
button: pulsante
cache: cache
cancel (to): annullare
check (to): selezionare, spuntare
clear (to): cancellare, svuotare, depending on context
clipboard: area degli appunti
container: contenitore
container page: pagina contenitore
core: principale
cron: cron
decline (to): rifiutare
default: predefinito
delete (to): eliminare, for distinct resources/objects; cancellare, for history, logs, cache, and registry entries
digest: digest
direct edit: modifica diretta
directory: cartella, directory, in technical contexts
display: visualizzazione
download: scaricamento, file scaricabile, depending on context
download (to): scaricare
edit (to): modificare
editor: editor
email: e-mail
entry: voce
enum: enumerazione
explorer: esplorazione
erase (to): cancellare
favorite: preferito
file system: file system
flag: indicatore
flush (to): svuotare
folder: cartella
form: modulo
formatter: formattatore
handle: gestore
handle (to): gestire
history: cronologia
ID: ID
illegal: non valido, illecito, in security contexts
instantiation: creazione di un'istanza
institution: organizzazione
item: elemento
job: processo
label: etichetta
link: collegamento
live: in esecuzione, online, in project names
lock: blocco
log: registro
login: accesso
logout: uscita
logout (to): uscire
lost+found: lost+found
null: null, nullo, when describing a value
OK: OK
optional: facoltativo
overview: riepilogo, panoramica
package: pacchetto
parent: principale, superiore, depending on context
parse (to): analizzare, validare sintatticamente, where needed
pattern: modello
permission: autorizzazione
plugin: plugin
pool: pool
pop-up: finestra a comparsa
principal: identità, for user or group security principals
process (to): elaborare
progress: avanzamento
query: interrogazione
redo (to): ripetere
regular expression: espressione regolare
render (to): presentare, generare, depending on context
report: report
reset (to): reimpostare, annullare, in specific contexts
resource: risorsa
restore (to): ripristinare
root administrator: amministratore del sistema
root organizational unit: unità organizzativa radice
root site: sito radice
scale (to): ridimensionare
schedule (to): pianificare
scheduled: pianificato
scheduler: scheduler
search (to): cercare
set (to): impostare
sibling: risorsa collegata
sitemap: mappa del sito
skipping: saltato
source: origine
steal: acquisire
store (to): memorizzare
stream: flusso
stream socket: stream socket
stylesheet: foglio di stile
subfolder: sottocartella
subresource: sottorisorsa
support (to): supportare
tag: tag, for HTML, etichetta, in publishing contexts
target: destinazione
template: modello, template, where established in OpenCms UI
thread: unità di elaborazione
timestamp: data e ora
touch (to): aggiornare
undo (to): annullare
unzip: estrazione del contenuto
update (to): aggiornare
upload: caricamento
upload (to): caricare
warning: avviso
webuser: web user, when used as organizational unit name
widget: widget
workflow: workflow
workplace: workplace
wrapper: wrapper
