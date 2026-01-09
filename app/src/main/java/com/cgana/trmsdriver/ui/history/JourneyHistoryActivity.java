package com.cgana.trmsdriver.ui.history;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cgana.trmsdriver.R;
import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.data.model.Journey;
import com.cgana.trmsdriver.data.repository.JourneyHistoryRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Journey History Activity (Module 5 Part 3)
 */
public class JourneyHistoryActivity extends AppCompatActivity implements JourneyAdapter.OnJourneyClickListener {

    // UI Components
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private TextInputEditText etSearch;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerJourneys;
    private LinearLayout emptyState;
    private View loadingOverlay;

    // ViewModel & Adapter
    private JourneyHistoryViewModel viewModel;
    private JourneyAdapter adapter;

    // Data
    private TokenManager tokenManager;
    private String vehicleId;
    private boolean isLoadingMore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_history);

        // Initialize TokenManager
        tokenManager = new TokenManager(this);

        // Get vehicle ID
        if (tokenManager.getDriver() != null && tokenManager.getDriver().getVehicleId() != null) {
            vehicleId = tokenManager.getDriver().getVehicleId();
        } else {
            vehicleId = "TRM-BT-001"; // Fallback
        }

        // Initialize views
        initializeViews();

        // Setup toolbar
        setupToolbar();

        // Setup ViewModel
        setupViewModel();

        // Setup tabs
        setupTabs();

        // Setup search
        setupSearch();

        // Setup recycler view
        setupRecyclerView();

        // Setup swipe refresh
        setupSwipeRefresh();

        // Observe ViewModel
        observeViewModel();

        // Load initial data
        viewModel.loadJourneys(vehicleId, "all");
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        etSearch = findViewById(R.id.etSearch);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerJourneys = findViewById(R.id.recyclerJourneys);
        emptyState = findViewById(R.id.emptyState);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViewModel() {
        JourneyHistoryRepository repository = new JourneyHistoryRepository(tokenManager);
        JourneyHistoryViewModelFactory factory = new JourneyHistoryViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(JourneyHistoryViewModel.class);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.filter_all));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.filter_today));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.filter_week));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.filter_month));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.filter_fare_collected));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.filter_fare_not_collected));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String filter = getFilterFromTab(tab.getPosition());
                viewModel.loadJourneys(vehicleId, filter);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private String getFilterFromTab(int position) {
        switch (position) {
            case 0: return "all";
            case 1: return "today";
            case 2: return "week";
            case 3: return "month";
            case 4: return "fare_collected";
            case 5: return "fare_not_collected";
            default: return "all";
        }
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchJourneys(vehicleId, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new JourneyAdapter(this, this);
        recyclerJourneys.setLayoutManager(new LinearLayoutManager(this));
        recyclerJourneys.setAdapter(adapter);

        // Pagination - load more on scroll
        recyclerJourneys.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoadingMore) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        viewModel.loadMoreJourneys(vehicleId);
                    }
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> viewModel.refreshJourneys(vehicleId));
    }

    private void observeViewModel() {
        viewModel.getJourneyHistoryState().observe(this, state -> {
            switch (state.getStatus()) {
                case LOADING:
                    showLoading(true);
                    swipeRefresh.setRefreshing(false);
                    isLoadingMore = false;
                    break;

                case LOADING_MORE:
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                    isLoadingMore = true;
                    break;

                case SUCCESS:
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                    isLoadingMore = false;

                    if (state.getJourneys() != null && !state.getJourneys().isEmpty()) {
                        adapter.setJourneys(state.getJourneys());
                        recyclerJourneys.setVisibility(View.VISIBLE);
                        emptyState.setVisibility(View.GONE);
                    } else {
                        recyclerJourneys.setVisibility(View.GONE);
                        emptyState.setVisibility(View.VISIBLE);
                    }
                    break;

                case ERROR:
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                    isLoadingMore = false;
                    Toast.makeText(this, state.getError(), Toast.LENGTH_SHORT).show();
                    break;

                case IDLE:
                default:
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                    isLoadingMore = false;
                    break;
            }
        });
    }

    @Override
    public void onJourneyClick(Journey journey) {
        // Show journey detail dialog
        JourneyDetailDialog dialog = JourneyDetailDialog.newInstance(journey);
        dialog.show(getSupportFragmentManager(), "JourneyDetailDialog");
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_journey_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

