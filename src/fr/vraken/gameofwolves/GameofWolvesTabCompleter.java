package fr.vraken.gameofwolves;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class GameofWolvesTabCompleter implements TabCompleter
{
	static GameOfWolves plugin;
	

	public GameofWolvesTabCompleter(GameOfWolves gameofwolves)
	{
		plugin = gameofwolves;
	}
	  
	  
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("gwvote") 
				|| cmd.getName().equalsIgnoreCase("gwshoot") 
				|| cmd.getName().equalsIgnoreCase("gwprotect") 
				|| cmd.getName().equalsIgnoreCase("gwdamage"))
		{
			if(sender instanceof Player)
			{
				List<String> list = new ArrayList<String>();
				for(Player p : Bukkit.getOnlinePlayers())
				{
					list.add(p.getName());
				}
				
				return list;
			}
		}
		
		return null;
	}
}
