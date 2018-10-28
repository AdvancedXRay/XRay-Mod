package com.xray.common.reference.block;

import java.util.*;

public class BlockStore {

    private HashMap<String, Deque<BlockData>> store = new HashMap<>();
    private boolean hasDrawables = false;

    // This is used to avoid having to scan the BlockData used in this.store.list<BlockData>
    public List<String> defaultStore = new ArrayList<>();

    public boolean putBlock(String key, BlockData data) {
        if (this.defaultStore.contains(key))
            return false;

        // Start a list if we're the first instance
        if (!this.store.containsKey(key)) {
            Deque<BlockData> list = new LinkedList<>();
            list.add(data);
            this.store.put(key, list);
        } else {
            // If we're adding a default it's possible that the list already contains alternative
            // versions of the same block. if this happens the render will pick the first index
            // and use that version of the block to render. To solve this we simply insert to the
            // start of the list :D
            if (data.isDefault())
                this.store.get(key).addFirst(data);
            else
                this.store.get(key).add(data);
        }
        if (data.isDefault())
            this.defaultStore.add(key);

        // Must be updated upon removal or draw toggle
        if( !this.hasDrawables && data.isDrawing() )
            this.hasDrawables = true;

        return true;
    }

    public HashMap<String, Deque<BlockData>> getStore() {
        return store;
    }

    public boolean hasDrawables() {
        return this.hasDrawables;
    }

    public void toggleDrawing( String key, BlockData block ) {
        if( !this.store.containsKey(key) )
            return;

        Deque<BlockData> data = this.store.get(key);
        if( block.isDefault() ) {
            data.getFirst().drawing = !data.getFirst().drawing;
            this.updateDrawables();
            return;
        }

        for ( BlockData d : data ) {
            if (d.getState() == block.getState()) {
                d.drawing = !d.drawing;
                this.updateDrawables();
                break; // We're done. Lets not waste time
            }
        }
    }

    /**
     * Not sure if this is the best way to do this but we need to make
     * sure that our list does actually contain some kind drawable block.
     *
     * Pos solution to this would be to add a UUID to the blockData and store
     * a list of those UUID in our store. Then we can simplify this loop dramatically.
     */
    private void updateDrawables() {
        boolean hasAtLeastOne = false;
        for(Map.Entry<String, Deque<BlockData>> store : this.store.entrySet()) {
            if( hasAtLeastOne )
                break;

            // Skip loop if there is only a single entry we care about
            if( this.defaultStore.contains(store.getKey()) ) {
                if( store.getValue().getFirst().isDrawing() )
                    hasAtLeastOne = true;
                continue;
            }

            for (BlockData data : store.getValue()) {
                if( hasAtLeastOne )
                    break;

                if( data.isDrawing() )
                    hasAtLeastOne = true;
            }
        }

        this.hasDrawables = hasAtLeastOne;
    }

    public boolean defaultContains(String key) {
        return this.defaultStore.contains(key);
    }


    /**
     * Used for debugging. Shouldn't be used in released version
     * TODO: add support for automatically disabling
     */
    public void printStore() {
        System.out.println("----==============================================----");
        System.out.println("-> [Printing Block Store]");
        for (Map.Entry<String, Deque<BlockData>> data:
                this.store.entrySet()) {

            System.out.println(String.format("-> [%s]", data.getKey()));
            for (BlockData block : data.getValue() ) {
                System.out.println(String.format("---> [%b, %s, %s, %s]", block.isDefault(), block.getState().toString(), block.getOutline().getBlue(), block.getName()));
            }
        }
        System.out.println("----==============================================----");
    }
}
