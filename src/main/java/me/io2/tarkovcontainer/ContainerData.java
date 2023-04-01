package me.io2.tarkovcontainer;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ContainerData {
    public List<ItemStack> stacks;
    public ContainerData(List<ItemStack> stackList) {
        stacks = stackList;
    }
}
