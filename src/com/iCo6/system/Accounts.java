package com.iCo6.system;

import java.util.List;
import java.util.UUID;

import com.iCo6.Constants;

public class Accounts {
    public Accounts() { }

    public boolean exists(String name,UUID uuid) {
    	if(Queried.hasAccount(name)) {
    		System.out.println(name + " has an account");
    		if(!Queried.hasUUID(uuid)) {
    			System.out.println("adding uuid for " + name);
    			//Es gibt keinen User mit der uuid
        		Queried.insertAccountUUID(name, uuid,Queried.getPID(name));
        	}else {
        		System.out.println("setting name for " + uuid + " to " + name);
        		//Es gibt einen User mit der uuid,update den letzten Nutzername
        		Queried.updateAccountUUID(name, uuid,Queried.getPID(uuid));
        	}
    	}else if(Queried.hasUUID(uuid)) {
    		System.out.println(name + " changed his name!");
    		Queried.updateAccountUUID(name, uuid,Queried.getPID(uuid));
    	}
        return Queried.hasAccount(name);
    }
    /**
     * Old methode, here so it wont break vault/other plugins
     * @param name
     * @return
     */
    public boolean exists(String name) {
        return Queried.hasAccount(name);
    }

    public Account get(String name,UUID uuid) {
    	//Falls kein User mit dem Namen und der UUID existiert -> neu erstellen
        //if(!Queried.hasAccount(name) && !Queried.hasUUID(uuid)) {
    	if(!exists(name, uuid)) {
        	this.create(name,uuid);
        }
        return new Account(name,uuid);
    }
    public Account get(String name) {
        return new Account(name);
    }

    public List<Account> getTopAccounts(int amount) {
        return Queried.topAccounts(amount);
    }

    public boolean create(String name,UUID uuid) {
        //return create(name, Constants.Nodes.Balance.getDouble());
        boolean created = create(name, Constants.Nodes.Balance.getDouble());
        
        Queried.insertAccountUUID(name, uuid, Queried.getPID(name))
;        
        storeUUID(name, uuid);
        return created;
    }

    public boolean updateUUID(String name,UUID uuid) {
        boolean created = create(name, Constants.Nodes.Balance.getDouble());
        storeUUID(name, uuid);
        return created;
    }

    public boolean create(String name, Double balance) {
        return create(name, balance, 0);
    }
    
    public boolean storeUUID(String name,UUID uuid){
    	return Queried.updateAccountUUID(name, uuid,Queried.getPID(name));
    }
    
    public boolean hasUUIDStored(String name,UUID uuid) {
    	return Queried.hasUUID(uuid);
    }

    public boolean create(String name, Double balance, Integer status) {
        if(!Queried.hasAccount(name))
            return Queried.createAccount(name, balance, status);

        return false;
    }

    public boolean remove(String... name) {
        Boolean success = false;

        for(String n: name)
            if(Queried.hasAccount(n))
                success = Queried.removeAccount(n);

        return success;
    }

    public void purge() {
        Queried.purgeDatabase();
    }

    public void empty() {
        Queried.emptyDatabase();
    }
}