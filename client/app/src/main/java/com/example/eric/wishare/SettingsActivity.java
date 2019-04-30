package com.example.eric.wishare;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.dialog.WiInvitationAcceptDeclineDialog;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiInvitation;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
    private static String TAG = "SettingsActivity";

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "Received a new intent!");

        if(intent != null){
            if(intent.hasExtra("invitation")){
                Log.d(TAG, "Prepaing invitation dialog...");
                WiInvitation invitation = intent.getParcelableExtra("invitation");
                new WiInvitationAcceptDeclineDialog(this, invitation).show();
            }
        }
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        WiSharedPreferences.initialize(this);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName)
                || DeveloperPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("example_text"));
            bindPreferenceSummaryToValue(findPreference("example_list"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DeveloperPreferenceFragment extends PreferenceFragment {
        Preference prefRebuildDatabase;
        Preference prefAddContact;

        SwitchPreference prefSendInvitationsToSelf;
        SwitchPreference prefEnableDatabase;
        SwitchPreference prefEnableWifiManager;
        SwitchPreference prefDemoMode;

        EditTextPreference prefPhone;
        EditTextPreference prefHost;

        private String TAG = "DeveloperSettingsFrag";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_developer);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("pk_port"));
            bindPreferenceSummaryToValue(findPreference("pk_host"));
            bindPreferenceSummaryToValue(findPreference("pk_phone"));

            prefPhone = (EditTextPreference) findPreference("pk_phone");
            prefPhone.setSummary(WiUtils.getDevicePhone());
            prefPhone.setDefaultValue(WiUtils.getDevicePhone());
            prefPhone.setText(WiUtils.getDevicePhone());

            prefEnableDatabase = (SwitchPreference) findPreference("pk_enable_database");
            prefSendInvitationsToSelf = (SwitchPreference) findPreference("pk_send_invitations_to_self");
            prefEnableWifiManager = (SwitchPreference) findPreference("pk_wifi_manager");
            prefHost = (EditTextPreference) findPreference("pk_host");

            prefRebuildDatabase = findPreference("pk_rebuild_database");
            prefAddContact = findPreference("pk_add_contact");
            prefDemoMode = (SwitchPreference)findPreference("pk_demo_mode");

            prefRebuildDatabase.setOnPreferenceClickListener(displayRebuildDatabaseDialog());
            prefAddContact.setOnPreferenceClickListener(displayAddContactDialog());
        }

        private Preference.OnPreferenceClickListener displayRebuildDatabaseDialog(){
            return new Preference.OnPreferenceClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new MaterialDialog.Builder(getContext())
                            .title("Rebuild Database")
                            .content("Are you sure you want to rebuild the database?")
                            .positiveText("yes")
                            .negativeText("cancel")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    WiSQLiteDatabase.getInstance(getContext()).reset(); // this doesn't work yet...
                                    Toast.makeText(getContext(), "Rebuilt database!", Toast.LENGTH_LONG).show();
                                }
                            }).show();
                    return false;
                }
            };
        }

        private Preference.OnPreferenceClickListener displayAddContactDialog(){
            return new Preference.OnPreferenceClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final WiContact contact = new WiContact("", WiUtils.randomPhoneNumber());

                    new MaterialDialog.Builder(getContext())
                            .title("Add a Contact")
                            .input("Name", "", false, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    contact.setName(input.toString());
                                    Log.d(TAG, "Setting contact name...");
                                }
                            })
                            .content("Enter the contact's name")
                            .positiveText("Add Contact")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    WiContactList.getInstance(getContext()).save(contact);
                                }
                            })
                            .neutralText("Networks")
                            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    final List<WiConfiguration> networks = WiNetworkManager.getInstance(getContext()).getConfiguredNetworks();
                                    final List<String> ssids = new ArrayList<>();
                                    String name = dialog.getInputEditText().getText().toString();
                                    contact.setName(name);

                                    for(WiConfiguration config: networks){
                                        ssids.add(config.SSID.replace("\"", ""));
                                    }

                                    new MaterialDialog.Builder(getContext())
                                            .title("Permitted Networks")
                                            .items(ssids)
                                            .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                                                @Override
                                                public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                                    return false;
                                                }
                                            })
                                            .negativeText("Cancel")
                                            .positiveText("Add Contact")
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    Integer indices[] = dialog.getSelectedIndices();

                                                    for(int i: indices){
                                                        Log.d(TAG, "Granting access to "+ networks.get(i).SSID);
                                                        contact.grantAccess(networks.get(i));
                                                    }

                                                    Log.d(TAG, "Saved contact: " + contact.getName());

                                                    WiSQLiteDatabase.getInstance(getContext()).insert(contact);
                                                    WiContactList.getInstance(getContext()).save(contact);
                                                }
                                            })
                                            .show();
                                }
                            })
                            .negativeText("Cancel")
                            .show();
                    return false;
                }
            };
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            Log.d("SETTINGS", "Clicked");
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.d(TAG, " onPause() called");

            savePreferences();
        }

        private void savePreferences(){
            Log.d(TAG, "Saving preferences...");
            boolean dbEnabled = prefEnableDatabase.isChecked();
            boolean sendInvitationsToSelf = prefSendInvitationsToSelf.isChecked();
            boolean wifiManagerEnabled = prefEnableWifiManager.isChecked();
            boolean demoEnabled = prefDemoMode.isChecked();

            WiSharedPreferences.putBoolean(WiSharedPreferences.KEY_DATABASE_ENABLED, dbEnabled);
            WiSharedPreferences.putBoolean(WiSharedPreferences.KEY_SEND_INVITATIONS_TO_SELF, sendInvitationsToSelf);
            WiSharedPreferences.putBoolean(WiSharedPreferences.KEY_WIFI_MANAGER_ENABLED, wifiManagerEnabled);
            WiSharedPreferences.putString(WiSharedPreferences.KEY_HOST, prefHost.getText());
            WiSharedPreferences.putBoolean(WiSharedPreferences.KEY_DEMO_MODE, demoEnabled);

            Log.d(TAG, "DB Enabled? " + dbEnabled);
            Log.d(TAG, "Send invite to self? " + sendInvitationsToSelf);
            Log.d(TAG, "WifiManger Enabled? " + sendInvitationsToSelf);
            Log.d(TAG, "Host: " + prefHost.getText());

            WiSharedPreferences.save();
            Log.d(TAG, "Saved preferences!");
        }
    }
}
