package com.iCo6.system;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.iCo6.Constants;
import com.iCo6.iConomy;
import com.iCo6.util.Messaging;
import com.iCo6.util.Template;

public class Interest extends TimerTask {
    Template template = null;

    public Interest(String directory) {
        template = new Template(directory, "Template.yml");
    }

    @Override
    public void run() {
        Accounts Accounts = new Accounts();
        DecimalFormat DecimalFormat = new DecimalFormat("#.##");
        List<String> players = new ArrayList<String>();
        LinkedHashMap<String, HashMap<String, Object>> queries = new LinkedHashMap<String, HashMap<String, Object>>();

        if(Constants.Nodes.InterestOnline.getBoolean()) {
            Collection<? extends Player> player = iConomy.Server.getOnlinePlayers();
            
            for(Player p : player) {
                players.add(p.getName());
            }
        } else {
            players.addAll(Queried.accountList());
        }

        double cutoff = Constants.Nodes.InterestCutoff.getDouble(),
               percentage = Constants.Nodes.InterestPercentage.getDouble(),
               min = Constants.Nodes.InterestMin.getDouble(),
               max = Constants.Nodes.InterestMax.getDouble(),
               amount = 0.0;
        
        String table = Constants.Nodes.DatabaseTable.toString();
        String query = "UPDATE " + table + " SET balance = ? WHERE username = ?";

        if(percentage == 0.0){
            try {
                if(min != max)
                    amount = Double.valueOf(DecimalFormat.format(Math.random() * (max - min) + (min)));
                else {
                    amount = max;
                }
            } catch (NumberFormatException e) {
                amount = max;
            }
        }

        for(String name: players) {
            if(!Accounts.exists(name))
                continue;

            Account account = new Account(name);
            Double balance = account.getHoldings().getBalance();

            if(cutoff > 0.0)
                if(balance >= cutoff)
                    continue;
            else if(cutoff < 0.0)
                if(balance <= cutoff)
                    continue;

            LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();

            if(percentage != 0.0)
                amount = Double.valueOf(DecimalFormat.format((percentage * balance)/100));

            data.put("original", balance);
            data.put("balance", (balance + amount));
            
            if(Constants.Nodes.InterestAnnounce.getBoolean()) {
            	template.set(Template.Node.INTEREST_ANNOUNCEMENT);
                template.add("amount", iConomy.format(amount));

                Messaging.send(Bukkit.getPlayer(name), template.parse());
            }
            
            queries.put(name, data);
        }

        if(queries.isEmpty())
            return;

        Queried.doInterest(query, queries);
    }
}
