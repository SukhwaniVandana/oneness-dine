package com.oneness.dine.restaurant.controller;

import com.oneness.dine.core.dto.ApiResponse;
import com.oneness.dine.restaurant.dto.RestaurantDto;
import com.oneness.dine.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/restaurant")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantDto>> getRestaurant(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(restaurantService.getRestaurant(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantDto>> updateRestaurant(
            @PathVariable Long id,
            @RequestBody RestaurantDto dto) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        restaurantService.updateRestaurant(id, dto),
                        "Restaurant updated successfully"));
    }
}