package com.example.chatserver.member.service;

import com.example.chatserver.member.domain.Member;
import com.example.chatserver.member.dto.MemberLoginRequest;
import com.example.chatserver.member.dto.MemberResponse;
import com.example.chatserver.member.dto.MemberSaveRequest;
import com.example.chatserver.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member save(MemberSaveRequest request) {
        // 이미 가입 되었다면 에러
        if(memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Member member = Member.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .name(request.getName())
                .build();

        return memberRepository.save(member);
    }

    public Member login(MemberLoginRequest memberLoginRequest) {
        Member member = memberRepository.findByEmail(memberLoginRequest.getEmail()).orElseThrow(() -> new IllegalArgumentException("Email not found"));
        if(!member.getPassword().equals(memberLoginRequest.getPassword())) {
            throw new IllegalArgumentException("Wrong password");
        }
        return member;
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream().map(m -> new MemberResponse(m.getId(), m.getEmail(), m.getName())).toList();
    }
}
