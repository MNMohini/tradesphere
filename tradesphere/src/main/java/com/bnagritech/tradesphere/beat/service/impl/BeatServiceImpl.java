package com.bnagritech.tradesphere.beat.service.impl;

import com.bnagritech.tradesphere.beat.dto.BeatsRequest;
import com.bnagritech.tradesphere.beat.dto.BeatsResponse;
import com.bnagritech.tradesphere.beat.model.Beat;
import com.bnagritech.tradesphere.beat.repository.BeatRepository;
import com.bnagritech.tradesphere.beat.service.BeatService;
import com.bnagritech.tradesphere.common.enums.ApprovalStatus;
import com.bnagritech.tradesphere.common.enums.BeatDay;
import com.bnagritech.tradesphere.common.enums.BeatStatus;
import com.bnagritech.tradesphere.common.enums.RetailerType;
import com.bnagritech.tradesphere.common.exception.EmployeeNotFoundException;
import com.bnagritech.tradesphere.common.exception.ResourceAlreadyExistsException;
import com.bnagritech.tradesphere.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class BeatServiceImpl implements BeatService {
    private final BeatRepository beatRepository;

    @Override
    public BeatsResponse createBeat(BeatsRequest request) {
        if(beatRepository.existsByBeatNameAndTerritoryIdAndBeatCode(
                request.getBeatName(), request.getTerritoryId(), request.getBeatCode())){
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
        return null;
    }

    @Override
    public List<BeatsResponse> getAllBeats() {
        return List.of();
    }

    @Override
    public void deleteBeat(String beatId) {

            Beat beat =
                    beatRepository
                            .findByBeatId(beatId)
                            .orElseThrow(
                                    () -> new RuntimeException("Beat not found")
                            );


        beatRepository.delete(beat);

    }
    @Override
    public List<BeatsResponse> getBeatsByTerritory(String territoryId) {
        return List.of();
    }

    @Override
    public List<BeatsResponse> getEmployeeBeats(String employeeId) {
        return List.of();
    }

    @Override
    public List<BeatsResponse> getEmployeeDayBeats(String employeeId, BeatDay beatDay) {
        return List.of();
    }

    @Override
    public BeatsResponse assignRetailerToBeat(String beatId, String retailerId) {
        return null;
    }

    @Override
    public BeatsResponse removeRetailerFromBeat(String beatId, String retailerId) {
        return null;
    }

    @Override
    public BeatsResponse getBeatByRetailer(String retailerId) {
        return null;
    }

    @Override
    public BeatsResponse approveBeat(String beatId, String managerId) {
        return null;
    }

    @Override
    public BeatsResponse rejectBeat(String beatId, String managerId) {
        return null;
    }

    @Override
    public List<BeatsResponse> searchByCity(String city) {
        return List.of();
    }

    @Override
    public List<BeatsResponse> searchByState(String state) {
        return List.of();
    }

    @Override
    public List<BeatsResponse> getByStatus(BeatStatus status) {
        return List.of();
    }

    @Override
    public List<BeatsResponse> getByBeatType(RetailerType beatType) {
        return List.of();
    }

    @Override
    public List<BeatsResponse> getByApprovalStatus(ApprovalStatus approvalStatus) {
        return List.of();
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
}
