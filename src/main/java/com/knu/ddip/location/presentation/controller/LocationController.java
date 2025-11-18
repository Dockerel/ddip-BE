package com.knu.ddip.location.presentation.controller;

import com.knu.ddip.auth.domain.AuthUser;
import com.knu.ddip.auth.presentation.annotation.RequireAuth;
import com.knu.ddip.location.business.dto.UpdateMyLocationRequest;
import com.knu.ddip.location.business.service.LocationService;
import com.knu.ddip.location.presentation.api.LocationApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LocationController implements LocationApi {

    private final LocationService locationService;

    @Override
    @RequireAuth
    public ResponseEntity<Void> updateMyLocation(AuthUser user, UpdateMyLocationRequest request) {
        locationService.saveUserLocationAtomic(user.getId(), request);
        return ResponseEntity.ok().build();
    }
}
