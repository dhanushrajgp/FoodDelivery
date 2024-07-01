/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.exchanges;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.math.BigInteger;

// TODO: CRIO_TASK_MODULE_RESTAURANTSAPI
//  Implement GetRestaurantsRequest.
//  Complete the class such that it is able to deserialize the incoming query params from
//  REST API clients.
//  For instance, if a REST client calls API
//  /qeats/v1/restaurants?latitude=28.4900591&longitude=77.536386&searchFor=tamil,
//  this class should be able to deserialize lat/long and optional searchFor from that.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetRestaurantsRequest {
  @NotNull
  @Min(value = -90)
  @Max(value = 90)
  private Double latitude;
  @NotNull
  @Min(value = -180)
  @Max(value = 180)
  private Double longitude;

  private String searchFor;

  public GetRestaurantsRequest(@NotNull @Min(value = -90) @Max(value = 90) Double latitude, 
  @NotNull @Min(value = -180) @Max(value = 180) Double longitude) {
this.latitude = latitude;
this.longitude = longitude;
}
}

