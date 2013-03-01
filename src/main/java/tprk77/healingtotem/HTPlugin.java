package tprk77.healingtotem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Plugin class for this plugin (clearly). Creates new instances of the 
 * TotemManager, BlockListener, and HealerRunnable classes, manages the logging
 * of messages to the server, and handles cleanup on server stop. Also handles
 * command usage.
 * @author tim, modification by Aaron
 */
public final class HTPlugin extends JavaPlugin {

	private static final Logger log = Logger.getLogger("Minecraft");
	private HTTotemManager totemmanager;
	private HTBlockListener blocklistener;
	private HTHealerRunnable healerrunnable;
    private final String TOTEM_FILENAME = "totems.yml";
    private FileConfiguration totemConfig = null;
    private File totemConfigFile = null;

    /**
     * This code is executed once, on server startup.  Loads the config
     * files, registers the block placement listener, schedules the healer
     * runnable, and creates the command executor which handles the reload
     * command.
     */
	@Override
	public void onEnable()
        {

		this.log("is enabled!");

                //load totems and totemtypes
		this.totemmanager = new HTTotemManager(this);
        this.loadTotemsFile();
		this.totemmanager.loadTotemTypesOrDefault();
		this.totemmanager.loadTotems();
                
                //register block listener
		this.blocklistener = new HTBlockListener(this);
		getServer().getPluginManager().registerEvents(blocklistener, this);
                
                //schedule to healer runnable
		this.healerrunnable = new HTHealerRunnable(this);
		this.healerrunnable.schedule();
                
                //creates the executor which handles the reload command
		HTCommandExecutor commandExecutor = new HTCommandExecutor(this);		
        getCommand("HTReload").setExecutor(commandExecutor);

	}       
        
    /**
     * This code is executed once, on server shutdown.  It un-schedules the
     * healer runnable and saves totems.
     */
	@Override
	public void onDisable()
    {
                
        if(this.healerrunnable != null)
        {this.healerrunnable.cancel();}
        
        if(this.totemmanager != null)
        {this.totemmanager.saveTotems();}
        
        this.log("is disabled!");
	}
        
	protected HTTotemManager getTotemManager()
    {
		return this.totemmanager;
	}
	
	protected HTHealerRunnable getHealerRunnable()
	{
		return this.healerrunnable;
	}	

    /**
     * gets the totems.yml file
     * @return FileConfiguration representing the totems.yml file
     */
	protected FileConfiguration getTotemsFile() 
    {
        if (totemConfig == null) {
            this.loadTotemsFile();
        }
        return totemConfig;
    }
        
     /**
     * Loads the totems.yml file into memory.  Note that this does NOT
     * load the totems into the program yet, just makes it accessible to us
     */
	protected void loadTotemsFile() 
    {
    	totemConfigFile = new File(getDataFolder(), TOTEM_FILENAME);
        totemConfig = YamlConfiguration.loadConfiguration(totemConfigFile);
 
        // Look for defaults in the jar
        InputStream defConfigStream = this.getResource(TOTEM_FILENAME);
        if (defConfigStream != null) 
        {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            totemConfig.setDefaults(defConfig);
        }
    }
        
    /**
     * Saves the totems.yml file
     */
	protected void savetotemConfig() 
    {
        if (totemConfig == null || totemConfigFile == null) 
        {return;}
        try {
            totemConfig.save(totemConfigFile);
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save config to " + totemConfigFile, ex);
        }
    }

	protected void log(String message)
    {
		this.log(Level.INFO, message);
	}

	protected void log(Level level, String message)
    {
		PluginDescriptionFile desc = this.getDescription();
		HTPlugin.log.log(level, desc.getName() + " v" + desc.getVersion() + ": " + message);
	}

	protected void warn(String message)
    {
		this.log(Level.WARNING, message);
    }
	
}
