package com.korea.sbb1.answer;

import com.korea.sbb1.answer.Answer;
import com.korea.sbb1.question.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {

}