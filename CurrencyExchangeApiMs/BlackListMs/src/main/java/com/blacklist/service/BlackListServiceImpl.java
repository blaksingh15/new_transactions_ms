package com.blacklist.service;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.blacklist.entity.BlackList;
import com.blacklist.repository.BlackListRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
@Service
public class BlackListServiceImpl implements BlackListService {

    private final BlackListRepository blackListRepo;
    public BlackListServiceImpl(BlackListRepository blackListRepo) {
        this.blackListRepo = blackListRepo;
    }

    private static final short STATUS_ACTIVE = 1;
    private static final short STATUS_INACTIVE = 0;

    @Override
    public BlackList save(BlackList blackList) {
        return blackListRepo.save(blackList);
    }

    @Override
    public List<BlackList> findAll() {
        return blackListRepo.findAll();
    }

    @Override
    public BlackList findById(Integer id) {
        return blackListRepo.findById(id).orElseThrow(() -> new RuntimeException("Blacklist not found with id: " + id));
    }

    @Override
    public List<BlackList> findByClientId(Integer clientId) {
        return blackListRepo.findByClientIdAndStatus(clientId, STATUS_ACTIVE);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        BlackList blackList = findById(id);
        if (blackList == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Blacklist record not found with id: " + id
            );
        }
        blackList.setStatus(STATUS_INACTIVE); // soft delete
        blackListRepo.save(blackList);
    }



    @Override
    public boolean isBlacklisted(Integer clientId, String blacklistType, String blacklistValue) {
        return blackListRepo.existsByClientIdAndBlacklistTypeAndBlacklistValue(clientId, blacklistType, blacklistValue);
    }

    @Override
    public List<BlackList> findByTypeAndValue(String blacklistType, String blacklistValue, String condition) {
        return blackListRepo.findByBlacklistTypeAndBlacklistValueAndStatusAndCondition(
                blacklistType, blacklistValue, STATUS_ACTIVE, condition);
    }

    @Override
    public List<BlackList> findAllByOrderByIdDesc() {
        return blackListRepo.findAllByOrderByIdDesc();
    }
}
