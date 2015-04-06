package base;

import common.services.GeneralDao;

import javax.persistence.EntityManager;

/**
 * Created by liubin on 15/4/6.
 */
public interface BaseTestIF {

    <T> T doInTransaction(EntityManagerCallback<T> callback);

    <T> T doInTransactionWithGeneralDao(GeneralDaoCallback<T> callback);


    @FunctionalInterface
    static interface EntityManagerCallback<T> {
        T call(EntityManager em);
    }

    @FunctionalInterface
    static interface GeneralDaoCallback<T> {
        T call(GeneralDao generalDao);
    }
}
