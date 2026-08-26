package com.bnagritech.tradesphere.beat.service.impl;

import com.bnagritech.tradesphere.beat.dto.BeatsRequest;
import com.bnagritech.tradesphere.beat.dto.BeatsResponse;
import com.bnagritech.tradesphere.beat.model.Beat;
import com.bnagritech.tradesphere.beat.repository.BeatRepository;
import com.bnagritech.tradesphere.beat.service.BeatService;
import com.bnagritech.tradesphere.common.enums.BeatStatus;
import com.bnagritech.tradesphere.common.exception.ResourceNotFoundException;
import com.bnagritech.tradesphere.promoter.model.Promoter;
import com.bnagritech.tradesphere.promoter.repository.PromoterRepository;
import com.bnagritech.tradesphere.territory.model.Territory;
import com.bnagritech.tradesphere.territory.repository.TerritoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BeatServiceImpl implements BeatService {
    private final BeatRepository beatRepository;
    private final PromoterRepository promoterRepository;
    private final TerritoryRepository territoryRepository;

    @Override
    public BeatsResponse createBeat(BeatsRequest request) {
        Promoter promoter= promoterRepository.findByPromoterId(request.getPromoterId())
                .orElseThrow(()-> new ResourceNotFoundException("Promoter Id does not Exists"));
        Territory territory = territoryRepository.findByTerritoryId(request.getTerritoryId())
                .orElseThrow(()-> new ResourceNotFoundException("Territory Id doesn't Exists"));

        if (beatRepository.existsByTerritoryIdAndBeatId(request.getTerritoryId(), request.getBeatId())) {
            throw new RuntimeException("Beat already exists for this territory");
        }

        if (beatRepository.existsByBeatId(request.getBeatId())) {
            throw new RuntimeException("Beat ID already exists: " + request.getBeatId());
        }

        Beat beat = Beat.builder()
                .beatId(request.getBeatId())
                .territoryId(territory.getTerritoryId())
                .promoterId(promoter.getPromoterId())
                .beatStatus(request.getBeatStatus())
                .state(request.getState())
                .city(request.getCity())
                .outletIds(
                        request.getOutletIds() != null
                                ? new ArrayList<>(request.getOutletIds())
                                : new ArrayList<>()
                )
                .beatDays(request.getBeatDays())
                .frequency(request.getFrequency())
                .build();

        Beat savedBeat = beatRepository.save(beat);

        return mapToResponse(savedBeat);
    }

    @Override
    public BeatsResponse getBeatById(String beatId) {

       Beat beat = beatRepository.findByBeatId(beatId).orElseThrow(
                       ()-> new ResourceNotFoundException("Beat Id isn't exists"));
        return mapToResponse(beat);
    }

    @Override
    public List<BeatsResponse> getAllBeats() {

        return beatRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<BeatsResponse> getBeatsByTerritory(String territoryId) {

         territoryRepository.findByTerritoryId(territoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Territory Id doesn't Exists"));

        List<Beat> beatList = beatRepository.findByTerritoryId(territoryId);
        if (beatList.isEmpty()){
            throw new ResourceNotFoundException("No beat is exists");
        }
            return beatList
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<BeatsResponse> searchByCity(String city) {
        List<Beat> beatList = beatRepository.findByCityIgnoreCase(city);
        if (beatList.isEmpty()){
            throw new ResourceNotFoundException("No beat is exists");
        }

          return beatList
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<BeatsResponse> searchByState(String state) {

        List<Beat> beatList = beatRepository.findByStateIgnoreCase(state);
        if (beatList.isEmpty()){
            throw new ResourceNotFoundException("No beat is exists");
        }
          return beatList
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<BeatsResponse> getByBeatStatus(BeatStatus beatStatus) {

        List<Beat> beatList = beatRepository.findByBeatStatus(beatStatus);
        if (beatList.isEmpty()){
            throw new ResourceNotFoundException("No beat is exists");
        }
        return beatList
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public BeatsResponse updateBeat(String beatId, BeatsRequest request) {
       Beat beat = beatRepository.findByBeatId(beatId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Beat Id not found"));
        beat.setBeatId(request.getBeatId());
        beat.setTerritoryId(request.getTerritoryId());
        beat.setPromoterId(request.getPromoterId());
        beat.setBeatStatus(request.getBeatStatus());
        beat.setState(request.getState());
        beat.setCity(request.getCity());
        beat.setBeatDays(request.getBeatDays());
        beat.setFrequency(request.getFrequency());
        if (request.getOutletIds() != null) {
            beat.setOutletIds(new ArrayList<>(request.getOutletIds()));}

        Beat updatedBeat = beatRepository.save(beat);
        return mapToResponse(updatedBeat);
    }

    @Override
    public BeatsResponse updateBeatStatus(String beatId, BeatStatus beatsStatus) {
        Beat beat = beatRepository.findByBeatId(beatId)
                .orElseThrow(()-> new ResourceNotFoundException("Beat Id not found"));

        return mapToResponse(beatRepository.save(beat));
    }

    @Override
    public BeatsResponse addOutletToBeat(String beatId, String outletId) {

        Beat beat = beatRepository.findByBeatId(beatId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Beat Id not found"));

        if (beat.getOutletIds() == null) {
            beat.setOutletIds(new ArrayList<>());
        }

        if (!beat.getOutletIds().contains(outletId)) {
            beat.getOutletIds().add(outletId);
        }

        return mapToResponse(
                beatRepository.save(beat)
        );
    }
    @Override
    public BeatsResponse removeOutletFromBeat(String beatId, String outletId) {

        Beat beat = beatRepository.findByBeatId(beatId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Beat Id not found"));

        if (beat.getOutletIds() != null) {
            beat.getOutletIds().remove(outletId);
        }

        return mapToResponse(
                beatRepository.save(beat)
        );
    }


    @Override
    public void deleteBeat(String beatId) {

        Beat beat = beatRepository.findByBeatId(beatId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Beat Id not found"));
        beatRepository.delete(beat);
    }

    private BeatsResponse mapToResponse(Beat beat) {

        return BeatsResponse.builder()
                .beatId(beat.getBeatId())
                .territoryId(beat.getTerritoryId())
                .promoterId(beat.getPromoterId())
                .beatStatus(beat.getBeatStatus())
                .state(beat.getState())
                .city(beat.getCity())
                .outletIds(beat.getOutletIds())
                .beatDays(beat.getBeatDays())
                .frequency(beat.getFrequency())
                .createdAt(beat.getCreatedAt())
                .updatedAt(beat.getUpdatedAt())
                .createdBy(beat.getCreatedBy())
                .updatedBy(beat.getUpdatedBy())
                .build();
    }


}
