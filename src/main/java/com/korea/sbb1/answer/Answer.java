package com.korea.sbb1.answer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.korea.sbb1.comment.Comment;
import com.korea.sbb1.question.Question;

import com.korea.sbb1.user.account.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createDate;

    @ManyToOne
    private Question question;

    @ManyToOne
    private SiteUser author;

    private LocalDateTime modifyDate;

    @ManyToMany
    Set<SiteUser> voter;

    @OneToMany(mappedBy = "answer", cascade = CascadeType.REMOVE)
    private List<Comment> commentList; // 대답에 속한 답글 컬렉션 필드
}