package com.korea.sbb1.comment;

import com.korea.sbb1.DataNotFoundException;
import com.korea.sbb1.answer.Answer;
import com.korea.sbb1.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository CommentRepository;

    public Comment create(Answer answer, String content, SiteUser author) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setCreateDate(LocalDateTime.now());
        comment.setAnswer(answer);
        comment.setAuthor(author);
        this.CommentRepository.save(comment);
        return comment;
    }

    public Comment getComment(Integer id) {
        Optional<Comment> comment = this.CommentRepository.findById(id);
        if (comment.isPresent()) {
            return comment.get();
        } else {
            throw new DataNotFoundException("comment not found");
        }
    }

    public void modify(Comment comment, String content) {
        comment.setContent(content);
        comment.setModifyDate(LocalDateTime.now());
        this.CommentRepository.save(comment);
    }

    public void delete(Comment comment) {
        this.CommentRepository.delete(comment);
    }

    public void vote(Comment comment, SiteUser siteUser) {
        // 투표 로직 구현 (중복 투표 방지 등)
        // 예시: comment.getVoter().add(siteUser); 를 사용하지 않고 투표 여부 확인 후 추가
        // 이미 투표한 경우 예외처리 등 필요
    }
}
