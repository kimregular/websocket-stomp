package com.example.chatserver.chat.repository;

import com.example.chatserver.chat.domain.ChatMessage;
import com.example.chatserver.chat.domain.ReadStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadStatusRepository extends CrudRepository<ReadStatus, Long> {
}
