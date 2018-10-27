package com.xray.common.reference;

import java.util.*;

public class BlockStore {

    public HashMap<String, Deque<BlockData>> store = new HashMap<>();

    // This is used to avoid having to scan the BlockData used in this.store.list<BlockData>
    public List<String> defaultStore = new ArrayList<>();

    public boolean putBlock(String key, BlockData data) {
        if( this.defaultStore.contains(key) )
            return false;

        // Start a list if we're the first instance
        if( !this.store.containsKey(key) ) {
            Deque<BlockData> list = new LinkedList<>();
            list.add(data);
            this.store.put(key, list);
        }
        else {
            // If we're adding a default it's possible that the list already contains alternative
            // Versions of the same block. if this happens the render will pick the 0'th index
            // and use that version of the block to render. To solve this we simply insert to the
            // start of the list :D
            if( data.isDefault() )
                this.store.get(key).addFirst(data);
            else
                this.store.get(key).add(data);
        }
        if( data.isDefault() )
            this.defaultStore.add(key);

        return true;
    }

    public boolean defaultContains(String key) {
        return this.defaultStore.contains(key);
    }

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
