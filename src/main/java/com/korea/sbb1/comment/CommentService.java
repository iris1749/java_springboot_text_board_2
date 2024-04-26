package com.korea.sbb1.comment;

import com.korea.sbb1.DataNotFoundException;
import com.korea.sbb1.answer.Answer;
import com.korea.sbb1.question.Question;
import com.korea.sbb1.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public Comment create(Answer answer, String content, SiteUser author) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setCreateDate(LocalDateTime.now());
        comment.setAnswer(answer);
        comment.setAuthor(author);
        this.commentRepository.save(comment);
        return comment;
    }

    public List<Comment> getCommentsByAnswer(Answer answer) {
        return commentRepository.findByAnswer(answer);
    }

    public Comment getComment(Integer id) {
        Optional<Comment> comment = this.commentRepository.findById(id);
        if (comment.isPresent()) {
            return comment.get();
        } else {
            throw new DataNotFoundException("comment not found");
        }
    }

    public void modify(Comment comment, String content) {
        comment.setContent(content);
        comment.setModifyDate(LocalDateTime.now());
        this.commentRepository.save(comment);
    }

    public void delete(Comment comment) {
        this.commentRepository.delete(comment);
    }

    public void vote(Comment comment, SiteUser siteUser) {
        // 투표 로직 구현 (중복 투표 방지 등)
        // 예시: comment.getVoter().add(siteUser); 를 사용하지 않고 투표 여부 확인 후 추가
        // 이미 투표한 경우 예외처리 등 필요
    }
}
