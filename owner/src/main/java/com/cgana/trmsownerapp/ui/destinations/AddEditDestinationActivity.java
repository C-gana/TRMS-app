package com.cgana.trmsownerapp.ui.destinations;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;

import com.cgana.trmsownerapp.R;
import com.cgana.trmsownerapp.data.local.TokenManager;
import com.cgana.trmsownerapp.data.model.GenericResponse;
import com.cgana.trmsownerapp.data.repository.DestinationsRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


import java.util.Locale;

public class AddEditDestinationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    // Default location (Blantyre, Malawi)
    private static final double DEFAULT_LAT = -15.7667;
    private static final double DEFAULT_LON = 35.0168;

    private MaterialToolbar toolbar;
    private TextInputLayout tilName, tilFare;
    private TextInputEditText etName, etFare;
    private Slider sliderRadius;
    private TextView tvRadiusValue, tvCoordinates;
    private MaterialButton btnCurrentLocation, btnSave;
    private ProgressBar progressBar;

    private GoogleMap googleMap;
    private Marker currentMarker;
    private Circle radiusCircle;

    private DestinationsRepository repository;
    private FusedLocationProviderClient fusedLocationClient;

    private String vehicleId;
    private int destinationId = -1;
    private boolean isEditMode = false;

    private double selectedLatitude = 0;
    private double selectedLongitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_destination);

        initViews();
        initData();
        setupMap();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tilName = findViewById(R.id.tilName);
        tilFare = findViewById(R.id.tilFare);
        etName = findViewById(R.id.etName);
        etFare = findViewById(R.id.etFare);
        sliderRadius = findViewById(R.id.sliderRadius);
        tvRadiusValue = findViewById(R.id.tvRadiusValue);
        tvCoordinates = findViewById(R.id.tvCoordinates);
        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initData() {
        TokenManager tokenManager = new TokenManager(this);
        repository = new DestinationsRepository(tokenManager);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        vehicleId = getIntent().getStringExtra("vehicle_id");
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Configure map settings for professional look
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);

        // Set initial position
        LatLng defaultLocation = new LatLng(DEFAULT_LAT, DEFAULT_LON);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f));

        // Add map click listener
        googleMap.setOnMapClickListener(latLng -> {
            setMarkerAt(latLng.latitude, latLng.longitude);
        });

        // Load intent data after map is ready
        loadIntentData();
    }

    private void setMarkerAt(double latitude, double longitude) {
        if (googleMap == null) return;

        selectedLatitude = latitude;
        selectedLongitude = longitude;

        // Update coordinates display
        tvCoordinates.setText(String.format(Locale.getDefault(),
                "Lat: %.6f, Lon: %.6f", latitude, longitude));

        // Remove existing marker
        if (currentMarker != null) {
            currentMarker.remove();
        }

        // Remove existing radius circle
        if (radiusCircle != null) {
            radiusCircle.remove();
        }

        LatLng position = new LatLng(latitude, longitude);

        // Add new marker with professional styling
        currentMarker = googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title("Selected Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .draggable(true));

        // Add radius circle to visualize alert radius
        int radius = (int) sliderRadius.getValue();
        radiusCircle = googleMap.addCircle(new CircleOptions()
                .center(position)
                .radius(radius)
                .strokeColor(getResources().getColor(R.color.primary, null))
                .strokeWidth(3f)
                .fillColor(0x220066CC));

        // Animate camera to marker
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(position));

        // Enable marker drag
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {}

            @Override
            public void onMarkerDrag(@NonNull Marker marker) {
                LatLng pos = marker.getPosition();
                tvCoordinates.setText(String.format(Locale.getDefault(),
                        "Lat: %.6f, Lon: %.6f", pos.latitude, pos.longitude));
                if (radiusCircle != null) {
                    radiusCircle.setCenter(pos);
                }
            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                LatLng pos = marker.getPosition();
                selectedLatitude = pos.latitude;
                selectedLongitude = pos.longitude;
            }
        });
    }

    private void setupListeners() {
        // Toolbar navigation
        toolbar.setNavigationOnClickListener(v -> finish());

        // Radius slider
        sliderRadius.addOnChangeListener((slider, value, fromUser) -> {
            tvRadiusValue.setText((int) value + "m");
            // Update radius circle if marker exists
            if (radiusCircle != null) {
                radiusCircle.setRadius(value);
            }
        });

        // Current location button
        btnCurrentLocation.setOnClickListener(v -> getCurrentLocation());

        // Save button
        btnSave.setOnClickListener(v -> saveDestination());
    }

    private void loadIntentData() {
        destinationId = getIntent().getIntExtra("destination_id", -1);
        isEditMode = destinationId != -1;

        if (isEditMode) {
            toolbar.setTitle(R.string.edit_destination);
            btnSave.setText(R.string.save_destination);

            String name = getIntent().getStringExtra("destination_name");
            double lat = getIntent().getDoubleExtra("latitude", DEFAULT_LAT);
            double lon = getIntent().getDoubleExtra("longitude", DEFAULT_LON);
            int fare = getIntent().getIntExtra("fare_amount", 0);
            int radius = getIntent().getIntExtra("alert_radius", 500);

            etName.setText(name);
            etFare.setText(String.valueOf(fare));
            sliderRadius.setValue(radius);
            tvRadiusValue.setText(radius + "m");

            setMarkerAt(lat, lon);
        } else {
            toolbar.setTitle(R.string.add_destination);
            // Try to get current location on start
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        setMarkerAt(location.getLatitude(), location.getLongitude());
                        if (googleMap != null) {
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(location.getLatitude(), location.getLongitude()), 17f));
                        }
                    } else {
                        Toast.makeText(this, "Could not get current location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, R.string.location_permission_required, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveDestination() {
        // Validate inputs
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String fareStr = etFare.getText() != null ? etFare.getText().toString().trim() : "";

        if (name.isEmpty()) {
            tilName.setError(getString(R.string.please_enter_name));
            return;
        }
        tilName.setError(null);

        if (fareStr.isEmpty()) {
            tilFare.setError(getString(R.string.please_enter_fare));
            return;
        }
        tilFare.setError(null);

        if (selectedLatitude == 0 && selectedLongitude == 0) {
            Toast.makeText(this, R.string.please_select_location, Toast.LENGTH_SHORT).show();
            return;
        }

        int fare = Integer.parseInt(fareStr);
        int radius = (int) sliderRadius.getValue();

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        LiveData<DestinationsRepository.Result<GenericResponse>> result;
        if (isEditMode) {
            result = repository.updateDestination(destinationId, name, selectedLatitude,
                    selectedLongitude, fare, radius);
        } else {
            result = repository.createDestination(vehicleId, name, selectedLatitude,
                    selectedLongitude, fare, radius);
        }

        result.observe(this, response -> {
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);

            if (response.isSuccess()) {
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error: " + response.getError(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

