package com.google.ar.core.codelabs.hellogeospatial.helpers;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;
import com.google.android.libraries.places.api.net.SearchNearbyResponse;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NearbySearch {

    private final PlacesClient placesClient;

    // Constructor to initialize PlacesClient
    public NearbySearch(PlacesClient placesClient) {
        this.placesClient = placesClient;

    }

    // Define a list of fields to include in the response for each returned place.
    private final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG);

    // Define a list of types to include.
    private final List<String> includedTypes = Arrays.asList();

    // Define a list of types to exclude.
    private final List<String> excludedTypes = Arrays.asList();

    /**
     * Searches for places near the given coordinates.
     *
     * @param latitude  Latitude of the search center.
     * @param longitude Longitude of the search center.
     * @param radius    Search radius in meters.
     * @return CompletableFuture containing a list of Place objects.
     */
    public CompletableFuture<List<Place>> searchNearby(double latitude, double longitude, int radius) {
        LatLng center = new LatLng(latitude, longitude);
        CircularBounds circle = CircularBounds.newInstance(center, radius);

        // Create the SearchNearbyRequest
        SearchNearbyRequest searchNearbyRequest = SearchNearbyRequest.builder(circle, placeFields)
                .setIncludedTypes(includedTypes)
                .setExcludedTypes(excludedTypes)
                .setMaxResultCount(4)
                .build();

        // Use CompletableFuture to handle asynchronous operation
        CompletableFuture<List<Place>> future = new CompletableFuture<>();

        // Perform the search
//        placesClient.searchNearby(searchNearbyRequest)
//                .addOnSuccessListener(response -> {
//                    List<Place> places = response.getPlaces();
//                    future.complete(places);
//
//
//                })
//                .addOnFailureListener(future::completeExceptionally);
        placesClient.searchNearby(searchNearbyRequest)
                .addOnSuccessListener(response -> {
                    List<Place> places = response.getPlaces(); // Get the list of places from the response

                    // Iterate through each place and get its latitude and longitude
                    for (Place place : places) {
                        LatLng placeLocation = place.getLatLng(); // Get the LatLng of the place

                        // If LatLng is null, try to fetch place details to get more information
                        if (placeLocation == null) {
                            // Create a FetchPlaceRequest with place ID and placeFields
                            FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(place.getId(), placeFields)
                                    .build();

                            // Fetch the detailed place information
                            placesClient.fetchPlace(fetchPlaceRequest)
                                    .addOnSuccessListener(fetchResponse -> {
                                        Place detailedPlace = fetchResponse.getPlace();
                                        LatLng detailedLocation = detailedPlace.getLatLng();
                                        if (detailedLocation != null) {
                                            double placeLatitude = detailedLocation.latitude;
                                            double placeLongitude = detailedLocation.longitude;

                                            // Log or process the latitude and longitude of the place
                                            Log.d("NearbyPlace", "Place: " + detailedPlace.getName() + ", Lat: " + placeLatitude + ", Lng: " + placeLongitude);
                                        } else {
                                            Log.d("NearbyPlace", "Place: " + detailedPlace.getName() + " has no location.");
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("NearbyPlace", "Failed to fetch details for place ID: " + place.getId(), e);
                                    });
                        } else {
                            // Process the location if available
                            double placeLatitude = placeLocation.latitude;
                            double placeLongitude = placeLocation.longitude;

                            // Log or process the latitude and longitude of the place
                            Log.d("NearbyPlace", "Place: " + place.getName() + ", Lat: " + placeLatitude + ", Lng: " + placeLongitude);
                        }
                    }

                    // Complete the future with the places
                    future.complete(places);
                })
                .addOnFailureListener(future::completeExceptionally); // Handle failure

        return future;

    }
}
