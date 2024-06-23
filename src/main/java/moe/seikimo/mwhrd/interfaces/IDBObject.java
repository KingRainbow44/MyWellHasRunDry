package moe.seikimo.mwhrd.interfaces;

import moe.seikimo.data.DatabaseObject;

public interface IDBObject<T extends DatabaseObject<T>> {
    T mwhrd$getData();

    void mwhrd$loadData();

}
