package common.services;

import common.utils.page.Page;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static common.utils.SQLUtils.*;

/**
 * Created by liubin on 15-4-3.
 */
@Repository
public class GeneralDao {

    @PersistenceContext
    EntityManager em;

    public EntityManager getEm() {
        return em;
    }

    /**
     * 使用jpql进行查询
     * @param ql jpql
     * @param page 分页对象,可选
     * @param queryParams 查询参数
     * @param <RT>
     * @return
     */
    public <RT> List<RT> query(String ql, Optional<Page<RT>> page, Map<String, Object> queryParams) {

        Query query = em.createQuery(ql);
        queryParams.forEach(query::setParameter);

        if(page.isPresent()) {

            String countQl = " select count(1) " + removeFetchInCountQl(removeSelect(removeOrderBy(ql)));
            Query countQuery = em.createQuery(countQl);
            queryParams.forEach(countQuery::setParameter);

            if(hasGroupBy(ql)) {
                List resultList = countQuery.getResultList();
                page.get().setTotalCount(resultList.size());

            } else {
                Long count = (Long)countQuery.getSingleResult();
                page.get().setTotalCount(count.intValue());

            }
            query.setFirstResult(page.get().getStart());
            query.setMaxResults(page.get().getLimit());
        }

        List<RT> results = query.getResultList();

        if(page.isPresent()) {
            page.get().setResult(results);
        }

        return results;
    }


}
