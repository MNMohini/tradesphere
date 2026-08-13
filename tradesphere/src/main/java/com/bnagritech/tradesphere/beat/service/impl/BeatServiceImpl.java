package com.bnagritech.tradesphere.beat.service.impl;

import com.bnagritech.tradesphere.beat.dto.BeatsRequest;
import com.bnagritech.tradesphere.beat.dto.BeatsResponse;
import com.bnagritech.tradesphere.beat.model.Beat;
import com.bnagritech.tradesphere.beat.model.BeatRetailer;
import com.bnagritech.tradesphere.beat.repository.BeatRepository;
import com.bnagritech.tradesphere.beat.service.BeatService;
import com.bnagritech.tradesphere.common.enums.ApprovalStatus;
import com.bnagritech.tradesphere.common.enums.BeatDay;
import com.bnagritech.tradesphere.common.enums.BeatStatus;
import com.bnagritech.tradesphere.common.exception.ResourceAlreadyExistsException;
import com.bnagritech.tradesphere.common.exception.ResourceNotFoundException;
import com.bnagritech.tradesphere.retailer.repository.RetailerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BeatServiceImpl implements BeatService {
    private final BeatRepository beatRepository;
    private final RetailerRepository retailerRepository;

    @Override
    public BeatsResponse createBeat(BeatsRequest request) {
        if (beatRepository.existsByBeatNameAndTerritoryIdAndBeatCode(
                request.getBeatName(), request.getTerritoryId(), request.getBeatCode())) {
            throw new ResourceAlreadyExistsException("Beat already exists");
        }
        if (beatRepository.existsByBeatId(request.getBeatId())){
            throw new ResourceAlreadyExistsException("Beat already exists");
        }
        Beat beat = Beat.builder()
                .beatName(request.getBeatName())
                .beatCode(request.getBeatCode())
                .description(request.getDescription())
                .beatType(request.getBeatType())
                .territoryId(request.getTerritoryId())
                .assignedEmployeeId(request.getAssignedEmployeeId())
                .beatOwnerType(request.getBeatOwnerType())
                .managerId(request.getManagerId())
                .state(request.getState())
                .city(request.getCity())
                .area(request.getArea())
                .pinCode(request.getPinCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .retailers(request.getRetailers())
                .totalRetailers(request.getTotalRetailers())
                .beatDays(request.getBeatDays())
                .frequency(request.getFrequency())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .estimatedDistanceKm(request.getEstimatedDistanceKm())
                .estimatedTravelTimeMinutes(request.getEstimatedTravelTimeMinutes())
                .approvalStatus(request.getApprovalStatus())
                .build();
        Beat beatSaved = beatRepository.save(beat);
        return mapToResponse(beatSaved);
    }

    @Override
    public BeatsResponse updateBeat(String beatId, BeatsRequest request) {
        Beat beat = beatRepository.findByBeatId(beatId)
                .orElseThrow(() -> new ResourceNotFoundException("beat not found"));

        beat.setBeatName(request.getBeatName());
        beat.setBeatCode(request.getBeatCode());
        beat.setDescription(request.getDescription());
        beat.setBeatType(request.getBeatType());
        beat.setTerritoryId(request.getTerritoryId());
        beat.setAssignedEmployeeId(request.getAssignedEmployeeId());
        beat.setBeatOwnerType(request.getBeatOwnerType());
        beat.setManagerId(request.getManagerId());
        beat.setState(request.getState());
        beat.setCity(request.getCity());
        beat.setArea(request.getArea());
        beat.setPinCode(request.getPinCode());
        beat.setLatitude(request.getLatitude());
        beat.setLongitude(request.getLongitude());
        beat.setRetailers(request.getRetailers());
        beat.setTotalRetailers(request.getTotalRetailers());
        beat.setBeatDays(request.getBeatDays());
        beat.setFrequency(request.getFrequency());
        beat.setEffectiveFrom(request.getEffectiveFrom());
        beat.setEffectiveTo(request.getEffectiveTo());
        beat.setEstimatedDistanceKm(request.getEstimatedDistanceKm());
        beat.setEstimatedTravelTimeMinutes(request.getEstimatedTravelTimeMinutes());
        beat.setApprovalStatus(request.getApprovalStatus());

        Beat beatUpdated = beatRepository.save(beat);
        return mapToResponse(beatUpdated);
    }

    @Override
    public BeatsResponse getBeatById(String beatId) {

        Beat beat = beatRepository.findByBeatId(beatId)
                .orElseThrow(() -> new ResourceNotFoundException("beat not found"));

        return mapToResponse(beat);
    }

    @Override
    public List<BeatsResponse> getAllBeats() {
        return beatRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBeat(String beatId) {

        Beat beat = beatRepository.findByBeatId(beatId)
                .orElseThrow(() -> new RuntimeException("Beat not found"));

        beatRepository.delete(beat);
    }

    @Override
    public List<BeatsResponse> getBeatsByTerritory(String territoryId) {

        return beatRepository.findByTerritoryId(territoryId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<BeatsResponse> getEmployeeBeats(String employeeId) {
        return beatRepository.findByAssignedEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<BeatsResponse> getEmployeeDayBeats(String employeeId, BeatDay beatDay) {
        return beatRepository.findByAssignedEmployeeIdAndBeatDaysContaining(employeeId, beatDay)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public BeatsResponse assignRetailerToBeat(String beatId, String retailerId) {
        Beat beat = getBeatEntity(beatId);
        retailerRepository.findByRetailerId(retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("retailer not found"));

        BeatRetailer retailer = BeatRetailer.builder()
                .retailerId(retailerId)
                .sequenceNumber(beat.getRetailers().size() + 1)
                .mandatoryVisit(true)
                .active(true)
                .build();
        beat.getRetailers().add(retailer);
        return mapToResponse(beatRepository.save(beat));
    }

    @Override
    public BeatsResponse removeRetailerFromBeat(String beatId, String retailerId) {
        Beat beat = getBeatEntity(beatId);
        beat.getRetailers().removeIf(r->r.getRetailerId().equals(retailerId));

        return mapToResponse(beatRepository.save(beat));
    }

    @Override
    public BeatsResponse getBeatByRetailer(String retailerId) {
        Beat beat = beatRepository.findByRetailersRetailerId(retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("retailer not found"));
        return mapToResponse(beat);
    }

    @Override
    public BeatsResponse approveBeat(String beatId, String managerId) {
        Beat beat = getBeatEntity(beatId);
        beat.setApprovalStatus(ApprovalStatus.APPROVED);
        return mapToResponse(beatRepository.save(beat));
    }

    @Override
    public BeatsResponse rejectBeat(String beatId, String managerId) {
        Beat beat = getBeatEntity(beatId);
        beat.setApprovalStatus(ApprovalStatus.REJECTED);
        return mapToResponse(beatRepository.save(beat));
    }

    @Override
    public List<BeatsResponse> searchByCity(String city) {
        return beatRepository.findByCityIgnoreCase(city)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<BeatsResponse> searchByState(String state) {
        return beatRepository.findByStateIgnoreCase(state)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<BeatsResponse> getByStatus(BeatStatus status) {
        return beatRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<BeatsResponse> getByBeatType(String beatType) {
        return beatRepository.findByBeatType(beatType)
                .stream()
                .map(this:: mapToResponse)
                .toList();
    }

    @Override
    public List<BeatsResponse> getByApprovalStatus(ApprovalStatus approvalStatus) {
        return beatRepository.findByApprovalStatus(approvalStatus)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    private BeatsResponse mapToResponse(Beat beat) {

        return BeatsResponse.builder()
                .beatId(beat.getBeatId())
                .beatName(beat.getBeatName())
                .beatCode(beat.getBeatCode())
                .description(beat.getDescription())
                .beatType(beat.getBeatType())
                .territoryId(beat.getTerritoryId())
                .assignedEmployeeId(beat.getAssignedEmployeeId())
                .beatOwnerType(beat.getBeatOwnerType())
                .state(beat.getState())
                .city(beat.getCity())
                .area(beat.getArea())
                .retailers(beat.getRetailers())
                .totalRetailers(beat.getTotalRetailers())
                .beatDays(beat.getBeatDays())
                .frequency(beat.getFrequency())
                .approvalStatus(beat.getApprovalStatus())
                .status(beat.getStatus())
                .createdAt(beat.getCreatedAt())
                .updatedAt(beat.getUpdatedAt())
                .build();


    }
    public Beat getBeatEntity(String beatId) {
        return beatRepository.findByBeatId(beatId)
                .orElseThrow(() -> new ResourceNotFoundException("beat not found"));
    }
}
