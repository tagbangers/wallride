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

  $.extend($.FE.POPUP_TEMPLATES, {
    'table.insert': '[_BUTTONS_][_ROWS_COLUMNS_]',
    'table.edit': '[_BUTTONS_]',
    'table.colors': '[_BUTTONS_][_COLORS_]'
  })

  // Extend defaults.
  $.extend($.FE.DEFAULTS, {
    tableInsertMaxSize: 10,
    tableEditButtons: ['tableHeader', 'tableRemove', '|', 'tableRows', 'tableColumns', 'tableStyle', '-', 'tableCells', 'tableCellBackground', 'tableCellVerticalAlign', 'tableCellHorizontalAlign', 'tableCellStyle'],
    tableInsertButtons: ['tableBack', '|'],
    tableResizerOffset: 5,
    tableResizingLimit: 30,
    tableColorsButtons: ['tableBack', '|'],
    tableColors: [
      '#61BD6D', '#1ABC9C', '#54ACD2', '#2C82C9', '#9365B8', '#475577', '#CCCCCC',
      '#41A85F', '#00A885', '#3D8EB9', '#2969B0', '#553982', '#28324E', '#000000',
      '#F7DA64', '#FBA026', '#EB6B56', '#E25041', '#A38F84', '#EFEFEF', '#FFFFFF',
      '#FAC51C', '#F37934', '#D14841', '#B8312F', '#7C706B', '#D1D5D8', 'REMOVE'
    ],
    tableColorsStep: 7,
    tableCellStyles: {
      'fr-highlighted': 'Highlighted',
      'fr-thick': 'Thick'
    },
    tableStyles: {
      'fr-dashed-borders': 'Dashed Borders',
      'fr-alternate-rows': 'Alternate Rows'
    },
    tableCellMultipleStyles: true,
    tableMultipleStyles: true,
    tableInsertHelper: true,
    tableInsertHelperOffset: 15
  });

  $.FE.PLUGINS.table = function (editor) {
    var $resizer;
    var $insert_helper;
    var mouseDownCellFlag;
    var mouseDownFlag;
    var mouseDownCell;
    var mouseMoveTimer;
    var resizingFlag;

    /*
     * Show the insert table popup.
     */
    function _showInsertPopup () {
      var $btn = editor.$tb.find('.fr-command[data-cmd="insertTable"]');

      var $popup = editor.popups.get('table.insert');
      if (!$popup) $popup = _initInsertPopup();

      if (!$popup.hasClass('fr-active')) {

        // Insert table popup
        editor.popups.refresh('table.insert');
        editor.popups.setContainer('table.insert', editor.$tb);

        // Insert table left and top position.
        var left = $btn.offset().left + $btn.outerWidth() / 2;
        var top = $btn.offset().top + (editor.opts.toolbarBottom ? 10 : $btn.outerHeight() - 10);
        editor.popups.show('table.insert', left, top, $btn.outerHeight());
      }
    }

    /*
     * Show the table edit popup.
     */
    function _showEditPopup () {
      // Set popup position.
      var map = _tableMap();
      if (map) {
        var $popup = editor.popups.get('table.edit');
        if (!$popup) $popup = _initEditPopup();

        editor.popups.setContainer('table.edit', $(editor.opts.scrollableContainer));
        var offset = _selectionOffset(map);
        var left = (offset.left + offset.right) / 2;
        var top = offset.bottom;

        editor.popups.show('table.edit', left, top, offset.bottom - offset.top);

        // Disable toolbar buttons only if there are more than one cells selected.
        if (editor.$el.find('.fr-selected-cell').length > 1) {
          // Disable toolbar.
          editor.toolbar.disable();

          // Allow text selection.
          editor.$el.removeClass('fr-no-selection');
          editor.edit.on();

          var scroll_top = $(editor.o_win).scrollTop();
          editor.$el.focus();
          editor.selection.setAtEnd(editor.$el.find('.fr-selected-cell:last').get(0));
          editor.selection.restore();
          $(editor.o_win).scrollTop(scroll_top);
          editor.button.bulkRefresh();
        }
      }
    }

    /*
     * Show the table colors popup.
     */
    function _showColorsPopup () {
      // Set popup position.
      var map = _tableMap();
      if (map) {
        var $popup = editor.popups.get('table.colors');
        if (!$popup) $popup = _initColorsPopup();

        editor.popups.setContainer('table.colors', $(editor.opts.scrollableContainer));
        var offset = _selectionOffset(map);
        var left = (offset.left + offset.right) / 2;
        var top = offset.bottom;

        // Refresh selected color.
        _refreshColor();

        editor.popups.show('table.colors', left, top, offset.bottom - offset.top);
      }
    }

    /*
     * Hide table edit popup.
     */
    function _hideEditPopup () {
      // Enable toolbar.
      if (editor.$el.get(0).querySelectorAll('.fr-selected-cell').length === 0) {
        editor.toolbar.enable();
      }
    }

    /**
     * Init the insert table popup.
     */
    function _initInsertPopup (delayed) {
      if (delayed) {
        editor.popups.onHide('table.insert', function () {
          // Clear previous cell selection.
          editor.popups.get('table.insert').find('.fr-table-size .fr-select-table-size > span[data-row="1"][data-col="1"]').trigger('mouseenter');
        });

        return true;
      }

      // Table buttons.
      var table_buttons = '';
      if (editor.opts.tableInsertButtons.length > 0) {
        table_buttons = '<div class="fr-buttons">' + editor.button.buildList(editor.opts.tableInsertButtons) + '</div>';
      }

      var template = {
        buttons: table_buttons,
        rows_columns: _insertTableHtml()
      };

      var $popup = editor.popups.create('table.insert', template);

      // Initialize insert table grid events.
      editor.events.$on($popup, 'mouseenter', '.fr-table-size .fr-select-table-size .fr-table-cell', function (e) {
        var $table_cell = $(e.currentTarget);
        var row = $table_cell.data('row');
        var col = $table_cell.data('col');
        var $select_size = $table_cell.parent();

        // Update size in title.
        $select_size.siblings('.fr-table-size-info').html(row + ' &times; ' + col);

        // Remove hover class from all cells.
        $select_size.find('> span').removeClass('hover');

        // Add hover class only to the correct cells.
        for (var i = 1; i <= editor.opts.tableInsertMaxSize; i++) {
          for (var j = 0; j <= editor.opts.tableInsertMaxSize; j++) {
            var $cell = $select_size.find('> span[data-row="' + i + '"][data-col="' + j + '"]');

            if (i <= row && j <= col) {
              $cell.addClass('hover');
            } else if ((i <= row + 1 || (i <= 2 && !editor.helpers.isMobile()))) {
              $cell.css('display', 'inline-block');
            } else if (i > 2 && !editor.helpers.isMobile()) {
              $cell.css('display', 'none');
            }
          }
        }
      }, true);

      return $popup;
    }

    /*
     * The HTML for insert table grid.
     */
    function _insertTableHtml () {
      // Grid html
      var rows_columns = '<div class="fr-table-size"><div class="fr-table-size-info">1 &times; 1</div><div class="fr-select-table-size">'

      for (var i = 1; i <= editor.opts.tableInsertMaxSize; i++) {
        for (var j = 1; j <= editor.opts.tableInsertMaxSize; j++) {
          var display = 'inline-block';

          // Display only first 2 rows.
          if (i > 2 && !editor.helpers.isMobile()) {
            display = 'none';
          }

          var cls = 'fr-table-cell ';
          if (i == 1 && j == 1) {
            cls += ' hover';
          }

          rows_columns += '<span class="fr-command ' + cls + '" data-cmd="tableInsert" data-row="' + i + '" data-col="' + j + '" data-param1="' + i + '" data-param2="' + j + '" style="display: ' + display + ';"><span></span></span>';
        }
        rows_columns += '<div class="new-line"></div>';
      }

      rows_columns += '</div></div>';

      return rows_columns;
    }

    /**
     * Init the table edit popup.
     */
    function _initEditPopup (delayed) {
      if (delayed) {
        editor.popups.onHide('table.edit', _hideEditPopup);

        return true;
      }

      // Table buttons.
      var table_buttons = '';
      if (editor.opts.tableEditButtons.length > 0) {
        table_buttons = '<div class="fr-buttons">' + editor.button.buildList(editor.opts.tableEditButtons) + '</div>';
      }

      var template = {
        buttons: table_buttons
      };

      var $popup = editor.popups.create('table.edit', template);

      editor.events.$on(editor.$wp, 'scroll.table-edit', function () {
        if (editor.popups.isVisible('table.edit')) {
          _showEditPopup();
        }
      });

      return $popup;
    }

    /*
     * Init the table cell background popup.
     */
    function _initColorsPopup () {
      // Table colors buttons.
      var table_buttons = '';
      if (editor.opts.tableColorsButtons.length > 0) {
        table_buttons = '<div class="fr-buttons fr-table-colors-buttons">' + editor.button.buildList(editor.opts.tableColorsButtons) + '</div>';
      }

      var template = {
        buttons: table_buttons,
        colors: _colorsHTML()
      };

      var $popup = editor.popups.create('table.colors', template);

      editor.events.$on(editor.$wp, 'scroll.table-colors', function () {
        if (editor.popups.isVisible('table.colors')) {
          _showColorsPopup();
        }
      });

      return $popup;
    }

    /*
     * HTML for the table colors.
     */
    function _colorsHTML () {
      // Create colors html.
      var colors_html = '<div class="fr-table-colors">';

      // Add colors.
      for (var i = 0; i < editor.opts.tableColors.length; i++) {
        if (i !== 0 && i % editor.opts.tableColorsStep === 0) {
          colors_html += '<br>';
        }

        if (editor.opts.tableColors[i] != 'REMOVE') {
          colors_html += '<span class="fr-command" style="background: ' + editor.opts.tableColors[i] + ';" data-cmd="tableCellBackgroundColor" data-param1="' + editor.opts.tableColors[i] + '"></span>';
        }

        else {
          colors_html += '<span class="fr-command" data-cmd="tableCellBackgroundColor" data-param1="REMOVE" title="' + editor.language.translate('Clear Formatting') + '"><i class="fa fa-eraser"></i></span>';
        }
      }

      colors_html += '</div>';

      return colors_html;
    }

    /*
     * Show the current selected color.
     */
    function _refreshColor () {
      var $popup = editor.popups.get('table.colors');
      var $cell = editor.$el.find('.fr-selected-cell:first');

      // Remove current color selection.
      $popup.find('.fr-selected-color').removeClass('fr-selected-color');

      // Find the selected color.
      $popup.find('span[data-param1="' + editor.helpers.RGBToHex($cell.css('background-color')) + '"]').addClass('fr-selected-color');
    }

    /*
     * Insert table method.
     */
    function insert (rows, cols) {
      // Create table HTML.
      var table = '<table style="width: 100%;"><tbody>';
      var cell_width = 100 / cols;
      var i;
      var j;

      for (i = 0; i < rows; i++) {
        table += '<tr>';

        for (j = 0; j < cols; j++) {
          table += '<td style="width: ' + cell_width.toFixed(4) + '%;">';
          if (i === 0 && j === 0)table += $.FE.MARKERS;
          table += '<br></td>';
        }
        table += '</tr>';
      }
      table += '</tbody></table>';

      editor.html.insert(table);

      // Update cursor position.
      editor.selection.restore()
    }

    /*
     * Delete table method.
     */
    function remove () {
      if (editor.$el.find('.fr-selected-cell').length > 0) {
        var $current_table = editor.$el.find('.fr-selected-cell').closest('table');

        // Update cursor position.
        editor.selection.setBefore($current_table.get(0)) || editor.selection.setAfter($current_table.get(0));
        editor.selection.restore();

        // Hide table edit popup.
        editor.popups.hide('table.edit');

        // Delete table.
        $current_table.remove();
      }
    }

    /*
     * Add table header.
     */
    function addHeader () {
      var $table = editor.$el.find('.fr-selected-cell').closest('table')

      // If there is a selection in the table and the table doesn't have a header already.
      if ($table.length > 0 && $table.find('th').length === 0) {
        // Create header HTML.
        var thead = '<thead><tr>';

        var i;
        var col = 0;

        // Get first row and count table columns.
        $table.find('tr:first > td').each (function () {
          var $td = $(this);

          col += parseInt($td.attr('colspan'), 10) || 1;
        });

        // Add cells.
        for (i = 0; i < col; i++) {
          thead += '<th><br></th>';
        }

        thead += '</tr></thead>'

        $table.prepend(thead);

        // Reposition table edit popup.
        _showEditPopup();
      }
    }

    /*
     * Remove table header.
     */
    function removeHeader () {
      var $current_table = editor.$el.find('.fr-selected-cell').closest('table');
      var $table_header = $current_table.find('thead');

      // Table has a header.
      if ($table_header.length > 0) {
        // If table does not have any other rows then delete table.
        if ($current_table.find('tbody tr').length === 0) {
          // Remove table.
          remove();
        }

        else {
          $table_header.remove();

          // Reposition table edit popup if there any more selected celss.
          if (editor.$el.find('.fr-selected-cell').length > 0) {
            _showEditPopup();
          }
          else {
            // Hide popup.
            editor.popups.hide('table.edit');

            // Update cursor position.
            var td = $current_table.find('tbody tr:first td:first').get(0);
            if (td) {
              editor.selection.setAtEnd(td);
              editor.selection.restore();
            }
          }
        }
      }
    }

    /*
     * Insert row method.
     */
    function insertRow (position) {
      var $table = editor.$el.find('.fr-selected-cell').closest('table');

      // We have selection in a table.
      if ($table.length > 0) {
        // Cannot insert row above the table header.
        if (editor.$el.find('th.fr-selected-cell').length > 0 && position == 'above') {
          return;
        }

        var i;
        var ref_row;

        // Create a table map.
        var map = _tableMap();

        // Get selected cells from the table.
        var selection = _currentSelection(map);

        // Reference row.
        if (position == 'above') {
          ref_row = selection.min_i;
        } else {
          ref_row = selection.max_i;
        }

        // Create row HTML.
        var tr = '<tr>';

        // Add cells.
        for (i = 0; i < map[ref_row].length; i++) {
          // If cell has rowspan we should increase it.
          if ((position == 'below' && ref_row < map.length - 1 && map[ref_row][i] == map[ref_row + 1][i]) ||
              (position == 'above' && ref_row > 0 && map[ref_row][i] == map[ref_row - 1][i])) {

            // Don't increase twice for colspan.
            if (i === 0 || (i > 0 && map[ref_row][i] != map[ref_row][i - 1])) {
              var $cell = $(map[ref_row][i]);
              $cell.attr('rowspan', parseInt($cell.attr('rowspan'), 10) + 1);
            }

          } else {
            tr += '<td><br></td>';
          }
        }

        // Close row tag.
        tr += '</tr>';

        var $ref_row = $($table.find('tr').not($table.find('table tr')).get(ref_row));

        // Insert new row.
        if (position == 'below') {
          // Table edit popup should not change position.
          $ref_row.after(tr);
        }
        else if (position == 'above') {
          $ref_row.before(tr);

          // Reposition table edit popup.
          if (editor.popups.isVisible('table.edit')) {
            _showEditPopup();
          }
        }
      }
    }

    /*
     * Delete row method.
     */
    function deleteRow () {
      var $table = editor.$el.find('.fr-selected-cell').closest('table');

      // We have selection in a table.
      if ($table.length > 0) {
        var i;
        var j;
        var $row;

        // Create a table map.
        var map = _tableMap();

        // Get selected cells from the table.
        var selection = _currentSelection(map);

        // If all the rows are selected then delete the entire table.
        if (selection.min_i === 0 && selection.max_i == map.length - 1) {
          remove();

        } else {
          // We should delete selected rows.
          for (i = selection.max_i; i >= selection.min_i; i--) {
            $row = $($table.find('tr').not($table.find('table tr')).get(i));

            // Go through the table map to check for rowspan.
            for (j = 0; j < map[i].length; j++) {
              // Don't do this twice if we have a colspan.
              if (j === 0 || map[i][j] != map[i][j - 1]) {
                var $cell = $(map[i][j]);

                // We should decrease rowspan.
                if (parseInt($cell.attr('rowspan'), 10) > 1) {
                  var rowspan = parseInt($cell.attr('rowspan'), 10) - 1;

                  if (rowspan == 1) {
                    $cell.removeAttr('rowspan');
                  } else {
                    $cell.attr('rowspan', rowspan);
                  }
                }

                // We might need to move tds on the row below if we have a rowspan that starts here.
                if (i < map.length - 1 && map[i][j] == map[i + 1][j] && (i === 0 || map[i][j] != map[i - 1][j])) {
                  // Move td to the row below.
                  var td = map[i][j];
                  var col = j;

                  // Go back until we get the last element (we might have colspan).
                  while (col > 0 && map[i][col] == map[i][col - 1]) {
                    col--;
                  }

                  // Add td at the beginning of the row below.
                  if (col === 0) {
                    $($table.find('tr').not($table.find('table tr')).get(i + 1)).prepend(td);

                  } else {
                    $(map[i + 1][col - 1]).after(td);
                  }
                }
              }
            }

            // Remove tbody or thead if there are no more rows.
            var $row_parent = $row.parent();
            $row.remove();
            if ($row_parent.find('tr').length === 0) {
              $row_parent.remove();
            }

            // Table has changed.
            map = _tableMap($table);
          }

          // Update cursor position
          if (selection.min_i > 0) {
            // Place cursor in the row above selection.
            editor.selection.setAtEnd(map[selection.min_i - 1][0]);
          }
          else {
            // Place cursor in the row below selection.
            editor.selection.setAtEnd(map[0][0]);
          }
          editor.selection.restore();

          // Hide table edit popup.
          editor.popups.hide('table.edit');
        }
      }
    }

    /*
     * Insert column method.
     */
    function insertColumn (position) {
      var $table = editor.$el.find('.fr-selected-cell').closest('table');

      // We have selection in a table.
      if ($table.length > 0) {
        // Create a table map.
        var map = _tableMap();

        // Get selected cells from the table.
        var selection = _currentSelection(map);

        // Reference row.
        var ref_col;

        if (position == 'before') {
          ref_col = selection.min_j;
        } else {
          ref_col = selection.max_j;
        }

        // Old and new column widths.
        var old_width = 100 / map[0].length;
        var new_width = 100 / (map[0].length + 1);

        // Go through all cells and get their initial (old) widths.
        var $cell;

        $table.find('th, td').each (function () {
          $cell = $(this);
          $cell.data('old-width', $cell.outerWidth() / $table.outerWidth() * 100);
        });

        // Parse each row to insert a new td.
        $table.find('tr').not($table.find('table tr')).each (function (index) {
          // Get the exact td index before / after which we have to add the new td.
          // ref_col means the table column number, but this is not the same with the td number in a row.
          // We might have colspan or rowspan greater than 1.
          var $row = $(this);
          var col_no = 0;
          var td_no = 0;
          var ref_td;

          // Start with the first td (td_no = 0) in the current row.
          // Sum colspans (col_no) to see when we reach ref_col.
          // Summing colspans we get the same number with the table column number.
          while (col_no - 1 < ref_col) {
            // Get current td.
            ref_td = $row.find('> th, > td').get(td_no);

            // There are no more tds in this row.
            if (!ref_td) {
              ref_td = null;
              break;
            }

            // The current td is the same with the td from the table map.
            if (ref_td == map[index][col_no]) {
              // The current td might have colspan.
              col_no += parseInt($(ref_td).attr('colspan'), 10) || 1;

              // Go to the next td on the current row.
              td_no++;
            }

            // If current td is not the same with the td from the table map.
            // This means that this table cell (map[index][td_no]) has rowspan.
            // There is at least one td less on this row due to rowspan (based on map[index][td_no] colspan value).
            // We want to count this as a column as well.
            else {
              col_no += parseInt($(map[index][col_no]).attr('colspan'), 10) || 1;

              // ref_td is one td ahead. Get previous td if we want to insert column after.
              if (position == 'after') {
                // There is a rowspan and so ref_td is the first td, but it is not in the first column.
                if (td_no === 0) {
                  ref_td = -1;

                } else {
                  ref_td = $row.find('> th, > td').get(td_no - 1);
                }
              }
            }
          }

          var $ref_td = $(ref_td);

          // If the computed column number is higher than the reference number
          // then on this row we have a colspan longer than the reference column.
          // When adding a column after we should increase colspan on this row.
          //
          // If we want to add a column before, but the td on the reference column is
          // the same with the previous one then we have a td with colspan that
          // starts before the column reference number.
          // When adding a column before we should increase colspan on this row.
          if ((position == 'after' && col_no - 1 > ref_col) ||
              (position == 'before' && ref_col > 0 && map[index][ref_col] == map[index][ref_col - 1])) {

            // Don't increase twice for rowspan.
            if (index === 0 || (index > 0 && map[index][ref_col] != map[index - 1][ref_col])) {
              var colspan = parseInt($ref_td.attr('colspan'), 10) + 1;

              $ref_td.attr('colspan', colspan)

              // Update this cell's width.
              $ref_td.css('width', ($ref_td.data('old-width') * new_width / old_width + new_width).toFixed(4) + '%');
              $ref_td.removeData('old-width');
            }

          }

          else {
            // Create HTML for a new cell.
            var td;

            // Might be a td or a th.
            if ($row.find('th').length > 0) {
              td = '<th style="width: ' + new_width.toFixed(4) + '%;"><br></th>';
            }
            else {
              td = '<td style="width: ' + new_width.toFixed(4) + '%;"><br></td>';
            }

            // Insert exactly at the beginning.
            if (ref_td == -1) {
              $row.prepend(td);

            // Insert exactly at the end.
            } else if (ref_td == null) {
              $row.append(td);

            // Insert td on the current row.
            } else {
              if (position == 'before') {
                $ref_td.before(td);
              }

              else if (position == 'after') {
                $ref_td.after(td);
              }
            }
          }
        });

        // Parse each row to update cells' width.
        $table.find('th, td').each (function () {
          $cell = $(this);

          // Update width and remove data.
          if ($cell.data('old-width')) {
            $cell.css('width', ($cell.data('old-width') * new_width / old_width).toFixed(4) + '%');
            $cell.removeData('old-width')
          }
        });

        // Reposition table edit popup.
        if (editor.popups.isVisible('table.edit')) {
          _showEditPopup();
        }
      }
    }

    /*
     * Delete column method.
     */
    function deleteColumn () {
      var $table = editor.$el.find('.fr-selected-cell').closest('table');

      // We have selection in a table.
      if ($table.length > 0) {
        var i;
        var j;
        var $cell;

        // Create a table map.
        var map = _tableMap();

        // Get selected cells from the table.
        var selection = _currentSelection(map);

        // If all the rows are selected then delete the entire table.
        if (selection.min_j === 0 && selection.max_j == map[0].length - 1) {
          remove();

        } else {
          // Old and new column widths.
          var old_width = 100 / map[0].length;
          var new_width = 100 / (map[0].length - selection.max_j + selection.min_j - 1);

          // Go through all cells and get their initial (old) widths.
          $table.find('th, td').each (function () {
            $cell = $(this);

            if (!$cell.hasClass('fr-selected-cell')) {
              $cell.data('old-width', $cell.outerWidth() / $table.outerWidth() * 100);
            }
          });

          // We should delete selected columns.
          for (j = selection.max_j; j >= selection.min_j; j--) {
            // Go through the table map to check for colspan.
            for (i = 0; i < map.length; i++) {
              // Don't do this twice if we have a rowspan.
              if (i === 0 || map[i][j] != map[i - 1][j]) {
                // We should decrease colspan.
                $cell = $(map[i][j]);

                if (parseInt($cell.attr('colspan'), 10) > 1) {
                  var colspan = parseInt($cell.attr('colspan'), 10) - 1;

                  if (colspan == 1) {
                    $cell.removeAttr('colspan');
                  } else {
                    $cell.attr('colspan', colspan);
                  }

                  // Update cell width.
                  $cell.css('width', (($cell.data('old-width') - _columnWidth(j, map)) * new_width / old_width).toFixed(4) + '%');
                  $cell.removeData('old-width');

                // If there is no colspan delete the cell.
                } else {
                  // We might need to delete the row too if it is empty.
                  var $row = $($cell.parent().get(0));

                  $cell.remove();

                  // Check if there are any more tds in the current row.
                  if ($row.find('> th, > td').length === 0) {
                    // Check if it is okay to delete the tr.
                    if ($row.prev().length === 0 || $row.next().length === 0 ||
                        $row.prev().find('> th[rowspan], > td[rowspan]').length < $row.prev().find('> th, > td').length) {
                      $row.remove();
                    }
                  }
                }
              }
            }
          }

          // Update cursor position
          if (selection.min_j > 0) {
            // Place cursor in the column before selection.
            editor.selection.setAtEnd(map[selection.min_i][selection.min_j - 1]);
          }
          else {
            // Place cursor in the column after selection.
            editor.selection.setAtEnd(map[selection.min_i][0]);
          }
          editor.selection.restore();

          // Hide table edit popup.
          editor.popups.hide('table.edit');

          // Scale current cells' width after column has been deleted.
          $table.find('th, td').each (function () {
            $cell = $(this);

            // Update width and remove data.
            if ($cell.data('old-width')) {
              $cell.css('width', ($cell.data('old-width') * new_width / old_width).toFixed(4) + '%');
              $cell.removeData('old-width')
            }
          });
        }
      }
    }

    /*
     * Merge selected cells method.
     */
    function mergeCells () {
      // We have more than one cell selected in a table. Cannot merge td and th.
      if (editor.$el.find('.fr-selected-cell').length > 1 && (editor.$el.find('th.fr-selected-cell').length === 0 || editor.$el.find('td.fr-selected-cell').length === 0)) {
        // Create a table map.
        var map = _tableMap();

        // Get selected cells.
        var selection = _currentSelection(map);

        var i;
        var $cell;
        var $row;
        var cells = editor.$el.find('.fr-selected-cell');
        var $first_cell = $(cells[0]);
        var $first_row = $first_cell.parent();
        var first_row_cells = $first_row.find('.fr-selected-cell');
        var $current_table = $first_cell.closest('table');
        var content = $first_cell.html();
        var width = 0;

        // Update cell width.
        for (i = 0; i < first_row_cells.length; i++) {
          width += $(first_row_cells[i]).outerWidth();
        }

        $first_cell.css('width', (width / $first_row.outerWidth() * 100).toFixed(4) + '%');

        // Set the colspan for the merged cells.
        if (selection.min_j < selection.max_j) {
          $first_cell.attr('colspan', selection.max_j - selection.min_j + 1);
        }

        // Set the rowspan for the merged cells.
        if (selection.min_i < selection.max_i) {
          $first_cell.attr('rowspan', selection.max_i - selection.min_i + 1);
        }

        // Go through all selected cells to merge their content.
        for (i = 1; i < cells.length; i++) {
          $cell = $(cells[i])

          // If cell is empty, don't add only <br> tags.
          if ($cell.html() != '<br>' && $cell.html() !== '') {
            content += '<br>' + $cell.html();
          }

          // Remove cell.
          $cell.remove();
        }

        // Set the HTML content.
        $first_cell.html(content);
        editor.selection.setAtEnd($first_cell.get(0));
        editor.selection.restore();

        // Enable toolbar.
        editor.toolbar.enable();

        // Merge is done, check if we have empty trs to clean.
        var empty_trs = $current_table.find('tr:empty');

        for (i = empty_trs.length - 1; i >= 0; i--) {
          $row = $(empty_trs[i]);

          // Check if it is okay to delete the tr.
          if ($row.prev().length === 0 || $row.next().length === 0 ||
              $row.prev().find('> th[rowspan], > td[rowspan]').length < $row.prev().find('> th, > td').length) {
            $row.remove();
          }
        }

        // Reposition table edit popup.
        _showEditPopup();
      }
    }

    /*
     * Split cell horizontally method.
     */
    function splitCellHorizontally () {
      // We have only one cell selected in a table.
      if (editor.$el.find('.fr-selected-cell').length == 1) {
        var $selected_cell = editor.$el.find('.fr-selected-cell');
        var $current_row = $selected_cell.parent();
        var $current_table = $selected_cell.closest('table');
        var current_rowspan = parseInt($selected_cell.attr('rowspan'), 10);

        // Create a table map.
        var map = _tableMap();
        var cell_origin = _cellOrigin($selected_cell.get(0), map);

        // Create new td.
        var $new_td = $selected_cell.clone().html('<br>');

        // Cell has rowspan.
        if (current_rowspan > 1) {
          // Split current cell's rowspan.
          var new_rowspan = Math.ceil(current_rowspan / 2);

          if (new_rowspan > 1) {
            $selected_cell.attr('rowspan', new_rowspan);
          } else {
            $selected_cell.removeAttr('rowspan');
          }

          // Update new td's rowspan.
          if (current_rowspan - new_rowspan > 1) {
            $new_td.attr('rowspan', current_rowspan - new_rowspan);
          } else {
            $new_td.removeAttr('rowspan');
          }

          // Find where we should insert the new td.
          var row = cell_origin.row + new_rowspan;
          var col = cell_origin.col === 0 ? cell_origin.col : cell_origin.col - 1;

          // Go back until we find a td on this row. We might have colspans and rowspans.
          while (col >= 0 && (map[row][col] == map[row][col - 1] || (row > 0 && map[row][col] == map[row - 1][col]))) {
            col--;
          }

          if (col == -1) {
            // We couldn't find a previous td on this row. Prepend the new td.
            $($current_table.find('tr').not($current_table.find('table tr')).get(row)).prepend($new_td);

          } else {
            $(map[row][col]).after($new_td);
          }

        } else {
          // Add new row bellow with only one cell.
          var $row = $('<tr>').append($new_td);
          var i;

          // Increase rowspan for all cells on the current row.
          for (i = 0; i < map[0].length; i++) {
            // Don't do this twice if we have a colspan.
            if (i === 0 || map[cell_origin.row][i] != map[cell_origin.row][i - 1]) {
              var $cell = $(map[cell_origin.row][i]);

              if (!$cell.is($selected_cell)) {
                $cell.attr('rowspan', (parseInt($cell.attr('rowspan'), 10) || 1) + 1);
              }
            }
          }

          $current_row.after($row);
        }

        // Remove selection
        _removeSelection();

        // Hide table edit popup.
        editor.popups.hide('table.edit');
      }
    }

    /*
     * Split cell vertically method.
     */
    function splitCellVertically () {
      // We have only one cell selected in a table.
      if (editor.$el.find('.fr-selected-cell').length == 1) {
        var $selected_cell = editor.$el.find('.fr-selected-cell');
        var current_colspan = parseInt($selected_cell.attr('colspan'), 10) || 1;
        var parent_width = $selected_cell.parent().outerWidth();
        var width = $selected_cell.outerWidth();

        // Create new td.
        var $new_td = $selected_cell.clone().html('<br>');

        // Create a table map.
        var map = _tableMap();
        var cell_origin = _cellOrigin($selected_cell.get(0), map);

        if (current_colspan > 1) {
          // Split current colspan.
          var new_colspan = Math.ceil(current_colspan / 2);

          width = _columnsWidth(cell_origin.col, cell_origin.col + new_colspan - 1, map) / parent_width * 100;
          var new_width = _columnsWidth(cell_origin.col + new_colspan, cell_origin.col + current_colspan - 1, map) / parent_width * 100;

          // Update colspan for current cell.
          if (new_colspan > 1) {
            $selected_cell.attr('colspan', new_colspan);
          } else {
            $selected_cell.removeAttr('colspan');
          }

          // Update new td's colspan.
          if (current_colspan - new_colspan > 1) {
            $new_td.attr('colspan', current_colspan - new_colspan);
          } else {
            $new_td.removeAttr('colspan');
          }

          // Update cell width.
          $selected_cell.css('width', width.toFixed(4) + '%');
          $new_td.css('width', new_width.toFixed(4) + '%');

        // Increase colspan for all cells on the current column.
        } else {
          var i;

          for (i = 0; i < map.length; i++) {
            // Don't do this twice if we have a rowspan.
            if (i === 0 || map[i][cell_origin.col] != map[i - 1][cell_origin.col]) {
              var $cell = $(map[i][cell_origin.col]);

              if (!$cell.is($selected_cell)) {
                var colspan = (parseInt($cell.attr('colspan'), 10) || 1) + 1;
                $cell.attr('colspan', colspan);
              }
            }
          }

          // Update cell width.
          width = width / parent_width * 100 / 2;

          $selected_cell.css('width', width.toFixed(4) + '%');
          $new_td.css('width', width.toFixed(4) + '%');
        }

        // Add a new td after the current one.
        $selected_cell.after($new_td);

        // Remove selection
        _removeSelection();

        // Hide table edit popup.
        editor.popups.hide('table.edit');
      }
    }

    /*
     * Set background color to selected cells.
     */
    function setBackground (color) {
      // Set background  color.
      if (color != 'REMOVE') {
        editor.$el.find('.fr-selected-cell').css('background-color', editor.helpers.HEXtoRGB(color));
      }

      // Remove background color.
      else {
        editor.$el.find('.fr-selected-cell').css('background-color', '');
      }
    }

    /*
     * Set vertical align to selected cells.
     */
    function verticalAlign (val) {
      editor.$el.find('.fr-selected-cell').css('vertical-align', val);
    }

    /*
     * Apply horizontal alignment to selected cells.
     */
    function horizontalAlign (val) {
      editor.$el.find('.fr-selected-cell').css('text-align', val);
    }

    /**
     * Apply specific style to selected table or selected cells.
     * val              class to apply.
     * obj              table or selected cells.
     * multiple_styles  editor.opts.tableStyles or editor.opts.tableCellStyles.
     * style            editor.opts.tableStyles or editor.opts.tableCellStyles
     */
    function applyStyle (val, obj, multiple_styles, styles) {
      if (obj.length > 0) {
        // Remove multiple styles.
        if (!multiple_styles) {
          var classes = Object.keys(styles);
          classes.splice(classes.indexOf(val), 1);
          obj.removeClass(classes.join(' '));
        }

        obj.toggleClass(val);
      }
    }

    /*
     * Create a table map.
     */
    function _tableMap (table) {
      table = table || null;

      var map = [];

      if (table == null && editor.$el.find('.fr-selected-cell').length > 0) {
        table = editor.$el.find('.fr-selected-cell').closest('table');
      }

      if (table) {
        var $table = $(table);
        $table.find('tr').not($table.find('table tr')).each (function (row, tr) {
          var $tr = $(tr);

          var c_index = 0;
          $tr.find('> th, > td').each (function (col, td) {
            var $td = $(td);
            var cspan = parseInt($td.attr('colspan'), 10) || 1;
            var rspan = parseInt($td.attr('rowspan'), 10) || 1;

            for (var i = row; i < row + rspan; i++) {
              for (var j = c_index; j < c_index + cspan; j++) {
                if (!map[i]) map[i] = [];
                if (!map[i][j]) {
                  map[i][j] = td;
                } else {
                  c_index++;
                }
              }
            }

            c_index += cspan;
          })
        });

        return map;
      }
    }

    /*
     * Get the i, j coordinates of a cell in the table map.
     * These are the coordinates where the cell starts.
     */
    function _cellOrigin (td, map) {
      for (var i = 0; i < map.length; i++) {
        for (var j = 0; j < map[i].length; j++) {
          if (map[i][j] == td) {
            return {
              row: i,
              col: j
            };
          }
        }
      }
    }

    /*
     * Get the i, j coordinates where a cell ends in the table map.
     */
    function _cellEnds (origin_i, origin_j, map) {
      var max_i = origin_i + 1;
      var max_j = origin_j + 1;

      // Compute max_i
      while (max_i < map.length) {
        if (map[max_i][origin_j] != map[origin_i][origin_j]) {
          max_i--;
          break;
        }

        max_i++;
      }

      if (max_i == map.length) {
        max_i--;
      }

      // Compute max_j
      while (max_j < map[origin_i].length) {
        if (map[origin_i][max_j] != map[origin_i][origin_j]) {
          max_j--;
          break;
        }

        max_j++;
      }

      if (max_j == map[origin_i].length) {
        max_j--;
      }

      return {
        row: max_i,
        col: max_j
      };
    }

    /*
     * Remove selection from cells.
     */
    function _removeSelection () {
      var cells = editor.$el.find('.fr-selected-cell');

      if (cells.length > 0) {
        // Remove selection.
        cells.each (function () {
          var $cell = $(this);

          $cell.removeClass('fr-selected-cell');

          if ($cell.attr('class') === '') {
            $cell.removeAttr('class');
          }
        });
      }
    }

    /*
     * Clear selection to prevent Firefox cell selection.
     */
    function _clearSelection () {
      setTimeout(function () {
        editor.selection.clear();

        // Prevent text selection while selecting multiple cells.
        // Happens in Chrome.
        editor.$el.addClass('fr-no-selection');
        editor.edit.off();

        // Cursor will not appear if we don't make blur.
        editor.$el.blur();
      }, 0);
    }

    /*
     * Get current selected cells coordintates.
     */
    function _currentSelection (map) {
      var min_i = map.length;
      var max_i = 0;
      var min_j = map[0].length;
      var max_j = 0;
      var i;
      var cells = editor.$el.find('.fr-selected-cell');

      for (i = 0; i < cells.length; i++) {
        var cellOrigin = _cellOrigin(cells[i], map);
        var cellEnd = _cellEnds(cellOrigin.row, cellOrigin.col, map);

        min_i = Math.min(cellOrigin.row, min_i);
        max_i = Math.max(cellEnd.row, max_i);
        min_j = Math.min(cellOrigin.col, min_j);
        max_j = Math.max(cellEnd.col, max_j);
      }

      return {
        min_i: min_i,
        max_i: max_i,
        min_j: min_j,
        max_j: max_j
      };
    }

    /*
     * Minimum and maximum coordinates for the selection in the table map.
     */
    function _selectionLimits (min_i, max_i, min_j, max_j, map) {
      var first_i = min_i;
      var last_i = max_i;
      var first_j = min_j;
      var last_j = max_j;
      var i;
      var j;
      var cellOrigin;
      var cellEnd;

      // Go through first and last columns.
      for (i = first_i; i <= last_i; i++) {
        // First column.
        if ((parseInt($(map[i][first_j]).attr('rowspan'), 10) || 1) > 1 ||
            (parseInt($(map[i][first_j]).attr('colspan'), 10) || 1) > 1) {
          cellOrigin = _cellOrigin(map[i][first_j], map);
          cellEnd = _cellEnds(cellOrigin.row, cellOrigin.col, map);

          first_i = Math.min(cellOrigin.row, first_i);
          last_i = Math.max(cellEnd.row, last_i);
          first_j = Math.min(cellOrigin.col, first_j);
          last_j = Math.max(cellEnd.col, last_j);
        }

        // Last column.
        if ((parseInt($(map[i][last_j]).attr('rowspan'), 10) || 1) > 1 ||
            (parseInt($(map[i][last_j]).attr('colspan'), 10) || 1) > 1) {
          cellOrigin = _cellOrigin(map[i][last_j], map);
          cellEnd = _cellEnds(cellOrigin.row, cellOrigin.col, map);

          first_i = Math.min(cellOrigin.row, first_i);
          last_i = Math.max(cellEnd.row, last_i);
          first_j = Math.min(cellOrigin.col, first_j);
          last_j = Math.max(cellEnd.col, last_j);
        }
      }

      // Go through first and last rows.
      for (j = first_j; j <= last_j; j++) {
        // First row.
        if ((parseInt($(map[first_i][j]).attr('rowspan'), 10) || 1) > 1 ||
            (parseInt($(map[first_i][j]).attr('colspan'), 10) || 1) > 1) {
          cellOrigin = _cellOrigin(map[first_i][j], map);
          cellEnd = _cellEnds(cellOrigin.row, cellOrigin.col, map);

          first_i = Math.min(cellOrigin.row, first_i);
          last_i = Math.max(cellEnd.row, last_i);
          first_j = Math.min(cellOrigin.col, first_j);
          last_j = Math.max(cellEnd.col, last_j);
        }

        // Last column.
        if ((parseInt($(map[last_i][j]).attr('rowspan'), 10) || 1) > 1 ||
            (parseInt($(map[last_i][j]).attr('colspan'), 10) || 1) > 1) {
          cellOrigin = _cellOrigin(map[last_i][j], map);
          cellEnd = _cellEnds(cellOrigin.row, cellOrigin.col, map);

          first_i = Math.min(cellOrigin.row, first_i);
          last_i = Math.max(cellEnd.row, last_i);
          first_j = Math.min(cellOrigin.col, first_j);
          last_j = Math.max(cellEnd.col, last_j);
        }
      }

      if (first_i == min_i && last_i == max_i && first_j == min_j && last_j == max_j) {
        return {
          min_i: min_i,
          max_i: max_i,
          min_j: min_j,
          max_j: max_j
        };

      } else {
        return _selectionLimits(first_i, last_i, first_j, last_j, map);
      }
    }

    /*
     * Get the left and right offset position for the current selection.
     */
    function _selectionOffset (map) {
      var selection = _currentSelection(map);

      // Top left cell.
      var $tl = $(map[selection.min_i][selection.min_j]);

      // Top right cell.
      var $tr = $(map[selection.min_i][selection.max_j]);

      // Bottom left cell.
      var $bl = $(map[selection.max_i][selection.min_j]);

      var left = $tl.offset().left
      var right = $tr.offset().left + $tr.outerWidth();
      var top = $tl.offset().top;
      var bottom = $bl.offset().top + $bl.outerHeight();

      return {
        left: left,
        right: right,
        top: top,
        bottom: bottom
      };
    }

    /*
     * Select table cells
     */
    function _selectCells (firstCell, lastCell) {
      // If the first and last cells are the same then just select it.
      if ($(firstCell).is(lastCell)) {
        // Remove previous selection.
        _removeSelection();

        $(firstCell).addClass('fr-selected-cell');

      } else {
        // Prevent Firefox cell selection.
        _clearSelection();

        // Create a table map.
        var map = _tableMap();

        // Get first and last cell's i and j map coordinates.
        var firstCellOrigin = _cellOrigin(firstCell, map);
        var lastCellOrigin = _cellOrigin(lastCell, map);

        // Some cells between these coordinates might have colspan or rowspan.
        // The selected area exceeds first and last cells' coordinates.
        var limits = _selectionLimits(Math.min(firstCellOrigin.row, lastCellOrigin.row),
                                                  Math.max(firstCellOrigin.row, lastCellOrigin.row),
                                                  Math.min(firstCellOrigin.col, lastCellOrigin.col),
                                                  Math.max(firstCellOrigin.col, lastCellOrigin.col),
                                                  map);
        // Remove previous selection.
        _removeSelection();

        // Select all cells between the first and last cell.
        for (var i = limits.min_i; i <= limits.max_i; i++) {
          for (var j = limits.min_j; j <= limits.max_j; j++) {
            $(map[i][j]).addClass('fr-selected-cell');
          }
        }
      }
    }

    /*
     * Get the cell under the mouse cursor.
     */
    function _getCellUnder (e) {
      var cell = null;
      var $target = $(e.target);

      if (e.target.tagName == 'TD' || e.target.tagName == 'TH') {
        cell = e.target;
      } else if ($target.closest('td').length > 0) {
        cell = $target.closest('td').get(0);
      } else if ($target.closest('th').length > 0) {
        cell = $target.closest('th').get(0);
      }

      // Cell should reside inside editor.
      if (editor.$el.find(cell).length === 0) return null;

      return cell;
    }

    /*
     * Mark that mouse is down.
     */
    function _mouseDown (e) {
      // We always have to clear previous selection except when using shift key to select multiple cells.
      if (editor.$el.find('.fr-selected-cell').length > 0 && !e.shiftKey) {
        // Clear previous selection.
        _removeSelection();

        // Allow text selection.
        editor.$el.removeClass('fr-no-selection');
        editor.edit.on();
      }

      // On left click.
      if (e.which == 1) {
        mouseDownFlag = true;

        var cell = _getCellUnder(e);

        // User clicked on a table cell.
        if (cell) {
          // Hide table edit popup.
          editor.popups.hide('table.edit');

          e.stopPropagation();

          editor.events.trigger('image.hideResizer');
          editor.events.trigger('video.hideResizer');

          // Keep record of left mouse click being down
          mouseDownCellFlag = true;

          var tag_name = cell.tagName.toLowerCase();

          // Select multiple cells using Shift key
          if (e.shiftKey && editor.$el.find(tag_name + '.fr-selected-cell').length > 0) {

            // Cells must be in the same table.
            if ($(editor.$el.find(tag_name + '.fr-selected-cell').closest('table')).is($(cell).closest('table'))) {
              // Select cells between.
              _selectCells(mouseDownCell, cell);

            // Do nothing if cells are not in the same table.
            } else {
              // Prevent Firefox selection.
              _clearSelection();
            }
          }

          else {
            // Prevent Firefox selection for ctrl / cmd key.
            if (editor.keys.ctrlKey(e) || e.shiftKey) {
              _clearSelection();
            }

            // Save cell where mouse has been clicked
            mouseDownCell = cell;

            // Select cell.
            _selectCells(mouseDownCell, mouseDownCell);
          }
        }
      }
    }

    /*
     * Notify that mouse is no longer pressed.
     */
    function _mouseUp (e) {
      // On left click.
      if (e.which == 1) {
        mouseDownFlag = false;

        // Mouse down was in a table cell.
        if (mouseDownCellFlag) {
          // Left click is no longer pressed.
          mouseDownCellFlag = false;

          var cell = _getCellUnder(e);

          // If we have one selected cell and mouse is lifted somewhere else.
          if (!cell && editor.$el.find('.fr-selected-cell').length == 1) {
            // We have a text selection and not cell selection.
            _removeSelection();
          }

          // If there are selected cells then show table edit popup.
          else if (editor.$el.find('.fr-selected-cell').length > 0) {
            if (editor.selection.isCollapsed()) {
              _showEditPopup();
            }

            // No text selection.
            else {
              _removeSelection();
            }
          }
        }

        // User clicked somewhere else in the editor (except the toolbar).
        // We need this because mouse down is not triggered outside the editor.
        else if (!editor.$tb.is(e.target) && !editor.$tb.is($(e.target).closest(editor.$tb.get(0)))) {
          if (editor.$el.get(0).querySelectorAll('.fr-selected-cell').length > 0) {
            editor.toolbar.enable();
          }

          _removeSelection();
        }

        // Resizing stops.
        if (resizingFlag) {
          resizingFlag = false;

          $resizer.removeClass('fr-moving');

          // Allow text selection.
          editor.$el.removeClass('fr-no-selection');
          editor.edit.on();

          // Set release Y coordinate.
          var left = parseFloat($resizer.css('left')) + editor.opts.tableResizerOffset;
          if (editor.opts.iframe) {
            left -= editor.$iframe.offset().left;
          }
          $resizer.data('release-position', left);

          // Clear resizing limits.
          $resizer.removeData('max-left');
          $resizer.removeData('max-right');

          // Resize.
          _resize(e);

          // Hide resizer.
          _hideResizer();
        }
      }
    }

    /*
     * User drags mouse over multiple cells to select them.
     */
    function _mouseEnter (e) {
      if (mouseDownCellFlag === true) {
        var $cell = $(e.currentTarget);

        // Cells should be in the same table.
        if ($cell.closest('table').is(editor.$el.find('.fr-selected-cell').closest('table'))) {
          // Don't select both ths and tds.
          if (e.currentTarget.tagName == 'TD' && editor.$el.find('th.fr-selected-cell').length === 0) {
            // Select cells between.
            _selectCells(mouseDownCell, e.currentTarget);
            return;
          }

          else if (e.currentTarget.tagName == 'TH' && editor.$el.find('td.fr-selected-cell').length === 0) {
            // Select cells between.
            _selectCells(mouseDownCell, e.currentTarget);
            return;
          }
        }

       // Prevent firefox selection.
        _clearSelection();
      }
    }

    /*
     * Using the arrow keys to move the cursor through the table will not select cells.
     */
    function _usingArrows (e) {
      if (e.which == 37 || e.which == 38 || e.which == 39 || e.which == 40) {
        if (editor.$el.find('.fr-selected-cell').length > 0) {
          // Remove selection
          _removeSelection();

          // Hide table edit popup.
          editor.popups.hide('table.edit');
        }
      }
    }

    /*
     * Initilize table resizer.
     */
    function _initResizer () {
      // Append resizer HTML to editor wrapper.
      if (!editor.shared.$table_resizer) editor.shared.$table_resizer = $('<div class="fr-table-resizer"><div></div></div>');
      $resizer = editor.shared.$table_resizer;

      // Resize table. Mousedown.
      editor.events.$on($resizer, 'mousedown', function () {
        if (!editor.core.sameInstance($resizer)) return true;

        resizingFlag = true;

        $resizer.addClass('fr-moving');

        // Clear previous cell selection.
        _removeSelection();

        // Prevent text selection while dragging the table resizer.
        _clearSelection();

        // Show resizer.
        $resizer.find('div').css('opacity', 1);

        // Prevent selecting text when doing resize.
        return false;
      });

      // Mousemove on table resizer.
      editor.events.$on($resizer, 'mousemove', function (e) {
        if (!editor.core.sameInstance($resizer)) return true;

        if (resizingFlag) {
          if (editor.opts.iframe) {
            e.pageX -= editor.$iframe.offset().left;
          }

          _mouseMove(e);
        }
      })

      // Editor destroy.
      editor.events.on('shared.destroy', function () {
        $resizer.html('').removeData().remove();
      }, true);

      editor.events.on('destroy', function () {
        editor.$el.find('.fr-selected-cell').removeClass('fr-selected-cell');
        $resizer.hide().appendTo($('body'));
      }, true);
    }

    /*
     * Also clears top and left values, so it doesn't interfer with the insert helper.
     */
    function _hideResizer () {
      if ($resizer) {
        $resizer.find('div').css('opacity', 0);
        $resizer.css('top', 0);
        $resizer.css('left', 0);
        $resizer.css('height', 0);
        $resizer.find('div').css('height', 0);
        $resizer.hide();
      }
    }

    /**
     * Hide the insert helper.
     */
    function _hideInsertHelper () {
      if ($insert_helper) $insert_helper.removeClass('fr-visible').css('left', '-9999px');
    }

    /*
     * Place the table resizer between the columns where the mouse is.
     */
    function _placeResizer (e, tag_under) {
      var $tag_under = $(tag_under);
      var $table = $tag_under.closest('table');

      // We might have another tag inside the table cell.
      if (tag_under && (tag_under.tagName != 'TD' && tag_under.tagName != 'TH')) {
        if ($tag_under.closest('td').length > 0) {
          tag_under = $tag_under.closest('td');
        } else if ($tag_under.closest('th').length > 0) {
          tag_under = $tag_under.closest('th');
        }
      }

      // The tag should be a table cell (TD or TH).
      if (tag_under && (tag_under.tagName == 'TD' || tag_under.tagName == 'TH')) {
        $tag_under = $(tag_under);

        // https://github.com/froala/wysiwyg-editor/issues/786.
        if (editor.$el.find($tag_under).length === 0) return false;

        // Tag's left and right coordinate.
        var tag_left = $tag_under.offset().left - 1;
        var tag_right = tag_left + $tag_under.outerWidth();

        // Only if the mouse is close enough to the left or right edges.
        if (Math.abs(e.pageX - tag_left) <= editor.opts.tableResizerOffset ||
            Math.abs(tag_right - e.pageX) <= editor.opts.tableResizerOffset) {

          // Create a table map.
          var map = _tableMap($table);
          var tag_origin = _cellOrigin(tag_under, map);

          var tag_end = _cellEnds(tag_origin.row, tag_origin.col, map);

          // The column numbers from the map that have to be resized.
          var first;
          var second;

          // Table resizer position and height.
          var resizer_top = $table.offset().top;
          var resizer_height = $table.outerHeight() - 1;
          var resizer_left;

          // The left and right limits between which the resizer can be moved.
          var max_left;
          var max_right;

          // Mouse is near the cells's left margin (skip left table border).
          if (tag_origin.col > 0 && e.pageX - tag_left <= editor.opts.tableResizerOffset) {
            // Table resizer's left position.
            resizer_left = tag_left;

            // Previous table cell. (There's always a prev cell since we're skipping the left table border)
            var $prev_tag = $(map[tag_origin.row][tag_origin.col - 1]);

            // Left limit.
            if ((parseInt($prev_tag.attr('colspan'), 10) || 1) == 1) {
              max_left = $prev_tag.offset().left - 1 + editor.opts.tableResizingLimit;
            } else {
              max_left = tag_left - _columnWidth(tag_origin.col - 1, map) + editor.opts.tableResizingLimit;
            }

            // Right limit.
            if ((parseInt($tag_under.attr('colspan'), 10) || 1) == 1) {
              max_right = tag_left + $tag_under.outerWidth() - editor.opts.tableResizingLimit;
            } else {
              max_right = tag_left + _columnWidth(tag_origin.col, map) - editor.opts.tableResizingLimit;
            }

            // Columns to resize.
            first = tag_origin.col - 1;
            second = tag_origin.col;
          }

          // Mouse is near the cell's right margin.
          else if (tag_right - e.pageX <= editor.opts.tableResizerOffset) {
            // Table resizer's left possition.
            resizer_left = tag_right;

            // Check for next td.
            if (tag_end.col < map[tag_end.row].length && map[tag_end.row][tag_end.col + 1]) {
              // Next table cell.
              var $next_tag = $(map[tag_end.row][tag_end.col + 1]);

              // Left limit.
              if ((parseInt($tag_under.attr('colspan'), 10) || 1) == 1) {
                max_left = tag_left + editor.opts.tableResizingLimit;
              } else {
                max_left = tag_right - _columnWidth(tag_end.col, map) + editor.opts.tableResizingLimit;
              }

              // Right limit.
              if ((parseInt($next_tag.attr('colspan'), 10) || 1) == 1) {
                max_right = tag_right + $next_tag.outerWidth() - editor.opts.tableResizingLimit;
              } else {
                max_right = tag_right + _columnWidth(tag_origin.col + 1, map) - editor.opts.tableResizingLimit;
              }

              // Columns to resize.
              first = tag_end.col;
              second = tag_end.col + 1;
            }

            // Resize table.
            else {
              // Columns to resize.
              first = tag_end.col;
              second = null;

              // Resizer limits.
              var $table_parent = $table.parent();
              max_left = $table.offset().left - 1 + map[0].length * editor.opts.tableResizingLimit;
              max_right = $table_parent.offset().left - 1 + $table_parent.width() + parseFloat($table_parent.css('padding-left'));
            }
          }

          if (!$resizer) _initResizer();

          // Save table.
          $resizer.data('table', $table);

          // Save columns to resize.
          $resizer.data('first', first);
          $resizer.data('second', second);

          $resizer.data('instance', editor);
          editor.$wp.append($resizer);

          var left = resizer_left - editor.win.pageXOffset - editor.opts.tableResizerOffset;
          var top = resizer_top - editor.win.pageYOffset;

          if (editor.opts.iframe) {
            left += editor.$iframe.offset().left - $(editor.o_win).scrollLeft();
            top += editor.$iframe.offset().top - $(editor.o_win).scrollTop();

            max_left += editor.$iframe.offset().left;
            max_right += editor.$iframe.offset().left;
          }

          // Set resizing limits.
          $resizer.data('max-left', max_left);
          $resizer.data('max-right', max_right);

          // Initial position of the resizer
          $resizer.data('origin', resizer_left - editor.win.pageXOffset);

          // Set table resizer's top, left and height.
          $resizer.css('top', top);
          $resizer.css('left', left);
          $resizer.css('height', resizer_height);
          $resizer.find('div').css('height', resizer_height);

          // Set padding according to tableResizerOffset.
          $resizer.css('padding-left', editor.opts.tableResizerOffset);
          $resizer.css('padding-right', editor.opts.tableResizerOffset);

          // Show table resizer.
          $resizer.show();
        }

        // Hide resizer when the mouse moves away from the cell's border.
        else {
          if (editor.core.sameInstance($resizer)) _hideResizer();
        }
      }

      // Hide resizer if mouse is no longer over it.
      else if ($resizer && $tag_under.get(0) != $resizer.get(0) && $tag_under.parent().get(0) != $resizer.get(0)) {
        if (editor.core.sameInstance($resizer))  _hideResizer();
      }
    }

    /*
     * Show the insert column helper button.
     */
    function _showInsertColHelper (e, table) {
      if (editor.$box.find('.fr-line-breaker').is(':visible')) return false;

      // Insert Helper.
      if (!$insert_helper) _initInsertHelper();

      editor.$box.append($insert_helper);
      $insert_helper.data('instance', editor);

      var $table = $(table);
      var $row = $table.find('tr:first');

      var mouseX = e.pageX;

      var left = 0;
      var top = 0;

      if (editor.opts.iframe) {
        left += editor.$iframe.offset().left - $(editor.o_win).scrollLeft();
        top += editor.$iframe.offset().top - $(editor.o_win).scrollTop();
      }

      // Check where the column should be inserted.
      var btn_width;
      $row.find('th, td').each (function () {
        var $td = $(this);

        // Insert before this td.
        if ($td.offset().left <= mouseX && mouseX < $td.offset().left + $td.outerWidth() / 2) {
          btn_width = parseInt($insert_helper.find('a').css('width'), 10);

          $insert_helper.css('top', top + $td.offset().top - editor.win.pageYOffset - btn_width - 5);
          $insert_helper.css('left', left + $td.offset().left - editor.win.pageXOffset - btn_width / 2);
          $insert_helper.data('selected-cell', $td);
          $insert_helper.data('position', 'before');
          $insert_helper.addClass('fr-visible');

          return false;

        // Insert after this td.
        } else if ($td.offset().left + $td.outerWidth() / 2 <= mouseX && mouseX < $td.offset().left + $td.outerWidth()) {
          btn_width = parseInt($insert_helper.find('a').css('width'), 10);

          $insert_helper.css('top', top + $td.offset().top - editor.win.pageYOffset - btn_width - 5);
          $insert_helper.css('left', left + $td.offset().left + $td.outerWidth() - editor.win.pageXOffset - btn_width / 2);
          $insert_helper.data('selected-cell', $td);
          $insert_helper.data('position', 'after');
          $insert_helper.addClass('fr-visible');

          return false;
        }
      });
    }

    /*
     * Show the insert row helper button.
     */
    function _showInsertRowHelper (e, table) {
      if (editor.$box.find('.fr-line-breaker').is(':visible')) return false;

      if (!$insert_helper) _initInsertHelper();

      editor.$box.append($insert_helper);
      $insert_helper.data('instance', editor);

      var $table = $(table);
      var mouseY = e.pageY;

      var left = 0;
      var top = 0;
      if (editor.opts.iframe) {
        left += editor.$iframe.offset().left - $(editor.o_win).scrollLeft();
        top += editor.$iframe.offset().top - $(editor.o_win).scrollTop();
      }

      // Check where the row should be inserted.
      var btn_width;
      $table.find('tr').each (function () {
        var $tr = $(this);

        // Insert above this tr.
        if ($tr.offset().top <= mouseY && mouseY < $tr.offset().top + $tr.outerHeight() / 2) {
          btn_width = parseInt($insert_helper.find('a').css('width'), 10);

          $insert_helper.css('top', top + $tr.offset().top - editor.win.pageYOffset - btn_width / 2);
          $insert_helper.css('left', left + $tr.offset().left - editor.win.pageXOffset - btn_width - 5);
          $insert_helper.data('selected-cell', $tr.find('td:first'));
          $insert_helper.data('position', 'above');
          $insert_helper.addClass('fr-visible');

          return false;

        // Insert below this tr.
        } else if ($tr.offset().top + $tr.outerHeight() / 2 <= mouseY && mouseY < $tr.offset().top + $tr.outerHeight()) {
          btn_width = parseInt($insert_helper.find('a').css('width'), 10);

          $insert_helper.css('top', top + $tr.offset().top + $tr.outerHeight() - editor.win.pageYOffset - btn_width / 2);
          $insert_helper.css('left', left + $tr.offset().left - editor.win.pageXOffset - btn_width - 5);
          $insert_helper.data('selected-cell', $tr.find('td:first'));
          $insert_helper.data('position', 'below');
          $insert_helper.addClass('fr-visible');

          return false;
        }
      });
    }

    /*
     * Check if should show the insert column / row helper button.
     */
    function _insertHelper (e, tag_under) {
      // Don't show the insert helper if there are table cells selected.
      if (editor.$el.find('.fr-selected-cell').length === 0) {
        var i;
        var tag_below;
        var tag_right;

        // Tag is the editor element or body (inline toolbar). Look for closest tag bellow and at the right.
        if (tag_under && (tag_under.tagName == 'HTML' || tag_under.tagName == 'BODY' || editor.node.isElement(tag_under))) {
          // Look 1px down until a table tag is found or the insert helper offset is reached.
          for (i = 1; i <= editor.opts.tableInsertHelperOffset; i++) {
            // Look for tag below.
            tag_below = editor.doc.elementFromPoint(e.pageX - editor.win.pageXOffset, e.pageY - editor.win.pageYOffset + i);

            // We're on tooltip.
            if ($(tag_below).hasClass('fr-tooltip')) return true;

            // We found a tag bellow.
            if (tag_below && ((tag_below.tagName == 'TH' || tag_below.tagName == 'TD' || tag_below.tagName == 'TABLE') && $(tag_below).parents(editor.$wp).length)) {
              // Show the insert column helper button.
              _showInsertColHelper (e, tag_below.closest('table'));
              return true;
            }

            // Look for tag at the right.
            tag_right = editor.doc.elementFromPoint(e.pageX - editor.win.pageXOffset + i, e.pageY - editor.win.pageYOffset);

            // We're on tooltip.
            if ($(tag_right).hasClass('fr-tooltip')) return true;

            // We found a tag at the right.
            if (tag_right && ((tag_right.tagName == 'TH' || tag_right.tagName == 'TD' || tag_right.tagName == 'TABLE') && $(tag_right).parents(editor.$wp).length)) {
              // Show the insert row helper button.
              _showInsertRowHelper (e, tag_right.closest('table'));
              return true;
            }
          }
        }

        // Hide insert helper.
        if (editor.core.sameInstance($insert_helper)) {
          _hideInsertHelper();
        }
      }
    }

    /*
     * Check tag under the mouse on mouse move.
     */
    function _tagUnder (e) {
      mouseMoveTimer = null;

      // The tag under the mouse cursor.
      var tag_under = editor.doc.elementFromPoint(e.pageX - editor.win.pageXOffset, e.pageY - editor.win.pageYOffset);

      // Place table resizer if necessary.
      if (!editor.popups.areVisible() || (editor.popups.areVisible() && editor.popups.isVisible('table.edit'))) {
        _placeResizer(e, tag_under);
      }

      // Show the insert column / row helper button.
      if (!editor.popups.areVisible() && !(editor.$tb.hasClass('fr-inline') && editor.$tb.is(':visible'))) {
        _insertHelper(e, tag_under);
      }
    }

    /*
     * Repositon the resizer if the user scrolls while resizing.
     */
    function _repositionResizer () {
      if (resizingFlag) {
        var $table = $resizer.data('table');
        var top = $table.offset().top - editor.win.pageYOffset;

        if (editor.opts.iframe) {
          top += editor.$iframe.offset().top - $(editor.o_win).scrollTop();
        }

        $resizer.css('top', top);
      }
    }

    /*
     * Resize table method.
     */
    function _resize () {
      // Resizer initial position.
      var initial_positon = $resizer.data('origin');

      // Resizer release position.
      var release_position = $resizer.data('release-position');

      // Do resize only if the resizer's position has changed.
      if (initial_positon !== release_position) {
        // Columns that have to be resized.
        var first = $resizer.data('first');
        var second = $resizer.data('second');

        var $table = $resizer.data('table');
        var table_width = $table.outerWidth();

        // Resize columns and not the table.
        if (first !== null && second !== null) {
          // Create a table map.
          var map = _tableMap($table);

          // Got through all cells on these columns and get their initial width.
          var first_widths = [];
          var first_percentages = [];
          var second_widths = [];
          var second_percentages = [];
          var i;
          var $first_cell;
          var $second_cell;

          // We must do this before updating widths.
          for (i = 0; i < map.length; i++) {
            $first_cell = $(map[i][first]);
            $second_cell = $(map[i][second]);

            // Widths in px.
            first_widths[i] = $first_cell.outerWidth();
            second_widths[i] = $second_cell.outerWidth();

            // Widths in percentages.
            first_percentages[i] = first_widths[i] / table_width * 100;
            second_percentages[i] = second_widths[i] / table_width * 100;
          }

          // Got through all cells on these columns and update their widths.
          for (i = 0; i < map.length; i++) {
            $first_cell = $(map[i][first]);
            $second_cell = $(map[i][second]);

            // New percentage for the first cell.
            var first_cell_percentage = (first_percentages[i] * (first_widths[i] + release_position - initial_positon) / first_widths[i]).toFixed(4);

            $first_cell.css('width', first_cell_percentage + '%');
            $second_cell.css('width', (first_percentages[i] + second_percentages[i] - first_cell_percentage).toFixed(4) + '%');
          }
        }

        // Resize the table.
        else {
          var $table_parent = $table.parent();
          var table_percentage = table_width / $table_parent.width() * 100;
          var width;

          if (first == null) {
            width = (table_width - release_position + initial_positon) / table_width * table_percentage;
          } else {
            width = (table_width + release_position - initial_positon) / table_width * table_percentage;
          }

          $table.css('width', Math.round(width).toFixed(4) + '%');
        }
      }

      // Clear resizer data.
      $resizer.removeData('origin');
      $resizer.removeData('release-position');
      $resizer.removeData('first');
      $resizer.removeData('second');
      $resizer.removeData('table');

      editor.undo.saveStep();
    }

    /*
     * Get the width of the column. (columns may have colspan)
     */
    function _columnWidth (col, map) {
      var i;
      var width = $(map[0][col]).outerWidth();

      for (i = 1; i < map.length; i++) {
        width = Math.min(width, $(map[i][col]).outerWidth());
      }

      return width;
    }

    /*
     * Get the width of the columns between specified indexes.
     */
    function _columnsWidth(col1, col2, map) {
      var i;
      var width = 0;

      // Sum all columns widths.
      for (i = col1; i <= col2; i++) {
        width += _columnWidth(i, map);
      }

      return width;
    }

    /*
     * Set mouse timer to improve performance.
     */
    function _mouseMove (e) {
      // Prevent selecting text when we have cells selected.
      if (editor.$el.find('.fr-selected-cell').length > 1 && mouseDownFlag) {
        _clearSelection();
      }

      // Reset or set timer.
      if (mouseDownFlag === false && mouseDownCellFlag === false && resizingFlag === false) {
        if (mouseMoveTimer) {
          clearTimeout(mouseMoveTimer);
        }

        // Check tag under in order to place the table resizer or insert helper button.
        mouseMoveTimer = setTimeout(_tagUnder, 30, e);

      // Move table resizer.
      } else if (resizingFlag) {
        // Cursor position.
        var pos = e.pageX - editor.win.pageXOffset;

        if (editor.opts.iframe) {
          pos += editor.$iframe.offset().left;
        }

        // Left and right limits.
        var left_limit = $resizer.data('max-left');
        var right_limit = $resizer.data('max-right');

        // Cursor is between the left and right limits.
        if (pos >= left_limit && pos <= right_limit) {
          $resizer.css('left', pos - editor.opts.tableResizerOffset);

        // Cursor has exceeded the left limit. Don't update if it already has the correct value.
        } else if (pos < left_limit && parseFloat($resizer.css('left'), 10) > left_limit - editor.opts.tableResizerOffset) {
          $resizer.css('left', left_limit - editor.opts.tableResizerOffset);

        // Cursor has exceeded the right limit. Don't update if it already has the correct value.
        } else if (pos > right_limit && parseFloat($resizer.css('left'), 10) < right_limit - editor.opts.tableResizerOffset) {
          $resizer.css('left', right_limit - editor.opts.tableResizerOffset);
        }
      } else if (mouseDownFlag) {
        _hideInsertHelper();
      }
    }

    /*
     * Place selection markers in a table cell.
     */
    function _addMarkersInCell ($cell) {
      if (editor.node.isEmpty($cell.get(0))) {
        $cell.prepend($.FE.MARKERS);
      }
      else {
        $cell.prepend($.FE.START_MARKER).append($.FE.END_MARKER);
      }
    }

    /*
     * Use TAB key to navigate through cells.
     */
    function _useTab (e) {
      var key_code = e.which;

      if (key_code == $.FE.KEYCODE.TAB && editor.opts.tabSpaces === 0) {
        // Get starting cell.
        var $cell;

        if (editor.$el.find('.fr-selected-cell').length > 0) {
          $cell = editor.$el.find('.fr-selected-cell:last')
        }
        else {
          var cell = editor.selection.element();

          if (cell.tagName == 'TD' || cell.tagName == 'TH') {
            $cell = $(cell);
          }
          else if ($(cell).closest('td').length > 0) {
            $cell = $(cell).closest('td');
          }
          else if ($(cell).closest('th').length > 0) {
            $cell = $(cell).closest('th');
          }
        }

        if ($cell) {
          e.preventDefault();

          _removeSelection();
          editor.popups.hide('table.edit');

          // Go backwards.
          if (e.shiftKey) {
            // Go to previous cell.
            if ($cell.prev().length > 0) {
              _addMarkersInCell($cell.prev());
            }

            // Go to prev row, last cell.
            else if ($cell.closest('tr').length > 0 && $cell.closest('tr').prev().length > 0) {
              _addMarkersInCell($cell.closest('tr').prev().find('td:last'));
            }

            // Go in THEAD, last cell.
            else if ($cell.closest('tbody').length > 0 && $cell.closest('table').find('thead tr').length > 0) {
              _addMarkersInCell($cell.closest('table').find('thead tr th:last'));
            }
          }

          // Go forward.
          else {
            // Go to next cell.
            if ($cell.next().length > 0) {
              _addMarkersInCell($cell.next());
            }

            // Go to next row, first cell.
            else if ($cell.closest('tr').length > 0 && $cell.closest('tr').next().length > 0) {
              _addMarkersInCell($cell.closest('tr').next().find('td:first'));
            }

            // Cursor is in THEAD. Go to next row in TBODY
            else if ($cell.closest('thead').length > 0 && $cell.closest('table').find('tbody tr').length > 0) {
              _addMarkersInCell($cell.closest('table').find('tbody tr td:first'));
            }

            // Add new row.
            else {
              $cell.addClass('fr-selected-cell');
              insertRow('below');
              _removeSelection();

              _addMarkersInCell($cell.closest('tr').next().find('td:first'));
            }
          }

          // Update cursor position.
          editor.selection.restore();
        }
      }
    }

    /*
     * Initilize insert helper.
     */
    function _initInsertHelper () {
      // Append insert helper HTML to editor wrapper.
      if (!editor.shared.$ti_helper) {
        editor.shared.$ti_helper = $('<div class="fr-insert-helper"><a class="fr-floating-btn" role="button" tabindex="-1" title="' + editor.language.translate('Insert') + '"><svg viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg"><path d="M22,16.75 L16.75,16.75 L16.75,22 L15.25,22.000 L15.25,16.75 L10,16.75 L10,15.25 L15.25,15.25 L15.25,10 L16.75,10 L16.75,15.25 L22,15.25 L22,16.75 Z"/></svg></a></div>');

        // Click on insert helper.
        editor.events.bindClick(editor.shared.$ti_helper, 'a', function () {
          var $td = $insert_helper.data('selected-cell');
          var position = $insert_helper.data('position');

          var inst = $insert_helper.data('instance') || editor;

          if (position == 'before') {
            $td.addClass('fr-selected-cell');
            inst.table.insertColumn(position);
            $td.removeClass('fr-selected-cell');

          } else if (position == 'after') {
            $td.addClass('fr-selected-cell');
            inst.table.insertColumn(position);
            $td.removeClass('fr-selected-cell');

          } else if (position == 'above') {
            $td.addClass('fr-selected-cell');
            inst.table.insertRow(position);
            $td.removeClass('fr-selected-cell');

          } else if (position == 'below') {
            $td.addClass('fr-selected-cell');
            inst.table.insertRow(position);
            $td.removeClass('fr-selected-cell');
          }

          // Hide the insert helper so it will reposition.
          _hideInsertHelper();
        });
      }

      $insert_helper = editor.shared.$ti_helper;

      // Table insert helper tooltip.
      editor.tooltip.bind(editor.$box, '.fr-insert-helper > a.fr-floating-btn');

      // Editor destroy.
      editor.events.on('shared.destroy', function () {
        $insert_helper.html('').removeData().remove();
      }, true);

      // Prevent the insert helper hide when mouse is over it.
      editor.events.$on($insert_helper, 'mousemove', function (e) {
        e.stopPropagation();
      }, true);

      // Hide the insert helper if the page is scrolled.
      editor.events.$on($(editor.win), 'scroll', function () {
        _hideInsertHelper();
      }, true);
    }

    /*
     * Init table.
     */
    function _init () {
      if (!editor.$wp) return false;

      // Do cell selection only on desktops (no touch devices)
      if (!editor.helpers.isMobile()) {
        // Remember if mouse is clicked.
        mouseDownFlag = false;
        mouseDownCellFlag = false;
        resizingFlag = false;

        // Mouse is down in a table cell.
        editor.events.$on(editor.$el, 'mousedown', _mouseDown);

        // Deselect table cells when user clicks on an image.
        editor.popups.onShow('image.edit', function () {
          _removeSelection();
          mouseDownFlag = false;
          mouseDownCellFlag = false;
        });

        // Deselect table cells when user clicks on a link.
        editor.popups.onShow('link.edit', function () {
          _removeSelection();
          mouseDownFlag = false;
          mouseDownCellFlag = false;
        });

        // Deselect table cells when a command is run.
        editor.events.on('commands.mousedown', function ($btn) {
          if ($btn.parents('.fr-toolbar').length > 0) {
            _removeSelection();
          }
        });

        // Mouse enter's a table cell.
        editor.events.$on(editor.$el, 'mouseenter', 'th, td', _mouseEnter);

        // Mouse is no longer pressed.
        editor.events.$on(editor.$win, 'mouseup', _mouseUp);

        // Iframe mouseup.
        if (editor.opts.iframe) {
          editor.events.$on($(editor.o_win), 'mouseup', _mouseUp);
        }

        // Moving cursor with arrow keys.
        editor.events.$on(editor.$el, 'keydown', _usingArrows);

        // Check tags under the mouse to see if the resizer needs to be shown.
        editor.events.$on(editor.$win, 'mousemove', _mouseMove);

        // Update resizer's position on scroll.
        editor.events.$on($(editor.o_win), 'scroll', _repositionResizer);

        // Reposition table edit popup when table cell content changes.
        editor.events.on('contentChanged', function () {
          if (editor.$el.find('.fr-selected-cell').length > 0) {
            _showEditPopup();

            // Make sure we reposition on image load.
            editor.$el.find('img').on('load.selected-cells', function () {
              $(this).off('load.selected-cells');
              if (editor.$el.find('.fr-selected-cell').length > 0) {
                _showEditPopup();
              }
            });
          }
        });

        // Reposition table edit popup on window resize.
        editor.events.$on($(editor.o_win), 'resize', function () {
          _removeSelection();
        });

        // Prevent backspace from doing browser back.
        editor.events.on('keydown', function (e) {
          if (editor.$el.find('.fr-selected-cell').length > 0) {
            if (e.which == $.FE.KEYCODE.ESC) {
              if (editor.popups.isVisible('table.edit')) {
                _removeSelection();
                editor.popups.hide('table.edit');
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();
                return false;
              }
            }

            if (editor.$el.find('.fr-selected-cell').length > 1) {
              e.preventDefault();
              return false;
            }
          }
        }, true);

        editor.events.$on($(editor.win), 'keydown input keyup', _showEditPopup);

        // Clean selected cells.
        var $selected_cells;
        editor.events.on('html.beforeGet', function () {
          $selected_cells = editor.$el.find('.fr-selected-cell');
          $selected_cells.removeClass('fr-selected-cell');
        });

        editor.events.on('html.get', function (html) {
          html = html.replace(/<(td|th)((?:[\w\W]*?)) class=""((?:[\w\W]*?))>((?:[\w\W]*?))<\/(td|th)>/g, '<$1$2$3>$4</$5>');

          return html;
        });

        editor.events.on('html.afterGet', function () {
          $selected_cells.addClass('fr-selected-cell');
        });

        _initInsertPopup(true);
        _initEditPopup(true);
      }

      // Tab in cell
      editor.events.on('keydown', _useTab, true);
    }

    /*
     * Go back to the table edit popup.
     */
    function back () {
      if (editor.$el.find('.fr-selected-cell').length > 0) {
        _showEditPopup();
      }
      else {
        editor.popups.hide('table.insert');
        editor.toolbar.showInline();
      }
    }

    return {
      _init: _init,
      insert: insert,
      remove: remove,
      insertRow: insertRow,
      deleteRow: deleteRow,
      insertColumn: insertColumn,
      deleteColumn: deleteColumn,
      mergeCells: mergeCells,
      splitCellVertically: splitCellVertically,
      splitCellHorizontally: splitCellHorizontally,
      addHeader: addHeader,
      removeHeader: removeHeader,
      setBackground: setBackground,
      showInsertPopup: _showInsertPopup,
      showEditPopup: _showEditPopup,
      showColorsPopup: _showColorsPopup,
      back: back,
      verticalAlign: verticalAlign,
      horizontalAlign: horizontalAlign,
      applyStyle: applyStyle
    }
  };

  // Insert table button.
  $.FE.DefineIcon('insertTable', { NAME: 'table' });
  $.FE.RegisterCommand('insertTable', {
    title: 'Insert Table',
    undo: false,
    focus: true,
    refreshOnCallback: false,
    popup: true,
    callback: function () {
      if (!this.popups.isVisible('table.insert')) {
        this.table.showInsertPopup();
      }
      else {
        if (this.$el.find('.fr-marker')) {
          this.events.disableBlur();
          this.selection.restore();
        }
        this.popups.hide('table.insert');
      }
    },
    plugin: 'table'
  });

  $.FE.RegisterCommand('tableInsert', {
    callback: function (cmd, rows, cols) {
      this.table.insert(rows, cols);
      this.popups.hide('table.insert');
    }
  })

  // Table header button.
  $.FE.DefineIcon('tableHeader', { NAME: 'header' })
  $.FE.RegisterCommand('tableHeader', {
    title: 'Table Header',
    focus: false,
    callback: function () {
      var $btn = this.popups.get('table.edit').find('.fr-command[data-cmd="tableHeader"]');

      // If button is active the table has a header,
      if ($btn.hasClass('fr-active')) {
        this.table.removeHeader();
      }

      // Add table header.
      else {
        this.table.addHeader();
      }
    },
    refresh: function ($btn) {
      var $table = this.$el.find('.fr-selected-cell').closest('table');

      if ($table.length > 0) {
        // If table doesn't have a header.
        if ($table.find('th').length === 0) {
          $btn.removeClass('fr-active');
        }

        // Header button is active if table has header.
        else {
          $btn.addClass('fr-active');
        }
      }
    }
  });

  // Table rows action dropdown.
  $.FE.DefineIcon('tableRows', { NAME: 'bars' })
  $.FE.RegisterCommand('tableRows', {
    type: 'dropdown',
    focus: false,
    title: 'Row',
    options: {
      above: 'Insert row above',
      below: 'Insert row below',
      'delete': 'Delete row'
    },
    html: function () {
      var c = '<ul class="fr-dropdown-list">';
      var options =  $.FE.COMMANDS.tableRows.options;
      for (var val in options) {
        c += '<li><a class="fr-command" data-cmd="tableRows" data-param1="' + val + '" title="' + this.language.translate(options[val]) + '">' + this.language.translate(options[val]) + '</a></li>';
      }
      c += '</ul>';

      return c;
    },
    callback: function (cmd, val) {
      if (val == 'above' || val == 'below') {
        this.table.insertRow(val);
      } else {
        this.table.deleteRow();
      }
    }
  });

  // Table columns action dropdown.
  $.FE.DefineIcon('tableColumns', { NAME: 'bars fa-rotate-90' })
  $.FE.RegisterCommand('tableColumns', {
    type: 'dropdown',
    focus: false,
    title: 'Column',
    options: {
      before: 'Insert column before',
      after: 'Insert column after',
      'delete': 'Delete column'
    },
    html: function () {
      var c = '<ul class="fr-dropdown-list">';
      var options =  $.FE.COMMANDS.tableColumns.options;
      for (var val in options) {
        c += '<li><a class="fr-command" data-cmd="tableColumns" data-param1="' + val + '" title="' + this.language.translate(options[val]) + '">' + this.language.translate(options[val]) + '</a></li>';
      }
      c += '</ul>';

      return c;
    },
    callback: function (cmd, val) {
      if (val == 'before' || val == 'after') {
        this.table.insertColumn(val);
      } else {
        this.table.deleteColumn();
      }
    }
  });

  // Table cells action dropdown.
  $.FE.DefineIcon('tableCells', { NAME: 'square-o' })
  $.FE.RegisterCommand('tableCells', {
    type: 'dropdown',
    focus: false,
    title: 'Cell',
    options: {
      merge: 'Merge cells',
      'vertical-split': 'Vertical split',
      'horizontal-split': 'Horizontal split'
    },
    html: function () {
      var c = '<ul class="fr-dropdown-list">';
      var options =  $.FE.COMMANDS.tableCells.options;
      for (var val in options) {
        c += '<li><a class="fr-command" data-cmd="tableCells" data-param1="' + val + '" title="' + this.language.translate(options[val]) + '">' + this.language.translate(options[val]) + '</a></li>';
      }
      c += '</ul>';

      return c;
    },
    callback: function (cmd, val) {
      if (val == 'merge') {
        this.table.mergeCells();
      }
      else if (val == 'vertical-split') {
        this.table.splitCellVertically();
      }
      // 'horizontal-split'
      else {
        this.table.splitCellHorizontally();
      }
    },
    refreshOnShow: function ($btn, $dropdown) {
      // More than one cell selected.
      if (this.$el.find('.fr-selected-cell').length > 1) {
        $dropdown.find('a[data-param1="vertical-split"]').addClass('fr-disabled');
        $dropdown.find('a[data-param1="horizontal-split"]').addClass('fr-disabled');
        $dropdown.find('a[data-param1="merge"]').removeClass('fr-disabled');
      }

      // Only one selected cell.
      else {
        $dropdown.find('a[data-param1="merge"]').addClass('fr-disabled');
        $dropdown.find('a[data-param1="vertical-split"]').removeClass('fr-disabled');
        $dropdown.find('a[data-param1="horizontal-split"]').removeClass('fr-disabled');
      }
    }
  });

  // Remove table button.
  $.FE.DefineIcon('tableRemove', { NAME: 'trash' })
  $.FE.RegisterCommand('tableRemove', {
    title: 'Remove Table',
    focus: false,
    callback: function () {
      this.table.remove();
    }
  });

  // Table styles.
  $.FE.DefineIcon('tableStyle', { NAME: 'paint-brush' })
  $.FE.RegisterCommand('tableStyle', {
    title: 'Table Style',
    type: 'dropdown',
    focus: false,
    html: function () {
      var c = '<ul class="fr-dropdown-list">';
      var options =  this.opts.tableStyles;
      for (var val in options) {
        c += '<li><a class="fr-command" data-cmd="tableStyle" data-param1="' + val + '" title="' + this.language.translate(options[val]) + '">' + this.language.translate(options[val]) + '</a></li>';
      }
      c += '</ul>';

      return c;
    },
    callback: function (cmd, val) {
      this.table.applyStyle(val, this.$el.find('.fr-selected-cell').closest('table'), this.opts.tableMultipleStyles, this.opts.tableStyles);
    },
    refreshOnShow: function ($btn, $dropdown) {
      var $table = this.$el.find('.fr-selected-cell').closest('table');

      if ($table) {
        $dropdown.find('.fr-command').each (function () {
          var cls = $(this).data('param1');
          $(this).toggleClass('fr-active', $table.hasClass(cls));
        })
      }
    }
  });

  // Table cell background color button.
  $.FE.DefineIcon('tableCellBackground', { NAME: 'tint' })
  $.FE.RegisterCommand('tableCellBackground', {
    title: 'Cell Background',
    focus: false,
    callback: function () {
      this.table.showColorsPopup();
    }
  });

  // Select table cell background color command.
  $.FE.RegisterCommand('tableCellBackgroundColor', {
    undo: true,
    focus: false,
    callback: function (cmd, val) {
      this.table.setBackground(val);
    }
  });

  // Table back.
  $.FE.DefineIcon('tableBack', { NAME: 'arrow-left' });
  $.FE.RegisterCommand('tableBack', {
    title: 'Back',
    undo: false,
    focus: false,
    back: true,
    callback: function () {
      this.table.back();
    },
    refresh: function ($btn) {
      if (this.$el.find('.fr-selected-cell').length === 0 && !this.opts.toolbarInline) {
        $btn.addClass('fr-hidden');
        $btn.next('.fr-separator').addClass('fr-hidden');
      }
      else {
        $btn.removeClass('fr-hidden');
        $btn.next('.fr-separator').removeClass('fr-hidden');
      }
    }
  });

  // Table vertical align dropdown.
  $.FE.DefineIcon('tableCellVerticalAlign', { NAME: 'arrows-v' })
  $.FE.RegisterCommand('tableCellVerticalAlign', {
    type: 'dropdown',
    focus: false,
    title: 'Vertical Align',
    options: {
      Top: 'Align Top',
      Middle: 'Align Middle',
      Bottom: 'Align Bottom'
    },
    html: function () {
      var c = '<ul class="fr-dropdown-list">';
      var options =  $.FE.COMMANDS.tableCellVerticalAlign.options;
      for (var val in options) {
        c += '<li><a class="fr-command" data-cmd="tableCellVerticalAlign" data-param1="' + val.toLowerCase() + '" title="' + this.language.translate(options[val]) + '">' + this.language.translate(val) + '</a></li>';
      }
      c += '</ul>';

      return c;
    },
    callback: function (cmd, val) {
      this.table.verticalAlign(val);
    },
    refreshOnShow: function ($btn, $dropdown) {
      $dropdown.find('.fr-command[data-param1="' + this.$el.find('.fr-selected-cell').css('vertical-align') + '"]').addClass('fr-active');
    }
  });

  // Table horizontal align dropdown.
  $.FE.DefineIcon('tableCellHorizontalAlign', { NAME: 'align-left' });
  $.FE.DefineIcon('align-left', { NAME: 'align-left' });
  $.FE.DefineIcon('align-right', { NAME: 'align-right' });
  $.FE.DefineIcon('align-center', { NAME: 'align-center' });
  $.FE.DefineIcon('align-justify', { NAME: 'align-justify' });
  $.FE.RegisterCommand('tableCellHorizontalAlign', {
    type: 'dropdown',
    focus: false,
    title: 'Horizontal Align',
    options: {
      left: 'Align Left',
      center: 'Align Center',
      right: 'Align Right',
      justify: 'Align Justify'
    },
    html: function () {
      var c = '<ul class="fr-dropdown-list">';
      var options =  $.FE.COMMANDS.tableCellHorizontalAlign.options;
      for (var val in options) {
        c += '<li><a class="fr-command fr-title" data-cmd="tableCellHorizontalAlign" data-param1="' + val + '" title="' + this.language.translate(options[val]) + '">' + this.icon.create('align-' + val) + '</a></li>';
      }
      c += '</ul>';

      return c;
    },
    callback: function (cmd, val) {
      this.table.horizontalAlign(val);
    },
    refresh: function ($btn) {
      $btn.find('> *:first').replaceWith(this.icon.create('align-' + this.helpers.getAlignment(this.$el.find('.fr-selected-cell:first'))));
    },
    refreshOnShow: function ($btn, $dropdown) {
      $dropdown.find('.fr-command[data-param1="' + this.helpers.getAlignment(this.$el.find('.fr-selected-cell:first')) + '"]').addClass('fr-active');
    }
  });

  // Table cell styles.
  $.FE.DefineIcon('tableCellStyle', { NAME: 'magic' })
  $.FE.RegisterCommand('tableCellStyle', {
    title: 'Cell Style',
    type: 'dropdown',
    focus: false,
    html: function () {
      var c = '<ul class="fr-dropdown-list">';
      var options =  this.opts.tableCellStyles;
      for (var val in options) {
        c += '<li><a class="fr-command" data-cmd="tableCellStyle" data-param1="' + val + '" title="' + this.language.translate(options[val]) + '">' + this.language.translate(options[val]) + '</a></li>';
      }
      c += '</ul>';

      return c;
    },
    callback: function (cmd, val) {
      this.table.applyStyle(val, this.$el.find('.fr-selected-cell'), this.opts.tableCellMultipleStyles, this.opts.tableCellStyles);
    },
    refreshOnShow: function ($btn, $dropdown) {
      var $cell = this.$el.find('.fr-selected-cell:first');

      if ($cell) {
        $dropdown.find('.fr-command').each (function () {
          var cls = $(this).data('param1');
          $(this).toggleClass('fr-active', $cell.hasClass(cls));
        })
      }
    }
  });

}));
