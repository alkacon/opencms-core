﻿(function(cms) {

   /** Parameter 'action' value 'startedit' constant. */
   var /** String */ ACTION_STARTEDIT = 'startedit';
   
   /** Parameter 'action' value 'stopedit' constant. */
   var /** String */ ACTION_STOPEDIT = 'stopedit';
   
   /** The current locale.   */
   var /** String */ LOCALE = cms.data.LOCALE;
   
   /** The sitemap uri.    */
   var /**String*/ SITEMAP_URI = cms.data.SITEMAP_URI;
   
   /** The url for server requests.    */
   var /** String */ SERVER_URL = cms.data.SERVER_URL;
   
   /** The current locale used to render the container page. */
   var /** String */ locale = cms.data.locale = 'en';
   
   /** If the container page itself can be edited, ie. move and delete elements. */
   var /** boolean */ allowEdit = cms.data.allowEdit = true;
   
   /** The name of the user that has locked the container page. */
   var /** String */ lockedBy = cms.data.lockedBy = '';
   
   /**
    * Generic function for posting JSON data to the server.
    *
    * @param {String} action a string to tell the server what to do with the data
    * @param {Object} data the JSON data
    * @param {Function} afterPost the callback that should be called after the server replied
    * @param {boolean} async optional flag to indicate is the request should synchronized or not, by default it is not
    * @param {int} timeout optional timeout in millisecs, default is #AJAX_TIMEOUT
    */
   var sitemapPostJSON = cms.data.sitemapPostJSON = /** void */ function(/** String */action, /** Object */ data, /** void Function(boolean, Object) */ afterPost, /** boolean */ sync, /** int */ timeout) {
   
      cms.comm.postJSON(SERVER_URL, {
            'sitemap': SITEMAP_URI,
            'locale': LOCALE,
            'action': action,
            'data': JSON.stringify(data)
         }, afterPost, sync, timeout);
   }
   
   /**
    * Locks the container page on the server via AJAX.
    *
    * @param {Function} callback the callback that should be called
    */
   var startEdit = cms.data.startEdit = /** void */ function(/** void Function(boolean) */ callback) {
   
      postJSON(ACTION_STARTEDIT, {}, callback);
   }
   
   /**
    * Unlocks the container page on the server via AJAX.
    *
    * @param {Function} callback the callback that should be called
    */
   var stopEdit = cms.data.stopEdit = /** void */ function(/** void Function(boolean) */ callback) {
   
      postJSON(ACTION_STOPEDIT, {}, callback);
   }   
})(cms);

