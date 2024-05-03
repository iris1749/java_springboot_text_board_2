package com.korea.sbb1.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangingPasswordForm {

    @NotEmpty(message = "기존 비밀번호를 입력해주세요.")
    private String password;

    @NotEmpty(message = "변경할 비밀번호는 필수항목입니다.")
    private String newpassword1;

    @NotEmpty(message = "변경할 비밀번호 확인은 필수항목입니다.")
    private String newpassword2;

}
