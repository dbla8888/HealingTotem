package tprk77.healingtotem;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HTCommandExecutor implements CommandExecutor 
{
	HTPlugin plugin;
	HTHealerRunnable healerRunnable;
	HTTotemManager totemManager;
	
	public HTCommandExecutor(HTPlugin plugin)
	{
		this.plugin = plugin;
		this.healerRunnable = plugin.getHealerRunnable();
		this.totemManager = plugin.getTotemManager();
	}
	
	public boolean onCommand(CommandSender cs, Command cmnd, String alias, String[] args) 
	{	
		//Code has shown to cause sneaky issues, be on your guard.
		if(cmnd.getName().equals("HTReload"))
		{
            if (args.length > 0) 
            {//if there are any arguments, do nothing
            	//TODO: Should send meaningful error message.
                return false;
            }
            
            if (cs instanceof Player)//if the command was sent by a player
            {
                Player player = (Player)cs;
                
                if (player.hasPermission("healingtotem.reload")) 
                {
                    //unschedule the runnable so we don't have
                    //issues with the runnable calling unloaded
                    //totem types
                    healerRunnable.cancel();
                    //save what we have to avoid consistency issues
                    totemManager.saveTotems();
                    //reload stuff
                    plugin.reloadConfig();
                    totemManager.loadTotemTypesOrDefault();
                    totemManager.loadTotems();
                    //reschedule the runnable
                    healerRunnable.schedule();
                    player.sendMessage(ChatColor.RED +
                            "HealingTotem reloaded");
                    
                }else 
                {
                    player.sendMessage(ChatColor.RED + 
                            "You do not have permission to use this command");
                }
            }else //if the sender is not a player(e.g. the console)
            {
                if(healerRunnable != null)
                {healerRunnable.cancel();}
                if(totemManager != null)
                {totemManager.saveTotems();}
                
                plugin.reloadConfig();
                totemManager.loadTotemTypesOrDefault();
                totemManager.loadTotems();
                healerRunnable.schedule();
                plugin.log("HealingTotem Reloaded");
            }            
            return true;
		}
		return false;
	}

}
