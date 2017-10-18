package fr.vraken.gameofwolves;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class GameOfWolves extends JavaPlugin
{
  FilesManager filesManager;
  FileConfiguration rolef;
  GameofWolvesTabCompleter tab;

  ArrayList<UUID> playersAlive = new ArrayList<>();
  
  boolean rolessetup;
  ArrayList<Integer> roleIds = new ArrayList<Integer>();
  HashMap<Integer, Integer> totalRoles = new HashMap<Integer, Integer>();
  HashMap<UUID, Integer> playerRoles = new HashMap<UUID, Integer>();
  HashMap<Integer, String> rolesName = new HashMap<Integer, String>();
  HashMap<UUID, Integer> wolvesKill = new HashMap<UUID, Integer>();

  Team village;
  Team wolves;
  Team whiteWolf;
  Team assassin;
  Team littleGirl;
  
  UUID lover1;
  UUID lover2;
  boolean loversAlive = true;
  UUID wild;
  UUID idol;
  boolean wildAlive = false;
  UUID saved;
  UUID idiot;
  boolean idiotRevealed = false;
  UUID ancient;
  boolean ancientDead = false;
  UUID girl;
  boolean girlAlive = false;
  UUID witch;
  boolean witchAlive = false;
  boolean witchRevived = true;
  boolean witchDamaged = true;
  boolean witchCanRevive = true;
  boolean witchCanDamage = true;
  UUID hunter;
  boolean hunterShot = false;
  boolean hunterCanShoot = false;
  UUID chaman;
  boolean chamanAlive = false;
  UUID psychic;
  boolean psychicCanReveal = false;
  UUID savior;
  UUID lastProtected;
  boolean saviorCanProtect = false;
  
  UUID lastDead;
  boolean voteOpen = false;
  HashMap<UUID, Integer> votes = new HashMap<UUID, Integer>();
  HashMap<UUID, Integer> voted = new HashMap<UUID, Integer>();

  int episode;
  boolean gameStarted = false;
  int gameState;
  boolean hasChangedGS;
  int restractEpisode;
  BukkitTask runnable;
  
  World world;
  
  ScoreboardManager sm;
  Scoreboard s;
  Objective obj;
  String objMinute;
  String objSecond;
  String objTxt;
  String countdownObj;
  int tmpPlayers;
  int tmpBorder;
  NumberFormat objFormatter;
  
  
  public void onEnable()
  {
    System.out.println("+-------------VrakenGameOfWolves--------------+");
    System.out.println("|           Plugin cree par Vraken            |");
    System.out.println("+---------------------------------------------+");
    
    try {
		filesManager = new FilesManager(this);
	} catch (IOException | InvalidConfigurationException e) {
		e.printStackTrace();
	}
    
    this.sm = Bukkit.getScoreboardManager();
    this.s = this.sm.getMainScoreboard();
    Bukkit.getPluginManager().registerEvents(new EventsClass(this), this);
    if (this.s.getObjective("Game Of Wolves") != null) 
    {
      this.s.getObjective("Game Of Wolves").unregister();
    }


    this.rolef = this.filesManager.getRoleConfig();
    getConfig().options().copyDefaults(true);
    this.rolef.options().copyDefaults(true);
    saveConfig();
    
    this.obj = this.s.registerNewObjective("Game Of Wolves", "dummy");
    this.obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    for (Team team : this.s.getTeams()) 
    {
      team.unregister();
    }
    
    
    //ASSIGNING TAB COMPLETER FOR PLAYERS TO AUTOCOMPLETE PLAYERS NAMES
    //-----------------------------------------------------------------
    tab = new GameofWolvesTabCompleter(this);
    getCommand("gwvote").setTabCompleter(tab);   
    getCommand("gwshoot").setTabCompleter(tab);
    getCommand("gwdamage").setTabCompleter(tab);
    getCommand("gwprotect").setTabCompleter(tab);   
    
    
    //CREATING SPECIAL CRAFTS
    //-----------------------
    ShapedRecipe craft = new ShapedRecipe(new ItemStack(Material.SPECKLED_MELON));
    craft.shape(new String[] { "***", "*x*", "***" });
    craft.setIngredient('*', Material.GOLD_INGOT);
    craft.setIngredient('x', Material.MELON);
    Bukkit.addRecipe(craft);

    ShapedRecipe craft2 = new ShapedRecipe(new ItemStack(Material.GOLDEN_APPLE));
    craft2.shape(new String[] { "***", "*x*", "***" });
    craft2.setIngredient('*', Material.GOLD_INGOT);
    craft2.setIngredient('x', Material.SKULL_ITEM);
    Bukkit.addRecipe(craft2);

    
    //CREATING TEAMS
    //--------------
    this.village = this.s.registerNewTeam("village");
    this.wolves = this.s.registerNewTeam("wolves");
    this.whiteWolf = this.s.registerNewTeam("whiteWolf");
    this.assassin = this.s.registerNewTeam("assassin");
    this.littleGirl = this.s.registerNewTeam("littleGirl");
    this.village.setPrefix(ChatColor.WHITE.toString());
    this.wolves.setPrefix(ChatColor.WHITE.toString());
    this.whiteWolf.setPrefix(ChatColor.WHITE.toString());
    this.assassin.setPrefix(ChatColor.WHITE.toString());
    this.littleGirl.setPrefix(ChatColor.WHITE.toString());
    this.village.setSuffix(ChatColor.WHITE.toString());
    this.wolves.setSuffix(ChatColor.WHITE.toString());
    this.whiteWolf.setSuffix(ChatColor.WHITE.toString());
    this.assassin.setSuffix(ChatColor.WHITE.toString());
    this.littleGirl.setSuffix(ChatColor.WHITE.toString());
    this.littleGirl.setNameTagVisibility(NameTagVisibility.NEVER);
    
    
    //CREATING LIST OF ROLES
    //----------------------
    createRoles();
    
    
    //CREATING ROLES NAMES LIST
    //-------------------------
    this.rolesName.put(0, "villageois");
    this.rolesName.put(1, "loup-garou");
    this.rolesName.put(2, "voyante");
    this.rolesName.put(3, "sorcière");
    this.rolesName.put(4, "salvateur");
    this.rolesName.put(5, "petite fille");
    this.rolesName.put(6, "loup blanc");
    this.rolesName.put(7, "chasseur");
    this.rolesName.put(8, "assassin");
    this.rolesName.put(9, "ancien");
    this.rolesName.put(10, "chaman");
    this.rolesName.put(11, "idiot");
    this.rolesName.put(12, "maître-chien");
    this.rolesName.put(13, "enfant sauvage");
    this.rolesName.put(14, "mercenaire");
    
    
    Bukkit.createWorld(new WorldCreator("world"));
    super.onEnable();
  }
  
  public void startgame()
  {
	this.world = Bukkit.getWorld(getConfig().getString("world"));
	this.gameStarted = true;
	this.gameState = 0;
	this.hasChangedGS = false;	  
    EventsClass.rushIsStart = true;
    
    world.setGameRuleValue("doDaylightCycle", "true");
    world.setStorm(false);
    world.setThundering(false);
    world.setTime(24000L);
	
	this.episode += 1;
	
	
	//SCOREBOARD INITIALIZATION
	//-------------------------
	this.objFormatter = new DecimalFormat("00");
	initScoreboard();    
	
    
    //CLEARING INVENTORY AND STATUS OF EVERY PLAYER THEN TELEPORTING THEM TO 0 - 0
    //----------------------------------------------------------------------------
    clearPlayers();
    
    
    //SETTING A ROLE FOR EVERY PLAYER
    //-------------------------------
    setRoles();
    
    
    //SETTING THE LOVERS
    //------------------
    setLovers();
    
    
    //UPDATE COMPASS TARGET
    //---------------------
    UpdateCompassTarget(false);
    
    
    //SPREADING PLAYERS ACCROSS THE MAP WITH THE COMMAND
    spreadPlayers();
    
    
    //RUNNABLE TASKS DURING ALL GAME
    //------------------------------
    this.runnable = new BukkitRunnable()
    {
      int minutes = 20;
      int seconds = 0;      
      
      public void run()
      {

    	//SCOREBOARD RESET AT EVERY SECOND
    	//--------------------------------
        NumberFormat formatter = new DecimalFormat("00");
        String minute = formatter.format(this.minutes);
        String second = formatter.format(this.seconds);  
        GameOfWolves.this.s.resetScores(minute + ":" + second);   
        GameOfWolves.this.s.resetScores(ChatColor.WHITE + "Episode " + GameOfWolves.this.episode);
        GameOfWolves.this.s.resetScores("" + ChatColor.WHITE + GameOfWolves.this.tmpPlayers + ChatColor.GRAY + " joueurs");    
        GameOfWolves.this.s.resetScores(ChatColor.WHITE + "Border : " + tmpBorder + " x " + tmpBorder);      
        GameOfWolves.this.s.resetScores(ChatColor.WHITE + GameOfWolves.this.countdownObj);            
        
        
        //UNREGISTER TEAMS
        //----------------
        unregisterTeam();
        
        
    	//VICTORY IF LAST TEAM STANDING
    	//-----------------------------
        checkVictory();
        
        
        if (this.seconds == 0)
        {          
          
          //EPISODE CHANGE ANNOUNCEMENT AT BEGINNING
          //----------------------------------------
          if (this.minutes == 0)
          {
        	  GameOfWolves.this.episode += 1;
            Bukkit.broadcastMessage(ChatColor.AQUA + 
              "------------- Episode " + GameOfWolves.this.episode + 
              " -------------");
            
            
            if(GameOfWolves.this.episode == 2)
            {
            	rolesAnnouncement();
            	loversAnnouncement();
                UpdateCompassTarget(false);
            }
            
            this.seconds = 59;
            this.minutes = 19;
          }
          else
          {
            this.seconds = 59;
            this.minutes -= 1;
          }          

          if(this.minutes == 10 && GameOfWolves.this.episode >= 2)
          {        	  
    		  littleGirlScout();
          }
          else if(this.minutes == 9 && GameOfWolves.this.episode >= 2)
          {
        	  nightFall();
              UpdateCompassTarget(true);
          }
          else if(this.minutes == 8 && GameOfWolves.this.episode >= 2)
          {
        	  GameOfWolves.this.psychicCanReveal = false;
        	  GameOfWolves.this.saviorCanProtect = false;
          }
          else if(this.minutes == 5 && GameOfWolves.this.episode >= 2)
          {
        	  houndMasterScout();    		  
    		  littleGirlScout();
          }
        }
        else
        {
          if(this.seconds == 30 && this.minutes == 19 && GameOfWolves.this.episode >= 3)
          {
        	  voteResult();
          }
          else if(this.seconds == 30 && this.minutes == 0 && GameOfWolves.this.episode >= 2)
          {
        	  sunRise();
              UpdateCompassTarget(false);
          }
        	
          this.seconds -= 1;
        }    
        
        
        //WRITING SCOREBOARD
    	//------------------
        writeScoreboard(this.minutes, this.seconds);
      }
    }.runTaskTimer(this, 0L, 20L);
    
    getServer().getWorld(getConfig().getString("world"))
    	.getWorldBorder()
    	.setSize(getConfig().getDouble("worldborder.size"));

    
    //PVP ENABLE
    //----------
    new BukkitRunnable()
    {
      public void run()
      {
        EventsClass.pvp = true;
        Bukkit.broadcastMessage(ChatColor.RED + 
          "Le pvp est maintenant actif !");

        
        //Updating scoreboard status
        GameOfWolves.this.s.resetScores(ChatColor.WHITE + GameOfWolves.this.countdownObj);
        GameOfWolves.this.gameState++;
        GameOfWolves.this.objMinute = objFormatter.format(GameOfWolves.this.getConfig().getInt("worldborder.retractafter") - GameOfWolves.this.getConfig().getInt("options.pvptime") - 1);
    	GameOfWolves.this.objSecond = "59";
    	GameOfWolves.this.objTxt = "World border : ";
    	GameOfWolves.this.hasChangedGS = true;        
        GameOfWolves.this.countdownObj = GameOfWolves.this.objTxt + GameOfWolves.this.objMinute + ":" + GameOfWolves.this.objSecond;
      }
    }.runTaskLater(this, 1200 * getConfig().getInt("options.pvptime"));
    
    
    //WORLDBORDER SHRINKING
    //---------------------
    new BukkitRunnable()
    {
      public void run()
      {
   	  
        //UPDATING SCOREBOARD STATUS
        GameOfWolves.this.s.resetScores(ChatColor.WHITE + GameOfWolves.this.countdownObj);
        GameOfWolves.this.gameState++;
      
      }
    }.runTaskLater(this, 1200 * getConfig().getInt("worldborder.retractafter"));

	 
    for (Player online : Bukkit.getOnlinePlayers()) 
    {
      online.setScoreboard(this.s);
    }
  }
  
  
  //PLAYER INGAME COMMANDS
  //----------------------
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
  {
    if (sender instanceof Player)
    {
      Player player = (Player)sender;     
      
      if(this.episode >= 2)
      {
          //INFOS COMMAND TO TELL PLAYERS THEIR COMMANDS AND DETAILS ABOUT THEIR ROLE
          //-------------------------------------------------------------------------
          if(cmd.getName().equalsIgnoreCase("gwinfo"))
          {
        	  UUID uid = player.getUniqueId();
        	  
        	  switch(this.playerRoles.get(uid))
    		  {
    		  case 0:
    			  player.sendMessage("Vous êtes un simple villageois, vous ne disposez d'aucun pouvoir particulier");
    			  break;
    		  case 1:
    			  player.sendMessage("Vous êtes un loup-garou ! Vous êtes plus fort la nuit, profitez-en pour utiliser votre boussole afin de traquer les villageois proches. Vous gagnez 2 coeurs de vie max en plus à chaque fois que vous tuez un villageois !");
    			  break;
    		  case 2:
    			  player.sendMessage("Vous êtes la voyante !");
    			  break;
    		  case 3:
    			  player.sendMessage("Vous êtes la sorcière !");
    			  break;
    		  case 4:
    			  player.sendMessage("Vous êtes le salvateur !");
    			  break;
    		  case 5:
    			  player.sendMessage("Vous êtes la petite fille ! La nuit, vous devenez invisible et vous êtes informé de tous les joueurs se trouvant proches de vous.");
    			  break;
    		  case 6:
    			  player.sendMessage("Vous êtes le loup blanc ! Vous avez plus de vie que les autres loups-garous. Vous êtes plus fort la nuit, profitez-en pour utiliser votre boussole afin de traquer les villageois proches !");
    			  break;
    		  case 7:
    			  player.sendMessage("Vous êtes le chasseur !");
    			  break;
    		  case 8:
    			  player.sendMessage("Vous êtes l'assassin ! Vous avez plus de vie que les autres villageois et vous êtes plus fort la journée");
    			  break;
    		  case 9:
    			  player.sendMessage("Vous êtes l'ancien ! La première fois que vous vous ferez tuer par les loups, vous réapparaîtrez à un endroit aléatoire de la carte au lieu de mourir. Si un villageois vous tue, il perd la moitié de sa vie.");
    			  break;
    		  case 10:
    			  player.sendMessage("Vous êtes le chaman ! Lorsqu'un joueur meurt, vous disposez de 15sec pour communiquer avec l'esprit du défunt.");
    			  break;
    		  case 11:
    			  player.sendMessage("Vous êtes l'idiot du village ! Si le village vote pour vous exécuter, votre rôle est révélé et vous ne perdez pas de vie. Les autres joueurs ne pourront plus voter contre vous.");
    			  break;
    		  case 12:
    			  player.sendMessage("Vous êtes le maître-chien ! Au milieu de la nuit, vous serez informé si des loups-garous se trouvent à proximité.");
    			  break;
    		  case 13:
    			  player.sendMessage("Vous êtes l'enfant sauvage ! Votre destin est lié à celui d'un autre villageois. S'il vient à mourir, vous deviendrez un loup-garou.");
    			  player.sendMessage("Le joueur avec lequel vous êtes lié est : " + Bukkit.getPlayer(idol).getName());
    			  break;
    		  case 14:
    			  player.sendMessage("Vous êtes le mercenaire ! Votre but est d'aider les loups-garous à dévorer les autres villageois.");
    			  break;
    		  }
        	  
        	  if(uid == this.witch)
        	  {
        		  player.sendMessage("/gwrevive : Permet de faire revivre le dernier joueur mort. Utilisable une fois par partie.");
        		  player.sendMessage("/gwdamage \"pseudo\" : Fait perdre la moitié de sa vie à un joueur. Utilisable une fois par partie.");
        	  }
        	  else if(uid == this.psychic)
        	  {
        		  player.sendMessage("/gwreveal \"pseudo\" : Permet de connaître le rôle d'un autre joueur. Le rôle de ce joueur sera écrit dans le chat global, mais pas son pseudo. Utilisable une fois par épisode pendant une minute après la tombée de la nuit.");
        	  }
        	  else if(uid == this.savior)
        	  {
        		  player.sendMessage("/gwprotect \"pseudo\" : Permet de conférer Protection à un autre joueur. Vous ne pouvez pas vous désigner vous-même, ni désigner le même joueur deux fois d'affilée. Utilisable une fois par épisode pendant une minute après la tombée de la nuit.");
        	  }
        	  else if(uid == this.hunter)
        	  {
        		  player.sendMessage("/gwshoot \"pseudo\" : Fait perdre la moitié de sa vie à un joueur. Utilisable une fois par partie pendant 30 secondes à votre mort.");
        	  }
    		  player.sendMessage("/gwvote \"pseudo\" : Permet de voter pour un autre joueur lors du vote se déroulant à chaque épisode au levée du jour. Le joueur ayant accumulé le plus de votes perdra la moitié de sa vie. Le vote n'a aucun effet sur une égalité.");
          }
          
          
          //PLAYERS VOTE
          //------------
          if(cmd.getName().equalsIgnoreCase("gwvote") && this.voteOpen)
          {
        	  this.voted.put(player.getUniqueId(), 1);
        	  String name = args[0];
        	  UUID id = Bukkit.getPlayer(name).getUniqueId();
        	  if(id == this.idiot && this.idiotRevealed)
        	  {
        		  player.sendMessage("Vous ne pouvez pas voter pour l'idiot du village. On ne tape pas sur les handicapés espèce de monstre !");
        		  return false;
        	  }
        	  if(this.votes.containsKey(id))
        	  {
        		  this.votes.replace(id, this.votes.get(id) + 1);
        	  }
        	  else
        	  {
        		  this.votes.put(id, 1);
        	  }
          }
          
          
          //HUNTER SHOOT
          //------------
          if(cmd.getName().equalsIgnoreCase("gwshoot"))
          {
        	  if(player.getUniqueId() == this.hunter && this.hunterCanShoot && !this.hunterShot)
        	  {
            	  String name = args[0];
            	  try
              	  {
    	        	  UUID id = Bukkit.getPlayer(name).getUniqueId();
    	          	  Bukkit.broadcastMessage("Le chasseur, dans son agonie, tire sur  " + name);
              	  	  Bukkit.getPlayer(id).setHealth(Bukkit.getPlayer(id).getHealth() / 2);
    	        	  this.hunterShot = true;
              	  }
              	  catch(Exception ex) {}
        	  }
          }
          
          
          //PSYCHIC SPY
          //-----------
          if(cmd.getName().equalsIgnoreCase("gwreveal"))
          {
        	  if(player.getUniqueId() == this.psychic && this.psychicCanReveal)
        	  {
            	  String name = args[0];
            	  try
              	  {
    	        	  UUID id = Bukkit.getPlayer(name).getUniqueId();
    	          	  Bukkit.broadcastMessage("La voyante a espionné un des villageois qui s'est révélé être " + this.rolesName.get(this.playerRoles.get(id)));
    	        	  this.psychicCanReveal = false;
              	  }
              	  catch(Exception ex) {}
        	  }
          }
          
          
          //WITCH DAMAGE
          //------------
          if(cmd.getName().equalsIgnoreCase("gwdamage"))
          {
        	  if(player.getUniqueId() == this.witch && this.witchCanDamage && !this.witchDamaged)
        	  {
            	  String name = args[0];
            	  try
              	  {
    	        	  UUID id = Bukkit.getPlayer(name).getUniqueId();
    	          	  Bukkit.broadcastMessage("La sorcière a jeté un sort à " + name);
              	  	  Bukkit.getPlayer(id).setHealth(Bukkit.getPlayer(id).getHealth() / 2);
              	  	  this.witchDamaged = true;
              	  }
              	  catch(Exception ex) {}
        	  }
          }
          
          
          //WITCH REVIVE
          //------------
          if(cmd.getName().equalsIgnoreCase("gwrevive"))
          {
        	  if(player.getUniqueId() == this.witch && this.witchCanRevive && !this.witchRevived)
        	  {
            	  try
              	  {
    	          	  Bukkit.broadcastMessage("La sorcière a utilisé ses pouvoirs pour rendre la vie à " + Bukkit.getPlayer(this.lastDead).getName());
              	  	  Bukkit.getPlayer(this.lastDead).setHealth(10);
              	  	  Bukkit.getPlayer(this.lastDead).teleport(player.getLocation());
              	  	  Bukkit.getPlayer(this.lastDead).setGameMode(GameMode.SURVIVAL);
              	  	  this.witchRevived = true;
              	  }
              	  catch(Exception ex) {}
        	  }
          }
          
          
          //SAVIOR PROTECT
          //--------------
          if(cmd.getName().equalsIgnoreCase("gwprotect"))
          {
        	  if(player.getUniqueId() == this.savior && this.saviorCanProtect)
        	  {
            	  String name = args[0];
            	  try
              	  {
    	        	  UUID id = Bukkit.getPlayer(name).getUniqueId();
    	        	  
    	        	  if(id == this.savior)
    	        	  {
    	        		  player.sendMessage("Vous ne pouvez pas vous protéger vous-même ! ");
    	        	  }	        	  
    	        	  else if(id == this.lastProtected)
    	        	  {
    	        		  player.sendMessage("Vous ne pouvez pas vous protéger deux fois d'affilée la même personne ! ");
    	        	  }
    	        	  else
    	        	  {	        		  
    		        	  PotionEffect protect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 60 * 20, 0, false, false);
    		        	  Bukkit.getPlayer(id).addPotionEffect(protect);
    		        	  Bukkit.getPlayer(id).sendMessage("Le salvateur vous a accordé sa protection pour la journée à venir ! ");
    		        	  this.lastProtected = id;
    	          	  	  this.saviorCanProtect = false;
    	        	  }
              	  }
              	  catch(Exception ex) {}
        	  }
          }
      }
      
      
      //ADMIN MANUAL START
      //------------------
      if (cmd.getName().equalsIgnoreCase("start") && player.isOp())
      {
        startgame();
        return true;
      }
      return true;
    }
    return false;
  }

  
  //UTILITY FUNCTIONS
  //-----------------
    
  public void createRoles()
  {
	  totalRoles.put(0, rolef.getInt("villageois"));
	  totalRoles.put(1, rolef.getInt("loup-garou"));
	  totalRoles.put(2, rolef.getInt("voyante"));
	  totalRoles.put(3, rolef.getInt("sorciere"));
	  totalRoles.put(4, rolef.getInt("salvateur"));
	  totalRoles.put(5, rolef.getInt("petite fille"));
	  totalRoles.put(6, rolef.getInt("loup blanc"));
	  totalRoles.put(7, rolef.getInt("chasseur"));
	  totalRoles.put(8, rolef.getInt("assassin"));
	  totalRoles.put(9, rolef.getInt("ancien"));
	  totalRoles.put(10, rolef.getInt("chaman"));
	  totalRoles.put(11, rolef.getInt("idiot"));
	  totalRoles.put(12, rolef.getInt("maitre-chien"));
	  totalRoles.put(13, rolef.getInt("enfant sauvage"));
	  totalRoles.put(14, rolef.getInt("mercenaire"));
	  
	  for(int i = 0; i < 15; i++)
	  {
		  for(int j = 0; j < totalRoles.get(i); j++)
		  {
			  roleIds.add(i);
		  }
	  }
  }
  
  public void clearPlayers()
  {
	  for (Player p : Bukkit.getOnlinePlayers())
	  {
	      p.getInventory().clear();
	      p.getInventory().setHelmet(null);
	      p.getInventory().setChestplate(null);
	      p.getInventory().setLeggings(null);
	      p.getInventory().setBoots(null);
	      p.setExp(0.0f);
	      p.setLevel(0);
	      p.getActivePotionEffects().clear();
	      p.setGameMode(GameMode.SURVIVAL);
	      p.setHealth(20.0D);
	      p.setFoodLevel(40);

	      p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 
	    		  20 * getConfig().getInt("options.nodamagetime"), 4));	      

	      this.playersAlive.add(p.getUniqueId());
	      EventsClass.alive.add(p.getUniqueId());
	      
	      p.teleport(new Location(this.world, 0, 250, 0));
	      p.setGameMode(GameMode.SURVIVAL);
	  }
  }
  
  public void setRoles()
  {
	  Random rdm = new Random();
	  int role;
	  
	  for(UUID uid : playersAlive)
	  {
		  role = rdm.nextInt(this.roleIds.size());
		  this.playerRoles.put(uid, this.roleIds.get(role));
		  
		  if(this.roleIds.get(role) == 13)
		  {			  
			  this.wild = uid;
			  this.wildAlive = true;
			  
			  Random rdm2 = new Random();
			  int idol;
			  do
			  {
				  idol = rdm2.nextInt(this.playersAlive.size());
			  }
			  while(this.playersAlive.get(idol) == uid);		  
			  
			  this.idol = this.playersAlive.get(idol);
		  }
		  else if(this.roleIds.get(role) == 11)
		  {
			  this.idiot = uid;
		  }
		  else if(this.roleIds.get(role) == 9)
		  {
			  this.ancient = uid;
		  }
		  else if(this.roleIds.get(role) == 5)
		  {
			  this.girl = uid;
			  this.girlAlive = true;
		  }
		  else if(this.roleIds.get(role) == 3)
		  {
			  this.witch = uid;
			  this.witchAlive = true;
		  }
		  else if(this.roleIds.get(role) == 10)
		  {
			  this.chaman = uid;
			  this.chamanAlive = true;
		  }
		  else if(this.roleIds.get(role) == 2)
		  {
			  this.psychic = uid;
			  this.psychicCanReveal = false;
		  }
		  else if(this.roleIds.get(role) == 4)
		  {
			  this.savior = uid;
			  this.saviorCanProtect = false;
		  }
		  else if(this.roleIds.get(role) == 7)
		  {
			  this.hunter = uid;
		  }
		  
		  this.roleIds.remove(role);
	  }
  }
  
  public void setLovers()
  {
	  Random rdm = new Random();
	  int lov1, lov2;
	  
	  do
	  {
		  lov1 = rdm.nextInt(this.playersAlive.size());
		  lov2 = rdm.nextInt(this.playersAlive.size());
	  }
	  while(lov1 == lov2);
	  
	  this.lover1 = this.playersAlive.get(lov1);
	  this.lover2 = this.playersAlive.get(lov2);
  }
  
  public void spreadPlayers()
  {
	  int x = 0;
	  int z = 0;
	  int minDistance = getConfig().getInt("options.minspawndistance");
	  int maxRange = (int)(getConfig().getDouble("worldborder.size") / 2);
	  boolean respectTeams = false;
	  String players = "@a";

	  ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
	  Bukkit.getServer().dispatchCommand(console, String.format("spreadplayers %d %d %d %d %b %s", x, z, minDistance, maxRange, respectTeams, players));
  }
  
  public void unregisterTeam()
  {
	  if(this.episode >= 2)
	  {
		  try
		  {
			  if(this.wolves.getPlayers().size() == 0 && !this.wildAlive)
			  {
				  this.wolves.unregister();
			  }
		  }
		  catch(Exception ex) {}
		  
		  try
		  {
			  if(this.whiteWolf.getPlayers().size() == 0)
			  {
				  this.whiteWolf.unregister();
			  }
		  }
		  catch(Exception ex) {}
		  
		  try
		  {
			  if(this.assassin.getPlayers().size() == 0)
			  {
				  this.assassin.unregister();
			  }
		  }
		  catch(Exception ex) {}
		  
		  try
		  {
			  if(!this.girlAlive && this.littleGirl.getPlayers().size() == 0)
			  {
				  this.littleGirl.unregister();
			  }
		  }
		  catch(Exception ex) {}
		  
		  try
		  {
			  if(!this.girlAlive && this.village.getPlayers().size() == 0)
			  {
				  this.village.unregister();
			  }
		  }
		  catch(Exception ex) {}	  
	  }
  }
  
  public void checkVictory()
  {
        if (this.s.getTeams().size() == 1) 
        {
          for (Team lastteam : this.s.getTeams())
          {
        	Bukkit.broadcastMessage("L'équipe " + lastteam.getName() + " a gagné !");
        	if(this.loversAlive)
        	{
            	Bukkit.broadcastMessage("Les amoureux ont survécu ! Ils pourront vivre heureux et...vous connaissez la suite !");
        	}
            Bukkit.getScheduler().cancelAllTasks();
          }
        }	
        else if(this.s.getTeams().size() == 0)
        {
        	Bukkit.broadcastMessage("Toutes les équipes ont été éliminées, personne n'a gagné ! ");
        	Bukkit.getScheduler().cancelAllTasks();
        }	
        else if(this.s.getTeams().size() == 2 && this.loversAlive && this.playersAlive.size() == 2)
        {
        	Bukkit.broadcastMessage("Quelle plus belle preuve d'amour qu'un massacre de masse ? Les amoureux ont triomphé de l'adversité !");
        	Bukkit.getScheduler().cancelAllTasks();
        }		  
  }
  
  public void rolesAnnouncement()
  {
	  PotionEffect nightvision = new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false);

	  for(UUID uid : this.playersAlive)
	  {
		  try
		  {
			  Player pl = Bukkit.getPlayer(uid);
			  OfflinePlayer offpl = (OfflinePlayer)pl;
			  switch(this.playerRoles.get(uid))
			  {
			  case 0:
				  Title.sendTitle(pl, "Vous êtes un villageois", "Survivez aux ténèbres de la nuit");
				  pl.sendMessage("Vous êtes un simple villageois, vous ne disposez d'aucun pouvoir particulier");
				  this.village.addPlayer(offpl);
				  break;
			  case 1:
				  Title.sendTitle(pl, "Vous êtes un loup-garou", "Dévorez les villageois");
				  pl.sendMessage("Vous êtes un loup-garou ! Vous êtes plus fort la nuit, profitez-en pour utiliser votre boussole afin de traquer les villageois proches. Vous gagnez 2 coeurs de vie max en plus à chaque fois que vous tuez un villageois !");
    			  pl.addPotionEffect(nightvision);    			  
    			  this.wolves.addPlayer(offpl);
    			  pl.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
				  break;
			  case 2:
				  Title.sendTitle(pl, "Vous êtes la voyante", "Démasquez les loups");
				  pl.sendMessage("Vous êtes la voyante ! La nuit, utilisez la commande /gwreveal \"pseudo\" pour révéler le rôle d'un joueur.");
				  this.village.addPlayer(offpl);
				  break;
			  case 3:
				  Title.sendTitle(pl, "Vous êtes la sorcière", "Utilisez vos pouvoirs pour sauver le village");
				  pl.sendMessage("Vous êtes la sorcière ! Lorsqu'un joueur meurt, utilisez la commande /gwrevive pour le ressusciter. Une fois par partie, utilisez la commande /gwdamage \"pseudo\" pour faire perdre la moitié de sa vie à un joueur.");
				  this.village.addPlayer(offpl);
				  break;
			  case 4:
				  Title.sendTitle(pl, "Vous êtes le salvateur", "Sauvez vos concitoyens");
				  pl.sendMessage("Vous êtes le salvateur ! Utilisez la commande /gwprotect \"pseudo\" pour conférer Protection à un joueur à chaque épisode. Attention, vous ne pouvez pas vous désigner ni désigner 2 fois d'affilée le même joueur !");
				  this.village.addPlayer(offpl);
				  break;
			  case 5:
				  Title.sendTitle(pl, "Vous êtes la petite fille", "Espionnez les villageois pour démasquer les loups");
				  pl.sendMessage("Vous êtes la petite fille ! La nuit, vous devenez invisible et vous êtes informé de tous les joueurs se trouvant proches de vous.");
    			  pl.addPotionEffect(nightvision);    			  
    			  this.village.addPlayer(offpl);
				  break;
			  case 6:
				  Title.sendTitle(pl, "Vous êtes le loup blanc", "Dévorez les villageois et éliminez les autres loups pour être le mâle alpha");
				  pl.sendMessage("Vous êtes le loup blanc ! Vous avez plus de vie que les autres loups-garous. Vous êtes plus fort la nuit, profitez-en pour utiliser votre boussole afin de traquer les villageois proches !");
    			  pl.addPotionEffect(nightvision);
				  PotionEffect health = new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 4, false, false);
				  pl.addPotionEffect(health);
    			  this.whiteWolf.addPlayer(offpl);
    			  pl.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
				  break;
			  case 7:
				  Title.sendTitle(pl, "Vous êtes le chasseur", "Traquez et tuez les loups");
				  pl.sendMessage("Vous êtes le chasseur ! Lorsque vous mourrez, vous disposerez de 30sec pour utiliser la commande /gwshoot \"pseudo\" pour tirer sur un joueur et lui faire perdre la moitié de sa vie.");
				  this.village.addPlayer(offpl);
				  break;
			  case 8:
				  Title.sendTitle(pl, "Vous êtes l'assassin", "Eliminez le village et les loups pour gagner");
				  pl.sendMessage("Vous êtes l'assassin ! Vous avez plus de vie que les autres villageois et vous êtes plus fort la journée");
				  PotionEffect health1 = new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 2, false, false);
				  pl.addPotionEffect(health1);
    			  this.assassin.addPlayer(offpl);
				  break;
			  case 9:
				  Title.sendTitle(pl, "Vous êtes l'ancien", "Votre sagesse vous protège des loups");
				  pl.sendMessage("Vous êtes l'ancien ! La première fois que vous vous ferez tuer par les loups, vous réapparaîtrez à un endroit aléatoire de la carte au lieu de mourir. Si un villageois vous tue, il perd la moitié de sa vie.");
				  this.village.addPlayer(offpl);
				  break;
			  case 10:
				  Title.sendTitle(pl, "Vous êtes le chaman", "Parlez avec les esprits pour démasquer les loups");
				  pl.sendMessage("Vous êtes le chaman ! Lorsqu'un joueur meurt, vous disposez de 15sec pour communiquer avec l'esprit du défunt.");
				  this.village.addPlayer(offpl);
				  break;
			  case 11:
				  Title.sendTitle(pl, "Vous êtes l'idiot du village", "Qui aura la cruauté de vous accuser ?");
				  pl.sendMessage("Vous êtes l'idiot du village ! Si le village vote pour vous exécuter, votre rôle est révélé et vous ne perdez pas de vie. Les autres joueurs ne pourront plus voter contre vous.");
				  this.village.addPlayer(offpl);
				  break;
			  case 12:
				  Title.sendTitle(pl, "Vous êtes le maître-chien", "Utilisez vos sens pour traquer les loups");
				  pl.sendMessage("Vous êtes le maître-chien ! Au milieu de la nuit, vous serez informé si des loups-garous se trouvent à proximité.");
				  this.village.addPlayer(offpl);
				  break;
			  case 13:
				  Title.sendTitle(pl, "Vous êtes l'enfant sauvage", "Votre fidélité aux villageois ne tient qu'à un fil");
				  pl.sendMessage("Vous êtes l'enfant sauvage ! Votre destin est lié à celui d'un autre villageois. S'il vient à mourir, vous deviendrez un loup-garou.");
				  pl.sendMessage("Le joueur avec lequel vous êtes lié est : " + Bukkit.getPlayer(idol).getName());
				  this.village.addPlayer(offpl);
				  break;
			  case 14:
				  Title.sendTitle(pl, "Vous êtes le mercenaire", "Un tueur sanguinaire à la solde des loups");
				  pl.sendMessage("Vous êtes le mercenaire ! Votre but est d'aider les loups-garous à dévorer les autres villageois.");
				  this.wolves.addPlayer(offpl);
				  break;
			  }
		  }
		  catch(Exception ex) {}
	  }
  }
  
  public void loversAnnouncement()
  {
	  try
	  {
		  Player pl = Bukkit.getPlayer(lover1);
		  pl.sendMessage("Vous avez été touché par une flèche de Cupidon ! Servez-vous de votre boussole pour retrouver votre âme soeur. Mais rappelez-vous que si l'un de vous meurt, l'autre meurt également !");
		  pl.getInventory().addItem(new ItemStack(Material.COMPASS, 1));}
	  catch(Exception ex) {}

	  try
	  {
		  Player pl = Bukkit.getPlayer(lover2);
		  pl.sendMessage("Vous avez été touché par une flèche de Cupidon ! Servez-vous de votre boussole pour retrouver votre âme soeur. Mais rappelez-vous que si l'un de vous meurt, l'autre meurt également !");
		  pl.getInventory().addItem(new ItemStack(Material.COMPASS, 1));	  }
	  catch(Exception ex) {}
  }
  
  public void nightFall()
  {
	  Bukkit.broadcastMessage(ChatColor.GOLD + 
      		"La nuit tombe sur le village, que la chasse commence...");
      	
	  this.psychicCanReveal = true;
	  this.witchCanDamage = true;
	  
      for(UUID uid : this.playersAlive)
	  {
		  if(this.playerRoles.get(uid) == 1 || this.playerRoles.get(uid) == 6)
		  {
			  try
			  {
				  PotionEffect strength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 60 * 20 * 9 + 30 * 20, 0, false, false);
    			  Bukkit.getPlayer(uid).addPotionEffect(strength);
			  }
			  catch(Exception ex) {}
		  }
		  else if(this.playerRoles.get(uid) == 5)
		  {        			
			  this.littleGirl.addPlayer(Bukkit.getOfflinePlayer(uid));
			  
			  try
			  {
				  Player pl = Bukkit.getPlayer(uid);
				  PotionEffect invis = new PotionEffect(PotionEffectType.INVISIBILITY, 60 * 20 * 9 + 30 * 20, 0, false, false);
    			  pl.addPotionEffect(invis);
				  PotionEffect weak = new PotionEffect(PotionEffectType.WEAKNESS, 60 * 20 * 9 + 30 * 20, 0, false, false);
    			  pl.addPotionEffect(weak);
			  }
			  catch(Exception ex) {}
		  }
		  else if(uid == this.savior)
		  {
			  this.saviorCanProtect = true;
			  Bukkit.getPlayer(uid).sendMessage("En tant que salvateur, vous disposez de 1 minute pour utiliser la commande /gwprotect \"pseudo\" pour protéger un villageois pour la nuit et la journée à venir ! Faites le bon choix...");
		  }
		  else if(uid == this.psychic)
		  {
			  this.psychicCanReveal = true;
			  Bukkit.getPlayer(uid).sendMessage("En tant que voyante, vous disposez de 1 minute pour utiliser la commande /gwreveal \"pseudo\" pour révéler le rôle d'un autre villageois ! ");
		  }
	  }
  }
  
  public void sunRise()
  {
	  Bukkit.broadcastMessage(ChatColor.GOLD + 
            	"Le soleil se lève, les villageois se réveillent et pansent leurs blessures...");
      Bukkit.broadcastMessage(ChatColor.GOLD + 
              "C'est maintenant l'heure du vote ! Utilisez la commande /gwvote \"pseudo\" pour désigner un joueur qui perdra la moitié de sa vie.");        	  
        
      this.voteOpen = true;
    	
	  this.psychicCanReveal = false;
	  this.witchCanDamage = false;
	  
	  for(UUID uid : this.playersAlive)
	  {
		  if(this.playerRoles.get(uid) == 5)
		  {
			  this.village.addPlayer(Bukkit.getOfflinePlayer(uid));
			  break;
		  }
		  else if(this.playerRoles.get(uid) == 8)
		  {
			  try
			  {
				  PotionEffect strength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 11 * 60 * 20 + 30 * 20, 0, false, false);
    			  Bukkit.getPlayer(uid).addPotionEffect(strength);
			  }
			  catch(Exception ex) {}
		  }
	  }
  }
  
  public void voteResult()
  {
  	  GameOfWolves.this.voteOpen = false;
  	  
  	  boolean even = false;
  	  int maxVote = 0;
  	  UUID voted = null;
  	  for(UUID uid : this.votes.keySet())
  	  {
  		  if(this.votes.get(uid) > maxVote)
  		  {
  			  even = false;
  			  maxVote = this.votes.get(uid);
  			  voted = uid;
  		  }
  		  if(this.votes.get(uid) == maxVote)
  		  {
  			  even = true;
  		  }
  	  }  	  
  	  this.voted.clear();
  	  this.votes.clear();
  	  
  	  if(even)
  	  {
  		  Bukkit.broadcastMessage("Le village n'a pas réussi à se mettre d'accord lors du conseil, personne n'est exécuté aujourd'hui.");
  		  return;
  	  }
  	  
  	  if(voted == null)
  	  {
  		  return;
  	  }
  	  
  	  if(voted == this.idiot)
  	  {
  		  idiotReveal(Bukkit.getOfflinePlayer(voted).getName());
  		  return;
  	  }
  	  
  	  Bukkit.broadcastMessage("Le village a voté et a décidé d'exécuter " + Bukkit.getOfflinePlayer(voted).getName() + ". Il perd donc la moitié de sa vie.");
  	  try
  	  {
  	  	  Bukkit.getPlayer(voted).setHealth(Bukkit.getPlayer(voted).getHealth() / 2);
  	  }
  	  catch(Exception ex) {}
  }
  
  public void littleGirlScout()
  {
	  if(playerRoles.containsValue(5))
	  {
		  UUID p = null;
		  for(UUID uid : playerRoles.keySet())
		  {
			  if(playerRoles.get(uid) == 5)
			  {
				  p = uid;
				  break;
			  }
		  }
		  
		  if(playersAlive.contains(p))
		  {
    		  try
    		  {
        		  ArrayList<UUID> near = checkNearPlayers(p, 100, false);
    			  Player pl = Bukkit.getPlayer(p);
        		  if(near.size() == 0)
        		  {
        			  pl.sendMessage("Vous êtes seule, personne ne vous entendra crier dans la nuit...");
        		  }
        		  else
        		  {
        			  pl.sendMessage("Les personnes suivantes se trouvent proches de vous : ");
        			  String list = "";
        			  for(UUID id : near)
        			  {
        				  list += Bukkit.getPlayer(id).getName() + " / ";
        			  }
        			  pl.sendMessage(list);
        		  }
    		  }
    		  catch(Exception ex) {}
		  }
	  }
  }
  
  public void houndMasterScout()
  {
	  if(playerRoles.containsValue(12))
	  {
		  UUID p = null;
		  for(UUID uid : playerRoles.keySet())
		  {
			  if(playerRoles.get(uid) == 12)
			  {
				  p = uid;
				  break;
			  }
		  }
		  
		  if(playersAlive.contains(p))
		  {
    		  try
    		  {
        		  ArrayList<UUID> near = checkNearPlayers(p, 200, true);
    			  Player pl = Bukkit.getPlayer(p);
        		  if(near.size() == 0)
        		  {
        			  pl.sendMessage("Aucun danger dans les environs, pour l'instant...");
        		  }
        		  else if(near.size() == 1)
        		  {
        			  pl.sendMessage("Un loup est tout proche, soyez sur vos gardes...");
        		  }
        		  else
        		  {
        			  pl.sendMessage("La meute est en approche, FUYEZ !!");
        		  }
    		  }
    		  catch(Exception ex) {}
		  }
	  }
  }
  
  public void idiotReveal(String name)
  {
	  this.idiotRevealed = true;
	  Bukkit.broadcastMessage(name + " était l'idiot du village ! Rien ne lui arrive, et vous ne pourrez plus voter contre lui désormais ! Monstres !");
  }
  
  public void UpdateCompassTarget(boolean night)
  {
  		for(UUID uid : this.playersAlive)
  		{
  			try
  			{
  				Player pl = Bukkit.getPlayer(uid);
  				pl.setCompassTarget(new Location(this.world, 0, 0, 0));
  				
  				if(uid == lover1)
  				{
  					pl.setCompassTarget(Bukkit.getPlayer(lover2).getLocation());
  				}
  				else if(uid == lover2)
  				{
  					pl.setCompassTarget(Bukkit.getPlayer(lover1).getLocation());
  				}
  				
  				if((playerRoles.get(uid) == 1 || playerRoles.get(uid) == 6) && night)
  				{
  					pl.setCompassTarget(Bukkit.getPlayer(getNearestVillager(uid)).getLocation());
  				}
  			}
  			catch(Exception ex) {}
  		}
  }
  
  public ArrayList<UUID> checkNearPlayers(UUID uid, int distance, boolean wolf)
  {
	  ArrayList<UUID> nearPlayers = new ArrayList<UUID>();
	  try
	  {
		  Player pl = Bukkit.getPlayer(uid);
		  for(UUID pid : this.playersAlive)
		  {
			  Player p = Bukkit.getPlayer(pid);
			  if(uid != pid && pl.getLocation().distance(p.getLocation()) <= distance)
			  {
				  if(wolf && !(this.playerRoles.get(pid) == 1 || this.playerRoles.get(pid) == 6))
				  {
					  continue;
				  }
				  nearPlayers.add(pid);
			  }
		  }		  
	  }
	  catch(Exception ex) {}
	  
	  return nearPlayers;
  }
  
  public UUID getNearestVillager(UUID uid)
  {
	  UUID nearestPlayer = null;
	  double minDist = 99999;
	  double dist;
	  for(UUID pid : this.playersAlive)
	  {
		  try
		  {
			  Player pl = Bukkit.getPlayer(uid);
			  Player p = Bukkit.getPlayer(pid);
			  dist = pl.getLocation().distance(p.getLocation());
			  if(uid != pid 
					  && dist <= minDist 
					  && !(playerRoles.get(pid) == 1 || playerRoles.get(pid) == 6))
			  {
				  minDist = dist;
				  nearestPlayer = pid;
			  }
		  }		  
		  catch(Exception ex) {}
	  }
	  
	  return nearestPlayer;
  }
	
  
  //SCOREBOARD FUNCTIONS
  //-------------------
  
  public void initScoreboard()
  {
	    this.s.getObjective(this.obj.getDisplayName())
	      .getScore(ChatColor.WHITE + "Episode " + this.episode)
	      .setScore(0);
	    this.s.getObjective(this.obj.getDisplayName())
	      .getScore("" + ChatColor.WHITE + EventsClass.alive.size() + ChatColor.GRAY + " joueurs")
	      .setScore(-1);
	    this.tmpBorder = (int)getServer().getWorld(getConfig().getString("world")).getWorldBorder().getSize();
	    this.s.getObjective(this.obj.getDisplayName())
	      .getScore(ChatColor.WHITE + "Border : " + tmpBorder + " x " + tmpBorder)
	      .setScore(-3);
	    NumberFormat objFormatter = new DecimalFormat("00");
	    this.objMinute = objFormatter.format(this.getConfig().getInt("options.pvptime"));
	    this.objSecond = "00";
	    this.objTxt = "PvP : ";
	    this.countdownObj = this.objTxt + this.objMinute + ":" + this.objSecond;
	    this.tmpPlayers = EventsClass.alive.size();
  }
  
  public void writeScoreboard(int minutes, int seconds)
  {      
      NumberFormat formatter2 = new DecimalFormat("00");
      String minute2 = ((NumberFormat)formatter2).format(minutes);
      String second2 = ((NumberFormat)formatter2).format(seconds);
      
      this.s.getObjective(GameOfWolves.this.obj.getDisplayName())
        .getScore(ChatColor.WHITE + "Episode " + this.episode)
        .setScore(0);
      this.s.getObjective(this.obj.getDisplayName())
        .getScore("" + ChatColor.WHITE + EventsClass.alive.size() + ChatColor.GRAY + " joueurs")
        .setScore(-1);
      this.tmpBorder = (int)getServer().getWorld(getConfig().getString("world")).getWorldBorder().getSize();
      this.s.getObjective(this.obj.getDisplayName())
        .getScore(ChatColor.WHITE + "Border : " + tmpBorder + " x " + tmpBorder)
        .setScore(-3);

      this.tmpPlayers = EventsClass.alive.size();

      if(this.gameState < 2)
      {
        if(!this.hasChangedGS)
        {
          int min = Integer.parseInt(this.objMinute);
          int sec = Integer.parseInt(this.objSecond);
           
          if (sec == 0)
          {              
        	  this.objSecond = "59";
        	  this.objMinute = formatter2.format(min - 1);
          }
          else
          {
        	  this.objSecond = formatter2.format(sec - 1);
          }
          
          this.countdownObj = this.objTxt + this.objMinute + ":" + this.objSecond;
        }
        else
        {
        	this.hasChangedGS = false;
        }
        
        this.s.getObjective(this.obj.getDisplayName())
          .getScore(ChatColor.WHITE + this.countdownObj)
          .setScore(-4);
      }

      this.s.getObjective(this.obj.getDisplayName())
        .getScore(minute2 + ":" + second2)
        .setScore(-5);
  }
  
}
