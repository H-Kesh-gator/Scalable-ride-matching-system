package com.ridedemo;

import java.util.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** In-memory demo with atomic driver allocation and server-owned fares. */
@RestController
@RequestMapping("/api/demo")
public class RideApi {
    public static class Place {
        public final String name; public final double lat, lon;
        Place(String name, double lat, double lon) { this.name=name; this.lat=lat; this.lon=lon; }
    }
    public static class Driver {
        public final int id; public final String name, category; public final double lat, lon;
        public boolean available = true;
        Driver(int id, String name, String category, double lat, double lon) {
            this.id=id; this.name=name; this.category=category; this.lat=lat; this.lon=lon;
        }
    }
    public static class Request {
        @NotNull public Integer pickup;
        @NotNull public Integer dropoff;
        @NotBlank public String category;
        @NotBlank @Size(max=60) public String rider;
    }
    public static class Ride {
        public long id; public String rider, pickup, dropoff, category, driver, status;
        public double distance, fare; public int driverId;
    }
    private final List<Place> places = Arrays.asList(
        new Place("Indiranagar",12.9784,77.6408), new Place("Koramangala",12.9352,77.6245),
        new Place("MG Road",12.9756,77.6067), new Place("Whitefield",12.9698,77.7500),
        new Place("HSR Layout",12.9121,77.6446), new Place("Jayanagar",12.9250,77.5938));
    private final List<Driver> drivers = Arrays.asList(
        new Driver(1,"Aarav","ECONOMY",12.98,77.64), new Driver(2,"Diya","ECONOMY",12.94,77.62),
        new Driver(3,"Kabir","COMFORT",12.97,77.61), new Driver(4,"Meera","COMFORT",12.92,77.65),
        new Driver(5,"Arjun","PREMIUM",12.96,77.73), new Driver(6,"Isha","PREMIUM",12.93,77.60));
    private final Map<Long,Ride> rides = new LinkedHashMap<>();
    private long nextId = 1001;

    @GetMapping("/places") public List<Place> places() { return places; }
    @GetMapping("/drivers") public synchronized List<Driver> drivers() { return new ArrayList<>(drivers); }
    @GetMapping("/rides") public synchronized List<Ride> rides() {
        List<Ride> result = new ArrayList<>(rides.values()); Collections.reverse(result); return result;
    }
    private void validate(Request request) {
        if (request.pickup == null || request.dropoff == null || request.pickup < 0 || request.pickup >= places.size()
            || request.dropoff < 0 || request.dropoff >= places.size() || request.pickup.equals(request.dropoff))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose two different supported locations");
        if (!Arrays.asList("ECONOMY","COMFORT","PREMIUM").contains(request.category))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported vehicle category");
        if (request.rider == null || request.rider.trim().isEmpty() || request.rider.length()>60)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Enter a rider name (1–60 characters)");
    }
    static double distance(double lat1, double lon1, double lat2, double lon2) {
        double a = Math.pow(Math.sin(Math.toRadians(lat2-lat1)/2),2)
            + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.pow(Math.sin(Math.toRadians(lon2-lon1)/2),2);
        return 6371*2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
    }
    @PostMapping("/quote") public Map<String,Double> quote(@Valid @RequestBody Request request) {
        validate(request); Place a=places.get(request.pickup), b=places.get(request.dropoff);
        double km = Math.round(distance(a.lat,a.lon,b.lat,b.lon)*10)/10.0;
        double rate = request.category.equals("PREMIUM") ? 24 : request.category.equals("COMFORT") ? 18 : 12;
        Map<String,Double> quote = new LinkedHashMap<>(); quote.put("distance",km); quote.put("fare",(double)Math.round(40+km*rate)); return quote;
    }
    @PostMapping("/rides") @ResponseStatus(HttpStatus.CREATED)
    public synchronized Ride book(@Valid @RequestBody Request request) {
        Map<String,Double> quote=quote(request); Place pickup=places.get(request.pickup);
        Driver selected=drivers.stream().filter(d -> d.available && d.category.equals(request.category))
            .min(Comparator.comparingDouble(d -> distance(pickup.lat,pickup.lon,d.lat,d.lon)))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,"No available driver in this category. Try another category."));
        Ride ride=new Ride(); ride.id=nextId++; ride.rider=request.rider.trim(); ride.pickup=pickup.name;
        ride.dropoff=places.get(request.dropoff).name; ride.category=request.category;
        ride.distance=quote.get("distance"); ride.fare=quote.get("fare"); ride.driver=selected.name;
        ride.driverId=selected.id; ride.status="ACCEPTED"; selected.available=false; rides.put(ride.id,ride); return ride;
    }
    @PostMapping("/rides/{id}/{action}") public synchronized Ride transition(@PathVariable long id, @PathVariable String action) {
        Ride ride=rides.get(id); if(ride==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Ride not found");
        if (action.equals("start") && ride.status.equals("ACCEPTED")) ride.status="IN_PROGRESS";
        else if (action.equals("complete") && ride.status.equals("IN_PROGRESS")) ride.status="COMPLETED";
        else if (action.equals("cancel") && ride.status.equals("ACCEPTED")) ride.status="CANCELLED";
        else throw new ResponseStatusException(HttpStatus.CONFLICT,"This action is not available for the current ride status");
        if(ride.status.equals("COMPLETED") || ride.status.equals("CANCELLED"))
            drivers.stream().filter(d->d.id==ride.driverId).findFirst().get().available=true;
        return ride;
    }
}
