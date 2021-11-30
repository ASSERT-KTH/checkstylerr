package com.gaoyiping.demo;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioSession;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class DemoService implements MessageProcessor<byte[]>, Runnable {
	private HashMap<String, AioSession<byte[]>> clients = new HashMap<String, AioSession<byte[]>>();
	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(12);
	
	public DemoService() {
		executorService.scheduleAtFixedRate(this, 2, 2, TimeUnit.SECONDS);
	}
	
	public void run() {
		// send data every 2 second...
		if (this.clients.isEmpty()) return;
		for (AioSession<byte[]> session: this.clients.values()) {
			try {
				session.write("Hey! Smart-Socket it's work...".getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void process(AioSession<byte[]> session, byte[] msg) {
		JSONObject jsonObject = JSON.parseObject(msg, JSONObject.class);
		System.out.println(jsonObject.getString("content"));
		// SomeCode...
		try {
			// Response
			session.write("{\"result\": \"OK\"}".getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stateEvent(AioSession<byte[]> session, StateMachineEnum stateMachineEnum, Throwable throwable) {
		// when connection state changed.
		switch (stateMachineEnum) {
			case NEW_SESSION:
				System.out.println("StateMachineEnum.NEW_SESSION");
				break;
			case INPUT_SHUTDOWN:
				System.out.println("StateMachineEnum.INPUT_SHUTDOWN");
				break;
			case PROCESS_EXCEPTION:
				System.out.println("StateMachineEnum.PROCESS_EXCEPTION");
				break;
			case DECODE_EXCEPTION:
				System.out.println("StateMachineEnum.DECODE_EXCEPTION");
				break;
			case INPUT_EXCEPTION:
				System.out.println("StateMachineEnum.INPUT_EXCEPTION");
				break;
			case OUTPUT_EXCEPTION:
				System.out.println("StateMachineEnum.OUTPUT_EXCEPTION");
				break;
			case SESSION_CLOSING:
				System.out.println("StateMachineEnum.SESSION_CLOSING");
				break;
			case SESSION_CLOSED:
				System.out.println("StateMachineEnum.SESSION_CLOSED");
				break;
			case FLOW_LIMIT:
				System.out.println("StateMachineEnum.FLOW_LIMIT");
				break;
			case RELEASE_FLOW_LIMIT:
				System.out.println("StateMachineEnum.RELEASE_FLOW_LIMIT");
				break;
			default:
				System.out.println("StateMachineEnum.default");
		}
	}

}
