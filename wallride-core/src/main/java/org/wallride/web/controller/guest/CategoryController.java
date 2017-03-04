package org.wallride.web.controller.guest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;
import org.wallride.domain.BlogLanguage;
import org.wallride.domain.Post;
import org.wallride.domain.Category;
import org.wallride.model.PostSearchRequest;
import org.wallride.model.CategorySearchRequest;
import org.wallride.service.PostService;
import org.wallride.service.CategoryService;
import org.wallride.web.support.HttpNotFoundException;
import org.wallride.web.support.Pagination;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/category")
public class CategoryController {

    @Inject
    private CategoryService categoryService;
    @Inject
    private PostService postService;

    @RequestMapping
    public String index(
            @PageableDefault Pageable pageable,
            Model model,
            HttpServletRequest servletRequest) {
        Page<Category> categories = categoryService.getCategories(new CategorySearchRequest(), pageable);
        model.addAttribute("categories", categories);
        model.addAttribute("pageable", pageable);
        model.addAttribute("pagination", new Pagination<>(categories, servletRequest));
        return "category/index";
    }

    @RequestMapping("/**/{code}")
    public String post(
            @PathVariable String code,
            @PageableDefault Pageable pageable,
            BlogLanguage blogLanguage,
            Model model,
            HttpServletRequest servletRequest) {
        String path = extractPathFromPattern(servletRequest);
        String[] codes = path.split("/");
        String lastCode = codes[codes.length - 1];

        Category category = categoryService.getCategoryByCode(lastCode, blogLanguage.getLanguage());
        if (category == null) {
            category = categoryService.getCategoryByCode(lastCode, blogLanguage.getBlog().getDefaultLanguage());
        }
        if (category == null) {
            throw new HttpNotFoundException();
        }

        PostSearchRequest request = new PostSearchRequest(category.getLanguage());
        request.withCategoryCodes(code);

        Page<Post> posts = postService.getPosts(request, pageable);
        model.addAttribute("category", category);
        model.addAttribute("posts", posts);
        model.addAttribute("pageable", pageable);
        model.addAttribute("pagination", new Pagination<>(posts, servletRequest));
        return "category/post";
    }

    private String extractPathFromPattern(final HttpServletRequest request){
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String ) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

        AntPathMatcher apm = new AntPathMatcher();
        String finalPath = apm.extractPathWithinPattern(bestMatchPattern, path);

        return finalPath;
    }
    
}
