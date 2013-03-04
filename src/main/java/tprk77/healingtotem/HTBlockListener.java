package tprk77.healingtotem;

import java.util.List;
import java.util.Set;
import org.bukkit.ChatColor;
import tprk77.healingtotem.totem.Totem;
import tprk77.healingtotem.totem.TotemType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;

/**
 *
 * @author tim
 */
public class HTBlockListener implements Listener 
{
	private final HTPlugin plugin;

	//enum SubstructurePolicy {ALLOWED, REPLACE, NOT_ALLOWED};
	//private final SubstructurePolicy substructurepolicy;

	public HTBlockListener(HTPlugin plugin)
	{
		this.plugin = plugin;
		//this.substructurepolicy = SubstructurePolicy.NOT_ALLOWED;
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if(event.isCancelled()) return;
		
        String owner = event.getPlayer().getName();                
		Block placedblock = event.getBlockPlaced();
		List<TotemType> totemtypes = this.plugin.getTotemManager().getTotemTypes();

		totembuild:
		for(TotemType totemtype : totemtypes)
		{
			Totem totem = new Totem(totemtype, placedblock, owner);
			if(!totem.verifyStructure()) continue totembuild;

			// check permissions!
			Player player = event.getPlayer();
			if(!player.hasPermission("healingtotem.build"))
			{
				event.setCancelled(true);
				player.sendMessage(ChatColor.RED + "You do not have permission to build totems.");
				return;
			}

			// check the number of totems
			Set<Totem> totemset = this.plugin.getTotemManager().getTotemsFromPlayer(player);
			if(totemset != null && totemset.size()
							>= this.plugin.getTotemManager().getTotemsPerPlayer()
							&& !player.hasPermission("healingtotem.unlimitedbuild"))
			{
				event.setCancelled(true);
				player.sendMessage(ChatColor.RED + "You have reached the maximum number of totems you can build.");
				return;
			}

//			if(this.substructurepolicy == SubstructurePolicy.NOT_ALLOWED)
//			{
				for(Block block : totem.getBlocks())
				{
					if(this.plugin.getTotemManager().getTotemsFromBlock(block) != null)
					{
						break totembuild;
					}
				}
//			}else if(this.substructurepolicy == SubstructurePolicy.REPLACE)
//			{
//				// TODO this REPLACE code doesn't work / isn't finished
//				for(Block block : totem.getBlocks())
//				{
//					Set<Totem> subtotems = this.plugin.getTotemManager().getTotemsFromBlock(block);
//					if(subtotems != null)
//				    {
//						for(Totem subtotem : subtotems)
//				        {
//							this.plugin.getTotemManager().removeTotem(subtotem);
//						}
//					}
//				}
//			}

			// lightning strike!
			if(this.plugin.getTotemManager().isLightning())
			{
				placedblock.getWorld().strikeLightningEffect(placedblock.getLocation());
			}
                                
            this.plugin.getTotemManager().addTotem(totem);
			this.plugin.getTotemManager().saveTotems();
                        
            if(!this.plugin.getTotemManager().isQuiet())
            {
				player.sendMessage(ChatColor.DARK_AQUA + "A " + totem.getTotemType().getName().toLowerCase() +
                                         " totem has been built.");
			}
			break totembuild;
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if(event.isCancelled()) return;

		Block brokenblock = event.getBlock();
		Set<Totem> totems = this.plugin.getTotemManager().getTotemsFromBlock(brokenblock);

		if(totems == null) return;

		// check permissions!
		Player player = event.getPlayer();
		if(!player.hasPermission("healingtotem.break"))
		{
			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + "You do not have permission to break totems.");
			return;
		}

		// lightning strike!
		if(this.plugin.getTotemManager().isLightning()){
			brokenblock.getWorld().strikeLightningEffect(brokenblock.getLocation());
		}

		for(Totem totem : totems)
		{
			// TODO add REPLACE code?
			this.plugin.getTotemManager().removeTotem(totem);
			this.plugin.getTotemManager().saveTotems();
		}

		if(!this.plugin.getTotemManager().isQuiet())
		{
			player.sendMessage(ChatColor.DARK_AQUA + "A totem has been destroyed.");
		}
	}
}
