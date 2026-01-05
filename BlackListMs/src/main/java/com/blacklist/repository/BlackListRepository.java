package com.blacklist.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.blacklist.entity.BlackList;
public interface BlackListRepository extends JpaRepository<BlackList, Integer>{
	 List<BlackList> findByClientIdAndStatus(Integer clientId, Short status);
	 List<BlackList> findByBlacklistTypeAndBlacklistValueAndStatusAndCondition(String blacklistType, String blacklistValue, Short status, String condition);
	 boolean existsByClientIdAndBlacklistTypeAndBlacklistValue(Integer clientId, String blacklistType, String blacklistValue);
	 List<BlackList> findAllByOrderByIdDesc();

}
