package com.bnagritech.tradesphere.promoter.service.impl;

import com.bnagritech.tradesphere.common.exception.EmployeeAlreadyExistsException;
import com.bnagritech.tradesphere.common.exception.ResourceNotFoundException;
import com.bnagritech.tradesphere.promoter.dto.PromoterRequest;
import com.bnagritech.tradesphere.promoter.dto.PromoterResponse;
import com.bnagritech.tradesphere.promoter.model.Promoter;
import com.bnagritech.tradesphere.promoter.repository.PromoterRepository;
import com.bnagritech.tradesphere.promoter.service.PromoterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
    @Service
    @RequiredArgsConstructor
    public class PromoterServiceImpl implements PromoterService {
        private final PromoterRepository promoterRepository;

        @Override
        public PromoterResponse createPromoter(PromoterRequest request) {

            if (promoterRepository.existsByEmail(request.getEmail())) {
                throw new EmployeeAlreadyExistsException("Email already exists");
            }
            if (promoterRepository.existsByPromoterId(request.getPromoterId())) {
                throw new EmployeeAlreadyExistsException("Promoter Id already exists");
            }
            if (promoterRepository.existsByEmployeeId(request.getEmployeeId())) {
                throw new EmployeeAlreadyExistsException("Employee Id already exists");
            }

            Promoter promoter = Promoter.builder()
                    .promoterId(request.getPromoterId())
                    .promoterName(request.getPromoterName())
                    .phoneNumber(request.getPhoneNumber())
                    .address(request.getAddress())
                    .email(request.getEmail())
                    .employeeId(request.getEmployeeId())
                    .territoryId(request.getTerritoryId())
                    .territoryName(request.getTerritoryName())
                    .city(request.getCity())
                    .state(request.getState())
                    .status(request.getStatus())
                    .build();
            Promoter savedPromoter = promoterRepository.save(promoter);

            return mapToResponse(savedPromoter);

        }
        @Override
        public List<PromoterResponse> getAllPromoters() {
            return promoterRepository.findAll()
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        @Override
        public PromoterResponse getPromoterById(String promoterId) {
            Promoter promoter = promoterRepository.findByPromoterId(promoterId)
                    .orElseThrow(
                            ()->
                                    new ResourceNotFoundException(
                                            "Employee not found with " +promoterId +" id"));

            return mapToResponse(promoter);
        }

        @Override
        public PromoterResponse updatePromoter(String promoterId, PromoterRequest request) {

            Promoter promoter = promoterRepository.findByPromoterId(promoterId)
                    .orElseThrow(() -> new ResourceNotFoundException("Promoter not found"));

            promoter.setPromoterId(request.getPromoterId());
            promoter.setPromoterName(request.getPromoterName());
            promoter.setPhoneNumber(request.getPhoneNumber());
            promoter.setEmail(request.getEmail());
            promoter.setAddress(request.getAddress());
            promoter.setEmployeeId(request.getEmployeeId());
            promoter.setStatus(request.getStatus());
            promoter.setTerritoryId(request.getTerritoryId());
            promoter.setTerritoryName(request.getTerritoryName());

            Promoter updatedPromoter= promoterRepository.save(promoter);
            return  mapToResponse(updatedPromoter);
        }

        @Override
        public void deletePromoter(String promoterId) {
            Promoter promoter = promoterRepository.findByPromoterId(promoterId)
                    .orElseThrow(
                            ()->
                                    new ResourceNotFoundException(
                                            "Employee not found with " +promoterId +" id"));
            promoterRepository.delete(promoter);

        }


    @Override
    public PromoterResponse getPromoterByPhoneNumber(long  phoneNumber) {
        Promoter promoter = promoterRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(
                        ()->
                                new ResourceNotFoundException(
                                        "Employee not found with " +phoneNumber ));
        return mapToResponse(promoter);
    }

    @Override
    public PromoterResponse getPromoterByEmail(String email) {
        Promoter promoter = promoterRepository.findByEmail(email)
                .orElseThrow(
                        ()->
                                new ResourceNotFoundException(
                                        "Employee not found with " +email ));

        return mapToResponse(promoter);
    }
    @Override
    public List<PromoterResponse> getPromoterByTerritory(String territoryId) {
    List<Promoter> promoters = promoterRepository.findByTerritoryId(territoryId);
        return promoters.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<PromoterResponse> getPromoterByStatus(Boolean status) {
        List<Promoter> promoters = promoterRepository.findByStatus(String.valueOf(status));
        return promoters.stream()
                .map(this::mapToResponse)
                .toList();

    }

    private PromoterResponse mapToResponse(Promoter promoter) {
        PromoterResponse response = new PromoterResponse();
        response.setPromoterId(promoter.getPromoterId());
        response.setPromoterName(promoter.getPromoterName());
        response.setPhoneNumber(promoter.getPhoneNumber());
        response.setEmail(promoter.getEmail());
        response.setAddress(promoter.getAddress());
        response.setState(promoter.getState());
        response.setCity(promoter.getCity());
        response.setStatus(promoter.getStatus());
        response.setEmployeeId(promoter.getEmployeeId());

            return response;
        }

    }
