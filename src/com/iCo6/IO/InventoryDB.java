package com.iCo6.IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.iCo6.Constants;
import com.iCo6.iConomy;
import com.iCo6.util.nbt.ByteTag;
import com.iCo6.util.nbt.CompoundTag;
import com.iCo6.util.nbt.ListTag;
import com.iCo6.util.nbt.NBTInputStream;
import com.iCo6.util.nbt.NBTOutputStream;
import com.iCo6.util.nbt.ShortTag;
import com.iCo6.util.nbt.StringTag;
import com.iCo6.util.nbt.Tag;

/**
 * Controls inventory for monetary use.
 *
 * @author SpaceManiac
 * @author Nijikokun
 * @author RasTaIARI
 */
@SuppressWarnings("deprecation")
public class InventoryDB {

    private File dataDir;

    public InventoryDB() {
        dataDir = new File(iConomy.Server.getWorlds().get(0).getName(), "players");
    }

    public List<String> getAllPlayers() {
        ArrayList<String> result = new ArrayList<String>();

        for (String file : dataDir.list())
            if (file.endsWith(".dat"))
                result.add(file.substring(0, file.length() - 4));

        return result;
    }

    public boolean dataExists(String name) {
        return new File(dataDir, name + ".dat").exists();
    }

	public void setBalance(String name, double balance) {
        if (iConomy.Server.getPlayer(name) != null && iConomy.Server.getPlayer(name).isOnline()) {

            ItemStack[] stacks = iConomy.Server.getPlayer(name).getInventory().getContents().clone();
            setBalance(stacks, balance);

            iConomy.Server.getPlayer(name).getInventory().setContents(stacks);
            iConomy.Server.getPlayer(name).updateInventory();
            return;
        }

        if (!dataExists(name))
            return;

        ItemStack[] stacks = readInventory(name);
        if (stacks != null) {
            setBalance(stacks, balance);
            writeInventory(name, stacks);
        }
        
    }

    public double getBalance(String name) {
        if (iConomy.Server.getPlayer(name) != null && iConomy.Server.getPlayer(name).isOnline()) {
            return getBalance(iConomy.Server.getPlayer(name).getInventory().getContents());
        } else {
            ItemStack[] stacks = readInventory(name);
            if (stacks != null) {
                return getBalance(stacks);
            } else {
                return Constants.Nodes.Balance.getDouble();
            }
        }
    }

	private ItemStack[] readInventory(String name) {
        try {
            NBTInputStream in = new NBTInputStream(new FileInputStream(new File(dataDir, name + ".dat")));
            CompoundTag tag = (CompoundTag) in.readTag();
            in.close();
            
			ListTag inventory = (ListTag) tag.getValue().get("Inventory");

            ItemStack[] stacks = new ItemStack[40];
            for (int i = 0; i < inventory.getValue().size(); ++i) {
                CompoundTag item = (CompoundTag) inventory.getValue().get(i);
                byte count = ((ByteTag) item.getValue().get("Count")).getValue();
                byte slot = ((ByteTag) item.getValue().get("Slot")).getValue();
                short damage = ((ShortTag) item.getValue().get("Damage")).getValue();
                String id = (String) (item.getValue().get("id")).getValue();

                //stacks[slot] = new ItemStack(Material.valueOf(id), count, damage);
                stacks[slot] = new ItemStack(Material.valueOf(id), count);                
            }
            return stacks;
        } catch (IOException ex) {
            iConomy.Server.getLogger().log(Level.WARNING, "[iCo/InvDB] error reading inventory {0}: {1}", new Object[]{name, ex.getMessage()});
            return null;
        }
    }

	private void writeInventory(String name, ItemStack[] stacks) {
        try {
            NBTInputStream in = new NBTInputStream(new FileInputStream(new File(dataDir, name + ".dat")));
            CompoundTag tag = (CompoundTag) in.readTag();
            in.close();

            ArrayList<Tag> tagList = new ArrayList<Tag>();

            for (int i = 0; i < stacks.length; ++i) {
                if (stacks[i] == null) continue;

                ByteTag count = new ByteTag("Count", (byte) stacks[i].getAmount());
                ByteTag slot = new ByteTag("Slot", (byte) i);
                ShortTag damage = new ShortTag("Damage", stacks[i].getDurability());
                //ShortTag id = new ShortTag("id", (short) stacks[i].getTypeId());

                StringTag id = new StringTag("id", stacks[i].getType().name());
                
                HashMap<String, Tag> tagMap = new HashMap<String, Tag>();
                tagMap.put("Count", count);
                tagMap.put("Slot", slot);
                tagMap.put("Damage", damage);
                tagMap.put("id", id);

                tagList.add(new CompoundTag("", tagMap));
            }

			ListTag inventory = new ListTag("Inventory", CompoundTag.class, tagList);

            HashMap<String, Tag> tagCompound = new HashMap<String, Tag>(tag.getValue());
            tagCompound.put("Inventory", inventory);
            tag = new CompoundTag("Player", tagCompound);

            NBTOutputStream out = new NBTOutputStream(new FileOutputStream(new File(dataDir, name + ".dat")));
            out.writeTag(tag);
            out.close();
        } catch (IOException ex) {
            iConomy.Server.getLogger().log(Level.WARNING, "[iCo/InvDB] error writing inventory {0}: {1}", new Object[]{name, ex.getMessage()});
        }
    }

	private void setBalance(ItemStack[] contents, double balance) {
        Material major = Material.valueOf((String) Constants.Nodes.DatabaseMajorItem.getValue());
        Material minor = Material.valueOf((String) Constants.Nodes.DatabaseMinorItem.getValue());

        // Remove all existing items
        for (int i = 0; i < contents.length; ++i) {
            ItemStack item = contents[i];

            if (item != null)
                if (item.getType().equals(major) || item.getType().equals(minor))
                    contents[i] = null;
        }

        // Re-add balance to inventory
        for (int i = 0; i < contents.length; ++i) {
            if (contents[i] == null) {
                if (balance >= 1) {
                    int add = (int) balance;
                    if (add > major.getMaxStackSize())
                        add = major.getMaxStackSize();

                    contents[i] = new ItemStack(major, add);
                    balance -= add;
                } else if (balance >= 0.01) {
                    int add = (int) (roundTwoDecimals(balance) * 100);

                    if (add > minor.getMaxStackSize())
                        add = minor.getMaxStackSize();

                    contents[i] = new ItemStack(minor, add);
                    balance -= 0.01 * add;
                } else {
                    balance = 0;
                    break;
                }
            }
        }

        // Make sure nothing is left.
        if (balance > 0)
            throw new RuntimeException("Unable to set balance, inventory is overfull");
    }

	private double getBalance(ItemStack[] contents) {
        double balance = 0;
        /*int major = Constants.Nodes.DatabaseMajorItem.getInteger();
        int minor = Constants.Nodes.DatabaseMinorItem.getInteger();*/
        Material major = Material.valueOf((String) Constants.Nodes.DatabaseMajorItem.getValue());
        Material minor = Material.valueOf((String) Constants.Nodes.DatabaseMinorItem.getValue());

        for (ItemStack item : contents)
            if (item != null)
                if (item.getType().equals(major))
                    balance += item.getAmount();
                else if (item.getType().equals(minor))
                    balance += 0.01 * item.getAmount();

        return balance;
    }

    private double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }
}