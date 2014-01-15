package com.paku.mavlinkhub.fragments;

import java.util.ArrayList;

import com.paku.mavlinkhub.ActivityMain;
import com.paku.mavlinkhub.R;
import com.paku.mavlinkhub.communication.devicelist.ListPeerDevicesBluetooth;
import com.paku.mavlinkhub.communication.devicelist.ItemPeerDevice;
import com.paku.mavlinkhub.enums.PEER_DEV_STATE;
import com.paku.mavlinkhub.fragments.viewadapters.ViewAdapterPeerDevsList;
import com.paku.mavlinkhub.interfaces.IConnectionFailed;
import com.paku.mavlinkhub.interfaces.IUiModeChanged;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class FragmentConnectivity extends HUBFragment implements IUiModeChanged, IConnectionFailed {

	private static final String TAG = "FragmentConnectivity";

	ListPeerDevicesBluetooth btDevList;
	ListView btDevListView;
	ViewAdapterPeerDevsList devListAdapter;

	View progressBarConnectingBIG;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		btDevList = new ListPeerDevicesBluetooth(globalVars);

	}

	@Override
	public void onStart() {
		super.onStart();

		refreshBtDevList();

	}

	@Override
	public void onResume() {
		super.onResume();
		globalVars.messenger.registerForOnUiModeChanged(this);
		globalVars.messenger.registerForOnConnectionFailed(this);
		refreshUI();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View connView = inflater.inflate(R.layout.fragment_connectivity, container, false);

		return connView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		btDevListView = (ListView) getView().findViewById(R.id.list_bt_bonded);
		progressBarConnectingBIG = getView().findViewById(R.id.RelativeLayoutProgressBarBig);

		refreshUI();

	}

	public void refreshUI() {

		// mostly used states set as defaults
		((ActivityMain) getActivity()).enableProgressBar(false);
		progressBarConnectingBIG.setVisibility(View.INVISIBLE);
		btDevListView.setVisibility(View.VISIBLE);

		switch (globalVars.uiMode) {
		case UI_MODE_CREATED:
			break;
		case UI_MODE_TURNING_ON:
			progressBarConnectingBIG.setVisibility(View.VISIBLE);
			break;
		case UI_MODE_STATE_ON:
			refreshBtDevList();
			break;
		case UI_MODE_TURNING_OFF:
			progressBarConnectingBIG.setVisibility(View.VISIBLE);
			btDevListView.setVisibility(View.INVISIBLE);
			break;
		case UI_MODE_STATE_OFF:
			btDevListView.setVisibility(View.INVISIBLE);
			break;
		case UI_MODE_CONNECTED:
			((ActivityMain) getActivity()).enableProgressBar(true);
			refreshBtDevListView();
			break;
		case UI_MODE_DISCONNECTED:
			refreshBtDevListView();
			break;
		default:
			break;
		}

	}

	// to be called on possible peer BT device state change (connect disconnect
	// etc)
	private void refreshBtDevListView() {
		final ArrayList<ItemPeerDevice> clone = new ArrayList<ItemPeerDevice>();
		clone.addAll(btDevList.getDeviceList());
		devListAdapter.clear();
		devListAdapter.addAll(clone);
	}

	// to be called on start and after re-enabling the BT module
	private void refreshBtDevList() {

		switch (btDevList.refresh()) {
		case ERROR_NO_ADAPTER:
			Toast.makeText(getActivity().getApplicationContext(), R.string.no_bluetooth_adapter_found,
					Toast.LENGTH_LONG).show();
			return;
		case ERROR_ADAPTER_OFF:
			final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			// this.startActivityForResult(enableBtIntent,
			// APP_STATE.REQUEST_ENABLE_BT);
			this.startActivity(enableBtIntent);
			return;
		case ERROR_NO_BONDED_DEV:
			Toast.makeText(getActivity().getApplicationContext(),
					R.string.error_no_paired_bt_devices_found_pair_device_first, Toast.LENGTH_LONG).show();
			return;

		case LIST_OK:
			devListAdapter = new ViewAdapterPeerDevsList(this.getActivity(), btDevList.getDeviceList());
			btDevListView.setAdapter(devListAdapter);
			btDevListView.setOnItemClickListener(btListClickListener);
			return;
		default:
			break;

		}

	}

	private final AdapterView.OnItemClickListener btListClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			final ItemPeerDevice selectedDev = btDevList.getItem(position);
			// TextView txtView = (TextView)
			// view.findViewById(R.id.listViewItemTxt_dev_name);

			switch (selectedDev.getState()) {
			case DEV_STATE_UNKNOWN:
			case DEV_STATE_DISCONNECTED:
				if (!globalVars.droneClient.isConnected()) {
					globalVars.logger.sysLog(TAG, "Connecting...");
					globalVars.logger.sysLog(TAG, "Me  : " + globalVars.droneClient.getMyName() + " ["
							+ globalVars.droneClient.getMyAddress() + "]");
					globalVars.logger.sysLog(TAG, "Peer: " + selectedDev.getName() + " [" + selectedDev.getAddress()
							+ "]");

					globalVars.droneClient.startConnection(selectedDev.getAddress());
					globalVars.msgCenter.mavlinkCollector.startMavLinkParserThread();

					btDevList.setDevState(position, PEER_DEV_STATE.DEV_STATE_CONNECTED);
				}
				else {
					Log.d(TAG, "Connect on connected device attempt");
					Toast.makeText(getActivity(), R.string.disconnect_first, Toast.LENGTH_SHORT).show();
				}

				break;

			case DEV_STATE_CONNECTED:
				if (globalVars.droneClient.isConnected()) {
					globalVars.logger.sysLog(TAG, "Closing Connection ...");
					globalVars.droneClient.stopConnection();
					globalVars.msgCenter.mavlinkCollector.stopMavLinkParserThread();
					btDevList.setDevState(position, PEER_DEV_STATE.DEV_STATE_DISCONNECTED);
				}
				else {
					Log.d(TAG, "Already disconnected ...");
				}

				break;

			default:
				break;
			}

		}
	};

	// interfaces
	@Override
	public void onConnectionFailed(String errorMsg) {

		globalVars.logger.sysLog(TAG, errorMsg);
		globalVars.droneClient.stopConnection();
		globalVars.msgCenter.mavlinkCollector.stopMavLinkParserThread();

		Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();

		btDevList.setAllDevState(PEER_DEV_STATE.DEV_STATE_DISCONNECTED);
		refreshBtDevListView();

	}

	@Override
	public void onUiModeChanged() {
		refreshUI();
	}

}