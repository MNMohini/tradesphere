package com.bnagritech.tradesphere.beat.service.impl;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.repository.UserRepository;
import com.bnagritech.tradesphere.beat.dto.BeatsRequest;
import com.bnagritech.tradesphere.beat.dto.BeatsResponse;
import com.bnagritech.tradesphere.beat.model.Beat;
import com.bnagritech.tradesphere.beat.repository.BeatRepository;
import com.bnagritech.tradesphere.beat.service.BeatService;
import com.bnagritech.tradesphere.common.enums.BeatStatus;
import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import com.bnagritech.tradesphere.common.exception.ResourceNotFoundException;
import com.bnagritech.tradesphere.territory.model.Territory;
import com.bnagritech.tradesphere.territory.repository.TerritoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BeatServiceImpl implements BeatService {
    private final BeatRepository beatRepository;
    private final TerritoryRepository territoryRepository;
    private final UserRepository userRepository;

    @Override
    public BeatsResponse createBeat(BeatsRequest request) {

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
                .beatStatus(request.getBeatStatus())
                .state(request.getState())
                .city(request.getCity())
                .outletCodes(
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
    public List<BeatsResponse> getMyBeats(User currentUser) {
        LinkedHashMap<String, Beat> assignedBeats = new LinkedHashMap<>();
        beatRepository.findByAssignedUserId(currentUser.getId())
                .forEach(beat -> assignedBeats.put(beat.getBeatId(), beat));
        beatRepository.findByUserNameIgnoreCase(currentUser.getUsername())
                .forEach(beat -> assignedBeats.put(beat.getBeatId(), beat));
        return assignedBeats.values().stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BeatsResponse> getAssignableBeats(User currentUser) {
        requireBeatManager(currentUser);
        List<User> team = userRepository.findByPath(currentUser.getId());
        Set<String> teamIds = team.stream().map(User::getId).collect(Collectors.toSet());
        Set<String> teamNames = team.stream().map(User::getUsername)
                .map(name -> name.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());

        return beatRepository.findAll().stream()
                .filter(beat -> isUnassigned(beat)
                        || teamIds.contains(beat.getAssignedUserId())
                        || (beat.getAssignedUserId() == null && beat.getUserName() != null
                        && teamNames.contains(beat.getUserName().toLowerCase(Locale.ROOT))))
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public BeatsResponse assignBeat(String beatId, String userId, User currentUser) {
        requireBeatManager(currentUser);

        Beat beat = beatRepository.findByBeatId(beatId)
                .orElseThrow(() -> new ResourceNotFoundException("Beat Id not found"));
        List<User> team = userRepository.findByPath(currentUser.getId());
        Set<String> teamIds = team.stream().map(User::getId).collect(Collectors.toSet());
        Set<String> teamNames = team.stream().map(User::getUsername)
                .map(name -> name.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());

        boolean assignedToTeam = teamIds.contains(beat.getAssignedUserId())
                || (beat.getAssignedUserId() == null && beat.getUserName() != null
                && teamNames.contains(beat.getUserName().toLowerCase(Locale.ROOT)));
        if (!isUnassigned(beat) && !assignedToTeam) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This beat is assigned outside your team");
        }

        User assignee = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found"));
        if (assignee.getStatus() != UserStatus.ACTIVE || !teamIds.contains(assignee.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can assign beats only to active team members");
        }
        if (!isAllowedAssignee(currentUser.getRole(), assignee.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot assign a beat to this role");
        }

        beat.setAssignedUserId(assignee.getId());
        beat.setUserName(assignee.getUsername());
        return mapToResponse(beatRepository.save(beat));
    }

    private void requireBeatManager(User user) {
        if (user.getRole() != UserRole.SALES_OFFICER && user.getRole() != UserRole.SUPERVISOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Sales Officers and Supervisors can assign beats");
        }
    }

    private boolean isAllowedAssignee(UserRole managerRole, UserRole assigneeRole) {
        return switch (managerRole) {
            case SALES_OFFICER -> assigneeRole == UserRole.SALES_REPRESENTATIVE
                    || assigneeRole == UserRole.SUPERVISOR || assigneeRole == UserRole.PROMOTER;
            case SUPERVISOR -> assigneeRole == UserRole.PROMOTER;
            default -> false;
        };
    }

    private boolean isUnassigned(Beat beat) {
        return (beat.getAssignedUserId() == null || beat.getAssignedUserId().isBlank())
                && (beat.getUserName() == null || beat.getUserName().isBlank());
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
        List<Beat> beatList = beatRepository.findBeatByCityIgnoreCase(city);
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

        List<Beat> beatList = beatRepository.findBeatByStateIgnoreCase(state);
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
        beat.setBeatStatus(request.getBeatStatus());
        beat.setState(request.getState());
        beat.setCity(request.getCity());
        beat.setBeatDays(request.getBeatDays());
        beat.setFrequency(request.getFrequency());
        if (request.getOutletIds() != null) {
            beat.setOutletCodes(new ArrayList<>(request.getOutletIds()));}

        Beat updatedBeat = beatRepository.save(beat);
        return mapToResponse(updatedBeat);
    }

    @Override
    public BeatsResponse updateBeatStatus(String beatId, BeatsRequest request) {
        Beat beat = beatRepository.findByBeatId(beatId)
                .orElseThrow(()-> new ResourceNotFoundException("Beat Id not found"));
        beat.setBeatStatus(request.getBeatStatus());
        Beat updatedBeat = beatRepository.save(beat);
        return mapToResponse(updatedBeat);
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
                .assignedUserId(beat.getAssignedUserId())
                .userName(beat.getUserName())
                .territoryId(beat.getTerritoryId())
                .beatStatus(beat.getBeatStatus())
                .state(beat.getState())
                .city(beat.getCity())
                .outletIds(beat.getOutletCodes())
                .beatDays(beat.getBeatDays())
                .frequency(beat.getFrequency())
                .createdAt(beat.getCreatedAt())
                .updatedAt(beat.getUpdatedAt())
                .createdBy(beat.getCreatedBy())
                .updatedBy(beat.getUpdatedBy())
                .build();
    }


}
