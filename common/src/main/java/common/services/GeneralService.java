package common.services;

import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by liubin on 15-4-3.
 */
@Service
public class GeneralService {

    @PersistenceContext
    EntityManager em;

    public EntityManager getEm() {
        return em;
    }
}
