/**
 * Copyright (C) 2018 Kurt Raschke <kurt@kurtraschke.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_transformer.updates;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

import java.util.List;

public class SwapStopsOnRouteStrategy implements GtfsTransformStrategy {

  @CsvField
  private String firstStopId;

  @CsvField
  private String secondStopId;

  @CsvField
  private String routeId;

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    Stop stopA = dao.getStopForId(AgencyAndId.convertFromString(firstStopId));
    Stop stopB = dao.getStopForId(AgencyAndId.convertFromString(secondStopId));
    Route targetRoute = dao.getRouteForId(AgencyAndId.convertFromString(routeId));

    List<StopTime> stopAStopTimes = dao.getStopTimesForStop(stopA);
    List<StopTime> stopBStopTimes = dao.getStopTimesForStop(stopB);

    performSwap(stopAStopTimes, stopB, targetRoute);
    performSwap(stopBStopTimes, stopA, targetRoute);

    for (StopTime st : stopAStopTimes) {
      dao.updateEntity(st);
    }

    for (StopTime st : stopBStopTimes) {
      dao.updateEntity(st);
    }

  }

  private void performSwap(List<StopTime> stopTimes, Stop toStop, Route targetRoute) {
    for (StopTime st : stopTimes) {
      if (st.getTrip().getRoute().equals(targetRoute)) {
        st.setStop(toStop);
      }
    }
  }

  public String getFirstStopId() {
    return firstStopId;
  }

  public void setFirstStopId(String firstStopId) {
    this.firstStopId = firstStopId;
  }

  public String getSecondStopId() {
    return secondStopId;
  }

  public void setSecondStopId(String secondStopId) {
    this.secondStopId = secondStopId;
  }

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }
}
