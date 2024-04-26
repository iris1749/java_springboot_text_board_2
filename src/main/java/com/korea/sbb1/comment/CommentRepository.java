package com.korea.sbb1.comment;

import com.korea.sbb1.answer.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByAnswer(Answer answer);
}
