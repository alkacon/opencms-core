function resources(){
  var $wnd_0 = window, $doc_0 = document, $stats = $wnd_0.__gwtStatsEvent?function(a){
    return $wnd_0.__gwtStatsEvent(a);
  }
  :null, $sessionId_0 = $wnd_0.__gwtStatsSessionId?$wnd_0.__gwtStatsSessionId:null, scriptsDone, loadDone, bodyDone, base = '', metaProps = {}, values = [], providers = [], answers = [], softPermutationId = 0, onLoadErrorFunc, propertyErrorFunc;
  $stats && $stats({moduleName:'resources', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'begin'});
  if (!$wnd_0.__gwt_stylesLoaded) {
    $wnd_0.__gwt_stylesLoaded = {};
  }
  if (!$wnd_0.__gwt_scriptsLoaded) {
    $wnd_0.__gwt_scriptsLoaded = {};
  }
  function isHostedMode(){
    var result = false;
    try {
      var query = $wnd_0.location.search;
      return (query.indexOf('gwt.codesvr=') != -1 || (query.indexOf('gwt.hosted=') != -1 || $wnd_0.external && $wnd_0.external.gwtOnLoad)) && query.indexOf('gwt.hybrid') == -1;
    }
     catch (e) {
    }
    isHostedMode = function(){
      return result;
    }
    ;
    return result;
  }

  function maybeStartModule(){
    if (scriptsDone && loadDone) {
      var iframe = $doc_0.getElementById('resources');
      var frameWnd = iframe.contentWindow;
      if (isHostedMode()) {
        frameWnd.__gwt_getProperty = function(name_0){
          return computePropValue(name_0);
        }
        ;
      }
      resources = null;
      frameWnd.gwtOnLoad(onLoadErrorFunc, 'resources', base, softPermutationId);
      $stats && $stats({moduleName:'resources', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'moduleStartup', millis:(new Date).getTime(), type:'end'});
    }
  }

  function computeScriptBase(){
    function getDirectoryOfFile(path){
      var hashIndex = path.lastIndexOf('#');
      if (hashIndex == -1) {
        hashIndex = path.length;
      }
      var queryIndex = path.indexOf('?');
      if (queryIndex == -1) {
        queryIndex = path.length;
      }
      var slashIndex = path.lastIndexOf('/', Math.min(queryIndex, hashIndex));
      return slashIndex >= 0?path.substring(0, slashIndex + 1):'';
    }

    function ensureAbsoluteUrl(url){
      if (url.match(/^\w+:\/\//)) {
      }
       else {
        var img = $doc_0.createElement('img');
        img.src = url + 'clear.cache.gif';
        url = getDirectoryOfFile(img.src);
      }
      return url;
    }

    function tryMetaTag(){
      var metaVal = __gwt_getMetaProperty('baseUrl');
      if (metaVal != null) {
        return metaVal;
      }
      return '';
    }

    function tryNocacheJsTag(){
      var scriptTags = $doc_0.getElementsByTagName('script');
      for (var i = 0; i < scriptTags.length; ++i) {
        if (scriptTags[i].src.indexOf('resources.nocache.js') != -1) {
          return getDirectoryOfFile(scriptTags[i].src);
        }
      }
      return '';
    }

    function tryMarkerScript(){
      var thisScript;
      if (typeof isBodyLoaded == 'undefined' || !isBodyLoaded()) {
        var markerId = '__gwt_marker_resources';
        var markerScript;
        $doc_0.write('<script id="' + markerId + '"><\/script>');
        markerScript = $doc_0.getElementById(markerId);
        thisScript = markerScript && markerScript.previousSibling;
        while (thisScript && thisScript.tagName != 'SCRIPT') {
          thisScript = thisScript.previousSibling;
        }
        if (markerScript) {
          markerScript.parentNode.removeChild(markerScript);
        }
        if (thisScript && thisScript.src) {
          return getDirectoryOfFile(thisScript.src);
        }
      }
      return '';
    }

    function tryBaseTag(){
      var baseElements = $doc_0.getElementsByTagName('base');
      if (baseElements.length > 0) {
        return baseElements[baseElements.length - 1].href;
      }
      return '';
    }

    var tempBase = tryMetaTag();
    if (tempBase == '') {
      tempBase = tryNocacheJsTag();
    }
    if (tempBase == '') {
      tempBase = tryMarkerScript();
    }
    if (tempBase == '') {
      tempBase = tryBaseTag();
    }
    if (tempBase == '') {
      tempBase = getDirectoryOfFile($doc_0.location.href);
    }
    tempBase = ensureAbsoluteUrl(tempBase);
    base = tempBase;
    return tempBase;
  }

  function processMetas(){
    var metas = document.getElementsByTagName('meta');
    for (var i = 0, n = metas.length; i < n; ++i) {
      var meta = metas[i], name_0 = meta.getAttribute('name'), content_0;
      if (name_0) {
        name_0 = name_0.replace('resources::', '');
        if (name_0.indexOf('::') >= 0) {
          continue;
        }
        if (name_0 == 'gwt:property') {
          content_0 = meta.getAttribute('content');
          if (content_0) {
            var value, eq = content_0.indexOf('=');
            if (eq >= 0) {
              name_0 = content_0.substring(0, eq);
              value = content_0.substring(eq + 1);
            }
             else {
              name_0 = content_0;
              value = '';
            }
            metaProps[name_0] = value;
          }
        }
         else if (name_0 == 'gwt:onPropertyErrorFn') {
          content_0 = meta.getAttribute('content');
          if (content_0) {
            try {
              propertyErrorFunc = eval(content_0);
            }
             catch (e) {
              alert('Bad handler "' + content_0 + '" for "gwt:onPropertyErrorFn"');
            }
          }
        }
         else if (name_0 == 'gwt:onLoadErrorFn') {
          content_0 = meta.getAttribute('content');
          if (content_0) {
            try {
              onLoadErrorFunc = eval(content_0);
            }
             catch (e) {
              alert('Bad handler "' + content_0 + '" for "gwt:onLoadErrorFn"');
            }
          }
        }
      }
    }
  }

  function __gwt_isKnownPropertyValue(propName, propValue){
    return propValue in values[propName];
  }

  function __gwt_getMetaProperty(name_0){
    var value = metaProps[name_0];
    return value == null?null:value;
  }

  function unflattenKeylistIntoAnswers(propValArray, value){
    var answer = answers;
    for (var i = 0, n = propValArray.length - 1; i < n; ++i) {
      answer = answer[propValArray[i]] || (answer[propValArray[i]] = []);
    }
    answer[propValArray[n]] = value;
  }

  function computePropValue(propName){
    var value = providers[propName](), allowedValuesMap = values[propName];
    if (value in allowedValuesMap) {
      return value;
    }
    var allowedValuesList = [];
    for (var k in allowedValuesMap) {
      allowedValuesList[allowedValuesMap[k]] = k;
    }
    if (propertyErrorFunc) {
      propertyErrorFunc(propName, allowedValuesList, value);
    }
    throw null;
  }

  var frameInjected;
  function maybeInjectFrame(){
    if (!frameInjected) {
      frameInjected = true;
      var iframe = $doc_0.createElement('iframe');
      iframe.src = "javascript:''";
      iframe.id = 'resources';
      iframe.style.cssText = 'position:absolute;width:0;height:0;border:none';
      iframe.tabIndex = -1;
      $doc_0.body.appendChild(iframe);
      $stats && $stats({moduleName:'resources', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'moduleStartup', millis:(new Date).getTime(), type:'moduleRequested'});
      iframe.contentWindow.location.replace(base + initialHtml);
    }
  }

  providers['fileapi.support'] = function(){
    var input = document.createElement('input');
    input.setAttribute('type', 'file');
    if (input.files != null) {
      if (typeof FileReader == 'function' || typeof FileReader == 'object') {
        if (typeof FormData == 'function' || typeof FormData == 'object') {
          return 'formdata';
        }
        return 'fileapi';
      }
    }
    return 'no';
  }
  ;
  values['fileapi.support'] = {fileapi:0, formdata:1, no:2};
  providers['locale'] = function(){
    var locale = null;
    var rtlocale = 'en';
    try {
      if (!locale) {
        var queryParam = location.search;
        var qpStart = queryParam.indexOf('locale=');
        if (qpStart >= 0) {
          var value = queryParam.substring(qpStart + 7);
          var end = queryParam.indexOf('&', qpStart);
          if (end < 0) {
            end = queryParam.length;
          }
          locale = queryParam.substring(qpStart + 7, end);
        }
      }
      if (!locale) {
        locale = __gwt_getMetaProperty('locale');
      }
      if (!locale) {
        locale = $wnd_0['__gwt_Locale'];
      }
      if (locale) {
        rtlocale = locale;
      }
      while (locale && !__gwt_isKnownPropertyValue('locale', locale)) {
        var lastIndex = locale.lastIndexOf('_');
        if (lastIndex < 0) {
          locale = null;
          break;
        }
        locale = locale.substring(0, lastIndex);
      }
    }
     catch (e) {
      alert('Unexpected exception in locale detection, using default: ' + e);
    }
    $wnd_0['__gwt_Locale'] = rtlocale;
    return locale || 'en';
  }
  ;
  values['locale'] = {de:0, 'default':1, en:2};
  providers['user.agent'] = function(){
    var ua = navigator.userAgent.toLowerCase();
    var makeVersion = function(result){
      return parseInt(result[1]) * 1000 + parseInt(result[2]);
    }
    ;
    if (ua.indexOf('opera') != -1) {
      return 'opera';
    }
     else if (ua.indexOf('webkit') != -1) {
      return 'safari';
    }
     else if (ua.indexOf('msie') != -1) {
      if (document.documentMode >= 8) {
        return 'ie8';
      }
       else {
        var result_0 = /msie ([0-9]+)\.([0-9]+)/.exec(ua);
        if (result_0 && result_0.length == 3) {
          var v = makeVersion(result_0);
          if (v >= 6000) {
            return 'ie6';
          }
        }
      }
    }
     else if (ua.indexOf('gecko') != -1) {
      return 'gecko1_8';
    }
    return 'unknown';
  }
  ;
  values['user.agent'] = {gecko1_8:0, ie6:1, ie8:2, opera:3, safari:4};
  resources.onScriptLoad = function(){
    if (frameInjected) {
      loadDone = true;
      maybeStartModule();
    }
  }
  ;
  resources.onInjectionDone = function(){
    scriptsDone = true;
    $stats && $stats({moduleName:'resources', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'loadExternalRefs', millis:(new Date).getTime(), type:'end'});
    maybeStartModule();
  }
  ;
  processMetas();
  computeScriptBase();
  var strongName;
  var initialHtml;
  if (isHostedMode()) {
    if ($wnd_0.external && ($wnd_0.external.initModule && $wnd_0.external.initModule('resources'))) {
      $wnd_0.location.reload();
      return;
    }
    initialHtml = 'hosted.html?resources';
    strongName = '';
  }
  $stats && $stats({moduleName:'resources', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'selectingPermutation'});
  if (!isHostedMode()) {
    try {
      unflattenKeylistIntoAnswers(['fileapi', 'de', 'gecko1_8'], '032F8EBE81ADCBCD8333F1033A1914DD');
      unflattenKeylistIntoAnswers(['formdata', 'de', 'gecko1_8'], '032F8EBE81ADCBCD8333F1033A1914DD');
      unflattenKeylistIntoAnswers(['no', 'de', 'gecko1_8'], '032F8EBE81ADCBCD8333F1033A1914DD');
      unflattenKeylistIntoAnswers(['fileapi', 'en', 'gecko1_8'], '032F8EBE81ADCBCD8333F1033A1914DD' + ':1');
      unflattenKeylistIntoAnswers(['formdata', 'en', 'gecko1_8'], '032F8EBE81ADCBCD8333F1033A1914DD' + ':1');
      unflattenKeylistIntoAnswers(['no', 'en', 'gecko1_8'], '032F8EBE81ADCBCD8333F1033A1914DD' + ':1');
      unflattenKeylistIntoAnswers(['fileapi', 'de', 'gecko1_8'], '032F8EBE81ADCBCD8333F1033A1914DD' + ':2');
      unflattenKeylistIntoAnswers(['formdata', 'de', 'gecko1_8'], '032F8EBE81ADCBCD8333F1033A1914DD' + ':2');
      unflattenKeylistIntoAnswers(['no', 'de', 'gecko1_8'], '032F8EBE81ADCBCD8333F1033A1914DD' + ':2');
      unflattenKeylistIntoAnswers(['fileapi', 'en', 'gecko1_8'], '032F8EBE81ADCBCD8333F1033A1914DD' + ':3');
      unflattenKeylistIntoAnswers(['formdata', 'en', 'gecko1_8'], '032F8EBE81ADCBCD8333F1033A1914DD' + ':3');
      unflattenKeylistIntoAnswers(['no', 'en', 'gecko1_8'], '032F8EBE81ADCBCD8333F1033A1914DD' + ':3');
      unflattenKeylistIntoAnswers(['fileapi', 'de', 'gecko1_8'], '032F8EBE81ADCBCD8333F1033A1914DD' + ':4');
      unflattenKeylistIntoAnswers(['formdata', 'de', 'gecko1_8'], '032F8EBE81ADCBCD8333F1033A1914DD' + ':4');
      unflattenKeylistIntoAnswers(['no', 'de', 'gecko1_8'], '032F8EBE81ADCBCD8333F1033A1914DD' + ':4');
      unflattenKeylistIntoAnswers(['fileapi', 'en', 'gecko1_8'], '032F8EBE81ADCBCD8333F1033A1914DD' + ':5');
      unflattenKeylistIntoAnswers(['formdata', 'en', 'gecko1_8'], '032F8EBE81ADCBCD8333F1033A1914DD' + ':5');
      unflattenKeylistIntoAnswers(['no', 'en', 'gecko1_8'], '032F8EBE81ADCBCD8333F1033A1914DD' + ':5');
      unflattenKeylistIntoAnswers(['no', 'de', 'opera'], '226A5A48F01DF9AEF95C5CC3DE0B6EA6');
      unflattenKeylistIntoAnswers(['no', 'en', 'opera'], '226A5A48F01DF9AEF95C5CC3DE0B6EA6' + ':1');
      unflattenKeylistIntoAnswers(['formdata', 'de', 'safari'], '659FB2AFD91A772C83E8FC071C74821D');
      unflattenKeylistIntoAnswers(['no', 'de', 'safari'], '659FB2AFD91A772C83E8FC071C74821D');
      unflattenKeylistIntoAnswers(['formdata', 'en', 'safari'], '659FB2AFD91A772C83E8FC071C74821D' + ':1');
      unflattenKeylistIntoAnswers(['no', 'en', 'safari'], '659FB2AFD91A772C83E8FC071C74821D' + ':1');
      unflattenKeylistIntoAnswers(['formdata', 'de', 'safari'], '659FB2AFD91A772C83E8FC071C74821D' + ':2');
      unflattenKeylistIntoAnswers(['no', 'de', 'safari'], '659FB2AFD91A772C83E8FC071C74821D' + ':2');
      unflattenKeylistIntoAnswers(['formdata', 'en', 'safari'], '659FB2AFD91A772C83E8FC071C74821D' + ':3');
      unflattenKeylistIntoAnswers(['no', 'en', 'safari'], '659FB2AFD91A772C83E8FC071C74821D' + ':3');
      unflattenKeylistIntoAnswers(['no', 'de', 'ie8'], '90B6FA1195BA241C9ACD2C34813DFA00');
      unflattenKeylistIntoAnswers(['no', 'en', 'ie8'], '90B6FA1195BA241C9ACD2C34813DFA00' + ':1');
      unflattenKeylistIntoAnswers(['no', 'de', 'ie6'], 'AB9E33F80B6B31CB257534EF974D9D10');
      unflattenKeylistIntoAnswers(['no', 'en', 'ie6'], 'AB9E33F80B6B31CB257534EF974D9D10' + ':1');
      strongName = answers[computePropValue('fileapi.support')][computePropValue('locale')][computePropValue('user.agent')];
      var idx = strongName.indexOf(':');
      if (idx != -1) {
        softPermutationId = Number(strongName.substring(idx + 1));
        strongName = strongName.substring(0, idx);
      }
      initialHtml = strongName + '.cache.html';
    }
     catch (e) {
      return;
    }
  }
  var onBodyDoneTimerId;
  function onBodyDone(){
    if (!bodyDone) {
      bodyDone = true;
      maybeStartModule();
      if ($doc_0.removeEventListener) {
        $doc_0.removeEventListener('DOMContentLoaded', onBodyDone, false);
      }
      if (onBodyDoneTimerId) {
        clearInterval(onBodyDoneTimerId);
      }
    }
  }

  if ($doc_0.addEventListener) {
    $doc_0.addEventListener('DOMContentLoaded', function(){
      maybeInjectFrame();
      onBodyDone();
    }
    , false);
  }
  var onBodyDoneTimerId = setInterval(function(){
    if (/loaded|complete/.test($doc_0.readyState)) {
      maybeInjectFrame();
      onBodyDone();
    }
  }
  , 50);
  $stats && $stats({moduleName:'resources', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'end'});
  $stats && $stats({moduleName:'resources', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'loadExternalRefs', millis:(new Date).getTime(), type:'begin'});
  $doc_0.write('<script defer="defer">resources.onInjectionDone(\'resources\')<\/script>');
}

resources();
