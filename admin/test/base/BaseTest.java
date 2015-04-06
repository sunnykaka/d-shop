package base;

import common.services.GeneralDao;
import utils.Global;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

/**
 * Created by liubin on 15-4-3.
 */
public abstract class BaseTest implements BaseTestIF {

    public <T> T doInTransaction(EntityManagerCallback<T> callback) {
        EntityManagerFactory emf = Global.ctx.getBean(EntityManagerFactory.class);
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            T result = callback.call(em);

            tx.commit();

            return result;
        }
        catch (Exception e) {
            if ( tx != null && tx.isActive() ) tx.rollback();
            throw e; // or display error message
        }
        finally {
            em.close();
        }

    }

    public <T> T doInTransactionWithGeneralDao(GeneralDaoCallback<T> callback) {
        return doInTransaction(em -> {
            GeneralDao generalDao = new GeneralDao(em);
            return callback.call(generalDao);
        });
    }


}

