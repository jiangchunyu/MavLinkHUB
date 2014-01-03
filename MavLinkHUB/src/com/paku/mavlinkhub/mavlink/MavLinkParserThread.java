package com.paku.mavlinkhub.mavlink;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import android.util.Log;

import com.MAVLink.Parser;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.paku.mavlinkhub.SysStatsHolder;

public class MavLinkParserThread extends Thread {

	private static final String TAG = "MavLinkParserThread";

	private Parser parser;

	private byte[] buffer;
	private int bufferLen;

	// data read stream

	private ByteArrayOutputStream mByteDataStream;
	private ByteArrayOutputStream mByteLoggingOutputStream;
	private ObjectOutputStream mMsgsLoggingOutputStream;
	private boolean running = true;
	private MAVLinkPacket lastMavLinkPacket = null;
	private SysStatsHolder mSysStatsHolder;
	private BufferedOutputStream mFileByteLogStream;

	public MavLinkParserThread(ByteArrayOutputStream mDataStream,ByteArrayOutputStream mByteOutputStream,
			ObjectOutputStream mMsgOutputStream,SysStatsHolder mStatsHolder,File logFile) {

		parser = new Parser();

		// read this
		mByteDataStream = mDataStream;

		//write bytes here - system logging stream
		mByteLoggingOutputStream = mByteOutputStream;
		

		// write ML messages here
		mMsgsLoggingOutputStream = mMsgOutputStream;
		
		mSysStatsHolder = mStatsHolder;
		
		running = true;
		
		
		//file logging
		try {
			mFileByteLogStream  = new BufferedOutputStream(new FileOutputStream(logFile, true));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

	public void run() {
				

		while (running) {

			if (mByteDataStream.size() > 0) {
				lastMavLinkPacket = null;
				
				buffer = mByteDataStream.toByteArray();
				bufferLen = mByteDataStream.size();

				//flush input stream
				mByteDataStream.reset();				
				
				//store bytes stream
				
				mByteLoggingOutputStream.write(buffer, 0, bufferLen);
				
				try {
					mFileByteLogStream.write(buffer, 0, bufferLen);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					Log.d(TAG, e1.getMessage());
					e1.printStackTrace();
				}
				
				
				
				mSysStatsHolder.statsByteCount+=bufferLen;


				Log.d(TAG, "ML Parser got bytes: " + bufferLen);

				for (int i = 0; i < bufferLen; i++) {

					lastMavLinkPacket = parser
							.mavlink_parse_char(buffer[i] & 0x00ff);
					if (lastMavLinkPacket != null) {
						Log.d(TAG, "Pkg: " + lastMavLinkPacket.seq + " "
								+ lastMavLinkPacket.msgid);
						MAVLinkMessage lastMavLinkMsg = lastMavLinkPacket
								.unpack();
						Log.d(TAG, "Msg: " + lastMavLinkMsg.toString());

						// fill msgs stream with new arrival
						try {
							mMsgsLoggingOutputStream
									.writeObject(lastMavLinkMsg);
						} catch (IOException e) {
							Log.d(TAG, "MsgStream write: " + e.getMessage());
							// e.printStackTrace();
						}

					}
				}

				bufferLen = 0;
			}
		}
		
	
	}

	public void stopMe(boolean doStop) {
		running = doStop;
		
		try {
			mFileByteLogStream.flush(); //working ??
			mFileByteLogStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
