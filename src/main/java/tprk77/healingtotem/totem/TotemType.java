package tprk77.healingtotem.totem;

import java.util.List;
import tprk77.util.structure.Rotator;
import tprk77.util.structure.StructureType;
import org.bukkit.configuration.ConfigurationSection;

/**
 * An immutable type representing totem types.
 *
 * @author tim
 */
public final class TotemType {

	private final String name;             
	private final double range;
	private final StructureType structuretype;
    private final ConfigurationSection effects;        
	private final List<StructureType> rotatedstructuretypes;
	private final Rotator rotator;
	private final String gradient;

	public TotemType(String name, ConfigurationSection effects, double range, 
			StructureType structuretype)
        {
		this(name, effects, range, structuretype, Rotator.NONE, "NONE");
	}

	public TotemType(String name, ConfigurationSection effects, 
			double range, StructureType structuretype, Rotator rotator, String gradient)
    {
        this.name = name;
        this.effects = effects;
		this.range = range;
		this.gradient = gradient;
		this.structuretype = structuretype;
		this.rotatedstructuretypes = structuretype.makeRotatedStructureTypes(rotator);
		this.rotator = rotator;
	}

	public String getName()
    {
		return this.name;
	}
        
    public ConfigurationSection getEffects()
    {
        return this.effects;
    }

	public double getRange()
    {
		return this.range;
	}

	public String getGradient()
	{
		return this.gradient;
	}
	
	public StructureType getStructureType()
    {
		return this.structuretype;
	}

	public List<StructureType> getAllStructureTypes()
    {
		return this.rotatedstructuretypes;
	}

	public Rotator getRotator()
    {
		return this.rotator;
	}

	@Override
	public String toString()
    {
		return "totemtype { name: " + this.name + ", effects: " + this.effects +
						", range: " + this.range + ", number of structures: " +
						this.rotatedstructuretypes.size() + " }";
	}
}
