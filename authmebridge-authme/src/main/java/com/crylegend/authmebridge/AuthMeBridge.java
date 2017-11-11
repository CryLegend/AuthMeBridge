package com.crylegend.authmebridge;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

public class AuthMeBridge extends JavaPlugin implements PluginMessageListener {
	Logger log = Logger.getLogger("Minecraft");
	String prefix = "[AuthMeBridge] ";
	String incomingChannel = "BAuthMeBridge";
	String outgoingChannel = "AuthMeBridge";
	String autoLoginMessage = ChatColor.GREEN + "Your session has been resumed by the bridge.";

	public void onEnable() {
		if (!getServer().getPluginManager().isPluginEnabled("AuthMe")) {
			log.info(prefix + "AuthMe not found, disabling");
			getServer().getPluginManager().disablePlugin(this);
		}
		
		getServer().getPluginManager().registerEvents(new AuthMeBridgeListener(this), this);
		getServer().getMessenger().registerIncomingPluginChannel(this, incomingChannel, this);
		getServer().getMessenger().registerOutgoingPluginChannel(this, outgoingChannel);
		
		autoLoginMessage = getConfig().getString("autoLoginMessage", autoLoginMessage);
		getConfig().set("autoLoginMessage", autoLoginMessage);
		
		saveConfig();
	}

	public void onDisable() {
		log.info(prefix + "Goodbye world");
	}

	@Override
	public void onPluginMessageReceived(String channel, Player p, byte[] message) {
		if (channel.equals(incomingChannel)) {
			ByteArrayDataInput in = ByteStreams.newDataInput(message);
			String subchannel = in.readUTF();

			if (subchannel.equals("AutoLogin")) {
				Player player = Bukkit.getPlayer(in.readUTF());

				if (player != null) {
					if (!fr.xephi.authme.api.NewAPI.getInstance().isAuthenticated(player)) {
						fr.xephi.authme.api.NewAPI.getInstance().forceLogin(player);
						if (!autoLoginMessage.isEmpty())
							player.sendMessage(autoLoginMessage);
					}
				}
			}
		}
	}
}
