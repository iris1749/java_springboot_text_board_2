package com.korea.sbb1.comment;

import com.korea.sbb1.answer.Answer;
import com.korea.sbb1.answer.AnswerService;
import com.korea.sbb1.question.Question;
import com.korea.sbb1.question.QuestionService;
import com.korea.sbb1.user.SiteUser;
import com.korea.sbb1.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RequestMapping("/comment")
@RequiredArgsConstructor
@Controller
public class CommentController {

    private final QuestionService questionService;
    private final AnswerService answerService;
    private final CommentService commentService;
    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{id}")
    public String createComment(Model model, @PathVariable("id") Integer id, @Valid CommentForm commentForm,
                                BindingResult bindingResult, Principal principal) {
        Answer answer = this.answerService.getAnswer(id); // 답변 객체 가져오기
        SiteUser siteUser = this.userService.getUser(principal.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("answer", answer); // 답변 정보를 모델에 추가
            return "question_detail"; // 답변 상세 페이지로 이동
        }

        Comment comment = this.commentService.create(answer, commentForm.getContent(), siteUser);
        return String.format("redirect:/question/detail/%s#answer_%s",
                answer.getQuestion().getId(), answer.getId());

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String commentModify(CommentForm commentForm, @PathVariable("id") Integer id, Principal principal) {
        Comment comment = this.commentService.getComment(id);
        if (!comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }

        commentForm.setContent(comment.getContent());
        return "comment_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String commentModify(@Valid CommentForm commentForm, BindingResult bindingResult,
                                @PathVariable("id") Integer id, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "comment_form";
        }
        Comment comment = this.commentService.getComment(id);
        if (!comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.commentService.modify(comment, commentForm.getContent());
        return String.format("redirect:/question/detail/%s#answer_%s",
                comment.getAnswer().getQuestion().getId(), comment.getAnswer().getId());
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String commentDelete(Principal principal, @PathVariable("id") Integer id) {
        Comment comment = this.commentService.getComment(id);
        if (!comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.commentService.delete(comment);
        return String.format("redirect:/question/detail/%s#answer_%s",
                comment.getAnswer().getQuestion().getId(), comment.getAnswer().getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String commentVote(Principal principal, @PathVariable("id") Integer id) {
        Comment comment = this.commentService.getComment(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.commentService.vote(comment, siteUser);
        return String.format("redirect:/question/detail/%s#answer_%s",
                comment.getAnswer().getQuestion().getId(), comment.getAnswer().getId());
    }
}
