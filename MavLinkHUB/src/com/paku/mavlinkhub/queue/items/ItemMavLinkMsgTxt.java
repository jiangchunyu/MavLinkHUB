package com.paku.mavlinkhub.queue.items;

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;
import com.MAVLink.Messages.ardupilotmega.msg_statustext;
import com.paku.mavlinkhub.mavlink.MavLinkClassExtractor;

public class ItemMavLinkMsgTxt {

	private String msgName;
	private String mainTxt;
	private String desc_1;
	private String desc_2;
	private String desc_3;
	private String desc_4;
	private String desc_5;

	public ItemMavLinkMsgTxt(ItemMavLinkMsg msgItem, MavLinkClassExtractor mavClasses) {
		setMe(msgItem, mavClasses);
	}

	private void setMe(ItemMavLinkMsg msgItem, MavLinkClassExtractor mavClasses) {

		switch (msgItem.getMsg().msgid) {
		case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
			final msg_heartbeat msg_heartbeat_ = (msg_heartbeat) msgItem.getMsg();
			// setMsgName(msg_heartbeat_.toString().substring(0,msg_heartbeat_.toString().indexOf("-",
			// 0)-1));
			setMsgName(msg_heartbeat_.getClass().getSimpleName());

			// operation mode + state
			final ApmModes mode;
			mode = ApmModes.getMode(msg_heartbeat_.custom_mode, msg_heartbeat_.type);
			setMainTxt(mode.name() + " " + mavClasses.getMavState().get(msg_heartbeat_.system_status).getName());

			// ship's type name + autopilot name
			setDesc_3(mavClasses.getMavType().get(msg_heartbeat_.type).getName() + ":"
					+ mavClasses.getMavAutopilot().get(msg_heartbeat_.autopilot).getName());

			break;

		case msg_statustext.MAVLINK_MSG_ID_STATUSTEXT:

			final msg_statustext msg_statustext_ = (msg_statustext) msgItem.getMsg();
			// setMsgName(msg_statustext_.toString().substring(0,msg_statustext_.toString().indexOf("-",
			// 0)-1));
			setMsgName(msg_statustext_.getClass().getSimpleName());

			setMainTxt(String.valueOf(msg_statustext_.getText()));
			setDesc_3("Severity:" + String.valueOf(msg_statustext_.severity));
			break;

		default:
			break;
		}

		// MavLink package sender and sequence number
		setDesc_1("[" + String.valueOf(msgItem.getSysId()) + "]");
		setDesc_2("[" + String.valueOf(msgItem.getSeqNo()) + "]");

	}

	public String getName() {
		return msgName;
	}

	public void setMsgName(String name) {
		this.msgName = name;
	}

	public String getMainTxt() {
		return mainTxt;
	}

	public void setMainTxt(String mainTxt) {
		this.mainTxt = mainTxt;
	}

	public String getDesc_1() {
		return desc_1;
	}

	public void setDesc_1(String desc_1) {
		this.desc_1 = desc_1;
	}

	public String getDesc_3() {
		return desc_3;
	}

	public void setDesc_3(String desc_3) {
		this.desc_3 = desc_3;
	}

	public String getDesc_2() {
		return desc_2;
	}

	public void setDesc_2(String desc_2) {
		this.desc_2 = desc_2;
	}

	public String getDesc_4() {
		return desc_4;
	}

	public void setDesc_4(String desc_4) {
		this.desc_4 = desc_4;
	}

	public String getDesc_5() {
		return desc_5;
	}

	public void setDesc_5(String desc_5) {
		this.desc_5 = desc_5;
	}

}