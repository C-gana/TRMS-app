package com.cgana.trmsownerapp.ui.route;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cgana.trmsownerapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class RouteHistoryActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MaterialToolbar toolbar;
    private GoogleMap googleMap;
    private FloatingActionButton fabMapType;
    private ProgressBar progressBar;
    private String vehicleId;
    private int currentMapType = GoogleMap.MAP_TYPE_NORMAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_history);

        toolbar = findViewById(R.id.toolbar);
        fabMapType = findViewById(R.id.fabMapType);
        progressBar = findViewById(R.id.progressBar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        vehicleId = getIntent().getStringExtra("vehicle_id");

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        setupMapTypeToggle();
    }

    private void setupMapTypeToggle() {
        fabMapType.setOnClickListener(v -> {
            if (googleMap != null) {
                // Cycle through map types
                switch (currentMapType) {
                    case GoogleMap.MAP_TYPE_NORMAL:
                        currentMapType = GoogleMap.MAP_TYPE_SATELLITE;
                        break;
                    case GoogleMap.MAP_TYPE_SATELLITE:
                        currentMapType = GoogleMap.MAP_TYPE_HYBRID;
                        break;
                    case GoogleMap.MAP_TYPE_HYBRID:
                        currentMapType = GoogleMap.MAP_TYPE_TERRAIN;
                        break;
                    default:
                        currentMapType = GoogleMap.MAP_TYPE_NORMAL;
                        break;
                }
                googleMap.setMapType(currentMapType);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Configure map settings for professional look
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setTiltGesturesEnabled(true);

        // Center on Blantyre, Malawi initially
        LatLng blantyre = new LatLng(-15.7861, 35.0058);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(blantyre, 13f));

        loadRouteHistory();
    }

    private void loadRouteHistory() {
        progressBar.setVisibility(View.VISIBLE);
        // TODO: Call API to get route points
        // For now, show sample route
        showSampleRoute();
    }

    private void showSampleRoute() {
        if (googleMap == null) return;

        // Sample route points around Blantyre (replace with API data)
        List<LatLng> routePoints = new ArrayList<>();
        routePoints.add(new LatLng(-15.7891, 35.0412));
        routePoints.add(new LatLng(-15.7920, 35.0450));
        routePoints.add(new LatLng(-15.7950, 35.0480));
        routePoints.add(new LatLng(-15.7980, 35.0510));
        routePoints.add(new LatLng(-15.8021, 35.0265));

        // Draw polyline with professional styling
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(routePoints)
                .width(12f)
                .color(getResources().getColor(R.color.primary, null))
                .geodesic(true)
                .startCap(new RoundCap())
                .endCap(new RoundCap());

        googleMap.addPolyline(polylineOptions);

        // Add start marker
        if (!routePoints.isEmpty()) {
            googleMap.addMarker(new MarkerOptions()
                    .position(routePoints.get(0))
                    .title("Start")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            // Add end marker
            googleMap.addMarker(new MarkerOptions()
                    .position(routePoints.get(routePoints.size() - 1))
                    .title("End")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }

        // Zoom to fit all points
        if (!routePoints.isEmpty()) {
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            for (LatLng point : routePoints) {
                boundsBuilder.include(point);
            }
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
        }

        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, "Showing route for today", Toast.LENGTH_SHORT).show();
    }
}

