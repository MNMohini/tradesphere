package com.bnagritech.tradesphere.retailer.service.impl;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.repository.UserRepository;
import com.bnagritech.tradesphere.common.enums.RetailerStatus;
import com.bnagritech.tradesphere.common.exception.ResourceAlreadyExistsException;
import com.bnagritech.tradesphere.common.exception.ResourceNotFoundException;
import com.bnagritech.tradesphere.retailer.dto.RetailerRequest;
import com.bnagritech.tradesphere.retailer.dto.RetailerResponse;
import com.bnagritech.tradesphere.retailer.model.Retailer;
import com.bnagritech.tradesphere.retailer.repository.RetailerRepository;
import com.bnagritech.tradesphere.retailer.service.RetailerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RetailerServiceImpl implements RetailerService {

    private final RetailerRepository retailerRepository;
    private final UserRepository userRepository;

    @Override
    public RetailerResponse createRetailer(RetailerRequest request) {
        if(retailerRepository.existsByEmail(request.getEmail())){
            throw new ResourceAlreadyExistsException("Resource already exists");
        }
        if(retailerRepository.existsByPhoneNumber(request.getPhoneNumber())){
            throw new ResourceAlreadyExistsException("Resource already exists");
        }
        if (retailerRepository.existsByRetailerId(request.getRetailerId())){
            throw new ResourceAlreadyExistsException("Resource already exists");
        }
        if(userRepository.existsByUserName(String.valueOf(request.getUserName()))){
            throw new ResourceNotFoundException("This user Name isn't exists.");
        }
        if (retailerRepository.existsByShopNameAndCityAndState(
                request.getShopName(),
                request.getCity(),
                request.getState())){
            throw new ResourceAlreadyExistsException("Resource already exists");
        }
            Retailer retailer = Retailer.builder()
                    .retailerId(request.getRetailerId())
                    .shopName(request.getShopName())
                    .userName(request.getUserName())
                    .ownerName(request.getOwnerName())
                    .phoneNumber(request.getPhoneNumber())
                    .alternateContactNumber(request.getAlternateContactNumber())
                    .email(request.getEmail())
                    .city(request.getCity())
                    .state(request.getState())
                    .address(request.getAddress())
                    .territoryId(request.getTerritoryId())
                    .beatId(request.getBeatId())
                    .promoterId(request.getPromoterId())
                    .retailerType(request.getRetailerType())
                    .retailerStatus(request.getRetailerStatus())
                    .gstNumber(request.getGstNumber())
                    .panNumber(request.getPanNumber())
                    .longitude(request.getLongitude())
                    .latitude(request.getLatitude())
                    .creditDays(request.getCreditDays())
                    .creditLimits(request.getCreditLimits())
                    .createdAt(request.getCreatedAt())
                    .updatedAt(request.getUpdatedAt())
                    .createdBy(request.getCreatedBy())
                    .updatedBy(request.getUpdatedBy())
                    .retailerStatus(request.getRetailerStatus())
                    .build();
        Retailer savedRetailer = retailerRepository.save(retailer);
        return mapToResponse(savedRetailer);
    }

    @Override
    public RetailerResponse getRetailerById(String retailerId) {
        Retailer retailer = retailerRepository.findByRetailerId(retailerId)
                .orElseThrow(
                        ()->
                                new ResourceNotFoundException(
                                        "Retailer not found with " +retailerId +" id"));

        return mapToResponse(retailer);
    }

    @Override
    public RetailerResponse updateRetailer(String retailerId, RetailerRequest request) {
        Retailer retailer = retailerRepository.findByRetailerId(retailerId)
                .orElseThrow(
                        ()->
                                new ResourceNotFoundException(
                                        "Retailer not found with " +retailerId +" id"));
        retailer.setRetailerId(request.getRetailerId());
        retailer.setRetailerStatus(request.getRetailerStatus());
        retailer.setShopName(request.getShopName());
        retailer.setOwnerName(request.getOwnerName());
        retailer.setPhoneNumber(request.getPhoneNumber());
        retailer.setAlternateContactNumber(request.getAlternateContactNumber());
        retailer.setEmail(request.getEmail());
        retailer.setCity(request.getCity());
        retailer.setState(request.getState());
        retailer.setAddress(request.getAddress());
        retailer.setTerritoryId(request.getTerritoryId());
        retailer.setBeatId(request.getBeatId());
        retailer.setPromoterId(request.getPromoterId());
        retailer.setRetailerType(request.getRetailerType());
        retailer.setRetailerStatus(request.getRetailerStatus());
        retailer.setUserName(request.getUserName());
        retailer.setGstNumber(request.getGstNumber());
        retailer.setPanNumber(request.getPanNumber());
        retailer.setLongitude(request.getLongitude());
        retailer.setLatitude(request.getLatitude());
        retailer.setCreditDays(request.getCreditDays());
        retailer.setCreditLimits(request.getCreditLimits());
        retailer.setUpdatedAt(LocalDateTime.now());
        retailer.setCreatedBy(request.getCreatedBy());
        retailer.setUpdatedBy(request.getUpdatedBy());

            Retailer updatedRetailer = retailerRepository.save(retailer);
            return mapToResponse(updatedRetailer);
    }

    @Override
    public RetailerResponse getRetailerByPhoneNumber(String phoneNumber) {
        Retailer retailer = retailerRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(
                        ()->
                                 new ResourceNotFoundException(
                                         "Retailer not found with " +phoneNumber));
        return mapToResponse(retailer);
    }

    @Override
    public RetailerResponse getRetailerByEmail(String email) {
        Retailer retailer = retailerRepository.findByEmail(email)
                .orElseThrow(
                        ()->
                                new ResourceNotFoundException(
                                        "Retailer not found with " +email ));

        return mapToResponse(retailer);
    }

    @Override
    public RetailerResponse updateRetailerStatus(String retailerId, RetailerRequest request) {
        Retailer retailer = retailerRepository.findByRetailerId(retailerId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException(
                                "Retailer not found with " +retailerId ));
         retailer.setRetailerStatus(request.getRetailerStatus());
         Retailer updatedRetailer= retailerRepository.save(retailer);

        return mapToResponse(updatedRetailer);
    }

    @Override
    public RetailerResponse assignRetailer(String retailerId, RetailerRequest request) {
        Retailer retailer = retailerRepository.findByRetailerId(retailerId)
                .orElseThrow(
                        ()->
                                new ResourceNotFoundException("Retailer not found"));
        retailer.setUserName(request.getUserName());

        return mapToResponse(retailerRepository.save(retailer));
    }

    @Override
    public void deleteRetailer(String retailerId) {
        Retailer retailer = retailerRepository.findByRetailerId(retailerId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException(
                                "Retailer not found with " +retailerId + " id"));
        retailerRepository.delete(retailer);
    }

    @Override
    public List<RetailerResponse> getRetailerByTerritory(String territoryId) {
        List<Retailer> retailerList = retailerRepository.findByTerritoryId(territoryId);
        return retailerList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<RetailerResponse> getRetailerByShopName(String shopName) {
        List<Retailer> retailerList = retailerRepository.findByShopNameContainingIgnoreCase(shopName);
        return retailerList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<RetailerResponse> getRetailerByOwnerName(String ownerName) {
        List<Retailer> retailerList = retailerRepository.findByOwnerNameContainingIgnoreCase(ownerName);
        return  retailerList.stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    public List<RetailerResponse> getRetailerByUserName(User userName) {
        List<Retailer> retailerList = retailerRepository.findRetailerByUserName(userName);
        return retailerList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<RetailerResponse> getRetailerByCity(String city) {
        List<Retailer> retailerList = retailerRepository.findByCity(city);
        return retailerList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<RetailerResponse> getRetailerByState(String state) {
       List<Retailer> retailerList = retailerRepository.findByState(state);
        return retailerList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<RetailerResponse> getRetailerByRetailerStatus(RetailerStatus status) {
        List<Retailer> retailerList = retailerRepository.findRetailerByRetailerStatus(status);
        return retailerList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<RetailerResponse> getAllRetailer() {

        return retailerRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private RetailerResponse mapToResponse(Retailer retailer){
        RetailerResponse retailerResponse = new RetailerResponse();
        retailerResponse.setRetailerId(retailer.getRetailerId());
        retailerResponse.setCity(retailer.getCity());
        retailerResponse.setState(retailer.getState());
        retailerResponse.setShopName(retailer.getShopName());
        retailerResponse.setOwnerName(retailer.getOwnerName());
        retailerResponse.setPhoneNumber(retailer.getPhoneNumber());
        retailerResponse.setUserName(retailer.getUserName());
        retailerResponse.setAlternateContactNumber(retailer.getAlternateContactNumber());
        retailerResponse.setEmail(retailer.getEmail());
        retailerResponse.setAddress(retailer.getAddress());
        retailerResponse.setTerritoryId(retailer.getTerritoryId());
        retailerResponse.setBeatId(retailer.getBeatId());
        retailerResponse.setPromoterId(retailer.getPromoterId());
        retailerResponse.setRetailerType(retailer.getRetailerType());
        retailerResponse.setGstNumber(retailer.getGstNumber());
        retailerResponse.setPanNumber(retailer.getPanNumber());
        retailerResponse.setLatitude(retailer.getLatitude());
        retailerResponse.setLongitude(retailer.getLongitude());
        retailerResponse.setCreditDays(retailer.getCreditDays());
        retailerResponse.setCreditLimits(retailer.getCreditLimits());
        retailerResponse.setCreatedAt(LocalDateTime.now());
        retailerResponse.setUpdatedAt(LocalDateTime.now());
        retailerResponse.setRetailerStatus(retailer.getRetailerStatus());
        retailerResponse.setCreatedBy(retailer.getCreatedBy());
        retailerResponse.setUpdatedBy(retailer.getUpdatedBy());

        return retailerResponse;
    }
}
