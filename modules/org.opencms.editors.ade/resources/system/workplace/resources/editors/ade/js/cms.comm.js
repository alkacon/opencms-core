﻿(function(cms) {
    
   /** Timeout in ms for ajax requests. */
   var /** long */ AJAX_TIMEOUT = 20000;
   
   /** Generic error message for json parse errors. */
   var /** String */ JSON_PARSE_ERROR = cms.messages.JSON_PARSE_ERROR;
   
   /** Generic error message for ajax post errors. */
   var /** String */ AJAX_SENT_ERROR = cms.messages.AJAX_SENT_ERROR;
   
   /** Keeping track of requests to be able to abort them. */
   var /** Array<XMLHttpRequest> */ requests = [];
   
   /**
    * The request did finish, so dismiss it.<p>
    *
    * @param {XMLHttpRequest} xhr the request object
    */
   var _removeRequest = /** void */ function(/** XMLHttpRequest */ xhr) {
       var pos = requests.indexOf(xhr);
       if (pos >= 0) {
           requests.splice(pos, 1);
       }       
   };
   
   /**
    * Aborts all pending requests.<p>
    */
   var abortAllRequests = cms.data.abortAllRequests = /** void */ function() {
       for (i = 0, l = requests.length; i < l; i++) {
           try {
               requests[i].abort();
           } catch(e) {
               // ignore
           }
       }
   };
   
   /**
    * Generic function for posting JSON data to the server.
    *
    * @param {String} url the URL to send the data to
    * @param {Object} data the JSON data
    * @param {Function} afterPost the callback that should be called after the server replied
    * @param {boolean} async optional flag to indicate is the request should synchronized or not, by default it is not
    * @param {int} timeout optional timeout in millisecs, default is #AJAX_TIMEOUT
    * @param {Function} revive optional function to convert JSON to objects when parsing the JSON response
    * 
    * @return the XMLHttpRequest
    */
   var postJSON = cms.comm.postJSON = /** void */ function(/** String */url, /** Object */ data, /** void Function(boolean, Object) */ afterPost, /** boolean */ sync, /** int */ timeout, /** Function */ revive) {
   
      var async = !sync;
      if (!timeout) {
          timeout = AJAX_TIMEOUT;
      }
      var xhr = $.ajax({
         'url': url,
         'data': data,
         'type': 'POST',
         'timeout': timeout,
         'async': async,
         'error': function(xhr, status, error) {
             _removeRequest(xhr);
            if (cms.toolbar && cms.toolbar.leavingPage) {
               return;
            }
            alert(AJAX_SENT_ERROR);
            afterPost(false);
         },
         'success': function(data) {
             _removeRequest(xhr);
            try {
               var jsonData = JSON.parse(data, revive);
            } catch (e) {
               alert(JSON_PARSE_ERROR);
               afterPost(false, {});
               return;
            }
            if (jsonData.state == 'error') {
               alert(jsonData.error);
               afterPost(false, jsonData);
               return;
            }
            afterPost(true, jsonData);
         }
      });
      requests.push(xhr);
      
      return xhr;
   }
})(cms);

