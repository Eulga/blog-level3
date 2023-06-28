package com.example.bloghw2.post.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bloghw2.jwtutil.LoginUser;
import com.example.bloghw2.post.Exception.PasswordMismatchException;
import com.example.bloghw2.post.Exception.PostNotFoundException;
import com.example.bloghw2.post.dto.PostRequestDTO;
import com.example.bloghw2.post.dto.PostResponseDTO;
import com.example.bloghw2.post.service.PostService;
import com.example.bloghw2.user.entity.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;


    // 게시글 생성
    @PostMapping("/posts")
    public ResponseEntity<PostResponseDTO> createPost(@Valid @RequestBody PostRequestDTO postRequestDTO, @LoginUser User user) {
        String username = user.getUsername();
        PostResponseDTO response = postService.createPost(postRequestDTO, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 게시글 전체 조회
    @GetMapping("/posts")
    public ResponseEntity<List<PostResponseDTO>> getPosts() {
        List<PostResponseDTO> response = postService.getPosts();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    // 게시글 지정 조회
    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostResponseDTO> getPost(@PathVariable("postId") Long postId) {
        PostResponseDTO response = postService.getPost(postId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    // 게시글 수정
    @PutMapping("/posts/{postId}")
    public ResponseEntity<PostResponseDTO> modifyPost(@PathVariable("postId") Long postId,@Valid @RequestBody PostRequestDTO postRequestDTO
    , @LoginUser User user) {
        String username = user.getUsername();
        PostResponseDTO response = postService.modifyPost(postId, postRequestDTO, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    //게시글 삭제
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Map<String,String>> deletePost(@PathVariable("postId") Long postId, @LoginUser User user){
        String username = user.getUsername();
        Map<String, String> response = postService.deletePost(postId, username);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    // 예외 관리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String,String>> handleException(Exception ex){
        Map<String, String> result = Collections.singletonMap("success", "false");
        if (ex instanceof PasswordMismatchException){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        } else if (ex instanceof PostNotFoundException){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleException(MethodArgumentNotValidException ex, HttpStatus status){
        Map<String, Object> result = new HashMap<>();
        result.put("success", "false");
        result.put("message", ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        return ResponseEntity.status(status.value()).body(result);
    }
}
