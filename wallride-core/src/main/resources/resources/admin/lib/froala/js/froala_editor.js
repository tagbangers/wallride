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
  /*jslint browser: true, debug: true, vars: true, devel: true, expr: true, jQuery: true */
  // EDITABLE CLASS DEFINITION
  // =========================

  'use strict';

  var FE = function (element, options) {
    this.id = ++$.FE.ID;

    this.opts = $.extend(true, {}, $.extend({}, FE.DEFAULTS, typeof options == 'object' && options));
    var opts_string = JSON.stringify(this.opts);

    $.FE.OPTS_MAPPING[opts_string] = $.FE.OPTS_MAPPING[opts_string] || this.id;
    this.sid = $.FE.OPTS_MAPPING[opts_string];
    $.FE.SHARED[this.sid] = $.FE.SHARED[this.sid] || {};
    this.shared = $.FE.SHARED[this.sid];
    this.shared.count = (this.shared.count || 0) + 1;

    this.$oel = $(element);
    this.$oel.data('froala.editor', this);

    this.o_doc = element.ownerDocument;
    this.o_win = 'defaultView' in this.o_doc ? this.o_doc.defaultView :   this.o_doc.parentWindow;
    var c_scroll = $(this.o_win).scrollTop();

    this.$oel.on('froala.doInit', $.proxy(function () {
      this.$oel.off('froala.doInit');
      this.doc = this.$el.get(0).ownerDocument;
      this.win = 'defaultView' in this.doc ? this.doc.defaultView : this.doc.parentWindow;
      this.$doc = $(this.doc);
      this.$win = $(this.win);

      if (!this.opts.pluginsEnabled) this.opts.pluginsEnabled = Object.keys($.FE.PLUGINS);

      if (this.opts.initOnClick) {
        this.load($.FE.MODULES);

        this.$el.on('mousedown.init dragenter.init focus.init', $.proxy(function (e) {
          if (e.which === 1) {
            this.$el.off('mousedown.init dragenter.init focus.init');

            this.load($.FE.MODULES);
            this.load($.FE.PLUGINS);

            var target = e.originalEvent && e.originalEvent.originalTarget;
            if (target && target.tagName == 'IMG') $(target).trigger('mousedown');

            if (typeof this.ul == 'undefined') this.destroy();

            this.events.trigger('initialized');
          }
        }, this));
      }
      else {
        this.load($.FE.MODULES);
        this.load($.FE.PLUGINS);

        $(this.o_win).scrollTop(c_scroll);

        if (typeof this.ul == 'undefined') this.destroy();

        this.events.trigger('initialized');
      }
    }, this));

    this._init();
  };

  FE.DEFAULTS = {
    initOnClick: false,
    pluginsEnabled: null
  };

  FE.MODULES = {};

  FE.PLUGINS = {};

  FE.VERSION = '2.2.1';

  FE.INSTANCES = [];

  FE.OPTS_MAPPING = {};

  FE.SHARED = {};

  FE.ID = 0;

  FE.prototype._init = function () {
    // Get the tag name of the original element.
    var tag_name = this.$oel.prop('tagName');

    // Initialize on anything else.
    var initOnDefault = $.proxy(function () {
      this._original_html = (this._original_html || this.$oel.html());
      this.$box = this.$box || this.$oel;

      // Turn on iframe if fullPage is on.
      if (this.opts.fullPage) this.opts.iframe = true;

      if (!this.opts.iframe) {
        this.$el = $('<div></div>');
        this.$wp = $('<div></div>').append(this.$el);
        this.$box.html(this.$wp);
        this.$oel.trigger('froala.doInit');
      }
      else {
        this.$iframe = $('<iframe src="about:blank" frameBorder="0">');
        this.$wp = $('<div></div>');
        this.$box.html(this.$wp);
        this.$wp.append(this.$iframe);
        this.$iframe.get(0).contentWindow.document.open();
        this.$iframe.get(0).contentWindow.document.write('<!DOCTYPE html>');
        this.$iframe.get(0).contentWindow.document.write('<html><head></head><body></body></html>');
        this.$iframe.get(0).contentWindow.document.close();

        this.$el = this.$iframe.contents().find('body');
        this.$head = this.$iframe.contents().find('head');
        this.$html = this.$iframe.contents().find('html');
        this.iframe_document = this.$iframe.get(0).contentWindow.document;

        this.$oel.trigger('froala.doInit');
      }
    }, this);

    // Initialize on a TEXTAREA.
    var initOnTextarea = $.proxy(function () {
      this.$box = $('<div>');
      this.$oel.before(this.$box).hide();

      this._original_html = this.$oel.val();

      // Before submit textarea do a sync.
      this.$oel.parents('form').on('submit.' + this.id, $.proxy(function () {
        this.events.trigger('form.submit');
      }, this));

      this.$oel.parents('form').on('reset.' + this.id, $.proxy(function () {
        this.events.trigger('form.reset');
      }, this));

      initOnDefault();
    }, this);

    // Initialize on a Link.
    var initOnA = $.proxy(function () {
      this.$el = this.$oel;
      this.$el.attr('contenteditable', true).css('outline', 'none').css('display', 'inline-block');
      this.opts.multiLine = false;
      this.opts.toolbarInline = false;

      this.$oel.trigger('froala.doInit');
    }, this)

    // Initialize on an Image.
    var initOnImg = $.proxy(function () {
      this.$el = this.$oel;
      this.opts.toolbarInline = false;

      this.$oel.trigger('froala.doInit');
    }, this)

    var editInPopup = $.proxy(function () {
      this.$el = this.$oel;
      this.opts.toolbarInline = false;

      this.$oel.on('click.popup', function (e) {
        e.preventDefault();
      })
      this.$oel.trigger('froala.doInit');
    }, this);

    // Check on what element it was initialized.
    if (this.opts.editInPopup) editInPopup();
    else if (tag_name == 'TEXTAREA') initOnTextarea();
    else if (tag_name == 'A') initOnA();
    else if (tag_name == 'IMG') initOnImg();
    else if (tag_name == 'BUTTON' || tag_name == 'INPUT') {
      this.opts.editInPopup = true;
      this.opts.toolbarInline = false;
      editInPopup();
    }
    else {
      initOnDefault();
    }
  }

  FE.prototype.load = function (module_list) {
    // Bind modules to the current instance and tear them up.
    for (var m_name in module_list) {
      if (this[m_name]) continue;

      // Do not include plugin.
      if ($.FE.PLUGINS[m_name] && this.opts.pluginsEnabled.indexOf(m_name) < 0) continue;

      this[m_name] = new module_list[m_name](this);
      if (this[m_name]._init) {
        this[m_name]._init();
        if (this.opts.initOnClick && m_name == 'core') {
          return false;
        }
      }
    }
  }

  // Do destroy.
  FE.prototype.destroy = function () {
    if (this.edit.isDisabled()) return false;

    this.shared.count--;

    this.events.$off();

    this.events.trigger('destroy');
    this.events.trigger('shared.destroy');

    // Remove shared.
    if (this.shared.count === 0) {
      for (var k in this.shared) {
        delete this.shared[k];
      }
    }

    this.$oel.parents('form').off('.' + this.id);
    this.$oel.off('click.popup');
    this.$oel.removeData('froala.editor');

    $.FE.INSTANCES.splice($.FE.INSTANCES.indexOf(this), 1);
  }

  // FROALA EDITOR PLUGIN DEFINITION
  // ==========================
  $.fn.froalaEditor = function (option) {
    var arg_list = [];
    for (var i = 0; i < arguments.length; i++) {
      arg_list.push(arguments[i]);
    }

    if (typeof option == 'string') {
      var returns = [];

      this.each(function () {
        var $this = $(this);
        var editor = $this.data('froala.editor');

        if (!editor) {
          return console.warn('Editor should be initialized before calling the ' + option + ' method.');
        }

        var context;
        var nm;

        // Might do a module call.
        if (option.indexOf('.') > 0 && editor[option.split('.')[0]]) {
          if (editor[option.split('.')[0]]) {
            context = editor[option.split('.')[0]];
          }
          nm = option.split('.')[1];
        }
        else {
          context = editor;
          nm = option.split('.')[0]
        }

        if (context[nm]) {
          var returned_value = context[nm].apply(editor, arg_list.slice(1));
          if (returned_value === undefined) {
            returns.push(this);
          } else if (returns.length === 0) {
            returns.push(returned_value);
          }
        }
        else {
          return $.error('Method ' +  option + ' does not exist in Froala Editor.');
        }
      });

      return (returns.length == 1) ? returns[0] : returns;
    }
    else if (typeof option === 'object' || !option) {
      return this.each(function () {
        var editor = $(this).data('froala.editor');

        if (!editor) {
          var that = this;
          new FE(that, option);
        }
      });
    }
  }

  $.fn.froalaEditor.Constructor = FE;
  $.FroalaEditor = FE;
  $.FE = FE;


  $.FE.MODULES.node = function (editor) {
    function getContents(node) {
      if (!node || node.tagName == 'IFRAME') return [];
      return $(node).contents();
    }

    /**
     * Determine if the node is a block tag.
     */
    function isBlock (node) {
      if (!node) return false;
      if (node.nodeType != Node.ELEMENT_NODE) return false;

      return $.FE.BLOCK_TAGS.indexOf(node.tagName.toLowerCase()) >= 0;
    }

    /**
     * Check if a DOM element is empty.
     */
    function isEmpty (el, ignore_markers) {
      if ($(el).find('table').length > 0) return false;

      // Look for void nodes.
      if (el.querySelectorAll($.FE.VOID_ELEMENTS.join(',')).length - el.querySelectorAll('br').length) return false;

      // Look for empty allowed tags.
      if (el.querySelectorAll(editor.opts.htmlAllowedEmptyTags.join(',')).length) return false;

      // Look for block tags.
      if (el.querySelectorAll($.FE.BLOCK_TAGS.join(',')).length > 1) return false;

      // Look for do not wrap tags.
      if (el.querySelectorAll(editor.opts.htmlDoNotWrapTags.join(',')).length) return false;

      // Get element contents.
      var contents = getContents(el);

      // Check if there is a block tag.
      if (contents.length == 1 && isBlock(contents[0])) {
        contents = getContents(contents[0]);
      }

      var has_br = false;
      for (var i = 0; i < contents.length; i++) {
        var node = contents[i];

        if (ignore_markers && $(node).hasClass('fr-marker')) continue;

        if (node.nodeType == Node.TEXT_NODE && node.textContent.length == 0) continue;

        if (node.tagName != 'BR' && (node.textContent || '').replace(/\u200B/gi, '').length > 0) return false;

        if (has_br) {
          return false;
        }
        else if (node.tagName == 'BR') {
          has_br = true;
        }
      }

      return true;
    }

    /**
     * Get the block parent.
     */
    function blockParent (node) {
      while (node && node.parentNode !== editor.$el.get(0) && !(node.parentNode && $(node.parentNode).hasClass('fr-inner'))) {
        node = node.parentNode;
        if (isBlock(node)) {
          return node;
        }
      }

      return null;
    }

    /**
     * Get deepest parent till the element.
     */
    function deepestParent (node, until, simple_enter) {
      if (typeof until == 'undefined') until = [];
      if (typeof simple_enter == 'undefined') simple_enter = true;
      until.push(editor.$el.get(0));

      if (until.indexOf(node.parentNode) >= 0 || (node.parentNode && $(node.parentNode).hasClass('fr-inner')) || (node.parentNode && $.FE.SIMPLE_ENTER_TAGS.indexOf(node.parentNode.tagName) >= 0 && simple_enter)) {
        return null;
      }

      // 1. Before until.
      // 2. Parent node doesn't has class fr-inner.
      // 3. Parent node is not a simple enter tag or quote.
      // 4. Parent node is not a block tag
      while (until.indexOf(node.parentNode) < 0 && node.parentNode && !$(node.parentNode).hasClass('fr-inner') && ($.FE.SIMPLE_ENTER_TAGS.indexOf(node.parentNode.tagName) < 0 || !simple_enter) && (!(isBlock(node) && isBlock(node.parentNode)) || !simple_enter)) {
        node = node.parentNode;
      }

      return node;
    }

    function rawAttributes (node) {
      var attrs = {};

      var atts = node.attributes;
      if (atts) {
        for (var i = 0; i < atts.length; i++) {
          var att = atts[i];
          attrs[att.nodeName] = att.value;
        }
      }

      return attrs;
    }

    /**
     * Get attributes for a node as a string.
     */
    function attributes (node) {
      var str = '';
      var atts = rawAttributes(node);

      var keys = Object.keys(atts).sort();
      for (var i = 0; i < keys.length; i++) {
        var nodeName = keys[i];
        var value = atts[nodeName];

        // Make sure we don't break any HTML.
        if (value.indexOf('"') < 0) {
          str += ' ' + nodeName + '="' + value + '"';
        }
        else {
          str += ' ' + nodeName + '=\'' + value + '\'';
        }
      }

      return str;
    }

    function clearAttributes (node) {
      var atts = node.attributes;
      for (var i = 0; i < atts.length; i++) {
        var att = atts[i];
        node.removeAttribute(att.nodeName);
      }
    }

    /**
     * Open string for a node.
     */
    function openTagString (node) {
      return '<' + node.tagName.toLowerCase() + attributes(node) + '>';
    }

    /**
     * Close string for a node.
     */
    function closeTagString (node) {
      return '</' + node.tagName.toLowerCase() + '>';
    }

    /**
     * Determine if the node has any left sibling.
     */
    function isFirstSibling (node, ignore_markers) {
      if (typeof ignore_markers == 'undefined') ignore_markers = true;
      var sibling = node.previousSibling;

      while (sibling && ignore_markers && $(sibling).hasClass('fr-marker')) {
        sibling = sibling.previousSibling;
      }

      if (!sibling) return true;
      if (sibling.nodeType == Node.TEXT_NODE && sibling.textContent === '') return isFirstSibling(sibling);
      return false;
    }

    function isVoid(node) {
      return node && node.nodeType == Node.ELEMENT_NODE && $.FE.VOID_ELEMENTS.indexOf((node.tagName || '').toLowerCase()) >= 0
    }

    /**
     * Check if the node is a list.
     */
    function isList (node) {
      if (!node) return false;
      return ['UL', 'OL'].indexOf(node.tagName) >= 0;
    }

    /**
     * Check if the node is the editable element.
     */
    function isElement (node) {
      return node === editor.$el.get(0);
    }

    /**
     * Check if the node has focus.
     */
    function hasFocus (node) {
      return node === editor.doc.activeElement && (!editor.doc.hasFocus || editor.doc.hasFocus()) && !!(isElement(node) || node.type || node.href || ~node.tabIndex);;
    }

    function isEditable (node) {
      return !node.getAttribute || node.getAttribute('contenteditable') != 'false';
    }

    return {
      isBlock: isBlock,
      isEmpty: isEmpty,
      blockParent: blockParent,
      deepestParent: deepestParent,
      rawAttributes: rawAttributes,
      attributes: attributes,
      clearAttributes: clearAttributes,
      openTagString: openTagString,
      closeTagString: closeTagString,
      isFirstSibling: isFirstSibling,
      isList: isList,
      isElement: isElement,
      contents: getContents,
      isVoid: isVoid,
      hasFocus: hasFocus,
      isEditable: isEditable
    }
  };


  // Extend defaults.
  $.extend($.FE.DEFAULTS, {
    // Tags that describe head from HEAD http://www.w3schools.com/html/html_head.asp.
    htmlAllowedTags: ['a', 'abbr', 'address', 'area', 'article', 'aside', 'audio', 'b', 'base', 'bdi', 'bdo', 'blockquote', 'br', 'button', 'canvas', 'caption', 'cite', 'code', 'col', 'colgroup', 'datalist', 'dd', 'del', 'details', 'dfn', 'dialog', 'div', 'dl', 'dt', 'em', 'embed', 'fieldset', 'figcaption', 'figure', 'footer', 'form', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'header', 'hgroup', 'hr', 'i', 'iframe', 'img', 'input', 'ins', 'kbd', 'keygen', 'label', 'legend', 'li', 'link', 'main', 'map', 'mark', 'menu', 'menuitem', 'meter', 'nav', 'noscript', 'object', 'ol', 'optgroup', 'option', 'output', 'p', 'param', 'pre', 'progress', 'queue', 'rp', 'rt', 'ruby', 's', 'samp', 'script', 'style', 'section', 'select', 'small', 'source', 'span', 'strike', 'strong', 'sub', 'summary', 'sup', 'table', 'tbody', 'td', 'textarea', 'tfoot', 'th', 'thead', 'time', 'tr', 'track', 'u', 'ul', 'var', 'video', 'wbr'],
    htmlRemoveTags: ['script', 'style'],
    htmlAllowedAttrs: ['accept', 'accept-charset', 'accesskey', 'action', 'align', 'allowfullscreen', 'allowtransparency', 'alt', 'async', 'autocomplete', 'autofocus', 'autoplay', 'autosave', 'background', 'bgcolor', 'border', 'charset', 'cellpadding', 'cellspacing', 'checked', 'cite', 'class', 'color', 'cols', 'colspan', 'content', 'contenteditable', 'contextmenu', 'controls', 'coords', 'data', 'data-.*', 'datetime', 'default', 'defer', 'dir', 'dirname', 'disabled', 'download', 'draggable', 'dropzone', 'enctype', 'for', 'form', 'formaction', 'frameborder', 'headers', 'height', 'hidden', 'high', 'href', 'hreflang', 'http-equiv', 'icon', 'id', 'ismap', 'itemprop', 'keytype', 'kind', 'label', 'lang', 'language', 'list', 'loop', 'low', 'max', 'maxlength', 'media', 'method', 'min', 'mozallowfullscreen', 'multiple', 'name', 'novalidate', 'open', 'optimum', 'pattern', 'ping', 'placeholder', 'poster', 'preload', 'pubdate', 'radiogroup', 'readonly', 'rel', 'required', 'reversed', 'rows', 'rowspan', 'sandbox', 'scope', 'scoped', 'scrolling', 'seamless', 'selected', 'shape', 'size', 'sizes', 'span', 'src', 'srcdoc', 'srclang', 'srcset', 'start', 'step', 'summary', 'spellcheck', 'style', 'tabindex', 'target', 'title', 'type', 'translate', 'usemap', 'value', 'valign', 'webkitallowfullscreen', 'width', 'wrap'],
    htmlAllowComments: true,
    fullPage: false // Will also turn iframe on.
  });

  $.FE.HTML5Map = {
    'B': 'STRONG',
    'I': 'EM',
    'STRIKE': 'S'
  },

  $.FE.MODULES.clean = function (editor) {
    var $iframe, body;
    var allowedTagsRE, removeTagsRE, allowedAttrsRE;

    function _removeInvisible (node) {
      if (node.className && node.className.indexOf('fr-marker') >= 0) return false;

      var contents = editor.node.contents(node);
      var markers = [];
      var i;

      // Get markers.
      for (i = 0; i < contents.length; i++) {
        if (contents[i].className && contents[i].className.indexOf('fr-marker') >= 0) {
          markers.push(contents[i]);
        }
      }

      // Reasess contents.
      if (contents.length - markers.length == 1 && node.textContent.replace(/\u200b/g, '').length === 0) {
        for (i = 0; i < markers.length; i++) {
          node.parentNode.insertBefore(markers[i].cloneNode(true), node);
        }
        node.parentNode.removeChild(node);
        return false;
      }

      for (i = 0; i < contents.length; i++) {
        if (contents[i].nodeType == Node.ELEMENT_NODE) {
          if (contents[i].textContent.replace(/\u200b/g, '').length != contents[i].textContent.length) {
            _removeInvisible(contents[i]);
          }
        }
        else if (contents[i].nodeType == Node.TEXT_NODE) {
          contents[i].textContent = contents[i].textContent.replace(/\u200b/g, '');
        }
      }
    }

    function _toHTML (el) {
      if (el.nodeType == Node.COMMENT_NODE) return '<!--' + el.nodeValue + '-->';
      if (el.nodeType == Node.TEXT_NODE) return el.textContent.replace(/\</g, '&lt;').replace(/\>/g, '&gt;').replace(/\u00A0/g, '&nbsp;');
      if (el.nodeType != Node.ELEMENT_NODE) return el.outerHTML;
      if (el.nodeType == Node.ELEMENT_NODE && ['STYLE', 'SCRIPT'].indexOf(el.tagName) >= 0) return el.outerHTML;
      if (el.tagName == 'IFRAME') return el.outerHTML;

      var contents = el.childNodes;

      if (contents.length === 0) return el.outerHTML;

      var str = '';
      for (var i = 0; i < contents.length; i++) {
        str += _toHTML(contents[i]);
      }

      return editor.node.openTagString(el) + str + editor.node.closeTagString(el);
    }

    var scripts = [];
    function _encode (dirty_html) {
      // Replace script tag with comments.
      scripts = [];
      dirty_html = dirty_html.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, function (str) {
        scripts.push(str);
        return '[FROALA.EDITOR.SCRIPT ' + (scripts.length - 1) + ']';
      });

      dirty_html = dirty_html.replace(/<img((?:[\w\W]*?)) src="/g, '<img$1 data-fr-src="');

      return dirty_html;
    }

    function _decode (dirty_html) {
      // Replace script comments with the original script.
      dirty_html = dirty_html.replace(/\[FROALA\.EDITOR\.SCRIPT ([\d]*)\]/gi, function (str, a1) {
        if (editor.opts.htmlRemoveTags.indexOf('script') >= 0) {
          return '';
        }
        else {
          return scripts[parseInt(a1, 10)];
        }
      });

      dirty_html = dirty_html.replace(/<img((?:[\w\W]*?)) data-fr-src="/g, '<img$1 src="');

      return dirty_html;
    }

    function _cleanAttrs (attrs) {
      var nm;

      for (nm in attrs) {
        if (!nm.match(allowedAttrsRE)) {
          delete attrs[nm];
        }
      }

      var str = '';
      var keys = Object.keys(attrs).sort();
      for (var i = 0; i < keys.length; i++) {
        nm = keys[i];

        // Make sure we don't break any HTML.
        if (attrs[nm].indexOf('"') < 0) {
          str += ' ' + nm + '="' + attrs[nm] + '"';
        }
        else {
          str += ' ' + nm + '=\'' + attrs[nm] + '\'';
        }
      }

      return str;
    }

    function _rebuild (body_html, head_html, original_html) {
      if (editor.opts.fullPage) {
        // Get DOCTYPE.
        var doctype = editor.html.extractDoctype(original_html);

        // Get HTML attributes.
        var html_attrs = _cleanAttrs(editor.html.extractNodeAttrs(original_html, 'html'));

        // Get HEAD data.
        head_html = head_html == null ? editor.html.extractNode(original_html, 'head') || '<title></title>' : head_html;
        var head_attrs = _cleanAttrs(editor.html.extractNodeAttrs(original_html, 'head'));

        // Get BODY attributes.
        var body_attrs = _cleanAttrs(editor.html.extractNodeAttrs(original_html, 'body'));

        return doctype + '<html' + html_attrs + '><head' + head_attrs + '>' + head_html + '</head><body' + body_attrs + '>' + body_html + '</body></html>';
      }

      return body_html;
    }

    function _process (html, func) {
      var $el = $('<div>' + html + '</div>');

      var new_html = '';
      if ($el) {
        var els = editor.node.contents($el.get(0));
        for (var i = 0; i < els.length; i++) {
          func(els[i]);
        }

        els = editor.node.contents($el.get(0));
        for (var i = 0; i < els.length; i++) {
          new_html += _toHTML(els[i]);
        }
      }

      return new_html;
    }

    function exec (html, func, parse_head) {
      html = _encode(html);

      var b_html = html;
      var h_html = null;
      if (editor.opts.fullPage) {
        // Get BODY data.
        var b_html = (editor.html.extractNode(html, 'body') || (html.indexOf('<body') >= 0 ? '' : html));

        if (parse_head) {
          h_html = (editor.html.extractNode(html, 'head') || '');
        }
      }

      b_html = _process(b_html, func);
      if (h_html) h_html = _process(h_html, func);

      var new_html = _rebuild(b_html, h_html, html);

      return _decode(new_html);
    }

    function invisibleSpaces (dirty_html) {
      if (dirty_html.replace(/\u200b/g, '').length == dirty_html.length) return dirty_html;

      return editor.clean.exec(dirty_html, _removeInvisible);
    }

    function toHTML5 () {
      var els = editor.$el.get(0).querySelectorAll(Object.keys($.FE.HTML5Map).join(','));
      if (els.length) {
        editor.selection.save();
        for (var i = 0; i < els.length; i++) {
          if (editor.node.attributes(els[i]) === '') {
             $(els[i]).replaceWith('<' + $.FE.HTML5Map[els[i].tagName] + '>' + els[i].innerHTML + '</' + $.FE.HTML5Map[els[i].tagName] + '>');
          }
        }
        editor.selection.restore();
      }
    }

    function _node (node) {
      if (node.tagName == 'PRE') _cleanPre(node);

      if (node.nodeType == Node.ELEMENT_NODE) {
        if (node.getAttribute('data-fr-src')) node.setAttribute('data-fr-src', editor.helpers.sanitizeURL(node.getAttribute('data-fr-src')));
        if (node.getAttribute('href')) node.setAttribute('href', editor.helpers.sanitizeURL(node.getAttribute('href')));

        if (['TABLE', 'TBODY', 'TFOOT', 'TR'].indexOf(node.tagName) >= 0) {
          node.innerHTML = node.innerHTML.trim();
        }
      }

      // Remove local images if option they are not allowed.
      if (!editor.opts.pasteAllowLocalImages && node.nodeType == Node.ELEMENT_NODE && node.tagName == 'IMG' && node.getAttribute('data-fr-src') && node.getAttribute('data-fr-src').indexOf('file://') == 0) {
        node.parentNode.removeChild(node);
        return false;
      }

      if (node.nodeType == Node.ELEMENT_NODE && $.FE.HTML5Map[node.tagName] && editor.node.attributes(node) === '') {
        var tg = $.FE.HTML5Map[node.tagName];
        var new_node = '<' + tg + '>' + node.innerHTML + '</' + tg + '>';
        node.insertAdjacentHTML('beforebegin', new_node);
        node = node.previousSibling;
        node.parentNode.removeChild(node.nextSibling);
      }

      if (!editor.opts.htmlAllowComments && node.nodeType == Node.COMMENT_NODE) {
        // Do not remove FROALA.EDITOR comments.
        if (node.data.indexOf('[FROALA.EDITOR') !== 0) {
          node.parentNode.removeChild(node);
        }
      }

      // Remove completely tags in denied tags.
      else if (node.tagName && node.tagName.match(removeTagsRE)) {
        node.parentNode.removeChild(node);
      }

      // Unwrap tags not in allowed tags.
      else if (node.tagName && !node.tagName.match(allowedTagsRE)) {
        node.outerHTML = node.innerHTML;
      }

      // Check denied attributes.
      else {
        var attrs = node.attributes;
        if (attrs) {
          for (var i = attrs.length - 1; i >= 0; i--) {
            var attr = attrs[i];
            if (!attr.nodeName.match(allowedAttrsRE)) {
              node.removeAttribute(attr.nodeName);
            }
          }
        }
      }
    }

    function _run (node) {
      var contents = editor.node.contents(node);
      for (var i = 0; i < contents.length; i++) {
        if (contents[i].nodeType != Node.TEXT_NODE) {
          _run(contents[i]);
        }
      }

      _node(node);
    }

    /**
     * Clean pre.
     */
    function _cleanPre (pre) {
      var content = pre.innerHTML;
      if (content.indexOf('\n') >= 0) {
        pre.innerHTML = content.replace(/\n/g, '<br>');
      }
    }

    /**
     * Clean the html input.
     */
    var scripts = [];
    function html (dirty_html, denied_tags, denied_attrs, full_page) {
      if (typeof denied_tags == 'undefined') denied_tags = [];
      if (typeof denied_attrs == 'undefined') denied_attrs = [];
      if (typeof full_page == 'undefined') full_page = false;

      // Strip tabs.
      dirty_html = dirty_html.replace(/\u0009/g, '');

      // Build the allowed tags array.
      var allowed_tags = $.merge([], editor.opts.htmlAllowedTags);
      var i;
      for (i = 0; i < denied_tags.length; i++) {
        if (allowed_tags.indexOf(denied_tags[i]) >= 0) {
          allowed_tags.splice(allowed_tags.indexOf(denied_tags[i]), 1);
        }
      }

      // Build the allowed attrs array.
      var allowed_attrs = $.merge([], editor.opts.htmlAllowedAttrs);
      for (i = 0; i < denied_attrs.length; i++) {
        if (allowed_attrs.indexOf(denied_attrs[i]) >= 0) {
          allowed_attrs.splice(allowed_attrs.indexOf(denied_attrs[i]), 1);
        }
      }

      // We should allow data-fr.
      allowed_attrs.push('data-fr-.*');
      allowed_attrs.push('fr-.*');

      // Generate cleaning RegEx.
      allowedTagsRE = new RegExp('^' + allowed_tags.join('$|^') + '$', 'gi');
      allowedAttrsRE = new RegExp('^' + allowed_attrs.join('$|^') + '$', 'gi');
      removeTagsRE = new RegExp('^' + editor.opts.htmlRemoveTags.join('$|^') + '$', 'gi');

      dirty_html = exec(dirty_html, _run, true);

      return dirty_html;
    }

    /**
     * Clean quotes.
     */
    function quotes () {
      // Join quotes.
      var sibling_quotes = editor.$el.get(0).querySelectorAll('blockquote + blockquote');
      for (var k = 0; k < sibling_quotes.length; k++) {
        var quote = sibling_quotes[k];
        if (editor.node.attributes(quote) == editor.node.attributes(quote.previousSibling)) {
          $(quote).prev().append($(quote).html());
          $(quote).remove();
        }
      }
    }

    function _tablesWrapTHEAD () {
      var trs = editor.$el.get(0).querySelectorAll('tr');

      // Make sure the TH lives inside thead.
      for (var i = 0; i < trs.length; i++) {
        if (trs[i].querySelectorAll('th').length) {
          var tr = trs[i];

          while (tr && tr.tagName != 'TABLE' && tr.tagName != 'THEAD') {
            tr = tr.parentNode;
          }

          var thead = tr;
          if (thead.tagName != 'THEAD') {
            thead = editor.doc.createElement('THEAD');
            tr.insertBefore(thead, tr.firstChild);
          }

          thead.appendChild(trs[i]);
        }
      }
    }

    function _tablesBRBefore () {
      // Make sure we have a br before tables.
      var tbls = editor.$el.get(0).querySelectorAll('table');
      for (var i = 0; i < tbls.length; i++) {
        var prev_node = tbls[i].previousSibling;

        while (prev_node && prev_node.nodeType == Node.TEXT_NODE && prev_node.textContent.length == 0) {
          prev_node = prev_node.previousSibling;
        }

        if (prev_node && !editor.node.isBlock(prev_node) && prev_node.tagName != 'BR' && (prev_node.nodeType == Node.TEXT_NODE || prev_node.nodeType == Node.ELEMENT_NODE) && !$(prev_node).is(editor.opts.htmlDoNotWrapTags.join(','))) {
          tbls[i].parentNode.insertBefore(editor.doc.createElement('br'), tbls[i]);
        }
      }
    }

    function _tablesRemovePFromCell () {
      // Remove P from TH and TH.
      var default_tag = editor.html.defaultTag();
      if (default_tag) {
        var nodes = editor.$el.get(0).querySelectorAll('td > ' + default_tag + ', th > ' + default_tag);
        for (var i = 0; i < nodes.length; i++) {
          if (editor.node.attributes(nodes[i]) === '') {
            $(nodes[i]).replaceWith(nodes[i].innerHTML + '<br>');
          }
        }
      }
    }

    /**
     * Clean tables.
     */
    function tables () {
      _tablesWrapTHEAD();

      _tablesBRBefore();

      _tablesRemovePFromCell();
    }

    function _listsWrapMissplacedLI () {
      // Find missplaced list items.
      var lis = [];
      var filterListItem = function (li) {
        return !editor.node.isList(li.parentNode);
      };

      do {
        if (lis.length) {
          var li = lis[0];
          var ul = editor.doc.createElement('ul');
          li.parentNode.insertBefore(ul, li);
          do {
            var tmp = li;
            li = li.nextSibling;
            ul.appendChild(tmp);
          } while (li && li.tagName == 'LI');
        }

        lis = [];
        var li_sel = editor.$el.get(0).querySelectorAll('li');
        for (var i = 0; i < li_sel.length; i++) {
          if (filterListItem(li_sel[i])) lis.push(li_sel[i]);
        }
      } while (lis.length > 0);
    }

    function _listsJoinSiblings () {
      // Join lists.
      var sibling_lists = editor.$el.get(0).querySelectorAll('ol + ol, ul + ul');
      for (var k = 0; k < sibling_lists.length; k++) {
        var list = sibling_lists[k];
        if (editor.node.attributes(list) == editor.node.attributes(list.previousSibling)) {
          var childs = editor.node.contents(list);
          for (var i = 0; i < childs.length; i++) {
            list.previousSibling.appendChild(childs[i]);
          }
          list.parentNode.removeChild(list);
        }
      }
    }

    function _listsRemoveEmpty () {
      // Remove empty lists.
      var do_remove;
      var removeEmptyList = function (lst) {
        if (lst.querySelectorAll('LI').length === 0) {
          do_remove = true;
          lst.parentNode.removeChild(lst);
        }
      };

      do {
        do_remove = false;

        // Remove empty li.
        var empty_lis = editor.$el.get(0).querySelectorAll('li:empty');
        for (var i = 0; i < empty_lis.length; i++) {
          empty_lis[i].parentNode.removeChild(empty_lis[i]);
        }

        // Remove empty ul and ol.
        var remaining_lists = editor.$el.get(0).querySelectorAll('ul, ol');
        for (var i = 0; i < remaining_lists.length; i++) {
          removeEmptyList(remaining_lists[i]);
        }
      } while (do_remove === true);
    }

    function _listsWrapLists () {
      // Do not allow list directly inside another list.
      var direct_lists = editor.$el.get(0).querySelectorAll('ul > ul, ol > ol, ul > ol, ol > ul');
      for (var i = 0; i < direct_lists.length; i++) {
        var list = direct_lists[i];
        var prev_li = list.previousSibling;
        if (prev_li) {
          if (prev_li.tagName == 'LI') {
            prev_li.appendChild(list);
          }
          else {
            $(list).wrap('<li></li>');
          }
        }
      }
    }

    function _listsNoTagAfterNested () {
      // Check if nested lists don't have HTML after them.
      var nested_lists = editor.$el.get(0).querySelectorAll('li > ul, li > ol');
      for (var i = 0; i < nested_lists.length; i++) {
        var lst = nested_lists[i];

        if (lst.nextSibling) {
          var node = lst.nextSibling;
          var $new_li = $('<li>');
          $(lst.parentNode).after($new_li);
          do {
            var tmp = node;
            node = node.nextSibling;
            $new_li.append(tmp);
          } while (node);
        }
      }
    }

    function _listsTypeInNested () {
      // Make sure we can type in nested list.
      var nested_lists = editor.$el.get(0).querySelectorAll('li > ul, li > ol');
      for (var i = 0; i < nested_lists.length; i++) {
        var lst = nested_lists[i];

        // List is the first in the LI.
        if (editor.node.isFirstSibling(lst)) {
          $(lst).before('<br/>');
        }

        // Make sure we don't leave BR before list.
        else if (lst.previousSibling && lst.previousSibling.tagName == 'BR') {
          var prev_node = lst.previousSibling.previousSibling;

          // Skip markers.
          while (prev_node && $(prev_node).hasClass('fr-marker')) {
            prev_node = prev_node.previousSibling;
          }

          // Remove BR only if there is something else than BR.
          if (prev_node && prev_node.tagName != 'BR') {
            $(lst.previousSibling).remove();
          }
        }
      }
    }

    function _listsRemoveEmptyLI () {
      // Remove empty li.
      var empty_lis = editor.$el.get(0).querySelectorAll('li:empty');
      for (var i = 0; i < empty_lis.length; i++) {
        $(empty_lis[i]).remove();
      }
    }

    /**
     * Clean lists.
     */
    function lists () {
      _listsWrapMissplacedLI();

      _listsJoinSiblings();

      _listsRemoveEmpty();

      _listsWrapLists();

      _listsNoTagAfterNested();

      _listsTypeInNested();

      _listsRemoveEmptyLI();
    }

    /**
     * Initialize
     */
    function _init () {
      // If fullPage is on allow head and title.
      if (editor.opts.fullPage) {
        $.merge(editor.opts.htmlAllowedTags, ['head', 'title', 'style', 'link', 'base', 'body', 'html']);
      }
    }

    return {
      _init: _init,
      html: html,
      toHTML5: toHTML5,
      tables: tables,
      lists: lists,
      quotes: quotes,
      invisibleSpaces: invisibleSpaces,
      exec: exec
    }
  };


  $.FE.XS = 0;
  $.FE.SM = 1;
  $.FE.MD = 2;
  $.FE.LG = 3;

  $.FE.MODULES.helpers = function (editor) {
    /**
     * Get the IE version.
     */
    function _ieVersion () {
      /*global navigator */
      var rv = -1;
      var ua;
      var re;

      if (navigator.appName == 'Microsoft Internet Explorer') {
        ua = navigator.userAgent;
        re = new RegExp('MSIE ([0-9]{1,}[\\.0-9]{0,})');
        if (re.exec(ua) !== null)
          rv = parseFloat(RegExp.$1);
      } else if (navigator.appName == 'Netscape') {
        ua = navigator.userAgent;
        re = new RegExp('Trident/.*rv:([0-9]{1,}[\\.0-9]{0,})');
        if (re.exec(ua) !== null)
          rv = parseFloat(RegExp.$1);
      }
      return rv;
    }

    /**
     * Determine the browser.
     */
    function _browser () {
      var browser = {};

      var ie_version = _ieVersion();
      if (ie_version > 0) {
        browser.msie = true;
      } else {
        var ua = navigator.userAgent.toLowerCase();

        var match =
          /(edge)[ \/]([\w.]+)/.exec(ua) ||
          /(chrome)[ \/]([\w.]+)/.exec(ua) ||
          /(webkit)[ \/]([\w.]+)/.exec(ua) ||
          /(opera)(?:.*version|)[ \/]([\w.]+)/.exec(ua) ||
          /(msie) ([\w.]+)/.exec(ua) ||
          ua.indexOf('compatible') < 0 && /(mozilla)(?:.*? rv:([\w.]+)|)/.exec(ua) ||
          [];

        var matched = {
          browser: match[1] || '',
          version: match[2] || '0'
        };

        if (match[1]) browser[matched.browser] = true;

        // Chrome is Webkit, but Webkit is also Safari.
        if (browser.chrome) {
          browser.webkit = true;
        } else if (browser.webkit) {
          browser.safari = true;
        }
      }

      if (browser.msie) browser.version = ie_version;

      return browser;
    }

    function isIOS () {
      return /(iPad|iPhone|iPod)/g.test(navigator.userAgent) && !isWindowsPhone();
    }

    function isAndroid () {
      return /(Android)/g.test(navigator.userAgent) && !isWindowsPhone();
    }

    function isBlackberry () {
      return /(Blackberry)/g.test(navigator.userAgent);
    }

    function isWindowsPhone () {
      return /(Windows Phone)/gi.test(navigator.userAgent);
    }

    function isMobile () {
      return isAndroid() || isIOS() || isBlackberry();
    }

    function requestAnimationFrame () {
      return window.requestAnimationFrame ||
              window.webkitRequestAnimationFrame ||
              window.mozRequestAnimationFrame    ||
              function (callback) {
                window.setTimeout(callback, 1000 / 60);
              };
    }

    function getPX (val) {
      return parseInt(val, 10) || 0;
    }

    function screenSize () {
      var $test = $('<div class="fr-visibility-helper"></div>').appendTo('body');
      var size = getPX($test.css('margin-left'));
      $test.remove();
      return size;
    }

    function isTouch () {
      return ('ontouchstart' in window) || window.DocumentTouch && document instanceof DocumentTouch;
    }

    function isURL (url) {
      if (!/^(https?:|ftps?:|)\/\//.test(url)) return false;

      url = String(url)
              .replace(/</g, '%3C')
              .replace(/>/g, '%3E')
              .replace(/"/g, '%22')
              .replace(/ /g, '%20');


      var test_reg = /(http|ftp|https):\/\/[\w-]+(\.[\w-]*)*([\w.,@?^=%&amp;:\/~+#-]*[\w@?^=%&amp;\/~+#-])?/gi;

      return test_reg.test(url);
    }

    // Sanitize URL.
    function sanitizeURL (url) {
      if (/^(https?:|ftps?:|)\/\//.test(url)) {
        if (!isURL(url)) {
          return '';
        }
      }
      else {
        url = encodeURIComponent(url)
                  .replace(/%23/g, '#')
                  .replace(/%2F/g, '/')
                  .replace(/%25/g, '%')
                  .replace(/mailto%3A/g, 'mailto:')
                  .replace(/file%3A/g, 'file:')
                  .replace(/sms%3A/g, 'sms:')
                  .replace(/tel%3A/g, 'tel:')
                  .replace(/notes%3A/g, 'notes:')
                  .replace(/data%3Aimage/g, 'data:image')
                  .replace(/webkit-fake-url%3A/g, 'webkit-fake-url:')
                  .replace(/%3F/g, '?')
                  .replace(/%3D/g, '=')
                  .replace(/%26/g, '&')
                  .replace(/&amp;/g, '&')
                  .replace(/%2C/g, ',')
                  .replace(/%3B/g, ';')
                  .replace(/%2B/g, '+')
                  .replace(/%40/g, '@');
      }

      return url;
    }

    function isArray (obj) {
      return obj && !(obj.propertyIsEnumerable('length')) &&
              typeof obj === 'object' && typeof obj.length === 'number';
    }

    /*
     * Transform RGB color to hex value.
     */
    function RGBToHex (rgb) {
      function hex(x) {
        return ('0' + parseInt(x, 10).toString(16)).slice(-2);
      }

      try {
        if (!rgb || rgb === 'transparent') return '';

        if (/^#[0-9A-F]{6}$/i.test(rgb)) return rgb;

        rgb = rgb.match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/);

        return ('#' + hex(rgb[1]) + hex(rgb[2]) + hex(rgb[3])).toUpperCase();
      }
      catch (ex) {
        return null;
      }
    }

    function HEXtoRGB (hex) {
      // Expand shorthand form (e.g. "03F") to full form (e.g. "0033FF")
      var shorthandRegex = /^#?([a-f\d])([a-f\d])([a-f\d])$/i;
      hex = hex.replace(shorthandRegex, function (m, r, g, b) {
        return r + r + g + g + b + b;
      });

      var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
      return result ? 'rgb(' + parseInt(result[1], 16) + ', ' + parseInt(result[2], 16) + ', ' + parseInt(result[3], 16) + ')' : '';
    }

    /*
     * Get block alignment.
     */
    var default_alignment;
    function getAlignment ($block) {
      var alignment = ($block.css('text-align') || '').replace(/-(.*)-/g, '');

      // Detect rtl.
      if (['left', 'right', 'justify', 'center'].indexOf(alignment) < 0) {
        if (!default_alignment) {
          var $div = $('<div dir="auto" style="text-align: initial; position: fixed; left: -3000px;"><span id="s1">.</span><span id="s2">.</span></div>');
          $('body').append($div);

          var l1 = $div.find('#s1').get(0).getBoundingClientRect().left;
          var l2 = $div.find('#s2').get(0).getBoundingClientRect().left;

          $div.remove();

          default_alignment = l1 < l2 ? 'left' : 'right';
        }

        alignment = default_alignment;
      }

      return alignment;
    }

    /**
     * Tear up.
     */
    function _init () {
      editor.browser = _browser();
    }

    return {
      _init: _init,
      isIOS: isIOS,
      isAndroid: isAndroid,
      isBlackberry: isBlackberry,
      isWindowsPhone: isWindowsPhone,
      isMobile: isMobile,
      requestAnimationFrame: requestAnimationFrame,
      getPX: getPX,
      screenSize: screenSize,
      isTouch: isTouch,
      sanitizeURL: sanitizeURL,
      isArray: isArray,
      RGBToHex: RGBToHex,
      HEXtoRGB: HEXtoRGB,
      isURL: isURL,
      getAlignment: getAlignment
    }
  }


  $.FE.MODULES.events = function (editor) {
    var _events = {};
    var _do_blur;

    function _assignEvent($el, evs, handler) {
      $on($el, evs, handler);
    }

    function _forPaste () {
      _assignEvent(editor.$el, 'cut copy paste beforepaste', function (e) {
        trigger(e.type, [e]);
      });
    }

    function _forElement() {
      _assignEvent(editor.$el, 'click mouseup mousedown touchstart touchend dragenter dragover dragleave dragend drop dragstart', function (e) {
        trigger(e.type, [e]);
      });

      on('mousedown', function () {
        for (var i = 0; i < $.FE.INSTANCES.length; i++) {
          if ($.FE.INSTANCES[i] != editor) {
            $.FE.INSTANCES[i].$el.find('.fr-marker').remove();
          }
        }
      })
    }

    function _forKeys () {
      // Map events.
      _assignEvent(editor.$el, 'keydown keypress keyup input', function (e) {
        trigger(e.type, [e]);
      });
    }

    function _forWindow () {
      _assignEvent(editor.$win, editor._mousedown, function (e) {
        trigger('window.mousedown', [e]);
        enableBlur();
      });

      _assignEvent(editor.$win, editor._mouseup, function (e) {
        trigger('window.mouseup', [e]);
      });

      _assignEvent(editor.$win, 'keydown keyup touchmove', function (e) {
        trigger('window.' + e.type, [e]);
      });
    }

    function _forDocument() {
      _assignEvent(editor.$doc, 'dragend drop', function (e) {
        trigger('document.' + e.type, [e]);
      })
    }

    function focus (do_focus) {
      if (typeof do_focus == 'undefined') do_focus = true;
      if (!editor.$wp) return false;

      // If there is no focus, then force focus.
      if (!editor.core.hasFocus() && do_focus) {
        editor.$el.focus();
        return false;
      }

      // Don't go further if we haven't focused or there are markers.
      if (!editor.core.hasFocus() || editor.$el.find('.fr-marker').length > 0) {
        return false;
      }

      var info = editor.selection.info(editor.$el.get(0));

      if (info.atStart && editor.selection.isCollapsed()) {
        if (editor.html.defaultTag() != null) {
          var marker = editor.markers.insert();
          if (marker && !editor.node.blockParent(marker)) {
            $(marker).remove();

            var element = editor.$el.find(editor.html.blockTagsQuery()).get(0);
            if (element) {
              $(element).prepend($.FE.MARKERS);
              editor.selection.restore();
            }
          }
          else if (marker) {
            $(marker).remove();
          }
        }
      }
    }

    var focused = false;

    function _forFocus () {
      _assignEvent(editor.$el, 'focus', function (e) {
        if (blurActive()) {
          focus(false);
          if (focused === false) {
            trigger(e.type, [e]);
          }
        }
      });

      _assignEvent(editor.$el, 'blur', function (e) {
        if (blurActive() /* && document.activeElement != this */) {
          if (focused === true) {
            trigger(e.type, [e]);
          }
        }
      });

      on('focus', function () {
        focused = true;
      });

      on('blur', function () {
        focused = false;
      });
    }

    function _forMouse () {
      if (editor.helpers.isMobile()) {
        editor._mousedown = 'touchstart';
        editor._mouseup = 'touchend';
        editor._move = 'touchmove';
        editor._mousemove = 'touchmove';
      }
      else {
        editor._mousedown = 'mousedown';
        editor._mouseup = 'mouseup';
        editor._move = '';
        editor._mousemove = 'mousemove';
      }
    }

    function _buttonMouseDown (e) {
      var $btn = $(e.currentTarget);
      if (editor.edit.isDisabled() || $btn.hasClass('fr-disabled')) {
        e.preventDefault();
        return false;
      }

      // Not click button.
      if (e.type === 'mousedown' && e.which !== 1) return true;

      // Scroll in list.
      if (!editor.helpers.isMobile()) {
        e.preventDefault();
      }

      if ((editor.helpers.isAndroid() || editor.helpers.isWindowsPhone()) && $btn.parents('.fr-dropdown-menu').length === 0) {
        e.preventDefault();
        e.stopPropagation();
      }

      // Simulate click.
      $btn.addClass('fr-selected');

      editor.events.trigger('commands.mousedown', [$btn]);
    }

    function _buttonMouseUp (e, handler) {
      var $btn = $(e.currentTarget);

      if (editor.edit.isDisabled() || $btn.hasClass('fr-disabled')) {
        e.preventDefault();
        return false;
      }

      if (e.type === 'mouseup' && e.which !== 1) return true;
      if (!$btn.hasClass('fr-selected')) return true;

      if (e.type != 'touchmove') {
        e.stopPropagation();
        e.stopImmediatePropagation();
        e.preventDefault();

        // Simulate click.
        if (!$btn.hasClass('fr-selected')) {
          $('.fr-selected').removeClass('fr-selected');
          return false;
        }
        $('.fr-selected').removeClass('fr-selected');

        if ($btn.data('dragging') || $btn.attr('disabled')) {
          $btn.removeData('dragging');
          return false;
        }

        var timeout = $btn.data('timeout');
        if (timeout) {
          clearTimeout(timeout);
          $btn.removeData('timeout');
        }

        handler.apply(editor, [e]);
      }
      else {
        if (!$btn.data('timeout')) {
          $btn.data('timeout', setTimeout(function () {
            $btn.data('dragging', true);
          }, 100));
        }
      }
    }

    function enableBlur () {
      _do_blur = true;
    }

    function disableBlur () {
      _do_blur = false;
    }

    function blurActive () {
      return _do_blur;
    }

    /**
     * Bind click on an element.
     */
    function bindClick ($element, selector, handler) {
      $on($element, editor._mousedown, selector, function (e) {
        _buttonMouseDown(e);
      }, true);

      $on($element, editor._mouseup + ' ' + editor._move, selector, function (e) {
        _buttonMouseUp(e, handler);
      }, true);

      $on($element, 'mousedown click mouseup', selector, function (e) {
        e.stopPropagation();
      }, true);

      on('window.mouseup', function () {
        $element.find(selector).removeClass('fr-selected');
        enableBlur();
      });
    }

    /**
     * Add event.
     */
    function on (name, callback, first) {
      var names = name.split(' ');
      if (names.length > 1) {
        for (var i = 0; i < names.length; i++) {
          on(names[i], callback, first);
        }

        return true;
      }

      if (typeof first == 'undefined') first = false;

      var callbacks;
      if (name.indexOf('shared.') != 0) {
        callbacks = (_events[name] = _events[name] || []);
      }
      else {
        callbacks = (editor.shared._events[name] = editor.shared._events[name] || []);
      }

      if (first) {
        callbacks.unshift(callback);
      }
      else {
        callbacks.push(callback);
      }
    }

		var $_events = [];
		function $on ($el, evs, selector, callback, shared) {
      if (typeof selector == 'function') {
        shared = callback;
        callback = selector;
        selector = false;
      }

      var ary = (!shared ? $_events : editor.shared.$_events);
      var id = (!shared ? editor.id : editor.sid);

      if (!selector) {
        $el.on(evs.split(' ').join('.ed' + id + ' ') + '.ed' + id, callback);
      }
      else {
        $el.on(evs.split(' ').join('.ed' + id + ' ') + '.ed' + id, selector, callback);
      }

      if (ary.indexOf($el.get(0)) < 0) ary.push($el.get(0));
		}

    function _$off (evs, id) {
			for (var i = 0; i < evs.length; i++) {
				$(evs[i]).off('.ed' + id);
			}
    }

		function $off () {
      _$off($_events, editor.id);

      if (editor.shared.count == 0) {
  			_$off(editor.shared.$_events, editor.sid);
      }
		}

    /**
     * Trigger an event.
     */
    function trigger (name, args, force) {
      if (!editor.edit.isDisabled() || force) {
        var callbacks;

        if (name.indexOf('shared.') != 0) {
          callbacks = _events[name];
        }
        else {
          if (editor.shared.count > 0) return false;
          callbacks = editor.shared._events[name];
        }

        var val;

        if (callbacks) {
          for (var i = 0; i < callbacks.length; i++) {
            val = callbacks[i].apply(editor, args);
            if (val === false) return false;
          }
        }

        // Trigger event outside.
        val = editor.$oel.triggerHandler('froalaEditor.' + name, $.merge([editor], (args || [])));
        if (val === false) return false;

        return val;
      }
    }

    function chainTrigger (name, param, force) {
      if (!editor.edit.isDisabled() || force) {
        var callbacks;

        if (name.indexOf('shared.') != 0) {
          callbacks = _events[name];
        }
        else {
          if (editor.shared.count > 0) return false;
          callbacks = editor.shared._events[name];
        }

        var resp;

        if (callbacks) {
          for (var i = 0; i < callbacks.length; i++) {
            // Get the callback response.
            resp = callbacks[i].apply(editor, [param]);

            // If callback response is defined then assign it to param.
            if (typeof resp !== 'undefined') param = resp;
          }
        }

        // Trigger event outside.
        resp = editor.$oel.triggerHandler('froalaEditor.' + name, $.merge([editor], [param]));

        // If callback response is defined then assign it to param.
        if (typeof resp !== 'undefined') param = resp;

        return param;
      }
    }

    /**
     * Destroy
     */
    function _destroy () {
      // Clear the events list.
      for (var k in _events) {
        delete _events[k];
      }
    }

    function _sharedDestroy () {
      for (var k in _events) {
        delete editor.shared._events[k];
      }
    }

    /**
     * Tear up.
     */
    function _init () {
      editor.shared.$_events = editor.shared.$_events || [];
      editor.shared._events = {};

      _forMouse();

      _forElement();
      _forWindow();
      _forDocument();
      _forKeys();

      _forFocus();
      enableBlur();

      _forPaste();

      on('destroy', _destroy);
      on('shared.destroy', _sharedDestroy);
    }

    return {
      _init: _init,
      on: on,
      trigger: trigger,
      bindClick: bindClick,
      disableBlur: disableBlur,
      enableBlur: enableBlur,
      blurActive: blurActive,
      focus: focus,
      chainTrigger: chainTrigger,
			$on: $on,
			$off: $off
    }
  };


  $.FE.INVISIBLE_SPACE = '&#8203;';
  $.FE.START_MARKER = '<span class="fr-marker" data-id="0" data-type="true" style="display: none; line-height: 0;">' + $.FE.INVISIBLE_SPACE + '</span>';
  $.FE.END_MARKER = '<span class="fr-marker" data-id="0" data-type="false" style="display: none; line-height: 0;">'  + $.FE.INVISIBLE_SPACE + '</span>';
  $.FE.MARKERS = $.FE.START_MARKER + $.FE.END_MARKER;

  $.FE.MODULES.markers = function (editor) {
    /**
     * Build marker element.
     */
    function _build (marker, id) {
      return $('<span class="fr-marker" data-id="' + id + '" data-type="' + marker + '" style="display: ' + (editor.browser.safari ? 'none' : 'inline-block') + '; line-height: 0;">' + $.FE.INVISIBLE_SPACE + '</span>', editor.doc)[0];
    }

    /**
     * Place marker.
     */
    function place (range, marker, id) {
      try {
        var boundary = range.cloneRange();
        boundary.collapse(marker);

        boundary.insertNode(_build(marker, id));

        if (marker === true && range.collapsed) {
          var mk = editor.$el.find('span.fr-marker[data-type="true"][data-id="' + id + '"]');
          var sibling = mk.get(0).nextSibling;
          while (sibling && sibling.nodeType === Node.TEXT_NODE && sibling.textContent.length === 0) {
            $(sibling).remove();
            sibling = mk.nextSibling;
          }
        }

        if (marker === true && !range.collapsed) {
          var mk = editor.$el.find('span.fr-marker[data-type="true"][data-id="' + id + '"]').get(0);
          var sibling = mk.nextSibling;
          if (sibling && sibling.nodeType === Node.ELEMENT_NODE && editor.node.isBlock(sibling)) {
            // Place the marker deep inside the block tags.
            var contents = [sibling];
            do {
              sibling = contents[0];
              contents = editor.node.contents(sibling);
            } while (contents[0] && editor.node.isBlock(contents[0]));

            $(sibling).prepend($(mk));
          }
        }

        if (marker === false && !range.collapsed) {
          var mk = editor.$el.find('span.fr-marker[data-type="false"][data-id="' + id + '"]').get(0);
          var sibling = mk.previousSibling;
          if (sibling && sibling.nodeType === Node.ELEMENT_NODE && editor.node.isBlock(sibling)) {
            // Place the marker deep inside the block tags.
            var contents = [sibling];
            do {
              sibling = contents[contents.length - 1];
              contents = editor.node.contents(sibling);
            } while (contents[contents.length - 1] && editor.node.isBlock(contents[contents.length - 1]));

            $(sibling).append($(mk));
          }

          // https://github.com/froala/wysiwyg-editor/issues/705
          if (mk.parentNode && ['TD', 'TH'].indexOf(mk.parentNode.tagName) >= 0) {
            if (mk.parentNode.previousSibling && !mk.previousSibling) {
              $(mk.parentNode.previousSibling).append(mk);
            }
          }
        }

        var dom_marker = editor.$el.find('span.fr-marker[data-type="' + marker + '"][data-id="' + id + '"]').get(0);

        if (dom_marker) dom_marker.style.display = 'none';
        return dom_marker;
      }
      catch (ex) {
        return null;
      }
    }

    /**
     * Insert a single marker.
     */
    function insert () {
      if (!editor.$wp) return null;

      try {
        var range = editor.selection.ranges(0);
        var containter = range.commonAncestorContainer;

        // Check if selection is inside editor.
        if (containter != editor.$el.get(0) && editor.$el.find(containter).length == 0) return null;

        var boundary = range.cloneRange();
        var original_range = range.cloneRange();
        boundary.collapse(true);

        var mk = $('<span class="fr-marker" style="display: none; line-height: 0;">' + $.FE.INVISIBLE_SPACE + '</span>', editor.doc)[0];

        boundary.insertNode(mk);

        mk = editor.$el.find('span.fr-marker').get(0);

        if (mk) {
          var sibling = mk.nextSibling;
          while (sibling && sibling.nodeType === Node.TEXT_NODE && sibling.textContent.length === 0) {
            $(sibling).remove();
            sibling = editor.$el.find('span.fr-marker').get(0).nextSibling;
          }

          // Keep original selection.
          editor.selection.clear();
          editor.selection.get().addRange(original_range);

          return mk;
        }
        else {
          return null;
        }
      }
      catch (ex) {
        console.warn ('MARKER', ex)
      }
    }

    /**
     * Split HTML at the marker position.
     */
    function split () {
      if (!editor.selection.isCollapsed()) {
        editor.selection.remove();
      }

      var marker = editor.$el.find('.fr-marker').get(0);
      if (marker == null) marker = insert();
      if (marker == null) return null;

      var deep_parent;
      if ((deep_parent = editor.node.deepestParent(marker))) {
        if (editor.node.isBlock(deep_parent) && editor.node.isEmpty(deep_parent)) {
          $(deep_parent).replaceWith('<span class="fr-marker"></span>');
        }
        else {
          var node = marker;
          var close_str = '';
          var open_str = '';
          do {
            node = node.parentNode;
            close_str = close_str + editor.node.closeTagString(node);
            open_str = editor.node.openTagString(node) + open_str;
          } while (node != deep_parent);

          $(marker).replaceWith('<span id="fr-break"></span>');
          var h = editor.node.openTagString(deep_parent) + $(deep_parent).html() + editor.node.closeTagString(deep_parent);
          h = h.replace(/<span id="fr-break"><\/span>/g, close_str + '<span class="fr-marker"></span>' + open_str);

          $(deep_parent).replaceWith(h);
        }
      }

      return editor.$el.find('.fr-marker').get(0)
    }

    /**
     * Insert marker at point from event.
     *
     * http://stackoverflow.com/questions/11191136/set-a-selection-range-from-a-to-b-in-absolute-position
     * https://developer.mozilla.org/en-US/docs/Web/API/this.document.caretPositionFromPoint
     */
    function insertAtPoint (e) {
      var x = e.clientX;
      var y = e.clientY;

      // Clear markers.
      remove();

      var start;
      var range = null;

      // Default.
      if (typeof editor.doc.caretPositionFromPoint != 'undefined') {
        start = editor.doc.caretPositionFromPoint(x, y);
        range = editor.doc.createRange();

        range.setStart(start.offsetNode, start.offset);
        range.setEnd(start.offsetNode, start.offset);
      }

      // Webkit.
      else if (typeof editor.doc.caretRangeFromPoint != 'undefined') {
        start = editor.doc.caretRangeFromPoint(x, y);
        range = editor.doc.createRange();

        range.setStart(start.startContainer, start.startOffset);
        range.setEnd(start.startContainer, start.startOffset);
      }

      // Set ranges.
      if (range !== null && typeof editor.win.getSelection != 'undefined') {
        var sel = editor.win.getSelection();
        sel.removeAllRanges();
        sel.addRange(range);
      }

      // MSIE.
      else if (typeof editor.doc.body.createTextRange != 'undefined') {
        try {
          range = editor.doc.body.createTextRange();
          range.moveToPoint(x, y);
          var end_range = range.duplicate();
          end_range.moveToPoint(x, y);
          range.setEndPoint('EndToEnd', end_range);
          range.select();
        }
        catch (ex) {
          return false;
        }
      }

      insert();
    }

    /**
     * Remove markers.
     */
    function remove () {
      editor.$el.find('.fr-marker').remove();
    }

    return {
      place: place,
      insert: insert,
      split: split,
      insertAtPoint: insertAtPoint,
      remove: remove
    }
  };


  $.FE.MODULES.selection = function (editor) {
    /**
     * Get selection text.
     */
    function text () {
      var text = '';

      if (editor.win.getSelection) {
        text = editor.win.getSelection();
      } else if (editor.doc.getSelection) {
        text = editor.doc.getSelection();
      } else if (editor.doc.selection) {
        text = editor.doc.selection.createRange().text;
      }

      return text.toString();
    }

    /**
     * Get the selection object.
     */
    function get () {
      var selection = '';
      if (editor.win.getSelection) {
        selection = editor.win.getSelection();
      } else if (editor.doc.getSelection) {
        selection = editor.doc.getSelection();
      } else {
        selection = editor.doc.selection.createRange();
      }

      return selection;
    }

    /**
     * Get the selection ranges or a single range at a specified index.
    */
    function ranges (index) {
      var sel = get();
      var ranges = [];

      // Get ranges.
      if (sel && sel.getRangeAt && sel.rangeCount) {
        var ranges = [];
        for (var i = 0; i < sel.rangeCount; i++) {
          ranges.push(sel.getRangeAt(i));
        }
      }
      else {
        if (editor.doc.createRange) {
          ranges = [editor.doc.createRange()];
        } else {
          ranges = [];
        }
      }

      return (typeof index != 'undefined' ? ranges[index] : ranges);
    }

    /**
     * Clear selection.
     */
    function clear () {
      var sel = get();

      try {
        if (sel.removeAllRanges) {
          sel.removeAllRanges();
        } else if (sel.empty) {  // IE?
          sel.empty();
        } else if (sel.clear) {
          sel.clear();
        }
      }
      catch (ex) {}
    }

    /**
     * Selection element.
    */
    function element () {
      var sel = get();

      try {
        if (sel.rangeCount) {
          var range = ranges(0);
          var node = range.startContainer;

          // Get parrent if node type is not DOM.
          if (node.nodeType == Node.ELEMENT_NODE) {
            var node_found = false;

            // Search for node deeper.
            if (node.childNodes.length > 0 && node.childNodes[range.startOffset]) {
              var child = node.childNodes[range.startOffset];
              while (child && child.nodeType == Node.TEXT_NODE && child.textContent.length == 0) {
                child = child.nextSibling;
              }

              if (child && child.textContent.replace(/\u200B/g, '') === text().replace(/\u200B/g, '')) {
                node = child;
                node_found = true;
              }
            }
            // Selection starts just at the end of the node.
            else if (!range.collapsed && node.nextSibling && node.nextSibling.nodeType == Node.ELEMENT_NODE) {
              var child = node.nextSibling;

              if (child && child.textContent.replace(/\u200B/g, '') === text().replace(/\u200B/g, '')) {
                node = child;
                node_found = true;
              }
            }

            if (!node_found && node.childNodes.length > 0 && $(node.childNodes[0]).text().replace(/\u200B/g, '') === text().replace(/\u200B/g, '') && ['BR', 'IMG', 'HR'].indexOf(node.childNodes[0].tagName) < 0) {
              node = node.childNodes[0];
            }
          }

          while (node.nodeType != Node.ELEMENT_NODE && node.parentNode) {
            node = node.parentNode;
          }

          // Make sure the node is in editor.
          var p = node;
          while (p && p.tagName != 'HTML') {
            if (p == editor.$el.get(0)) {
              return node;
            }

            p = $(p).parent()[0];
          }
        }
      }
      catch (ex) {

      }

      return editor.$el.get(0);
    }

    /**
     * Selection element.
    */
    function endElement () {
      var sel = get();

      try {
        if (sel.rangeCount) {
          var range = ranges(0);
          var node = range.endContainer;

          // Get parrent if node type is not DOM.
          if (node.nodeType == Node.ELEMENT_NODE) {
            var node_found = false;

            // Search for node deeper.
            if (node.childNodes.length > 0 && node.childNodes[range.endOffset] && $(node.childNodes[range.endOffset]).text() === text()) {
              node = node.childNodes[range.endOffset];
              node_found = true;
            }
            // Selection starts just at the end of the node.
            else if (!range.collapsed && node.previousSibling && node.previousSibling.nodeType == Node.ELEMENT_NODE) {
              var child = node.previousSibling;

              if (child && child.textContent.replace(/\u200B/g, '') === text().replace(/\u200B/g, '')) {
                node = child;
                node_found = true;
              }
            }

            if (!node_found && node.childNodes.length > 0 && $(node.childNodes[node.childNodes.length - 1]).text() === text() && ['BR', 'IMG', 'HR'].indexOf(node.childNodes[node.childNodes.length - 1].tagName) < 0) {
              node = node.childNodes[node.childNodes.length - 1];
            }
          }

          if (node.nodeType == Node.TEXT_NODE && range.endOffset == 0 && node.previousSibling && node.previousSibling.nodeType == Node.ELEMENT_NODE) {
            node = node.previousSibling;
          }

          while (node.nodeType != Node.ELEMENT_NODE && node.parentNode) {
            node = node.parentNode;
          }

          // Make sure the node is in editor.
          var p = node;
          while (p && p.tagName != 'HTML') {
            if (p == editor.$el.get(0)) {
              return node;
            }

            p = $(p).parent()[0];
          }
        }
      }
      catch (ex) {

      }

      return editor.$el.get(0);
    }

    /**
     * Get the ELEMENTS node where the selection starts.
     * By default TEXT node might be selected.
     */
    function rangeElement(rangeContainer, offset) {
      var node = rangeContainer;
      if (node.nodeType == Node.ELEMENT_NODE) {
        // Search for node deeper.
        if (node.childNodes.length > 0 && node.childNodes[offset]) {
          node = node.childNodes[offset];
        }
      }

      if (node.nodeType == Node.TEXT_NODE) {
        node = node.parentNode;
      }

      return node;
    }

    /**
     * Search for the current selected blocks.
     */
    function blocks () {
      var blks = [];

      var sel = get();

      // Selection must be inside editor.
      if (inEditor() && sel.rangeCount) {

        // Loop through ranges.
        var rngs = ranges();
        for (var i = 0; i < rngs.length; i++) {
          var range = rngs[i];

          // Get start node and end node for range.
          var start_node = rangeElement(range.startContainer, range.startOffset);
          var end_node  = rangeElement(range.endContainer, range.endOffset);

          // Add the start node.
          if (editor.node.isBlock(start_node) && blks.indexOf(start_node) < 0) blks.push(start_node);

          // Check for the parent node of the start node.
          var block_parent = editor.node.blockParent(start_node);
          if (block_parent && blks.indexOf(block_parent) < 0) {
            blks.push(block_parent);
          }

          // Do not add nodes where we've been once.
          var was_into = [];

          // Loop until we reach end.
          var next_node = start_node;
          while (next_node !== end_node && next_node !== editor.$el.get(0)) {
            // Get deeper into the current node.
            if (was_into.indexOf(next_node) < 0 && next_node.children && next_node.children.length) {
              was_into.push(next_node);
              next_node = next_node.children[0];
            }

            // Get next sibling.
            else if (next_node.nextSibling) {
              next_node = next_node.nextSibling;
            }

            // Get parent node.
            else if (next_node.parentNode) {
              next_node = next_node.parentNode;
              was_into.push(next_node);
            }

            // Add node to the list.
            if (editor.node.isBlock(next_node) && was_into.indexOf(next_node) < 0 && blks.indexOf(next_node) < 0) blks.push(next_node);
          }

          // Add the end node.
          if (editor.node.isBlock(end_node) && blks.indexOf(end_node) < 0) blks.push(end_node);

          // Check for the parent node of the end node.
          var block_parent = editor.node.blockParent(end_node);
          if (block_parent && blks.indexOf(block_parent) < 0) {
            blks.push(block_parent);
          }
        }
      }

      // Remove blocks that we don't need.
      for (var i = blks.length - 1; i > 0; i--) {
        // Nodes that contain another node. Don't do it for LI.
        if ($(blks[i]).find(blks).length && blks[i].tagName != 'LI') blks.splice(i, 1);
      }

      return blks;
    }

    /**
     * Save selection.
     */
    function save () {
      if (editor.$wp) {
        editor.markers.remove();

        var rgs = ranges();
        var new_ranges = [];

        for (var i = 0; i < rgs.length; i++) {
          if (rgs[i].startContainer !== editor.doc) {
            var range = rgs[i];
            var collapsed = range.collapsed;
            var start_m = editor.markers.place(range, true, i); // Start.
            var end_m = editor.markers.place(range, false, i); // End.

            if (editor.browser.safari && !collapsed) {
              var range = editor.doc.createRange();
              range.setStartAfter(start_m);
              range.setEndBefore(end_m);
              new_ranges.push(range);
            }
          }
        }

        if (editor.browser.safari && new_ranges.length) {
          editor.selection.clear();
          for (var i = 0; i < new_ranges.length; i++) {
            editor.selection.get().addRange(new_ranges[i]);
          }
        }
      }
    }

    /**
     * Restore selection.
     */
    function restore () {
      // Get markers.
      var markers = editor.$el.get(0).querySelectorAll('.fr-marker[data-type="true"]');

      if (!editor.$wp) {
        editor.markers.remove();
        return false;
      }

      // No markers.
      if (markers.length === 0) {
        return false;
      }

      if (editor.browser.msie || editor.browser.edge) {
        for (var i = 0; i < markers.length; i++) {
          markers[i].style.display = 'inline-block';
        }
      }

      // Focus.
      if (!editor.core.hasFocus() && !editor.browser.msie && !editor.browser.webkit) {
        editor.$el.focus();
      }

      clear();
      var sel = get();

      var parents = [];

      // Add ranges.
      for (var i = 0; i < markers.length; i++) {
        var id = $(markers[i]).data('id');
        var start_marker = markers[i];
        var range = editor.doc.createRange();
        var end_marker = editor.$el.find('.fr-marker[data-type="false"][data-id="' + id + '"]');

        if (editor.browser.msie || editor.browser.edge) end_marker.css('display', 'inline-block');

        var ghost = null;

        // Make sure there is an start marker.
        if (end_marker.length > 0) {
          end_marker = end_marker[0];

          try {
            // If we have markers one next to each other inside text, then we should normalize text by joining it.
            var special_case = false;

            // Clear empty text nodes.
            var s_node = start_marker.nextSibling;
            while (s_node && s_node.nodeType == Node.TEXT_NODE && s_node.textContent.length == 0) {
              var tmp = s_node;
              s_node = s_node.nextSibling;
              $(tmp).remove();
            }

            var e_node = end_marker.nextSibling;
            while (e_node && e_node.nodeType == Node.TEXT_NODE && e_node.textContent.length == 0) {
              var tmp = e_node;
              e_node = e_node.nextSibling;
              $(tmp).remove();
            }

            if (start_marker.nextSibling == end_marker || end_marker.nextSibling == start_marker) {
              // Decide which is first and which is last between markers.
              var first_node = (start_marker.nextSibling == end_marker) ? start_marker : end_marker;
              var last_node = (first_node == start_marker) ? end_marker : start_marker;

              // Previous node.
              var prev_node = first_node.previousSibling;
              while (prev_node && prev_node.nodeType == Node.TEXT_NODE && prev_node.length == 0) {
                var tmp = prev_node;
                prev_node = prev_node.previousSibling;
                $(tmp).remove();
              }

              // Normalize text before.
              if (prev_node && prev_node.nodeType == Node.TEXT_NODE) {
                while (prev_node && prev_node.previousSibling && prev_node.previousSibling.nodeType == Node.TEXT_NODE) {
                  prev_node.previousSibling.textContent = prev_node.previousSibling.textContent + prev_node.textContent;
                  prev_node = prev_node.previousSibling;
                  $(prev_node.nextSibling).remove();
                }
              }

              // Next node.
              var next_node = last_node.nextSibling;
              while (next_node && next_node.nodeType == Node.TEXT_NODE && next_node.length == 0) {
                var tmp = next_node;
                next_node = next_node.nextSibling;
                $(tmp).remove();
              }

              // Normalize text after.
              if (next_node && next_node.nodeType == Node.TEXT_NODE) {
                while (next_node && next_node.nextSibling && next_node.nextSibling.nodeType == Node.TEXT_NODE) {
                  next_node.nextSibling.textContent =  next_node.textContent + next_node.nextSibling.textContent;
                  next_node = next_node.nextSibling;
                  $(next_node.previousSibling).remove();
                }
              }

              if (prev_node && editor.node.isVoid(prev_node)) prev_node = null;
              if (next_node && editor.node.isVoid(next_node)) next_node = null;

              // Previous node and next node are both text.
              if (prev_node && next_node && prev_node.nodeType == Node.TEXT_NODE && next_node.nodeType == Node.TEXT_NODE) {
                // Remove markers.
                $(start_marker).remove();
                $(end_marker).remove();

                // Save cursor position.
                var len = prev_node.textContent.length;
                prev_node.textContent = prev_node.textContent + next_node.textContent;
                $(next_node).remove();

                // Normalize spaces.
                editor.html.normalizeSpaces(prev_node);

                // Restore position.
                range.setStart(prev_node, len);
                range.setEnd(prev_node, len);

                special_case = true;
              }
              else if (!prev_node && next_node && next_node.nodeType == Node.TEXT_NODE) {
                // Remove markers.
                $(start_marker).remove();
                $(end_marker).remove();

                // Normalize spaces.
                editor.html.normalizeSpaces(next_node);

                ghost = $(editor.doc.createTextNode('\u200B'));
                $(next_node).before(ghost);

                // Restore position.
                range.setStart(next_node, 0);
                range.setEnd(next_node, 0);
                special_case = true;
              }
              else if (!next_node && prev_node && prev_node.nodeType == Node.TEXT_NODE) {
                // Remove markers.
                $(start_marker).remove();
                $(end_marker).remove();

                // Normalize spaces.
                editor.html.normalizeSpaces(prev_node);

                ghost = $(editor.doc.createTextNode('\u200B'));
                $(prev_node).after(ghost);

                // Restore position.
                range.setStart(prev_node, prev_node.textContent.length);
                range.setEnd(prev_node, prev_node.textContent.length);

                special_case = true;
              }
            }

            if (!special_case) {
              var x, y;
              // DO NOT TOUCH THIS OR IT WILL BREAK!!!
              if (editor.browser.chrome && start_marker.nextSibling == end_marker) {
                x = _normalizedMarker(end_marker, range, true) || range.setStartAfter(end_marker);
                y = _normalizedMarker(start_marker, range, false) || range.setEndBefore(start_marker);
              }
              else {
                if (start_marker.previousSibling == end_marker) {
                  start_marker = end_marker;
                  end_marker = start_marker.nextSibling;
                }

                // https://github.com/froala/wysiwyg-editor/issues/759
                if (!(end_marker.nextSibling && end_marker.nextSibling.tagName === 'BR') &&
                    !(!end_marker.nextSibling && editor.node.isBlock(start_marker.previousSibling)) &&
                    !(start_marker.previousSibling && start_marker.previousSibling.tagName == 'BR')) {
                  start_marker.style.display = 'inline';
                  end_marker.style.display = 'inline';
                  ghost = $(editor.doc.createTextNode('\u200B'));
                }

                x = _normalizedMarker(start_marker, range, true) || ($(start_marker).before(ghost) && range.setStartBefore(start_marker));
                y = _normalizedMarker(end_marker, range, false) || ($(end_marker).after(ghost) && range.setEndAfter(end_marker));
              }

              if (typeof x == 'function') x();
              if (typeof y == 'function') y();
            }
          } catch (ex) {
            console.warn ('RESTORE RANGE', ex);
          }
        }

        if (ghost) {
          ghost.remove();
        }

        sel.addRange(range);
      }

      // Remove used markers.
      editor.markers.remove();
    }

    /**
     * Normalize marker when restoring selection.
     */
    function _normalizedMarker(marker, range, start) {
      var prev_node = marker.previousSibling;
      var next_node = marker.nextSibling;

      // Prev and next node are both text nodes.
      if (prev_node && next_node && prev_node.nodeType == Node.TEXT_NODE && next_node.nodeType == Node.TEXT_NODE) {
        var len = prev_node.textContent.length;

        if (start) {
         next_node.textContent = prev_node.textContent + next_node.textContent;
         $(prev_node).remove();
         $(marker).remove();

         editor.html.normalizeSpaces(next_node);

         return function () {
           range.setStart(next_node, len);
         }
        }
        else {
          prev_node.textContent = prev_node.textContent + next_node.textContent;
          $(next_node).remove();
          $(marker).remove();

          editor.html.normalizeSpaces(prev_node);

          return function () {
            range.setEnd(prev_node, len);
          }
        }
      }
      // Prev node is text node.
      else if (prev_node && !next_node && prev_node.nodeType == Node.TEXT_NODE) {
        var len = prev_node.textContent.length;

        if (start) {
          editor.html.normalizeSpaces(prev_node);
          return function () {
            range.setStart(prev_node, len);
          }
        }
        else {
          editor.html.normalizeSpaces(prev_node);
          return function () {
            range.setEnd(prev_node, len);
          }
        }
      }

      // Next node is text node.
      else if (next_node && !prev_node && next_node.nodeType == Node.TEXT_NODE) {
        if (start) {
          editor.html.normalizeSpaces(next_node);
          return function () {
            range.setStart(next_node, 0);
          }
        }
        else {
          editor.html.normalizeSpaces(next_node);
          return function () {
            range.setEnd(next_node, 0);
          }
        }
      }

      return false;
    }

    /**
     * Determine if we can do delete.
     */
    function _canDelete () {
      return true;
    }

    /**
     * Check if selection is collapsed.
     */
    function isCollapsed () {
      var rgs = ranges();
      for (var i = 0; i < rgs.length; i++) {
        if (!rgs[i].collapsed) return false;
      }

      return true;
    }

    // From: http://www.coderexception.com/0B1B33z1NyQxUQSJ/contenteditable-div-how-can-i-determine-if-the-cursor-is-at-the-start-or-end-of-the-content
    function info (el) {
      var atStart = false;
      var atEnd = false;
      var selRange;
      var testRange;

      if (editor.win.getSelection) {
        var sel = editor.win.getSelection();
        if (sel.rangeCount) {
          selRange = sel.getRangeAt(0);
          testRange = selRange.cloneRange();

          testRange.selectNodeContents(el);
          testRange.setEnd(selRange.startContainer, selRange.startOffset);
          atStart = (testRange.toString() === '');

          testRange.selectNodeContents(el);
          testRange.setStart(selRange.endContainer, selRange.endOffset);
          atEnd = (testRange.toString() === '');
        }
      } else if (editor.doc.selection && editor.doc.selection.type != 'Control') {
        selRange = editor.doc.selection.createRange();
        testRange = selRange.duplicate();

        testRange.moveToElementText(el);
        testRange.setEndPoint('EndToStart', selRange);
        atStart = (testRange.text === '');

        testRange.moveToElementText(el);
        testRange.setEndPoint('StartToEnd', selRange);
        atEnd = (testRange.text === '');
      }

      return { atStart: atStart, atEnd: atEnd };
    }

    /**
     * Check if everything is selected inside the editor.
     */
    function isFull () {
      if (isCollapsed()) return false;

      // https://github.com/froala/wysiwyg-editor/issues/710
      editor.$el.find('td').prepend('<span class="fr-mk">' + $.FE.INVISIBLE_SPACE + '</span>');
      editor.$el.find('img').append('<span class="fr-mk">' + $.FE.INVISIBLE_SPACE + '</span>');

      var full = false;
      var inf = info(editor.$el.get(0));
      if (inf.atStart && inf.atEnd) full = true;

      // https://github.com/froala/wysiwyg-editor/issues/710
      editor.$el.find('.fr-mk').remove();

      return full;
    }

    /**
     * Remove HTML from inner nodes when we deal with keepFormatOnDelete option.
     */
    function _emptyInnerNodes (node, first) {
      if (typeof first == 'undefined') first = true;

      // Remove invisible spaces.
      var h = $(node).html();
      if (h && h.replace(/\u200b/g, '').length != h.length) $(node).html(h.replace(/\u200b/g, ''));

      // Loop contents.
      var contents = editor.node.contents(node);
      for (var j = 0; j < contents.length; j++) {
        // Remove text nodes.
        if (contents[j].nodeType != Node.ELEMENT_NODE) {
          $(contents[j]).remove();
        }

        // Empty inner nodes further.
        else {
          // j == 0 determines if the node is the first one and we should keep format.
          _emptyInnerNodes(contents[j], j == 0);

          // There are inner nodes, ignore the current one.
          if (j == 0) first = false;
        }
      }

      // First node is a text node, so replace it with a span.
      if (node.nodeType == Node.TEXT_NODE) {
        $(node).replaceWith('<span data-first="true" data-text="true"></span>');
      }

      // Add the first node marker so that we add selection in it later on.
      else if (first) {
        $(node).attr('data-first', true);
      }
    }

    /**
     * Process deleting nodes.
     */
    function _processNodeDelete ($node, should_delete) {
      var contents = editor.node.contents($node.get(0));

      // Node is TD or TH.
      if (['TD', 'TH'].indexOf($node.get(0).tagName) >= 0 && $node.find('.fr-marker').length == 1 && $(contents[0]).hasClass('fr-marker')) {
        $node.attr('data-del-cell', true);
      }

      for (var i = 0; i < contents.length; i++) {
        var node = contents[i];

        // We found a marker.
        if ($(node).hasClass('fr-marker')) {
          should_delete = (should_delete + 1) % 2;
        }
        else if (should_delete) {
          // Check if we have a marker inside it.
          if ($(node).find('.fr-marker').length > 0) {
            should_delete = _processNodeDelete($(node), should_delete);
          }
          else {
            if (['TD', 'TH'].indexOf(node.tagName) < 0 && !$(node).hasClass('fr-inner')) {
              if (!editor.opts.keepFormatOnDelete || should_delete > 1 || editor.$el.find('[data-first]').length > 0) {
                $(node).remove();
              }
              else {
                _emptyInnerNodes(node);
              }
            }
            else if ($(node).hasClass('fr-inner')) {
              if ($(node).find('.fr-inner').length == 0) {
                $(node).html('<br>');
              }
              else {
                $(node).find('.fr-inner').filter(function () {
                  return $(this).find('fr-inner').length == 0;
                }).html('<br>');
              }
            }
            else {
              $(node).empty();
              $(node).attr('data-del-cell', true);
            }
          }
        }
        else {
          if ($(node).find('.fr-marker').length > 0) {
            should_delete = _processNodeDelete($(node), should_delete);
          }
        }
      }

      return should_delete;
    }

    /**
     * Determine if selection is inside the editor.
     */
    function inEditor () {
      try {
        if (!editor.$wp) return false;

        var range = ranges(0);
        var container = range.commonAncestorContainer;

        while (container && !editor.node.isElement(container)) {
          container = container.parentNode;
        }

        if (editor.node.isElement(container)) return true;
        return false;
      }
      catch (ex) {
        return false;
      }
    }

    /**
     * Remove the current selection html.
     */
    function remove () {
      save();

      // Get the previous sibling normalized.
      var _prevSibling = function (node) {
        var prev_node = node.previousSibling;
        while (prev_node && prev_node.nodeType == Node.TEXT_NODE && prev_node.textContent.length == 0) {
          var tmp = prev_node;
          var prev_node = prev_node.previousSibling;
          $(tmp).remove();
        }

        return prev_node;
      }

      // Get the next sibling normalized.
      var _nextSibling = function (node) {
        var next_node = node.nextSibling;
        while (next_node && next_node.nodeType == Node.TEXT_NODE && next_node.textContent.length == 0) {
          var tmp = next_node;
          var next_node = next_node.nextSibling;
          $(tmp).remove();
        }

        return next_node;
      }

      // Normalize start markers.
      var start_markers = editor.$el.find('.fr-marker[data-type="true"]');
      for (var i = 0; i < start_markers.length; i++) {
        var sm = start_markers[i];
        while (!_prevSibling(sm) && !editor.node.isBlock(sm.parentNode)) {
          $(sm.parentNode).before(sm);
        }
      }

      // Normalize end markers.
      var end_markers = editor.$el.find('.fr-marker[data-type="false"]');
      for (var i = 0; i < end_markers.length; i++) {
        var em = end_markers[i];
        while (!_nextSibling(em) && !editor.node.isBlock(em.parentNode)) {
          $(em.parentNode).after(em);
        }
      }

      // Check if selection can be deleted.
      if (_canDelete()) {
        _processNodeDelete(editor.$el, 0);

        // Look for selection marker.
        var $first_node = editor.$el.find('[data-first="true"]');
        if ($first_node.length) {
          // Remove markers.
          editor.$el.find('.fr-marker').remove();

          // Add markers in the node that we marked as the first one.
          $first_node
            .append($.FE.INVISIBLE_SPACE + $.FE.MARKERS)
            .removeAttr('data-first');

          // Remove span with data-text if there is one.
          if ($first_node.attr('data-text')) {
            $first_node.replaceWith($first_node.html());
          }
        }
        else {
          // Remove tables.
          editor.$el.find('table').filter(function () {
            var ok = $(this).find('[data-del-cell]').length > 0 && $(this).find('[data-del-cell]').length == $(this).find('td, th').length;

            return ok;
          }).remove();
          editor.$el.find('[data-del-cell]').removeAttr('data-del-cell');

          // Merge contents between markers.
          var start_markers = editor.$el.find('.fr-marker[data-type="true"]');
          for (var i = 0; i < start_markers.length; i++) {
            // Get start marker.
            var start_marker = start_markers[i];

            // Get the next node after start marker.
            var next_node = start_marker.nextSibling;

            // Get the end node.
            var end_marker = editor.$el.find('.fr-marker[data-type="false"][data-id="' + $(start_marker).data('id') + '"]').get(0);

            if (end_marker) {
              // Markers are next to other.
              if (next_node && next_node == end_marker) {
                // Do nothing.
              }
              else if (start_marker) {
                // Get the parents of the nodes.
                var start_parent = editor.node.blockParent(start_marker);
                var end_parent = editor.node.blockParent(end_marker);

                // Move end marker next to start marker.
                $(start_marker).after(end_marker);

                // We're in the same parent. Moving marker is enough.
                if (start_parent == end_parent) {
                }

                // The block parent of the start marker is the element itself.
                else if (start_parent == null) {
                  var deep_parent = editor.node.deepestParent(start_marker);

                  // There is a parent for the marker. Move the end html to it.
                  if (deep_parent) {
                    $(deep_parent).after($(end_parent).html());
                    $(end_parent).remove();
                  }

                  // There is no parent for the marker.
                  else if ($(end_parent).parentsUntil(editor.$el, 'table').length == 0) {
                    $(start_marker).next().after($(end_parent).html());
                    $(end_parent).remove();
                  }
                }

                // End marker is inside element. We don't merge in table.
                else if (end_parent == null && $(start_parent).parentsUntil(editor.$el, 'table').length == 0) {
                  // Get the node that has a next sibling.
                  var next_node = start_parent;
                  while (!next_node.nextSibling && next_node.parentNode != editor.$el.get(0)) {
                    next_node = next_node.parentNode;
                  }
                  next_node = next_node.nextSibling;

                  // Join HTML inside the start node.
                  while (next_node && next_node.tagName != 'BR') {
                    var tmp_node = next_node.nextSibling;
                    $(start_parent).append(next_node);
                    next_node = tmp_node;
                  }
                }

                // Join end block with start block.
                else if ($(start_parent).parentsUntil(editor.$el, 'table').length == 0 && $(end_parent).parentsUntil(editor.$el, 'table').length == 0) {
                  $(start_parent).append($(end_parent).html());
                  $(end_parent).remove();
                }
              }
            }

            else {
              end_marker = $(start_marker).clone().attr('data-type', false);
              $(start_marker).after(end_marker);
            }
          }
        }
      }

      if (!editor.opts.keepFormatOnDelete) {
        editor.html.fillEmptyBlocks();
      }

      editor.html.cleanEmptyTags(true);
      editor.clean.lists();

      editor.html.normalizeSpaces();
      restore();
    }

    function setAtStart (node) {
      if ($(node).find('.fr-marker').length > 0) return false;

      var contents = editor.node.contents(node);
      while (contents.length && editor.node.isBlock(contents[0])) {
        node = contents[0];
        contents = editor.node.contents(node);
      }

      $(node).prepend($.FE.MARKERS);
    }

    function setAtEnd (node) {
      if ($(node).find('.fr-marker').length > 0) return false;

      var contents = editor.node.contents(node);
      while (contents.length && editor.node.isBlock(contents[contents.length - 1])) {
        node = contents[contents.length - 1];
        contents = editor.node.contents(node);
      }

      $(node).append($.FE.MARKERS);
    }

    function setBefore (node) {
      var prev_node = node.previousSibling;
      while (prev_node && prev_node.nodeType == Node.TEXT_NODE && prev_node.textContent.length == 0) {
        prev_node = prev_node.previousSibling;
      }

      if (prev_node) {
        if (editor.node.isBlock(prev_node)) {
          setAtEnd(prev_node);
        }
        else if (prev_node.tagName == 'BR') {
          $(prev_node).before($.FE.MARKERS);
        }
        else {
          $(prev_node).after($.FE.MARKERS);
        }

        return true;
      }
      else {
        return false;
      }
    }

    function setAfter (node) {
      var next_node = node.nextSibling;
      while (next_node && next_node.nodeType == Node.TEXT_NODE && next_node.textContent.length == 0) {
        next_node = next_node.nextSibling;
      }

      if (next_node) {
        if (editor.node.isBlock(next_node)) {
          setAtStart(next_node);
        }
        else {
          $(next_node).before($.FE.MARKERS);
        }

        return true;
      }
      else {
        return false;
      }
    }

    return {
      text: text,
      get: get,
      ranges: ranges,
      clear: clear,
      element: element,
      endElement: endElement,
      save: save,
      restore: restore,
      isCollapsed: isCollapsed,
      isFull: isFull,
      inEditor: inEditor,
      remove: remove,
      blocks: blocks,
      info: info,
      setAtEnd: setAtEnd,
      setAtStart: setAtStart,
      setBefore: setBefore,
      setAfter: setAfter,
      rangeElement: rangeElement
    }
  };


  $.FE.UNICODE_NBSP = String.fromCharCode(160);

  // Void Elements http://www.w3.org/html/wg/drafts/html/master/syntax.html#void-elements
  $.FE.VOID_ELEMENTS = ['area', 'base', 'br', 'col', 'embed', 'hr', 'img', 'input', 'keygen', 'link', 'menuitem', 'meta', 'param', 'source', 'track', 'wbr'];

  $.FE.BLOCK_TAGS = ['p', 'div', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'pre', 'blockquote', 'ul', 'ol', 'li', 'table', 'td', 'th', 'thead', 'tfoot', 'tbody', 'tr', 'hr', 'dl', 'dt', 'dd', 'form'];

  // Extend defaults.
  $.extend($.FE.DEFAULTS, {
    htmlAllowedEmptyTags: ['textarea', 'a', 'iframe', 'object', 'video', 'style', 'script', '.fa', '.fr-emoticon'],
    htmlDoNotWrapTags: ['script', 'style'],
    htmlSimpleAmpersand: false
  });

  $.FE.MODULES.html = function (editor) {
    /**
     * Determine the default block tag.
     */
    function defaultTag () {
      if (editor.opts.enter == $.FE.ENTER_P) return 'p';
      if (editor.opts.enter == $.FE.ENTER_DIV) return 'div';
      if (editor.opts.enter == $.FE.ENTER_BR) return null;
    }

    /**
     * Get the empty blocs.
     */
    function emptyBlocks () {
      var empty_blocks = [];

      // Block tag elements.
      var els = editor.$el.get(0).querySelectorAll(blockTagsQuery());

      // Check if there are empty block tags with markers.
      for (var i = 0; i < els.length; i++) {
        var contents = editor.node.contents(els[i]);

        var found = false;
        for (var j = 0; j < contents.length; j++) {
          if (contents[j].nodeType == Node.COMMENT_NODE) continue;

          // Element node or void element.
          if ((contents[j].nodeType == Node.ELEMENT_NODE && ($.FE.VOID_ELEMENTS.indexOf(contents[j].tagName.toLowerCase()) >= 0 || contents[j].querySelectorAll(editor.opts.htmlAllowedEmptyTags.join(',')).length > 0)) || (contents[j].textContent && contents[j].textContent.replace(/\u200B/g, '').length > 0)) {
            found = true;
            break;
          }
        }

        // Make sure we don't add TABLE and TD at the same time for instance.
        if (!found && els[i].querySelectorAll(blockTagsQuery()).length === 0) empty_blocks.push(els[i]);
      }

      return empty_blocks;
    }

    /**
     * Create jQuery query for empty block tags.
     */
    function emptyBlockTagsQuery () {
      return $.FE.BLOCK_TAGS.join(':empty, ') + ':empty';
    }

    /**
     * Create jQuery query for selecting block tags.
     */
    function blockTagsQuery () {
      return $.FE.BLOCK_TAGS.join(', ');
    }

    /**
     * Remove empty elements that are not VOID elements.
     */
    function cleanEmptyTags (remove_blocks) {
      var els = $.merge([], $.FE.VOID_ELEMENTS);
      els = $.merge(els, editor.opts.htmlAllowedEmptyTags);

      if (typeof remove_blocks == 'undefined') els = $.merge(els, $.FE.BLOCK_TAGS);

      var elms;
      var ok;
      do {
        ok = false;
        elms = editor.$el.get(0).querySelectorAll('*:empty:not(' + els.join('):not(') + '):not(.fr-marker)');

        // Remove those elements that have no attributes.
        for (var i = 0; i < elms.length; i++) {
          if (elms[i].attributes.length === 0 || typeof elms[i].getAttribute('href') !== 'undefined') {
            $(elms[i]).remove();
            ok = true;
          }
        }

        elms = editor.$el.get(0).querySelectorAll('*:empty:not(' + els.join('):not(') + '):not(.fr-marker)');
      } while (elms.length && ok);

    }

    /**
     * Wrap the content inside the element passed as argument.
     */
    function _wrapElement($el, temp) {
      var default_tag = defaultTag();
      if (temp) default_tag = 'div class="fr-temp-div"';

      if (default_tag) {
        var contents = editor.node.contents($el.get(0));
        var $anchor = null;

        // Loop through contents.
        for (var i = 0; i < contents.length; i++) {

          // Current node.
          var node = contents[i];

          // Current node is a block node.
          if (node.nodeType == Node.ELEMENT_NODE && (editor.node.isBlock(node) || $(node).is(editor.opts.htmlDoNotWrapTags.join(',')))) {
            $anchor = null;
          }

          // Other node types than element and text.
          else if (node.nodeType != Node.ELEMENT_NODE && node.nodeType != Node.TEXT_NODE) {
            $anchor = null;
          }

          // Current node is BR.
          else if (node.nodeType == Node.ELEMENT_NODE && node.tagName == 'BR') {
            // There is no anchor.
            if ($anchor == null) {
              if (temp) {
                $(node).replaceWith('<' + default_tag + ' data-empty="true"><br></div>');
              }
              else {
                $(node).replaceWith('<' + default_tag + '><br></' + default_tag + '>');
              }
            }

            // There is anchor. Just remove BR.
            else {
              $(node).remove();

              // Check if in anchor we have something else than markers.
              var cnts = editor.node.contents($anchor);
              var found = false;
              for (var j = 0; j < cnts.length; j++) {
                if (!$(cnts[j]).hasClass('fr-marker') && !(cnts[j].nodeType == Node.TEXT_NODE && cnts[j].textContent.replace(/ /g, '').length === 0)) {
                  found = true;
                  break;
                }
              }

              if (found === false) {
                $anchor.append('<br>');
                $anchor.data('empty', true);
              }

              $anchor = null;
            }
          }

          // Text node or other node type.
          else {
            if (node.nodeType == Node.TEXT_NODE && $(node).text().trim().length == 0) {
              $(node).remove();
            }
            else {
              if ($anchor == null) {
                $anchor = $('<' + default_tag + '>');
                $(node).before($anchor);
              }

              if (node.nodeType == Node.TEXT_NODE && $(node).text().trim().length > 0) {
                $anchor.append($(node).clone());
                $(node).remove();
              }
              else {
                $anchor.append($(node));
              }
            }
          }
        }
      }
    }

    /**
     * Wrap the direct content inside the default block tag.
     */
    function _wrap (temp, tables, blockquote) {
      if (!editor.$wp) return false;

      if (typeof temp == 'undefined') temp = false;
      if (typeof tables == 'undefined') tables = false;
      if (typeof blockquote == 'undefined') blockquote = false;

      // Wrap element.
      _wrapElement(editor.$el, temp);

      editor.$el.find('.fr-inner').each (function () {
        _wrapElement($(this), temp);
      })

      // Wrap table contents.
      if (tables) {
        editor.$el.find('td, th').each (function () {
          _wrapElement($(this), temp);
        })
      }

      // Wrap table contents.
      if (blockquote) {
        editor.$el.find('blockquote').each (function () {
          _wrapElement($(this), temp);
        })
      }
    }

    /**
     * Unwrap temporary divs.
     */
    function unwrap () {
      editor.$el.find('div.fr-temp-div').each(function () {
        if ($(this).data('empty') || this.parentNode.tagName == 'LI') {
          $(this).replaceWith($(this).html());
        }
        else {
          $(this).replaceWith($(this).html() + '<br>');
        }
      });

      // Remove temp class from other blocks.
      editor.$el.find('.fr-temp-div').removeClass('fr-temp-div').filter(function () {
        return $(this).attr('class') == '';
      }).removeAttr('class');
    }

    /**
     * Add BR inside empty elements.
     */
    function fillEmptyBlocks () {
      var blocks = emptyBlocks();
      for (var i = 0; i < blocks.length; i++) {
        var block = blocks[i];
        if (block.getAttribute('contenteditable') != "false" && block.querySelectorAll(editor.opts.htmlAllowedEmptyTags.join(',')).length == 0 && !editor.node.isVoid(block)) {
          block.appendChild(editor.doc.createElement('br'));
        }
      }
    }

    /**
     * Get the blocks inside the editable area.
     */
    function blocks () {
      return editor.$el.find(blockTagsQuery());
    }

    /**
     * Clean the blank spaces between the block tags.
     */
    function cleanBlankSpaces (node) {
      if (typeof node == 'undefined') node = editor.$el.get(0);

      var contents = editor.node.contents(node);
      // Loop contents.
      for (var i = contents.length - 1; i >= 0; i--) {
        // Content is text and node is block.
        if (contents[i].nodeType == Node.TEXT_NODE) {
          var len = -1;

          // Remove middle spaces.
          contents[i].textContent = contents[i].textContent.replace(/(?!^)( ){2,}(?!$)/g, ' ');

          // Replace new lines with spaces.
          contents[i].textContent = contents[i].textContent.replace(/\n/g, ' ');

          // Replace begin/end spaces.
          contents[i].textContent = contents[i].textContent.replace(/^  /g, ' ');
          contents[i].textContent = contents[i].textContent.replace(/  $/g, ' ');

          // No previous siblings.
          if (!contents[i].previousSibling) {
            contents[i].textContent = contents[i].textContent.replace(/^ */,'');
          }

          // No next siblings.
          if (!contents[i].nextSibling) {
            contents[i].textContent = contents[i].textContent.replace(/ *$/,'');
          }

          if (contents[i].previousSibling && contents[i].nextSibling && contents[i].textContent == ' ') {
            contents[i].textContent = "\n";
          }
        }
        else {
          cleanBlankSpaces(contents[i]);
        }
      }
    }

    function _isBlock (node) {
      return node && (editor.node.isBlock(node) || ['STYLE', 'SCRIPT', 'HEAD', 'BR', 'HR'].indexOf(node.tagName) >= 0 || node.nodeType == Node.COMMENT_NODE);
    }

    /**
     * Make sure spaces always render propely.
     */
    function normalizeSpaces (node) {
      if (typeof node == 'undefined') node = editor.$el.get(0);

      if (node.nodeType == Node.ELEMENT_NODE && ['STYLE', 'SCRIPT', 'HEAD'].indexOf(node.tagName) < 0) {
        var contents = editor.node.contents(node);
        for (var i = contents.length - 1; i >= 0 ; i--) {
          if ((contents[i].className || '').indexOf('fr-marker') < 0) {
            normalizeSpaces(contents[i]);
          }
        }
      }
      else if (node.nodeType == Node.TEXT_NODE && node.textContent.length > 0) {
        var prev_node = node.previousSibling;
        var next_node = node.nextSibling;

        if (_isBlock(prev_node) && _isBlock(next_node) && node.textContent.trim().length === 0) {
          $(node).remove();
        }
        else {
          var txt = node.textContent;
          txt = txt.replace(new RegExp($.FE.UNICODE_NBSP, 'g'), ' ');

          var new_text = ''
          for (var t = 0; t < txt.length; t++) {
            if (txt.charCodeAt(t) == 32 && (t === 0 || new_text.charCodeAt(t - 1) == 32)) {
              new_text += $.FE.UNICODE_NBSP;
            }
            else {
              new_text += txt[t];
            }
          }

          if (!node.nextSibling) new_text = new_text.replace(/ $/, $.FE.UNICODE_NBSP);
          if (node.previousSibling && !editor.node.isVoid(node.previousSibling)) new_text = new_text.replace(/^\u00A0([^ $])/, ' $1');

          new_text = new_text.replace(/([^ \u00A0])\u00A0([^ \u00A0])/g, '$1 $2');

          if (node.textContent != new_text) {
            node.textContent = new_text;
          }
        }
      }
    }

    function doNormalize (node) {
      if (typeof node == 'undefined') node = editor.$el.get(0);

      if (node.nodeType == Node.ELEMENT_NODE && ['STYLE', 'SCRIPT', 'HEAD'].indexOf(node.tagName) < 0) {
        var contents = editor.node.contents(node);
        for (var i = contents.length - 1; i >= 0 ; i--) {
          if (!$(contents[i]).hasClass('fr-marker')) {
            var r = doNormalize(contents[i]);
            if (r == true) return true;
          }
        }
      }
      else if (node.nodeType == Node.TEXT_NODE && node.textContent.length > 0) {
        var prev_node = node.previousSibling;
        var next_node = node.nextSibling;

        if (_isBlock(prev_node) && _isBlock(next_node) && node.textContent.trim().length === 0) {
          return true;
        }
        else {
          var txt = node.textContent;
          txt = txt.replace(new RegExp($.FE.UNICODE_NBSP, 'g'), ' ');

          var new_text = ''
          for (var t = 0; t < txt.length; t++) {
            if (txt.charCodeAt(t) == 32 && (t === 0 || new_text.charCodeAt(t - 1) == 32)) {
              new_text += $.FE.UNICODE_NBSP;
            }
            else {
              new_text += txt[t];
            }
          }

          if (!node.nextSibling) new_text = new_text.replace(/ $/, $.FE.UNICODE_NBSP);
          if (node.previousSibling && !editor.node.isVoid(node.previousSibling)) new_text = new_text.replace(/^\u00A0([^ $])/, ' $1');

          new_text = new_text.replace(/([^ \u00A0])\u00A0([^ \u00A0])/g, '$1 $2');

          if (node.textContent != new_text) {
            return true;
          }
        }
      }

      return false;
    }

    /**
     * Extract a specific match for a RegEx.
     */
    function _extractMatch (html, re, id) {
      var reg_exp = new RegExp(re, 'gi');
      var matches = reg_exp.exec(html);

      if (matches) {
        return matches[id];
      }

      return null;
    }

    /**
     * Create new doctype.
     */
    function _newDoctype (string, doc) {
      var matches = string.match(/<!DOCTYPE ?([^ ]*) ?([^ ]*) ?"?([^"]*)"? ?"?([^"]*)"?>/i);
      if (matches) {
        return doc.implementation.createDocumentType(
          matches[1],
          matches[3],
          matches[4]
        )
      }
      else {
        return doc.implementation.createDocumentType('html');
      }
    }

    /**
     * Get string doctype of a document.
     */
    function getDoctype (doc) {
      var node = doc.doctype;
      var doctype = '<!DOCTYPE html>';
      if (node) {
        doctype = '<!DOCTYPE '
                  + node.name
                  + (node.publicId ? ' PUBLIC "' + node.publicId + '"' : '')
                  + (!node.publicId && node.systemId ? ' SYSTEM' : '')
                  + (node.systemId ? ' "' + node.systemId + '"' : '')
                  + '>';
      }

      return doctype;
    }

    /**
     * Normalize.
     */
    function _normalize () {
      // Wrap possible text.
      _wrap();

      // Clean blank spaces.
      cleanBlankSpaces();

      // Remove empty tags.
      cleanEmptyTags();

      // Normalize spaces.
      normalizeSpaces();

      // Add BR tag where it is necessary.
      fillEmptyBlocks();

      // Clean quotes.
      editor.clean.quotes();

      // Clean lists.
      editor.clean.lists();

      // Clean tables.
      editor.clean.tables();

      // Convert to HTML5.
      editor.clean.toHTML5();

      // Restore selection.
      editor.selection.restore();

      // Check if editor is empty and add placeholder.
      checkIfEmpty();

      // Refresh placeholder.
      editor.placeholder.refresh();
    }

    function checkIfEmpty () {
      if (editor.core.isEmpty()) {
        if (defaultTag() != null) {
          // There is no block tag inside the editor.
          if (editor.$el.get(0).querySelectorAll(blockTagsQuery()).length === 0 &&
               editor.$el.get(0).querySelectorAll(editor.opts.htmlDoNotWrapTags.join(',')).length === 0) {
            if (editor.core.hasFocus()) {
              editor.$el.html('<' + defaultTag() + '>' + $.FE.MARKERS + '<br/></' + defaultTag() + '>');
              editor.selection.restore();
            }
            else {
              editor.$el.html('<' + defaultTag() + '>' + '<br/></' + defaultTag() + '>');
            }
          }
        }
        else {
          // There is nothing in the editor.
          if (editor.$el.get(0).querySelectorAll('*:not(.fr-marker):not(br)').length === 0) {
            if (editor.core.hasFocus()) {
              editor.$el.html($.FE.MARKERS + '<br/>');
              editor.selection.restore();
            }
            else {
              editor.$el.html('<br/>');
            }
          }
        }
      }
    }

    function extractNode (html, tag) {
      return _extractMatch(html, '<' + tag +'[^>]*?>([\\w\\W]*)<\/' + tag + '>', 1);
    }

    function extractNodeAttrs (html, tag) {
      var $dv = $('<div ' + (_extractMatch(html, '<' + tag + '([^>]*?)>', 1) || '') + '>');
      return editor.node.rawAttributes($dv.get(0));
    }

    function extractDoctype (html) {
      return _extractMatch(html, '<!DOCTYPE([^>]*?)>', 0) || '<!DOCTYPE html>';
    }

    /**
     * Set HTML.
     */
    function set (html) {
      var clean_html = editor.clean.html(html, [], [], editor.opts.fullPage);

      clean_html = clean_html.replace(/\r|\n/g, ' ');

      if (!editor.opts.fullPage) {
        editor.$el.html(clean_html);
      }
      else {
        // Get BODY data.
        var body_html = (extractNode(clean_html, 'body') || (clean_html.indexOf('<body') >= 0 ? '' : clean_html));
        var body_attrs = extractNodeAttrs(clean_html, 'body');

        // Get HEAD data.
        var head_html = extractNode(clean_html, 'head') || '<title></title>';
        var head_attrs = extractNodeAttrs(clean_html, 'head');

        // Get DOCTYPE.
        var doctype = extractDoctype(clean_html);

        // Get HTML attributes.
        var html_attrs = extractNodeAttrs(clean_html, 'html');

        editor.$el.html(body_html);
        editor.node.clearAttributes(editor.$el.get(0));
        editor.$el.attr(body_attrs);

        editor.$head.html(head_html);
        editor.node.clearAttributes(editor.$head.get(0));
        editor.$head.attr(head_attrs);

        editor.node.clearAttributes(editor.$html.get(0));

        editor.$html.attr(html_attrs);

        editor.iframe_document.doctype.parentNode.replaceChild(
          _newDoctype(doctype, editor.iframe_document),
          editor.iframe_document.doctype
        );
      }

      // Make sure the content is editable.
      editor.edit.on();

      editor.core.injectStyle(editor.opts.iframeStyle);

      _normalize();

      if (!editor.opts.useClasses) {
        // Restore orignal attributes if present.
        editor.$el.find('[fr-original-class]').each (function () {
          this.setAttribute('class', this.getAttribute('fr-original-class'));
          this.removeAttribute('fr-original-class');
        });

        editor.$el.find('[fr-original-style]').each (function () {
          this.setAttribute('style', this.getAttribute('fr-original-style'));
          this.removeAttribute('fr-original-style');
        });
      }

      editor.events.trigger('html.set');
    }

    /**
     * Get HTML.
     */
    function get (keep_markers, keep_classes) {
      var html = '';

      editor.events.trigger('html.beforeGet');

      var specifity = function (selector) {
        var idRegex = /(#[^\s\+>~\.\[:]+)/g;
        var attributeRegex = /(\[[^\]]+\])/g;
        var classRegex = /(\.[^\s\+>~\.\[:]+)/g;
        var pseudoElementRegex = /(::[^\s\+>~\.\[:]+|:first-line|:first-letter|:before|:after)/gi;
        var pseudoClassWithBracketsRegex = /(:[\w-]+\([^\)]*\))/gi;
  			// A regex for other pseudo classes, which don't have brackets
  			var pseudoClassRegex = /(:[^\s\+>~\.\[:]+)/g;
  			var elementRegex = /([^\s\+>~\.\[:]+)/g;

        // Remove the negation psuedo-class (:not) but leave its argument because specificity is calculated on its argument
    		(function() {
    			var regex = /:not\(([^\)]*)\)/g;
    			if (regex.test(selector)) {
    				selector = selector.replace(regex, '     $1 ');
    			}
    		}());

        var s = (selector.match(idRegex) || []).length * 100 +
                (selector.match(attributeRegex) || []).length * 10 +
                (selector.match(classRegex) || []).length * 10 +
                (selector.match(pseudoClassWithBracketsRegex) || []).length * 10 +
                (selector.match(pseudoClassRegex) || []).length * 10 +
                (selector.match(pseudoElementRegex) || []).length;

        // Remove universal selector and separator characters
    		selector = selector.replace(/[\*\s\+>~]/g, ' ');

    		// Remove any stray dots or hashes which aren't attached to words
    		// These may be present if the user is live-editing this selector
    		selector = selector.replace(/[#\.]/g, ' ');

        s += (selector.match(elementRegex) || []).length;

        return s;
      }

      // Convert STYLE from CSS files to inline style.
      var updated_elms = [];
      var elms_info = {};
      var i;
      if (!editor.opts.useClasses && !keep_classes) {
        for (i = 0; i < editor.doc.styleSheets.length; i++) {
          var rules;
          var head_style = 0;
          try {
            rules = editor.doc.styleSheets[i].cssRules;
            if (editor.doc.styleSheets[i].ownerNode && editor.doc.styleSheets[i].ownerNode.nodeType == 'STYLE') {
              head_style = 1;
            }
          }
          catch (ex) {
          }

          if (rules) {
            for (var idx = 0, len = rules.length; idx < len; idx++) {
              var class_selector = editor.opts.iframe ? 'body ' : '.fr-view ';

              if (rules[idx].selectorText && rules[idx].selectorText.indexOf(class_selector) === 0) {
                if (rules[idx].style.cssText.length > 0) {
                  var selector = rules[idx].selectorText.replace(class_selector, '').replace(/::/g, ':');
                  var elms = editor.$el.get(0).querySelectorAll(selector);

                  for (var j = 0; j < elms.length; j++) {
                    // Save original style.
                    if (!elms[j].getAttribute('fr-original-style') && elms[j].getAttribute('style')) {
                      elms[j].setAttribute('fr-original-style', elms[j].getAttribute('style'));
                      updated_elms.push(elms[j]);
                    }
                    else if (!elms[j].getAttribute('fr-original-style')) {
                      updated_elms.push(elms[j]);
                    }

                    if (!elms_info[elms[j]]) {
                      elms_info[elms[j]] = {};
                    }

                    // Compute specification.
                    var spec = head_style * 1000 + specifity(rules[idx].selectorText);

                    var css_text = rules[idx].style.cssText.split(';');
                    for (var k = 0; k < css_text.length; k++) {
                      var rule = css_text[k].trim().split(':')[0];
                      if (!elms_info[elms[j]][rule]) {
                        elms_info[elms[j]][rule] = 0;

                        if ((elms[j].getAttribute('fr-original-style') || '').indexOf(rule + ':') >= 0) {
                          elms_info[elms[j]][rule] = 10000;
                        }
                      }

                      if (spec >= elms_info[elms[j]][rule]) {
                        elms_info[elms[j]][rule] = spec;
                        if (css_text[k].trim().length) {
                          elms[j].setAttribute('style', (elms[j].getAttribute('style') || '') + css_text[k] + ';');
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }

        // Save original class.
        for (i = 0; i < updated_elms.length; i++) {
          if (updated_elms[i].getAttribute('class')) {
            updated_elms[i].setAttribute('fr-original-class', updated_elms[i].getAttribute('class'));
            updated_elms[i].removeAttribute('class');
          }

          var style = updated_elms[i].getAttribute('style');
          if (style) {
            updated_elms[i].setAttribute('style', style.split(';').sort(function (a, b) {
              a = a.split(':')[0];
              b = b.split(':')[0];

              if (a == '' || b == '') return -1;

              return a.localeCompare(b);
            }).join('; ').trim());
          }
        }
      }

      // If editor is not empty.
      if (!editor.core.isEmpty()) {
        if (typeof keep_markers == 'undefined') keep_markers = false;

        if (!editor.opts.fullPage) {
          html = editor.$el.html();
        }
        else {
          html = getDoctype(editor.iframe_document);
          html += '<html' + editor.node.attributes(editor.$html.get(0)) + '>' + editor.$html.html() + '</html>';
        }
      }
      else if (editor.opts.fullPage) {
        html = getDoctype(editor.iframe_document);
        html += '<html' + editor.node.attributes(editor.$html.get(0)) + '>' + editor.$html.find('head').get(0).outerHTML + '<body></body></html>';
      }

      // Remove unwanted attributes.
      if (!editor.opts.useClasses && !keep_classes) {
        for (i = 0; i < updated_elms.length; i++) {
          if (updated_elms[i].getAttribute('fr-original-class')) {
            updated_elms[i].setAttribute('class', updated_elms[i].getAttribute('fr-original-class'));
            updated_elms[i].removeAttribute('fr-original-class');
          }

          if (updated_elms[i].getAttribute('fr-original-style')) {
            updated_elms[i].setAttribute('style', updated_elms[i].getAttribute('fr-original-style'));
            updated_elms[i].removeAttribute('fr-original-style');
          }
          else {
            updated_elms[i].removeAttribute('style');
          }
        }
      }

      // Clean helpers.
      if (editor.opts.fullPage) {
        html = html.replace(/<style data-fr-style="true">(?:[\w\W]*?)<\/style>/g, '');
        html = html.replace(/<style(?:[\w\W]*?)class="firebugResetStyles"(?:[\w\W]*?)>(?:[\w\W]*?)<\/style>/g, '');
        html = html.replace(/<body((?:[\w\W]*?)) spellcheck="true"((?:[\w\W]*?))>((?:[\w\W]*?))<\/body>/g, '<body$1$2>$3</body>');
        html = html.replace(/<body((?:[\w\W]*?)) contenteditable="(true|false)"((?:[\w\W]*?))>((?:[\w\W]*?))<\/body>/g, '<body$1$3>$4</body>');

        html = html.replace(/<body((?:[\w\W]*?)) dir="([\w]*)"((?:[\w\W]*?))>((?:[\w\W]*?))<\/body>/g, '<body$1$3>$4</body>');
        html = html.replace(/<body((?:[\w\W]*?))class="([\w\W]*?)(fr-rtl|fr-ltr)([\w\W]*?)"((?:[\w\W]*?))>((?:[\w\W]*?))<\/body>/g, '<body$1class="$2$4"$5>$6</body>');
        html = html.replace(/<body((?:[\w\W]*?)) class=""((?:[\w\W]*?))>((?:[\w\W]*?))<\/body>/g, '<body$1$2>$3</body>');
      }

      // Ampersand fix.
      if (editor.opts.htmlSimpleAmpersand) {
        html = html.replace(/\&amp;/gi, '&');
      }

      editor.events.trigger('html.afterGet');

      // Remove markers.
      if (!keep_markers) {
        html = html.replace(/<span[^>]*? class\s*=\s*["']?fr-marker["']?[^>]+>\u200b<\/span>/gi, '');
      }

      html = editor.clean.invisibleSpaces(html);

      var new_html = editor.events.chainTrigger('html.get', html);
      if (typeof new_html == 'string') {
        html = new_html;
      }

      // Deal with pre.
      html = html.replace(/<pre(?:[\w\W]*?)>(?:[\w\W]*?)<\/pre>/g, function (str) {
        return str.replace(/<br>/g, '\n');
      });

      return html;
    }

    /**
     * Get selected HTML.
     */
    function getSelected () {
      var wrapSelection = function (container, node) {
        while (node && (node.nodeType == Node.TEXT_NODE || !editor.node.isBlock(node))) {
          if (node && node.nodeType != Node.TEXT_NODE) {
            $(container).wrapInner(editor.node.openTagString(node) + editor.node.closeTagString(node));
          }

          node = node.parentNode;
        }

        if (node && container.innerHTML == node.innerHTML) {
          container.innerHTML = node.outerHTML;
        }
      }

      var selectionParent = function () {
        var parent = null;
        var sel;

        if (editor.win.getSelection) {
          sel = editor.win.getSelection();
          if (sel && sel.rangeCount) {
            parent = sel.getRangeAt(0).commonAncestorContainer;
            if (parent.nodeType != Node.ELEMENT_NODE) {
              parent = parent.parentNode;
            }
          }
        } else if ((sel = editor.doc.selection) && sel.type != 'Control') {
          parent = sel.createRange().parentElement();
        }

        if (parent != null && ($.inArray(editor.$el.get(0), $(parent).parents()) >= 0 || parent == editor.$el.get(0))) {
          return parent;
        }
        else {
          return null;
        }
      }

      var html = '';
      if (typeof editor.win.getSelection != 'undefined') {

        // Multiple ranges hack.
        if (editor.browser.mozilla) {
          editor.selection.save();
          if (editor.$el.find('.fr-marker[data-type="false"]').length > 1) {
            editor.$el.find('.fr-marker[data-type="false"][data-id="0"]').remove();
            editor.$el.find('.fr-marker[data-type="false"]:last').attr('data-id', '0');
            editor.$el.find('.fr-marker').not('[data-id="0"]').remove();
          }
          editor.selection.restore();
        }

        var ranges = editor.selection.ranges();
        for (var i = 0; i < ranges.length; i++) {
          var container = document.createElement('div');
          container.appendChild(ranges[i].cloneContents());
          wrapSelection(container, selectionParent());

          // Fix for https://github.com/froala/wysiwyg-editor/issues/1010.
          if ($(container).find('.fr-element').length > 0) {
            container = editor.$el.get(0);
          }

          html += container.innerHTML;
        }
      }

      else if (typeof editor.doc.selection != 'undefined') {
        if (editor.doc.selection.type == 'Text') {
          html = editor.doc.selection.createRange().htmlText;
        }
      }
      return html;
    }

    function _hasBlockTags (html) {
      var $tmp = $('<div>').html(html);
      return $tmp.find(blockTagsQuery()).length > 0;
    }

    function _setCursorAtEnd (html) {
      var tmp = editor.doc.createElement('div');
      tmp.innerHTML = html;

      editor.selection.setAtEnd(tmp);

      return tmp.innerHTML;
    }

    function escapeEntities (str) {
      return str.replace(/</gi, '&lt;')
                .replace(/>/gi, '&gt;')
                .replace(/"/gi, '&quot;')
                .replace(/'/gi, '&apos;')
    }

    /**
     * Insert HTML.
     */
    function insert (dirty_html, clean, do_split) {
      // There is no selection.
      if (!editor.selection.isCollapsed()) {
        editor.selection.remove();
      }

      var clean_html;
      if (!clean) {
        clean_html = editor.clean.html(dirty_html);
      }
      else {
        clean_html = dirty_html;
      }

      clean_html = clean_html.replace(/\r|\n/g, ' ');

      if (dirty_html.indexOf('class="fr-marker"') < 0) {
        clean_html = _setCursorAtEnd(clean_html);
      }

      if (editor.core.isEmpty()) {
        editor.$el.html(clean_html);
      }
      else {
        // Insert a marker.
        var marker = editor.markers.insert();

        if (!marker) return false;

        // Check if HTML contains block tags and if so then break the current HTML.
        var deep_parent;
        if ((_hasBlockTags(clean_html) || do_split) && (deep_parent = editor.node.deepestParent(marker))) {
          var marker = editor.markers.split();
          if (!marker) return false;
          $(marker).replaceWith(clean_html);
        }
        else {
          $(marker).replaceWith(clean_html);
        }
      }

      _normalize();

      editor.events.trigger('html.inserted');
    }

    /**
     * Clean those tags that have an invisible space inside.
     */
    function cleanWhiteTags (ignore_selection) {
      var current_el = null;
      if (typeof ignore_selection == 'undefined') {
        current_el = editor.selection.element();
      }

      var possible_elements;
      var removed;
      do {
        removed = false;
        possible_elements = editor.$el.get(0).querySelectorAll('*:not(.fr-marker)');
        for (var i = 0; i < possible_elements.length; i++) {
          var el = possible_elements[i];
          if (current_el == el) continue;

          var text = el.textContent;
          if (el.children.length === 0 && text.length === 1 && text.charCodeAt(0) == 8203) {
            $(el).remove();
            removed = true;
          }
        }
      } while (removed);
    }

    /**
     * Initialization.
     */
    function _init () {
      var cleanTags = function () {
        cleanWhiteTags();

        if (editor.placeholder) editor.placeholder.refresh();
      }

      editor.events.on('mouseup', cleanTags);
      editor.events.on('keydown', cleanTags);
      editor.events.on('contentChanged', checkIfEmpty);
    }

    return {
      defaultTag: defaultTag,
      emptyBlocks: emptyBlocks,
      emptyBlockTagsQuery: emptyBlockTagsQuery,
      blockTagsQuery: blockTagsQuery,
      fillEmptyBlocks: fillEmptyBlocks,
      cleanEmptyTags: cleanEmptyTags,
      cleanWhiteTags: cleanWhiteTags,
      normalizeSpaces: normalizeSpaces,
      doNormalize: doNormalize,
      cleanBlankSpaces: cleanBlankSpaces,
      blocks: blocks,
      getDoctype: getDoctype,
      set: set,
      get: get,
      getSelected: getSelected,
      insert: insert,
      wrap: _wrap,
      unwrap: unwrap,
      escapeEntities: escapeEntities,
      checkIfEmpty: checkIfEmpty,
      extractNode: extractNode,
      extractNodeAttrs: extractNodeAttrs,
      extractDoctype: extractDoctype,
      _init: _init
    }
  }


  // Extend defaults.
  $.extend($.FE.DEFAULTS, {
    height: null,
    heightMax: null,
    heightMin: null,
    width: null
  });

  $.FE.MODULES.size = function (editor) {
    function syncIframe () {
      if (editor.opts.height) {
        editor.$el.css('minHeight', editor.opts.height - editor.helpers.getPX(editor.$el.css('padding-top')) - editor.helpers.getPX(editor.$el.css('padding-bottom')));
      }

      editor.$iframe.height(editor.$el.outerHeight(true));
    }

    function refresh () {
      if (editor.opts.heightMin) {
        editor.$el.css('minHeight', editor.opts.heightMin);
      }
      else {
        editor.$el.css('minHeight', '');
      }

      if (editor.opts.heightMax) {
        editor.$wp.css('maxHeight', editor.opts.heightMax);
        editor.$wp.css('overflow', 'auto');
      }
      else {
        editor.$wp.css('maxHeight', '');
        editor.$wp.css('overflow', '');
      }

      // Set height.
      if (editor.opts.height) {
        editor.$wp.height(editor.opts.height);
        editor.$el.css('minHeight', editor.opts.height - editor.helpers.getPX(editor.$el.css('padding-top')) - editor.helpers.getPX(editor.$el.css('padding-bottom')));
        editor.$wp.css('overflow', 'auto');
      }
      else {
        editor.$wp.css('height', '');
        if (!editor.opts.heightMin) editor.$el.css('minHeight', '');
        if (!editor.opts.heightMax) editor.$wp.css('overflow', '');
      }

      if (editor.opts.width) editor.$box.width(editor.opts.width);
    }

    function _init () {
      if (!editor.$wp) return false;

      refresh();

      // Sync iframe height.
      if (editor.opts.iframe) {
        editor.events.on('keyup', syncIframe);
        editor.events.on('commands.after', syncIframe);
        editor.events.on('html.set', syncIframe);
        editor.events.on('init', syncIframe);
        editor.events.on('initialized', syncIframe);
      }
    }

    return {
      _init: _init,
      syncIframe: syncIframe,
      refresh: refresh
    }
  };


  // Extend defaults.
  $.extend($.FE.DEFAULTS, {
    language: null
  });

  $.FE.LANGUAGE = {};

  $.FE.MODULES.language = function (editor) {
    var lang;

    /**
     * Translate.
     */
    function translate (str) {
      if (lang && lang.translation[str]) {
        return lang.translation[str];
      }
      else {
        return str;
      }
    }

    /* Initialize */
    function _init () {
      // Load lang.
      if ($.FE.LANGUAGE) {
        lang = $.FE.LANGUAGE[editor.opts.language];
      }

      // Set direction.
      if (lang && lang.direction) {
        editor.opts.direction = lang.direction;
      }
    }

    return {
      _init: _init,
      translate: translate
    }
  };



  // Extend defaults.
  $.extend($.FE.DEFAULTS, {
    placeholderText: 'Type something'
  });

  $.FE.MODULES.placeholder = function (editor) {
    /* Show placeholder. */
    function show () {
      if (!editor.$placeholder) _add();

      // Determine the placeholder position based on the first element inside editor.
      var margin_top = 0;
      var contents = editor.node.contents(editor.$el.get(0));
      if (contents.length && contents[0].nodeType == Node.ELEMENT_NODE) {
        margin_top = editor.helpers.getPX($(contents[0]).css('margin-top'));
        editor.$placeholder.css('font-size', $(contents[0]).css('font-size'));
        editor.$placeholder.css('line-height', $(contents[0]).css('line-height'));
      }
      else {
        editor.$placeholder.css('font-size', editor.$el.css('font-size'));
        editor.$placeholder.css('line-height', editor.$el.css('line-height'));
      }

      editor.$wp.addClass('show-placeholder');
      editor.$placeholder
        .css('margin-top', margin_top)
        .text(editor.language.translate(editor.opts.placeholderText || editor.$oel.attr('placeholder') || ''));
    }

    /* Hide placeholder. */
    function hide () {
      editor.$wp.removeClass('show-placeholder');
    }

    /* Check if placeholder is visible */
    function isVisible () {
      return !editor.$wp ? true : editor.$wp.hasClass('show-placeholder');
    }

    /* Refresh placeholder. */
    function refresh () {
      if (!editor.$wp) return false;

      if (editor.core.isEmpty()) {
        show();
      }
      else {
        hide();
      }
    }

    function _add () {
      editor.$placeholder = $('<span class="fr-placeholder"></span>');
      editor.$wp.append(editor.$placeholder);
    }

    /* Initialize. */
    function _init () {
      if (!editor.$wp) return false;

      editor.events.on('init input keydown keyup contentChanged', refresh);
    }

    return {
      _init: _init,
      show: show,
      hide: hide,
      refresh: refresh,
      isVisible: isVisible
    }
  };


  $.FE.MODULES.edit = function (editor) {
    /**
     * Disable editing design.
     */
    function disableDesign () {
      if (editor.browser.mozilla) {
        try {
          editor.doc.execCommand('enableObjectResizing', false, 'false');
          editor.doc.execCommand('enableInlineTableEditing', false, 'false');
        }
        catch (ex) {

        }
      }
    }

    var disabled = false;

    /**
     * Add contneteditable attribute.
     */
    function on () {
      if (editor.$wp) {
        editor.$el.attr('contenteditable', true);
        editor.$el.removeClass('fr-disabled');
        if (editor.$tb) editor.$tb.removeClass('fr-disabled');
        disableDesign();
      }

      disabled = false;
    }

    /**
     * Remove contenteditable attribute.
     */
    function off () {
      if (editor.$wp) {
        editor.$el.attr('contenteditable', false);
        editor.$el.addClass('fr-disabled');
        editor.$tb.addClass('fr-disabled');
      }

      disabled = true;
    }

    function isDisabled () {
      return disabled;
    }

    return {
      on: on,
      off: off,
      disableDesign: disableDesign,
      isDisabled: isDisabled
    }
  };



  // Extend defaults.
  $.extend($.FE.DEFAULTS, {
    editorClass: null,
    typingTimer: 500,
    iframe: false,
    requestWithCORS: true,
    requestHeaders: {},
    useClasses: true,
    spellcheck: true,
    iframeStyle: 'html{margin: 0px;}body{padding:10px;background:transparent;color:#000000;position:relative;z-index: 2;-webkit-user-select:auto;margin:0px;overflow:hidden;}body:after{content:"";clear:both;display:block}',
    direction: 'auto',
    zIndex: 1,
    disableRightClick: false,
    scrollableContainer: 'body',
    keepFormatOnDelete: false,
    theme: null
  })

  $.FE.MODULES.core = function(editor) {
    function injectStyle(style) {
      if (editor.opts.iframe) {
        editor.$head.append('<style data-fr-style="true">' + style + '</style>');
      }
    }

    function _initElementStyle() {
      if (!editor.opts.iframe) {
        editor.$el.addClass('fr-element fr-view');
      }
    }

    /**
     * Init the editor style.
     */

    function _initStyle() {
      editor.$box.addClass('fr-box' + (editor.opts.editorClass ? ' ' + editor.opts.editorClass : ''));
      editor.$wp.addClass('fr-wrapper');

      _initElementStyle();

      if (editor.opts.iframe) {
        editor.$iframe.addClass('fr-iframe');

        for (var i = 0; i < editor.o_doc.styleSheets.length; i++) {
          var rules;
          try {
            rules = editor.o_doc.styleSheets[i].cssRules;
          }
          catch (ex) {

          }

          if (rules) {
            for (var idx = 0, len = rules.length; idx < len; idx++) {
              if (rules[idx].selectorText && (rules[idx].selectorText.indexOf('.fr-view') === 0 || rules[idx].selectorText.indexOf('.fr-element') === 0)) {
                if (rules[idx].style.cssText.length > 0) {
                  if (rules[idx].selectorText.indexOf('.fr-view') === 0) {
                    editor.opts.iframeStyle += rules[idx].selectorText.replace(/\.fr-view/g, 'body') + '{' + rules[idx].style.cssText + '}';
                  }
                  else {
                    editor.opts.iframeStyle += rules[idx].selectorText.replace(/\.fr-element/g, 'body') + '{' + rules[idx].style.cssText + '}';
                  }
                }
              }
            }
          }
        }
      }

      if (editor.opts.direction != 'auto') {
        editor.$box.removeClass('fr-ltr fr-rtl').addClass('fr-' + editor.opts.direction);
      }
      editor.$el.attr('dir', editor.opts.direction);
      editor.$wp.attr('dir', editor.opts.direction);

      if (editor.opts.zIndex > 1) {
        editor.$box.css('z-index', editor.opts.zIndex);
      }

      if (editor.opts.theme) {
        editor.$box.addClass(editor.opts.theme + '-theme');
      }
    }

    /**
     * Determine if the editor is empty.
     */

    function isEmpty() {
      return editor.node.isEmpty(editor.$el.get(0));
    }

    /**
     * Check if the browser allows drag and init it.
     */

    function _initDrag() {
      // Drag and drop support.
      editor.drag_support = {
        filereader: typeof FileReader != 'undefined',
        formdata: !! editor.win.FormData,
        progress: 'upload' in new XMLHttpRequest()
      };
    }

    /**
     * Return an XHR object.
     */

    function getXHR(url, method) {
      var xhr = new XMLHttpRequest();

      // Make it async.
      xhr.open(method, url, true);

      // Set with credentials.
      if (editor.opts.requestWithCORS) {
        xhr.withCredentials = true;
      }

      // Set headers.
      for (var header in editor.opts.requestHeaders) {
        xhr.setRequestHeader(header, editor.opts.requestHeaders[header]);
      }

      return xhr;
    }

    function _destroy() {
      if (editor.$oel.get(0).tagName == 'TEXTAREA') {
        editor.$oel.val(editor.html.get());
      }

      if (editor.$wp) {
        if (editor.$oel.get(0).tagName == 'TEXTAREA') {
          editor.$box.replaceWith(editor.$oel);
          editor.$oel.show();
        } else {
          editor.$wp.replaceWith(editor.html.get());
          editor.$box.removeClass('fr-view fr-ltr fr-box ' + (editor.opts.editorClass || ''));

          if (editor.opts.theme) {
            editor.$box.addClass(editor.opts.theme + '-theme');
          }
        }
      }
    }

    function hasFocus() {
      return editor.node.hasFocus(editor.$el.get(0)) || editor.$el.find('*:focus').length > 0;
    }

    function sameInstance ($obj) {
      if (!$obj) return false;

      var inst = $obj.data('instance');

      return (inst ? inst.id == editor.id : false);
    }

    /**
     * Tear up.
     */

    function _init () {
      $.FE.INSTANCES.push(editor);

      _initDrag();

      // Call initialization methods.
      if (editor.$wp) {
        _initStyle();
        editor.html.set(editor._original_html);

        // Set spellcheck.
        editor.$el.attr('spellcheck', editor.opts.spellcheck);

        // Disable autocomplete.
        if (editor.helpers.isMobile()) {
          editor.$el.attr('autocomplete', editor.opts.spellcheck ? 'on' : 'off');
          editor.$el.attr('autocorrect', editor.opts.spellcheck ? 'on' : 'off');
          editor.$el.attr('autocapitalize', editor.opts.spellcheck ? 'on' : 'off');
        }

        // Disable right click.
        if (editor.opts.disableRightClick) {
          editor.evens.$on(editor.$el, 'contextmenu', function(e) {
            if (e.button == 2) {
              return false;
            }
          });
        }

        try {
          editor.doc.execCommand('styleWithCSS', false, false);
        } catch (ex) {

        }
      }

      // Do not allow drop inside the editor.
      editor.events.on('drop', function (e) {
        e.preventDefault();
        e.stopPropagation();
      });

      // Options.
      editor.events.trigger('init');
      editor.events.on('destroy', _destroy);

      if (editor.$oel.get(0).tagName == 'TEXTAREA') {
        // Sync on contentChanged.
        editor.events.on('contentChanged', function() {
          editor.$oel.val(editor.html.get());
        });

        // Set HTML on form submit.
        editor.events.on('form.submit', function() {
          editor.$oel.val(editor.html.get());
        });

        editor.events.on('form.reset', function () {
          editor.html.set(editor._original_html);
        })

        editor.$oel.val(editor.html.get());
      }
    }

    return {
      _init: _init,
      isEmpty: isEmpty,
      getXHR: getXHR,
      injectStyle: injectStyle,
      hasFocus: hasFocus,
      sameInstance: sameInstance
    }
  }



  $.FE.COMMANDS = {
    bold: {
      title: 'Bold'
    },
    italic: {
      title: 'Italic'
    },
    underline: {
      title: 'Underline'
    },
    strikeThrough: {
      title: 'Strikethrough'
    },
    subscript: {
      title: 'Subscript'
    },
    superscript: {
      title: 'Superscript'
    },
    outdent: {
      title: 'Decrease Indent'
    },
    indent: {
      title: 'Increase Indent'
    },
    undo: {
      title: 'Undo',
      undo: false,
      forcedRefresh: true,
      disabled: true
    },
    redo: {
      title: 'Redo',
      undo: false,
      forcedRefresh: true,
      disabled: true
    },
    insertHR: {
      title: 'Insert Horizontal Line'
    },
    clearFormatting: {
      title: 'Clear Formatting'
    },
    selectAll: {
      title: 'Select All',
      undo: false
    }
  };

  $.FE.RegisterCommand = function (name, info) {
    $.FE.COMMANDS[name] = info;
  }

  $.FE.MODULES.commands = function (editor) {
    var mapping = {
      bold: function () {
        _execCommand('bold', 'strong');
      },

      subscript: function () {
        _execCommand('subscript', 'sub');
      },

      superscript: function () {
        _execCommand('superscript', 'sup');
      },

      italic: function () {
        _execCommand('italic', 'em');
      },

      strikeThrough: function () {
        _execCommand('strikeThrough', 's');
      },

      underline: function () {
        _execCommand('underline', 'u');
      },

      undo: function () {
        editor.undo.run();
      },

      redo: function () {
        editor.undo.redo();
      },

      indent: function () {
        _processIndent(1);
      },

      outdent: function () {
        _processIndent(-1);
      },

      show: function () {
        if (editor.opts.toolbarInline) {
          editor.toolbar.showInline(null, true);
        }
      },

      insertHR: function () {
        editor.selection.remove();

        editor.html.insert('<hr id="fr-just">');

        var $hr = editor.$el.find('hr#fr-just');
        $hr.removeAttr('id');

        editor.selection.setAfter($hr.get(0)) || editor.selection.setBefore($hr.get(0));

        editor.selection.restore();
      },

      clearFormatting: function () {
        if (editor.browser.msie || editor.browser.edge) {
          var clean = function (prop) {
            editor.commands.applyProperty(prop, '#123456');
            editor.selection.save();
            editor.$el.find('span:not(.fr-marker)').each(function (index, span) {
              var $span = $(span);
              var color = $span.css(prop);

              if (color === '#123456' || editor.helpers.RGBToHex(color) === '#123456') {
                $span.replaceWith($span.html());
              }
            });
            editor.selection.restore();
          }


          clean('color');
          clean('background-color');
        }

        editor.doc.execCommand('removeFormat', false, false);
        editor.doc.execCommand('unlink', false, false);
      },

      selectAll: function () {
        editor.doc.execCommand('selectAll', false, false);
      }
    }

    /**
     * Exec command.
     */
    function exec (cmd, params) {
      // Trigger before command to see if to execute the default callback.
      if (editor.events.trigger('commands.before', $.merge([cmd], params || [])) !== false) {
        // Get the callback.
        var callback = ($.FE.COMMANDS[cmd] && $.FE.COMMANDS[cmd].callback) || mapping[cmd];

        var focus = true;
        if ($.FE.COMMANDS[cmd] && typeof $.FE.COMMANDS[cmd].focus != 'undefined') {
          focus = $.FE.COMMANDS[cmd].focus;
        }

        // Make sure we have focus.
        if (!editor.core.hasFocus() && focus && !editor.popups.areVisible()) {
          // Focus in the editor at any position.
          editor.events.focus(true);
        }

        // Callback.
        // Save undo step.
        if ($.FE.COMMANDS[cmd] && $.FE.COMMANDS[cmd].undo !== false) {
          editor.undo.saveStep();
        }

        if (callback) {
          callback.apply(editor, $.merge([cmd], params || []));
        }

        // Trigger after command.
        editor.events.trigger('commands.after', $.merge([cmd], params || []));

        // Save undo step again.
        if ($.FE.COMMANDS[cmd] && $.FE.COMMANDS[cmd].undo !== false) editor.undo.saveStep();
      }
    }

    /**
     * Exex default.
     */
    function _execCommand(cmd, tag) {
      // Selection is collapsed and state is not active.
      if (editor.selection.isCollapsed() && editor.doc.queryCommandState(cmd) === false) {
        editor.markers.insert();
        var $marker = editor.$el.find('.fr-marker');
        $marker.replaceWith('<' + tag + '>' + $.FE.INVISIBLE_SPACE + $.FE.MARKERS + '</' + tag + '>')
        editor.selection.restore();
      }
      else {
        var el = editor.selection.element();

        // Selection is collapsed.
        // State is active.
        // Current tag is empty.
        if (editor.selection.isCollapsed() && editor.doc.queryCommandState(cmd) === true && el.tagName == tag.toUpperCase() && (el.textContent || '').replace(/\u200B/g, '').length === 0) {
          $(el).replaceWith($.FE.MARKERS);
          editor.selection.restore();
        }
        else {
          var spans = editor.$el.find('span');

          var selection_saved = false;
          if (editor.doc.queryCommandState(cmd) === false && !editor.browser.chrome) {
            editor.selection.save();
            selection_saved = true;
          }
          editor.doc.execCommand(cmd, false, false);

          if (selection_saved) editor.selection.restore();

          var new_spans = editor.$el.find('span[style]').not(spans).filter(function () {
            return $(this).attr('style').indexOf('font-weight: normal') >= 0;
          });

          if (new_spans.length) {
            editor.selection.save();
            new_spans.each(function () {
              $(this).replaceWith($(this).html());
            });
            editor.selection.restore();
          }

          editor.clean.toHTML5();
        }
      }
    }

    function _processIndent(indent) {
      editor.selection.save();
      editor.html.wrap(true, true);
      editor.selection.restore();

      var blocks = editor.selection.blocks();

      for (var i = 0; i < blocks.length; i++) {
        if (blocks[i].tagName != 'LI' && blocks[i].parentNode.tagName != 'LI') {
          var $block = $(blocks[i]);

          var prop = (editor.opts.direction == 'rtl' || $block.css('direction') == 'rtl') ? 'margin-right' : 'margin-left';

          var margin_left = editor.helpers.getPX($block.css(prop));

          $block.css(prop, Math.max(margin_left + indent * 20, 0) || '');
          $block.removeClass('fr-temp-div');
        }
      }

      editor.selection.save();
      editor.html.unwrap();
      editor.selection.restore();
    }

    /**
     * Preserve font size.
     */
    function _saveFontSize() {
      // Check if the el has font size.
      var hasFontSizeSet = function ($el) {
        return $el.attr('style').indexOf('font-size') >= 0;
      }

      editor.$el.find('[style]').each (function () {
        var $el = $(this);

        if (hasFontSizeSet($el)) {
          $el.attr('data-font-size', $el.css('font-size'));
          $el.css('font-size', '');
        }
      })
    }

    /**
     * Restore font size.
     */
    function _restoreFontSize() {
      editor.$el.find('[data-font-size]').each (function () {
        var $el = $(this);

        $el.css('font-size', $el.attr('data-font-size'));
        $el.removeAttr('data-font-size');
      })
    }

    function _clearBlankSpans () {
      // Remove spans with no style.
      editor.$el.find('span').each(function () {
        if (editor.node.attributes(this) === '') {
          $(this).replaceWith($(this).html());
        }
      })
    }

    function applyProperty (prop, val) {
      if (editor.selection.isCollapsed()) {
        editor.markers.insert();
        var $marker = editor.$el.find('.fr-marker');
        $marker.replaceWith('<span style="' + prop + ': ' + val + ';">' + $.FE.INVISIBLE_SPACE + $.FE.MARKERS + '</span>')
        editor.selection.restore();
      }
      else {
        _saveFontSize();

        // Apply format.
        editor.doc.execCommand('fontSize', false, 4);

        editor.selection.save();

        _restoreFontSize();

        // Clean font.
        var clean_format = function (elem) {
          var $elem = $(elem);
          $elem.css(prop, '');

          if ($elem.attr('style') === '') {
            $elem.replaceWith($elem.html());
          }
        }

        var filter_spans = function () {
          return $(this).attr('style').indexOf(prop + ':') === 0 || $(this).attr('style').indexOf(';' + prop + ':') >= 0 || $(this).attr('style').indexOf('; ' + prop + ':') >= 0;
        };

        var i;

        // Replace font with spans.
        while (editor.$el.find('font').length > 0) {
          var $font = editor.$el.find('font:first');
          var $span = $('<span class="fr-just" style="' + prop + ': ' + val + ';">' + $font.html() + '</span>');
          $font.replaceWith($span);

          // Replace in reverse order to take care of the inner spans first.
          var inner_spans = $span.find('span');
          for (i = inner_spans.length - 1; i >= 0; i--) {
            clean_format(inner_spans[i]);
          }

          // Look at parents with the same property.
          var $outer_span = $span.parentsUntil(editor.$el, 'span[style]').filter(filter_spans);
          if ($outer_span.length) {
            var c_str = '';
            var o_str = '';
            var ic_str = '';
            var io_str = '';
            var c_node = $span.get(0);
            do {
              c_node = c_node.parentNode;
              c_str = c_str + editor.node.closeTagString(c_node);
              o_str = editor.node.openTagString(c_node) + o_str;

              // Inner close and open.
              if ($outer_span.get(0) != c_node) {
                ic_str = ic_str + editor.node.closeTagString(c_node);
                io_str = editor.node.openTagString(c_node) + io_str;
              }
            } while ($outer_span.get(0) != c_node);

            // Build breaking string.
            var str = c_str + '<span class="fr-just" style="' + prop + ': ' + val + ';">' + io_str + $span.html() + ic_str + '</span>' + o_str;
            $span.replaceWith('<span id="fr-break"></span>');
            var html = $outer_span.get(0).outerHTML;

            // Replace the outer node.
            $outer_span.replaceWith(html.replace(/<span id="fr-break"><\/span>/g, str));
          }
        }

        editor.html.cleanEmptyTags();
        _clearBlankSpans();

        // Join current spans together if they are one next to each other.
        var just_spans = editor.$el.find('.fr-just + .fr-just');
        for (i = 0; i < just_spans.length; i++) {
          var $s = $(just_spans[i]);
          $s.prepend($s.prev().html());
          $s.prev().remove();
        }

        editor.$el.find('.fr-marker + .fr-just').each(function () {
          $(this).prepend($(this).prev());
        })

        editor.$el.find('.fr-just + .fr-marker').each(function () {
          $(this).append($(this).next());
        })

        editor.$el.find('.fr-just').removeAttr('class');

        editor.selection.restore();
      }
    }

    function callExec (k) {
      return function () {
        exec(k);
      }
    }

    var resp = {};
    for (var k in mapping) {
      resp[k] = callExec(k);
    }

    return $.extend(resp, {
      exec: exec,
      applyProperty: applyProperty
    });
  };


  $.FE.MODULES.cursorLists = function (editor) {
    /**
     * Find the first li parent.
     */
    function _firstParentLI (node) {
      var p_node = node;
      while (p_node.tagName != 'LI') {
        p_node = p_node.parentNode;
      }

      return p_node;
    }

    /**
     * Find the first list parent.
     */
    function _firstParentList (node) {
      var p_node = node;
      while (!editor.node.isList(p_node)) {
        p_node = p_node.parentNode;
      }

      return p_node;
    }


    /**
     * Do enter at the beginning of a list item.
     */
    function _startEnter (marker) {
      var li = _firstParentLI(marker);

      // Get previous and next siblings.
      var next_li = li.nextSibling;
      var prev_li = li.previousSibling;
      var default_tag = editor.html.defaultTag();

      var ul;

      // We are in a list item at the middle of the list or an list item that is not empty.
      if (editor.node.isEmpty(li, true) && next_li) {
        var o_str = '';
        var c_str = ''
        var p_node = marker.parentNode;

        // Create open / close string.
        while (!editor.node.isList(p_node) && p_node.parentNode && p_node.parentNode.tagName !== 'LI') {
          o_str = editor.node.openTagString(p_node) + o_str;
          c_str = c_str + editor.node.closeTagString(p_node);
          p_node = p_node.parentNode;
        }

        o_str = editor.node.openTagString(p_node) + o_str;
        c_str = c_str + editor.node.closeTagString(p_node);

        var str = ''
        if (p_node.parentNode && p_node.parentNode.tagName == 'LI') {
          str = c_str + '<li>' + $.FE.MARKERS + '<br>' + o_str;
        }
        else {
          if (default_tag) {
            str = c_str + '<' + default_tag + '>' + $.FE.MARKERS + '<br>' + '</' + default_tag + '>' + o_str;
          }
          else {
            str = c_str + $.FE.MARKERS + '<br>' + o_str;
          }
        }

        $(li).html('<span id="fr-break"></span>');

        while (['UL', 'OL'].indexOf(p_node.tagName) < 0 || (p_node.parentNode && p_node.parentNode.tagName === 'LI')) {
          p_node = p_node.parentNode;
        }
        var html = editor.node.openTagString(p_node) + $(p_node).html() + editor.node.closeTagString(p_node);
        html = html.replace(/<span id="fr-break"><\/span>/g, str);

        $(p_node).replaceWith(html);

        editor.$el.find('li:empty').remove();
      }
      else if ((prev_li && next_li) || !editor.node.isEmpty(li, true)) {
        $(li).before('<li><br></li>');
        $(marker).remove();
      }

      // There is no previous list item so transform the current list item to an empty line.
      else if (!prev_li) {
        ul = _firstParentList(li);

        // We are in a nested list so add a new li before it.
        if (ul.parentNode && ul.parentNode.tagName == 'LI') {
          $(ul.parentNode).before('<li>' + $.FE.MARKERS + '<br></li>');
        }

        // We are in a normal list. Add a new line before.
        else {
          if (default_tag) {
            $(ul).before('<' + default_tag + '>' + $.FE.MARKERS + '<br></' + default_tag + '>');
          }
          else {
            $(ul).before($.FE.MARKERS  + '<br>');
          }
        }

        // Remove the current li.
        $(li).remove();
      }

      // There is no next_li item so transform the current list item to an empty line.
      else {
        ul = _firstParentList(li);

        // We are in a nested lists so add a new li after it.
        if (ul.parentNode && ul.parentNode.tagName == 'LI') {
          $(ul.parentNode).after('<li>' + $.FE.MARKERS + '<br></li>');
        }

        // We are in a normal list. Add a new line after.
        else {
          if (default_tag) {
            $(ul).after('<' + default_tag + '>' + $.FE.MARKERS + '<br></' + default_tag + '>');
          }
          else {
            $(ul).after($.FE.MARKERS  + '<br>');
          }
        }

        // Remove the current li.
        $(li).remove();
      }
    }

    /**
     * Enter at the middle of a list.
     */
    function _middleEnter (marker) {
      var li = _firstParentLI(marker);

      // Build the closing / opening list item string.
      var str = '';
      var node = marker;
      var o_str = '';
      var c_str = '';
      while (node != li) {
        node = node.parentNode;

        var cls = (node.tagName == 'A' && editor.cursor.isAtEnd(marker, node)) ? 'fr-to-remove' : '';

        o_str = editor.node.openTagString($(node).clone().addClass(cls).get(0)) + o_str;
        c_str = editor.node.closeTagString(node) + c_str;
      }

      // Add markers.
      str = c_str + str + o_str + $.FE.MARKERS;

      // Build HTML.
      $(marker).replaceWith('<span id="fr-break"></span>');
      var html = editor.node.openTagString(li) + $(li).html() + editor.node.closeTagString(li);
      html = html.replace(/<span id="fr-break"><\/span>/g, str);

      // Replace the current list item.
      $(li).replaceWith(html);
    }

    /**
     * Enter at the end of a list item.
     */
    function _endEnter (marker) {
      var li = _firstParentLI(marker);

      var str = $.FE.MARKERS;
      var node = marker;
      while (node != li) {
        node = node.parentNode;

        var cls = (node.tagName == 'A' && editor.cursor.isAtEnd(marker, node)) ? 'fr-to-remove' : '';

        str = editor.node.openTagString($(node).clone().addClass(cls).get(0)) + str + editor.node.closeTagString(node);
      }

      $(marker).remove();
      $(li).after(str);
    }

    /**
     * Do backspace on a list item. This method is called only when wer are at the beginning of a LI.
     */
    function _backspace (marker) {
      var li = _firstParentLI(marker);

      // Get previous sibling.
      var prev_li = li.previousSibling;

      // There is a previous li.
      if (prev_li) {
        // Get the li inside a nested list or inner block tags.
        prev_li = $(prev_li).find(editor.html.blockTagsQuery()).get(-1) || prev_li;

        // Add markers.
        $(marker).replaceWith($.FE.MARKERS);

        // Remove possible BR at the end of the previous list.
        var contents = editor.node.contents(prev_li);
        if (contents.length && contents[contents.length - 1].tagName == 'BR') {
          $(contents[contents.length - 1]).remove();
        }

        // Remove any nodes that might be wrapped.
        $(li).find(editor.html.blockTagsQuery()).not('ol, ul, table').each (function () {
          if (this.parentNode == li) {
            $(this).replaceWith($(this).html() + (editor.node.isEmpty(this) ? '' : '<br>'));
          }
        })

        // Append the current list item content to the previous one.
        var node = editor.node.contents(li)[0];
        var tmp;
        while (node && !editor.node.isList(node)) {
          tmp = node.nextSibling;
          $(prev_li).append(node);
          node = tmp;
        }

        prev_li = li.previousSibling;
        while (node) {
          tmp = node.nextSibling;
          $(prev_li).append(node);
          node = tmp;
        }

        // Remove the current LI.
        $(li).remove();
      }

      // No previous li.
      else {
        var ul = _firstParentList(li);

        // Add markers.
        $(marker).replaceWith($.FE.MARKERS);

        // Nested lists.
        if (ul.parentNode && ul.parentNode.tagName == 'LI') {
          var prev_node = ul.previousSibling;

          // Previous node is block.
          if (editor.node.isBlock(prev_node)) {
            // Remove any nodes that might be wrapped.
            $(li).find(editor.html.blockTagsQuery()).not('ol, ul, table').each (function () {
              if (this.parentNode == li) {
                $(this).replaceWith($(this).html() + (editor.node.isEmpty(this) ? '' : '<br>'));
              }
            });

            $(prev_node).append($(li).html());
          }

          // Text right in li.
          else {
            $(ul).before($(li).html());
          }
        }

        // Normal lists. Add an empty li instead.
        else {
          var default_tag = editor.html.defaultTag();
          if (default_tag && $(li).find(editor.html.blockTagsQuery()).length === 0) {
            $(ul).before('<' + default_tag + '>' + $(li).html() + '</' + default_tag + '>');
          }
          else {
            $(ul).before($(li).html());
          }
        }

        // Remove the current li.
        $(li).remove();

        // Remove the ul if it is empty.
        if ($(ul).find('li').length === 0) $(ul).remove();
      }
    }

    /**
     * Delete at the end of list item.
     */
    function _del (marker) {
      var li = _firstParentLI(marker);
      var next_li = li.nextSibling;
      var contents;

      // There is a next li.
      if (next_li) {
        // Remove possible BR at the beginning of the next LI.
        contents = editor.node.contents(next_li);
        if (contents.length && contents[0].tagName == 'BR') {
          $(contents[0]).remove();
        }

        // Unwrap content from the next node.
        $(next_li).find(editor.html.blockTagsQuery()).not('ol, ul, table').each (function () {
          if (this.parentNode == next_li) {
            $(this).replaceWith($(this).html() + (editor.node.isEmpty(this) ? '' : '<br>'));
          }
        });

        // Append the next LI to the current LI.
        var last_node = marker;
        var node = editor.node.contents(next_li)[0];
        var tmp;
        while (node && !editor.node.isList(node)) {
          tmp = node.nextSibling;
          $(last_node).after(node);
          last_node = node;
          node = tmp;
        }

        // Append nested lists.
        while (node) {
          tmp = node.nextSibling;
          $(li).append(node);
          node = tmp;
        }

        // Replace marker with markers.
        $(marker).replaceWith($.FE.MARKERS);

        // Remove next li.
        $(next_li).remove();
      }

      // No next li.
      else {
        // Search the next sibling in parents.
        var next_node = li;
        while (!next_node.nextSibling && next_node != editor.$el.get(0)) {
          next_node = next_node.parentNode;
        }

        // We're right at the end.
        if (next_node == editor.$el.get(0)) return false;

        // Get the next sibling.
        next_node = next_node.nextSibling;

        // Next sibling is a block tag.
        if (editor.node.isBlock(next_node)) {
          // Check if we can do delete in it.
          if ($.FE.NO_DELETE_TAGS.indexOf(next_node.tagName) < 0) {

            // Add markers.
            $(marker).replaceWith($.FE.MARKERS);

            // Remove any possible BR at the end of the LI.
            contents = editor.node.contents(li);
            if (contents.length && contents[contents.length - 1].tagName == 'BR') {
              $(contents[contents.length - 1]).remove();
            }

            // Append next node.
            $(li).append($(next_node).html());

            // Remove the next node.
            $(next_node).remove();
          }
        }

        // Append everything till the next block tag or BR.
        else {
          // Remove any possible BR at the end of the LI.
          contents = editor.node.contents(li);
          if (contents.length && contents[contents.length - 1].tagName == 'BR') {
            $(contents[contents.length - 1]).remove();
          }

          // var next_node = next_li;
          $(marker).replaceWith($.FE.MARKERS);
          while (next_node && !editor.node.isBlock(next_node) && next_node.tagName != 'BR') {
            $(li).append($(next_node));
            next_node = next_node.nextSibling;
          }
        }
      }
    }

    return {
      _startEnter: _startEnter,
      _middleEnter: _middleEnter,
      _endEnter: _endEnter,
      _backspace: _backspace,
      _del: _del
    }
  };


  // Do not merge with the previous one.
  $.FE.NO_DELETE_TAGS = ['TH', 'TD', 'TABLE', 'FORM'];

  // Do simple enter.
  $.FE.SIMPLE_ENTER_TAGS = ['TH', 'TD', 'LI', 'DL', 'DT', 'FORM'];

  $.FE.MODULES.cursor = function (editor) {
    /**
     * Check if node is at the end of a block tag.
     */
    function _atEnd (node) {
      if (editor.node.isBlock(node)) return true;
      if (node.nextSibling) return false;

      return _atEnd(node.parentNode);
    }

    /**
     * Check if node is at the start of a block tag.
     */
    function _atStart (node) {
      if (editor.node.isBlock(node)) return true;
      if (node.previousSibling) return false;

      return _atStart(node.parentNode);
    }

    /**
     * Check if node is a the start of the container.
     */
    function _isAtStart (node, container) {
      if (!node) return false;
      if (node == editor.$wp.get(0)) return false;
      if (node.previousSibling) return false;
      if (node.parentNode == container) return true;

      return _isAtStart(node.parentNode, container);
    }

    /**
     * Check if node is a the start of the container.
     */
    function _isAtEnd (node, container) {
      if (!node) return false;
      if (node == editor.$wp.get(0)) return false;
      if (node.nextSibling) return false;
      if (node.parentNode == container) return true;

      return _isAtEnd(node.parentNode, container);
    }

    /**
     * Check if the node is inside a LI.
     */
    function _inLi (node) {
      return $(node).parentsUntil(editor.$el, 'LI').length > 0 && $(node).parentsUntil('LI', 'TABLE').length === 0;
    }

    /**
     * Do backspace at the start of a block tag.
     */
    function _startBackspace (marker) {
      var quote = $(marker).parentsUntil(editor.$el, 'BLOCKQUOTE').length > 0;
      var deep_parent = editor.node.deepestParent(marker, [], !quote);

      if (deep_parent && deep_parent.tagName == 'BLOCKQUOTE') {
        var m_parent = editor.node.deepestParent(marker, [$(marker).parentsUntil(editor.$el, 'BLOCKQUOTE').get(0)]);
        if (m_parent && m_parent.previousSibling) {
          deep_parent = m_parent;
        }
      }

      // Deepest parent is not the main element.
      if (deep_parent !== null) {
        var prev_node = deep_parent.previousSibling;
        var contents;

        // We are inside a block tag.
        if (editor.node.isBlock(deep_parent) && editor.node.isEditable(deep_parent)) {
          // There is a previous node.
          if (prev_node && $.FE.NO_DELETE_TAGS.indexOf(prev_node.tagName) < 0) {
            // Previous node is a block tag.
            if (editor.node.isEditable(prev_node)) {
              if (editor.node.isBlock(prev_node)) {
                if (editor.node.isEmpty(prev_node) && !editor.node.isList(prev_node)) {
                  $(prev_node).remove();
                }
                else {
                  if (editor.node.isList(prev_node)) {
                    prev_node = $(prev_node).find('li:last').get(0);
                  }

                  // Remove last BR.
                  contents = editor.node.contents(prev_node);
                  if (contents.length && contents[contents.length - 1].tagName == 'BR') {
                    $(contents[contents.length - 1]).remove();
                  }

                  // Prev node is blockquote but the current one isn't.
                  if (prev_node.tagName == 'BLOCKQUOTE' && deep_parent.tagName != 'BLOCKQUOTE') {
                    contents = editor.node.contents(prev_node);
                    while (contents.length && editor.node.isBlock(contents[contents.length - 1])) {
                      prev_node = contents[contents.length - 1];
                      contents = editor.node.contents(prev_node);
                    }
                  }
                  // Prev node is not blockquote, but the current one is.
                  else if (prev_node.tagName != 'BLOCKQUOTE' && deep_parent.tagName == 'BLOCKQUOTE') {
                    contents = editor.node.contents(deep_parent);
                    while (contents.length && editor.node.isBlock(contents[0])) {
                      deep_parent = contents[0];
                      contents = editor.node.contents(deep_parent);
                    }
                  }

                  $(marker).replaceWith($.FE.MARKERS);
                  $(prev_node).append(editor.node.isEmpty(deep_parent) ? $.FE.MARKERS : deep_parent.innerHTML);
                  $(deep_parent).remove();
                }
              }
              else {
                $(marker).replaceWith($.FE.MARKERS);

                if (deep_parent.tagName == 'BLOCKQUOTE' && prev_node.nodeType == Node.ELEMENT_NODE) {
                  $(prev_node).remove();
                }
                else {
                  $(prev_node).after(editor.node.isEmpty(deep_parent) ? '' : $(deep_parent).html());
                  $(deep_parent).remove();
                  if (prev_node.tagName == 'BR') $(prev_node).remove();
                }
              }
            }
          }
        }

        // No block tag.
        /* jshint ignore:start */
        /* jscs:disable */
        else {
          // This should never happen.
        }
        /* jshint ignore:end */
        /* jscs:enable */
      }
    }

    /**
     * Do backspace at the middle of a block tag.
     */
    function _middleBackspace (marker) {
      var prev_node = marker;

      // Get the parent node that has a prev sibling.
      while (!prev_node.previousSibling) {
        prev_node = prev_node.parentNode;
      }
      prev_node = prev_node.previousSibling;

      // Not block tag.
      var contents;
      if (!editor.node.isBlock(prev_node)) {
        contents = editor.node.contents(prev_node);

        // Previous node is text.
        while (prev_node.nodeType != Node.TEXT_NODE && contents.length && !$(prev_node).is('[contenteditable=\'false\']')) {
          prev_node = contents[contents.length - 1];
          contents = editor.node.contents(prev_node);
        }

        if (prev_node.nodeType == Node.TEXT_NODE) {
          if (editor.helpers.isIOS()) return true;

          $(prev_node).after($.FE.MARKERS);

          var txt = prev_node.textContent;
          var len = txt.length - 1;

          // Tab UNDO.
          if (editor.opts.tabSpaces && txt.length >= editor.opts.tabSpaces) {
            var tab_str = txt.substr(txt.length - editor.opts.tabSpaces, txt.length - 1);
            if (tab_str.replace(/ /g, '').replace(new RegExp($.FE.UNICODE_NBSP, 'g'), '').length == 0) {
              len = txt.length - editor.opts.tabSpaces;
            }
          }

          prev_node.textContent = txt.substring(0, len);
          if (prev_node.textContent.length && prev_node.textContent.charCodeAt(prev_node.textContent.length - 1) == 55357) {
            prev_node.textContent = prev_node.textContent.substr(0, prev_node.textContent.length - 1);
          }
        }
        else {
          if (editor.events.trigger('node.remove', [$(prev_node)]) !== false) {
            $(prev_node).after($.FE.MARKERS);
            $(prev_node).remove();
          }
        }
      }

      // Block tag but we are allowed to delete it.
      else if ($.FE.NO_DELETE_TAGS.indexOf(prev_node.tagName) < 0) {
        if (editor.node.isEmpty(prev_node) && !editor.node.isList(prev_node)) {
          $(prev_node).remove();
          $(marker).replaceWith($.FE.MARKERS);
        }
        else {
          // List correction.
          if (editor.node.isList(prev_node)) prev_node = $(prev_node).find('li:last').get(0);

          contents = editor.node.contents(prev_node);
          if (contents && contents[contents.length - 1].tagName == 'BR') {
            $(contents[contents.length - 1]).remove();
          }

          contents = editor.node.contents(prev_node);
          while (contents && editor.node.isBlock(contents[contents.length - 1])) {
            prev_node = contents[contents.length - 1];
            contents = editor.node.contents(prev_node);
          }

          $(prev_node).append($.FE.MARKERS);

          var next_node = marker;
          while (!next_node.previousSibling) {
            next_node = next_node.parentNode;
          }

          while (next_node && next_node.tagName !== 'BR' && !editor.node.isBlock(next_node)) {
            var copy_node = next_node;
            next_node = next_node.nextSibling;
            $(prev_node).append(copy_node);
          }

          // Remove BR.
          if (next_node && next_node.tagName == 'BR') $(next_node).remove();

          $(marker).remove();
        }
      }
    }

    /**
     * Do backspace.
     */
    function backspace () {
      var do_default = false;

      // Add a marker in HTML.
      var marker = editor.markers.insert();

      if (!marker) return true;

      editor.$el.get(0).normalize();

      // We should remove invisible space first of all.
      var prev_node = marker.previousSibling;
      if (prev_node) {
        var txt = prev_node.textContent;
        if (txt && txt.length && txt.charCodeAt(txt.length - 1) == 8203) {
          if (txt.length == 1) {
            $(prev_node).remove()
          }
          else {
            prev_node.textContent = prev_node.textContent.substr(0, txt.length - 1);
            if (prev_node.textContent.length && prev_node.textContent.charCodeAt(prev_node.textContent.length - 1) == 55357) {
              prev_node.textContent = prev_node.textContent.substr(0, prev_node.textContent.length - 1);
            }
          }
        }
      }

      // Delete at end.
      if (_atEnd(marker)) {
        do_default = _middleBackspace(marker);
      }

      // Delete at start.
      else if (_atStart(marker)) {
        if (_inLi(marker) && _isAtStart(marker, $(marker).parents('li:first').get(0))) {
          editor.cursorLists._backspace(marker);
        }
        else {
          _startBackspace(marker);
        }
      }

      // Delete at middle.
      else {
        do_default = _middleBackspace(marker);
      }

      $(marker).remove();

      editor.$el.find('blockquote:empty').remove();
      editor.html.fillEmptyBlocks();
      editor.html.cleanEmptyTags();
      editor.clean.quotes();
      editor.clean.lists();
      editor.html.normalizeSpaces();
      editor.selection.restore();

      return do_default;
    }

    /**
     * Delete at the end of a block tag.
     */
    function _endDel (marker) {
      var quote = $(marker).parentsUntil(editor.$el, 'BLOCKQUOTE').length > 0;
      var deep_parent = editor.node.deepestParent(marker, [], !quote);

      if (deep_parent && deep_parent.tagName == 'BLOCKQUOTE') {
        var m_parent = editor.node.deepestParent(marker, [$(marker).parentsUntil(editor.$el, 'BLOCKQUOTE').get(0)]);
        if (m_parent && m_parent.nextSibling) {
          deep_parent = m_parent;
        }
      }

      // Deepest parent is not the main element.
      if (deep_parent !== null) {
        var next_node = deep_parent.nextSibling;
        var contents;

        // We are inside a block tag.
        if (editor.node.isBlock(deep_parent) && editor.node.isEditable(deep_parent)) {
          // There is a next node.
          if (next_node && $.FE.NO_DELETE_TAGS.indexOf(next_node.tagName) < 0) {
            // Next node is a block tag.
            if (editor.node.isBlock(next_node) && editor.node.isEditable(next_node)) {
              // Next node is a list.
              if (editor.node.isList(next_node)) {
                // Current block tag is empty.
                if (editor.node.isEmpty(deep_parent, true)) {
                  $(deep_parent).remove();

                  $(next_node).find('li:first').prepend($.FE.MARKERS);
                }
                else {
                  var $li = $(next_node).find('li:first');

                  if (deep_parent.tagName == 'BLOCKQUOTE') {
                    contents = editor.node.contents(deep_parent);
                    if (contents.length && editor.node.isBlock(contents[contents.length - 1])) {
                      deep_parent = contents[contents.length - 1];
                    }
                  }

                  // There are no nested lists.
                  if ($li.find('ul, ol').length === 0) {
                    $(marker).replaceWith($.FE.MARKERS);

                    // Remove any nodes that might be wrapped.
                    $li.find(editor.html.blockTagsQuery()).not('ol, ul, table').each (function () {
                      if (this.parentNode == $li.get(0)) {
                        $(this).replaceWith($(this).html() + (editor.node.isEmpty(this) ? '' : '<br>'));
                      }
                    });

                    $(deep_parent).append(editor.node.contents($li.get(0)));
                    $li.remove();

                    if ($(next_node).find('li').length === 0) $(next_node).remove();
                  }
                }
              }
              else {
                // Remove last BR.
                contents = editor.node.contents(next_node);
                if (contents.length && contents[0].tagName == 'BR') {
                  $(contents[0]).remove();
                }

                if (next_node.tagName != 'BLOCKQUOTE' && deep_parent.tagName == 'BLOCKQUOTE') {
                  contents = editor.node.contents(deep_parent);
                  while (contents.length && editor.node.isBlock(contents[contents.length - 1])) {
                    deep_parent = contents[contents.length - 1];
                    contents = editor.node.contents(deep_parent);
                  }
                }
                else if (next_node.tagName == 'BLOCKQUOTE' && deep_parent.tagName != 'BLOCKQUOTE') {
                  contents = editor.node.contents(next_node);
                  while (contents.length && editor.node.isBlock(contents[0])) {
                    next_node = contents[0];
                    contents = editor.node.contents(next_node);
                  }
                }

                $(marker).replaceWith($.FE.MARKERS);
                $(deep_parent).append(next_node.innerHTML);
                $(next_node).remove();
              }
            }
            else {
              $(marker).replaceWith($.FE.MARKERS);

              // var next_node = next_node.nextSibling;
              while (next_node && next_node.tagName !== 'BR' && !editor.node.isBlock(next_node) && editor.node.isEditable(next_node)) {
                var copy_node = next_node;
                next_node = next_node.nextSibling;
                $(deep_parent).append(copy_node);
              }

              if (next_node && next_node.tagName == 'BR' && editor.node.isEditable(next_node)) {
                $(next_node).remove();
              }
            }
          }
        }

        // No block tag.
        /* jshint ignore:start */
        /* jscs:disable */
        else {
          // This should never happen.
        }
        /* jshint ignore:end */
        /* jscs:enable */
      }
    }

    /**
     * Delete at the middle of a block tag.
     */
    function _middleDel (marker) {
      var next_node = marker;

      // Get the parent node that has a next sibling.
      while (!next_node.nextSibling) {
        next_node = next_node.parentNode;
      }
      next_node = next_node.nextSibling;

      // Handle the case when the next node is a BR.
      if (next_node.tagName == 'BR' && editor.node.isEditable(next_node)) {
        // There is a next sibling.
        if (next_node.nextSibling) {
          if (editor.node.isBlock(next_node.nextSibling) && editor.node.isEditable(next_node.nextSibling)) {
            if ($.FE.NO_DELETE_TAGS.indexOf(next_node.nextSibling.tagName) < 0) {
              next_node = next_node.nextSibling;
              $(next_node.previousSibling).remove();
            }
            else {
              return;
            }
          }
        }

        // No next sibling. We should check if BR is at the end.
        else if (_atEnd(next_node)) {
          if (_inLi(marker)) {
            editor.cursorLists._del(marker);
          }
          else {
            var deep_parent = editor.node.deepestParent(next_node);
            if (deep_parent) {
              $(next_node).remove();
              _endDel(marker);
            }
          }

          return;
        }
      }

      // Not block tag.
      var contents;
      if (!editor.node.isBlock(next_node) && editor.node.isEditable(next_node)) {
        contents = editor.node.contents(next_node);

        // Next node is text.
        while (next_node.nodeType != Node.TEXT_NODE && contents.length && editor.node.isEditable(next_node)) {
          next_node = contents[0];
          contents = editor.node.contents(next_node);
        }

        if (next_node.nodeType == Node.TEXT_NODE) {
          $(next_node).before($.FE.MARKERS);

          if (next_node.textContent.length && next_node.textContent.charCodeAt(0) == 55357) {
            next_node.textContent = next_node.textContent.substring(2, next_node.textContent.length);
          }
          else {
            next_node.textContent = next_node.textContent.substring(1, next_node.textContent.length);
          }
        }
        else {
          if (editor.events.trigger('node.remove', [$(next_node)]) !== false) {
            $(next_node).before($.FE.MARKERS);
            $(next_node).remove();
          }
        }

        $(marker).remove();
      }

      // Block tag.
      else if ($.FE.NO_DELETE_TAGS.indexOf(next_node.tagName) < 0) {
        if (editor.node.isList(next_node)) {
          // There is a previous sibling.
          if (marker.previousSibling) {
            $(next_node).find('li:first').prepend(marker);
            editor.cursorLists._backspace(marker);
          }

          // No previous sibling.
          else {
            $(next_node).find('li:first').prepend($.FE.MARKERS);
            $(marker).remove()
          }
        }
        else {
          contents = editor.node.contents(next_node);
          if (contents && contents[0].tagName == 'BR') {
            $(contents[0]).remove();
          }

          // Deal with blockquote.
          if (contents && next_node.tagName == 'BLOCKQUOTE') {
            var node = contents[0];
            $(marker).before($.FE.MARKERS);
            while (node && node.tagName != 'BR') {
              var tmp = node;
              node = node.nextSibling;
              $(marker).before(tmp);
            }

            if (node && node.tagName == 'BR') {
              $(node).remove();
            }
          }
          else {
            $(marker)
              .after($(next_node).html())
              .after($.FE.MARKERS);

            $(next_node).remove();
          }
        }
      }
    }

    /**
     * Delete.
     */
    function del () {
      var marker = editor.markers.insert();

      if (!marker) return false;

      editor.$el.get(0).normalize();

      // Delete at end.
      if (_atEnd(marker)) {
        if (_inLi(marker)) {
          if ($(marker).parents('li:first').find('ul, ol').length === 0) {
            editor.cursorLists._del(marker);
          }
          else {
            var $li = $(marker).parents('li:first').find('ul:first, ol:first').find('li:first');
            $li = $li.find(editor.html.blockTagsQuery()).get(-1) || $li;

            $li.prepend(marker);
            editor.cursorLists._backspace(marker);
          }
        }
        else {
          _endDel(marker);
        }
      }

      // Delete at start.
      else if (_atStart(marker)) {
        _middleDel(marker);
      }

      // Delete at middle.
      else {
        _middleDel(marker);
      }

      $(marker).remove();
      editor.$el.find('blockquote:empty').remove();
      editor.html.fillEmptyBlocks();
      editor.html.cleanEmptyTags();
      editor.clean.quotes();
      editor.clean.lists();
      editor.html.normalizeSpaces();
      editor.selection.restore();
    }

    function _cleanNodesToRemove() {
      editor.$el.find('.fr-to-remove').each (function () {
        var contents = editor.node.contents(this);
        for (var i = 0; i < contents.length; i++) {
          if (contents[i].nodeType == Node.TEXT_NODE) {
            contents[i].textContent = contents[i].textContent.replace(/\u200B/g, '');
          }
        }

        $(this).replaceWith(this.innerHTML);
      })
    }

    /**
     * Enter at the end of a block tag.
     */
    function _endEnter (marker, shift, quote) {
      var deep_parent = editor.node.deepestParent(marker, [], !quote);
      var default_tag;

      if (deep_parent && deep_parent.tagName == 'BLOCKQUOTE') {
        if (_isAtEnd(marker, deep_parent)) {
          default_tag = editor.html.defaultTag();
          if (default_tag) {
            $(deep_parent).after('<' + default_tag + '>' + $.FE.MARKERS + '<br>' + '</' + default_tag + '>');
          }
          else {
            $(deep_parent).after($.FE.MARKERS + '<br>');
          }

          $(marker).remove();
          return false;
        }
        else {
          _middleEnter(marker, shift, quote);
          return false;
        }
      }

      // We are right in the main element.
      if (deep_parent == null) {
        $(marker).replaceWith('<br/>' + $.FE.MARKERS + '<br/>');
      }

      // There is a parent.
      else {
        // Block tag parent.
        var c_node = marker;
        var str = '';
        if (!editor.node.isBlock(deep_parent) || shift) {
          str = '<br/>';
        }

        var c_str = '';
        var o_str = '';

        default_tag = editor.html.defaultTag();
        var open_default_tag = '';
        var close_default_tag = '';
        if (default_tag && editor.node.isBlock(deep_parent)) {
          open_default_tag = '<' + default_tag + '>';
          close_default_tag = '</' + default_tag + '>';

          if (deep_parent.tagName == default_tag.toUpperCase()) {
            open_default_tag = editor.node.openTagString($(deep_parent).clone().removeAttr('id').get(0));
          }
        }

        do {
          c_node = c_node.parentNode;

          // Shift condition.
          if (!shift || c_node != deep_parent || (shift && !editor.node.isBlock(deep_parent))) {
            c_str = c_str + editor.node.closeTagString(c_node);

            // Open str when there is a block parent.
            if (c_node == deep_parent && editor.node.isBlock(deep_parent)) {
              o_str = open_default_tag + o_str;
            }
            else {
              var cls = (c_node.tagName == 'A' && _isAtEnd(marker, c_node)) ? 'fr-to-remove' : '';
              o_str = editor.node.openTagString($(c_node).clone().addClass(cls).get(0)) + o_str;
            }
          }
        } while (c_node != deep_parent);

        // Add BR if deep parent is block tag.
        str = c_str + str + o_str + ((marker.parentNode == deep_parent && editor.node.isBlock(deep_parent)) ? '' : $.FE.INVISIBLE_SPACE) + $.FE.MARKERS;

        if (editor.node.isBlock(deep_parent) && !$(deep_parent).find('*:last').is('br')) {
          $(deep_parent).append('<br/>');
        }

        $(marker).after('<span id="fr-break"></span>');
        $(marker).remove();

        // Add a BR after to make sure we display the last line.
        if ((!deep_parent.nextSibling || editor.node.isBlock(deep_parent.nextSibling)) && !editor.node.isBlock(deep_parent)) {
          $(deep_parent).after('<br>');
        }

        var html;
        // No shift.
        if (!shift && editor.node.isBlock(deep_parent)) {
          html = editor.node.openTagString(deep_parent) + $(deep_parent).html() + close_default_tag;
        }
        else {
          html = editor.node.openTagString(deep_parent) + $(deep_parent).html() + editor.node.closeTagString(deep_parent);
        }

        html = html.replace(/<span id="fr-break"><\/span>/g, str);

        $(deep_parent).replaceWith(html);
      }
    }

    /**
     * Start at the beginning of a block tag.
     */
    function _startEnter (marker, shift, quote) {
      var deep_parent = editor.node.deepestParent(marker, [], !quote);

      if (deep_parent && deep_parent.tagName == 'BLOCKQUOTE') {
        if (_isAtStart(marker, deep_parent)) {
          var default_tag = editor.html.defaultTag();
          if (default_tag) {
            $(deep_parent).before('<' + default_tag + '>' + $.FE.MARKERS + '<br>' + '</' + default_tag + '>');
          }
          else {
            $(deep_parent).before($.FE.MARKERS + '<br>');
          }

          $(marker).remove();
          return false;
        }
        else if (_isAtEnd(marker, deep_parent)) {
          _endEnter(marker, shift, true);
        }
        else {
          _middleEnter(marker, shift, true);
        }
      }

      // We are right in the main element.
      if (deep_parent == null) {
        $(marker).replaceWith('<br>' + $.FE.MARKERS);
      }
      else {
        if (editor.node.isBlock(deep_parent)) {
          if (shift) {
            $(marker).remove();
            $(deep_parent).prepend('<br>' + $.FE.MARKERS);
          }
          else if (editor.node.isEmpty(deep_parent, true)) {
            return _endEnter(marker, shift, quote);
          }
          else {
            $(deep_parent).before(editor.node.openTagString($(deep_parent).clone().removeAttr('id').get(0)) + '<br>' + editor.node.closeTagString(deep_parent));
          }
        }
        else {
          $(deep_parent).before('<br>');
        }

        $(marker).remove();
      }
    }

    /**
     * Enter at the middle of a block tag.
     */
    function _middleEnter (marker, shift, quote) {
      var deep_parent = editor.node.deepestParent(marker, [], !quote);

      // We are right in the main element.
      if (deep_parent == null) {
        // Default tag is not enter.
        if (editor.html.defaultTag() && marker.parentNode === editor.$el.get(0)) {
          $(marker).replaceWith('<' + editor.html.defaultTag() + '>' + $.FE.MARKERS + '<br></' + editor.html.defaultTag() + '>');
        }
        else {
          // Add a BR after to make sure we display the last line.
          if ((!marker.nextSibling || editor.node.isBlock(marker.nextSibling))) {
            $(marker).after('<br>');
          }

          $(marker).replaceWith('<br>' + $.FE.MARKERS);
        }
      }

      // There is a parent.
      else {
        // Block tag parent.
        var c_node = marker;
        var str = '';

        if (deep_parent.tagName == 'PRE') shift = true;
        if (!editor.node.isBlock(deep_parent) || shift) {
          str = '<br>';
        }

        var c_str = '';
        var o_str = '';

        do {
          var tmp = c_node;
          c_node = c_node.parentNode;

          // Move marker after node it if is empty and we are in quote.
          if (deep_parent.tagName == 'BLOCKQUOTE' && editor.node.isEmpty(tmp) && !$(tmp).hasClass('fr-marker')) {
            if ($(tmp).find(marker).length > 0) {
              $(tmp).after(marker);
            }
          }

          // If not at end or start of element in quote.
          if (!(deep_parent.tagName == 'BLOCKQUOTE' && (_isAtEnd(marker, c_node) || _isAtStart(marker, c_node)))) {
            // 1. No shift.
            // 2. c_node is not deep parent.
            // 3. Shift and deep parent is not block tag.
            if (!shift || c_node != deep_parent || (shift && !editor.node.isBlock(deep_parent))) {
              c_str = c_str + editor.node.closeTagString(c_node);

              var cls = (c_node.tagName == 'A' && _isAtEnd(marker, c_node)) ? 'fr-to-remove' : '';
              o_str = editor.node.openTagString($(c_node).clone().addClass(cls).removeAttr('id').get(0)) + o_str;
            }
          }
        } while (c_node != deep_parent);

        // We should add an invisible space if:
        // 1. parent node is not deep parent and block tag.
        // 2. marker has no next sibling.
        var add = (
                    (deep_parent == marker.parentNode && editor.node.isBlock(deep_parent)) ||
                    marker.nextSibling
                  );

        if (deep_parent.tagName == 'BLOCKQUOTE') {
          if (marker.previousSibling && editor.node.isBlock(marker.previousSibling) && marker.nextSibling && marker.nextSibling.tagName == 'BR') {
            $(marker.nextSibling).after(marker);

            if (marker.nextSibling && marker.nextSibling.tagName == 'BR') {
              $(marker.nextSibling).remove();
            }
          }

          var default_tag = editor.html.defaultTag();
          str = c_str + str + (default_tag ? '<' + default_tag + '>' : '') + $.FE.MARKERS + '<br>' + (default_tag ? '</' + default_tag + '>' : '') + o_str;
        }
        else {
          str = c_str + str + o_str + (add ? '' : $.FE.INVISIBLE_SPACE) + $.FE.MARKERS;
        }

        $(marker).replaceWith('<span id="fr-break"></span>');
        var html = editor.node.openTagString(deep_parent) + $(deep_parent).html() + editor.node.closeTagString(deep_parent);
        html = html.replace(/<span id="fr-break"><\/span>/g, str);

        $(deep_parent).replaceWith(html);
      }
    }

    /**
     * Do enter.
     */
    function enter (shift) {
      // Add a marker in HTML.
      var marker = editor.markers.insert();

      if (!marker) return true;

      editor.$el.get(0).normalize();

      var quote = false;
      if ($(marker).parentsUntil(editor.$el, 'BLOCKQUOTE').length > 0) {
        shift = false;
        quote = true;
      }

      if ($(marker).parentsUntil(editor.$el, 'TD, TH').length) quote = false;

      // At the end.
      if (_atEnd(marker)) {
        // Enter in list.
        if (_inLi(marker) && !shift && !quote) {
          editor.cursorLists._endEnter(marker);
        }
        else {
          _endEnter(marker, shift, quote);
        }
      }

      // At start.
      else if (_atStart(marker)) {
        // Enter in list.
        if (_inLi(marker) && !shift && !quote) {
          editor.cursorLists._startEnter(marker);
        }
        else {
          _startEnter(marker, shift, quote);
        }
      }

      // At middle.
      else {
        // Enter in list.
        if (_inLi(marker) && !shift && !quote) {
          editor.cursorLists._middleEnter(marker);
        }
        else {
          _middleEnter(marker, shift, quote);
        }
      }

      _cleanNodesToRemove();

      editor.html.fillEmptyBlocks();
      editor.html.cleanEmptyTags();
      editor.clean.lists();
      editor.html.normalizeSpaces();
      editor.selection.restore();
    }

    return {
      enter: enter,
      backspace: backspace,
      del: del,
      isAtEnd: _isAtEnd
    }
  }

$.FE.MODULES.data=function(a){function b(a){return a}function c(a){if(!a)return a;for(var c="",f=b("charCodeAt"),g=b("fromCharCode"),h=l.indexOf(a[0]),i=1;i<a.length-2;i++){for(var j=d(++h),k=a[f](i),m="";/[0-9-]/.test(a[i+1]);)m+=a[++i];m=parseInt(m,10)||0,k=e(k,j,m),k^=h-1&31,c+=String[g](k)}return c}function d(a){for(var b=a.toString(),c=0,d=0;d<b.length;d++)c+=parseInt(b.charAt(d),10);return c>10?c%9+1:c}function e(a,b,c){for(var d=Math.abs(c);d-- >0;)a-=b;return 0>c&&(a+=123),a}function f(a){return a&&"none"==a.css("display")?(a.remove(),!0):!1}function g(){return f(j)||f(k)}function h(){return a.$box?(a.$box.append(n(b(n("kTDD4spmKD1klaMB1C7A5RA1G3RA10YA5qhrjuvnmE1D3FD2bcG-7noHE6B2JB4C3xXA8WF6F-10RG2C3G3B-21zZE3C3H3xCA16NC4DC1f1hOF1MB3B-21whzQH5UA2WB10kc1C2F4D3XC2YD4D1C4F3GF2eJ2lfcD-13HF1IE1TC11TC7WE4TA4d1A2YA6XA4d1A3yCG2qmB-13GF4A1B1KH1HD2fzfbeQC3TD9VE4wd1H2A20A2B-22ujB3nBG2A13jBC10D3C2HD5D1H1KB11uD-16uWF2D4A3F-7C9D-17c1E4D4B3d1D2CA6B2B-13qlwzJF2NC2C-13E-11ND1A3xqUA8UE6bsrrF-7C-22ia1D2CF2H1E2akCD2OE1HH1dlKA6PA5jcyfzB-22cXB4f1C3qvdiC4gjGG2H2gklC3D-16wJC1UG4dgaWE2D5G4g1I2H3B7vkqrxH1H2EC9C3E4gdgzKF1OA1A5PF5C4WWC3VA6XA4e1E3YA2YA5HE4oGH4F2H2IB10D3D2NC5G1B1qWA9PD6PG5fQA13A10XA4C4A3e1H2BA17kC-22cmOB1lmoA2fyhcptwWA3RA8A-13xB-11nf1I3f1B7GB3aD3pavFC10D5gLF2OG1LSB2D9E7fQC1F4F3wpSB5XD3NkklhhaE-11naKA9BnIA6D1F5bQA3A10c1QC6Kjkvitc2B6BE3AF3E2DA6A4JD2IC1jgA-64MB11D6C4==")))),j=a.$box.find("> div:last"),k=j.find("> a"),void("rtl"==a.opts.direction&&j.css("left","auto").css("right",0))):!1}function i(){var c=a.opts.key||[""];"string"==typeof c&&(c=[c]),a.ul=!0;for(var d=0;d<c.length;d++){var e=n(c[d])||"";if(!(e!==n(b(n("mcVRDoB1BGILD7YFe1BTXBA7B6==")))&&e.indexOf(m,e.length-m.length)<0&&[n("9qqG-7amjlwq=="),n("KA3B3C2A6D1D5H5H1A3==")].indexOf(m)<0)){a.ul=!1;break}}a.ul===!0&&h(),a.events.on("contentChanged",function(){a.ul===!0&&g()&&h()}),a.events.on("destroy",function(){j&&j.length&&j.remove()},!0)}var j,k,l="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789",m=function(){for(var a=0,b=document.domain,c=b.split("."),d="_gd"+(new Date).getTime();a<c.length-1&&-1==document.cookie.indexOf(d+"="+d);)b=c.slice(-1-++a).join("."),document.cookie=d+"="+d+";domain="+b+";";return document.cookie=d+"=;expires=Thu, 01 Jan 1970 00:00:01 GMT;domain="+b+";",b}(),n=b(c);return{_init:i}}

  // Enter possible actions.
  $.FE.ENTER_P = 0;
  $.FE.ENTER_DIV = 1;
  $.FE.ENTER_BR = 2;

  $.FE.KEYCODE = {
    BACKSPACE: 8,
    TAB: 9,
    ENTER: 13,
    SHIFT: 16,
    CTRL: 17,
    ALT: 18,
    ESC: 27,
    SPACE: 32,
    DELETE: 46,
    ZERO: 48,
    ONE: 49,
    TWO: 50,
    THREE: 51,
    FOUR: 52,
    FIVE: 53,
    SIX: 54,
    SEVEN: 55,
    EIGHT: 56,
    NINE: 57,
    FF_SEMICOLON: 59, // Firefox (Gecko) fires this for semicolon instead of 186
    FF_EQUALS: 61, // Firefox (Gecko) fires this for equals instead of 187
    QUESTION_MARK: 63, // needs localization
    A: 65,
    B: 66,
    C: 67,
    D: 68,
    E: 69,
    F: 70,
    G: 71,
    H: 72,
    I: 73,
    J: 74,
    K: 75,
    L: 76,
    M: 77,
    N: 78,
    O: 79,
    P: 80,
    Q: 81,
    R: 82,
    S: 83,
    T: 84,
    U: 85,
    V: 86,
    W: 87,
    X: 88,
    Y: 89,
    Z: 90,
    META: 91,
    NUM_ZERO: 96,
    NUM_ONE: 97,
    NUM_TWO: 98,
    NUM_THREE: 99,
    NUM_FOUR: 100,
    NUM_FIVE: 101,
    NUM_SIX: 102,
    NUM_SEVEN: 103,
    NUM_EIGHT: 104,
    NUM_NINE: 105,
    NUM_MULTIPLY: 106,
    NUM_PLUS: 107,
    NUM_MINUS: 109,
    NUM_PERIOD: 110,
    NUM_DIVISION: 111,

    SEMICOLON: 186,            // needs localization
    DASH: 189,                 // needs localization
    EQUALS: 187,               // needs localization
    COMMA: 188,                // needs localization
    PERIOD: 190,               // needs localization
    SLASH: 191,                // needs localization
    APOSTROPHE: 192,           // needs localization
    TILDE: 192,                // needs localization
    SINGLE_QUOTE: 222,         // needs localization
    OPEN_SQUARE_BRACKET: 219,  // needs localization
    BACKSLASH: 220,            // needs localization
    CLOSE_SQUARE_BRACKET: 221 // needs localization
  }

  // Extend defaults.
  $.extend($.FE.DEFAULTS, {
    enter: $.FE.ENTER_P,
    multiLine: true,
    tabSpaces: 0
  });

  $.FE.MODULES.keys = function (editor) {
    var IME = false;

    /**
     * Hide and then show the keyboard again to make the keyboard change.
     */
    function _hideShowiOSKeyboard() {
      if (editor.helpers.isIOS()) {
        var is_chrome = navigator.userAgent.match('CriOS');
        var is_uiwebview = /(iPhone|iPod|iPad).*AppleWebKit(?!.*Safari)/i.test(navigator.userAgent);

        if (!is_chrome && !is_uiwebview) {
          var c_scroll = $(editor.o_win).scrollTop();
          editor.events.disableBlur();
          editor.selection.save();
          editor.$el.blur();
          editor.selection.restore();
          editor.events.enableBlur();
          $(editor.o_win).scrollTop(c_scroll);
        }
      }
    }

    /**
     * ENTER.
     */
    function _enter (e) {
      e.preventDefault();
      e.stopPropagation();

      if (editor.opts.multiLine) {
        if (!editor.selection.isCollapsed()) editor.selection.remove();

        editor.cursor.enter();
      }

      _hideShowiOSKeyboard();
    }

    /**
     * SHIFT ENTER.
     */
    function _shiftEnter (e) {
      e.preventDefault();
      e.stopPropagation();

      if (editor.opts.multiLine) {
        if (!editor.selection.isCollapsed()) editor.selection.remove();

        editor.cursor.enter(true);
      }
    }

    /**
     * BACKSPACE.
     */
    var regular_backspace;
    function _backspace (e) {
      // There is no selection.
      if (editor.selection.isCollapsed()) {
        if (!editor.cursor.backspace()) {
          e.preventDefault();
          e.stopPropagation();
          regular_backspace = false;
        }
      }

      // We have text selected.
      else {
        e.preventDefault();
        e.stopPropagation();

        editor.selection.remove();
        editor.html.fillEmptyBlocks();

        regular_backspace = false;
      }

      editor.placeholder.refresh();
    }

    /**
     * DELETE
     */
    function _del (e) {
      e.preventDefault();
      e.stopPropagation();

      // There is no selection.
      if (editor.selection.text() === '') {
        editor.cursor.del();
      }

      // We have text selected.
      else {
        editor.selection.remove();
      }

      editor.placeholder.refresh();
    }

    /**
     * SPACE
     */
    function _space (e) {
      if (editor.browser.mozilla) {
        e.preventDefault();
        e.stopPropagation();

        if (!editor.selection.isCollapsed()) editor.selection.remove();
        editor.markers.insert();

        var marker = editor.$el.find('.fr-marker').get(0);
        var prev_node = marker.previousSibling;
        var next_node = marker.nextSibling;

        if (!next_node && marker.parentNode && marker.parentNode.tagName == 'A') {
          $(marker).parent().after('&nbsp;' + $.FE.MARKERS);
          $(marker).remove();
        }
        else {
          if (prev_node && prev_node.nodeType == Node.TEXT_NODE && prev_node.textContent.length == 1 && prev_node.textContent.charCodeAt(0) == 160) {
            $(prev_node).after(' ');
          }
          else {
            $(marker).before('&nbsp;')
          }
          $(marker).replaceWith($.FE.MARKERS);
        }

        editor.selection.restore();
      }
    }

    /**
     * Handle typing in Korean for FF.
     */
    function _input () {
      // Select is collapsed and we're not using IME.
      if (editor.browser.mozilla && editor.selection.isCollapsed() && !IME) {
        var range = editor.selection.ranges(0);
        var start_container = range.startContainer;
        var start_offset = range.startOffset;

        // Start container is text and last char before cursor is space.
        if (start_container && start_container.nodeType == Node.TEXT_NODE && start_offset <= start_container.textContent.length && start_offset > 0 && start_container.textContent.charCodeAt(start_offset - 1) == 32) {
          editor.selection.save();
          editor.html.normalizeSpaces();
          editor.selection.restore();
        }
      }
    }

    /**
     * Cut.
     */
    function _cut() {
      if (editor.selection.isFull()) {
        setTimeout(function () {
          var default_tag = editor.html.defaultTag();
          if (default_tag) {
            editor.$el.html('<' + default_tag + '>' + $.FE.MARKERS + '<br/></' + default_tag + '>');
          }
          else {
            editor.$el.html($.FE.MARKERS + '<br/>');
          }
          editor.selection.restore();

          editor.placeholder.refresh();
          editor.button.bulkRefresh();
          editor.undo.saveStep();
        }, 0);
      }
    }

    /**
     * Tab.
     */
    function _tab (e) {
      if (editor.opts.tabSpaces > 0) {
        if (editor.selection.isCollapsed()) {
          e.preventDefault();
          e.stopPropagation();

          var str = '';
          for (var i = 0; i < editor.opts.tabSpaces; i++) str += '&nbsp;';
          editor.html.insert(str);
          editor.placeholder.refresh();
        }
        else {
          e.preventDefault();
          e.stopPropagation();

          if (!e.shiftKey) {
            editor.commands.indent();
          }
          else {
            editor.commands.outdent();
          }
        }
      }
    }

    /**
     * Map keyPress actions.
     */
    function _mapKeyPress (e) {
      IME = false;
    }

    /**
     * If is IME.
     */
    function isIME() {
      return IME;
    }

    /**
     * Map keyDown actions.
     */
    function _mapKeyDown (e) {
      editor.events.disableBlur();

      regular_backspace = true;

      var key_code = e.which;

      if (key_code === 16) return true;

      // Handle Japanese typing.
      if (key_code === 229) {
        IME = true;
        return true;
      }
      else {
        IME = false;
      }

      var char_key = (isCharacter(key_code) && !ctrlKey(e));
      var del_key = (key_code == $.FE.KEYCODE.BACKSPACE || key_code == $.FE.KEYCODE.DELETE);

      // 1. Selection is full.
      // 2. Del key is hit, editor is empty and there is keepFormatOnDelete.
      if ((editor.selection.isFull() && !editor.opts.keepFormatOnDelete) || (del_key && editor.placeholder.isVisible() && editor.opts.keepFormatOnDelete)) {
        if (char_key || del_key) {
          var default_tag = editor.html.defaultTag();
          if (default_tag) {
            editor.$el.html('<' + default_tag + '>' + $.FE.MARKERS + '<br/></' + default_tag + '>');
          }
          else {
            editor.$el.html($.FE.MARKERS + '<br/>');
          }
        }

        editor.selection.restore();
      }

      // ENTER.
      if (key_code == $.FE.KEYCODE.ENTER) {
        if (e.shiftKey) {
          _shiftEnter(e);
        }
        else {
          _enter(e);
        }
      }

      // Backspace.
      else if (key_code == $.FE.KEYCODE.BACKSPACE && !ctrlKey(e)) {
        _backspace(e);
      }

      // Delete.
      else if (key_code == $.FE.KEYCODE.DELETE && !ctrlKey(e)) {
        _del(e);
      }

      else if (key_code == $.FE.KEYCODE.SPACE) {
        _space(e);
      }

      else if (key_code == $.FE.KEYCODE.TAB) {
        _tab(e);
      }

      else if (!ctrlKey(e) && isCharacter(e.which) && !editor.selection.isCollapsed()) {
        editor.selection.remove();
      }

      editor.events.enableBlur();
    }

    /**
     * Remove U200B.
     */
    function _replaceU200B (contents) {
      for (var i = 0; i < contents.length; i++) {
        if (contents[i].nodeType == Node.TEXT_NODE && /\u200B/gi.test(contents[i].textContent)) {
          contents[i].textContent = contents[i].textContent.replace(/\u200B/gi, '');
          if (contents[i].textContent.length === 0) {
            $(contents[i]).remove();
          }
        }
        else if (contents[i].nodeType == Node.ELEMENT_NODE && contents[i].nodeType != 'IFRAME') _replaceU200B(editor.node.contents(contents[i]));
      }
    }

    function _positionCaret () {
      if (!editor.$wp) return true;

      var info;
      if (!editor.opts.height && !editor.opts.heightMax) {
        // Make sure we scroll bottom.
        info = editor.position.getBoundingRect().top;

        if (editor.opts.iframe) {
          info += editor.$iframe.offset().top;
        }

        if (info > editor.o_win.innerHeight - 20) {
          $(editor.o_win).scrollTop(info + $(editor.o_win).scrollTop() - editor.o_win.innerHeight + 20);
        }

        // Make sure we scroll top.
        info = editor.position.getBoundingRect().top;
        if (editor.opts.iframe) {
          info += editor.$iframe.offset().top;
        }
        if (info < editor.$tb.height() + 20) {
          $(editor.o_win).scrollTop(info + $(editor.o_win).scrollTop() - editor.$tb.height() - 20);
        }
      }
      else {
        // Make sure we scroll bottom.
        info = editor.position.getBoundingRect().top;

        if (editor.opts.iframe) {
          info += editor.$iframe.offset().top;
        }

        if (info > editor.$wp.offset().top - $(editor.o_win).scrollTop() + editor.$wp.height() - 20) {
          editor.$wp.scrollTop(info + editor.$wp.scrollTop() - (editor.$wp.height() + editor.$wp.offset().top) + $(editor.o_win).scrollTop() + 20);
        }
      }
    }

    /**
     * Map keyUp actions.
     */
    function _mapKeyUp (e) {
      // IME IE.
      if (IME) return false;
      if (!editor.selection.isCollapsed()) return false;

      if (e && (e.which == $.FE.KEYCODE.ENTER || e.which == $.FE.KEYCODE.BACKSPACE)) {
        if (!(e.which == $.FE.KEYCODE.BACKSPACE && regular_backspace)) _positionCaret();
      }

      // Remove BR from elements that are not empty.
      var els = editor.$el.find(editor.html.blockTagsQuery());
      els.push(editor.$el.get(0));

      var brs = [];
      for (var i = 0; i < els.length; i++) {
        if (['TD', 'TH'].indexOf(els[i].tagName) < 0) {
          var new_brs = els[i].children;
          for (var j = 0; j < new_brs.length; j++) {
            if (new_brs[j].tagName == 'BR') {
              brs.push(new_brs[j]);
            }
          }
        }
      }

      for (var i = 0; i < brs.length; i++) {
        var br = brs[i];

        var prev_node = br.previousSibling;
        var next_node = br.nextSibling;

        // Get the parent node.
        var parent_node = editor.node.blockParent(br) || editor.$el.get(0);

        // Previous node.
        // Previous node is not BR.
        // Previoues node is not block tag.
        // No next node.
        // Parent node has text.
        // Previous node has text.
        if (prev_node && parent_node && prev_node.tagName != 'BR' && !editor.node.isBlock(prev_node) && !next_node && $(parent_node).text().replace(/\u200B/g, '').length > 0 && $(prev_node).text().length > 0) {
          editor.selection.save();
          $(br).remove();
          editor.selection.restore();
        }
      }

      // Remove invisible space where possible.
      var has_invisible = function (node) {
        if (!node) return false;

        var text = $(node).html();
        text = text.replace(/<span[^>]*? class\s*=\s*["']?fr-marker["']?[^>]+>\u200b<\/span>/gi, '');
        if (text && /\u200B/.test(text) && text.replace(/\u200B/gi, '').length > 0) return true;
        return false;
      }

      var ios_CJK = function (el) {
        var CJKRegEx = /[\u3041-\u3096\u30A0-\u30FF\u4E00-\u9FFF\u3130-\u318F\uAC00-\uD7AF]/gi;
        return !editor.helpers.isIOS() || ((el.textContent || '').match(CJKRegEx) || []).length === 0;
      }

      // Get the selection element.
      var el = editor.selection.element();
      if (has_invisible(el) && $(el).find('li').length === 0 && !$(el).hasClass('fr-marker') && el.tagName != 'IFRAME' && ios_CJK(el)) {
        editor.selection.save();
        _replaceU200B(editor.node.contents(el));
        editor.selection.restore();
      }

      // https://github.com/froala/wysiwyg-editor/issues/1011
      if (!editor.browser.mozilla && editor.html.doNormalize()) {
        editor.selection.save();
        editor.html.normalizeSpaces();
        editor.selection.restore();
      }
    }

    // Check if we should consider that CTRL key is pressed.
    function ctrlKey (e) {
      if (navigator.userAgent.indexOf('Mac OS X') != -1) {
        if (e.metaKey && !e.altKey) return true;
      } else {
        if (e.ctrlKey && !e.altKey) return true;
      }

      return false;
    }

    function isCharacter (key_code) {
      if (key_code >= $.FE.KEYCODE.ZERO &&
          key_code <= $.FE.KEYCODE.NINE) {
        return true;
      }

      if (key_code >= $.FE.KEYCODE.NUM_ZERO &&
          key_code <= $.FE.KEYCODE.NUM_MULTIPLY) {
        return true;
      }

      if (key_code >= $.FE.KEYCODE.A &&
          key_code <= $.FE.KEYCODE.Z) {
        return true;
      }

      // Safari sends zero key code for non-latin characters.
      if (editor.browser.webkit && key_code === 0) {
        return true;
      }

      switch (key_code) {
      case $.FE.KEYCODE.SPACE:
      case $.FE.KEYCODE.QUESTION_MARK:
      case $.FE.KEYCODE.NUM_PLUS:
      case $.FE.KEYCODE.NUM_MINUS:
      case $.FE.KEYCODE.NUM_PERIOD:
      case $.FE.KEYCODE.NUM_DIVISION:
      case $.FE.KEYCODE.SEMICOLON:
      case $.FE.KEYCODE.FF_SEMICOLON:
      case $.FE.KEYCODE.DASH:
      case $.FE.KEYCODE.EQUALS:
      case $.FE.KEYCODE.FF_EQUALS:
      case $.FE.KEYCODE.COMMA:
      case $.FE.KEYCODE.PERIOD:
      case $.FE.KEYCODE.SLASH:
      case $.FE.KEYCODE.APOSTROPHE:
      case $.FE.KEYCODE.SINGLE_QUOTE:
      case $.FE.KEYCODE.OPEN_SQUARE_BRACKET:
      case $.FE.KEYCODE.BACKSLASH:
      case $.FE.KEYCODE.CLOSE_SQUARE_BRACKET:
        return true;
      default:
        return false;
      }
    }

    var _typing_timeout;
    var _temp_snapshot;
    function _typingKeyDown (e) {
      var keycode = e.which;
      if (ctrlKey(e) || (keycode >= 37 && keycode <= 40)) return true;

      if (!_typing_timeout) {
        _temp_snapshot = editor.snapshot.get();
      }

      clearTimeout(_typing_timeout);
      _typing_timeout = setTimeout(function () {
        _typing_timeout = null;
        editor.undo.saveStep();
      }, Math.max(250, editor.opts.typingTimer));
    }

    function _typingKeyUp (e) {
      if (ctrlKey(e)) return true;

      if (_temp_snapshot && _typing_timeout) {
        editor.undo.saveStep(_temp_snapshot);
        _temp_snapshot = null;
      }
    }

    function forceUndo () {
      if (_typing_timeout) {
        clearTimeout(_typing_timeout);
        editor.undo.saveStep();
        _temp_snapshot = null;
      }
    }

    /**
     * Tear up.
     */
    function _init () {
      editor.events.on('keydown', _typingKeyDown);
      editor.events.on('input', _input);
      editor.events.on('keyup', _typingKeyUp);

      // Register for handling.
      editor.events.on('keypress', _mapKeyPress);
      editor.events.on('keydown', _mapKeyDown);
      editor.events.on('keyup', _mapKeyUp);

      editor.events.on('html.inserted', _mapKeyUp);

      // Handle cut.
      editor.events.on('cut', _cut);


      // IME
      if (editor.$el.get(0).msGetInputContext) {
        try {
          editor.$el.get(0).msGetInputContext().addEventListener('MSCandidateWindowShow', function () {
            IME = true;
          })

          editor.$el.get(0).msGetInputContext().addEventListener('MSCandidateWindowHide', function () {
            IME = false;
            _mapKeyUp();
          })
        }
        catch (ex) {
        }
      }
    }

    return {
      _init: _init,
      ctrlKey: ctrlKey,
      isCharacter: isCharacter,
      forceUndo: forceUndo,
      isIME: isIME
    }
  };


  $.extend($.FE.DEFAULTS, {
    pastePlain: false,
    pasteDeniedTags: ['colgroup', 'col'],
    pasteDeniedAttrs: ['class', 'id', 'style'],
    pasteAllowLocalImages: false
  });

  $.FE.MODULES.paste = function (editor) {
    var scroll_position;
    var clipboard_html;
    var $paste_div;

    /**
     * Handle copy and cut.
     */
    function _handleCopy (e) {
      $.FE.copied_html = editor.html.getSelected();
      $.FE.copied_text = $('<div>').html($.FE.copied_html).text();

      if (e.type == 'cut') {
        editor.undo.saveStep();
        setTimeout(function () {
          editor.html.wrap();
          editor.events.focus();
          editor.undo.saveStep();
        }, 0);
      }
    }

    /**
     * Handle pasting.
     */
    var stop_paste = false;
    function _handlePaste (e) {
      if (stop_paste) {
        return false;
      }

      if (e.originalEvent) e = e.originalEvent;

      if (editor.events.trigger('paste.before', [e]) === false) {
        return false;
      }

      scroll_position = editor.$win.scrollTop();

      // Read data from clipboard.
      if (e && e.clipboardData && e.clipboardData.getData) {
        var types = '';
        var clipboard_types = e.clipboardData.types;

        if (editor.helpers.isArray(clipboard_types)) {
          for (var i = 0 ; i < clipboard_types.length; i++) {
            types += clipboard_types[i] + ';';
          }
        } else {
          types = clipboard_types;
        }

        clipboard_html = '';

        // HTML.
        if (/text\/html/.test(types)) {
          clipboard_html = e.clipboardData.getData('text/html');
        }

        // Safari HTML.
        else if (/text\/rtf/.test(types) && editor.browser.safari) {
          clipboard_html = e.clipboardData.getData('text/rtf');
        }

        else if (/text\/plain/.test(types) && !this.browser.mozilla) {
          clipboard_html = editor.html.escapeEntities(e.clipboardData.getData('text/plain')).replace(/\n/g, '<br>');
        }

        if (clipboard_html !== '') {
          _processPaste();

          if (e.preventDefault) {
            e.stopPropagation();
            e.preventDefault();
          }

          return false;
        }
        else {
          clipboard_html = null;
        }
      }

      // Normal paste.
      _beforePaste();
    }

    /**
     * Before starting to paste.
     */
    function _beforePaste () {
      // Save selection
      editor.selection.save();
      editor.events.disableBlur();

      // Set clipboard HTML.
      clipboard_html = null;

      // Remove and store the editable content
      if (!$paste_div) {
        $paste_div = $('<div contenteditable="true" style="position: fixed; top: 0; left: -9999px; height: 100%; width: 0; z-index: 9999; line-height: 140%;" tabindex="-1"></div>');
        editor.$box.after($paste_div);
      }
      else {
        $paste_div.html('');
      }

      // Focus on the pasted div.
      $paste_div.focus();

      // Process paste soon.
      editor.win.setTimeout(_processPaste, 1);
    }

    /**
     * Clean HTML that was pasted from Word.
     */
    function _wordClean (html) {
      // Single item list.
      html = html.replace(
        /<p(.*?)class="?'?MsoListParagraph"?'? ([\s\S]*?)>([\s\S]*?)<\/p>/gi,
        '<ul><li>$3</li></ul>'
      );
      html = html.replace(
        /<p(.*?)class="?'?NumberedText"?'? ([\s\S]*?)>([\s\S]*?)<\/p>/gi,
        '<ol><li>$3</li></ol>'
      );

      // List start.
      html = html.replace(
        /<p(.*?)class="?'?MsoListParagraphCxSpFirst"?'?([\s\S]*?)(level\d)?([\s\S]*?)>([\s\S]*?)<\/p>/gi,
        '<ul><li$3>$5</li>'
      );
      html = html.replace(
        /<p(.*?)class="?'?NumberedTextCxSpFirst"?'?([\s\S]*?)(level\d)?([\s\S]*?)>([\s\S]*?)<\/p>/gi,
        '<ol><li$3>$5</li>'
      );

      // List middle.
      html = html.replace(
        /<p(.*?)class="?'?MsoListParagraphCxSpMiddle"?'?([\s\S]*?)(level\d)?([\s\S]*?)>([\s\S]*?)<\/p>/gi,
        '<li$3>$5</li>'
      );
      html = html.replace(
        /<p(.*?)class="?'?NumberedTextCxSpMiddle"?'?([\s\S]*?)(level\d)?([\s\S]*?)>([\s\S]*?)<\/p>/gi,
        '<li$3>$5</li>'
      );
      html = html.replace(
        /<p(.*?)class="?'?MsoListBullet"?'?([\s\S]*?)(level\d)?([\s\S]*?)>([\s\S]*?)<\/p>/gi,
        '<li$3>$5</li>'
      );

      // List end.
      html = html.replace(
        /<p(.*?)class="?'?MsoListParagraphCxSpLast"?'?([\s\S]*?)(level\d)?([\s\S]*?)>([\s\S]*?)<\/p>/gi,
        '<li$3>$5</li></ul>'
      );
      html = html.replace(
        /<p(.*?)class="?'?NumberedTextCxSpLast"?'?([\s\S]*?)(level\d)?([\s\S]*?)>([\s\S]*?)<\/p>/gi,
        '<li$3>$5</li></ol>'
      );

      // Clean list bullets.
      html = html.replace(/<span([^<]*?)style="?'?mso-list:Ignore"?'?([\s\S]*?)>([\s\S]*?)<span/gi, '<span><span');

      // Webkit clean list bullets.
      html = html.replace(/<!--\[if \!supportLists\]-->([\s\S]*?)<!--\[endif\]-->/gi, '');
      html = html.replace(/<!\[if \!supportLists\]>([\s\S]*?)<!\[endif\]>/gi, '');

      // Remove mso classes.
      html = html.replace(/(\n|\r| class=(")?Mso[a-zA-Z0-9]+(")?)/gi, ' ');

      // Remove comments.
      html = html.replace(/<!--[\s\S]*?-->/gi, '');

      // Remove tags but keep content.
      html = html.replace(/<(\/)*(meta|link|span|\\?xml:|st1:|o:|font)(.*?)>/gi, '');

      // Remove no needed tags.
      var word_tags = ['style', 'script', 'applet', 'embed', 'noframes', 'noscript'];
      for (var i = 0; i < word_tags.length; i++) {
        var regex = new RegExp('<' + word_tags[i] + '.*?' + word_tags[i] + '(.*?)>', 'gi');
        html = html.replace(regex, '');
      }

      // Remove spaces.
      html = html.replace(/&nbsp;/gi, ' ');

      // Keep empty TH and TD.
      html = html.replace(/<td([^>]*)><\/td>/g, '<td$1><br></td>');
      html = html.replace(/<th([^>]*)><\/th>/g, '<th$1><br></th>');

      // Remove empty tags.
      var oldHTML;
      do {
        oldHTML = html;
        html = html.replace(/<[^\/>][^>]*><\/[^>]+>/gi, '');
      } while (html != oldHTML);

      // Process list indentation.
      html = html.replace(/<lilevel([^1])([^>]*)>/gi, '<li data-indent="true"$2>');
      html = html.replace(/<lilevel1([^>]*)>/gi, '<li$1>');

      // Clean HTML.
      html = editor.clean.html(html, editor.opts.pasteDeniedTags, editor.opts.pasteDeniedAttrs);

      // Clean empty links.
      html = html.replace(/<a>(.[^<]+)<\/a>/gi, '$1');

      // Process list indent.
      var $div = $('<div>').html(html);
      $div.find('li[data-indent]').each (function (index, li) {
        var $li = $(li);
        if ($li.prev('li').length > 0) {
          var $list = $li.prev('li').find('> ul, > ol');
          if ($list.length === 0) {
            $list = $('ul');
            $li.prev('li').append($list);
          }

          $list.append(li);
        }
        else {
          $li.removeAttr('data-indent');
        }
      });

      editor.html.cleanBlankSpaces($div.get(0));

      html = $div.html();

      return html;
    }

    /**
     * Plain clean.
     */
    function _plainPasteClean (html) {
      var $div = $('<div>').html(html);

      $div.find('p, div, h1, h2, h3, h4, h5, h6, pre, blockquote').each (function (i, el) {
        $(el).replaceWith('<' + (editor.html.defaultTag() || 'DIV') + '>' + $(el).html() + '</' + (editor.html.defaultTag() || 'DIV') + '>');
      });

      // Remove with the content.
      $($div.find('*').not('p, div, h1, h2, h3, h4, h5, h6, pre, blockquote, ul, ol, li, table, tbody, thead, tr, td, br').get().reverse()).each (function () {
        $(this).replaceWith($(this).html());
      });

      // Remove comments.
      var cleanComments = function (node) {
        var contents = editor.node.contents(node);

        for (var i = 0; i < contents.length; i++) {
          if (contents[i].nodeType != 3 && contents[i].nodeType != 1) {
            $(contents[i]).remove();
          }
          else {
            cleanComments(contents[i]);
          }
        }
      };

      cleanComments($div.get(0));

      return $div.html();
    }

    /**
     * Process the pasted HTML.
     */
    function _processPaste () {
      editor.keys.forceUndo();
      var snapshot = editor.snapshot.get();

      if (clipboard_html === null) {
        clipboard_html = $paste_div.html();

        editor.selection.restore();
        editor.events.enableBlur();
      }

      var response = editor.events.chainTrigger('paste.beforeCleanup', clipboard_html);
      if (typeof(response) === 'string') {
        clipboard_html = response;
      }

      // Keep only body if there is.
      if (clipboard_html.indexOf('<body') >= 0) {
        clipboard_html = clipboard_html.replace(/[.\s\S\w\W<>]*<body[^>]*>([.\s\S\w\W<>]*)<\/body>[.\s\S\w\W<>]*/g, '$1');
      }

      // Google Docs paste.
      if (clipboard_html.indexOf('id="docs-internal-guid') >= 0) {
        clipboard_html = clipboard_html.replace(/^.* id="docs-internal-guid[^>]*>(.*)<\/b>.*$/, '$1');
      }

      // Word paste.
      if (clipboard_html.match(/(class=\"?Mso|style=\"[^\"]*\bmso\-|w:WordDocument)/gi)) {
        // Strip spaces at the beginning.
        clipboard_html = clipboard_html.replace(/^\n*/g, '').replace(/^ /g, '');

        // Firefox paste.
        if (clipboard_html.indexOf('<colgroup>') === 0) {
          clipboard_html = '<table>' + clipboard_html + '</table>';
        }

        clipboard_html = _wordClean(clipboard_html);
        clipboard_html = _removeEmptyTags(clipboard_html);
      }

      // Paste.
      else {
        editor.opts.htmlAllowComments = false;
        clipboard_html = editor.clean.html(clipboard_html, editor.opts.pasteDeniedTags, editor.opts.pasteDeniedAttrs);
        editor.opts.htmlAllowComments = true;
        clipboard_html = _removeEmptyTags(clipboard_html);

        clipboard_html = clipboard_html.replace(/\r|\n|\t/g, '');

        if ($.FE.copied_text && $('<div>').html(clipboard_html).text().replace(/(\u00A0)/gi, ' ').replace(/\r|\n/gi, '') == $.FE.copied_text.replace(/(\u00A0)/gi, ' ').replace(/\r|\n/gi, '')) {
          clipboard_html = $.FE.copied_html;
        }

        clipboard_html = clipboard_html.replace(/^ */g, '').replace(/ *$/g, '');
      }

      // Do plain paste cleanup.
      if (editor.opts.pastePlain) {
        clipboard_html = _plainPasteClean(clipboard_html);
      }

      // After paste cleanup event.
      response = editor.events.chainTrigger('paste.afterCleanup', clipboard_html);
      if (typeof(response) === 'string') {
        clipboard_html = response;
      }

      // Check if there is anything to clean.
      if (clipboard_html !== '') {
        // Normalize spaces.
        var $tmp = $('<div>').html(clipboard_html);
        editor.html.normalizeSpaces($tmp.get(0));
        $tmp.find('span').each (function () {
          if (this.attributes.length == 0) {
            $(this).replaceWith(this.innerHTML);
          }
        })

        // Remove unecessary new_lines.
        $tmp.find('br').each (function () {
          if (this.previousSibling && editor.node.isBlock(this.previousSibling)) {
            $(this).remove();
          }
        })

        clipboard_html = $tmp.html();

        // Insert HTML.
        editor.html.insert(clipboard_html, true);
      }

      _afterPaste();

      editor.undo.saveStep(snapshot);
      editor.undo.saveStep();
    }

    /**
     * After pasting.
     */
    function _afterPaste () {
      editor.events.trigger('paste.after');
    }

    /**
     * Remove possible empty tags in pasted HTML.
     */
    function _removeEmptyTags (html) {
      var i;
      var $div = $('<div>').html(html);

      // Clean empty tags.
      var empty_tags = $div.find('*:empty:not(br, img, td, th)');
      while (empty_tags.length) {
        for (i = 0; i < empty_tags.length; i++) {
          $(empty_tags[i]).remove();
        }

        empty_tags = $div.find('*:empty:not(br, img, td, th)');
      }

      // Workaround for Nodepad paste.
      var divs = $div.find('> div:not([style]), td > div, th > div, li > div');
      while (divs.length) {
        var $dv = $(divs[divs.length - 1]);
        $dv.replaceWith($dv.html() + '<br>');
        divs = $div.find('> div:not([style]), td > div, th > div, li > div');
      }

      // Remove divs.
      divs = $div.find('div:not([style])');
      while (divs.length) {
        for (i = 0; i < divs.length; i++) {
          var $el = $(divs[i]);
          var text = $el.html().replace(/\u0009/gi, '').trim();

          $el.replaceWith(text);
        }

        divs = $div.find('div:not([style])');
      }

      return $div.html();
    }

    /**
     * Initialize.
     */
    function _init () {
      editor.events.on('copy', _handleCopy);
      editor.events.on('cut', _handleCopy);
      editor.events.on('paste', _handlePaste);

      if (editor.browser.msie && editor.browser.version < 11) {
        editor.events.on('mouseup', function (e) {
          if (e.button == 2) {
            setTimeout(function () {
              stop_paste = false;
            }, 50);
            stop_paste = true;
          }
        }, true)

        editor.events.on('beforepaste', _handlePaste);
      }
    }

    return {
      _init: _init
    }
  };


  $.FE.MODULES.tooltip = function (editor) {
    function hide () {
      if (editor.$tooltip) editor.$tooltip.removeClass('fr-visible').css('left', '-3000px');
    }

    function to ($el, above) {
      if (!$el.data('title')) {
        $el.data('title', $el.attr('title'));
      }

      if (!$el.data('title')) return false;

      if (!editor.$tooltip) _init();

      $el.removeAttr('title');
      editor.$tooltip.text($el.data('title'));
      editor.$tooltip.addClass('fr-visible');

      var left = $el.offset().left + ($el.outerWidth() - editor.$tooltip.outerWidth()) / 2;

      // Normalize screen position.
      if (left < 0) left = 0;
      if (left + editor.$tooltip.outerWidth() > $(editor.o_win).width()) {
        left = $(editor.o_win).width() - editor.$tooltip.outerWidth();
      }

      if (typeof above == 'undefined') above = editor.opts.toolbarBottom;
      var top = !above ? $el.offset().top + $el.outerHeight() : $el.offset().top - editor.$tooltip.height();

      editor.$tooltip.css('left', left);
      editor.$tooltip.css('top', top);
    }

    function bind ($el, selector, above) {
      if (!editor.helpers.isMobile()) {
        editor.events.$on($el, 'mouseenter', selector, function (e) {
          if (!$(e.currentTarget).hasClass('fr-disabled')) {
            to($(e.currentTarget), above);
          }
        }, true);

        editor.events.$on($el, 'mouseleave ' + editor._mousedown + ' ' + editor._mouseup, selector, function (e) {
          hide();
        }, true);
      }
    }

    function _init () {
      if (!editor.helpers.isMobile()) {
        if (!editor.shared.$tooltip) {
          editor.shared.$tooltip = $('<div class="fr-tooltip"></div>');

          editor.$tooltip = editor.shared.$tooltip;

          if (editor.opts.theme) {
            editor.$tooltip.addClass(editor.opts.theme + '-theme');
          }

          $(editor.o_doc).find('body').append(editor.$tooltip);
        }
        else {
          editor.$tooltip = editor.shared.$tooltip;
        }

        editor.events.on('shared.destroy', function () {
          editor.$tooltip.html('').removeData().remove();
        }, true);
      }
    }

    return {
      hide: hide,
      to: to,
      bind: bind
    }
  };


  $.FE.ICON_DEFAULT_TEMPLATE = 'font_awesome';

  $.FE.ICON_TEMPLATES = {
    font_awesome: '<i class="fa fa-[NAME]"></i>',
    text: '<span style="text-align: center;">[NAME]</span>',
    image: '<img src=[SRC] alt=[ALT] />'
  }

  $.FE.ICONS = {
    bold: {NAME: 'bold'},
    italic: {NAME: 'italic'},
    underline: {NAME: 'underline'},
    strikeThrough: {NAME: 'strikethrough'},
    subscript: {NAME: 'subscript'},
    superscript: {NAME: 'superscript'},
    color: {NAME: 'tint'},
    outdent: {NAME: 'outdent'},
    indent: {NAME: 'indent'},
    undo: {NAME: 'rotate-left'},
    redo: {NAME: 'rotate-right'},
    insertHR: {NAME: 'minus'},
    clearFormatting: {NAME: 'eraser'},
    selectAll: {NAME: 'mouse-pointer'}
  }

  $.FE.DefineIconTemplate = function (name, options) {
    $.FE.ICON_TEMPLATES[name] = options;
  }

  $.FE.DefineIcon = function (name, options) {
    $.FE.ICONS[name] = options;
  }

  $.FE.MODULES.icon = function (editor) {
    function create (command) {
      var icon = null;
      var info = $.FE.ICONS[command];
      if (typeof info != 'undefined') {
        var template = info.template || $.FE.ICON_DEFAULT_TEMPLATE;
        if (template && (template = $.FE.ICON_TEMPLATES[template])) {
          icon = template.replace(/\[([a-zA-Z]*)\]/g, function (str, a1) {
            return (a1 == 'NAME' ? (info[a1] || command) : info[a1]);
          });
        }
      }

      return (icon || command);
    }

    return {
      create: create
    }
  };


  $.FE.MODULES.button = function (editor) {
    var buttons = [];

    if (editor.opts.toolbarInline || editor.opts.toolbarContainer) {
      if (!editor.shared.buttons) editor.shared.buttons = [];
      buttons = editor.shared.buttons;
    }

    /**
     * Click was made on a dropdown button.
     */
    function _dropdownButtonClick (e) {
      // Get current btn and dropdown.
      var $btn = $(e.currentTarget);
      var $dropdown = $btn.next();

      var active = $btn.hasClass('fr-active');
      var mobile = editor.helpers.isMobile();

      var $active_dropdowns = $('.fr-dropdown.fr-active').not($btn);

      var inst = $btn.parents('.fr-toolbar, .fr-popup').data('instance') || editor;

      // Hide keyboard. We need the entire space.
      if (inst.helpers.isIOS() && inst.find('.fr-marker').length == 0) {
        inst.save();
        inst.clear();
        inst.restore();
      }

      // Dropdown is not active.
      if (!active) {
        // Call refresh on show.
        var cmd = $btn.data('cmd');
        $dropdown.find('.fr-command').removeClass('fr-active');
        if ($.FE.COMMANDS[cmd] && $.FE.COMMANDS[cmd].refreshOnShow) {
          $.FE.COMMANDS[cmd].refreshOnShow.apply(inst, [$btn, $dropdown]);
        }

        $dropdown.css('left', $btn.offset().left - $btn.parent().offset().left - (editor.opts.direction == 'rtl' ? $dropdown.width() - $btn.outerWidth() : 0));

        if (!editor.opts.toolbarBottom) {
          $dropdown.css('top', $btn.position().top + $btn.outerHeight());
        }
        else {
          $dropdown.css('bottom', editor.$tb.height() - $btn.position().top);
        }
      }

      // Blink and activate.
      $btn.addClass('fr-blink').toggleClass('fr-active');
      setTimeout (function () {
        $btn.removeClass('fr-blink');
      }, 300);

      // Check if it exceeds window on the right.
      if ($dropdown.offset().left + $dropdown.outerWidth() > $(editor.opts.scrollableContainer).offset().left +  $(editor.opts.scrollableContainer).outerWidth()) {
        $dropdown.css('margin-left', -($dropdown.offset().left + $dropdown.outerWidth() - $(editor.opts.scrollableContainer).offset().left - $(editor.opts.scrollableContainer).outerWidth()))
      }

      // Hide dropdowns that might be active.
      $active_dropdowns.removeClass('fr-active');
      $active_dropdowns.parent('.fr-toolbar').css('zIndex', '');

      if ($btn.parents('.fr-popup').length == 0) {
        if ($btn.hasClass('fr-active')) {
          editor.$tb.css('zIndex', 5);
        }
        else {
          editor.$tb.css('zIndex', '');
        }
      }
    }

    function exec ($btn) {
      // Blink.
      $btn.addClass('fr-blink');
      setTimeout (function () {
        $btn.removeClass('fr-blink');
      }, 500);

      // Get command, value and additional params.
      var cmd = $btn.data('cmd');
      var params = [];
      while (typeof $btn.data('param' + (params.length + 1)) != 'undefined') {
        params.push($btn.data('param' + (params.length + 1)));
      }

      // Hide dropdowns that might be active including the current one.
      var $active_dropdowns = $('.fr-dropdown.fr-active');
      if ($active_dropdowns.length) {
        $active_dropdowns.removeClass('fr-active');

        $active_dropdowns.parent('.fr-toolbar').css('zIndex', '');
      }

      // Call the command.
      $btn.parents('.fr-popup, .fr-toolbar').data('instance').commands.exec(cmd, params);
    }

    /**
     * Click was made on a command button.
     */
    function _commandButtonClick (e) {
      var $btn = $(e.currentTarget);
      exec($btn);
    }

    function _click (e) {
      var $btn = $(e.currentTarget);

      var inst = $btn.parents('.fr-popup, .fr-toolbar').data('instance');

      if ($btn.parents('.fr-popup').length == 0 && !$btn.data('popup')) {
        inst.popups.hideAll();
      }

      // Popups are visible, but not in the current instance.
      if (inst.popups.areVisible() && !inst.popups.areVisible(inst)) {
        // Hide markers in other instances.
        for (var i = 0; i < $.FE.INSTANCES.length; i++) {
          if ($.FE.INSTANCES[i] != inst) {
            $.FE.INSTANCES[i].$el.find('.fr-marker').remove();
          }
        }

        inst.popups.hideAll();
      }

      // Dropdown button.
      if ($btn.hasClass('fr-dropdown')) {
        _dropdownButtonClick(e);
      }

      // Regular button.
      else {
        _commandButtonClick(e);

        if ($.FE.COMMANDS[$btn.data('cmd')] && $.FE.COMMANDS[$btn.data('cmd')].refreshAfterCallback != false) {
          inst.button.bulkRefresh();
        }
      }
    }

    function _hideActiveDropdowns ($el) {
      var $active_dropdowns = $el.find('.fr-dropdown.fr-active');

      if ($active_dropdowns.length) {
        $active_dropdowns.removeClass('fr-active');

        $active_dropdowns.parent('.fr-toolbar').css('zIndex', '');
      }
    }

    /**
     * Click in the dropdown menu.
     */
    function _dropdownMenuClick (e) {
      e.preventDefault();
      e.stopPropagation();
    }

    /**
     * Click on the dropdown wrapper.
     */
    function _dropdownWrapperClick (e) {
      e.stopPropagation();

      if (editor.opts.toolbarInline) return false;
    }

    /**
     * Bind callbacks for commands.
     */
    function bindCommands ($el, tooltipAbove) {
      editor.events.bindClick($el, '.fr-command:not(.fr-disabled)', _click);

      // Click on the dropdown menu.
      editor.events.$on($el, editor._mousedown + ' ' + editor._mouseup + ' ' + editor._move, '.fr-dropdown-menu', _dropdownMenuClick, true);

      // Click on the dropdown wrapper.
      editor.events.$on($el, editor._mousedown + ' ' + editor._mouseup + ' ' + editor._move, '.fr-dropdown-menu .fr-dropdown-wrapper', _dropdownWrapperClick, true);

      // Hide dropdowns that might be active.
      var _document = $el.get(0).ownerDocument;
      var _window = 'defaultView' in _document ? _document.defaultView : _document.parentWindow;
      var hideDropdowns = function (e) {
        if (!e || (e.type == editor._mouseup && e.target != $('html').get(0)) || (e.type == 'keydown' && ((editor.keys.isCharacter(e.which) && !editor.keys.ctrlKey(e)) || e.which == $.FE.KEYCODE.ESC))) {
          _hideActiveDropdowns($el);
        }
      }
      editor.events.$on($(_window), editor._mouseup + ' resize keydown', hideDropdowns, true);

      if (editor.opts.iframe) {
        editor.events.$on(editor.$win, editor._mouseup, hideDropdowns, true);
      }

      // Add refresh.
      $.merge(buttons, $el.find('.fr-btn').toArray());

      // Assing tooltips to buttons.
      editor.tooltip.bind($el, '.fr-btn, .fr-title', tooltipAbove);
    }

    /**
     * Create the content for dropdown.
     */
    function _content (command, info) {
      var c = '';

      if (info.html) {
        if (typeof info.html == 'function') {
          c += info.html.call(editor);
        }
        else {
          c += info.html;
        }
      }
      else {
        var options = info.options;
        if (typeof options == 'function') options = options();

        c += '<ul class="fr-dropdown-list">';
        for (var val in options) {
          c += '<li><a class="fr-command" data-cmd="' + command + '" data-param1="' + val + '" title="' + options[val] + '">' + editor.language.translate(options[val]) + '</a></li>';
        }
        c += '</ul>';
      }

      return c;
    }

    /**
     * Create button.
     */
    function _build (command, info, visible) {
      var display_selection = info.displaySelection;
      if (typeof display_selection == 'function') {
        display_selection = display_selection(editor);
      }

      var icon;
      if (display_selection) {
        var default_selection = (typeof info.defaultSelection == 'function' ? info.defaultSelection(editor) : info.defaultSelection);
        icon = '<span style="width:' + (info.displaySelectionWidth || 100) + 'px">' + (default_selection || editor.language.translate(info.title)) + '</span>';
      }
      else {
        icon = editor.icon.create(info.icon || command)
      }

      var popup = info.popup ? ' data-popup="true"' : '';

      var btn = '<button type="button" tabindex="-1" title="' + (editor.language.translate(info.title) || '') + '" class="fr-command fr-btn' + (info.type == 'dropdown' ? ' fr-dropdown' : '') + (info.back ? ' fr-back' : '') + (info.disabled ? ' fr-disabled' : '') + (!visible ? ' fr-hidden' : '') + '" data-cmd="' + command + '"' + popup + '>' + icon + '</button>';

      if (info.type == 'dropdown') {
        // Build dropdown.
        var dropdown = '<div class="fr-dropdown-menu"><div class="fr-dropdown-wrapper"><div class="fr-dropdown-content" tabindex="-1">';

        dropdown += _content(command, info);

        dropdown += '</div></div></div>';

        btn += dropdown;
      }

      return btn;
    }

    function buildList (buttons, visible_buttons) {
      var str = '';
      for (var i = 0; i < buttons.length; i++) {
        var cmd_name = buttons[i];
        var cmd_info = $.FE.COMMANDS[cmd_name];

        if (cmd_info && typeof cmd_info.plugin !== 'undefined' && editor.opts.pluginsEnabled.indexOf(cmd_info.plugin) < 0) continue;

        if (cmd_info) {
          var visible = typeof visible_buttons != 'undefined' ? visible_buttons.indexOf(cmd_name) >= 0 : true;
          str += _build(cmd_name, cmd_info, visible);
        }
        else if (cmd_name == '|') {
          str += '<div class="fr-separator fr-vs"></div>';
        }
        else if (cmd_name == '-') {
          str += '<div class="fr-separator fr-hs"></div>';
        }
      }

      return str;
    }

    function refresh ($btn) {
      var inst = editor.$tb ? (editor.$tb.data('instance') || editor) : editor;

      var cmd = $btn.data('cmd');

      var $dropdown;
      if (!$btn.hasClass('fr-dropdown')) $btn.removeClass('fr-active');
      else $dropdown = $btn.next();

      if ($.FE.COMMANDS[cmd] && $.FE.COMMANDS[cmd].refresh) {
       $.FE.COMMANDS[cmd].refresh.apply(inst, [$btn, $dropdown]);
      }
      else if (editor.refresh[cmd]) {
        inst.refresh[cmd]($btn, $dropdown);
      }
      else {
        inst.refresh.default($btn, cmd);
      }
    }

    /**
     * Do buttons refresh.
     */
    function bulkRefresh () {
      var inst = editor.$tb ? (editor.$tb.data('instance') || editor) : editor;

      // Check the refresh event.
      if (editor.events.trigger('buttons.refresh') == false) return true;

      setTimeout(function () {
        var focused = (inst.selection.inEditor() && inst.core.hasFocus());

        for (var i = 0; i < buttons.length; i++) {
          var $btn = $(buttons[i]);
          var cmd = $btn.data('cmd');
          if ($btn.parents('.fr-popup').length == 0) {
            if (focused || ($.FE.COMMANDS[cmd] && $.FE.COMMANDS[cmd].forcedRefresh)) {
              inst.button.refresh($btn);
            }
            else {
              if (!$btn.hasClass('fr-dropdown')) $btn.removeClass('fr-active');
            }
          }
          else if ($btn.parents('.fr-popup').is(':visible')) {
            inst.button.refresh($btn);
          }
        }
      }, 0);
    }

    /**
     * Initialize.
     */
    function _init () {
      // Assign refresh and do refresh.
      if (editor.opts.toolbarInline) {
        editor.events.on('toolbar.show', bulkRefresh);
      }
      else {
        editor.events.on('mouseup', bulkRefresh);
        editor.events.on('keyup', bulkRefresh);
        editor.events.on('blur', bulkRefresh);
        editor.events.on('focus', bulkRefresh);
        editor.events.on('contentChanged', bulkRefresh);
      }
    }

    return {
      _init: _init,
      buildList: buildList,
      bindCommands: bindCommands,
      refresh: refresh,
      bulkRefresh: bulkRefresh,
      exec: exec
    }
  };


  $.FE.MODULES.position = function (editor) {
    /**
    * Get bounding rect around selection.
    *
    */
    function getBoundingRect () {
      var boundingRect;

      var range = editor.selection.ranges(0);
      if (range && range.collapsed && editor.selection.inEditor()) {
        var remove = false;
        if (editor.$el.find('.fr-marker').length == 0) {
          editor.selection.save();
          remove = true;
        }

        var $marker = editor.$el.find('.fr-marker:first');
        $marker.css('display', 'inline');
        $marker.css('line-height', '');
        var offset = $marker.offset();
        var height = $marker.outerHeight();
        $marker.css('display', 'none');
        $marker.css('line-height', 0);

        boundingRect = {}
        boundingRect.left = offset.left;
        boundingRect.width = 0;
        boundingRect.height = height;
        boundingRect.top = offset.top - $(editor.o_win).scrollTop();
        boundingRect.right = 1;
        boundingRect.bottom = 1;
        boundingRect.ok = true;

        if (remove) editor.selection.restore();
      }
      else if (range) {
        boundingRect = range.getBoundingClientRect();
      }

      return boundingRect;
    }

    /**
     * Normalize top positioning.
     */
    function _topNormalized ($el, top, obj_height) {
      var height = $el.outerHeight();

      if (!editor.helpers.isMobile() && editor.$tb && $el.parent().get(0) != editor.$tb.get(0)) {
        // 1. Parent offset + toolbar top + toolbar height > document height.
        // 2. Selection doesn't go above the screen.
        var p_height = $el.parent().height() - 20 - (editor.opts.toolbarBottom ? editor.$tb.outerHeight() : 0);
        var p_offset = $el.parent().offset().top;
        var new_top = top - height - (obj_height || 0);

        if ($el.parent().get(0) == $(editor.opts.scrollableContainer).get(0)) p_offset = p_offset - $el.parent().position().top;

        if (p_offset + top + height > $(editor.o_doc).outerHeight() && $el.parent().offset().top + new_top > 0) {
          top = new_top;
          $el.addClass('fr-above');
        }
        else {
          $el.removeClass('fr-above');
        }
      }

      return top;
    }

    /**
     * Normalize left position.
     */
    function _leftNormalized ($el, left) {
      var width = $el.outerWidth();

      // Normalize right.
      if ($el.parent().offset().left + left + width > $(editor.opts.scrollableContainer).width() - 10) {
       left = $(editor.opts.scrollableContainer).width() - width - 10 - $el.parent().offset().left + $(editor.opts.scrollableContainer).offset().left;
      }

      // Normalize left.
      if ($el.parent().offset().left + left < $(editor.opts.scrollableContainer).offset().left) {
        left = 10 - $el.parent().offset().left + $(editor.opts.scrollableContainer).offset().left;
      }

      return left;
    }

    /**
     * Place editor below selection.
     */
    function forSelection ($el) {
      var selection_rect = getBoundingRect();

      $el.css('top', 0).css('left', 0);

      var top = selection_rect.top + selection_rect.height;
      var left = selection_rect.left + selection_rect.width / 2 - $el.outerWidth() / 2 + $(editor.o_win).scrollLeft();

      if (!editor.opts.iframe) {
        top += $(editor.o_win).scrollTop();
      }

      at(left, top, $el, selection_rect.height);
    }

    /**
     * Position element at the specified position.
     */
    function at (left, top, $el, obj_height) {
      var $container = $el.data('container');

      if ($container && $container.get(0).tagName != 'BODY') {
        if (left) left -= $container.offset().left;
        if (top) top -= $container.offset().top - $container.scrollTop();
      }

      // Apply iframe correction.
      if (editor.opts.iframe && $container && editor.$tb && $container.get(0) != editor.$tb.get(0)) {
        if (left) left += editor.$iframe.offset().left;
        if (top) top += editor.$iframe.offset().top;
      }

      var new_left = _leftNormalized($el, left);

      if (left) {
        // Set the new left.
        $el.css('left', new_left);

        // Normalize arrow.
        var $arrow = $el.find('.fr-arrow');
        if (!$arrow.data('margin-left')) $arrow.data('margin-left', editor.helpers.getPX($arrow.css('margin-left')));
        $arrow.css('margin-left', left - new_left + $arrow.data('margin-left'));
      }

      if (top) $el.css('top', _topNormalized($el, top, obj_height));
    }

    /**
     * Special case for update sticky on iOS.
     */
    function _updateIOSSticky (el) {
      var $el = $(el);
      var is_on = $el.is('.fr-sticky-on');
      var prev_top = $el.data('sticky-top');
      var scheduled_top = $el.data('sticky-scheduled');

      // Create a dummy div that we show then sticky is on.
			if (typeof prev_top == 'undefined') {
        $el.data('sticky-top', 0);
        var $dummy = $('<div class="fr-sticky-dummy" style="height: ' + $el.outerHeight() + 'px;"></div>');
			  editor.$box.prepend($dummy);
			}

      // Position sticky doesn't work when the keyboard is on the screen.
      if (editor.core.hasFocus() || editor.$tb.find('input:visible:focus').length > 0) {
        // Get the current scroll.
        var x_scroll = $(window).scrollTop();

        // Get the current top.
        // We make sure that we keep it within the editable box.
        var x_top = Math.min(Math.max(x_scroll - editor.$tb.parent().offset().top, 0), editor.$tb.parent().outerHeight() - $el.outerHeight());

        // Not the same top and different than the already scheduled.
        if (x_top != prev_top && x_top != scheduled_top) {
          // Clear any too soon change to avoid flickering.
          clearTimeout($el.data('sticky-timeout'));

          // Store the current scheduled top.
          $el.data('sticky-scheduled', x_top);

          // Hide the toolbar for a rich experience.
          if ($el.outerHeight() < x_scroll - editor.$tb.parent().offset().top) {
            $el.addClass('fr-opacity-0');
          }

          // Set the timeout for changing top.
          // Based on the test 100ms seems to be the best timeout.
          $el.data('sticky-timeout', setTimeout(function () {
            // Get the current top.
            var c_scroll = $(window).scrollTop();
            var c_top = Math.min(Math.max(c_scroll - editor.$tb.parent().offset().top, 0), editor.$tb.parent().outerHeight() - $el.outerHeight());

            if (c_top > 0 && editor.$tb.parent().get(0).tagName == 'BODY') c_top += editor.$tb.parent().position().top;

            // Don't update if it is not different than the prev top.
            if (c_top != prev_top) {
              $el.css('top', Math.max(c_top, 0));

              if (editor.$tb.hasClass('fr-inline')) {
                $el.css('top', x_scroll);
              }

              $el.data('sticky-top', c_top);
              $el.data('sticky-scheduled', c_top);
            }

            // Show toolbar.
            $el.removeClass('fr-opacity-0');

            if (editor.$tb.hasClass('fr-inline')) editor.toolbar.show();
          }, 100));
        }

        // Turn on sticky mode.
        if (!is_on) {
          $el.css('top', '0');
          $el.width(editor.$tb.parent().width());
          $el.addClass('fr-sticky-on');
          editor.$box.addClass('fr-sticky-box');
        }
      }

      // Turn off sticky mode.
      else {
        clearTimeout($(el).css('sticky-timeout'));
        $el.css('top', '0');
        $el.css('position', '');
        $el.width('');
        $el.data('sticky-top', 0);
        $el.removeClass('fr-sticky-on');
        editor.$box.removeClass('fr-sticky-box');

        if (editor.$tb.hasClass('fr-inline')) editor.toolbar.hide();
      }
    }

    /**
     * Update sticky location for browsers that don't support sticky.
     * https://github.com/filamentgroup/fixed-sticky
     *
     * The MIT License (MIT)
     *
     * Copyright (c) 2013 Filament Group
     */
    function _updateSticky (el) {
      if( !el.offsetWidth ) { return; }

			var el_top;
			var el_bottom;
      var $el = $(el);
      var height = $el.outerHeight();

      var position = $el.data('sticky-position');

      // Viewport height.
      var viewport_height = $(editor.opts.scrollableContainer == 'body' ? editor.o_win : editor.opts.scrollableContainer).outerHeight();

      var scrollable_top = 0;
      var scrollable_bottom = 0;
      if (editor.opts.scrollableContainer !== 'body') {
        scrollable_top = $(editor.opts.scrollableContainer).offset().top;
        scrollable_bottom = $(editor.o_win).outerHeight() - scrollable_top - viewport_height;
      }

      var offset_top = editor.opts.scrollableContainer == 'body' ? $(editor.o_win).scrollTop() : scrollable_top;

			var is_on = $el.is('.fr-sticky-on');

      // Decide parent.
      if (!$el.data('sticky-parent')) {
        $el.data('sticky-parent', $el.parent());
      }
      var $parent = $el.data('sticky-parent');
		  var parent_top = $parent.offset().top;
			var parent_height = $parent.outerHeight();


			if (!$el.data('sticky-offset')) {
				$el.data('sticky-offset', true);
				$el.after('<div class="fr-sticky-dummy" style="height: ' + height + 'px;"></div>');
			}

      // Detect position placement.
      if (!position) {
				// Some browsers require fixed/absolute to report accurate top/left values.
				var skip_setting_fixed = $el.css('top') !== 'auto' || $el.css('bottom') !== 'auto';

        // Set to position fixed for a split of second.
				if(!skip_setting_fixed) {
					$el.css('position', 'fixed');
				}

        // Find position.
				position = {
					top: $el.css('top') !== 'auto',
					bottom: $el.css('bottom') !== 'auto'
				};

        // Remove position fixed.
				if(!skip_setting_fixed) {
					$el.css('position', '');
				}

        // Store position.
				$el.data('sticky-position', position);

        $el.data('top', $el.css('top'));
        $el.data('bottom', $el.css('bottom'));
  		}

      // Detect if is OK to fix at the top.
			var isFixedToTop = function () {
				// 1. Top condition.
        // 2. Bottom condition.
				return parent_top <  offset_top + el_top &&
                parent_top + parent_height - height >= offset_top + el_top;
			}

      // Detect if it is OK to fix at the bottom.
			var isFixedToBottom = function () {
				return parent_top + height < offset_top + viewport_height - el_bottom &&
        				parent_top + parent_height > offset_top + viewport_height - el_bottom ;
			}

			el_top = editor.helpers.getPX($el.data('top'));
			el_bottom = editor.helpers.getPX($el.data('bottom'));

      var at_top = (position.top && isFixedToTop());
      var at_bottom = (position.bottom && isFixedToBottom());

      // Should be fixed.
			if (at_top || at_bottom) {
        $el.css('width', $parent.width() + 'px');

        if (!is_on) {
					$el.addClass('fr-sticky-on')
          $el.removeClass('fr-sticky-off');

          if ($el.css('top')) {
            if ($el.data('top') != 'auto') {
              $el.css('top', editor.helpers.getPX($el.data('top')) + scrollable_top);
            }
            else {
              $el.data('top', 'auto');
            }
          }

          if ($el.css('bottom')) {
            if ($el.data('bottom') != 'auto') {
              $el.css('bottom', editor.helpers.getPX($el.data('bottom')) + scrollable_bottom);
            }
            else {
              $el.css('bottom', 'auto');
            }
          }
				}
			}

      // Shouldn't be fixed.
      else {
				if (!$el.hasClass('fr-sticky-off')) {
          // Reset.
          $el.width('');
          $el.removeClass('fr-sticky-on');
          $el.addClass('fr-sticky-off');

          if ($el.css('top') && $el.css('top') != 'auto') {
            $el.css('top', 0);
          }
          if ($el.css('bottom')) $el.css('bottom', 0);
				}
      }
    }

   /**
    * Test if browser supports sticky.
    * https://github.com/filamentgroup/fixed-sticky
    *
    * The MIT License (MIT)
    *
    * Copyright (c) 2013 Filament Group
    */
    function _testSticky () {
      var el = document.createElement('test');
  		var mStyle = el.style;

			mStyle.cssText = 'position:' + [ '-webkit-', '-moz-', '-ms-', '-o-', '' ].join('sticky; position:') + ' sticky;';

  		return mStyle['position'].indexOf('sticky') !== -1 && !editor.helpers.isIOS() && !editor.helpers.isAndroid();
    }

    /**
     * Initialize sticky position.
     */
    function _initSticky () {
      if (!_testSticky()) {
        editor._stickyElements = [];

        // iOS special case.
        if (editor.helpers.isIOS()) {
          // Use an animation frame to make sure we're always OK with the updates.
          var animate = function () {
            editor.helpers.requestAnimationFrame()(animate);

            for (var i = 0; i < editor._stickyElements.length; i++) {
              _updateIOSSticky(editor._stickyElements[i]);
            }
          };
          animate();

          // Hide toolbar on touchmove. This is very useful on iOS versions < 8.
          editor.events.$on($(editor.o_win), 'scroll', function () {
            if (editor.core.hasFocus()) {
              for (var i = 0; i < editor._stickyElements.length; i++) {
                var $el = $(editor._stickyElements[i]);
                var $parent = $el.parent();
                var c_scroll = $(window).scrollTop();

                if ($el.outerHeight() < c_scroll - $parent.offset().top) {
                  $el.addClass('fr-opacity-0');
                  $el.data('sticky-top', -1);
                  $el.data('sticky-scheduled', -1);
                }
              }
            }
          }, true);
        }

        // Default case. Do the updates on scroll.
        else {
          editor.events.$on($(editor.opts.scrollableContainer == 'body' ? editor.o_win : editor.opts.scrollableContainer), 'scroll', refresh, true);
          editor.events.$on($(editor.o_win), 'resize', refresh, true);

          editor.events.on('initialized', refresh);
          editor.events.on('focus', refresh);

          editor.events.$on($(editor.o_win), 'resize', 'textarea', refresh, true);
        }
      }
    }

    function refresh () {
      for (var i = 0; i < editor._stickyElements.length; i++) {
        _updateSticky(editor._stickyElements[i]);
      }
    }

    /**
     * Mark element as sticky.
     */
    function addSticky ($el) {
      $el.addClass('fr-sticky');

      if (editor.helpers.isIOS()) $el.addClass('fr-sticky-ios');

      if (!_testSticky()) {
        editor._stickyElements.push($el.get(0));
      }
    }

    function _init () {
      _initSticky();
    }

    return {
      _init: _init,
      forSelection: forSelection,
      addSticky: addSticky,
      refresh: refresh,
      at: at,
      getBoundingRect: getBoundingRect
    }
  };


  // Extend defaults.
  $.extend($.FE.DEFAULTS, {
    toolbarBottom: false,
    toolbarButtons: ['fullscreen', 'bold', 'italic', 'underline', 'strikeThrough', 'subscript', 'superscript', 'fontFamily', 'fontSize', '|', 'color', 'emoticons', 'inlineStyle', 'paragraphStyle', '|', 'paragraphFormat', 'align', 'formatOL', 'formatUL', 'outdent', 'indent', 'quote', 'insertHR', '-', 'insertLink', 'insertImage', 'insertVideo', 'insertFile', 'insertTable', 'undo', 'redo', 'clearFormatting', 'selectAll', 'html'],
    toolbarButtonsXS: ['bold', 'italic', 'fontFamily', 'fontSize', '|', 'undo', 'redo'],
    toolbarButtonsSM: ['bold', 'italic', 'underline', '|', 'fontFamily', 'fontSize', 'insertLink', 'insertImage', 'table', '|', 'undo', 'redo'],
    toolbarButtonsMD: ['fullscreen', 'bold', 'italic', 'underline', 'fontFamily', 'fontSize', 'color', 'paragraphStyle', 'paragraphFormat', 'align', 'formatOL', 'formatUL', 'outdent', 'indent', 'quote', 'insertHR', '-', 'insertLink', 'insertImage', 'insertVideo', 'insertFile', 'insertTable', 'undo', 'redo', 'clearFormatting'],
    toolbarContainer: null,
    toolbarInline: false,
    toolbarSticky: true,
    toolbarStickyOffset: 0,
    toolbarVisibleWithoutSelection: false
  });

  $.FE.MODULES.toolbar = function (editor) {
    var _document, _window;

    // Create a button map for each screen size.
    var _buttons_map = [];
    _buttons_map[$.FE.XS] = editor.opts.toolbarButtonsXS || editor.opts.toolbarButtons;
    _buttons_map[$.FE.SM] = editor.opts.toolbarButtonsSM || editor.opts.toolbarButtons;
    _buttons_map[$.FE.MD] = editor.opts.toolbarButtonsMD || editor.opts.toolbarButtons;
    _buttons_map[$.FE.LG] = editor.opts.toolbarButtons;

    function _addOtherButtons (buttons, toolbarButtons) {
      for (var i = 0; i < toolbarButtons.length; i++) {
        if (toolbarButtons[i] != '-' && toolbarButtons[i] != '|' && buttons.indexOf(toolbarButtons[i]) < 0) {
          buttons.push(toolbarButtons[i]);
        }
      }
    }

    /**
     * Add buttons to the toolbar.
     */
    function _addButtons () {
      var _buttons = $.merge([], _screenButtons());
      _addOtherButtons(_buttons, editor.opts.toolbarButtonsXS || []);
      _addOtherButtons(_buttons, editor.opts.toolbarButtonsSM || []);
      _addOtherButtons(_buttons, editor.opts.toolbarButtonsMD || []);
      _addOtherButtons(_buttons, editor.opts.toolbarButtons);

      for (var i = _buttons.length - 1; i >= 0; i--) {
        if (_buttons[i] != '-' && _buttons[i] != '|' && _buttons.indexOf(_buttons[i]) < i) {
          _buttons.splice(i, 1);
        }
      }

      var buttons_list = editor.button.buildList(_buttons, _screenButtons());
      editor.$tb.append(buttons_list);
      editor.button.bindCommands(editor.$tb);
    }

    /**
     * The buttons that should be visible on the current screen size.
     */
    function _screenButtons () {
      var screen_size = editor.helpers.screenSize();
      return _buttons_map[screen_size];
    }

    function _showScreenButtons () {
      var c_buttons = _screenButtons();

      // Remove separator from toolbar.
      editor.$tb.find('.fr-separator').remove();

      // Hide all buttons.
      editor.$tb.find('> .fr-command').addClass('fr-hidden');

      // Reorder buttons.
      for (var i = 0; i < c_buttons.length; i++) {
        if (c_buttons[i] == '|' || c_buttons[i] == '-') {
          editor.$tb.append(editor.button.buildList([c_buttons[i]]));
        }
        else {
          var $btn = editor.$tb.find('> .fr-command[data-cmd="' + c_buttons[i] + '"]');
          var $dropdown = null;
          if ($btn.next().hasClass('fr-dropdown-menu')) $dropdown = $btn.next();

          $btn.removeClass('fr-hidden').appendTo(editor.$tb);
          if ($dropdown) $dropdown.appendTo(editor.$tb);
        }
      }
    }

    /**
     * Set the buttons visibility based on screen size.
     */
    function _setVisibility () {
      editor.events.$on($(editor.o_win), 'resize', _showScreenButtons, true);
      editor.events.$on($(editor.o_win), 'orientationchange', _showScreenButtons, true);
    }

    function showInline (e, force) {
      if (editor.helpers.isMobile()) {
        editor.toolbar.show();
      }
      else {
        setTimeout(function () {
          if (e && e.which == $.FE.KEYCODE.ESC) {
            // Nothing.
          }
          else if (editor.selection.inEditor() && editor.core.hasFocus() && !editor.popups.areVisible()) {
            if ((editor.opts.toolbarVisibleWithoutSelection && e && e.type != 'keyup') || (!editor.selection.isCollapsed() && !editor.keys.isIME()) || force) {
              editor.$tb.data('instance', editor);

              // Check if we should actually show the toolbar.
              if (editor.events.trigger('toolbar.show', [e]) == false) return false;

              if (!editor.helpers.isMobile() && !editor.opts.toolbarContainer) {
                editor.position.forSelection(editor.$tb);
              }

              editor.$tb.show();
            }
          }
        }, 0);
      }
    }

    function hide (e) {
      // Check if we should actually hide the toolbar.
      if (editor.events.trigger('toolbar.hide') == false) return false;

      editor.$tb.hide();
    }

    function show () {
      // Check if we should actually hide the toolbar.
      if (editor.events.trigger('toolbar.show') == false) return false;

      editor.$tb.show();
    }

    /**
     * Set the events for show / hide toolbar.
     */
    function _initInlineBehavior () {
      // Window mousedown.
      editor.events.on('window.mousedown', hide);

      // Element keydown.
      editor.events.on('keydown', hide);

      // Element blur.
      editor.events.on('blur', hide);

      // Window mousedown.
      editor.events.on('window.mouseup', showInline);
      editor.events.on('window.keyup', showInline);

      // Hide editor on ESC.
      editor.events.on('keydown', function (e) {
        if (e && e.which == $.FE.KEYCODE.ESC) {
          hide();
        }
      });

      editor.events.$on(editor.$wp, 'scroll.toolbar', showInline);
      editor.events.on('commands.after', showInline);
    }

    /**
     * Set the events for show / hide toolbar when on mobile.
     */
    function _initInlineMobileBehavior () {
      editor.events.on('focus', showInline, true);

      editor.events.on('blur', hide, true)
    }

    function _initPositioning () {
      // Toolbar is inline.
      if (editor.opts.toolbarInline) {
        // Mobile should handle this as regular.
        if (!editor.helpers.isMobile()) {
          $(editor.opts.scrollableContainer).append(editor.$tb);

          // Add toolbar to body.
          editor.$tb.data('container', $(editor.opts.scrollableContainer));

          // Add inline class.
          editor.$tb.addClass('fr-inline');

          // Add arrow.
          editor.$tb.prepend('<span class="fr-arrow"></span>')

          // Init mouse behavior.
          _initInlineBehavior();

          editor.opts.toolbarBottom = false;
        }
        else {
          if (editor.helpers.isIOS()) {
            $(editor.opts.scrollableContainer).prepend(editor.$tb);
            editor.position.addSticky(editor.$tb);
          }
          else {
            editor.$tb.addClass('fr-bottom');
            editor.$box.append(editor.$tb);
            editor.position.addSticky(editor.$tb);
            editor.opts.toolbarBottom = true;
          }

          editor.$tb.addClass('fr-inline');
          _initInlineMobileBehavior();

          editor.opts.toolbarInline = false;
        }
      }

      // Toolbar is normal.
      else {
        // Won't work on iOS.
        if (editor.opts.toolbarBottom && !editor.helpers.isIOS()) {
          editor.$box.append(editor.$tb);
          editor.$tb.addClass('fr-bottom');
          editor.$box.addClass('fr-bottom');
        }
        else {
          editor.opts.toolbarBottom = false;
          editor.$box.prepend(editor.$tb);
          editor.$tb.addClass('fr-top');
          editor.$box.addClass('fr-top');
        }

        editor.$tb.addClass('fr-basic');

        if (editor.opts.toolbarSticky) {
          if (editor.opts.toolbarStickyOffset) {
            if (editor.opts.toolbarBottom) {
              editor.$tb.css('bottom', editor.opts.toolbarStickyOffset);
            }
            else {
              editor.$tb.css('top', editor.opts.toolbarStickyOffset);
            }
          }

          editor.position.addSticky(editor.$tb);
        }
      }
    }

    /**
     * Destroy.
     */
    function _sharedDestroy () {
      editor.$tb.html('').removeData().remove();
    }

    function _destroy () {
      editor.$box.removeClass('fr-top fr-bottom fr-inline fr-basic');
      editor.$box.find('.fr-sticky-dummy').remove();
    }

    function _setDefaults () {
      if (editor.opts.theme) {
        editor.$tb.addClass(editor.opts.theme + '-theme');
      }

      if (editor.opts.zIndex > 1) {
        editor.$tb.css('z-index', editor.opts.zIndex + 1);
      }

      // Set direction.
      if (editor.opts.direction != 'auto') {
        editor.$tb.removeClass('fr-ltr fr-rtl').addClass('fr-' + editor.opts.direction);
      }

      // Mark toolbar for desktop / mobile.
      if (!editor.helpers.isMobile()) {
        editor.$tb.addClass('fr-desktop');
      }
      else {
        editor.$tb.addClass('fr-mobile');
      }

      // Set the toolbar specific position inline / normal.
      if (!editor.opts.toolbarContainer) {
        _initPositioning();
      }
      else {
        if (editor.opts.toolbarInline) {
          _initInlineBehavior();
          hide();
        }

        if (editor.opts.toolbarBottom) editor.$tb.addClass('fr-bottom');
        else editor.$tb.addClass('fr-top');
      }

      // Set documetn and window for toolbar.
      _document = editor.$tb.get(0).ownerDocument;
      _window = 'defaultView' in _document ? _document.defaultView : _document.parentWindow;

      // Add buttons to the toolbar.
      // Set their visibility for different screens.
      // Asses commands to the butttons.
      _addButtons();
      _setVisibility();

      // Make sure we don't trigger blur.
      editor.events.$on(editor.$tb, editor._mousedown + ' ' + editor._mouseup, function (e) {
        var originalTarget = e.originalEvent ? (e.originalEvent.target || e.originalEvent.originalTarget) : null;
        if (originalTarget && originalTarget.tagName != 'INPUT') {
          e.stopPropagation();
          e.preventDefault();
          return false;
        }
      }, true);
    }

    /**
     * Initialize
     */
    var tb_exists = false;
    function _init () {
      if (!editor.$wp) return false;

      // Container for toolbar.
      if (editor.opts.toolbarContainer) {
        // Shared toolbar.
        if (!editor.shared.$tb) {
          editor.shared.$tb = $('<div class="fr-toolbar"></div>');
          editor.$tb = editor.shared.$tb;
          $(editor.opts.toolbarContainer).append(editor.$tb);
          _setDefaults();
          editor.$tb.data('instance', editor);
        }
        else {
          editor.$tb = editor.shared.$tb;

          if (editor.opts.toolbarInline) _initInlineBehavior();
        }

        // On focus set the current instance.
        editor.events.on('focus', function () {
          editor.$tb.data('instance', editor);
        }, true);

        editor.opts.toolbarInline = false;
      }
      else {
        // Inline toolbar.
        if (editor.opts.toolbarInline) {
          // Update box.
          editor.$box.addClass('fr-inline');

          // Check for shared toolbar.
          if (!editor.shared.$tb) {
            editor.shared.$tb = $('<div class="fr-toolbar"></div>');
            editor.$tb = editor.shared.$tb;
            _setDefaults();
          }
          else {
            editor.$tb = editor.shared.$tb;

            // Init mouse behavior.
            _initInlineBehavior();
          }
        }
        else {
          editor.$box.addClass('fr-basic');
          editor.$tb = $('<div class="fr-toolbar"></div>');
          _setDefaults();

          editor.$tb.data('instance', editor);
        }
      }

      // Destroy.
      editor.events.on('destroy', _destroy, true);
      editor.events.on(!editor.opts.toolbarInline ? 'destroy' : 'shared.destroy', _sharedDestroy, true);
    }

    var disabled = false;
    function disable () {
      if (!disabled && editor.$tb) {
        editor.$tb.find('> .fr-command').addClass('fr-disabled fr-no-refresh');
        disabled = true;
      }
    }

    function enable () {
      if (disabled && editor.$tb) {
        editor.$tb.find('> .fr-command').removeClass('fr-disabled fr-no-refresh');
        disabled = false;
      }

      editor.button.bulkRefresh();
    }

    return {
      _init: _init,
      hide: hide,
      show: show,
      showInline: showInline,
      disable: disable,
      enable: enable
    }
  };


  $.FE.SHORTCUTS_MAP = {
    69: { cmd: 'show' },
    66: { cmd: 'bold' },
    73: { cmd: 'italic' },
    85: { cmd: 'underline' },
    83: { cmd: 'strikeThrough' },
    221: { cmd: 'indent' },
    219: { cmd: 'outdent' },
    90: { cmd: 'undo' },
    '-90': { cmd: 'redo' }
  }

  $.extend($.FE.DEFAULTS, {
    shortcutsEnabled: ['show', 'bold', 'italic', 'underline', 'strikeThrough', 'indent', 'outdent', 'undo', 'redo']
  });

  $.FE.RegisterShortcut = function (key, cmd, val, shift) {
    $.FE.SHORTCUTS_MAP[key * (shift ? -1 : 1)] = {
      cmd: cmd,
      val: val
    }

    $.FE.DEFAULTS.shortcutsEnabled.push(cmd);
  }

  $.FE.MODULES.shortcuts = function (editor) {

    /**
     * Execute shortcut.
     */
    function exec (e) {
      var keycode = e.which;

      // CTRL key.
      // If SHIFT then check for negative.
      // If no SHIFT then check normal.
      if (editor.keys.ctrlKey(e) && ((e.shiftKey && $.FE.SHORTCUTS_MAP[-keycode]) ||  (!e.shiftKey && $.FE.SHORTCUTS_MAP[keycode]))) {
        var cmd = $.FE.SHORTCUTS_MAP[keycode * (e.shiftKey ? -1 : 1)].cmd;

        // Check if shortcut is enabled.
        if (cmd && editor.opts.shortcutsEnabled.indexOf(cmd) >= 0) {
          var val = $.FE.SHORTCUTS_MAP[keycode * (e.shiftKey ? -1 : 1)].val;

          // Search for button.
          var $btn;
          if (cmd && !val) {
            $btn = editor.$tb.find('.fr-command[data-cmd="' + cmd + '"]');
          }
          else if (cmd && val) {
            $btn = editor.$tb.find('.fr-command[data-cmd="' + cmd + '"][data-param1="' + val + '"]');
          }

          // Button found.
          if ($btn.length) {
            e.preventDefault();
            e.stopPropagation();

            if (e.type == 'keydown') {
              editor.button.exec($btn);
            }

            return false;
          }

          // Search for command.
          else if (cmd && editor.commands[cmd]) {
            e.preventDefault();
            e.stopPropagation();

            if (e.type == 'keydown') {
              editor.commands[cmd]();
            }

            return false;
          }
        }
      }
    }

    /**
     * Initialize.
     */
    function _init () {
      editor.events.on('keydown', exec, true)
      editor.events.on('keyup', exec, true)
    }

    return {
      _init: _init
    }
  }


  $.FE.MODULES.snapshot = function (editor) {
    /**
     * Get the index of a node inside it's parent.
     */
    function _getNodeIndex (node) {
      var childNodes = node.parentNode.childNodes;
      var idx = 0;
      var prevNode = null;
      for (var i = 0; i < childNodes.length; i++) {
        if (prevNode) {
          // Current node is text and it is empty.
          var isEmptyText = (childNodes[i].nodeType === Node.TEXT_NODE && childNodes[i].textContent === '');

          // Previous node is text, current node is text.
          var twoTexts = (prevNode.nodeType === Node.TEXT_NODE && childNodes[i].nodeType === Node.TEXT_NODE);

          if (!isEmptyText && !twoTexts) idx++;
        }

        if (childNodes[i] == node) return idx;

        prevNode = childNodes[i];
      }
    }

    /**
     * Determine the location of the node inside the element.
     */
    function _getNodeLocation (node) {
      var loc = [];
      if (!node.parentNode) return [];
      while (!editor.node.isElement(node)) {
        loc.push(_getNodeIndex(node));
        node = node.parentNode;
      }

      return loc.reverse();
    }

    /**
     * Get the range offset inside the node.
     */
    function _getRealNodeOffset (node, offset) {
      while (node && node.nodeType === Node.TEXT_NODE) {
        var prevNode = node.previousSibling;
        if (prevNode && prevNode.nodeType == Node.TEXT_NODE) {
          offset += prevNode.textContent.length;
        }
        node = prevNode;
      }

      return offset;
    }

    /**
     * Codify each range.
     */
    function _getRange (range) {
      return {
        scLoc: _getNodeLocation(range.startContainer),
        scOffset: _getRealNodeOffset(range.startContainer, range.startOffset),
        ecLoc: _getNodeLocation(range.endContainer),
        ecOffset: _getRealNodeOffset(range.endContainer, range.endOffset)
      }
    }

    /**
     * Get the current snapshot.
     */
    function get () {
      var snapshot = {};

      editor.events.trigger('snapshot.before');

      snapshot.html = editor.$el.html();

      snapshot.ranges = [];
      if (editor.selection.inEditor() && editor.core.hasFocus()) {
        var ranges = editor.selection.ranges();
        for (var i = 0; i < ranges.length; i++) {
          snapshot.ranges.push(_getRange(ranges[i]));
        }
      }

      editor.events.trigger('snapshot.after');

      return snapshot;
    }

    /**
     * Determine node by its location in the main element.
     */
    function _getNodeByLocation (loc) {
      var node = editor.$el.get(0);
      for (var i = 0; i < loc.length; i++) {
        node = node.childNodes[loc[i]];
      }

      return node;
    }

    /**
     * Restore range from snapshot.
     */
    function _restoreRange (sel, range_snapshot) {
      try {
        // Get range info.
        var startNode = _getNodeByLocation(range_snapshot.scLoc);
        var startOffset = range_snapshot.scOffset;
        var endNode = _getNodeByLocation(range_snapshot.ecLoc);
        var endOffset = range_snapshot.ecOffset;

        // Restore range.
        var range = editor.doc.createRange();
        range.setStart(startNode, startOffset);
        range.setEnd(endNode, endOffset);

        sel.addRange(range);
      }
      catch (ex) {
        console.warn (ex)
      }
    }

    /**
     * Restore snapshot.
     */
    function restore (snapshot) {
      // Restore HTML.
      if (editor.$el.html() != snapshot.html) editor.$el.html(snapshot.html);

      // Get selection.
      var sel = editor.selection.get();

      // Make sure to clear current selection.
      editor.selection.clear();

      // Focus.
      editor.events.focus(true);

      // Restore Ranges.
      for (var i = 0; i < snapshot.ranges.length; i++) {
        _restoreRange(sel, snapshot.ranges[i]);
      }
    }

    /**
     * Compare two snapshots.
     */
    function equal (s1, s2) {
      if (s1.html != s2.html) return false;
      if (JSON.stringify(s1.ranges) != JSON.stringify(s2.ranges)) return false;

      return true;
    }

    return {
      get: get,
      restore: restore,
      equal: equal
    }
  };


  $.FE.MODULES.undo = function (editor) {
    /**
     * Disable the default browser undo.
     */
    function _disableBrowserUndo (e) {
      var keyCode = e.which;
      var ctrlKey = editor.keys.ctrlKey(e);

      // Ctrl Key.
      if (ctrlKey) {
        if (keyCode == 90 && e.shiftKey) {
          e.preventDefault();
        }

        if (keyCode == 90) {
          e.preventDefault();
        }
      }
    }

    function canDo () {
      if (editor.undo_stack.length === 0 || editor.undo_index <= 1) {
        return false;
      }

      return true;
    }

    function canRedo () {
      if (editor.undo_index == editor.undo_stack.length) {
        return false;
      }

      return true;
    }

    var last_html = null;
    function saveStep (snapshot) {
      if (!editor.undo_stack || editor.undoing) return false;

      dropRedo();

      if (typeof snapshot == 'undefined') {
        snapshot = editor.snapshot.get();

        if (!editor.undo_stack[editor.undo_index - 1] || !editor.snapshot.equal(editor.undo_stack[editor.undo_index - 1], snapshot)) {
          editor.undo_stack.push(snapshot);
          editor.undo_index++;

          if (snapshot.html != last_html) {
            editor.events.trigger('contentChanged');
            last_html = snapshot.html;
          }
        }
      }
      else {
        if (editor.undo_index > 0) {
          editor.undo_stack[editor.undo_index - 1] = snapshot;
        }
        else {
          editor.undo_stack.push(snapshot);
          editor.undo_index++;
        }
      }
    }

    function dropRedo () {
      if (!editor.undo_stack || editor.undoing) return false;

      while (editor.undo_stack.length > editor.undo_index) {
        editor.undo_stack.pop();
      }
    }

    function _do () {
      if (editor.undo_index > 1) {
        editor.undoing = true;

        // Get snapshot.
        var snapshot = editor.undo_stack[--editor.undo_index - 1];

        // Clear any existing content changed timers.
        clearTimeout(editor._content_changed_timer);

        // Restore snapshot.
        editor.snapshot.restore(snapshot);

        // Hide popups.
        editor.popups.hideAll();

        // Enable toolbar.
        editor.toolbar.enable();

        // Call content changed.
        editor.events.trigger('contentChanged');

        editor.events.trigger('commands.undo');

        editor.undoing = false;
      }
    }

    function _redo () {
      if (editor.undo_index < editor.undo_stack.length) {
        editor.undoing = true;

        // Get snapshot.
        var snapshot = editor.undo_stack[editor.undo_index++];

        // Clear any existing content changed timers.
        clearTimeout(editor._content_changed_timer)

        // Restore snapshot.
        editor.snapshot.restore(snapshot);

        // Hide popups.
        editor.popups.hideAll();

        // Enable toolbar.
        editor.toolbar.enable();

        // Call content changed.
        editor.events.trigger('contentChanged');

        editor.events.trigger('commands.redo');

        editor.undoing = false;
      }
    }

    function reset () {
      editor.undo_index = 0;
      editor.undo_stack = [];
    }

    /**
     * Initialize
     */
    function _init () {
      reset();
      editor.events.on('initialized', function () {
        last_html = editor.html.get(false, true);
      });

      editor.events.on('blur', function () {
        editor.undo.saveStep();
      })

      editor.events.on('keydown', _disableBrowserUndo);
    }

    return {
      _init: _init,
      run: _do,
      redo: _redo,
      canDo: canDo,
      canRedo: canRedo,
      dropRedo: dropRedo,
      reset: reset,
      saveStep: saveStep
    }
  };


  $.FE.POPUP_TEMPLATES = {
    'text.edit': '[_EDIT_]'
  };

  $.FE.RegisterTemplate = function (name, template) {
    $.FE.POPUP_TEMPLATES[name] = template;
  }

  $.FE.MODULES.popups = function (editor) {
    if (!editor.shared.popups) editor.shared.popups = {};
    var popups = editor.shared.popups;

    function setContainer(id, $container) {
      popups[id].data('container', $container);
      $container.append(popups[id]);
    }

    /**
     * Show popup at a specific position.
     */
    function show (id, left, top, obj_height) {
      // Restore selection on show if it is there.
      if (areVisible() && editor.$el.find('.fr-marker').length > 0) {
        editor.events.disableBlur();
        editor.selection.restore();
      }

      hideAll([id]);

      if (!popups[id]) return false;

      // Hide active dropdowns.
      $('.fr-dropdown.fr-active').removeClass('fr-active').parent('.fr-toolbar').css('zIndex', '');

      // Set the current instance for the popup.
      popups[id].data('instance', editor);

      var width = popups[id].outerWidth();
      var height = popups[id].outerHeight();
      var is_visible = isVisible(id);
      popups[id].addClass('fr-active').find('input, textarea').removeAttr('disabled');

      var $container = popups[id].data('container');

      // If container is toolbar then increase zindex.
      if ($container.is(editor.$tb)) editor.$tb.css('zIndex', 5);

      // Inline mode when container is toolbar.
      if (editor.opts.toolbarInline && $container && editor.$tb && $container.get(0) == editor.$tb.get(0)) {
        setContainer(id, editor.opts.toolbarInline ? $(editor.opts.scrollableContainer) : editor.$box);
        if (top) top = editor.$tb.offset().top - editor.helpers.getPX(editor.$tb.css('margin-top'));
        if (left) left = editor.$tb.offset().left + editor.$tb.width() / 2;

        if (editor.$tb.hasClass('fr-above')) {
          top += editor.$tb.outerHeight();
        }

        obj_height = 0;
      }

      // Apply iframe correction.
      $container = popups[id].data('container');
      if (editor.opts.iframe && !obj_height && !is_visible) {
        if (left) left -= editor.$iframe.offset().left;
        if (top) top -= editor.$iframe.offset().top;
      }

      // Apply left correction.
      if (left) left = left - width / 2;

      // Toolbar at the bottom and container is toolbar.
      if (editor.opts.toolbarBottom && $container && editor.$tb && $container.get(0) == editor.$tb.get(0)) {
        popups[id].addClass('fr-above');
        top = top - popups[id].outerHeight();
      }

      // Position editor.
      popups[id].removeClass('fr-active');
      editor.position.at(left, top, popups[id], obj_height || 0);
      popups[id].addClass('fr-active');

      // Focus in the first field.
      var active_popup = popups[id].find('input:visible, textarea:visible').get(0);
      if (active_popup) {
        // Save selection if necessary.

        if (editor.$el.find('.fr-marker').length == 0 && editor.core.hasFocus()) {
          editor.selection.save();
        }

        editor.events.disableBlur();
        $(active_popup).select().focus();
      }

      if (editor.opts.toolbarInline && !editor.helpers.isMobile()) editor.toolbar.hide();

      editor.events.trigger('popups.show.' + id);
    }

    function onShow (id, callback) {
      editor.events.on('popups.show.' + id, callback);
    }

    /**
     * Find visible popup.
     */
    function isVisible (id) {
      return (popups[id] && popups[id].hasClass('fr-active') && editor.core.sameInstance(popups[id])) || false;
    }

    /**
     * Check if there is any popup visible.
     */
    function areVisible (new_instance) {
      for (var id in popups) {
        if (isVisible(id) && (typeof new_instance == 'undefined' || popups[id].data('instance') == new_instance)) return true;
      }

      return false;
    }

    /**
     * Hide popup.
     */
    function hide (id) {
      if (popups[id] && popups[id].hasClass('fr-active')) {
        popups[id].removeClass('fr-active fr-above');
        editor.events.trigger('popups.hide.' + id);

        // Reset toolbar zIndex.
        if (editor.$tb) editor.$tb.css('zIndex', '');

        editor.events.disableBlur();
        popups[id].find('input, textarea, button').filter(':focus').blur();
        popups[id].find('input, textarea').attr('disabled', 'disabled');
      }
    }

    /**
     * Assign an event for hiding.
     */
    function onHide (id, callback) {
      editor.events.on('popups.hide.' + id, callback);
    }

    /**
     * Get the popup with the specific id.
     */
    function get (id) {
      var $popup = popups[id];

      if ($popup && !$popup.data('inst' + editor.id)) {
        var ev = _events(id);
        _bindInstanceEvents(ev, id);
      }

      return $popup;
    }

    function onRefresh (id, callback) {
      editor.events.on('popups.refresh.' + id, callback);
    }

    /**
     * Refresh content inside the popup.
     */
    function refresh (id) {
      editor.events.trigger('popups.refresh.' + id);

      var btns = popups[id].find('.fr-command');
      for (var i = 0; i < btns.length; i++) {
        var $btn = $(btns[i]);
        if ($btn.parents('.fr-dropdown-menu').length == 0) {
          editor.button.refresh($btn);
        }
      }
    }

    /**
     * Hide all popups.
     */
    function hideAll (except) {
      if (typeof except == 'undefined') except = [];

      for (var id in popups) {
        if (except.indexOf(id) < 0) {
          hide(id);
        }
      }
    }

    editor.shared.exit_flag = false;
    function _markExit () {
      editor.shared.exit_flag = true;
    }

    function _unmarkExit () {
      editor.shared.exit_flag = false;
    }

    function _canExit () {
      return editor.shared.exit_flag;
    }

    function _buildTemplate (id, template) {
      // Load template.
      var html = $.FE.POPUP_TEMPLATES[id];
      if (typeof html == 'function') html = html.apply(editor);

      for (var nm in template) {
        html = html.replace('[_' + nm.toUpperCase() + '_]', template[nm]);
      }

      return html;
    }

    function _build (id, template) {
      var html = _buildTemplate(id, template);

      var $popup = $('<div class="fr-popup' + (editor.helpers.isMobile() ? ' fr-mobile' : ' fr-desktop') +  (editor.opts.toolbarInline ? ' fr-inline' : '') + '"><span class="fr-arrow"></span>' + html + '</div>');

      if (editor.opts.theme) {
        $popup.addClass(editor.opts.theme + '-theme');
      }

      if (editor.opts.zIndex > 1) {
        editor.$tb.css('z-index', editor.opts.zIndex + 2);
      }

      if (editor.opts.direction != 'auto') {
        $popup.removeClass('fr-ltr fr-rtl').addClass('fr-' + editor.opts.direction);
      }

      $popup.find('input, textarea').attr('dir', editor.opts.direction).attr('disabled', 'disabled');

      var $container = $('body');
      $container.append($popup);
      $popup.data('container', $container);

      popups[id] = $popup;

      // Bind commands from the popup.
      editor.button.bindCommands($popup, false);

      return $popup;
    }

    function _events (id) {
      var $popup = popups[id];

      return {
        /**
         * Resize window.
         */
        _windowResize: function () {
          var inst = $popup.data('instance') || editor;

          if (!inst.helpers.isMobile() && $popup.is(':visible')) {
            inst.events.disableBlur();
            inst.popups.hide(id);
            inst.events.enableBlur();
          }
        },

        /**
         * Focus on an input.
         */
        _inputFocus: function (e) {
          var inst = $popup.data('instance') || editor;

          e.preventDefault();
          e.stopPropagation();

          // IE workaround.
          setTimeout(function () {
            inst.events.enableBlur();
          }, 0);

          // Reposition scroll on mobile to the original one.
          if (inst.helpers.isMobile()) {
            var t = $(inst.o_win).scrollTop();
            setTimeout(function () {
              $(inst.o_win).scrollTop(t);
            }, 0);
          }
        },

        /**
         * Blur on an input.
         */
        _inputBlur: function (e) {
          var inst = $popup.data('instance') || editor;

          // Do not do blur on window change.
          if (document.activeElement != this && $(this).is(':visible')) {
            if (inst.events.blurActive()) {
              inst.events.trigger('blur');
            }

            inst.events.enableBlur();
          }
        },

        /**
         * Keydown on an input.
         */
        _inputKeydown: function (e) {
          var inst = $popup.data('instance') || editor;
          var key_code = e.which;

          // Tabbing.
          if ($.FE.KEYCODE.TAB == key_code) {
            e.preventDefault();

            var inputs = $popup.find('input, textarea, button, select').filter(':visible').not(':disabled').toArray();
            inputs.sort(function (a, b) {
              if (e.shiftKey) return $(a).attr('tabIndex') < $(b).attr('tabIndex');
              return $(a).attr('tabIndex') > $(b).attr('tabIndex');
            });

            inst.events.disableBlur();
            var idx = inputs.indexOf(this) + 1;
            if (idx == inputs.length) idx = 0;
            $(inputs[idx]).focus();
          }

          // ENTER.
          else if ($.FE.KEYCODE.ENTER == key_code) {
            if ($popup.find('.fr-submit:visible').length > 0) {
              e.preventDefault();
              e.stopPropagation();
              inst.events.disableBlur();
              inst.button.exec($popup.find('.fr-submit:visible:first'));
            }
          }

          // ESC.
          else if ($.FE.KEYCODE.ESC == key_code) {
            e.preventDefault();
            e.stopPropagation();

            if (inst.$el.find('.fr-marker')) {
              inst.events.disableBlur();
              $(this).data('skip', true);
              inst.selection.restore();
              inst.events.enableBlur();
            }

            if (isVisible(id) && $popup.find('.fr-back:visible').length) {
              inst.button.exec($popup.find('.fr-back:visible:first'))
            }
            else {
              inst.popups.hide(id);
            }

            if (inst.opts.toolbarInline) inst.toolbar.showInline(null, true);

            return false;
          }

          // Other KEY. Stop propagation to the window.
          else {
            e.stopPropagation();
          }
        },

        /**
         * Window keydown.
         */
        _windowKeydown: function (e) {
          if (!editor.core.sameInstance($popup)) return true;

          var inst = $popup.data('instance') || editor;

          var key_code = e.which;

          // ESC.
          if ($.FE.KEYCODE.ESC == key_code) {
            if (isVisible(id) && inst.opts.toolbarInline) {
              e.stopPropagation();

              if (isVisible(id) && $popup.find('.fr-back:visible').length) {
                inst.button.exec($popup.find('.fr-back:visible:first'))
              }
              else {
                inst.popups.hide(id);
                inst.toolbar.showInline(null, true);
              }
              return false;
            }
            else {
              if (isVisible(id) && $popup.find('.fr-back:visible').length) {
                inst.button.exec($popup.find('.fr-back:visible:first'))
              }
              else {
                inst.popups.hide(id);
              }
            }
          }
        },

        /**
         * Editor keydown.
         */
        _editorKeydown: function (e) {
          var inst = $popup.data('instance') || editor;

          // ESC.
          if (!inst.keys.ctrlKey(e) && e.which != $.FE.KEYCODE.ESC) {
            if (isVisible(id) && $popup.find('.fr-back:visible').length) {
              inst.button.exec($popup.find('.fr-back:visible:first'))
            }
            else {
              inst.popups.hide(id);
            }
          }
        },

        /**
         * Handling hitting the popup elements with the mouse.
         */
        _preventFocus: function (e) {
          var inst = $popup.data('instance') || editor;
          inst.events.disableBlur();

          // Get the original target.
          var originalTarget = e.originalEvent ? (e.originalEvent.target || e.originalEvent.originalTarget) : null;

          // Define the input selector.
          var input_selector = 'input, textarea, button, select, label, .fr-command';

          // Click was not made inside an input.
          if (originalTarget && !$(originalTarget).is(input_selector) && $(originalTarget).parents(input_selector).length === 0) {
            e.stopPropagation();
            return false;
          }

          // Click was made on another input inside popup. Prevent propagation of the event.
          else if (originalTarget && $(originalTarget).is(input_selector)) {
            e.stopPropagation();
          }
        },

        /**
         * Mouseup inside the editor.
         */
        _editorMouseup: function (e) {
          // Check if popup is visible and we can exit.
          if ($popup.is(':visible') && _canExit()) {
            // If we have an input focused, then disable blur.
            if ($popup.find('input:focus, textarea:focus, button:focus, select:focus').filter(':visible').length > 0) {
              editor.events.disableBlur();
            }
          }
        },

        /**
         * Mouseup on window.
         */
        _windowMouseup: function (e) {
          if (!editor.core.sameInstance($popup)) return true;

          var inst = $popup.data('instance') || editor;

          if ($popup.is(':visible') && _canExit()) {
            e.stopPropagation();
            inst.markers.remove();
            inst.popups.hide(id);

            _unmarkExit();
          }
        },

        /**
         * Placeholder effect.
         */
        _doPlaceholder: function (e) {
          var $span = $(this).next();
          if ($span.length == 0) {
            $(this).after('<span>' + $(this).attr('placeholder') + '</span>');
          }

          $(this).toggleClass('fr-not-empty', $(this).val() != '');
        },

        /**
         * Reposition popup.
         */
        _repositionPopup: function (e) {
          if (isVisible(id) && $popup.parent().get(0) == $(editor.opts.scrollableContainer).get(0)) {
            var p_top = $popup.offset().top - editor.$wp.offset().top;
            var w_scroll = editor.$wp.scrollTop();
            var w_height = editor.$wp.outerHeight();

            if (p_top > w_height || p_top < 0) {
              $popup.addClass('fr-hidden');
            }
            else {
              $popup.removeClass('fr-hidden');
            }
          }
        }
      }
    }

    function _bindInstanceEvents (ev, id) {
      // Editor mouseup.
      editor.events.on('mouseup', ev._editorMouseup, true);
      if (editor.$wp) editor.events.on('keydown', ev._editorKeydown);

      // Hide all popups on blur.
      editor.events.on('blur', function (e) {
        hideAll();
      });

      // Update the position of the popup.
      if (editor.$wp && !editor.helpers.isMobile()) {
        editor.events.$on(editor.$wp, 'scroll.popup' + id, ev._repositionPopup);
      }

      editor.events.on('window.keydown', ev._windowKeydown);
      editor.events.on('window.mouseup', ev._windowMouseup, true);

      popups[id].data('inst' + editor.id, true);

      editor.events.on('destroy', function () {
        if (editor.core.sameInstance(popups[id])) {
          popups[id].removeClass('fr-active').appendTo('body');
        }
      }, true)
    }

    /**
     * Create a popup.
     */
    function create (id, template) {
      var $popup = _build(id, template);

      // Build events.
      var ev = _events(id);

      _bindInstanceEvents(ev, id);

      // Input Focus / Blur / Keydown.
      editor.events.$on($popup, 'mousedown mouseup touchstart touchend touch', '*', ev._preventFocus, true);
      editor.events.$on($popup, 'focus', 'input, textarea, button, select', ev._inputFocus, true);
      editor.events.$on($popup, 'blur', 'input, textarea, button, select', ev._inputBlur, true);
      editor.events.$on($popup, 'keydown', 'input, textarea, button, select', ev._inputKeydown, true);

      // Placeholder.
      editor.events.$on($popup, 'keydown keyup change input', 'input, textarea', ev._doPlaceholder, true);

      // Toggle checkbox.
      if (editor.helpers.isIOS()) {
        editor.events.$on($popup, 'touchend', 'label', function () {
          $('#' + $(this).attr('for')).prop('checked', function (i, val) {
            return !val;
          })
        }, true);
      }

      // Window mouseup.
      editor.events.$on($(editor.o_win), 'resize', ev._windowResize, true);

      return $popup;
    }

    /**
     * Destroy.
     */
    function _destroy () {
      for (var id in popups) {
        var $popup = popups[id];
        $popup.html('').removeData().remove();
      }
    }

    /**
     * Initialization.
     */
    function _init () {
      editor.events.on('shared.destroy', _destroy, true);

      editor.events.on('window.mousedown', _markExit);
      editor.events.on('window.touchmove', _unmarkExit);

      editor.events.on('mousedown', function (e) {
        if (areVisible()) {
          e.stopPropagation();

          // Remove markers.
          editor.$el.find('.fr-marker').remove();

          // Prepare for exit.
          _markExit();

          // Disable blur.
          editor.events.disableBlur();
        }
      })
    }

    return {
      _init: _init,
      create: create,
      get: get,
      show: show,
      hide: hide,
      onHide: onHide,
      hideAll: hideAll,
      setContainer: setContainer,
      refresh: refresh,
      onRefresh: onRefresh,
      onShow: onShow,
      isVisible: isVisible,
      areVisible: areVisible
    }
  };


  $.FE.MODULES.refresh = function (editor) {
    function _default ($btn, cmd) {
      try {
        if (editor.doc.queryCommandState(cmd) === true) {
          $btn.addClass('fr-active');
        }
      } catch (ex) {
      }
    }

    function undo ($btn) {
      $btn.toggleClass('fr-disabled', !editor.undo.canDo());
    }

    function redo ($btn) {
      $btn.toggleClass('fr-disabled', !editor.undo.canRedo());
    }

    function indent ($btn) {
      if ($btn.hasClass('fr-no-refresh')) return false;

      var blocks = editor.selection.blocks();
      for (var i = 0; i < blocks.length; i++) {
        if (blocks[i].tagName == 'LI' && !blocks[i].previousSibling) {
          $btn.addClass('fr-disabled');
        }
        else {
          $btn.removeClass('fr-disabled');
          return true;
        }
      }
    }

    function outdent ($btn) {
      if ($btn.hasClass('fr-no-refresh')) return false;

      var blocks = editor.selection.blocks();
      for (var i = 0; i < blocks.length; i++) {
        var prop = (editor.opts.direction == 'rtl' || $(blocks[i]).css('direction') == 'rtl') ? 'margin-right' : 'margin-left';

        if (blocks[i].tagName == 'LI' || blocks[i].parentNode.tagName == 'LI') {
          $btn.removeClass('fr-disabled');
          return true;
        }

        if (editor.helpers.getPX($(blocks[i]).css(prop)) > 0) {
          $btn.removeClass('fr-disabled');
          return true;
        }
      }

      $btn.addClass('fr-disabled');
    }

    return {
      default: _default,
      undo: undo,
      redo: redo,
      outdent: outdent,
      indent: indent
    }
  };


  $.extend($.FE.DEFAULTS, {
    editInPopup: false
  });

  $.FE.MODULES.textEdit = function (editor) {
    function _initPopup () {
      // Image buttons.
      var txt = '<div id="fr-text-edit-' + editor.id + '" class="fr-layer fr-text-edit-layer"><div class="fr-input-line"><input type="text" placeholder="' + editor.language.translate('Text') + '" tabIndex="1"></div><div class="fr-action-buttons"><button type="button" class="fr-command fr-submit" data-cmd="updateText" tabIndex="2">' + editor.language.translate('Update') + '</button></div></div>'

      var template = {
        edit: txt
      };

      var $popup = editor.popups.create('text.edit', template);
    }

    function _showPopup () {
      var $popup = editor.popups.get('text.edit');

      var text;
      if (editor.$el.prop('tagName') === 'INPUT') {
        text = editor.$el.attr('placeholder');
      }
      else {
        text = editor.$el.text();
      }

      $popup.find('input').val(text).trigger('change');
      editor.popups.setContainer('text.edit', $('body'));
      editor.popups.show('text.edit', editor.$el.offset().left + editor.$el.outerWidth() / 2, editor.$el.offset().top + editor.$el.outerHeight(), editor.$el.outerHeight());
    }

    function _initEvents () {
      // Show edit popup.
      editor.events.$on(editor.$el, editor._mouseup, function (e) {
        setTimeout (function () {
          _showPopup();
        }, 10);
      })
    }

    function update () {
      var $popup = editor.popups.get('text.edit');

      var new_text = $popup.find('input').val();

      if (new_text.length == 0) new_text = editor.opts.placeholderText;

      if (editor.$el.prop('tagName') === 'INPUT') {
        editor.$el.attr('placeholder', new_text);
      }
      else {
        editor.$el.text(new_text);
      }

      editor.events.trigger('contentChanged');

      editor.popups.hide('text.edit');
    }

    /**
     * Initialize.
     */
    function _init () {
      if (editor.opts.editInPopup) {
        _initPopup();
        _initEvents();
      }
    }

    return {
      _init: _init,
      update: update
    }
  };

  $.FE.RegisterCommand('updateText', {
    focus: false,
    undo: false,
    callback: function () {
      this.textEdit.update();
    }
  })



}));
