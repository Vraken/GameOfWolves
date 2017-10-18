package fr.vraken.gameofwolves;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class FilesManager
{
	private File configf;
	private File rolef;
    private FileConfiguration config;
    private FileConfiguration role;
    GameOfWolves plugin;
    
    public FilesManager(GameOfWolves plugin) throws IOException, InvalidConfigurationException
    {
    	this.plugin = plugin;
        createFiles();
        addConfigDefault();
        addRoleDefault();
    }
    
    public FileConfiguration getRoleConfig()
    {
    	return this.role;
    }

    private void createFiles() throws IOException 
    {
        configf = new File(plugin.getDataFolder(), "config.yml");
        rolef = new File(plugin.getDataFolder(), "role.yml");

        if (!configf.exists()) 
        {
            configf.createNewFile();
        }
        if (!rolef.exists()) 
        {
        	rolef.createNewFile();
        }

        config = new YamlConfiguration();
        role = new YamlConfiguration();
    }

    public void addConfigDefault() throws FileNotFoundException, IOException, InvalidConfigurationException
    {
        config.load(configf);

        plugin.getConfig().addDefault("worldborder.size", Integer.valueOf(1500));
        plugin.getConfig().addDefault("worldborder.finalsize", Integer.valueOf(100));
        plugin.getConfig().addDefault("worldborder.retractafter", Integer.valueOf(100));
        plugin.getConfig().addDefault("worldborder.episodestorestract", Integer.valueOf(1));
        plugin.getConfig().addDefault("lobby.world", "lobby");
        plugin.getConfig().addDefault("lobby.X", Integer.valueOf(0));
        plugin.getConfig().addDefault("lobby.Y", Integer.valueOf(100));
        plugin.getConfig().addDefault("lobby.Z", Integer.valueOf(0));
        plugin.getConfig().addDefault("world", "world");
        plugin.getConfig().addDefault("options.nodamagetime", Integer.valueOf(20));
        plugin.getConfig().addDefault("options.minplayers", Integer.valueOf(20));
        plugin.getConfig().addDefault("options.pvptime", Integer.valueOf(20));
        plugin.getConfig().addDefault("options.cooldown", Boolean.valueOf(false));
        plugin.getConfig().addDefault("options.setrolesafter", Integer.valueOf(30));
        plugin.getConfig().addDefault("options.minspawndistance", Integer.valueOf(100));
        
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }

    public void addRoleDefault() throws FileNotFoundException, IOException, InvalidConfigurationException
    {
        this.role.load(rolef);

        this.role.addDefault("villageois", Integer.valueOf(1));
        this.role.addDefault("loup-garou", Integer.valueOf(1));
        this.role.addDefault("voyante", Integer.valueOf(1));
        this.role.addDefault("sorciere", Integer.valueOf(1));
        this.role.addDefault("salvateur", Integer.valueOf(1));
        this.role.addDefault("petite fille", Integer.valueOf(1));
        this.role.addDefault("loup blanc", Integer.valueOf(1));
        this.role.addDefault("chasseur", Integer.valueOf(1));
        this.role.addDefault("assassin", Integer.valueOf(1));
        this.role.addDefault("ancien", Integer.valueOf(1));
        this.role.addDefault("chaman", Integer.valueOf(1));
        this.role.addDefault("idiot", Integer.valueOf(1));
        this.role.addDefault("maitre-chien", Integer.valueOf(1));
        this.role.addDefault("enfant sauvage", Integer.valueOf(1));
        this.role.addDefault("mercenaire", Integer.valueOf(1));
        
        this.role.options().copyDefaults(true);
        this.role.save(this.rolef);
    }
}
