/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.controller;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.models.ErrorResponseEntity;
import com.crio.qeats.services.RestaurantService;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Log4j2
@RequestMapping(RestaurantController.RESTAURANT_API_ENDPOINT)
public class RestaurantController {

  public static final String RESTAURANT_API_ENDPOINT = "/qeats/v1";
  public static final String RESTAURANTS_API = "/restaurants";
  public static final String MENU_API = "/menu";
  public static final String CART_API = "/cart";
  public static final String CART_ITEM_API = "/cart/item";
  public static final String CART_CLEAR_API = "/cart/clear";
  public static final String POST_ORDER_API = "/order";
  public static final String GET_ORDERS_API = "/orders";

  @Autowired
  private RestaurantService restaurantService;



  @GetMapping(RESTAURANTS_API)
  public ResponseEntity<GetRestaurantsResponse> getRestaurants( GetRestaurantsRequest getRestaurantsRequest) {
    Logger log = LogManager.getLogger(RestaurantController.class);
    log.info("getRestaurants called with {}", getRestaurantsRequest);
    GetRestaurantsResponse getRestaurantsResponse;
    if (getRestaurantsRequest.getLatitude() != null && getRestaurantsRequest.getLongitude() != null
        && getRestaurantsRequest.getLatitude() >= -90 && getRestaurantsRequest.getLatitude() <= 90
        && getRestaurantsRequest.getLongitude() >= -180 
        && getRestaurantsRequest.getLongitude() <= 180) {

      List<Restaurant> restaurants = new ArrayList<>();

      if(getRestaurantsRequest.getSearchFor() != null && !getRestaurantsRequest.getSearchFor().isEmpty()){
          getRestaurantsResponse = restaurantService.findRestaurantsBySearchQuery(getRestaurantsRequest, LocalTime.now());
          if(getRestaurantsResponse == null){
            return ResponseEntity.ok().body(null);
          }
          restaurants = getRestaurantsResponse.getRestaurants();
      }
      else{
        getRestaurantsResponse = restaurantService
        .findAllRestaurantsCloseBy(getRestaurantsRequest, LocalTime.now());
      restaurants = getRestaurantsResponse.getRestaurants();
      }
        
      
      for (Restaurant r : restaurants) {
        String str = r.getName().replaceAll("[Â©éí]", "e");
        r.setName(str);
      }
      log.info("getRestaurants returned {}", getRestaurantsResponse);
      getRestaurantsResponse.setRestaurants(restaurants);
      return ResponseEntity.ok().body(getRestaurantsResponse);
    } else {
      return ResponseEntity.badRequest().body(null);
    }
  }
  // TIP(MODULE_MENUAPI): Model Implementation for getting menu given a restaurantId.
  // Get the Menu for the given restaurantId
  // API URI: /qeats/v1/menu?restaurantId=11
  // Method: GET
  // Query Params: restaurantId
  // Success Output:
  // 1). If restaurantId is present return Menu
  // 2). Otherwise respond with BadHttpRequest.
  //
  // HTTP Code: 200
  // {
  //  "menu": {
  //    "items": [
  //      {
  //        "attributes": [
  //          "South Indian"
  //        ],
  //        "id": "1",
  //        "imageUrl": "www.google.com",
  //        "itemId": "10",
  //        "name": "Idly",
  //        "price": 45
  //      }
  //    ],
  //    "restaurantId": "11"
  //  }
  // }
  // Error Response:
  // HTTP Code: 4xx, if client side error.
  //          : 5xx, if server side error.
  // Eg:
  // curl -X GET "http://localhost:8081/qeats/v1/menu?restaurantId=11"













}

