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

import static org.junit.Assert.assertEquals;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs_transformer.services.TransformContext;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class SwapStopsOnRouteStrategyTest {

  private final SwapStopsOnRouteStrategy _strategy = new SwapStopsOnRouteStrategy();

  private MockGtfs _gtfs;

  @Before
  public void before() throws IOException {
    _gtfs = MockGtfs.create();
  }

  @Test
  public void test() throws IOException {
    _gtfs.putAgencies(1);
    _gtfs.putStops(2);
    _gtfs.putRoutes(2);
    _gtfs.putDefaultCalendar();
    _gtfs.putTrips(2, "r0,r1", "WEEK");
    _gtfs.putStopTimes("t0,t1", "s0,s1");

    GtfsMutableRelationalDao dao = _gtfs.read();
    TransformContext tc = new TransformContext();
    tc.setDefaultAgencyId("a0");

    Stop stopZero = dao.getStopForId(AgencyAndId.convertFromString("a0_s0"));
    Stop stopOne = dao.getStopForId(AgencyAndId.convertFromString("a0_s1"));

    assertEquals(stopZero, getStopOnTrip(dao, "a0_t0", 0));
    assertEquals(stopOne, getStopOnTrip(dao, "a0_t0", 1));

    _strategy.setFirstStopId("a0_s0");
    _strategy.setSecondStopId("a0_s1");
    _strategy.setRouteId("a0_r0");
    _strategy.run(tc, dao);

    UpdateLibrary.clearDaoCache(dao);

    assertEquals(stopOne, getStopOnTrip(dao, "a0_t0", 0));
    assertEquals(stopZero, getStopOnTrip(dao, "a0_t0", 1));

  }

  private Stop getStopOnTrip(GtfsRelationalDao dao, String tripId, int sequence) {
    return dao.getStopTimesForTrip(dao.getTripForId(AgencyAndId.convertFromString(tripId))).get(sequence).getStop();
  }
}
