package com.ridedemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.concurrent.*;
import java.util.*;

@SpringBootTest(classes=RideDemoApplication.class)
@AutoConfigureMockMvc
class RideApiTest {
    @Autowired MockMvc mvc;
    static RideApi.Request request() {
        RideApi.Request r=new RideApi.Request();r.rider="Test rider";r.pickup=0;r.dropoff=1;r.category="ECONOMY";return r;
    }
    @Test void servesUiAndValidatesRequests() throws Exception {
        mvc.perform(get("/index.html")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("rideflow")));
        mvc.perform(post("/api/demo/rides").contentType("application/json").content("{}" )).andExpect(status().isBadRequest());
        mvc.perform(post("/api/demo/quote").contentType("application/json").content("{\"rider\":\"Guest\",\"pickup\":0,\"dropoff\":1,\"category\":\"ECONOMY\"}")).andExpect(status().isOk()).andExpect(jsonPath("fare").isNumber());
        mvc.perform(post("/api/demo/rides/99999/start").contentType("application/json").content("{}")).andExpect(status().isNotFound());
    }
    @Test void lifecycleReleasesDriverAndRejectsInvalidTransitions() {
        RideApi api=new RideApi();RideApi.Ride first=api.book(request());
        assertThrows(ResponseStatusException.class,()->api.transition(first.id,"complete"));
        api.transition(first.id,"start");assertThrows(ResponseStatusException.class,()->api.transition(first.id,"cancel"));
        api.transition(first.id,"complete");assertTrue(api.drivers().stream().allMatch(d->d.available));
        assertThrows(ResponseStatusException.class,()->api.transition(first.id,"start"));
        RideApi.Ride second=api.book(request());api.transition(second.id,"cancel");assertTrue(api.drivers().stream().allMatch(d->d.available));
    }
    @Test void rejectsSamePickupAndDropoff() {
        RideApi.Request r=request();r.dropoff=r.pickup;
        assertThrows(ResponseStatusException.class,()->new RideApi().book(r));
    }
    @Test void concurrentBookingsCannotDoubleAssignDrivers() throws Exception {
        RideApi api=new RideApi();ExecutorService pool=Executors.newFixedThreadPool(8);
        try {
            List<Callable<Long>> jobs=new ArrayList<>();
            for(int i=0;i<8;i++) jobs.add(()->{try{return api.book(request()).id;}catch(ResponseStatusException e){assertEquals(409,e.getRawStatusCode());return null;}});
            int accepted=0;for(Future<Long> f:pool.invokeAll(jobs))if(f.get()!=null)accepted++;
            assertEquals(2,accepted);assertEquals(2,api.rides().stream().map(r->r.driverId).distinct().count());
        } finally {pool.shutdownNow();}
    }
}
