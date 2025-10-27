package com.dhbw.broker.bff.service;

import com.dhbw.broker.bff.domain.PortfolioLot;
import com.dhbw.broker.bff.domain.User;
import com.dhbw.broker.bff.repository.PortfolioLotRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioLotService {
    private final PortfolioLotRepository portfolioLotRepository;

    public PortfolioLotService(PortfolioLotRepository portfolioLotRepository) {
        this.portfolioLotRepository = portfolioLotRepository;
    }

    public List<PortfolioLot> getHeldLotsForUser(User user) {
        return portfolioLotRepository.findHeldLotsByUser(user);
    }
}
