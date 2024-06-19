/*! typograf | © 2020 Denis Seleznev | MIT  License | https://github.com/typograf/typograf */
(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory() :
	typeof define === 'function' && define.amd ? define(factory) :
	(global.Typograf = factory());
}(this, (function () { 'use strict';

function _typeof(obj) {
  if (typeof Symbol === "function" && typeof Symbol.iterator === "symbol") {
    _typeof = function (obj) {
      return typeof obj;
    };
  } else {
    _typeof = function (obj) {
      return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj;
    };
  }

  return _typeof(obj);
}

function _classCallCheck(instance, Constructor) {
  if (!(instance instanceof Constructor)) {
    throw new TypeError("Cannot call a class as a function");
  }
}

function _defineProperties(target, props) {
  for (var i = 0; i < props.length; i++) {
    var descriptor = props[i];
    descriptor.enumerable = descriptor.enumerable || false;
    descriptor.configurable = true;
    if ("value" in descriptor) descriptor.writable = true;
    Object.defineProperty(target, descriptor.key, descriptor);
  }
}

function _createClass(Constructor, protoProps, staticProps) {
  if (protoProps) _defineProperties(Constructor.prototype, protoProps);
  if (staticProps) _defineProperties(Constructor, staticProps);
  return Constructor;
}

function _slicedToArray(arr, i) {
  return _arrayWithHoles(arr) || _iterableToArrayLimit(arr, i) || _nonIterableRest();
}

function _arrayWithHoles(arr) {
  if (Array.isArray(arr)) return arr;
}

function _iterableToArrayLimit(arr, i) {
  if (!(Symbol.iterator in Object(arr) || Object.prototype.toString.call(arr) === "[object Arguments]")) {
    return;
  }

  var _arr = [];
  var _n = true;
  var _d = false;
  var _e = undefined;

  try {
    for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) {
      _arr.push(_s.value);

      if (i && _arr.length === i) break;
    }
  } catch (err) {
    _d = true;
    _e = err;
  } finally {
    try {
      if (!_n && _i["return"] != null) _i["return"]();
    } finally {
      if (_d) throw _e;
    }
  }

  return _arr;
}

function _nonIterableRest() {
  throw new TypeError("Invalid attempt to destructure non-iterable instance");
}

var groupIndexes = {
  symbols: 110,
  space: 210,
  dash: 310,
  punctuation: 410,
  nbsp: 510,
  'number': 610,
  money: 710,
  date: 810,
  other: 910,
  optalign: 1010,
  typo: 1110,
  html: 1210
};

// http://www.w3.org/TR/html4/sgml/entities
var visibleEntities = [['iexcl', 161], ['cent', 162], ['pound', 163], ['curren', 164], ['yen', 165], ['brvbar', 166], ['sect', 167], ['uml', 168], ['copy', 169], ['ordf', 170], ['laquo', 171], ['not', 172], ['reg', 174], ['macr', 175], ['deg', 176], ['plusmn', 177], ['sup2', 178], ['sup3', 179], ['acute', 180], ['micro', 181], ['para', 182], ['middot', 183], ['cedil', 184], ['sup1', 185], ['ordm', 186], ['raquo', 187], ['frac14', 188], ['frac12', 189], ['frac34', 190], ['iquest', 191], ['Agrave', 192], ['Aacute', 193], ['Acirc', 194], ['Atilde', 195], ['Auml', 196], ['Aring', 197], ['AElig', 198], ['Ccedil', 199], ['Egrave', 200], ['Eacute', 201], ['Ecirc', 202], ['Euml', 203], ['Igrave', 204], ['Iacute', 205], ['Icirc', 206], ['Iuml', 207], ['ETH', 208], ['Ntilde', 209], ['Ograve', 210], ['Oacute', 211], ['Ocirc', 212], ['Otilde', 213], ['Ouml', 214], ['times', 215], ['Oslash', 216], ['Ugrave', 217], ['Uacute', 218], ['Ucirc', 219], ['Uuml', 220], ['Yacute', 221], ['THORN', 222], ['szlig', 223], ['agrave', 224], ['aacute', 225], ['acirc', 226], ['atilde', 227], ['auml', 228], ['aring', 229], ['aelig', 230], ['ccedil', 231], ['egrave', 232], ['eacute', 233], ['ecirc', 234], ['euml', 235], ['igrave', 236], ['iacute', 237], ['icirc', 238], ['iuml', 239], ['eth', 240], ['ntilde', 241], ['ograve', 242], ['oacute', 243], ['ocirc', 244], ['otilde', 245], ['ouml', 246], ['divide', 247], ['oslash', 248], ['ugrave', 249], ['uacute', 250], ['ucirc', 251], ['uuml', 252], ['yacute', 253], ['thorn', 254], ['yuml', 255], ['fnof', 402], ['Alpha', 913], ['Beta', 914], ['Gamma', 915], ['Delta', 916], ['Epsilon', 917], ['Zeta', 918], ['Eta', 919], ['Theta', 920], ['Iota', 921], ['Kappa', 922], ['Lambda', 923], ['Mu', 924], ['Nu', 925], ['Xi', 926], ['Omicron', 927], ['Pi', 928], ['Rho', 929], ['Sigma', 931], ['Tau', 932], ['Upsilon', 933], ['Phi', 934], ['Chi', 935], ['Psi', 936], ['Omega', 937], ['alpha', 945], ['beta', 946], ['gamma', 947], ['delta', 948], ['epsilon', 949], ['zeta', 950], ['eta', 951], ['theta', 952], ['iota', 953], ['kappa', 954], ['lambda', 955], ['mu', 956], ['nu', 957], ['xi', 958], ['omicron', 959], ['pi', 960], ['rho', 961], ['sigmaf', 962], ['sigma', 963], ['tau', 964], ['upsilon', 965], ['phi', 966], ['chi', 967], ['psi', 968], ['omega', 969], ['thetasym', 977], ['upsih', 978], ['piv', 982], ['bull', 8226], ['hellip', 8230], ['prime', 8242], ['Prime', 8243], ['oline', 8254], ['frasl', 8260], ['weierp', 8472], ['image', 8465], ['real', 8476], ['trade', 8482], ['alefsym', 8501], ['larr', 8592], ['uarr', 8593], ['rarr', 8594], ['darr', 8595], ['harr', 8596], ['crarr', 8629], ['lArr', 8656], ['uArr', 8657], ['rArr', 8658], ['dArr', 8659], ['hArr', 8660], ['forall', 8704], ['part', 8706], ['exist', 8707], ['empty', 8709], ['nabla', 8711], ['isin', 8712], ['notin', 8713], ['ni', 8715], ['prod', 8719], ['sum', 8721], ['minus', 8722], ['lowast', 8727], ['radic', 8730], ['prop', 8733], ['infin', 8734], ['ang', 8736], ['and', 8743], ['or', 8744], ['cap', 8745], ['cup', 8746], ['int', 8747], ['there4', 8756], ['sim', 8764], ['cong', 8773], ['asymp', 8776], ['ne', 8800], ['equiv', 8801], ['le', 8804], ['ge', 8805], ['sub', 8834], ['sup', 8835], ['nsub', 8836], ['sube', 8838], ['supe', 8839], ['oplus', 8853], ['otimes', 8855], ['perp', 8869], ['sdot', 8901], ['lceil', 8968], ['rceil', 8969], ['lfloor', 8970], ['rfloor', 8971], ['lang', 9001], ['rang', 9002], ['spades', 9824], ['clubs', 9827], ['hearts', 9829], ['diams', 9830], ['loz', 9674], ['OElig', 338], ['oelig', 339], ['Scaron', 352], ['scaron', 353], ['Yuml', 376], ['circ', 710], ['tilde', 732], ['ndash', 8211], ['mdash', 8212], ['lsquo', 8216], ['rsquo', 8217], ['sbquo', 8218], ['ldquo', 8220], ['rdquo', 8221], ['bdquo', 8222], ['dagger', 8224], ['Dagger', 8225], ['permil', 8240], ['lsaquo', 8249], ['rsaquo', 8250], ['euro', 8364], ['NestedGreaterGreater', 8811], ['NestedLessLess', 8810]];

var invisibleEntities = [['nbsp', 160], ['thinsp', 8201], ['ensp', 8194], ['emsp', 8195], ['shy', 173], ['zwnj', 8204], ['zwj', 8205], ['lrm', 8206], ['rlm', 8207]];

var HtmlEntities =
/*#__PURE__*/
function () {
  function HtmlEntities() {
    _classCallCheck(this, HtmlEntities);

    this._entities = this._prepareEntities([].concat(visibleEntities, invisibleEntities));
    this._entitiesByName = {};
    this._entitiesByNameEntity = {};
    this._entitiesByDigitEntity = {};
    this._entitiesByUtf = {};

    this._entities.forEach(function (entity) {
      this._entitiesByName[entity.name] = entity;
      this._entitiesByNameEntity[entity.nameEntity] = entity;
      this._entitiesByDigitEntity[entity.digitEntity] = entity;
      this._entitiesByUtf[entity.utf] = entity;
    }, this);

    this._invisibleEntities = this._prepareEntities(invisibleEntities);
  }
  /**
   * Entities as name or digit to UTF-8.
   *
   * @param {Object} context
   */


  _createClass(HtmlEntities, [{
    key: "toUtf",
    value: function toUtf(context) {
      var _this = this;

      if (context.text.search(/&#/) !== -1) {
        context.text = this.decHexToUtf(context.text);
      }

      if (context.text.search(/&[a-z]/i) !== -1) {
        // 2 - min length of entity without & and ;. Example: &DD;
        // 31 - max length of entity without & and ;. Example: &CounterClockwiseContourIntegral;
        context.text = context.text.replace(/&[a-z\d]{2,31};/gi, function (key) {
          var entity = _this._entitiesByNameEntity[key];
          return entity ? entity.utf : key;
        });
      }
    }
    /**
     * Entities in decimal or hexadecimal form to UTF-8.
     *
     * @param {string} text
     * @returns {string}
     */

  }, {
    key: "decHexToUtf",
    value: function decHexToUtf(text) {
      return text.replace(/&#(\d{1,6});/gi, function ($0, $1) {
        return String.fromCharCode(parseInt($1, 10));
      }).replace(/&#x([\da-f]{1,6});/gi, function ($0, $1) {
        return String.fromCharCode(parseInt($1, 16));
      });
    }
    /**
     * Restore HTML entities in text.
     *
     * @param {Object} context
     */

  }, {
    key: "restore",
    value: function restore(context) {
      var params = context.prefs.htmlEntity;
      var type = params.type;
      var entities = this._entities;

      if (type === 'name' || type === 'digit') {
        if (params.onlyInvisible || params.list) {
          entities = [];

          if (params.onlyInvisible) {
            entities = entities.concat(this._invisibleEntities);
          }

          if (params.list) {
            entities = entities.concat(this._prepareListParam(params.list));
          }
        }

        context.text = this._restoreEntitiesByIndex(context.text, type + 'Entity', entities);
      }
    }
    /**
     * Get a entity by utf using the type.
     *
     * @param {string} symbol
     * @param {string} [type]
     * @returns {string}
     */

  }, {
    key: "getByUtf",
    value: function getByUtf(symbol, type) {
      var result = '';

      switch (type) {
        case 'digit':
          result = this._entitiesByDigitEntity[symbol];
          break;

        case 'name':
          result = this._entitiesByNameEntity[symbol];
          break;

        default:
          result = symbol;
          break;
      }

      return result;
    }
  }, {
    key: "_prepareEntities",
    value: function _prepareEntities(entities) {
      var result = [];
      entities.forEach(function (entity) {
        var _entity = _slicedToArray(entity, 2),
            name = _entity[0],
            digit = _entity[1];

        var utf = String.fromCharCode(digit);
        result.push({
          name: name,
          nameEntity: '&' + name + ';',
          // &nbsp;
          digitEntity: '&#' + digit + ';',
          // &#160;
          utf: utf,
          // \u00A0
          reName: new RegExp('&' + name + ';', 'g'),
          reUtf: new RegExp(utf, 'g')
        });
      }, this);
      return result;
    }
  }, {
    key: "_prepareListParam",
    value: function _prepareListParam(list) {
      var result = [];
      list.forEach(function (name) {
        var entity = this._entitiesByName[name];

        if (entity) {
          result.push(entity);
        }
      }, this);
      return result;
    }
  }, {
    key: "_restoreEntitiesByIndex",
    value: function _restoreEntitiesByIndex(text, type, entities) {
      entities.forEach(function (entity) {
        text = text.replace(entity.reUtf, entity[type]);
      });
      return text;
    }
  }]);

  return HtmlEntities;
}();

var HtmlEntities$1 = new HtmlEntities();
/**
 * @typedef HtmlEntity
 *
 * @property {string} type - 'default' - UTF-8, 'digit' - &#160;, 'name' - &nbsp;
 * @property {boolean} [onlyInvisible]
 * @property {string[]} [list]
 */

var locales = [];
/**
 * Add a locale.
 *
 * @param {string} locale
 */

function addLocale(locale) {
  var code = (locale || '').split('/')[0];

  if (code && code !== 'common' && !hasLocale(code)) {
    locales.push(code);
    locales.sort();
  }
}
/**
 * Get locales.
 *
 * @returns {Array}
 */

function getLocales() {
  return locales;
}
/**
 * Has a locale.
*
 * @param {string} locale
 *
 * @returns {boolean}
 */

function hasLocale(locale) {
  return locale === 'common' || locales.indexOf(locale) !== -1;
}
function prepareLocale(locale1, locale2) {
  var locale = locale1 || locale2;
  var result = locale;

  if (!Array.isArray(locale)) {
    result = [locale];
  }

  return result;
}

var data = {};
/**
 * Get data for use in rules.
 *
 * @param {string} key
 *
 * @returns {*}
 */

function getData(key) {
  return data[key];
}
/**
 * Set data for use in rules.
 *
 * @param {string|Object} key
 * @param {*} [value]
 */

function setData(key, value) {
  if (typeof key === 'string') {
    addLocale(key);
    data[key] = value;
  } else if (_typeof(key) === 'object') {
    Object.keys(key).forEach(function (k) {
      addLocale(k);
      data[k] = key[k];
    });
  }
}

var inlineElements = ['a', 'abbr', 'acronym', 'b', 'bdo', 'big', 'br', 'button', 'cite', 'code', 'dfn', 'em', 'i', 'img', 'input', 'kbd', 'label', 'map', 'object', 'q', 'samp', 'script', 'select', 'small', 'span', 'strong', 'sub', 'sup', 'textarea', 'time', 'tt', 'var'];

var regExpUrl = new RegExp('(https?|file|ftp)://([a-zA-Z0-9/+-=%&:_.~?]+[a-zA-Z0-9#+]*)', 'g');

var privateLabel = "\uF000";
var privateSeparateLabel = "\uF001";

var SafeTags =
/*#__PURE__*/
function () {
  function SafeTags() {
    _classCallCheck(this, SafeTags);

    var html = [['<!--', '-->'], ['<!ENTITY', '>'], ['<!DOCTYPE', '>'], ['<\\?xml', '\\?>'], ['<!\\[CDATA\\[', '\\]\\]>']];
    ['code', 'kbd', 'object', 'pre', 'samp', 'script', 'style', 'var'].forEach(function (tag) {
      html.push(['<' + tag + '(\\s[^>]*?)?>', '</' + tag + '>']);
    }, this);
    this._tags = {
      own: [],
      html: html.map(this._prepareRegExp),
      url: [regExpUrl]
    };
    this._groups = ['own', 'html', 'url'];
  }
  /**
   * Add own safe tag.
   *
   * @param {RegExp|string[]} tag
   */


  _createClass(SafeTags, [{
    key: "add",
    value: function add(tag) {
      this._tags.own.push(this._prepareRegExp(tag));
    }
    /**
     * Show safe tags.
     *
     * @param {Object} context
     * @param {string} group
     */

  }, {
    key: "show",
    value: function show(context, group) {
      var reReplace = new RegExp(privateLabel + 'tf\\d+' + privateLabel, 'g');
      var reSearch = new RegExp(privateLabel + 'tf\\d');

      var replaceLabel = function replaceLabel(match) {
        return context.safeTags.hidden[group][match] || match;
      };

      for (var i = 0, len = this._tags[group].length; i < len; i++) {
        context.text = context.text.replace(reReplace, replaceLabel);

        if (context.text.search(reSearch) === -1) {
          break;
        }
      }
    }
    /**
     * Hide safe tags.
     *
     * @param {Object} context
     * @param {string} group
     */

  }, {
    key: "hide",
    value: function hide(context, group) {
      context.safeTags = context.safeTags || {
        hidden: {},
        i: 0
      };
      context.safeTags.hidden[group] = {};

      var pasteLabel = this._pasteLabel.bind(this, context, group);

      this._tags[group].forEach(function (tag) {
        context.text = context.text.replace(this._prepareRegExp(tag), pasteLabel);
      }, this);
    }
    /**
     * Hide HTML tags.
     *
     * @param {Object} context
     */

  }, {
    key: "hideHTMLTags",
    value: function hideHTMLTags(context) {
      if (context.isHTML) {
        var pasteLabel = this._pasteLabel.bind(this, context, 'html');

        context.text = context.text.replace(/<\/?[a-z][^]*?>/gi, pasteLabel) // Tags
        .replace(/&lt;\/?[a-z][^]*?&gt;/gi, pasteLabel) // Escaping tags
        .replace(/&[gl]t;/gi, pasteLabel);
      }
    }
    /**
     * Get previous label.
     *
     * @param {string} text
     * @param {number} position
     *
     * @returns {string|false}
     */

  }, {
    key: "getPrevLabel",
    value: function getPrevLabel(text, position) {
      for (var i = position - 1; i >= 0; i--) {
        if (text[i] === privateLabel) {
          return text.slice(i, position + 1);
        }
      }

      return false;
    }
    /**
     * Get next label.
     *
     * @param {string} text
     * @param {number} position
     *
     * @returns {string|false}
     */

  }, {
    key: "getNextLabel",
    value: function getNextLabel(text, position) {
      for (var i = position + 1; i < text.length; i++) {
        if (text[i] === privateLabel) {
          return text.slice(position, i + 1);
        }
      }

      return false;
    }
    /**
     * Get a tag by a label.
     *
     * @param {Object} context
     * @param {string} label
     *
     * @returns {Object|boolean}
     */

  }, {
    key: "getTagByLabel",
    value: function getTagByLabel(context, label) {
      var result = false;

      this._groups.some(function (group) {
        var value = context.safeTags.hidden[group][label];

        if (typeof value !== 'undefined') {
          result = {
            group: group,
            value: value
          };
        }

        return result;
      });

      return result;
    }
    /**
     * Get info about a tag.
     *
     * @param {Object|undefined} tag
     *
     * @returns {Object|undefined}
     */

  }, {
    key: "getTagInfo",
    value: function getTagInfo(tag) {
      if (!tag) {
        return;
      }

      var result = {
        group: tag.group
      };

      switch (tag.group) {
        case 'html':
          result.name = tag.value.split(/[<\s>]/)[1];
          result.isInline = inlineElements.indexOf(result.name) > -1;
          result.isClosing = tag.value.search(/^<\//) > -1;
          break;

        case 'url':
          result.isInline = true;
          break;

        case 'own':
          result.isInline = false;
          break;
      }

      return result;
    }
  }, {
    key: "_pasteLabel",
    value: function _pasteLabel(context, group, match) {
      var safeTags = context.safeTags;
      var key = privateLabel + 'tf' + safeTags.i + privateLabel;
      safeTags.hidden[group][key] = match;
      safeTags.i++;
      return key;
    }
  }, {
    key: "_prepareRegExp",
    value: function _prepareRegExp(tag) {
      var re;

      if (tag instanceof RegExp) {
        re = tag;
      } else {
        var _tag = _slicedToArray(tag, 3),
            startTag = _tag[0],
            endTag = _tag[1],
            middle = _tag[2];

        if (typeof middle === 'undefined') {
          middle = '[^]*?';
        }

        re = new RegExp(startTag + middle + endTag, 'gi');
      }

      return re;
    }
  }]);

  return SafeTags;
}();

function repeat(symbol, count) {
  var result = '';

  for (;;) {
    if ((count & 1) === 1) {
      result += symbol;
    }

    count >>>= 1;

    if (count === 0) {
      break;
    }

    symbol += symbol;
  }

  return result;
}
function replaceNbsp(text) {
  return text.replace(/\u00A0/g, ' ');
}
function replace(text, re) {
  for (var i = 0; i < re.length; i++) {
    text = text.replace(re[i][0], re[i][1]);
  }

  return text;
}
function isHTML(text) {
  return text.search(/(<\/?[a-z]|<!|&[lg]t;)/i) !== -1;
}
function removeCR(text) {
  return text.replace(/\r\n?/g, '\n');
}
function fixLineEnding(text, type) {
  if (type === 'CRLF') {
    // Windows
    return text.replace(/\n/g, '\r\n');
  } else if (type === 'CR') {
    // Mac
    return text.replace(/\n/g, '\r');
  }

  return text;
}

/**
 * Get a deep copy of a object.
 *
 * @param {*} obj
 *
 * @returns {*}
 */
function deepCopy(obj) {
  return _typeof(obj) === 'object' ? JSON.parse(JSON.stringify(obj)) : obj;
}

/**
 * @constructor
 * @param {Object} [prefs]
 * @param {string} [prefs.locale] Locale
 * @param {string} [prefs.lineEnding] Line ending. 'LF' (Unix), 'CR' (Mac) or 'CRLF' (Windows). Default: 'LF'.
 * @param {HtmlEntity} [prefs.htmlEntity]
 * @param {boolean} [prefs.live] Live mode
 * @param {string|string[]} [prefs.enableRule] Enable a rule
 * @param {string|string[]} [prefs.disableRule] Disable a rule
 */

var Typograf =
/*#__PURE__*/
function () {
  function Typograf(prefs) {
    _classCallCheck(this, Typograf);

    this._prefs = _typeof(prefs) === 'object' ? prefs : {};
    this._prefs.locale = prepareLocale(this._prefs.locale);
    this._prefs.live = this._prefs.live || false;
    this._safeTags = new SafeTags();
    this._settings = {};
    this._enabledRules = {};
    this._innerRulesByQueues = {};
    this._innerRules = [].concat(this._innerRules);

    this._innerRules.forEach(function (rule) {
      var q = rule.queue || 'default';
      this._innerRulesByQueues[q] = this._innerRulesByQueues[q] || [];

      this._innerRulesByQueues[q].push(rule);
    }, this);

    this._rulesByQueues = {};
    this._rules = [].concat(this._rules);

    this._rules.forEach(function (rule) {
      var q = rule.queue || 'default';

      this._prepareRule(rule);

      this._rulesByQueues[q] = this._rulesByQueues[q] || [];

      this._rulesByQueues[q].push(rule);
    }, this);

    this._prefs.disableRule && this.disableRule(this._prefs.disableRule);
    this._prefs.enableRule && this.enableRule(this._prefs.enableRule);
    this._separatePartsTags = ['title', 'p', 'h[1-6]', 'select', 'legend'];
  }
  /**
   * Add a rule.
   *
   * @static
   * @param {TypografRule} rule
   *
   * @returns {Typograf} this
   */


  _createClass(Typograf, [{
    key: "execute",

    /**
     * Execute typographical rules for text.
     *
     * @param {string} text
     * @param {Object} [prefs]
     * @param {string} [prefs.locale] Locale
     * @param {HtmlEntity} [prefs.htmlEntity] Type of HTML entities
     * @param {string} [prefs.lineEnding] Line ending. 'LF' (Unix), 'CR' (Mac) or 'CRLF' (Windows). Default: 'LF'.
     *
     * @returns {string}
     */
    value: function execute(text, prefs) {
      text = '' + text;

      if (!text) {
        return '';
      }

      var context = this._prepareContext(text);

      this._preparePrefs(context, prefs);

      return this._process(context);
    }
  }, {
    key: "_prepareContext",
    value: function _prepareContext(text) {
      return {
        text: text,
        isHTML: isHTML(text),
        prefs: deepCopy(this._prefs),
        getData: function getData$$1(key) {
          if (key === 'char') {
            return this.prefs.locale.map(function (item) {
              return getData(item + '/' + key);
            }).join('');
          } else {
            return getData(this.prefs.locale[0] + '/' + key);
          }
        }
      };
    }
  }, {
    key: "_preparePrefs",
    value: function _preparePrefs(context, prefs) {
      prefs = prefs || {};
      var contextPrefs = context.prefs;

      for (var _i = 0, _arr = ['htmlEntity', 'lineEnding', 'processingSeparateParts', 'ruleFilter']; _i < _arr.length; _i++) {
        var name = _arr[_i];

        if (name in prefs) {
          contextPrefs[name] = prefs[name];
        } else if (name in this._prefs) {
          contextPrefs[name] = this._prefs[name];
        }
      }

      contextPrefs.htmlEntity = contextPrefs.htmlEntity || {};
      contextPrefs.locale = prepareLocale(prefs.locale, this._prefs.locale);
      var locale = contextPrefs.locale;
      var locale0 = locale[0];

      if (!locale.length || !locale0) {
        throw Error('Not defined the property "locale".');
      }

      if (!hasLocale(locale0)) {
        throw Error('"' + locale0 + '" is not supported locale.');
      }
    }
  }, {
    key: "_splitBySeparateParts",
    value: function _splitBySeparateParts(context) {
      if (!context.isHTML || context.prefs.processingSeparateParts === false) {
        return [context.text];
      }

      var text = [],
          reTags = new RegExp('<(' + this._separatePartsTags.join('|') + ')(\\s[^>]*?)?>[^]*?</\\1>', 'gi');
      var position = 0;
      context.text.replace(reTags, function ($0, $1, $2, itemPosition) {
        if (position !== itemPosition) {
          text.push((position ? privateSeparateLabel : '') + context.text.slice(position, itemPosition) + privateSeparateLabel);
        }

        text.push($0);
        position = itemPosition + $0.length;
        return $0;
      });
      text.push(position ? privateSeparateLabel + context.text.slice(position, context.text.length) : context.text);
      return text;
    }
  }, {
    key: "_process",
    value: function _process(context) {
      var _this = this;

      context.text = removeCR(context.text);

      this._executeRules(context, 'start');

      this._safeTags.hide(context, 'own');

      this._executeRules(context, 'hide-safe-tags-own');

      this._safeTags.hide(context, 'html');

      this._executeRules(context, 'hide-safe-tags-html');

      var isRootHTML = context.isHTML,
          re = new RegExp(privateSeparateLabel, 'g');
      context.text = this._splitBySeparateParts(context).map(function (item) {
        context.text = item;
        context.isHTML = isHTML(item);

        _this._safeTags.hideHTMLTags(context);

        _this._safeTags.hide(context, 'url');

        _this._executeRules(context, 'hide-safe-tags-url');

        _this._executeRules(context, 'hide-safe-tags');

        HtmlEntities$1.toUtf(context);

        if (_this._prefs.live) {
          context.text = replaceNbsp(context.text);
        }

        _this._executeRules(context, 'utf');

        _this._executeRules(context);

        HtmlEntities$1.restore(context);

        _this._executeRules(context, 'html-entities');

        _this._safeTags.show(context, 'url');

        _this._executeRules(context, 'show-safe-tags-url');

        return context.text.replace(re, '');
      }).join('');
      context.isHTML = isRootHTML;

      this._safeTags.show(context, 'html');

      this._executeRules(context, 'show-safe-tags-html');

      this._safeTags.show(context, 'own');

      this._executeRules(context, 'show-safe-tags-own');

      this._executeRules(context, 'end');

      return fixLineEnding(context.text, context.prefs.lineEnding);
    }
    /**
     * Get a setting.
     *
     * @param {string} ruleName
     * @param {string} setting
     *
     * @returns {*}
     */

  }, {
    key: "getSetting",
    value: function getSetting(ruleName, setting) {
      return this._settings[ruleName] && this._settings[ruleName][setting];
    }
    /**
     * Set a setting.
     *
     * @param {string} ruleName
     * @param {string} setting
     * @param {*} [value]
     *
     * @returns {Typograf}
     */

  }, {
    key: "setSetting",
    value: function setSetting(ruleName, setting, value) {
      this._settings[ruleName] = this._settings[ruleName] || {};
      this._settings[ruleName][setting] = value;
      return this;
    }
    /**
     * Is enabled a rule.
     *
     * @param {string} ruleName
     *
     * @returns {boolean}
     */

  }, {
    key: "isEnabledRule",
    value: function isEnabledRule(ruleName) {
      return this._enabledRules[ruleName];
    }
    /**
     * Is disabled a rule.
     *
     * @param {string} ruleName
     *
     * @returns {boolean}
     */

  }, {
    key: "isDisabledRule",
    value: function isDisabledRule(ruleName) {
      return !this._enabledRules[ruleName];
    }
    /**
     * Enable a rule.
     *
     * @param {string|string[]} ruleName
     *
     * @returns {Typograf} this
     */

  }, {
    key: "enableRule",
    value: function enableRule(ruleName) {
      return this._enable(ruleName, true);
    }
    /**
     * Disable a rule.
     *
     * @param {string|string[]} ruleName
     *
     * @returns {Typograf} this
     */

  }, {
    key: "disableRule",
    value: function disableRule(ruleName) {
      return this._enable(ruleName, false);
    }
    /**
     * Add safe tag.
     *
     * @example
     * // var t = new Typograf({locale: 'ru'});
     * // t.addSafeTag('<mytag>', '</mytag>');
     * // t.addSafeTag('<mytag>', '</mytag>', '.*?');
     * // t.addSafeTag(/<mytag>.*?</mytag>/gi);
     *
     * @param {string|RegExp} startTag
     * @param {string} [endTag]
     * @param {string} [middle]
     *
     * @returns {Typograf} this
    */

  }, {
    key: "addSafeTag",
    value: function addSafeTag(startTag, endTag, middle) {
      var tag = startTag instanceof RegExp ? startTag : [startTag, endTag, middle];

      this._safeTags.add(tag);

      return this;
    }
  }, {
    key: "_executeRules",
    value: function _executeRules(context, queue) {
      queue = queue || 'default';
      var rules = this._rulesByQueues[queue];
      var innerRules = this._innerRulesByQueues[queue];
      innerRules && innerRules.forEach(function (rule) {
        this._ruleIterator(context, rule);
      }, this);
      rules && rules.forEach(function (rule) {
        this._ruleIterator(context, rule);
      }, this);
    }
  }, {
    key: "_ruleIterator",
    value: function _ruleIterator(context, rule) {
      var rlocale = rule._locale;
      var live = this._prefs.live;

      if (live === true && rule.live === false || live === false && rule.live === true) {
        return;
      }

      if ((rlocale === 'common' || rlocale === context.prefs.locale[0]) && this.isEnabledRule(rule.name)) {
        if (context.prefs.ruleFilter && !context.prefs.ruleFilter(rule)) {
          return;
        }

        this._onBeforeRule && this._onBeforeRule(rule.name, context.text, context);
        context.text = rule.handler.call(this, context.text, this._settings[rule.name], context);
        this._onAfterRule && this._onAfterRule(rule.name, context.text, context);
      }
    }
  }, {
    key: "_prepareRule",
    value: function _prepareRule(rule) {
      var name = rule.name;

      var t = _typeof(rule.settings);

      var settings = {};

      if (t === 'object') {
        settings = deepCopy(rule.settings);
      } else if (t === 'function') {
        settings = rule.settings(rule);
      }

      this._settings[name] = settings;
      this._enabledRules[name] = rule._enabled;
    }
  }, {
    key: "_enable",
    value: function _enable(rule, enabled) {
      if (Array.isArray(rule)) {
        rule.forEach(function (el) {
          this._enableByMask(el, enabled);
        }, this);
      } else {
        this._enableByMask(rule, enabled);
      }

      return this;
    }
  }, {
    key: "_enableByMask",
    value: function _enableByMask(rule, enabled) {
      if (!rule) {
        return;
      }

      if (rule.search(/\*/) !== -1) {
        var re = new RegExp(rule.replace(/\//g, '\\/').replace(/\*/g, '.*'));

        this._rules.forEach(function (el) {
          var name = el.name;

          if (re.test(name)) {
            this._enabledRules[name] = enabled;
          }
        }, this);
      } else {
        this._enabledRules[rule] = enabled;
      }
    }
  }, {
    key: "_getRule",
    value: function _getRule(name) {
      var rule = null;

      this._rules.some(function (item) {
        if (item.name === name) {
          rule = item;
          return true;
        }

        return false;
      });

      return rule;
    }
  }], [{
    key: "addRule",
    value: function addRule(rule) {
      var _rule$name$split = rule.name.split('/'),
          _rule$name$split2 = _slicedToArray(_rule$name$split, 3),
          locale = _rule$name$split2[0],
          group = _rule$name$split2[1],
          name = _rule$name$split2[2];

      rule._enabled = rule.disabled === true ? false : true;
      rule._locale = locale;
      rule._group = group;
      rule._name = name;
      this.addLocale(rule._locale);

      this._setIndex(rule);

      this.prototype._rules.push(rule);

      this._sortRules(this.prototype._rules);

      return this;
    }
    /**
     * Add rules.
     *
     * @static
     * @param {TypografRule[]} rules
     * 
     * @returns {Typograf} this
     */

  }, {
    key: "addRules",
    value: function addRules(rules) {
      var _this2 = this;

      rules.forEach(function (item) {
        _this2.addRule(item);
      });
      return this;
    }
    /**
     * Add internal rule.
     * Internal rules are executed before main.
     *
     * @static
     * @param {TypografRule} rule
     *
     * @returns {Typograf} this
     */

  }, {
    key: "addInnerRule",
    value: function addInnerRule(rule) {
      this.prototype._innerRules.push(rule);

      rule._locale = rule.name.split('/')[0];
      return this;
    }
    /**
     * Add internal rules.
     * Internal rules are executed before main.
     *
     * @static
     * @param {TypografRule[]} rules
     *
     * @returns {Typograf} this
     */

  }, {
    key: "addInnerRules",
    value: function addInnerRules(rules) {
      var _this3 = this;

      rules.forEach(function (item) {
        _this3.addInnerRule(item);
      });
      return this;
    }
  }, {
    key: "_setIndex",
    value: function _setIndex(rule) {
      var index = rule.index;

      var t = _typeof(index);

      var groupIndex = groupIndexes[rule._group];

      if (t === 'undefined') {
        index = groupIndex;
      } else if (t === 'string') {
        index = (groupIndex || 0) + parseInt(rule.index, 10);
      }

      rule._index = index;
    }
  }, {
    key: "_sortRules",
    value: function _sortRules(rules) {
      rules.sort(function (a, b) {
        return a._index > b._index ? 1 : -1;
      });
    }
  }]);

  return Typograf;
}();

Typograf.version = '6.11.0';
Typograf.addLocale = addLocale;
Typograf.getLocales = getLocales;
Typograf.hasLocale = hasLocale;
Typograf.setData = setData;
Typograf.getData = getData; // @deprecated

Typograf.deepCopy = deepCopy;
Typograf.prototype._rules = [];
Typograf.prototype._innerRules = [];
/**
 * @typedef TypografRule
 * @type {object}
 * 
 * @property {string} name Name of rule
 * @property {Function} handler Processing function
 * @property {number} [index] Sorting index for rule
 * @property {boolean} [disabled] Rule is disabled by default
 * @property {boolean} [live] Live mode
 * @property {Object} [settings] Settings for rule
 */

var common = {
  'common/char': 'a-z',
  'common/dash': '--?|‒|–|—',
  // --, &#8210, &ndash, &mdash
  'common/quote': '«‹»›„“‟”"'
};

var be = {
  'be/char': 'абвгдежзйклмнопрстуфхцчшыьэюяёіўґ',
  'be/quote': {
    left: '«“',
    right: '»”'
  }
};

var bg = {
  'bg/char': 'абвгдежзийклмнопрстуфхцчшщъьюя',
  'bg/quote': {
    left: '„’',
    right: '“’'
  }
};

var ca = {
  'ca/char': 'abcdefghijlmnopqrstuvxyzàçèéíïòóúü',
  'ca/quote': {
    left: '«“',
    right: '»”'
  }
};

var da = {
  'da/char': 'a-zåæø',
  'da/quote': {
    left: '»›',
    right: '«‹'
  }
};

var de = {
  'de/char': 'a-zßäöü',
  'de/quote': {
    left: '„‚',
    right: '“‘'
  }
};

var el = {
  'el/char': 'ΐάέήίΰαβγδεζηθικλμνξοπρςστυφχψωϊϋόύώϲάέήίόύώ',
  'el/quote': {
    left: '«“',
    right: '»”'
  }
};

var enGB = {
  'en-GB/char': 'a-z',
  'en-GB/quote': {
    left: '‘“',
    right: '’”'
  }
};

var enUS = {
  'en-US/char': 'a-z',
  'en-US/quote': {
    left: '“‘',
    right: '”’'
  }
};

var eo = {
  'eo/char': 'abcdefghijklmnoprstuvzĉĝĥĵŝŭ',
  'eo/quote': {
    left: '“‘',
    right: '”’'
  }
};

var es = {
  'es/char': 'a-záéíñóúü',
  'es/quote': {
    left: '«“',
    right: '»”'
  }
};

var et = {
  'et/char': 'abdefghijklmnoprstuvzäõöüšž',
  'et/quote': {
    left: '„«',
    right: '“»'
  }
};

var fi = {
  'fi/char': 'abcdefghijklmnopqrstuvyöäå',
  'fi/quote': {
    left: '”’',
    right: '”’'
  }
};

var fr = {
  'fr/char': 'a-zàâçèéêëîïôûüœæ',
  'fr/quote': {
    left: '«‹',
    right: '»›',
    spacing: true
  }
};

var ga = {
  'ga/char': 'abcdefghilmnoprstuvwxyzáéíóú',
  'ga/quote': {
    left: '“‘',
    right: '”’'
  }
};

var hu = {
  'hu/char': 'a-záäéíóöúüőű',
  'hu/quote': {
    left: '„»',
    right: '”«'
  }
};

var it = {
  'it/char': 'a-zàéèìòù',
  'it/quote': {
    left: '«“',
    right: '»”'
  }
};

var lv = {
  'lv/char': 'abcdefghijklmnopqrstuvxzæœ',
  'lv/quote': {
    left: '«„',
    right: '»“'
  }
};

var nl = {
  'nl/char': 'a-zäçèéêëîïñöûü',
  'nl/quote': {
    left: '‘“',
    right: '’”'
  }
};

var no = {
  'no/char': 'a-zåæèéêòóôø',
  'no/quote': {
    left: '«’',
    right: '»’'
  }
};

var pl = {
  'pl/char': 'abcdefghijklmnoprstuvwxyzóąćęłńśźż',
  'pl/quote': {
    left: '„«',
    right: '”»'
  }
};

var ro = {
  'ro/char': 'abcdefghijklmnoprstuvxzîășț',
  'ro/quote': {
    left: '„«',
    right: '”»'
  }
};

var ru = {
  'ru/char': 'а-яё',
  'ru/dashBefore': '(^| |\\n)',
  'ru/dashAfter': "(?=[\xA0 ,.?:!]|$)",
  'ru/dashAfterDe': "(?=[,.?:!]|[\xA0 ][^\u0410-\u042F\u0401]|$)",
  'ru/l': 'а-яёa-z',
  'ru/L': 'А-ЯЁA-Z',
  'ru/month': 'январь|февраль|март|апрель|май|июнь|июль|август|сентябрь|октябрь|ноябрь|декабрь',
  'ru/monthGenCase': 'января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря',
  'ru/monthPreCase': 'январе|феврале|марте|апреле|мае|июне|июле|августе|сентябре|октябре|ноябре|декабре',
  'ru/quote': {
    left: '«„‚',
    right: '»“‘',
    removeDuplicateQuotes: true
  },
  'ru/shortMonth': 'янв|фев|мар|апр|ма[ейя]|июн|июл|авг|сен|окт|ноя|дек',
  'ru/weekday': 'понедельник|вторник|среда|четверг|пятница|суббота|воскресенье'
};

var sk = {
  'sk/char': 'abcdefghijklmnoprstuvwxyzáäéíóôúýčďľňŕšťž',
  'sk/quote': {
    left: '„‚',
    right: '“‘'
  }
};

var sl = {
  'sl/char': 'a-zčšž',
  'sl/quote': {
    left: '„‚',
    right: '“‘'
  }
};

var sr = {
  'sr/char': 'abcdefghijklmnoprstuvzćčđšž',
  'sr/quote': {
    left: '„’',
    right: '”’'
  }
};

var sv = {
  'sv/char': 'a-zäåéö',
  'sv/quote': {
    left: '”’',
    right: '”’'
  }
};

var tr = {
  'tr/char': 'abcdefghijklmnoprstuvyzâçîöûüğış',
  'tr/quote': {
    left: '“‘',
    right: '”’'
  }
};

var uk = {
  'uk/char': 'абвгдежзийклмнопрстуфхцчшщьюяєіїґ',
  'uk/quote': {
    left: '«„',
    right: '»“'
  }
};

var data$1 = [common, be, bg, ca, da, de, el, enGB, enUS, eo, es, et, fi, fr, ga, hu, it, lv, nl, no, pl, ro, ru, sk, sl, sr, sv, tr, uk];
data$1.forEach(function (item) {
  return setData(item);
});

var eMail = {
  name: 'common/html/e-mail',
  queue: 'end',
  handler: function handler(text, settings, context) {
    return context.isHTML ? text : text.replace(/(^|[\s;(])([\w\-.]{2,64})@([\w\-.]{2,64})\.([a-z]{2,64})([)\s.,!?]|$)/gi, '$1<a href="mailto:$2@$3.$4">$2@$3.$4</a>$5');
  },
  disabled: true,
  htmlAttrs: false
};

var escape = {
  name: 'common/html/escape',
  index: '+100',
  queue: 'end',
  handler: function handler(text) {
    var entityMap = {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      '\'': '&#39;',
      '/': '&#x2F;'
    };
    return text.replace(/[&<>"'/]/g, function (s) {
      return entityMap[s];
    });
  },
  disabled: true
};

var nbr = {
  name: 'common/html/nbr',
  index: '+10',
  queue: 'end',
  handler: function handler(text) {
    return text.replace(/([^\n>])\n(?=[^\n])/g, '$1<br/>\n');
  },
  disabled: true,
  htmlAttrs: false
};

var blockElements = ['address', 'article', 'aside', 'blockquote', 'canvas', 'dd', 'div', 'dl', 'fieldset', 'figcaption', 'figure', 'footer', 'form', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'header', 'hgroup', 'hr', 'li', 'main', 'nav', 'noscript', 'ol', 'output', 'p', 'pre', 'section', 'table', 'tfoot', 'ul', 'video'];

var p = {
  name: 'common/html/p',
  index: '+5',
  queue: 'end',
  handler: function handler(text) {
    var blockRe = new RegExp('<(' + blockElements.join('|') + ')[>\\s]');
    var separator = '\n\n';
    var buffer = text.split(separator);
    buffer.forEach(function (text, i, data) {
      if (!text.trim()) {
        return;
      }

      if (!blockRe.test(text)) {
        data[i] = text.replace(/^(\s*)/, '$1<p>').replace(/(\s*)$/, '</p>$1');
      }
    });
    return buffer.join(separator);
  },
  disabled: true,
  htmlAttrs: false
};

var processingAttrs = {
  name: 'common/html/processingAttrs',
  queue: 'hide-safe-tags-own',
  // After "hide-safe-tags-own", before "hide-safe-tags-html".
  handler: function handler(text, settings, context) {
    var that = this;
    var reAttrs = new RegExp('(^|\\s)(' + settings.attrs.join('|') + ')=("[^"]*?"|\'[^\']*?\')', 'gi');
    var prefs = deepCopy(context.prefs);

    prefs.ruleFilter = function (rule) {
      return rule.htmlAttrs !== false;
    };

    return text.replace(/(<[-\w]+\s)([^>]+?)(?=>)/g, function (match, tagName, attrs) {
      var resultAttrs = attrs.replace(reAttrs, function (submatch, space, attrName, attrValue) {
        var lquote = attrValue[0];
        var rquote = attrValue[attrValue.length - 1];
        var value = attrValue.slice(1, -1);
        return space + attrName + '=' + lquote + that.execute(value, prefs) + rquote;
      });
      return tagName + resultAttrs;
    });
  },
  settings: {
    attrs: ['title', 'placeholder']
  },
  disabled: true,
  htmlAttrs: false
};

var quot = {
  name: 'common/html/quot',
  queue: 'hide-safe-tags',
  handler: function handler(text) {
    return text.replace(/&quot;/g, '"');
  }
};

var stripTags = {
  name: 'common/html/stripTags',
  index: '+99',
  queue: 'end',
  handler: function handler(text) {
    return text.replace(/<[^>]+>/g, '');
  },
  disabled: true
};

var url = {
  name: 'common/html/url',
  queue: 'end',
  handler: function handler(text, settings, context) {
    return context.isHTML ? text : text.replace(regExpUrl, function ($0, protocol, path) {
      path = path.replace(/([^/]+\/?)(\?|#)$/, '$1') // Remove ending ? and #
      .replace(/^([^/]+)\/$/, '$1'); // Remove ending /

      if (protocol === 'http') {
        path = path.replace(/^([^/]+)(:80)([^\d]|\/|$)/, '$1$3'); // Remove 80 port
      } else if (protocol === 'https') {
        path = path.replace(/^([^/]+)(:443)([^\d]|\/|$)/, '$1$3'); // Remove 443 port
      }

      var url = path;
      var fullUrl = protocol + '://' + path;
      var firstPart = '<a href="' + fullUrl + '">';

      if (protocol === 'http' || protocol === 'https') {
        url = url.replace(/^www\./, '');
        return firstPart + (protocol === 'http' ? url : protocol + '://' + url) + '</a>';
      }

      return firstPart + fullUrl + '</a>';
    });
  },
  disabled: true,
  htmlAttrs: false
};

Typograf.addRules([eMail, escape, nbr, p, processingAttrs, quot, stripTags, url]);

var afterNumber = {
  name: 'common/nbsp/afterNumber',
  handler: function handler(text, settings, context) {
    var re = '(^|\\D)(\\d{1,5}) ([' + context.getData('char') + ']+)';
    return text.replace(new RegExp(re, 'gi'), "$1$2\xA0$3");
  },
  disabled: true
};

var afterParagraphMark = {
  name: 'common/nbsp/afterParagraphMark',
  handler: function handler(text) {
    return text.replace(/¶ ?(?=\d)/g, "\xB6\xA0");
  }
};

var afterSectionMark = {
  name: 'common/nbsp/afterSectionMark',
  handler: function handler(text, settings, context) {
    // \u2009 - THIN SPACE
    // \u202F - NARROW NO-BREAK SPACE
    var locale = context.prefs.locale[0];
    return text.replace(/§[ \u00A0\u2009]?(?=\d|I|V|X)/g, locale === 'ru' ? "\xA7\u202F" : "\xA7\xA0");
  }
};

var afterShortWord = {
  name: 'common/nbsp/afterShortWord',
  handler: function handler(text, settings, context) {
    var len = settings.lengthShortWord;
    var before = " \xA0(" + privateLabel + getData('common/quote');
    var subStr = '(^|[' + before + '])([' + context.getData('char') + ']{1,' + len + '}) ';
    var newSubStr = "$1$2\xA0";
    var re = new RegExp(subStr, 'gim');
    return text.replace(re, newSubStr).replace(re, newSubStr);
  },
  settings: {
    lengthShortWord: 2
  }
};

var beforeShortLastNumber = {
  name: 'common/nbsp/beforeShortLastNumber',
  handler: function handler(text, settings, context) {
    var ch = context.getData('char');
    var CH = ch.toUpperCase();
    var re = new RegExp('([' + ch + CH + ']) (?=\\d{1,' + settings.lengthLastNumber + '}[-+−%\'"' + context.getData('quote').right + ')]?([.!?…]( [' + CH + ']|$)|$))', 'gm');
    return text.replace(re, "$1\xA0");
  },
  live: false,
  settings: {
    lengthLastNumber: 2
  }
};

var beforeShortLastWord = {
  name: 'common/nbsp/beforeShortLastWord',
  handler: function handler(text, settings, context) {
    var ch = context.getData('char');
    var CH = ch.toUpperCase();
    var re = new RegExp('([' + ch + '\\d]) ([' + ch + CH + ']{1,' + settings.lengthLastWord + '}[.!?…])( [' + CH + ']|$)', 'g');
    return text.replace(re, "$1\xA0$2$3");
  },
  settings: {
    lengthLastWord: 3
  }
};

var dpi = {
  name: 'common/nbsp/dpi',
  handler: function handler(text) {
    return text.replace(/(\d) ?(lpi|dpi)(?!\w)/, "$1\xA0$2");
  }
};

function replaceNbsp$1($0, $1, $2, $3) {
  return $1 + $2.replace(/([^\u00A0])\u00A0([^\u00A0])/g, '$1 $2') + $3;
}

var nowrap = {
  name: 'common/nbsp/nowrap',
  queue: 'end',
  handler: function handler(text) {
    return text.replace(/(<nowrap>)(.*?)(<\/nowrap>)/g, replaceNbsp$1).replace(/(<nobr>)(.*?)(<\/nobr>)/g, replaceNbsp$1);
  }
};

var replaceNbsp$2 = {
  name: 'common/nbsp/replaceNbsp',
  queue: 'utf',
  live: false,
  handler: replaceNbsp,
  disabled: true
};

Typograf.addRules([afterNumber, afterParagraphMark, afterSectionMark, afterShortWord, beforeShortLastNumber, beforeShortLastWord, dpi, nowrap, replaceNbsp$2]);

var digitGrouping = {
  name: 'common/number/digitGrouping',
  index: '310',
  disabled: true,
  handler: function handler(text, settings) {
    return text.replace(new RegExp("(^ ?|\\D |".concat(privateLabel, ")(\\d{1,3}([ \xA0\u202F\u2009]\\d{3})+)(?! ?[\\d-])"), 'gm'), function ($0, $1, $2) {
      return $1 + $2.replace(/\s/g, settings.space);
    }) // https://www.bipm.org/utils/common/pdf/si-brochure/SI-Brochure-9-EN.pdf #5.4.4
    .replace(/(\d{5,}([.,]\d+)?)/g, function ($0, $1) {
      var decimalMarker = $1.match(/[.,]/);

      var _ref = decimalMarker ? $1.split(decimalMarker) : [$1],
          _ref2 = _slicedToArray(_ref, 2),
          integerPart = _ref2[0],
          fractionalPart = _ref2[1];

      integerPart = integerPart.replace(/(\d)(?=(\d{3})+([^\d]|$))/g, '$1' + settings.space);
      return decimalMarker ? integerPart + decimalMarker + fractionalPart : integerPart;
    });
  },
  settings: {
    space: "\u202F"
  }
};

var fraction = {
  name: 'common/number/fraction',
  handler: function handler(text) {
    return text.replace(/(^|\D)1\/2(\D|$)/g, '$1½$2').replace(/(^|\D)1\/4(\D|$)/g, '$1¼$2').replace(/(^|\D)3\/4(\D|$)/g, '$1¾$2');
  }
};

var mathSigns = {
  name: 'common/number/mathSigns',
  handler: function handler(text) {
    return replace(text, [[/!=/g, '≠'], [/<=/g, '≤'], [/(^|[^=])>=/g, '$1≥'], [/<=>/g, '⇔'], [/<</g, '≪'], [/>>/g, '≫'], [/~=/g, '≅'], [/(^|[^+])\+-/g, '$1±']]);
  }
};

var times = {
  name: 'common/number/times',
  handler: function handler(text) {
    return text.replace(/(\d)[ \u00A0]?[xх][ \u00A0]?(\d)/g, '$1×$2');
  }
};

Typograf.addRules([digitGrouping, fraction, mathSigns, times]);

var delBOM = {
  name: 'common/other/delBOM',
  queue: 'start',
  index: -1,
  handler: function handler(text) {
    if (text.charCodeAt(0) === 0xFEFF) {
      return text.slice(1);
    }

    return text;
  }
};

var repeatWord = {
  name: 'common/other/repeatWord',
  handler: function handler(text, settings, context) {
    var punc = '[;:,.?! \n' + getData('common/quote') + ']';
    var re = new RegExp('(' + punc + '|^)' + '([' + context.getData('char') + ']{' + settings.min + ',}) ' + '\\2(' + punc + '|$)', 'gi');
    return text.replace(re, '$1$2$3');
  },
  settings: {
    min: 2
  },
  disabled: true
};

Typograf.addRules([delBOM, repeatWord]);

var apostrophe = {
  name: 'common/punctuation/apostrophe',
  handler: function handler(text, settings, context) {
    var letters = '([' + context.getData('char') + '])';
    var re = new RegExp(letters + '\'' + letters, 'gi');
    return text.replace(re, '$1’$2');
  }
};

var delDoublePunctuation = {
  name: 'common/punctuation/delDoublePunctuation',
  handler: function handler(text) {
    return text.replace(/(^|[^,]),,(?!,)/g, '$1,').replace(/(^|[^:])::(?!:)/g, '$1:').replace(/(^|[^!?.])\.\.(?!\.)/g, '$1.').replace(/(^|[^;]);;(?!;)/g, '$1;').replace(/(^|[^?])\?\?(?!\?)/g, '$1?');
  }
};

var hellip = {
  name: 'common/punctuation/hellip',
  handler: function handler(text, settings, context) {
    return context.prefs.locale[0] === 'ru' ? text.replace(/(^|[^.])\.{3,4}(?=[^.]|$)/g, '$1…') : text.replace(/(^|[^.])\.{3}(\.?)(?=[^.]|$)/g, '$1…$2');
  }
};

var Quote = {
  bufferQuotes: {
    left: "\uF005\uF006\uF007",
    right: "\uF008\uF009\uF0A0"
  },
  maxLevel: 3,
  beforeLeft: " \n\t\xA0[(",
  afterRight: " \n\t\xA0!?.:;#*,\u2026)",
  process: function process(params) {
    var text = params.context.text;
    var count = this.count(text);

    if (!count.total) {
      return text;
    }

    var originalSettings = params.settings;
    var isEqualQuotes = params.settings.left[0] === params.settings.right[0]; // For SW, FI

    if (isEqualQuotes) {
      params.settings = deepCopy(params.settings);
      params.settings.left = this.bufferQuotes.left.slice(0, params.settings.left.length);
      params.settings.right = this.bufferQuotes.right.slice(0, params.settings.right.length);
    } // For FR


    if (params.settings.spacing) {
      text = this.removeSpacing(text, params.settings);
    }

    text = this.set(text, params); // For FR

    if (params.settings.spacing) {
      text = this.setSpacing(text, params.settings);
    } // For RU


    if (params.settings.removeDuplicateQuotes) {
      text = this.removeDuplicates(text, params.settings);
    } // For SW, FI


    if (isEqualQuotes) {
      text = this.returnOriginalQuotes(text, originalSettings, params.settings);
      params.settings = originalSettings;
    }

    return text;
  },
  returnOriginalQuotes: function returnOriginalQuotes(text, originalSettings, bufferSettings) {
    var buffer = {};

    for (var i = 0; i < bufferSettings.left.length; i++) {
      buffer[bufferSettings.left[i]] = originalSettings.left[i];
      buffer[bufferSettings.right[i]] = originalSettings.right[i];
    }

    return text.replace(new RegExp('[' + bufferSettings.left + bufferSettings.right + ']', 'g'), function (quote) {
      return buffer[quote];
    });
  },
  count: function count(text) {
    var count = {
      total: 0
    };
    text.replace(new RegExp('[' + getData('common/quote') + ']', 'g'), function (quote) {
      if (!count[quote]) {
        count[quote] = 0;
      }

      count[quote]++;
      count.total++;
      return quote;
    });
    return count;
  },
  removeDuplicates: function removeDuplicates(text, settings) {
    var lquote = settings.left[0];
    var lquote2 = settings.left[1] || lquote;
    var rquote = settings.right[0];

    if (lquote !== lquote2) {
      return text;
    }

    return text // ««word» word» -> «word» word»
    .replace(new RegExp(lquote + lquote, 'g'), lquote) // «word «word»» -> «word «word»
    .replace(new RegExp(rquote + rquote, 'g'), rquote);
  },
  removeSpacing: function removeSpacing(text, settings) {
    for (var i = 0, len = settings.left.length; i < len; i++) {
      var lquote = settings.left[i];
      var rquote = settings.right[i];
      text = text.replace(new RegExp(lquote + "([ \u202F\xA0])", 'g'), lquote).replace(new RegExp("([ \u202F\xA0])" + rquote, 'g'), rquote);
    }

    return text;
  },
  setSpacing: function setSpacing(text, settings) {
    for (var i = 0, len = settings.left.length; i < len; i++) {
      var lquote = settings.left[i];
      var rquote = settings.right[i];
      text = text.replace(new RegExp(lquote + "([^\u202F])", 'g'), lquote + "\u202F$1").replace(new RegExp("([^\u202F])" + rquote, 'g'), "$1\u202F" + rquote);
    }

    return text;
  },
  set: function set(text, params) {
    var quotes = getData('common/quote');
    var lquote = params.settings.left[0];
    var lquote2 = params.settings.left[1] || lquote;
    var rquote = params.settings.right[0];
    var reL = new RegExp('(^|[' + this.beforeLeft + '])([' + quotes + ']{1,' + this.maxLevel + '})(?=[^\\s' + privateLabel + '])', 'gim');
    var reR = new RegExp('([^\\s' + privateLabel + '])([' + quotes + ']{1,' + this.maxLevel + '})(?=[' + this.afterRight + ']|$)', 'gim');
    text = text.replace(reL, function ($0, $1, $2) {
      return $1 + repeat(lquote, $2.length);
    }).replace(reR, function ($0, $1, $2) {
      return $1 + repeat(rquote, $2.length);
    });
    text = this.setAboveTags(text, params);

    if (lquote !== lquote2) {
      text = this.setInner(text, params.settings);
    }

    return text;
  },
  setAboveTags: function setAboveTags(text, params) {
    var _this = this;

    var quotes = getData('common/quote');
    var lquote = params.settings.left[0];
    var rquote = params.settings.right[0];
    return text.replace(new RegExp('(^|.)([' + quotes + '])(.|$)', 'gm'), function (original, prev, quote, next, pos) {
      if (prev !== privateLabel && next !== privateLabel) {
        return original;
      }

      if (prev === privateLabel && next === privateLabel) {
        if (quote === '"') {
          return prev + _this.getAboveTwoTags(text, pos + 1, params) + next;
        }

        return original;
      }

      if (prev === privateLabel) {
        var hasRight = _this.afterRight.indexOf(next) > -1;

        var prevInfo = _this.getPrevTagInfo(text, pos - 1, params);

        if (hasRight && prevInfo && prevInfo.group === 'html') {
          return prev + (prevInfo.isClosing ? rquote : lquote) + next;
        }

        return prev + (!next || hasRight ? rquote : lquote) + next;
      } else {
        var hasLeft = _this.beforeLeft.indexOf(prev) > -1;

        var nextInfo = _this.getNextTagInfo(text, pos + 1, params);

        if (hasLeft && nextInfo && nextInfo.group === 'html') {
          return prev + (nextInfo.isClosing ? rquote : lquote) + next;
        }

        return prev + (!prev || hasLeft ? lquote : rquote) + next;
      }
    });
  },
  getAboveTwoTags: function getAboveTwoTags(text, pos, params) {
    var prevInfo = this.getPrevTagInfo(text, pos, params);
    var nextInfo = this.getNextTagInfo(text, pos, params);

    if (prevInfo) {
      if (prevInfo.group === 'html') {
        if (!prevInfo.isClosing) {
          return params.settings.left[0];
        }

        if (nextInfo && nextInfo.isClosing && prevInfo.isClosing) {
          return params.settings.right[0];
        }
      }
    }

    return text[pos];
  },
  getPrevTagInfo: function getPrevTagInfo(text, pos, params) {
    var prevLabel = params.safeTags.getPrevLabel(text, pos - 1);

    if (prevLabel) {
      var prevTag = params.safeTags.getTagByLabel(params.context, prevLabel);

      if (prevTag) {
        return params.safeTags.getTagInfo(prevTag);
      }
    }

    return null;
  },
  getNextTagInfo: function getNextTagInfo(text, pos, params) {
    var nextLabel = params.safeTags.getNextLabel(text, pos + 1);

    if (nextLabel) {
      var nextTag = params.safeTags.getTagByLabel(params.context, nextLabel);

      if (nextTag) {
        return params.safeTags.getTagInfo(nextTag);
      }
    }

    return null;
  },
  setInner: function setInner(text, settings) {
    var leftQuotes = [];
    var rightQuotes = [];

    for (var k = 0; k < settings.left.length; k++) {
      leftQuotes.push(settings.left[k]);
      rightQuotes.push(settings.right[k]);
    }

    var lquote = settings.left[0];
    var rquote = settings.right[0];
    var minLevel = -1;
    var maxLevel = leftQuotes.length - 1;
    var level = minLevel;
    var result = '';

    for (var i = 0, len = text.length; i < len; i++) {
      var letter = text[i];

      if (letter === lquote) {
        level++;

        if (level > maxLevel) {
          level = maxLevel;
        }

        result += leftQuotes[level];
      } else if (letter === rquote) {
        if (level <= minLevel) {
          level = 0;
          result += rightQuotes[level];
        } else {
          result += rightQuotes[level];
          level--;

          if (level < minLevel) {
            level = minLevel;
          }
        }
      } else {
        if (letter === '"') {
          level = minLevel;
        }

        result += letter;
      }
    }

    var count = this.count(result, settings);
    return count[lquote] !== count[rquote] ? text : result;
  }
};
var quote = {
  name: 'common/punctuation/quote',
  handler: function handler(text, commonSettings, context) {
    var locale = context.prefs.locale[0];
    var settings = commonSettings[locale];

    if (!settings) {
      return text;
    }

    return Quote.process({
      context: context,
      settings: settings,
      safeTags: this._safeTags
    });
  },
  settings: function settings() {
    var settings = {};
    getLocales().forEach(function (locale) {
      settings[locale] = deepCopy(getData(locale + '/quote'));
    });
    return settings;
  }
};

var quoteLink = {
  name: 'common/punctuation/quoteLink',
  queue: 'show-safe-tags-html',
  index: '+5',
  handler: function handler(text, settings, context) {
    var quotes = this.getSetting('common/punctuation/quote', context.prefs.locale[0]);

    if (!quotes) {
      return text;
    }

    var lquote1 = HtmlEntities$1.getByUtf(quotes.left[0]);
    var rquote1 = HtmlEntities$1.getByUtf(quotes.right[0]);
    var lquote2 = HtmlEntities$1.getByUtf(quotes.left[1]);
    var rquote2 = HtmlEntities$1.getByUtf(quotes.right[1]);
    lquote2 = lquote2 ? '|' + lquote2 : '';
    rquote2 = rquote2 ? '|' + rquote2 : '';
    var re = new RegExp('(<[aA]\\s[^>]*?>)(' + lquote1 + lquote2 + ')([^]*?)(' + rquote1 + rquote2 + ')(</[aA]>)', 'g');
    return text.replace(re, '$2$1$3$5$4');
  }
};

Typograf.addRules([apostrophe, delDoublePunctuation, hellip, quote, quoteLink]);

var afterPunctuation = {
  name: 'common/space/afterPunctuation',
  handler: function handler(text) {
    var reExcl = new RegExp('(!|;|\\?)([^).…!;?\\s[\\])' + privateLabel + getData('common/quote') + '])', 'g');
    var reComma = new RegExp('(\\D)(,|:)([^)",:.?\\s\\/\\\\' + privateLabel + '])', 'g');
    return text.replace(reExcl, '$1 $2').replace(reComma, '$1$2 $3');
  }
};

var beforeBracket = {
  name: 'common/space/beforeBracket',
  handler: function handler(text, settings, context) {
    var re = new RegExp('([' + context.getData('char') + '.!?,;…)])\\(', 'gi');
    return text.replace(re, '$1 (');
  }
};

var bracket = {
  name: 'common/space/bracket',
  handler: function handler(text) {
    return text.replace(/(\() +/g, '(').replace(/ +\)/g, ')');
  }
};

var delBeforePercent = {
  name: 'common/space/delBeforePercent',
  handler: function handler(text) {
    return text.replace(/(\d)( |\u00A0)(%|‰|‱)/g, '$1$3');
  }
};

var delBeforePunctuation = {
  name: 'common/space/delBeforePunctuation',
  handler: function handler(text) {
    return text.replace(/([!?]) (?=[!?])/g, '$1').replace(/(^|[^!?:;,.…]) ([!?:;,.])(?!\))/g, '$1$2');
  }
};

var delLeadingBlanks = {
  name: 'common/space/delLeadingBlanks',
  handler: function handler(text) {
    return text.replace(/^[ \t]+/mg, '');
  },
  disabled: true
};

var delRepeatN = {
  name: 'common/space/delRepeatN',
  index: '-1',
  handler: function handler(text, settings) {
    var maxConsecutiveLineBreaks = settings.maxConsecutiveLineBreaks;
    var consecutiveLineBreaksRegex = new RegExp("\n{".concat(maxConsecutiveLineBreaks + 1, ",}"), 'g');
    var replaceValue = repeat('\n', maxConsecutiveLineBreaks);
    return text.replace(consecutiveLineBreaksRegex, replaceValue);
  },
  settings: {
    maxConsecutiveLineBreaks: 2
  }
};

var delRepeatSpace = {
  name: 'common/space/delRepeatSpace',
  index: '-1',
  handler: function handler(text) {
    return text.replace(/([^\n \t])[ \t]{2,}(?![\n \t])/g, '$1 ');
  }
};

var delTrailingBlanks = {
  name: 'common/space/delTrailingBlanks',
  index: '-3',
  handler: function handler(text) {
    return text.replace(/[ \t]+\n/g, '\n');
  }
};

var insertFinalNewline = {
  name: 'common/space/insertFinalNewline',
  live: false,
  disabled: true,
  queue: 'end',
  handler: function handler(text) {
    return text[text.length - 1] === '\n' ? text : text + '\n';
  }
};

var replaceTab = {
  name: 'common/space/replaceTab',
  index: '-5',
  handler: function handler(text) {
    return text.replace(/\t/g, '    ');
  }
};

var squareBracket = {
  name: 'common/space/squareBracket',
  handler: function handler(text) {
    return text.replace(/(\[) +/g, '[').replace(/ +\]/g, ']');
  }
};

var trimLeft = {
  name: 'common/space/trimLeft',
  index: '-4',
  handler: String.prototype.trimLeft ? function (text) {
    return text.trimLeft();
  } :
  /* istanbul ignore next */
  function (text) {
    return text.replace(/^[\s\uFEFF\xA0]+/g, '');
  }
};

var trimRight = {
  name: 'common/space/trimRight',
  index: '-3',
  live: false,
  handler: String.prototype.trimRight ? function (text) {
    return text.trimRight();
  } :
  /* istanbul ignore next */
  function (text) {
    return text.replace(/[\s\uFEFF\xA0]+$/g, '');
  }
};

Typograf.addRules([afterPunctuation, beforeBracket, bracket, delBeforePercent, delBeforePunctuation, delLeadingBlanks, delRepeatN, delRepeatSpace, delTrailingBlanks, insertFinalNewline, replaceTab, squareBracket, trimLeft, trimRight]);

var arrow = {
  name: 'common/symbols/arrow',
  handler: function handler(text) {
    return replace(text, [[/(^|[^-])->(?!>)/g, '$1→'], [/(^|[^<])<-(?!-)/g, '$1←']]);
  }
};

var cf = {
  name: 'common/symbols/cf',
  handler: function handler(text) {
    var re = new RegExp("(^|[\\s(\\[+\u2248\xB1\u2212\u2014\u2013\\-])(\\d+(?:[.,]\\d+)?)[ \xA0\u2009]?(C|F)([\\W\\s.,:!?\")\\]]|$)", 'mg');
    return text.replace(re, "$1$2\u2009\xB0$3$4");
  }
};

var copy = {
  name: 'common/symbols/copy',
  handler: function handler(text) {
    return replace(text, [[/\(r\)/gi, '®'], [/(copyright )?\((c|с)\)/gi, '©'], [/\(tm\)/gi, '™']]);
  }
};

Typograf.addRules([arrow, cf, copy]);

var main = {
  name: 'en-US/dash/main',
  index: '-5',
  handler: function handler(text) {
    var dashes = getData('common/dash');
    var nonBreakingSpace = "\xA0";
    var emDash = "\u2014";
    var spaceBefore = "[ ".concat(nonBreakingSpace, "]"); // white space or a non-breaking space

    var spaceAfter = "[ ".concat(nonBreakingSpace, "\n]"); // same as spaceBefore, but includes line break

    var re = new RegExp("".concat(spaceBefore, "(").concat(dashes, ")(").concat(spaceAfter, ")"), 'g');
    return text.replace(re, "".concat(nonBreakingSpace).concat(emDash, "$2"));
  }
};

Typograf.addRules([main]);

var centuries = {
  name: 'ru/dash/centuries',
  handler: function handler(text, settings) {
    var dashes = '(' + getData('common/dash') + ')';
    var re = new RegExp("(X|I|V)[ |\xA0]?" + dashes + "[ |\xA0]?(X|I|V)", 'g');
    return text.replace(re, '$1' + settings.dash + '$3');
  },
  settings: {
    dash: "\u2013" // &ndash;

  }
};

var daysMonth = {
  name: 'ru/dash/daysMonth',
  handler: function handler(text, settings) {
    var re = new RegExp('(^|\\s)([123]?\\d)' + '(' + getData('common/dash') + ')' + "([123]?\\d)[ \xA0]" + '(' + getData('ru/monthGenCase') + ')', 'g');
    return text.replace(re, '$1$2' + settings.dash + "$4\xA0$5");
  },
  settings: {
    dash: "\u2013" // &ndash;

  }
};

var de$1 = {
  name: 'ru/dash/de',
  handler: function handler(text) {
    var re = new RegExp('([a-яё]+) де' + getData('ru/dashAfterDe'), 'g');
    return text.replace(re, '$1-де');
  },
  disabled: true
};

var decade = {
  name: 'ru/dash/decade',
  handler: function handler(text, settings) {
    var re = new RegExp('(^|\\s)(\\d{3}|\\d)0' + '(' + getData('common/dash') + ')' + "(\\d{3}|\\d)0(-\u0435[ \xA0])" + "(?=\u0433\\.?[ \xA0]?\u0433|\u0433\u043E\u0434)", 'g');
    return text.replace(re, '$1$20' + settings.dash + '$40$5');
  },
  settings: {
    dash: "\u2013" // &ndash;

  }
};

var directSpeech = {
  name: 'ru/dash/directSpeech',
  handler: function handler(text) {
    var dashes = getData('common/dash');
    var re1 = new RegExp("([\"\xBB\u2018\u201C,])[ |\xA0]?(".concat(dashes, ")[ |\xA0]"), 'g');
    var re2 = new RegExp("(^|".concat(privateLabel, ")(").concat(dashes, ")( |\xA0)"), 'gm');
    var re3 = new RegExp("([.\u2026?!])[ \xA0](".concat(dashes, ")[ \xA0]"), 'g');
    return text.replace(re1, "$1\xA0\u2014 ").replace(re2, "$1\u2014\xA0").replace(re3, "$1 \u2014\xA0");
  }
};

var izpod = {
  name: 'ru/dash/izpod',
  handler: function handler(text) {
    var re = new RegExp(getData('ru/dashBefore') + '(И|и)з под' + getData('ru/dashAfter'), 'g');
    return text.replace(re, '$1$2з-под');
  }
};

var izza = {
  name: 'ru/dash/izza',
  handler: function handler(text) {
    var re = new RegExp(getData('ru/dashBefore') + '(И|и)з за' + getData('ru/dashAfter'), 'g');
    return text.replace(re, '$1$2з-за');
  }
};

var ka = {
  name: 'ru/dash/ka',
  handler: function handler(text) {
    var re = new RegExp('([a-яё]+) ка(сь)?' + getData('ru/dashAfter'), 'g');
    return text.replace(re, '$1-ка$2');
  }
};

var koe = {
  name: 'ru/dash/koe',
  handler: function handler(text) {
    var re = new RegExp(getData('ru/dashBefore') + '([Кк]о[ей])\\s([а-яё]{3,})' + getData('ru/dashAfter'), 'g');
    return text.replace(re, '$1$2-$3');
  }
};

var main$1 = {
  name: 'ru/dash/main',
  index: '-5',
  handler: function handler(text) {
    var dashes = getData('common/dash');
    var re = new RegExp("([ \xA0])(" + dashes + ")([ \xA0\\n])", 'g');
    return text.replace(re, "\xA0\u2014$3");
  }
};

var month = {
  name: 'ru/dash/month',
  handler: function handler(text, settings) {
    var months = '(' + getData('ru/month') + ')';
    var monthsPre = '(' + getData('ru/monthPreCase') + ')';
    var dashes = getData('common/dash');
    var re = new RegExp(months + ' ?(' + dashes + ') ?' + months, 'gi');
    var rePre = new RegExp(monthsPre + ' ?(' + dashes + ') ?' + monthsPre, 'gi');
    var newSubStr = '$1' + settings.dash + '$3';
    return text.replace(re, newSubStr).replace(rePre, newSubStr);
  },
  settings: {
    dash: "\u2013" // &ndash;

  }
};

var surname = {
  name: 'ru/dash/surname',
  handler: function handler(text) {
    var re = new RegExp('([А-ЯЁ][а-яё]+)\\s-([а-яё]{1,3})(?![^а-яё]|$)', 'g');
    return text.replace(re, "$1\xA0\u2014$2");
  }
};

var taki = {
  name: 'ru/dash/taki',
  handler: function handler(text) {
    var re = new RegExp('(верно|довольно|опять|прямо|так|вс[её]|действительно|неужели)\\s(таки)' + getData('ru/dashAfter'), 'g');
    return text.replace(re, '$1-$2');
  }
};

var time = {
  name: 'ru/dash/time',
  handler: function handler(text, settings) {
    var re = new RegExp(getData('ru/dashBefore') + '(\\d?\\d:[0-5]\\d)' + getData('common/dash') + '(\\d?\\d:[0-5]\\d)' + getData('ru/dashAfter'), 'g');
    return text.replace(re, '$1$2' + settings.dash + '$3');
  },
  settings: {
    dash: "\u2013" // &ndash;

  }
};

var to = {
  name: 'ru/dash/to',
  handler: function handler(text) {
    var words = ['откуда', 'куда', 'где', 'когда', 'зачем', 'почему', 'как', 'како[ейм]', 'какая', 'каки[емх]', 'какими', 'какую', 'что', 'чего', 'че[йм]', 'чьим?', 'кто', 'кого', 'кому', 'кем'];
    var re = new RegExp('(' + words.join('|') + ')( | -|- )(то|либо|нибудь)' + getData('ru/dashAfter'), 'gi');
    return text.replace(re, '$1-$3');
  }
};

var weekday = {
  name: 'ru/dash/weekday',
  handler: function handler(text, settings) {
    var part = '(' + getData('ru/weekday') + ')';
    var re = new RegExp(part + ' ?(' + getData('common/dash') + ') ?' + part, 'gi');
    return text.replace(re, '$1' + settings.dash + '$3');
  },
  settings: {
    dash: "\u2013" // &ndash;

  }
};

var years = {
  name: 'ru/dash/years',
  handler: function handler(text, settings) {
    var dashes = getData('common/dash');
    var re = new RegExp("(\\D|^)(\\d{4})[ \xA0]?(" + dashes + ")[ \xA0]?(\\d{4})(?=[ \xA0]?\u0433)", 'g');
    return text.replace(re, function ($0, $1, $2, $3, $4) {
      if (parseInt($2, 10) < parseInt($4, 10)) {
        return $1 + $2 + settings.dash + $4;
      }

      return $0;
    });
  },
  settings: {
    dash: "\u2013" // &ndash;

  }
};

Typograf.addRules([centuries, daysMonth, de$1, decade, directSpeech, izpod, izza, ka, koe, main$1, month, surname, taki, time, to, weekday, years]);

var fromISO = {
  name: 'ru/date/fromISO',
  handler: function handler(text) {
    var sp1 = '(-|\\.|\\/)';
    var sp2 = '(-|\\/)';
    var re1 = new RegExp('(^|\\D)(\\d{4})' + sp1 + '(\\d{2})' + sp1 + '(\\d{2})(\\D|$)', 'gi');
    var re2 = new RegExp('(^|\\D)(\\d{2})' + sp2 + '(\\d{2})' + sp2 + '(\\d{4})(\\D|$)', 'gi');
    return text.replace(re1, '$1$6.$4.$2$7').replace(re2, '$1$4.$2.$6$7');
  }
};

var weekday$1 = {
  name: 'ru/date/weekday',
  handler: function handler(text) {
    var space = "( |\xA0)";
    var monthCase = getData('ru/monthGenCase');
    var weekday = getData('ru/weekday');
    var re = new RegExp('(\\d)' + space + '(' + monthCase + '),' + space + '(' + weekday + ')', 'gi');
    return text.replace(re, function () {
      var a = arguments;
      return a[1] + a[2] + a[3].toLowerCase() + ',' + a[4] + a[5].toLowerCase();
    });
  }
};

Typograf.addRules([fromISO, weekday$1]);

var currency = {
  name: 'ru/money/currency',
  handler: function handler(text) {
    var currency = '([$€¥Ұ£₤₽])';
    var space = "[ \xA0\u2009\u202F]";
    var re1 = new RegExp('(^|[\\D]{2})' + currency + ' ?([\\d.,]+(' + space + '\\d{3})*)(' + space + '?(тыс\\.|млн|млрд|трлн))?', 'gm');
    var re2 = new RegExp('(^|[\\D])([\\d.,]+) ?' + currency, 'gm');
    return text.replace(re1, function ($0, $1, $2, $3, $4, $5, $6) {
      return $1 + $3 + ($6 ? "\xA0" + $6 : '') + "\xA0" + $2;
    }).replace(re2, "$1$2\xA0$3");
  },
  disabled: true
};

var ruble = {
  name: 'ru/money/ruble',
  handler: function handler(text) {
    var newSubstr = "$1\xA0\u20BD";
    var commonPart = "(\\d+)( |\xA0)?(\u0440|\u0440\u0443\u0431)\\.";
    var re1 = new RegExp('^' + commonPart + '$', 'g');
    var re2 = new RegExp(commonPart + '(?=[!?,:;])', 'g');
    var re3 = new RegExp(commonPart + '(?=\\s+[A-ЯЁ])', 'g');
    return text.replace(re1, newSubstr).replace(re2, newSubstr).replace(re3, newSubstr + '.');
  },
  disabled: true
};

Typograf.addRules([currency, ruble]);

var abbr = {
  name: 'ru/nbsp/abbr',
  handler: function handler(text) {
    function abbr($0, $1, $2, $3) {
      // дд.мм.гггг
      if ($2 === 'дд' && $3 === 'мм') {
        return $0;
      } // Являются ли сокращения ссылкой


      if (['рф', 'ру', 'рус', 'орг', 'укр', 'бг', 'срб'].indexOf($3) > -1) {
        return $0;
      }

      return $1 + $2 + '.' + "\xA0" + $3 + '.';
    }

    var re = new RegExp("(^|\\s|".concat(privateLabel, ")([\u0430-\u044F\u0451]{1,3})\\. ?([\u0430-\u044F\u0451]{1,3})\\."), 'g');
    return text.replace(re, abbr) // Для тройных сокращений - а.е.м.
    .replace(re, abbr);
  }
};

var addr = {
  name: 'ru/nbsp/addr',
  handler: function handler(text) {
    return text.replace(/(\s|^)(дом|д\.|кв\.|под\.|п-д) *(\d+)/gi, "$1$2\xA0$3").replace(/(\s|^)(мкр-н|мк-н|мкр\.|мкрн)\s/gi, "$1$2\xA0") // микрорайон
    .replace(/(\s|^)(эт\.) *(-?\d+)/gi, "$1$2\xA0$3").replace(/(\s|^)(\d+) +этаж([^а-яё]|$)/gi, "$1$2\xA0\u044D\u0442\u0430\u0436$3").replace(/(\s|^)литер\s([А-Я]|$)/gi, "$1\u043B\u0438\u0442\u0435\u0440\xA0$2")
    /*
        область, край, станция, поселок, село,
        деревня, улица, переулок, проезд, проспект,
        бульвар, площадь, набережная, шоссе,
        тупик, офис, комната, участок, владение, строение, корпус
    */
    .replace(/(\s|^)(обл|кр|ст|пос|с|д|ул|пер|пр|пр-т|просп|пл|бул|б-р|наб|ш|туп|оф|комн?|уч|вл|влад|стр|кор)\. *([а-яёa-z\d]+)/gi, "$1$2.\xA0$3") // город
    .replace(/(\D[ \u00A0]|^)г\. ?([А-ЯЁ])/gm, "$1\u0433.\xA0$2");
  }
};

var afterNumberSign = {
  name: 'ru/nbsp/afterNumberSign',
  handler: function handler(text) {
    // \u2009 - THIN SPACE
    // \u202F - NARROW NO-BREAK SPACE
    return text.replace(/№[ \u00A0\u2009]?(\d|п\/п)/g, "\u2116\u202F$1");
  }
};

var beforeParticle = {
  name: 'ru/nbsp/beforeParticle',
  index: '+5',
  handler: function handler(text) {
    var particles = '(ли|ль|же|ж|бы|б)';
    var re1 = new RegExp('([А-ЯЁа-яё]) ' + particles + '(?=[,;:?!"‘“»])', 'g');
    var re2 = new RegExp("([\u0410-\u042F\u0401\u0430-\u044F\u0451])[ \xA0]" + particles + "[ \xA0]", 'g');
    return text.replace(re1, "$1\xA0$2").replace(re2, "$1\xA0$2 ");
  }
};

var centuries$1 = {
  name: 'ru/nbsp/centuries',
  handler: function handler(text) {
    var dashes = getData('common/dash');
    var before = '(^|\\s)([VIX]+)';
    var after = '(?=[,;:?!"‘“»]|$)';
    var re1 = new RegExp(before + "[ \xA0]?\u0432\\.?" + after, 'gm');
    var re2 = new RegExp(before + '(' + dashes + ')' + "([VIX]+)[ \xA0]?\u0432\\.?([ \xA0]?\u0432\\.?)?" + after, 'gm');
    return text.replace(re1, "$1$2\xA0\u0432.").replace(re2, "$1$2$3$4\xA0\u0432\u0432.");
  }
};

var dayMonth = {
  name: 'ru/nbsp/dayMonth',
  handler: function handler(text) {
    var re = new RegExp('(\\d{1,2}) (' + getData('ru/shortMonth') + ')', 'gi');
    return text.replace(re, "$1\xA0$2");
  }
};

var initials = {
  name: 'ru/nbsp/initials',
  handler: function handler(text) {
    var spaces = "\xA0\u202F "; // nbsp, thinsp

    var quote = getData('ru/quote');
    var re = new RegExp('(^|[' + spaces + quote.left + privateLabel + '"])([А-ЯЁ])\\.[' + spaces + ']?([А-ЯЁ])\\.[' + spaces + ']?([А-ЯЁ][а-яё]+)(?=[\\s.,;:?!"' + quote.right + ']|$)', 'gm');
    return text.replace(re, "$1$2.\xA0$3.\xA0$4");
  }
};

var m = {
  name: 'ru/nbsp/m',
  index: '+5',
  handler: function handler(text) {
    var re = new RegExp('(^|[\\s,.\\(' + privateLabel + '])' + "(\\d+)[ \xA0]?(\u043C\u043C?|\u0441\u043C|\u043A\u043C|\u0434\u043C|\u0433\u043C|mm?|km|cm|dm)([23\xB2\xB3])?([\\s\\).!?,;" + privateLabel + ']|$)', 'gm');
    return text.replace(re, function ($0, $1, $2, $3, $4, $5) {
      var pow = {
        '2': '²',
        '²': '²',
        '3': '³',
        '³': '³',
        '': ''
      }[$4 || ''];
      return $1 + $2 + "\xA0" + $3 + pow + ($5 === "\xA0" ? ' ' : $5);
    });
  }
};

var mln = {
  name: 'ru/nbsp/mln',
  handler: function handler(text) {
    return text.replace(/(\d) ?(тыс|млн|млрд|трлн)(\.|\s|$)/gi, "$1\xA0$2$3");
  }
};

var ooo = {
  name: 'ru/nbsp/ooo',
  handler: function handler(text) {
    return text.replace(/(^|[^a-яёA-ЯЁ])(ООО|ОАО|ЗАО|НИИ|ПБОЮЛ) /g, "$1$2\xA0");
  }
};

var page = {
  name: 'ru/nbsp/page',
  handler: function handler(text) {
    var re = new RegExp('(^|[)\\s' + privateLabel + '])' + '(стр|гл|рис|илл?|ст|п|c)\\. *(\\d+)([\\s.,?!;:]|$)', 'gim');
    return text.replace(re, "$1$2.\xA0$3$4");
  }
};

var ps = {
  name: 'ru/nbsp/ps',
  handler: function handler(text) {
    var re = new RegExp("(^|\\s|".concat(privateLabel, ")[p\u0437]\\.[ \xA0]?([p\u0437]\\.[ \xA0]?)?[s\u044B]\\.:? "), 'gim');
    return text.replace(re, function ($0, $1, $2) {
      return $1 + ($2 ? "P.\xA0P.\xA0S. " : "P.\xA0S. ");
    });
  }
};

var rubleKopek = {
  name: 'ru/nbsp/rubleKopek',
  handler: function handler(text) {
    return text.replace(/(\d) ?(?=(руб|коп)\.)/g, "$1\xA0");
  }
};

var see = {
  name: 'ru/nbsp/see',
  handler: function handler(text) {
    var re = new RegExp("(^|\\s|".concat(privateLabel, "|\\()(\u0441\u043C|\u0438\u043C)\\.[ \xA0]?([\u0430-\u044F\u04510-9a-z]+)([\\s.,?!]|$)"), 'gi');
    return text.replace(re, function ($0, $1, $2, $3, $4) {
      return ($1 === "\xA0" ? ' ' : $1) + $2 + ".\xA0" + $3 + $4;
    });
  }
};

var year = {
  name: 'ru/nbsp/year',
  handler: function handler(text) {
    return text.replace(/(^|\D)(\d{4}) ?г([ ,;.\n]|$)/g, "$1$2\xA0\u0433$3");
  }
};

var years$1 = {
  name: 'ru/nbsp/years',
  index: '+5',
  handler: function handler(text) {
    var dashes = getData('common/dash');
    var re = new RegExp('(^|\\D)(\\d{4})(' + dashes + ")(\\d{4})[ \xA0]?\u0433\\.?([ \xA0]?\u0433\\.)?(?=[,;:?!\"\u2018\u201C\xBB\\s]|$)", 'gm');
    return text.replace(re, "$1$2$3$4\xA0\u0433\u0433.");
  }
};

Typograf.addRules([abbr, addr, afterNumberSign, beforeParticle, centuries$1, dayMonth, initials, m, mln, ooo, page, ps, rubleKopek, see, year, years$1]);

var comma = {
  name: 'ru/number/comma',
  handler: function handler(text) {
    // \u00A0 - NO-BREAK SPACE
    // \u2009 - THIN SPACE
    // \u202F - NARROW NO-BREAK SPACE
    return text.replace(/(^|\s)(\d+)\.(\d+[\u00A0\u2009\u202F ]*?[%‰°×x])/gim, '$1$2,$3');
  }
};

var ordinals = {
  name: 'ru/number/ordinals',
  handler: function handler(text, settings, context) {
    var re = new RegExp('(\\d[%‰]?)-(ый|ой|ая|ое|ые|ым|ом|ых|ого|ому|ыми)(?![' + context.getData('char') + '])', 'g');
    return text.replace(re, function ($0, $1, $2) {
      var parts = {
        'ой': 'й',
        'ый': 'й',
        'ая': 'я',
        'ое': 'е',
        'ые': 'е',
        'ым': 'м',
        'ом': 'м',
        'ых': 'х',
        'ого': 'го',
        'ому': 'му',
        'ыми': 'ми'
      };
      return $1 + '-' + parts[$2];
    });
  }
};

Typograf.addRules([comma, ordinals]);

function removeOptAlignTags(text, classNames) {
  var re = new RegExp('<span class="(' + classNames.join('|') + ')">([^]*?)</span>', 'g');
  return text.replace(re, '$2');
}
function removeOptAlignTagsFromTitle(text, classNames) {
  return text.replace(/<title>[^]*?<\/title>/i, function (text) {
    return removeOptAlignTags(text, classNames);
  });
}

var classNames = ['typograf-oa-lbracket', 'typograf-oa-n-lbracket', 'typograf-oa-sp-lbracket'];
var name = 'ru/optalign/bracket';
var bracket$1 = {
  name: name,
  handler: function handler(text) {
    return text.replace(/( |\u00A0)\(/g, '<span class="typograf-oa-sp-lbracket">$1</span><span class="typograf-oa-lbracket">(</span>').replace(/^\(/gm, '<span class="typograf-oa-n-lbracket">(</span>');
  },
  disabled: true,
  htmlAttrs: false
};
var innerStartBracket = {
  name: name,
  queue: 'start',
  handler: function handler(text) {
    return removeOptAlignTags(text, classNames);
  }
};
var innerEndBracket = {
  name: name,
  queue: 'end',
  handler: function handler(text) {
    return removeOptAlignTagsFromTitle(text, classNames);
  }
};

var classNames$1 = ['typograf-oa-comma', 'typograf-oa-comma-sp'];
var name$1 = 'ru/optalign/comma';
var comma$1 = {
  name: name$1,
  handler: function handler(text, settings, context) {
    var re = new RegExp('([' + context.getData('char') + "\\d\u0301]+), ", 'gi');
    return text.replace(re, '$1<span class="typograf-oa-comma">,</span><span class="typograf-oa-comma-sp"> </span>');
  },
  disabled: true,
  htmlAttrs: false
};
var innerStartComma = {
  name: name$1,
  queue: 'start',
  handler: function handler(text) {
    return removeOptAlignTags(text, classNames$1);
  }
};
var innerEndComma = {
  name: name$1,
  queue: 'end',
  handler: function handler(text) {
    return removeOptAlignTagsFromTitle(text, classNames$1);
  }
};

var classNames$2 = ['typograf-oa-lquote', 'typograf-oa-n-lquote', 'typograf-oa-sp-lquote'];
var name$2 = 'ru/optalign/quote';
var quote$1 = {
  name: name$2,
  handler: function handler(text) {
    var quote = this.getSetting('common/punctuation/quote', 'ru');
    var lquotes = '([' + quote.left[0] + (quote.left[1] || '') + '])';
    var reNewLine = new RegExp('(^|\n\n|' + privateLabel + ')(' + lquotes + ')', 'g');
    var reInside = new RegExp('([^\n' + privateLabel + "])([ \xA0\n])(" + lquotes + ')', 'gi');
    return text.replace(reNewLine, '$1<span class="typograf-oa-n-lquote">$2</span>').replace(reInside, '$1<span class="typograf-oa-sp-lquote">$2</span><span class="typograf-oa-lquote">$3</span>');
  },
  disabled: true,
  htmlAttrs: false
};
var innerStartQuote = {
  name: name$2,
  queue: 'start',
  handler: function handler(text) {
    return removeOptAlignTags(text, classNames$2);
  }
};
var innerEndQuote = {
  name: name$2,
  queue: 'end',
  handler: function handler(text) {
    return removeOptAlignTagsFromTitle(text, classNames$2);
  }
};

Typograf.addRules([bracket$1, comma$1, quote$1]);
Typograf.addInnerRules([innerStartBracket, innerEndBracket, innerStartComma, innerEndComma, innerStartQuote, innerEndQuote]);

var accent = {
  name: 'ru/other/accent',
  handler: function handler(text) {
    return text.replace(/([а-яё])([АЕЁИОУЫЭЮЯ])([^А-ЯЁ\w]|$)/g, function ($0, $1, $2, $3) {
      return $1 + $2.toLowerCase() + "\u0301" + $3;
    });
  },
  disabled: true
};

var defaultCityCodeLength = 5;
var countryCode = '7';
var exceptions = [];
var exceptionsMax = 8;
var exceptionsMin = 2;
[4162, 416332, 8512, 851111, 4722, 4725, 391379, 8442, 4732, 4152, 4154451, 4154459, 4154455, 41544513, 8142, 8332, 8612, 8622, 3525, 812, 8342, 8152, 3812, 4862, 3422, 342633, 8112, 9142, 8452, 3432, 3434, 3435, 4812, 8432, 8439, 3822, 4872, 3412, 3511, 3512, 3022, 4112, 4852, 4855, 3852, 3854, 8182, 818, 90, 3472, 4741, 4764, 4832, 4922, 8172, 8202, 8722, 4932, 493, 3952, 3951, 3953, 411533, 4842, 3842, 3843, 8212, 4942, '39131-39179', '39190-39199', 391, 4712, 4742, 8362, 495, 499, 4966, 4964, 4967, 498, 8312, 8313, 3832, 383612, 3532, 8412, 4232, 423370, 423630, 8632, 8642, 8482, 4242, 8672, 8652, 4752, 4822, 482502, 4826300, 3452, 8422, 4212, 3466, 3462, 8712, 8352, '901-934', '936-939', '950-953', 958, '960-969', '977-989', '991-997', 999].forEach(function (num) {
  if (typeof num === 'string') {
    var buf = num.split('-');

    for (var i = +buf[0]; i <= +buf[1]; i++) {
      exceptions.push(i);
    }
  } else {
    exceptions.push(num);
  }
});

function phone(num) {
  var firstSym = num[0];
  var cityCode = '';
  var hasPlusWithCode;
  var hasEight;

  if (num.length < 8) {
    return phoneBlocks(num);
  } // 8 495 123-45-67, +7 495 123-45-67


  if (num.length > 10) {
    if (firstSym === '+') {
      if (num[1] === countryCode) {
        hasPlusWithCode = true;
        num = num.substr(2);
      } else {
        return num;
      }
    } else if (firstSym === '8') {
      hasEight = true;
      num = num.substr(1);
    }
  }

  for (var cityCodeLen = exceptionsMax; cityCodeLen >= exceptionsMin; cityCodeLen--) {
    var code = +num.substr(0, cityCodeLen);

    if (exceptions.indexOf(code) > -1) {
      cityCode = num.substr(0, cityCodeLen);
      num = num.substr(cityCodeLen);
      break;
    }
  }

  if (!cityCode) {
    cityCode = num.substr(0, defaultCityCodeLength);
    num = num.substr(defaultCityCodeLength);
  }

  return (hasPlusWithCode ? '+' + countryCode + "\xA0" : '') + (hasEight ? "8\xA0" : '') + prepareCode(cityCode) + "\xA0" + phoneBlocks(num);
}

function prepareCode(code) {
  var numCode = +code;
  var len = code.length;
  var result = [code];
  var withoutBrackets = false;

  if (len > 3) {
    switch (len) {
      case 4:
        result = [code.substr(0, 2), code.substr(2, 2)];
        break;

      case 5:
        result = [code.substr(0, 3), code.substr(3, 3)];
        break;

      case 6:
        result = [code.substr(0, 2), code.substr(2, 2), code.substr(4, 2)];
        break;
    }
  } else {
    // Мобильные и московские номера без скобок
    withoutBrackets = numCode > 900 && numCode <= 999 || numCode === 495 || numCode === 499;
  }

  result = result.join('-');
  return withoutBrackets ? result : '(' + result + ')';
}

function phoneBlocks(num) {
  var add = '';

  if (num.length % 2) {
    add = num[0];
    add += num.length <= 5 ? '-' : '';
    num = num.substr(1, num.length - 1);
  }

  return add + num.split(/(?=(?:\d\d)+$)/).join('-');
}

function clearPhone(text) {
  return text.replace(/[^\d+]/g, '');
}

var phoneNumber = {
  name: 'ru/other/phone-number',
  live: false,
  handler: function handler(text) {
    var re = new RegExp('(^|,| |' + privateLabel + ")(\\+7[\\d\\(\\) \xA0-]{10,18})(?=,|;|" + privateLabel + '|$)', 'gm');
    return text.replace(re, function ($0, $1, $2) {
      var buf = clearPhone($2);
      return buf.length === 12 ? $1 + phone(buf) : $0;
    }).replace( // eslint-disable-next-line no-misleading-character-class
    /(^|[^а-яё])([☎☏✆📠📞📱]|т\.|тел\.|ф\.|моб\.|факс|сотовый|мобильный|телефон)(:?\s*?)([+\d(][\d \u00A0\-()]{3,}\d)/gi, function ($0, $1, $2, $3, $4) {
      var buf = clearPhone($4);

      if (buf.length >= 5) {
        return $1 + $2 + $3 + phone(buf);
      }

      return $0;
    });
  }
};

Typograf.addRules([accent, phoneNumber]);

var ano = {
  name: 'ru/punctuation/ano',
  handler: function handler(text) {
    var re = new RegExp("([^!?,:;\\-\u2012\u2013\u2014\\s])(\\s+)(\u0430|\u043D\u043E)(?= |\xA0|\\n)", 'g');
    return text.replace(re, '$1,$2$3');
  },
  // Запятая может идти после ссылки.
  queue: 'hide-safe-tags-html'
};

var exclamation = {
  name: 'ru/punctuation/exclamation',
  live: false,
  handler: function handler(text) {
    return text.replace(/(^|[^!])!{2}($|[^!])/gm, '$1!$2').replace(/(^|[^!])!{4}($|[^!])/gm, '$1!!!$2');
  }
};

var exclamationQuestion = {
  name: 'ru/punctuation/exclamationQuestion',
  index: '+5',
  handler: function handler(text) {
    var re = new RegExp('(^|[^!])!\\?([^?]|$)', 'g');
    return text.replace(re, '$1?!$2');
  }
};

var hellipQuestion = {
  name: 'ru/punctuation/hellipQuestion',
  handler: function handler(text) {
    return text.replace(/(^|[^.])(\.\.\.|…),/g, '$1…').replace(/(!|\?)(\.\.\.|…)(?=[^.]|$)/g, '$1..');
  }
};

Typograf.addRules([ano, exclamation, exclamationQuestion, hellipQuestion]);

var afterHellip = {
  name: 'ru/space/afterHellip',
  handler: function handler(text) {
    return text.replace(/([а-яё])(\.\.\.|…)([А-ЯЁ])/g, '$1$2 $3').replace(/([?!]\.\.)([а-яёa-z])/gi, '$1 $2');
  }
};

var year$1 = {
  name: 'ru/space/year',
  handler: function handler(text, settings, context) {
    var re = new RegExp("(^| |\xA0)(\\d{3,4})(\u0433\u043E\u0434([\u0430\u0443\u0435]|\u043E\u043C)?)([^" + context.getData('char') + ']|$)', 'g');
    return text.replace(re, '$1$2 $3$5');
  }
};

Typograf.addRules([afterHellip, year$1]);

var nn = {
  name: 'ru/symbols/NN',
  handler: function handler(text) {
    return text.replace(/№№/g, '№');
  }
};

Typograf.addRules([nn]);

var replacements = {
  A: 'А',
  // Latin: Russian
  a: 'а',
  B: 'В',
  E: 'Е',
  e: 'е',
  K: 'К',
  M: 'М',
  H: 'Н',
  O: 'О',
  o: 'о',
  P: 'Р',
  p: 'р',
  C: 'С',
  c: 'с',
  T: 'Т',
  y: 'у',
  X: 'Х',
  x: 'х'
};
var keys = Object.keys(replacements).join('');
var switchingKeyboardLayout = {
  name: 'ru/typo/switchingKeyboardLayout',
  handler: function handler(text) {
    var re = new RegExp('([' + keys + ']{1,3})(?=[А-ЯЁа-яё]+?)', 'g');
    return text.replace(re, function (str, $1) {
      var result = '';

      for (var i = 0; i < $1.length; i++) {
        result += replacements[$1[i]];
      }

      return result;
    });
  }
};

Typograf.addRules([switchingKeyboardLayout]);

var titles = {
  "common/html/e-mail": {
    "en-US": "Placement of links for e-mail",
    "ru": "Расстановка ссылок для эл. почты"
  },
  "common/html/escape": {
    "en-US": "Escaping HTML",
    "ru": "Экранирование HTML"
  },
  "common/html/nbr": {
    "en-US": "Replacement line break on <br/>",
    "ru": "Замена перевода строки на <br/>"
  },
  "common/html/p": {
    "en-US": "Placement of paragraph",
    "ru": "Расстановка абзацев"
  },
  "common/html/processingAttrs": {
    "en-US": "Processing HTML attributes",
    "ru": "Типографирование HTML-атрибутов"
  },
  "common/html/quot": {
    "common": "&⁠quot; → \""
  },
  "common/html/stripTags": {
    "en-US": "Removing HTML-tags",
    "ru": "Удаление HTML-тегов"
  },
  "common/html/url": {
    "en-US": "Placement of links",
    "ru": "Расстановка ссылок"
  },
  "common/nbsp/afterNumber": {
    "en-US": "Non-breaking space between number and word",
    "ru": "Нераз. пробел между числом и словом"
  },
  "common/nbsp/afterParagraphMark": {
    "en-US": "Non-breaking space after ¶",
    "ru": "Нераз. пробел после ¶"
  },
  "common/nbsp/afterSectionMark": {
    "en-US": "Non-breaking space after §",
    "ru": "Нераз. узкий пробел после §"
  },
  "common/nbsp/afterShortWord": {
    "en-US": "Non-breaking space after short word",
    "ru": "Нераз. пробел после короткого слова"
  },
  "common/nbsp/beforeShortLastNumber": {
    "en-US": "Non-breaking space before number (maximum 2 digits) at end of sentence",
    "ru": "Нераз. пробел перед числом (не более 2 цифр) в конце предложения"
  },
  "common/nbsp/beforeShortLastWord": {
    "en-US": "Non-breaking space before last short word in sentence",
    "ru": "Нераз. пробел перед последним коротким словом в предложении"
  },
  "common/nbsp/dpi": {
    "en-US": "Non-breaking space before lpi and dpi",
    "ru": "Нераз. пробел перед lpi и dpi"
  },
  "common/nbsp/nowrap": {
    "en-US": "Replace non-breaking space to normal space in tags nowrap and nobr",
    "ru": "Заменять нераз. пробел на обычный пробел в тегах nowrap и nobr"
  },
  "common/nbsp/replaceNbsp": {
    "en-US": "Replacing non-breaking space on normal before text correction",
    "ru": "Замена неразрывного пробела на обычный перед типографированием"
  },
  "common/number/digitGrouping": {
    "en-US": "Divide into groups numbers with many digits",
    "ru": "Разбивать длинные числа по разрядам"
  },
  "common/number/fraction": {
    "common": "1/2 → ½, 1/4 → ¼, 3/4 → ¾"
  },
  "common/number/mathSigns": {
    "common": "!= → ≠, <= → ≤, >= → ≥, ~= → ≅, +- → ±"
  },
  "common/number/times": {
    "common": "x → × (10 x 5 → 10×5)"
  },
  "common/other/delBOM": {
    "en-US": "Delete character BOM (Byte Order Mark)",
    "ru": "Удаление символа BOM (Byte Order Mark)"
  },
  "common/other/repeatWord": {
    "en-US": "Removing repeat words",
    "ru": "Удаление повтора слова"
  },
  "common/punctuation/apostrophe": {
    "en-US": "Placement of correct apostrophe",
    "ru": "Расстановка правильного апострофа"
  },
  "common/punctuation/delDoublePunctuation": {
    "en-US": "Removing double punctuation",
    "ru": "Удаление двойной пунктуации"
  },
  "common/punctuation/hellip": {
    "en-US": "Replacement of three points by ellipsis",
    "ru": "Замена трёх точек на многоточие"
  },
  "common/punctuation/quote": {
    "en-US": "Placement of quotation marks in texts",
    "ru": "Расстановка кавычек правильного вида"
  },
  "common/punctuation/quoteLink": {
    "en-US": "Removal quotes outside a link",
    "ru": "Вынос кавычек за пределы ссылки"
  },
  "common/space/afterPunctuation": {
    "en-US": "space after punctuation",
    "ru": "Пробел после знаков пунктуации"
  },
  "common/space/beforeBracket": {
    "en-US": "Space before opening bracket",
    "ru": "Пробел перед открывающей скобкой"
  },
  "common/space/bracket": {
    "en-US": "Remove extra spaces after opening and before closing bracket",
    "ru": "Удаление лишних пробелов после открывающей и перед закрывающей скобкой"
  },
  "common/space/delBeforePercent": {
    "en-US": "Remove space before %, ‰ and ‱",
    "ru": "Удаление пробела перед %, ‰ и ‱"
  },
  "common/space/delBeforePunctuation": {
    "en-US": "Remove spaces before punctuation",
    "ru": "Удаление пробелов перед знаками пунктуации"
  },
  "common/space/delLeadingBlanks": {
    "en-US": "Remove spaces at start of line",
    "ru": "Удаление пробелов в начале строки"
  },
  "common/space/delRepeatN": {
    "en-US": "Remove duplicate line breaks",
    "ru": "Удаление повторяющихся переносов строки"
  },
  "common/space/delRepeatSpace": {
    "en-US": "Removing duplicate spaces between characters",
    "ru": "Удаление повторяющихся пробелов между символами"
  },
  "common/space/delTrailingBlanks": {
    "en-US": "Remove spaces at end of line",
    "ru": "Удаление пробелов в конце строки"
  },
  "common/space/insertFinalNewline": {
    "en-US": "Insert final newline",
    "ru": "Вставить в конце текста перевод строки"
  },
  "common/space/replaceTab": {
    "en-US": "Replacement of tab to 4 spaces",
    "ru": "Замена таба на 4 пробела"
  },
  "common/space/squareBracket": {
    "en-US": "Remove extra spaces after opening and before closing square bracket",
    "ru": "Удаление лишних пробелов после открывающей и перед закрывающей квадратной скобкой"
  },
  "common/space/trimLeft": {
    "en-US": "Remove spaces and line breaks in beginning of text",
    "ru": "Удаление пробелов и переносов строк в начале текста"
  },
  "common/space/trimRight": {
    "en-US": "Remove spaces and line breaks at end of text",
    "ru": "Удаление пробелов и переносов строк в конце текста"
  },
  "common/symbols/arrow": {
    "common": "-> → →, <- → ←"
  },
  "common/symbols/cf": {
    "en-US": "Adding ° to C and F",
    "ru": "Добавление ° к C и F"
  },
  "common/symbols/copy": {
    "common": "(c) → ©, (tm) → ™, (r) → ®"
  },
  "en-US/dash/main": {
    "en-US": "Replace hyphens surrounded by spaces with an em-dash",
    "ru": "Замена дефиса на длинное тире"
  },
  "ru/dash/centuries": {
    "en-US": "Hyphen to dash in centuries",
    "ru": "Замена дефиса на тире в веках"
  },
  "ru/dash/daysMonth": {
    "en-US": "Dash between days of one month",
    "ru": "Тире между днями одного месяца"
  },
  "ru/dash/de": {
    "en-US": "Hyphen before “де”",
    "ru": "Дефис перед «де»"
  },
  "ru/dash/decade": {
    "en-US": "Dash in decade",
    "ru": "Тире в десятилетиях, 80—90-е гг."
  },
  "ru/dash/directSpeech": {
    "en-US": "Dash in direct speech",
    "ru": "Тире в прямой речи"
  },
  "ru/dash/izpod": {
    "en-US": "Hyphen between “из-под”",
    "ru": "Дефис между «из-под»"
  },
  "ru/dash/izza": {
    "en-US": "Hyphen between “из-за”",
    "ru": "Дефис между «из-за»"
  },
  "ru/dash/ka": {
    "en-US": "Hyphen before “ка” and “кась”",
    "ru": "Дефис перед «ка» и «кась»"
  },
  "ru/dash/koe": {
    "en-US": "Hyphen after “кое” and “кой”",
    "ru": "Дефис после «кое» и «кой»"
  },
  "ru/dash/main": {
    "en-US": "Replacement hyphen with dash",
    "ru": "Замена дефиса на тире"
  },
  "ru/dash/month": {
    "en-US": "Dash between months",
    "ru": "Тире между месяцами"
  },
  "ru/dash/surname": {
    "en-US": "Acronyms with a dash",
    "ru": "Сокращения с помощью тире"
  },
  "ru/dash/taki": {
    "en-US": "Hyphen between “верно-таки” and etc.",
    "ru": "Дефис между «верно-таки» и т. д."
  },
  "ru/dash/time": {
    "en-US": "Dash in time intervals",
    "ru": "Тире в интервалах времени"
  },
  "ru/dash/to": {
    "en-US": "Hyphen before “то”, “либо”, “нибудь”",
    "ru": "Дефис перед «то», «либо», «нибудь»"
  },
  "ru/dash/weekday": {
    "en-US": "Dash between the days of the week",
    "ru": "Тире между днями недели"
  },
  "ru/dash/years": {
    "en-US": "Hyphen to dash in years",
    "ru": "Замена дефиса на тире в годах"
  },
  "ru/date/fromISO": {
    "en-US": "Converting dates YYYY-MM-DD type DD.MM.YYYY",
    "ru": "Преобразование дат YYYY-MM-DD к виду DD.MM.YYYY"
  },
  "ru/date/weekday": {
    "common": "2 Мая, Понедельник → 2 мая, понедельник"
  },
  "ru/money/currency": {
    "en-US": "Currency symbol ($, €, ¥, Ұ, £ and ₤) after the number, $100 → 100 $",
    "ru": "Символ валюты ($, €, ¥, Ұ, £ и ₤) после числа, $100 → 100 $"
  },
  "ru/money/ruble": {
    "common": "1 руб. → 1 ₽"
  },
  "ru/nbsp/abbr": {
    "en-US": "Non-breaking space in abbreviations, e.g. “т. д.”",
    "ru": "Нераз. пробел в сокращениях, например, в «т. д.»"
  },
  "ru/nbsp/addr": {
    "en-US": "Placement of non-breaking space after “г.”, “обл.”, “ул.”, “пр.”, “кв.” et al.",
    "ru": "Расстановка нераз. пробела после «г.», «обл.», «ул.», «пр.», «кв.» и др."
  },
  "ru/nbsp/afterNumberSign": {
    "en-US": "Non-breaking thin space after №",
    "ru": "Нераз. узкий пробел после №"
  },
  "ru/nbsp/beforeParticle": {
    "en-US": "Non-breaking space before “ли”, “ль”, “же”, “бы”, “б”",
    "ru": "Нераз. пробел перед «ли», «ль», «же», «бы», «б»"
  },
  "ru/nbsp/centuries": {
    "en-US": "Remove spaces and extra points in “вв.”",
    "ru": "Удаление пробелов и лишних точек в «вв.»"
  },
  "ru/nbsp/dayMonth": {
    "en-US": "Non-breaking space between number and month",
    "ru": "Нераз. пробел между числом и месяцем"
  },
  "ru/nbsp/initials": {
    "en-US": "Binding of initials to the name",
    "ru": "Привязка инициалов к фамилии"
  },
  "ru/nbsp/m": {
    "en-US": "m2 → м², m3 → м³ and non-breaking space",
    "ru": "м2 → м², м3 → м³ и нераз. пробел"
  },
  "ru/nbsp/mln": {
    "en-US": "Non-breaking space between number and “тыс.”, “млн”, “млрд” and “трлн”",
    "ru": "Неразр. пробел между числом и «тыс.», «млн», «млрд» и «трлн»"
  },
  "ru/nbsp/ooo": {
    "en-US": "Non-breaking space after “OOO, ОАО, ЗАО, НИИ, ПБОЮЛ”",
    "ru": "Нераз. пробел после OOO, ОАО, ЗАО, НИИ и ПБОЮЛ"
  },
  "ru/nbsp/page": {
    "en-US": "Non-breaking space after “стр.”, “гл.”, “рис.”, “илл.”",
    "ru": "Нераз. пробел после «стр.», «гл.», «рис.», «илл.»"
  },
  "ru/nbsp/ps": {
    "en-US": "Non-breaking space in P. S. and P. P. S.",
    "ru": "Нераз. пробел в P. S. и P. P. S."
  },
  "ru/nbsp/rubleKopek": {
    "en-US": "Not once. space before the “rub” and “cop.”",
    "ru": "Нераз. пробел перед «руб.» и «коп.»"
  },
  "ru/nbsp/see": {
    "en-US": "Non-breaking space after abbreviation «см.» and «им.»",
    "ru": "Нераз. пробел после сокращений «см.» и «им.»"
  },
  "ru/nbsp/year": {
    "en-US": "Non-breaking space before XXXX г. (2012 г.)",
    "ru": "Нераз. пробел после XXXX г. (2012 г.)"
  },
  "ru/nbsp/years": {
    "en-US": "г.г. → гг. and non-breaking space",
    "ru": "г.г. → гг. и нераз. пробел"
  },
  "ru/number/comma": {
    "en-US": "Commas in numbers",
    "ru": "Замена точки на запятую в числах"
  },
  "ru/number/ordinals": {
    "common": "N-ый, -ой, -ая, -ое, -ые, -ым, -ом, -ых → N-й, -я, -е, -м, -х (25-й)"
  },
  "ru/optalign/bracket": {
    "en-US": "for opening bracket",
    "ru": "для открывающей скобки"
  },
  "ru/optalign/comma": {
    "en-US": "for comma",
    "ru": "для запятой"
  },
  "ru/optalign/quote": {
    "en-US": "for opening quotation marks",
    "ru": "для открывающей кавычки"
  },
  "ru/other/accent": {
    "en-US": "Replacement capital letters to lowercase with addition of accent",
    "ru": "Замена заглавной буквы на строчную с добавлением ударения"
  },
  "ru/other/phone-number": {
    "en-US": "Formatting phone numbers",
    "ru": "Форматирование телефонных номеров"
  },
  "ru/punctuation/ano": {
    "en-US": "Placement of commas before “а” and “но”",
    "ru": "Расстановка запятых перед «а» и «но»"
  },
  "ru/punctuation/exclamation": {
    "common": "!! → !"
  },
  "ru/punctuation/exclamationQuestion": {
    "common": "!? → ?!"
  },
  "ru/punctuation/hellipQuestion": {
    "common": "«?…» → «?..», «!…» → «!..», «…,» → «…»"
  },
  "ru/space/afterHellip": {
    "en-US": "Space after “...”, “!..” and “?..”",
    "ru": "Пробел после «...», «!..» и «?..»"
  },
  "ru/space/year": {
    "en-US": "Space between number and word “год”",
    "ru": "Пробел между числом и словом «год»"
  },
  "ru/symbols/NN": {
    "common": "№№ → №"
  },
  "ru/typo/switchingKeyboardLayout": {
    "en-US": "Replacement of Latin letters in Russian. Typos occur when you switch keyboard layouts",
    "ru": "Замена латинских букв на русские. Опечатки, возникающие при переключении клавиатурной раскладки"
  }
};

var groups = [{
  "name": "punctuation",
  "title": {
    "en-US": "Punctuation",
    "ru": "Пунктуация"
  }
}, {
  "name": "optalign",
  "title": {
    "en-US": "Hanging punctuation",
    "ru": "Висячая пунктуация"
  }
}, {
  "name": "dash",
  "title": {
    "en-US": "Dash and hyphen",
    "ru": "Тире и дефис"
  }
}, {
  "name": "nbsp",
  "title": {
    "en-US": "Non-breaking space",
    "ru": "Неразрывный пробел"
  }
}, {
  "name": "space",
  "title": {
    "en-US": "Space and line ending",
    "ru": "Пробел и окончание строки"
  }
}, {
  "name": "html",
  "title": {
    "en-US": "HTML",
    "ru": "HTML"
  }
}, {
  "name": "date",
  "title": {
    "en-US": "Date",
    "ru": "Дата"
  }
}, {
  "name": "money",
  "title": {
    "en-US": "Money",
    "ru": "Деньги"
  }
}, {
  "name": "number",
  "title": {
    "en-US": "Numbers and mathematical expressions",
    "ru": "Числа и математические выражения"
  }
}, {
  "name": "symbols",
  "title": {
    "en-US": "Symbols and signs",
    "ru": "Символы и знаки"
  }
}, {
  "name": "typo",
  "title": {
    "en-US": "Typos",
    "ru": "Опечатки"
  }
}, {
  "name": "other",
  "title": {
    "en-US": "Other",
    "ru": "Прочее"
  }
}];

Typograf.titles = titles;
Typograf.groups = groups;

return Typograf;

})));
