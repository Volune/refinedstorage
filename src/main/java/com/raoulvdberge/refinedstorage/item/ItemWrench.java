package com.raoulvdberge.refinedstorage.item;

import com.raoulvdberge.refinedstorage.api.util.IWrenchable;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemWrench extends ItemBase {
    private enum WrenchMode {
        ROTATION(0),
        CONFIGURATION(1);

        private final int id;

        WrenchMode(int id) {
            this.id = id;
        }

        public WrenchMode cycle() {
            return this == ROTATION ? CONFIGURATION : ROTATION;
        }

        public NBTTagCompound writeToNBT(NBTTagCompound tag) {
            tag.setInteger(NBT_WRENCH_MODE, id);

            return tag;
        }

        public static WrenchMode readFromNBT(NBTTagCompound tag) {
            if (tag != null && tag.hasKey(NBT_WRENCH_MODE)) {
                int id = tag.getInteger(NBT_WRENCH_MODE);

                for (WrenchMode mode : values()) {
                    if (mode.id == id) {
                        return mode;
                    }
                }
            }

            return ROTATION;
        }
    }

    private static final String NBT_WRENCH_MODE = "WrenchMode";
    private static final String NBT_WRENCHED_DATA = "WrenchedData";
    private static final String NBT_WRENCHED_TILE = "WrenchedTile";

    public ItemWrench() {
        super("wrench");

        setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote || !player.isSneaking()) {
            return EnumActionResult.PASS;
        }

        WrenchMode mode = WrenchMode.readFromNBT(stack.getTagCompound());

        if (mode == WrenchMode.ROTATION) {
            Block block = world.getBlockState(pos).getBlock();

            block.rotateBlock(world, pos, player.getHorizontalFacing().getOpposite());

            return EnumActionResult.SUCCESS;
        } else if (mode == WrenchMode.CONFIGURATION) {
            TileEntity tile = world.getTileEntity(pos);

            if (tile instanceof IWrenchable) {
                IWrenchable wrenchable = (IWrenchable) tile;

                boolean canWrite = false;

                if (stack.hasTagCompound() && stack.getTagCompound().hasKey(NBT_WRENCHED_DATA) && stack.getTagCompound().hasKey(NBT_WRENCHED_TILE)) {
                    NBTTagCompound wrenchedData = stack.getTagCompound().getCompoundTag(NBT_WRENCHED_DATA);
                    String wrenchedTile = stack.getTagCompound().getString(NBT_WRENCHED_TILE);

                    if (wrenchable.getClass().getName().equals(wrenchedTile)) {
                        wrenchable.readConfiguration(wrenchedData);

                        tile.markDirty();

                        player.sendMessage(new TextComponentTranslation("item.refinedstorage:wrench.read"));
                    } else {
                        canWrite = true;
                    }
                } else {
                    canWrite = true;
                }

                if (canWrite) {
                    stack.getTagCompound().setString(NBT_WRENCHED_TILE, wrenchable.getClass().getName());
                    stack.getTagCompound().setTag(NBT_WRENCHED_DATA, wrenchable.writeConfiguration(new NBTTagCompound()));

                    player.sendMessage(new TextComponentTranslation("item.refinedstorage:wrench.saved"));
                }

                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote && !player.isSneaking()) {
            WrenchMode mode = WrenchMode.readFromNBT(stack.getTagCompound());

            stack.setTagCompound(new NBTTagCompound());

            WrenchMode next = mode.cycle();

            next.writeToNBT(stack.getTagCompound());

            player.sendMessage(new TextComponentTranslation(
                "item.refinedstorage:wrench.mode",
                new TextComponentTranslation("item.refinedstorage:wrench.mode." + next.id).setStyle(new Style().setColor(TextFormatting.YELLOW))
            ));
        }

        return super.onItemRightClick(stack, world, player, hand);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);

        WrenchMode mode = WrenchMode.readFromNBT(stack.getTagCompound());

        tooltip.add(I18n.format("item.refinedstorage:wrench.mode", TextFormatting.YELLOW + I18n.format("item.refinedstorage:wrench.mode." + mode.id) + TextFormatting.RESET));
    }
}
