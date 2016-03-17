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
    quickInsertButtons: ['image', 'table', 'ul', 'ol', 'hr'],
    quickInsertTags: ['p', 'div', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'pre', 'blockquote']
  });

  $.FE.QUICK_INSERT_BUTTONS = {
    image: {
      icon: 'insertImage',
      callback: function () {
        var editor = this;

        if (!editor.shared.$qi_image_input) {
          editor.shared.$qi_image_input = $('<input accept="image/*" name="quickInsertImage' + this.id + '" style="display: none;" type="file">');
          $('body').append(editor.$qi_image_input);
        }

        editor.$qi_image_input = editor.shared.$qi_image_input;

        editor.events.$on(editor.$qi_image_input, 'change', function () {
          var inst = $(this).data('inst');
          if (this.files) {
            inst.quickInsert.hide();
            inst.image.showInsertPopup();
            var $popup = inst.popups.get('image.insert');
            inst.position.forSelection($popup);

            inst.image.upload(this.files);

            // Chrome fix.
            $(this).val('');
            $(this).blur();
          }
        }, true);

        editor.$qi_image_input.data('inst', editor).trigger('click');
      },
      requiredPlugin: 'image',
      title: 'Insert Image'
    },
    table: {
      icon: 'insertTable',
      callback: function () {
        this.quickInsert.hide();
        this.table.insert(2, 2);
        this.undo.saveStep();
      },
      requiredPlugin: 'table',
      title: 'Insert Table'
    },
    ol: {
      icon: 'formatOL',
      callback: function () {
        this.quickInsert.hide();
        this.lists.format('OL');
        this.undo.saveStep();
      },
      requiredPlugin: 'lists',
      title: 'Ordered List'
    },
    ul: {
      icon: 'formatUL',
      callback: function () {
        this.quickInsert.hide();
        this.lists.format('UL');
        this.undo.saveStep();
      },
      requiredPlugin: 'lists',
      title: 'Unordered List'
    },
    hr: {
      icon: 'insertHR',
      callback: function () {
        this.quickInsert.hide();
        this.commands.insertHR();
        this.undo.saveStep();
      },
      title: 'Insert Horizontal Line'
    }
  }

  $.FE.RegisterQuickInsertCommand = function (name, data) {
    $.FE.QUICK_INSERT_BUTTONS[name] = data;
  }

  $.FE.PLUGINS.quickInsert = function (editor) {
    var $quick_insert;

    /*
     * Show quick insert.
     * Compute top, left, width and show the quick insert.
     */
    function _show ($tag) {
      if (!$quick_insert) _initquickInsert();

      editor.$box.append($quick_insert);

      // Quick insert's possition.
      var qiTop;
      var qiLeft;

      qiTop = $tag.offset().top - editor.$box.offset().top - ($quick_insert.outerHeight() - $tag.outerHeight()) / 2;
      qiLeft = 0 - $quick_insert.outerWidth();

      if (editor.opts.iframe) {
        qiTop += editor.$iframe.offset().top - $(editor.o_win).scrollTop();
      }

      // Set quick insert's top and left.
      $quick_insert.css('top', qiTop);
      $quick_insert.css('left', qiLeft);

      $quick_insert.data('tag', $tag);

      // Show the quick insert.
      $quick_insert.addClass('fr-visible');
    }

    /*
     * Check the tag where the cursor is.
     */
    function _checkTag () {
      var tag = editor.selection.element();

      // Get block tag.
      if (!editor.node.isBlock(tag)) {
        tag = editor.node.blockParent(tag);
      }

      // Tag must be empty and direct child of element in order to show the quick insert.
      if (tag && editor.node.isEmpty(tag) && editor.node.isElement(tag.parentNode)) {
        // If tag is block and selection is collapsed.
        if (tag && editor.selection.isCollapsed()) {
          _show($(tag));
        }
      }

      // Quick insert should not be visible.
      else {
        hide();
      }
    }

    /*
     * Hide quick insert.
     */
    function hide () {
      if ($quick_insert) {
        editor.html.checkIfEmpty();

        // Hide the quick insert helper if visible.
        if ($quick_insert.hasClass('fr-on')) {
          _hideHelper();
        }

        // Hide the quick insert.
        $quick_insert.removeClass('fr-visible fr-on');
        $quick_insert.css('left', -9999).css('top', -9999);
      }
    }

    /*
     * Show the quick insert helper.
     */
    var $helper;
    function _showQIHelper (e) {
      e.preventDefault();

      // Hide helper.
      if ($quick_insert.hasClass('fr-on')) {
        _hideHelper();
      }

      else {
        if (!editor.shared.$qi_helper) {
          var btns = editor.opts.quickInsertButtons;
          var btns_html = '<div class="fr-qi-helper">';
          var idx = 0;

          for (var i = 0; i < btns.length; i++) {
            var info = $.FE.QUICK_INSERT_BUTTONS[btns[i]];
            if (info) {
              if (!info.requiredPlugin || ($.FE.PLUGINS[info.requiredPlugin] && editor.opts.pluginsEnabled.indexOf(info.requiredPlugin) >= 0)) {
                btns_html += '<a class="fr-btn fr-floating-btn" role="button" title="' + editor.language.translate(info.title) + '" tabindex="-1" data-cmd="' + btns[i] + '" style="transition-delay: ' + (0.025 * (idx++)) + 's;">' + editor.icon.create(info.icon) + '</a>';
              }
            }
          }

          btns_html += '</div>';
          editor.shared.$qi_helper = $(btns_html);

          // Quick insert helper tooltip.
          editor.tooltip.bind(editor.shared.$qi_helper, '.fr-qi-helper > a.fr-btn');
        }

        $helper = editor.shared.$qi_helper;
        $helper.appendTo(editor.$box);

        // Show the quick insert helper.
        setTimeout(function () {
          $helper.css('top', parseFloat($quick_insert.css('top')));
          $helper.css('left', parseFloat($quick_insert.css('left')) + $quick_insert.outerWidth());
          $helper.find('a').addClass('fr-size-1')
          $quick_insert.addClass('fr-on');
        }, 10);
      }
    }

    /*
     * Hides the quick insert helper and places the cursor.
     */
    function _hideHelper () {
      var $helper = editor.$box.find('.fr-qi-helper');

      if ($helper.length) {
        $helper.find('a').removeClass('fr-size-1');
        $helper.css('left', -9999);
        $quick_insert.removeClass('fr-on');
      }
    }

    /*
     * Initialize the quick insert.
     */
    function _initquickInsert () {
      if (!editor.shared.$quick_insert) {
        // Append quick insert HTML to editor wrapper.
        editor.shared.$quick_insert = $('<div class="fr-quick-insert"><a class="fr-floating-btn" role="button" tabindex="-1" title="' + editor.language.translate('Quick Insert') + '"><svg viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg"><path d="M22,16.75 L16.75,16.75 L16.75,22 L15.25,22.000 L15.25,16.75 L10,16.75 L10,15.25 L15.25,15.25 L15.25,10 L16.75,10 L16.75,15.25 L22,15.25 L22,16.75 Z"/></svg></a></div>');
      }
      $quick_insert = editor.shared.$quick_insert;

      // Quick Insert tooltip.
      editor.tooltip.bind(editor.$box, '.fr-quick-insert > a.fr-floating-btn');

      // Editor destroy.
      editor.events.on('destroy', function () {
        $quick_insert.removeClass('fr-on').appendTo($('body')).css('left', -9999).css('top', -9999);

        if ($helper) {
          _hideHelper();
          $helper.appendTo($('body'));
        }
      }, true);

      editor.events.on('shared.destroy', function () {
        $quick_insert.html('').removeData().remove();
        if ($helper) $helper.html('').removeData().remove();
      }, true);

      // Hide before a command is executed.
      editor.events.on('commands.before', hide);

      // Check if the quick insert should be shown after a command has been executed.
      editor.events.on('commands.after', function () {
        if (!editor.popups.areVisible()) {
          _checkTag();
        }
      });

      // User clicks on the quick insert.
      editor.events.bindClick(editor.$box, '.fr-quick-insert > a', _showQIHelper);

      // User clicks on a button from the quick insert helper.
      editor.events.bindClick(editor.$box, '.fr-qi-helper > a.fr-btn', function (e) {
        var cmd = $(e.currentTarget).data('cmd');

        $.FE.QUICK_INSERT_BUTTONS[cmd].callback.apply(editor, [e.currentTarget]);
      });
    }

    /*
     * Tear up.
     */
    function _init () {
      if (!editor.$wp) return false;

      if (editor.opts.iframe) {
        editor.$el.parent('html').find('head').append('<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.4.0/css/font-awesome.min.css">');
      }

      // Hide the quick insert if user click on an image.
      editor.popups.onShow('image.edit', hide);

      // Check tag where cursor is to see if the quick insert needs to be shown.
      editor.events.on('mouseup', _checkTag);

      // Hide the quick insert when editor loses focus.
      editor.events.on('blur', hide);

      // Check if the quick insert should be shown after a key was pressed.
      editor.events.on('keyup', _checkTag);
    }

    return {
      _init: _init,
      hide: hide
    }
  };

}));
