package com.iCo6.handlers;

import java.util.LinkedHashMap;

import org.bukkit.command.CommandSender;

import com.iCo6.iConomy;
import com.iCo6.command.Handler;
import com.iCo6.command.Parser.Argument;
import com.iCo6.command.exceptions.InvalidUsage;
import com.iCo6.system.Accounts;
import com.iCo6.util.Messaging;
import com.iCo6.util.Template;

public class Remove extends Handler {
    private Accounts Accounts = new Accounts();

    public Remove(iConomy plugin) {
        super(plugin, iConomy.Template);
    }

    @Override
    public boolean perform(CommandSender sender, LinkedHashMap<String, Argument> arguments) throws InvalidUsage {
        if(!hasPermissions(sender, "remove")) {
			template.noPermission(sender);
			return false;
        }

        String name = arguments.get("name").getStringValue();
        String tag = template.color(Template.Node.TAG_MONEY);

        if(name.equals("0"))
            throw new InvalidUsage("Missing <white>name<rose>: /money remove <name>");

        if(!Accounts.existsOldMethode(name)) {
            template.set(Template.Node.ERROR_ACCOUNT);
            template.add("name", name);
            Messaging.send(sender, tag + template.parse());
            return false;
        }

        if(!Accounts.remove(name)) {
            template.set(Template.Node.ERROR_CREATE);
            template.add("name", name);
            Messaging.send(sender, tag + template.parse());
            return false;
        }

        template.set(Template.Node.ACCOUNTS_REMOVE);
        template.add("name", name);
        Messaging.send(sender, tag + template.parse());
        return false;
    }
}
