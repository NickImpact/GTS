package me.nickimpact.gts.signview;

import com.flowpowered.math.vector.Vector3d;
import com.ichorpowered.protocolcontrol.channel.ChannelProfile;
import com.ichorpowered.protocolcontrol.packet.PacketDirection;
import com.ichorpowered.protocolcontrol.packet.PacketRemapper;
import com.ichorpowered.protocolcontrol.service.ProtocolService;
import me.nickimpact.gts.api.query.SignQuery;
import me.nickimpact.gts.api.query.SignSubmission;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketSignEditorOpen;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.BlockPos;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class SpongeSignQuery implements SignQuery<Text, Player> {

    private final List<Text> lines;
    private final Vector3d position;
    private final boolean reopen;
    private final SignSubmission callback;

    private SpongeSignQuery(SpongeSignQueryBuilder builder) {
        this.lines = builder.lines;
        this.position = builder.position;
        this.reopen = builder.reopen;
        this.callback = builder.callback;
    }

    @Override
    public List<Text> getText() {
        return this.lines;
    }

    @Override
    public Vector3d getSignPosition() {
        return this.position;
    }

    @Override
    public boolean shouldReopenOnFailure() {
        return this.reopen;
    }

    @Override
    public SignSubmission getSubmissionHandler() {
        return this.callback;
    }

    @Override
    public void sendTo(Player player) {
        BlockState sign = BlockTypes.STANDING_SIGN.getDefaultState();
        player.sendBlockChange(this.position.toInt(), sign);

        ProtocolService service = Sponge.getServiceManager().provideUnchecked(ProtocolService.class);
        final ChannelProfile profile = Objects.requireNonNull(service.channels().profile(player.getUniqueId()));

        try {
            final PacketRemapper.Wrapped<SPacketSignEditorOpen> view = service.remapper().wrap(new SPacketSignEditorOpen());
            final PacketRemapper.Wrapped<SPacketUpdateTileEntity> update = service.remapper().wrap(new SPacketUpdateTileEntity());

            BlockPos position = new BlockPos(this.position.getX(), this.position.getY(), this.position.getZ());
            view.set(BlockPos.class, 0, position);

            NBTTagCompound data = new NBTTagCompound();
            data.setInteger("x", position.getX());
            data.setInteger("y", position.getY());
            data.setInteger("z", position.getZ());
            data.setString("id", "minecraft:sign");

            IntStream.rangeClosed(1, 4).forEach(line -> data.setString(
                    "Text" + line,
                    this.getText().size() >= line ? String.format(TEXT_FORMAT, TextSerializers.LEGACY_FORMATTING_CODE.serialize(this.getText().get(line - 1))) : " "
            ));

            update.set(BlockPos.class, 0, position);
            update.set(int.class, 0, 9);
            update.set(NBTTagCompound.class, 0, data);

            profile.send(PacketDirection.OUTGOING, update.packet());
            profile.send(PacketDirection.OUTGOING, view.packet());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static class SpongeSignQueryBuilder implements SignQueryBuilder<Text, Player> {

        private List<Text> lines;
        private Vector3d position;
        private boolean reopen;
        private SignSubmission callback;

        @Override
        public SignQueryBuilder<Text, Player> text(List<Text> text) {
            this.lines = text;
            return this;
        }

        @Override
        public SignQueryBuilder<Text, Player> position(Vector3d position) {
            this.position = position;
            return this;
        }

        @Override
        public SignQueryBuilder<Text, Player> reopenOnFailure(boolean state) {
            this.reopen = state;
            return this;
        }

        @Override
        public SignQueryBuilder<Text, Player> response(SignSubmission response) {
            this.callback = response;
            return this;
        }

        @Override
        public SignQueryBuilder<Text, Player> from(SignQuery<Text, Player> query) {
            this.lines = query.getText();
            this.position = query.getSignPosition();
            this.reopen = query.shouldReopenOnFailure();
            this.callback = query.getSubmissionHandler();
            return this;
        }

        @Override
        public SignQuery<Text, Player> build() {
            return new SpongeSignQuery(this);
        }
    }
}
