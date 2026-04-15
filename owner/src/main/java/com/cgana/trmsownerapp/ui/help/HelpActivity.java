package com.cgana.trmsownerapp.ui.help;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cgana.trmsownerapp.R;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.List;

public class HelpActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView recyclerHelp;
    private HelpAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        toolbar = findViewById(R.id.toolbar);
        recyclerHelp = findViewById(R.id.recyclerHelp);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        setupHelp();
    }

    private void setupHelp() {
        List<HelpItem> helpItems = new ArrayList<>();

        // Getting Started
        helpItems.add(new HelpItem(
            "Getting Started",
            "Learn how to use the TRMS Owner app",
            "1. Login with your phone number\n" +
            "2. Select your vehicle (if you have multiple)\n" +
            "3. View real-time dashboard\n" +
            "4. Monitor passenger journeys\n" +
            "5. Manage destinations"
        ));

        // Dashboard
        helpItems.add(new HelpItem(
            "Dashboard",
            "Understanding the dashboard",
            "The dashboard shows:\n" +
            "• Vehicle status (online/offline)\n" +
            "• 4 seat grid with real-time status\n" +
            "• Vacant (grey) - empty seat\n" +
            "• Awaiting (blue) - passenger boarded, no destination\n" +
            "• Active (green) - journey in progress\n" +
            "• Approaching (orange) - near destination\n\n" +
            "Auto-refreshes every 10 seconds"
        ));

        // Journeys
        helpItems.add(new HelpItem(
            "Journeys",
            "View journey history",
            "Use the Journeys tab to:\n" +
            "• View all completed journeys\n" +
            "• Filter by date range\n" +
            "• Quick filters (Today, Week, Month)\n" +
            "• Tap journey to see details\n" +
            "• Export to CSV for reporting"
        ));

        // Destinations
        helpItems.add(new HelpItem(
            "Destinations",
            "Manage destinations",
            "Add/Edit destinations:\n" +
            "1. Tap + button\n" +
            "2. Enter destination name\n" +
            "3. Set fare amount\n" +
            "4. Adjust alert radius (100-1000m)\n" +
            "5. Tap map to select location\n" +
            "6. Save\n\n" +
            "Delete: Tap delete button, confirm"
        ));

        // Alerts
        helpItems.add(new HelpItem(
            "Alerts",
            "Understanding alerts",
            "Alert types:\n" +
            "⏱️ Timeout - Destination not selected in 90s\n" +
            "📍 Proximity - Vehicle approaching destination\n" +
            "⚠️ Missed Stop - Passenger didn't alight\n" +
            "🚨 Confirmed Missed Stop - Critical alert\n\n" +
            "Tap alert to acknowledge and add notes"
        ));

        // Analytics
        helpItems.add(new HelpItem(
            "Analytics",
            "Track your vehicle performance",
            "View analytics for:\n" +
            "• Total journeys and revenue\n" +
            "• Revenue by destination\n" +
            "• Peak hours chart\n" +
            "• Driver compliance metrics\n" +
            "• Average journey duration\n\n" +
            "Filter by: Today, Week, or Month"
        ));

        // Offline Mode
        helpItems.add(new HelpItem(
            "Offline Mode",
            "Using the app without internet",
            "The app works offline:\n" +
            "• Dashboard shows cached data\n" +
            "• Journeys show recent history\n" +
            "• Destinations are cached\n" +
            "• Orange banner shows offline status\n\n" +
            "Data syncs automatically when online"
        ));

        // Settings
        helpItems.add(new HelpItem(
            "Settings",
            "Customize your app",
            "Settings include:\n" +
            "• Theme: Light, Dark, or System\n" +
            "• Notifications: Enable/disable\n" +
            "• Profile: View your info\n" +
            "• FCM Token: For push notifications\n" +
            "• Logout: Sign out safely"
        ));

        // Vehicle Selection
        helpItems.add(new HelpItem(
            "Multiple Vehicles",
            "Managing multiple vehicles",
            "If you have multiple vehicles:\n" +
            "• Tap the car icon in toolbar\n" +
            "• Select vehicle from list\n" +
            "• All screens update automatically\n" +
            "• Your selection is remembered\n\n" +
            "Switch anytime from any screen"
        ));

        adapter = new HelpAdapter(helpItems);
        recyclerHelp.setLayoutManager(new LinearLayoutManager(this));
        recyclerHelp.setAdapter(adapter);
    }
}

