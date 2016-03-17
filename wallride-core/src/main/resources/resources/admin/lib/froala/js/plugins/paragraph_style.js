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

  $.extend($.FE.DEFAULTS, {
    paragraphStyles: {
      'fr-text-gray': 'Gray',
      'fr-text-bordered': 'Bordered',
      'fr-text-spaced': 'Spaced',
      'fr-text-uppercase': 'Uppercase'
    },
    paragraphMultipleStyles: true
  });

  $.FE.PLUGINS.paragraphStyle = function (editor) {
    /**
     * Apply style.
     */
    function apply (val) {
      var styles = '';
      // Remove multiple styles.
      if (!editor.opts.paragraphMultipleStyles) {
        styles = Object.keys(editor.opts.paragraphStyles);
        styles.splice(styles.indexOf(val), 1);
        styles = styles.join(' ');
      }

      editor.selection.save();
      editor.html.wrap(true, true, true);
      editor.selection.restore();

      var blocks = editor.selection.blocks();

      // Save selection to restore it later.
      editor.selection.save();

      for (var i = 0; i < blocks.length; i++) {
        $(blocks[i]).removeClass(styles).toggleClass(val);

        if ($(blocks[i]).hasClass('fr-temp-div')) $(blocks[i]).removeClass('fr-temp-div');
        if ($(blocks[i]).attr('class') === '') $(blocks[i]).removeAttr('class');
      }

      // Unwrap temp divs.
      editor.html.unwrap();

      // Restore selection.
      editor.selection.restore();
    }

    function refreshOnShow($btn, $dropdown) {
      var blocks = editor.selection.blocks();

      if (blocks.length) {
        var $blk = $(blocks[0]);
        $dropdown.find('.fr-command').each (function () {
          var cls = $(this).data('param1');
          $(this).toggleClass('fr-active', $blk.hasClass(cls));
        })
      }
    }

    function _init () {
    }

    return {
      _init: _init,
      apply: apply,
      refreshOnShow: refreshOnShow
    }
  }

  // Register the font size command.
  $.FE.RegisterCommand('paragraphStyle', {
    type: 'dropdown',
    html: function () {
      var c = '<ul class="fr-dropdown-list">';
      var options =  this.opts.paragraphStyles;
      for (var val in options) {
        c += '<li><a class="fr-command ' + val + '" data-cmd="paragraphStyle" data-param1="' + val + '" title="' + this.language.translate(options[val]) + '">' + this.language.translate(options[val]) + '</a></li>';
      }
      c += '</ul>';

      return c;
    },
    title: 'Paragraph Style',
    callback: function (cmd, val) {
      this.paragraphStyle.apply(val);
    },
    refreshOnShow: function ($btn, $dropdown) {
      this.paragraphStyle.refreshOnShow($btn, $dropdown);
    },
    plugin: 'paragraphStyle'
  })

  // Add the font size icon.
  $.FE.DefineIcon('paragraphStyle', {
    NAME: 'magic'
  });

}));
