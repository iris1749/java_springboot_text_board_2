package com.korea.sbb1.question;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

    Page<Question> findAll(Pageable pageable);
    Page<Question> findAll(Specification<Question> spec, Pageable pageable);

    @Query("SELECT DISTINCT q FROM Question q " +
            "LEFT JOIN q.author u1 " +
            "LEFT JOIN q.answerList a " +
            "LEFT JOIN a.author u2 " +
            "WHERE (:searchoption = 'title' AND q.subject LIKE '%' || :kw || '%') OR " +
            "(:searchoption = 'author' AND u1.username LIKE '%' || :kw || '%') OR " +
            "(:searchoption = 'comment' AND (a.content LIKE '%' || :kw || '%' OR u2.username LIKE '%' || :kw || '%')) OR " +
            "(:searchoption = 'all' AND (q.subject LIKE '%' || :kw || '%' OR u1.username LIKE '%' || :kw || '%' OR a.content LIKE '%' || :kw || '%' OR u2.username LIKE '%' || :kw || '%'))")

    Page<Question> findAllByKeywordAndSearchoption(@Param("searchoption") String searchoption, @Param("kw") String kw, Pageable pageable);

    @Modifying
    @Query("update Question q set q.view = q.view + 1 where q.id = :id")
    int updateView(@Param("id") Integer id);
}


