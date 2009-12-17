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
   
   var ACTION_SAVE = 'save';
   var ACTION_GET = 'get';
   var ACTION_SET = 'set';
   var ACTION_ALL = 'all';
   var ACTION_VALIDATE = 'validate';
   var ACTION_NEWSITEMAP = 'new_sitemap';
   
   
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
   
      /**
    * Sends the favorite list to the server to save it.
    *
    * @param {Object} callback the function to be called after the server has replied
    */
   var saveFavorites = cms.data.saveFavorites = function(callback) {
      cms.data.sitemapPostJSON(ACTION_SET, {
         'fav': cms.sitemap.favorites
      }, callback);
   }
   
      /**
    * Requests the favorite list from the server and stores it in cms.sitemap.favorites.
    *
    * @param {Object} callback the function to be called after the favorite list has been loaded
    */
   var loadFavorites = cms.data.loadFavorites = function(callback) {
      cms.data.sitemapPostJSON(ACTION_GET, {
         'fav': true
      }, function(ok, data) {
         if (!ok) {
            return;
         }
         cms.sitemap.favorites = data.favorites;
         callback(ok, data);
      })
   }
   
   /**
    * Requests the recent list from the server and stores it in cms.sitemap.recent.
    *
    * @param {Object} callback the function to be called after the recent list has been loaded
    */
   var loadRecent = cms.data.loadRecent = function(callback) {
      cms.data.sitemapPostJSON(ACTION_GET, {
         'rec': true
      }, function(ok, data) {
         if (!ok) {
            return;
         }
         cms.sitemap.recent = data.recent;
         callback(ok, data)
      })
   }
   
   /**
    * Sends the favorite list to the server to save it.
    *
    * @param {Object} callback the function to be called after the server has replied
    */
   var saveFavorites = cms.data.saveFavorites = function(callback) {
      var _removeContent = function(item) {
          delete item['content'];
          if (item.children) {
              for (var child in item.children) {
                  _removeContent(child);
              }
          }
          return item;
      }
      cms.data.sitemapPostJSON(ACTION_SET, {
         'fav': $.map(cms.sitemap.favorites, _removeContent)
      }, callback);
   }
   
   /**
    * Sends the recent list to the server to save it.
    *
    * @param {Object} callback the function to be called after the server has replied
    */
   var saveRecent = cms.data.saveRecent = function(callback) {
      cms.data.sitemapPostJSON(ACTION_SET, {
         'rec': cms.sitemap.recent
      }, callback);
   }
   
    /**
    * Loads the sitemap and continues to initialize the sitemap editor after loading is finished.
    *
    */
   var loadAndInitSitemap = cms.data.loadAndInitSitemap = function() {
      cms.sitemap.setWaitOverlayVisible(true);
      cms.data.sitemapPostJSON(ACTION_ALL, {}, cms.sitemap.onLoadSitemap)
   }
   
   /**
    * AJAX call that sends the sitemap to the server to save it.
    * @param {Object} sitemap the sitemap
    * @param {Object} callback the callback that should be called after the server sends its response
    */
   var saveSitemap = cms.data.saveSitemap = function(sitemap, callback) {
      cms.data.sitemapPostJSON(ACTION_SAVE, {
         'sitemap': sitemap
      }, callback);
   }
   
   
   var createSitemap = cms.data.createSitemap = function(sitemap, title, callback) {
       cms.data.sitemapPostJSON(ACTION_NEWSITEMAP, {
           'sitemap': sitemap,
           'title':   title
       }, callback);
   }
   
   
   /**
    * Sends a url name candidate to the server for translation, then calls a callback with the translated name.
    * @param {Object} name the URL name to be translated
    * @param {Object} callback the callback which should be called after translation
    */
   var convertUrlName = cms.data.convertUrlName = function(name, callback) {
      cms.data.sitemapPostJSON(ACTION_VALIDATE, {
         'name': name
      }, function(ok, data) {
         if (!ok) {
            return;
         }
         // also replace slashes with underscores
         callback(data.name.replace(/\//g, '_'));
      });
   }

   /**
    * Gets the property settings for sitemaps
    * @param {Object} callback
    */
   var getSitemapProperties = cms.data.getSitemapProperties = function(callback) {
      cms.data.sitemapPostJSON('props', {}, callback)
   }
   
   var addContent = cms.data.addContent = function(entries, callback) {
       cms.data.sitemapPostJSON('content', {
           entries: entries
       },
       callback);
   }
   
   var createEntry = cms.data.createEntry = function(type, callback) {
       cms.data.sitemapPostJSON('new_entry', {
           'type': type
       }, callback);
   }
   
   var createEntryByModel = cms.data.createEntryByModel = function(id, callback) {
       cms.data.sitemapPostJSON('new_entry_model', {'model': id}, callback)
   }
      
})(cms);

