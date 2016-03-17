/*!
 * froala_editor v2.2.1 (https://www.froala.com/wysiwyg-editor)
 * License https://froala.com/wysiwyg-editor/terms/
 * Copyright 2014-2016 Froala Labs
 */

(function (factory) {
    if (typeof define === 'function' && define.amd) {
        // AMD. Register as an anonymous module.
        define(['jquery'], factory);
    } else if (typeof module === 'object' && module.exports) {
        // Node/CommonJS
        module.exports = function( root, jQuery ) {
            if ( jQuery === undefined ) {
                // require('jQuery') returns a factory that requires window to
                // build a jQuery instance, we normalize how we use modules
                // that require this pattern but the window provided is a noop
                // if it's defined (how jquery works)
                if ( typeof window !== 'undefined' ) {
                    jQuery = require('jquery');
                }
                else {
                    jQuery = require('jquery')(root);
                }
            }
            factory(jQuery);
            return jQuery;
        };
    } else {
        // Browser globals
        factory(jQuery);
    }
}(function ($) {

  'use strict';

  // Extend defaults.
  $.extend($.FE.DEFAULTS, {
    entities: '&amp;&lt;&gt;&quot;&apos;&iexcl;&cent;&pound;&curren;&yen;&brvbar;&sect;&uml;&copy;&ordf;&laquo;&not;&shy;&reg;&macr;&deg;&plusmn;&sup2;&sup3;&acute;&micro;&para;&middot;&cedil;&sup1;&ordm;&raquo;&frac14;&frac12;&frac34;&iquest;&Agrave;&Aacute;&Acirc;&Atilde;&Auml;&Aring;&AElig;&Ccedil;&Egrave;&Eacute;&Ecirc;&Euml;&Igrave;&Iacute;&Icirc;&Iuml;&ETH;&Ntilde;&Ograve;&Oacute;&Ocirc;&Otilde;&Ouml;&times;&Oslash;&Ugrave;&Uacute;&Ucirc;&Uuml;&Yacute;&THORN;&szlig;&agrave;&aacute;&acirc;&atilde;&auml;&aring;&aelig;&ccedil;&egrave;&eacute;&ecirc;&euml;&igrave;&iacute;&icirc;&iuml;&eth;&ntilde;&ograve;&oacute;&ocirc;&otilde;&ouml;&divide;&oslash;&ugrave;&uacute;&ucirc;&uuml;&yacute;&thorn;&yuml;&OElig;&oelig;&Scaron;&scaron;&Yuml;&fnof;&circ;&tilde;&Alpha;&Beta;&Gamma;&Delta;&Epsilon;&Zeta;&Eta;&Theta;&Iota;&Kappa;&Lambda;&Mu;&Nu;&Xi;&Omicron;&Pi;&Rho;&Sigma;&Tau;&Upsilon;&Phi;&Chi;&Psi;&Omega;&alpha;&beta;&gamma;&delta;&epsilon;&zeta;&eta;&theta;&iota;&kappa;&lambda;&mu;&nu;&xi;&omicron;&pi;&rho;&sigmaf;&sigma;&tau;&upsilon;&phi;&chi;&psi;&omega;&thetasym;&upsih;&piv;&ensp;&emsp;&thinsp;&zwnj;&zwj;&lrm;&rlm;&ndash;&mdash;&lsquo;&rsquo;&sbquo;&ldquo;&rdquo;&bdquo;&dagger;&Dagger;&bull;&hellip;&permil;&prime;&Prime;&lsaquo;&rsaquo;&oline;&frasl;&euro;&image;&weierp;&real;&trade;&alefsym;&larr;&uarr;&rarr;&darr;&harr;&crarr;&lArr;&uArr;&rArr;&dArr;&hArr;&forall;&part;&exist;&empty;&nabla;&isin;&notin;&ni;&prod;&sum;&minus;&lowast;&radic;&prop;&infin;&ang;&and;&or;&cap;&cup;&int;&there4;&sim;&cong;&asymp;&ne;&equiv;&le;&ge;&sub;&sup;&nsub;&sube;&supe;&oplus;&otimes;&perp;&sdot;&lceil;&rceil;&lfloor;&rfloor;&lang;&rang;&loz;&spades;&clubs;&hearts;&diams;'
  });


  $.FE.PLUGINS.entities = function (editor) {
    var _reg_exp;
    var _map;

    function _encode (el) {
      var contents = editor.node.contents(el);

      for (var i = 0; i < contents.length; i++) {
        if (contents[i].nodeType == Node.TEXT_NODE) {
          var text = contents[i].textContent;
          if (text.match(_reg_exp)) {
            var new_text = '';
            for (var j = 0; j < text.length; j++) {
              if (_map[text[j]]) new_text += _map[text[j]];
              else new_text += text[j];
            }
            contents[i].textContent = new_text;
          }
        }
        else {
          _encode(contents[i]);
        }
      }
    }

    /**
     * Encode entities.
     */
    function _encodeEntities (html) {
      if (html.length === 0) return '';

      return editor.clean.exec(html, _encode);
    }

    /*
     * Initialize.
     */
    function _init () {
      if (editor.opts.htmlSimpleAmpersand) {
        editor.opts.entities = editor.opts.entities.replace('&amp;', '');
      }

      // Do escape.
      var entities_text = $('<div>').html(editor.opts.entities).text();
      var entities_array = editor.opts.entities.split(';');
      _map = {};
      _reg_exp = '';
      for (var i = 0; i < entities_text.length; i++) {
        var chr = entities_text.charAt(i);
        _map[chr] = entities_array[i] + ';';
        _reg_exp += '\\' + chr + (i < entities_text.length - 1 ? '|' : '');
      }
      _reg_exp = new RegExp('(' + _reg_exp + ')', 'g');

      editor.events.on('html.get', _encodeEntities, true);
    }

    return {
      _init: _init
    }
  }

}));
