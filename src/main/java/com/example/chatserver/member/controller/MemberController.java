package com.example.chatserver.member.controller;

import com.example.chatserver.common.auth.JwtProvider;
import com.example.chatserver.member.domain.Member;
import com.example.chatserver.member.dto.MemberLoginRequest;
import com.example.chatserver.member.dto.MemberSaveRequest;
import com.example.chatserver.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    @PostMapping("/save")
    public ResponseEntity<?> saveMember(@RequestBody MemberSaveRequest memberSaveRequest) {
        Member member = memberService.save(memberSaveRequest);
        return new ResponseEntity<>(member, HttpStatus.CREATED);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody MemberLoginRequest memberLoginRequest) {
        Member member = memberService.login(memberLoginRequest);

        String token = jwtProvider.ceateToken(member.getEmail(), member.getRole().toString());
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", member.getId());
        loginInfo.put("token", token);
        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<?> getMembers() {
        return new ResponseEntity<>(memberService.findAll(), HttpStatus.OK);
    }
}
