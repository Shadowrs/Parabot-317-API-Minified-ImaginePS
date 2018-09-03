package org.rev317.min;

import java.net.InetAddress;
import org.parabot.core.Context;
import org.parabot.core.Core;
import org.parabot.core.Directories;
import org.parabot.core.asm.ASMClassLoader;
import org.parabot.core.asm.adapters.AddInterfaceAdapter;
import org.parabot.core.asm.hooks.HookFile;
import org.parabot.core.desc.ServerProviderInfo;
import org.parabot.core.reflect.RefClass;
import org.parabot.core.ui.components.VerboseLoader;
import org.parabot.environment.api.utils.WebUtil;
import org.parabot.environment.scripts.Script;
import org.parabot.environment.servers.ServerManifest;
import org.parabot.environment.servers.ServerProvider;
import org.parabot.environment.servers.Type;
import org.rev317.min.accessors.Client;
import org.rev317.min.script.ScriptEngine;
import org.rev317.min.ui.BotMenu;

import javax.swing.*;
import java.applet.Applet;
import java.io.File;
import java.net.URL;

/**
 * @author Everel, JKetelaar
 */
@ServerManifest(author = "Everel & JKetelaar", name = "Server name here", type = Type.INJECTION, version = 2.1)
public class Loader extends ServerProvider {
    private boolean extended = true;

    /**
     * Static accessor for Client. Used by Randoms module.
     * @return
     */
    public static Client getClient() {
        return (Client)Context.getInstance().getClient();
    }

    /**
     * Called once, fetches a new Applet instance which is the entry point to run the RSPS client.
     * @return
     */
    @Override
    public Applet fetchApplet() {
        try {
            final Context        context     = Context.getInstance();
            final ASMClassLoader classLoader = context.getASMClassLoader();

            final RefClass config = new RefClass(classLoader.loadClass("com/imagineps/client/Configuration"));
            config.getField("LOADING_MESSAGE", "Ljava/lang/String;").setString("Parabot is loading...");

            final RefClass client = new RefClass(classLoader.loadClass(Context.getInstance().getServerProviderInfo().getClientClass()));
            client.getField("nodeID", "I").setInt(10);
            client.getField("portOff", "I").setInt(0);
            client.getMethod("setHighMem").invoke();
            client.getField("isMembers", "Z").setBoolean(true);
            final RefClass sign = new RefClass(classLoader.loadClass("com/imagineps/client/signlink/Signlink"));
            sign.getMethod("startpriv", new Class[] {InetAddress.class}).invoke((InetAddress)InetAddress.getLocalHost());

            final RefClass clientInstance = client.newInstance();
            client.getField("instance", "Lcom/imagineps/client/Client;").set(clientInstance.getInstance());
            // init is done by Context, not here.

            return (Applet) clientInstance.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public URL getJar() {
        ServerProviderInfo serverProvider = Context.getInstance().getServerProviderInfo();

        File target = new File(Directories.getCachePath(), serverProvider.getClientCRC32() + ".jar");
        if (!target.exists()) {
            WebUtil.downloadFile(serverProvider.getClient(), target, VerboseLoader.get());
        }

        return WebUtil.toURL(target);
    }

    @Override
    public void addMenuItems(JMenuBar bar) {
        new BotMenu(bar);
    }

    @Override
    public void injectHooks() {
        AddInterfaceAdapter.setAccessorPackage("org/rev317/min/accessors/");
        try {
            super.injectHooks();
        } catch (Exception e) {
            if (Core.inVerboseMode()) {
                e.printStackTrace();
            }
            this.extended = false;
            super.injectHooks();
        }
    }

    @Override
    public void initScript(Script script) {
        ScriptEngine.getInstance().setScript(script);
        ScriptEngine.getInstance().init();
    }

    @Override
    public HookFile getHookFile() {
        if (this.extended) {
            return new HookFile(Context.getInstance().getServerProviderInfo().getExtendedHookFile(), HookFile.TYPE_XML);
        } else {
            return new HookFile(Context.getInstance().getServerProviderInfo().getHookFile(), HookFile.TYPE_XML);
        }
    }

    public void unloadScript(Script script) {
        ScriptEngine.getInstance().unload();
    }

    @Override
    public void init() {
    }
}