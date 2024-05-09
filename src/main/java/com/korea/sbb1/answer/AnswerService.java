package com.korea.sbb1.answer;

import com.korea.sbb1.DataNotFoundException;
import com.korea.sbb1.question.Question;
import com.korea.sbb1.user.account.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AnswerService {

    private final AnswerRepository answerRepository;

    public Answer create(Question question, String content, SiteUser author) {
        Answer answer = new Answer();
        answer.setContent(content);
        answer.setCreateDate(LocalDateTime.now());
        answer.setQuestion(question);
        answer.setAuthor(author);
        this.answerRepository.save(answer);
        return answer;
    }

    public Page<Answer> getListByQuestion(Question question, int page, String sortOption) {
        Pageable pageable;
        if (sortOption.equals("latest")) {
            pageable = PageRequest.of(page, 5, Sort.by("createDate").descending());
        } else if (sortOption.equals("earliest")) {
            pageable = PageRequest.of(page, 5, Sort.by("createDate").ascending());
        } else if (sortOption.equals("recommended")) {
            pageable = PageRequest.of(page, 5, Sort.by("voter").descending());
        } else {
            pageable = PageRequest.of(page, 5, Sort.by("createDate").descending());
        }
        return answerRepository.findByQuestion(question, pageable);
    }



    public Answer getAnswer(Integer id) {
        Optional<Answer> answer = this.answerRepository.findById(id);
        if (answer.isPresent()) {
            return answer.get();
        } else {
            throw new DataNotFoundException("answer not found");
        }
    }

    public void modify(Answer answer, String content) {
        answer.setContent(content);
        answer.setModifyDate(LocalDateTime.now());
        this.answerRepository.save(answer);
    }

    public void delete(Answer answer) {
        this.answerRepository.delete(answer);
    }

    public  void vote(Answer answer, SiteUser siteUser){
        answer.getVoter().add(siteUser);
        this.answerRepository.save(answer);
    }

}
