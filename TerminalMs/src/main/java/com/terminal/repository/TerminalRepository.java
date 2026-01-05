package com.terminal.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.terminal.entity.Terminal;
public interface TerminalRepository extends JpaRepository<Terminal, Integer>,JpaSpecificationExecutor<Terminal> {
Terminal findByPublicKey(String publicKey);
}
