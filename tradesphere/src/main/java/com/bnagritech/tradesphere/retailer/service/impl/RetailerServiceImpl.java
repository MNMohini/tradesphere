package com.bnagritech.tradesphere.retailer.service.impl;

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

    @Override
    public RetailerResponse createRetailer(RetailerRequest request) {

        if(retailerRepository.existsByPhoneNumber(request.getPhoneNumber())){
            throw new ResourceAlreadyExistsException("Retailer already exists with this Number.");
        }
        if (retailerRepository.existsByRetailerId(request.getRetailerId())){
           throw new ResourceAlreadyExistsException("Retailer already exists with this RetailerId.");
        }
        if (retailerRepository.existsByRetailerNameAndRetailerIdAndPhoneNumber(
                request.getRetailerName(), request.getRetailerId(), request.getPhoneNumber()))
        {
            throw new ResourceAlreadyExistsException("Retailer Already Exists");
        }
            Retailer retailer = Retailer.builder()
                    .retailerId(request.getRetailerId())
                    .retailerName(request.getRetailerName())
                    .outletIds(request.getOutletIds())
                    .phoneNumber(request.getPhoneNumber())
                    .address(request.getAddress())
                    .panNumber(request.getPanNumber())
                    .createdAt(request.getCreatedAt())
                    .createdBy(request.getCreatedBy())
                    .retailerStatus(request.getRetailerStatus())
                    .build();
        Retailer savedRetailer = retailerRepository.save(retailer);
        return mapToResponse(savedRetailer);
    }

    @Override
    public RetailerResponse getRetailerByRetailerId(String retailerId) {
        Retailer retailer = retailerRepository.findByRetailerId(retailerId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Retailer not found with " +retailerId +" id"));
        return mapToResponse(retailer);
    }

    @Override
    public RetailerResponse updateRetailer(String retailerId, RetailerRequest request) {
        Retailer retailer = retailerRepository.findByRetailerId(retailerId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException(
                                        "Retailer not found with " +retailerId +" id"));
        retailer.setRetailerId(request.getRetailerId());
        retailer.setRetailerName(request.getRetailerName());
        retailer.setOutletIds(request.getOutletIds());
        retailer.setRetailerStatus(request.getRetailerStatus());
        retailer.setPhoneNumber(request.getPhoneNumber());
        retailer.setAddress(request.getAddress());
        retailer.setRetailerStatus(request.getRetailerStatus());
        retailer.setPanNumber(request.getPanNumber());
        retailer.setCreatedBy(request.getCreatedBy());

            Retailer updatedRetailer = retailerRepository.save(retailer);
            return mapToResponse(updatedRetailer);
    }

    @Override
    public RetailerResponse getRetailerByPhoneNumber(String phoneNumber) {
        Retailer retailer = retailerRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(()-> new ResourceNotFoundException(
                                         "Retailer not found with " +phoneNumber));
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
    public RetailerResponse updateOutletIds(String retailerId, RetailerRequest request) {
        Retailer retailer = retailerRepository.findByRetailerId(retailerId)
                .orElseThrow(()-> new ResourceNotFoundException(
                                        "Retailer not found with this Id"));
        retailer.setOutletIds(request.getOutletIds());
        Retailer updatedOutletIds= retailerRepository.save(retailer);

        return mapToResponse(updatedOutletIds);
    }

    @Override
    public RetailerResponse getRetailerByRetailerName(String retailerName) {
        Retailer retailer = retailerRepository.findByRetailerNameContainingIgnoreCase(retailerName)
                .orElseThrow(
                        ()-> new ResourceNotFoundException(
                                "Retailer not found "));
        return mapToResponse(retailer);

    }

    @Override
    public List<RetailerResponse> getAllRetailer() {

        return retailerRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void deleteRetailer(String retailerId) {
        Retailer retailer = retailerRepository.findByRetailerId(retailerId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException(
                                "Retailer not found with " +retailerId + " id"));
        retailerRepository.delete(retailer);
    }

    private RetailerResponse mapToResponse(Retailer retailer){
        RetailerResponse retailerResponse = new RetailerResponse();
        retailerResponse.setId(retailer.getId());
        retailerResponse.setRetailerId(retailer.getRetailerId());
        retailerResponse.setRetailerName(retailer.getRetailerName());
        retailerResponse.setOutletIds(retailer.getOutletIds());
        retailerResponse.setPhoneNumber(retailer.getPhoneNumber());
        retailerResponse.setAddress(retailer.getAddress());
        retailerResponse.setPanNumber(retailer.getPanNumber());
        retailerResponse.setCreatedAt(LocalDateTime.now());
        retailerResponse.setRetailerStatus(retailer.getRetailerStatus());
        retailerResponse.setCreatedBy(retailer.getCreatedBy());
        return retailerResponse;
    }
}
