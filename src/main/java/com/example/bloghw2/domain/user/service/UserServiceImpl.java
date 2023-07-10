package com.example.bloghw2.domain.user.service;

import com.example.bloghw2.domain.user.dto.LoginRequestDTO;
import com.example.bloghw2.domain.user.dto.LoginResponseDTO;
import com.example.bloghw2.domain.user.dto.SignupRequestDTO;
import com.example.bloghw2.domain.user.dto.SignupResponseDTO;
import com.example.bloghw2.domain.user.entity.User;
import com.example.bloghw2.domain.user.entity.UserRoleEnum;
import com.example.bloghw2.domain.user.exception.AdminTokenMismatchException;
import com.example.bloghw2.domain.user.repository.UserRepository;
import com.example.bloghw2.global.jwtutil.JwtProvider;
import com.example.bloghw2.domain.user.exception.PasswordMismatchException;
import com.example.bloghw2.domain.user.exception.UserDuplicationException;
import com.example.bloghw2.domain.user.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // ADMIN_TOKEN
    private final String ADMIN_TOKEN = "AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC";

    @Transactional
    @Override
    public SignupResponseDTO signup(SignupRequestDTO signupRequestDTO) {
        String username = signupRequestDTO.getUsername();
        String password = passwordEncoder.encode(signupRequestDTO.getPassword());

        // 회원 중복 확인
        Optional<User> findUser = userRepository.findByUsername(username);
        if (findUser.isPresent()){
            throw new UserDuplicationException("아이디 중복");
        }

        // 사용자 ROLE 확인
        UserRoleEnum role = UserRoleEnum.USER;
        if (signupRequestDTO.isAdmin()) {
            if (!ADMIN_TOKEN.equals(signupRequestDTO.getAdminToken())) {
                throw new AdminTokenMismatchException("잘못된 관리자 암호");
            }
            role = UserRoleEnum.ADMIN;
        }

        // 사용자 등록
        User user = User.builder()
            .username(username)
            .password(password)
            .role(role)
            .build();
        userRepository.save(user);

        return new SignupResponseDTO("true",201);
    }

    @Transactional
    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO, HttpServletResponse res) {
        String username = loginRequestDTO.getUsername();
        String rawPassword = loginRequestDTO.getPassword();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("등록된 사용자가 아닙니다."));

        if (!passwordEncoder.matches(rawPassword,user.getPassword())){
            throw new PasswordMismatchException("비밀번호 오류");
        }

        String accessToken = jwtProvider.createToken(username);
        jwtProvider.addJwtToHeader(accessToken, res);
        return new LoginResponseDTO("true",200);
    }
}