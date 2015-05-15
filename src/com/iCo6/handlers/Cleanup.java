package com.iCo6.handlers;

import java.util.LinkedHashMap;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.iCo6.iConomy;
import com.iCo6.command.Handler;
import com.iCo6.command.Parser.Argument;
import com.iCo6.command.exceptions.InvalidUsage;
import com.iCo6.system.Queried;

public class Cleanup extends Handler {

    public Cleanup(iConomy plugin) {
        super(plugin, iConomy.Template);
    }

    @Override
    public boolean perform(CommandSender sender, LinkedHashMap<String, Argument> arguments) throws InvalidUsage {
        if(!hasPermissions(sender, "cleanup")){
			template.noPermission(sender);
			return false;
        }

		Double amount;
		
        try {
        	amount = arguments.get("amount").getDoubleValue();
        } catch(NumberFormatException e) {
            throw new InvalidUsage("Invalid <white>amount<rose>, must be double.");
        }
        
        Queried.cleanDatabase((Player)sender, amount);

        return false;
    }
}
