
/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.services;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.repositoryservices.RestaurantRepositoryService;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RestaurantServiceImpl implements RestaurantService {

  private final Double peakHoursServingRadiusInKms = 3.0;
  private final Double normalHoursServingRadiusInKms = 5.0;
  @Autowired
  private RestaurantRepositoryService restaurantRepositoryService;


  // TODO: CRIO_TASK_MODULE_RESTAURANTSAPI - Implement findAllRestaurantsCloseby.
  // Check RestaurantService.java file for the interface contract.
  @Override
  public GetRestaurantsResponse findAllRestaurantsCloseBy(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
    // - For peak hours: 8AM - 10AM, 1PM-2PM, 7PM-9PM
    double latitude = getRestaurantsRequest.getLatitude();
    double longitude = getRestaurantsRequest.getLongitude();
    if (isPeakHour(currentTime)) {
      return new GetRestaurantsResponse(restaurantRepositoryService.findAllRestaurantsCloseBy(
          latitude, longitude, currentTime, peakHoursServingRadiusInKms));
    }

    return new GetRestaurantsResponse(restaurantRepositoryService.findAllRestaurantsCloseBy(
        latitude, longitude, currentTime, normalHoursServingRadiusInKms));
  }

  public boolean isTimeWithinRange(LocalTime time, LocalTime startTime, LocalTime endTime) {
    return !time.isBefore(startTime) && !time.isAfter(endTime);
  }


  public boolean isPeakHour(LocalTime time){
    return isTimeWithinRange(time, LocalTime.of(7, 59, 59), 
    LocalTime.of(10, 00, 01)) 
      || isTimeWithinRange(time, LocalTime.of(12, 59, 59), 
    LocalTime.of(14, 00, 01))
      || isTimeWithinRange(time, LocalTime.of(18, 59, 59), 
    LocalTime.of(21, 00, 01));
  }



  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Implement findRestaurantsBySearchQuery. The request object has the search string.
  // We have to combine results from multiple sources:
  // 1. Restaurants by name (exact and inexact)
  // 2. Restaurants by cuisines (also called attributes)
  // 3. Restaurants by food items it serves
  // 4. Restaurants by food item attributes (spicy, sweet, etc)
  // Remember, a restaurant must be present only once in the resulting list.
  // Check RestaurantService.java file for the interface contract.
  @Override
  public GetRestaurantsResponse findRestaurantsBySearchQuery(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
        Double servingRadiusInKms = isPeakHour(currentTime) ? peakHoursServingRadiusInKms : normalHoursServingRadiusInKms;
        String searchString= getRestaurantsRequest.getSearchFor();
        List<Restaurant> restaurants = new ArrayList<>();
        HashSet<String> restaurantIDs = new HashSet<>();
        List<List<Restaurant>> restaurantLists = new ArrayList<>();
        Double latitude = getRestaurantsRequest.getLatitude();
        Double longitude = getRestaurantsRequest.getLongitude();
        if(searchString != null && !searchString.isEmpty()){
          restaurantLists.add(restaurantRepositoryService.findRestaurantsByItemName(latitude, longitude, searchString, currentTime, servingRadiusInKms));
          restaurantLists.add(restaurantRepositoryService.findRestaurantsByName(latitude, longitude, searchString, currentTime, servingRadiusInKms));
          restaurantLists.add(restaurantRepositoryService.findRestaurantsByItemAttributes(latitude, longitude, searchString, currentTime, servingRadiusInKms));
          restaurantLists.add(restaurantRepositoryService.findRestaurantsByAttributes(latitude, longitude, searchString, currentTime, servingRadiusInKms));
        
          restaurantLists.stream()
        .flatMap(List::stream)
        .filter(restaurant -> !restaurantIDs.contains(restaurant.getRestaurantId()))
        .forEach(restaurant-> {
          restaurants.add(restaurant);
          restaurantIDs.add(restaurant.getRestaurantId());
        });
        }
        GetRestaurantsResponse response = new GetRestaurantsResponse(restaurants);
        


     return response;
  }

 
  // TODO: CRIO_TASK_MODULE_MULTITHREADING
  // Implement multi-threaded version of RestaurantSearch.
  // Implement variant of findRestaurantsBySearchQuery which is at least 1.5x time faster than
  // findRestaurantsBySearchQuery.
  @Override
  public GetRestaurantsResponse findRestaurantsBySearchQueryMt(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
        Double servingRadiusInKms = isPeakHour(currentTime) 
        ? peakHoursServingRadiusInKms : normalHoursServingRadiusInKms;
    
    String searchString = getRestaurantsRequest.getSearchFor();
    List<Restaurant> restaurants;
    if (!searchString.isEmpty()) {
      Future<List<Restaurant>> futureRestaurantsByName = restaurantRepositoryService
          .findRestaurantsByNameAsync(getRestaurantsRequest.getLatitude(), 
          getRestaurantsRequest.getLongitude(), searchString, currentTime, servingRadiusInKms);
      Future<List<Restaurant>> futureRestaurantsByAttributes = restaurantRepositoryService
          .findRestaurantsByAttributesAsync(getRestaurantsRequest.getLatitude(), 
          getRestaurantsRequest.getLongitude(), searchString, currentTime, servingRadiusInKms);

      List<Restaurant> restaurantsByName;
      List<Restaurant> restaurantsByAttributes;

      try {
        while (true) {
          if (futureRestaurantsByName.isDone() && futureRestaurantsByAttributes.isDone()) {
            restaurantsByName = futureRestaurantsByName.get();
            restaurantsByAttributes = futureRestaurantsByAttributes.get();
            break;
          }
        }
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
        return new GetRestaurantsResponse(new ArrayList<>());
      }

      Map<String,Restaurant> restaurantMap = new HashMap<>();
      for (Restaurant r: restaurantsByName) {
        restaurantMap.put(r.getRestaurantId(), r);
      }
      for (Restaurant r: restaurantsByAttributes) {
        restaurantMap.put(r.getRestaurantId(), r);
      }
      restaurants = new ArrayList<>(restaurantMap.values());
    } else {
      restaurants = new ArrayList<>();
    }

    return new GetRestaurantsResponse(restaurants);
  }
}

