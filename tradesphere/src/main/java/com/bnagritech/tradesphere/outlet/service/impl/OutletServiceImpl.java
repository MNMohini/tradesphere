package com.bnagritech.tradesphere.outlet.service.impl;

import com.bnagritech.tradesphere.common.enums.OutletStatus;
import com.bnagritech.tradesphere.common.enums.OutletType;
import com.bnagritech.tradesphere.common.exception.ResourceAlreadyExistsException;
import com.bnagritech.tradesphere.common.exception.ResourceNotFoundException;
import com.bnagritech.tradesphere.outlet.dto.OutletRequest;
import com.bnagritech.tradesphere.outlet.dto.OutletResponse;
import com.bnagritech.tradesphere.outlet.model.Outlet;
import com.bnagritech.tradesphere.outlet.repository.OutletRepository;
import com.bnagritech.tradesphere.outlet.service.OutletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutletServiceImpl implements OutletService {
    private final OutletRepository outletRepository;

    @Override
    public OutletResponse createOutlet(OutletRequest request) {

        if(outletRepository.existsByEmail(request.getEmail()))
        {
            throw new ResourceAlreadyExistsException("This email is already exists");
        }
        if (outletRepository.existsByOutletId(request.getOutletId()))
        {
            throw new ResourceAlreadyExistsException("This Outlet Id is already exists");
        }
        if(outletRepository.existsByMobileNumber(request.getMobileNumber()))
        {
            throw new ResourceAlreadyExistsException("Number is already exists");
        }

        Outlet outlet = Outlet.builder()
                .outletId(request.getOutletId())
                .outletName(request.getOutletName())
                .mobileNumber(request.getMobileNumber())
                .alternateContactNumber(request.getAlternateContactNumber())
                .email(request.getEmail())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pinCode(request.getPinCode())
                .territoryId(request.getTerritoryId())
                .beatId(request.getBeatId())
                .outletType(request.getOutletType())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .outletStatus(request.getOutletStatus())
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
                .orElseThrow(()-> new ResourceNotFoundException("OutletId isn't exists"));
        return mapToResponse(outlet);
    }

    @Override
    public OutletResponse updateOutlet(String outletId, OutletRequest request) {
        Outlet outlet = outletRepository.findByOutletId(outletId)
                .orElseThrow(()->new ResourceNotFoundException("Outlet not found"));
        outlet.setOutletName(request.getOutletName());
        outlet.setMobileNumber(request.getMobileNumber());
        outlet.setAlternateContactNumber(request.getAlternateContactNumber());
        outlet.setEmail(request.getEmail());
        outlet.setCity(request.getCity());
        outlet.setState(request.getState());
        outlet.setPinCode(request.getPinCode());
        outlet.setAddress(request.getAddress());
        outlet.setTerritoryId(request.getTerritoryId());
        outlet.setBeatId(request.getBeatId());
        outlet.setOutletType(request.getOutletType());
        outlet.setOutletStatus(request.getOutletStatus());
        outlet.setLongitude(request.getLongitude());
        outlet.setLatitude(request.getLatitude());
        outlet.setUpdatedBy(request.getUpdatedBy());

       Outlet updatedOutlet = outletRepository.save(outlet);
        return mapToResponse(updatedOutlet);
    }



    @Override
    public OutletResponse getOutletByMobileNumber(String mobileNumber) {
        Outlet outlet= outletRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Number isn't exists"));
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
    public List<OutletResponse> getOutletsByPinCode(String pinCode) {
        List<Outlet> outletList = outletRepository.findByPinCodeContainingIgnoreCase(pinCode);
        if(outletList.isEmpty()) {
            throw new ResourceNotFoundException("This PinCode isn't exists in our database.");
        }
        return outletList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<OutletResponse> getOutletByOutletStatus(OutletStatus outletStatus) {
        List<Outlet> outletList = outletRepository.findOutletByOutletStatus(outletStatus);
        if(outletList.isEmpty()) {
            throw new ResourceNotFoundException(
                    " No Outlets exists with this status. ");
        }
        return outletList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<OutletResponse> getOutletByCity(String city) {
        List<Outlet> outletList = outletRepository.findByCityContainingIgnoreCase(city);
        if(outletList.isEmpty()) {
            throw new ResourceNotFoundException(
                    " No Outlets exists in this city. ");
        }
        return outletList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<OutletResponse> getOutletByState(String state) {
        List<Outlet> outletList = outletRepository.findByStateContainingIgnoreCase(state);
        if(outletList.isEmpty()) {
            throw new ResourceNotFoundException(
                    " No Outlets exists in this state. ");
        }
        return outletList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<OutletResponse> getOutletByOutletType(OutletType outletType) {
        List<Outlet> outletList = outletRepository.findOutletByOutletType(outletType);
        if(outletList.isEmpty()) {
            throw new ResourceNotFoundException(
                    " we don't have this type of outlets. ");
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
        outletResponse.setPinCode(outlet.getPinCode());
        outletResponse.setOutletName(outlet.getOutletName());
        outletResponse.setAlternateContactNumber(outlet.getAlternateContactNumber());
        outletResponse.setEmail(outlet.getEmail());
        outletResponse.setAddress(outlet.getAddress());
        outletResponse.setTerritoryId(outlet.getTerritoryId());
        outletResponse.setBeatId(outlet.getBeatId());
        outletResponse.setOutletType(outlet.getOutletType());
        outletResponse.setLatitude(outlet.getLatitude());
        outletResponse.setLongitude(outlet.getLongitude());
        outletResponse.setUpdatedAt(LocalDateTime.now());
        outletResponse.setOutletStatus(outlet.getOutletStatus());
        outletResponse.setCreatedBy(outlet.getCreatedBy());
        outletResponse.setUpdatedBy(outlet.getUpdatedBy());
        return outletResponse;
    }

}
