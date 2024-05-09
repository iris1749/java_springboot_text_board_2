package com.korea.sbb1.user.account;

import com.korea.sbb1.DataNotFoundException;
import com.korea.sbb1.user.password.ChangePasswordForm;
import com.korea.sbb1.user.password.TempPasswordForm;
import com.korea.sbb1.user.password.TempPasswordMail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private static final String TEMP_PASSWORD_FORM = "temp_password_form";

    @GetMapping("/login")
    public String login() {
        return "login_form";
    }

    @GetMapping("/login/oauth2/code/google")
    public String sociallogin() {
        return "login_form";
    }

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup_form";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 패스워드가 일치하지 않습니다.");
            return "signup_form";
        }

        try {
            userService.create(userCreateForm.getUsername(),
                    userCreateForm.getEmail(), userCreateForm.getPassword1());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "signup_form";
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup_form";
        }
        return "redirect:/";

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/changepassword")
    public String changePassword(Model model) {
        // 인증된 사용자인지 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("changePasswordForm", new ChangePasswordForm()); // 변경
            return "changepassword_form";
        } else {
            // 로그인하지 않은 사용자에게는 다른 페이지를 보여줄 수 있음
            return "redirect:/login";
        }
    }

    @PostMapping("/changepassword")
    public String changePassword(@Valid ChangePasswordForm changingPasswordForm,
                                 BindingResult bindingResult, Model model) {
        // 비밀번호 변경 폼의 유효성 검사
        if (bindingResult.hasErrors()) {
            return "changepassword_form";
        }

        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 기존 비밀번호 확인
        if (!userService.getUser(username).getPassword().equals(changingPasswordForm.getPassword())) {
            bindingResult.rejectValue("password", "incorrectPassword", "기존 비밀번호가 일치하지 않습니다.");
        }

        // 새로운 비밀번호 확인
        if (!changingPasswordForm.getNewpassword1().equals(changingPasswordForm.getNewpassword2())) {
            bindingResult.rejectValue("newpassword2", "newpasswordMismatch", "새 비밀번호가 일치하지 않습니다.");
        }

        // 새로운 비밀번호로 업데이트하기
        boolean passwordChanged = userService.changePassword(username, changingPasswordForm.getPassword(), changingPasswordForm.getNewpassword1(), changingPasswordForm.getNewpassword2());

        switch (passwordChanged ? "success" : "failure") {
            case "success":
                // 비밀번호 변경 성공
                return "redirect:/";
            case "failure":
                // 비밀번호 변경 실패
                bindingResult.reject("passwordChangeFailed", "비밀번호를 변경할 수 없습니다. 다시 시도하세요.");
                return "changepassword_form";
            default:
                return "changepassword_form";
        }
    }

    @GetMapping("/forgotpassword")
    public String sendTempPassword(TempPasswordForm tempPasswordForm) {
        return "temp_password";
    }

    @PostMapping("/forgotpassword")
    public String sendTempPassword(@Valid TempPasswordForm tempPasswordForm,
                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "temp_password";
        }

        try {
            userService.sendTempPassword(tempPasswordForm.getUsername(), tempPasswordForm.getEmail());
        } catch (DataNotFoundException e) {
            e.printStackTrace();
            bindingResult.reject("userNotFound", e.getMessage());
            return "temp_password";
        } catch (TempPasswordMail.EmailException e) {
            e.printStackTrace();
            bindingResult.reject("sendEmailFail", e.getMessage());
            return "temp_password";
        }
        return "redirect:/";
    }
}

