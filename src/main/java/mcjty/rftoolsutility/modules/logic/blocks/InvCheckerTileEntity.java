package mcjty.rftoolsutility.modules.logic.blocks;

import it.unimi.dsi.fastutil.ints.IntSet;
import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.*;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.tileentity.LogicTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.CapabilityTools;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.logic.LogicBlockSetup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;
import static mcjty.rftoolsutility.modules.logic.client.GuiInvChecker.META_MATCH;
import static mcjty.rftoolsutility.modules.logic.client.GuiInvChecker.OREDICT_USE;

public class InvCheckerTileEntity extends LogicTileEntity implements ITickableTileEntity {

    public static final String CMD_SETAMOUNT = "inv.setCounter";
    public static final String CMD_SETSLOT = "inv.setSlot";
    public static final String CMD_SETOREDICT = "inv.setOreDict";
    public static final String CMD_SETMETA = "inv.setUseMeta";

    public static final String CONTAINER_INVENTORY = "container";
    public static final int SLOT_ITEMMATCH = 0;
    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(1) {
        @Override
        protected void setup() {
            slot(SlotDefinition.ghost(), CONTAINER_CONTAINER, SLOT_ITEMMATCH, 154, 24);
            playerSlots(10, 70);
        }
    };

    private NoDirectionItemHander items = createItemHandler();
    private LazyOptional<NoDirectionItemHander> itemHandler = LazyOptional.of(() -> items);
    private LazyOptional<AutomationFilterItemHander> automationItemHandler = LazyOptional.of(() -> new AutomationFilterItemHander(items));

    private LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Inventory Checker")
            .containerSupplier((windowId, player) -> new GenericContainer(LogicBlockSetup.CONTAINER_INVCHECKER.get(), windowId, CONTAINER_FACTORY, getPos(), InvCheckerTileEntity.this))
            .itemHandler(itemHandler));

    private int amount = 1;
    private int slot = 0;
    private boolean oreDict = false;
    private boolean useMeta = false;
    private IntSet set1 = null;

    private int checkCounter = 0;

    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, 1);

    public InvCheckerTileEntity() {
        super(LogicBlockSetup.TYPE_INVCHECKER.get());
    }

    public static LogicSlabBlock createBlock() {
        return new LogicSlabBlock(new BlockBuilder()
                .topDriver(RFToolsUtilityTOPDriver.DRIVER)
                .info(key("message.rftoolsutility.shiftmessage"))
                .infoShift(header())
                .tileEntitySupplier(InvCheckerTileEntity::new));
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
        markDirtyClient();
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
        markDirtyClient();
    }

    public boolean isOreDict() {
        return oreDict;
    }

    public void setOreDict(boolean oreDict) {
        this.oreDict = oreDict;
        markDirtyClient();
    }

    public boolean isUseMeta() {
        return useMeta;
    }

    public void setUseMeta(boolean useMeta) {
        this.useMeta = useMeta;
        markDirtyClient();
    }

    @Override
    public void tick() {
        if (world.isRemote) {
            return;
        }

        checkCounter--;
        if (checkCounter > 0) {
            return;
        }
        checkCounter = 10;

        setRedstoneState(checkOutput() ? 15 : 0);
    }

    public boolean checkOutput() {
        Direction inputSide = getFacing(world.getBlockState(getPos())).getInputSide();
        BlockPos inputPos = getPos().offset(inputSide);
        TileEntity te = world.getTileEntity(inputPos);
        if (InventoryHelper.isInventory(te)) {
            return CapabilityTools.getItemCapabilitySafe(te).map(capability -> {
                ItemStack stack = capability.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    int nr = isItemMatching(stack);
                    return nr >= amount;
                }
                return false;
            }).orElse(false);
        }
        return false;
    }

    private int isItemMatching(ItemStack stack) {
        int nr = 0;
        ItemStack matcher = inventoryHelper.getStackInSlot(0);
        if (!matcher.isEmpty()) {
            if (oreDict) {
                if (isEqualForOredict(matcher, stack)) {
                    if ((!useMeta) || matcher.getDamage() == stack.getDamage()) {
                        nr = stack.getCount();
                    }
                }
            } else {
                if (useMeta) {
                    if (matcher.isItemEqual(stack)) {
                        nr = stack.getCount();
                    }
                } else {
                    if (matcher.getItem() == stack.getItem()) {
                        nr = stack.getCount();
                    }
                }
            }
        } else {
            nr = stack.getCount();
        }
        return nr;
    }

    // @todo 1.15 oredict? Use tags?
    private boolean isEqualForOredict(ItemStack s1, ItemStack s2) {
//        if (set1 == null) {
//            int[] oreIDs1 = OreDictionary.getOreIDs(s1);
//            set1 = new TIntHashSet(oreIDs1);
//        }
//        if (set1.isEmpty()) {
//            // The first item is not an ore. In this case we do normal equality of item
//            return s1.getItem() == s2.getItem();
//        }
//
//        int[] oreIDs2 = OreDictionary.getOreIDs(s2);
//        if (oreIDs2.length == 0) {
//            // The first is an ore but this isn't. So we cannot match.
//            return false;
//        }
//        TIntSet set2 = new TIntHashSet(oreIDs2);
//        set2.retainAll(set1);
//        return !set2.isEmpty();
        return false;
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        powerOutput = tagCompound.getBoolean("rs") ? 15 : 0;
    }

    @Override
    public void readInfo(CompoundNBT tagCompound) {
        super.readInfo(tagCompound);
        CompoundNBT info = tagCompound.getCompound("Info");
        amount = info.getInt("amount");
        slot = info.getInt("slot");
        oreDict = info.getBoolean("oredict");
        useMeta = info.getBoolean("useMeta");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        tagCompound.putBoolean("rs", powerOutput > 0);
        return tagCompound;
    }

    @Override
    public void writeInfo(CompoundNBT tagCompound) {
        super.writeInfo(tagCompound);
        CompoundNBT info = getOrCreateInfo(tagCompound);
        info.putInt("amount", amount);
        info.putInt("slot", slot);
        info.putBoolean("oredict", oreDict);
        info.putBoolean("useMeta", useMeta);
    }

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_SETMETA.equals(command)) {
            setUseMeta(META_MATCH.equals(params.get(ChoiceLabel.PARAM_CHOICE)));
            return true;
        } else if (CMD_SETOREDICT.equals(command)) {
            setOreDict(OREDICT_USE.equals(params.get(ChoiceLabel.PARAM_CHOICE)));
            return true;
        } else if (CMD_SETSLOT.equals(command)) {
            int slot;
            try {
                slot = Integer.parseInt(params.get(TextField.PARAM_TEXT));
            } catch (NumberFormatException e) {
                slot = 0;
            }
            setSlot(slot);
            return true;
        } else if (CMD_SETAMOUNT.equals(command)) {
            int amount;
            try {
                amount = Integer.parseInt(params.get(TextField.PARAM_TEXT));
            } catch (NumberFormatException e) {
                amount = 1;
            }
            setAmount(amount);
            return true;
        }
        return false;
    }

    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(this, CONTAINER_FACTORY) {

            @Override
            protected void onUpdate(int index) {
                super.onUpdate(index);
                // Clear the oredict cache
                set1 = null;
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return false;
            }

            @Override
            public boolean isItemInsertable(int slot, @Nonnull ItemStack stack) {
                return isItemValid(slot, stack);
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return automationItemHandler.cast();
        }
        if (cap == CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY) {
            return screenHandler.cast();
        }
        return super.getCapability(cap, facing);
    }
}