package com.example.travelagency.trip;

import com.example.travelagency.exception.GuideNotFoundException;
import com.example.travelagency.exception.TripNotFoundException;
import com.example.travelagency.user.AppUser;
import com.example.travelagency.trip.Trip;
import com.example.travelagency.user.AppUserRepository;
import com.example.travelagency.trip.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {
    private static final int PAGE_SIZE = 5;
    private final TripRepository tripRepository;
    private final AppUserRepository appUserRepository;

    public Trip addTrip(Trip trip) {
        return tripRepository.save(trip);
    }

    public Trip getTrip(Long id) {
        return tripRepository.findById(id).orElseThrow(() -> new TripNotFoundException(id));
    }

    public List<Trip> getAllTrips(int page) {
        return tripRepository.findAllTrips(PageRequest.of(page, PAGE_SIZE));
    }

    // users extracted in another query to eliminate N + 1 problem
    public List<Trip> getAllTripsWithUsers(int page) {
        List<Trip> trips = tripRepository.findAllTrips(PageRequest.of(page, PAGE_SIZE));
        List<Long> ids = trips.stream()
                .map(Trip::getId)
                .toList();
        List<AppUser> users = appUserRepository.findByTripIds(ids);
        trips.forEach(trip -> trip.setAppUsers(extractUsers(users, trip.getId())));
        return trips;
    }

    private List<AppUser> extractUsers(List<AppUser> users, Long id) {
        return users.stream()
                .filter(trip -> trip.getId().equals(id))
                .toList();
    }


    public void updateTrip(Trip trip) {
        Trip tripUpdated = tripRepository.findById(trip.getId())
                .orElseThrow(() -> new GuideNotFoundException(trip.getId()));
        tripUpdated.setPrice(trip.getPrice());
        tripUpdated.setDepartureDate(trip.getDepartureDate());
        tripUpdated.setReturnDate(trip.getReturnDate());
        tripUpdated.setDestination(trip.getDestination());
        tripUpdated.setGuide(trip.getGuide());
        tripRepository.save(tripUpdated);
    }

    @Transactional
    public void deleteTrip(Long id) {
        Trip trip = tripRepository.findById(id).orElseThrow(() -> new TripNotFoundException(id));
        //trip.getAppUsers().forEach(trip::removeUser);
        for(AppUser user: new ArrayList<>(trip.getAppUsers())) {
            trip.removeUser(user);
        }
        tripRepository.deleteById(id);
    }
}
