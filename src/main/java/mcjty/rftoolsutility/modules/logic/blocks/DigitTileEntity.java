package mcjty.rftoolsutility.modules.logic.blocks;


import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.tileentity.LogicSupport;
import mcjty.rftoolsutility.compat.RFToolsUtilityTOPDriver;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class DigitTileEntity extends GenericTileEntity {

    private final LogicSupport support = new LogicSupport();

    public DigitTileEntity() {
        super(LogicBlockModule.TYPE_DIGIT.get());
    }

    public static LogicSlabBlock createBlock() {
        return new LogicSlabBlock(new BlockBuilder()
                .topDriver(RFToolsUtilityTOPDriver.DRIVER)
                .info(key("message.rftoolsutility.shiftmessage"))
                .infoShift(header())
                .tileEntitySupplier(DigitTileEntity::new));
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tagCompound) {
        CompoundTag infoTag = getOrCreateInfo(tagCompound);
        infoTag.putByte("powered", (byte) powerLevel);
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tagCompound) {
        CompoundTag infoTag = tagCompound.getCompound("Info");
        if (infoTag.contains("powered")) {
            powerLevel = infoTag.getByte("powered");
        }
    }

    @Override
    public void checkRedstone(Level world, BlockPos pos) {
        support.checkRedstone(this, world, pos);
    }

    @Override
    public void setPowerInput(int powered) {
        if (powerLevel != powered) {
            powerLevel = powered;
            markDirtyClient();
        }
    }
}
