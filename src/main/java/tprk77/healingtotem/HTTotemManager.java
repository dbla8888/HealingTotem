package tprk77.healingtotem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import tprk77.healingtotem.totem.Totem;
import tprk77.healingtotem.totem.TotemType;
import tprk77.util.BlockHashable;
import tprk77.util.structure.Rotator;
import tprk77.util.structure.StructureType;

/**
 *  This class handles the loading of both TOTEMTYPES and GLOBAL configurations,
 * and manages the loading and storage of created totems.
 * @author tim, Aaron
 */
public class HTTotemManager {

	private final HTPlugin plugin;

	private final String TOTEM_TYPES_FILENAME = "config.yml";
	//private final String TOTEM_FILENAME = "totems.yml";

	private List<TotemType> totemtypes;
	private List<Totem> totems;
    private int stacked_heal;
    private int stacked_damage;
    private int effect_interval;
    private int totemsperplayer;
	private boolean lightning;
	private boolean quiet;
	HashMap<BlockHashable, Set<Totem>> blockhash;
    HashMap<String, Set<Totem>> ownerhash;
    HashMap<World,Set<Totem>> worldTotemsHash;

	public HTTotemManager(HTPlugin plugin)
    {
		this.plugin = plugin;
		this.totemtypes = new ArrayList<TotemType>();
		this.totems = new ArrayList<Totem>();
		this.blockhash = new HashMap<BlockHashable, Set<Totem>>();
        this.ownerhash = new HashMap<String, Set<Totem>>();
        this.worldTotemsHash = new HashMap<World,Set<Totem>>();
        this.stacked_damage = 4;
        this.stacked_heal = 4;
        this.effect_interval = 20;
	}

	public List<Totem> getTotems()
    {
		return new ArrayList<Totem>(this.totems);
	}

	public List<Totem> getTotemsByWorld(World world)
	{
		if(worldTotemsHash.get(world) != null)
		{
			return new ArrayList<Totem>(worldTotemsHash.get(world));
		}else{
			return new ArrayList<Totem>();
		}
	}

	public List<TotemType> getTotemTypes()
    {
		return new ArrayList<TotemType>(this.totemtypes);
	}
        
    public int getStackedDamage()
    {
        return stacked_damage;
    }
        
    public int getStackedHeal()
    {
        return stacked_damage;
    }
    
    public int getEffectInterval()
    {
        return effect_interval;
    }
    
    public boolean isLightning()
    {
        return this.lightning;
}

	public boolean isQuiet()
    {
        return this.quiet;
	}

	public int getTotemsPerPlayer()
    {
        return this.totemsperplayer;
	}

	public void addTotem(Totem totem)
    {
		this.totems.add(totem);

		Set<Totem> existing;
		
		// add to block hash
		for(Block block : totem.getBlocks()){
			BlockHashable bh = new BlockHashable(block);
			existing = this.blockhash.get(bh);
			if(existing == null){
				this.blockhash.put(bh, new HashSet<Totem>(Arrays.asList(totem)));
			}else{
				existing.add(totem);
			}
		}
                
        // add to owner hash
		String owner = totem.getOwner();
		existing = this.ownerhash.get(owner);
		if(existing == null){
			this.ownerhash.put(owner, new HashSet<Totem>(Arrays.asList(totem)));
		}else{
			existing.add(totem);
		}
		
		//add to world totems hash
		if(this.worldTotemsHash.get(totem.getWorld()) == null)
		{
			this.worldTotemsHash.put(totem.getWorld(), new HashSet<Totem>(Arrays.asList(totem)));
		}else{
			this.worldTotemsHash.get(totem.getWorld()).add(totem);
		}
	}

	public void removeTotem(Totem totem)
    {
		this.totems.remove(totem);

		Set<Totem> existing;
		
		// remove from block hash
		for(Block block : totem.getBlocks()){
			BlockHashable bh = new BlockHashable(block);
			existing = this.blockhash.get(bh);
			existing.remove(totem);
			if(existing.isEmpty()){
				this.blockhash.remove(bh);
			}
		}
                
        // remove from owner hash
		String owner = totem.getOwner();
		existing = this.ownerhash.get(owner);
		existing.remove(totem);
		if(existing.isEmpty()){
			this.ownerhash.remove(owner);
		}
		
		//remove from world totems hash
		if(this.worldTotemsHash.get(totem.getWorld())!= null)
		{
			this.worldTotemsHash.get(totem.getWorld()).remove(totem);
			if(this.worldTotemsHash.get(totem.getWorld()).isEmpty())
			{
				this.worldTotemsHash.remove(totem.getWorld());
			}
		}
	}

	public Set<Totem> getTotemsFromBlock(Block block)
    {
        BlockHashable bh = new BlockHashable(block);
		Set<Totem> totemset = this.blockhash.get(bh);
		if(totemset == null) return null;
		return new HashSet<Totem>(totemset);
	}
        
    public Set<Totem> getTotemsFromPlayer(Player player)
    {
		String owner = player.getName();
		Set<Totem> totemset = this.ownerhash.get(owner);
		if(totemset == null) return null;
		return new HashSet<Totem>(totemset);
	}

	public TotemType getTotemType(String name)
    {
		for(TotemType type : this.totemtypes){
			if(type.getName().equals(name)){
				return type;
			}
		}
		return null;
	}
        
	public void loadTotemTypesOrDefault()
    {
		File totemtypesfile = new File(this.plugin.getDataFolder(), this.TOTEM_TYPES_FILENAME);
		if(!totemtypesfile.exists()){
			try{
				this.plugin.log("No "+ TOTEM_TYPES_FILENAME + 
						" found. Loading default "+ TOTEM_TYPES_FILENAME);
				this.plugin.saveDefaultConfig();
				this.plugin.reloadConfig();
			}catch(Exception ex){
				this.plugin.warn("Error loading "+ TOTEM_TYPES_FILENAME);
			}
		}
		this.loadTotemTypes();
	}

	private void loadTotemTypes()
    {         
       totemtypes.clear();
       
       loadGlobalConfigs(plugin.getConfig().getConfigurationSection("GLOBAL"));
                
       List<Map<?,?>> nodes = plugin.getConfig().getMapList("TOTEMTYPES");
       List<ConfigurationSection> nodelist = this.mapListToConfigSectionList(nodes);
                
       for(ConfigurationSection node : nodelist)
		{
			TotemType totemtype = this.yaml2totemtype(node);          
			if(totemtype == null)
			{
				this.plugin.warn("a totem type couldn't be loaded");
			}else
			{
				this.totemtypes.add(totemtype);
			}
		}

		/*
		 * Sort the TotemTypes by structure size. This way, larger totems will be
		 * found before smaller totems (and possibly subtotems).
		*/ 
		Collections.sort(this.totemtypes, new Comparator<TotemType>(){

			public int compare(TotemType o1, TotemType o2){
				return o1.getStructureType().getBlockCount()
								- o2.getStructureType().getBlockCount();
			}
		});
		Collections.reverse(this.totemtypes);

		this.plugin.log("loaded " + this.totemtypes.size() + " totem types");
                
	}
        
	public void loadTotems()
    {
		//clear out all the old totems
        totems.clear();        
        worldTotemsHash.clear();
        blockhash.clear();
        ownerhash.clear();
        
        List<Map<?,?>> nodes = plugin.getTotemsFile().getMapList("totems");
        List<ConfigurationSection> nodelist = this.mapListToConfigSectionList(nodes);
        //this.plugin.log(nodelist.size() + " totems found");//DEBUG
        
		for(ConfigurationSection node : nodelist){
			Totem totem = this.yaml2totem(node);                  
			if(totem == null){
				this.plugin.warn("a totem couldn't be loaded");
			}else{
				this.addTotem(totem);
			}
		}

		this.plugin.log("loaded " + this.totems.size() + " totems");
	}

	public void saveTotems()
    {		
                
		List<Object> yamllist = new ArrayList<Object>();

		for(Totem totem : this.totems){
			yamllist.add(this.totem2yaml(totem));
		}

		this.plugin.log("saved " + this.totems.size() + " totems");
                
		plugin.getTotemsFile().set("totems", yamllist);
		plugin.savetotemConfig();
	}

	private Map<String, Object> totem2yaml(Totem totem)
    {
		HashMap<String, Object> yamlmap = new HashMap<String, Object>();
		yamlmap.put("world", totem.getRootBlock().getWorld().getName());
		yamlmap.put("x", totem.getRootBlock().getX());
		yamlmap.put("y", totem.getRootBlock().getY());
		yamlmap.put("z", totem.getRootBlock().getZ());
		yamlmap.put("type", totem.getTotemType().getName());
                
        String owner = totem.getOwner();
		if(totem.getOwner() != null){
			yamlmap.put("owner", owner);
		}else{                                                          //test code
                    yamlmap.put("owner", "gaia");
                }
		return yamlmap;
	}

	private Totem yaml2totem(ConfigurationSection node)
    {
		String worldstr = node.getString("world", null);
		if(worldstr == null){
			this.plugin.warn("totem's world is not set");
			return null;
		}

		int x = node.getInt("x", Integer.MIN_VALUE);
		int y = node.getInt("y", Integer.MIN_VALUE);
		int z = node.getInt("z", Integer.MIN_VALUE);
		if(x == Integer.MIN_VALUE || y == Integer.MIN_VALUE || z == Integer.MIN_VALUE){
			this.plugin.warn("totem's x, y, or z is not set");
			return null;
		}
                
		String totemtypestr = node.getString("type", null);
		if(totemtypestr == null){
			this.plugin.warn("totem's type is not set");
			return null;
		}

		World world = this.plugin.getServer().getWorld(worldstr);
		if(world == null){
			this.plugin.warn("totem's world is not recognized");
			return null;
		}
                
        String owner = node.getString("owner", null);
		if(owner == null){
			this.plugin.warn("totem's owner is not set");
		}

		TotemType totemtype = this.getTotemType(totemtypestr);
		if(totemtype == null){
			this.plugin.warn("totem's type is not recognized");
			return null;
		}

		Block block = world.getBlockAt(x, y, z);
		Totem totem = new Totem(totemtype, block, owner);

		if(!totem.verifyStructure()){
			this.plugin.warn("totem's structure was bad");
			return null;
		}

		return totem;
	}
	
/*
	private List<Object> structuretype2yaml(StructureType structuretype)
    {
		List<Object> yamllist = new ArrayList<Object>();
		for(BlockOffset offset : structuretype.getPattern().keySet())
		{
			Material material = structuretype.getPattern().get(offset);
			HashMap<String, Object> part = new HashMap<String, Object>();
			part.put("x", offset.getX());
			part.put("y", offset.getY());
			part.put("z", offset.getZ());
			part.put("material", material.toString());
			yamllist.add(part);
		}
		return yamllist;
	}
*/	
	
	private StructureType yaml2structuretype(List<ConfigurationSection> nodes)
    {
		StructureType.Prototype prototype = new StructureType.Prototype();
		for(ConfigurationSection node : nodes){
			int x = node.getInt("x", Integer.MIN_VALUE);
			int y = node.getInt("y", Integer.MIN_VALUE);
			int z = node.getInt("z", Integer.MIN_VALUE);
			if(x == Integer.MIN_VALUE || y == Integer.MIN_VALUE || z == Integer.MIN_VALUE){
				this.plugin.warn("structure's x, y, or z is not set");
				return null;
			}

			String materialstr = node.getString("material", null);
			if(materialstr == null){
				this.plugin.warn("structure's material is not set");
				return null;
			}

			Material material = Material.matchMaterial(materialstr);
			if(material == null){
				this.plugin.warn("structure's material is not recognized");
				return null;
			}

			prototype.addBlock(x, y, z, material);
		}
		return new StructureType(prototype);
	}
        
    /**
    * This method takes a TOTEMTYPE configuration node and converts it to a 
    * TotemType
    * @param node
    * @return TotemType
    */
	private TotemType yaml2totemtype(ConfigurationSection node)
    {

		String name = node.getString("NAME",null);
		if(name == null){
			this.plugin.warn("A totem type's name is not set");
			return null;
		}

		//TODO: Allow users to choose between Spherical, Ellipsoid, Cylindrical,
		//Cubic, or Rectangular Cuboid ranges for totems.
		double range = node.getDouble("RANGE", Double.NaN);
		if(Double.isNaN(range)){
			this.plugin.warn("totem type's range is not set");
			return null;
		}
		//TODO: Add more information about gradients into the config file, perhaps
		//links to graphs or something.  Or maybe we should just wait until we are
		//at a mature stage in the plugin and make a Youtube video explaining
		//it all.  Or both.  Whatever.
		String gradient = node.getString("GRADIENT", "NONE");
		//System.out.println(gradient);//DEBUG
		if(!gradient.equals("NONE") && 
				!gradient.equals("LINEAR") && 
				!gradient.equals("QUADRATIC") &&
				!gradient.equals("EXPONENTIAL"))
		{
			this.plugin.warn("Invalid gradient for totem type");
			return null;
		}

		String rotatorstr = node.getString("ROTATOR", null);
		if(rotatorstr == null){
			this.plugin.warn("totem type's rotator is not set");
			rotatorstr = ":(";
		}

		Rotator rotator = Rotator.matchRotator(rotatorstr);
		if(rotator == null){
			this.plugin.warn("Totem type's rotator is not valid, using default");
			rotator = Rotator.getDefault();
		}
                
                ConfigurationSection effectsNode = node.getConfigurationSection("EFFECT");
		if(effectsNode == null){
			this.plugin.warn("totem has no effects");
			return null;
		}


        List<Map<?,?>> nodes = node.getMapList("STRUCTURE");
        List<ConfigurationSection> structuretypenodes = this.mapListToConfigSectionList(nodes);                
                
		if(structuretypenodes.isEmpty()){
			this.plugin.warn("totem type's structure is not set");
			return null;
		}

		StructureType structuretype = this.yaml2structuretype(structuretypenodes);
		if(structuretype == null){
			this.plugin.warn("totem type's structure is not valid");
			return null;
		}

		if(structuretype.getBlockCount() < 3){
			this.plugin.warn("For technical reasons, the structure's block count must be at least 3.");
			return null;
		}

		return new TotemType(name, effectsNode, range, structuretype, rotator, gradient);
	}

	/*
	private Map<String, Object> totemtype2yaml(TotemType totemtype)
    {
		HashMap<String, Object> yamlmap = new HashMap<String, Object>();
		yamlmap.put("NAME", totemtype.getName());
		yamlmap.put("EFFECTS", totemtype.getEffects());
		yamlmap.put("RANGE", totemtype.getRange());
		yamlmap.put("ROTATOR", totemtype.getRotator().toString());
		yamlmap.put("STRUCTURE", this.structuretype2yaml(totemtype.getStructureType()));
		return yamlmap;
	}	
	*/
	
    /**
    * Loads Global Configurations, such as the maximum stacked damage and
    * effect interval
    * @param node The GLOBAL configuration node at the head of the 
    * totemtypes.yml configuration file
    */
    private void loadGlobalConfigs(ConfigurationSection node)
    {
            
        stacked_heal = node.getInt("STACKED_HEALING_MAXIMUM", 0);
		if(stacked_heal > 20 || stacked_heal < 0)
		{
                    stacked_heal = 0;
                    this.plugin.warn("MAXIMUM STACKED HEALING SET TO 0!");
		}
                
        stacked_damage = node.getInt("STACKED_DAMAGE_MAXIMUM", 0);
		if(stacked_damage > 20 || stacked_damage < 0)
		{
                    stacked_damage = 0;
                    this.plugin.warn("MAXIMUM STACKED DAMAGE SET TO 0!");
		}
                
                //gets the effect interval from the config file in seconds and
                //converts it to ticks.  If the interval is less than a second,
                //set the interval to 1 second and warn the user
        effect_interval = node.getInt("EFFECT_INTERVAL", 1);
		if(effect_interval < 1)
        {
	        effect_interval = 20;
	        this.plugin.warn("Invalid effect interval");
		}else
        {
            effect_interval *= 20;
        }
        this.plugin.log("Effect interval set to " + 
                         effect_interval/20 + " second(s)");
        
        totemsperplayer = node.getInt("MAX_TOTEMS_PER_PLAYER", 4);
        if(totemsperplayer < 0)
        {
            totemsperplayer = 4;
        }
        //System.out.println("Totems per player: " + totemsperplayer);//DEBUG       
        
        lightning = node.getBoolean("LIGHTNING_ON_BUILD", false);
        //System.out.println("Lightning: " + lightning);//DEBUG
        
        quiet = node.getBoolean("SUPPRESS_MESSAGES", false);
        //System.out.println("Quiet: " + quiet);//DEBUG
    }
        
    private List<ConfigurationSection> mapListToConfigSectionList(List<Map<?,?>> maps)
        {
            LinkedList<ConfigurationSection> nodes = new LinkedList<ConfigurationSection>();
            nodes.clear();
            File dummy =new File(plugin.getDataFolder(), "dummy.yml");  
            ConfigurationSection conf = YamlConfiguration.loadConfiguration(dummy);
            
            for(Map<?,?> map: maps)
                {
                    nodes.add(conf.createSection("dummy", map));
                }    
            return nodes;
        }
                
}
