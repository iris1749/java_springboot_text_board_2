package com.korea.sbb1.question;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.korea.sbb1.DataNotFoundException;
import com.korea.sbb1.answer.Answer;
import com.korea.sbb1.user.SiteUser;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    public Specification<Question> search(String kw, String searchoption) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (kw.isEmpty()) {
                // 키워드가 비어있으면 아무 조건도 추가하지 않음
                return criteriaBuilder.conjunction();
            }

            switch (searchoption) {
                case "title":
                    predicates.add(criteriaBuilder.like(root.get("subject"), "%" + kw + "%"));
                    break;

                case "author":
                    Join<Question, SiteUser> authorJoin = root.join("author", JoinType.LEFT);
                    predicates.add(criteriaBuilder.like(authorJoin.get("username"), "%" + kw + "%"));
                    break;

                case "comment":
                    Join<Question, Answer> answerJoin = root.join("answerList", JoinType.LEFT);
                    predicates.add(criteriaBuilder.like(answerJoin.get("content"), "%" + kw + "%"));
                    break;

                case "all":
                    Join<Question, SiteUser> allAuthorJoin = root.join("author", JoinType.LEFT);
                    Join<Question, Answer> allAnswerJoin = root.join("answerList", JoinType.LEFT);
                    Join<Answer, SiteUser> allCommenterJoin = allAnswerJoin.join("author", JoinType.LEFT);
                    predicates.add(criteriaBuilder.or(
                            criteriaBuilder.like(root.get("subject"), "%" + kw + "%"),
                            criteriaBuilder.like(allAuthorJoin.get("username"), "%" + kw + "%"),
                            criteriaBuilder.like(allAnswerJoin.get("content"), "%" + kw + "%"),
                            criteriaBuilder.like(allCommenterJoin.get("username"), "%" + kw + "%")
                    ));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid search option: " + searchoption);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Page<Question> getList(int page, String kw, String searchoption) {

        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        Specification<Question> specification = search(kw, searchoption);

        Page<Question> result = questionRepository.findAll(specification, pageable);

        return result;
    }

    public Question getQuestion(Integer id) {
        Optional<Question> question = this.questionRepository.findById(id);
        if (question.isPresent()) {
            return question.get();
        } else {
            throw new DataNotFoundException("question not found");
        }
    }

    public void create(String subject, String content, SiteUser user) {
        Question q = new Question();
        q.setSubject(subject);
        q.setContent(content);
        q.setCreateDate(LocalDateTime.now());
        q.setAuthor(user);
        this.questionRepository.save(q);
    }

    public void modify(Question question, String subject, String content) {
        question.setSubject(subject);
        question.setContent(content);
        question.setModifyDate(LocalDateTime.now());
        this.questionRepository.save(question);
    }

    public void delete(Question question) {
        this.questionRepository.delete(question);
    }

    public void vote(Question question, SiteUser siteUser){
        question.getVoter().add(siteUser);
        this.questionRepository.save(question);
    }

}
