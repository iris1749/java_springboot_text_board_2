package com.korea.sbb1.user;

import com.korea.sbb1.DataNotFoundException;
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
    public boolean changePassword(String username, String password, String newpassword) {
        // 사용자 정보 가져오기
        SiteUser user = userRepository.findByusername(username).orElseThrow(() -> new DataNotFoundException("User not found"));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            // 현재 비밀번호가 일치하지 않으면 false 반환
            return false;
        }

        // 새로운 비밀번호로 업데이트
        user.setPassword(passwordEncoder.encode(newpassword));
        userRepository.save(user);
        return true;
    }


}
