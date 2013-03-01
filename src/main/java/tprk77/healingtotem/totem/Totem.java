package tprk77.healingtotem.totem;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import tprk77.util.structure.Structure;

/**
 * A Totem Pole, represented by it's TotemType and owner
 *
 * Originally written by tim, modified by Aaron Yeager
 */
public class Totem extends Structure {

	private final TotemType totemtype;
        private String owner;

        //if there is no owner, the owner is set to null
	public Totem(TotemType totemtype, Block block)
        {
		this(totemtype, block, null);
	}

	public Totem(TotemType totemtype, Block block, String owner)
        {
		super(totemtype.getAllStructureTypes(), block);
		this.totemtype = totemtype;
		this.owner = owner;
	}

	public TotemType getTotemType()
        {
		return this.totemtype;
	}
        
        public String getOwner()
        {
		return this.owner;
	}

    public double getDistance(Entity entity)
    {
		try{
			return Math.sqrt(this.getRootBlock().getLocation().distanceSquared(
					entity.getLocation()));
		}catch(IllegalArgumentException ex){
			return Double.MAX_VALUE;
		}
    }
    
    public double getDistanceSquared(Entity entity)
    {
		try{
			return this.getRootBlock().getLocation().distanceSquared(
					entity.getLocation());
		}catch(IllegalArgumentException ex){
			return Double.MAX_VALUE;
		}
    }
        
	public boolean inRange(Entity entity)
        {
		try{
			double range = this.totemtype.getRange();
			return this.getRootBlock().getLocation().distanceSquared(
							entity.getLocation()) < (range * range);
		}catch(IllegalArgumentException ex){
			return false;
		}
	}

        /* removed due to being unecessary at this time. May reimplement later - Aaron
	private boolean isPowered(){
		for(Block block : this.blocks){
			if(block.isBlockPowered() || block.isBlockIndirectlyPowered()){
				return true;
			}
		}
		return false;
	}
          */
         
}
