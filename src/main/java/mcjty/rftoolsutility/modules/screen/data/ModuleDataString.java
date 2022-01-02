package mcjty.rftoolsutility.modules.screen.data;

import io.netty.buffer.ByteBuf;
import mcjty.rftoolsbase.api.screens.data.IModuleDataString;
import mcjty.rftoolsutility.RFToolsUtility;
import net.minecraft.network.FriendlyByteBuf;

public class ModuleDataString implements IModuleDataString {

    public static final String ID = RFToolsUtility.MODID + ":string";

    private final String s;

    @Override
    public String getId() {
        return ID;
    }

    public ModuleDataString(String s) {
        this.s = s;
    }

    public ModuleDataString(ByteBuf buf) {
        s = ((FriendlyByteBuf) buf).readUtf(32767);
    }

    @Override
    public String get() {
        return s;
    }

    @Override
    public void writeToBuf(FriendlyByteBuf buf) {
        buf.writeUtf(s);
    }
}
