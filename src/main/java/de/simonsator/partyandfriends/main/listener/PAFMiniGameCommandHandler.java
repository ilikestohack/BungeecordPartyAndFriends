package de.simonsator.partyandfriends.main.listener;

import de.simonsator.partyandfriends.api.adapter.BukkitBungeeAdapter;
import de.simonsator.partyandfriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.partyandfriends.api.pafplayers.PAFPlayerManager;
import de.simonsator.partyandfriends.api.party.PlayerParty;
import de.simonsator.partyandfriends.api.system.WaitForTasksToFinish;
import de.simonsator.partyandfriends.main.Main;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import net.ME1312.SubServers.Sync.SubAPI;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class PAFMiniGameCommandHandler implements Listener {
	private final Set<String> COMMANDS = new HashSet<>();
	private final WaitForTasksToFinish TASK_COUNTER = new WaitForTasksToFinish();

	public PAFMiniGameCommandHandler(List<String> pCommands) {
		for (String command : pCommands)
			COMMANDS.add(command.toLowerCase(Locale.ROOT));
	}

	private void runCommandOnPlayers(ChatEvent pEvent){
		if (pEvent.getSender() instanceof ProxiedPlayer) {
			OnlinePAFPlayer player =
					PAFPlayerManager.getInstance().getPlayer((ProxiedPlayer) pEvent.getSender());
			PlayerParty party = player.getParty();
			if (party != null && party.isLeader(player)) {
				ServerInfo leaderServer = player.getServer();
				SubAPI.getInstance().getRemotePlayer(player.getPlayer().getUniqueId()).getServer(server -> {
					for (OnlinePAFPlayer member : party.getPlayers()) {
						if (leaderServer.equals(member.getServer())) {
							if (member.getPlayer().getPendingConnection().getVersion() >= 759) {
								server.command(player.getPlayer().getUniqueId(), member.getPlayer().getUniqueId(), pEvent.getMessage().substring(1));
							} else {
								member.getPlayer().chat(pEvent.getMessage());
							}
						}
					}
				});
			}
		}
	}

	@EventHandler
	public void onChat(ChatEvent pEvent) {
		BukkitBungeeAdapter.getInstance().runAsync(Main.getInstance(), () -> {
			try {
				TASK_COUNTER.taskStarts();
				String commandRan = pEvent.getMessage().toLowerCase(Locale.ROOT);
				AtomicReference<Boolean> commandEqual = new AtomicReference<>(true);
				for (String COMMAND : COMMANDS) {
					commandEqual.set(true);
					String[] commandRanArray = commandRan.split("\\s+");
					for (int i = 0; i < commandRanArray.length; i++) {
						String s = commandRanArray[i];
						if (!s.contains("$arg$")) {
							String[] commandSplit = COMMAND.split("\\s+");
							if (commandSplit.length > i && !s.equals(commandSplit[i])) {
								commandEqual.set(false);
							}
						}
					}
					if(commandEqual.get()){
						runCommandOnPlayers(pEvent);
						break;
					}
				}
			} finally {
				TASK_COUNTER.taskFinished();
			}
		});
	}
}
