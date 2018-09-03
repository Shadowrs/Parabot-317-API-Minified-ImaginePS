package org.rev317.min.callback;

import org.parabot.core.Context;
import org.parabot.core.Core;
import org.parabot.core.reflect.RefClass;
import org.rev317.min.api.events.MessageEvent;
import org.rev317.min.script.ScriptEngine;

public class MessageCallback {

	public static void messageListenerImaginePS(Object chatMessage) {
		try {
			RefClass refClass = new RefClass(Context.getInstance().getASMClassLoader().loadClass("com.imagineps.client.ChatMessage"), chatMessage);
			RefClass chatType = new RefClass(Context.getInstance().getASMClassLoader().loadClass("com.imagineps.client.ChatMessage$ChatType"), refClass.getField("type").asObject());
			String name = refClass.getField("name").asString();
			String title = refClass.getField("title").asString();
			String message = refClass.getField("message").asString();
			int type = chatType.getField("type", "I").asInt();
			Core.verbose("[message, type " + type + "]: " + title + " " + name + ": " + message);

			final MessageEvent messageEvent = new MessageEvent(type, name, message);
			ScriptEngine.getInstance().dispatch(messageEvent);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
