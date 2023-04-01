package me.io2.tarkovcontainer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ipvp.canvas.slot.ClickOptions;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.type.ChestMenu;

import java.util.*;

public class ContainerListener implements Listener {
    public static final List<UUID> yamiotiPlayerList = new ArrayList<>();
    public static final HashMap<UUID, ContainerData> containerDataHashMap = new HashMap<>();
    private final int[] yamiotiSlot = new int[]{12, 13, 14, 21, 22, 23, 30, 31, 32};
    private final int[] normalSlot = new int[]{12, 13, 21, 22};
    private final boolean debug = false;

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        int[] slotList = yamiotiPlayerList.contains(event.getPlayer().getUniqueId()) ? yamiotiSlot : normalSlot;

        if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.BLAZE_POWDER && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            ContainerData containerData = containerDataHashMap.get(event.getPlayer().getUniqueId());
            ChestMenu container = ChestMenu.builder(5).title("Container").build();

            for (int i = 0; i < 45; i++) {
                if (Collections.singletonList(slotList).contains(i)) continue;

                Slot slot = container.getSlot(i);
                ItemStack itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(" ");
                itemStack.setItemMeta(itemMeta);
                slot.setItem(itemStack);
                slot.setClickOptions(ClickOptions.DENY_ALL);
                slot.setClickHandler((player, clickInformation) -> {
                    if (!clickInformation.isAddingItem()) return;

                    String name = clickInformation.getAddingItem().getType().name();
                    for (String s : TarkovContainer.itemBlackList) {
                        if (name.equalsIgnoreCase(s)) {
                            clickInformation.setResult(Event.Result.DENY);
                            break;
                        }
                    }
                });
            }

            for (int i = 0; i < slotList.length; i++) {
                Slot slot = container.getSlot(slotList[i]);
                slot.setClickOptions(ClickOptions.ALLOW_ALL);
                int xd = i;
                if (!yamiotiPlayerList.contains(event.getPlayer().getUniqueId())) {
                    if (i > 1) {
                        xd = i + 1;
                    }
                }

                ItemStack itemStack = containerData.stacks.get(xd);
                if (itemStack.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                    slot.setItem(itemStack);
                    if (debug) {
                        Bukkit.broadcastMessage("Data is not gray_stained_glass_pane. setting!");
                    }
                }
            }

            container.setCloseHandler((a, b) -> {
                List<ItemStack> stackList = new ArrayList<>();
                for (int i : yamiotiSlot) {
                    ItemStack item = a.getOpenInventory().getItem(i);
                    if (item == null)
                        item = new ItemStack(Material.AIR);

                    stackList.add(item);
                }
                containerDataHashMap.replace(a.getUniqueId(), new ContainerData(stackList));
            });

            container.open(event.getPlayer());
        }
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        if (TarkovContainer.getPlugin().getConfig().getKeys(false).contains(event.getPlayer().getUniqueId().toString())) {
            List<ItemStack> stackList = new ArrayList<>();
            for (int i = 0; i < 10 ; i++) {
                stackList.add(TarkovContainer.getPlugin().getConfig().getItemStack(event.getPlayer().getUniqueId() + "." + i));
            }

            if (TarkovContainer.getPlugin().getConfig().getBoolean(event.getPlayer().getUniqueId() + ".yamioti", false))
                yamiotiPlayerList.add(event.getPlayer().getUniqueId());

            containerDataHashMap.put(event.getPlayer().getUniqueId(), new ContainerData(stackList));
        } else {
            List<ItemStack> stackList = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                stackList.add(new ItemStack(Material.AIR));
            }

            containerDataHashMap.put(event.getPlayer().getUniqueId(), new ContainerData(stackList));
        }
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent event) {
        ContainerData containerData = containerDataHashMap.get(event.getPlayer().getUniqueId());
        int stackPos = 0;
        for (ItemStack stack : containerData.stacks) {
            TarkovContainer.getPlugin().getConfig().set(event.getPlayer().getUniqueId() + "." + stackPos, stack);
            stackPos += 1;
        }

        boolean contains = yamiotiPlayerList.contains(event.getPlayer().getUniqueId());
        TarkovContainer.getPlugin().getConfig().set(event.getPlayer().getUniqueId() + ".yamioti", contains);
        if (contains) {
            yamiotiPlayerList.remove(event.getPlayer().getUniqueId());
        }
    }
}
