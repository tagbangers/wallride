/*!
 * froala_editor v2.5.1 (https://www.froala.com/wysiwyg-editor)
 * License https://froala.com/wysiwyg-editor/terms/
 * Copyright 2014-2017 Froala Labs
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
            return factory(jQuery);
        };
    } else {
        // Browser globals
        factory(window.jQuery);
    }
}(function ($) {

  

  // Extend defaults.
  $.extend($.FE.DEFAULTS, {

  });

  $.FE.URLRegEx = '(\\s|^|>)(((http|https|ftp|ftps)\\:\\/\\/)?[a-zA-Z0-9\\-\\.]+(\\.[a-zA-Z]{2,3})(:\\d*)?(\\/[^\\s<]*)?)(\\s|$|<)';

  $.FE.PLUGINS.url = function (editor) {

    function _ignore (node) {
      while (node.parentNode) {
        node = node.parentNode;

        if (['A', 'BUTTON', 'TEXTAREA'].indexOf(node.tagName) >= 0) {
          return true;
        }
      }

      return false;
    }

    function _convertURLS () {
      var walker = editor.doc.createTreeWalker(editor.el, NodeFilter.SHOW_TEXT, editor.node.filter(function (nd) {

        return ((new RegExp($.FE.URLRegEx,'gi')).test(nd.textContent.replace(/&nbsp;/gi, ' ')) && !_ignore(nd));
      }), false);

      var nodes = [];
      var node;

      while (walker.nextNode()) {
        node = walker.currentNode;

        nodes.push(node);
      }

      for (var i = 0; i < nodes.length; i++) {
        node = nodes[i];

        var rel = null;

        if (editor.opts.linkAlwaysNoFollow) {
          rel = 'nofollow';
        }

        // https://github.com/froala/wysiwyg-editor/issues/1576.
        if (editor.opts.linkAlwaysBlank) {
          if (!rel) rel = 'noopener noreferrer';
          else rel += ' noopener noreferrer';
        }

        // Convert it to A.
        $(node).before(node.textContent.replace((new RegExp($.FE.URLRegEx,'gi')), '$1<a' + (editor.opts.linkAlwaysBlank ? ' target="_blank"' : '') + (rel ? (' rel="' + rel + '"') : '') + ' href="$2">$2</a>$8'));

        node.parentNode.removeChild(node);
      }
    }

    /*
     * Initialize.
     */
    function _init () {
      editor.events.on('paste.afterCleanup', function (html) {
        if ((new RegExp($.FE.URLRegEx,'gi')).test(html)) {
          return html.replace((new RegExp($.FE.URLRegEx,'gi')), '$1<a' + (editor.opts.linkAlwaysBlank ? ' target="_blank"' : '') + (editor.opts.linkAlwaysNoFollow ? ' rel="nofollow"' : '') + ' href="$2">$2</a>$8')
        }
      });

      editor.events.on('keyup', function (e) {
        var keycode = e.which;

        if (keycode == $.FE.KEYCODE.ENTER || keycode == $.FE.KEYCODE.SPACE) {
          _convertURLS(editor.node.contents(editor.el));
        }
      },true);

      editor.events.on('keydown', function (e) {
        var keycode = e.which;

        if (keycode == $.FE.KEYCODE.ENTER) {
          var el = editor.selection.element();

          if ((el.tagName == 'A' || $(el).parents('a').length) && editor.selection.info(el).atEnd) {
            e.stopImmediatePropagation();

            if (el.tagName !== 'A') el = $(el).parents('a')[0];
            $(el).after('&nbsp;' + $.FE.MARKERS);
            editor.selection.restore();

            return false;
          }
        }
      });
    }

    return {
      _init: _init
    }
  }

}));
