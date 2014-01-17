package com.paku.mavlinkhub.fragments;

import java.util.ArrayList;

import com.paku.mavlinkhub.R;
import com.paku.mavlinkhub.enums.APP_STATE;
import com.paku.mavlinkhub.fragments.viewadapters.ViewAdapterMavlinkMsgList;
import com.paku.mavlinkhub.interfaces.IDataUpdateByteLog;
import com.paku.mavlinkhub.interfaces.IQueueMsgItemReady;
import com.paku.mavlinkhub.queue.items.ItemMavLinkMsg;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

public class FragmentRealTimeMavlink extends HUBFragment implements IDataUpdateByteLog, IQueueMsgItemReady {

	@SuppressWarnings("unused")
	private static final String TAG = "FragmentRealTimeMavlink";

	ViewAdapterMavlinkMsgList listAdapterMavLink;
	ListView listViewMavLinkMsg;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_realtime_mavlink_msglist, container, false);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		listAdapterMavLink = new ViewAdapterMavlinkMsgList(this.getActivity(), generateMavlinkListData());
		listViewMavLinkMsg = (ListView) (getView().findViewById(R.id.listView_mavlinkMsgs));
		listViewMavLinkMsg.setAdapter(listAdapterMavLink);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		hub.messenger.register(this, APP_STATE.MSG_DATA_UPDATE_BYTELOG);
		hub.messenger.register(this, APP_STATE.MSG_QUEUE_MSGITEM_READY);

		// GUI update
		onDataUpdateByteLog();
		onQueueMsgItemReady();
	}

	@Override
	public void onPause() {
		super.onPause();
		hub.messenger.unregister(this, APP_STATE.MSG_DATA_UPDATE_BYTELOG);
		hub.messenger.unregister(this, APP_STATE.MSG_QUEUE_MSGITEM_READY);
	}

	@Override
	public void onDataUpdateByteLog() {

		final TextView mTextViewBytesLog = (TextView) (getView().findViewById(R.id.textView_logByte));

		String buff;

		// get last n kb of data
		if (hub.logger.mInMemBytesStream.size() > hub.visBuffSize) {
			buff = new String(hub.logger.mInMemBytesStream.toByteArray(), hub.logger.mInMemBytesStream.size() - hub.visBuffSize, hub.visBuffSize);
		}
		else {
			buff = new String(hub.logger.mInMemBytesStream.toByteArray());

		}

		mTextViewBytesLog.setText(buff);

		// scroll down
		final ScrollView mScrollView = (ScrollView) (getView().findViewById(R.id.scrollView_logByte));

		// mScrollView.setLayoutParams(new
		// LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
		// LayoutParams.MATCH_PARENT, 4f));

		if (mScrollView != null) {

			mScrollView.post(new Runnable() {
				@Override
				public void run() {
					mScrollView.fullScroll(View.FOCUS_DOWN);
				}
			});

		}

	}

	@Override
	public void onQueueMsgItemReady() {

		listAdapterMavLink.clear();
		listAdapterMavLink.addAll(generateMavlinkListData());
		listViewMavLinkMsg.setSelection(listAdapterMavLink.getCount());

	}

	// get data to fill the list view
	private ArrayList<ItemMavLinkMsg> generateMavlinkListData() {

		// we need a clone for adapter.
		final ArrayList<ItemMavLinkMsg> clone = new ArrayList<ItemMavLinkMsg>();
		clone.addAll(hub.mavlinkQueue.getMsgItemsForUI());

		return clone;
	}

}