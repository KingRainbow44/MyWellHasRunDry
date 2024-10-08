package moe.seikimo.mwhrd.utils;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import moe.seikimo.general.EncodingUtils;
import moe.seikimo.general.JObject;

import java.lang.reflect.Type;
import java.util.List;

/**
 * A collection of item storages for players.
 */
@Getter
public final class PlayerStorage {
    private static final Type STRING_ARRAY = new TypeToken<List<String>>() { }.getType();

    private final ItemStorage
        armor = new ItemStorage(),
        inventory = new ItemStorage(),
        offHand = new ItemStorage();

    /**
     * Clears the backing storages.
     */
    public void clear() {
        this.armor.clear();
        this.inventory.clear();
        this.offHand.clear();
    }

    /**
     * @return All storages serialized.
     */
    public String serialize() {
        return JObject.c()
            .add("armor", this.armor.serialize())
            .add("inventory", this.inventory.serialize())
            .add("offHand", this.offHand.serialize())
            .toString();
    }

    /**
     * Performs a deserialization of the player storage.
     *
     * @param data The encoded object to deserialize.
     */
    public void deserialize(String data) {
        if (data == null) return;
        var object = EncodingUtils.jsonDecode(data, JsonObject.class);

        this.decodeBacking(object, "armor", this.armor);
        this.decodeBacking(object, "inventory", this.inventory);
        this.decodeBacking(object, "offHand", this.offHand);
    }

    /**
     * Deserializes a backing storage.
     *
     * @param obj The object to deserialize.
     * @param label The label to deserialize.
     * @param storage The storage to deserialize.
     */
    private void decodeBacking(JsonObject obj, String label, ItemStorage storage) {
        var array = obj.get(label);
        if (array instanceof JsonArray jsonArray) {
            var deserialized = EncodingUtils.<List<String>>jsonDecode(jsonArray, STRING_ARRAY);
            storage.deserialize(deserialized);
        }
    }
}
