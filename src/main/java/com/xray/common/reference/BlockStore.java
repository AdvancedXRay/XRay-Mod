package com.xray.common.reference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BlockStore {

    public HashMap<String, List<BlockData>> store = new HashMap<>();

    // This is used to avoid having to scan the BlockData used in this.store.list<BlockData>
    private List<String> defaultStore = new ArrayList<>();

    public boolean putBlock(String key, BlockData data) {
        if( this.defaultStore.contains(key) )
            return false;

        if( !this.store.containsKey(key) ) {
            List<BlockData> list = new ArrayList<>();
            list.add(data);
            this.store.put(key, list);
        }
        else
            this.store.get(key).add(data);

        if( data.isDefault() )
            this.defaultStore.add(key);

        return true;
    }

    public boolean defaultContains(String key) {
        return this.defaultStore.contains(key);
    }
}
