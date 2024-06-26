package com.korea.sbb1.question;

import com.korea.sbb1.answer.Answer;
import com.korea.sbb1.answer.AnswerForm;
import com.korea.sbb1.answer.AnswerService;
import com.korea.sbb1.comment.Comment;
import com.korea.sbb1.comment.CommentForm;
import com.korea.sbb1.comment.CommentService;
import com.korea.sbb1.user.SiteUser;
import com.korea.sbb1.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/question")
@RequiredArgsConstructor
@Controller
public class QuestionController {

    private final CommentService commentService;
    private final QuestionService questionService;
    private final UserService userService;

    private final AnswerService answerService;

    @GetMapping("/list")
    public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "search_kw", defaultValue = "") String kw,
                       @RequestParam(value = "searchoption", defaultValue = "all") String searchoption) {
        Page<Question> paging = this.questionService.getList(page, kw, searchoption);
        model.addAttribute("paging", paging);
        model.addAttribute("search_kw", kw);
        return "question_list";
    }

    @GetMapping(value = "/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id, AnswerForm answerForm, CommentForm commentForm,
                         @RequestParam(name = "sortoption", defaultValue = "latest") String sortoption,
                         @RequestParam(name = "answer_page", defaultValue = "0") int answer_page) {

        Question question = this.questionService.getQuestion(id);
        questionService.updateView(id);
        model.addAttribute("question", question);

        // 정렬 옵션에 따라 다른 정렬 기준으로 페이지를 조회
        Page<Answer> paging = this.answerService.getListByQuestion(question, answer_page, sortoption);
        model.addAttribute("paging", paging);

        // 각 답변에 대한 댓글을 가져와서 리스트에 추가
        List<Comment> commentList = new ArrayList<>();
        for (Answer answer : paging.getContent()) {
            List<Comment> commentsForAnswer = this.commentService.getCommentsByAnswer(answer);
            commentList.addAll(commentsForAnswer);
        }
        model.addAttribute("comment", commentList);

        return "question_detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String questionCreate(QuestionForm questionForm) {
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String questionCreate(@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.questionService.create(questionForm.getSubject(), questionForm.getContent(), siteUser, questionForm.getCategory());
        return "redirect:/question/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(QuestionForm questionForm, @PathVariable("id") Integer id, Principal principal){
        Question question = this.questionService.getQuestion(id);
        if(!question.getAuthor().getUsername().equals(principal.getName())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }

        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        questionForm.setCategory(question.getCategory());
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(@Valid QuestionForm questionForm, BindingResult bindingResult,
                                 Principal principal, @PathVariable("id") Integer id) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent(), questionForm.getCategory());
        return String.format("redirect:/question/detail/%s", id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.questionService.delete(question);
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") Integer id){
        Question question = this.questionService.getQuestion(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.questionService.vote(question, siteUser);
        return String.format("redirect:/question/detail/%s", id);
    }

}