package com.github.kusumotolab.tc2p.tools.db;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface DB<Model extends DBObject> {

  Completable connect();

  Completable close();

  <T extends Model> Completable insert(final Observable<T> object);

  <T extends Model> Completable update(final Observable<T> object);

  <T extends Model> Observable<T> fetch(final Single<Query<T>> query);

  <T extends Model> Completable delete(final Single<Query<T>> query);
}
