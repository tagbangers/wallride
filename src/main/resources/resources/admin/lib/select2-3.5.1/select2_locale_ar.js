/**﻿
 * Select2 Arabic translation.
 *
 * Author: Adel KEDJOUR <adel@kedjour.com>
 */
(function ($) {
    "use strict";

    $.fn.select2.locales['ar'] = {
        formatNoMatches: function () { return "لم يتم العثور على مطابقات"; },
        formatInputTooShort: function (input, min) { var n = min - input.length; if (n == 1){ return "الرجاء إدخال حر�? واحد على الأكثر"; } return n == 2 ? "الرجاء إدخال حر�?ين على الأكثر" : "الرجاء إدخال " + n + " على الأكثر"; },
        formatInputTooLong: function (input, max) { var n = input.length - max; if (n == 1){ return "الرجاء إدخال حر�? واحد على الأقل"; } return n == 2 ? "الرجاء إدخال حر�?ين على الأقل" : "الرجاء إدخال " + n + " على الأقل "; },
        formatSelectionTooBig: function (limit) { if (n == 1){ return "يمكنك أن تختار إختيار واحد �?قط"; } return n == 2 ? "يمكنك أن تختار إختيارين �?قط" : "يمكنك أن تختار " + n + " إختيارات �?قط"; },
        formatLoadMore: function (pageNumber) { return "تحميل المزيد من النتائج…"; },
        formatSearching: function () { return "البحث…"; }
    };

    $.extend($.fn.select2.defaults, $.fn.select2.locales['ar']);
})(jQuery);
