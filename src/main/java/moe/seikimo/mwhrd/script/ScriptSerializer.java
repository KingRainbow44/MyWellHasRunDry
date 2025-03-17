package moe.seikimo.mwhrd.script;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import com.esotericsoftware.reflectasm.MethodAccess;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaTable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public interface ScriptSerializer {
    Map<Class<?>, MethodAccess> methods = new ConcurrentHashMap<>();
    Map<Class<?>, ConstructorAccess<?>> constructors = new ConcurrentHashMap<>();
    Map<Class<?>, Map<String, FieldData>> fields = new ConcurrentHashMap<>();

    /**
     * Stores reflection data about fields in memory.
     */
    record FieldData(
        String name,
        String setter,
        int index,
        Class<?> type,
        @Nullable Field field
    ) { }

    /**
     * Serializes a Lua value to a Java List.
     *
     * @param obj The Lua value to serialize.
     * @param type The type of the value to serialize.
     * @return A List containing the serialized value.
     */
    static <T> List<T> toList(Object obj, Class<T> type) {
        var list = new ArrayList<T>();
        if (!(obj instanceof LuaTable table)) return list;

        try {
            var keys = table.keys();
            for (var k : keys) {
                try {
                    var keyValue = table.get(k);

                    T object;
                    if (keyValue.istable()) {
                        object = ScriptSerializer.serialize(type, null, keyValue.checktable());
                    } else if (keyValue.isint()) {
                        object = (T) (Integer) keyValue.toint();
                    } else if (keyValue.isnumber()) {
                        object = (T) (Float) keyValue.tofloat();
                    } else if (keyValue.isstring()) {
                        object = (T) keyValue.tojstring();
                    } else if (keyValue.isboolean()) {
                        object = (T) (Boolean) keyValue.toboolean();
                    } else {
                        object = (T) keyValue;
                    }

                    if (object != null) {
                        list.add(object);
                    }
                } catch (Exception ignored) {

                }
            }
        } catch (Exception ignored) {

        }

        return list;
    }

    /**
     * Serializes a Lua value to a Java object.
     *
     * @param obj The Lua value to serialize.
     * @param type The type of the value to serialize.
     * @return The serialized value.
     */
    static <T> T toObject(Object obj, Class<T> type) {
        return ScriptSerializer.serialize(type, null, (LuaTable) obj);
    }

    /**
     * Serializes a Lua value to a Java Map.
     *
     * @param obj The Lua value to serialize.
     * @param type The type of the value to serialize.
     * @return A Map containing the serialized value.
     */
    static <T> Map<String, T> toMap(Object obj, Class<T> type) {
        Map<String, T> map = new HashMap<>();
        if (!(obj instanceof LuaTable table)) return map;

        try {
            var keys = table.keys();
            for (var k : keys) {
                try {
                    var keyValue = table.get(k);

                    T object; if (keyValue.istable()) {
                        object = serialize(type, null, keyValue.checktable());
                    } else if (keyValue.isint()) {
                        object = (T) (Integer) keyValue.toint();
                    } else if (keyValue.isnumber()) {
                        object = (T) (Float) keyValue.tofloat(); // terrible...
                    } else if (keyValue.isstring()) {
                        object = (T) keyValue.tojstring();
                    } else if (keyValue.isboolean()) {
                        object = (T) (Boolean) keyValue.toboolean();
                    } else {
                        object = (T) keyValue;
                    }

                    if (object != null) {
                        map.put(String.valueOf(k), object);
                    }
                } catch (Exception ignored) { }
            }
        } catch (Exception ignored) { }

        return map;
    }

    /**
     * Fetches the List type.
     *
     * @param type The type to fetch the List type from.
     * @param field The field to fetch the List type from.
     * @return The List type.
     */
    private static Class<?> getListType(Class<?> type, @Nullable Field field) {
        if (field == null) {
            return type.getTypeParameters()[0].getClass();
        }

        var fieldType = field.getGenericType();
        if (fieldType instanceof ParameterizedType paramType) {
            return (Class<?>) paramType.getActualTypeArguments()[0];
        }

        return null;
    }

    /**
     * Serializes a Lua value to a Java value.
     *
     * @param type The type of the value to serialize.
     * @param field The field to serialize.
     * @param table The Lua value to serialize.
     * @return The serialized value.
     */
    private static <T> T serialize(Class<T> type, @Nullable Field field, LuaTable table) {
        T object;

        if (type == List.class) {
            try {
                var listType = ScriptSerializer.getListType(type, field);
                return (T) ScriptSerializer.toList(table, listType);
            } catch (Exception ignored) {
                return null;
            }
        }

        if (!methods.containsKey(type)) {
            ScriptSerializer.cacheType(type);
        }

        var methodAccess = methods.get(type);
        var fieldMetaMap = fields.get(type);

        object = (T) constructors.get(type).newInstance();
        if (table == null) {
            return object;
        }

        var keys = table.keys();
        for (var k : keys) {
            var keyName = k.checkjstring();
            if (!fieldMetaMap.containsKey(keyName)) {
                continue;
            }

            var fieldMeta = fieldMetaMap.get(keyName);
            var keyValue = table.get(k);

            if (keyValue.istable()) {
                methodAccess.invoke(
                    object,
                    fieldMeta.index,
                    serialize(fieldMeta.type(), fieldMeta.field(), keyValue.checktable()));
            } else if (fieldMeta.type().equals(float.class)) {
                methodAccess.invoke(object, fieldMeta.index, keyValue.tofloat());
            } else if (fieldMeta.type().equals(int.class)) {
                methodAccess.invoke(object, fieldMeta.index, keyValue.toint());
            } else if (fieldMeta.type().equals(String.class)) {
                methodAccess.invoke(object, fieldMeta.index, keyValue.tojstring());
            } else if (fieldMeta.type().equals(boolean.class)) {
                methodAccess.invoke(object, fieldMeta.index, keyValue.toboolean());
            } else {
                methodAccess.invoke(object, fieldMeta.index, keyValue.tojstring());
            }
        }

        return object;
    }

    /**
     * Caches a type's data.
     *
     * @param type The type to cache.
     * @return The cached data.
     */
    private static <T> Map<String, FieldData> cacheType(Class<T> type) {
        if (fields.containsKey(type)) {
            return fields.get(type);
        }

        if (!constructors.containsKey(type)) {
            constructors.putIfAbsent(type, ConstructorAccess.get(type));
        }

        var methodAccess =
            Optional.ofNullable(methods.get(type)).orElse(MethodAccess.get(type));
        methods.putIfAbsent(type, methodAccess);

        var fieldMetaMap = new HashMap<String, FieldData>();
        var methodNameSet = new HashSet<>(Arrays.stream(methodAccess.getMethodNames()).toList());

        Arrays.stream(type.getDeclaredFields())
            .filter(field -> methodNameSet.contains(getSetterName(field.getName())))
            .forEach(
                field -> {
                    var setter = getSetterName(field.getName());
                    var index = methodAccess.getIndex(setter);
                    fieldMetaMap.put(
                        field.getName(),
                        new FieldData(field.getName(), setter, index, field.getType(), field));
                });

        Arrays.stream(type.getFields())
            .filter(field -> !fieldMetaMap.containsKey(field.getName()))
            .filter(field -> methodNameSet.contains(getSetterName(field.getName())))
            .forEach(
                field -> {
                    var setter = getSetterName(field.getName());
                    var index = methodAccess.getIndex(setter);
                    fieldMetaMap.put(
                        field.getName(),
                        new FieldData(field.getName(), setter, index, field.getType(), field));
                });

        fields.put(type, fieldMetaMap);
        return fieldMetaMap;
    }

    /**
     * Gets the name of a field's setter.
     *
     * @param fieldName The name of the field.
     * @return The name of the method.
     */
    private static String getSetterName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return null;
        }

        if (fieldName.length() == 1) {
            return "set" + fieldName.toUpperCase();
        }

        return "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }
}
