package moe.seikimo.mwhrd.models;

import com.google.gson.JsonObject;
import dev.morphia.annotations.*;
import lombok.Data;
import moe.seikimo.data.DatabaseObject;
import moe.seikimo.general.JObject;
import moe.seikimo.mwhrd.utils.ItemStorage;
import net.minecraft.block.entity.BeaconBlockEntity;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity("beacons")
public final class BeaconModel implements DatabaseObject<BeaconModel> {
    @Id private long blockPos;

    /**
     * This list is internal, and should not be used directly.
     */
    private List<String> items = new ArrayList<>();

    private transient BeaconBlockEntity handle;
    private transient ItemStorage itemStorage = new ItemStorage();

    @PrePersist
    public void beforeSave() {
        this.items.clear();
        this.items.addAll(this.itemStorage.serialize());
    }

    @PostLoad
    public void afterLoad() {
        this.itemStorage.deserialize(this.items);
    }

    @Override
    public JsonObject explain() {
        this.beforeSave();

        return JObject.c()
            .add("blockPos", this.getBlockPos())
            .add("itemStorage", this.getItems())
            .gson();
    }
}
