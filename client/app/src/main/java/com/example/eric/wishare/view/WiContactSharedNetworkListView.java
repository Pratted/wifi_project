package com.example.eric.wishare.view;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.R;
import com.example.eric.wishare.WiSQLiteDatabase;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.*;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.List;

public class WiContactSharedNetworkListView extends LinearLayout {

    private CheckBox mSelectAll;
    private Button mNetworkLabel;

    private LinearLayout mSharedNetworkListItems;

    private List<WiContactSharedNetworkListViewItem> mSharedNetworkListItem;
    private List<WiConfiguration> mSharedNetworks;

    public interface OnCheckBoxVisibleListener {
        void onCheckBoxVisible();
    }

    private OnCheckBoxVisibleListener listener;

    public void setOnCheckBoxVisibleListener(OnCheckBoxVisibleListener listener) {
        this.listener = listener;
    }

    public WiContactSharedNetworkListView(Context c) {
        super(c);

        init();
    }

    public WiContactSharedNetworkListView(Context c, @Nullable AttributeSet attrs) {
        super(c, attrs);

        init();
    }

    public void init() {
        inflate(getContext(), R.layout.layout_contact_shared_network_list, this);

        mSharedNetworkListItem = new ArrayList<>();
        mSharedNetworks = new ArrayList<>();

        mSelectAll = findViewById(R.id.cb_select_all_networks);
        mNetworkLabel = findViewById(R.id.btn_refresh_networks);

//        mNetworkLabel.setOnClickListener(refreshListView());

        mSharedNetworkListItems = findViewById(R.id.ll_network_list_items);

        mSelectAll.setVisibility(GONE);
        mSelectAll.setOnCheckedChangeListener(onSelect());
    }

    private OnClickListener refreshListView() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Networks refreshed.", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private CompoundButton.OnCheckedChangeListener onSelect() {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for(WiContactSharedNetworkListViewItem network : mSharedNetworkListItem) {
                    network.mCheckBox.setChecked(isChecked);
                }
            }
        };
    }

    public void populateNetworks(Context context, WiContact contact){
        ArrayList<String> SSIDList = WiSQLiteDatabase.getInstance(context).getContactNetworks(contact);
        for (String SSID: SSIDList){
                WiConfiguration config = new WiConfiguration(SSID, "");

                addSharedNetwork(config);
                Log.d("SharedNetworkList", config.getSSID() + " added");
        }
    }

    public void addSharedNetwork(WiConfiguration config) {
        WiContactSharedNetworkListViewItem item = new WiContactSharedNetworkListViewItem(getContext(), config);

        mSharedNetworks.add(config);
        mSharedNetworkListItems.addView(item);
    }

    public boolean contains(WiConfiguration configuration) {
        return mSharedNetworks.contains(configuration);
    }

    public void showAllCheckBoxes() {
        mSelectAll.setVisibility(VISIBLE);
        for(WiContactSharedNetworkListViewItem child : mSharedNetworkListItem) {
            child.mCheckBox.setVisibility(VISIBLE);
        }
    }

    public void hideAllCheckBoxes() {
        mSelectAll.setVisibility(GONE);
        mSelectAll.setChecked(false);

        for(WiContactSharedNetworkListViewItem child : mSharedNetworkListItem) {
            child.mCheckBox.setVisibility(GONE);
            child.mCheckBox.setChecked(false);
        }
    }

    public void hideSelectedNetworks() {
        for(WiContactSharedNetworkListViewItem network : mSharedNetworkListItem) {
            System.out.println("Checkbox: " + network.getCheckBoxStatus());
            if(network.getCheckBoxStatus()) {
                network.hide();
            }
        }
    }

    public MaterialDialog.SingleButtonCallback hideAllNetworks() {

        return new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                for(WiContactSharedNetworkListViewItem network : mSharedNetworkListItem) {
                    network.hide();
                }
            }
        };
    }

    private class WiContactSharedNetworkListViewItem extends LinearLayout {

        private WiConfiguration mConfig;
        private CheckBox mCheckBox;

        private Button mNetworkActivityButton;

        private TextView mNetworkName;

        private LinearLayout mContactsWithSharedNetwork;

        private ExpandableLayout mExpandableLayout;

        public WiContactSharedNetworkListViewItem(Context c, WiConfiguration config) {
            super(c);
            mConfig = config;

            init();
        }

        public void init() {
            inflate(getContext(), R.layout.layout_contact_shared_network_list_item, this);

            mCheckBox = findViewById(R.id.cb_network_select);


            mExpandableLayout = findViewById(R.id.expandable_network);

            mNetworkName = findViewById(R.id.btn_network_name);
            mNetworkName.setText(mConfig.getSSID());

            mNetworkActivityButton = findViewById(R.id.btn_network_activity);

            //mContactsWithSharedNetwork = findViewById(R.id.ll_contacts_with_shared_network);

//            mNetworkName.setOnClickListener(expand());
            mNetworkName.setOnLongClickListener(onLongClick());
            (findViewById(R.id.center_view)).setOnClickListener(expand());
            (findViewById(R.id.center_view)).setOnLongClickListener(onLongClick());

            GraphView graph = (GraphView) findViewById(R.id.graph);
            LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                    new DataPoint(1, 1),
                    new DataPoint(2, 5),
                    new DataPoint(3, 3),
                    new DataPoint(4, 2),
                    new DataPoint(5, 6)
            });
            graph.addSeries(series);
            GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
            graph.getGridLabelRenderer().setHumanRounding(true);
            int myColor = getContext().getResources().getColor(R.color.themeGreen);
            series.setColor(myColor);
//            gridLabel.setHorizontalAxisTitle("Days");
//            gridLabel.setVerticalAxisTitle("Gb");
        }

        private OnLongClickListener onLongClick() {
            return new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showAllCheckBoxes();
                    listener.onCheckBoxVisible();
                    return false;
                }
            };
        }

        private View.OnClickListener expand() {
            return new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mExpandableLayout.isExpanded()) {
                        mExpandableLayout.collapse();
                    } else {
                        mExpandableLayout.expand();
                    }
                }
            };
        }

        private void hide() {
            mSharedNetworkListItems.removeView(this);
        }

        private boolean getCheckBoxStatus() {
            return mCheckBox.isChecked();
        }

        private String getName() {
            return mNetworkName.getText().toString();
        }
    }
}
