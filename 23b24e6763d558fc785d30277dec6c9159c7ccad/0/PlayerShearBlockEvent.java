package io.papermc.paper.event.block;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Called when a player uses sheers on a block.
 * <p>
 * This event is <b>not</b> called when breaking blocks with shears but instead only when a
 * player uses the sheer item on a block to garner drops from said block and/or change its state.
 * <p>
 * Examples include shearing a pumpkin to turn it into a carved pumpkin or shearing a beehive to get honeycomb.
 */
public class PlayerShearBlockEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private final Block block;
    private final ItemStack item;
    private final EquipmentSlot hand;
    private final List<ItemStack> drops;

    public PlayerShearBlockEvent(@NotNull Player who, @NotNull Block block, @NotNull ItemStack item, @NotNull EquipmentSlot hand, @NotNull List<ItemStack> drops) {
        super(who);
        this.block = block;
        this.item = item;
        this.hand = hand;
        this.drops = drops;
    }

    /**
     * Gets the block being sheared in this event.
     *
     * @return The {@link Block} which block is being sheared in this event.
     */
    @NotNull
    public Block getBlock() {
        return block;
    }

    /**
     * Gets the item used to shear the block.
     *
     * @return The {@link ItemStack} of the shears.
     */
    @NotNull
    public ItemStack getItem() {
        return item;
    }

    /**
     * Gets the hand used to shear the block.
     *
     * @return Either {@link EquipmentSlot#HAND} OR {@link EquipmentSlot#OFF_HAND}.
     */
    @NotNull
    public EquipmentSlot getHand() {
        return hand;
    }

    /**
     * Gets the resulting drops of this event.
     *
     * @return A {@link List list} of {@link ItemStack items} that will be dropped as result of this event.
     */
    @NotNull
    public List<ItemStack> getDrops() {
        return drops;
    }

    /**
     * Gets whether the shearing of the block should be cancelled or not.
     *
     * @return Whether the shearing of the block should be cancelled or not.
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets whether the shearing of the block should be cancelled or not.
     *
     * @param cancel whether the shearing of the block should be cancelled or not.
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
