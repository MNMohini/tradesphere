package com.bnagritech.tradesphere.outlet.service.impl;

import com.bnagritech.tradesphere.common.exception.ResourceAlreadyExistsException;
import com.bnagritech.tradesphere.common.exception.ResourceNotFoundException;
import com.bnagritech.tradesphere.outlet.dto.OutletRequest;
import com.bnagritech.tradesphere.outlet.dto.OutletResponse;
import com.bnagritech.tradesphere.outlet.model.Outlet;
import com.bnagritech.tradesphere.outlet.repository.OutletRepository;
import com.bnagritech.tradesphere.outlet.service.OutletService;
import com.bnagritech.tradesphere.promoter.model.Promoter;
import com.bnagritech.tradesphere.promoter.repository.PromoterRepository;
import com.bnagritech.tradesphere.retailer.model.Retailer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutletServiceImpl implements OutletService {
    private final OutletRepository outletRepository;
    private final PromoterRepository promoterRepository;

    @Override
    public OutletResponse createOutlet(OutletRequest request) {
        Promoter promoter= promoterRepository.findByPromoterId(request.getPromoterId())
                .orElseThrow(()-> new ResourceNotFoundException("Promoter Id does not exists"));

        if(outletRepository.existsByEmail(request.getEmail()))
        {
            throw new ResourceAlreadyExistsException("This email is already exists");
        }
        if (outletRepository.existsByOutletId(request.getOutletId()))
        {
            throw new ResourceAlreadyExistsException("This Outlet Id is already exists");
        }
        if(outletRepository.existsByPhoneNumber(request.getPhoneNumber()))
        {
            throw new ResourceAlreadyExistsException("Phone Number is already exists");
        }
        if(outletRepository.existsByOutletNameAndAddress(request.getOutletName(), request.getAddress()))
        {
            throw new ResourceAlreadyExistsException("Already registered with us");
        }

        Outlet outlet = Outlet.builder()
                .outletId(request.getOutletId())
                .outletName(request.getOutletName())
                .phoneNumber(request.getPhoneNumber())
                .alternateContactNumber(request.getAlternateContactNumber())
                .email(request.getEmail())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .territoryId(request.getTerritoryId())
                .beatId(request.getBeatId())
                .promoterId(promoter.getPromoterId())
                .outletType(request.getOutletType())
                .gstNumber(request.getGstNumber())
                .panNumber(request.getPanNumber())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .creditDays(request.getCreditDays())
                .creditLimits(request.getCreditLimits())
                .outletStatus(request.getOutletStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(request.getCreatedBy())
                .updatedBy(request.getUpdatedBy())
                .build();
        Outlet savedOutlet = outletRepository.save(outlet);
        return mapToResponse(savedOutlet);
    }

    @Override
    public OutletResponse getOutletByOutletId(String outletId) {
        Outlet outlet= outletRepository.findByOutletId(outletId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("OutletId isn't exists"));
        return mapToResponse(outlet);
    }

    @Override
    public OutletResponse updateOutlet(String outletId, OutletRequest request) {
        Outlet outlet = outletRepository.findByOutletId(outletId)
                .orElseThrow(
                        ()->
                                new ResourceNotFoundException(
                                        "Outlet not found"));

        outlet.setOutletName(request.getOutletName());
        outlet.setPhoneNumber(request.getPhoneNumber());
        outlet.setAlternateContactNumber(request.getAlternateContactNumber());
        outlet.setEmail(request.getEmail());
        outlet.setCity(request.getCity());
        outlet.setState(request.getState());
        outlet.setAddress(request.getAddress());
        outlet.setTerritoryId(request.getTerritoryId());
        outlet.setBeatId(request.getBeatId());
        outlet.setPromoterId(request.getPromoterId());
        outlet.setOutletType(request.getOutletType());
        outlet.setOutletStatus(request.getOutletStatus());
        outlet.setGstNumber(request.getGstNumber());
        outlet.setPanNumber(request.getPanNumber());
        outlet.setLongitude(request.getLongitude());
        outlet.setLatitude(request.getLatitude());
        outlet.setCreditDays(request.getCreditDays());
        outlet.setCreditLimits(request.getCreditLimits());
        outlet.setUpdatedBy(request.getUpdatedBy());

       Outlet updatedOutlet = outletRepository.save(outlet);
        return mapToResponse(updatedOutlet);
    }

    @Override
    public OutletResponse getOutletByPhoneNumber(String phoneNumber) {
        Outlet outlet= outletRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Phone number isn't exists"));
        return mapToResponse(outlet);
    }

    @Override
    public OutletResponse getOutletByEmail(String email) {
        Outlet outlet= outletRepository.findByEmail(email)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Email isn't exists"));
        return mapToResponse(outlet);
    }

    @Override
    public OutletResponse updateOutletStatus(String outletId, OutletRequest request) {
        Outlet outlet = outletRepository.findByOutletId(outletId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("outlet is not exists"));
        outlet.setOutletStatus(request.getOutletStatus());
        Outlet updatedStatus = outletRepository.save(outlet);
        return mapToResponse(updatedStatus);
    }

    @Override
    public List<OutletResponse> getOutletsByTerritoryId(String territoryId) {
        List<Outlet> outletList = outletRepository.findByTerritoryId(territoryId);
        if(outletList.isEmpty()) {
            throw new ResourceNotFoundException(
                    " No Outlets exists. ");
        }
        return outletList.stream()
                .map(this::mapToResponse)
                .toList();

    }

    @Override
    public List<OutletResponse> getOutletsByOutletName(String outletName) {
        List<Outlet> outletList = outletRepository.findByOutletNameContainingIgnoreCase(outletName);
        if(outletList.isEmpty()) {
            throw new ResourceNotFoundException(
                    " No Outlets exists with this name. ");
        }
        return outletList.stream()
                .map(this::mapToResponse)
                .toList();

    }

    @Override
    public List<OutletResponse> getOutletsByPromoterId(String promoterId) {
        promoterRepository.findByPromoterId(promoterId)
                .orElseThrow(()-> new ResourceNotFoundException("Promoter Id does not exists"));

        List<Outlet> outletList = outletRepository.findOutletByPromoterId(promoterId);
        if(outletList.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Outlets isn't assign to this Promoter ");
        }
        return outletList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void deleteOutlet(String outletId) {
       Outlet outlet = outletRepository.findByOutletId(outletId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException(
                                "Outlet not found "));
       outletRepository.delete(outlet);
    }

    @Override
    public List<OutletResponse> getAllOutlets() {
        return outletRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private OutletResponse mapToResponse(Outlet outlet){
        OutletResponse outletResponse = new OutletResponse();
        outletResponse.setOutletId(outlet.getOutletId());
        outletResponse.setCity(outlet.getCity());
        outletResponse.setState(outlet.getState());
        outletResponse.setOutletName(outlet.getOutletName());
        outletResponse.setPhoneNumber(outlet.getPhoneNumber());
        outletResponse.setAlternateContactNumber(outlet.getAlternateContactNumber());
        outletResponse.setEmail(outlet.getEmail());
        outletResponse.setAddress(outlet.getAddress());
        outletResponse.setTerritoryId(outlet.getTerritoryId());
        outletResponse.setBeatId(outlet.getBeatId());
        outletResponse.setPromoterId(outlet.getPromoterId());
        outletResponse.setOutletType(outlet.getOutletType());
        outletResponse.setGstNumber(outlet.getGstNumber());
        outletResponse.setPanNumber(outlet.getPanNumber());
        outletResponse.setLatitude(outlet.getLatitude());
        outletResponse.setLongitude(outlet.getLongitude());
        outletResponse.setCreditDays(outlet.getCreditDays());
        outletResponse.setCreditLimits(outlet.getCreditLimits());
        outletResponse.setCreatedAt(LocalDateTime.now());
        outletResponse.setUpdatedAt(LocalDateTime.now());
        outletResponse.setOutletStatus(outlet.getOutletStatus());
        outletResponse.setCreatedBy(outlet.getCreatedBy());
        outletResponse.setUpdatedBy(outlet.getUpdatedBy());
        return outletResponse;
    }

}
