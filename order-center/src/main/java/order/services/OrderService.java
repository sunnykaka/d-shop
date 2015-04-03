package order.services;

import common.utils.page.Page;
import order.constants.OrderStatus;
import order.dtos.OrderSearcher;
import order.models.Order;
import order.models.OrderItem;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liubin on 15-4-2.
 */
@Service
public class OrderService {

    @PersistenceContext
    EntityManager em;

    @Transactional(readOnly = true)
    public List<Order> findByKey(Optional<Page> page, Optional<String> orderNo,
        Optional<OrderStatus> status, Optional<DateTime> createTimeStart, Optional<DateTime> createTimeEnd) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> order = cq.from(Order.class);

        List<Predicate> predicateList = new ArrayList<>();
        if(orderNo.isPresent()) {
            predicateList.add(cb.equal(order.get("orderNo"), orderNo.get()));
        }
        if(createTimeStart.isPresent()) {
            predicateList.add(cb.greaterThanOrEqualTo(order.get("createTime"), createTimeStart.get()));
        }
        if(createTimeEnd.isPresent()) {
            predicateList.add(cb.lessThanOrEqualTo(order.get("createTime"), createTimeEnd.get()));
        }
        if(status.isPresent()) {
            predicateList.add(cb.equal(order.get("status"), status.get()));
        }

        cq.select(order).where(predicateList.toArray(new Predicate[predicateList.size()]));

        TypedQuery<Order> query = em.createQuery(cq);

        if(page.isPresent()) {
            CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
            countCq.select(cb.count(countCq.from(Order.class))).where(predicateList.toArray(new Predicate[predicateList.size()]));
            Long count = em.createQuery(countCq).getSingleResult();
            page.get().setTotalCount(count.intValue());

            query.setFirstResult(page.get().getStart());
            query.setMaxResults(page.get().getLimit());
        }

        List<Order> results = query.getResultList();

        if(page.isPresent()) {
            page.get().setResult(results);
        }

        return results;
    }

    @Transactional(readOnly = true)
    public List<Order> findByComplicateKey(Optional<Page> page, OrderSearcher orderSearcher) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> order = cq.from(Order.class);
        ListJoin<Order, OrderItem> orderItemList = order.joinList("orderItemList");

        List<Predicate> predicateList = new ArrayList<>();
        if(orderSearcher.orderNo != null) {
            predicateList.add(cb.equal(order.get("orderNo"), orderSearcher.orderNo));
        }
        if(orderSearcher.createTimeStart != null) {
            predicateList.add(cb.greaterThanOrEqualTo(order.get("createTime"), orderSearcher.createTimeStart));
        }
        if(orderSearcher.createTimeEnd != null) {
            predicateList.add(cb.lessThanOrEqualTo(order.get("createTime"), orderSearcher.createTimeEnd));
        }
        if(orderSearcher.status != null) {
            predicateList.add(cb.equal(order.get("status"), orderSearcher.status));
        }
        if(orderSearcher.type != null) {
            predicateList.add(cb.equal(order.get("type"), orderSearcher.type));
        }
        if(orderSearcher.orderItemStatus != null) {
            predicateList.add(cb.equal(orderItemList.get("status"), orderSearcher.orderItemStatus));
        }
        if(!StringUtils.isBlank(orderSearcher.productSku)) {
            predicateList.add(cb.equal(orderItemList.get("productSku"), orderSearcher.productSku));
        }

        cq.select(order).where(predicateList.toArray(new Predicate[predicateList.size()]));

        TypedQuery<Order> query = em.createQuery(cq);

        if(page.isPresent()) {
            CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
            Root<Order> countOrder = countCq.from(Order.class);
            countOrder.joinList("orderItemList");
            countCq.select(cb.count(countOrder)).where(predicateList.toArray(new Predicate[predicateList.size()]));
            Long count = em.createQuery(countCq).getSingleResult();
            page.get().setTotalCount(count.intValue());

            query.setFirstResult(page.get().getStart());
            query.setMaxResults(page.get().getLimit());
        }

        List<Order> results = query.getResultList();

        if(page.isPresent()) {
            page.get().setResult(results);
        }

        return results;
    }

    @Transactional(readOnly = true)
    public List<Order> findByComplicateKeyWithJpql(Optional<Page> page, OrderSearcher orderSearcher) {

        String jpql = "select o from Order o join o.orderItemList oi where 1=1 ";
        Map<String, Object> queryParams = new HashMap<>();

        if(orderSearcher.orderNo != null) {
            jpql += " and o.order = :orderNo ";
            queryParams.put("orderNo", orderSearcher.orderNo);
        }
        if(orderSearcher.createTimeStart != null) {
            jpql += " and o.createTime >= :createTimeStart ";
            queryParams.put("createTimeStart", orderSearcher.createTimeStart);
        }
        if(orderSearcher.createTimeEnd != null) {
            jpql += " and o.createTime <= :createTimeEnd ";
            queryParams.put("createTimeEnd", orderSearcher.createTimeEnd);
        }
        if(orderSearcher.status != null) {
            jpql += " and o.status = :status ";
            queryParams.put("status", orderSearcher.status);
        }
        if(orderSearcher.type != null) {
            jpql += " and o.type = :type ";
            queryParams.put("type", orderSearcher.type);
        }
        if(orderSearcher.orderItemStatus != null) {
            jpql += " and oi.type = :orderItemStatus ";
            queryParams.put("orderItemStatus", orderSearcher.orderItemStatus);
        }
        if(!StringUtils.isBlank(orderSearcher.productSku)) {
            jpql += " and oi.productSku = :productSku ";
            queryParams.put("productSku", orderSearcher.productSku);
        }

        Query query = em.createQuery(jpql);
        queryParams.forEach(query::setParameter);

        if(page.isPresent()) {

            String countJpql = " select count(1) " + removeFetchInCountQl(removeSelect(removeOrderBy(jpql)));
            Query countQuery = em.createQuery(countJpql);
            queryParams.forEach(countQuery::setParameter);
            Long count = (Long)countQuery.getSingleResult();

            page.get().setTotalCount(count.intValue());

            query.setFirstResult(page.get().getStart());
            query.setMaxResults(page.get().getLimit());
        }

        List<Order> results = query.getResultList();

        if(page.isPresent()) {
            page.get().setResult(results);
        }

        return results;
    }

    /**
     * hql语句如果用了fetch,删除查询总条数的fetch语句,否则会报错
     * @param countQL
     * @return
     */
    private String removeFetchInCountQl(String countQL) {
        if(StringUtils.contains(countQL, " fetch ")) {
            countQL = (countQL.toString().replaceAll("fetch", ""));
        }
        return countQL;
    }

    private boolean hasGroupBy(String ql) {
        if(ql != null && !"".equals(ql)){
            if(ql.indexOf("group by") > -1){
                return true;
            }
        }
        return false;
    }

    /**
     * 去除ql语句中的select子句
     * @param ql 查询语句
     * @return 删除后的语句
     */
    private String removeSelect(String ql) {
        Assert.hasText(ql);
        int beginPos = ql.toLowerCase().indexOf("from");
        Assert.isTrue(beginPos != -1, " ql : " + ql + " must has a keyword 'from'");
        return ql.substring(beginPos);
    }

    // 删除order by字句使用的正则表达式
    private static Pattern removeOrderByPattern = Pattern.compile("order\\s*by[\\w|\\W|\\s|\\S]*", Pattern.CASE_INSENSITIVE);

    /**
     * 删除ql语句中的order by字句
     * @param ql 查询语句
     * @return 删除后的查询语句
     */
    private String removeOrderBy(String ql){
        if(ql != null && !"".equals(ql)){
            Matcher m = removeOrderByPattern.matcher(ql);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, "");
            }
            m.appendTail(sb);
            return sb.toString();
        }
        return "";
    }

}
