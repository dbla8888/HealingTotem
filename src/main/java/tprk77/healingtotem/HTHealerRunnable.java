package tprk77.healingtotem;

import java.util.List;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import tprk77.healingtotem.totem.Totem;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * This class handles executing the
 * effects of totems in the game world. 
 * @author tim, Aaron
 */
public class HTHealerRunnable implements Runnable 
{
	private final HTPlugin plugin;
	private final int period;
	private int taskID;
    private LivingEntityProcessor processor;

	HTHealerRunnable(HTPlugin plugin)
    {
		this.plugin = plugin;
        period = this.plugin.getTotemManager().getEffectInterval();               
        processor = new LivingEntityProcessor(
                        this.plugin,
                    	this.plugin.getServer().getPluginManager(),
                    	this.plugin.getTotemManager().getStackedHeal(),
                    	this.plugin.getTotemManager().getStackedDamage());
    }
	
	public void schedule()
    {
		BukkitScheduler scheduler = this.plugin.getServer().getScheduler();
		this.taskID = scheduler.scheduleSyncRepeatingTask(this.plugin, this, 0, this.period);
		if(this.taskID == -1){this.plugin.warn("failed to schedule!");}
	}

	public void cancel()
    {
		BukkitScheduler scheduler = this.plugin.getServer().getScheduler();
		scheduler.cancelTask(this.taskID);
	}

	public void run()
    {

		List<World> worlds = this.plugin.getServer().getWorlds();
		List<Totem> totems;
		List<LivingEntity> livingentities;
		
		for(World world : worlds)
		{
			totems = this.plugin.getTotemManager().getTotemsByWorld(world);
			livingentities = world.getLivingEntities();
			
			for(LivingEntity livingentity : livingentities)
			{
					processor.process(livingentity, totems);
			}
		}
	}
}
