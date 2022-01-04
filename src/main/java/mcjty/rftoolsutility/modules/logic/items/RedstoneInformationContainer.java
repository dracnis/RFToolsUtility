package mcjty.rftoolsutility.modules.logic.items;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.rftoolsbase.modules.tablet.items.TabletItem;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.logic.network.PacketSendRedstoneData;
import mcjty.rftoolsutility.modules.logic.tools.RedstoneChannels;
import mcjty.rftoolsutility.setup.RFToolsUtilityMessages;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RedstoneInformationContainer extends GenericContainer {

	public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(0));

	private Player player;
	private Level world;
	private Map<Integer, Pair<String, Integer>> values = null;

	// Called client-side when data is received from server
	public void sendData(Map<Integer, Pair<String, Integer>> channelData) {
		values = channelData;
	}

	public Map<Integer, Pair<String, Integer>> getChannelData() {
		return values;
	}

	public static ItemStack getRedstoneInformationItem(Player player) {
		ItemStack tabletItem = player.getItemInHand(TabletItem.getHand(player));
		if (tabletItem.getItem() instanceof RedstoneInformationItem) {
			// We don't have a tablet but are directly using the redstone information item
			return tabletItem;
		}
		return TabletItem.getContainingItem(tabletItem, TabletItem.getCurrentSlot(tabletItem));
	}

	public RedstoneInformationContainer(int id, BlockPos pos, Player player) {
		super(LogicBlockModule.CONTAINER_REDSTONE_INFORMATION.get(), id, CONTAINER_FACTORY.get(), pos, null, player);
		this.player = player;
		world = player.getCommandSenderWorld();
	}

	@Override
	public void setupInventories(IItemHandler itemHandler, Inventory inventory) {
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		boolean dirty = false;
		RedstoneChannels redstoneChannels = RedstoneChannels.getChannels(world);

		ItemStack infoItem = getRedstoneInformationItem(player);
		Set<Integer> channels = RedstoneInformationItem.getChannels(infoItem);

		if (values == null || values.size() != channels.size()) {
			values = new HashMap<>();
			for (Integer channel : channels) {
				RedstoneChannels.RedstoneChannel c = redstoneChannels.getChannel(channel);
				if (c != null) {
					values.put(channel, Pair.of(c.getName(), c.getValue()));
				}
			}
			dirty = true;
		} else {
			for (Integer channel : channels) {
				RedstoneChannels.RedstoneChannel c = redstoneChannels.getChannel(channel);
				if (c != null) {
					if (values.get(channel).getRight() != c.getValue()) {
						values.put(channel, Pair.of(c.getName(), c.getValue()));
						dirty = true;
					}
				}
			}
		}

		if (dirty) {
			PacketSendRedstoneData message = new PacketSendRedstoneData(values);
			for (ContainerListener listener : containerListeners) {
				if (listener instanceof ServerPlayer) {
					RFToolsUtilityMessages.INSTANCE.sendTo(message, ((ServerPlayer) listener).connection.connection,
							NetworkDirection.PLAY_TO_CLIENT);
				}
			}
		}
	}
}
