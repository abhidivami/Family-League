package com.familyleague.league.service;

import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.league.dto.LeagueRequest;
import com.familyleague.league.dto.LeagueResponse;
import com.familyleague.league.entity.League;
import com.familyleague.league.repository.LeagueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeagueService {

    private final LeagueRepository leagueRepository;

    @Transactional
    public LeagueResponse create(LeagueRequest req) {
        League league = League.builder()
                .name(req.name())
                .description(req.description())
                .active(true)
                .build();
        return LeagueResponse.from(leagueRepository.save(league));
    }

    public Page<LeagueResponse> list(String search, Pageable pageable) {
        if (StringUtils.hasText(search)) {
            return leagueRepository
                    .findByNameContainingIgnoreCaseAndDeletedAtIsNull(search, pageable)
                    .map(LeagueResponse::from);
        }
        return leagueRepository.findByDeletedAtIsNull(pageable).map(LeagueResponse::from);
    }

    public LeagueResponse getById(UUID id) {
        return LeagueResponse.from(findOrThrow(id));
    }

    @Transactional
    public LeagueResponse update(UUID id, LeagueRequest req) {
        League league = findOrThrow(id);
        league.setName(req.name());
        if (req.description() != null) league.setDescription(req.description());
        return LeagueResponse.from(leagueRepository.save(league));
    }

    @Transactional
    public void delete(UUID id) {
        League league = findOrThrow(id);
        league.softDelete();
        leagueRepository.save(league);
    }

    private League findOrThrow(UUID id) {
        return leagueRepository.findById(id)
                .filter(l -> !l.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("League", id));
    }
}
