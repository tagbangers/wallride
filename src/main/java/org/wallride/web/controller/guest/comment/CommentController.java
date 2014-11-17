package org.wallride.web.controller.guest.comment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.wallride.core.domain.BlogLanguage;
import org.wallride.core.domain.Comment;
import org.wallride.core.service.CommentService;
import org.wallride.core.service.CreateCommentRequest;
import org.wallride.core.support.AuthorizedUser;
import org.wallride.web.support.RestValidationErrorModel;

import javax.inject.Inject;

@RestController
@RequestMapping("/comments")
public class CommentController {

	@Inject
	private CommentService commentService;
	@Inject
	private MessageSourceAccessor messageSourceAccessor;

	private static Logger logger = LoggerFactory.getLogger(CommentController.class);

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public RestValidationErrorModel bindException(BindException e) {
		logger.debug("BindException", e);
		return RestValidationErrorModel.fromBindingResult(e.getBindingResult(), messageSourceAccessor);
	}

	@RequestMapping(value = "/new", method = RequestMethod.POST)
	public Comment create(
			@Validated CommentForm form,
			BindingResult result,
			BlogLanguage blogLanguage,
			AuthorizedUser authorizedUser) throws BindException {
		if (result.hasErrors()) {
			throw new BindException(result);
		}

		CreateCommentRequest request = form.toCreateCommentRequest(blogLanguage, authorizedUser);
		Comment comment = commentService.createComment(request, authorizedUser);
		return comment;
	}

	@RequestMapping(value = "/{id}/edit", method = RequestMethod.PUT)
	public String update() {
		return "";
	}

	@RequestMapping(value = "/{id}/delete", method = RequestMethod.DELETE)
	public String delete() {
		return "";
	}
}
