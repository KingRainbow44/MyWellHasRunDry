package moe.seikimo.mwhrd.utils.items;

import dev.morphia.annotations.*;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.utils.Utils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;

import java.util.*;

/**
 * Paginated, dynamic storage for items.
 * <p>
 * Fields marked with {@link Transient} are not serialized into the database.
 */
@Embedded
public final class DynamicItemStorage {
    @Transient
    private List<List<ItemStack>> backing = Collections.synchronizedList(new ArrayList<>());

    /** This is the list to be serialized by Morphia. */
    private List<List<String>> backing$1 = new ArrayList<>();

    @Range(from = 1, to = 6)
    private int rows = 1;

    @Range(from = 1, to = 9)
    private int columns = 1;

    /**
     * This constructor is to be used by Morphia.
     */
    @ApiStatus.Internal
    private DynamicItemStorage() {
        // Empty constructor for Morphia.
    }

    /**
     * Creates an item storage with the specified rows and columns.
     *
     * @param rows The number of rows.
     * @param columns The number of columns.
     */
    public DynamicItemStorage(
        @Range(from = 1, to = 6) int rows,
        @Range(from = 1, to = 9) int columns
    ) {
        this.rows = rows;
        this.columns = columns;

        // Always allocate at least one page.
        this.allocatePage();
    }

    /**
     * This method is invoked before being serialized by Morphia.
     */
    @PrePersist
    private void beforeSave() {
        this.cleanup();

        var registry = MyWellHasRunDry
            .getServer()
            .getRegistryManager();

        // Allocate pages for the serialized list.
        this.backing$1 = new ArrayList<>();
        for (var i = 0; i < this.backing.size(); i++) {
            this.backing$1.add(new ArrayList<>());
        }

        // Serialize all pages.
        for (var i = 0; i < this.backing.size(); i++) {
            var page = this.backing.get(i);
            var serialized = new ArrayList<String>();

            // Allocate pages for the serialized list.
            var maxSize = this.rows * this.columns;
            for (var j = 0; j < maxSize; j++) {
                serialized.add("");
            }

            // Serialize all items on the page.
            for (var j = 0; j < page.size(); j++) {
                var stack = page.get(j);

                // If the stack is empty, set the value to an empty string.
                if (stack.isEmpty()) {
                    continue;
                }

                // Serialize the item stack into NBT -> Base64.
                var nbt = stack.toNbt(registry);
                var base64 = Utils.base64Encode(nbt);

                // Write the item to the list.
                serialized.set(j, base64);
            }

            // Write the items to the list.
            this.backing$1.set(i, serialized);
        }
    }

    /**
     * This method is invoked after being deserialized by Morphia.
     */
    @PostLoad
    private void onLoad() {
        var registry = MyWellHasRunDry
            .getServer()
            .getRegistryManager();

        this.backing.clear();

        // Allocate pages for the serialized list.
        var deserialized = new ArrayList<List<ItemStack>>();
        for (var i = 0; i < this.backing$1.size(); i++) {
            deserialized.add(new ArrayList<>());
        }

        // Deserialize all pages.
        for (var i = 0; i < this.backing$1.size(); i++) {
            var serialized = this.backing$1.get(i);
            var page = new ArrayList<ItemStack>();

            var maxSize = this.rows * this.columns;
            for (var j = 0; j < maxSize; j++) {
                page.add(ItemStack.EMPTY);
            }

            // Deserialize all items on the page.
            for (var j = 0; j < serialized.size(); j++) {
                var base64 = serialized.get(j);

                // Check if the item is empty.
                if (base64.isEmpty()) {
                    continue;
                }

                // Deserialize the item stack from Base64 -> NBT.
                var nbt = Utils.base64Decode(base64);
                var data = ItemStack.fromNbt(registry, nbt);

                // Write the item to the list.
                if (data.isPresent()) {
                    page.set(j, data.get());
                }
            }

            // Write the items to the list.
            deserialized.set(i, page);
        }

        this.backing = Collections.synchronizedList(deserialized);
    }

    /**
     * Removes empty item stacks from the storage.
     */
    public void cleanup() {
        this.backing.removeIf(page -> page.stream().allMatch(ItemStack::isEmpty));
    }

    /**
     * Randomly inserts item stacks into the storage.
     * See {@link #insert(Collection)} for more information.
     *
     * @param stacks The item stacks to insert.
     */
    public void insert(ItemStack... stacks) {
        this.insert(Arrays.asList(stacks));
    }

    /**
     * Randomly inserts item stacks into the storage.
     * <p>
     * Item stacks are inserted into the first available slot.
     * If a stack for the item exists, and is not full, the items get added there.
     * Items will be added to existing stacks until a new stack needs to be made.
     * Stacks are inserted into the first available page, until a new page needs to be allocated.
     *
     * @param stacks The item stacks to insert.
     */
    public void insert(Collection<ItemStack> stacks) {
        stacks.forEach(this::insert);
    }

    /**
     * Inserts a single stack into the storage.
     * See {@link #insert(Collection)} for more information.
     *
     * @param stack The item stack to insert.
     */
    public void insert(ItemStack stack) {
        var type = stack.getItem();

        var remaining = stack.getCount();
        var maxPerStack = type.getMaxCount();

        var depth = 0; // For preventing stack-overflow errors.
        ItemStack workingStack = null; // Holds a reference to the currently working stack.

        // This loop will try to run until the stack is fully depleted.
        // It can also end if there is not an existing stack to insert into.
        while (remaining > 0 && depth++ < 640) {
            // Try and find a stack to work on.
            if (workingStack == null) {
                workingStack = this.backing.stream()
                    .flatMap(Collection::stream)
                    .filter(s -> s.getItem() == type)
                    .filter(s -> s.getCount() < maxPerStack)
                    .findFirst()
                    .orElse(null);
            }

            // If the stack is still null, we will need to start allocating pages.
            // This will move us to the next part of the function.
            if (workingStack == null) {
                break;
            }

            // Reduce until we fit the stack.
            var space = maxPerStack - workingStack.getCount();
            var toAdd = Math.min(remaining, space);
            workingStack.setCount(workingStack.getCount() + toAdd);
            remaining -= toAdd;
        }

        // Once we are here, we need to allocate new stacks.

        var pageIterator = this.backing.iterator();
        var workingList = pageIterator.next();

        // This loop will run until the stack is fully depleted.
        while (remaining > 0 && workingList != null) {
            // Step 1. Check if there is an empty slot on the page.
            var emptySlot = workingList.indexOf(ItemStack.EMPTY);
            if (emptySlot == -1) {
                // Move on to the next page.
                if (pageIterator.hasNext()) {
                    workingList = pageIterator.next();
                } else {
                    // Move to allocating a new page.
                    workingList = null;
                }

                continue;
            }

            // Step 2. Allocate a new stack.
            var newStack = stack.copy();
            newStack.setCount(Math.min(remaining, maxPerStack));
            workingList.set(emptySlot, newStack);
            remaining -= newStack.getCount();
        }

        // If we still have items left, we need to allocate a new page.
        if (remaining > 0) {
            // Check the remaining stack size.
            if (remaining >= maxPerStack) {
                throw new IllegalArgumentException("Cannot insert more items than a single stack can hold.");
            }

            var page = this.allocatePage();

            var newStack = stack.copy();
            newStack.setCount(remaining);

            page.set(0, newStack);
        }
    }

    /**
     * Removes an item stack from the storage.
     *
     * @param page The page to remove the item from. (0-indexed)
     * @param index The index of the item to remove.
     * @return The item stack removed from the storage.
     */
    public ItemStack remove(int page, int index) {
        // Check if the page exists.
        if (this.backing.size() <= page) {
            throw new IndexOutOfBoundsException("Page does not exist.");
        }

        // Fetch the page.
        var pageBacking = this.backing.get(page);
        Objects.requireNonNull(pageBacking, "Null page encountered!");

        // Check if the index is valid.
        if (pageBacking.size() <= index) {
            throw new IndexOutOfBoundsException("Index does not exist.");
        }

        // Fetch the stack.
        var stack = pageBacking.get(index);
        Objects.requireNonNull(stack, "Null stack encountered!");

        // Remove the stack from the page.
        pageBacking.set(index, ItemStack.EMPTY);

        return stack;
    }

    /**
     * Fetches a page from the storage.
     *
     * @param page The page to fetch. (0-indexed)
     * @return The page fetched from the storage.
     */
    public List<ItemStack> get(int page) {
        return this.backing.get(page);
    }

    /**
     * Fetches a page from the storage, allocating it if it does not exist.
     *
     * @param page The page to fetch. (0-indexed)
     * @return The page fetched from the storage.
     */
    public List<ItemStack> getOrAllocate(int page) {
        while (this.backing.size() <= page) {
            this.allocatePage();
        }

        return Collections.synchronizedList(this.get(page));
    }

    /**
     * Fetches an item stack from the storage.
     *
     * @param page The page to fetch the item from. (0-indexed)
     * @param index The index of the item to fetch.
     * @return The item stack fetched from the storage.
     */
    public ItemStack get(int page, int index) {
        // Check if the page exists.
        if (this.backing.size() <= page) {
            throw new IndexOutOfBoundsException("Page does not exist.");
        }

        // Fetch the page.
        var pageBacking = this.backing.get(page);
        Objects.requireNonNull(pageBacking, "Null page encountered!");

        // Check if the index is valid.
        if (pageBacking.size() <= index) {
            throw new IndexOutOfBoundsException("Index does not exist.");
        }

        // Fetch the stack.
        var stack = pageBacking.get(index);
        Objects.requireNonNull(stack, "Null stack encountered!");

        return stack;
    }

    /**
     * Creates a new page in the storage.
     */
    public List<ItemStack> allocatePage() {
        var size = this.rows * this.columns;
        var page = new ArrayList<ItemStack>();

        // Allocate the page.
        for (var i = 0; i < size; i++) {
            page.add(ItemStack.EMPTY);
        }

        this.backing.add(page);
        return page;
    }

    /**
     * Removes all pages (and items) from the storage.
     */
    public void clear() {
        this.backing.clear();
    }

    /**
     * @return The amount of pages in the dynamic storage.
     */
    public int size() {
        return this.backing.size();
    }

    /**
     * @param item The item to count.
     * @return The amount of items fitting the type on all pages.
     */
    public int count(Item item) {
        return this.backing.stream()
            .mapToInt(page -> page.stream()
                .filter(stack -> stack.getItem() == item)
                .mapToInt(ItemStack::getCount)
                .sum()
            )
            .sum();
    }

    /**
     * This method does not include air items in the count.
     *
     * @return The amount of items in the dynamic storage.
     */
    public long count() {
        return this.backing.stream()
            .mapToLong(page -> page.stream()
                .filter(stack -> !stack.isEmpty())
                .count()
            )
            .sum();
    }

    @Override
    public String toString() {
        var builder = new StringBuilder("------START STORAGE------\n");

        for (var pageIndex = 0; pageIndex < this.size(); pageIndex++) {
            // Add page header.
            builder
                .append("Page ")
                .append(pageIndex + 1)
                .append(":\n");

            // Add each item (and quantity) on the page.
            for (var stack : this.backing.get(pageIndex)) {
                var name = stack.getItemName().getString();

                builder
                    .append("  ")
                    .append(name)
                    .append(" x")
                    .append(stack.getCount())
                    .append("\n");
            }
        }

        builder.append("------END STORAGE------");
        return builder.toString();
    }
}
