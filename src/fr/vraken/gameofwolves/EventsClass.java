package fr.vraken.gameofwolves;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;


public class EventsClass implements Listener
{
  static GameOfWolves plugin;
  static boolean rushIsStart = false;
  static boolean countdownIsStart = false;
  static ArrayList<UUID> alive = new ArrayList<UUID>();
  public static boolean pvp = false;
  ItemStack playerSkull = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
  File file = null;
  BufferedWriter writer = null;
  
  
  public EventsClass(GameOfWolves gameofwolves)
  {
    plugin = gameofwolves;	    
    file = new File(plugin.getDataFolder(), "death_log.txt");
    if(!file.exists())
    {
        try 
        {
            file.createNewFile();
        } 
        catch (Exception ex) {}
    }
    
    try 
    {
		writer = new BufferedWriter(new FileWriter(file));
	} 
    catch (Exception e) {}
  }
  
  public void ClearDrops(String world)
  {
    World w = Bukkit.getServer().getWorld(world);
    if (w == null) {
      return;
    }
    for (Entity e : w.getEntities()) {
      if (e.getType() == EntityType.DROPPED_ITEM) {
        e.remove();
      }
    }
  }
  
  static void xpLevel(int level)
  {
	  for(Team teams : plugin.s.getTeams())
	  {
		  for (OfflinePlayer p : teams.getPlayers()) 
		  {
		      p.getPlayer().setLevel(level);
		  } 
	  }
  }
  
  static void witherSound()
  {
	  for(Team teams : plugin.s.getTeams())
	  {
		  for (OfflinePlayer p : teams.getPlayers()) 
		  {
		      p.getPlayer().playSound(p.getPlayer().getLocation(), Sound.WITHER_DEATH, 10.0F, 10.0F);
		  } 
	  } 
  }
  
  @EventHandler
  public void Regen(EntityRegainHealthEvent e)
  {
    if (e.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED)) {
      e.setCancelled(true);
    }
  }
  
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent e)
  {
    Player p = e.getPlayer();
    
    if (!rushIsStart)
    {
      plugin.getServer().createWorld(
        new WorldCreator(plugin.getConfig().get("lobby.world")
        .toString()));
      p.teleport(new Location(Bukkit.getWorld(plugin.getConfig()
        .get("lobby.world").toString()), plugin.getConfig().getInt(
        "lobby.X"), plugin.getConfig().getInt("lobby.Y"), plugin
        .getConfig().getInt("lobby.Z")));
      p.setGameMode(GameMode.ADVENTURE);
      
      e.setJoinMessage(ChatColor.BLUE + p.getName() + ChatColor.YELLOW + 
        " a rejoint la partie  " + ChatColor.GRAY + "(" + 
        ChatColor.YELLOW + Bukkit.getOnlinePlayers().size() + "/" + 
        Bukkit.getMaxPlayers() + ChatColor.GRAY + ")");
    }
    else if (!alive.contains(p.getUniqueId()))
    {
      e.setJoinMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + 
        p.getName() + " a rejoint la partie  ");
      p.setGameMode(GameMode.SPECTATOR);
      p.teleport(new Location(Bukkit.getWorld(EventsClass.plugin.getConfig().get("lobby.world").toString()), EventsClass.plugin.getConfig().getInt("lobby.X"), EventsClass.plugin.getConfig().getInt("lobby.Y"), EventsClass.plugin.getConfig().getInt("lobby.Z")));p.setGameMode(GameMode.SPECTATOR);
      
     Title.sendTitle(p, "Pensez à vous mute sur Mumble !", "Par fairplay, assurez-vous que les joueurs en vie ne peuvent pas vous entendre !");
    }
  }
  
  @EventHandler
  public void OnPlayerRespawn(PlayerRespawnEvent e)
  {
	  Player p = e.getPlayer();
	  Title.sendTitle(p, "Pensez à vous mute sur Mumble !", "Par fairplay, assurez-vous que les joueurs en vie ne peuvent pas vous entendre !");

  }
  
  @EventHandler
  public void PlayerImmunity(EntityDamageEvent e)
  {
	  if(plugin.rolessetup)
	  {
		  return;
	  }
	  
	  try
	  {
		  Player player = (Player)e.getEntity();
		  boolean lethal = (player.getHealth() - e.getDamage()) < 1;
		  if(lethal)
		  {
			  player.setHealth(1.0);
		  }
	  }
	  catch(Exception ex){}
  }
  
  @EventHandler
  public void AncientDeath(EntityDamageByEntityEvent e)
  {	
	  try
	  {
		  Player player = (Player)e.getEntity();
		  
	  	  if(!plugin.ancientDead && player.getUniqueId() == plugin.ancient)
	  	  {		  	  
			  boolean lethal = (player.getHealth() - e.getDamage()) < 0;
			  if(lethal)
			  {
				  Entity killer = e.getEntity();
				  Player kill = null;
				  if ((killer instanceof Arrow))
		          {
		            Arrow arr = (Arrow)killer;
		            kill = (Player)arr.getShooter();
		          }
		          else
		          {
		        	  kill = (Player)killer;
		          }
				  
				  if(plugin.s.getPlayerTeam((OfflinePlayer)kill) == plugin.assassin || 
					plugin.s.getPlayerTeam((OfflinePlayer)kill) == plugin.wolves ||
					plugin.s.getPlayerTeam((OfflinePlayer)kill) == plugin.whiteWolf)
				  {					  
					  plugin.ancientDead = true;
					  player.setHealth(10.0);
					  
					  player.teleport(new Location(plugin.world, 0, 250, 0));

					  int x = 0;
					  int z = 0;
					  int minDistance = 100;
					  int maxRange = plugin.tmpBorder;
					  boolean respectTeams = false;
					  String players = player.getName();
					  
					  ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
					  Bukkit.getServer().dispatchCommand(console, String.format("spreadplayers %d %d %d %d %b %s", x, z, minDistance, maxRange, respectTeams, players));
				  }
				  else
				  {
					  kill.setHealth(kill.getHealth() / 2);
					  plugin.ancientDead = true;
				  }
			  }
	  	  } 
	  }
	  catch(Exception ex){}
  }
  
  @EventHandler
  public void PlayerDeath(PlayerDeathEvent e)
  {
    Player player = e.getEntity();
    
    if(plugin.playersAlive.contains(player.getUniqueId()))
    {    	
    	
    	//WRITE DEATH MESSAGE IN FILE BEFORE CHANGING IT
    	//----------------------------------------------
	    try 
        {
		    writer.write(e.getDeathMessage());
        } 
        catch (Exception ex) {}
	    	    
	    
	    //ADD DROPS
	    //---------
	    e.getDrops().add(new ItemStack(Material.SKULL_ITEM));
	    e.getDrops().add(new ItemStack(Material.GOLDEN_APPLE));	    
	    
	    
	    //SEND MODIFIED DEATH MESSAGE
	    //---------------------------
	    e.setDeathMessage(e.getEntity().getName() + " est mort ! "
	    		+ "Il était " + plugin.rolesName.get(plugin.playerRoles.get(player.getUniqueId())));
	    
	    
	    //SETTING LAST DEAD ID
	    //--------------------
	    plugin.lastDead = player.getUniqueId();
	    
	    
	    //PLAY SOUND FOR EVERY OTHER PLAYER
	    //---------------------------------
	    for (Player pl : Bukkit.getOnlinePlayers()) 
	    {
	      pl.playSound(pl.getLocation(), Sound.WITHER_DEATH, 10.0F, 10.0F);
	    }

	    
	    try
		{		    
		    //GET KILLER IF PLAYER
		    //--------------------
			Entity killer = e.getEntity();
			Player kill = null;		
			if (killer instanceof Arrow)
	        {
	          Arrow arr = (Arrow)killer;
	          kill = (Player)arr.getShooter();
	        }
	        else if(killer instanceof Player)
	        {
	      	  kill = (Player)killer;
	        }
			
			
			//GIVE HEALTH BOOST IF VILLAGER KILLED BY WOLF
			//--------------------------------------------
			if(plugin.playerRoles.get(kill.getUniqueId()) == 1 && !(plugin.playerRoles.get(player.getUniqueId()) == 1 || plugin.playerRoles.get(player.getUniqueId()) == 6))
			{
			  if(plugin.wolvesKill.containsKey(kill.getUniqueId()))
			  {
				  int ampli = plugin.wolvesKill.get(kill.getUniqueId()) + 1;
				  plugin.wolvesKill.replace(kill.getUniqueId(), ampli);
				  PotionEffect health = new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, ampli - 1, false, false);
				  kill.addPotionEffect(health);
			  }
			  else
			  {
				  plugin.wolvesKill.put(kill.getUniqueId(), 1);
				  PotionEffect health = new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 0, false, false);
				  kill.addPotionEffect(health);
			  }
			}
	    }
	    catch(Exception ex) {}
	    
		
		//KILL BOTH LOVERS IF ONE OF THEM IS KILLED
		//-----------------------------------------
	    if(player.getUniqueId() == plugin.lover1 && plugin.loversAlive)
	    {
	    	plugin.loversAlive = false;
	    	Bukkit.getPlayer(plugin.lover2).setHealth(0);
	    	Bukkit.broadcastMessage("L'Amour n'est malheureusement pas toujours vainqueur...");
	    }
	    else if(player.getUniqueId() == plugin.lover2 && plugin.loversAlive)
	    {
	    	plugin.loversAlive = false;
	    	Bukkit.getPlayer(plugin.lover1).setHealth(0);
	    	Bukkit.broadcastMessage("L'Amour n'est malheureusement pas toujours vainqueur...");
	    }
	    
	    
	    //UNREGISTER WITCH FROM OTHER CLASS
	    //---------------------------------
	    if(player.getUniqueId() == plugin.witch)
	    {
	    	plugin.witchAlive = false;
	    }
	    
	    
	    //IF HUNTER ALLOW TO SHOOT SOMEONE
	    //--------------------------------
	    if(player.getUniqueId() == plugin.hunter)
	    {
	    	plugin.hunterCanShoot = true;
		    new BukkitRunnable()
	        {
	          public void run()
	          {
	  	    	plugin.hunterCanShoot = false;
	          }
	        }.runTaskLater(plugin, 20 * 30);
	    }
	    
	    
	    //IF IDOL, WILD CHILD GOES WOLF
	    //-----------------------------
	    if(player.getUniqueId() == plugin.idol && plugin.wildAlive)
	    {
	    	plugin.playerRoles.replace(plugin.wild, 1);
	    	plugin.wolves.addPlayer(Bukkit.getOfflinePlayer(plugin.wild));
			player.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
	    }
	    
	    
	    
	    if(plugin.witchAlive && !plugin.witchRevived)
	    {
		    plugin.witchCanRevive = true;
		    
	    	new BukkitRunnable()
	        {
	          public void run()
	          {
	        	  
	        	plugin.witchCanRevive = false;
	        	
	        	if(!plugin.witchRevived)
	        	{

		  			//UNREGISTER ROLES FROM OTHER CLASS
		  			//---------------------------------	
		  		    if(player.getUniqueId() == plugin.wild)
		  		    {
		  		    	plugin.wildAlive = false;
		  		    }
		  		    else if(player.getUniqueId() == plugin.girl)
		  		    {
		  		    	plugin.girlAlive = false;
		  		    }
				    else if(player.getUniqueId() == plugin.chaman)
				    {
				    	plugin.chamanAlive = false;
				    }
		  		    
		  			//UNREGISTER PLAYER FROM ITS TEAM
		  			//-------------------------------	    
		  			Team team = player.getScoreboard().getPlayerTeam(player);
		  			team.removePlayer(player);
		  			alive.remove(player.getUniqueId());
		  			plugin.playersAlive.remove(player.getUniqueId());
	        	}

	          }
	        }.runTaskLater(plugin, 20 * 60);
	    }
	    else
	    {

			//UNREGISTER ROLES FROM OTHER CLASS
			//---------------------------------	
		    if(player.getUniqueId() == plugin.wild)
		    {
		    	plugin.wildAlive = false;
		    }
		    else if(player.getUniqueId() == plugin.girl)
		    {
		    	plugin.girlAlive = false;
		    }
		    else if(player.getUniqueId() == plugin.chaman)
		    {
		    	plugin.chamanAlive = false;
		    }
		    
			//UNREGISTER PLAYER FROM ITS TEAM
			//-------------------------------	    
			Team team = player.getScoreboard().getPlayerTeam(player);
			team.removePlayer(player);
			alive.remove(player.getUniqueId());  
			plugin.playersAlive.remove(player.getUniqueId());
	    }
	    
	    
	    //TELEPORT DEAD PLAYER TO CHAMAN
	    //------------------------------
	    if(plugin.chamanAlive && player.getUniqueId() != plugin.chaman)
	    {

    	    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
    	    double x = Bukkit.getPlayer(plugin.chaman).getLocation().getX();
    	    double y = Bukkit.getPlayer(plugin.chaman).getLocation().getY();
    	    double z = Bukkit.getPlayer(plugin.chaman).getLocation().getZ();
    	    player.setGameMode(GameMode.SPECTATOR);
    		Bukkit.getServer().dispatchCommand(console, String.format("tp %s %d %d %d", player.getName(), x, y, z));

		    new BukkitRunnable()
		    {
		    	public void run()
		    	{
		    		if(!plugin.witchRevived)
		    		{
			    		Bukkit.getServer().dispatchCommand(console, String.format("tp %s %d %d %d", player.getName(), 0, 250, 0));
		    		}
		    	}
		    }.runTaskLater(plugin, 20  * 15);
	    }
     }
  }
  
  @EventHandler
  public void RespawTp(PlayerRespawnEvent e)
  {
    final Player p = e.getPlayer();
    new BukkitRunnable()
    {
      public void run()
      {
        p.teleport(new Location(Bukkit.getWorld(EventsClass.plugin.getConfig().get("lobby.world").toString()), EventsClass.plugin.getConfig().getInt("lobby.X"), EventsClass.plugin.getConfig().getInt("lobby.Y"), EventsClass.plugin.getConfig().getInt("lobby.Z")));p.setGameMode(GameMode.SPECTATOR);
      }
    }.runTaskLater(plugin, 4L);
  }
  
  @EventHandler
  public void CancelDrop(PlayerDropItemEvent e)
  {
    Player p = e.getPlayer();
    if (p.getWorld().equals(Bukkit.getWorld(plugin.getConfig().get("lobby.world").toString()))) {
      e.setCancelled(true);
    }
  }
  
  @EventHandler
  public void CancelPVP(EntityDamageEvent e)
  {
    if (e.getEntity().getWorld().equals(Bukkit.getWorld(plugin.getConfig().get("lobby.world").toString()))) 
    {
      e.setCancelled(true);
    }
  }
  
  @EventHandler
  public void CancelPVp2(EntityDamageByEntityEvent e)
  {
    if ((!pvp) && ((e.getDamager() instanceof Player)) && 
      ((e.getEntity() instanceof Player))) {
      e.setCancelled(true);
    }
  }
  
}
