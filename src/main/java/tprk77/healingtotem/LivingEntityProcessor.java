package tprk77.healingtotem;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import tprk77.healingtotem.totem.Totem;

public class LivingEntityProcessor
{		
	private final PluginManager eventCaller;
	private final HTPlugin plugin;
	private final int stackedheal;
	private final int stackeddamage;
    Random random = new Random();
    byte[] randomcolor = new byte[1];  


	public LivingEntityProcessor(HTPlugin plugin, PluginManager eventCaller, int stackedheal, int stackeddamage)
    {
		this.eventCaller = eventCaller;
		this.plugin = plugin;
		this.stackedheal = stackedheal;		
		this.stackeddamage = stackeddamage;
	}

	//TODO: Add invincibility as a totem effect
	//TODO: Add totem effect that prevents players from interacting with blocks, using items, ect.
    public boolean process(LivingEntity entity, List<Totem> totems)
    {
    	//System.out.println(getEntityName(entity));//DEBUG
    	if((entity instanceof Player))
    	{
    		Player player = (Player) entity;
    		boolean canbehealed = player.hasPermission("healingtotem.heal");
            boolean canbedamaged = player.hasPermission("healingtotem.damage");
            int power = this.sumTotemEffectPower(entity, totems, "HEAL");
            power -= this.sumTotemEffectPower(entity, totems, "DAMAGE");
            if(power > 0 && !canbehealed) power = 0;
            if(power < 0 && !canbedamaged) power = 0;
    			//System.out.println("Power: " + power );//DEBUG
            //TODO: Integrate with that plugin that allows you to transform players into mobs
            //this.applyTransform(entity, totems);
            this.applyHeal(entity, power);
            this.applyPotionEffect(entity, totems);
            this.applyExplosion(entity, totems);
            this.applyFire(entity, totems);
            this.applyLightning(entity, totems);
            return true;
        }else
        {
            int power = this.sumTotemEffectPower(entity, totems, "HEAL");
            power -= this.sumTotemEffectPower(entity, totems, "DAMAGE");
    			//System.out.println("Power: " + power );//DEBUG
            this.applyTransform(entity, totems);
            this.applyPotionEffect(entity, totems);
            this.applyHeal(entity, power);    
            this.applyExplosion(entity, totems);
            this.applyFire(entity, totems);
            this.applyLightning(entity, totems);
            return true;
        }
    }

	protected int sumTotemEffectPower(LivingEntity entity, List<Totem> totems, String effect)
    {
        int power = 0;
		for(Totem totem : totems)
        {                          
			if(totem.inRange(entity))
            { 
				int totemPower = totem.getTotemType().getEffects().getInt(
		                  effect + "." + getEntityName(entity), 0);
				double range = totem.getTotemType().getRange();
				
				//Should these calculations take place here or in the totemtype class?
				if(totem.getTotemType().getGradient().equals("NONE"))
				{
		            power += totemPower;
		            
				}else if(totem.getTotemType().getGradient().equals("LINEAR"))
				{
					power += Math.ceil(-(totemPower/(range)) 
			                  * totem.getDistance(entity) + totemPower);
					
				}else if(totem.getTotemType().getGradient().equals("QUADRATIC"))
				{
					power += Math.ceil(-(totemPower/(range*range)) 
			                  * totem.getDistanceSquared(entity) + totemPower);
					
				}else if(totem.getTotemType().getGradient().equals("EXPONENTIAL"))
				{
					power += Math.ceil(totemPower* Math.pow(2.0,-totem.getDistance(entity)*6/range));
				}
            }
		}
		return power;		
	}
            
	protected void applyHeal(LivingEntity entity, int power)
    {
		if(power > this.stackedheal)
		{
			power = this.stackedheal;
		}else if(power < -this.stackeddamage)
		{
			power = -this.stackeddamage;
		}
		if(power > 0)
		{
			EntityRegainHealthEvent regen = new EntityRegainHealthEvent(
							entity, power, EntityRegainHealthEvent.RegainReason.CUSTOM);
			this.eventCaller.callEvent(regen);
			if(!regen.isCancelled())
			{
				int newhealth = entity.getHealth() + regen.getAmount();
				if(newhealth < 0)
				{
					newhealth = 0;
				}else if(newhealth > 20)
				{
					newhealth = 20;
				}
				entity.setHealth(newhealth);
			}
		}else if(power < 0)
		{
			EntityDamageEvent damage = new EntityDamageEvent(
				entity, EntityDamageEvent.DamageCause.CUSTOM, -power);
			this.eventCaller.callEvent(damage);
			if(!damage.isCancelled())
			{
				entity.damage(damage.getDamage());
			}
		}
	}
            
    protected void applyLightning(LivingEntity entity, List<Totem> totems)
    {
        for(Totem totem : totems)
        {
    		if(totem.inRange(entity) && totem.getTotemType().getEffects().getBoolean(
    				"LIGHTNING." + getEntityName(entity), false ))
            {
                 entity.getWorld().strikeLightning(entity.getLocation());
                 break;
            }
                    
        }
    }
    
    //TODO: Should check against all totems and apply the transform specified by the
    //closest totem.
    protected void applyTransform(Entity entity, List<Totem> totems)
    {
        for(Totem totem : totems)
        {
    		if(totem.inRange(entity) && !totem.getTotemType().getEffects().getString("TRANSFORM" + "." + 
			getEntityName(entity), "").equals(""))
            {          
                Location loc = entity.getLocation();
                entity.remove();
                //make a little smoke, for magic
                loc.getWorld().playEffect(loc, Effect.SMOKE, 0, 20);
                //if the entity specified in the config can be spawned, then replace the current entity with the new one
                if(EntityType.valueOf(totem.getTotemType().getEffects().getString(
                        "TRANSFORM" + "." + getEntityName(entity), "CHICKEN")).isSpawnable())
                {
                    entity = loc.getWorld().spawnEntity(loc, EntityType.valueOf(
                            totem.getTotemType().getEffects().getString(
                            "TRANSFORM" + "." + getEntityName(entity), "CHICKEN"))); 
                    if(entity instanceof Sheep)
                    {  //should make sheep color random. Should.
                        randomcolor[0] = (byte)random.nextInt(16);
                        ((Sheep)entity).setColor(DyeColor.getByWoolData(randomcolor[0]));
                    }
                }
                break;
            }
        }
    }
    
    protected void applyExplosion(LivingEntity entity, List<Totem> totems)
    {
        for(Totem totem : totems)
        {
            int yield = sumTotemEffectPower(entity, totems, "EXPLOSION");
            if(totem.inRange(entity) && yield > 0)
            {
                entity.getWorld().createExplosion(
                        entity.getLocation(), yield);
                break;
            }
        }
    }
    
    protected void applyFire(LivingEntity entity, List<Totem> totems)
    {
        for(Totem totem : totems)
        {
        	if(totem.inRange(entity) && totem.getTotemType().getEffects().getInt("FIRE" + "." + 
        			getEntityName(entity), 0) > 0)
            {
                entity.setFireTicks(sumTotemEffectPower(entity, totems, "FIRE")*20);
                break;
            }
        }
    }

    protected void applyPotionEffect(LivingEntity entity, List<Totem> totems)
    {
        for(Totem totem : totems)
        {
            List<Map<?,?>> nodes = totem.getTotemType().getEffects().getMapList("POTION_EFFECT"+ "." + 
                    getEntityName(entity));
            List<ConfigurationSection> potionTypeNodes = this.plugin.getTotemManager().mapListToConfigSectionList(nodes);                
        	if(totem.inRange(entity) && nodes != null)
            {
        	    for(ConfigurationSection node: potionTypeNodes)
        	    {
        	    
        		PotionEffect potEff = PotionEffectType.getByName(
        				node.getString("TYPE", "HEAL")).createEffect(
        				        node.getInt("DURATION", 0)*20, (node.getInt("POWER", 1) - 1));
        		//DEBUG
//        		System.out.println("Added potion effect " + potEff.getType().getName()+ 
//        				" with amplifier " + potEff.getAmplifier()+" and duration " + 
//        				potEff.getDuration() + " to " + getEntityName(entity));
                entity.addPotionEffect(potEff);
        	    }
            }
        }
    }
    
    public String getEntityName(Entity entity)
	{
		return entity.getClass().getName().substring(43).toUpperCase();
	}
    
}