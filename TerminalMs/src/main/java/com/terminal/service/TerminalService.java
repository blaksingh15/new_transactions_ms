package com.terminal.service;

import java.util.List;
import java.util.Map;

import com.terminal.entity.Terminal;

public interface TerminalService {
	 public List<Terminal> getAllTerminals();
	 public Terminal getTerminalById(int id);
	 public Terminal saveTerminal(Terminal terminal);
	 public void deleteTerminal(int id);
	 public Map<String, Object> getPublicKey(String public_key);
	 public List<Terminal> searchTerminals(Boolean active, String publicKey, String merId, String businessUrl,
	            String terName, String dbaBrandName, String connectorids);
	 public List<Terminal> searchTerminalsByMerid(String merId);
	 public List<Terminal> searchTerminalsByMerid(String merId, Boolean active, String publicKey,
	            String businessUrl, String terName, String dbaBrandName, String connectorids);
}
