package com.raoulvdberge.refinedstorage.api.network;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * Represents a node in the network.
 */
public interface INetworkNode {
    /**
     * @return the energy usage of this node
     */
    int getEnergyUsage();

    /**
     * @return the item of the node
     */
    @Nonnull
    ItemStack getItemStack();

    /**
     * Called when this node is connected to a network.
     *
     * @param network the network
     */
    void onConnected(INetworkMaster network);

    /**
     * Called when this node is disconnected from a network.
     *
     * @param network the network
     */
    void onDisconnected(INetworkMaster network);

    /**
     * @return true if this node can be treated as active, typically checks the redstone configuration
     */
    boolean canUpdate();

    /**
     * @return the network
     */
    INetworkMaster getNetwork();
}
