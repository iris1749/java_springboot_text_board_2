package com.korea.sbb1.user;

import com.korea.sbb1.CommonUtil;
import com.korea.sbb1.DataNotFoundException;
import com.korea.sbb1.user.TempPasswordMail.EmailException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TempPasswordMail tempPasswordMail;
    private final CommonUtil commonUtil;

    public SiteUser create(String username, String email, String password){
        SiteUser user = new SiteUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        this.userRepository.save(user);
        return user;
    }

    public SiteUser getUser(String username){
        Optional<SiteUser> siteUser = this.userRepository.findByusername(username);
        if (siteUser.isPresent()){
            return siteUser.get();
        } else{
            throw new DataNotFoundException("siteuser not found");
        }
    }

    // 기존 비밀번호를 사용하여 비밀번호를 변경하는 메서드
    public boolean changePassword(String username, String password, String newpassword1, String newpassword2) {
        // 사용자 정보 가져오기
        SiteUser user = userRepository.findByusername(username).orElseThrow(() -> new DataNotFoundException("User not found"));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            // 현재 비밀번호가 일치하지 않으면 false 반환
            return false;
        }

        // 새로운 비밀번호 확인
        if (!newpassword1.equals(newpassword2)) {
            // 새로운 비밀번호와 확인용 비밀번호가 일치하지 않으면 false 반환
            return false;
        }

        // 새로운 비밀번호로 업데이트
        user.setPassword(passwordEncoder.encode(newpassword1));
        userRepository.save(user);
        return true;
    }

    @Transactional
    public void sendTempPassword(String username, String email) {
        try {
            // 사용자 ID와 이메일이 일치하는지 확인
            SiteUser user = userRepository.findByUsernameAndEmail(username, email)
                    .orElseThrow(() -> new DataNotFoundException("해당 사용자 ID와 이메일이 일치하는 사용자가 없습니다."));

            // 임시 비밀번호 생성
            String tempPassword = commonUtil.createTempPassword();

            // 임시 비밀번호로 사용자의 비밀번호 업데이트
            user.setPassword(passwordEncoder.encode(tempPassword));
            userRepository.save(user);

            // 임시 비밀번호를 이메일로 전송
            tempPasswordMail.sendSimpleMessage(email, tempPassword);
        } catch (EmailException e) {
            e.printStackTrace();
            // 이메일 예외 처리 로직 작성
            // 예외 처리 로직을 작성하여 사용자에게 적절한 오류 메시지를 제공하거나 로그를 기록할 수 있습니다.
        }
    }


}
