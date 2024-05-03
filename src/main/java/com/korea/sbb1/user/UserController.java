package com.korea.sbb1.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
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

    @GetMapping("/login")
    public String login() {
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
    public String showChangePasswordForm(Model model) {
        // 인증된 사용자인지 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("changePasswordForm", new ChangingPasswordForm()); // 변경
            return "changepassword_form";
        } else {
            // 로그인하지 않은 사용자에게는 다른 페이지를 보여줄 수 있음
            return "redirect:/login";
        }
    }

    @PostMapping("/changepassword")
    public String changePassword(@Valid ChangingPasswordForm changingPasswordForm,
                                 BindingResult bindingResult, Model model) {
        // 비밀번호 변경 폼의 유효성 검사
        if (bindingResult.hasErrors()) {
            // 에러가 있는 경우 변경 비밀번호 폼을 다시 보여줌
            model.addAttribute("changePasswordForm", changingPasswordForm);
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
        boolean passwordChanged = userService.changePassword(username, changingPasswordForm.getPassword(), changingPasswordForm.getNewpassword1());

        if (passwordChanged) {
            // 비밀번호 변경 성공
            return "redirect:/";
        } else {
            // 비밀번호 변경 실패
            bindingResult.reject("passwordChangeFailed", "비밀번호를 변경할 수 없습니다. 다시 시도하세요.");
        }

        // 에러가 있는 경우 변경 비밀번호 폼을 다시 보여줌
        model.addAttribute("changePasswordForm", changingPasswordForm);
        return "changepassword_form";
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/forgotPassword")
    public String findPassword() {
        return "forgot_password";
    }

}

