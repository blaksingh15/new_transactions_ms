package com.terminal.service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.terminal.entity.Terminal;
import com.terminal.repository.TerminalRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;

@Service
public class TerminalServiceImp implements TerminalService {
	
	@Autowired
    private TerminalRepository terminalRepo;
    
	@Override
    public List<Terminal> getAllTerminals() {
        return terminalRepo.findAll();
    }
	
	@Override
    public Terminal getTerminalById(int id) {
        return terminalRepo.findById(id).orElse(null);
    }
	
	@Override
    public Terminal saveTerminal(Terminal terminal) {
        return terminalRepo.save(terminal);
    }
	
	@Override
    public void deleteTerminal(int id) {
    	terminalRepo.deleteById(id);
    }
	
	@Override
    public Map<String, Object> getPublicKey(String public_key) {
        throw new UnsupportedOperationException("Unimplemented method 'getPublicKey'");
    }
	
	
	@Override
	public List<Terminal> searchTerminalsByMerid(String merId) {

	    Specification<Terminal> spec = (root, query, cb) -> {
	        if (merId == null || merId.trim().isEmpty()) {
	            return cb.conjunction();
	        }
	        return cb.like(root.get("merId"), "%" + merId + "%");
	    };
	    return terminalRepo.findAll( spec, Sort.by(Sort.Direction.DESC, "id"));
	}

	
	@Override
	public List<Terminal> searchTerminals( Boolean active,String publicKey,String merId,String businessUrl, String terName, String dbaBrandName,String connectorids) {

	        Specification<Terminal> spec = (root, query, cb) -> {
	        List<Predicate> predicates = new ArrayList<>();
	        if (active != null) {
	            predicates.add(cb.equal(root.get("active"), active));
	        }
	        if (publicKey != null && !publicKey.isBlank()) {
	            predicates.add(cb.like(root.get("publicKey"), "%" + publicKey + "%"));
	        }
	        if (merId != null && !merId.isBlank()) {
	            predicates.add(cb.like(root.get("merId"), "%" + merId + "%"));
	        }
	        if (businessUrl != null && !businessUrl.isBlank()) {
	            predicates.add(cb.like(root.get("businessUrl"), "%" + businessUrl + "%"));
	        }
	        if (terName != null && !terName.isBlank()) {
	            predicates.add(cb.like(root.get("terName"), "%" + terName + "%"));
	        }
	        if (dbaBrandName != null && !dbaBrandName.isBlank()) {
	            predicates.add(cb.like(root.get("dbaBrandName"), "%" + dbaBrandName + "%"));
	        }
	        if (connectorids != null && !connectorids.isBlank()) {
	            predicates.add(cb.like(root.get("connectorids"), "%" + connectorids + "%"));
	        }

	        return predicates.isEmpty()
	                ? cb.conjunction()
	                : cb.and(predicates.toArray(new Predicate[0]));
	    };

	    return terminalRepo.findAll(spec,Sort.by(Sort.Direction.DESC, "id"));
	}
	@Override
	public List<Terminal> searchTerminalsByMerid(String merId, Boolean active,String publicKey, String businessUrl,String terName,String dbaBrandName,String connectorids) {
	    Specification<Terminal> spec = (root, query, cb) -> {
	        List<Predicate> predicates = new ArrayList<>();
	        if (merId != null && !merId.isBlank()) {
	            predicates.add(cb.like(root.get("merId"), "%" + merId + "%"));
	        }

	        if (active != null) {
	            predicates.add(cb.equal(root.get("active"), active));
	        }
	        if (publicKey != null && !publicKey.isBlank()) {
	            predicates.add(cb.like(root.get("publicKey"), "%" + publicKey + "%"));
	        }
	        if (businessUrl != null && !businessUrl.isBlank()) {
	            predicates.add(cb.like(root.get("businessUrl"), "%" + businessUrl + "%"));
	        }
	        if (terName != null && !terName.isBlank()) {
	            predicates.add(cb.like(root.get("terName"), "%" + terName + "%"));
	        }
	        if (dbaBrandName != null && !dbaBrandName.isBlank()) {
	            predicates.add(cb.like(root.get("dbaBrandName"), "%" + dbaBrandName + "%"));
	        }
	        if (connectorids != null && !connectorids.isBlank()) {
	            predicates.add(cb.like(root.get("connectorids"), "%" + connectorids + "%"));
	        }

	        return cb.and(predicates.toArray(new Predicate[0]));
	    };

	    return terminalRepo.findAll(spec,Sort.by(Sort.Direction.DESC, "id")
	    );
	}


}
